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

package com.google.devtools.j2objc.ast;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Base node class for a name.
 */
public abstract class Name extends Expression {

  private Element element;

  public Name() {}

  public Name(Name other) {
    super(other);
    element = other.getElement();
  }

  public Name(Element element) {
    this.element = element;
  }

  public static Name newName(Name qualifier, Element element, TypeMirror type) {
    return qualifier == null ? new SimpleName(element, type)
        : new QualifiedName(element, type, qualifier);
  }

  public static Name newName(Name qualifier, Element element) {
    return newName(qualifier, element, element.asType());
  }

  public abstract String getFullyQualifiedName();

  public Element getElement() {
    return element;
  }

  public Name setElement(Element newElement) {
    element = newElement;
    return this;
  }

  public boolean isQualifiedName() {
    return false;
  }

  @Override
  public abstract Name copy();
}
