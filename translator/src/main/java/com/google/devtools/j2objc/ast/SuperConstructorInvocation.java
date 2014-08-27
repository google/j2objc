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

import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.List;

/**
 * Node for a super constructor invocation. (i.e. "super(...);")
 */
public class SuperConstructorInvocation extends Statement {

  private IMethodBinding methodBinding = null;
  private final ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public SuperConstructorInvocation(org.eclipse.jdt.core.dom.SuperConstructorInvocation jdtNode) {
    super(jdtNode);
    methodBinding = jdtNode.resolveConstructorBinding();
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public SuperConstructorInvocation(SuperConstructorInvocation other) {
    super(other);
    methodBinding = other.getMethodBinding();
    expression.copyFrom(other.getExpression());
    arguments.copyFrom(other.getArguments());
  }

  public SuperConstructorInvocation(IMethodBinding methodBinding) {
    this.methodBinding = methodBinding;
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_CONSTRUCTOR_INVOCATION;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    methodBinding = newMethodBinding;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      arguments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SuperConstructorInvocation copy() {
    return new SuperConstructorInvocation(this);
  }
}
