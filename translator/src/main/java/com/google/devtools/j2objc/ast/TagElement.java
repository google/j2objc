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
 * Node for a tag within a doc comment.
 */
public class TagElement extends TreeNode {

  public static final String TAG_AUTHOR = "@author";
  public static final String TAG_CODE = "@code";
  public static final String TAG_DEPRECATED = "@deprecated";
  public static final String TAG_EXCEPTION = "@exception";
  public static final String TAG_LINK = "@link";
  public static final String TAG_LINKPLAIN = "@linkplain";
  public static final String TAG_LITERAL = "@literal";
  public static final String TAG_PARAM = "@param";
  public static final String TAG_RETURN = "@return";
  public static final String TAG_SEE = "@see";
  public static final String TAG_SINCE = "@since";
  public static final String TAG_THROWS = "@throws";
  public static final String TAG_VERSION = "@version";

  private String tagName;
  private ChildList<TreeNode> fragments = ChildList.create(TreeNode.class, this);

  public TagElement() {}

  public TagElement(TagElement other) {
    super(other);
    tagName = other.getTagName();
    fragments.copyFrom(other.getFragments());
  }

  @Override
  public Kind getKind() {
    return Kind.TAG_ELEMENT;
  }

  public String getTagName() {
    return tagName;
  }

  public TagElement setTagName(String tagName) {
    this.tagName = tagName;
    return this;
  }

  public List<TreeNode> getFragments() {
    return fragments;
  }

  public TagElement addFragment(TreeNode fragment) {
    fragments.add(fragment);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      fragments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public TagElement copy() {
    return new TagElement(this);
  }
}
