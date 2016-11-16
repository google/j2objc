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
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Determines which declarations should be moved out of the public header file.
 *
 * @author Keith Stanger
 */
public class PrivateDeclarationResolver extends UnitTreeVisitor {

  private Map<TypeElement, AbstractTypeDeclaration> typeMap = new HashMap<>();
  // Collects types that must be public because they are exposed by another
  // public declaration. These types and all of their supertypes must be public.
  private Set<TypeElement> publicTypes = new HashSet<>();
  private List<AbstractTypeDeclaration> publicNodesToVisit = new ArrayList<>();

  public PrivateDeclarationResolver(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public boolean visit(CompilationUnit node) {
    // Map the types by their elements.
    for (AbstractTypeDeclaration typeNode : node.getTypes()) {
      typeMap.put(typeNode.getTypeElement(), typeNode);
    }

    // Identify types that are public by their declaration.
    for (AbstractTypeDeclaration typeNode : node.getTypes()) {
      TypeElement typeElement = typeNode.getTypeElement();
      if (!ElementUtil.isPrivateInnerType(typeElement)) {
        addPublicType(typeElement);
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
      if (!publicTypes.contains(typeNode.getTypeElement())) {
        typeNode.setHasPrivateDeclaration(true);
        for (BodyDeclaration decl : typeNode.getBodyDeclarations()) {
          decl.setHasPrivateDeclaration(true);
        }
      }
    }
    return false;
  }

  private void addPublicType(TypeElement typeElement) {
    if (typeElement == null) {
      return;
    }
    AbstractTypeDeclaration typeNode = typeMap.get(typeElement);
    if (typeNode == null) {
      return;
    }
    if (publicTypes.add(typeElement)) {
      publicNodesToVisit.add(typeNode);
    }
    // Make sure supertypes of public types remain public, even if declared
    // private.
    addPublicType(typeElement.getSuperclass());
    for (TypeMirror interfaceType : typeElement.getInterfaces()) {
      addPublicType(interfaceType);
    }
  }

  private void addPublicType(TypeMirror type) {
    if (type != null) {
      for (TypeMirror bound : typeUtil.getUpperBounds(type)) {
        addPublicType(TypeUtil.asTypeElement(bound));
      }
    }
  }

  private void addPublicType(Type typeNode) {
    if (typeNode != null) {
      addPublicType(typeNode.getTypeMirror());
    }
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    boolean isPrivate = Modifier.isPrivate(node.getModifiers());
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
    boolean isPrivate = Modifier.isPrivate(node.getModifiers());
    node.setHasPrivateDeclaration(isPrivate);
    if (!isPrivate) {
      addPublicType(node.getReturnType());
    }
    return false;
  }

  @Override
  public boolean visit(NativeDeclaration node) {
    boolean isPrivate = Modifier.isPrivate(node.getModifiers());
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
