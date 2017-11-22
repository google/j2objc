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

package javax.lang.model.type;

public enum TypeKind {

  BOOLEAN(true),
  BYTE(true),
  SHORT(true),
  INT(true),
  LONG(true),
  CHAR(true),
  FLOAT(true),
  DOUBLE(true),
  VOID(false),
  NONE(false),
  NULL(false),
  ARRAY(false),
  DECLARED(false),
  ERROR(false),
  TYPEVAR(false),
  WILDCARD(false),
  PACKAGE(false),
  EXECUTABLE(false),
  OTHER(false),
  UNION(false),
  INTERSECTION(false);

  private final boolean isPrimitive;

  TypeKind(boolean isPrimitive) {
    this.isPrimitive = isPrimitive;
  }

  public boolean isPrimitive() {
    return isPrimitive;
  }
}
