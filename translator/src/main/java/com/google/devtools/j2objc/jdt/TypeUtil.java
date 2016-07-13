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

package com.google.devtools.j2objc.jdt;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ArrayType;
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

  public static boolean isEnum(TypeMirror t) {
    if (!t.getKind().equals(TypeKind.DECLARED)) {
      return false;
    }
    return ((DeclaredType) t).asElement().getKind().equals(ElementKind.ENUM);
  }

  // Ugly, but we can't have it actually implement IntersectionType or return TypeKind.INTERSECTION
  // until Java 8.
  public static boolean isIntersection(TypeMirror t) {
    return t instanceof JdtIntersectionType;
  }

  public static int getDimensions(ArrayType arrayType) {
    int dimCount = 0;
    TypeMirror t = arrayType;
    while (t.getKind().equals(TypeKind.ARRAY)) {
      dimCount++;
      t = (((ArrayType) t).getComponentType());
    }
    return dimCount;
  }

  public static int getModifiers(TypeMirror t) {
    // the public modifier api doesn't expose synthetic
    return BindingConverter.unwrapTypeMirrorIntoTypeBinding(t).getModifiers();
  }
}
