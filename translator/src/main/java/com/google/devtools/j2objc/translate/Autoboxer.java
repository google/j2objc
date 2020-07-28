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

import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.AssertStatement;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.PointerType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Adds support for boxing and unboxing numeric primitive values.
 *
 * @author Tom Ball
 */
public class Autoboxer extends UnitTreeVisitor {

  private static final String VALUE_METHOD = "Value";
  private static final String VALUEOF_METHOD = "valueOf";

  public Autoboxer(CompilationUnit unit) {
    super(unit);
  }

  /**
   * Convert a primitive type expression into a wrapped instance.  Each
   * wrapper class has a static valueOf factory method, so "expr" gets
   * translated to "Wrapper.valueOf(expr)".
   */
  private void box(Expression expr) {
    boxWithClass(expr, typeUtil.boxedClass((PrimitiveType) expr.getTypeMirror()));
  }

  private void box(Expression expr, TypeMirror boxedType) {
    if (typeUtil.isBoxedType(boxedType)) {
      boxWithClass(expr, TypeUtil.asTypeElement(boxedType));
    } else {
      box(expr);
    }
  }

  private void boxWithClass(Expression expr, TypeElement boxedClass) {
    PrimitiveType primitiveType = typeUtil.unboxedType(boxedClass.asType());
    assert primitiveType != null;
    ExecutableElement wrapperMethod = ElementUtil.findMethod(
        boxedClass, VALUEOF_METHOD, TypeUtil.getQualifiedName(primitiveType));
    assert wrapperMethod != null : "could not find valueOf method for " + boxedClass;
    MethodInvocation invocation = new MethodInvocation(
        new ExecutablePair(wrapperMethod), new SimpleName(boxedClass));
    expr.replaceWith(invocation);
    invocation.addArgument(expr);
  }

  /**
   * Convert a wrapper class instance to its primitive equivalent.  Each
   * wrapper class has a "classValue()" method, such as intValue() or
   * booleanValue().  This method therefore converts "expr" to
   * "expr.classValue()".
   */
  private void unbox(Expression expr) {
    unbox(expr, null);
  }

  /**
   * Convert a wrapper class instance to a specified primitive equivalent.
   */
  private void unbox(Expression expr, PrimitiveType primitiveType) {
    TypeElement boxedClass = findBoxedSuperclass(expr.getTypeMirror());
    if (primitiveType == null && boxedClass != null) {
      primitiveType = typeUtil.unboxedType(boxedClass.asType());
    }
    if (primitiveType == null) {
      return;
    }
    ExecutableElement valueMethod = ElementUtil.findMethod(
        boxedClass, TypeUtil.getName(primitiveType) + VALUE_METHOD);
    assert valueMethod != null : "could not find value method for " + boxedClass;
    MethodInvocation invocation = new MethodInvocation(new ExecutablePair(valueMethod), null);
    expr.replaceWith(invocation);
    invocation.setExpression(expr);
  }

  private TypeElement findBoxedSuperclass(TypeMirror type) {
    while (type != null) {
      if (typeUtil.isBoxedType(type)) {
        return TypeUtil.asTypeElement(type);
      }
      type = typeUtil.getSuperclass(type);
    }
    return null;
  }

  @Override
  public void endVisit(Assignment node) {
    TypeMirror lhType = node.getLeftHandSide().getTypeMirror();
    boolean lhPrimitive = lhType.getKind().isPrimitive();
    Expression rhs = node.getRightHandSide();
    TypeMirror rhType = rhs.getTypeMirror();
    boolean rhPrimitive = rhType.getKind().isPrimitive();
    Assignment.Operator op = node.getOperator();
    if (op != Assignment.Operator.ASSIGN && !lhPrimitive) {
      rewriteBoxedAssignment(node);
    } else if (lhPrimitive && !rhPrimitive) {
      unbox(rhs);
    } else if (!lhPrimitive && rhPrimitive) {
      box(rhs, lhType);
    }
  }

  private void rewriteBoxedAssignment(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    Expression rhs = node.getRightHandSide();
    TypeMirror type = lhs.getTypeMirror();
    TypeMirror primitiveType = typeUtil.unboxedType(type);
    if (primitiveType == null) {
      return;
    }
    TypeMirror pointerType = new PointerType(type);
    String funcName = "JreBoxed" + getAssignFunctionName(node.getOperator())
        + translationUtil.getOperatorFunctionModifier(lhs)
        + NameTable.capitalize(primitiveType.toString());
    FunctionElement element = new FunctionElement(funcName, type, TypeUtil.asTypeElement(type))
        .addParameters(pointerType, primitiveType);
    FunctionInvocation invocation = new FunctionInvocation(element, type);
    invocation.addArgument(new PrefixExpression(
        pointerType, PrefixExpression.Operator.ADDRESS_OF, TreeUtil.remove(lhs)));
    invocation.addArgument(TreeUtil.remove(rhs));
    unbox(rhs);
    node.replaceWith(invocation);
  }

