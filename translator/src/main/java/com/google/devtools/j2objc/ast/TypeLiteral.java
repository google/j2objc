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

import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.jdt.TreeConverter;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.ITypeBinding;

import javax.lang.model.type.TypeMirror;

/**
 * Type literal node type.
 */
public class TypeLiteral extends Expression {

  private final TypeMirror typeMirror;
  private ChildLink<Type> type = ChildLink.create(Type.class, this);

  public TypeLiteral(org.eclipse.jdt.core.dom.TypeLiteral jdtNode) {
    super(jdtNode);
    ITypeBinding typeBinding = BindingConverter.wrapBinding(jdtNode.resolveTypeBinding());
    typeMirror = BindingConverter.getType(typeBinding);
    type.set((Type) TreeConverter.convert(jdtNode.getType()));
  }

  public TypeLiteral(TypeLiteral other) {
    super(other);
    typeMirror = other.getTypeMirror();
    type.copyFrom(other.getType());
  }

  public TypeLiteral(ITypeBinding literalType, Types typeEnv) {
    typeMirror = BindingConverter.getType(typeEnv.resolveJavaType("java.lang.Class"));
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
