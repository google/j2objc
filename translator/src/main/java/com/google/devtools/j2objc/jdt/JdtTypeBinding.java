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

package com.google.devtools.j2objc.jdt;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

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
  protected boolean initialized = false;

  protected JdtTypeBinding(ITypeBinding binding) {
    super(binding);
  }

  private void maybeInitialize() {
    if (!initialized) {
      ITypeBinding typeBinding = (ITypeBinding) binding;
      this.bound = BindingConverter.wrapBinding(typeBinding.getBound());
      this.componentType = BindingConverter.wrapBinding(typeBinding.getComponentType());
      this.declaringClass = BindingConverter.wrapBinding(typeBinding.getDeclaringClass());
      this.declaringMethod = BindingConverter.wrapBinding(typeBinding.getDeclaringMethod());
      this.elementType = BindingConverter.wrapBinding(typeBinding.getElementType());
      this.erasure = BindingConverter.wrapBinding(typeBinding.getErasure());
      this.functionalInterfaceMethod =
          BindingConverter.wrapBinding(typeBinding.getFunctionalInterfaceMethod());
      this.genericWildcardType =
          BindingConverter.wrapBinding(typeBinding.getGenericTypeOfWildcardType());
      this.pkg = BindingConverter.wrapBinding(typeBinding.getPackage());
      this.superclass = BindingConverter.wrapBinding(typeBinding.getSuperclass());
      this.typeDeclaration = BindingConverter.wrapBinding(typeBinding.getTypeDeclaration());
      this.wildcard = BindingConverter.wrapBinding(typeBinding.getWildcard());
      initialized = true;
    }
  }

  public ITypeBinding createArrayType(int dimension) {
    return new JdtTypeBinding(((ITypeBinding) binding).createArrayType(dimension));
  }

  public String getBinaryName() {
    return ((ITypeBinding) binding).getBinaryName();
  }

  public ITypeBinding getBound() {
    maybeInitialize();
    return bound;
  }

  public ITypeBinding getComponentType() {
    maybeInitialize();
    return componentType;
  }

  public IVariableBinding[] getDeclaredFields() {
    if (declaredFields == null) {
      declaredFields = BindingConverter.wrapBindings(((ITypeBinding) binding).getDeclaredFields());
    }
    return declaredFields;
  }

  public IMethodBinding[] getDeclaredMethods() {
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

  public ITypeBinding[] getDeclaredTypes() {
    if (declaredTypes == null) {
      declaredTypes = BindingConverter.wrapBindings(((ITypeBinding) binding).getDeclaredTypes());
    }
    return declaredTypes;
  }

  public ITypeBinding getDeclaringClass() {
    maybeInitialize();
    return declaringClass;
  }

  public IBinding getDeclaringMember() {
    return null;
  }

  public IMethodBinding getDeclaringMethod() {
    maybeInitialize();
    return declaringMethod;
  }

  public int getDimensions() {
    return ((ITypeBinding) binding).getDimensions();
  }

  public ITypeBinding getElementType() {
    maybeInitialize();
    return elementType;
  }

  public ITypeBinding getErasure() {
    maybeInitialize();
    return erasure;
  }

  public IMethodBinding getFunctionalInterfaceMethod() {
    maybeInitialize();
    return functionalInterfaceMethod;
  }

  public ITypeBinding getGenericTypeOfWildcardType() {
    maybeInitialize();
    return genericWildcardType;
  }

  public ITypeBinding[] getInterfaces() {
    if (interfaces == null) {
      interfaces = BindingConverter.wrapBindings(((ITypeBinding) binding).getInterfaces());
    }
    return interfaces;
  }

  public IPackageBinding getPackage() {
    maybeInitialize();
    return pkg;
  }

  public String getQualifiedName() {
    return ((ITypeBinding) binding).getQualifiedName();
  }

  public int getRank() {
    return ((ITypeBinding) binding).getRank();
  }

  public ITypeBinding getSuperclass() {
    maybeInitialize();
    return superclass;
  }

  public IAnnotationBinding[] getTypeAnnotations() {
    if (typeAnnotations == null) {
      typeAnnotations =
          BindingConverter.wrapBindings(((ITypeBinding) binding).getTypeAnnotations());
    }
    return typeAnnotations;
  }

  public ITypeBinding[] getTypeArguments() {
    if (typeArguments == null) {
      typeArguments = BindingConverter.wrapBindings(((ITypeBinding) binding).getTypeArguments());
    }
    return typeArguments;
  }

  public ITypeBinding[] getTypeBounds() {
    if (typeBounds == null) {
      typeBounds = BindingConverter.wrapBindings(((ITypeBinding) binding).getTypeBounds());
    }
    return typeBounds;
  }

  public ITypeBinding getTypeDeclaration() {
    maybeInitialize();
    return typeDeclaration;
  }

  public ITypeBinding[] getTypeParameters() {
    if (typeParameters == null) {
      typeParameters = BindingConverter.wrapBindings(((ITypeBinding) binding).getTypeParameters());
    }
    return typeParameters;
  }

  public ITypeBinding getWildcard() {
    maybeInitialize();
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
  public boolean isAssignmentCompatible(ITypeBinding varType) {
    ITypeBinding otherVarType = varType instanceof JdtTypeBinding
        ? (ITypeBinding) ((JdtTypeBinding) varType).binding : varType;
    return ((ITypeBinding) binding).isAssignmentCompatible(otherVarType);
  }

  public boolean isAssignmentCompatible(JdtTypeBinding varType) {
    return ((ITypeBinding) binding).isAssignmentCompatible((ITypeBinding) varType.binding);
  }

  public boolean isCapture() {
    return ((ITypeBinding) binding).isCapture();
  }

  @Deprecated
  public boolean isCastCompatible(ITypeBinding type) {
    ITypeBinding otherType = type instanceof JdtTypeBinding
        ? (ITypeBinding) ((JdtTypeBinding) type).binding : type;
    return ((ITypeBinding) binding).isCastCompatible(otherType);
  }

  public boolean isCastCompatible(JdtTypeBinding type) {
    return ((ITypeBinding) binding).isCastCompatible((ITypeBinding) type.binding);
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
  public boolean isSubTypeCompatible(ITypeBinding type) {
    ITypeBinding otherType = type instanceof JdtTypeBinding
        ? (ITypeBinding) ((JdtTypeBinding) type).binding : type;
    return ((ITypeBinding) binding).isSubTypeCompatible(otherType);
  }

  public boolean isSubTypeCompatible(JdtTypeBinding type) {
    return ((ITypeBinding) binding).isSubTypeCompatible((ITypeBinding) type.binding);
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