  private static String getAssignFunctionName(Assignment.Operator op) {
    switch (op) {
      case PLUS_ASSIGN:
        return "PlusAssign";
      case MINUS_ASSIGN:
        return "MinusAssign";
      case TIMES_ASSIGN:
        return "TimesAssign";
      case DIVIDE_ASSIGN:
        return "DivideAssign";
      case BIT_AND_ASSIGN:
        return "BitAndAssign";
      case BIT_OR_ASSIGN:
        return "BitOrAssign";
      case BIT_XOR_ASSIGN:
        return "BitXorAssign";
      case REMAINDER_ASSIGN:
        return "ModAssign";
      case LEFT_SHIFT_ASSIGN:
        return "LShiftAssign";
      case RIGHT_SHIFT_SIGNED_ASSIGN:
        return "RShiftAssign";
      case RIGHT_SHIFT_UNSIGNED_ASSIGN:
        return "URShiftAssign";
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public void endVisit(ArrayAccess node) {
    Expression index = node.getIndex();
    if (!index.getTypeMirror().getKind().isPrimitive()) {
      unbox(index);
    }
  }

  @Override
  public void endVisit(ArrayCreation node) {
    for (Expression dim : node.getDimensions()) {
      if (!dim.getTypeMirror().getKind().isPrimitive()) {
        unbox(dim);
      }
    }
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    TypeMirror type = node.getTypeMirror().getComponentType();
    for (Expression expr : node.getExpressions()) {
      boxOrUnboxExpression(expr, type);
    }
  }

  @Override
  public void endVisit(AssertStatement node) {
    Expression expression = node.getMessage();
    if (expression != null && expression.getTypeMirror().getKind().isPrimitive()) {
      box(expression);
    }
  }

  @Override
  public void endVisit(CastExpression node) {
    TypeMirror castType = node.getTypeMirror();
    Expression expr = node.getExpression();
    TypeMirror exprType = expr.getTypeMirror();
    if (castType.getKind().isPrimitive() && !exprType.getKind().isPrimitive()) {
      if (typeUtil.isAssignable(exprType, typeUtil.getJavaNumber().asType())) {
        // Casting a Number object to a primitive, convert to value method.
        unbox(expr, (PrimitiveType) castType);
      } else if (exprType == typeUtil.boxedClass(typeUtil.getChar()).asType()) {
        // Unboxing and casting Character, which does not have number value functions.
        unbox(expr);
        if (castType.getKind() != TypeKind.CHAR) {
          // If the resulting type is not char - keep the cast, to preserve type information in
          // case of reboxing.
          CastExpression castExpr = new CastExpression(castType, null);
          Expression unboxedExpression = node.getExpression();
          unboxedExpression.replaceWith(castExpr);
          castExpr.setExpression(unboxedExpression);
        }
      } else {
        // Casting an object to a primitive. Convert the cast type to the wrapper
        // so that we do a proper cast check, as Java would.
        castType = typeUtil.boxedClass((PrimitiveType) castType).asType();
        node.setType(Type.newType(castType));
        boxOrUnboxExpression(expr, castType);
      }
    } else {
      boxOrUnboxExpression(expr, castType);
    }
    Expression newExpr = node.getExpression();
    if (newExpr != expr) {
      TreeNode parent = node.getParent();
      if (parent instanceof ParenthesizedExpression) {
        parent.replaceWith(TreeUtil.remove(newExpr));
      } else {
        node.replaceWith(TreeUtil.remove(newExpr));
      }
    }
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    convertArguments(node.getExecutableElement(), node.getArguments());
  }

  @Override
  public void endVisit(ConditionalExpression node) {
    Expression expr = node.getExpression();
    if (!expr.getTypeMirror().getKind().isPrimitive()) {
      unbox(expr);
    }

    boolean nodeIsPrimitive = node.getTypeMirror().getKind().isPrimitive();
    Expression thenExpr = node.getThenExpression();
    boolean thenIsPrimitive = thenExpr.getTypeMirror().getKind().isPrimitive();
    Expression elseExpr = node.getElseExpression();
    boolean elseIsPrimitive = elseExpr.getTypeMirror().getKind().isPrimitive();

    if (thenIsPrimitive && !nodeIsPrimitive) {
      box(thenExpr);
    } else if (!thenIsPrimitive && nodeIsPrimitive) {
      unbox(thenExpr);
    }

    if (elseIsPrimitive && !nodeIsPrimitive) {
      box(elseExpr);
    } else if (!elseIsPrimitive && nodeIsPrimitive) {
      unbox(elseExpr);
    }
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    convertArguments(node.getExecutableElement(), node.getArguments());
  }

  @Override
  public void endVisit(DoStatement node) {
    Expression expression = node.getExpression();
    if (!expression.getTypeMirror().getKind().isPrimitive()) {
      unbox(expression);
    }
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    convertArguments(node.getExecutableElement(), node.getArguments());
  }

  @Override
  public void endVisit(IfStatement node) {
    Expression expr = node.getExpression();
    if (!expr.getTypeMirror().getKind().isPrimitive()) {
      unbox(expr);
    }
  }

  @Override
  public void endVisit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    List<Expression> operands = node.getOperands();

    // Don't unbox for equality tests where both operands are boxed types.
    if ((op == InfixExpression.Operator.EQUALS || op == InfixExpression.Operator.NOT_EQUALS)
        && !operands.get(0).getTypeMirror().getKind().isPrimitive()
        && !operands.get(1).getTypeMirror().getKind().isPrimitive()) {
      return;
    }
    // Don't unbox for string concatenation.
    if (op == InfixExpression.Operator.PLUS && typeUtil.isString(node.getTypeMirror())) {
      return;
    }

    for (Expression operand : operands) {
      if (!operand.getTypeMirror().getKind().isPrimitive()) {
        unbox(operand);
      }
    }
  }

  @Override
  public void endVisit(MethodInvocation node) {
    convertArguments(node.getExecutableElement(), node.getArguments());
  }

  @Override
  public void endVisit(PrefixExpression node) {
    PrefixExpression.Operator op = node.getOperator();
    Expression operand = node.getOperand();
    if (op == PrefixExpression.Operator.INCREMENT) {
      rewriteBoxedPrefixOrPostfix(node, operand, "PreIncr");
    } else if (op == PrefixExpression.Operator.DECREMENT) {
      rewriteBoxedPrefixOrPostfix(node, operand, "PreDecr");
    } else if (!operand.getTypeMirror().getKind().isPrimitive()) {
      unbox(operand);
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

  private void rewriteBoxedPrefixOrPostfix(TreeNode node, Expression operand, String funcName) {
    TypeMirror type = operand.getTypeMirror();
    TypeMirror primitiveType = typeUtil.unboxedType(type);
    if (primitiveType == null) {
      return;
    }
    TypeMirror pointerType = new PointerType(type);
    funcName = "JreBoxed" + funcName + translationUtil.getOperatorFunctionModifier(operand)
        + NameTable.capitalize(primitiveType.toString());
    FunctionElement element = new FunctionElement(funcName, type, TypeUtil.asTypeElement(type))
        .addParameters(pointerType);
    FunctionInvocation invocation = new FunctionInvocation(element, type);
    invocation.addArgument(new PrefixExpression(
        pointerType, PrefixExpression.Operator.ADDRESS_OF, TreeUtil.remove(operand)));
    node.replaceWith(invocation);
  }

  @Override
  public void endVisit(ReturnStatement node) {
    Expression expr = node.getExpression();
    if (expr != null) {
      boolean returnsPrimitive = TreeUtil.getOwningReturnType(node).getKind().isPrimitive();
      boolean exprIsPrimitive = expr.getTypeMirror().getKind().isPrimitive();
      if (returnsPrimitive && !exprIsPrimitive) {
        unbox(expr);
      }
      if (!returnsPrimitive && exprIsPrimitive) {
        box(expr);
      }
    }
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    convertArguments(node.getExecutableElement(), node.getArguments());
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    convertArguments(node.getExecutableElement(), node.getArguments());
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      TypeMirror varType = node.getVariableElement().asType();
      boolean varIsPrimitive = varType.getKind().isPrimitive();
      boolean initIsPrimitive = initializer.getTypeMirror().getKind().isPrimitive();
      if (varIsPrimitive && !initIsPrimitive) {
        unbox(initializer);
      } else if (!varIsPrimitive && initIsPrimitive) {
        box(initializer, varType);
      }
    }
  }

  @Override
  public void endVisit(WhileStatement node) {
    Expression expression = node.getExpression();
    if (!expression.getTypeMirror().getKind().isPrimitive()) {
      unbox(expression);
    }
  }

  @Override
  public void endVisit(SwitchStatement node) {
    Expression expression = node.getExpression();
    if (!expression.getTypeMirror().getKind().isPrimitive()) {
      unbox(expression);
    }
  }

  private void convertArguments(ExecutableElement method, List<Expression> args) {
    List<? extends VariableElement> params = method.getParameters();
    for (int i = 0; i < args.size(); i++) {
      TypeMirror paramType;
      if (method.isVarArgs() && i >= params.size() - 1) {
        paramType = ((ArrayType) params.get(params.size() - 1).asType()).getComponentType();
      } else {
        paramType = params.get(i).asType();
      }
      boxOrUnboxExpression(args.get(i), paramType);
    }
  }

  private void boxOrUnboxExpression(Expression expr, TypeMirror type) {
    boolean exprIsPrimitive = expr.getTypeMirror().getKind().isPrimitive();
    boolean typeIsPrimitive = type.getKind().isPrimitive();
    if (typeIsPrimitive && !exprIsPrimitive) {
      unbox(expr);
    } else if (!typeIsPrimitive && exprIsPrimitive) {
      box(expr);
    }
  }
}
