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

import java.util.List;

/** Node type for a switch case statement. */
@SuppressWarnings("CanIgnoreReturnValueSuggester")
public class SwitchExpressionCase extends Statement {

  private boolean isDefault = false;
  private final ChildLink<Pattern> pattern = ChildLink.create(Pattern.class, this);
  private final ChildList<Expression> expressions = ChildList.create(Expression.class, this);
  private final ChildLink<Expression> guard = ChildLink.create(Expression.class, this);
  private final ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);

  public SwitchExpressionCase() {}

  public SwitchExpressionCase(SwitchExpressionCase other) {
    super(other);
    isDefault = other.isDefault();
    pattern.set(other.getPattern());
    expressions.copyFrom(other.getExpressions());
    guard.set(other.getGuard());
    body.set(other.getBody());
  }

  @Override
  public Kind getKind() {
    return Kind.SWITCH_EXPRESSION_CASE;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public SwitchExpressionCase setIsDefault(boolean value) {
    isDefault = value;
    return this;
  }

  public List<Expression> getExpressions() {
    return expressions;
  }

  public SwitchExpressionCase addExpression(Expression newExpression) {
    expressions.add(newExpression);
    return this;
  }

  public Pattern getPattern() {
    return pattern.get();
  }

  public SwitchExpressionCase setPattern(Pattern newPattern) {
    pattern.set(newPattern);
    return this;
  }

  public Expression getGuard() {
    return guard.get();
  }

  public SwitchExpressionCase setGuard(Expression newGuard) {
    guard.set(newGuard);
    return this;
  }

  public TreeNode getBody() {
    return body.get();
  }

  public SwitchExpressionCase setBody(TreeNode newBody) {
    body.set(newBody);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expressions.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SwitchExpressionCase copy() {
    return new SwitchExpressionCase(this);
  }
}
