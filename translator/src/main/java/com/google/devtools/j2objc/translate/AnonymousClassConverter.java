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

import com.google.common.annotations.VisibleForTesting;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NullLiteral;
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
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Stack;

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

  @Override
  public boolean visit(CompilationUnit node) {
    preProcessUnit(node);
    return true;
  }

  @VisibleForTesting
  void preProcessUnit(CompilationUnit node) {
    node.accept(new AnonymousClassRenamer());
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
    ITypeBinding typeBinding = node.getTypeBinding();
    ITypeBinding outerType = typeBinding.getDeclaringClass();
    TreeNode parent = node.getParent();
    ClassInstanceCreation newInvocation = null;
    EnumConstantDeclaration enumConstant = null;
    List<Expression> parentArguments;
    Expression outerExpression = null;
    if (parent instanceof ClassInstanceCreation) {
      newInvocation = (ClassInstanceCreation) parent;
      parentArguments = newInvocation.getArguments();
      outerExpression = newInvocation.getExpression();
      newInvocation.setExpression(null);
    } else if (parent instanceof EnumConstantDeclaration) {
      enumConstant = (EnumConstantDeclaration) parent;
      parentArguments = enumConstant.getArguments();
    } else {
      throw new AssertionError(
          "unknown anonymous class declaration parent: " + parent.getClass().getName());
    }

    // Create a type declaration for this anonymous class.
    TypeDeclaration typeDecl = new TypeDeclaration(typeBinding);
    typeDecl.setSourceRange(node.getStartPosition(), node.getLength());

    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      typeDecl.getBodyDeclarations().add(decl.copy());
    }

    // Add a default constructor.
    if (!parentArguments.isEmpty() || outerExpression != null) {
      GeneratedMethodBinding defaultConstructor =
          addDefaultConstructor(typeDecl, parentArguments, outerExpression);
      if (newInvocation != null) {
        newInvocation.setMethodBinding(defaultConstructor);
      } else {
        enumConstant.setMethodBinding(defaultConstructor);
      }
      if (outerExpression != null) {
        parentArguments.add(0, outerExpression.copy());
      }
      assert defaultConstructor.getParameterTypes().length == parentArguments.size();
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
      outerDecl.getBodyDeclarations().add(typeDecl);
    } else {
      AbstractTypeDeclaration outerDecl = TreeUtil.getOwningType(parent);
      outerDecl.getBodyDeclarations().add(typeDecl);
    }
    typeDecl.setKey(node.getKey());
    super.endVisit(node);
  }

  private GeneratedMethodBinding addDefaultConstructor(
      TypeDeclaration node, List<Expression> invocationArguments, Expression outerExpression) {
    ITypeBinding clazz = node.getTypeBinding();
    GeneratedMethodBinding binding = GeneratedMethodBinding.newConstructor(clazz, 0);
    MethodDeclaration constructor = new MethodDeclaration(binding);
    constructor.setBody(new Block());

    IMethodBinding superCallBinding =
        findSuperConstructorBinding(clazz.getSuperclass(), invocationArguments);
    SuperConstructorInvocation superCall = new SuperConstructorInvocation(superCallBinding);

    // If there is an outer expression (eg myFoo.new Foo() {};), then this must
    // be passed to the super class as its outer reference.
    if (outerExpression != null) {
      ITypeBinding outerExpressionType = outerExpression.getTypeBinding();
      GeneratedVariableBinding outerExpressionParam = new GeneratedVariableBinding(
          "superOuter$", Modifier.FINAL, outerExpressionType, false, true, clazz, binding);
      constructor.getParameters().add(0, new SingleVariableDeclaration(outerExpressionParam));
      binding.addParameter(0, outerExpressionType);
      superCall.setExpression(new SimpleName(outerExpressionParam));
    }

    // The invocation arguments must become parameters of the generated
    // constructor and passed to the super call.
    int argCount = 0;
    for (Expression arg : invocationArguments) {
      ITypeBinding argType =
          arg instanceof NullLiteral ? Types.getNSObject() : arg.getTypeBinding();
      GeneratedVariableBinding argBinding = new GeneratedVariableBinding(
          "arg$" + argCount++, 0, argType, false, true, clazz, binding);
      constructor.getParameters().add(new SingleVariableDeclaration(argBinding));
      binding.addParameter(argType);
      superCall.getArguments().add(new SimpleName(argBinding));
    }
    assert superCall.getArguments().size() == superCallBinding.getParameterTypes().length
        || superCallBinding.isVarargs()
            && superCall.getArguments().size() >= superCallBinding.getParameterTypes().length;

    constructor.getBody().getStatements().add(superCall);

    node.getBodyDeclarations().add(constructor);
    assert constructor.getParameters().size() == binding.getParameterTypes().length;

    return binding;
  }

  private IMethodBinding findSuperConstructorBinding(ITypeBinding clazz,
      List<Expression> superArgs) {
    if (clazz == null) {
      throw new AssertionError("could not find constructor");
    }
    outer: for (IMethodBinding m : clazz.getDeclaredMethods()) {
      if (m.isConstructor()) {
        ITypeBinding[] paramTypes = m.getParameterTypes();
        if (superArgs.size() == paramTypes.length
            || m.isVarargs() && superArgs.size() >= paramTypes.length) {
          for (int i = 0; i < paramTypes.length; i++) {
            if (m.isVarargs() && i == (paramTypes.length - 1)) {
              // Matched through vararg parameter.
              break;
            }
            ITypeBinding argType = superArgs.get(i).getTypeBinding().getErasure();
            if (!argType.isAssignmentCompatible(paramTypes[i].getErasure())) {
              continue outer;
            }
          }
          return m;
        }
      }
    }
    return findSuperConstructorBinding(clazz.getSuperclass(), superArgs);
  }

  /**
   * Rename anonymous classes to class file-like $n names, where n is the
   * index of the number of anonymous classes for the parent type.  A stack
   * is used to ensure that anonymous classes defined inside of other
   * anonymous classes are numbered correctly.
   */
  static class AnonymousClassRenamer extends TreeVisitor {

    private static class Frame {
      int classCount = 0;
    }
    final Stack<Frame> classIndex = new Stack<Frame>();

    @Override
    public boolean visit(TypeDeclaration node) {
      return processType();
    }

    @Override
    public boolean visit(EnumDeclaration node) {
      return processType();
    }

    private boolean processType() {
      classIndex.push(new Frame());
      return true;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
      Frame parentFrame = classIndex.peek();

      String className = "$" + ++parentFrame.classCount;
      ITypeBinding innerType = renameClass(className, node.getTypeBinding());
      node.setTypeBinding(innerType);
      NameTable.rename(node.getTypeBinding(), className);

      classIndex.push(new Frame());
      return true;
    }

    private ITypeBinding renameClass(String name, ITypeBinding oldBinding) {
      ITypeBinding outerType = Types.getRenamedBinding(oldBinding.getDeclaringClass());
      NameTable.rename(oldBinding, name);
      ITypeBinding newBinding = Types.renameTypeBinding(name, outerType, oldBinding);
      assert newBinding.getName().equals(name);
      return newBinding;
    }

    @Override
    public void endVisit(TypeDeclaration node) {
      classIndex.pop();
    }

    @Override
    public void endVisit(EnumDeclaration node) {
      classIndex.pop();
    }

    @Override
    public void endVisit(AnonymousClassDeclaration node) {
      classIndex.pop();
    }
  }
}
