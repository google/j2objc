/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.types;

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.jdt.JdtMethodBinding;
import com.google.devtools.j2objc.jdt.JdtTypeBinding;
import com.google.devtools.j2objc.jdt.JdtVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Set;

import javax.lang.model.element.PackageElement;

/**
 * Binding class for types created during translation.
 *
 * @author Keith Stanger
 */
public class GeneratedTypeBinding extends AbstractTypeBinding {

  protected final String name;
  private int modifiers;
  private final PackageElement packageElement;
  private final JdtTypeBinding superClass;
  private final boolean isInterface;
  private final JdtTypeBinding componentType;
  private final Set<IVariableBinding> fields = Sets.newHashSet();
  private final Set<IMethodBinding> methods = Sets.newHashSet();

  public GeneratedTypeBinding(
      String name, PackageElement packageElement, ITypeBinding superClass, boolean isInterface,
      ITypeBinding componentType) {
    this.name = name;
    this.packageElement = packageElement;
    this.superClass = BindingConverter.wrapBinding(superClass);
    this.isInterface = isInterface;
    this.componentType = BindingConverter.wrapBinding(componentType);
  }

  /**
   * Creates a new non-array type, extracting the package from the provided name.
   */
  public static GeneratedTypeBinding newTypeBinding(
      String name, ITypeBinding superClass, boolean isInterface) {
    int idx = name.lastIndexOf('.');
    String packageName = idx < 0 ? "" : name.substring(0, idx);
    PackageElement packageElement = (PackageElement)
        BindingConverter.getElement(new GeneratedPackageBinding(packageName));
    name = name.substring(idx + 1);
    return new GeneratedTypeBinding(name, packageElement, superClass, isInterface, null);
  }

  public static GeneratedTypeBinding newArrayType(ITypeBinding componentType) {
    return new GeneratedTypeBinding(
        componentType.getName() + "[]", (PackageElement) null, null, false, componentType);
  }

  @Override
  public String getKey() {
    return name;
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    if (binding == this) {
      return true;
    }
    if (binding instanceof GeneratedTypeBinding) {
      return name.equals(((GeneratedTypeBinding) binding).name);
    }
    return false;
  }

  @Override
  public String getBinaryName() {
    return getQualifiedName();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getModifiers() {
    return modifiers;
  }

  public void setModifiers(int newModifiers) {
    modifiers = newModifiers;
  }

  public void addModifiers(int modifiersToAdd) {
    modifiers |= modifiersToAdd;
  }

  @Override
  public String getQualifiedName() {
    if (packageElement != null) {
      return packageElement.getQualifiedName() + "." + name;
    }
    return name;
  }

  @Override
  public boolean isArray() {
    return componentType != null;
  }

  @Override
  public boolean isClass() {
    return !isInterface;
  }

  @Override
  public boolean isInterface() {
    return isInterface;
  }

  @Override
  public ITypeBinding getComponentType() {
    return componentType;
  }

  @Override
  public IVariableBinding[] getDeclaredFields() {
    return fields.toArray(new JdtVariableBinding[0]);
  }

  public void addField(IVariableBinding field) {
    fields.add(field);
  }

  @Override
  public IMethodBinding[] getDeclaredMethods() {
    return methods.toArray(new JdtMethodBinding[0]);
  }

  public void addMethod(IMethodBinding method) {
    methods.add(method);
  }

  @Override
  public int getDeclaredModifiers() {
    return Modifier.PUBLIC;
  }

  @Override
  public int getDimensions() {
    if (componentType != null) {
      return componentType.getDimensions() + 1;
    }
    return 0;
  }

  @Override
  public ITypeBinding getElementType() {
    JdtTypeBinding elementType = componentType;
    while (elementType != null && elementType.isArray()) {
      elementType = BindingConverter.wrapBinding(elementType.getComponentType());
    }
    return elementType;
  }

  @Override
  public IPackageBinding getPackage() {
    return (IPackageBinding) BindingConverter.unwrapElement(packageElement);
  }

  @Override
  public ITypeBinding getSuperclass() {
    return superClass;
  }

  @Override
  public boolean isAssignmentCompatible(ITypeBinding variableType) {
    if (componentType != null && variableType.isArray()) {
      return componentType.isAssignmentCompatible(variableType.getComponentType());
    }
    return isEqualTo(variableType);
  }

  @Override
  public boolean isCastCompatible(ITypeBinding type) {
    return isEqualTo(type);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean isSynthetic() {
    return BindingUtil.isSynthetic(this);
  }
}
