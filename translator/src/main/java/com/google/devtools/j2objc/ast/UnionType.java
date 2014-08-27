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
 * Node for a union type. (Used inside a Java7 multicatch)
 */
public class UnionType extends Type {

  private ChildList<Type> types = ChildList.create(Type.class, this);

  public UnionType(org.eclipse.jdt.core.dom.UnionType jdtNode) {
    super(jdtNode);
    for (Object type : jdtNode.types()) {
      types.add((Type) TreeConverter.convert(type));
    }
  }

  public UnionType(UnionType other) {
    super(other);
    types.copyFrom(other.getTypes());
  }

  @Override
  public Kind getKind() {
    return Kind.UNION_TYPE;
  }

  public List<Type> getTypes() {
    return types;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      types.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public UnionType copy() {
    return new UnionType(this);
  }
}
