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

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

/**
 * Annotation mirror class for annotations created during translation.
 */
public class GeneratedAnnotationMirror implements AnnotationMirror {
  private final DeclaredType type;
  private final Map<ExecutableElement, AnnotationValue> values = new HashMap<>();

  public GeneratedAnnotationMirror(DeclaredType annotationType) {
    this.type = annotationType;
  }

  @Override
  public DeclaredType getAnnotationType() {
    return type;
  }

  @Override
  public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
    return values;
  }

  public GeneratedAnnotationMirror addElementValue(ExecutableElement element,
      AnnotationValue value) {
    values.put(element, value);
    return this;
  }
}
