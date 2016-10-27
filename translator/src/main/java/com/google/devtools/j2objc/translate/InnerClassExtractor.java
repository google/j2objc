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
import com.google.devtools.j2objc.ast.EnumDeclaration;
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
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.j2objc.annotations.WeakOuter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

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

  public InnerClassExtractor(CompilationUnit unit) {
    super(unit);
    this.captureInfo = unit.getEnv().captureInfo();
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

      TypeElement type = node.getTypeElement();
      if (type.getKind().isClass() && !ElementUtil.isStatic(type)) {
        addOuterFields(node);
        updateConstructors(node);
      }

      // Make this node non-private, if necessary, and add it to the unit's type
      // list.
      node.removeModifiers(Modifier.PRIVATE);
      unitTypes.add(insertIdx, node);

      // Check for erroneous WeakOuter annotation on static inner class.
      if (ElementUtil.isStatic(type) && ElementUtil.hasAnnotation(type, WeakOuter.class)) {
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
        addOuterParameters(method, node.getTypeElement());
      }
    }
  }

  protected void addOuterParameters(MethodDeclaration constructor, TypeElement type) {
    // Adds the outer and captured parameters to the declaration.
    List<SingleVariableDeclaration> captureDecls = constructor.getParameters().subList(0, 0);

    VariableElement outerParam = captureInfo.getOuterParam(type);
    if (outerParam != null) {
      captureDecls.add(new SingleVariableDeclaration(outerParam));
    }
    VariableElement superOuterParam = captureInfo.getSuperOuterParam(type);
    if (superOuterParam != null) {
      captureDecls.add(new SingleVariableDeclaration(superOuterParam));
    }
    for (VariableElement captureParam : captureInfo.getCaptureParams(type)) {
      captureDecls.add(new SingleVariableDeclaration(captureParam));
    }

    if (TranslationUtil.isDesignatedConstructor(constructor)) {
      addCaptureAssignments(constructor, type);
    }
  }

  private void addCaptureAssignments(MethodDeclaration constructor, TypeElement type) {
    List<Statement> statements = constructor.getBody().getStatements().subList(0, 0);
    VariableElement outerField = captureInfo.getOuterField(type);
    if (outerField != null) {
      VariableElement outerParam = captureInfo.getOuterParam(type);
      assert outerParam != null;
      statements.add(new ExpressionStatement(
          new Assignment(new SimpleName(outerField), new SimpleName(outerParam))));
    }
    for (CaptureInfo.LocalCapture capture : captureInfo.getLocalCaptures(type)) {
      if (capture.hasField()) {
        statements.add(new ExpressionStatement(new Assignment(
            new SimpleName(capture.getField()), new SimpleName(capture.getParam()))));
      }
    }
    if (!hasSuperCall(constructor)) {
      TypeElement superType = ElementUtil.getSuperclass(type);
      statements.add(new SuperConstructorInvocation(
          TranslationUtil.findDefaultConstructorElement(superType, typeUtil)));
    }
  }

  private static boolean hasSuperCall(MethodDeclaration constructor) {
    for (Statement stmt : constructor.getBody().getStatements()) {
      if (stmt instanceof SuperConstructorInvocation) {
        return true;
      }
    }
    return false;
  }
}
