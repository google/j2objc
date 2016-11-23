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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * TypeMirror implementation for a C-style pointer type.
 *
 * @author Keith Stanger
 */
public class PointerType extends AbstractTypeMirror {

  private final TypeMirror pointeeType;

  public PointerType(TypeMirror pointeeType) {
    this.pointeeType = Preconditions.checkNotNull(pointeeType);
  }

  public TypeMirror getPointeeType() {
    return pointeeType;
  }

  @Override
  public String toString() {
    return pointeeType.toString() + "*";
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.OTHER;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitUnknown(this, p);
  }
}
