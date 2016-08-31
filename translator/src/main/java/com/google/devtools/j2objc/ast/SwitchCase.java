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
 * Node type for a switch case statement.
 */
public class SwitchCase extends Statement {

  private boolean isDefault = false;
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);

  public SwitchCase() {}

  public SwitchCase(SwitchCase other) {
    super(other);
    isDefault = other.isDefault();
    expression.copyFrom(other.getExpression());
  }

  @Override
  public Kind getKind() {
    return Kind.SWITCH_CASE;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public SwitchCase setIsDefault(boolean value) {
    isDefault = value;
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public SwitchCase setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SwitchCase copy() {
    return new SwitchCase(this);
  }
}
