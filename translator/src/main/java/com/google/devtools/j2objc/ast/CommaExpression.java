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

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Arrays;
import java.util.List;

/**
 * Comma expression node type. eg. "(expr1, expr2, expr3)"
 */
public class CommaExpression extends Expression {

  private final ChildList<Expression> expressions = ChildList.create(Expression.class, this);

  public CommaExpression(CommaExpression other) {
    super(other);
    expressions.copyFrom(other.getExpressions());
  }

  public CommaExpression(Expression... expressions) {
    this.expressions.addAll(Arrays.asList(expressions));
  }

  @Override
  public Kind getKind() {
    return Kind.COMMA_EXPRESSION;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return expressions.get(expressions.size() - 1).getTypeBinding();
  }

  public List<Expression> getExpressions() {
    return expressions;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expressions.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public CommaExpression copy() {
    return new CommaExpression(this);
  }
}
