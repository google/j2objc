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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.devtools.j2objc.types.AbstractTypeMirror;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedArrayType;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.types.PointerType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Utility methods for working with TypeMirrors.
 *
 * @author Nathan Braswell
 */
public final class TypeUtil {

  public static final TypeMirror ID_TYPE = new NativeType("id");
  public static final TypeMirror ID_PTR_TYPE = new PointerType(ID_TYPE);
  public static final TypeElement NS_OBJECT =
      GeneratedTypeElement.newIosClass("NSObject", null, "");
  public static final TypeElement NS_STRING =
      GeneratedTypeElement.newIosClass("NSString", NS_OBJECT, "");
  public static final TypeElement NS_EXCEPTION =
      GeneratedTypeElement.newIosClass("NSException", NS_OBJECT, "");
  public static final TypeElement NS_NUMBER =
      GeneratedTypeElement.newIosClass("NSNumber", NS_OBJECT, "");
  public static final TypeElement IOS_CLASS =
      GeneratedTypeElement.newIosClass("IOSClass", NS_OBJECT, "IOSClass.h");
  public static final TypeElement NS_COPYING =
      GeneratedTypeElement.newIosInterface("NSCopying", "");
  public static final TypeElement NS_FASTENUMERATION =
      GeneratedTypeElement.newIosInterface("NSFastEnumeration", "");
  public static final TypeElement IOS_OBJECT_ARRAY =
      GeneratedTypeElement.newIosClass("IOSObjectArray", NS_OBJECT, "IOSObjectArray.h");
  public static final TypeMirror NATIVE_CHAR_PTR = new NativeType("char *");
  private static final Map<TypeKind, TypeElement> PRIMITIVE_IOS_ARRAYS;

  static {
    Map<TypeKind, TypeElement> map = new EnumMap<>(TypeKind.class);
    map.put(TypeKind.BOOLEAN, newPrimitiveIosArray("IOSBooleanArray"));
    map.put(TypeKind.BYTE, newPrimitiveIosArray("IOSByteArray"));
    map.put(TypeKind.CHAR, newPrimitiveIosArray("IOSCharArray"));
    map.put(TypeKind.DOUBLE, newPrimitiveIosArray("IOSDoubleArray"));
    map.put(TypeKind.FLOAT, newPrimitiveIosArray("IOSFloatArray"));
    map.put(TypeKind.INT, newPrimitiveIosArray("IOSIntArray"));
    map.put(TypeKind.LONG, newPrimitiveIosArray("IOSLongArray"));
    map.put(TypeKind.SHORT, newPrimitiveIosArray("IOSShortArray"));
    PRIMITIVE_IOS_ARRAYS = map;
  }

  private final Elements javacElements;
  private final Types javacTypes;
  private final ElementUtil elementUtil;

  // Commonly accessed types.
  private final TypeElement javaObject;
  private final TypeElement javaString;
  private final TypeElement javaClass;
  private final TypeElement javaNumber;
  private final TypeElement javaThrowable;

  private final Map<TypeElement, TypeElement> javaToObjcTypeMap;

  private static final Joiner INNER_CLASS_JOINER = Joiner.on('$');

  public TypeUtil(ParserEnvironment env, ElementUtil elementUtil) {
    this.javacElements = env.elementUtilities();
    this.javacTypes = env.typeUtilities();
    this.elementUtil = elementUtil;

    javaObject = javacElements.getTypeElement("java.lang.Object");
    javaString = javacElements.getTypeElement("java.lang.String");
    javaClass = javacElements.getTypeElement("java.lang.Class");
    javaNumber = javacElements.getTypeElement("java.lang.Number");
    javaThrowable = javacElements.getTypeElement("java.lang.Throwable");
    TypeElement javaCloneable = javacElements.getTypeElement("java.lang.Cloneable");

    ImmutableMap.Builder<TypeElement, TypeElement> typeMapBuilder =
        ImmutableMap.<TypeElement, TypeElement>builder()
        .put(javaObject, NS_OBJECT)
        .put(javaString, NS_STRING)
        .put(javaClass, IOS_CLASS)
        .put(javaNumber, NS_NUMBER)
        .put(javaCloneable, NS_COPYING);

    TypeElement typeNSException = javacElements.getTypeElement("com.google.j2objc.NSException");
    TypeElement typeNSFastEnumeration =
        javacElements.getTypeElement("com.google.j2objc.NSFastEnumeration");

    // Types could be null if the user is not using jre_emul.jar as the boot path.
    if (typeNSException != null) {
      typeMapBuilder.put(typeNSException, NS_EXCEPTION);
    }
    if (typeNSFastEnumeration != null) {
      typeMapBuilder.put(typeNSFastEnumeration, NS_FASTENUMERATION);
    }

    javaToObjcTypeMap = typeMapBuilder.buildOrThrow();
  }

