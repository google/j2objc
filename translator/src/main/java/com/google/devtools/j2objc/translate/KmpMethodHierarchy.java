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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.ImmutableMap;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.jspecify.annotations.Nullable;

/**
 * Resolves {@link ObjectiveCKmpMethod} method hierarchies, supertype annotations, and interface
 * dispatch targets using Java overriding semantics and per-type caching.
 */
final class KmpMethodHierarchy {

  /** Collected methods and {@link ObjectiveCKmpMethod} annotations across a type's hierarchy. */
  record TypeMethods(
      TypeElement typeElement,
      ImmutableMap<String, ExecutablePair> allMethods,
      ImmutableMap<String, AnnotationMirror> annotatedSignatures,
      ImmutableMap<String, ExecutableElement> annotatedMethods) {}

  /** Concrete class in a type's hierarchy that owns the KMP adapter and helpers for a method. */
  record ConcreteKmpOwner(
      TypeElement ownerType, ExecutablePair method, AnnotationMirror annotation) {}

  private record CachedTypeMethods(DeclaredType declaredType, TypeMethods typeMethods) {}

  private final TypeUtil typeUtil;
  private final ElementUtil elementUtil;
  private final Map<TypeElement, List<CachedTypeMethods>> typeMethodsCache = new HashMap<>();

  KmpMethodHierarchy(TypeUtil typeUtil, ElementUtil elementUtil) {
    this.typeUtil = typeUtil;
    this.elementUtil = elementUtil;
  }

  /** Collects all reachable methods and annotated signatures for {@code typeElem}. */
  TypeMethods getTypeMethods(TypeElement typeElem) {
    return getTypeMethods(typeElem, (DeclaredType) typeElem.asType());
  }

  /**
   * Collects all reachable methods and annotated signatures for {@code typeElem}, instantiated as
   * {@code declaredType}, using per-type/per-instantiation caching.
   */
  private TypeMethods getTypeMethods(TypeElement typeElem, DeclaredType declaredType) {
    List<CachedTypeMethods> entries =
        typeMethodsCache.computeIfAbsent(typeElem, _ -> new ArrayList<>());
    for (CachedTypeMethods entry : entries) {
      if (typeUtil.isSameType(entry.declaredType(), declaredType)) {
        return entry.typeMethods();
      }
    }
    TypeMethods computed = computeTypeMethods(typeElem, declaredType);
    entries.add(new CachedTypeMethods(declaredType, computed));
    return computed;
  }

  private TypeMethods computeTypeMethods(TypeElement typeElem, DeclaredType declaredType) {
    Map<String, ExecutablePair> methods = new LinkedHashMap<>();
    Map<String, ExecutableElement> annotatedMethods = new LinkedHashMap<>();

    for (TypeMirror supertype : typeUtil.directSupertypes(declaredType)) {
      if (!(supertype instanceof DeclaredType superDeclared)) {
        continue;
      }
      TypeElement superElem = TypeUtil.asTypeElement(superDeclared);
      if (superElem == null) {
        continue;
      }
      TypeMethods superMethods = getTypeMethods(superElem, superDeclared);
      for (Map.Entry<String, ExecutablePair> entry : superMethods.allMethods().entrySet()) {
        String sig = entry.getKey();
        ExecutablePair inherited = entry.getValue();
        if (!isInheritedBy(inherited.element(), typeElem)) {
          continue;
        }
        ExecutablePair existing = methods.get(sig);
        if (existing == null || takesPrecedence(inherited.element(), existing.element())) {
          methods.put(sig, inherited);
        }
        ExecutableElement superAnnotated = superMethods.annotatedMethods().get(sig);
        if (superAnnotated != null && takesPrecedence(superAnnotated, annotatedMethods.get(sig))) {
          annotatedMethods.put(sig, superAnnotated);
        }
      }
    }

    for (ExecutableElement methodElem : ElementUtil.getExecutables(typeElem)) {
      ExecutablePair method =
          new ExecutablePair(methodElem, typeUtil.asMemberOf(declaredType, methodElem));
      String sig = getOverrideSignature(method);
      methods.put(sig, method);
      if (ElementUtil.hasAnnotation(methodElem, ObjectiveCKmpMethod.class)) {
        annotatedMethods.put(sig, methodElem);
      }
    }

    ImmutableMap.Builder<String, AnnotationMirror> annotations = ImmutableMap.builder();
    for (Map.Entry<String, ExecutableElement> entry : annotatedMethods.entrySet()) {
      annotations.put(
          entry.getKey(), ElementUtil.getAnnotation(entry.getValue(), ObjectiveCKmpMethod.class));
    }
    return new TypeMethods(
        typeElem,
        ImmutableMap.copyOf(methods),
        annotations.buildOrThrow(),
        ImmutableMap.copyOf(annotatedMethods));
  }

  /** Returns the JVM-style parameter-erased signature used to match method overrides. */
  private String getOverrideSignature(ExecutablePair method) {
    StringBuilder sb = new StringBuilder(ElementUtil.getName(method.element()));
    sb.append('(');
    for (TypeMirror pType : method.type().getParameterTypes()) {
      sb.append(typeUtil.getSignatureName(pType));
    }
    sb.append(')');
    return sb.toString();
  }

  /**
   * Finds the first direct supertype of {@code typeElem} whose hierarchy contains an {@link
   * ObjectiveCKmpMethod}-annotated method with {@code signature}.
   */
  @Nullable TypeMethods getSupertypeHierarchy(TypeElement typeElem, String signature) {
    for (TypeMirror supertype : typeUtil.directSupertypes(typeElem.asType())) {
      if (!(supertype instanceof DeclaredType superDeclared)) {
        continue;
      }
      TypeElement superElement = TypeUtil.asTypeElement(superDeclared);
      if (superElement == null) {
        continue;
      }
      TypeMethods superMethods = getTypeMethods(superElement, superDeclared);
      if (superMethods.annotatedSignatures().containsKey(signature)) {
        return superMethods;
      }
    }
    return null;
  }

