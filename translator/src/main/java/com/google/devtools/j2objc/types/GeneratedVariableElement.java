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

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.jdt.JdtElements;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Element class for variables and parameters created during translation.
 *
 * @author Nathan Braswell
 */
public class GeneratedVariableElement implements VariableElement {

  private final String name;
  private final TypeMirror type;
  private final boolean isParameter;
  private final boolean isField;
  private Element owner = null;
  private Set<Modifier> modifiers;

  public GeneratedVariableElement(String name, TypeMirror type,
      boolean isField, boolean isParameter, Set<Modifier> modifiers) {
    Preconditions.checkNotNull(name);
    this.name = name;
    this.type = type;
    this.isField = isField;
    this.isParameter = isParameter;
    this.modifiers = modifiers;
  }

  public GeneratedVariableElement(String name, TypeMirror type,
      boolean isField, boolean isParameter) {
    this(name, type, isField, isParameter, Collections.<Modifier>emptySet());
  }

  public void setOwner(Element e) {
    owner = e;
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    throw new AssertionError("not implemented");
  }

  // TODO(user): enable this Override when Java 8 is minimum version.
  //@Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    throw new AssertionError("not implemented");
  }

  @Override
  public TypeMirror asType() {
    return type;
  }

  @Override
  public ElementKind getKind() {
    if (isParameter) {
      return ElementKind.PARAMETER;
    } else if (isField) {
      return ElementKind.FIELD;
    }
    return ElementKind.LOCAL_VARIABLE;
  }

  @Override
  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  @Override
  public Object getConstantValue() {
    return null;
  }

  @Override
  public Name getSimpleName() {
    return JdtElements.getInstance().getName(name);
  }

  @Override
  public Element getEnclosingElement() {
    return owner;
  }
}
