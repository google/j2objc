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

import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * An associated IMethodBinding implementation for a GeneratedExecutableElement.
 */
class GeneratedMethodBinding implements IMethodBinding {
  private final GeneratedExecutableElement element;

  public GeneratedMethodBinding(GeneratedExecutableElement element) {
    this.element = element;
  }

  public ExecutableElement asElement() {
    return element;
  }

  @Override
  public int getKind() {
    return IBinding.METHOD;
  }

  @Override
  public String getKey() {
    return getName();
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    return binding == this;
  }

  @Override
  public boolean isSynthetic() {
    return element.isSynthetic();
  }

  @Override
  public int getModifiers() {
    return ElementUtil.fromModifierSet(element.getModifiers())
        | (isSynthetic() ? BindingUtil.ACC_SYNTHETIC : 0);
  }

  @Override
  public ITypeBinding getDeclaredReceiverType() {
    return null;
  }

  @Override
  public ITypeBinding getDeclaringClass() {
    return BindingConverter.unwrapTypeElement(
        ElementUtil.getDeclaringClass(element));
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public ITypeBinding[] getExceptionTypes() {
    return new ITypeBinding[0];
  }

  @Override
  public IMethodBinding getMethodDeclaration() {
    return this;
  }

  @Override
  public String getName() {
    return element.getSimpleName().toString();
  }

  @Override
  public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
    return new IAnnotationBinding[0];
  }

  @Override
  public ITypeBinding[] getParameterTypes() {
    List<VariableElement> parameters = element.getParameters();
    ITypeBinding[] paramTypes = new ITypeBinding[parameters.size()];
    for (int i = 0; i < parameters.size(); i++) {
      paramTypes[i] = BindingConverter.unwrapTypeMirrorIntoTypeBinding(
          parameters.get(i).asType());
    }
    return paramTypes;
  }

  @Override
  public ITypeBinding getReturnType() {
    return BindingConverter.unwrapTypeMirrorIntoTypeBinding(element.getReturnType());
  }

  @Override
  public ITypeBinding[] getTypeArguments() {
    return new ITypeBinding[0];
  }

  @Override
  public ITypeBinding[] getTypeParameters() {
    return new ITypeBinding[0];
  }

  @Override
  public boolean isAnnotationMember() {
    return false;
  }

  @Override
  public boolean isConstructor() {
    return element.getKind() == ElementKind.CONSTRUCTOR;
  }

  @Override
  public boolean isDefaultConstructor() {
    return false;
  }

  @Override
  public boolean isGenericMethod() {
    return false;
  }

  @Override
  public boolean isParameterizedMethod() {
    return false;
  }

  @Override
  public boolean isRawMethod() {
    return false;
  }

  @Override
  public boolean isSubsignature(IMethodBinding otherMethod) {
    return false;
  }

  @Override
  public boolean isVarargs() {
    return element.isVarArgs();
  }

  @Override
  public boolean overrides(IMethodBinding method) {
    return false;
  }

  @Override
  public IJavaElement getJavaElement() {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isRecovered() {
    return false;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  public IAnnotationBinding[] getAnnotations() {
    return new IAnnotationBinding[0];
  }

  // Internal JDT has a different version than external.
  @SuppressWarnings("MissingOverride")
  public IBinding getDeclaringMember() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GeneratedMethodBinding
        && element.equals(((GeneratedMethodBinding) obj).element);
  }

  @Override
  public int hashCode() {
    return element.hashCode();
  }
}
