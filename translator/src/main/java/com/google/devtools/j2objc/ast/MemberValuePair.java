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

import com.google.devtools.j2objc.jdt.TreeConverter;

/**
 * Node type for a member value pair in a normal annotation.
 */
public class MemberValuePair extends TreeNode {

  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildLink<Expression> value = ChildLink.create(Expression.class, this);

  public MemberValuePair(org.eclipse.jdt.core.dom.MemberValuePair jdtNode) {
    super(jdtNode);
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    value.set((Expression) TreeConverter.convert(jdtNode.getValue()));
  }

  public MemberValuePair(MemberValuePair other) {
    super(other);
    name.copyFrom(other.getName());
    value.copyFrom(other.getValue());
  }

  @Override
  public Kind getKind() {
    return Kind.MEMBER_VALUE_PAIR;
  }

  public SimpleName getName() {
    return name.get();
  }

  public Expression getValue() {
    return value.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      name.accept(visitor);
      value.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public MemberValuePair copy() {
    return new MemberValuePair(this);
  }
}
