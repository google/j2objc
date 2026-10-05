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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.Weak;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.jspecify.annotations.Nullable;

/**
 * Generate an Objective-C property based on a variable declaration and Property annotation (if
 * present)
 */
public final class PropertyGenerator {

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
  private final VariableDeclarationFragment member;
  /** The field declaration, used to report errors about the attributes. */
  private final FieldDeclaration declaration;
  private final VariableElement element;
  private final TypeMirror type;
  private final boolean isStatic;
  private final String propertyName;
  private final String objcType;
  private final ExecutableElement getter;
  private final ExecutableElement setter;
  private final PropertyAnnotation annotation;
  private final boolean staticToInstance;

  private PropertyGenerator(
      TypeGenerator generator,
      VariableDeclarationFragment member,
      PropertyAnnotation annotation,
      boolean staticToInstance) {
    this.options = generator.options;
    this.nameTable = generator.nameTable;
    this.typeUtil = generator.typeUtil;
    this.parametersNonnullByDefault = generator.parametersNonnullByDefault;
    this.member = member;
    this.annotation = annotation;
    this.staticToInstance = staticToInstance;
    this.declaration = (FieldDeclaration) member.getParent();
    this.element = member.getVariableElement();
    this.type = element.asType();
    this.isStatic = ElementUtil.isStatic(element);
    this.propertyName = nameTable.getStaticAccessorName(element);
    this.objcType = nameTable.getObjCType(type);
    TypeElement declaringClass = ElementUtil.getDeclaringClass(element);
    this.getter = ElementUtil.findGetterMethod(propertyName, type, declaringClass, isStatic);
    this.setter = ElementUtil.findSetterMethod(propertyName, type, declaringClass, isStatic);
  }

  private boolean isReadonly() {
    return ElementUtil.isFinal(element);
  }

  private boolean isWeak() {
    return ElementUtil.hasAnnotation(declaration.getFragment().getVariableElement(), Weak.class);
  }

  private boolean hasPrivateDeclaration() {
    return declaration.hasPrivateDeclaration();
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

  private boolean processMemoryManagementAttributes(Set<String> attributes) {
    if (typeUtil.isString(type)) {
      attributes.add("copy");
    } else if (isWeak()) {
      if (attributes.contains("strong")) {
        ErrorUtil.error(
            declaration, "Weak field annotation conflicts with strong Property attribute");
        return false;
      }
      attributes.add("weak");
    }

    // strong is the default when using ARC; otherwise, assign is the default.
    if (options.useARC()) {
      attributes.remove("strong");
    } else if (!type.getKind().isPrimitive()
        && !PropertyAnnotation.hasMemoryManagementAttribute(attributes)) {
      attributes.add("strong");
    }
    return true;
  }

  private void processAccessorAttributes(Set<String> attributes) {
    if (getter != null) {
      // Update getter from its Java name to its selector. This is normally the
      // same since getters have no parameters, but the name may be reserved.
      String getterSelector = nameTable.getMethodSelector(getter);
      attributes.remove("getter=" + annotation.getGetter());
      if (!getterSelector.equals(propertyName)) {
        attributes.add("getter=" + getterSelector);
      }
      if (!ElementUtil.isSynchronized(getter)) {
        attributes.add("nonatomic");
      }
    }
    if (setter != null) {
      // Update setter from its Java name to its selector.
      attributes.remove("setter=" + annotation.getSetter());
      attributes.add("setter=" + nameTable.getMethodSelector(setter));
      if (!ElementUtil.isSynchronized(setter)) {
        attributes.add("nonatomic");
      }
    }
  }

  private void processClassAttribute(Set<String> attributes) {
    if (isStatic && !staticToInstance) {
      attributes.add("class");
    } else if (attributes.contains("class")) {
      ErrorUtil.error(member, "Only static fields can be translated to class properties");
    }
    if (attributes.contains("class")) {
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
    if (options.nullability() && !type.getKind().isPrimitive()) {
      if (ElementUtil.hasNullableAnnotation(element)) {
        attributes.add("nullable");
      } else if (ElementUtil.isNonnull(element, parametersNonnullByDefault)) {
        attributes.add("nonnull");
      }
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
