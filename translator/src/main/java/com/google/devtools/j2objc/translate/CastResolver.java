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

import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InfixExpression.Operator;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Adds cast checks to existing java cast expressions.
 * Adds casts as needed for Objective-C compilation. Usually this occurs when a
 * method has a declared return type that is more generic than the resolved type
 * of the expression.
 */
public class CastResolver extends UnitTreeVisitor {

  public CastResolver(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(CastExpression node) {
    TypeMirror type = node.getType().getTypeMirror();
    Expression expr = node.getExpression();
    TypeMirror exprType = expr.getTypeMirror();

    if (TypeUtil.isFloatingPoint(exprType)) {
      // Java wouldn't allow a cast from primitive to non-primitive.
      assert type.getKind().isPrimitive();
      switch (type.getKind()) {
        case LONG:
          node.replaceWith(rewriteFloatToIntegralCast(type, expr, "JreFpToLong", type));
          return;
        case CHAR:
          node.replaceWith(rewriteFloatToIntegralCast(type, expr, "JreFpToChar", type));
          return;
        case BYTE:
        case SHORT:
        case INT:
          node.replaceWith(rewriteFloatToIntegralCast(type, expr, "JreFpToInt", typeUtil.getInt()));
          return;
        default:  // Fall through.
      }
    }

    // Lean on Java's type-checking.
    if (!type.getKind().isPrimitive() && typeUtil.isAssignable(exprType, typeUtil.erasure(type))) {
      node.replaceWith(TreeUtil.remove(expr));
      return;
    }

    FunctionInvocation castCheck = createCastCheck(type, expr);
    if (castCheck != null) {
      node.setExpression(castCheck);
    }
  }

  private Expression rewriteFloatToIntegralCast(
      TypeMirror castType, Expression expr, String funcName, TypeMirror funcReturnType) {
    FunctionElement element = new FunctionElement(funcName, funcReturnType, null)
        .addParameters(typeUtil.getDouble());
    FunctionInvocation invocation = new FunctionInvocation(element, funcReturnType);
    invocation.addArgument(TreeUtil.remove(expr));
    Expression newExpr = invocation;
    if (!castType.equals(funcReturnType)) {
      newExpr = new CastExpression(castType, newExpr);
    }
    return newExpr;
  }

  private static boolean isObjectArray(TypeMirror type) {
    return TypeUtil.isArray(type) && !((ArrayType) type).getComponentType().getKind().isPrimitive();
  }

  private FunctionInvocation createCastCheck(TypeMirror type, Expression expr) {
    type = typeUtil.erasure(type);
    TypeMirror idType = TypeUtil.ID_TYPE;
    if (TypeUtil.isInterface(type) || isObjectArray(type)) {
      // Interfaces and object arrays requre a isInstance call.
      FunctionElement element = new FunctionElement("cast_check", idType, null)
          .addParameters(idType, TypeUtil.IOS_CLASS.asType());
      FunctionInvocation invocation = new FunctionInvocation(element, idType);
      invocation.addArgument(TreeUtil.remove(expr));
      invocation.addArgument(new TypeLiteral(type, typeUtil));
      return invocation;
    } else if (TypeUtil.isArray(type) || TypeUtil.isDeclaredType(type)) {
      // Primitive array and non-interface type casts are checked using Objective-C's
      // isKindOfClass:.
      TypeElement objcClass = typeUtil.getObjcClass(type);
      FunctionElement checkFunction = new FunctionElement("cast_chk", idType, null)
          .addParameters(idType, idType);
      FunctionInvocation invocation = new FunctionInvocation(checkFunction, idType);
      invocation.addArgument(TreeUtil.remove(expr));
      ExecutableElement classElement =
          GeneratedExecutableElement.newMethodWithSelector("class", idType, objcClass)
          .addModifiers(Modifier.STATIC);
      MethodInvocation classInvocation =
          new MethodInvocation(new ExecutablePair(classElement), new SimpleName(objcClass));
      invocation.addArgument(classInvocation);
      return invocation;
    }
    return null;
  }

  private void addCast(Expression expr) {
    CastExpression castExpr = new CastExpression(expr.getTypeMirror(), null);
    expr.replaceWith(ParenthesizedExpression.parenthesize(castExpr));
    castExpr.setExpression(expr);
  }

  private void maybeAddCast(Expression expr, TypeMirror expectedType, boolean shouldCastFromId) {
    if (expr instanceof ConditionalExpression) {
      ConditionalExpression condExpr = (ConditionalExpression) expr;
      maybeAddCast(condExpr.getThenExpression(), expectedType, shouldCastFromId);
      maybeAddCast(condExpr.getElseExpression(), expectedType, shouldCastFromId);
      return;
    }
    if (needsCast(expr, expectedType, shouldCastFromId)) {
      addCast(expr);
    }
  }

  private boolean needsCast(Expression expr, TypeMirror expectedType, boolean shouldCastFromId) {
    TypeMirror declaredType = getDeclaredType(expr);
    if (declaredType == null) {
      return false;
    }
    TypeMirror exprType = expr.getTypeMirror();
    if (
        // In general we do not need to cast primitive types.
        exprType.getKind().isPrimitive()
        // In most cases we don't need to cast from an id type. However, if the
        // expression is being dereferenced then the compiler needs the type
        // info.
        || (typeUtil.isDeclaredAsId(declaredType) && !shouldCastFromId)
        // If the declared type can be assigned into the actual type, or the
        // expected type, then the compiler already has sufficient type info.
        || typeUtil.isObjcAssignable(declaredType, exprType)
        || (expectedType != null && typeUtil.isObjcAssignable(declaredType, expectedType))) {
      return false;
    }
    return true;
  }

  private TypeMirror getDeclaredType(Expression expr) {
    VariableElement var = TreeUtil.getVariableElement(expr);
    if (var != null) {
      return var.asType();
    }
    switch (expr.getKind()) {
      case CLASS_INSTANCE_CREATION:
        return TypeUtil.ID_TYPE;
      case FUNCTION_INVOCATION:
        return ((FunctionInvocation) expr).getFunctionElement().getReturnType();
      case LAMBDA_EXPRESSION:
        // Lambda expressions are generated as function calls that return "id".
        return TypeUtil.ID_TYPE;
      case METHOD_INVOCATION: {
        MethodInvocation invocation = (MethodInvocation) expr;
        ExecutableElement method = invocation.getExecutableElement();
        // Object receiving the message, or null if it's a method in this class.
        Expression receiver = invocation.getExpression();
        TypeMirror receiverType = receiver != null ? receiver.getTypeMirror()
            : ElementUtil.getDeclaringClass(method).asType();
        return getDeclaredReturnType(method, receiverType);
      }
      case PARENTHESIZED_EXPRESSION:
        return getDeclaredType(((ParenthesizedExpression) expr).getExpression());
      case SUPER_METHOD_INVOCATION: {
        SuperMethodInvocation invocation = (SuperMethodInvocation) expr;
        return getDeclaredReturnType(
            invocation.getExecutableElement(),
            TreeUtil.getEnclosingTypeElement(invocation).getSuperclass());
      }
      default:
        return null;
    }
  }

  private TypeMirror getDeclaredReturnType(ExecutableElement method, TypeMirror receiverType) {
    // Check if the method is declared on the receiver type.
    if (ElementUtil.getDeclaringClass(method).equals(TypeUtil.asTypeElement(receiverType))) {
      return method.getReturnType();
    }

    // Search all inherited types for matching method declarations. Choose the
    // most narrow return type, because AbstractMethodRewriter will ensure that
    // a declaration exists with the most narrow return type.
    ExecutableType methodType = (ExecutableType) method.asType();
    String selector = nameTable.getMethodSelector(method);
    for (TypeMirror typeBound : typeUtil.getUpperBounds(receiverType)) {
      TypeMirror returnType = null;
      for (DeclaredType inheritedType : typeUtil.getObjcOrderedInheritedTypes(typeBound)) {
        TypeElement inheritedElem = (TypeElement) inheritedType.asElement();
        for (ExecutableElement currentMethod : ElementUtil.getMethods(inheritedElem)) {
          ExecutableType currentMethodType = typeUtil.asMemberOf(inheritedType, currentMethod);
          if (typeUtil.isSubsignature(methodType, currentMethodType)
              && nameTable.getMethodSelector(currentMethod).equals(selector)) {
            TypeMirror newReturnType = typeUtil.erasure(currentMethodType.getReturnType());
            if (returnType == null || typeUtil.isSubtype(newReturnType, returnType)) {
              returnType = newReturnType;
            }
          }
        }
      }
      if (returnType != null) {
        return returnType;
      }
    }

    // Last resort. Might be a GeneratedExecutableElement.
    return method.getReturnType();
  }

  // Some native objective-c methods are declared to return NSUInteger.
  private boolean returnValueNeedsIntCast(Expression arg) {
    ExecutableElement methodElement = TreeUtil.getExecutableElement(arg);
    assert methodElement != null;

    if (arg.getParent() instanceof ExpressionStatement) {
      // Avoid "unused return value" warning.
      return false;
    }

    String methodName = nameTable.getMethodSelector(methodElement);
    if (methodName.equals("hash") && methodElement.getReturnType().getKind() == TypeKind.INT) {
      return true;
    }
    if (typeUtil.isString(ElementUtil.getDeclaringClass(methodElement))
        && methodName.equals("length")) {
      return true;
    }
    return false;
  }

  private void maybeCastArguments(
      List<Expression> args, Iterable<? extends TypeMirror> paramTypes) {
    Iterator<Expression> argIter = args.iterator();
    Iterator<? extends TypeMirror> paramTypeIter = paramTypes.iterator();
    // Implicit assert that size(paramTypes) >= size(args). Don't cast vararg arguments.
    while (paramTypeIter.hasNext()) {
      maybeAddCast(argIter.next(), paramTypeIter.next(), false);
    }
  }

  private void maybeCastArguments(List<Expression> args, ExecutableElement method) {
    maybeCastArguments(args, ElementUtil.asTypes(method.getParameters()));
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    maybeCastArguments(node.getArguments(), node.getExecutableElement());
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    maybeCastArguments(node.getArguments(), node.getExecutableElement());
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    maybeCastArguments(node.getArguments(), node.getExecutableElement());
  }

  @Override
  public void endVisit(FieldAccess node) {
    maybeAddCast(node.getExpression(), null, true);
  }

  @Override
  public void endVisit(FunctionInvocation node) {
    maybeCastArguments(node.getArguments(), node.getFunctionElement().getParameterTypes());
  }

  @Override
  public void endVisit(MethodInvocation node) {
    Expression receiver = node.getExpression();
    if (receiver != null && !ElementUtil.isStatic(node.getExecutableElement())) {
      maybeAddCast(receiver, null, true);
    }
    maybeCastArguments(node.getArguments(), node.getExecutableElement());
    if (returnValueNeedsIntCast(node)) {
      addCast(node);
    }
  }

  @Override
  public void endVisit(ReturnStatement node) {
    Expression expr = node.getExpression();
    if (expr != null) {
      maybeAddCast(expr, TreeUtil.getOwningReturnType(node), false);
    }
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    maybeCastArguments(node.getArguments(), node.getExecutableElement());
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    maybeCastArguments(node.getArguments(), node.getExecutableElement());
    if (returnValueNeedsIntCast(node)) {
      addCast(node);
    }
  }

  @Override
  public void endVisit(Assignment node) {
    maybeAddCast(node.getRightHandSide(), node.getTypeMirror(), false);
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      maybeAddCast(initializer, node.getVariableElement().asType(), false);
    }
  }

