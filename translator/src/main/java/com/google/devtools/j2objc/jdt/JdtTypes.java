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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Utility methods for types. Methods not referenced by the
 * translator are not implemented.
 */
// TODO(tball): add to ProcessingEnvironment impl, make package-private.
public class JdtTypes implements Types {

  private static final JdtTypes INSTANCE = new JdtTypes();

  // TODO(tball): remove when added to ProcessingEnvironment.
  public static JdtTypes getInstance() {
    return INSTANCE;
  }

  @Override
  public Element asElement(TypeMirror t) {
    throw new AssertionError("not implemented");
  }

  @Override
  public TypeMirror asMemberOf(DeclaredType containing, Element element) {
    throw new AssertionError("not implemented");
  }

  @Override
  public TypeElement boxedClass(PrimitiveType p) {
    throw new AssertionError("not implemented");
  }

  @Override
  public TypeMirror capture(TypeMirror t) {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean contains(TypeMirror t1, TypeMirror t2) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
    ITypeBinding binding = BindingConverter.unwrapTypeMirrorIntoTypeBinding(t);
    List<TypeMirror> mirrors = new ArrayList<>();
    if (binding.getSuperclass() != null) {
      mirrors.add(BindingConverter.getType(binding.getSuperclass()));
    }
    for (ITypeBinding b : binding.getInterfaces()) {
      mirrors.add(BindingConverter.getType(b));
    }
    return mirrors;
  }

  @Override
  public TypeMirror erasure(TypeMirror t) {
    throw new AssertionError("not implemented");
  }

  @Override
  public DeclaredType getDeclaredType(DeclaredType containing,
      TypeElement typeElem, TypeMirror... typeArgs) {
    throw new AssertionError("not implemented");
  }

  @Override
  public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
    throw new AssertionError("not implemented");
  }

  @Override
  public NoType getNoType(TypeKind kind) {
    throw new AssertionError("not implemented");
  }

  @Override
  public NullType getNullType() {
    throw new AssertionError("not implemented");
  }

  @Override
  public PrimitiveType getPrimitiveType(TypeKind kind) {
    throw new AssertionError("not implemented");
  }

  @Override
  public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isSameType(TypeMirror t1, TypeMirror t2) {
      return ((JdtTypeMirror) t1).bindingsEqual((JdtTypeMirror) t2);
  }

  @Override
  public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
    return ((IMethodBinding) BindingConverter.unwrapTypeMirrorIntoBinding(m1)).isSubsignature(
        ((IMethodBinding) BindingConverter.unwrapTypeMirrorIntoBinding(m2)));
  }

  @Override
  public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
    throw new AssertionError("not implemented");
  }

  @Override
  public PrimitiveType unboxedType(TypeMirror t) {
    throw new AssertionError("not implemented");
  }

  @Override
  public ArrayType getArrayType(TypeMirror componentType) {
    throw new AssertionError("not implemented");
  }
}
