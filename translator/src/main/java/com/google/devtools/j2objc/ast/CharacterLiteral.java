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
 * Node type for character literal values.
 */
public class CharacterLiteral extends Expression {

  private final TypeMirror typeMirror;

  public CharacterLiteral(CharacterLiteral other) {
    super(other);
    this.typeMirror = other.getTypeMirror();
  }

  public CharacterLiteral(char charValue, TypeUtil typeUtil) {
    this(charValue, typeUtil.getChar());
  }

  public CharacterLiteral(char charValue, TypeMirror typeMirror) {
    this.constantValue = charValue;
    this.typeMirror = typeMirror;
  }

  @Override
  public Kind getKind() {
    return Kind.CHARACTER_LITERAL;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public char charValue() {
    return (Character) constantValue;
  }

  @Override
  public CharacterLiteral setConstantValue(Object value) {
    assert value == null || value instanceof Character;
    return (CharacterLiteral) super.setConstantValue(value);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public CharacterLiteral copy() {
    return new CharacterLiteral(this);
  }
}
