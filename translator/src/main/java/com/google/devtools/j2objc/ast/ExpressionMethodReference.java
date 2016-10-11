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

/**
 * Expression method reference node type (added in JLS8, section 15.13).
 */
public class ExpressionMethodReference extends MethodReference {

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);

  public ExpressionMethodReference() {}

  public ExpressionMethodReference(ExpressionMethodReference other) {
    super(other);
    expression.copyFrom(other.getExpression());
    name.copyFrom(other.getName());
  }

  @Override
  public Kind getKind() {
    return Kind.EXPRESSION_METHOD_REFERENCE;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public ExpressionMethodReference setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public ExpressionMethodReference setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      lambdaOuterArg.accept(visitor);
      lambdaCaptureArgs.accept(visitor);
      expression.accept(visitor);
      typeArguments.accept(visitor);
      name.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ExpressionMethodReference copy() {
    return new ExpressionMethodReference(this);
  }
}
