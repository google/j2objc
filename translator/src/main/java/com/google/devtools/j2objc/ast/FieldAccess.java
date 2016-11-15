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
 * Node type for a field access.
 */
public class FieldAccess extends Expression {

  private VariableElement variableElement = null;
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);

  public FieldAccess() {}

  public FieldAccess(FieldAccess other) {
    super(other);
    variableElement = other.getVariableElement();
    expression.copyFrom(other.getExpression());
    name.copyFrom(other.getName());
  }

  public FieldAccess(
      VariableElement variableElement, TypeMirror typeMirror, Expression expression) {
    this.variableElement = variableElement;
    this.expression.set(expression);
    name.set(new SimpleName(variableElement, typeMirror));
  }

  public FieldAccess(VariableElement variableElement, Expression expression) {
    this(variableElement, variableElement.asType(), expression);
  }

  @Override
  public Kind getKind() {
    return Kind.FIELD_ACCESS;
  }

  @Override
  public TypeMirror getTypeMirror() {
    SimpleName nameNode = name.get();
    return nameNode != null ? nameNode.getTypeMirror() : null;
  }

  public VariableElement getVariableElement() {
    return variableElement;
  }

  public FieldAccess setVariableElement(VariableElement newVariable) {
    variableElement = newVariable;
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public FieldAccess setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public FieldAccess setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      name.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public FieldAccess copy() {
    return new FieldAccess(this);
  }
}
