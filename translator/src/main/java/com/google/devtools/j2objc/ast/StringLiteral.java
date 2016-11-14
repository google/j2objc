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
public class StringLiteral extends Expression {

  private String literalValue = null;
  private TypeMirror typeMirror;

  public StringLiteral() {}

  public StringLiteral(StringLiteral other) {
    super(other);
    literalValue = other.getLiteralValue();
    typeMirror = other.getTypeMirror();
  }

  public StringLiteral(String literalValue, TypeMirror type) {
    this.literalValue = literalValue;
    typeMirror = type;
  }

  public StringLiteral(String literalValue, TypeUtil typeUtil) {
    this(literalValue, typeUtil.getJavaString().asType());
  }

  @Override
  public Kind getKind() {
    return Kind.STRING_LITERAL;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public StringLiteral setTypeMirror(TypeMirror newType) {
    typeMirror = newType;
    return this;
  }

  public String getLiteralValue() {
    return literalValue;
  }

  public StringLiteral setLiteralValue(String value) {
    literalValue = value;
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public StringLiteral copy() {
    return new StringLiteral(this);
  }
}
