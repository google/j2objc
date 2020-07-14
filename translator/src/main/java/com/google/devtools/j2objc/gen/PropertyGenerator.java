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
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.Property;
import com.google.j2objc.annotations.Weak;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Generate an Objective-C property based on a variable declaration and Property annotation (if
 * present)
 */
public final class PropertyGenerator {

  public static Optional<String> generate(
      VariableDeclarationFragment fragment,
      Options options,
      NameTable nameTable,
      TypeUtil typeUtil,
      boolean parametersNonnullByDefault) {
    return new PropertyGenerator(fragment, options, nameTable, typeUtil, parametersNonnullByDefault)
        .build();
  }

  private final VariableDeclarationFragment fragment;
  private final Options options;
  private final NameTable nameTable;
  private final TypeUtil typeUtil;
  private final boolean parametersNonnullByDefault;
  private final PropertyAnnotation annotation;
  private final VariableElement varElement;
  private final TypeMirror varType;
  private final String propertyName;
  private final FieldDeclaration declaration;

  private PropertyGenerator(
      VariableDeclarationFragment fragment,
      Options options,
      NameTable nameTable,
      TypeUtil typeUtil,
      boolean parametersNonnullByDefault) {
    this.fragment = fragment;
    this.options = options;
    this.nameTable = nameTable;
    this.typeUtil = typeUtil;
    this.parametersNonnullByDefault = parametersNonnullByDefault;
    declaration = (FieldDeclaration) fragment.getParent();
    PropertyAnnotation annotation =
        (PropertyAnnotation) TreeUtil.getAnnotation(Property.class, declaration.getAnnotations());
    varElement = fragment.getVariableElement();
    if (annotation == null
        && options.classProperties()
        && ElementUtil.isStatic(varElement)
        && !declaration.hasPrivateDeclaration()) {
      // Generate the property for a static variable by simulating the @Property annotation.
      annotation = new PropertyAnnotation();
    }
    this.annotation = annotation;
    varType = varElement.asType();
    propertyName = nameTable.getStaticAccessorName(varElement);
  }

  private Optional<String> build() {
    if (annotation == null) {
      return Optional.empty();
    }
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
    VariableDeclarationFragment firstVarNode = declaration.getFragment();
    if (typeUtil.isString(varType)) {
      attributes.add("copy");
    } else if (ElementUtil.hasAnnotation(firstVarNode.getVariableElement(), Weak.class)) {
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
    } else if (!varType.getKind().isPrimitive()
        && !PropertyAnnotation.hasMemoryManagementAttribute(attributes)) {
      attributes.add("strong");
    }
    return true;
  }

  private void processAccessorAttributes(Set<String> attributes) {
    // Add default getter/setter here, as each fragment needs its own attributes
    // to support its unique accessors.
    TypeElement declaringClass = ElementUtil.getDeclaringClass(varElement);
    ExecutableElement getter =
        ElementUtil.findGetterMethod(
            propertyName, varType, declaringClass, ElementUtil.isStatic(varElement));
    if (getter != null) {
      // Update getter from its Java name to its selector. This is normally the
      // same since getters have no parameters, but the name may be reserved.
      attributes.remove("getter=" + annotation.getGetter());
      attributes.add("getter=" + nameTable.getMethodSelector(getter));
      if (!ElementUtil.isSynchronized(getter)) {
        attributes.add("nonatomic");
      }
    }
    ExecutableElement setter =
        ElementUtil.findSetterMethod(
            propertyName, varType, declaringClass, ElementUtil.isStatic(varElement));
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
    if (ElementUtil.isStatic(varElement)) {
      attributes.add("class");
    } else if (attributes.contains("class")) {
      ErrorUtil.error(fragment, "Only static fields can be translated to class properties");
    }
    if (attributes.contains("class")) {
      if (!options.staticAccessorMethods()) {
        // Class property accessors must be present, as they are not synthesized by runtime.
        ErrorUtil.error(
            fragment,
            "Class properties require any of these flags: "
                + "--swift-friendly, --class-properties or --static-accessor-methods");
      } else if (declaration.hasPrivateDeclaration()) {
        ErrorUtil.error(fragment, "Properties are not supported for private static fields.");
      }
    }
  }

  private void processNullabilityAttributes(Set<String> attributes) {
    if (options.nullability() && !varElement.asType().getKind().isPrimitive()) {
      if (ElementUtil.hasNullableAnnotation(varElement)) {
        attributes.add("nullable");
      } else if (ElementUtil.isNonnull(varElement, parametersNonnullByDefault)) {
        attributes.add("nonnull");
      }
    }
  }

  private void processOtherAttributes(Set<String> attributes) {
    // Remove default attributes.
    attributes.remove("readwrite");
    attributes.remove("atomic");

    if (ElementUtil.isFinal(varElement)) {
      attributes.add("readonly");
    }
  }

  private String getStringRepresentation(Set<String> attributes) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("@property ");
    if (!attributes.isEmpty()) {
      buffer.append('(').append(PropertyAnnotation.toAttributeString(attributes)).append(") ");
    }

    String objcType = nameTable.getObjCType(varType);
    buffer.append(objcType);
    if (!objcType.endsWith("*")) {
      buffer.append(' ');
    }
    buffer.append(propertyName);
    if (options.classProperties() && ElementUtil.isStatic(varElement)) {
      buffer.append(" NS_SWIFT_NAME(").append(propertyName).append(")");
    }
    buffer.append(";");
    return buffer.toString();
  }
}
