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
 * Node type for array creation.
 */
public class ArrayCreation extends Expression {

  private final ChildList<Expression> dimensions = ChildList.create(Expression.class, this);
  private final ChildLink<ArrayInitializer> initializer =
      ChildLink.create(ArrayInitializer.class, this);

  public ArrayCreation(org.eclipse.jdt.core.dom.ArrayCreation jdtNode) {
    super(jdtNode);
    for (Object dimension : jdtNode.dimensions()) {
      dimensions.add((Expression) TreeConverter.convert(dimension));
    }
    initializer.set((ArrayInitializer) TreeConverter.convert(jdtNode.getInitializer()));
  }

  public ArrayCreation(ArrayCreation other) {
    super(other);
    dimensions.copyFrom(other.getDimensions());
    initializer.copyFrom(other.getInitializer());
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_CREATION;
  }

  public List<Expression> getDimensions() {
    return dimensions;
  }

  public ArrayInitializer getInitializer() {
    return initializer.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
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
