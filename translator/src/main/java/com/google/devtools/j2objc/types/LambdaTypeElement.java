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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.type.TypeMirror;

/**
 * Element class for lambdas and method references. This type mainly exists for the purpose of
 * isLambda checks.
 *
 * @author Keith Stanger
 */
public class LambdaTypeElement extends GeneratedTypeElement {

  private final boolean isWeakOuter;

  public LambdaTypeElement(
      String name, Element enclosingElement, TypeMirror superclass, boolean isWeakOuter) {
    super(name, ElementKind.CLASS, enclosingElement, superclass, NestingKind.ANONYMOUS, null,
          false, false);
    this.isWeakOuter = isWeakOuter;
    addModifiers(Modifier.PRIVATE);
  }

  public boolean isWeakOuter() {
    return isWeakOuter;
  }
}
