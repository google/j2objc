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
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Set;

/**
 * Collects the set of imports needed to resolve type references in a header.
 *
 * @author Tom Ball
 */
public class HeaderImportCollector extends TreeVisitor {

  private Set<Import> forwardDecls = Sets.newLinkedHashSet();
  private Set<Import> superTypes = Sets.newLinkedHashSet();
  private Set<Import> declaredTypes = Sets.newHashSet();

  public void collect(TreeNode node) {
    run(node);
    for (Import imp : superTypes) {
      if (forwardDecls.contains(imp)) {
        forwardDecls.remove(imp);
      }
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
    forwardDecls.addAll(Sets.difference(Import.getImports(type), declaredTypes));
  }

  private void addSuperType(Type type) {
    if (type != null) {
      addSuperType(type.getTypeBinding());
    }
  }

  private void addSuperType(ITypeBinding type) {
    superTypes.addAll(Sets.difference(Import.getImports(type), declaredTypes));
  }

  private void addDeclaredType(ITypeBinding type) {
    Import.addImports(type, declaredTypes);
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    addForwardDecl(node.getType());
    return true;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    addForwardDecl(node.getType());
    return true;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    addForwardDecl(node.getReturnType());
    IMethodBinding binding = node.getMethodBinding();
    for (ITypeBinding paramType : binding.getParameterTypes()) {
      addForwardDecl(paramType);
    }
    return true;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    ITypeBinding binding = node.getTypeBinding();
    addDeclaredType(binding);
    if (binding.isEqualTo(Types.getNSObject())) {
      return false;
    }
    addSuperType(node.getSuperclassType());
    for (Type interfaze : node.getSuperInterfaceTypes()) {
      addSuperType(interfaze);
    }
    return true;
  }

  private static final ITypeBinding JAVA_LANG_ENUM =
      GeneratedTypeBinding.newTypeBinding("java.lang.Enum", null, false);

  @Override
  public boolean visit(EnumDeclaration node) {
    addSuperType(JAVA_LANG_ENUM);
    ITypeBinding binding = node.getTypeBinding();
    addDeclaredType(binding);
    for (ITypeBinding interfaze : binding.getInterfaces()) {
      addSuperType(interfaze);
    }
    return true;
  }

  private static final ITypeBinding JAVA_LANG_ANNOTATION =
      GeneratedTypeBinding.newTypeBinding("java.lang.annotation.Annotation", null, false);

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    addSuperType(JAVA_LANG_ANNOTATION);
    ITypeBinding binding = node.getTypeBinding();
    addDeclaredType(binding);
    return true;
  }
}
