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
  private int lineNumber = -1;

  protected TreeNode() {}

  protected TreeNode(ASTNode jdtNode) {
    startPosition = jdtNode.getStartPosition();
    length = jdtNode.getLength();
    ASTNode root = jdtNode.getRoot();
    if (root instanceof org.eclipse.jdt.core.dom.CompilationUnit) {
      lineNumber = ((org.eclipse.jdt.core.dom.CompilationUnit) root).getLineNumber(startPosition);
    }
  }

  protected TreeNode(TreeNode other) {
    startPosition = other.getStartPosition();
    length = other.getLength();
    lineNumber = other.getLineNumber();
  }

  public TreeNode getParent() {
    return owner == null ? null : owner.getParent();
  }

  /* package */ void setOwner(ChildLink<? extends TreeNode> newOwner) {
    assert owner == null || newOwner == null : "Node is already parented";
    owner = newOwner;
  }

  public void replaceWith(TreeNode other) {
    assert owner != null : "Can't replace a parentless node.";
    owner.setDynamic(other);
  }

  public final int getStartPosition() {
    return startPosition;
  }

  public final int getLength() {
    return length;
  }

  public void setSourceRange(int newStartPosition, int newLength) {
    startPosition = newStartPosition;
    length = newLength;
  }

  public final int getLineNumber() {
    return lineNumber;
  }

  public final void accept(TreeVisitor visitor) {
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
  public final void validate() {
    this.accept(new TreeVisitor() {
      @Override
      public boolean preVisit(TreeNode node) {
        node.validateInner();
        return true;
      }
    });
  }

  public void validateInner() {}
}
