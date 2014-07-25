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

  private final TreeNode parent;
  private List<ChildLink<T>> delegate = Lists.newArrayList();

  public ChildList(TreeNode parent) {
    this.parent = parent;
  }

  public static <T extends TreeNode> ChildList<T> create(TreeNode parent) {
    return new ChildList<T>(parent);
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
  public boolean add(T node) {
    ChildLink<T> link = ChildLink.create(parent);
    link.set(node);
    return delegate.add(link);
  }

  @SuppressWarnings("unchecked")
  public void copyFrom(List<T> other) {
    for (T elem : other) {
      add((T) elem.copy());
    }
  }

  public void accept(TreeVisitor visitor) {
    for (ChildLink<T> link : delegate) {
      link.accept(visitor);
    }
  }
}
