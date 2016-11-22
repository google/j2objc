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

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.type.TypeMirror;

/**
 * Base class for all type nodes.
 */
public abstract class Type extends TreeNode {

  protected TypeMirror typeMirror;

  Type() {}

  public Type(Type other) {
    super(other);
    typeMirror = other.getTypeMirror();
    Preconditions.checkNotNull(typeMirror);
  }

  public Type(TypeMirror typeMirror) {
    super();
    this.typeMirror = typeMirror;
    Preconditions.checkNotNull(typeMirror);
  }

  public static Type newType(TypeMirror typeMirror) {
    if (TypeUtil.isPrimitiveOrVoid(typeMirror)) {
      return new PrimitiveType(typeMirror);
    } else if (TypeUtil.isArray(typeMirror)) {
      return new ArrayType((javax.lang.model.type.ArrayType) typeMirror);
    } else {
      return new SimpleType(typeMirror);
    }
  }

  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public Type setTypeMirror(TypeMirror newTypeMirror) {
    typeMirror = newTypeMirror;
    return this;
  }

  public boolean isPrimitiveType() {
    return false;
  }

  public boolean isIntersectionType() {
    return false;
  }

  public boolean isAnnotatable() {
    return false;
  }

  @Override
  public abstract Type copy();

  @Override
  public void validateInner() {
    super.validateInner();
  }
}
