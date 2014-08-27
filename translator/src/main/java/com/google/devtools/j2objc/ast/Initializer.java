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

import com.google.common.base.Preconditions;

/**
 * Node type for an initializer block.
 */
public class Initializer extends BodyDeclaration {

  private final ChildLink<Block> body = ChildLink.create(Block.class, this);

  public Initializer(org.eclipse.jdt.core.dom.Initializer jdtNode) {
    super(jdtNode);
    body.set((Block) TreeConverter.convert(jdtNode.getBody()));
  }

  public Initializer(Initializer other) {
    super(other);
    body.copyFrom(other.getBody());
  }

  @Override
  public Kind getKind() {
    return Kind.INITIALIZER;
  }

  public Block getBody() {
    return body.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      // annotations should be empty.
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public Initializer copy() {
    return new Initializer(this);
  }

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkState(annotations.isEmpty());
    Preconditions.checkNotNull(body.get());
  }
}
