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

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

class JdtTypeParameterElement extends JdtElement implements TypeParameterElement {

  JdtTypeParameterElement(ITypeBinding binding) {
    super(binding, binding.getName(), 0);
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.TYPE_PARAMETER;
  }

  @Override
  public Element getGenericElement() {
    return BindingConverter.getElement(((ITypeBinding) binding).getErasure());
  }

  @Override
  public Element getEnclosingElement() {
    return getGenericElement();
  }

  @Override
  public TypeMirror asType() {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<? extends TypeMirror> getBounds() {
    List<TypeMirror> bounds = new ArrayList<>();
    for (ITypeBinding bound : ((ITypeBinding) binding).getTypeBounds()) {
      bounds.add(BindingConverter.getType(bound));
    }
    return bounds;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitTypeParameter(this, p);
  }
}
