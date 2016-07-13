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

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

class JdtVariableElement extends JdtElement implements VariableElement {
  private final JdtBinding owner;

  JdtVariableElement(IVariableBinding binding) {
    this(binding, binding.getName(), binding.getModifiers(),
        binding.getDeclaringMethod(), binding.getDeclaringClass());
  }

  /**
   * Constructor used to create VariableElements for method parameters.
   * The JDT doesn't have bindings for parameters, so the parameter type
   * is used as the binding, along with a unique name.
   */
  static JdtVariableElement createParameterElement(IBinding type, IMethodBinding owner, int index) {
    return new JdtVariableElement(type, "param" + index, 0, owner, null);
  }

  private JdtVariableElement(IBinding declaration, String name, int modifiers,
      IMethodBinding declaringMethod, ITypeBinding declaringClass) {
    super(declaration, name, modifiers);
    this.owner =
        BindingConverter.wrapBinding(declaringMethod != null ? declaringMethod : declaringClass);
  }

  @Override
  public ElementKind getKind() {
    if (binding instanceof ITypeBinding) {
      return ElementKind.PARAMETER;
    }
    IVariableBinding var = (IVariableBinding) binding;
    if (var.isField()) {
      return ElementKind.FIELD;
    }
    if (var.isParameter()) {
      return ElementKind.PARAMETER;
    }
    if (var.isEnumConstant()) {
      return ElementKind.ENUM_CONSTANT;
    }
    return ElementKind.LOCAL_VARIABLE;
  }

  @Override
  public Object getConstantValue() {
    return getKind() != ElementKind.PARAMETER
        ? ((IVariableBinding) binding).getConstantValue() : null;
  }

  @Override
  public TypeMirror asType() {
    return BindingConverter.getType(((IVariableBinding) binding).getType());
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  @Override
  public Element getEnclosingElement() {
    return BindingConverter.getElement(owner);
  }
}
