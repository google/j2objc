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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.ArrayList;
import java.util.Set;

// TODO(user): Need to add nil_chk to increment and decrement and certain
// assignment types like +=.
/**
 * Adds nil_chk calls where required to maintain compatibility Java's
 * NullPointerException being thrown when null is dereferenced.
 *
 * @author Keith Stanger
 */
public class NilCheckResolver extends ErrorReportingASTVisitor {

  private static final IOSMethodBinding NIL_CHK_DECL = IOSMethodBinding.newFunction(
      "nil_chk", Types.resolveIOSType("id"), null, Types.resolveIOSType("id"));

  // Contains the set of "safe" variables that don't need nil checks. A new
  // "scope" is added to the stack when entering conditionally executed code
  // such as if-statements, loops, conditional operators (&&, ||).
  private final ArrayList<Set<IVariableBinding>> safeVarsStack = Lists.newArrayList();

  // These sets are used to pass down to parent nodes the set of variables that
  // are safe given that the expression is true or false.
  private Set<IVariableBinding> safeVarsTrue = Sets.newHashSet();
  private Set<IVariableBinding> safeVarsFalse = Sets.newHashSet();

  private void pushScope() {
    safeVarsStack.add(Sets.<IVariableBinding>newHashSet());
  }

  private void popScope() {
    safeVarsStack.remove(safeVarsStack.size() - 1);
  }

  // A node visitor must call this before visiting a child if it wishes to use
  // the contents of safeVarsTrue or safeVarsFalse.
  private void clearConditionalSafeVars() {
    safeVarsTrue.clear();
    safeVarsFalse.clear();
  }

  private void addSafeVar(IVariableBinding var) {
    if (safeVarsStack.size() > 0) {
      safeVarsStack.get(safeVarsStack.size() - 1).add(var);
    }
  }

  private void addSafeVars(Set<IVariableBinding> vars) {
    if (safeVarsStack.size() > 0) {
      safeVarsStack.get(safeVarsStack.size() - 1).addAll(vars);
    }
  }

  private void removeSafeVar(IVariableBinding var) {
    for (Set<IVariableBinding> scope : safeVarsStack) {
      scope.remove(var);
    }
  }

  private Set<IVariableBinding> getAllSafeVars() {
    Set<IVariableBinding> safeVars = Sets.newHashSet();
    for (Set<IVariableBinding> scope : safeVarsStack) {
      safeVars.addAll(scope);
    }
    return safeVars;
  }

  private boolean isSafeVar(IVariableBinding var) {
    for (Set<IVariableBinding> scope : safeVarsStack) {
      if (scope.contains(var)) {
        return true;
      }
    }
    return false;
  }

  private boolean needsNilCheck(Expression e) {
    IVariableBinding sym = Types.getVariableBinding(e);
    if (sym != null) {
      // Outer class references should always be non-nil.
      return !isSafeVar(sym) && !sym.getName().startsWith("this$")
          && !sym.getName().equals("outer$");
    }
    IMethodBinding method = Types.getMethodBinding(e);
    if (method != null) {
      // Check for some common cases where the result is known not to be null.
      return !method.isConstructor() && !method.getName().equals("getClass")
          && !(Types.isBoxedPrimitive(method.getDeclaringClass())
               && method.getName().equals("valueOf"));
    }
    if (e instanceof ParenthesizedExpression) {
      return needsNilCheck(((ParenthesizedExpression) e).getExpression());
    }
    if (e instanceof CastExpression) {
      return needsNilCheck(((CastExpression) e).getExpression());
    }
    switch (e.getNodeType()) {
      case ASTNode.ARRAY_ACCESS:
      case ASTNode.NULL_LITERAL:
        return true;
    }
    return false;
  }

  private static Expression stripCastsAndParentheses(Expression node) {
    if (node instanceof ParenthesizedExpression) {
      return stripCastsAndParentheses(((ParenthesizedExpression) node).getExpression());
    } else if (node instanceof CastExpression) {
      return stripCastsAndParentheses(((CastExpression) node).getExpression());
    }
    return node;
  }

