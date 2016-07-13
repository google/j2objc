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
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.jdt.TreeConverter;
import javax.lang.model.type.TypeMirror;

/**
 * Node type for a field access.
 */
public class FieldAccess extends Expression {

  private IVariableBinding variableBinding = null;
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);

  public FieldAccess(org.eclipse.jdt.core.dom.FieldAccess jdtNode) {
    super(jdtNode);
    variableBinding = BindingConverter.wrapBinding(jdtNode.resolveFieldBinding());
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
  }

  public FieldAccess(FieldAccess other) {
    super(other);
    variableBinding = other.getVariableBinding();
    expression.copyFrom(other.getExpression());
    name.copyFrom(other.getName());
  }

  public FieldAccess(IVariableBinding variableBinding, Expression expression) {
    this.variableBinding = variableBinding;
    this.expression.set(expression);
    name.set(new SimpleName(variableBinding));
  }

  @Override
  public Kind getKind() {
    return Kind.FIELD_ACCESS;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return BindingConverter.getType(variableBinding.getType());
  }

  public IVariableBinding getVariableBinding() {
    return variableBinding;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public SimpleName getName() {
    return name.get();
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
