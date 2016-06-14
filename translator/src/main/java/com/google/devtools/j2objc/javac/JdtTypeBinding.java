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

package com.google.devtools.j2objc.javac;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Wrapper class around ITypeBinding.
 */
public class JdtTypeBinding extends JdtBinding implements ITypeBinding {
  private JdtTypeBinding bound;
  private JdtTypeBinding componentType;
  private JdtVariableBinding[] declaredFields;
  private JdtMethodBinding[] declaredMethods;
  private JdtTypeBinding[] declaredTypes;
  private JdtTypeBinding declaringClass;
  private JdtMethodBinding declaringMethod;
  private JdtTypeBinding elementType;
  private JdtTypeBinding erasure;
  private JdtMethodBinding functionalInterfaceMethod;
  private JdtTypeBinding genericWildcardType;
  private JdtTypeBinding[] interfaces;
  private JdtPackageBinding pkg;
  private JdtTypeBinding superclass;
  private JdtAnnotationBinding[] typeAnnotations;
  private JdtTypeBinding[] typeArguments;
  private JdtTypeBinding[] typeBounds;
  private JdtTypeBinding typeDeclaration;
  private JdtTypeBinding[] typeParameters;
  private JdtTypeBinding wildcard;

  JdtTypeBinding(ITypeBinding binding) {
    super(binding);
    this.bound = BindingConverter.wrapBinding(binding.getBound());
    this.componentType = BindingConverter.wrapBinding(binding.getComponentType());
    this.declaringClass = BindingConverter.wrapBinding(binding.getDeclaringClass());
    this.declaringMethod = BindingConverter.wrapBinding(binding.getDeclaringMethod());
    this.elementType = BindingConverter.wrapBinding(binding.getElementType());
    ITypeBinding erase = binding.getErasure();
    this.erasure = erase != binding ? BindingConverter.wrapBinding(erase) : this;
    this.functionalInterfaceMethod = BindingConverter
        .wrapBinding(binding.getFunctionalInterfaceMethod());
    this.genericWildcardType = BindingConverter.wrapBinding(binding.getGenericTypeOfWildcardType());
    this.pkg = BindingConverter.wrapBinding(binding.getPackage());
    this.superclass = BindingConverter.wrapBinding(binding.getSuperclass());
    ITypeBinding decl = binding.getTypeDeclaration();
    this.typeDeclaration = decl != binding ? BindingConverter.wrapBinding(decl) : this;
    this.wildcard = BindingConverter.wrapBinding(binding.getWildcard());
  }

  public JdtTypeBinding createArrayType(int arg0) {
    return new JdtTypeBinding(((ITypeBinding) binding).createArrayType(arg0));
  }

  public String getBinaryName() {
    return ((ITypeBinding) binding).getBinaryName();
  }

  public JdtTypeBinding getBound() {
    return bound;
  }

  public JdtTypeBinding getComponentType() {
    return componentType;
  }

  public JdtVariableBinding[] getDeclaredFields() {
    if (declaredFields == null) {
      declaredFields = BindingConverter.wrapBindings(((ITypeBinding) binding).getDeclaredFields());
    }
    return declaredFields;
  }

  public JdtMethodBinding[] getDeclaredMethods() {
    if (declaredMethods == null) {
      declaredMethods =
          BindingConverter.wrapBindings(((ITypeBinding) binding).getDeclaredMethods());
    }
    return declaredMethods;
  }

  @Deprecated
  public int getDeclaredModifiers() {
    return ((ITypeBinding) binding).getDeclaredModifiers();
  }

  public JdtTypeBinding[] getDeclaredTypes() {
    if (declaredTypes == null) {
      declaredTypes = BindingConverter.wrapBindings(((ITypeBinding) binding).getDeclaredTypes());
    }
    return declaredTypes;
  }

  public JdtTypeBinding getDeclaringClass() {
    return declaringClass;
  }

  public JdtBinding getDeclaringMember() {
    return null;
  }

  public JdtMethodBinding getDeclaringMethod() {
    return declaringMethod;
  }

  public int getDimensions() {
    return ((ITypeBinding) binding).getDimensions();
  }

  public JdtTypeBinding getElementType() {
    return elementType;
  }

  public JdtTypeBinding getErasure() {
    return erasure;
  }

  public JdtMethodBinding getFunctionalInterfaceMethod() {
    return functionalInterfaceMethod;
  }

  public JdtTypeBinding getGenericTypeOfWildcardType() {
    return genericWildcardType;
  }

