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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

class JdtTypeParameterElement extends JdtElement implements TypeParameterElement {

  private final Element enclosingElement;

  JdtTypeParameterElement(ITypeBinding binding) {
    super(binding, binding.isCapture() ? "<captured wildcard>" : binding.getName(), 0);
    enclosingElement = resolveEnclosingElement(binding);
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
    return enclosingElement;
  }

  private Element resolveEnclosingElement(ITypeBinding binding) {
    if (binding.isCapture()) {
      return new DummyCaptureEncloser();
    }
    IMethodBinding declaringMethod = binding.getDeclaringMethod();
    if (declaringMethod != null) {
      return BindingConverter.getElement(declaringMethod);
    }
    return BindingConverter.getElement(binding.getDeclaringClass());
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

  private class DummyCaptureEncloser implements Element {

    @Override
    public ElementKind getKind() {
      return ElementKind.OTHER;
    }

    @Override
    public Name getSimpleName() {
      return BindingConverter.getName("");
    }

    @Override
    public Set<Modifier> getModifiers() {
      return Collections.emptySet();
    }

    @Override
    public TypeMirror asType() {
      throw new AssertionError("not implemented");
    }

    @Override
    public Element getEnclosingElement() {
      return null;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
      return Collections.singletonList(JdtTypeParameterElement.this);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return v.visitUnknown(this, p);
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return Collections.emptyList();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return (A[]) Array.newInstance(annotationType, 0);
    }
  }
}