  private void addNilCheck(Expression node) {
    Expression strippedNode = stripCastsAndParentheses(node);
    if (!needsNilCheck(strippedNode)) {
      return;
    }
    IVariableBinding var = Types.getVariableBinding(node);
    if (var != null) {
      addSafeVar(var);
      safeVarsTrue.add(var);
      safeVarsFalse.add(var);
    }
    AST ast = node.getAST();
    IOSMethodBinding nilChkBinding = IOSMethodBinding.newTypedInvocation(
        NIL_CHK_DECL, Types.getTypeBinding(node));
    MethodInvocation nilChkInvocation = ASTFactory.newMethodInvocation(ast, nilChkBinding, null);
    ASTUtil.getArguments(nilChkInvocation).add(NodeCopier.copySubtree(ast, strippedNode));
    ASTUtil.setProperty(node, nilChkInvocation);
  }

  @Override
  public void endVisit(ArrayAccess node) {
    addNilCheck(node.getArray());
  }

  @Override
  public void endVisit(FieldAccess node) {
    addNilCheck(node.getExpression());
  }

  @Override
  public boolean visit(QualifiedName node) {
    if (!needsNilCheck(node.getQualifier())) {
      return true;
    }

    // Instance references to static fields don't need to be nil-checked.
    // This is true in Java (surprisingly), where instance.FIELD returns
    // FIELD even when instance is null.
    IVariableBinding var = Types.getVariableBinding(node);
    IVariableBinding qualifierVar = Types.getVariableBinding(node.getQualifier());
    if (var != null && qualifierVar != null && BindingUtil.isStatic(var)
        && !BindingUtil.isStatic(qualifierVar)) {
      return true;
    }

    // We can't substitute the qualifier with a nil_chk because it must have a
    // Name type, so we have to convert to a FieldAccess node.
    FieldAccess newNode = convertToFieldAccess(node);
    newNode.accept(this);
    return false;
  }

