/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.translate;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.BreakStatement;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.ContinueStatement;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.LabeledStatement;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.ThrowStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Adds nil_chk calls where required to maintain compatibility Java's
 * NullPointerException being thrown when null is dereferenced.
 *
 * @author Keith Stanger
 */
public class NilCheckResolver extends UnitTreeVisitor {

  private static final FunctionElement NIL_CHK_ELEM =
      new FunctionElement("nil_chk", TypeUtil.ID_TYPE, null)
          .addParameters(TypeUtil.ID_TYPE)
          .setIsMacro(true);

  // Contains the set of "safe" variables that don't need nil checks. A new
  // Scope is added to the stack when entering conditionally executed code such
  // as if-statements, loops, conditional operators (&&, ||).
  private Scope scope = null;

  // These sets are used to pass down to parent nodes the set of variables that
  // are safe given that the expression is true or false.
  private Set<VariableElement> safeVarsTrue = null;
  private Set<VariableElement> safeVarsFalse = null;
  // Identifies the node from which safeVarsTrue and safeVarsFalse have been
  // assigned.
  private Expression conditionalSafeVarsNode = null;

  private static final Set<VariableElement> EMPTY_VARS = Collections.emptySet();

  public NilCheckResolver(CompilationUnit unit) {
    super(unit);
  }

  /**
   * A stack element that tracks which variables are safe and don't need a
   * nil_chk or not safe.
   */
  private static class Scope {

    private enum Kind {
      DEFAULT, LOOP_OR_SWITCH, TRY, LABELED
    }

    private final Scope next;
    private final Kind kind;
    private final String label;
    // Indicates that control flow does not continue through the end of this
    // scope because of a return, throw, break or continue.
    private boolean terminates = false;
    private final Map<VariableElement, Boolean> vars = new HashMap<>();
    // Saves unsafe vars to be applied the next time this scope becomes the top
    // of the stack.
    private Map<VariableElement, Boolean> mergedVars = null;

    private Scope(Scope next, Kind kind, String label) {
      this.next = next;
      this.kind = kind;
      this.label = label;
      // When the stack grows, the existing scope is assumed to be terminating
      // until a decendent branch merges back into it.
      if (next != null) {
        next.terminates = true;
      }
    }

    private void mergeVars(Map<VariableElement, Boolean> varsToMerge) {
      if (mergedVars == null) {
        mergedVars = new HashMap<>();
        mergedVars.putAll(varsToMerge);
        terminates = false;
        return;
      }
      // Remove any safe variables that aren't in both maps.
      Iterator<Map.Entry<VariableElement, Boolean>> iter = mergedVars.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<VariableElement, Boolean> entry = iter.next();
        if (entry.getValue()) {
          Boolean mergedValue = varsToMerge.get(entry.getKey());
          if (mergedValue == null || !mergedValue) {
            iter.remove();
          }
        }
      }
      // Add any unsafe variable from the merging map.
      for (Map.Entry<VariableElement, Boolean> entry : varsToMerge.entrySet()) {
        if (!entry.getValue()) {
          mergedVars.put(entry.getKey(), false);
        }
      }
    }

    private void mergeVars(Set<VariableElement> varsToMerge) {
      mergeVars(Maps.asMap(varsToMerge, Functions.constant(true)));
    }

    private void mergeInto(Scope targetScope, Set<VariableElement> extraVars) {
      Map<VariableElement, Boolean> vars = new HashMap<>();
      for (VariableElement var : extraVars) {
        vars.put(var, true);
      }
      Scope curScope = this;
      while (curScope != targetScope) {
        for (Map.Entry<VariableElement, Boolean> entry : curScope.vars.entrySet()) {
          if (!vars.containsKey(entry.getKey())) {
            vars.put(entry.getKey(), entry.getValue());
          }
        }
        curScope = curScope.next;
      }
      targetScope.mergeVars(vars);
    }

    private void mergeInto(Scope targetScope) {
      mergeInto(targetScope, EMPTY_VARS);
    }

    private void mergeDownAndReset() {
      if (!terminates) {
        next.mergeVars(vars);
      }
      terminates = false;
      vars.clear();
    }

