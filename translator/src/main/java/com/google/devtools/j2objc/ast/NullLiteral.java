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
import javax.lang.model.type.TypeMirror;

/**
 * Null literal node type.
 */
public class NullLiteral extends Expression {

  public NullLiteral(org.eclipse.jdt.core.dom.NullLiteral jdtNode) {
    super(jdtNode);
  }

  public NullLiteral(NullLiteral other) {
    super(other);
  }

  public NullLiteral() {}

  @Override
  public Kind getKind() {
    return Kind.NULL_LITERAL;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return BindingConverter.NULL_TYPE;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public NullLiteral copy() {
    return new NullLiteral(this);
  }
}
