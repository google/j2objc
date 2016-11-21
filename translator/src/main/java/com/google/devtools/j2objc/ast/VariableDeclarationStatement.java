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

import com.google.devtools.j2objc.util.ElementUtil;
import java.util.List;
import javax.lang.model.element.VariableElement;

/**
 * Node type for a local variable declaration.
 */
public class VariableDeclarationStatement extends Statement {

  private int modifiers = 0;
  protected ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);
  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildList<VariableDeclarationFragment> fragments =
      ChildList.create(VariableDeclarationFragment.class, this);

  public VariableDeclarationStatement() {}

  public VariableDeclarationStatement(VariableDeclarationStatement other) {
    super(other);
    annotations.copyFrom(other.getAnnotations());
    type.copyFrom(other.getType());
    fragments.copyFrom(other.getFragments());
  }

  public VariableDeclarationStatement(VariableDeclarationFragment fragment) {
    VariableElement variableElement = fragment.getVariableElement();
    modifiers = ElementUtil.fromModifierSet(variableElement.getModifiers());
    type.set(Type.newType(variableElement.asType()));
    fragments.add(fragment);
  }

  public VariableDeclarationStatement(VariableElement variableElement, Expression initializer) {
    this(new VariableDeclarationFragment(variableElement, initializer));
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION_STATEMENT;
  }

  public int getModifiers() {
    return modifiers;
  }

  public VariableDeclarationStatement setModifiers(int newMods) {
    modifiers = newMods;
    return this;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public VariableDeclarationStatement addAnnotation(Annotation ann) {
    annotations.add(ann);
    return this;
  }

  public Type getType() {
    return type.get();
  }

  public VariableDeclarationStatement setType(Type newType) {
    type.set(newType);
    return this;
  }

  public List<VariableDeclarationFragment> getFragments() {
    return fragments;
  }

  public VariableDeclarationStatement addFragment(VariableDeclarationFragment fragment) {
    fragments.add(fragment);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      annotations.accept(visitor);
      type.accept(visitor);
      fragments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public VariableDeclarationStatement copy() {
    return new VariableDeclarationStatement(this);
  }
}
