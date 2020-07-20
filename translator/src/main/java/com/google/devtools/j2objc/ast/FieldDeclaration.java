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

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Node for a field declaration.
 */
public class FieldDeclaration extends BodyDeclaration {

  private final ChildLink<VariableDeclarationFragment> fragment =
      ChildLink.create(VariableDeclarationFragment.class, this);

  public FieldDeclaration() {}

  public FieldDeclaration(FieldDeclaration other) {
    super(other);
    fragment.copyFrom(other.getFragment());
  }

  public FieldDeclaration(VariableElement variableElement, Expression initializer) {
    super(variableElement);
    fragment.set(new VariableDeclarationFragment(variableElement, initializer));
  }

  @Override
  public Kind getKind() {
    return Kind.FIELD_DECLARATION;
  }

  public TypeMirror getTypeMirror() {
    return fragment.get().getVariableElement().asType();
  }

  public VariableDeclarationFragment getFragment() {
    return fragment.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      fragment.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public FieldDeclaration copy() {
    return new FieldDeclaration(this);
  }
}
