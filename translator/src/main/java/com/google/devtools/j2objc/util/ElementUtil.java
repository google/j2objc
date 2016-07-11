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
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.LambdaTypeElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IVariableBinding;

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

  private static final Map<Integer, Set<Modifier>> modifierSets = new HashMap<>();

  public static String getName(Element element) {
    return element.getSimpleName().toString();
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
    return hasModifier(element, Modifier.VOLATILE);
  }

  public static boolean isLambda(TypeElement type) {
    return type instanceof LambdaTypeElement;
  }

  public static boolean isInterface(Element type) {
    return type.getKind() == ElementKind.INTERFACE;
  }

  public static boolean isTypeElement(Element e) {
    ElementKind kind = e.getKind();
    return kind.isClass() || kind.isInterface();
  }

  public static boolean isExecutableElement(Element e) {
    ElementKind kind = e.getKind();
    return kind == ElementKind.CONSTRUCTOR || kind == ElementKind.METHOD;
  }

  public static TypeElement getDeclaringClass(Element element) {
    do {
      element = element.getEnclosingElement();
    } while (element != null && !isTypeElement(element));
    return (TypeElement) element;
  }

  public static TypeElement getSuperclass(TypeElement element) {
    DeclaredType superClass = (DeclaredType) element.getSuperclass();
    return superClass != null ? (TypeElement) superClass.asElement() : null;
  }

  public static boolean isPrimitiveConstant(VariableElement element) {
    return isFinal(element) && element.asType().getKind().isPrimitive()
        && element.getConstantValue() != null
        // Exclude local variables declared final.
        && element.getKind().isField();
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
    if (element instanceof GeneratedVariableElement) {
      return ((GeneratedVariableElement) element).isNonnull();
    }
    return BindingUtil.isNonnull(BindingConverter.unwrapVariableElement(element));
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

  public static boolean isType(Element element) {
    ElementKind kind = element.getKind();
    return kind == ElementKind.ANNOTATION_TYPE || kind == ElementKind.CLASS
        || kind == ElementKind.ENUM || kind == ElementKind.INTERFACE
        || kind == ElementKind.TYPE_PARAMETER;
  }

  public static boolean isVariable(Element element) {
    ElementKind kind = element.getKind();
    return kind == ElementKind.FIELD || kind == ElementKind.LOCAL_VARIABLE
        || kind == ElementKind.PARAMETER || kind == ElementKind.EXCEPTION_PARAMETER
        || kind == ElementKind.RESOURCE_VARIABLE;
  }

  public static boolean isField(Element element) {
    ElementKind kind = element.getKind();
    return kind == ElementKind.FIELD || kind == ElementKind.ENUM_CONSTANT;
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

  public static boolean isWeakReference(VariableElement varElement) {
    IVariableBinding var = (IVariableBinding) BindingConverter.unwrapElement(varElement);
    if (var.getName().startsWith("this$")
        && BindingUtil.isWeakOuterAnonymousClass(var.getDeclaringClass())) {
      return true;
    }
    return BindingUtil.hasNamedAnnotation(var, "Weak")
        || BindingUtil.hasWeakPropertyAttribute(var)
        || (var.getName().startsWith("this$")
        && BindingUtil.hasNamedAnnotation(var.getDeclaringClass(), "WeakOuter"));
  }

  public static boolean isLocal(TypeElement type) {
    NestingKind nestingKind = type.getNestingKind();
    return nestingKind == NestingKind.ANONYMOUS || nestingKind == NestingKind.LOCAL;
  }

  public static List<DeclaredType> getInheritedDeclaredTypesInclusive(TypeMirror type,
      ParserEnvironment env) {
    List<DeclaredType> typeElements = new ArrayList<>();
    for (TypeMirror superType : getOrderedInheritedTypesInclusive(type, env)) {
      if (!TypeUtil.isIntersection(superType)) {
        typeElements.add((DeclaredType) superType);
      }
    }
    return typeElements;
  }

  public static LinkedHashSet<TypeMirror> getOrderedInheritedTypesInclusive(TypeMirror type,
      ParserEnvironment env) {
    LinkedHashSet<TypeMirror> inheritedTypes = new LinkedHashSet<>();
    collectInheritedTypesInclusive(type, inheritedTypes, env);
    return inheritedTypes;
  }

  private static void collectInheritedTypesInclusive(
      TypeMirror type, Set<TypeMirror> inheritedTypes, ParserEnvironment env) {
    if (type == null) {
      return;
    }
    inheritedTypes.add(type);
    for (TypeMirror superType : env.typeUtilities().directSupertypes(type)) {
      collectInheritedTypesInclusive(superType, inheritedTypes, env);
    }
  }

  public static <T extends Element> Iterable<T> filterEnclosedElements(
      Element elem, Class<T> resultClass, ElementKind... kinds) {
    List<ElementKind> kindsList = Arrays.asList(kinds);
    return Iterables.transform(Iterables.filter(
        elem.getEnclosedElements(), e -> kindsList.contains(e.getKind())), resultClass::cast);
  }

  public static List<ExecutableElement> getDeclaredMethods(Element e) {
    return Lists.newArrayList(filterEnclosedElements(
        e, ExecutableElement.class, ElementKind.CONSTRUCTOR, ElementKind.METHOD));
  }

  public static List<VariableElement> getDeclaredFields(Element e) {
    return Lists.newArrayList(filterEnclosedElements(e, VariableElement.class, ElementKind.FIELD));
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

  public static Set<Modifier> toModifierSet(int modifiers) {
    Set<Modifier> set = modifierSets.get(modifiers);
    if (set == null) {
      set = EnumSet.noneOf(Modifier.class);
      if ((modifiers & java.lang.reflect.Modifier.PUBLIC) > 0) {
        set.add(Modifier.PUBLIC);
      }
      if ((modifiers & java.lang.reflect.Modifier.PRIVATE) > 0) {
        set.add(Modifier.PRIVATE);
      }
      if ((modifiers & java.lang.reflect.Modifier.PROTECTED) > 0) {
        set.add(Modifier.PROTECTED);
      }
      if ((modifiers & java.lang.reflect.Modifier.STATIC) > 0) {
        set.add(Modifier.STATIC);
      }
      if ((modifiers & java.lang.reflect.Modifier.FINAL) > 0) {
        set.add(Modifier.FINAL);
      }
      if ((modifiers & java.lang.reflect.Modifier.SYNCHRONIZED) > 0) {
        set.add(Modifier.SYNCHRONIZED);
      }
      if ((modifiers & java.lang.reflect.Modifier.VOLATILE) > 0) {
        set.add(Modifier.VOLATILE);
      }
      if ((modifiers & java.lang.reflect.Modifier.TRANSIENT) > 0) {
        set.add(Modifier.TRANSIENT);
      }
      if ((modifiers & java.lang.reflect.Modifier.NATIVE) > 0) {
        set.add(Modifier.NATIVE);
      }
      if ((modifiers & java.lang.reflect.Modifier.ABSTRACT) > 0) {
        set.add(Modifier.ABSTRACT);
      }
      if ((modifiers & java.lang.reflect.Modifier.STRICT) > 0) {
        set.add(Modifier.STRICTFP);
      }
      // Indirectly check whether Modifier.DEFAULT exists, since it was
      // added in Java 8.
      if ((modifiers & org.eclipse.jdt.core.dom.Modifier.DEFAULT) > 0) {
        try {
          Modifier m = Modifier.valueOf("DEFAULT");
          set.add(m);
        } catch (IllegalArgumentException e) {
          // Can only add DEFAULT modifier in Java 8.
        }
      }

      modifierSets.put(modifiers, set);
    }
    return set;
  }

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
    // Indirectly check whether Modifier.DEFAULT exists, since it was
    // added in Java 8.
    try {
      Modifier m = Modifier.valueOf("DEFAULT");
      if (set.contains(m)) {
        modifiers |= org.eclipse.jdt.core.dom.Modifier.DEFAULT;
      }
    } catch (IllegalArgumentException e) {
      // Can only add DEFAULT modifier in Java 8.
    }
    return modifiers;
  }

  public static boolean isRuntimeAnnotation(AnnotationMirror mirror) {
    for (AnnotationMirror ann : mirror.getAnnotationType().asElement().getAnnotationMirrors()) {
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
    for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
      String className = TypeUtil.getQualifiedName(annotation.getAnnotationType());
      if (className.equals(annotationClass.getName())) {
        return annotation;
      }
    }
    return null;
  }

  public static boolean hasAnnotation(Element element, Class<?> annotationClass) {
    return getAnnotation(element, annotationClass) != null;
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
}
