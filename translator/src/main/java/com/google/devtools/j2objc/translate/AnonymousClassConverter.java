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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.lang.reflect.Modifier;

/**
 * Converts anonymous classes into inner classes.  This includes creating
 * constructors that take the referenced final variables and parameters,
 * but references to other classes remain aren't modified.  By separating
 * anonymous class conversion from inner class extraction, each step can
 * be separately and more thoroughly verified.
 *
 * @author Tom Ball
 */
public class AnonymousClassConverter extends TreeVisitor {

  /**
   * Convert the anonymous class into an inner class.  Fields are added for
   * final variables that are referenced, and a constructor is added.
   *
   * Note: endVisit is used for a depth-first traversal, to make it easier
   * to scan their containing nodes for references.
   */
  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    ITypeBinding typeBinding = node.getTypeBinding();
    ITypeBinding outerType = typeBinding.getDeclaringClass();
    TreeNode parent = node.getParent();
    ClassInstanceCreation newInvocation = null;
    EnumConstantDeclaration enumConstant = null;
    Expression outerExpression = null;
    IMethodBinding constructorBinding = null;
    if (parent instanceof ClassInstanceCreation) {
      newInvocation = (ClassInstanceCreation) parent;
      outerExpression = newInvocation.getExpression();
      newInvocation.setExpression(null);
      constructorBinding = newInvocation.getMethodBinding();
    } else if (parent instanceof EnumConstantDeclaration) {
      enumConstant = (EnumConstantDeclaration) parent;
      constructorBinding = enumConstant.getMethodBinding();
    } else {
      throw new AssertionError(
          "unknown anonymous class declaration parent: " + parent.getClass().getName());
    }

    // Create a type declaration for this anonymous class.
    TypeDeclaration typeDecl = new TypeDeclaration(typeBinding);
    typeDecl.setSourceRange(node.getStartPosition(), node.getLength());

    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      typeDecl.addBodyDeclaration(decl.copy());
    }

    // Add a default constructor.
    GeneratedMethodBinding defaultConstructor =
        addDefaultConstructor(typeDecl, constructorBinding, outerExpression);
    if (newInvocation != null) {
      newInvocation.setMethodBinding(defaultConstructor);
      if (outerExpression != null) {
        newInvocation.addArgument(0, outerExpression);
      }
    } else {
      enumConstant.setMethodBinding(defaultConstructor);
    }

    // If invocation, replace anonymous class invocation with the new constructor.
    if (newInvocation != null) {
      newInvocation.setAnonymousClassDeclaration(null);
      newInvocation.setType(Type.newType(typeBinding));
      IMethodBinding oldBinding = newInvocation.getMethodBinding();
      if (oldBinding != null) {
        GeneratedMethodBinding invocationBinding = new GeneratedMethodBinding(oldBinding);
        invocationBinding.setDeclaringClass(typeBinding);
        newInvocation.setMethodBinding(invocationBinding);
      }
    } else {
      enumConstant.setAnonymousClassDeclaration(null);
    }

    // Add type declaration to enclosing type.
    if (outerType.isAnonymous()) {
      AnonymousClassDeclaration outerDecl =
          TreeUtil.getNearestAncestorWithType(AnonymousClassDeclaration.class, parent);
      outerDecl.addBodyDeclaration(typeDecl);
    } else {
      AbstractTypeDeclaration outerDecl = TreeUtil.getOwningType(parent);
      outerDecl.addBodyDeclaration(typeDecl);
    }
    typeDecl.setKey(node.getKey());
    super.endVisit(node);
  }

  private GeneratedMethodBinding addDefaultConstructor(
      TypeDeclaration node, IMethodBinding constructorBinding, Expression outerExpression) {
    ITypeBinding clazz = node.getTypeBinding();
    GeneratedMethodBinding binding = new GeneratedMethodBinding(constructorBinding);
    MethodDeclaration constructor = new MethodDeclaration(binding);
    constructor.setBody(new Block());

    IMethodBinding superCallBinding = findSuperConstructorBinding(constructorBinding);
    SuperConstructorInvocation superCall = new SuperConstructorInvocation(superCallBinding);

    // If there is an outer expression (eg myFoo.new Foo() {};), then this must
    // be passed to the super class as its outer reference.
    if (outerExpression != null) {
      ITypeBinding outerExpressionType = outerExpression.getTypeBinding();
      GeneratedVariableBinding outerExpressionParam = new GeneratedVariableBinding(
          "superOuter$", Modifier.FINAL, outerExpressionType, false, true, clazz, binding);
      constructor.addParameter(0, new SingleVariableDeclaration(outerExpressionParam));
      binding.addParameter(0, outerExpressionType);
      superCall.setExpression(new SimpleName(outerExpressionParam));
    }

    // The invocation arguments must become parameters of the generated
    // constructor and passed to the super call.
    int argCount = 0;
    for (ITypeBinding argType : constructorBinding.getParameterTypes()) {
      GeneratedVariableBinding argBinding = new GeneratedVariableBinding(
          "arg$" + argCount++, 0, argType, false, true, clazz, binding);
      constructor.addParameter(new SingleVariableDeclaration(argBinding));
      superCall.addArgument(new SimpleName(argBinding));
    }
    assert superCall.getArguments().size() == superCallBinding.getParameterTypes().length
        || superCallBinding.isVarargs()
            && superCall.getArguments().size() >= superCallBinding.getParameterTypes().length - 1;

    constructor.getBody().addStatement(superCall);

    node.addBodyDeclaration(constructor);
    assert constructor.getParameters().size() == binding.getParameterTypes().length;

    return binding;
  }

  private IMethodBinding findSuperConstructorBinding(IMethodBinding constructorBinding) {
    ITypeBinding superClass = constructorBinding.getDeclaringClass().getSuperclass();
    for (IMethodBinding m : superClass.getDeclaredMethods()) {
      if (m.isConstructor() && constructorBinding.isSubsignature(m)) {
        return m;
      }
    }
    throw new AssertionError("could not find constructor");
  }
}
