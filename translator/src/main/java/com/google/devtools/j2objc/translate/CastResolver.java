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
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.Types;
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

  private static final IOSMethod CLASS_METHOD = IOSMethod.create("NSObject class");

  @Override
  public void endVisit(CastExpression node) {
    ITypeBinding type = node.getType().getTypeBinding();
    Expression expr = node.getExpression();
    ITypeBinding exprType = expr.getTypeBinding();
    if (Types.isFloatingPointType(exprType)) {
      if (Types.isLongType(type)) {
        FunctionInvocation invocation = new FunctionInvocation("J2ObjCFpToLong", type, type, null);
        invocation.getArguments().add(TreeUtil.remove(expr));
        node.replaceWith(invocation);
        return;
      } else if (type.isEqualTo(Types.resolveJavaType("char"))) {
        FunctionInvocation invocation =
            new FunctionInvocation("J2ObjCFpToUnichar", type, type, null);
        invocation.getArguments().add(TreeUtil.remove(expr));
        node.replaceWith(invocation);
        return;
      } else if (Types.isIntegralType(type)) {
        ITypeBinding intType = Types.resolveJavaType("int");
        FunctionInvocation invocation =
            new FunctionInvocation("J2ObjCFpToInt", intType, intType, null);
        invocation.getArguments().add(TreeUtil.remove(expr));
        Expression newExpr = invocation;
        if (!type.isEqualTo(intType)) {
          newExpr = new CastExpression(type, newExpr);
        }
        node.replaceWith(newExpr);
        return;
      }
      // else fall-through.
    }
    ITypeBinding idType = Types.resolveIOSType("id");
    if (type.isInterface() && !type.isAnnotation()) {
      FunctionInvocation invocation =
          new FunctionInvocation("check_protocol_cast", idType, idType, null);
      expr.replaceWith(invocation);
      invocation.getArguments().add(expr);
      FunctionInvocation protocolLiteral =
          new FunctionInvocation("@protocol", idType, idType, null);
      protocolLiteral.getArguments().add(new SimpleName(type));
      invocation.getArguments().add(protocolLiteral);
    } else if (type.isClass() || type.isArray() || type.isAnnotation()) {
      FunctionInvocation invocation =
          new FunctionInvocation("check_class_cast", idType, idType, null);
      expr.replaceWith(invocation);
      invocation.getArguments().add(expr);
      IOSMethodBinding binding = IOSMethodBinding.newMethod(
          CLASS_METHOD, Modifier.STATIC, idType, type);
      MethodInvocation classInvocation = new MethodInvocation(binding, new SimpleName(type));
      invocation.getArguments().add(classInvocation);
    }
  }

  private void maybeAddCast(Expression expr, boolean shouldCastFromId) {
    if (needsCast(expr, shouldCastFromId)) {
      ITypeBinding exprType = Types.mapType(expr.getTypeBinding().getTypeDeclaration());
      CastExpression castExpr = new CastExpression(exprType, null);
      expr.replaceWith(ParenthesizedExpression.parenthesize(castExpr));
      castExpr.setExpression(expr);
    }
  }

  private boolean needsCast(Expression expr, boolean shouldCastFromId) {
    ITypeBinding declaredType = getDeclaredType(expr);
    if (declaredType == null) {
      return false;
    }
    ITypeBinding exprType = Types.mapType(expr.getTypeBinding().getTypeDeclaration());
    declaredType = Types.mapType(declaredType.getTypeDeclaration());
    if (declaredType.isAssignmentCompatible(exprType)) {
      return false;
    }
    if (declaredType == Types.resolveIOSType("id") && !shouldCastFromId) {
      return false;
    }
    if (exprType.isPrimitive() || Types.isVoidType(exprType)) {
      return false;
    }
    String typeName = NameTable.getSpecificObjCType(exprType);
    if (typeName.equals(NameTable.ID_TYPE)) {
      return false;
    }
    return true;
  }

  private ITypeBinding getDeclaredType(Expression expr) {
    IVariableBinding var = TreeUtil.getVariableBinding(expr);
    if (var != null && var.getVariableDeclaration().getType().isTypeVariable()) {
      return Types.resolveIOSType("id");
    }
    switch (expr.getKind()) {
      case CLASS_INSTANCE_CREATION:
        return Types.resolveIOSType("id");
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
              return Types.resolveIOSType("id");
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

  private static ITypeBinding getDeclaredReturnType(
      IMethodBinding method, ITypeBinding receiverType) {
    IMethodBinding actualDeclaration =
        getFirstDeclaration(getObjCMethodSignature(method), receiverType);
    if (actualDeclaration == null) {
      actualDeclaration = method.getMethodDeclaration();
    }
    ITypeBinding returnType = actualDeclaration.getReturnType();
    if (returnType.isTypeVariable()) {
      return Types.resolveIOSType("id");
    }
    return returnType.getErasure();
  }

  /**
   * Finds the declaration for a given method and receiver in the same way that
   * the ObjC compiler will search for a declaration.
   */
  private static IMethodBinding getFirstDeclaration(String methodSig, ITypeBinding type) {
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

  private static String getObjCMethodSignature(IMethodBinding method) {
    StringBuilder sb = new StringBuilder(method.getName());
    boolean first = true;
    for (ITypeBinding paramType : method.getParameterTypes()) {
      String keyword = NameTable.parameterKeyword(paramType);
      if (first) {
        first = false;
        keyword = NameTable.capitalize(keyword);
      }
      sb.append(keyword + ":");
    }
    return sb.toString();
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
  public void endVisit(VariableDeclarationFragment node) {
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      maybeAddCast(initializer, false);
    }
  }
}
