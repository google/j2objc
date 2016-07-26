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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
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
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Adds cast checks to existing java cast expressions.
 * Adds casts as needed for Objective-C compilation. Usually this occurs when a
 * method has a declared return type that is more generic than the resolved type
 * of the expression.
 */
public class CastResolver extends TreeVisitor {

  @Override
  public void endVisit(CastExpression node) {
    ITypeBinding type = node.getType().getTypeBinding();
    Expression expr = node.getExpression();
    ITypeBinding exprType = expr.getTypeBinding();

    // TODO(kirbs): Implement correct conversion of Java 8 intersection types to Objective-C.
    if (node.getType().isIntersectionType() && !Options.isJava8Translator()) {
      // Technically we can't currently get here, but as we add support and change flags in the
      // future this should alert us to implement intersection types.
        assert false : "not implemented yet";
    }

    if (BindingUtil.isFloatingPoint(exprType)) {
      assert type.isPrimitive();  // Java wouldn't allow a cast from primitive to non-primitive.
      switch (type.getBinaryName().charAt(0)) {
        case 'J':
          node.replaceWith(rewriteFloatToIntegralCast(type, expr, "JreFpToLong", type));
          return;
        case 'C':
          node.replaceWith(rewriteFloatToIntegralCast(type, expr, "JreFpToChar", type));
          return;
        case 'B':
        case 'S':
        case 'I':
          node.replaceWith(rewriteFloatToIntegralCast(
              type, expr, "JreFpToInt", typeEnv.resolveJavaType("int")));
          return;
      }
      // else fall-through.
    }

    // Lean on Java's type-checking.
    if (!type.isPrimitive() && exprType.isAssignmentCompatible(type.getErasure())) {
      node.replaceWith(TreeUtil.remove(expr));
      return;
    }

    FunctionInvocation castCheck = createCastCheck(type, expr);
    if (castCheck != null) {
      node.setExpression(castCheck);
    }
  }

  private Expression rewriteFloatToIntegralCast(
      ITypeBinding castType, Expression expr, String funcName, ITypeBinding funcReturnType) {
    FunctionBinding binding = new FunctionBinding(funcName, funcReturnType, null);
    binding.addParameters(typeEnv.resolveJavaTypeMirror("double"));
    FunctionInvocation invocation = new FunctionInvocation(binding, funcReturnType);
    invocation.addArgument(TreeUtil.remove(expr));
    Expression newExpr = invocation;
    if (!castType.isEqualTo(funcReturnType)) {
      newExpr = new CastExpression(castType, newExpr);
    }
    return newExpr;
  }

  private FunctionInvocation createCastCheck(ITypeBinding type, Expression expr) {
    type = type.getErasure();
    TypeMirror idType = typeEnv.resolveIOSTypeMirror("id");
    FunctionInvocation invocation = null;
    if ((type.isInterface() && !type.isAnnotation())
        || (type.isArray() && !type.getComponentType().isPrimitive())) {
      FunctionBinding binding = new FunctionBinding("cast_check", idType, null);
      binding.addParameters(idType, typeEnv.getIOSClassMirror());
      invocation = new FunctionInvocation(binding, idType);
      invocation.addArgument(TreeUtil.remove(expr));
      invocation.addArgument(new TypeLiteral(type, typeEnv));
    } else if (type.isClass() || type.isArray() || type.isAnnotation() || type.isEnum()) {
      FunctionBinding binding = new FunctionBinding("cast_chk", idType, null);
      binding.addParameters(idType, idType);
      invocation = new FunctionInvocation(binding, idType);
      invocation.addArgument(TreeUtil.remove(expr));
      IOSMethodBinding classBinding = IOSMethodBinding.newMethod(
          "class", Modifier.STATIC, idType, BindingConverter.getType(type));
      MethodInvocation classInvocation = new MethodInvocation(classBinding, new SimpleName(type));
      invocation.addArgument(classInvocation);
    }
    return invocation;
  }

  private void addCast(Expression expr) {
    ITypeBinding exprType = typeEnv.mapType(expr.getTypeBinding());
    CastExpression castExpr = new CastExpression(exprType, null);
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
    if (needsCast(expr, BindingConverter.unwrapTypeMirrorIntoTypeBinding(expectedType),
        shouldCastFromId)) {
      addCast(expr);
    }
  }

