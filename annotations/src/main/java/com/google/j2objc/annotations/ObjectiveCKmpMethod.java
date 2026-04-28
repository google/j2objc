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

package com.google.j2objc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation applicable to methods that generates a peer "adapter" method in the transpiled class
 * or interface.
 *
 * <p><b>Note:</b> This feature is very experimental and subject to change.
 *
 * <p>Adapter methods are intended to reduce friction between J2ObjC and Kotlin/Native interfaces.
 *
 * <h2>How the Adapter field Works</h2>
 *
 * <p>The adapter class (specified in the annotation) should contain methods that convert between
 * Java collection types and native Objective-C collection types. The translator looks for methods
 * in the adapter class based on name prefixes and type matching. The adapter class can be a custom
 * class defined by the user.
 *
 * <h3>Method Selection Rules</h3>
 *
 * <p>The translator selects adapter methods based on the direction of conversion:
 *
 * <ul>
 *   <li><b>Native to Java (Method Parameters)</b>: The translator looks for methods whose name
 *       starts with "to" and whose <b>return type</b> matches the target Java type. It assumes the
 *       single parameter of this method is the native Objective-C type.
 *   <li><b>Java to Native (Method Return Value)</b>: The translator looks for methods whose name
 *       starts with "from" and whose <b>parameter type</b> matches the source Java type. It assumes
 *       the return type of this method is the native Objective-C type.
 * </ul>
 *
 * <h3>Type Matching (Candidates)</h3>
 *
 * <p>When matching types, the translator generates candidate types by progressively replacing type
 * arguments with wildcards ({@code ?}) from deepest to shallowest. It searches for a matching
 * adapter method for each candidate, prioritizing the most specific type first. This allows for
 * custom adapter implementations to handle parameterized types with collection types or other
 * mapped types.
 *
 * <p>For example, for the type {@code List<String>}, the candidates are:
 *
 * <ol>
 *   <li>{@code List<String>} (depth 1)
 *   <li>{@code List<?>} (depth 0)
 * </ol>
 *
 * <p>The translator will first try to find an adapter method that matches {@code List<String>}. If
 * not found, it will fallback to a method that matches {@code List<?>}.
 */
@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface ObjectiveCKmpMethod {
  /**
   * The Objective-C selector to use for the adapter method. Has the same naming requirements as the
   * ObjectiveCName annotation for methods. This field is required.
   */
  String selector();

  /**
   * The class to map types on the method to Kotlin/Native interop types. This field is required.
   */
  Class<?> adapter();
}
