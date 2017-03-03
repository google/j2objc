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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Node type for a class or interface declaration.
 */
public class TypeDeclaration extends AbstractTypeDeclaration {

  private boolean isInterface = false;

  // DeadCodeEliminator will set this field if its superclass is marked as unused
  private boolean stripSuperclass = false;
  private final ChildList<Type> superInterfaceTypes = ChildList.create(Type.class, this);
  private final ChildLink<Expression> superOuter = ChildLink.create(Expression.class, this);
  private final ChildList<Expression> superCaptureArgs = ChildList.create(Expression.class, this);

  public TypeDeclaration() {}

  public TypeDeclaration(TypeDeclaration other) {
    super(other);
    isInterface = other.isInterface();
    stripSuperclass = other.stripSuperclass;
    superInterfaceTypes.copyFrom(other.getSuperInterfaceTypes());
    superOuter.copyFrom(other.getSuperOuter());
    superCaptureArgs.copyFrom(other.getSuperCaptureArgs());
  }

  public TypeDeclaration(TypeElement typeElement) {
    super(typeElement);
    isInterface = typeElement.getKind().isInterface();
    for (TypeMirror interfaceMirror : typeElement.getInterfaces()) {
      superInterfaceTypes.add(Type.newType(interfaceMirror));
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

  public TypeMirror getSuperclassTypeMirror() {
    return stripSuperclass ? null : getTypeElement().getSuperclass();
  }

  public void stripSuperclass() {
    stripSuperclass = true;
  }

  public List<Type> getSuperInterfaceTypes() {
    return superInterfaceTypes;
  }

  public TypeDeclaration addSuperInterfaceType(Type type) {
    superInterfaceTypes.add(type);
    return this;
  }

  public Expression getSuperOuter() {
    return superOuter.get();
  }

  public TypeDeclaration setSuperOuter(Expression newSuperOuter) {
    superOuter.set(newSuperOuter);
    return this;
  }

  public List<Expression> getSuperCaptureArgs() {
    return superCaptureArgs;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      name.accept(visitor);
      superInterfaceTypes.accept(visitor);
      bodyDeclarations.accept(visitor);
      classInitStatements.accept(visitor);
      superOuter.accept(visitor);
      superCaptureArgs.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public TypeDeclaration copy() {
    return new TypeDeclaration(this);
  }
}
