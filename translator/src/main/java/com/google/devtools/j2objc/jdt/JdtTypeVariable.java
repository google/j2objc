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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;

class JdtTypeVariable extends JdtTypeMirror implements TypeVariable {

  private final TypeMirror upperBound;

  private JdtTypeVariable(ITypeBinding binding, TypeMirror upperBound) {
    super(binding);
    this.upperBound = upperBound;
  }

  static JdtTypeVariable fromTypeVariable(ITypeBinding binding) {
    return new JdtTypeVariable(
        binding, createUpperBound(binding, Arrays.asList(binding.getTypeBounds())));
  }

  static TypeMirror createUpperBound(ITypeBinding binding, List<ITypeBinding> bounds) {
    if (bounds.isEmpty()) {
      return BindingConverter.getType(binding.getSuperclass());  // java.lang.Object or null.
    } else if (bounds.size() == 1) {
      return BindingConverter.getType(bounds.get(0));
    }
    List<TypeMirror> upperBounds = new ArrayList<>();
    for (ITypeBinding bound : bounds) {
      upperBounds.add(BindingConverter.getType(bound));
    }
    return new JdtIntersectionType(null, upperBounds);
  }

  static JdtTypeVariable fromCapture(ITypeBinding binding) {
    List<ITypeBinding> bounds = new ArrayList<>();
    ITypeBinding superclass = binding.getSuperclass();
    if (superclass != null) {
      bounds.add(superclass);
    }
    for (ITypeBinding intrface : binding.getInterfaces()) {
      bounds.add(intrface);
    }
    return new JdtTypeVariable(binding, createUpperBound(binding, bounds));
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
    return BindingConverter.getElement(binding);
  }

  @Override
  public TypeMirror getUpperBound() {
    return upperBound;
  }

  @Override
  public TypeMirror getLowerBound() {
    return null;  // getLowerBound isn't used by J2ObjC.
  }
}
