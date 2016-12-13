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

import com.google.devtools.j2objc.types.GeneratedVariableElement;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * An ExecutableElement implementation backed by a JDT method binding.
 */
class JdtExecutableElement extends JdtElement implements ExecutableElement {

  private VariableElement superOuterParam = null;

  public JdtExecutableElement(IMethodBinding binding) {
    super(binding.getMethodDeclaration(), getMethodName(binding),
        binding.getMethodDeclaration().getModifiers());
  }

  private static String getMethodName(IMethodBinding binding) {
    return binding.isConstructor() ? "<init>" : binding.getMethodDeclaration().getName();
  }

  @Override
  public ElementKind getKind() {
    return ((IMethodBinding) binding).isConstructor()
        ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    List<TypeParameterElement> typeParams = new ArrayList<>();
    for (ITypeBinding tp : ((IMethodBinding) binding).getTypeParameters()) {
      Element tpe = BindingConverter.getElement(tp);
      assert tpe instanceof TypeParameterElement;
      typeParams.add((TypeParameterElement) tpe);
    }
    return typeParams;
  }

  @Override
  public TypeMirror getReturnType() {
    return BindingConverter.getType(((IMethodBinding) binding).getReturnType());
  }

  @Override
  public TypeMirror asType() {
    return BindingConverter.getType((IMethodBinding) binding);
  }

  void setSuperOuterParam(VariableElement superOuterParam) {
    this.superOuterParam = superOuterParam;
  }

  boolean hasSuperOuter() {
    return superOuterParam != null;
  }

  @Override
  public List<? extends VariableElement> getParameters() {
    IMethodBinding methodBinding = (IMethodBinding) binding;
    List<VariableElement> params = new ArrayList<>();
    if (superOuterParam != null) {
      params.add(superOuterParam);
    }
    ITypeBinding[] paramTypes = methodBinding.getParameterTypes();
    for (int i = 0; i < paramTypes.length; i++) {
      params.add(GeneratedVariableElement.newParameter(
          "param" + i, BindingConverter.getType(paramTypes[i]), this));
    }
    return params;
  }

  @Override
  public TypeMirror getReceiverType() {
    throw new AssertionError("not implemented");
  }

  @Override
  public Element getEnclosingElement() {
    return BindingConverter.getElement(((IMethodBinding) binding).getDeclaringClass());
  }

  @Override
  public boolean isVarArgs() {
    return ((IMethodBinding) binding).isVarargs();
  }

  @Override
  public boolean isDefault() {
   throw new AssertionError("not implemented");
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    List<TypeMirror> thrownTypes = new ArrayList<>();
    for (ITypeBinding type : ((IMethodBinding) binding).getExceptionTypes()) {
      thrownTypes.add(BindingConverter.getType(type));
    }
    return thrownTypes;
  }

  @Override
  public AnnotationValue getDefaultValue() {
    Object value = ((IMethodBinding) binding).getDefaultValue();
    return value == null ? null : new JdtAnnotationValue(value);
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  @Override
  public String toString() {
    return ((IMethodBinding) binding).toString();
  }
}
