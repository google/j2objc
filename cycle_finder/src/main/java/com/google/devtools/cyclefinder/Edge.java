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

import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Represents a possible reference from one type to another.
 */
class Edge {

  private IVariableBinding field;
  private TypeNode origin;
  private TypeNode target;
  private String description;

  private Edge(
      IVariableBinding field, TypeNode origin, TypeNode target, String description) {
    this.field = field;
    this.origin = origin;
    this.target = target;
    this.description = description;
  }

  public static Edge newFieldEdge(TypeNode origin, TypeNode target, IVariableBinding field) {
    return new Edge(field, origin, target,
        "(field " + field.getName() + " with type " + target.getName() + ")");
  }

  public static Edge newSubtypeEdge(Edge original, TypeNode target) {
    return new Edge(original.field, original.origin, target,
        "(" + target.getName() + " subtype of " + original.description + ")");
  }

  public static Edge newSuperclassEdge(Edge original, TypeNode origin, TypeNode superclass) {
    return new Edge(original.field, origin, original.target,
        "(superclass " + superclass.getName() + " has " + original.description + ")");
  }

  public static Edge newOuterClassEdge(TypeNode origin, TypeNode target) {
    return new Edge(null, origin, target, "(outer class " + target.getName() + ")");
  }

  public static Edge newCaptureEdge(
      TypeNode origin, TypeNode target, IVariableBinding capturedVar) {
    return new Edge(capturedVar, origin, target,
        "(capture " + capturedVar.getName() + " with type " + target.getName() + ")");
  }

  public IVariableBinding getField() {
    return field;
  }

  public TypeNode getOrigin() {
    return origin;
  }

  public TypeNode getTarget() {
    return target;
  }

  public String toString() {
    return origin.getName() + " -> " + description;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Edge)) {
      return false;
    }
    Edge e = (Edge) o;
    return e.origin.equals(origin) && e.target.equals(target);
  }

  public int hashCode() {
    return Objects.hashCode(origin, target);
  }
}
