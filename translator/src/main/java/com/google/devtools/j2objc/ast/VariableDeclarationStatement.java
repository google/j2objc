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

import com.google.devtools.j2objc.ast.VariableDeclaration.ObjectiveCModifier;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Node type for a local variable declaration.
 */
public class VariableDeclarationStatement extends Statement {

  protected ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);

  private final Set<ObjectiveCModifier> modifiers = new LinkedHashSet<>();
  private ChildList<VariableDeclarationFragment> fragments =
      ChildList.create(VariableDeclarationFragment.class, this);

  public VariableDeclarationStatement() {}

  public VariableDeclarationStatement(VariableDeclarationStatement other) {
    super(other);
    annotations.copyFrom(other.getAnnotations());
    fragments.copyFrom(other.getFragments());
    modifiers.addAll(other.getModifiers());
  }

  public VariableDeclarationStatement(VariableDeclarationFragment fragment) {
    fragments.add(fragment);
  }

  public VariableDeclarationStatement(VariableElement variableElement, Expression initializer) {
    this(new VariableDeclarationFragment(variableElement, initializer));
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION_STATEMENT;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  @CanIgnoreReturnValue
  public VariableDeclarationStatement addAnnotation(Annotation ann) {
    annotations.add(ann);
    return this;
  }

  public Set<ObjectiveCModifier> getModifiers() {
    return modifiers;
  }

  @CanIgnoreReturnValue
  public VariableDeclarationStatement addModifier(ObjectiveCModifier modifier) {
    modifiers.add(modifier);
    return this;
  }

  public TypeMirror getTypeMirror() {
    return fragments.get(0).getVariableElement().asType();
  }

  public List<VariableDeclarationFragment> getFragments() {
    return fragments;
  }

  @CanIgnoreReturnValue
  public VariableDeclarationStatement addFragment(VariableDeclarationFragment fragment) {
    fragments.add(fragment);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      annotations.accept(visitor);
      fragments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public VariableDeclarationStatement copy() {
    return new VariableDeclarationStatement(this);
  }
}