  private boolean needsCast(Expression expr, ITypeBinding expectedType, boolean shouldCastFromId) {
    ITypeBinding declaredType = getDeclaredType(expr);
    if (declaredType == null) {
      return false;
    }
    ITypeBinding exprType = typeEnv.mapType(expr.getTypeBinding());
    declaredType = typeEnv.mapType(declaredType);
    if (
        // In general we do not need to cast primitive types.
        exprType.isPrimitive()
        // In most cases we don't need to cast from an id type. However, if the
        // expression is being dereferenced then the compiler needs the type
        // info.
        || (declaredAsId(declaredType) && !shouldCastFromId)
        // If the declared type can be assigned into the actual type, or the
        // expected type, then the compiler already has sufficient type info.
        || declaredAsId(exprType) || declaredType.isAssignmentCompatible(exprType)
        || (expectedType != null && (declaredAsId(expectedType)
            || declaredType.isAssignmentCompatible(expectedType)))) {
      return false;
    }
    return true;
  }

  // Determine if the declaration for this type would end up being "id".
  private boolean declaredAsId(ITypeBinding type) {
    if (typeEnv.isIdType(type)) {
      return true;
    }
    List<ITypeBinding> bounds = BindingUtil.getTypeBounds(type);
    return bounds.size() == 1 && typeEnv.isIdType(bounds.get(0));
  }

  private ITypeBinding getDeclaredType(Expression expr) {
    IVariableBinding var = TreeUtil.getVariableBinding(expr);
    if (var != null) {
      return var.getVariableDeclaration().getType();
    }
    switch (expr.getKind()) {
      case CLASS_INSTANCE_CREATION:
        return typeEnv.getIdType();
      case FUNCTION_INVOCATION:
        return BindingConverter.unwrapTypeMirrorIntoTypeBinding(
            ((FunctionInvocation) expr).getFunctionBinding().getReturnType());
      case LAMBDA_EXPRESSION:
        // Lambda expressions are generated as function calls that return "id".
        return typeEnv.getIdType();
      case METHOD_INVOCATION: {
        MethodInvocation invocation = (MethodInvocation) expr;
        IMethodBinding method = invocation.getMethodBinding();
        // Object receiving the message, or null if it's a method in this class.
        Expression receiver = invocation.getExpression();
        ITypeBinding receiverType = receiver != null ? receiver.getTypeBinding()
            : method.getDeclaringClass();
        return getDeclaredReturnType(method, receiverType);
      }
      case PARENTHESIZED_EXPRESSION:
        return getDeclaredType(((ParenthesizedExpression) expr).getExpression());
      case SUPER_METHOD_INVOCATION: {
        SuperMethodInvocation invocation = (SuperMethodInvocation) expr;
        IMethodBinding method = invocation.getMethodBinding();
        if (invocation.getQualifier() != null) {
          // For a qualified super invocation, the statement generator will look
          // up the IMP using instanceMethodForSelector.
          if (!method.getReturnType().isPrimitive()) {
            return typeEnv.getIdType();
          } else {
            return null;
          }
        }
        return getDeclaredReturnType(
            method, TreeUtil.getOwningType(invocation).getTypeBinding().getSuperclass());
      }
      default:
        return null;
    }
  }

  private ITypeBinding getDeclaredReturnType(IMethodBinding method, ITypeBinding receiverType) {
    final IMethodBinding methodDecl = method.getMethodDeclaration();

    // Check if the method is declared on the receiver type.
    if (receiverType.getTypeDeclaration() == methodDecl.getDeclaringClass()) {
      return methodDecl.getReturnType();
    }

    // Search all inherited types for matching method declarations. Choose the
    // most narrow return type, because AbstractMethodRewriter will ensure that
    // a declaration exists with the most narrow return type.
    String selector = nameTable.getMethodSelector(methodDecl);
    for (ITypeBinding typeBound : BindingUtil.getTypeBounds(receiverType)) {
      ITypeBinding returnType = null;
      for (ITypeBinding inheritedType :
           BindingUtil.getOrderedInheritedTypesInclusive(typeBound.getTypeDeclaration())) {
        for (IMethodBinding declaredMethod : inheritedType.getDeclaredMethods()) {
          if (methodDecl.isSubsignature(declaredMethod)
              && nameTable.getMethodSelector(declaredMethod).equals(selector)) {
            ITypeBinding newReturnType = declaredMethod.getReturnType().getErasure();
            if (returnType == null || newReturnType.isSubTypeCompatible(returnType)) {
              returnType = newReturnType;
            }
          }
        }
      }
      if (returnType != null) {
        return returnType;
      }
    }

    // Last resort. Might be a GeneratedMethodBinding.
    return methodDecl.getReturnType();
  }

