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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.GeneratedElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.LambdaTypeElement;
import com.google.j2objc.annotations.Property;
import com.google.j2objc.annotations.RetainedWith;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

/**
 * Utility methods for working with elements.
 */
public final class ElementUtil {

  // Flags defined in JVM spec, table 4.1. These constants are also defined in
  // java.lang.reflect.Modifier, but aren't public.
  public static final int ACC_BRIDGE = 0x40;
  public static final int ACC_VARARGS = 0x80;
  public static final int ACC_SYNTHETIC = 0x1000;
  public static final int ACC_ANNOTATION = 0x2000;
  public static final int ACC_ENUM = 0x4000;

  // Not defined in JVM spec, but used by reflection support.
  public static final int ACC_ANONYMOUS = 0x8000;

  // Class files can only use the lower 16 bits.
  public static final int ACC_FLAG_MASK = 0xFFFF;

  private static final Set<Modifier> VISIBILITY_MODIFIERS = EnumSet.of(
      Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE);

  private static final String LAZY_INIT = "com.google.errorprone.annotations.concurrent.LazyInit";

  private static final Pattern NULLABLE_PATTERN = Pattern.compile("Nullable");
  private static final Pattern NONNULL_PATTERN = Pattern.compile("No[nt][Nn]ull");

  private final Elements javacElements;
  private final Map<Element, TypeMirror> elementTypeMap = new HashMap<>();

  public ElementUtil(Elements javacElements) {
    this.javacElements = javacElements;
  }

  public static String getName(Element element) {
    // Always return qualified package names.
    Name name = element.getKind() == ElementKind.PACKAGE
        ? ((PackageElement) element).getQualifiedName() : element.getSimpleName();
    return name.toString();
  }

  public static String getQualifiedName(TypeElement element) {
    return element.getQualifiedName().toString();
  }

  public static boolean isNamed(Element element, String name) {
    return element.getSimpleName().contentEquals(name);
  }

  public static boolean isStatic(Element element) {
    return hasModifier(element, Modifier.STATIC);
  }

