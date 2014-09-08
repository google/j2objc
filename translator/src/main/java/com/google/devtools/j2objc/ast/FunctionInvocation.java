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

import java.util.List;

/**
 * Function invocation node type.
 */
public class FunctionInvocation extends Expression {

  private String name = null;
  // The context-specific known type of this expression.
  private ITypeBinding typeBinding = null;
  // The return type of the declared function.
  private ITypeBinding declaredReturnType = null;
  private ITypeBinding declaringType = null;
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public FunctionInvocation(FunctionInvocation other) {
    super(other);
    name = other.getName();
    typeBinding = other.getTypeBinding();
    declaredReturnType = other.getDeclaredReturnType();
    declaringType = other.getDeclaringType();
    arguments.copyFrom(other.getArguments());
  }

  public FunctionInvocation(
      String name, ITypeBinding typeBinding, ITypeBinding declaredReturnType,
      ITypeBinding declaringType) {
    this.name = name;
    this.typeBinding = typeBinding;
    this.declaredReturnType = declaredReturnType;
    this.declaringType = declaringType;
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_INVOCATION;
  }

  public String getName() {
    return name;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  public ITypeBinding getDeclaredReturnType() {
    return declaredReturnType;
  }

  public ITypeBinding getDeclaringType() {
    return declaringType;
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
