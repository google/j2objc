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

import java.util.List;

/**
 * Node type for a local variable declaration.
 */
public class VariableDeclarationStatement extends Statement {

  private int modifiers = 0;
  protected ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);
  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildList<VariableDeclarationFragment> fragments =
      ChildList.create(VariableDeclarationFragment.class, this);

  public VariableDeclarationStatement(
      org.eclipse.jdt.core.dom.VariableDeclarationStatement jdtNode) {
    super(jdtNode);
    for (Object modifier : jdtNode.modifiers()) {
      if (modifier instanceof org.eclipse.jdt.core.dom.Annotation) {
        annotations.add((Annotation) TreeConverter.convert(modifier));
      }
    }
    type.set((Type) TreeConverter.convert(jdtNode.getType()));
    for (Object fragment : jdtNode.fragments()) {
      fragments.add((VariableDeclarationFragment) TreeConverter.convert(fragment));
    }
  }

  public VariableDeclarationStatement(VariableDeclarationStatement other) {
    super(other);
    annotations.copyFrom(other.getAnnotations());
    type.copyFrom(other.getType());
    fragments.copyFrom(other.getFragments());
  }

  public VariableDeclarationStatement(VariableDeclarationFragment fragment) {
    IVariableBinding variableBinding = fragment.getVariableBinding();
    modifiers = variableBinding.getModifiers();
    type.set(Type.newType(variableBinding.getType()));
    fragments.add(fragment);
  }

  public VariableDeclarationStatement(IVariableBinding variableBinding, Expression initializer) {
    this(new VariableDeclarationFragment(variableBinding, initializer));
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION_STATEMENT;
  }

  public int getModifiers() {
    return modifiers;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public Type getType() {
    return type.get();
  }

  public void setType(Type newType) {
    type.set(newType);
  }

  public List<VariableDeclarationFragment> getFragments() {
    return fragments;
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
