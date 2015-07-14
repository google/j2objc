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
 * Base class for nodes in the J2ObjC AST.
 */
public abstract class TreeNode {

  private ChildLink<? extends TreeNode> owner = null;
  private Key key;
  private int startPosition = -1;
  private int length = 0;
  private int lineNumber = -1;

  protected TreeNode() {
    key = new Key();
  }

  protected TreeNode(ASTNode jdtNode) {
    this();
    startPosition = jdtNode.getStartPosition();
    length = jdtNode.getLength();
    ASTNode root = jdtNode.getRoot();
    if (root instanceof org.eclipse.jdt.core.dom.CompilationUnit) {
      lineNumber = ((org.eclipse.jdt.core.dom.CompilationUnit) root).getLineNumber(startPosition);
    }
  }

  protected TreeNode(TreeNode other) {
    key = other.getKey();
    startPosition = other.getStartPosition();
    length = other.getLength();
    lineNumber = other.getLineNumber();
  }

  public abstract Kind getKind();

  public Key getKey() {
    return key;
  }

  public void setKey(Key newKey) {
    key = newKey;
  }

  public TreeNode getParent() {
    return owner == null ? null : owner.getParent();
  }

  /* package */ void setOwner(ChildLink<? extends TreeNode> newOwner) {
    assert owner == null || newOwner == null : "Node is already parented";
    owner = newOwner;
  }

  public void remove() {
    if (owner != null) {
      owner.remove();
    }
  }

  public void replaceWith(TreeNode other) {
    assert owner != null : "Can't replace a parentless node.";
    owner.setDynamic(other);
  }

  public final int getStartPosition() {
    return startPosition;
  }

  public final int getLength() {
    return length;
  }

  public void setSourceRange(int newStartPosition, int newLength) {
    startPosition = newStartPosition;
    length = newLength;
  }

  public final int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public final void accept(TreeVisitor visitor) {
    if (visitor.preVisit(this)) {
      acceptInner(visitor);
    }
    visitor.postVisit(this);
  }

  protected abstract void acceptInner(TreeVisitor visitor);

  /**
   * Returns an unparented deep copy of this node.
   */
  public abstract TreeNode copy();

  /**
   * Validates the tree to preemptively catch errors.
   */
  public final void validate() {
    this.accept(new TreeVisitor() {
      @Override
      public boolean preVisit(TreeNode node) {
        node.validateInner();
        return true;
      }
    });
  }

  public void validateInner() {}

  public String toString() {
    try {
      return DebugASTPrinter.toString(this);
    } catch (RuntimeException e) {
      // Debugger may sometimes call toString methods on an instance that is partially initialized.
      return super.toString();
    }
  }

  /**
   * Key type for associating nodes with additional data. Keys are normally
   * unique to a node, except that copies keep the same key as the original.
   */
  public static class Key {
    private Key() {}
  }

  /**
   * Enumeration of all the node types. Useful for switch statements.
   */
  public enum Kind {
    ANNOTATION_TYPE_DECLARATION,
    ANNOTATION_TYPE_MEMBER_DECLARATION,
    ANONYMOUS_CLASS_DECLARATION,
    ARRAY_ACCESS,
    ARRAY_CREATION,
    ARRAY_INITIALIZER,
    ARRAY_TYPE,
    ASSERT_STATEMENT,
    ASSIGNMENT,
    BLOCK,
    BLOCK_COMMENT,
    BOOLEAN_LITERAL,
    BREAK_STATEMENT,
    C_STRING_LITERAL,
    CAST_EXPRESSION,
    CATCH_CLAUSE,
    CHARACTER_LITERAL,
    CLASS_INSTANCE_CREATION,
    COMMA_EXPRESSION,
    COMPILATION_UNIT,
    CONDITIONAL_EXPRESSION,
    CONSTRUCTOR_INVOCATION,
    CONTINUE_STATEMENT,
    CREATION_REFERENCE,
    DIMENSION,
    DO_STATEMENT,
    EMPTY_STATEMENT,
    ENHANCED_FOR_STATEMENT,
    ENUM_CONSTANT_DECLARATION,
    ENUM_DECLARATION,
    EXPRESSION_METHOD_REFERENCE,
    EXPRESSION_STATEMENT,
    FIELD_ACCESS,
    FIELD_DECLARATION,
    FOR_STATEMENT,
    FUNCTION_DECLARATION,
    FUNCTION_INVOCATION,
    IF_STATEMENT,
    INFIX_EXPRESSION,
    INITIALIZER,
    INSTANCEOF_EXPRESSION,
    INTERSECTION_TYPE,
    JAVADOC,
    LABELED_STATEMENT,
    LAMBDA_EXPRESSION,
    LINE_COMMENT,
    MARKER_ANNOTATION,
    MEMBER_VALUE_PAIR,
    METHOD_DECLARATION,
    METHOD_INVOCATION,
    NAME_QUALIFIED_TYPE,
    NATIVE_DECLARATION,
    NATIVE_EXPRESSION,
    NATIVE_STATEMENT,
    NORMAL_ANNOTATION,
    NULL_LITERAL,
    NUMBER_LITERAL,
    PACKAGE_DECLARATION,
    PARAMETERIZED_TYPE,
    PARENTHESIZED_EXPRESSION,
    POSTFIX_EXPRESSION,
    PREFIX_EXPRESSION,
    PRIMITIVE_TYPE,
    PROPERTY_ANNOTATION,
    QUALIFIED_NAME,
    QUALIFIED_TYPE,
    RETURN_STATEMENT,
    SIMPLE_NAME,
    SIMPLE_TYPE,
    SINGLE_MEMBER_ANNOTATION,
    SINGLE_VARIABLE_DECLARATION,
    STRING_LITERAL,
    SUPER_CONSTRUCTOR_INVOCATION,
    SUPER_METHOD_INVOCATION,
    SUPER_METHOD_REFERENCE,
    SUPER_FIELD_ACCESS,
    SWITCH_CASE,
    SWITCH_STATEMENT,
    SYNCHRONIZED_STATEMENT,
    TAG_ELEMENT,
    TEXT_ELEMENT,
    THIS_EXPRESSION,
    THROW_STATEMENT,
    TRY_STATEMENT,
    TYPE_DECLARATION,
    TYPE_DECLARATION_STATEMENT,
    TYPE_LITERAL,
    TYPE_METHOD_REFERENCE,
    UNION_TYPE,
    VARIABLE_DECLARATION_EXPRESSION,
    VARIABLE_DECLARATION_FRAGMENT,
    VARIABLE_DECLARATION_STATEMENT,
    WHILE_STATEMENT
  }
}
