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

import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Number literal node type.
 */
public class NumberLiteral extends Expression {

  private ITypeBinding typeBinding = null;
  private String token = null;
  private Number value = null;

  public NumberLiteral(org.eclipse.jdt.core.dom.NumberLiteral jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    token = jdtNode.getToken();
    Object constantValue = jdtNode.resolveConstantExpressionValue();
    // TODO(kstanger): We should be able to remove the null test once all the
    // mutations are converted to the new AST.
    assert constantValue == null || constantValue instanceof Number;
    value = (Number) constantValue;
  }

  public NumberLiteral(NumberLiteral other) {
    super(other);
    typeBinding = other.getTypeBinding();
    token = other.getToken();
    value = other.getValue();
  }

  public NumberLiteral(Number value) {
    typeBinding = typeForNumber(value);
    this.value = value;
  }

  public static NumberLiteral newIntLiteral(Integer i) {
    return new NumberLiteral(i);
  }

  @Override
  public Kind getKind() {
    return Kind.NUMBER_LITERAL;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
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
