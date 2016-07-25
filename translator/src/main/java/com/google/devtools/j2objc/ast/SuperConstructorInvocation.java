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
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.jdt.TreeConverter;
import java.util.List;
import javax.lang.model.element.ExecutableElement;

/**
 * Node for a super constructor invocation. (i.e. "super(...);")
 */
public class SuperConstructorInvocation extends Statement {

  private ExecutableElement method = null;
  private final ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public SuperConstructorInvocation(org.eclipse.jdt.core.dom.SuperConstructorInvocation jdtNode) {
    super(jdtNode);
    IMethodBinding methodBinding = BindingConverter.wrapBinding(
        jdtNode.resolveConstructorBinding());
    method = BindingConverter.getExecutableElement(methodBinding);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public SuperConstructorInvocation(SuperConstructorInvocation other) {
    super(other);
    method = other.getExecutableElement();
    expression.copyFrom(other.getExpression());
    arguments.copyFrom(other.getArguments());
  }

  public SuperConstructorInvocation(IMethodBinding methodBinding) {
    method = BindingConverter.getExecutableElement(methodBinding);
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_CONSTRUCTOR_INVOCATION;
  }

  public IMethodBinding getMethodBinding() {
    return (IMethodBinding) BindingConverter.unwrapElement(method);
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    method = BindingConverter.getExecutableElement(newMethodBinding);
  }

  public ExecutableElement getExecutableElement() {
    return method;
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

  public SuperConstructorInvocation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }

  public SuperConstructorInvocation addArgument(int index, Expression arg) {
    arguments.add(index, arg);
    return this;
  }
}
