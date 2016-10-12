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
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;

/**
 * Converts anonymous classes into inner classes.  This includes creating
 * constructors that take the referenced final variables and parameters,
 * but references to other classes remain aren't modified.  By separating
 * anonymous class conversion from inner class extraction, each step can
 * be separately and more thoroughly verified.
 *
 * @author Tom Ball
 */
public class AnonymousClassConverter extends UnitTreeVisitor {

  public AnonymousClassConverter(CompilationUnit unit) {
    super(unit);
  }

  /**
   * Convert the anonymous class into an inner class.  Fields are added for
   * final variables that are referenced, and a constructor is added.
   *
   * Note: endVisit is used for a depth-first traversal, to make it easier
   * to scan their containing nodes for references.
   */
  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    TypeElement typeElement = node.getTypeElement();
    ExecutableElement constructorElement = null;

    TreeNode parent = node.getParent();
    if (parent instanceof ClassInstanceCreation) {
      ClassInstanceCreation newInvocation = (ClassInstanceCreation) parent;
      constructorElement = newInvocation.getExecutableElement();
      newInvocation.setAnonymousClassDeclaration(null);
      newInvocation.setType(Type.newType(typeElement.asType()));
    } else if (parent instanceof EnumConstantDeclaration) {
      EnumConstantDeclaration enumConstant = (EnumConstantDeclaration) parent;
      constructorElement = enumConstant.getExecutableElement();
      enumConstant.setAnonymousClassDeclaration(null);
    } else {
      throw new AssertionError(
          "unknown anonymous class declaration parent: " + parent.getClass().getName());
    }

    // Create a type declaration for this anonymous class.
    TypeDeclaration typeDecl = new TypeDeclaration(typeElement);
    typeDecl.setSourceRange(node.getStartPosition(), node.getLength());
    TreeUtil.moveList(node.getBodyDeclarations(), typeDecl.getBodyDeclarations());
    TreeUtil.moveList(node.getSuperCaptureArgs(), typeDecl.getSuperCaptureArgs());

    // Add a default constructor.
    addDefaultConstructor(typeDecl, constructorElement);

    // Add type declaration to enclosing type.
    TreeUtil.getEnclosingTypeBodyDeclarations(parent).add(typeDecl);
    typeDecl.setSuperOuter(TreeUtil.remove(node.getSuperOuter()));
    super.endVisit(node);
  }

  private void addDefaultConstructor(TypeDeclaration node, ExecutableElement constructorElement) {
    MethodDeclaration constructor = new MethodDeclaration(constructorElement);
    constructor.setBody(new Block());

    ExecutableElement superCallElement = findSuperConstructorElement(constructorElement);
    SuperConstructorInvocation superCall = new SuperConstructorInvocation(superCallElement);

    // The invocation arguments must become parameters of the generated
    // constructor and passed to the super call.
    int argCount = 0;
    for (VariableElement param : constructorElement.getParameters()) {
      GeneratedVariableElement newParam = new GeneratedVariableElement(
          "arg$" + argCount++, param.asType(), ElementKind.PARAMETER, constructorElement);
      constructor.addParameter(new SingleVariableDeclaration(newParam));
      superCall.addArgument(new SimpleName(newParam));
    }
    assert (superCall.getArguments().size() == superCallElement.getParameters().size())
        || (superCallElement.isVarArgs()
            && superCall.getArguments().size() >= superCallElement.getParameters().size() - 1);

    constructor.getBody().addStatement(superCall);

    node.addBodyDeclaration(constructor);
    assert constructor.getParameters().size() == constructorElement.getParameters().size();
  }

  private ExecutableElement findSuperConstructorElement(ExecutableElement constructorElement) {
    DeclaredType superClass =
        (DeclaredType) ElementUtil.getDeclaringClass(constructorElement).getSuperclass();
    for (ExecutableElement m : ElementUtil.getDeclaredMethods(TypeUtil.asTypeElement(superClass))) {
      if (ElementUtil.isConstructor(m)) {
        ExecutableType mType = (ExecutableType) env.typeUtilities().asMemberOf(superClass, m);
        if (env.typeUtilities().isSubsignature(
            (ExecutableType) constructorElement.asType(), mType)) {
          return m;
        }
      }
    }
    throw new AssertionError("could not find constructor");
  }
}