  // Some native objective-c methods are declared to return NSUInteger.
  private boolean returnValueNeedsIntCast(Expression arg) {
    IMethodBinding methodBinding = TreeUtil.getMethodBinding(arg);
    assert methodBinding != null;

    if (arg.getParent() instanceof ExpressionStatement) {
      // Avoid "unused return value" warning.
      return false;
    }

    String methodName = nameTable.getMethodSelector(methodBinding);
    if (methodName.equals("hash")
        && methodBinding.getReturnType().isEqualTo(typeEnv.resolveJavaType("int"))) {
      return true;
    }
    if (typeEnv.isStringType(methodBinding.getDeclaringClass()) && methodName.equals("length")) {
      return true;
    }
    return false;
  }

  private void maybeCastArguments(List<Expression> args, List<TypeMirror> argTypes) {
    // Possible varargs, don't cast vararg arguments.
    assert args.size() >= argTypes.size();
    for (int i = 0; i < argTypes.size(); i++) {
      maybeAddCast(args.get(i), argTypes.get(i), false);
    }
  }

  private void maybeCastArguments(List<Expression> args, IMethodBinding method) {
    List<TypeMirror> paramTypes = new ArrayList<>();
    for (ITypeBinding param : method.getParameterTypes()) {
      paramTypes.add(BindingConverter.getType(param));
    }
    maybeCastArguments(args, paramTypes);
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    maybeCastArguments(node.getArguments(), node.getMethodBinding());
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    maybeCastArguments(node.getArguments(), node.getMethodBinding());
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    maybeCastArguments(node.getArguments(), node.getMethodBinding());
  }

  @Override
  public void endVisit(FieldAccess node) {
    maybeAddCast(node.getExpression(), null, true);
  }

  @Override
  public void endVisit(FunctionInvocation node) {
    maybeCastArguments(node.getArguments(), node.getFunctionBinding().getParameterTypes());
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding();
    Expression receiver = node.getExpression();
    if (receiver != null && !BindingUtil.isStatic(binding)) {
      maybeAddCast(receiver, null, true);
    }
    maybeCastArguments(node.getArguments(), binding);
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
    maybeCastArguments(node.getArguments(), node.getMethodBinding());
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    maybeCastArguments(node.getArguments(), node.getMethodBinding());
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
    IMethodBinding binding = node.getMethodBinding();
    if (!binding.getName().equals("compareTo") || node.getBody() == null) {
      return;
    }
    ITypeBinding comparableType =
        BindingUtil.findInterface(binding.getDeclaringClass(), "java.lang.Comparable");
    if (comparableType == null) {
      return;
    }
    ITypeBinding[] typeArguments = comparableType.getTypeArguments();
    ITypeBinding[] parameterTypes = binding.getParameterTypes();
    if (typeArguments.length != 1 || parameterTypes.length != 1
        || !typeArguments[0].isEqualTo(parameterTypes[0])) {
      return;
    }

    IVariableBinding param = node.getParameter(0).getVariableBinding();

    FunctionInvocation castCheck = createCastCheck(typeArguments[0], new SimpleName(param));
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
      if (needsIdCast(operands.get(0), operands.get(1))) {
        // Add (id) cast to right-hand operand(s).
        operands.add(1, new CastExpression(typeEnv.getIdType(), operands.remove(1)));
      }
    }
  }

  private boolean needsIdCast(Expression lhs, Expression rhs) {
    ITypeBinding lhsType = lhs.getTypeBinding();
    ITypeBinding rhsType = rhs.getTypeBinding();
    return !lhsType.isPrimitive() && !rhsType.isPrimitive()
        && !lhsType.isAssignmentCompatible(rhsType) && !rhsType.isAssignmentCompatible(lhsType);
  }
}