  /**
   * Adds a cast check to compareTo methods. This helps Comparable types behave
   * well in sorted collections which rely on Java's runtime type checking.
   */
  @Override
  public void endVisit(MethodDeclaration node) {
    ExecutableElement element = node.getExecutableElement();
    if (!ElementUtil.getName(element).equals("compareTo") || node.getBody() == null) {
      return;
    }
    DeclaredType comparableType = typeUtil.findSupertype(
        ElementUtil.getDeclaringClass(element).asType(), "java.lang.Comparable");
    if (comparableType == null) {
      return;
    }
    List<? extends TypeMirror> typeArguments = comparableType.getTypeArguments();
    List<? extends VariableElement> parameters = element.getParameters();
    if (typeArguments.size() != 1 || parameters.size() != 1
        || !typeArguments.get(0).equals(parameters.get(0).asType())) {
      return;
    }

    VariableElement param = node.getParameter(0).getVariableElement();

    FunctionInvocation castCheck = createCastCheck(typeArguments.get(0), new SimpleName(param));
    if (castCheck != null) {
      node.getBody().addStatement(0, new ExpressionStatement(castCheck));
    }
  }

  @Override
  public void endVisit(InfixExpression node) {
    // Clang reports an incompatible pointer comparison when comparing two
    // objects with different interface types. That's potentially wrong both
    // in Java and Objective-C, since a single class can implement both
    // interfaces. CastResolverTest.testInterfaceComparisons() demonstrates
    // the problem.
    Operator operator = node.getOperator();
    if (operator == InfixExpression.Operator.EQUALS
        || operator == InfixExpression.Operator.NOT_EQUALS) {
      List<Expression> operands = node.getOperands();
      if (incompatibleTypes(operands.get(0), operands.get(1))) {
        // Add (id) cast to right-hand operand(s).
        operands.add(1, new CastExpression(TypeUtil.ID_TYPE, operands.remove(1)));
      }
    }
  }

  @Override
  public void endVisit(ConditionalExpression node) {
    Expression thenExpr = node.getThenExpression();
    Expression elseExpr = node.getElseExpression();
    if (incompatibleTypes(thenExpr, elseExpr)) {
      // Add (id) cast to else expression.
      node.setElseExpression(new CastExpression(TypeUtil.ID_TYPE, TreeUtil.remove(elseExpr)));
    }
  }

  private boolean incompatibleTypes(Expression a, Expression b) {
    TypeMirror aType = a.getTypeMirror();
    TypeMirror bType = b.getTypeMirror();
    return TypeUtil.isReferenceType(aType) && TypeUtil.isReferenceType(bType)
        && !typeUtil.isObjcAssignable(aType, bType) && !typeUtil.isObjcAssignable(bType, aType);
  }
}
