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

import com.google.common.collect.Lists;

import java.util.AbstractList;
import java.util.List;

/**
 * List type for lists of child nodes. Nodes added or removed from a ChildList
 * are reparented appropriately.
 */
class ChildList<T extends TreeNode> extends AbstractList<T> {

  private final Class<T> childType;
  private final TreeNode parent;
  private List<ChildLink<T>> delegate = Lists.newArrayList();

  public ChildList(Class<T> childType, TreeNode parent) {
    this.childType = childType;
    this.parent = parent;
  }

  public static <T extends TreeNode> ChildList<T> create(Class<T> childType, TreeNode parent) {
    return new ChildList<T>(childType, parent);
  }

  @Override
  public T get(int index) {
    return delegate.get(index).get();
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public T set(int index, T node) {
    ChildLink<T> link = delegate.get(index);
    T oldNode = link.get();
    link.set(node);
    return oldNode;
  }

  @Override
  public void add(int index, T node) {
    ChildLink<T> link = new Link(childType, parent);
    link.set(node);
    delegate.add(index, link);
  }

  @Override
  public T remove(int index) {
    ChildLink<T> link = delegate.remove(index);
    T node = link.get();
    link.set(null);
    return node;
  }

  @SuppressWarnings("unchecked")
  public void copyFrom(List<T> other) {
    for (T elem : other) {
      add((T) elem.copy());
    }
  }

  public void accept(TreeVisitor visitor) {
    // Copy all the children into an array to avoid a
    // ConcurrentModificationException if the visitor removes one of the nodes.
    ChildLink<?>[] array = delegate.toArray(new ChildLink<?>[delegate.size()]);
    for (ChildLink<?> link : array) {
      link.accept(visitor);
    }
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  private class Link extends ChildLink<T> {

    public Link(Class<T> childType, TreeNode parent) {
      super(childType, parent);
    }

    @Override
    public void remove() {
      super.remove();
      delegate.remove(this);
    }
  }
}
