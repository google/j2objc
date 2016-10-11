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
 * Type method reference expression AST node type (added in JLS8, section 15.13).
 */
public class TypeMethodReference extends MethodReference {

  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);

  public TypeMethodReference() {}

  public TypeMethodReference(TypeMethodReference other) {
    super(other);
    type.copyFrom(other.getType());
    name.copyFrom(other.getName());
  }

  @Override
  public Kind getKind() {
    return Kind.TYPE_METHOD_REFERENCE;
  }

  public SimpleName getName() {
    return name.get();
  }

  public TypeMethodReference setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  public Type getType() {
    return type.get();
  }

  public TypeMethodReference setType(Type newType) {
    type.set(newType);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      lambdaOuterArg.accept(visitor);
      lambdaCaptureArgs.accept(visitor);
      type.accept(visitor);
      typeArguments.accept(visitor);
      name.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public TypeMethodReference copy() {
    return new TypeMethodReference(this);
  }
}
