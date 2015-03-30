/*
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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Map;
import java.util.Set;

/**
 * Determines which declarations should be moved out of the public header file.
 *
 * @author Keith Stanger
 */
public class PrivateDeclarationResolver extends TreeVisitor {

  // Collects types that must be public because they are exposed by another
  // public declaration. These types and all of their supertypes must be public.
  private Set<ITypeBinding> publicTypes = Sets.newHashSet();

  @Override
  public boolean visit(CompilationUnit node) {
    Map<ITypeBinding, AbstractTypeDeclaration> typeMap = Maps.newHashMap();
    for (AbstractTypeDeclaration typeNode : node.getTypes()) {
      typeNode.accept(this);
      typeMap.put(typeNode.getTypeBinding(), typeNode);
    }

    // Make sure supertypes of public types remain public, even if declared
    // private.
    for (ITypeBinding type : publicTypes) {
      ensurePublicSuperTypes(type, typeMap);
    }
    return false;
  }

  private void ensurePublicSuperTypes(
      ITypeBinding typeBinding, Map<ITypeBinding, AbstractTypeDeclaration> typeMap) {
    if (typeBinding == null) {
      return;
    }
    typeBinding = typeBinding.getTypeDeclaration();
    AbstractTypeDeclaration typeNode = typeMap.get(typeBinding);
    if (typeNode == null) {
      return;
    }
    typeNode.setHasPrivateDeclaration(false);
    ensurePublicSuperTypes(typeBinding.getSuperclass(), typeMap);
    for (ITypeBinding interfaceType : typeBinding.getInterfaces()) {
      ensurePublicSuperTypes(interfaceType, typeMap);
    }
  }

  private void addPublicType(Type typeNode) {
    if (typeNode != null) {
      publicTypes.add(typeNode.getTypeBinding());
    }
  }

  private boolean isPrivateType(ITypeBinding type) {
    // TODO(kstanger): Uncomment the code below to hide private types.
    return false;
    /*if (type == null || !Options.hidePrivateMembers()) {
      return false;
    }
    return isPrivateType(type.getDeclaringClass()) || BindingUtil.isPrivate(type) || type.isLocal()
        || type.isAnonymous();*/
  }

  private boolean visitType(AbstractTypeDeclaration node) {
    ITypeBinding typeBinding = node.getTypeBinding();
    boolean isPrivate = isPrivateType(typeBinding);
    node.setHasPrivateDeclaration(isPrivate);
    if (!isPrivate) {
      publicTypes.add(typeBinding);
    }
    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      if (isPrivate) {
        decl.setHasPrivateDeclaration(true);
      } else {
        decl.accept(this);
      }
    }
    return false;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    return visitType(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return visitType(node);
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return visitType(node);
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    boolean isPrivate = Options.hidePrivateMembers() && Modifier.isPrivate(node.getModifiers());
    node.setHasPrivateDeclaration(isPrivate);
    if (!isPrivate) {
      addPublicType(node.getType());
    }
    return false;
  }

  @Override
  public boolean visit(FunctionDeclaration node) {
    boolean isPrivate = Modifier.isPrivate(node.getModifiers());
    node.setHasPrivateDeclaration(isPrivate);
    if (!isPrivate) {
      addPublicType(node.getReturnType());
    }
    return false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    boolean isPrivate = Options.hidePrivateMembers() && Modifier.isPrivate(node.getModifiers());
    node.setHasPrivateDeclaration(isPrivate);
    if (!isPrivate) {
      addPublicType(node.getReturnType());
    }
    return false;
  }

  @Override
  public boolean visit(NativeDeclaration node) {
    node.setHasPrivateDeclaration(
        Options.hidePrivateMembers() && Modifier.isPrivate(node.getModifiers()));
    return false;
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    return false;
  }

  @Override
  public boolean visit(EnumConstantDeclaration node) {
    return false;
  }

  @Override
  public boolean visit(Initializer node) {
    return false;
  }
}
