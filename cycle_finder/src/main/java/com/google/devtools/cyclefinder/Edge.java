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

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Represents a possible reference from one type to another.
 */
class Edge {

  private IVariableBinding field;
  private ITypeBinding origin;
  private ITypeBinding target;
  private String description;

  private Edge(
      IVariableBinding field, ITypeBinding origin, ITypeBinding target, String description) {
    this.field = field;
    this.origin = origin;
    this.target = target;
    this.description = description;
  }

  private static Edge newVarEdge(ITypeBinding origin, IVariableBinding field, String varType) {
    ITypeBinding type = field.getType();
    ITypeBinding target = type.isArray() ? type.getElementType() : type;
    assert !target.isPrimitive();
    return new Edge(field, origin, target,
        "(" + varType + " " + field.getName() + " with type " + TypeCollector.getNameForType(type)
        + ")");
  }

  public static Edge newFieldEdge(ITypeBinding origin, IVariableBinding field) {
    return newVarEdge(origin, field, "field");
  }

  public static Edge newSubtypeEdge(Edge original, ITypeBinding target) {
    return new Edge(original.field, original.origin, target,
        "(" + TypeCollector.getNameForType(target) + " subtype of " + original.description + ")");
  }

  public static Edge newSuperclassEdge(
      Edge original, ITypeBinding origin, ITypeBinding superclass) {
    return new Edge(original.field, origin, original.target,
        "(superclass " + TypeCollector.getNameForType(superclass) + " has " + original.description
        + ")");
  }

  public static Edge newOuterClassEdge(ITypeBinding origin, ITypeBinding target) {
    return new Edge(null, origin, target,
        "(outer class " + TypeCollector.getNameForType(target) + ")");
  }

  public static Edge newCaptureEdge(ITypeBinding origin, IVariableBinding capturedVar) {
    return newVarEdge(origin, capturedVar, "capture");
  }

  public IVariableBinding getField() {
    return field;
  }

  public ITypeBinding getOrigin() {
    return origin;
  }

  public ITypeBinding getTarget() {
    return target;
  }

  public String toString() {
    return TypeCollector.getNameForType(origin) + " -> " + description;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Edge)) {
      return false;
    }
    Edge e = (Edge) o;
    return e.origin.getKey().equals(origin.getKey()) && e.target.getKey().equals(target.getKey());
  }

  public int hashCode() {
    return Objects.hashCode(origin.getKey(), target.getKey());
  }
}
