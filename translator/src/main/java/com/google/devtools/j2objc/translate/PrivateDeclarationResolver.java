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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Determines which declarations should be moved out of the public header file.
 *
 * @author Keith Stanger
 */
public class PrivateDeclarationResolver extends TreeVisitor {

  private Map<ITypeBinding, AbstractTypeDeclaration> typeMap = Maps.newHashMap();
  // Collects types that must be public because they are exposed by another
  // public declaration. These types and all of their supertypes must be public.
  private Set<ITypeBinding> publicTypes = Sets.newHashSet();
  private List<AbstractTypeDeclaration> publicNodesToVisit = Lists.newArrayList();

  @Override
  public boolean visit(CompilationUnit node) {
    // Map the types by their bindings.
    for (AbstractTypeDeclaration typeNode : node.getTypes()) {
      typeMap.put(typeNode.getTypeBinding().getTypeDeclaration(), typeNode);
    }

    // Identify types that are public by their declaration.
    for (AbstractTypeDeclaration typeNode : node.getTypes()) {
      ITypeBinding typeBinding = typeNode.getTypeBinding();
      if (!isPrivateType(typeBinding)) {
        addPublicType(typeBinding);
      }
    }

    // Visit public nodes, possibly identifying additional nodes that must be
    // public because they are exposed by a field or method from another type.
    while (!publicNodesToVisit.isEmpty()) {
      AbstractTypeDeclaration publicNode = publicNodesToVisit.remove(publicNodesToVisit.size() - 1);
      publicNode.setHasPrivateDeclaration(false);
      for (BodyDeclaration decl : publicNode.getBodyDeclarations()) {
        decl.accept(this);
      }
    }

    // After all public nodes are identified, mark remaining nodes and their
    // declarations as private.
    for (AbstractTypeDeclaration typeNode : node.getTypes()) {
      if (!publicTypes.contains(typeNode.getTypeBinding())) {
        typeNode.setHasPrivateDeclaration(true);
        for (BodyDeclaration decl : typeNode.getBodyDeclarations()) {
          decl.setHasPrivateDeclaration(true);
        }
      }
    }
    return false;
  }

  private void addPublicType(ITypeBinding typeBinding) {
    if (typeBinding == null) {
      return;
    }
    typeBinding = typeBinding.getTypeDeclaration();
    AbstractTypeDeclaration typeNode = typeMap.get(typeBinding);
    if (typeNode == null) {
      return;
    }
    if (publicTypes.add(typeBinding)) {
      publicNodesToVisit.add(typeNode);
    }
    // Make sure supertypes of public types remain public, even if declared
    // private.
    addPublicType(typeBinding.getSuperclass());
    for (ITypeBinding interfaceType : typeBinding.getInterfaces()) {
      addPublicType(interfaceType);
    }
  }

  private void addPublicType(Type typeNode) {
    if (typeNode != null) {
      addPublicType(typeNode.getTypeBinding());
    }
  }

  private boolean isPrivateType(ITypeBinding type) {
    return Options.hidePrivateMembers() && BindingUtil.isPrivateInnerType(type);
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
    boolean isPrivate = Options.hidePrivateMembers() && Modifier.isPrivate(node.getModifiers());
    node.setHasPrivateDeclaration(isPrivate);
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
