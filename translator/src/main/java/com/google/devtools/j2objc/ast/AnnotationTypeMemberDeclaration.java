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

import javax.lang.model.element.ExecutableElement;

/**
 * Node for an annotation type member declaration.
 */
public final class AnnotationTypeMemberDeclaration extends BodyDeclaration {

  private ExecutableElement element = null;
  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildLink<Expression> defaultValue = ChildLink.create(Expression.class, this);

  public AnnotationTypeMemberDeclaration() {}

  public AnnotationTypeMemberDeclaration(AnnotationTypeMemberDeclaration other) {
    super(other);
    element = other.getElement();
    type.copyFrom(other.getType());
    name.copyFrom(other.getName());
    defaultValue.copyFrom(other.getDefault());
  }

  @Override
  public Kind getKind() {
    return Kind.ANNOTATION_TYPE_MEMBER_DECLARATION;
  }

  public ExecutableElement getElement() {
    return element;
  }

  public Type getType() {
    return type.get();
  }

  public SimpleName getName() {
    return name.get();
  }

  public Expression getDefault() {
    return defaultValue.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      type.accept(visitor);
      name.accept(visitor);
      defaultValue.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public AnnotationTypeMemberDeclaration copy() {
    return new AnnotationTypeMemberDeclaration(this);
  }

  public AnnotationTypeMemberDeclaration setDefault(Expression newDefault) {
    defaultValue.set(newDefault);
    return this;
  }

  public AnnotationTypeMemberDeclaration setElement(ExecutableElement newElement) {
    element = newElement;
    return this;
  }

  public AnnotationTypeMemberDeclaration setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  public AnnotationTypeMemberDeclaration setType(Type newType) {
    type.set(newType);
    return this;
  }
}
