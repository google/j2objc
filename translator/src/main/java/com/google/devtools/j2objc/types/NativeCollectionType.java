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

import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.NativeExpression;
import java.util.List;
import javax.lang.model.type.TypeMirror;

/** Native type for native collections, potentially containing Java types. */
public class NativeCollectionType extends NativeType {

  public static NativeCollectionType newNativeArray(List<? extends TypeMirror> typeArguments) {
    return new NativeCollectionType("NSArray", typeArguments);
  }

  private NativeCollectionType(String name, List<? extends TypeMirror> typeArguments) {
    // All type arguments are also referenced types.
    super(name, null, typeArguments, null);
  }

  @Override
  public String getName() {
    return String.format("%s *", super.getName());
  }

  @Override
  public String getNameWithTypeArgumentNames(List<String> typeArgumentNames) {
    return String.format("%s<%s> *", super.getName(), String.join(", ", typeArgumentNames));
  }

  @Override
  public String getHeader() {
    return "JreCollectionAdapters.h"; // No native import
  }

  @Override
  public Expression getDefaultValueExpression() {
    // All native collections are pointers.
    return new NativeExpression("nil", this);
  }
}
