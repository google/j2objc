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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies how an enhanced for loop should be translated by
 * the J2ObjC translator.
 *
 * @author Keith Stanger
 */
@Target(ElementType.LOCAL_VARIABLE)
@Retention(RetentionPolicy.SOURCE)
public @interface LoopTranslation {

  /**
   * Enumerates the available translation options for enhanced for loops.
   * FAST_ENUMERATION is the default style emitted by the translator.
   */
  enum LoopStyle {
    /*
     * id<JavaUtilIterator> iter__ = [expr iterator];
     * while ([iter__ hasNext]) {
     *   id var = [iter__ next];
     *   ...
     * }
     */
    JAVA_ITERATOR,
    /*
     * for (id var in expr) {
     *   ...
     * }
     */
    FAST_ENUMERATION
  }

  LoopStyle value();
}
