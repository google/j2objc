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
import javax.lang.model.type.TypeMirror;

/**
 * Node for an annotation type member declaration.
 */
public final class AnnotationTypeMemberDeclaration extends BodyDeclaration {

  private ExecutableElement element = null;
  private ChildLink<Expression> defaultValue = ChildLink.create(Expression.class, this);

  public AnnotationTypeMemberDeclaration() {}

  public AnnotationTypeMemberDeclaration(AnnotationTypeMemberDeclaration other) {
    super(other);
    element = other.getExecutableElement();
    defaultValue.copyFrom(other.getDefault());
  }

  public AnnotationTypeMemberDeclaration(ExecutableElement element) {
    super(element);
    this.element = element;
  }

  @Override
  public Kind getKind() {
    return Kind.ANNOTATION_TYPE_MEMBER_DECLARATION;
  }

  public ExecutableElement getExecutableElement() {
    return element;
  }

  public AnnotationTypeMemberDeclaration setExecutableElement(ExecutableElement newElement) {
    element = newElement;
    return this;
  }

  public TypeMirror getTypeMirror() {
    return element.asType();
  }

  public Expression getDefault() {
    return defaultValue.get();
  }

  public AnnotationTypeMemberDeclaration setDefault(Expression newDefault) {
    defaultValue.set(newDefault);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      defaultValue.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public AnnotationTypeMemberDeclaration copy() {
    return new AnnotationTypeMemberDeclaration(this);
  }
}
