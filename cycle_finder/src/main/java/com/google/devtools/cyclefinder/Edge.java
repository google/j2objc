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

package com.google.devtools.cyclefinder;

import com.google.common.base.Objects;

/**
 * Represents a possible reference from one type to another.
 */
class Edge {

  private final TypeNode origin;
  private final TypeNode target;
  private final String fieldQualifiedName;
  private final String description;

  private Edge(TypeNode origin, TypeNode target, String fieldQualifiedName, String description) {
    this.origin = origin;
    this.target = target;
    this.fieldQualifiedName = fieldQualifiedName;
    this.description = description;
  }

  public static Edge newFieldEdge(TypeNode origin, TypeNode target, String fieldName) {
    return new Edge(origin, target, origin.getQualifiedName() + '.' + fieldName,
        "(field " + fieldName + " with type " + target.getName() + ")");
  }

  public static Edge newSubtypeEdge(Edge original, TypeNode target) {
    return new Edge(original.origin, target, original.fieldQualifiedName,
        "(" + target.getName() + " subtype of " + original.description + ")");
  }

  public static Edge newSuperclassEdge(Edge original, TypeNode origin, TypeNode superclass) {
    return new Edge(origin, original.target, original.fieldQualifiedName,
        "(superclass " + superclass.getName() + " has " + original.description + ")");
  }

  public static Edge newOuterClassEdge(TypeNode origin, TypeNode target) {
    return new Edge(origin, target, null, "(outer class " + target.getName() + ")");
  }

  public static Edge newCaptureEdge(TypeNode origin, TypeNode target, String varName) {
    return new Edge(origin, target, null,
        "(capture " + varName + " with type " + target.getName() + ")");
  }

  public TypeNode getOrigin() {
    return origin;
  }

  public TypeNode getTarget() {
    return target;
  }

  public String getFieldQualifiedName() {
    return fieldQualifiedName;
  }

  @Override
  public String toString() {
    return origin.getName() + " -> " + description;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Edge)) {
      return false;
    }
    Edge e = (Edge) o;
    return e.origin.equals(origin) && e.target.equals(target);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(origin, target);
  }
}
