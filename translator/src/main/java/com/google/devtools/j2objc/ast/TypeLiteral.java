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

package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.type.TypeMirror;

/**
 * Type literal node type.
 */
public class TypeLiteral extends Expression {

  private final TypeMirror typeMirror;
  private ChildLink<Type> type = ChildLink.create(Type.class, this);

  public TypeLiteral(TypeMirror typeMirror) {
    this.typeMirror = typeMirror;
  }

  public TypeLiteral(TypeLiteral other) {
    super(other);
    typeMirror = other.getTypeMirror();
    type.copyFrom(other.getType());
  }

  public TypeLiteral(TypeMirror literalType, TypeUtil typeUtil) {
    typeMirror = typeUtil.getJavaClass().asType();
    type.set(Type.newType(literalType));
  }

  @Override
  public Kind getKind() {
    return Kind.TYPE_LITERAL;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public Type getType() {
    return type.get();
  }

  public TypeLiteral setType(Type newType) {
    type.set(newType);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      type.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public TypeLiteral copy() {
    return new TypeLiteral(this);
  }
}
