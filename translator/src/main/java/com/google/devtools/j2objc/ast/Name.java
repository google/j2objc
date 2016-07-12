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

import com.google.devtools.j2objc.javac.BindingConverter;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Base node class for a name.
 */
public abstract class Name extends Expression {

  private Element element;

  public Name(org.eclipse.jdt.core.dom.Name jdtNode) {
    super(jdtNode);
    IBinding binding = BindingConverter.wrapBinding(jdtNode.resolveBinding());
    element = BindingConverter.getElement(binding);
  }

  public Name(Name other) {
    super(other);
    element = other.getElement();
  }

  public Name(IBinding binding) {
    this.element = BindingConverter.getElement(binding);
  }

  public Name(Element element) {
    this.element = element;
  }

  public static Name newName(Name qualifier, IBinding binding) {
    return qualifier == null ? new SimpleName(binding) : new QualifiedName(binding, qualifier);
  }

  public static Name newName(List<? extends IBinding> path) {
    Name name = null;
    for (IBinding binding : path) {
      name = newName(name, binding);
    }
    return name;
  }

  public abstract String getFullyQualifiedName();

  public Element getElement() {
    return element;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return BindingUtil.toTypeBinding(BindingConverter.unwrapElement(element));
  }

  @Override
  public TypeMirror getTypeMirror() {
    return element.asType();
  }

  public boolean isQualifiedName() {
    return false;
  }

  @Override
  public abstract Name copy();
}
