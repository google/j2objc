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
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.util.TranslationUtil;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;
import java.util.Set;

/**
 * Collects the set of imports needed to resolve type references in a header.
 *
 * @author Tom Ball
 */
public class HeaderImportCollector extends TreeVisitor {

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
  private Set<Import> forwardDecls = Sets.newLinkedHashSet();
  // Supertypes of the below declared types that haven't been seen by this collector.
  private Set<Import> superTypes = Sets.newLinkedHashSet();
  // Declared types seen by this collector.
  private Set<Import> declaredTypes = Sets.newHashSet();

  public HeaderImportCollector(Filter filter) {
    this.filter = filter;
  }

  public void collect(TreeNode node) {
    run(node);
  }

  public void collect(List<? extends TreeNode> nodes) {
    for (TreeNode node : nodes) {
      collect(node);
    }
  }

  public Set<Import> getForwardDeclarations() {
    return forwardDecls;
  }

  public Set<Import> getSuperTypes() {
    return superTypes;
  }

  private void addForwardDecl(Type type) {
    if (type != null) {
      addForwardDecl(type.getTypeBinding());
    }
  }

  private void addForwardDecl(ITypeBinding type) {
    forwardDecls.addAll(Sets.difference(Import.getImports(type, unit), declaredTypes));
  }

  private void addSuperType(ITypeBinding type) {
    Import.addImports(type, superTypes, unit);
  }

  private void addDeclaredType(ITypeBinding type) {
    Import.addImports(type, declaredTypes, unit);
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    if (filter.include(node)) {
      addForwardDecl(node.getType());
    }
    return false;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    if (filter.include(node)) {
      addForwardDecl(node.getType());
    }
    return false;
  }

  @Override
  public boolean visit(FunctionDeclaration node) {
    if (filter.include(node)) {
      addForwardDecl(node.getReturnType());
      for (SingleVariableDeclaration param : node.getParameters()) {
        addForwardDecl(param.getVariableBinding().getType());
      }
    }
    return false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    if (filter.include(node)) {
      addForwardDecl(node.getReturnType());
      IMethodBinding binding = node.getMethodBinding();
      for (ITypeBinding paramType : binding.getParameterTypes()) {
        addForwardDecl(paramType);
      }
    }
    return false;
  }

  private boolean visitTypeDeclaration(AbstractTypeDeclaration node) {
    if (filter.include(node)) {
      ITypeBinding binding = node.getTypeBinding();
      addDeclaredType(binding);
      addSuperType(TranslationUtil.getSuperType(node));
      for (ITypeBinding interfaze : TranslationUtil.getInterfaceTypes(node)) {
        addSuperType(interfaze);
      }
    }
    return true;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    return visitTypeDeclaration(node);
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
