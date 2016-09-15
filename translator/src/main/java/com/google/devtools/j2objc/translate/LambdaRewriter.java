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

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionalExpression;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Rewrites LambdaExpression nodes into TypeDeclarations.
 *
 * @author Nathan Braswell, Keith Stanger
 */
public class LambdaRewriter extends TreeVisitor {

  private final OuterReferenceResolver outerResolver;

  public LambdaRewriter(OuterReferenceResolver outerResolver) {
    this.outerResolver = outerResolver;
  }

  @Override
  public void endVisit(LambdaExpression node) {
    TypeElement lambdaType = node.getTypeElement();
    TypeMirror typeMirror = node.getTypeMirror();
    TypeDeclaration typeDecl = new TypeDeclaration(BindingConverter.unwrapTypeElement(lambdaType));
    typeDecl.setSourceRange(node.getStartPosition(), node.getLength());

    // Add the constructor.
    GeneratedMethodBinding constructorBinding = GeneratedMethodBinding.newConstructor(
        BindingConverter.unwrapTypeElement(lambdaType), java.lang.reflect.Modifier.PRIVATE,
        typeEnv);
    MethodDeclaration constructorDecl = new MethodDeclaration(constructorBinding);
    constructorDecl.setBody(new Block());
    typeDecl.addBodyDeclaration(constructorDecl);

    // Add the functional interface method.
    ExecutableElement fiMethod = ElementUtil.getFunctionalInterface(typeMirror);
    String selector = nameTable.getMethodSelector(fiMethod);
    IOSMethodBinding implBinding = IOSMethodBinding.newMappedMethod(
        selector, BindingConverter.unwrapExecutableElement(fiMethod));
    implBinding.setDeclaringClass(BindingConverter.unwrapTypeElement(lambdaType));
    implBinding.removeModifiers(java.lang.reflect.Modifier.ABSTRACT);
    MethodDeclaration implDecl = new MethodDeclaration(implBinding)
        .setBody(getLambdaBody(node, fiMethod));
    for (VariableDeclaration decl : node.getParameters()) {
      implDecl.addParameter(new SingleVariableDeclaration(decl.getVariableElement()));
    }
    typeDecl.addBodyDeclaration(implDecl);

    TreeUtil.getEnclosingTypeBodyDeclarations(node).add(typeDecl);
    ClassInstanceCreation creation = new ClassInstanceCreation(
        constructorBinding, Type.newType(lambdaType.asType()));

    creation.setKey(node.getKey());

    removeCastExpression(node);
    if (isCapturing(lambdaType)) {
      node.replaceWith(creation);
    } else {
      // For non-capturing lambdas, create a static final instance.
      VariableElement instanceVar = new GeneratedVariableElement(
          "instance", lambdaType.asType(), ElementKind.FIELD, lambdaType)
          .addModifiers(Modifier.STATIC, Modifier.FINAL);
      typeDecl.addBodyDeclaration(new FieldDeclaration(instanceVar, creation));
      node.replaceWith(new SimpleName(instanceVar));
    }
  }

  private boolean isCapturing(TypeElement type) {
    return outerResolver.getOuterField(type) != null
        || !outerResolver.getInnerFields(type).isEmpty();
  }

  private Block getLambdaBody(LambdaExpression node, ExecutableElement fiMethod) {
    TreeNode body = TreeUtil.remove(node.getBody());
    if (body instanceof Block) {
      return (Block) body;
    }
    Block block = new Block();
    if (TypeUtil.isVoid(fiMethod.getReturnType())) {
      block.addStatement(new ExpressionStatement((Expression) body));
    } else {
      block.addStatement(new ReturnStatement((Expression) body));
    }
    return block;
  }

  private void removeCastExpression(FunctionalExpression node) {
    TreeNode parent = node.getParent();
    if (parent instanceof CastExpression) {
      parent.replaceWith(TreeUtil.remove(node));
    }
  }
}
