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

import java.util.List;

/**
 * Creation reference expression AST node type (added in JLS8, section 15.13).
 */
public class CreationReference extends MethodReference {

  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildLink<Expression> creationOuterArg = ChildLink.create(Expression.class, this);
  private ChildList<Expression> creationCaptureArgs = ChildList.create(Expression.class, this);

  public CreationReference() {}

  public CreationReference(CreationReference other) {
    super(other);
    type.copyFrom(other.getType());
    creationOuterArg.copyFrom(other.getCreationOuterArg());
    creationCaptureArgs.copyFrom(other.getCreationCaptureArgs());
  }

  @Override
  public Kind getKind() {
    return Kind.CREATION_REFERENCE;
  }

  public Type getType() {
    return type.get();
  }

  public CreationReference setType(Type newType) {
    type.set(newType);
    return this;
  }

  public Expression getCreationOuterArg() {
    return creationOuterArg.get();
  }

  public CreationReference setCreationOuterArg(Expression newOuterArg) {
    creationOuterArg.set(newOuterArg);
    return this;
  }

  public List<Expression> getCreationCaptureArgs() {
    return creationCaptureArgs;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      lambdaOuterArg.accept(visitor);
      lambdaCaptureArgs.accept(visitor);
      type.accept(visitor);
      typeArguments.accept(visitor);
      creationOuterArg.accept(visitor);
      creationCaptureArgs.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public CreationReference copy() {
    return new CreationReference(this);
  }
}
