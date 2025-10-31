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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.List;

/**
 * Node type for a switch case statement.
 */
public class SwitchCase extends Statement {

  private boolean isDefault = false;
  private final ChildLink<Pattern> pattern = ChildLink.create(Pattern.class, this);
  private final ChildList<Expression> expressions = ChildList.create(Expression.class, this);
  private final ChildLink<Expression> guard = ChildLink.create(Expression.class, this);
  private final ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);

  public SwitchCase() {}

  public SwitchCase(SwitchCase other) {
    super(other);
    isDefault = other.isDefault();
    pattern.copyFrom(other.getPattern());
    expressions.copyFrom(other.getExpressions());
    guard.copyFrom(other.getGuard());
    body.copyFrom(other.getBody());
  }

  @Override
  public Kind getKind() {
    return Kind.SWITCH_CASE;
  }

  public boolean isDefault() {
    return isDefault;
  }

  @CanIgnoreReturnValue
  public SwitchCase setIsDefault(boolean value) {
    isDefault = value;
    return this;
  }

  public List<Expression> getExpressions() {
    return expressions;
  }

  @CanIgnoreReturnValue
  public SwitchCase addExpression(Expression newExpression) {
    expressions.add(newExpression);
    return this;
  }

  public Pattern getPattern() {
    return pattern.get();
  }

  @CanIgnoreReturnValue
  public SwitchCase setPattern(Pattern newPattern) {
    pattern.set(newPattern);
    return this;
  }

  public Expression getGuard() {
    return guard.get();
  }

  @CanIgnoreReturnValue
  public SwitchCase setGuard(Expression newGuard) {
    guard.set(newGuard);
    return this;
  }

  public TreeNode getBody() {
    return body.get();
  }

  @CanIgnoreReturnValue
  public SwitchCase setBody(TreeNode newBody) {
    body.set(newBody);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      pattern.accept(visitor);
      expressions.accept(visitor);
      guard.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SwitchCase copy() {
    return new SwitchCase(this);
  }
}
