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
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.jspecify.annotations.Nullable;

/**
 * Resolves {@link ObjectiveCKmpMethod} method hierarchies, supertype annotations, and interface
 * dispatch targets.
 */
final class KmpMethodHierarchy {

  /** Collected methods and {@link ObjectiveCKmpMethod} annotations across a type's hierarchy. */
  record TypeMethods(
      TypeElement typeElement,
      ImmutableMap<String, ExecutablePair> allMethods,
      ImmutableMap<String, AnnotationMirror> annotatedSignatures) {}

  private final TypeUtil typeUtil;

  KmpMethodHierarchy(TypeUtil typeUtil) {
    this.typeUtil = typeUtil;
  }

  /** Collects all reachable methods and annotated signatures for {@code typeElem}. */
  TypeMethods getTypeMethods(TypeElement typeElem) {
    return getTypeMethods(typeElem, (DeclaredType) typeElem.asType());
  }

  /**
   * Collects all reachable methods and annotated signatures for {@code typeElem}, instantiated as
   * {@code declaredType}.
   */
  private TypeMethods getTypeMethods(TypeElement typeElem, DeclaredType declaredType) {
    Map<String, ExecutablePair> methods = new LinkedHashMap<>();
    Map<String, AnnotationMirror> annotations = new LinkedHashMap<>();
    collectMethods(declaredType, methods, annotations);
    return new TypeMethods(
        typeElem, ImmutableMap.copyOf(methods), ImmutableMap.copyOf(annotations));
  }

  /** Returns the JVM-style parameter-erased signature used to match method overrides. */
  private String getOverrideSignature(ExecutablePair method) {
    return getOverrideSignature(method, typeUtil);
  }

