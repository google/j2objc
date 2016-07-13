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
import com.google.devtools.j2objc.types.Types;

import javax.lang.model.type.TypeMirror;

/**
 * Node type for string literals.
 */
public class CStringLiteral extends Expression {

  private String literalValue = null;
  private final TypeMirror typeMirror;

  public CStringLiteral(CStringLiteral other) {
    super(other);
    literalValue = other.getLiteralValue();
    typeMirror = other.getTypeMirror();
  }

  public CStringLiteral(String literalValue, Types typeEnv) {
    this.literalValue = literalValue;
    typeMirror = BindingConverter.getType(typeEnv.getPointerType(typeEnv.resolveJavaType("char")));
  }

  @Override
  public Kind getKind() {
    return Kind.C_STRING_LITERAL;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public String getLiteralValue() {
    return literalValue;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public CStringLiteral copy() {
    return new CStringLiteral(this);
  }
}
