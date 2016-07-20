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

import org.eclipse.jdt.core.dom.IVariableBinding;
import com.google.devtools.j2objc.jdt.TreeConverter;
import java.util.List;
import javax.lang.model.element.VariableElement;

/**
 * Node for a field declaration.
 */
public class FieldDeclaration extends BodyDeclaration {

  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildList<VariableDeclarationFragment> fragments =
      ChildList.create(VariableDeclarationFragment.class, this);

  public FieldDeclaration(org.eclipse.jdt.core.dom.FieldDeclaration jdtNode) {
    super(jdtNode);
    type.set((Type) TreeConverter.convert(jdtNode.getType()));
    for (Object fragment : jdtNode.fragments()) {
      fragments.add((VariableDeclarationFragment) TreeConverter.convert(fragment));
    }
  }

  public FieldDeclaration(FieldDeclaration other) {
    super(other);
    type.copyFrom(other.getType());
    fragments.copyFrom(other.getFragments());
  }

  public FieldDeclaration(VariableDeclarationFragment fragment) {
    super(fragment.getVariableBinding());
    type.set(Type.newType(fragment.getVariableBinding().getType()));
    fragments.add(fragment);
  }

  public FieldDeclaration(IVariableBinding variableBinding, Expression initializer) {
    super(variableBinding);
    type.set(Type.newType(variableBinding.getType()));
    fragments.add(new VariableDeclarationFragment(variableBinding, initializer));
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

  public VariableDeclarationFragment getFragment(int index) {
    return fragments.get(index);
  }

  public List<VariableDeclarationFragment> getFragments() {
    return fragments;
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
