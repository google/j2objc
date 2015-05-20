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

package com.google.devtools.j2objc.types;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Abstract base class for ITypeBinding providing default implementations for
 * most interface methods.
 *
 * @author Keith Stanger
 */
public abstract class AbstractTypeBinding extends AbstractBinding implements ITypeBinding {

  @Override
  public int getKind() {
    return IBinding.TYPE;
  }

  @Override
  public ITypeBinding createArrayType(int dimension) {
    throw new AssertionError("not implemented");
  }

  @Override
  public String getBinaryName() {
    return getQualifiedName();
  }

  @Override
  public ITypeBinding getErasure() {
    return this;
  }

  @Override
  public ITypeBinding[] getInterfaces() {
    return new ITypeBinding[0];
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isClass() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return false;
  }

  @Override
  public boolean isFromSource() {
    return false;
  }

  @Override
  public boolean isGenericType() {
    return false;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public boolean isLocal() {
    return false;
  }

  @Override
  public boolean isMember() {
    return false;
  }

  @Override
  public boolean isNested() {
    return false;
  }

  @Override
  public boolean isNullType() {
    return false;
  }

  @Override
  public boolean isParameterizedType() {
    return false;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public boolean isRawType() {
    return false;
  }

  @Override
  public boolean isTopLevel() {
    return true;
  }

  @Override
  public boolean isTypeVariable() {
    return false;
  }

  @Override
  public boolean isUpperbound() {
    return false;
  }

  @Override
  public boolean isWildcardType() {
    return false;
  }

  @Override
  public ITypeBinding getBound() {
    return null;
  }

  @Override
  public ITypeBinding getGenericTypeOfWildcardType() {
    return null;
  }

  @Override
  public int getRank() {
    return -1;
  }

  @Override
  public ITypeBinding getComponentType() {
    return null;
  }

  @Override
  public IVariableBinding[] getDeclaredFields() {
    return new IVariableBinding[0];
  }

  @Override
  public IMethodBinding[] getDeclaredMethods() {
    return new IMethodBinding[0];
  }

  @Override
  public int getDeclaredModifiers() {
    return 0;
  }

  @Override
  public ITypeBinding[] getDeclaredTypes() {
    return new ITypeBinding[0];
  }

  @Override
  public ITypeBinding getDeclaringClass() {
    return null;
  }

  @Override
  public IMethodBinding getDeclaringMethod() {
    return null;
  }

  @Override
  public int getDimensions() {
    return 0;
  }

  @Override
  public ITypeBinding getElementType() {
    return null;
  }

  @Override
  public IPackageBinding getPackage() {
    return null;
  }

  @Override
  public ITypeBinding getSuperclass() {
    return null;
  }

  @Override
  public ITypeBinding[] getTypeArguments() {
    return new ITypeBinding[0];
  }

  @Override
  public ITypeBinding[] getTypeBounds() {
    return new ITypeBinding[0];
  }

  @Override
  public ITypeBinding getTypeDeclaration() {
    return this;
  }

  @Override
  public ITypeBinding[] getTypeParameters() {
    return new ITypeBinding[0];
  }

  @Override
  public ITypeBinding getWildcard() {
    return null;
  }

  @Override
  public boolean isAnnotation() {
    return false;
  }

  @Override
  public boolean isAnonymous() {
    return false;
  }

  @Override
  public boolean isCapture() {
    return false;
  }

  @Override
  public boolean isCastCompatible(ITypeBinding type) {
    return isEqualTo(type);
  }

  @Override
  public boolean isSubTypeCompatible(ITypeBinding type) {
    throw new AssertionError("not implemented");
  }

  public IMethodBinding getFunctionalInterfaceMethod() {
    return null;
  }

  public IAnnotationBinding[] getTypeAnnotations() {
    return null;
  }
}
