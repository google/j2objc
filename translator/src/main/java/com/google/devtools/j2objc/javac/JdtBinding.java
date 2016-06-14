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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IBinding;

/**
 * Wrapper class around IBinding, allowing getKind() to be redefined by
 * TypeMirror.
 */
public abstract class JdtBinding implements IBinding {
  protected final IBinding binding;
  private JdtAnnotationBinding[] annotations;

  JdtBinding(IBinding binding) {
    this.binding = binding;
  }

  public JdtAnnotationBinding[] getAnnotations() {
    if (annotations == null) {
      annotations = BindingConverter.wrapBindings(binding.getAnnotations());
    }
    return annotations;
  }

  @Deprecated
  public IJavaElement getJavaElement() {
    return binding.getJavaElement();
  }

  public String getKey() {
    return binding.getKey();
  }

  @Deprecated
  public int getKind() {
    return binding.getKind();
  }

  // Replace getKind() with isTypeKind(), because there is a
  // conflicting TypeMirror.getKind(). IBinding.getKind() is
  // only used in this project to test whether the kind is TYPE.
  public boolean isTypeKind() {
    return binding.getKind() == IBinding.TYPE;
  }

  public int getModifiers() {
    return binding.getModifiers();
  }

  public String getName() {
    return binding.getName();
  }

  public boolean isDeprecated() {
    return binding.isDeprecated();
  }

  public boolean isEqualTo(IBinding arg0) {
    return binding.isEqualTo(arg0);
  }

  public boolean isRecovered() {
    return binding.isRecovered();
  }

  public boolean isSynthetic() {
    return binding.isSynthetic();
  }

  public boolean equals(Object obj) {
    return obj instanceof JdtBinding && this.binding.equals(((JdtBinding) obj).binding);
  }

  public int hashCode() {
    return binding.hashCode();
  }
}
