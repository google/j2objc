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

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.type.TypeMirror;

/**
 * Number literal node type.
 */
public class NumberLiteral extends Expression {

  private String token = null;
  private final TypeMirror typeMirror;

  public NumberLiteral(NumberLiteral other) {
    super(other);
    token = other.getToken();
    typeMirror = other.getTypeMirror();
  }

  public NumberLiteral(Number value, TypeUtil typeUtil) {
    this(value, typeForNumber(value, typeUtil));
  }

  public NumberLiteral(Number value, TypeMirror typeMirror) {
    this.constantValue = value;
    this.typeMirror = typeMirror;
  }

  public static NumberLiteral newIntLiteral(Integer i, TypeUtil typeUtil) {
    return new NumberLiteral(i, typeUtil);
  }

  @Override
  public Kind getKind() {
    return Kind.NUMBER_LITERAL;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public String getToken() {
    return token;
  }

  public NumberLiteral setToken(String token) {
    this.token = token;
    return this;
  }

  public Number getValue() {
    return (Number) constantValue;
  }

  public NumberLiteral setValue(Number newValue) {
    constantValue = newValue;
    return this;
  }

  @Override
  public NumberLiteral setConstantValue(Object value) {
    assert value == null || value instanceof Number;
    return (NumberLiteral) super.setConstantValue(value);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public NumberLiteral copy() {
    return new NumberLiteral(this);
  }

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkNotNull(constantValue);
  }

  private static TypeMirror typeForNumber(Number value, TypeUtil typeUtil) {
    if (value instanceof Byte) {
      return typeUtil.getByte();
    } else if (value instanceof Short) {
      return typeUtil.getShort();
    } else if (value instanceof Integer) {
      return typeUtil.getInt();
    } else if (value instanceof Long) {
      return typeUtil.getLong();
    } else if (value instanceof Float) {
      return typeUtil.getFloat();
    } else if (value instanceof Double) {
      return typeUtil.getDouble();
    } else {
      throw new AssertionError("Invalid number literal type: " + value.getClass().getName());
    }
  }
}
