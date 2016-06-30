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

import com.google.devtools.j2objc.javac.BindingConverter;
import com.google.devtools.j2objc.types.FunctionBinding;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * Function invocation node type.
 */
public class FunctionInvocation extends Expression {

  private FunctionBinding functionBinding = null;
  // The context-specific known type of this expression.
  private ITypeBinding typeBinding = null;
  private TypeMirror typeMirror = null;
  private boolean hasRetainedResult = false;
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public FunctionInvocation(FunctionInvocation other) {
    super(other);
    functionBinding = other.getFunctionBinding();
    typeBinding = other.getTypeBinding();
    typeMirror = other.getTypeMirror();
    arguments.copyFrom(other.getArguments());
  }

  public FunctionInvocation(FunctionBinding functionBinding, ITypeBinding typeBinding) {
    this.functionBinding = functionBinding;
    this.typeBinding = typeBinding;
    this.typeMirror = BindingConverter.getType(typeBinding);
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_INVOCATION;
  }

  public FunctionBinding getFunctionBinding() {
    return functionBinding;
  }

  public String getName() {
    return hasRetainedResult ? functionBinding.getRetainedResultName() : functionBinding.getName();
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
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
