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
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.TypeElement;

/**
 * Modifies initializers to be more iOS like.  Static initializers are
 * combined into a static initialize method, instance initializer
 * statements are injected into constructors.  If a class doesn't have
 * any constructors but does have instance initialization statements,
 * a default constructor is added to run them.
 *
 * @author Tom Ball
 */
public class InitializationNormalizer extends UnitTreeVisitor {

  private final CaptureInfo captureInfo;

  public InitializationNormalizer(CompilationUnit unit) {
    super(unit);
    captureInfo = unit.getEnv().captureInfo();
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    new TypeNormalizer(node).normalizeMembers();
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    new TypeNormalizer(node).normalizeMembers();
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    new TypeNormalizer(node).normalizeMembers();
  }

  private class TypeNormalizer {

    private final AbstractTypeDeclaration node;
    private final TypeElement type;
    private final List<Statement> initStatements;
    private final List<Statement> classInitStatements;
    private int constInitIdx = 0;

    private TypeNormalizer(AbstractTypeDeclaration node) {
      this.node = node;
      type = node.getTypeElement();
      initStatements = new ArrayList<>();
      classInitStatements = node.getClassInitStatements();
    }

    private void normalizeMembers() {
      // Scan class, gathering initialization statements in declaration order.
      Iterator<BodyDeclaration> iterator = node.getBodyDeclarations().iterator();
      while (iterator.hasNext()) {
        BodyDeclaration member = iterator.next();
        switch (member.getKind()) {
          case INITIALIZER:
            addInitializer((Initializer) member);
            iterator.remove();
            break;
          case FIELD_DECLARATION:
            addFieldInitializer((FieldDeclaration) member);
            break;
          default:
            // Fall-through.
        }
      }

      // Update any primary constructors with init statements.
      if (type.getKind().isClass()) {
        for (MethodDeclaration methodDecl : TreeUtil.getMethodDeclarations(node)) {
          if (isDesignatedConstructor(methodDecl)) {
            TreeUtil.copyList(initStatements, getInitLocation(methodDecl));
            addCaptureAssignments(methodDecl, type);
          }
        }
      }
    }

    /**
     * Add a static or instance init block's statements to the appropriate list
     * of initialization statements.
     */
    private void addInitializer(Initializer initializer) {
      List<Statement> list =
          Modifier.isStatic(initializer.getModifiers()) ? classInitStatements : initStatements;
      list.add(TreeUtil.remove(initializer.getBody()));
    }

    /**
     * Strip field initializers, convert them to assignment statements, and
     * add them to the appropriate list of initialization statements.
     */
    private void addFieldInitializer(FieldDeclaration field) {
      for (VariableDeclarationFragment frag : field.getFragments()) {
        if (frag.getInitializer() != null) {
          handleFieldInitializer(frag);
        }
      }
    }

    /**
     * Moves the field initializer to the appropriate list of initializer statements.
     */
    private void handleFieldInitializer(VariableDeclarationFragment frag) {
      if (ElementUtil.isInstanceVar(frag.getVariableElement())) {
        // always initialize instance variables, since they can't be constants
        initStatements.add(makeAssignmentStatement(frag));
        return;
      }
      Object constantValue = frag.getInitializer().getConstantValue();
      if (constantValue == null) {
        // The initializer does not have a constant value. Move it to the class initializer.
        classInitStatements.add(makeAssignmentStatement(frag));
        return;
      }
      if (constantValue instanceof String
          && !UnicodeUtils.hasValidCppCharacters((String) constantValue)) {
        // String constant that can't be an ObjC literal. Move it to the class initializer but order
        // constants such as this before other init statements.
        classInitStatements.add(constInitIdx++, makeAssignmentStatement(frag));
        return;
      }
      // The initializer has a constant value, convert it to a literal. (as javac would do)
      frag.setInitializer(TreeUtil.newLiteral(constantValue, typeUtil));
    }
  }

  private ExpressionStatement makeAssignmentStatement(VariableDeclarationFragment fragment) {
    return new ExpressionStatement(new Assignment(
        new SimpleName(fragment.getVariableElement()), TreeUtil.remove(fragment.getInitializer())));
  }

  /**
   * Finds the location in a constructor where init statements should be added.
   */
  private List<Statement> getInitLocation(MethodDeclaration node) {
    List<Statement> stmts = node.getBody().getStatements();
    if (!stmts.isEmpty() && stmts.get(0) instanceof SuperConstructorInvocation) {
      return stmts.subList(0, 1);
    }
    // java.lang.Object supertype is null. All other types should have a super() call.
    assert TypeUtil.isNone(
        ElementUtil.getDeclaringClass(node.getExecutableElement()).getSuperclass())
        : "Constructor didn't have a super() call.";
    return stmts.subList(0, 0);
  }

  private void addCaptureAssignments(MethodDeclaration constructor, TypeElement type) {
    List<Statement> statements = constructor.getBody().getStatements().subList(0, 0);
    for (CaptureInfo.Capture capture : captureInfo.getCaptures(type)) {
      if (capture.hasField()) {
        statements.add(new ExpressionStatement(new Assignment(
            new SimpleName(capture.getField()), new SimpleName(capture.getParam()))));
      }
    }
  }

  /**
   * Returns true if this is a constructor that doesn't call "this(...)".  This constructors are
   * skipped so initializers aren't run more than once per instance creation.
   */
  public static boolean isDesignatedConstructor(MethodDeclaration node) {
    if (!node.isConstructor()) {
      return false;
    }
    Block body = node.getBody();
    if (body == null) {
      return false;
    }
    List<Statement> stmts = body.getStatements();
    return (stmts.isEmpty() || !(stmts.get(0) instanceof ConstructorInvocation));
  }
}
