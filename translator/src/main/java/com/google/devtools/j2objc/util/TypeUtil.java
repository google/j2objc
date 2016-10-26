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
import com.google.devtools.j2objc.types.ExecutablePair;
import java.util.LinkedHashSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Utility methods for working with TypeMirrors.
 *
 * @author Nathan Braswell
 */
public final class TypeUtil {

  private final Types javacTypes;
  private final ElementUtil elementUtil;

  public TypeUtil(Types javacTypes, ElementUtil elementUtil) {
    this.javacTypes = javacTypes;
    this.elementUtil = elementUtil;
  }

  public ElementUtil elementUtil() {
    return elementUtil;
  }

  public static ElementKind getDeclaredTypeKind(TypeMirror t) {
    return t.getKind() == TypeKind.DECLARED ? ((DeclaredType) t).asElement().getKind() : null;
  }

  public static boolean isClass(TypeMirror t) {
    ElementKind kind = getDeclaredTypeKind(t);
    return kind != null ? kind.isClass() : false;
  }

  public static boolean isInterface(TypeMirror t) {
    ElementKind kind = getDeclaredTypeKind(t);
    return kind != null ? kind.isInterface() : false;
  }

  public static boolean isEnum(TypeMirror t) {
    return getDeclaredTypeKind(t) == ElementKind.ENUM;
  }

  public static boolean isVoid(TypeMirror t) {
    return t.getKind() == TypeKind.VOID;
  }

  public static boolean isArray(TypeMirror t) {
    return t.getKind() == TypeKind.ARRAY;
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

  public DeclaredType getSuperclass(TypeMirror t) {
    List<? extends TypeMirror> supertypes = directSupertypes(t);
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

  public ExecutableType asMemberOf(DeclaredType containing, ExecutableElement method) {
    return (ExecutableType) javacTypes.asMemberOf(containing, method);
  }

  public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    return javacTypes.isAssignable(t1, t2);
  }

  public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
    return javacTypes.isSubtype(t1, t2);
  }

  public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
    return javacTypes.isSubsignature(m1, m2);
  }

  public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
    return javacTypes.directSupertypes(t);
  }

  public TypeMirror erasure(TypeMirror t) {
    return javacTypes.erasure(t);
  }

  public ArrayType getArrayType(TypeMirror componentType) {
    return javacTypes.getArrayType(componentType);
  }

  /**
   * Find a supertype matching the given qualified name.
   */
  public DeclaredType findSupertype(TypeMirror type, String qualifiedName) {
    TypeElement element = asTypeElement(type);
    if (element != null && element.getQualifiedName().toString().equals(qualifiedName)) {
      return (DeclaredType) type;
    }
    for (TypeMirror t : directSupertypes(type)) {
      DeclaredType result = findSupertype(t, qualifiedName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public ExecutablePair findMethod(DeclaredType type, String name, String... paramTypes) {
    ExecutableElement methodElem =
        ElementUtil.findMethod((TypeElement) type.asElement(), name, paramTypes);
    if (methodElem != null) {
      return new ExecutablePair(methodElem, asMemberOf(type, methodElem));
    }
    return null;
  }

  public LinkedHashSet<DeclaredType> getObjcOrderedInheritedTypes(TypeMirror type) {
    LinkedHashSet<DeclaredType> inheritedTypes = new LinkedHashSet<>();
    visitTypeHierarchyObjcOrder(type, visitType -> {
      inheritedTypes.add(visitType);
      return true;
    });
    return inheritedTypes;
  }

  /**
   * Visitor type for calling the methods below. Return true to continue visiting, false to
   * short-circuit.
   */
  public interface TypeVisitor {
    boolean accept(DeclaredType type);
  }

  /**
   * Visit all declared types in the hierarchy using a depth-first traversal, visiting classes
   * before interfaces.
   */
  public boolean visitTypeHierarchy(TypeMirror type, TypeVisitor visitor) {
    boolean result = true;
    if (type == null) {
      return result;
    }
    if (type.getKind() == TypeKind.DECLARED) {
      result = visitor.accept((DeclaredType) type);
    }
    for (TypeMirror superType : directSupertypes(type)) {
      if (!result) {
        return false;
      }
      result = visitTypeHierarchy(superType, visitor);
    }
    return result;
  }

  /**
   * Visit all declared types in the order that Objective-C compilation will visit when resolving
   * the type signature of a method. Uses a depth-first traversal, visiting interfaces before
   * classes.
   */
  public boolean visitTypeHierarchyObjcOrder(TypeMirror type, TypeVisitor visitor) {
    boolean result = true;
    if (type == null) {
      return result;
    }
    if (type.getKind() == TypeKind.DECLARED) {
      result = visitor.accept((DeclaredType) type);
    }
    // Visit the class type after interface types which is the order the ObjC compiler visits the
    // hierarchy.
    TypeMirror classType = null;
    for (TypeMirror superType : directSupertypes(type)) {
      if (!result) {
        return false;
      }
      if (isClass(superType)) {
        classType = superType;
      } else {
        visitTypeHierarchyObjcOrder(superType, visitor);
      }
    }
    if (classType != null && result) {
      result = visitTypeHierarchyObjcOrder(classType, visitor);
    }
    return result;
  }

  public static boolean isReferenceType(TypeMirror t) {
    switch (t.getKind()) {
      case ARRAY:
      case DECLARED:
      case ERROR:
      case INTERSECTION:
      case NULL:
      case TYPEVAR:
      case UNION:
      case WILDCARD:
        return true;
      default:
        return false;
    }
  }

  public PrimitiveType getPrimitiveType(TypeKind kind) {
    return javacTypes.getPrimitiveType(kind);
  }

  public NoType getVoidType() {
    return javacTypes.getNoType(TypeKind.VOID);
  }

  public PrimitiveType unboxedType(TypeMirror t) {
    try {
      return javacTypes.unboxedType(t);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public boolean isBoxedType(TypeMirror t) {
    return unboxedType(t) != null;
  }

  public TypeElement boxedClass(PrimitiveType t) {
    return javacTypes.boxedClass(t);
  }

  public static String getName(TypeMirror t) {
    switch (t.getKind()) {
      case ARRAY:
        return getName(((ArrayType) t).getComponentType()) + "[]";
      case DECLARED:
        return ElementUtil.getName(asTypeElement(t));
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
        throw new AssertionError("Cannot resolve name for type: " + t);
    }
  }

  public static String getQualifiedName(TypeMirror t) {
    switch (t.getKind()) {
      case ARRAY:
        return "[" + getQualifiedName(((ArrayType) t).getComponentType());
      case DECLARED:
        return asTypeElement(t).getQualifiedName().toString();
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case DOUBLE:
      case FLOAT:
      case INT:
      case LONG:
      case SHORT:
      case VOID:
        return getName(t);
      default:
        throw new AssertionError("Cannot resolve qualified name for type: " + t);
    }
  }

  public String getSignatureName(TypeMirror t) {
    t = erasure(t);
    switch (t.getKind()) {
      case ARRAY:
        return "[" + getSignatureName(((ArrayType) t).getComponentType());
      case DECLARED:
        return "L" + elementUtil.getBinaryName(asTypeElement(t)).replace('.', '/') + ";";
      case BOOLEAN:
        return "Z";
      case BYTE:
        return "B";
      case CHAR:
        return "C";
      case DOUBLE:
        return "D";
      case FLOAT:
        return "F";
      case INT:
        return "I";
      case LONG:
        return "J";
      case SHORT:
        return "S";
      case VOID:
        return "V";
      default:
        throw new AssertionError("Cannot resolve binary name for type: " + t);
    }
  }
}
