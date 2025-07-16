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

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.Pattern;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SwitchExpression;
import com.google.devtools.j2objc.ast.SwitchExpressionCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.List;
import javax.lang.model.element.VariableElement;

/**
 * Rewrites switches that have patterns or guards to be if/else statements,
 * using those patterns as the new statements' conditional expressions.
 */
public class SwitchCaseRewriter extends UnitTreeVisitor {

  public SwitchCaseRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(SwitchStatement node) {
    if (hasPatternsOrGuards(node.getStatements())) {
      IfStatement rewrite = rewriteCases(node.getExpression(), node.getStatements());
      node.replaceWith(rewrite);
    }
  }

  @Override
  public void endVisit(SwitchExpression node) {
    if (hasPatternsOrGuards(node.getStatements())) {
      IfStatement rewrite = rewriteCases(node.getExpression(), node.getStatements());
      TreeNode parent = node.getParent();
      while (!(parent instanceof Statement)) {
        parent = parent.getParent();
      }
      parent.replaceWith(rewrite);
    }
  }

  private boolean hasPatternsOrGuards(List<Statement> stmts) {
    for (Statement stmt : stmts) {
      if (stmt.getKind() == TreeNode.Kind.SWITCH_EXPRESSION_CASE) {
        SwitchExpressionCase caseExprStmt = (SwitchExpressionCase) stmt;
        if (caseExprStmt.getPattern() != null || caseExprStmt.getGuard() != null) {
          return true;
        }
      }
    }
    return false;
  }

  private IfStatement rewriteCases(Expression switchExpression, List<Statement> cases) {
    IfStatement result = null;
    Statement defaultStmt = null;
    IfStatement lastCase = null;
    for (Statement stmt : cases) {
      if (stmt.getKind() == TreeNode.Kind.SWITCH_EXPRESSION_CASE) {
        SwitchExpressionCase switchExpressionCase = (SwitchExpressionCase) stmt;
        if (switchExpressionCase.isDefault()) {
          defaultStmt = (Statement) switchExpressionCase.getBody().copy();
          continue;
        }
        IfStatement ifCase = rewriteSwitchExpressionCase(switchExpression, switchExpressionCase);
        if (result == null) {
          result = ifCase;
        } else {
          lastCase.setElseStatement(ifCase);
        }
        lastCase = ifCase;
      } else {
        if (stmt.getKind() == TreeNode.Kind.BREAK_STATEMENT) {
          continue;
        }
        Statement lastCaseStmt = lastCase.getThenStatement();
        if (lastCaseStmt.getKind() == TreeNode.Kind.BLOCK) {
          ((Block) lastCaseStmt).addStatement(stmt.copy());
        } else {
          // Move non-block statement to new block, then add current statement.
          Block block = new Block();
          block.addStatement(lastCaseStmt.copy());
          lastCaseStmt.replaceWith(block);
        }
      }
    }
    if (result != null && defaultStmt != null) {
      // Find last else-if.
      IfStatement ifStmt = result;
      while (ifStmt.getElseStatement() != null) {
        ifStmt = (IfStatement) ifStmt.getElseStatement();
      }
      ifStmt.setElseStatement(blockify(defaultStmt));
    }
    return result;
  }

  private IfStatement rewriteSwitchExpressionCase(
      Expression switchExpression, SwitchExpressionCase switchExpressionCase) {
    IfStatement result = null;
    if (switchExpressionCase.getPattern() != null) {
      Pattern pattern = switchExpressionCase.getPattern();
      if (pattern.getKind() == TreeNode.Kind.BINDING_PATTERN) {
        Pattern.BindingPattern bindingPattern = (Pattern.BindingPattern) pattern;
        VariableElement var = bindingPattern.getVariable().getVariableElement();
        InstanceofExpression instanceofExpression =
            new InstanceofExpression()
                .setLeftOperand(switchExpression.copy())
                .setRightOperand(Type.newType(var.asType()));
        CastExpression castExpr = new CastExpression(var.asType(), switchExpression.copy());
        Expression varCast = new ParenthesizedExpression().setExpression(castExpr);
        Expression guard = switchExpressionCase.getGuard();
        if (guard != null) {
          InfixExpression ifExpr = new InfixExpression(
              TypeUtil.BOOL_TYPE,
              InfixExpression.Operator.CONDITIONAL_AND,
              instanceofExpression,
              (Expression) replacePatternVarWithCast(var, guard, varCast));
          Statement thenStatement = (Statement) replacePatternVarWithCast(
              var, blockify((Statement) switchExpressionCase.getBody().copy()), varCast);
          result = new IfStatement()
              .setExpression(ifExpr)
              .setThenStatement(thenStatement);
        } else {
          Block block = new Block();
          Statement body = (Statement) switchExpressionCase.getBody().copy();
          block.addStatement((Statement) replacePatternVarWithCast(var, body, varCast));
          result = new IfStatement()
              .setExpression(instanceofExpression.copy())
              .setThenStatement(block);
        }
      } else {
        ErrorUtil.error("unimplemented pattern kind: " + pattern.getKind());
      }
    } else {
      ErrorUtil.error("switch expression case without pattern not yet implemented");
    }
    return result;
  }

  // Replace all references in an expression referencing the pattern variable to cast equivalents.
  //
  // For example, with "case String s when s.isEmpty() && s.length() > 5 -> return s;",
  // "String s" is the pattern and "s" is the variable;
  // "s.isEmpty() && s.length() > 5" is the guard; and
  // "((String) objc)" is the cast expression. This method would return
  // "((String) objc).isEmpty() && ((String) objc).length() > 5" when the guard is the expr,
  // and "return ((String) objc); is when the case expression's body is the expr.
  private TreeNode replacePatternVarWithCast(
      VariableElement var, TreeNode node, Expression castExpr) {
    node.accept(new TreeVisitor() {
      @Override
      public void endVisit(SimpleName node) {
        if (node.getElement().equals(var)) {
          node.replaceWith(castExpr.copy());
        }
      }
    });
    return node.copy();
  }

  // Not strictly necessary, but makes generated source more readable.
  private Statement blockify(Statement stmt) {
    if (stmt.getKind() == TreeNode.Kind.BLOCK) {
      return stmt;
    }
    return new Block().addStatement(stmt);
  }
}
