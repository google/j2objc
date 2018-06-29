/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.ImmutableList;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.util.ElementUtil;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * If needed, it removes the following serialization related members from the Java AST:
 *
 * <ul>
 *   <li>Field serialVersionUID.
 *   <li>Method writeObject.
 *   <li>Method readObject.
 *   <li>Method readObjectNoData.
 *   <li>Method writeReplace
 *   <li>Method readResolve
 * </ul>
 */
public final class SerializationStripper extends UnitTreeVisitor {

  public SerializationStripper(CompilationUnit unit) {
    super(unit);
  }

  /** Only modify Serializable types when stripping reflection metadata. */
  @Override
  public boolean visit(TypeDeclaration node) {
    TypeMirror serializableType = typeUtil.resolveJavaType("java.io.Serializable").asType();
    TypeMirror nodeType = node.getTypeElement().asType();
    boolean isSerializable = typeUtil.isAssignable(nodeType, serializableType);
    boolean stripReflection = !translationUtil.needsReflection(node.getTypeElement());
    return stripReflection && isSerializable;
  }

  /** Removes serialVersionUID field. */
  @Override
  public boolean visit(FieldDeclaration node) {
    if (isSerializationField(node.getFragment(0).getVariableElement())) {
      node.remove();
    }
    return false;
  }

  /** Removes serialization related methods. */
  @Override
  public boolean visit(MethodDeclaration node) {
    if (isSerializationMethod(node.getExecutableElement())) {
      node.remove();
    }
    return false;
  }

  private static boolean isSerializationField(VariableElement field) {
    boolean isStaticFinal = ElementUtil.isGlobalVar(field);
    boolean isLong = field.asType().getKind() == TypeKind.LONG;
    boolean matchesName = ElementUtil.isNamed(field, "serialVersionUID");
    return isStaticFinal && isLong && matchesName;
  }

  // Expected access modifier of a serialization method.
  private enum Access {
    PRIVATE,
    ANY
  };

  private static class SerializationMethod {
    final String name;
    final String signature;
    final Access access;

    SerializationMethod(String name, String signature, Access access) {
      this.name = name;
      this.signature = signature;
      this.access = access;
    }

    boolean requiresPrivate() {
      return access.equals(Access.PRIVATE);
    }
  }

  private static final ImmutableList<SerializationMethod> SERIALIZATION_METHODS =
      ImmutableList.of(
          new SerializationMethod("writeObject", "(Ljava/io/ObjectOutputStream;)V", Access.PRIVATE),
          new SerializationMethod("readObject", "(Ljava/io/ObjectInputStream;)V", Access.PRIVATE),
          new SerializationMethod("readObjectNoData", "()V", Access.PRIVATE),
          new SerializationMethod("writeReplace", "()Ljava/lang/Object;", Access.ANY),
          new SerializationMethod("readResolve", "()Ljava/lang/Object;", Access.ANY));

  private boolean isSerializationMethod(ExecutableElement method) {
    String signature = typeUtil.getReferenceSignature(method);
    boolean isPrivate = ElementUtil.isPrivate(method);
    return SERIALIZATION_METHODS
        .stream()
        .anyMatch(
            m ->
                ElementUtil.isNamed(method, m.name)
                    && m.signature.equals(signature)
                    && (!m.requiresPrivate() || isPrivate));
  }
}