  private static FieldAccess convertToFieldAccess(QualifiedName node) {
    AST ast = node.getAST();
    ASTNode parent = node.getParent();
    if (parent instanceof QualifiedName) {
      FieldAccess newParent = convertToFieldAccess((QualifiedName) parent);
      Expression expr = newParent.getExpression();
      assert expr instanceof QualifiedName;
      node = (QualifiedName) expr;
    }
    FieldAccess newNode = ASTFactory.newFieldAccess(
        ast, Types.getVariableBinding(node), NodeCopier.copySubtree(ast, node.getQualifier()));
    ASTUtil.setProperty(node, newNode);
    return newNode;
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = Types.getMethodBinding(node);
    if (BindingUtil.isStatic(binding)) {
      return;
    }
    Expression receiver = node.getExpression();
    if (receiver != null) {
      addNilCheck(receiver);
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    assert safeVarsStack.isEmpty();
    Block body = node.getBody();
    if (body != null) {
      pushScope();
      body.accept(this);
      popScope();
    }
    return false;
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

  private boolean handleConditional(Expression expr, ASTNode thenNode, ASTNode elseNode) {
    clearConditionalSafeVars();
    expr.accept(this);
    Set<IVariableBinding> safeVarsElse = getAllSafeVars();
    safeVarsElse.addAll(safeVarsFalse);
    pushScope();
    addSafeVars(safeVarsTrue);
    thenNode.accept(this);
    popScope();
    if (elseNode != null) {
      pushScope();
      addSafeVars(safeVarsElse);
      elseNode.accept(this);
      popScope();
    }
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
      Expression lhs = node.getLeftOperand();
      Expression rhs = node.getRightOperand();
      IVariableBinding maybeNullVar = null;
      if (lhs instanceof NullLiteral) {
        maybeNullVar = Types.getVariableBinding(rhs);
      } else if (rhs instanceof NullLiteral) {
        maybeNullVar = Types.getVariableBinding(lhs);
      }
      if (maybeNullVar != null) {
        if (equals) {
          safeVarsFalse.add(maybeNullVar);
        } else {
          safeVarsTrue.add(maybeNullVar);
        }
      }
    }
    return true;
  }

  private boolean handleConditionalOperator(InfixExpression node, boolean logicalAnd) {
    Set<IVariableBinding> newSafeVarsTrue = Sets.newHashSet();
    Set<IVariableBinding> newSafeVarsFalse = Sets.newHashSet();
    clearConditionalSafeVars();
    node.getLeftOperand().accept(this);
    if (logicalAnd) {
      newSafeVarsTrue.addAll(safeVarsTrue);
    } else {
      newSafeVarsFalse.addAll(safeVarsFalse);
    }
    pushScope();
    addSafeVars(logicalAnd ? safeVarsTrue : safeVarsFalse);
    int pushCount = 1;
    clearConditionalSafeVars();
    node.getRightOperand().accept(this);
    if (logicalAnd) {
      newSafeVarsTrue.addAll(safeVarsTrue);
    } else {
      newSafeVarsFalse.addAll(safeVarsFalse);
    }
    for (Expression extendedOperand : ASTUtil.getExtendedOperands(node)) {
      pushScope();
      addSafeVars(logicalAnd ? safeVarsTrue : safeVarsFalse);
      pushCount++;
      clearConditionalSafeVars();
      extendedOperand.accept(this);
      if (logicalAnd) {
        newSafeVarsTrue.addAll(safeVarsTrue);
      } else {
        newSafeVarsFalse.addAll(safeVarsFalse);
      }
    }
    while (pushCount-- > 0) {
      popScope();
    }
    safeVarsTrue = newSafeVarsTrue;
    safeVarsFalse = newSafeVarsFalse;
    return false;
  }

  private void handleAssignment(IVariableBinding var, Expression value) {
    if (needsNilCheck(value)) {
      removeSafeVar(var);
    } else {
      addSafeVar(var);
    }
  }

  @Override
  public void endVisit(Assignment node) {
    if (node.getOperator() == Assignment.Operator.ASSIGN) {
      handleAssignment(Types.getVariableBinding(node.getLeftHandSide()), node.getRightHandSide());
    }
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      handleAssignment(Types.getVariableBinding(node), initializer);
    }
  }

  @Override
  public boolean visit(DoStatement node) {
    pushScope();
    node.getBody().accept(this);
    node.getExpression().accept(this);
    popScope();
    return false;
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    node.getExpression().accept(this);
    pushScope();
    node.getBody().accept(this);
    popScope();
    return false;
  }

  @Override
  public boolean visit(ForStatement node) {
    for (Expression initializer : ASTUtil.getInitializers(node)) {
      initializer.accept(this);
    }
    Expression expr = node.getExpression();
    if (expr != null) {
      expr.accept(this);
    }
    pushScope();
    node.getBody().accept(this);
    for (Expression updater : ASTUtil.getUpdaters(node)) {
      updater.accept(this);
    }
    popScope();
    return false;
  }

  @Override
  public boolean visit(SwitchStatement node) {
    node.getExpression().accept(this);
    pushScope();
    for (Statement stmt : ASTUtil.getStatements(node)) {
      stmt.accept(this);
    }
    popScope();
    return false;
  }

  @Override
  public boolean visit(SwitchCase node) {
    popScope();
    pushScope();
    return false;
  }

  @Override
  public boolean visit(TryStatement node) {
    pushScope();
    for (VariableDeclarationExpression resource : ASTUtil.getResources(node)) {
      resource.accept(this);
    }
    node.getBody().accept(this);
    popScope();
    for (CatchClause catchClause : ASTUtil.getCatchClauses(node)) {
      pushScope();
      catchClause.accept(this);
      popScope();
    }
    Block finallyBlock = node.getFinally();
    if (finallyBlock != null) {
      finallyBlock.accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(WhileStatement node) {
    node.getExpression().accept(this);
    pushScope();
    node.getBody().accept(this);
    popScope();
    return false;
  }
}
