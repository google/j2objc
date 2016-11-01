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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;

/**
 * A convenient container holding an ExecutableType and ExecutableElement. Useful because
 * ExecutableType does not provide any way to get an associated ExecutableElement.
 */
public class ExecutablePair {

  public static final ExecutablePair NULL = new ExecutablePair(null, null);

  private final ExecutableElement element;
  private final ExecutableType type;

  public ExecutablePair(ExecutableElement element, ExecutableType type) {
    this.element = element;
    this.type = type;
  }

  public ExecutablePair(ExecutableElement element) {
    this(element, (ExecutableType) element.asType());
  }

  public ExecutableElement element() {
    return element;
  }

  public ExecutableType type() {
    return type;
  }
}