  public ElementUtil elementUtil() {
    return elementUtil;
  }

  public TypeElement resolveJavaType(String qualifiedName) {
    return javacElements.getTypeElement(qualifiedName);
  }

  public static boolean isDeclaredType(TypeMirror t) {
    return t.getKind() == TypeKind.DECLARED;
  }

  public static ElementKind getDeclaredTypeKind(TypeMirror t) {
    return isDeclaredType(t) ? ((DeclaredType) t).asElement().getKind() : null;
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

  public static boolean isAnnotation(TypeMirror t) {
    return getDeclaredTypeKind(t) == ElementKind.ANNOTATION_TYPE;
  }

  public static boolean isBoolean(TypeMirror t) {
    return t.getKind() == TypeKind.BOOLEAN;
  }

  public static boolean isVoid(TypeMirror t) {
    return t.getKind() == TypeKind.VOID;
  }

  public static boolean isPrimitiveOrVoid(TypeMirror t) {
    return isVoid(t) || t.getKind().isPrimitive();
  }

  public static boolean isNone(TypeMirror t) {
    // Check for null because BindingConverter converts null bindings to null types.
    return t == null || t.getKind() == TypeKind.NONE;
  }

  public static boolean isArray(TypeMirror t) {
    return t.getKind() == TypeKind.ARRAY;
  }

  public static boolean isFloatingPoint(TypeMirror t) {
    TypeKind kind = t.getKind();
    return kind == TypeKind.FLOAT || kind == TypeKind.DOUBLE;
  }

  public static boolean isIntersection(TypeMirror t) {
    return t.getKind() == TypeKind.INTERSECTION;
  }

  public static boolean isTypeVariable(TypeMirror t) {
    return t.getKind() == TypeKind.TYPEVAR;
  }

  public static TypeElement asTypeElement(TypeMirror t) {
    if (isDeclaredType(t)) {
      return (TypeElement) ((DeclaredType) t).asElement();
    }
    // Return the left-most component of an intersection type, for compatibility with JDK 10 and
    // earlier.
    if (isIntersection(t)) {
      return asTypeElement(((IntersectionType) t).getBounds().iterator().next());
    }
    return null;
  }

  public static boolean isJavaObject(TypeMirror t) {
    TypeElement typeElement = asTypeElement(t);
    return typeElement != null && !isInterface(t) && isNone(typeElement.getSuperclass());
  }

  public static TypeParameterElement asTypeParameterElement(TypeMirror t) {
    return isTypeVariable(t) ? (TypeParameterElement) ((TypeVariable) t).asElement() : null;
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
      t = ((ArrayType) t).getComponentType();
    }
    return dimCount;
  }

  public ExecutableType asMemberOf(DeclaredType containing, ExecutableElement method) {
    return (ExecutableType) javacTypes.asMemberOf(containing, method);
  }

  public TypeMirror asMemberOf(DeclaredType containing, VariableElement var) {
    return javacTypes.asMemberOf(containing, var);
  }

