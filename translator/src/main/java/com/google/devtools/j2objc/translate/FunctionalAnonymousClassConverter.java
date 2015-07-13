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

import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeNode.Kind;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Converts anonymous classes which are functional interfaces into lambdas. This intermediate
 * conversion is to make sure that functional anonymous classes eventually resolve to blocks in
 * Objective-C.
 *
 * @author Seth Kirby
 */
public class FunctionalAnonymousClassConverter extends TreeVisitor {
  // TODO(kirbs): Consider deleting if translation of anonymous classes to lambdas isn't performant.
  private boolean isFunctionalAnonymous(ClassInstanceCreation node) {
    ITypeBinding typeBinding = node.getTypeBinding();
    if (typeBinding == null) {
      return false;
    }
    ITypeBinding[] interfaces = typeBinding.getInterfaces();
    if (interfaces.length != 1) {
      return false;
    }
    if (interfaces[0].getFunctionalInterfaceMethod() == null) {
      return false;
    }
    AnonymousClassDeclaration anonymTypeDecl = node.getAnonymousClassDeclaration();
    if (anonymTypeDecl == null || anonymTypeDecl.getTypeBinding() == null) {
      return false;
    }
    MethodDeclaration methodDecl = null;
    for (BodyDeclaration bd : TreeUtil.getMethodDeclarations(anonymTypeDecl)) {
      // TODO(kirbs): Fix Anonymous classes with overrides.
      // TODO(kirbs): Fix Anonymous classes with polymorphism.
      // TODO(kirbs): Define restrictions on Lambdas being converted in j2objc, especially loss of
      // java.lang.Object features.
      methodDecl = (MethodDeclaration) bd;
    }
    assert methodDecl != null : "methodDecl is broken inside Anonymous Class";
    IMethodBinding methodBinding = methodDecl.getMethodBinding();
    // TODO(kirbs): Add generic lambda support.
    if (methodBinding == null || methodBinding.isGenericMethod()) {
      return false;
    }
    // TODO(kirbs): Add super / this support.
    // TODO(kirbs): Add target context support.

    return true;
  }
  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    if (node.getParent().getKind() != Kind.CLASS_INSTANCE_CREATION
        || !isFunctionalAnonymous((ClassInstanceCreation) node.getParent())) {
      return;
    }
    MethodDeclaration methodDecl = null;
    List<BodyDeclaration> bodyDeclarations = node.getBodyDeclarations();
    for (BodyDeclaration bd : bodyDeclarations) {
      if (bd.getKind() == Kind.METHOD_DECLARATION) {
        methodDecl = (MethodDeclaration) bd;
        break;
      }
    }
    LambdaExpression lambdaExpression = new LambdaExpression(node.getTypeBinding(),
        node.getTypeBinding(), methodDecl.getMethodBinding(), true);
    List<VariableDeclaration> lambdaParameters = lambdaExpression.getParameters();
    for (SingleVariableDeclaration x : methodDecl.getParameters()) {
      VariableDeclarationFragment lambdaParameter = new VariableDeclarationFragment(
          x.getVariableBinding(), x.getInitializer());
      lambdaParameters.add(lambdaParameter);
    }
    ClassInstanceCreation parent = (ClassInstanceCreation) node.getParent();
    Block b = methodDecl.getBody().copy();
    lambdaExpression.setBody(b);
    parent.replaceWith(lambdaExpression);
  }
}
