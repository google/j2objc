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

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Number literal node type.
 */
public class NumberLiteral extends Expression {

  private String token = null;
  private Number value = null;

  public NumberLiteral(org.eclipse.jdt.core.dom.NumberLiteral jdtNode) {
    super(jdtNode);
    token = jdtNode.getToken();
    Object constantValue = jdtNode.resolveConstantExpressionValue();
    assert constantValue instanceof Number;
    value = (Number) constantValue;
  }

  public NumberLiteral(NumberLiteral other) {
    super(other);
    token = other.getToken();
    value = other.getValue();
  }

  public NumberLiteral(Number value) {
    this.value = value;
  }

  public static NumberLiteral newIntLiteral(Integer i) {
    NumberLiteral numLit = new NumberLiteral(i);
    numLit.token = Integer.toString(i);
    return numLit;
  }

  @Override
  public Kind getKind() {
    return Kind.NUMBER_LITERAL;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeForNumber(value);
  }

  public String getToken() {
    return token;
  }

  public Number getValue() {
    return value;
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

  private ITypeBinding typeForNumber(Number value) {
    if (value instanceof Byte) {
      return Types.resolveJavaType("byte");
    } else if (value instanceof Short) {
      return Types.resolveJavaType("short");
    } else if (value instanceof Integer) {
      return Types.resolveJavaType("int");
    } else if (value instanceof Long) {
      return Types.resolveJavaType("long");
    } else if (value instanceof Float) {
      return Types.resolveJavaType("float");
    } else if (value instanceof Double) {
      return Types.resolveJavaType("double");
    } else {
      throw new AssertionError("Invalid number literal type: " + value.getClass().getName());
    }
  }
}
