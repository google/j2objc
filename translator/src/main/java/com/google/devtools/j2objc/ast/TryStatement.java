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

import java.util.List;

/**
 * Try/catch statement node type.
 */
public class TryStatement extends Statement {

  private ChildList<VariableDeclarationExpression> resources =
      ChildList.create(VariableDeclarationExpression.class, this);
  private ChildLink<Block> body = ChildLink.create(Block.class, this);
  private ChildList<CatchClause> catchClauses = ChildList.create(CatchClause.class, this);
  private ChildLink<Block> finallyBlock = ChildLink.create(Block.class, this);

  public TryStatement() {}

  public TryStatement(TryStatement other) {
    super(other);
    resources.copyFrom(other.getResources());
    body.copyFrom(other.getBody());
    catchClauses.copyFrom(other.getCatchClauses());
    finallyBlock.copyFrom(other.getFinally());
  }

  @Override
  public Kind getKind() {
    return Kind.TRY_STATEMENT;
  }

  public List<VariableDeclarationExpression> getResources() {
    return resources;
  }

  public TryStatement addResource(VariableDeclarationExpression resource) {
    resources.add(resource);
    return this;
  }

  public Block getBody() {
    return body.get();
  }

  public TryStatement setBody(Block newBody) {
    body.set(newBody);
    return this;
  }

  public List<CatchClause> getCatchClauses() {
    return catchClauses;
  }

  public TryStatement addCatchClause(CatchClause newClause) {
    catchClauses.add(newClause);
    return this;
  }

  public Block getFinally() {
    return finallyBlock.get();
  }

  public TryStatement setFinally(Block newFinally) {
    finallyBlock.set(newFinally);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      resources.accept(visitor);
      body.accept(visitor);
      catchClauses.accept(visitor);
      finallyBlock.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public TryStatement copy() {
    return new TryStatement(this);
  }
}
