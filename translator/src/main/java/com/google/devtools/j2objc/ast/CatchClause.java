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
 * Node type for a catch clause.
 */
public class CatchClause extends TreeNode {

  private ChildLink<SingleVariableDeclaration> exception =
      ChildLink.create(SingleVariableDeclaration.class, this);
  private ChildLink<Block> body = ChildLink.create(Block.class, this);

  public CatchClause() {}
  
  public CatchClause(CatchClause other) {
    super(other);
    exception.copyFrom(other.getException());
    body.copyFrom(other.getBody());
  }

  @Override
  public Kind getKind() {
    return Kind.CATCH_CLAUSE;
  }

  public SingleVariableDeclaration getException() {
    return exception.get();
  }
  
  public CatchClause setException(SingleVariableDeclaration e) {
    exception.set(e);
    return this;
  }

  public Block getBody() {
    return body.get();
  }
  
  public CatchClause setBody(Block newBody) {
    body.set(newBody);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      exception.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public CatchClause copy() {
    return new CatchClause(this);
  }
}
