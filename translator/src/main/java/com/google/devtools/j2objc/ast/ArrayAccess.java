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

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

/**
 * Array access node type.
 */
public class ArrayAccess extends Expression {

  private final ChildLink<Expression> array = ChildLink.create(Expression.class, this);
  private final ChildLink<Expression> index = ChildLink.create(Expression.class, this);

  public ArrayAccess() {}

  public ArrayAccess(ArrayAccess other) {
    super(other);
    array.copyFrom(other.getArray());
    index.copyFrom(other.getIndex());
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_ACCESS;
  }

  @Override
  public TypeMirror getTypeMirror() {
    Expression arrayNode = array.get();
    TypeMirror arrayType = arrayNode != null ? arrayNode.getTypeMirror() : null;
    return arrayType != null ? ((ArrayType) arrayType).getComponentType() : null;
  }

  public Expression getArray() {
    return array.get();
  }

  public ArrayAccess setArray(Expression newArray) {
    array.set(newArray);
    return this;
  }

  public Expression getIndex() {
    return index.get();
  }

  public ArrayAccess setIndex(Expression newIndex) {
    index.set(newIndex);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      array.accept(visitor);
      index.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ArrayAccess copy() {
    return new ArrayAccess(this);
  }
}
