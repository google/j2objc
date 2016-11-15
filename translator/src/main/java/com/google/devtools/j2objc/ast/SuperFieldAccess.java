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
 * Node for accessing a field via "super" keyword.
 */
public class SuperFieldAccess extends Expression {

  private VariableElement variableElement = null;
  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);

  public SuperFieldAccess() {}

  public SuperFieldAccess(SuperFieldAccess other) {
    super(other);
    variableElement = other.getVariableElement();
    qualifier.copyFrom(other.getQualifier());
    name.copyFrom(other.getName());
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_FIELD_ACCESS;
  }

  public VariableElement getVariableElement() {
    return variableElement;
  }

  public SuperFieldAccess setVariableElement(VariableElement var) {
    variableElement = var;
    return this;
  }

  @Override
  public TypeMirror getTypeMirror() {
    SimpleName nameNode = name.get();
    return nameNode != null ? nameNode.getTypeMirror() : null;
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public SuperFieldAccess setQualifier(Name newQualifier) {
    qualifier.set(newQualifier);
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public SuperFieldAccess setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      qualifier.accept(visitor);
      name.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SuperFieldAccess copy() {
    return new SuperFieldAccess(this);
  }
}
