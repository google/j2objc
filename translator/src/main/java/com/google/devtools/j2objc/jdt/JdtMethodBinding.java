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
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Wrapper class around IMethodBinding.
 */
public class JdtMethodBinding extends JdtBinding implements IMethodBinding {
  private JdtTypeBinding declaredReceiverType;
  private JdtTypeBinding declaringClass;
  private JdtTypeBinding[] exceptionTypes;
  private JdtMethodBinding methodDeclaration;
  private JdtAnnotationBinding[] parameterAnnotations;
  private JdtTypeBinding[] parameterTypes;
  private JdtTypeBinding returnType;
  private JdtTypeBinding[] typeArguments;
  private JdtTypeBinding[] typeParameters;
  private boolean initialized = false;

  protected JdtMethodBinding(IMethodBinding binding) {
    super(binding);
  }

  private void maybeInitialize() {
    if (!initialized) {
      IMethodBinding methodBinding = (IMethodBinding) binding;
      this.declaredReceiverType =
          BindingConverter.wrapBinding(methodBinding.getDeclaredReceiverType());
      this.declaringClass = BindingConverter.wrapBinding(methodBinding.getDeclaringClass());
      this.methodDeclaration = BindingConverter.wrapBinding(methodBinding.getMethodDeclaration());
      this.returnType = BindingConverter.wrapBinding(methodBinding.getReturnType());
      initialized = true;
    }
  }

  public ITypeBinding getDeclaredReceiverType() {
    maybeInitialize();
    return declaredReceiverType;
  }

  public ITypeBinding getDeclaringClass() {
    maybeInitialize();
    return declaringClass;
  }

  public IBinding getDeclaringMember() {
    return null;
  }

  public Object getDefaultValue() {
    return ((IMethodBinding) binding).getDefaultValue();
  }

  public ITypeBinding[] getExceptionTypes() {
    if (exceptionTypes == null) {
      exceptionTypes =
          BindingConverter.wrapBindings(((IMethodBinding) binding).getExceptionTypes());
    }
    return exceptionTypes;
  }

  public IMethodBinding getMethodDeclaration() {
    maybeInitialize();
    return methodDeclaration;
  }

  public IAnnotationBinding[] getParameterAnnotations(int i) {
    if (parameterAnnotations == null) {
      parameterAnnotations =
          BindingConverter.wrapBindings(((IMethodBinding) binding).getParameterAnnotations(i));
    }
    return parameterAnnotations;
  }

  public ITypeBinding[] getParameterTypes() {
    if (parameterTypes == null) {
      parameterTypes =
          BindingConverter.wrapBindings(((IMethodBinding) binding).getParameterTypes());
    }
    return parameterTypes;
  }

  public ITypeBinding getReturnType() {
    maybeInitialize();
    return returnType;
  }

  public ITypeBinding[] getTypeArguments() {
    if (typeArguments == null) {
      typeArguments = BindingConverter.wrapBindings(((IMethodBinding) binding).getTypeArguments());
    }
    return typeArguments;
  }

  @Override
  public ITypeBinding[] getTypeParameters() {
    if (typeParameters == null) {
      typeParameters =
          BindingConverter.wrapBindings(((IMethodBinding) binding).getTypeParameters());
    }
    return typeParameters;
  }

  public boolean isAnnotationMember() {
    return ((IMethodBinding) binding).isAnnotationMember();
  }

  public boolean isConstructor() {
    return ((IMethodBinding) binding).isConstructor();
  }

  public boolean isDefaultConstructor() {
    return ((IMethodBinding) binding).isDefaultConstructor();
  }

  public boolean isGenericMethod() {
    return ((IMethodBinding) binding).isGenericMethod();
  }

  public boolean isParameterizedMethod() {
    return ((IMethodBinding) binding).isParameterizedMethod();
  }

  public boolean isRawMethod() {
    return ((IMethodBinding) binding).isRawMethod();
  }

  public boolean isSubsignature(IMethodBinding other) {
    IMethodBinding otherBinding = other instanceof JdtMethodBinding
        ? (IMethodBinding) ((JdtMethodBinding) other).binding : other;
    return ((IMethodBinding) binding).isSubsignature(otherBinding);
  }

  public boolean isVarargs() {
    return ((IMethodBinding) binding).isVarargs();
  }

  public boolean overrides(IMethodBinding other) {
    IMethodBinding otherBinding = other instanceof JdtMethodBinding
        ? (IMethodBinding) ((JdtMethodBinding) other).binding : other;
    return ((IMethodBinding) binding).overrides(otherBinding);
  }

}
