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
import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Element class for variables and parameters created during translation.
 *
 * @author Nathan Braswell
 */
public class GeneratedVariableElement extends GeneratedElement implements VariableElement {

  private final TypeMirror type;
  private boolean nonnull = false;

  public GeneratedVariableElement(
      String name, TypeMirror type, ElementKind kind, Element enclosingElement) {
    super(Preconditions.checkNotNull(name), checkElementKind(kind), enclosingElement);
    this.type = type;
  }

  private static ElementKind checkElementKind(ElementKind kind) {
    Preconditions.checkArgument(
        kind == ElementKind.FIELD || kind == ElementKind.LOCAL_VARIABLE
        || kind == ElementKind.PARAMETER);
    return kind;
  }

  @Override
  public TypeMirror asType() {
    return type;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  @Override
  public Object getConstantValue() {
    return null;
  }

  public boolean isNonnull() {
    return nonnull;
  }

  public GeneratedVariableElement setNonnull(boolean value) {
    nonnull = value;
    return this;
  }

  @Override
  public GeneratedVariableElement addAnnotationMirrors(
      Collection<? extends AnnotationMirror> newAnnotations) {
    return (GeneratedVariableElement) super.addAnnotationMirrors(newAnnotations);
  }

  @Override
  public GeneratedVariableElement addModifiers(Modifier... modifiers) {
    return (GeneratedVariableElement) super.addModifiers(modifiers);
  }
}
