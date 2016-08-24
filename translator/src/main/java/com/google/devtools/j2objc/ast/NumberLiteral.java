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
import com.google.devtools.j2objc.types.Types;
import javax.lang.model.type.TypeMirror;

/**
 * Number literal node type.
 */
public class NumberLiteral extends Expression {

  private String token = null;
  private Number value = null;
  private final TypeMirror typeMirror;

  public NumberLiteral(NumberLiteral other) {
    super(other);
    token = other.getToken();
    value = other.getValue();
    typeMirror = other.getTypeMirror();
  }

  public NumberLiteral(Number value, Types typeEnv) {
    this(value, typeForNumber(value, typeEnv));
  }

  public NumberLiteral(Number value, TypeMirror typeMirror) {
    this.value = value;
    this.typeMirror = typeMirror;
  }

  public static NumberLiteral newIntLiteral(Integer i, Types typeEnv) {
    return new NumberLiteral(i, typeEnv);
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
    return value;
  }

  public NumberLiteral setValue(Number newValue) {
    value = newValue;
    return this;
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
    Preconditions.checkNotNull(value);
  }

  private static TypeMirror typeForNumber(Number value, Types typeEnv) {
    if (value instanceof Byte) {
      return typeEnv.resolveJavaTypeMirror("byte");
    } else if (value instanceof Short) {
      return typeEnv.resolveJavaTypeMirror("short");
    } else if (value instanceof Integer) {
      return typeEnv.resolveJavaTypeMirror("int");
    } else if (value instanceof Long) {
      return typeEnv.resolveJavaTypeMirror("long");
    } else if (value instanceof Float) {
      return typeEnv.resolveJavaTypeMirror("float");
    } else if (value instanceof Double) {
      return typeEnv.resolveJavaTypeMirror("double");
    } else {
      throw new AssertionError("Invalid number literal type: " + value.getClass().getName());
    }
  }
}
