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

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Base class for nodes in the J2ObjC AST.
 */
public abstract class TreeNode {

  private ChildLink<? extends TreeNode> owner = null;
  private int startPosition = -1;
  private int length = 0;

  protected TreeNode() {}

  protected TreeNode(ASTNode jdtNode) {
    startPosition = jdtNode.getStartPosition();
    length = jdtNode.getLength();
  }

  protected TreeNode(TreeNode other) {
  }

  public TreeNode getParent() {
    return owner == null ? null : owner.getParent();
  }

  /* package */ void setOwner(ChildLink<? extends TreeNode> newOwner) {
    assert owner == null || newOwner == null : "Node is already parented";
    owner = newOwner;
  }

  public final int getStartPosition() {
    return startPosition;
  }

  public final int getLength() {
    return length;
  }

  public void accept(TreeVisitor visitor) {
    if (visitor.preVisit(this)) {
      acceptInner(visitor);
    }
    visitor.postVisit(this);
  }

  protected abstract void acceptInner(TreeVisitor visitor);

  /**
   * Returns an unparented deep copy of this node.
   */
  public abstract TreeNode copy();

  /**
   * Validates the tree to preemptively catch errors.
   */
  public void validate() {}
}