  public static boolean isDefault(Element element) {
    // Indirectly check whether Modifier.DEFAULT exists, since it was
    // added in Java 8.
    try {
      Modifier m = Modifier.valueOf("DEFAULT");
      return hasModifier(element, m);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isFinal(Element element) {
    return hasModifier(element, Modifier.FINAL);
  }

  public static boolean isPublic(Element element) {
    return hasModifier(element, Modifier.PUBLIC);
  }

  public static boolean isPrivate(Element element) {
    return hasModifier(element, Modifier.PRIVATE);
  }

  public static boolean isVolatile(VariableElement element) {
    return hasModifier(element, Modifier.VOLATILE)
        // Upgrade reference type fields marked with error prone's LazyInit because this indicates
        // an intentional racy init.
        || (!element.asType().getKind().isPrimitive()
            && hasQualifiedNamedAnnotation(element, LAZY_INIT));
  }

  public static boolean isTopLevel(TypeElement type) {
    return type.getNestingKind() == NestingKind.TOP_LEVEL;
  }

  public static boolean isAnonymous(TypeElement type) {
    return type.getNestingKind() == NestingKind.ANONYMOUS;
  }

  public static boolean isLocal(TypeElement type) {
    NestingKind nestingKind = type.getNestingKind();
    return nestingKind == NestingKind.ANONYMOUS || nestingKind == NestingKind.LOCAL;
  }

  public static boolean isLambda(TypeElement type) {
    return type instanceof LambdaTypeElement;
  }

  public static boolean isInterface(Element type) {
    return type.getKind() == ElementKind.INTERFACE;
  }

  public static boolean isAnnotationType(Element type) {
    return type.getKind() == ElementKind.ANNOTATION_TYPE;
  }

  public static boolean isEnum(Element e) {
    return e.getKind() == ElementKind.ENUM;
  }

  public static boolean isEnumConstant(Element e) {
    return e.getKind() == ElementKind.ENUM_CONSTANT;
  }

  public static boolean isPackage(Element e) {
    return e.getKind() == ElementKind.PACKAGE;
  }

  public static boolean isTypeElement(Element e) {
    ElementKind kind = e.getKind();
    return kind.isClass() || kind.isInterface();
  }

  public static boolean isExecutableElement(Element e) {
    ElementKind kind = e.getKind();
    return kind == ElementKind.CONSTRUCTOR || kind == ElementKind.METHOD;
  }

  public static boolean isTypeParameterElement(Element e) {
    return e.getKind() == ElementKind.TYPE_PARAMETER;
  }

  public static boolean isAnnotationMember(ExecutableElement e) {
    return isAnnotationType(getDeclaringClass(e));
  }

  //TODO(malvania): For elements inside static blocks, this method returns a "TypeElement" of a
  //  static block, which does not work with getBinaryName(TypeElement) (one proven example)
  public static TypeElement getDeclaringClass(Element element) {
    do {
      element = element.getEnclosingElement();
    } while (element != null && !isTypeElement(element));
    return (TypeElement) element;
  }

  public static TypeElement getSuperclass(TypeElement element) {
    return TypeUtil.asTypeElement(element.getSuperclass());
  }

  public static List<TypeElement> getInterfaces(TypeElement element) {
    return Lists.newArrayList(Iterables.transform(
        element.getInterfaces(), i -> TypeUtil.asTypeElement(i)));
  }

  public static boolean isPrimitiveConstant(VariableElement element) {
    return isFinal(element) && element.asType().getKind().isPrimitive()
        && element.getConstantValue() != null
        // Exclude local variables declared final.
        && element.getKind().isField();
  }

  public static boolean isConstant(VariableElement element) {
    Object constantValue = element.getConstantValue();
    return constantValue != null
        && (element.asType().getKind().isPrimitive()
            || (constantValue instanceof String
                && UnicodeUtils.hasValidCppCharacters((String) constantValue)));
  }

  public static boolean isStringConstant(VariableElement element) {
    Object constantValue = element.getConstantValue();
    return constantValue != null && constantValue instanceof String
        && UnicodeUtils.hasValidCppCharacters((String) constantValue);
  }

  /**
   * Returns whether this variable will be declared in global scope in ObjC.
   */
  public static boolean isGlobalVar(VariableElement element) {
    return isStatic(element) || isPrimitiveConstant(element);
  }

  /**
   * Returns whether this variable will be an ObjC instance variable.
   */
  public static boolean isInstanceVar(VariableElement element) {
    return element.getKind() == ElementKind.FIELD && !isGlobalVar(element);
  }

  public static boolean isNonnull(VariableElement element) {
    return element instanceof GeneratedVariableElement
        && ((GeneratedVariableElement) element).isNonnull();
  }

  public static String getTypeQualifiers(VariableElement element) {
    return element instanceof GeneratedVariableElement
        ? ((GeneratedVariableElement) element).getTypeQualifiers() : null;
  }

  public static boolean isAbstract(Element element) {
    return hasModifier(element, Modifier.ABSTRACT);
  }

  public static boolean isNative(Element element) {
    return hasModifier(element, Modifier.NATIVE);
  }

  public static boolean isSynchronized(Element element) {
    return hasModifier(element, Modifier.SYNCHRONIZED);
  }

  public static boolean isSynthetic(int modifiers) {
    return (modifiers & ACC_SYNTHETIC) != 0;
  }

  public static boolean isSynthetic(Element e) {
    if (e instanceof GeneratedElement) {
      return ((GeneratedElement) e).isSynthetic();
    }
    if (e instanceof Symbol) {
      return (((Symbol) e).flags() & Flags.SYNTHETIC) > 0;
    }
    return false;
  }

  public static String getHeader(TypeElement e) {
    return e instanceof GeneratedTypeElement ? ((GeneratedTypeElement) e).getHeader() : null;
  }

  public static boolean isIosType(TypeElement e) {
    return e instanceof GeneratedTypeElement && ((GeneratedTypeElement) e).isIosType();
  }

  public static String getSelector(ExecutableElement e) {
    if (e instanceof GeneratedExecutableElement) {
      return ((GeneratedExecutableElement) e).getSelector();
    }
    return null;
  }

  public static boolean isPackageInfo(TypeElement type) {
    return type.getSimpleName().toString().equals(NameTable.PACKAGE_INFO_CLASS_NAME);
  }

  /**
   * Tests if this type element is private to it's source file. A public type declared
   * within a private type is considered private.
   */
  public static boolean isPrivateInnerType(TypeElement type) {
    switch (type.getNestingKind()) {
      case ANONYMOUS:
      case LOCAL:
        return true;
      case MEMBER:
        return isPrivate(type) || isPrivateInnerType((TypeElement) type.getEnclosingElement());
      case TOP_LEVEL:
        return isPrivate(type);
    }
    throw new AssertionError("Unknown NestingKind");
  }

  /**
   * Determines if a type element can access fields and methods from an outer class.
   */
  public static boolean hasOuterContext(TypeElement type) {
    switch (type.getNestingKind()) {
      case ANONYMOUS:
      case LOCAL:
        return !isStatic(type.getEnclosingElement());
      case MEMBER:
        return !isStatic(type);
      case TOP_LEVEL:
        return false;
    }
    throw new AssertionError("Unknown NestingKind");
  }

  private static boolean hasModifier(Element element, Modifier modifier) {
    return element.getModifiers().contains(modifier);
  }

  public static boolean isVariable(Element element) {
    ElementKind kind = element.getKind();
    return kind == ElementKind.FIELD || kind == ElementKind.LOCAL_VARIABLE
        || kind == ElementKind.PARAMETER || kind == ElementKind.EXCEPTION_PARAMETER
        || kind == ElementKind.RESOURCE_VARIABLE || kind == ElementKind.ENUM_CONSTANT;
  }

  public static boolean isField(Element element) {
    return element.getKind() == ElementKind.FIELD;
  }

  public static boolean isParameter(Element element) {
    return element.getKind() == ElementKind.PARAMETER;
  }

  public static boolean isLocalVariable(Element element) {
    return element.getKind() == ElementKind.LOCAL_VARIABLE;
  }

  public static boolean isMethod(Element element) {
    return element.getKind() == ElementKind.METHOD;
  }

  public static boolean isConstructor(Element element) {
    return element.getKind() == ElementKind.CONSTRUCTOR;
  }

  public static boolean isInstanceMethod(Element element) {
    return isMethod(element) && !isStatic(element);
  }

  public static boolean isWeakReference(VariableElement var) {
    return hasNamedAnnotation(var, "Weak")
        || hasWeakPropertyAttribute(var)
        || (var instanceof GeneratedVariableElement && ((GeneratedVariableElement) var).isWeak());
  }

  public boolean isWeakOuterType(TypeElement type) {
    if (type instanceof LambdaTypeElement) {
      return ((LambdaTypeElement) type).isWeakOuter();
    } else if (isAnonymous(type)) {
      // TODO(kstanger): remove this block when javac conversion is complete.
      // For anonymous classes we must check for a TYPE_USE annotation on the supertype used in the
      // declaration. For example:
      // Runnable r = new @WeakOuter Runnable() { ... };
      TypeMirror superclass = type.getSuperclass();
      if (superclass != null && hasNamedAnnotation(superclass, "WeakOuter")) {
        return true;
      }
      for (TypeMirror intrface : type.getInterfaces()) {
        if (hasNamedAnnotation(intrface, "WeakOuter")) {
          return true;
        }
      }
      if (elementTypeMap.containsKey(type)) {
        return hasNamedAnnotation(elementTypeMap.get(type), "WeakOuter");
      }
      return hasNamedAnnotation(type.asType(), "WeakOuter");
    } else {
      return hasNamedAnnotation(type, "WeakOuter");
    }
  }

  private static boolean hasWeakPropertyAttribute(VariableElement var) {
    AnnotationMirror annotation = getAnnotation(var, Property.class);
    return annotation != null && parsePropertyAttribute(annotation).contains("weak");
  }

  /**
   * Returns the attributes of a Property annotation.
   */
  public static Set<String> parsePropertyAttribute(AnnotationMirror annotation) {
    assert getName(annotation.getAnnotationType().asElement()).equals("Property");
    String attributesStr = (String) getAnnotationValue(annotation, "value");
    Set<String> attributes = new HashSet<>();
    if (attributesStr != null) {
      attributes.addAll(Arrays.asList(attributesStr.split(",\\s*")));
      attributes.remove(""); // Clear any empty strings.
    }
    return attributes;
  }

  public static boolean isRetainedWithField(VariableElement varElement) {
    return hasAnnotation(varElement, RetainedWith.class);
  }

  public static <T extends Element> Iterable<T> filterEnclosedElements(
      Element elem, Class<T> resultClass, ElementKind... kinds) {
    List<ElementKind> kindsList = Arrays.asList(kinds);
    return Iterables.transform(Iterables.filter(
        elem.getEnclosedElements(), e -> kindsList.contains(e.getKind())), resultClass::cast);
  }

  public static Iterable<ExecutableElement> getMethods(TypeElement e) {
    return filterEnclosedElements(e, ExecutableElement.class, ElementKind.METHOD);
  }

  public static Iterable<ExecutableElement> getConstructors(TypeElement e) {
    return filterEnclosedElements(e, ExecutableElement.class, ElementKind.CONSTRUCTOR);
  }

  public static List<ExecutableElement> getExecutables(TypeElement e) {
    return Lists.newArrayList(filterEnclosedElements(
        e, ExecutableElement.class, ElementKind.CONSTRUCTOR, ElementKind.METHOD));
  }

  public static List<VariableElement> getDeclaredFields(Element e) {
    return Lists.newArrayList(filterEnclosedElements(e, VariableElement.class, ElementKind.FIELD));
  }

  public static Iterable<TypeElement> getDeclaredTypes(TypeElement e) {
    return filterEnclosedElements(
        e, TypeElement.class, ElementKind.ANNOTATION_TYPE, ElementKind.ENUM, ElementKind.CLASS,
        ElementKind.INTERFACE);
  }

  private static boolean paramsMatch(ExecutableElement method, String[] paramTypes) {
    List<? extends VariableElement> params = method.getParameters();
    int size = params.size();
    if (size != paramTypes.length) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (!TypeUtil.getQualifiedName(params.get(i).asType()).equals(paramTypes[i])) {
        return false;
      }
    }
    return true;
  }

  public static ExecutableElement findMethod(TypeElement type, String name, String... paramTypes) {
    return Iterables.getFirst(Iterables.filter(
        filterEnclosedElements(type, ExecutableElement.class, ElementKind.METHOD),
        method -> getName(method).equals(name) && paramsMatch(method, paramTypes)), null);
  }

  /** Locate method which matches either Java or Objective C getter name patterns. */
  public static ExecutableElement findGetterMethod(
      String propertyName, TypeMirror propertyType, TypeElement declaringClass) {
    // Try Objective-C getter naming convention.
    ExecutableElement getter = ElementUtil.findMethod(declaringClass, propertyName);
    if (getter == null) {
      // Try Java getter naming conventions.
      String prefix = TypeUtil.isBoolean(propertyType) ? "is" : "get";
      getter = ElementUtil.findMethod(declaringClass, prefix + NameTable.capitalize(propertyName));
    }
    return getter;
  }

  /** Locate method which matches the Java/Objective C setter name pattern. */
  public static ExecutableElement findSetterMethod(
      String propertyName, TypeMirror type, TypeElement declaringClass) {
    return ElementUtil.findMethod(
        declaringClass,
        "set" + NameTable.capitalize(propertyName),
        TypeUtil.getQualifiedName(type));
  }

  public static ExecutableElement findConstructor(TypeElement type, String... paramTypes) {
    return Iterables.getFirst(Iterables.filter(
        getConstructors(type),
        method -> paramsMatch(method, paramTypes)), null);
  }

  public static VariableElement findField(TypeElement type, String name) {
    return Iterables.getFirst(Iterables.filter(
        filterEnclosedElements(type, VariableElement.class, ElementKind.FIELD),
        field -> getName(field).equals(name)), null);
  }

  public static Iterable<TypeMirror> asTypes(Iterable<? extends Element> elements) {
    return Iterables.transform(elements, elem -> elem.asType());
  }

  public boolean overrides(
      ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
    return javacElements.overrides(overrider, overridden, type);
  }

  public static PackageElement getPackage(Element e) {
    while (e != null && !isPackage(e)) {
      e = e.getEnclosingElement();
    }
    return (PackageElement) e;
  }

  public String getBinaryName(TypeElement e) {
    if (e instanceof GeneratedTypeElement) {
      TypeElement declaringClass = getDeclaringClass(e);
      if (declaringClass != null) {
        return getBinaryName(declaringClass) + '$' + getName(e);
      } else {
        return getQualifiedName(e);
      }
    }
    return javacElements.getBinaryName(e).toString();
  }

  Map<? extends ExecutableElement, ? extends AnnotationValue>
      getElementValuesWithDefaults(AnnotationMirror a) {
    return javacElements.getElementValuesWithDefaults(a);
  }

  public static Set<Modifier> getVisibilityModifiers(Element e) {
    return Sets.intersection(e.getModifiers(), VISIBILITY_MODIFIERS);
  }

  // This conversion is lossy because there is no bit for "default" the JVM spec.
  public static int fromModifierSet(Set<Modifier> set) {
    int modifiers = 0;
    if (set.contains(Modifier.PUBLIC)) {
      modifiers |= java.lang.reflect.Modifier.PUBLIC;
    }
    if (set.contains(Modifier.PRIVATE)) {
      modifiers |= java.lang.reflect.Modifier.PRIVATE;
    }
    if (set.contains(Modifier.PROTECTED)) {
      modifiers |= java.lang.reflect.Modifier.PROTECTED;
    }
    if (set.contains(Modifier.STATIC)) {
      modifiers |= java.lang.reflect.Modifier.STATIC;
    }
    if (set.contains(Modifier.FINAL)) {
      modifiers |= java.lang.reflect.Modifier.FINAL;
    }
    if (set.contains(Modifier.SYNCHRONIZED)) {
      modifiers |= java.lang.reflect.Modifier.SYNCHRONIZED;
    }
    if (set.contains(Modifier.VOLATILE)) {
      modifiers |= java.lang.reflect.Modifier.VOLATILE;
    }
    if (set.contains(Modifier.TRANSIENT)) {
      modifiers |= java.lang.reflect.Modifier.TRANSIENT;
    }
    if (set.contains(Modifier.NATIVE)) {
      modifiers |= java.lang.reflect.Modifier.NATIVE;
    }
    if (set.contains(Modifier.ABSTRACT)) {
      modifiers |= java.lang.reflect.Modifier.ABSTRACT;
    }
    if (set.contains(Modifier.STRICTFP)) {
      modifiers |= java.lang.reflect.Modifier.STRICT;
    }
    return modifiers;
  }

  public static boolean isRuntimeAnnotation(AnnotationMirror mirror) {
    return isRuntimeAnnotation(mirror.getAnnotationType().asElement());
  }

  public static boolean isRuntimeAnnotation(Element e) {
    if (e.getKind() != ElementKind.ANNOTATION_TYPE) {
      return false;
    }
    for (AnnotationMirror ann : e.getAnnotationMirrors()) {
      String annotationName = ann.getAnnotationType().asElement().getSimpleName().toString();
      if (annotationName.equals("Retention")) {
        for (AnnotationValue value : ann.getElementValues().values()) {
          // Retention's value is a RetentionPolicy enum constant.
          VariableElement v = (VariableElement) value.getValue();
          return v.getSimpleName().toString().equals("RUNTIME");
        }
      }
    }
    return false;
  }

  public static AnnotationMirror getAnnotation(Element element, Class<?> annotationClass) {
    return getQualifiedNamedAnnotation(element, annotationClass.getName());
  }

  public static boolean hasAnnotation(Element element, Class<?> annotationClass) {
    return getAnnotation(element, annotationClass) != null;
  }

  /**
   * Less strict version of the above where we don't care about the annotation's package.
   */
  public static boolean hasNamedAnnotation(AnnotatedConstruct ac, String name) {
    for (AnnotationMirror annotation : ac.getAnnotationMirrors()) {
      if (getName(annotation.getAnnotationType().asElement()).equals(name)) {
        return true;
      }
    }
    return false;
  }

  /** Similar to the above but matches against a pattern. */
  public static boolean hasNamedAnnotation(AnnotatedConstruct ac, Pattern pattern) {
    for (AnnotationMirror annotation : ac.getAnnotationMirrors()) {
      if (pattern.matcher(getName(annotation.getAnnotationType().asElement())).matches()) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasQualifiedNamedAnnotation(Element element, String name) {
    return getQualifiedNamedAnnotation(element, name) != null;
  }

  public static AnnotationMirror getQualifiedNamedAnnotation(Element element, String name) {
    for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
      if (getQualifiedName((TypeElement) annotation.getAnnotationType().asElement()).equals(name)) {
        return annotation;
      }
    }
    return null;
  }

  /**
   * Return true if a binding has a named "Nullable" annotation. Package names aren't
   * checked because different nullable annotations are defined by several different
   * Java frameworks.
   */
  public static boolean hasNullableAnnotation(Element element) {
    return hasNullabilityAnnotation(element, NULLABLE_PATTERN);
  }

  /**
   * Return true if a binding has a named "Nonnull" annotation. Package names aren't
   * checked because different nonnull annotations are defined in several Java
   * frameworks, with varying but similar names.
   */
  public static boolean hasNonnullAnnotation(Element element) {
    return hasNullabilityAnnotation(element, NONNULL_PATTERN);
  }

  private static boolean hasNullabilityAnnotation(Element element, Pattern pattern) {
    // The two if statements cover type annotations.
    if (isMethod(element)
        && hasNamedAnnotation(((ExecutableElement) element).getReturnType(), pattern)) {
      return true;
    }
    if (isVariable(element) && hasNamedAnnotation(element.asType(), pattern)) {
      return true;
    }
    // This covers declaration annotations.
    return hasNamedAnnotation(element, pattern);
  }

  public static Object getAnnotationValue(AnnotationMirror annotation, String name) {
    for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
        : annotation.getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals(name)) {
        return entry.getValue().getValue();
      }
    }
    return null;
  }

