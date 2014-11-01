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
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Iterator;
import java.util.List;

/**
 * Modifies initializers to be more iOS like.  Static initializers are
 * combined into a static initialize method, instance initializer
 * statements are injected into constructors.  If a class doesn't have
 * any constructors but does have instance initialization statements,
 * a default constructor is added to run them.
 *
 * @author Tom Ball
 */
public class InitializationNormalizer extends TreeVisitor {

  @Override
  public void endVisit(TypeDeclaration node) {
    normalizeMembers(node);
    super.endVisit(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    normalizeMembers(node);
    super.endVisit(node);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    normalizeMembers(node);
    super.endVisit(node);
  }


  void normalizeMembers(AbstractTypeDeclaration node) {
    List<Statement> initStatements = Lists.newArrayList();
    List<Statement> classInitStatements = node.getClassInitStatements();
    List<MethodDeclaration> methods = Lists.newArrayList();
    ITypeBinding binding = node.getTypeBinding();

    // Scan class, gathering initialization statements in declaration order.
    List<BodyDeclaration> members = node.getBodyDeclarations();
    Iterator<BodyDeclaration> iterator = members.iterator();
    while (iterator.hasNext()) {
      BodyDeclaration member = iterator.next();
      switch (member.getKind()) {
        case ENUM_DECLARATION:
        case TYPE_DECLARATION:
          normalizeMembers((AbstractTypeDeclaration) member);
          break;
        case METHOD_DECLARATION:
          methods.add((MethodDeclaration) member);
          break;
        case INITIALIZER:
          addInitializer(member, initStatements, classInitStatements);
          iterator.remove();
          break;
        case FIELD_DECLARATION:
          addFieldInitializer(member, binding.isInterface(), initStatements, classInitStatements);
          break;
      }
    }

    // Update any primary constructors with init statements.
    if (!binding.isInterface()) {
      boolean needsConstructor = true;
      for (MethodDeclaration md : methods) {
        if (md.isConstructor()) {
          needsConstructor = false;
        }
        normalizeMethod(md, initStatements);
      }
      if (needsConstructor) {
        addDefaultConstructor(binding, members, initStatements);
      }
    }
  }

  /**
   * Add a static or instance init block's statements to the appropriate list
   * of initialization statements.
   */
  private void addInitializer(BodyDeclaration member, List<Statement> initStatements,
      List<Statement> classInitStatements) {
    Initializer initializer = (Initializer) member;
    List<Statement> l =
        Modifier.isStatic(initializer.getModifiers()) ? classInitStatements : initStatements;
    l.add(TreeUtil.remove(initializer.getBody()));
  }

  /**
   * Strip field initializers, convert them to assignment statements, and
   * add them to the appropriate list of initialization statements.
   */
  private void addFieldInitializer(
      BodyDeclaration member, boolean isInterface, List<Statement> initStatements,
      List<Statement> classInitStatements) {
    FieldDeclaration field = (FieldDeclaration) member;
    for (VariableDeclarationFragment frag : field.getFragments()) {
      if (frag.getInitializer() != null) {
        Statement assignStmt = makeAssignmentStatement(frag);
        if (Modifier.isStatic(field.getModifiers()) || isInterface) {
          if (requiresInitializer(frag)) {
            classInitStatements.add(assignStmt);
            frag.setInitializer(null);
          }
        } else {
          // always initialize instance variables, since they can't be constants
          initStatements.add(assignStmt);
          frag.setInitializer(null);
        }
      }
    }
  }

  /**
   * Determines if a variable declaration requires initialization. (ie. cannot
   * be assigned to a literal value in ObjC.
   */
  private boolean requiresInitializer(VariableDeclarationFragment frag) {
    Expression initializer = frag.getInitializer();
    switch (initializer.getKind()) {
      case BOOLEAN_LITERAL:
      case CHARACTER_LITERAL:
      case NULL_LITERAL:
      case NUMBER_LITERAL:
        return false;
      case STRING_LITERAL:
        return !UnicodeUtils.hasValidCppCharacters(((StringLiteral) initializer).getLiteralValue());
    }
    if (BindingUtil.isPrimitiveConstant(frag.getVariableBinding())) {
      return false;
    }
    // If the initializer is not a literal, but has a constant value, convert it
    // to a literal. (as javac would do)
    Object constantValue = initializer.getConstantValue();
    if (constantValue != null) {
      if (constantValue instanceof String
          && !UnicodeUtils.hasValidCppCharacters((String) constantValue)) {
        return true;
      }
      frag.setInitializer(TreeUtil.newLiteral(constantValue));
      return false;
    }
    return true;
  }

  private ExpressionStatement makeAssignmentStatement(VariableDeclarationFragment fragment) {
    return new ExpressionStatement(new Assignment(
        new SimpleName(fragment.getVariableBinding()), fragment.getInitializer().copy()));
  }

  /**
   * Insert initialization statements into "primary" constructors.  A
   * "primary" construction is defined here as a constructor that doesn't
   * call other constructors in this class, and is similar in concept to
   * Objective-C's "designated initializers."
   *
   * @return true if constructor was normalized
   */
  void normalizeMethod(MethodDeclaration node, List<Statement> initStatements) {
    if (isDesignatedConstructor(node)) {
      List<Statement> stmts = node.getBody().getStatements();
      int superCallIdx = findSuperConstructorInvocation(stmts);

      // Insert initializer statements after the super invocation. If there
      // isn't a super invocation, add one (like all Java compilers do).
      if (superCallIdx == -1) {
        ITypeBinding superType = node.getMethodBinding().getDeclaringClass().getSuperclass();
        GeneratedMethodBinding newBinding = GeneratedMethodBinding.newConstructor(
            superType, Modifier.PUBLIC);
        stmts.add(0, new SuperConstructorInvocation(newBinding));
        superCallIdx = 0;
      }

      TreeUtil.copyList(initStatements, stmts.subList(0, superCallIdx + 1));
    }
  }

  private int findSuperConstructorInvocation(List<Statement> statements) {
    for (int i = 0; i < statements.size(); i++) {
      if (statements.get(i) instanceof SuperConstructorInvocation) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns true if this is a constructor that doesn't doesn't call
   * "this(...)".  This constructors are skipped so initializers
   * aren't run more than once per instance creation.
   */
  private static boolean isDesignatedConstructor(MethodDeclaration node) {
    if (!node.isConstructor()) {
      return false;
    }
    Block body = node.getBody();
    if (body == null) {
      return false;
    }
    List<Statement> stmts = body.getStatements();
    if (stmts.isEmpty()) {
      return true;
    }
    Statement firstStmt = stmts.get(0);
    return !(firstStmt instanceof ConstructorInvocation);
  }

  void addDefaultConstructor(
      ITypeBinding type, List<BodyDeclaration> members, List<Statement> initStatements) {
    int constructorModifier =
        type.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE);
    GeneratedMethodBinding binding = GeneratedMethodBinding.newConstructor(
        type.getSuperclass(), constructorModifier);
    initStatements.add(0, new SuperConstructorInvocation(binding));
    members.add(createMethod(GeneratedMethodBinding.newConstructor(type, constructorModifier),
                             initStatements));
  }

  private MethodDeclaration createMethod(IMethodBinding binding, List<Statement> statements) {
    Block body = new Block();
    TreeUtil.copyList(statements, body.getStatements());
    MethodDeclaration method = new MethodDeclaration(binding);
    method.setBody(body);
    return method;
  }
}
