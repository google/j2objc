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
 * Node type for string literals.
 */
public class CStringLiteral extends Expression {

  public CStringLiteral(CStringLiteral other) {
    super(other);
  }

  public CStringLiteral(String literalValue) {
    this.constantValue = literalValue;
  }

  @Override
  public Kind getKind() {
    return Kind.C_STRING_LITERAL;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return TypeUtil.NATIVE_CHAR_PTR;
  }

  public String getLiteralValue() {
    return (String) constantValue;
  }

  @Override
  public CStringLiteral setConstantValue(Object value) {
    assert value == null || value instanceof String;
    return (CStringLiteral) super.setConstantValue(value);
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
