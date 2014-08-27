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

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Node type for "this".
 */
public class ThisExpression extends Expression {

  private ITypeBinding typeBinding = null;
  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);

  public ThisExpression(org.eclipse.jdt.core.dom.ThisExpression jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    qualifier.set((Name) TreeConverter.convert(jdtNode.getQualifier()));
  }

  public ThisExpression(ThisExpression other) {
    super(other);
    typeBinding = other.getTypeBinding();
    qualifier.copyFrom(other.getQualifier());
  }

  public ThisExpression(ITypeBinding typeBinding) {
    this.typeBinding = typeBinding;
  }

  @Override
  public Kind getKind() {
    return Kind.THIS_EXPRESSION;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public void setQualifier(Name newQualifier) {
    qualifier.set(newQualifier);
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
