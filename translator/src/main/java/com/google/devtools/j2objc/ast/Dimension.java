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
 * AST node for an array dimension (added in Eclipse JLS8 support, to handle annotations on
 * ArrayType dimensions).
 */
// TODO(kirbs): Clean up ad-hoc dimension support that we had to implement previously.
public class Dimension extends TreeNode {

  protected ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);

  public Dimension() {}

  public Dimension(Dimension other) {
    super(other);
    annotations.copyFrom(other.annotations());
  }

  @Override
  public Kind getKind() {
    return Kind.DIMENSION;
  }

  public List<Annotation> annotations() {
    return annotations;
  }

  public Dimension addAnnotation(Annotation a) {
    annotations.add(a);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      annotations.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public Dimension copy() {
    return new Dimension(this);
  }
}
