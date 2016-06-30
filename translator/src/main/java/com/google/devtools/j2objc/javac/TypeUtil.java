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

package com.google.devtools.j2objc.javac;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Utility methods for working with TypeMirrors.
 *
 * @author Nathan Braswell
 */
public final class TypeUtil {

  public static boolean isTypeParameter(TypeMirror t) {
    if (!t.getKind().equals(TypeKind.DECLARED)) {
      return false;
    }
    return ((DeclaredType) t).asElement().getKind().equals(ElementKind.TYPE_PARAMETER);
  }

  public static boolean isInterface(TypeMirror t) {
    if (!t.getKind().equals(TypeKind.DECLARED)) {
      return false;
    }
    return ((DeclaredType) t).asElement().getKind().equals(ElementKind.INTERFACE);
  }
}
