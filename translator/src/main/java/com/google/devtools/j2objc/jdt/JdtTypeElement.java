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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

class JdtTypeElement extends JdtElement implements TypeElement {

  JdtTypeElement(ITypeBinding binding) {
    super(binding.getTypeDeclaration(), binding.getTypeDeclaration().getName(),
        binding.getTypeDeclaration().getModifiers());
  }

  @Override
  public String toString() {
    return ((ITypeBinding) binding).getKey();
  }

  @Override
  public ElementKind getKind() {
    ITypeBinding type = (ITypeBinding) binding;
    if (type.isAnnotation()) {
      return ElementKind.ANNOTATION_TYPE;
    }
    if (type.isEnum()) {
      return ElementKind.ENUM;
    }
    if (type.isInterface()) {
      return ElementKind.INTERFACE;
    }
    return ElementKind.CLASS;
  }

  @Override
  public NestingKind getNestingKind() {
    ITypeBinding type = (ITypeBinding) binding;
    if (type.isAnonymous()) {
      return NestingKind.ANONYMOUS;
    }
    if (type.isLocal()) {
      return NestingKind.LOCAL;
    }
    if (type.isMember()) {
      return NestingKind.MEMBER;
    }
    return NestingKind.TOP_LEVEL;
  }

  @Override
  public Name getQualifiedName() {
    return BindingConverter.getName(((ITypeBinding) binding).getQualifiedName());
  }

  @Override
  public TypeMirror getSuperclass() {
    return BindingConverter.getType(((ITypeBinding) binding).getSuperclass());
  }

  @Override public TypeMirror asType() {
    return BindingConverter.getType((ITypeBinding) binding);
  }

  @Override
  public Element getEnclosingElement() {
    ITypeBinding decl = (ITypeBinding) binding;
    // We check to make sure that we only return the declaring method as the enclosing element
    // if it's inside the declaring class, otherwise the declaring class must be inside
    // the method and thus is the true enclosing element.
    if (decl.getDeclaringMethod() != null
        && decl.getDeclaringMethod().getDeclaringClass() == decl.getDeclaringClass()) {
      return BindingConverter.getElement(decl.getDeclaringMethod());
    } else if (decl.isTopLevel()) {
      return BindingConverter.getElement(decl.getPackage());
    } else if (decl.isLocal()) {
      // This comes up when an anonymous class is initializing a member outside of a constructor.
      // The enclosing element should be the field it's being assigned to, but the binding doesn't
      // give us this information, so we have to fake it.
      return GeneratedVariableElement.newField(
          "fake_field", this.asType(), BindingConverter.getElement(decl.getDeclaringClass()));
    } else {
      return BindingConverter.getElement(decl.getDeclaringClass());
    }
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    ITypeBinding decl = (ITypeBinding) binding;
    List<Element> toReturn = new ArrayList<>();
    for (IVariableBinding i : decl.getDeclaredFields()) {
      toReturn.add(BindingConverter.getElement(i));
    }
    for (IMethodBinding i : decl.getDeclaredMethods()) {
      toReturn.add(BindingConverter.getElement(i));
    }
    for (ITypeBinding i : decl.getDeclaredTypes()) {
      toReturn.add(BindingConverter.getElement(i));
    }
    return toReturn;
  }

  @Override
  public List<? extends TypeMirror> getInterfaces() {
    List<TypeMirror> interfaces = new ArrayList<>();
    for (ITypeBinding iface : ((ITypeBinding) binding).getInterfaces()) {
      interfaces.add(BindingConverter.getType(iface));
    }
    return interfaces;
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    List<TypeParameterElement> typeParams = new ArrayList<>();
    for (ITypeBinding typeParam : ((ITypeBinding) binding).getTypeParameters()) {
      TypeParameterElement tpe = (TypeParameterElement) BindingConverter.getElement(typeParam);
      typeParams.add(tpe);
    }
    return typeParams;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitType(this, p);
  }
}
