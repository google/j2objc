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
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;

class JdtDeclaredType extends JdtTypeMirror implements DeclaredType, ReferenceType {

  JdtDeclaredType(ITypeBinding binding) {
    super(binding);
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.DECLARED;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitDeclared(this, p);
  }

  @Override
  public Element asElement() {
    return BindingConverter.getElement(binding);
  }

  @Override
  public TypeMirror getEnclosingType() {
    ITypeBinding enclosingType = ((ITypeBinding) binding).getDeclaringClass();
    return enclosingType != null
        ? BindingConverter.getType(enclosingType)
        : BindingConverter.NO_TYPE;
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    List<TypeMirror> typeArgs = new ArrayList<TypeMirror>();
    ITypeBinding typeBinding = (ITypeBinding) binding;
    if (typeBinding.isGenericType()) {
      for (ITypeBinding typeArg : typeBinding.getTypeParameters()) {
        typeArgs.add(BindingConverter.getType(typeArg));
      }
    } else if (typeBinding.isParameterizedType()) {
      for (ITypeBinding typeArg : typeBinding.getTypeArguments()) {
        typeArgs.add(BindingConverter.getType(typeArg));
      }
    }
    return typeArgs;
  }

  @Override
  public String toString() {
    return ((ITypeBinding) binding).getQualifiedName();
  }
}
