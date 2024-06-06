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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.ast.Expression;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * TypeMirror for native C types.
 *
 * @author Nathan Braswell
 */
public class NativeType extends AbstractTypeMirror {

  private final String name;
  private final List<TypeMirror> typeArguments = new ArrayList<>();
  private final List<TypeMirror> referencedTypes = new ArrayList<>();
  private String header = ""; // No native import by default

  public NativeType(String name) {
    this.name = name;
  }

  public NativeType(String name, String header) {
    this.name = name;
    if (header != null) {
      this.header = header;
    }
  }

  public NativeType(
      String name,
      String header,
      List<? extends TypeMirror> typeArguments,
      List<? extends TypeMirror> referencedTypes) {
    this.name = name;
    if (header != null) {
      this.header = header;
    }
    if (typeArguments != null) {
      this.typeArguments.addAll(typeArguments);
    }
    if (referencedTypes != null) {
      this.referencedTypes.addAll(referencedTypes);
    }
  }

  public String getName() {
    return name;
  }

  public String getNameWithTypeArgumentNames(List<String> typeArgumentNames) {
    return name; // Base class doesn't support type argument naming.
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.OTHER;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitUnknown(this, p);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof NativeType)) {
      return false;
    }
    NativeType otherNative = (NativeType) other;
    return otherNative.getName().equals(name)
        && otherNative.getHeader().equals(header)
        && otherNative.getReferencedTypes().equals(referencedTypes)
        && otherNative.getTypeArguments().equals(typeArguments)
        && otherNative.getHeader().equals(getHeader())
        && otherNative.getForwardDeclaration().equals(getForwardDeclaration());
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  public List<? extends TypeMirror> getTypeArguments() {
    return typeArguments;
  }

  public void setTypeArguments(Collection<? extends TypeMirror> types) {
    typeArguments.clear();
    typeArguments.addAll(types);
  }

  public List<TypeMirror> getReferencedTypes() {
    return referencedTypes;
  }

  public String getHeader() {
    return header;
  }

  public String getForwardDeclaration() {
    return ""; // No forward declaration
  }

  /**
   * If available, return an expression for the type's "default" value, usually the value that would
   * be obtained by messaging nil for this type.
   */
  public Expression getDefaultValueExpression() {
    return null;
  }
}