  /**
   * Returns an alphabetically sorted list of an annotation type's members.
   * This is necessary since an annotation's values can be specified in any
   * order, but the annotation's constructor needs to be invoked using its
   * declaration order.
   */
  public static List<ExecutableElement> getSortedAnnotationMembers(TypeElement annotation) {
    List<ExecutableElement> members = Lists.newArrayList(getMethods(annotation));
    Collections.sort(members, (m1, m2) -> getName(m1).compareTo(getName(m2)));
    return members;
  }

  public boolean areParametersNonnullByDefault(TypeElement typeElement, Options options) {
    if (ElementUtil.hasAnnotation(typeElement, ParametersAreNonnullByDefault.class)) {
      return true;
    }
    PackageElement pkg = getPackage(typeElement);
    String pkgName = pkg.getQualifiedName().toString();
    return options.getPackageInfoLookup().hasParametersAreNonnullByDefault(pkgName);
  }

  /**
   * Returns true if there's a SuppressedWarning annotation with the specified warning.
   * The SuppressWarnings annotation can be inherited from the owning method or class,
   * but does not have package scope.
   */
  @SuppressWarnings("unchecked")
  public static boolean suppressesWarning(String warning, Element element) {
    if (element == null || isPackage(element)) {
      return false;
    }
    AnnotationMirror annotation = getAnnotation(element, SuppressWarnings.class);
    if (annotation != null) {
      for (AnnotationValue elem
           : (List<? extends AnnotationValue>) getAnnotationValue(annotation, "value")) {
        if (warning.equals(elem.getValue())) {
          return true;
        }
      }
    }
    return suppressesWarning(warning, element.getEnclosingElement());
  }

