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
 * Annotation that specifies the level of reflection support for a particular
 * class.
 *
 * @author Keith Stanger
 */
@Target({ ElementType.TYPE, ElementType.PACKAGE })
@Retention(RetentionPolicy.CLASS)
public @interface ReflectionSupport {

  /**
   * Enumerates the available levels of reflection support.
   */
  enum Level {
    /*
     * No metadata is emitted, so reflection support is limited to the
     * information that can be obtained from the Objective-C runtime.
     */
    NATIVE_ONLY,
    /*
     * Additional metadata is emitted, allowing for full reflection support.
     */
    FULL
  }

  Level value();
}
