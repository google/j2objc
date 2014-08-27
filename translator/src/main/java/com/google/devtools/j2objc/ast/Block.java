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
 * Node for a block of statements.
 */
public class Block extends Statement {

  private ChildList<Statement> statements = ChildList.create(Statement.class, this);
  private boolean hasAutoreleasePool = false;

  public Block(org.eclipse.jdt.core.dom.Block jdtNode) {
    super(jdtNode);
    for (Object statement : jdtNode.statements()) {
      statements.add((Statement) TreeConverter.convert(statement));
    }
  }

  public Block(Block other) {
    super(other);
    statements.copyFrom(other.getStatements());
    hasAutoreleasePool = other.hasAutoreleasePool();
  }

  public Block() {}

  @Override
  public Kind getKind() {
    return Kind.BLOCK;
  }

  public List<Statement> getStatements() {
    return statements;
  }

  public boolean hasAutoreleasePool() {
    return hasAutoreleasePool;
  }

  public void setHasAutoreleasePool(boolean newHasAutoreleasePool) {
    hasAutoreleasePool = newHasAutoreleasePool;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      statements.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public Block copy() {
    return new Block(this);
  }
}
