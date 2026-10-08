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

package com.google.devtools.j2objc.gen;

import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.Weak;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.jspecify.annotations.Nullable;

/**
 * Generates an Objective-C property declaration.
 *
 * <p>Properties are generated either for fields (annotated with {@code @Property} or static fields
 * exposed as class properties) or for getter methods (pseudo-properties, i.e. methods annotated
 * with {@code @Property} directly or through their enclosing type). Both kinds share the same
 * attribute computation; they only differ in where the property name, type, accessors and
 * read-only-ness come from.
 */
public final class PropertyGenerator {

  private static final ImmutableSet<String> NULLABILITY_ATTRIBUTES =
      ImmutableSet.of("nonnull", "nullable", "null_resettable", "null_unspecified");

  /**
   * Generates the property for a field, if it is annotated with {@code @Property} or is a static
   * field exposed as a class property.
   */
  public static Optional<String> generate(
      TypeGenerator generator, VariableDeclarationFragment fragment) {
    return generate(generator, fragment, /* staticToInstance= */ false);
  }

  /**
   * Generates the property for a field, if it is annotated with {@code @Property} or is a static
   * field exposed as a class property.
   *
   * @param staticToInstance whether a static field is exposed as an instance property
   */
  public static Optional<String> generate(
      TypeGenerator generator, VariableDeclarationFragment fragment, boolean staticToInstance) {
    FieldDeclaration declaration = (FieldDeclaration) fragment.getParent();
    PropertyAnnotation annotation = findPropertyAnnotation(declaration.getAnnotations());
    if (annotation == null
        && generator.options.classProperties()
        && ElementUtil.isStatic(fragment.getVariableElement())
        && !declaration.hasPrivateDeclaration()) {
      // Generate the property for a static variable by simulating the @Property annotation.
      annotation = new PropertyAnnotation();
    }
    if (annotation == null) {
      return Optional.empty();
    }
    return new PropertyGenerator(generator, fragment, annotation, staticToInstance).build();
  }

  /**
   * Generates the pseudo-property for a getter method.
   *
   * @param staticToInstance whether a static getter is exposed as an instance property
   */
  public static Optional<String> generate(
      TypeGenerator generator, MethodDeclaration method, boolean staticToInstance) {
    return new PropertyGenerator(
            generator, method, getPseudoPropertyAnnotation(generator, method), staticToInstance)
        .build();
  }

  /**
   * Generates the {@code @synthesize} statement for an instance field annotated with
   * {@code @Property}, if Objective-C needs to synthesize at least one of its accessors.
   */
  public static Optional<String> generateSynthesizeStatement(
      TypeGenerator generator, VariableDeclarationFragment fragment) {
    FieldDeclaration declaration = (FieldDeclaration) fragment.getParent();
    return Optional.ofNullable(findPropertyAnnotation(declaration.getAnnotations()))
        .map(
            annotation ->
                new PropertyGenerator(
                        generator, fragment, annotation, /* staticToInstance= */ false)
                    .buildSynthesizeStatement());
  }

  /** Returns the name of the pseudo-property for a getter method. */
  public static String getPseudoPropertyName(NameTable nameTable, MethodDeclaration method) {
    String methodName = nameTable.getMethodSelector(method.getExecutableElement());
    if (methodName.length() > 3
        && methodName.startsWith("get")
        && Character.isUpperCase(methodName.charAt(3))) {
      methodName = methodName.substring(3);
    }
    return NameTable.lowercaseFirst(methodName);
  }

  /**
   * Returns the {@code @Property} annotation that applies to a pseudo-property, which is either
   * declared on the method itself or on its enclosing type.
   */
  private static PropertyAnnotation getPseudoPropertyAnnotation(
      TypeGenerator generator, MethodDeclaration method) {
    PropertyAnnotation methodAnnotation = findPropertyAnnotation(method.getAnnotations());
    if (methodAnnotation != null) {
      return methodAnnotation;
    }
    PropertyAnnotation typeAnnotation = findPropertyAnnotation(generator.typeNode.getAnnotations());
    if (typeAnnotation == null) {
      return new PropertyAnnotation();
    }
    if (method.getReturnTypeMirror().getKind().isPrimitive()) {
      // Type level memory management attributes only apply to the getters that return objects.
      typeAnnotation = typeAnnotation.copy();
      for (String attribute :
          PropertyAnnotation.getMemoryManagementAttributes(
              typeAnnotation.getPropertyAttributes())) {
        typeAnnotation.removeAttribute(attribute);
      }
    }
    return typeAnnotation;
  }