  /**
   * Finds the class in {@code typeElem}'s superclass hierarchy (inclusive of {@code typeElem})
   * that owns the concrete Objective-C KMP adapter and helpers for instance method {@code
   * signature}, or {@code null} if {@code signature} is not a concrete instance KMP method.
   */
  @Nullable ConcreteKmpOwner findConcreteKmpOwner(TypeElement typeElem, String signature) {
    DeclaredType currentType = (DeclaredType) typeElem.asType();
    TypeMethods currentMethods = getTypeMethods(typeElem, currentType);
    if (!hasConcreteKmpMethod(currentMethods, signature)) {
      return null;
    }
    // Walk up while the superclass already owns a concrete KMP adapter with the same config.
    DeclaredType superType = typeUtil.getSuperclass(currentType);
    while (superType != null) {
      TypeMethods superMethods = getTypeMethods(TypeUtil.asTypeElement(superType), superType);
      // Stop if the superclass method is absent, abstract, unannotated, or re-configured.
      if (!hasConcreteKmpMethod(superMethods, signature)
          || !sameKmpConfig(currentMethods, superMethods, signature)) {
        break;
      }
      currentType = superType;
      currentMethods = superMethods;
      superType = typeUtil.getSuperclass(currentType);
    }

    // Return the method as declared on the owner class (e.g. Base<T>) rather than with subclass
    // type arguments (e.g. Base<String>).
    TypeElement ownerElem = currentMethods.typeElement();
    DeclaredType ownerType = (DeclaredType) ownerElem.asType();
    ExecutablePair pair = currentMethods.allMethods().get(signature);
    ExecutablePair ownerMethod =
        typeUtil.isSameType(currentType, ownerType)
            ? pair
            : new ExecutablePair(pair.element(), typeUtil.asMemberOf(ownerType, pair.element()));
    return new ConcreteKmpOwner(
        ownerElem, ownerMethod, currentMethods.annotatedSignatures().get(signature));
  }

  private static boolean hasConcreteKmpMethod(TypeMethods methods, String signature) {
    if (!methods.annotatedSignatures().containsKey(signature)) {
      return false;
    }
    ExecutablePair pair = methods.allMethods().get(signature);
    return pair != null
        && !ElementUtil.isAbstract(pair.element())
        && ElementUtil.isInstanceMethod(pair.element());
  }

  private boolean sameKmpConfig(TypeMethods sub, TypeMethods sup, String signature) {
    if (Objects.equals(
        sub.annotatedMethods().get(signature), sup.annotatedMethods().get(signature))) {
      return true;
    }
    AnnotationMirror a = sub.annotatedSignatures().get(signature);
    AnnotationMirror b = sup.annotatedSignatures().get(signature);
    return Objects.equals(
            ElementUtil.getAnnotationValue(a, "selector"),
            ElementUtil.getAnnotationValue(b, "selector"))
        && typeUtil.isSameType(
            (TypeMirror) ElementUtil.getAnnotationValue(a, "adapter"),
            (TypeMirror) ElementUtil.getAnnotationValue(b, "adapter"));
  }

  private static boolean isInheritedBy(ExecutableElement method, TypeElement subType) {
    if (!ElementUtil.isInstanceMethod(method) || ElementUtil.isPrivate(method)) {
      return false;
    }
    if (subType.getKind().isInterface()) {
      return ElementUtil.isPublic(method);
    }
    return !ElementUtil.getVisibilityModifiers(method).isEmpty()
        || ElementUtil.getPackage(method).equals(ElementUtil.getPackage(subType));
  }

  private boolean takesPrecedence(ExecutableElement a, @Nullable ExecutableElement b) {
    if (b == null) {
      return true;
    }
    TypeElement aOwner = ElementUtil.getDeclaringClass(a);
    TypeElement bOwner = ElementUtil.getDeclaringClass(b);
    if (aOwner == null || bOwner == null) {
      return false;
    }
    boolean aIsClass = aOwner.getKind().isClass();
    boolean bIsClass = bOwner.getKind().isClass();
    if (aIsClass != bIsClass) {
      return aIsClass;
    }
    return safeOverrides(a, b);
  }

  private boolean safeOverrides(ExecutableElement overrider, ExecutableElement overridden) {
    TypeElement owner = ElementUtil.getDeclaringClass(overrider);
    if (owner == null
        || overrider instanceof GeneratedElement
        || overridden instanceof GeneratedElement
        || owner instanceof GeneratedElement) {
      return false;
    }
    return elementUtil.overrides(overrider, overridden, owner);
  }

  /** Returns {@code method} or its superinterface declaration if it requires static dispatch. */
  @Nullable ExecutableElement findInterfaceMethodRequiringDispatch(
      @Nullable ExecutableElement method) {
    if (method == null
        || ElementUtil.isStatic(method)
        || ElementUtil.isConstructor(method)
        || method instanceof GeneratedElement) {
      return null;
    }
    TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
    if (declaringClass == null
        || !declaringClass.getKind().isInterface()
        || declaringClass instanceof GeneratedElement) {
      return null;
    }
    if (ElementUtil.hasAnnotation(method, ObjectiveCKmpMethod.class)) {
      return method;
    }
    String signature = getOverrideSignature(new ExecutablePair(method));
    return getTypeMethods(declaringClass).annotatedMethods().get(signature);
  }
}
