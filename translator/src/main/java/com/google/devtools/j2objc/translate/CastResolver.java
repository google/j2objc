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
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Arrays;
import java.util.List;

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
    if (BindingUtil.isFloatingPoint(exprType)) {
      assert type.isPrimitive();  // Java wouldn't allow a cast from primitive to non-primitive.
      switch (type.getBinaryName().charAt(0)) {
        case 'J':
          node.replaceWith(rewriteFloatToIntegralCast(type, expr, "J2ObjCFpToLong", type));
          return;
        case 'C':
          node.replaceWith(rewriteFloatToIntegralCast(type, expr, "J2ObjCFpToUnichar", type));
          return;
        case 'B':
        case 'S':
        case 'I':
          node.replaceWith(rewriteFloatToIntegralCast(
              type, expr, "J2ObjCFpToInt", typeEnv.resolveJavaType("int")));
          return;
      }
      // else fall-through.
    }

    // Lean on Java's type-checking.
    if (!type.isPrimitive() && exprType.isAssignmentCompatible(type)) {
      node.replaceWith(TreeUtil.remove(expr));
      return;
    }

    FunctionInvocation castCheck = createCastCheck(type, expr);
    if (castCheck != null) {
      node.setExpression(castCheck);
    }
  }

  private static Expression rewriteFloatToIntegralCast(
      ITypeBinding castType, Expression expr, String funcName, ITypeBinding funcReturnType) {
    FunctionInvocation invocation = new FunctionInvocation(
        funcName, funcReturnType, funcReturnType, null);
    invocation.getArguments().add(TreeUtil.remove(expr));
    Expression newExpr = invocation;
    if (!castType.isEqualTo(funcReturnType)) {
      newExpr = new CastExpression(castType, newExpr);
    }
    return newExpr;
  }

  private FunctionInvocation createCastCheck(ITypeBinding type, Expression expr) {
    // Find the first bound for a type variable.
    while (type.isTypeVariable()) {
      ITypeBinding[] bounds = type.getTypeBounds();
      if (bounds.length == 0) {
        break;
      }
      type = bounds[0];
    }
    ITypeBinding idType = typeEnv.resolveIOSType("id");
    FunctionInvocation invocation = null;
    if (type.isInterface() && !type.isAnnotation()) {
      invocation = new FunctionInvocation("check_protocol_cast", idType, idType, null);
      invocation.getArguments().add(TreeUtil.remove(expr));
      FunctionInvocation protocolLiteral =
          new FunctionInvocation("@protocol", idType, idType, null);
      protocolLiteral.getArguments().add(new SimpleName(type));
      invocation.getArguments().add(protocolLiteral);
    } else if (type.isClass() || type.isArray() || type.isAnnotation() || type.isEnum()) {
      invocation = new FunctionInvocation("check_class_cast", idType, idType, null);
      invocation.getArguments().add(TreeUtil.remove(expr));
      IOSMethodBinding binding = IOSMethodBinding.newMethod("class", Modifier.STATIC, idType, type);
      MethodInvocation classInvocation = new MethodInvocation(binding, new SimpleName(type));
      invocation.getArguments().add(classInvocation);
    }
    return invocation;
  }

  private void addCast(Expression expr) {
    ITypeBinding exprType = typeEnv.mapType(expr.getTypeBinding().getTypeDeclaration());
    CastExpression castExpr = new CastExpression(exprType, null);
    expr.replaceWith(ParenthesizedExpression.parenthesize(castExpr));
    castExpr.setExpression(expr);
  }

  private void maybeAddCast(Expression expr, boolean shouldCastFromId) {
    if (needsCast(expr, shouldCastFromId)) {
      addCast(expr);
    }
  }

  private boolean needsCast(Expression expr, boolean shouldCastFromId) {
    ITypeBinding declaredType = getDeclaredType(expr);
    if (declaredType == null) {
      return false;
    }
    ITypeBinding exprType = typeEnv.mapType(expr.getTypeBinding().getTypeDeclaration());
    declaredType = typeEnv.mapType(declaredType.getTypeDeclaration());
    if (declaredType.isAssignmentCompatible(exprType)) {
      return false;
    }
    if (declaredType == typeEnv.resolveIOSType("id") && !shouldCastFromId) {
      return false;
    }
    if (exprType.isPrimitive()) {
      return false;
    }
    String typeName = nameTable.getSpecificObjCType(exprType);
    if (typeName.equals(NameTable.ID_TYPE)) {
      return false;
    }
    return true;
  }

  private ITypeBinding getDeclaredType(Expression expr) {
    IVariableBinding var = TreeUtil.getVariableBinding(expr);
    if (var != null) {
      return var.getVariableDeclaration().getType();
    }
    switch (expr.getKind()) {
      case CLASS_INSTANCE_CREATION:
        return typeEnv.resolveIOSType("id");
      case FUNCTION_INVOCATION:
        return ((FunctionInvocation) expr).getDeclaredReturnType();
      case METHOD_INVOCATION:
        {
          MethodInvocation invocation = (MethodInvocation) expr;
          IMethodBinding method = invocation.getMethodBinding();
          // Object receiving the message, or null if it's a method in this class.
          Expression receiver = invocation.getExpression();
          ITypeBinding receiverType = receiver != null ? receiver.getTypeBinding()
              : method.getDeclaringClass();
          return getDeclaredReturnType(method, receiverType);
        }
      case SUPER_METHOD_INVOCATION:
        {
          SuperMethodInvocation invocation = (SuperMethodInvocation) expr;
          IMethodBinding method = invocation.getMethodBinding();
          if (invocation.getQualifier() != null) {
            // For a qualified super invocation, the statement generator will look
            // up the IMP using instanceMethodForSelector.
            if (!method.getReturnType().isPrimitive()) {
              return typeEnv.resolveIOSType("id");
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
    IMethodBinding actualDeclaration =
        getFirstDeclaration(getObjCMethodSignature(method), receiverType);
    if (actualDeclaration == null) {
      actualDeclaration = method.getMethodDeclaration();
    }
    ITypeBinding returnType = actualDeclaration.getReturnType();
    if (returnType.isTypeVariable()) {
      return typeEnv.resolveIOSType("id");
    }
    return returnType.getErasure();
  }

  /**
   * Finds the declaration for a given method and receiver in the same way that
   * the ObjC compiler will search for a declaration.
   */
  private IMethodBinding getFirstDeclaration(String methodSig, ITypeBinding type) {
    if (type == null) {
      return null;
    }
    type = type.getTypeDeclaration();
    for (IMethodBinding declaredMethod : type.getDeclaredMethods()) {
      if (methodSig.equals(getObjCMethodSignature(declaredMethod))) {
        return declaredMethod;
      }
    }
    List<ITypeBinding> supertypes = Lists.newArrayList();
    supertypes.addAll(Arrays.asList(type.getInterfaces()));
    supertypes.add(type.isTypeVariable() ? 0 : supertypes.size(), type.getSuperclass());
    for (ITypeBinding supertype : supertypes) {
      IMethodBinding result = getFirstDeclaration(methodSig, supertype);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private String getObjCMethodSignature(IMethodBinding method) {
    StringBuilder sb = new StringBuilder(method.getName());
    boolean first = true;
    for (ITypeBinding paramType : method.getParameterTypes()) {
      String keyword = nameTable.parameterKeyword(paramType);
      if (first) {
        first = false;
        keyword = NameTable.capitalize(keyword);
      }
      sb.append(keyword + ":");
    }
    return sb.toString();
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

  @Override
  public void endVisit(FieldAccess node) {
    maybeAddCast(node.getExpression(), true);
  }

  @Override
  public void endVisit(MethodInvocation node) {
    Expression receiver = node.getExpression();
    if (receiver != null && !BindingUtil.isStatic(node.getMethodBinding())) {
      maybeAddCast(receiver, true);
    }
    if (returnValueNeedsIntCast(node)) {
      addCast(node);
    }
  }

  @Override
  public void endVisit(QualifiedName node) {
    if (needsCast(node.getQualifier(), true)) {
      maybeAddCast(TreeUtil.convertToFieldAccess(node).getExpression(), true);
    }
  }

  @Override
  public void endVisit(ReturnStatement node) {
    Expression expr = node.getExpression();
    if (expr != null) {
      maybeAddCast(expr, false);
    }
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    if (returnValueNeedsIntCast(node)) {
      addCast(node);
    }
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      maybeAddCast(initializer, false);
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

    IVariableBinding param = node.getParameters().get(0).getVariableBinding();

    FunctionInvocation castCheck = createCastCheck(typeArguments[0], new SimpleName(param));
    if (castCheck != null) {
      node.getBody().getStatements().add(0, new ExpressionStatement(castCheck));
    }
  }
}
