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
import javax.lang.model.type.TypeMirror;

/**
 * Node for a field declaration.
 */
public class FieldDeclaration extends BodyDeclaration {

  // TODO(user): Change fragments to ChildLink after JDT code is dropped. With Javac,
  // FieldDeclaration only has one VariableDeclarationFragment.
  private ChildList<VariableDeclarationFragment> fragments =
      ChildList.create(VariableDeclarationFragment.class, this);

  public FieldDeclaration() {}

  public FieldDeclaration(FieldDeclaration other) {
    super(other);
    fragments.copyFrom(other.getFragments());
  }

  public FieldDeclaration(VariableDeclarationFragment fragment) {
    super(fragment.getVariableElement());
    fragments.add(fragment);
  }

  public FieldDeclaration(VariableElement variableElement, Expression initializer) {
    super(variableElement);
    fragments.add(new VariableDeclarationFragment(variableElement, initializer));
  }

  @Override
  public Kind getKind() {
    return Kind.FIELD_DECLARATION;
  }

  public TypeMirror getTypeMirror() {
    return fragments.get(0).getVariableElement().asType();
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
      fragments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public FieldDeclaration copy() {
    return new FieldDeclaration(this);
  }
}
