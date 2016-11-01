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

import com.google.common.collect.Iterables;
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
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.Modifier;

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
    normalizeMembers(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    normalizeMembers(node);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    normalizeMembers(node);
  }


  void normalizeMembers(AbstractTypeDeclaration node) {
    TypeElement type = node.getTypeElement();
    List<Statement> initStatements = Lists.newArrayList();
    List<Statement> classInitStatements = node.getClassInitStatements();

    // Scan class, gathering initialization statements in declaration order.
    Iterator<BodyDeclaration> iterator = node.getBodyDeclarations().iterator();
    while (iterator.hasNext()) {
      BodyDeclaration member = iterator.next();
      switch (member.getKind()) {
        case INITIALIZER:
          addInitializer((Initializer) member, initStatements, classInitStatements);
          iterator.remove();
          break;
        case FIELD_DECLARATION:
          addFieldInitializer((FieldDeclaration) member, initStatements, classInitStatements);
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
  private void addInitializer(Initializer initializer, List<Statement> initStatements,
      List<Statement> classInitStatements) {
    List<Statement> list =
        Modifier.isStatic(initializer.getModifiers()) ? classInitStatements : initStatements;
    list.add(TreeUtil.remove(initializer.getBody()));
  }

  /**
   * Strip field initializers, convert them to assignment statements, and
   * add them to the appropriate list of initialization statements.
   */
  private void addFieldInitializer(
      FieldDeclaration field, List<Statement> initStatements, List<Statement> classInitStatements) {
    for (VariableDeclarationFragment frag : field.getFragments()) {
      if (frag.getInitializer() != null) {
        if (ElementUtil.isInstanceVar(frag.getVariableElement())) {
          // always initialize instance variables, since they can't be constants
          initStatements.add(makeAssignmentStatement(frag));
        } else if (requiresInitializer(frag)) {
          classInitStatements.add(makeAssignmentStatement(frag));
        }
      }
    }
  }

  /**
   * Determines if a variable declaration requires initialization. (ie. cannot
   * be assigned to a literal value in ObjC.
   */
  private boolean requiresInitializer(VariableDeclarationFragment frag) {
    // If the initializer is not a literal, but has a constant value, convert it
    // to a literal. (as javac would do)
    Object constantValue = frag.getInitializer().getConstantValue();
    if (constantValue != null) {
      if (constantValue instanceof String
          && !UnicodeUtils.hasValidCppCharacters((String) constantValue)) {
        return true;
      }
      frag.setInitializer(TreeUtil.newLiteral(constantValue, typeEnv));
      return false;
    }
    return true;
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
    TypeMirror superType =
        ElementUtil.getDeclaringClass(node.getExecutableElement()).getSuperclass();
    if (TypeUtil.isNone(superType)) {  // java.lang.Object supertype is null.
      return stmts.subList(0, 0);
    }
    // If there isn't a super invocation, add one (like all Java compilers do).
    stmts.add(0, createDefaultSuperCall((DeclaredType) superType));
    return stmts.subList(0, 1);
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
  }

  private SuperConstructorInvocation createDefaultSuperCall(DeclaredType type) {
    TypeElement typeElem = TypeUtil.asTypeElement(type);
    if (ElementUtil.getQualifiedName(typeElem).equals("java.lang.Enum")) {
      // Enums are a special case where instead of a default no-param constructor it has a single
      // two param constructor that accepts the implicit name and ordinal values.
      ExecutableElement enumConstructor =
          Iterables.getFirst(ElementUtil.getConstructors(typeElem), null);
      assert enumConstructor != null && enumConstructor.getParameters().size() == 2;
      SuperConstructorInvocation superCall = new SuperConstructorInvocation(
          new ExecutablePair(enumConstructor, typeUtil.asMemberOf(type, enumConstructor)));
      for (VariableElement param : captureInfo.getImplicitEnumParams()) {
        superCall.addArgument(new SimpleName(param));
      }
      return superCall;
    }
    ExecutableElement defaultConstructor = findDefaultConstructorElement(typeElem);
    return new SuperConstructorInvocation(new ExecutablePair(
        defaultConstructor, typeUtil.asMemberOf(type, defaultConstructor)));
  }

  private ExecutableElement findDefaultConstructorElement(TypeElement type) {
    ExecutableElement result = null;
    for (ExecutableElement c : ElementUtil.getConstructors(type)) {
      // Search for a non-varargs match.
      if (c.getParameters().isEmpty()) {
        return c;
      // Search for a varargs match. Choose the most specific. (JLS 15.12.2.5)
      } else if (c.isVarArgs() && c.getParameters().size() == 1
          && (result == null || typeUtil.isAssignable(
              c.getParameters().get(0).asType(), result.getParameters().get(0).asType()))) {
        result = c;
      }
    }
    assert result != null : "Couldn't find default constructor for " + type;
    return result;
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
