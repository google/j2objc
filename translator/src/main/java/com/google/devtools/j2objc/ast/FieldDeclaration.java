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
import javax.lang.model.element.VariableElement;

/**
 * Node for a field declaration.
 */
public class FieldDeclaration extends BodyDeclaration {

  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildList<VariableDeclarationFragment> fragments =
      ChildList.create(VariableDeclarationFragment.class, this);

  public FieldDeclaration() {}

  public FieldDeclaration(FieldDeclaration other) {
    super(other);
    type.copyFrom(other.getType());
    fragments.copyFrom(other.getFragments());
  }

  public FieldDeclaration(VariableDeclarationFragment fragment) {
    super(fragment.getVariableElement());
    type.set(Type.newType(fragment.getVariableElement().asType()));
    fragments.add(fragment);
  }

  public FieldDeclaration(VariableElement variableElement, Expression initializer) {
    super(variableElement);
    type.set(Type.newType(variableElement.asType()));
    fragments.add(new VariableDeclarationFragment(variableElement, initializer));
  }

  @Override
  public Kind getKind() {
    return Kind.FIELD_DECLARATION;
  }

  public Type getType() {
    return type.get();
  }

  public FieldDeclaration setType(Type newType) {
    type.set(newType);
    return this;
  }

  public VariableDeclarationFragment getFragment(int index) {
    return fragments.get(index);
  }

  public List<VariableDeclarationFragment> getFragments() {
    return fragments;
  }

  public FieldDeclaration addFragment(VariableDeclarationFragment f) {
    fragments.add(f);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      type.accept(visitor);
      fragments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public FieldDeclaration copy() {
    return new FieldDeclaration(this);
  }
}