  public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    if (isGeneratedType(t1) || isGeneratedType(t2)) {
      // TODO(antoniocortes): implement as part of converting Elements to their generated versions.
      return false;
    }
    return javacTypes.isAssignable(t1, t2);
  }

  public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
    if (isGeneratedType(t1) || isGeneratedType(t2)) {
      // TODO(antoniocortes): implement as part of converting Elements to their generated versions.
      return false;
    }
    return javacTypes.isSubtype(t1, t2);
  }

  @SuppressWarnings("TypeEquals")
  public boolean isSameType(TypeMirror t1, TypeMirror t2) {
    if (isGeneratedType(t1) || isGeneratedType(t2)) {
      return t1.equals(t2);
    }
    return javacTypes.isSameType(t1, t2);
  }

  @SuppressWarnings("TypeEquals")
  public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
    if (isGeneratedType(m1) || isGeneratedType(m2)) {
      return m1.equals(m2);
    }
    return javacTypes.isSubsignature(m1, m2);
  }

  /**
   * If type is a byte TypeMirror, short TypeMirror, or char TypeMirror, an int TypeMirror is
   * returned. Otherwise, type is returned. See jls-5.6.1.
   * @param type a numeric type
   * @return the result of unary numeric promotion applied to type
   */
  public TypeMirror unaryNumericPromotion(TypeMirror type) {
    TypeKind t = type.getKind();
    if (t == TypeKind.DECLARED) {
      type = javacTypes.unboxedType(type);
      t = type.getKind();
    }
    if (t == TypeKind.BYTE || t == TypeKind.SHORT || t == TypeKind.CHAR) {
      return getInt();
    } else {
      return type;
    }
  }

  /**
   * If either type is a double TypeMirror, a double TypeMirror is returned.
   * Otherwise, if either type is a float TypeMirror, a float TypeMirror is returned.
   * Otherwise, if either type is a long TypeMirror, a long TypeMirror is returned.
   * Otherwise, an int TypeMirror is returned. See jls-5.6.2.
   * @param type1 a numeric type
   * @param type2 a numeric type
   * @return the result of binary numeric promotion applied to type1 and type2
   */
  public TypeMirror binaryNumericPromotion(TypeMirror type1, TypeMirror type2) {
    TypeKind t1 = type1.getKind();
    TypeKind t2 = type2.getKind();
    if (t1 == TypeKind.DECLARED) {
      t1 = javacTypes.unboxedType(type1).getKind();
    }
    if (t2 == TypeKind.DECLARED) {
      t2 = javacTypes.unboxedType(type2).getKind();
    }
    if (t1 == TypeKind.DOUBLE || t2 == TypeKind.DOUBLE) {
      return getDouble();
    } else if (t1 == TypeKind.FLOAT || t2 == TypeKind.FLOAT) {
      return getFloat();
    } else if (t1 == TypeKind.LONG || t2 == TypeKind.LONG) {
      return getLong();
    } else {
      return getInt();
    }
  }

  /**
   * TODO(manvithn): See jls-5.6.2 and jls-15.25.
   * @param trueType the type of the true expression
   * @param falseType the type of the false expression
   * @return the inferred type of the conditional expression
   */
  public TypeMirror inferConditionalExpressionType(TypeMirror trueType, TypeMirror falseType) {
    return isAssignable(trueType, falseType) ? falseType : trueType;
  }

  public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
    if (isGeneratedType(t)) {
      if (t instanceof GeneratedTypeElement.Mirror) {
        GeneratedTypeElement element = (GeneratedTypeElement)
            ((GeneratedTypeElement.Mirror) t).asElement();
        return element.getDirectSupertypes();
      } else {
        return Collections.emptyList();
      }
    }
    if (isArray(t)) {
      // javac's directSupertypes() for String[] is Object[], whereas
      // JDT's returns an empty list. Currently typed arrays aren't necessary,
      // so prefer the JDT behavior here.
      return Collections.emptyList();
    }
    return javacTypes.directSupertypes(t);
  }

  public TypeMirror erasure(TypeMirror t) {
    return javacTypes.erasure(t);
  }

  public ArrayType getArrayType(TypeMirror componentType) {
    if (isGeneratedType(componentType)) {
      return new GeneratedArrayType(componentType);
    }
    return javacTypes.getArrayType(componentType);
  }

  public ArrayType getArrayType(TypeMirror componentType, int dims) {
    if (dims < 1) {
      throw new IllegalArgumentException("dims must be greater than or equal to 1");
    }
    if (dims == 1) {
      return getArrayType(componentType);
    } else {
      return getArrayType(getArrayType(componentType), dims - 1);
    }
  }

  boolean isGeneratedType(TypeMirror type) {
    return type instanceof AbstractTypeMirror;
  }

  public TypeElement getIosArray(TypeMirror componentType) {
    return componentType.getKind().isPrimitive()
        ? PRIMITIVE_IOS_ARRAYS.get(componentType.getKind()) : IOS_OBJECT_ARRAY;
  }

  public TypeElement getJavaObject() {
    return javaObject;
  }

  public TypeElement getJavaString() {
    return javaString;
  }

  public TypeElement getJavaClass() {
    return javaClass;
  }

  public TypeElement getJavaNumber() {
    return javaNumber;
  }

  public TypeElement getJavaThrowable() {
    return javaThrowable;
  }

  public boolean isString(TypeElement e) {
    return javaString.equals(e) || NS_STRING.equals(e);
  }

  public boolean isString(TypeMirror t) {
    return isString(asTypeElement(t));
  }

  public boolean isClassType(TypeElement e) {
    return javaClass.equals(e) || IOS_CLASS.equals(e);
  }

  public boolean isClassType(TypeMirror t) {
    return isClassType(asTypeElement(t));
  }

  /**
   * Maps the given type to it's Objective-C equivalent. Array types are mapped to their equivalent
   * IOSArray type and common Java classes like String and Object are mapped to NSString and
   * NSObject.
   */
  public TypeElement getObjcClass(TypeMirror t) {
    if (isArray(t)) {
      return getIosArray(((ArrayType) t).getComponentType());
    } else if (isDeclaredType(t)) {
      return getObjcClass((TypeElement) ((DeclaredType) t).asElement());
    } else if (t.getKind() == TypeKind.UNION) {
      TypeMirror lub = leastUpperBound(((UnionType)t).getAlternatives());
      return getObjcClass(asTypeElement(lub));
    }
    return null;
  }

  private TypeMirror leastUpperBound(List<? extends TypeMirror> types) {
    List<TypeMirror> superTypes = new ArrayList<>();
    superTypes.add(types.get(0));
    while (!superTypes.isEmpty()) {
      TypeMirror lub = superTypes.remove(0);
      if (types.stream().allMatch(t -> isAssignable(t, lub))) {
        // At some point we'll get here because Object is the root of the class hierarchy.
        return lub;
      }
      superTypes.addAll(directSupertypes(lub));
    }
    throw new AssertionError("Unreachable path.");
  }

  public TypeElement getObjcClass(TypeElement element) {
    TypeElement mapped = javaToObjcTypeMap.get(element);
    return mapped != null ? mapped : element;
  }

  public boolean isMappedClass(TypeElement element) {
    return javaToObjcTypeMap.containsKey(element);
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
      case OTHER:
        return isId(t);
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

  public static boolean isId(TypeMirror t) {
    return t instanceof NativeType && ((NativeType) t).getName().equals("id");
  }

  public PrimitiveType getPrimitiveType(TypeKind kind) {
    return javacTypes.getPrimitiveType(kind);
  }

  public PrimitiveType getBoolean() {
    return getPrimitiveType(TypeKind.BOOLEAN);
  }

  public PrimitiveType getByte() {
    return getPrimitiveType(TypeKind.BYTE);
  }

  public PrimitiveType getChar() {
    return getPrimitiveType(TypeKind.CHAR);
  }

  public PrimitiveType getDouble() {
    return getPrimitiveType(TypeKind.DOUBLE);
  }

  public PrimitiveType getFloat() {
    return getPrimitiveType(TypeKind.FLOAT);
  }

  public PrimitiveType getInt() {
    return getPrimitiveType(TypeKind.INT);
  }

  public PrimitiveType getLong() {
    return getPrimitiveType(TypeKind.LONG);
  }

  public PrimitiveType getShort() {
    return getPrimitiveType(TypeKind.SHORT);
  }

  public NoType getVoid() {
    return javacTypes.getNoType(TypeKind.VOID);
  }

  public NullType getNull() {
    return javacTypes.getNullType();
  }

  public TypeMirror resolvePrimitiveType(String signature) {
    switch (signature) {
      case "B": return getByte();
      case "C": return getChar();
      case "D": return getDouble();
      case "F": return getFloat();
      case "I": return getInt();
      case "J": return getLong();
      case "S": return getShort();
      case "V": return getVoid();
      case "Z": return getBoolean();
      default:
        return null;
    }
  }

  public PrimitiveType unboxedType(TypeMirror t) {
    if (isGeneratedType(t)) {
      return null;
    }
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

  public boolean isDeclaredAsId(TypeMirror t) {
    return isReferenceType(t) && getObjcUpperBounds(t).isEmpty();
  }

  @SuppressWarnings("TypeEquals")
  public boolean isObjcAssignable(TypeMirror t1, TypeMirror t2) {
    if (!isReferenceType(t1) || !isReferenceType(t2)) {
      if (t1 instanceof PointerType && t2 instanceof PointerType) {
        return isObjcAssignable(((PointerType) t1).getPointeeType(),
                                ((PointerType) t2).getPointeeType());
      }
      return t1.equals(t2);
    }
    outer: for (TypeElement t2Class : getObjcUpperBounds(t2)) {
      for (TypeElement t1Class : getObjcUpperBounds(t1)) {
        if (isObjcSubtype(t1Class, t2Class)) {
          continue outer;
        }
      }
      return false;
    }
    return true;
  }

  public boolean isObjcSubtype(TypeElement type, TypeElement targetSupertype) {
    if (type == null) {
      return false;
    }
    if (type.equals(targetSupertype)) {
      return true;
    }
    TypeMirror superclass = type.getSuperclass();
    if (superclass != null && isObjcSubtype(getObjcClass(superclass), targetSupertype)) {
      return true;
    }
    for (TypeMirror intrface : type.getInterfaces()) {
      if (isObjcSubtype(getObjcClass(intrface), targetSupertype)) {
        return true;
      }
    }
    return false;
  }

  public List<TypeElement> getObjcUpperBounds(TypeMirror t) {
    List<TypeElement> result = new ArrayList<>();
    for (TypeMirror bound : getUpperBounds(t)) {
      TypeElement elem = getObjcClass(bound);
      // NSObject is emitted as "id".
      if (elem != null && !elem.equals(NS_OBJECT)) {
        result.add(elem);
      }
    }
    return result;
  }

  public List<? extends TypeMirror> getUpperBounds(TypeMirror t) {
    if (t == null) {
      return Collections.singletonList(
          javacElements.getTypeElement("java.lang.Object").asType());
    }
    switch (t.getKind()) {
      case INTERSECTION:
        return ((IntersectionType) t).getBounds();
      case TYPEVAR:
        return getUpperBounds(((TypeVariable) t).getUpperBound());
      case WILDCARD:
        return getUpperBounds(((WildcardType) t).getExtendsBound());
      default:
        return Collections.singletonList(t);
    }
  }

  public static String getName(TypeMirror t) {
    switch (t.getKind()) {
      case ARRAY:
        return getName(((ArrayType) t).getComponentType()) + "[]";
      case DECLARED:
        return ElementUtil.getName(asTypeElement(t));
      case TYPEVAR:
        return ElementUtil.getName(((TypeVariable) t).asElement());
      case UNION:
        return t.toString();
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
      case TYPEVAR:
      case UNION:
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
      case UNION:
        return "L" + t.toString().replace('.', '/') + ";";
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case DOUBLE:
      case FLOAT:
      case INT:
      case LONG:
      case SHORT:
      case VOID:
        return getBinaryName(t);
      default:
        throw new AssertionError("Cannot resolve signature name for type: " + t);
    }
  }

  /**
   * Returns the binary name for a primitive or void type.
   */
  public static String getBinaryName(TypeMirror t) {
    switch (t.getKind()) {
      case BOOLEAN: return "Z";
      case BYTE: return "B";
      case CHAR: return "C";
      case DOUBLE: return "D";
      case FLOAT: return "F";
      case INT: return "I";
      case LONG: return "J";
      case SHORT: return "S";
      case VOID: return "V";
      default:
        throw new AssertionError("Cannot resolve binary name for type: " + t);
    }
  }

  /**
   * Returns the descriptor of a constructor or method (JLS 4.3.3).
   */
  public String getMethodDescriptor(ExecutableType method) {
    StringBuilder sb = new StringBuilder("(");
    for (TypeMirror t : method.getParameterTypes()) {
      sb.append(getSignatureName(t));
    }
    sb.append(")");
    sb.append(getSignatureName(method.getReturnType()));
    return sb.toString();
  }

  /**
   * Returns the descriptor for a field (JLS 4.3.2).
   */
  public String getFieldDescriptor(TypeMirror type) {
    return getSignatureName(type);
  }

  /**
   * Get the "Reference" name of a method.
   * For non-constructors this is the method's name.
   * For constructors of top-level classes, this is the name of the class.
   * For constructors of inner classes, this is the $-delimited name path
   * from the outermost class declaration to the inner class declaration.
   */
  public String getReferenceName(ExecutableElement element) {
    if (!ElementUtil.isConstructor(element)) {
      return ElementUtil.getName(element);
    }
    TypeElement parent = ElementUtil.getDeclaringClass(element);
    assert parent != null;
    List<String> components = new LinkedList<>(); // LinkedList is faster for prepending.
    do {
      components.add(0, ElementUtil.getName(parent));
      parent = ElementUtil.getDeclaringClass(parent);
    } while (parent != null);
    return INNER_CLASS_JOINER.join(components);
  }


  /**
   * Returns a format specifier that is supported by the NSString, CFString and NSLog formatting
   * methods.
   */
  public static String getObjcFormatSpecifier(TypeMirror t) {
    switch (t.getKind()) {
      case BOOLEAN:
      case BYTE:
      case INT: return "d";
      case CHAR: return "c";
      case DOUBLE: return "lf";
      case FLOAT: return "f";
      case LONG: return "lld";
      case SHORT: return "hd";
      default: return "@";
    }
  }

  /**
   * Get the "Reference" signature of a method.
   */
  public String getReferenceSignature(ExecutableElement element) {
    StringBuilder sb = new StringBuilder("(");

    // If the method is an inner class constructor, prepend the outer class type.
    if (ElementUtil.isConstructor(element)) {
      TypeElement declaringClass = ElementUtil.getDeclaringClass(element);
      if (ElementUtil.hasOuterContext(declaringClass)) {
        TypeElement outerClass = ElementUtil.getDeclaringClass(declaringClass);
        sb.append(getSignatureName(outerClass.asType()));
      }
    }

    for (VariableElement param : element.getParameters()) {
      sb.append(getSignatureName(param.asType()));
    }

    sb.append(')');
    TypeMirror returnType = element.getReturnType();
    if (returnType != null) {
      sb.append(getSignatureName(returnType));
    }
    return sb.toString();
  }

  private static TypeElement newPrimitiveIosArray(String name) {
    return GeneratedTypeElement.newIosClass(name, NS_OBJECT, "IOSPrimitiveArray.h");
  }

  public static boolean isStubType(String typeName) {
    // Currently only NSException and NSFastEnumeration have com.google.j2objc stubs.
    return typeName.startsWith("com.google.j2objc.NS");
  }

  // Remove javac 1.8 AnnotatedType wrapper, if present.
  public static TypeMirror unannotatedType(TypeMirror type) {
    if (annotatedTypeClass != null) {
      try {
        if (annotatedTypeClass.isInstance(type)) {
          Method m = annotatedTypeClass.getDeclaredMethod("unannotatedType");
          return (TypeMirror) m.invoke(type);
        }
      } catch (Exception e) {
        // Fall-through, as any error shows no unannotated type is available.
      }
    }
    return type;
  }

  private static Class<?> annotatedTypeClass;
  static {
    try {
      annotatedTypeClass = Class.forName("com.sun.tools.javac.code.Type$AnnotatedType");
    } catch (ClassNotFoundException e) {
      annotatedTypeClass = null;
    }
  }
}
