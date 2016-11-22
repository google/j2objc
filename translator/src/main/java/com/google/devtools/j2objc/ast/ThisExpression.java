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
 * Node type for "this".
 */
public class ThisExpression extends Expression {

  private TypeMirror typeMirror = null;
  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);

  public ThisExpression() {}

  public ThisExpression(ThisExpression other) {
    super(other);
    typeMirror = other.getTypeMirror();
    qualifier.copyFrom(other.getQualifier());
  }

  public ThisExpression(TypeMirror typeMirror) {
    this.typeMirror = typeMirror;
  }

  @Override
  public Kind getKind() {
    return Kind.THIS_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public ThisExpression setTypeMirror(TypeMirror newType) {
    typeMirror = newType;
    return this;
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public ThisExpression setQualifier(Name newQualifier) {
    qualifier.set(newQualifier);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      qualifier.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ThisExpression copy() {
    return new ThisExpression(this);
  }
}
