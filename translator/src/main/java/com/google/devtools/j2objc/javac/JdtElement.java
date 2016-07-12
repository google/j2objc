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

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

abstract class JdtElement implements Element {
  protected Name name;
  protected int flags;
  protected JdtBinding binding;

  private static final Map<Integer, Set<Modifier>> modifierSets = new HashMap<>();

  protected JdtElement(IBinding binding, String name, int flags) {
    if (binding instanceof JdtBinding) {
      this.binding = (JdtBinding) binding;
    } else {
      this.binding = BindingConverter.wrapBinding(binding);
    }
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
    return toModifierSet(flags);
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

  private static Set<Modifier> toModifierSet(int modifiers) {
    Set<Modifier> set = modifierSets.get(modifiers);
    if (set == null) {
      set = EnumSet.noneOf(Modifier.class);
      if ((modifiers & java.lang.reflect.Modifier.PUBLIC) > 0) {
        set.add(Modifier.PUBLIC);
      }
      if ((modifiers & java.lang.reflect.Modifier.PRIVATE) > 0) {
        set.add(Modifier.PRIVATE);
      }
      if ((modifiers & java.lang.reflect.Modifier.PROTECTED) > 0) {
        set.add(Modifier.PROTECTED);
      }
      if ((modifiers & java.lang.reflect.Modifier.STATIC) > 0) {
        set.add(Modifier.STATIC);
      }
      if ((modifiers & java.lang.reflect.Modifier.FINAL) > 0) {
        set.add(Modifier.FINAL);
      }
      if ((modifiers & java.lang.reflect.Modifier.SYNCHRONIZED) > 0) {
        set.add(Modifier.SYNCHRONIZED);
      }
      if ((modifiers & java.lang.reflect.Modifier.VOLATILE) > 0) {
        set.add(Modifier.VOLATILE);
      }
      if ((modifiers & java.lang.reflect.Modifier.TRANSIENT) > 0) {
        set.add(Modifier.TRANSIENT);
      }
      if ((modifiers & java.lang.reflect.Modifier.NATIVE) > 0) {
        set.add(Modifier.NATIVE);
      }
      if ((modifiers & java.lang.reflect.Modifier.ABSTRACT) > 0) {
        set.add(Modifier.ABSTRACT);
      }
      if ((modifiers & java.lang.reflect.Modifier.STRICT) > 0) {
        set.add(Modifier.STRICTFP);
      }
      modifierSets.put(modifiers, set);
    }
    return set;
  }
}
