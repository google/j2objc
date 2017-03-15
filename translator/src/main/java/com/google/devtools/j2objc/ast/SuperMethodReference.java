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

/**
 * Super method reference AST node type (added in JLS8, section 15.13).
 */
public class SuperMethodReference extends MethodReference {

  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);
  // Resolved by OuterReferenceResolver.
  private ChildLink<Expression> receiver = ChildLink.create(Expression.class, this);

  public SuperMethodReference() {}

  public SuperMethodReference(SuperMethodReference other) {
    super(other);
    qualifier.copyFrom(other.getQualifier());
    receiver.copyFrom(other.getReceiver());
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_METHOD_REFERENCE;
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public SuperMethodReference setQualifier(Name newQualifier) {
    qualifier.set(newQualifier);
    return this;
  }

  public Expression getReceiver() {
    return receiver.get();
  }

  public SuperMethodReference setReceiver(Expression newReceiver) {
    receiver.set(newReceiver);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      lambdaOuterArg.accept(visitor);
      lambdaCaptureArgs.accept(visitor);
      qualifier.accept(visitor);
      typeArguments.accept(visitor);
      receiver.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SuperMethodReference copy() {
    return new SuperMethodReference(this);
  }
}