  private static @Nullable PropertyAnnotation findPropertyAnnotation(List<Annotation> annotations) {
    return annotations.stream()
        .filter(PropertyAnnotation.class::isInstance)
        .map(PropertyAnnotation.class::cast)
        .findFirst()
        .orElse(null);
  }

  private final Options options;
  private final NameTable nameTable;
  private final TypeUtil typeUtil;
  private final boolean parametersNonnullByDefault;
  private final boolean nullMarked;

  /** The field fragment or the getter method declaration. */
  private final TreeNode member;

  /** The field or method declaration, used to report errors about the attributes. */
  private final TreeNode declaration;

  private final boolean isField;
  private final Element element;
  private final TypeMirror type;
  private final boolean isStatic;
  private final String propertyName;
  private final String objcType;
  private final ExecutableElement getter;
  private final ExecutableElement setter;
  private final PropertyAnnotation annotation;
  private final boolean staticToInstance;

  /**
   * Creates a generator for {@code member}, which is either a field {@link
   * VariableDeclarationFragment} or a getter {@link MethodDeclaration}.
   */
  private PropertyGenerator(
      TypeGenerator generator,
      TreeNode member,
      PropertyAnnotation annotation,
      boolean staticToInstance) {
    this.options = generator.options;
    this.nameTable = generator.nameTable;
    this.typeUtil = generator.typeUtil;
    this.parametersNonnullByDefault = generator.parametersNonnullByDefault;
    this.nullMarked = generator.nullMarked;
    this.member = member;
    this.annotation = annotation;
    this.staticToInstance = staticToInstance;
    if (member instanceof VariableDeclarationFragment fragment) {
      this.isField = true;
      this.declaration = fragment.getParent();
      this.element = fragment.getVariableElement();
      this.type = element.asType();
      this.isStatic = ElementUtil.isStatic(element);
      this.propertyName = nameTable.getStaticAccessorName(fragment.getVariableElement());
      this.objcType = nameTable.getObjCType(type);
      this.getter = findGetterMethod();
    } else {
      MethodDeclaration method = (MethodDeclaration) member;
      this.isField = false;
      this.declaration = method;
      this.element = method.getExecutableElement();
      this.type = method.getReturnTypeMirror();
      this.isStatic = ElementUtil.isStatic(element);
      this.propertyName = getPseudoPropertyName(nameTable, method);
      this.objcType = generator.getReturnType(method, true); // Generics allowed in headers.
      this.getter = method.getExecutableElement();
    }
    this.setter = findSetterMethod();
  }

  private @Nullable ExecutableElement findGetterMethod() {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(element);
    ExecutableElement getter =
        annotation.getGetter() != null
            ? ElementUtil.findMethod(declaringClass, annotation.getGetter())
            : ElementUtil.findGetterMethod(propertyName, type, declaringClass, isStatic);
    return getter != null && ElementUtil.isStatic(getter) == isStatic ? getter : null;
  }

  private @Nullable ExecutableElement findSetterMethod() {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(element);
    ExecutableElement setter =
        annotation.getSetter() != null
            ? ElementUtil.findMethod(
                declaringClass, annotation.getSetter(), TypeUtil.getQualifiedName(type))
            : ElementUtil.findSetterMethod(propertyName, type, declaringClass, isStatic);
    return setter != null && ElementUtil.isStatic(setter) == isStatic ? setter : null;
  }

  private boolean isReadonly() {
    return isField
        ? ElementUtil.isFinal(element) || annotation.hasAttribute("readonly")
        : setter == null;
  }

  private boolean isWeak() {
    return isField
        && ElementUtil.hasAnnotation(
            ((FieldDeclaration) declaration).getFragment().getVariableElement(), Weak.class);
  }

  private boolean hasPrivateDeclaration() {
    return isField && ((FieldDeclaration) declaration).hasPrivateDeclaration();
  }

  private Optional<String> build() {
    Set<String> attributes = annotation.getPropertyAttributes();
    if (!processMemoryManagementAttributes(attributes)) {
      return Optional.empty();
    }
    processAccessorAttributes(attributes);
    processClassAttribute(attributes);
    processNullabilityAttributes(attributes);
    processOtherAttributes(attributes);
    return Optional.of(getStringRepresentation(attributes));
  }

  private @Nullable String buildSynthesizeStatement() {
    if (isStatic || (getter != null && (isReadonly() || setter != null))) {
      // All required accessors are already defined as methods on the class.
      return null;
    }
    String varName = nameTable.getVariableShortName((VariableElement) element);
    return "@synthesize " + propertyName + " = " + varName + ";";
  }

