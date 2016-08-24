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

package com.google.devtools.j2objc.ast;

import java.util.List;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Node type for a class or interface declaration.
 */
public class TypeDeclaration extends AbstractTypeDeclaration {

  private boolean isInterface = false;
  private ChildLink<Type> superclassType = ChildLink.create(Type.class, this);
  private ChildList<Type> superInterfaceTypes = ChildList.create(Type.class, this);

  public TypeDeclaration() {}

  public TypeDeclaration(TypeDeclaration other) {
    super(other);
    isInterface = other.isInterface();
    superclassType.copyFrom(other.getSuperclassType());
    superInterfaceTypes.copyFrom(other.getSuperInterfaceTypes());
  }

  public TypeDeclaration(ITypeBinding typeBinding) {
    super(typeBinding);
    isInterface = typeBinding.isInterface();
    ITypeBinding superclassTypeBinding = typeBinding.getSuperclass();
    if (superclassTypeBinding != null) {
      superclassType.set(Type.newType(superclassTypeBinding));
    }
    for (ITypeBinding interfaceTypeBinding : typeBinding.getInterfaces()) {
      superInterfaceTypes.add(Type.newType(interfaceTypeBinding));
    }
  }

  @Override
  public Kind getKind() {
    return Kind.TYPE_DECLARATION;
  }

  public boolean isInterface() {
    return isInterface;
  }

  public TypeDeclaration setInterface(boolean b) {
    isInterface = b;
    return this;
  }

  public Type getSuperclassType() {
    return superclassType.get();
  }

  public TypeDeclaration setSuperclassType(Type newSuperclassType) {
    superclassType.set(newSuperclassType);
    return this;
  }

  public List<Type> getSuperInterfaceTypes() {
    return superInterfaceTypes;
  }

  public TypeDeclaration addSuperInterfaceType(Type type) {
    superInterfaceTypes.add(type);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      name.accept(visitor);
      superclassType.accept(visitor);
      superInterfaceTypes.accept(visitor);
      bodyDeclarations.accept(visitor);
      classInitStatements.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public TypeDeclaration copy() {
    return new TypeDeclaration(this);
  }
}
