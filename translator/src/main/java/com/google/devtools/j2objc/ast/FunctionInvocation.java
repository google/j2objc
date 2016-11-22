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

import com.google.devtools.j2objc.types.FunctionElement;

import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * Function invocation node type.
 */
public class FunctionInvocation extends Expression {

  private FunctionElement functionElement = null;
  // The context-specific known type of this expression.
  private TypeMirror typeMirror = null;
  private boolean hasRetainedResult = false;
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public FunctionInvocation(FunctionInvocation other) {
    super(other);
    functionElement = other.getFunctionElement();
    typeMirror = other.getTypeMirror();
    arguments.copyFrom(other.getArguments());
  }

  public FunctionInvocation(FunctionElement functionElement, TypeMirror typeMirror) {
    this.functionElement = functionElement;
    this.typeMirror = typeMirror;
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_INVOCATION;
  }

  public FunctionElement getFunctionElement() {
    return functionElement;
  }

  public String getName() {
    return hasRetainedResult ? functionElement.getRetainedResultName() : functionElement.getName();
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public boolean hasRetainedResult() {
    return hasRetainedResult;
  }

  public void setHasRetainedResult(boolean hasRetainedResult) {
    this.hasRetainedResult = hasRetainedResult;
  }

  public FunctionInvocation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }

  public Expression getArgument(int index) {
    return arguments.get(index);
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
  public FunctionInvocation copy() {
    return new FunctionInvocation(this);
  }
}