  /**
   * Maps an Element to a TypeMirror. element.asType() is the preferred mapping,
   * but sometimes type information is lost. For example, an anonymous class with
   * a type use annotation has the annotation in the node's type, but not in the
   * node's element.asType().
   */
  public void mapElementType(Element element, TypeMirror type) {
    elementTypeMap.put(element, type);
  }

  /**
   * Returns the associated type mirror for an element.
   */
  public TypeMirror getType(Element element) {
    return elementTypeMap.containsKey(element) ? elementTypeMap.get(element) : element.asType();
  }

  /**
   * Returns whether an element is marked as always being non-null. Field, method,
   * and parameter elements can be defined as non-null with a Nonnull annotation.
   * Method parameters can also be defined as non-null by annotating the owning
   * package or type element with the ParametersNonnullByDefault annotation.
   */
  public static boolean isNonnull(Element element, boolean parametersNonnullByDefault) {
    return hasNonnullAnnotation(element)
        || isConstructor(element)  // Java constructors are always non-null.
        || (isParameter(element)
            && parametersNonnullByDefault
            && !((VariableElement) element).asType().getKind().isPrimitive());
  }

  /**
   * Returns the source file name for a type element. Returns null if the element
   * isn't a javac ClassSymbol, or if it is defined by a classfile which was compiled
   * without a source attribute.
   */
  public static String getSourceFile(TypeElement type) {
    if (type instanceof ClassSymbol) {
      JavaFileObject srcFile = ((ClassSymbol) type).sourcefile;
      if (srcFile != null) {
        return srcFile.getName();
      }
    }
    return null;
  }
}
