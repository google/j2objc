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

  /**
   * The kinds of TagElements.
   */
  public enum TagKind {
    AUTHOR("@author"),
    CODE("@code"),
    DEPRECATED("@deprecated"),
    EXCEPTION("@exception"),
    LINK("@link"),
    LINKPLAIN("@linkplain"),
    LITERAL("@literal"),
    PARAM("@param"),
    RETURN("@return"),
    SEE("@see"),
    SINCE("@since"),
    THROWS("@throw"),
    VERSION("@version"),

    // Tag elements that don't have a tag name.
    DESCRIPTION(""),
    UNKNOWN("");

    private final String tagName;

    TagKind(String name) {
      tagName = name;
    }

    public static TagKind parse(String tagName) {
      for (TagKind tk : values()) {
        if (tagName.equals(tk.tagName)) {
          return tk;
        }
      }
      return UNKNOWN;
    }

    @Override
    public String toString() {
      return tagName;
    }
  }

  private TagKind tagKind;
  private ChildList<TreeNode> fragments = ChildList.create(TreeNode.class, this);

  public TagElement() {
    this.tagKind = TagKind.UNKNOWN;
  }

  public TagElement(TagElement other) {
    super(other);
    this.tagKind = other.getTagKind();
    fragments.copyFrom(other.getFragments());
  }

  @Override
  public Kind getKind() {
    return Kind.TAG_ELEMENT;
  }

  public TagKind getTagKind() {
    return tagKind;
  }

  public TagElement setTagKind(TagKind tagKind) {
    this.tagKind = tagKind;
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
