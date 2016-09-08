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

import com.google.devtools.j2objc.jdt.JdtElements;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 * Base class for generated Element types.
 *
 * @author Keith Stanger
 */
public abstract class GeneratedElement implements Element {

  private final String name;
  private final ElementKind kind;
  private Set<Modifier> modifiers = new HashSet<>();
  private final Element enclosingElement;

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
    return JdtElements.getInstance().getName(name);
  }

  @Override
  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  @Override
  public Element getEnclosingElement() {
    return enclosingElement;
  }
}
