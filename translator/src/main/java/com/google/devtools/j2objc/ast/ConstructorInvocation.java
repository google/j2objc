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
 * Node for a alternate constructor invocation. (i.e. "this(...);")
 */
public class ConstructorInvocation extends Statement {

  private ExecutableElement method = null;
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public ConstructorInvocation(IMethodBinding methodBinding) {
    setMethodBinding(methodBinding);
  }

  public ConstructorInvocation(org.eclipse.jdt.core.dom.ConstructorInvocation jdtNode) {
    super(jdtNode);
    IMethodBinding methodBinding = BindingConverter.wrapBinding(
        jdtNode.resolveConstructorBinding());
    method = BindingConverter.getExecutableElement(methodBinding);
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public ConstructorInvocation(ConstructorInvocation other) {
    super(other);
    method = other.getExecutableElement();
    arguments.copyFrom(other.getArguments());
  }

  @Override
  public Kind getKind() {
    return Kind.CONSTRUCTOR_INVOCATION;
  }

  public IMethodBinding getMethodBinding() {
    return (IMethodBinding) BindingConverter.unwrapElement(method);
  }

  public void setMethodBinding(IMethodBinding methodBinding) {
    method = BindingConverter.getExecutableElement(methodBinding);
  }

  public ExecutableElement getExecutableElement() {
    return method;
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

  public ConstructorInvocation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }
}
