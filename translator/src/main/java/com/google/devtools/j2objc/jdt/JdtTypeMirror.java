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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

abstract class JdtTypeMirror implements TypeMirror {
  protected JdtBinding binding;

  JdtTypeMirror(JdtBinding binding) {
    this.binding = binding;
  }

  // TODO(tball): enable when Java 8 is minimum version.
  // @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    List<AnnotationMirror> mirrors = new ArrayList<>();
    for (IAnnotationBinding annotation : binding.getAnnotations()) {
      mirrors.add(new JdtAnnotationMirror(annotation));
    }
    return mirrors;
  }

  public boolean bindingsEqual(JdtTypeMirror other) {
      if (binding == other.binding) {
        return true;
      }
      if (binding == null || other.binding == null) {
        return false;
      }
      return binding.equals(other.binding);
  }

  // TypeMirror's toString should return a string reasonable
  // for representing the type in code, if possible.
  @Override
  public String toString() {
    return binding.getName();
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    throw new AssertionError("not implemented");
  }

  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    throw new AssertionError("not implemented");
  }
}
