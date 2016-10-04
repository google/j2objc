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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 * Base class for generated Element types.
 *
 * @author Keith Stanger
 */
abstract class GeneratedElement implements Element {

  private final String name;
  private final ElementKind kind;
  private Set<Modifier> modifiers = new HashSet<>();
  private final Element enclosingElement;
  private final List<AnnotationMirror> annotationMirrors = new ArrayList<>();

  protected GeneratedElement(String name, ElementKind kind, Element enclosingElement) {
    this.name = name;
    this.kind = kind;
    this.enclosingElement = enclosingElement;
  }

  public String getName() {
    return name;
  }

  @Override
  public ElementKind getKind() {
    return kind;
  }

  @Override
  public Name getSimpleName() {
    return new Name() {
      @Override
      public int length() {
        return name.length();
      }

      @Override
      public char charAt(int index) {
        return name.charAt(index);
      }

      @Override
      public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
      }

      @Override
      public boolean contentEquals(CharSequence cs) {
        return name.equals(cs);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  @Override
  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  public GeneratedElement addModifiers(Modifier... newModifiers) {
    for (Modifier m : newModifiers) {
      modifiers.add(m);
    }
    return this;
  }

  public GeneratedElement addModifiers(Collection<? extends Modifier> newModifiers) {
    modifiers.addAll(newModifiers);
    return this;
  }

  @Override
  public Element getEnclosingElement() {
    return enclosingElement;
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return annotationMirrors;
  }

  public GeneratedElement addAnnotationMirrors(
      Collection<? extends AnnotationMirror> newAnnotations) {
    annotationMirrors.addAll(newAnnotations);
    return this;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    throw new AssertionError("not implemented");
  }
}