  private boolean processMemoryManagementAttributes(Set<String> attributes) {
    ImmutableSet<String> explicitAttributes =
        PropertyAnnotation.getMemoryManagementAttributes(attributes);
    if (explicitAttributes.size() > 1) {
      ErrorUtil.error(
          declaration,
          "Conflicting memory management Property attributes: "
              + PropertyAnnotation.toAttributeString(explicitAttributes));
      return false;
    }
    boolean isPrimitive = type.getKind().isPrimitive();
    if (isWeak() && !explicitAttributes.isEmpty() && !explicitAttributes.contains("weak")) {
      ErrorUtil.error(
          declaration,
          "Weak field annotation conflicts with "
              + explicitAttributes.iterator().next()
              + " Property attribute");
      return false;
    }

    if (explicitAttributes.isEmpty()) {
      if (typeUtil.isString(type)) {
        attributes.add("copy");
      } else if (isWeak()) {
        attributes.add("weak");
      }
    }

    // strong is the default when using ARC; otherwise, assign is the default.
    if (options.useARC()) {
      attributes.remove("strong");
    } else if (!isPrimitive && !PropertyAnnotation.hasMemoryManagementAttribute(attributes)) {
      attributes.add("strong");
    }
    return true;
  }

  private void processAccessorAttributes(Set<String> attributes) {
    boolean isAtomic = attributes.contains("atomic");
    if (getter != null) {
      // Update getter from its Java name to its selector. This is normally the
      // same since getters have no parameters, but the name may be reserved.
      String getterSelector = nameTable.getMethodSelector(getter);
      attributes.remove("getter=" + annotation.getGetter());
      if (!getterSelector.equals(propertyName)) {
        attributes.add("getter=" + getterSelector);
      }
      if (!isAtomic && !ElementUtil.isSynchronized(getter)) {
        attributes.add("nonatomic");
      }
    }
    if (setter != null) {
      // Update setter from its Java name to its selector.
      attributes.remove("setter=" + annotation.getSetter());
      attributes.add("setter=" + nameTable.getMethodSelector(setter));
      if (!isAtomic && !ElementUtil.isSynchronized(setter)) {
        attributes.add("nonatomic");
      }
    }
  }

  private void processClassAttribute(Set<String> attributes) {
    if (isStatic && !staticToInstance) {
      attributes.add("class");
    } else if (attributes.contains("class")) {
      ErrorUtil.error(member, "Only static members can be translated to class properties");
    }
    if (isField && attributes.contains("class")) {
      if (!options.staticAccessorMethods()) {
        // Class property accessors must be present, as they are not synthesized by runtime.
        ErrorUtil.error(
            member,
            "Class properties require any of these flags: "
                + "--swift-friendly, --class-properties or --static-accessor-methods");
      } else if (hasPrivateDeclaration()) {
        ErrorUtil.error(member, "Properties are not supported for private static fields.");
      }
    }
  }

  private void processNullabilityAttributes(Set<String> attributes) {
    if (type.getKind().isPrimitive()) {
      // Nullability specifiers can only be applied to pointer types.
      attributes.removeAll(NULLABILITY_ATTRIBUTES);
      return;
    }
    // Java nullness annotations are authoritative, they replace any explicit specifier.
    if (ElementUtil.hasNullableAnnotation(element) && (options.nullability() || nullMarked)) {
      attributes.removeAll(NULLABILITY_ATTRIBUTES);
      attributes.add("nullable");
    } else if (options.nullability()
        && ElementUtil.isNonnull(element, parametersNonnullByDefault)) {
      attributes.removeAll(NULLABILITY_ATTRIBUTES);
      attributes.add("nonnull");
    }
  }

  private void processOtherAttributes(Set<String> attributes) {
    // Remove default attributes.
    attributes.remove("readwrite");
    attributes.remove("atomic");

    if (isReadonly()) {
      attributes.add("readonly");
    }
  }

  private String getStringRepresentation(Set<String> attributes) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("@property ");
    if (!attributes.isEmpty()) {
      buffer.append('(').append(PropertyAnnotation.toAttributeString(attributes)).append(") ");
    }

    buffer.append(objcType);
    if (!objcType.endsWith("*")) {
      buffer.append(' ');
    }
    buffer.append(propertyName);
    TypeElement declaringClass = ElementUtil.getDeclaringClass(element);
    boolean inSwiftNameContext =
        declaringClass != null
            && (nameTable.packageHasSwiftNameAnnotation(declaringClass)
                || nameTable.elementHasSwiftNameAnnotation(declaringClass));
    if ((options.classProperties() && isStatic) || inSwiftNameContext) {
      buffer.append(" NS_SWIFT_NAME(").append(propertyName).append(")");
    }
    buffer.append(";");
    return buffer.toString();
  }
}