    // This scope is re-entered from it's parent scope.
    private void backwardMerge() {
      if (terminates) {
        vars.clear();
        terminates = false;
      } else {
        Iterator<Map.Entry<VariableElement, Boolean>> iter = vars.entrySet().iterator();
        while (iter.hasNext()) {
          if (iter.next().getValue()) {
            iter.remove();
          }
        }
      }
    }

    private void resume() {
      if (mergedVars != null) {
        vars.putAll(mergedVars);
      } else {
        assert terminates : "Resumed scope has not been merged and does not terminate";
      }
      mergedVars = null;
    }
  }

  private void pushScope() {
    scope = new Scope(scope, Scope.Kind.DEFAULT, null);
  }

  private void pushLoopOrSwitchScope(String label) {
    scope = new Scope(scope, Scope.Kind.LOOP_OR_SWITCH, label);
  }

  private void pushLabeledScope(String label) {
    scope = new Scope(scope, Scope.Kind.LABELED, label);
  }

  private void pushTryScope() {
    scope = new Scope(scope, Scope.Kind.TRY, null);
  }

  private void pushFirstScope() {
    assert scope == null;
    scope = new Scope(null, Scope.Kind.DEFAULT, null);
  }

  private void popLastScope() {
    scope = scope.next;
    assert scope == null;
  }

  private void popWithoutMerge() {
    scope = scope.next;
    if (scope != null) {
      scope.resume();
    }
  }

  private void popAndMerge() {
    Scope next = scope.next;
    if (next != null) {
      // Merge vars down the stack.
      if (!scope.terminates) {
        next.mergeVars(scope.vars);
      }
    }
    popWithoutMerge();
  }

  private void setConditionalSafeVars(
      Expression node, Set<VariableElement> newSafeVarsTrue,
      Set<VariableElement> newSafeVarsFalse) {
    conditionalSafeVarsNode = node;
    safeVarsTrue = newSafeVarsTrue;
    safeVarsFalse = newSafeVarsFalse;
  }

  private Set<VariableElement> getSafeVarsTrue(Expression expr) {
    if (expr == conditionalSafeVarsNode) {
      return safeVarsTrue;
    }
    return EMPTY_VARS;
  }

  private Set<VariableElement> getSafeVarsFalse(Expression expr) {
    if (expr == conditionalSafeVarsNode) {
      return safeVarsFalse;
    }
    return EMPTY_VARS;
  }

  private void addSafeVar(VariableElement var) {
    if (scope != null) {
      scope.vars.put(var, true);
    }
  }

  private void addSafeVars(Set<VariableElement> vars) {
    if (scope != null && vars != null) {
      for (VariableElement var : vars) {
        scope.vars.put(var, true);
      }
    }
  }

  private void removeSafeVar(VariableElement var) {
    if (scope != null) {
      scope.vars.put(var, false);
    }
  }

  private void removeNonFinalFields() {
    if (scope == null) {
      return;
    }
    Scope curScope = scope;
    while (curScope != null) {
      for (VariableElement var : curScope.vars.keySet()) {
        if (var.getKind().isField() && !ElementUtil.isFinal(var)) {
          scope.vars.put(var, false);
        }
      }
      curScope = curScope.next;
    }
  }

  // If a statement throws, or might throw, then we must merge into the parent
  // scope of each try block.
  private void handleThrows() {
    Scope curScope = scope;
    while (curScope != null) {
      if (curScope.kind == Scope.Kind.TRY) {
        scope.mergeInto(curScope.next);
      }
      curScope = curScope.next;
    }
  }

  private boolean isSafeVar(VariableElement var) {
    Scope curScope = scope;
    while (curScope != null) {
      Boolean result = curScope.vars.get(var);
      if (result != null) {
        return result;
      }
      curScope = curScope.next;
    }
    return false;
  }

  private String getStatementLabel(Statement stmt) {
    TreeNode parent = stmt.getParent();
    if (parent instanceof LabeledStatement) {
      return ((LabeledStatement) parent).getLabel().getIdentifier();
    }
    return null;
  }

  private Scope findScope(Scope.Kind kind, String label) {
    Scope curScope = scope;
    while (curScope != null) {
      if (curScope.kind == kind && (label == null || label.equals(curScope.label))) {
        return curScope;
      }
      curScope = curScope.next;
    }
    return null;
  }

  // Checks if the given method is a primitive boxing or unboxing method.
  private boolean isBoxingMethod(ExecutableElement method) {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
    // Autoboxing methods.
    if (typeUtil.isBoxedType(declaringClass.asType())) {
      String name = ElementUtil.getName(method);
      TypeMirror returnType = method.getReturnType();
      List<? extends VariableElement> params = method.getParameters();
      if (name.equals("valueOf") && params.size() == 1
          && params.get(0).asType().getKind().isPrimitive()) {
        return true;
      }
      if (params.isEmpty() && returnType.getKind().isPrimitive()
          && name.equals(TypeUtil.getName(returnType) + "Value")) {
        return true;
      }
    }
    return false;
  }

  private boolean needsNilCheck(Expression e) {
    VariableElement sym = TreeUtil.getVariableElement(e);
    if (sym != null) {
      return !ElementUtil.isNonnull(sym) && (ElementUtil.isVolatile(sym) || !isSafeVar(sym));
    }
    ExecutableElement method = TreeUtil.getExecutableElement(e);
    if (method != null) {
      // Check for some common cases where the result is known not to be null.
      return !ElementUtil.isConstructor(method) && !ElementUtil.getName(method).equals("getClass")
          && !isBoxingMethod(method);
    }
    switch (e.getKind()) {
      case CAST_EXPRESSION:
        return needsNilCheck(((CastExpression) e).getExpression());
      case PARENTHESIZED_EXPRESSION:
        return needsNilCheck(((ParenthesizedExpression) e).getExpression());
      case ARRAY_ACCESS:
      case NULL_LITERAL:
      case PREFIX_EXPRESSION:
        return true;
      default:
        return false;
    }
  }

  private void addNilCheck(Expression node) {
    if (!needsNilCheck(node)) {
      return;
    }
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var != null) {
      addSafeVar(var);
    }
    FunctionInvocation nilChkInvocation =
        new FunctionInvocation(NIL_CHK_ELEM, node.getTypeMirror());
    node.replaceWith(nilChkInvocation);
    nilChkInvocation.addArgument(node);
  }

  @Override
  public void endVisit(ArrayAccess node) {
    addNilCheck(node.getArray());
  }

  @Override
  public void endVisit(FieldAccess node) {
    // Static fields lookups don't dereference the object expression.
    if (!ElementUtil.isStatic(node.getVariableElement())) {
      addNilCheck(node.getExpression());
    }
  }

  @Override
  public boolean visit(MethodInvocation node) {
    Expression receiver = node.getExpression();
    if (receiver != null) {
      receiver.accept(this);
      if (!ElementUtil.isStatic(node.getExecutableElement())) {
        addNilCheck(receiver);
      }
    }
    for (Expression arg : node.getArguments()) {
      arg.accept(this);
    }
    if (!isBoxingMethod(node.getExecutableElement())) {
      removeNonFinalFields();
      handleThrows();
    }
    return false;
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    removeNonFinalFields();
    handleThrows();
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    Expression outerTarget = node.getExpression();
    if (outerTarget != null) {
      outerTarget.accept(this);
      addNilCheck(outerTarget);
    }
    for (Expression arg : node.getArguments()) {
      arg.accept(this);
    }
    // Don't need to visit AnonymousClassDeclaration child because it's removed by
    // AnonymousClassConverter.
    removeNonFinalFields();
    handleThrows();
    return false;
  }

  @Override
  public boolean visit(SuperConstructorInvocation node) {
    Expression outerTarget = node.getExpression();
    if (outerTarget != null) {
      outerTarget.accept(this);
      addNilCheck(outerTarget);
    }
    for (Expression arg : node.getArguments()) {
      arg.accept(this);
    }
    removeNonFinalFields();
    handleThrows();
    return false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    Block body = node.getBody();
    if (body != null) {
      pushFirstScope();
      body.accept(this);
      popLastScope();
    }
    return false;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    // Don't visit fields. Any non-constant initialization will have been moved into the constructor
    // by InitializationNormalizer.
    return false;
  }

  private boolean visitType(AbstractTypeDeclaration node) {
    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      decl.accept(this);
    }
    pushFirstScope();
    for (Statement stmt : node.getClassInitStatements()) {
      stmt.accept(this);
    }
    popLastScope();
    return false;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return visitType(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return visitType(node);
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    return visitType(node);
  }

  @Override
  public boolean visit(IfStatement node) {
    return handleConditional(
        node.getExpression(), node.getThenStatement(), node.getElseStatement());
  }

  @Override
  public boolean visit(ConditionalExpression node) {
    return handleConditional(
        node.getExpression(), node.getThenExpression(), node.getElseExpression());
  }

  private boolean handleConditional(Expression expr, TreeNode thenNode, TreeNode elseNode) {
    expr.accept(this);
    Set<VariableElement> safeVarsThen = getSafeVarsTrue(expr);
    Set<VariableElement> safeVarsElse = getSafeVarsFalse(expr);
    Scope originalScope = scope;
    pushScope();
    addSafeVars(safeVarsThen);
    thenNode.accept(this);
    if (elseNode == null) {
      originalScope.mergeVars(safeVarsElse);
    } else {
      scope.mergeDownAndReset();
      addSafeVars(safeVarsElse);
      elseNode.accept(this);
    }
    popAndMerge();
    return false;
  }

  @Override
  public boolean visit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    boolean logicalAnd = op == InfixExpression.Operator.CONDITIONAL_AND;
    boolean logicalOr = op == InfixExpression.Operator.CONDITIONAL_OR;
    if (logicalAnd || logicalOr) {
      return handleConditionalOperator(node, logicalAnd);
    }
    boolean equals = op == InfixExpression.Operator.EQUALS;
    boolean notEquals = op == InfixExpression.Operator.NOT_EQUALS;
    if (equals || notEquals) {
      Expression lhs = node.getOperand(0);
      Expression rhs = node.getOperand(1);
      VariableElement maybeNullVar = null;
      if (lhs instanceof NullLiteral) {
        maybeNullVar = TreeUtil.getVariableElement(rhs);
      } else if (rhs instanceof NullLiteral) {
        maybeNullVar = TreeUtil.getVariableElement(lhs);
      }
      if (maybeNullVar != null) {
        if (equals) {
          setConditionalSafeVars(node, EMPTY_VARS, Collections.singleton(maybeNullVar));
        } else {
          setConditionalSafeVars(node, Collections.singleton(maybeNullVar), EMPTY_VARS);
        }
      }
    }
    return true;
  }

  private boolean handleConditionalOperator(InfixExpression node, boolean logicalAnd) {
    Set<VariableElement> newSafeVars = new HashSet<>();
    int pushCount = 0;
    for (Iterator<Expression> it = node.getOperands().iterator(); it.hasNext(); ) {
      Expression operand = it.next();
      operand.accept(this);
      Set<VariableElement> safeVarsForBranch =
          logicalAnd ? getSafeVarsTrue(operand) : getSafeVarsFalse(operand);
      Set<VariableElement> safeVarsForMerge =
          logicalAnd ? getSafeVarsFalse(operand) : getSafeVarsTrue(operand);
      newSafeVars.addAll(safeVarsForBranch);
      if (it.hasNext()) {
        pushScope();
        addSafeVars(safeVarsForBranch);
        scope.next.mergeVars(safeVarsForMerge);
        pushCount++;
      }
    }
    while (pushCount-- > 0) {
      popAndMerge();
    }
    setConditionalSafeVars(
        node, logicalAnd ? newSafeVars : EMPTY_VARS, logicalAnd ? EMPTY_VARS : newSafeVars);
    return false;
  }

  private void handleAssignment(VariableElement var, Expression value) {
    if (needsNilCheck(value)) {
      removeSafeVar(var);
    } else {
      addSafeVar(var);
    }
  }

  @Override
  public void endVisit(Assignment node) {
    if (node.getOperator() == Assignment.Operator.ASSIGN) {
      VariableElement var = TreeUtil.getVariableElement(node.getLeftHandSide());
      if (var != null) {
        handleAssignment(var, node.getRightHandSide());
      }
    }
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      handleAssignment(node.getVariableElement(), initializer);
    }
  }

  @Override
  public boolean visit(DoStatement node) {
    pushLoopOrSwitchScope(getStatementLabel(node));
    for (int i = 0; i < 2; i++) {
      pushScope();
      node.getBody().accept(this);
      popAndMerge();
      Expression expr = node.getExpression();
      expr.accept(this);
      scope.mergeInto(scope.next, getSafeVarsFalse(expr));  // Merge loop exit
      addSafeVars(getSafeVarsTrue(expr));
    }
    popWithoutMerge();
    return false;
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    node.getExpression().accept(this);
    addNilCheck(node.getExpression());
    pushLoopOrSwitchScope(getStatementLabel(node));
    scope.next.mergeVars(EMPTY_VARS);  // Merge loop exit
    for (int i = 0; i < 2; i++) {
      pushScope();
      node.getBody().accept(this);
      popAndMerge();
    }
    popWithoutMerge();
    return false;
  }

  @Override
  public boolean visit(ForStatement node) {
    for (Expression initializer : node.getInitializers()) {
      initializer.accept(this);
    }
    pushLoopOrSwitchScope(getStatementLabel(node));
    for (int i = 0; i < 2; i++) {
      Expression expr = node.getExpression();
      if (expr != null) {
        expr.accept(this);
        scope.mergeInto(scope.next, getSafeVarsFalse(expr));  // Merge loop exit
        addSafeVars(getSafeVarsTrue(expr));
      }
      pushScope();
      node.getBody().accept(this);
      popAndMerge();
      for (Expression updater : node.getUpdaters()) {
        updater.accept(this);
      }
    }
    popWithoutMerge();
    return false;
  }

  @Override
  public boolean visit(SwitchStatement node) {
    node.getExpression().accept(this);
    pushLoopOrSwitchScope(null);
    scope.next.mergeVars(EMPTY_VARS);  // Merge the case where no value is matched.
    for (Statement stmt : node.getStatements()) {
      stmt.accept(this);
    }
    popAndMerge();
    return false;
  }

  @Override
  public boolean visit(SwitchCase node) {
    scope.backwardMerge();
    return false;
  }

  @Override
  public boolean visit(TryStatement node) {
    pushTryScope();
    for (TreeNode resource : node.getResources()) {
      resource.accept(this);
    }
    node.getBody().accept(this);
    popAndMerge();
    pushScope();
    for (CatchClause catchClause : node.getCatchClauses()) {
      scope.mergeDownAndReset();
      addSafeVar(catchClause.getException().getVariableElement());
      catchClause.accept(this);
    }
    popAndMerge();
    Block finallyBlock = node.getFinally();
    if (finallyBlock != null) {
      finallyBlock.accept(this);
    }
    return false;
  }

  @Override
  public void endVisit(ThrowStatement node) {
    addNilCheck(node.getExpression());
    handleThrows();
    scope.terminates = true;
  }

  @Override
  public boolean visit(WhileStatement node) {
    pushLoopOrSwitchScope(getStatementLabel(node));
    for (int i = 0; i < 2; i++) {
      Expression expr = node.getExpression();
      expr.accept(this);
      scope.mergeInto(scope.next, getSafeVarsFalse(expr));  // Merge loop exit
      addSafeVars(getSafeVarsTrue(expr));
      pushScope();
      node.getBody().accept(this);
      popAndMerge();
    }
    popWithoutMerge();
    return false;
  }

  @Override
  public boolean visit(LabeledStatement node) {
    Statement body = node.getBody();
    if (body != null) {
      pushLabeledScope(node.getLabel().getIdentifier());
      body.accept(this);
      popAndMerge();
    }
    return false;
  }

  @Override
  public void endVisit(BreakStatement node) {
    Scope breakScope = null;
    if (node.getLabel() != null) {
      breakScope = findScope(Scope.Kind.LABELED, node.getLabel().getIdentifier());
    } else {
      breakScope = findScope(Scope.Kind.LOOP_OR_SWITCH, null);
    }
    scope.mergeInto(breakScope.next);
    scope.terminates = true;
  }

  @Override
  public void endVisit(ContinueStatement node) {
    String label = null;
    if (node.getLabel() != null) {
      label = node.getLabel().getIdentifier();
    }
    scope.mergeInto(findScope(Scope.Kind.LOOP_OR_SWITCH, label));
    scope.terminates = true;
  }

  @Override
  public void endVisit(ReturnStatement node) {
    scope.terminates = true;
  }

  // Loop nodes need to visit their body twice, so we may encounter already
  // added nil_chk's.
  @Override
  public void endVisit(FunctionInvocation node) {
    if (Objects.equals(node.getFunctionElement(), NIL_CHK_ELEM)) {
      VariableElement var = TreeUtil.getVariableElement(node.getArgument(0));
      if (var != null) {
        addSafeVar(var);
      }
    }
  }
}
