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
 * A link between a parent and child node that allows for efficient swapping of
 * nodes and handles reparenting of the old and new node when setting a child.
 */
class ChildLink<T extends TreeNode> {

  private final TreeNode parent;
  private T child = null;

  public ChildLink(TreeNode parent) {
    this.parent = parent;
  }

  public static <T extends TreeNode> ChildLink<T> create(TreeNode parent) {
    return new ChildLink<T>(parent);
  }

  public TreeNode getParent() {
    return parent;
  }

  public T get() {
    return child;
  }

  public void set(T newChild) {
    if (child == newChild) {
      return;
    }
    if (child != null) {
      child.setOwner(null);
    }
    if (newChild != null) {
      newChild.setOwner(this);
    }
    child = newChild;
  }

  @SuppressWarnings("unchecked")
  public void copyFrom(T other) {
    set((T) other.copy());
  }

  public void accept(TreeVisitor visitor) {
    if (child != null) {
      child.accept(visitor);
    }
  }
}
