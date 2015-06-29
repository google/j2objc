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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Adds nil_chk calls where required to maintain compatibility Java's
 * NullPointerException being thrown when null is dereferenced.
 *
 * TODO(kstanger): We need to be more strict with fields. When an external call
 * such as a MethodInvocation or ConstructorInvocation is encountered it could
 * have the side-effect of re-assigning the field. Therefore when encountering
 * such nodes we need to clear all non-local variables from the set of safe
 * vars.
 *
 * @author Keith Stanger
 */
public class NilCheckResolver extends TreeVisitor {

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
    IVariableBinding sym = TreeUtil.getVariableBinding(e);
    if (sym != null) {
      // Outer class references should always be non-nil.
      return !isSafeVar(sym) && !sym.getName().startsWith("this$")
          && !sym.getName().equals("outer$");
    }
    IMethodBinding method = TreeUtil.getMethodBinding(e);
    if (method != null) {
      // Check for some common cases where the result is known not to be null.
      return !method.isConstructor() && !method.getName().equals("getClass")
          && !(typeEnv.isBoxedPrimitive(method.getDeclaringClass())
               && method.getName().equals("valueOf"));
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
    }
    return false;
  }

  private void addNilCheck(Expression node) {
    if (!needsNilCheck(node)) {
      return;
    }
    IVariableBinding var = TreeUtil.getVariableBinding(node);
    if (var != null) {
      addSafeVar(var);
      safeVarsTrue.add(var);
      safeVarsFalse.add(var);
    }
    ITypeBinding idType = typeEnv.resolveIOSType("id");
    FunctionInvocation nilChkInvocation = new FunctionInvocation(
        "nil_chk", node.getTypeBinding(), idType, idType);
    node.replaceWith(nilChkInvocation);
    nilChkInvocation.getArguments().add(node);
  }

  @Override
  public void endVisit(ArrayAccess node) {
    addNilCheck(node.getArray());
  }

  @Override
  public void endVisit(FieldAccess node) {
    // Static fields lookups don't dereference the object expression.
    if (!BindingUtil.isStatic(node.getVariableBinding())) {
      addNilCheck(node.getExpression());
    }
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding();
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

  private boolean visitType(AbstractTypeDeclaration node) {
    assert safeVarsStack.isEmpty();
    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      decl.accept(this);
    }
    pushScope();
    for (Statement stmt : node.getClassInitStatements()) {
      stmt.accept(this);
    }
    popScope();
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
      Expression lhs = node.getOperands().get(0);
      Expression rhs = node.getOperands().get(1);
      IVariableBinding maybeNullVar = null;
      if (lhs instanceof NullLiteral) {
        maybeNullVar = TreeUtil.getVariableBinding(rhs);
      } else if (rhs instanceof NullLiteral) {
        maybeNullVar = TreeUtil.getVariableBinding(lhs);
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
    int pushCount = 0;
    for (Iterator<Expression> it = node.getOperands().iterator(); it.hasNext(); ) {
      clearConditionalSafeVars();
      it.next().accept(this);
      if (logicalAnd) {
        newSafeVarsTrue.addAll(safeVarsTrue);
      } else {
        newSafeVarsFalse.addAll(safeVarsFalse);
      }
      if (it.hasNext()) {
        pushScope();
        addSafeVars(logicalAnd ? safeVarsTrue : safeVarsFalse);
        pushCount++;
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
      handleAssignment(
          TreeUtil.getVariableBinding(node.getLeftHandSide()), node.getRightHandSide());
    }
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      handleAssignment(node.getVariableBinding(), initializer);
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
    addNilCheck(node.getExpression());
    node.getExpression().accept(this);
    pushScope();
    node.getBody().accept(this);
    popScope();
    return false;
  }

  @Override
  public boolean visit(ForStatement node) {
    for (Expression initializer : node.getInitializers()) {
      initializer.accept(this);
    }
    Expression expr = node.getExpression();
    if (expr != null) {
      expr.accept(this);
    }
    pushScope();
    node.getBody().accept(this);
    for (Expression updater : node.getUpdaters()) {
      updater.accept(this);
    }
    popScope();
    return false;
  }

  @Override
  public boolean visit(SwitchStatement node) {
    node.getExpression().accept(this);
    pushScope();
    for (Statement stmt : node.getStatements()) {
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
    for (VariableDeclarationExpression resource : node.getResources()) {
      resource.accept(this);
    }
    node.getBody().accept(this);
    popScope();
    for (CatchClause catchClause : node.getCatchClauses()) {
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
