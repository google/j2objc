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

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

class JdtTypeVariable extends JdtTypeMirror implements TypeVariable {

  JdtTypeVariable(JdtTypeBinding binding) {
    super(binding);
    assert binding.isTypeVariable();
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.TYPEVAR;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitTypeVariable(this, p);
  }

  @Override
  public Element asElement() {
    // TODO(tball): implement.
    return null;
  }

  @Override
  public TypeMirror getUpperBound() {
    ITypeBinding bound = ((ITypeBinding) binding).getBound();
    return bound.isUpperbound() ? BindingConverter.getType(bound) : BindingConverter.NULL_TYPE;
  }

  @Override
  public TypeMirror getLowerBound() {
    ITypeBinding bound = ((ITypeBinding) binding).getBound();
    return bound != null ? BindingConverter.getType(bound) : BindingConverter.NULL_TYPE;
  }
}
