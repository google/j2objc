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

import com.google.devtools.j2objc.util.TypeUtil;
import java.util.List;

/**
 * Node type for array creation.
 */
public class ArrayCreation extends Expression {

  // Indicates that this expression leaves the created object with a retain
  // count of 1. (i.e. does not call autorelease)
  private boolean hasRetainedResult = false;
  private final ChildLink<ArrayType> arrayType =
      ChildLink.create(ArrayType.class, this);
  private final ChildList<Expression> dimensions = ChildList.create(Expression.class, this);
  private final ChildLink<ArrayInitializer> initializer =
      ChildLink.create(ArrayInitializer.class, this);

  public ArrayCreation() {}

  public ArrayCreation(ArrayCreation other) {
    super(other);
    arrayType.copyFrom(other.getType());
    dimensions.copyFrom(other.getDimensions());
    initializer.copyFrom(other.getInitializer());
  }

  public ArrayCreation(javax.lang.model.type.ArrayType type, TypeUtil typeUtil, int... dimensions) {
    arrayType.set(new ArrayType(type));
    for (int i : dimensions) {
      this.dimensions.add(NumberLiteral.newIntLiteral(i, typeUtil));
    }
  }

  public ArrayCreation(ArrayInitializer initializer) {
    arrayType.set(new ArrayType((javax.lang.model.type.ArrayType) initializer.getTypeMirror()));
    this.initializer.set(initializer);
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_CREATION;
  }

  @Override
  public javax.lang.model.type.ArrayType getTypeMirror() {
    ArrayType arrayTypeNode = arrayType.get();
    return arrayTypeNode != null ? arrayTypeNode.getTypeMirror() : null;
  }

  public boolean hasRetainedResult() {
    return hasRetainedResult;
  }

  public void setHasRetainedResult(boolean hasRetainedResult) {
    this.hasRetainedResult = hasRetainedResult;
  }

  public ArrayType getType() {
    return arrayType.get();
  }

  public ArrayCreation setType(ArrayType newType) {
    arrayType.set(newType);
    return this;
  }

  public List<Expression> getDimensions() {
    return dimensions;
  }

  public ArrayCreation setDimensions(List<Expression> newDimensions) {
    dimensions.replaceAll(newDimensions);
    return this;
  }

  public ArrayInitializer getInitializer() {
    return initializer.get();
  }

  public ArrayCreation setInitializer(ArrayInitializer newInitializer) {
    initializer.set(newInitializer);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      arrayType.accept(visitor);
      dimensions.accept(visitor);
      initializer.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ArrayCreation copy() {
    return new ArrayCreation(this);
  }
}
