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
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclarationStatement;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.j2objc.annotations.WeakOuter;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Adds support for inner and anonymous classes, and extracts them to be
 * top-level classes (also like class files). This is similar to how Java
 * compilers convert inner classes into class files, which are all top-level.
 *
 * @author Tom Ball
 */
public class InnerClassExtractor extends UnitTreeVisitor {

  private final CaptureInfo captureInfo;
  private final List<AbstractTypeDeclaration> unitTypes;
  // Helps keep types in the order they are visited.
  private ArrayList<Integer> typeOrderStack = Lists.newArrayList();

  public InnerClassExtractor(CompilationUnit unit, CaptureInfo captureInfo) {
    super(unit);
    this.captureInfo = captureInfo;
    unitTypes = unit.getTypes();
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    return handleType();
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    endHandleType(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return handleType();
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    endHandleType(node);
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return handleType();
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    endHandleType(node);
  }

  private boolean handleType() {
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
    TypeElement clazz = node.getTypeElement();

    VariableElement outerFieldElement = captureInfo.getOuterField(clazz);
    if (outerFieldElement != null) {
      members.add(0, new FieldDeclaration(outerFieldElement, null));
    }

    for (VariableElement field : captureInfo.getCaptureFields(clazz)) {
      node.addBodyDeclaration(new FieldDeclaration(field, null));
    }
  }

  private void updateConstructors(AbstractTypeDeclaration node) {
    // Insert new parameters for each constructor in class.
    for (MethodDeclaration method : TreeUtil.getMethodDeclarations(node)) {
      if (method.isConstructor()) {
        addOuterParameters(node, method);
      }
    }
  }

  protected void addOuterParameters(
      AbstractTypeDeclaration typeNode, MethodDeclaration constructor) {
    ITypeBinding type = typeNode.getTypeBinding();
    TypeElement typeE = typeNode.getTypeElement();
    VariableElement outerParam = captureInfo.getOuterParam(typeE);
    VariableElement superOuterParam = captureInfo.getSuperOuterParam(typeE);

    GeneratedMethodBinding constructorBinding =
        new GeneratedMethodBinding(constructor.getMethodBinding().getMethodDeclaration());
    constructor.setMethodBinding(constructorBinding);

    // Adds the outer and captured parameters to the declaration.
    List<SingleVariableDeclaration> captureDecls = constructor.getParameters().subList(0, 0);
    List<ITypeBinding> captureTypes = constructorBinding.getParameters().subList(0, 0);
    if (outerParam != null) {
      captureDecls.add(new SingleVariableDeclaration(outerParam));
      captureTypes.add(BindingConverter.unwrapTypeMirrorIntoTypeBinding(outerParam.asType()));
    }
    if (superOuterParam != null) {
      captureDecls.add(new SingleVariableDeclaration(superOuterParam));
      captureTypes.add(BindingConverter.unwrapTypeMirrorIntoTypeBinding(superOuterParam.asType()));
    }
    for (VariableElement captureParam : captureInfo.getCaptureParams(typeE)) {
      captureDecls.add(new SingleVariableDeclaration(captureParam));
      captureTypes.add(BindingConverter.unwrapTypeMirrorIntoTypeBinding(captureParam.asType()));
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
      List<Expression> args = thisCall.getArguments().subList(0, 0);
      List<ITypeBinding> params = newThisBinding.getParameters().subList(0, 0);
      if (outerParam != null) {
        args.add(new SimpleName(outerParam));
        params.add(BindingConverter.unwrapTypeMirrorIntoTypeBinding(outerParam.asType()));
      }
      for (VariableElement captureParam : captureInfo.getCaptureParams(typeE)) {
        args.add(new SimpleName(captureParam));
        params.add(BindingConverter.unwrapTypeMirrorIntoTypeBinding(captureParam.asType()));
      }
    } else {
      ITypeBinding superType = type.getSuperclass().getTypeDeclaration();
      if (superCall == null) {
        superCall = new SuperConstructorInvocation(
            TranslationUtil.findDefaultConstructorBinding(superType, typeEnv));
        statements.add(0, superCall);
      }
      VariableElement outerField = captureInfo.getOuterField(typeE);
      int idx = 0;
      if (outerField != null) {
        assert outerParam != null;
        statements.add(idx++, new ExpressionStatement(
            new Assignment(new SimpleName(outerField), new SimpleName(outerParam))));
      }
      for (CaptureInfo.LocalCapture capture : captureInfo.getLocalCaptures(typeE)) {
        if (capture.hasField()) {
          statements.add(idx++, new ExpressionStatement(new Assignment(
              new SimpleName(capture.getField()), new SimpleName(capture.getParam()))));
        }
      }
    }
    assert constructor.getParameters().size()
        == constructor.getMethodBinding().getParameterTypes().length;
  }
}
