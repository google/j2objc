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

import javax.lang.model.type.TypeMirror;

/**
 * Creates a type node by wrapping a name.
 */
public class SimpleType extends AnnotatableType {

  public SimpleType(org.eclipse.jdt.core.dom.SimpleType jdtNode) {
    super(jdtNode);
  }

  public SimpleType(SimpleType other) {
    super(other);
  }

  public SimpleType(TypeMirror typeMirror) {
    super(typeMirror);
  }

  @Override
  public Kind getKind() {
    return Kind.SIMPLE_TYPE;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      annotations.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SimpleType copy() {
    return new SimpleType(this);
  }
}
