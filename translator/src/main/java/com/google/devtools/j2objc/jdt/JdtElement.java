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

import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;

abstract class JdtElement implements Element {
  protected Name name;
  protected int flags;
  protected IBinding binding;

  protected JdtElement(IBinding binding, String name, int flags) {
    this.binding = binding;
    this.name = BindingConverter.getName(name);
    this.flags = flags;
  }

  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    throw new AssertionError("not implemented");
  }

  @Override
  public abstract TypeMirror asType();

  @Override
  public Set<Modifier> getModifiers() {
    return ElementUtil.toModifierSet(flags);
  }

  @Override
  public Name getSimpleName() {
    return name;
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    List<AnnotationMirror> mirrors = new ArrayList<>();
    for (IAnnotationBinding annotation : binding.getAnnotations()) {
      mirrors.add(new JdtAnnotationMirror(annotation));
    }
    return mirrors;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    throw new AssertionError("not implemented");
  }

  @Override
  public int hashCode() {
    return binding.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof JdtElement) {
      JdtElement other = (JdtElement) obj;
      return binding == other.binding || binding.equals(other.binding);
    }
    return false;
  }

  public boolean isSynthetic() {
    return binding.isSynthetic();
  }
}
