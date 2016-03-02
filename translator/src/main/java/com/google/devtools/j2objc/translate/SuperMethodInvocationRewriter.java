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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Some super method invocations cannot be translated directly as an ObjC super
 * invocation. This occurs when the invocation is qualified by an outer type or
 * when the containing method has been functionized. To resolve these
 * invocations we declare a static function pointer and look up the
 * implementation during static initialization.
 *
 * @author Keith Stanger
 */
public class SuperMethodInvocationRewriter extends TreeVisitor {

  private Set<SuperMethodBindingPair> superMethods;
  private Map<ITypeBinding, AbstractTypeDeclaration> typeMap;

  @Override
  public boolean visit(CompilationUnit unit) {
    superMethods = Sets.newLinkedHashSet();
    typeMap = Maps.newHashMap();
    return true;
  }

  @Override
  public void endVisit(CompilationUnit unit) {
    for (SuperMethodBindingPair superMethod : superMethods) {
      String funcName = getSuperFunctionName(superMethod);
      String signature = getSuperFunctionSignature(superMethod.method);

      // Add declarations for the function pointers to call.
      unit.getNativeBlocks().add(NativeDeclaration.newOuterDeclaration(
          null, "static " + UnicodeUtils.format(signature, funcName) + ";"));

      // Look up the implementations in the static initialization.
      AbstractTypeDeclaration typeNode = typeMap.get(superMethod.type.getTypeDeclaration());
      assert typeNode != null : "Type is expected to be in this compilation unit";
      String superclassName = nameTable.getFullName(superMethod.type.getSuperclass());
      typeNode.getClassInitStatements().add(0, new NativeStatement(UnicodeUtils.format(
          "%s = (%s)[%s instanceMethodForSelector:@selector(%s)];",
          funcName, UnicodeUtils.format(signature, ""), superclassName,
          nameTable.getMethodSelector(superMethod.method))));
    }
  }

  private static String getSuperFunctionSignature(IMethodBinding method) {
    StringBuilder signature = new StringBuilder(
        NameTable.getPrimitiveObjCType(method.getReturnType()));
    signature.append(" (*%s)(id, SEL");
    for (ITypeBinding paramType : method.getParameterTypes()) {
      signature.append(", ").append(NameTable.getPrimitiveObjCType(paramType));
    }
    signature.append(")");
    return signature.toString();
  }

  private String getSuperFunctionName(SuperMethodBindingPair superMethod) {
    return UnicodeUtils.format("%s_super$_%s", nameTable.getFullName(superMethod.type),
                         nameTable.getFunctionName(superMethod.method));
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    Name qualifier = node.getQualifier();
    if (qualifier == null) {
      return;
    }
    IMethodBinding method = node.getMethodBinding();
    ITypeBinding exprType = node.getTypeBinding();

    // Handle default method invocation: SomeInterface.super.method(...)
    if (BindingUtil.isDefault(method)) {
      FunctionBinding binding = new FunctionBinding(
              nameTable.getFullFunctionName(method), exprType, typeEnv.getIdType());
      binding.addParameters(typeEnv.getIdType());
      binding.addParameters(method.getParameterTypes());
      FunctionInvocation invocation = new FunctionInvocation(binding, exprType);
      List<Expression> args = invocation.getArguments();
      ITypeBinding thisClass = TreeUtil.getOwningType(node).getTypeBinding();
      args.add(new ThisExpression(thisClass));
      TreeUtil.copyList(node.getArguments(), args);
      node.replaceWith(invocation);
      return;
    }

    IVariableBinding var = TreeUtil.getVariableBinding(qualifier);
    assert var != null : "Expected qualifier to be a variable";
    ITypeBinding qualifierType = var.getType();

    SuperMethodBindingPair superMethod = new SuperMethodBindingPair(qualifierType, method);
    superMethods.add(superMethod);

    FunctionBinding binding = new FunctionBinding(
        getSuperFunctionName(superMethod), exprType, qualifierType);
    binding.addParameters(qualifierType, typeEnv.getIdType());
    binding.addParameters(method.getParameterTypes());
    FunctionInvocation invocation = new FunctionInvocation(binding, exprType);
    List<Expression> args = invocation.getArguments();
    args.add(TreeUtil.remove(qualifier));
    String selectorExpr = UnicodeUtils.format("@selector(%s)", nameTable.getMethodSelector(method));
    args.add(new NativeExpression(selectorExpr, typeEnv.getIdType()));
    TreeUtil.copyList(node.getArguments(), args);
    node.replaceWith(invocation);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    typeMap.put(node.getTypeBinding(), node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    typeMap.put(node.getTypeBinding(), node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    typeMap.put(node.getTypeBinding(), node);
  }

  private static class SuperMethodBindingPair {
    private final ITypeBinding type;
    private final IMethodBinding method;

    private SuperMethodBindingPair(ITypeBinding type, IMethodBinding method) {
      this.type = type;
      this.method = method;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SuperMethodBindingPair)) {
        return false;
      }
      SuperMethodBindingPair other = (SuperMethodBindingPair) obj;
      return other.type == type && other.method == method;
    }

    @Override
    public int hashCode() {
      return 31 * System.identityHashCode(type) + System.identityHashCode(method);
    }
  }
}