  public JdtTypeBinding[] getInterfaces() {
    if (interfaces == null) {
      interfaces = BindingConverter.wrapBindings(((ITypeBinding) binding).getInterfaces());
    }
    return interfaces;
  }

  public JdtPackageBinding getPackage() {
    return pkg;
  }

  public String getQualifiedName() {
    return ((ITypeBinding) binding).getQualifiedName();
  }

  public int getRank() {
    return ((ITypeBinding) binding).getRank();
  }

  public JdtTypeBinding getSuperclass() {
    return superclass;
  }

  public JdtAnnotationBinding[] getTypeAnnotations() {
    if (typeAnnotations == null) {
      typeAnnotations =
          BindingConverter.wrapBindings(((ITypeBinding) binding).getTypeAnnotations());
    }
    return typeAnnotations;
  }

  public JdtTypeBinding[] getTypeArguments() {
    if (typeArguments == null) {
      typeArguments = BindingConverter.wrapBindings(((ITypeBinding) binding).getTypeArguments());
    }
    return typeArguments;
  }

  public JdtTypeBinding[] getTypeBounds() {
    if (typeBounds == null) {
      typeBounds = BindingConverter.wrapBindings(((ITypeBinding) binding).getTypeBounds());
    }
    return typeBounds;
  }

  public JdtTypeBinding getTypeDeclaration() {
    return typeDeclaration;
  }

  public JdtTypeBinding[] getTypeParameters() {
    if (typeParameters == null) {
      typeParameters = BindingConverter.wrapBindings(((ITypeBinding) binding).getTypeParameters());
    }
    return typeParameters;
  }

  public JdtTypeBinding getWildcard() {
    return wildcard;
  }

  public boolean isAnnotation() {
    return ((ITypeBinding) binding).isAnnotation();
  }

  public boolean isAnonymous() {
    return ((ITypeBinding) binding).isAnonymous();
  }

  public boolean isArray() {
    return ((ITypeBinding) binding).isArray();
  }

  @Deprecated
  public boolean isAssignmentCompatible(ITypeBinding arg0) {
    return ((ITypeBinding) binding).isAssignmentCompatible(arg0);
  }

  public boolean isAssignmentCompatible(JdtTypeBinding arg0) {
    return ((ITypeBinding) binding).isAssignmentCompatible((ITypeBinding) arg0.binding);
  }

  public boolean isCapture() {
    return ((ITypeBinding) binding).isCapture();
  }

  @Deprecated
  public boolean isCastCompatible(ITypeBinding arg0) {
    return ((ITypeBinding) binding).isCastCompatible(arg0);
  }

  public boolean isCastCompatible(JdtTypeBinding arg0) {
    return ((ITypeBinding) binding).isCastCompatible((ITypeBinding) arg0.binding);
  }

  public boolean isClass() {
    return ((ITypeBinding) binding).isClass();
  }

  public boolean isEnum() {
    return ((ITypeBinding) binding).isEnum();
  }

  public boolean isFromSource() {
    return ((ITypeBinding) binding).isFromSource();
  }

  public boolean isGenericType() {
    return ((ITypeBinding) binding).isGenericType();
  }

  public boolean isInterface() {
    return ((ITypeBinding) binding).isInterface();
  }

  public boolean isLocal() {
    return ((ITypeBinding) binding).isLocal();
  }

  public boolean isMember() {
    return ((ITypeBinding) binding).isMember();
  }

  public boolean isNested() {
    return ((ITypeBinding) binding).isNested();
  }

  public boolean isNullType() {
    return ((ITypeBinding) binding).isNullType();
  }

  public boolean isParameterizedType() {
    return ((ITypeBinding) binding).isParameterizedType();
  }

  public boolean isPrimitive() {
    return ((ITypeBinding) binding).isPrimitive();
  }

  public boolean isRawType() {
    return ((ITypeBinding) binding).isRawType();
  }

  @Deprecated
  public boolean isSubTypeCompatible(ITypeBinding arg0) {
    return ((ITypeBinding) binding).isSubTypeCompatible(arg0);
  }

  public boolean isSubTypeCompatible(JdtTypeBinding arg0) {
    return ((ITypeBinding) binding).isSubTypeCompatible((ITypeBinding) arg0.binding);
  }

  public boolean isTopLevel() {
    return ((ITypeBinding) binding).isTopLevel();
  }

  public boolean isTypeVariable() {
    return ((ITypeBinding) binding).isTypeVariable();
  }

  public boolean isUpperbound() {
    return ((ITypeBinding) binding).isUpperbound();
  }

  public boolean isWildcardType() {
    return ((ITypeBinding) binding).isWildcardType();
  }
}
