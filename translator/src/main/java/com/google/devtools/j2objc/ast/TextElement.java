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

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Node for text within a doc comment.
 */
public class TextElement extends TreeNode {

  private String text;

  public TextElement(org.eclipse.jdt.core.dom.TextElement jdtNode) {
    super(jdtNode);
    text = jdtNode.getText();
  }

  public TextElement(ASTNode jdtNode) {
    super(jdtNode);
    text = jdtNode.toString();
  }

  public TextElement(TextElement other) {
    super(other);
    text = other.getText();
  }

  @Override
  public Kind getKind() {
    return Kind.TEXT_ELEMENT;
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return text;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public TextElement copy() {
    return new TextElement(this);
  }
}
