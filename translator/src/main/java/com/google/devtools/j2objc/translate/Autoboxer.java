/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.PointerTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.List;

/**
 * Adds support for boxing and unboxing numeric primitive values.
 *
 * @author Tom Ball
 */
public class Autoboxer extends ErrorReportingASTVisitor {
  private final AST ast;
  private static final String VALUE_METHOD = "Value";
  private static final String VALUEOF_METHOD = "valueOf";

  public Autoboxer(AST ast) {
    this.ast = ast;
  }

  /**
   * Convert a primitive type expression into a wrapped instance.  Each
   * wrapper class has a static valueOf factory method, so "expr" gets
   * translated to "Wrapper.valueOf(expr)".
   */
  private Expression box(Expression expr) {
    ITypeBinding wrapperBinding = Types.getWrapperType(Types.getTypeBinding(expr));
    if (wrapperBinding != null) {
      return newBoxExpression(expr, wrapperBinding);
    } else {
      return NodeCopier.copySubtree(ast, expr);
    }
  }

  private Expression boxWithType(Expression expr, ITypeBinding wrapperType) {
    if (Types.isBoxedPrimitive(wrapperType)) {
      return newBoxExpression(expr, wrapperType);
    }
    return box(expr);
  }

  private Expression newBoxExpression(Expression expr, ITypeBinding wrapperType) {
    ITypeBinding primitiveType = Types.getPrimitiveType(wrapperType);
    assert primitiveType != null;
    IMethodBinding wrapperMethod = BindingUtil.findDeclaredMethod(
        wrapperType, VALUEOF_METHOD, primitiveType.getName());
    assert wrapperMethod != null : "could not find valueOf method for " + wrapperType;
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, wrapperMethod, ASTFactory.newSimpleName(ast, wrapperType));
    ASTUtil.getArguments(invocation).add(NodeCopier.copySubtree(ast, expr));
    return invocation;
  }

  private ITypeBinding findWrapperSuperclass(ITypeBinding type) {
    while (type != null) {
      if (Types.isBoxedPrimitive(type)) {
        return type;
      }
      type = type.getSuperclass();
    }
    return null;
  }

  /**
   * Convert a wrapper class instance to its primitive equivalent.  Each
   * wrapper class has a "classValue()" method, such as intValue() or
   * booleanValue().  This method therefore converts "expr" to
   * "expr.classValue()".
   */
  private Expression unbox(Expression expr) {
    ITypeBinding wrapperType = findWrapperSuperclass(Types.getTypeBinding(expr));
    ITypeBinding primitiveType = Types.getPrimitiveType(wrapperType);
    if (primitiveType != null) {
      IMethodBinding valueMethod = BindingUtil.findDeclaredMethod(
          wrapperType, primitiveType.getName() + VALUE_METHOD);
      assert valueMethod != null : "could not find value method for " + wrapperType;
      return ASTFactory.newMethodInvocation(ast, valueMethod, NodeCopier.copySubtree(ast, expr));
    } else {
      return NodeCopier.copySubtree(ast, expr);
    }
  }

  @Override
  public void endVisit(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    ITypeBinding lhType = Types.getTypeBinding(lhs);
    Expression rhs = node.getRightHandSide();
    ITypeBinding rhType = Types.getTypeBinding(rhs);
    Assignment.Operator op = node.getOperator();
    if (op != Assignment.Operator.ASSIGN && !lhType.isPrimitive() &&
        !lhType.equals(node.getAST().resolveWellKnownType("java.lang.String"))) {
      // Not a simple assignment; need to break the <operation>-WITH_ASSIGN
      // assignment apart.
      node.setOperator(Assignment.Operator.ASSIGN);
      node.setRightHandSide(box(newInfixExpression(lhs, rhs, op, lhType)));
    } else {
      if (lhType.isPrimitive() && !rhType.isPrimitive()) {
        node.setRightHandSide(unbox(rhs));
      } else if (!lhType.isPrimitive() && rhType.isPrimitive()) {
        node.setRightHandSide(boxWithType(rhs, lhType));
      }
    }
  }

  private InfixExpression newInfixExpression(
      Expression lhs, Expression rhs, Assignment.Operator op, ITypeBinding lhType) {
    InfixExpression newRhs = ast.newInfixExpression();
    newRhs.setLeftOperand(unbox(lhs));
    newRhs.setRightOperand(unbox(rhs));
    InfixExpression.Operator infixOp;
    // op isn't an enum, so this can't be a switch.
    if (op == Assignment.Operator.PLUS_ASSIGN) {
      infixOp = InfixExpression.Operator.PLUS;
    } else if (op == Assignment.Operator.MINUS_ASSIGN) {
      infixOp = InfixExpression.Operator.MINUS;
    } else if (op == Assignment.Operator.TIMES_ASSIGN) {
      infixOp = InfixExpression.Operator.TIMES;
    } else if (op == Assignment.Operator.DIVIDE_ASSIGN) {
      infixOp = InfixExpression.Operator.DIVIDE;
    } else if (op == Assignment.Operator.BIT_AND_ASSIGN) {
      infixOp = InfixExpression.Operator.AND;
    } else if (op == Assignment.Operator.BIT_OR_ASSIGN) {
      infixOp = InfixExpression.Operator.OR;
    } else if (op == Assignment.Operator.BIT_XOR_ASSIGN) {
      infixOp = InfixExpression.Operator.XOR;
    } else if (op == Assignment.Operator.REMAINDER_ASSIGN) {
      infixOp = InfixExpression.Operator.REMAINDER;
    } else if (op == Assignment.Operator.LEFT_SHIFT_ASSIGN) {
      infixOp = InfixExpression.Operator.LEFT_SHIFT;
    } else if (op == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN) {
      infixOp = InfixExpression.Operator.RIGHT_SHIFT_SIGNED;
    } else if (op == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
      infixOp = InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED;
    } else {
      throw new IllegalArgumentException();
    }
    newRhs.setOperator(infixOp);
    Types.addBinding(newRhs, Types.getPrimitiveType(lhType));
    return newRhs;
  }

  @Override
  public void endVisit(ArrayAccess node) {
    Expression index = node.getIndex();
    if (!Types.getTypeBinding(index).isPrimitive()) {
      node.setIndex(unbox(index));
    }
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    ITypeBinding type = Types.getTypeBinding(node).getElementType();
    List<Expression> expressions = ASTUtil.getExpressions(node);
    for (int i = 0; i < expressions.size(); i++) {
      Expression expr = expressions.get(i);
      Expression result = boxOrUnboxExpression(expr, type);
      if (expr != result) {
        expressions.set(i, result);
      }
    }
  }

  @Override
  public void endVisit(AssertStatement node) {
    Expression expression = node.getMessage();
    if (expression != null) {
      ITypeBinding exprType = Types.getTypeBinding(expression);
      if (exprType.isPrimitive()) {
        node.setMessage(box(expression));
      }
    }
  }

  @Override
  public void endVisit(CastExpression node) {
    Expression expr = boxOrUnboxExpression(node.getExpression(), Types.getTypeBinding(node));
    if (expr != node.getExpression()) {
      ASTNode parent = node.getParent();
      if (parent instanceof ParenthesizedExpression) {
        ASTUtil.setProperty(parent, expr);
      } else {
        ASTUtil.setProperty(node, expr);
      }
    }
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    convertArguments(Types.getMethodBinding(node), ASTUtil.getArguments(node));
  }

  @Override
  public void endVisit(ConditionalExpression node) {
    Expression expr = node.getExpression();
    ITypeBinding exprType = Types.getTypeBinding(expr);
    if (!exprType.isPrimitive()) {
      node.setExpression(unbox(expr));
    }

    ITypeBinding nodeType = Types.getTypeBinding(node);
    Expression thenExpr = node.getThenExpression();
    ITypeBinding thenType = Types.getTypeBinding(thenExpr);
    Expression elseExpr = node.getElseExpression();
    ITypeBinding elseType = Types.getTypeBinding(elseExpr);

    if (thenType.isPrimitive() && !nodeType.isPrimitive()) {
      node.setThenExpression(box(thenExpr));
    } else if (!thenType.isPrimitive() && nodeType.isPrimitive()) {
      node.setThenExpression(unbox(thenExpr));
    }

    if (elseType.isPrimitive() && !nodeType.isPrimitive()) {
      node.setElseExpression(box(elseExpr));
    } else if (!elseType.isPrimitive() && nodeType.isPrimitive()) {
      node.setElseExpression(unbox(elseExpr));
    }
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    convertArguments(Types.getMethodBinding(node), ASTUtil.getArguments(node));
  }

  @Override
  public void endVisit(DoStatement node) {
    Expression expression = node.getExpression();
    ITypeBinding exprType = Types.getTypeBinding(expression);
    if (!exprType.isPrimitive()) {
      node.setExpression(unbox(expression));
    }
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    convertArguments(Types.getMethodBinding(node), ASTUtil.getArguments(node));
  }

  @Override
  public void endVisit(IfStatement node) {
    Expression expr = node.getExpression();
    ITypeBinding binding = Types.getTypeBinding(expr);

    if (!binding.isPrimitive()) {
      node.setExpression(unbox(expr));
    }
  }

  @Override
  public void endVisit(InfixExpression node) {
    ITypeBinding type = Types.getTypeBinding(node);
    Expression lhs = node.getLeftOperand();
    ITypeBinding lhBinding = Types.getTypeBinding(lhs);
    Expression rhs = node.getRightOperand();
    ITypeBinding rhBinding = Types.getTypeBinding(rhs);
    InfixExpression.Operator op = node.getOperator();

    // Don't unbox for equality tests where both operands are boxed types.
    if ((op == InfixExpression.Operator.EQUALS || op == InfixExpression.Operator.NOT_EQUALS)
        && !lhBinding.isPrimitive() && !rhBinding.isPrimitive()) {
      return;
    }
    // Don't unbox for string concatenation.
    if (op == InfixExpression.Operator.PLUS && Types.isJavaStringType(type)) {
      return;
    }

    if (!lhBinding.isPrimitive()) {
      node.setLeftOperand(unbox(lhs));
    }
    if (!rhBinding.isPrimitive()) {
      node.setRightOperand(unbox(rhs));
    }
    List<Expression> extendedOperands = ASTUtil.getExtendedOperands(node);
    for (int i = 0; i < extendedOperands.size(); i++) {
      Expression expr = extendedOperands.get(i);
      if (!Types.getTypeBinding(expr).isPrimitive()) {
        extendedOperands.set(i, unbox(expr));
      }
    }
  }

  @Override
  public void endVisit(MethodInvocation node) {
    convertArguments(Types.getMethodBinding(node), ASTUtil.getArguments(node));
  }

  @Override
  public void endVisit(PrefixExpression node) {
    PrefixExpression.Operator op = node.getOperator();
    Expression operand = node.getOperand();
    if (op == PrefixExpression.Operator.INCREMENT) {
      rewriteBoxedPrefixOrPostfix(node, operand, "PreIncr");
    } else if (op == PrefixExpression.Operator.DECREMENT) {
      rewriteBoxedPrefixOrPostfix(node, operand, "PreDecr");
    } else if (!Types.getTypeBinding(operand).isPrimitive()) {
      node.setOperand(unbox(operand));
    }
  }

  @Override
  public void endVisit(PostfixExpression node) {
    PostfixExpression.Operator op = node.getOperator();
    if (op == PostfixExpression.Operator.INCREMENT) {
      rewriteBoxedPrefixOrPostfix(node, node.getOperand(), "PostIncr");
    } else if (op == PostfixExpression.Operator.DECREMENT) {
      rewriteBoxedPrefixOrPostfix(node, node.getOperand(), "PostDecr");
    }
  }

  private void rewriteBoxedPrefixOrPostfix(
      ASTNode node, Expression operand, String methodPrefix) {
    ITypeBinding type = Types.getTypeBinding(operand);
    if (!Types.isBoxedPrimitive(type)) {
      return;
    }
    AST ast = node.getAST();
    String methodName = methodPrefix + NameTable.capitalize(Types.getPrimitiveType(type).getName());
    IOSMethodBinding methodBinding = IOSMethodBinding.newFunction(
        methodName, type, type, new PointerTypeBinding(type));
    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, methodBinding, null);
    ASTUtil.getArguments(invocation).add(
        ASTFactory.newAddressOf(ast, NodeCopier.copySubtree(ast, operand)));
    ASTUtil.setProperty(node, invocation);
  }

  @Override
  public void endVisit(ReturnStatement node) {
    Expression expr = node.getExpression();
    if (expr != null) {
      ASTNode n = node.getParent();
      while (!(n instanceof MethodDeclaration)) {
        n = n.getParent();
      }
      ITypeBinding returnType = Types.getMethodBinding(n).getReturnType();
      ITypeBinding exprType = Types.getTypeBinding(expr);
      if (returnType.isPrimitive() && !exprType.isPrimitive()) {
        node.setExpression(unbox(expr));
      }
      if (!returnType.isPrimitive() && exprType.isPrimitive()) {
        node.setExpression(box(expr));
      }
    }
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    convertArguments(Types.getMethodBinding(node), ASTUtil.getArguments(node));
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    convertArguments(Types.getMethodBinding(node), ASTUtil.getArguments(node));
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      ITypeBinding nodeType = Types.getTypeBinding(node);
      ITypeBinding initType = Types.getTypeBinding(initializer);
      if (nodeType.isPrimitive() && !initType.isPrimitive()) {
        node.setInitializer(unbox(initializer));
      } else if (!nodeType.isPrimitive() && initType.isPrimitive()) {
        node.setInitializer(boxWithType(initializer, nodeType));
      }
    }
  }

  @Override
  public void endVisit(WhileStatement node) {
    Expression expression = node.getExpression();
    ITypeBinding exprType = Types.getTypeBinding(expression);
    if (!exprType.isPrimitive()) {
      node.setExpression(unbox(expression));
    }
  }

  @Override
  public void endVisit(SwitchStatement node) {
    Expression expression = node.getExpression();
    ITypeBinding exprType = Types.getTypeBinding(expression);
    if (!exprType.isPrimitive()) {
      node.setExpression(unbox(expression));
    }
  }

  private void convertArguments(IMethodBinding methodBinding, List<Expression> args) {
    if (methodBinding instanceof IOSMethodBinding) {
      return; // already converted
    }
    ITypeBinding[] paramTypes = methodBinding.getParameterTypes();
    for (int i = 0; i < args.size(); i++) {
      ITypeBinding paramType;
      if (methodBinding.isVarargs() && i >= paramTypes.length - 1) {
        paramType = paramTypes[paramTypes.length - 1].getComponentType();
      } else {
        paramType = paramTypes[i];
      }
      Expression arg = args.get(i);
      Expression replacementArg = boxOrUnboxExpression(arg, paramType);
      if (replacementArg != arg) {
        args.set(i, replacementArg);
      }
    }
  }

  private Expression boxOrUnboxExpression(Expression arg, ITypeBinding argType) {
    ITypeBinding argBinding = Types.getTypeBinding(arg);
    if (argType.isPrimitive() && !argBinding.isPrimitive()) {
      return unbox(arg);
    } else if (!argType.isPrimitive() && argBinding.isPrimitive()) {
      return box(arg);
    } else {
      return arg;
    }
  }
}
