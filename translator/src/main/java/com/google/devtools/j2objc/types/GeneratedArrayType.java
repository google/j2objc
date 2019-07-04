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

package com.google.devtools.j2objc.types;

import com.google.common.base.Preconditions;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * An array type that has a generated type as its component type.
 * These can't be created by the parser environment, because these
 * component types aren't defined by the front-end.
 */
public class GeneratedArrayType extends AbstractTypeMirror implements ArrayType {

  private final TypeMirror componentType;

  public GeneratedArrayType(TypeMirror componentType) {
    Preconditions.checkNotNull(componentType);
    this.componentType = componentType;
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.ARRAY;
  }

  @Override
  public TypeMirror getComponentType() {
    return componentType;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitArray(this, p);
  }

  @Override
  @SuppressWarnings("TypeEquals")
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof GeneratedArrayType
        && componentType.equals(((GeneratedArrayType) obj).componentType);
  }

  @Override
  public int hashCode() {
    return 31 * componentType.hashCode();
  }

  @Override
  public String toString() {
    return componentType.toString() + "[]";
  }
}
