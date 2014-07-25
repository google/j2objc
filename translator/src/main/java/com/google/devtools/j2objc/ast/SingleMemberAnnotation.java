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
 * Node type for an annotation with only a single parameter for the default value.
 */
public class SingleMemberAnnotation extends Annotation {

  public SingleMemberAnnotation(org.eclipse.jdt.core.dom.SingleMemberAnnotation jdtNode) {
    super(jdtNode);
  }

  public SingleMemberAnnotation(SingleMemberAnnotation other) {
    super(other);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      typeName.get().accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SingleMemberAnnotation copy() {
    return new SingleMemberAnnotation(this);
  }
}
