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

package com.google.devtools.j2objc.translate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.max;

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EmbeddedStatementExpression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;

/**
 * Extracts variable declarations from comma expressions into a appropriate block.
 *
 * @author Roberto Lublinerman
 */
public class VariableDeclarationHoister extends UnitTreeVisitor {

  public VariableDeclarationHoister(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(VariableDeclarationExpression node) {
    if (node.getParent() instanceof CommaExpression) {
      var declaration = new VariableDeclarationStatement();
      for (var fragment : node.getFragments()) {
        declaration.addFragment(fragment.copy());
      }

      // Find the block where to declare the variable.
      var block = getBlockForVariableDeclarations(node);

      // Find the statement this expression is in so that the declaration can be inserted just
      // before that statement.
      var insertionIndex = max(block.getStatements().indexOf(getParentStatement(node)), 0);
      block.addStatement(insertionIndex, declaration);

      // Remove declaration from comma expression.
      node.remove();
    }
  }

  @Override
  public void endVisit(CommaExpression node) {
    if (node.getExpressions().size() == 1) {
      var inner = node.getExpressions().get(0);
      inner.remove();
      node.replaceWith(inner);
    }
  }

  /**
   * Find a parent statement that is inside a block which will give a location where to hoist
   * variables to.
   */
  private static Statement getParentStatement(TreeNode node) {
    if (node == null) {
      // No appropriate parent statement has been found. This might be the case where the
      // declaration happened in a field initializer.
      return null;
    }

    var parent = node.getParent();
    if (parent instanceof Statement stmt && parent.getParent() instanceof Block) {
      // This is a parent statement that is directly inside a block where the variable can be
      // declared.
      return stmt;
    }

    // The parent might be a statement here, but it is not directly inside a block; e.g. it might
    // be the statement in an loop or if construct. Continue searching for a statement that is
    // directly inside a block.
    return getParentStatement(parent);
  }

  /** Returns an enclosing block where to declare the pattern and temporary variables. */
  private static Block getBlockForVariableDeclarations(TreeNode node) {
    while (!(node instanceof Block block)) {
      node = node.getParent();
      checkState(!(node instanceof LambdaExpression || node instanceof Type));
      if (node instanceof FieldDeclaration fieldDeclaration) {
        // The instanceof pattern is in a field initializer, no suitable block is found to
        // declare the temporary variables. Hence, create a new block where to declare the variables
        // and replace the field initializer with an embedded statement containing that block.
        var block = new Block();
        var embeddedStatement =
            new EmbeddedStatementExpression()
                .setTypeMirror(fieldDeclaration.getTypeMirror())
                .setStatement(block);

        // Reattach the initializer as the return value of the embedded statement.
        var initializer = fieldDeclaration.getFragment().getInitializer();
        initializer.remove();
        block.addStatement(new ReturnStatement().setExpression(initializer));
        fieldDeclaration.getFragment().setInitializer(embeddedStatement);

        // And return an enclosing block where variables can be declared.
        return block;
      }
    }
    return checkNotNull(block);
  }
}
