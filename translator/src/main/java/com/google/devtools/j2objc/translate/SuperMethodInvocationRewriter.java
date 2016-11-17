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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Some super method invocations cannot be translated directly as an ObjC super
 * invocation. This occurs when the invocation is qualified by an outer type or
 * when the containing method has been functionized. To resolve these
 * invocations we declare a static function pointer and look up the
 * implementation during static initialization.
 *
 * @author Keith Stanger
 */
public class SuperMethodInvocationRewriter extends UnitTreeVisitor {

  private Set<SuperMethodElementPair> superMethods = new LinkedHashSet<>();
  private Map<TypeElement, AbstractTypeDeclaration> typeMap = new HashMap<>();

  public SuperMethodInvocationRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(CompilationUnit unit) {
    for (SuperMethodElementPair superMethod : superMethods) {
      String funcName = getSuperFunctionName(superMethod);
      String signature = getSuperFunctionSignature(superMethod.method);

      // Add declarations for the function pointers to call.
      unit.addNativeBlock(NativeDeclaration.newOuterDeclaration(
          null, "static " + UnicodeUtils.format(signature, funcName) + ";"));

      // Look up the implementations in the static initialization.
      AbstractTypeDeclaration typeNode = typeMap.get(superMethod.type);
      assert typeNode != null : "Type is expected to be in this compilation unit";
      String superclassName = nameTable.getFullName(ElementUtil.getSuperclass(superMethod.type));
      typeNode.addClassInitStatement(0, new NativeStatement(UnicodeUtils.format(
          "%s = (%s)[%s instanceMethodForSelector:@selector(%s)];",
          funcName, UnicodeUtils.format(signature, ""), superclassName,
          nameTable.getMethodSelector(superMethod.method))));
    }
  }

  private static String getSuperFunctionSignature(ExecutableElement method) {
    StringBuilder signature = new StringBuilder(
        NameTable.getPrimitiveObjCType(method.getReturnType()));
    signature.append(" (*%s)(id, SEL");
    for (VariableElement param : method.getParameters()) {
      signature.append(", ").append(NameTable.getPrimitiveObjCType(param.asType()));
    }
    signature.append(")");
    return signature.toString();
  }

  private String getSuperFunctionName(SuperMethodElementPair superMethod) {
    return UnicodeUtils.format(
        "%s_super$_%s", nameTable.getFullName(superMethod.type),
        nameTable.getFunctionName(superMethod.method));
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    Expression receiver = node.getReceiver();
    ExecutableElement method = node.getExecutableElement();
    TypeMirror exprType = node.getTypeMirror();

    // Handle default method invocation: SomeInterface.super.method(...)
    if (ElementUtil.isDefault(method)) {
      FunctionElement element = new FunctionElement(
          nameTable.getFullFunctionName(method), exprType, ElementUtil.getDeclaringClass(method))
          .addParameters(TypeUtil.ID_TYPE)
          .addParameters(ElementUtil.asTypes(method.getParameters()));
      FunctionInvocation invocation = new FunctionInvocation(element, exprType);
      List<Expression> args = invocation.getArguments();
      if (receiver == null) {
        args.add(new ThisExpression(TreeUtil.getEnclosingTypeElement(node).asType()));
      } else {
        // OuterReferenceResolver has provided an outer path.
        args.add(TreeUtil.remove(receiver));
      }
      TreeUtil.copyList(node.getArguments(), args);
      node.replaceWith(invocation);
      return;
    }

    if (receiver == null) {
      return;
    }
    VariableElement var = TreeUtil.getVariableElement(receiver);
    assert var != null : "Expected receiver to be a variable";
    TypeMirror receiverType = var.asType();
    TypeElement receiverElem = TypeUtil.asTypeElement(receiverType);

    SuperMethodElementPair superMethod = new SuperMethodElementPair(receiverElem, method);
    superMethods.add(superMethod);

    FunctionElement element =
        new FunctionElement(getSuperFunctionName(superMethod), exprType, receiverElem)
        .addParameters(receiverType, TypeUtil.ID_TYPE)
        .addParameters(ElementUtil.asTypes(method.getParameters()));
    FunctionInvocation invocation = new FunctionInvocation(element, exprType);
    List<Expression> args = invocation.getArguments();
    args.add(TreeUtil.remove(receiver));
    String selectorExpr = UnicodeUtils.format("@selector(%s)", nameTable.getMethodSelector(method));
    args.add(new NativeExpression(selectorExpr, TypeUtil.ID_TYPE));
    TreeUtil.copyList(node.getArguments(), args);
    node.replaceWith(invocation);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    typeMap.put(node.getTypeElement(), node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    typeMap.put(node.getTypeElement(), node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    typeMap.put(node.getTypeElement(), node);
  }

  private static class SuperMethodElementPair {
    private final TypeElement type;
    private final ExecutableElement method;

    private SuperMethodElementPair(TypeElement type, ExecutableElement method) {
      this.type = type;
      this.method = method;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SuperMethodElementPair)) {
        return false;
      }
      SuperMethodElementPair other = (SuperMethodElementPair) obj;
      return other.type == type && other.method == method;
    }

    @Override
    public int hashCode() {
      return 31 * System.identityHashCode(type) + System.identityHashCode(method);
    }
  }
}
