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
 * Node type for a member reference within a javadoc comment.
 */
public class MemberRef extends TreeNode {

  public MemberRef(org.eclipse.jdt.core.dom.MemberRef jdtNode) {
    super(jdtNode);
  }

  public MemberRef(MemberRef other) {
    super(other);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public MemberRef copy() {
    return new MemberRef(this);
  }
}
