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

import com.google.common.base.Supplier;

/**
 * Base class for nodes in the J2ObjC AST.
 */
public abstract class TreeNode {

  private ChildLink<? extends TreeNode> owner = null;
  private int startPosition = -1;
  private int length = 0;
  private int lineNumber = -1;

  protected TreeNode() {
  }

  protected TreeNode(TreeNode other) {
    startPosition = other.getStartPosition();
    length = other.getLength();
    lineNumber = other.getLineNumber();
  }

  public abstract Kind getKind();

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

  public boolean canReplaceWith(Class<? extends TreeNode> type) {
    return owner != null && owner.getChildType().isAssignableFrom(type);
  }

  public void replaceWith(TreeNode other) {
    assert owner != null : "Can't replace a parentless node.";
    owner.setDynamic(other);
  }

  public void replaceWith(Supplier<? extends TreeNode> supplier) {
    ChildLink<? extends TreeNode> owner = this.owner;
    assert owner != null : "Can't replace a parentless node.";
    owner.set(null);
    owner.setDynamic(supplier.get());
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
    try {
      if (visitor.preVisit(this)) {
        acceptInner(visitor);
      }
      visitor.postVisit(this);
    } catch (TreeVisitorError e) {
      // Avoid re-wrapping.
      throw e;
    } catch (Throwable t) {
      throw new TreeVisitorError(t, this);
    }
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

  public TreeNode setPosition(SourcePosition position) {
    this.startPosition = position.getStartPosition();
    this.length = position.getLength();
    this.lineNumber = position.getLineNumber();
    return this;
  }

  @Override
  public String toString() {
    try {
      return DebugASTPrinter.toString(this);
    } catch (RuntimeException e) {
      // Debugger may sometimes call toString methods on an instance that is partially initialized.
      return super.toString();
    }
  }

  /**
   * Enumeration of all the node types. Useful for switch statements.
   */
  public enum Kind {
    ANNOTATION_TYPE_DECLARATION,
    ANNOTATION_TYPE_MEMBER_DECLARATION,
    ANY_PATTERN,
    ARRAY_ACCESS,
    ARRAY_CREATION,
    ARRAY_INITIALIZER,
    ARRAY_TYPE,
    ASSERT_STATEMENT,
    ASSIGNMENT,
    BINDING_PATTERN,
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
    RECORD_DECLARATION,
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
    SWITCH_EXPRESSION,
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
    WHILE_STATEMENT,
    YIELD_STATEMENT
  }
}
