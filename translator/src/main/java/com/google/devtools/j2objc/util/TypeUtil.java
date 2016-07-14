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

import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.jdt.JdtIntersectionType;
import com.google.devtools.j2objc.jdt.JdtTypes;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
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

  public static ElementKind getDeclaredTypeKind(TypeMirror t) {
    return t.getKind() == TypeKind.DECLARED ? ((DeclaredType) t).asElement().getKind() : null;
  }

  public static boolean isInterface(TypeMirror t) {
    return getDeclaredTypeKind(t) == ElementKind.INTERFACE;
  }

  public static boolean isEnum(TypeMirror t) {
    return getDeclaredTypeKind(t) == ElementKind.ENUM;
  }

  public static boolean isVoid(TypeMirror t) {
    return t.getKind() == TypeKind.VOID;
  }

  // Ugly, but we can't have it actually implement IntersectionType or return TypeKind.INTERSECTION
  // until Java 8.
  public static boolean isIntersection(TypeMirror t) {
    return t instanceof JdtIntersectionType;
  }

  public static TypeElement asTypeElement(TypeMirror t) {
    if (t.getKind() != TypeKind.DECLARED) {
      return null;
    }
    Element e = ((DeclaredType) t).asElement();
    switch (e.getKind()) {
      case ANNOTATION_TYPE:
      case CLASS:
      case ENUM:
      case INTERFACE:
        return (TypeElement) e;
      default:
        return null;
    }
  }

  public static DeclaredType getSuperclass(TypeMirror t) {
    List<? extends TypeMirror> supertypes = JdtTypes.getInstance().directSupertypes(t);
    if (supertypes.isEmpty()) {
      return null;
    }
    TypeMirror first = supertypes.get(0);
    if (getDeclaredTypeKind(first).isClass()) {
      return (DeclaredType) first;
    }
    return null;
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

  public static String getQualifiedName(TypeMirror t) {
    switch (t.getKind()) {
      case ARRAY:
        return "[" + getQualifiedName(((ArrayType) t).getComponentType());
      case DECLARED:
        return ((TypeElement) ((DeclaredType) t).asElement()).getQualifiedName().toString();
      case BOOLEAN:
        return "boolean";
      case BYTE:
        return "byte";
      case CHAR:
        return "char";
      case DOUBLE:
        return "double";
      case FLOAT:
        return "float";
      case INT:
        return "int";
      case LONG:
        return "long";
      case SHORT:
        return "short";
      case VOID:
        return "void";
      default:
        throw new AssertionError("Cannot resolve qualified name for type: " + t);
    }
  }
}
