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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclarationStatement;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.j2objc.annotations.WeakOuter;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds support for inner and anonymous classes, and extracts them to be
 * top-level classes (also like class files). This is similar to how Java
 * compilers convert inner classes into class files, which are all top-level.
 *
 * @author Tom Ball
 */
public class InnerClassExtractor extends TreeVisitor {

  private final List<AbstractTypeDeclaration> unitTypes;
  // Helps keep types in the order they are visited.
  private ArrayList<Integer> typeOrderStack = Lists.newArrayList();

  public InnerClassExtractor(CompilationUnit unit) {
    unitTypes = unit.getTypes();
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    return handleType(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    endHandleType(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return handleType(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    endHandleType(node);
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return handleType(node);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    endHandleType(node);
  }

  private boolean handleType(AbstractTypeDeclaration node) {
    typeOrderStack.add(unitTypes.size());
    return true;
  }

  private void endHandleType(AbstractTypeDeclaration node) {
    int insertIdx = typeOrderStack.remove(typeOrderStack.size() - 1);
    TreeNode parentNode = node.getParent();
    if (!(parentNode instanceof CompilationUnit)) {
      // Remove this type declaration from its current location.
      node.remove();
      if (parentNode instanceof TypeDeclarationStatement) {
        parentNode.remove();
      }

      ITypeBinding type = node.getTypeBinding();
      if (!type.isInterface() && !type.isAnnotation() && !Modifier.isStatic(type.getModifiers())) {
        addOuterFields(node);
        updateConstructors(node);
      }

      // Make this node non-private, if necessary, and add it to the unit's type
      // list.
      node.removeModifiers(Modifier.PRIVATE);
      unitTypes.add(insertIdx, node);

      // Check for erroneous WeakOuter annotation on static inner class.
      if (BindingUtil.isStatic(type) && BindingUtil.hasAnnotation(type, WeakOuter.class)) {
        ErrorUtil.warning("static class " + type.getQualifiedName() + " has WeakOuter annotation");
      }
    }
  }

  private void addOuterFields(AbstractTypeDeclaration node) {
    List<BodyDeclaration> members = node.getBodyDeclarations();
    ITypeBinding clazz = node.getTypeBinding();
    assert clazz.getDeclaringClass() != null;

    IVariableBinding outerFieldBinding = OuterReferenceResolver.getOuterField(clazz);
    if (outerFieldBinding != null) {
      members.add(0, new FieldDeclaration(outerFieldBinding, null));
    }

    List<IVariableBinding> innerFields = OuterReferenceResolver.getInnerFields(clazz);
    for (IVariableBinding field : innerFields) {
      node.getBodyDeclarations().add(new FieldDeclaration(field, null));
    }
  }

  private void updateConstructors(AbstractTypeDeclaration node) {
    // Insert new parameters for each constructor in class.
    boolean needsConstructor = true;
    for (MethodDeclaration method : TreeUtil.getMethodDeclarations(node)) {
      if (method.isConstructor()) {
        needsConstructor = false;
        addOuterParameters(node, method);
      }
    }

    if (needsConstructor) {
      GeneratedMethodBinding binding =
          GeneratedMethodBinding.newConstructor(node.getTypeBinding(), 0);
      MethodDeclaration constructor = new MethodDeclaration(binding);
      constructor.setBody(new Block());
      addOuterParameters(node, constructor);
      node.getBodyDeclarations().add(constructor);
    }
  }

  private GeneratedVariableBinding addParameter(
      MethodDeclaration constructor, ITypeBinding paramType, String name, int idx) {
    GeneratedMethodBinding constructorBinding =
        new GeneratedMethodBinding(constructor.getMethodBinding().getMethodDeclaration());
    constructor.setMethodBinding(constructorBinding);
    GeneratedVariableBinding paramBinding = new GeneratedVariableBinding(
        name, Modifier.FINAL, paramType, false, true, constructorBinding.getDeclaringClass(),
        constructorBinding);
    SingleVariableDeclaration paramNode = new SingleVariableDeclaration(paramBinding);
    if (idx == -1) {
      constructor.getParameters().add(paramNode);
      constructorBinding.addParameter(paramType);
    } else {
      constructor.getParameters().add(idx, paramNode);
      constructorBinding.addParameter(idx, paramType);
    }
    return paramBinding;
  }

  protected void addOuterParameters(
      AbstractTypeDeclaration typeNode, MethodDeclaration constructor) {
    ITypeBinding type = typeNode.getTypeBinding();
    ITypeBinding outerType = type.getDeclaringClass();
    IVariableBinding outerParamBinding = null;
    if (OuterReferenceResolver.needsOuterParam(type)) {
      outerParamBinding = addParameter(constructor, outerType, "outer$", 0);
    }
    List<IVariableBinding> innerFields = OuterReferenceResolver.getInnerFields(type);
    List<IVariableBinding> captureParams = Lists.newArrayListWithCapacity(innerFields.size());
    int captureCount = 0;
    for (IVariableBinding innerField : innerFields) {
      captureParams.add(addParameter(
          constructor, innerField.getType(), "capture$" + captureCount++, -1));
    }

    ConstructorInvocation thisCall = null;
    SuperConstructorInvocation superCall = null;

    List<Statement> statements = constructor.getBody().getStatements();
    for (Statement stmt : statements) {
      if (stmt instanceof ConstructorInvocation) {
        thisCall = (ConstructorInvocation) stmt;
        break;
      } else if (stmt instanceof SuperConstructorInvocation) {
        superCall = (SuperConstructorInvocation) stmt;
        break;
      }
    }

    if (thisCall != null) {
      GeneratedMethodBinding newThisBinding =
          new GeneratedMethodBinding(thisCall.getMethodBinding().getMethodDeclaration());
      thisCall.setMethodBinding(newThisBinding);
      if (outerParamBinding != null) {
        thisCall.getArguments().add(0, new SimpleName(outerParamBinding));
        newThisBinding.addParameter(0, outerParamBinding.getType());
      }
      for (IVariableBinding captureParam : captureParams) {
        thisCall.getArguments().add(new SimpleName(captureParam));
        newThisBinding.addParameter(captureParam.getType());
      }
    } else {
      ITypeBinding superType = type.getSuperclass().getTypeDeclaration();
      if (superCall == null) {
        superCall = new SuperConstructorInvocation(
            GeneratedMethodBinding.newConstructor(superType, Modifier.PUBLIC));
        statements.add(0, superCall);
      }
      passOuterParamToSuper(typeNode, superCall, superType, outerParamBinding);
      IVariableBinding outerField = OuterReferenceResolver.getOuterField(type);
      int idx = 0;
      if (outerField != null) {
        assert outerParamBinding != null;
        statements.add(idx++, new ExpressionStatement(
            new Assignment(new SimpleName(outerField), new SimpleName(outerParamBinding))));
      }
      for (int i = 0; i < innerFields.size(); i++) {
        statements.add(idx++, new ExpressionStatement(new Assignment(
            new SimpleName(innerFields.get(i)), new SimpleName(captureParams.get(i)))));
      }
    }
    assert constructor.getParameters().size()
        == constructor.getMethodBinding().getParameterTypes().length;
  }

  private void passOuterParamToSuper(
      AbstractTypeDeclaration typeNode, SuperConstructorInvocation superCall,
      ITypeBinding superType, IVariableBinding outerParamBinding) {
    if (!BindingUtil.hasOuterContext(superType) || superCall.getExpression() != null) {
      return;
    }
    assert outerParamBinding != null;
    GeneratedMethodBinding superCallBinding =
        new GeneratedMethodBinding(superCall.getMethodBinding().getMethodDeclaration());
    superCall.setMethodBinding(superCallBinding);

    List<IVariableBinding> path = OuterReferenceResolver.getPath(typeNode);
    assert path != null && path.size() > 0;
    path = Lists.newArrayList(path);
    path.set(0, outerParamBinding);
    Name superOuterArg = Name.newName(path);

    superCall.getArguments().add(0, superOuterArg);
    superCallBinding.addParameter(0, superType.getDeclaringClass());
  }
}
