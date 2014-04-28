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
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.RenamedTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
public class AnonymousClassConverter extends ErrorReportingASTVisitor {

  private final CompilationUnit unit;

  public AnonymousClassConverter(CompilationUnit unit) {
    this.unit = unit;
  }

  @Override
  public boolean visit(CompilationUnit node) {
    preProcessUnit(node);
    return true;
  }

  @VisibleForTesting
  void preProcessUnit(CompilationUnit node) {
    node.accept(new AnonymousClassRenamer());
  }

  // Uncomment to verify this translator's changes.
  //  @Override
  //  public void endVisit(CompilationUnit node) {
  //    Types.verifyNode(node);
  //  }

  /**
   * Convert the anonymous class into an inner class.  Fields are added for
   * final variables that are referenced, and a constructor is added.
   *
   * Note: endVisit is used for a depth-first traversal, to make it easier
   * to scan their containing nodes for references.
   */
  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    ITypeBinding typeBinding = Types.getTypeBinding(node);
    ITypeBinding outerType = typeBinding.getDeclaringClass();
    ASTNode parent = node.getParent();
    ClassInstanceCreation newInvocation = null;
    EnumConstantDeclaration enumConstant = null;
    List<Expression> parentArguments;
    Expression outerExpression = null;
    String newClassName = typeBinding.getName();
    ITypeBinding innerType = RenamedTypeBinding.rename(newClassName, outerType, typeBinding, 0);
    if (parent instanceof ClassInstanceCreation) {
      newInvocation = (ClassInstanceCreation) parent;
      parentArguments = ASTUtil.getArguments(newInvocation);
      outerExpression = newInvocation.getExpression();
      newInvocation.setExpression(null);
    } else if (parent instanceof EnumConstantDeclaration) {
      enumConstant = (EnumConstantDeclaration) parent;
      parentArguments = ASTUtil.getArguments(enumConstant);
    } else {
      throw new AssertionError(
          "unknown anonymous class declaration parent: " + parent.getClass().getName());
    }

    // Create a type declaration for this anonymous class.
    AST ast = node.getAST();
    TypeDeclaration typeDecl = ast.newTypeDeclaration();
    Types.addBinding(typeDecl, innerType);
    typeDecl.setName(ast.newSimpleName(newClassName));
    Types.addBinding(typeDecl.getName(), innerType);
    typeDecl.setSourceRange(node.getStartPosition(), node.getLength());

    Type superType = ASTFactory.newType(ast, Types.mapType(innerType.getSuperclass()));
    typeDecl.setSuperclassType(superType);
    for (ITypeBinding interfaceType : innerType.getInterfaces()) {
      ASTUtil.getSuperInterfaceTypes(typeDecl).add(
          ASTFactory.newType(ast, Types.mapType(interfaceType)));
    }

    for (Object bodyDecl : node.bodyDeclarations()) {
      BodyDeclaration decl = (BodyDeclaration) bodyDecl;
      ASTUtil.getBodyDeclarations(typeDecl).add(NodeCopier.copySubtree(ast, decl));
    }

    // Add a default constructor.
    if (!parentArguments.isEmpty() || outerExpression != null) {
      GeneratedMethodBinding defaultConstructor =
          addDefaultConstructor(typeDecl, parentArguments, outerExpression);
      Types.addBinding(parent, defaultConstructor);
      if (outerExpression != null) {
        parentArguments.add(0, NodeCopier.copySubtree(ast, outerExpression));
      }
      assert defaultConstructor.getParameterTypes().length == parentArguments.size();
    }

    // If invocation, replace anonymous class invocation with the new constructor.
    if (newInvocation != null) {
      newInvocation.setAnonymousClassDeclaration(null);
      newInvocation.setType(ASTFactory.newType(ast, innerType));
      IMethodBinding oldBinding = Types.getMethodBinding(newInvocation);
      if (oldBinding != null) {
        GeneratedMethodBinding invocationBinding = new GeneratedMethodBinding(oldBinding);
        invocationBinding.setDeclaringClass(innerType);
        Types.addBinding(newInvocation, invocationBinding);
      }
    } else {
      enumConstant.setAnonymousClassDeclaration(null);
    }

