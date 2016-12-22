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

import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Utility methods for types. Methods not referenced by the
 * translator are not implemented.
 */
class JdtTypes implements Types {

  private final Map<TypeKind, PrimitiveType> primitiveTypes = new EnumMap<>(TypeKind.class);
  private final Map<TypeKind, TypeElement> boxedClasses = new EnumMap<>(TypeKind.class);
  private final Map<TypeElement, PrimitiveType> unboxedTypes = new HashMap<>();
  private final NoType voidType;
  private final TypeMirror objectType;

  JdtTypes(AST ast) {
    populatePrimitiveMaps(ast);
    objectType = BindingConverter.getType(ast.resolveWellKnownType("java.lang.Object"));
    voidType = (NoType) BindingConverter.getType(ast.resolveWellKnownType("void"));
  }

  private void populatePrimitiveMaps(AST ast) {
    resolvePrimitiveType(ast, TypeKind.BOOLEAN, "boolean", "java.lang.Boolean");
    resolvePrimitiveType(ast, TypeKind.BYTE, "byte", "java.lang.Byte");
    resolvePrimitiveType(ast, TypeKind.CHAR, "char", "java.lang.Character");
    resolvePrimitiveType(ast, TypeKind.SHORT, "short", "java.lang.Short");
    resolvePrimitiveType(ast, TypeKind.INT, "int", "java.lang.Integer");
    resolvePrimitiveType(ast, TypeKind.LONG, "long", "java.lang.Long");
    resolvePrimitiveType(ast, TypeKind.FLOAT, "float", "java.lang.Float");
    resolvePrimitiveType(ast, TypeKind.DOUBLE, "double", "java.lang.Double");
  }

  private void resolvePrimitiveType(AST ast, TypeKind kind, String pName, String cName) {
    PrimitiveType primitiveType =
        (PrimitiveType) BindingConverter.getType(ast.resolveWellKnownType(pName));
    TypeElement classElement = BindingConverter.getTypeElement(ast.resolveWellKnownType(cName));
    primitiveTypes.put(kind, primitiveType);
    boxedClasses.put(kind, classElement);
    unboxedTypes.put(classElement, primitiveType);
  }

  @Override
  public Element asElement(TypeMirror t) {
    throw new AssertionError("not implemented");
  }

  @Override
  public TypeMirror asMemberOf(DeclaredType containing, Element element) {
    return asMemberOfInternal(containing, element);
  }

  // Static version of the above for internal use.
  static TypeMirror asMemberOfInternal(DeclaredType containing, Element element) {
    ITypeBinding c = BindingConverter.unwrapTypeMirrorIntoTypeBinding(containing);
    if (ElementUtil.isExecutableElement(element)) {
      IMethodBinding e = BindingConverter.unwrapExecutableElement((ExecutableElement) element);
      for (IMethodBinding m : c.getDeclaredMethods()) {
        if (m.isSubsignature(e)) {
          return BindingConverter.getType(m);
        }
      }
    } else if (ElementUtil.isVariable(element)) {
      IVariableBinding declaredVar =
          BindingConverter.unwrapVariableElement((VariableElement) element);
      for (IVariableBinding var : c.getDeclaredFields()) {
        if (var.getVariableDeclaration().isEqualTo(declaredVar)) {
          return BindingConverter.getType(var.getType());
        }
      }
    }
    throw new IllegalArgumentException("Element: " + element + " is not a member of " + containing);
  }

  @Override
  public TypeElement boxedClass(PrimitiveType p) {
    return boxedClasses.get(p.getKind());
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
    } else if (TypeUtil.isInterface(t)) {
      mirrors.add(objectType);
    }
    for (ITypeBinding b : binding.getInterfaces()) {
      mirrors.add(BindingConverter.getType(b));
    }
    return mirrors;
  }

  @Override
  public TypeMirror erasure(TypeMirror t) {
    ITypeBinding binding = BindingConverter.unwrapTypeMirrorIntoTypeBinding(t);
    return BindingConverter.getType(binding.getErasure());
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
    switch (kind) {
      case NONE:
        return BindingConverter.NO_TYPE;
      case VOID:
        return voidType;
      default:
        throw new IllegalArgumentException("Not a valid NoType kind: " + kind);
    }
  }

  @Override
  public NullType getNullType() {
    return new JdtNullType();
  }

  @Override
  public PrimitiveType getPrimitiveType(TypeKind kind) {
    PrimitiveType result = primitiveTypes.get(kind);
    if (result == null) {
      throw new IllegalArgumentException("Not a primitive kind: " + kind);
    }
    return result;
  }

  @Override
  public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    return BindingConverter.unwrapTypeMirrorIntoTypeBinding(t1).isAssignmentCompatible(
        BindingConverter.unwrapTypeMirrorIntoTypeBinding(t2));
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
    return BindingConverter.unwrapTypeMirrorIntoTypeBinding(t1).isSubTypeCompatible(
        BindingConverter.unwrapTypeMirrorIntoTypeBinding(t2));
  }

  @Override
  public PrimitiveType unboxedType(TypeMirror t) {
    if (t.getKind() == TypeKind.DECLARED) {
      TypeElement e = (TypeElement) ((DeclaredType) t).asElement();
      PrimitiveType result = unboxedTypes.get(e);
      if (result != null) {
        return result;
      }
    }
    throw new IllegalArgumentException("No unboxing convertion for: " + t);
  }

  @Override
  public ArrayType getArrayType(TypeMirror componentType) {
    return (ArrayType) BindingConverter.getType(GeneratedTypeBinding.newArrayType(
        BindingConverter.unwrapTypeMirrorIntoTypeBinding(componentType)));
  }
}
