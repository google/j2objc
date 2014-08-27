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
 * Node type for character literal values.
 */
public class CharacterLiteral extends Expression {

  private char charValue = '\0';

  public CharacterLiteral(org.eclipse.jdt.core.dom.CharacterLiteral jdtNode) {
    super(jdtNode);
    charValue = jdtNode.charValue();
  }

  public CharacterLiteral(CharacterLiteral other) {
    super(other);
    charValue = other.charValue();
  }

  public CharacterLiteral(char charValue) {
    this.charValue = charValue;
  }

  @Override
  public Kind getKind() {
    return Kind.CHARACTER_LITERAL;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return Types.resolveJavaType("char");
  }

  public char charValue() {
    return charValue;
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
