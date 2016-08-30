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

package com.google.devtools.j2objc.util;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * The environment used by the parser to generate compilation units.
 */
public interface ParserEnvironment {

  /**
   * Returns the element associated with a fully-qualified name.
   * Null is returned if there is no associated element for the
   * specified name.
   */
  Element resolve(String name);

  Elements elementUtilities();

  Types typeUtilities();

  // TODO(tball): remove when javac front-end update is complete.
  default void reset() {}
}
