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
 * Node type for a local type declaration.
 */
public class TypeDeclarationStatement extends Statement {

  private final ChildLink<AbstractTypeDeclaration> declaration =
      ChildLink.create(AbstractTypeDeclaration.class, this);

  public TypeDeclarationStatement() {}

  public TypeDeclarationStatement(TypeDeclarationStatement other) {
    super(other);
    declaration.copyFrom(other.getDeclaration());
  }

  @Override
  public Kind getKind() {
    return Kind.TYPE_DECLARATION_STATEMENT;
  }

  public AbstractTypeDeclaration getDeclaration() {
    return declaration.get();
  }

  public TypeDeclarationStatement setDeclaration(AbstractTypeDeclaration decl) {
    declaration.set(decl);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      declaration.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public TypeDeclarationStatement copy() {
    return new TypeDeclarationStatement(this);
  }
}