    // Add type declaration to enclosing type.
    if (outerType.isAnonymous()) {
      // Get outerType node.
      ASTNode n = parent.getParent();
      while (!(n instanceof AnonymousClassDeclaration) && !(n instanceof TypeDeclaration)) {
        n = n.getParent();
      }
      if (n instanceof AnonymousClassDeclaration) {
        AnonymousClassDeclaration outerDecl = (AnonymousClassDeclaration) n;
        ASTUtil.getBodyDeclarations(outerDecl).add(typeDecl);
      }
    } else {
      AbstractTypeDeclaration outerDecl =
          (AbstractTypeDeclaration) unit.findDeclaringNode(outerType);
      ASTUtil.getBodyDeclarations(outerDecl).add(typeDecl);
    }
    OuterReferenceResolver.copyNode(node, typeDecl);
    super.endVisit(node);
  }

  private GeneratedMethodBinding addDefaultConstructor(
      TypeDeclaration node, List<Expression> invocationArguments, Expression outerExpression) {
    AST ast = node.getAST();
    ITypeBinding clazz = Types.getTypeBinding(node);
    MethodDeclaration constructor = ast.newMethodDeclaration();
    constructor.setConstructor(true);
    ITypeBinding voidType = ast.resolveWellKnownType("void");
    GeneratedMethodBinding binding = GeneratedMethodBinding.newConstructor(clazz, 0);
    Types.addBinding(constructor, binding);
    Types.addBinding(constructor.getReturnType2(), voidType);
    SimpleName name = ast.newSimpleName("init");
    Types.addBinding(name, binding);
    constructor.setName(name);
    constructor.setBody(ast.newBlock());

    IMethodBinding superCallBinding =
        findSuperConstructorBinding(clazz.getSuperclass(), invocationArguments);
    SuperConstructorInvocation superCall =
        ASTFactory.newSuperConstructorInvocation(ast, superCallBinding);

    // If there is an outer expression (eg myFoo.new Foo() {};), then this must
    // be passed to the super class as its outer reference.
    if (outerExpression != null) {
      ITypeBinding outerExpressionType = Types.getTypeBinding(outerExpression);
      GeneratedVariableBinding outerExpressionParam = new GeneratedVariableBinding(
          "superOuter$", Modifier.FINAL, outerExpressionType, false, true, clazz, binding);
      ASTUtil.getParameters(constructor).add(0,
          ASTFactory.newSingleVariableDeclaration(ast, outerExpressionParam));
      binding.addParameter(0, outerExpressionType);
      superCall.setExpression(ASTFactory.newSimpleName(ast, outerExpressionParam));
    }

    // The invocation arguments must become parameters of the generated
    // constructor and passed to the super call.
    int argCount = 0;
    for (Expression arg : invocationArguments) {
      ITypeBinding argType =
          arg instanceof NullLiteral ? Types.getNSObject() : Types.getTypeBinding(arg);
      GeneratedVariableBinding argBinding = new GeneratedVariableBinding(
          "arg$" + argCount++, 0, argType, false, true, clazz, binding);
      ASTUtil.getParameters(constructor).add(
          ASTFactory.newSingleVariableDeclaration(ast, argBinding));
      binding.addParameter(argType);
      ASTUtil.getArguments(superCall).add(ASTFactory.newSimpleName(ast, argBinding));
    }
    assert superCall.arguments().size() == superCallBinding.getParameterTypes().length ||
        superCallBinding.isVarargs() &&
            superCall.arguments().size() >= superCallBinding.getParameterTypes().length;

    ASTUtil.getStatements(constructor.getBody()).add(superCall);

    ASTUtil.getBodyDeclarations(node).add(constructor);
    assert constructor.parameters().size() == binding.getParameterTypes().length;

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
        if (superArgs.size() == paramTypes.length ||
            m.isVarargs() && superArgs.size() >= paramTypes.length) {
          for (int i = 0; i < paramTypes.length; i++) {
            if (m.isVarargs() && i == (paramTypes.length - 1)) {
              // Matched through vararg parameter.
              break;
            }
            ITypeBinding argType = Types.getTypeBinding(superArgs.get(i)).getErasure();
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
  static class AnonymousClassRenamer extends ASTVisitor {

    private static class Frame {
      int classCount = 0;
    }
    final Stack<Frame> classIndex = new Stack<Frame>();

    @Override
    public boolean visit(TypeDeclaration node) {
      return processType(node);
    }

    @Override
    public boolean visit(EnumDeclaration node) {
      return processType(node);
    }

    private boolean processType(AbstractTypeDeclaration node) {
      classIndex.push(new Frame());
      return true;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
      Frame parentFrame = classIndex.peek();

      String className = "$" + ++parentFrame.classCount;
      ITypeBinding innerType = renameClass(className, Types.getTypeBinding(node));
      Types.addBinding(node, innerType);
      NameTable.rename(Types.getTypeBinding(node), className);

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
