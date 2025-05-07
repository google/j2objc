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
import com.google.common.collect.ImmutableList;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import javax.lang.model.type.TypeMirror;

/** Native type for enums generated from Java enums. */
public class NativeEnumType extends NativeType {

  public NativeEnumType(String name, TypeMirror javaEnumType) {
    // Because we do not control the ordering of the type declarations for enums we
    // need to be compatible with both forward declarations and the typedef so we
    // make it explicit this is an enum.
    super(
        UnicodeUtils.format("enum %s", name),
        null,
        null,
        ImmutableList.of(checkIsEnum(javaEnumType)));
  }

  @CanIgnoreReturnValue
  private static TypeMirror checkIsEnum(TypeMirror javaEnumType) {
    Preconditions.checkArgument(TypeUtil.isEnum(javaEnumType));
    return javaEnumType;
  }

  @Override
  public String getForwardDeclaration() {
    return UnicodeUtils.format("%s : int32_t", getName());
  }

  @Override
  public Expression getDefaultValueExpression() {
    // Enum zero values are not a great default (represents a real value) but we need this
    // to be in range for native-to-Java enum functions.
    return new NativeExpression("0", this);
  }
}
