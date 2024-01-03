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

package com.google.devtools.j2objc.types;

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Collects the set of imports needed to resolve type references in a header.
 *
 * @author Tom Ball
 */
public class HeaderImportCollector extends UnitTreeVisitor {

  /**
   * Describes which declarations should be visited by the collector.
   */
  public enum Filter {
    INCLUDE_ALL(true, true),
    PUBLIC_ONLY(true, false),
    PRIVATE_ONLY(false, true);

    private final boolean includePublic;
    private final boolean includePrivate;

    private Filter(boolean includePublic, boolean includePrivate) {
      this.includePublic = includePublic;
      this.includePrivate = includePrivate;
    }

    private boolean include(BodyDeclaration node) {
      return node.hasPrivateDeclaration() ? includePrivate : includePublic;
    }
  }

  private final Filter filter;

  // Forward declarations. The order in which imports are collected affect
  // which imports become forward declarations.
  private Set<Import> forwardDecls = new LinkedHashSet<>();
  // Supertypes of the below declared types that haven't been seen by this collector.
  private Set<Import> superTypes = new LinkedHashSet<>();
  // Declared types seen by this collector.
  private Set<Import> declaredTypes = new HashSet<>();
  // The current type declarations is annotated for ObjC generics.
  private boolean wantsGenerateObjectiveCGenerics = false;

  public HeaderImportCollector(CompilationUnit unit, Filter filter) {
    super(unit);
    this.filter = filter;
  }

  public Set<Import> getForwardDeclarations() {
    return forwardDecls;
  }

  public Set<Import> getSuperTypes() {
    return superTypes;
  }

  private void addForwardDecl(Type type) {
    if (type != null) {
      addForwardDecl(type.getTypeMirror());
    }
  }

  private void addForwardDecl(TypeMirror type) {
    forwardDecls.addAll(Sets.difference(Import.getImports(type, unit.getEnv()), declaredTypes));
  }

  private void addSuperType(TypeElement type) {
    if (type != null) {
      Import.addImports(type.asType(), superTypes, unit.getEnv());
    }
  }

  private void addDeclaredType(TypeElement type) {
    Import.addImports(type.asType(), declaredTypes, unit.getEnv());
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    if (filter.include(node)) {
      addForwardDecl(node.getTypeMirror());
    }
    return false;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    if (filter.include(node)) {
      addForwardDecl(node.getTypeMirror());
    }
    return false;
  }

  private Set<TypeMirror> objCForwardDeclaredGenericParameters(TypeMirror type) {
    // Parameters only needed with ObjC generics are on.
    if (!unit.getEnv().options().asObjCGenericDecl() && !wantsGenerateObjectiveCGenerics) {
      return new HashSet<>();
    }

    HashSet<TypeMirror> vistedTypes = new HashSet<>();
    vistedTypes.add(type);
    for (TypeElement bound : unit.getEnv().typeUtil().getObjcUpperBounds(type)) {
      vistedTypes.add(bound.asType());
    }
    if (TypeUtil.isDeclaredType(type)) {
      for (TypeMirror argType : ((DeclaredType) type).getTypeArguments()) {
        vistedTypes.addAll(objCForwardDeclaredGenericParameters(argType));
      }
    }
    return vistedTypes;
  }

  @Override
  public boolean visit(FunctionDeclaration node) {
    if (filter.include(node)) {
      addForwardDecl(node.getReturnType());
      for (TypeMirror returnGeneric :
          objCForwardDeclaredGenericParameters(node.getReturnType().getTypeMirror())) {
        addForwardDecl(returnGeneric);
      }
      for (SingleVariableDeclaration param : node.getParameters()) {
        addForwardDecl(param.getVariableElement().asType());
        for (TypeMirror paramGeneric :
            objCForwardDeclaredGenericParameters(param.getVariableElement().asType())) {
          addForwardDecl(paramGeneric);
        }
      }
    }
    return false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    if (filter.include(node)) {
      addForwardDecl(node.getReturnTypeMirror());
      for (TypeMirror returnGeneric :
          objCForwardDeclaredGenericParameters(node.getReturnTypeMirror())) {
        addForwardDecl(returnGeneric);
      }
      for (VariableElement param : node.getExecutableElement().getParameters()) {
        addForwardDecl(param.asType());
        for (TypeMirror paramGeneric : objCForwardDeclaredGenericParameters(param.asType())) {
          addForwardDecl(paramGeneric);
        }
      }
    }
    return false;
  }

  private boolean visitTypeDeclaration(AbstractTypeDeclaration node) {
    if (filter.include(node)) {
      addDeclaredType(node.getTypeElement());
      addSuperType(TranslationUtil.getSuperType(node));
      for (TypeElement interfaze : TranslationUtil.getInterfaceTypes(node)) {
        addSuperType(interfaze);
      }
    }
    return true;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    wantsGenerateObjectiveCGenerics =
        TypeUtil.hasGenerateObjectiveCGenerics(node.getTypeElement().asType());
    return visitTypeDeclaration(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    wantsGenerateObjectiveCGenerics = false;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return visitTypeDeclaration(node);
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return visitTypeDeclaration(node);
  }
}
