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

import com.google.devtools.j2objc.jdt.TreeConverter;
import java.util.List;

/**
 * For statement node type.
 */
public class ForStatement extends Statement {

  private ChildList<Expression> initializers = ChildList.create(Expression.class, this);
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildList<Expression> updaters = ChildList.create(Expression.class, this);
  private ChildLink<Statement> body = ChildLink.create(Statement.class, this);

  public ForStatement(org.eclipse.jdt.core.dom.ForStatement jdtNode) {
    super(jdtNode);
    for (Object initializer : jdtNode.initializers()) {
      initializers.add((Expression) TreeConverter.convert(initializer));
    }
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    for (Object updater : jdtNode.updaters()) {
      updaters.add((Expression) TreeConverter.convert(updater));
    }
    body.set((Statement) TreeConverter.convert(jdtNode.getBody()));
  }

  public ForStatement(ForStatement other) {
    super(other);
    initializers.copyFrom(other.getInitializers());
    expression.copyFrom(other.getExpression());
    updaters.copyFrom(other.getUpdaters());
    body.copyFrom(other.getBody());
  }

  @Override
  public Kind getKind() {
    return Kind.FOR_STATEMENT;
  }

  public Expression getInitializer(int index) {
    return initializers.get(index);
  }

  public List<Expression> getInitializers() {
    return initializers;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public List<Expression> getUpdaters() {
    return updaters;
  }

  public Statement getBody() {
    return body.get();
  }

  public void setBody(Statement newBody) {
    body.set(newBody);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      initializers.accept(visitor);
      expression.accept(visitor);
      updaters.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ForStatement copy() {
    return new ForStatement(this);
  }
}
