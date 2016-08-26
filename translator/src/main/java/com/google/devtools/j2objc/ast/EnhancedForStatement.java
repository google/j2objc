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
 * Enhanced for statement node type. (e.g. "for (int i : listOfInts) {...}")
 */
public class EnhancedForStatement extends Statement {

  private ChildLink<SingleVariableDeclaration> parameter =
      ChildLink.create(SingleVariableDeclaration.class, this);
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<Statement> body = ChildLink.create(Statement.class, this);

  public EnhancedForStatement() {}

  public EnhancedForStatement(EnhancedForStatement other) {
    super(other);
    parameter.copyFrom(other.getParameter());
    expression.copyFrom(other.getExpression());
    body.copyFrom(other.getBody());
  }

  @Override
  public Kind getKind() {
    return Kind.ENHANCED_FOR_STATEMENT;
  }

  public SingleVariableDeclaration getParameter() {
    return parameter.get();
  }

  public EnhancedForStatement setParameter(SingleVariableDeclaration newParameter) {
    parameter.set(newParameter);
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }
  
  public EnhancedForStatement setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  public Statement getBody() {
    return body.get();
  }
  
  public EnhancedForStatement setBody(Statement newBody) {
    body.set(newBody);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      parameter.accept(visitor);
      expression.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public EnhancedForStatement copy() {
    return new EnhancedForStatement(this);
  }
}
