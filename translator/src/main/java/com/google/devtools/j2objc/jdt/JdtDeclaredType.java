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

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

class JdtDeclaredType extends JdtTypeMirror implements DeclaredType, ReferenceType {

  JdtDeclaredType(JdtTypeBinding binding) {
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
    IMethodBinding enclosingMethod = ((JdtTypeBinding) binding).getDeclaringMethod();
    if (enclosingMethod != null) {
      return BindingConverter.getType(enclosingMethod);
    }
    ITypeBinding enclosingType = ((JdtTypeBinding) binding).getDeclaringClass();
    return enclosingType != null
        ? BindingConverter.getType(enclosingType)
        : BindingConverter.NO_TYPE;
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    List<TypeMirror> typeArgs = new ArrayList<TypeMirror>();
    for (ITypeBinding typeArg : ((JdtTypeBinding) binding).getTypeArguments()) {
      typeArgs.add(BindingConverter.getType(typeArg));
    }
    return typeArgs;
  }
}
