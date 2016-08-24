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
import javax.lang.model.type.TypeMirror;

/**
 * Type node for an intersection type in a cast expression (added in JLS8, section 4.9).
 */
public class IntersectionType extends Type {

  private ChildList<Type> types = ChildList.create(Type.class, this);

  public IntersectionType(TypeMirror typeMirror) {
    super(typeMirror);
  }

  public IntersectionType(IntersectionType other) {
    super(other);
    types.copyFrom(other.types);
  }

  @Override
  public Kind getKind() {
    return Kind.INTERSECTION_TYPE;
  }

  public List<Type> types() {
    return types;
  }

  public IntersectionType addType(Type type) {
    types.add(type);
    return this;
  }

  @Override
  public boolean isIntersectionType() {
    return true;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      types.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public IntersectionType copy() {
    return new IntersectionType(this);
  }
}
