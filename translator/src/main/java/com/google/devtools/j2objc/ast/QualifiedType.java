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

/**
 * Node type for a qualified type.
 */
public class QualifiedType extends Type {

  public QualifiedType(org.eclipse.jdt.core.dom.QualifiedType jdtNode) {
    super(jdtNode);
  }

  public QualifiedType(QualifiedType other) {
    super(other);
  }

  @Override
  public Kind getKind() {
    return Kind.QUALIFIED_TYPE;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public QualifiedType copy() {
    return new QualifiedType(this);
  }
}
