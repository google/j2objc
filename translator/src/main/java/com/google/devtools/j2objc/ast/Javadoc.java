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
 * Node for a javadoc-style comment.
 */
public class Javadoc extends Comment {

  private ChildList<TagElement> tags = ChildList.create(TagElement.class, this);

  public Javadoc(org.eclipse.jdt.core.dom.Javadoc jdtNode) {
    super(jdtNode);
    for (Object tag : jdtNode.tags()) {
      tags.add((TagElement) TreeConverter.convert(tag));
    }
  }

  public Javadoc(Javadoc other) {
    super(other);
    tags.copyFrom(other.getTags());
  }

  @Override
  public Kind getKind() {
    return Kind.JAVADOC;
  }

  public List<TagElement> getTags() {
    return tags;
  }

  @Override
  public boolean isDocComment() {
    return true;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      tags.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public Javadoc copy() {
    return new Javadoc(this);
  }
}
