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

import com.google.common.collect.Lists;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Element;

/**
 * Wrapper class around IBinding, allowing getKind() to be redefined by
 * TypeMirror.
 */
public abstract class JdtBinding implements IBinding {
  protected final IBinding binding;
  private final List<IAnnotationBinding> annotations;

  JdtBinding(IBinding binding) {
    this.binding = binding;
    if (binding != null) {
      JdtAnnotationBinding[] jdtAnnotations =
          BindingConverter.wrapBindings(binding.getAnnotations());
      annotations = Lists.newArrayList((IAnnotationBinding[]) jdtAnnotations);
    } else {
      annotations = new ArrayList<>();
    }
  }

  public IAnnotationBinding[] getAnnotations() {
    return annotations.toArray(new JdtAnnotationBinding[annotations.size()]);
  }

  public void addAnnotations(IBinding binding) {
    annotations.addAll(Arrays.asList(binding.getAnnotations()));
  }

  public void addAnnotations(Element element) {
    annotations.addAll(Arrays.asList(BindingConverter.unwrapElement(element).getAnnotations()));
  }

  @Deprecated
  public IJavaElement getJavaElement() {
    throw new AssertionError("not implemented");
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
    return binding != null ? binding.getModifiers() : 0;
  }

  public String getName() {
    return binding.getName();
  }

  public boolean isDeprecated() {
    return binding != null ? binding.isDeprecated() : false;
  }

  public boolean isEqualTo(IBinding other) {
    IBinding otherBinding = other instanceof JdtBinding ? ((JdtBinding) other).binding : other;
    return binding.isEqualTo(otherBinding);
  }

  public boolean isRecovered() {
    return binding != null ? binding.isRecovered() : false;
  }

  public boolean isSynthetic() {
    return binding != null ? binding.isSynthetic() : true;
  }

  @Override
  public boolean equals(Object obj) {
    return binding != null
        ? obj instanceof JdtBinding && this.binding.equals(((JdtBinding) obj).binding)
        : super.equals(obj);
  }

  @Override
  public int hashCode() {
    return binding != null ? binding.hashCode() : super.hashCode();
  }

  public String toString() {
    return binding != null ? binding.toString() : super.toString();
  }
}
