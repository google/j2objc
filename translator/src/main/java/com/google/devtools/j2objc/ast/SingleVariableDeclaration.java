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
 * Node type for a declaration of a single variable. Used in parameter lists
 * and catch clauses.
 */
public class SingleVariableDeclaration extends VariableDeclaration {

  private boolean isVarargs = false;
  private final ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);
  private final ChildLink<Type> type = ChildLink.create(Type.class, this);

  public SingleVariableDeclaration(org.eclipse.jdt.core.dom.SingleVariableDeclaration jdtNode) {
    super(jdtNode);
    isVarargs = jdtNode.isVarargs();
    for (Object modifier : jdtNode.modifiers()) {
      if (modifier instanceof org.eclipse.jdt.core.dom.Annotation) {
        annotations.add((Annotation) TreeConverter.convert(modifier));
      }
    }
    type.set((Type) TreeConverter.convert(jdtNode.getType()));
  }

  public SingleVariableDeclaration(SingleVariableDeclaration other) {
    super(other);
    isVarargs = other.isVarargs();
    annotations.copyFrom(other.getAnnotations());
    type.copyFrom(other.getType());
  }

  public SingleVariableDeclaration(IVariableBinding variableBinding) {
    super(variableBinding, null);
    type.set(Type.newType(variableBinding.getType()));
  }

  @Override
  public Kind getKind() {
    return Kind.SINGLE_VARIABLE_DECLARATION;
  }

  public boolean isVarargs() {
    return isVarargs;
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

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      annotations.accept(visitor);
      type.accept(visitor);
      name.accept(visitor);
      initializer.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SingleVariableDeclaration copy() {
    return new SingleVariableDeclaration(this);
  }
}