  /** Returns the JVM-style parameter-erased signature using {@code typeUtil}. */
  private static String getOverrideSignature(ExecutablePair method, TypeUtil typeUtil) {
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
      TypeElement superElement = TypeUtil.asTypeElement(supertype);
      if (superElement == null) {
        continue;
      }
      TypeMethods superMethods = getTypeMethods(superElement, (DeclaredType) supertype);
      if (superMethods.annotatedSignatures().containsKey(signature)) {
        return superMethods;
      }
    }
    return null;
  }

  /**
   * Traverses {@code type} and its supertypes to populate {@code allMethods} and {@code
   * annotatedSignatures}.
   */
  private void collectMethods(
      DeclaredType type,
      Map<String, ExecutablePair> allMethods,
      Map<String, AnnotationMirror> annotatedSignatures) {
    visitTypeHierarchy(
        type,
        typeUtil,
        new HashSet<>(),
        declaredType -> collectMethodsFromType(declaredType, allMethods, annotatedSignatures));
  }

  /**
   * Records methods declared on {@code type}, keeping the most specific implementation per override
   * signature and the first {@link ObjectiveCKmpMethod} annotation encountered.
   */
  private boolean collectMethodsFromType(
      DeclaredType type,
      Map<String, ExecutablePair> allMethods,
      Map<String, AnnotationMirror> annotatedSignatures) {
    TypeElement element = TypeUtil.asTypeElement(type);
    for (ExecutableElement methodElem : ElementUtil.getExecutables(element)) {
      ExecutablePair method = new ExecutablePair(methodElem, typeUtil.asMemberOf(type, methodElem));
      String signature = getOverrideSignature(method);

      if (!allMethods.containsKey(signature)) {
        allMethods.put(signature, method);
      } else {
        ExecutablePair existing = allMethods.get(signature);
        if (takesPrecedence(method, existing)) {
          allMethods.put(signature, method);
        }
      }

      AnnotationMirror annotation =
          ElementUtil.getAnnotation(methodElem, ObjectiveCKmpMethod.class);
      if (annotation != null && !annotatedSignatures.containsKey(signature)) {
        annotatedSignatures.put(signature, annotation);
      }
    }
    return true;
  }

  private static boolean takesPrecedence(ExecutablePair a, ExecutablePair b) {
    if (b == null) {
      return true;
    }
    // Since we traverse from subclass to superclass/interfaces (bottom-up), 'b' (already seen)
    // is generally more specific than 'a' (currently visiting).
    // The only case 'a' should replace 'b' is when 'a' is a concrete class implementation
    // and 'b' is an interface declaration, ensuring we use the class's method element.
    boolean aIsClass = ElementUtil.getDeclaringClass(a.element()).getKind().isClass();
    boolean bIsClass = ElementUtil.getDeclaringClass(b.element()).getKind().isClass();
    return aIsClass && !bIsClass;
  }

  /** A visitor for types in a type hierarchy. */
  @FunctionalInterface
  private interface TypeHierarchyVisitor {
    /**
     * Visits a type in a type hierarchy.
     *
     * @param declaredType the parameterized {@link DeclaredType} as referenced in the hierarchy
     *     (preserving type arguments for {@link TypeUtil#asMemberOf})
     * @return {@code true} to continue visiting supertypes, or {@code false} to stop traversal
     *     early
     */
    boolean visit(DeclaredType declaredType);
  }

  @CanIgnoreReturnValue
  private static boolean visitTypeHierarchy(
      DeclaredType type,
      TypeUtil typeUtil,
      Set<TypeElement> visited,
      TypeHierarchyVisitor visitor) {
    if (TypeUtil.isNone(type)) {
      return true;
    }
    TypeElement element = TypeUtil.asTypeElement(type);
    if (element == null || !visited.add(element)) {
      return true;
    }
    if (!visitor.visit(type)) {
      return false;
    }
    for (TypeMirror supertype : typeUtil.directSupertypes(type)) {
      if (!visitTypeHierarchy((DeclaredType) supertype, typeUtil, visited, visitor)) {
        return false;
      }
    }
    return true;
  }

  /** Returns {@code method} or its superinterface declaration if it requires static dispatch. */
  @Nullable ExecutableElement findInterfaceMethodRequiringDispatch(
      @Nullable ExecutableElement method) {
    if (method == null || ElementUtil.isStatic(method) || ElementUtil.isConstructor(method)) {
      return null;
    }
    TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
    if (declaringClass == null || !declaringClass.getKind().isInterface()) {
      return null;
    }
    if (ElementUtil.getAnnotation(method, ObjectiveCKmpMethod.class) != null) {
      return method;
    }
    return findSuperInterfaceRequiringDispatch(
        (DeclaredType) declaringClass.asType(),
        getOverrideSignature(new ExecutablePair(method)),
        new HashSet<>());
  }

  /**
   * Searches superinterfaces of {@code type} for an annotated method matching {@code signature}.
   */
  private @Nullable ExecutableElement findSuperInterfaceRequiringDispatch(
      DeclaredType type, String signature, Set<TypeElement> visited) {
    AnnotatedSuperMethodFinder finder = new AnnotatedSuperMethodFinder(typeUtil, signature);
    visitTypeHierarchy(type, typeUtil, visited, finder);
    return finder.result;
  }

  /**
   * Visitor that searches a type hierarchy for the first {@link ObjectiveCKmpMethod}-annotated
   * method matching a target override signature.
   */
  private static class AnnotatedSuperMethodFinder implements TypeHierarchyVisitor {
    private final TypeUtil typeUtil;
    private final String signature;
    private @Nullable ExecutableElement result;

    private AnnotatedSuperMethodFinder(TypeUtil typeUtil, String signature) {
      this.typeUtil = typeUtil;
      this.signature = signature;
    }

    @Override
    public boolean visit(DeclaredType declaredType) {
      for (ExecutableElement methodElem :
          ElementUtil.getExecutables(TypeUtil.asTypeElement(declaredType))) {
        if (ElementUtil.getAnnotation(methodElem, ObjectiveCKmpMethod.class) != null) {
          ExecutablePair pair =
              new ExecutablePair(methodElem, typeUtil.asMemberOf(declaredType, methodElem));
          if (getOverrideSignature(pair, typeUtil).equals(signature)) {
            result = methodElem;
            return false;
          }
        }
      }
      return true;
    }
  }
}
