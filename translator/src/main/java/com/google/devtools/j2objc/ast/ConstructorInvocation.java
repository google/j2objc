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
 * Node for a alternate constructor invocation. (i.e. "this(...);")
 */
public class ConstructorInvocation extends Statement {

  private IMethodBinding methodBinding = null;
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public ConstructorInvocation(IMethodBinding methodBinding) {
    setMethodBinding(methodBinding);
  }

  public ConstructorInvocation(org.eclipse.jdt.core.dom.ConstructorInvocation jdtNode) {
    super(jdtNode);
    methodBinding = jdtNode.resolveConstructorBinding();
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public ConstructorInvocation(ConstructorInvocation other) {
    super(other);
    methodBinding = other.getMethodBinding();
    arguments.copyFrom(other.getArguments());
  }

  @Override
  public Kind getKind() {
    return Kind.CONSTRUCTOR_INVOCATION;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public void setMethodBinding(IMethodBinding methodBinding) {
    this.methodBinding = methodBinding;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      arguments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ConstructorInvocation copy() {
    return new ConstructorInvocation(this);
  }
}
