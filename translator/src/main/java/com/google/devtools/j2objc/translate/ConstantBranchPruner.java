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

import static com.google.devtools.j2objc.ast.InfixExpression.Operator.CONDITIONAL_AND;
import static com.google.devtools.j2objc.ast.InfixExpression.Operator.CONDITIONAL_OR;
import static java.lang.Boolean.FALSE;

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.util.TranslationUtil;

import java.util.List;

/**
 * Removes branches that are tested with boolean constant expressions
 * (like javac does).
 *
 * @author Tom Ball
 */
public class ConstantBranchPruner extends TreeVisitor {

  /**
   * Removes all unreachable statements that occur after a return statement in
   * the given Block. Also recurses into child blocks.
   */
  private boolean removeUnreachable(Block block) {
    List<Statement> stmts = block.getStatements();
    for (int i = 0; i < stmts.size(); i++) {
      Statement stmt = stmts.get(i);
      if (stmt instanceof ReturnStatement
          || (stmt instanceof Block && removeUnreachable((Block) stmt))) {
        stmts.subList(i + 1, stmts.size()).clear();
        return true;
      }
    }
    return false;
  }

  @Override
  public void endVisit(Block node) {
    removeUnreachable(node);
  }

  @Override
  public void endVisit(ConditionalExpression node) {
    Expression expr = node.getExpression();
    Boolean value = getReplaceableValue(expr);
    if (value != null) {
      Expression result = value ? node.getThenExpression() : node.getElseExpression();
      node.replaceWith(result.copy());
    }
  }

  @Override
  public void endVisit(IfStatement node) {
    Expression expr = node.getExpression();
    Boolean value = getKnownValue(expr);
    if (value != null) {
      Statement sideEffects = getSideEffects(expr);
      if (sideEffects != null) {
        TreeUtil.insertBefore(node, sideEffects);
      }
      if (value) {
        node.replaceWith(TreeUtil.remove(node.getThenStatement()));
      } else if (node.getElseStatement() != null) {
        node.replaceWith(TreeUtil.remove(node.getElseStatement()));
      } else {
        node.remove();
      }
    }
  }

  @Override
  public void endVisit(InfixExpression node) {
    InfixExpression.Operator operator = node.getOperator();
    if (operator != CONDITIONAL_AND && operator != CONDITIONAL_OR) {
      return;
    }
    List<Expression> operands = node.getOperands();
    int lastSideEffect = -1;
    for (int i = 0; i < operands.size(); i++) {
      Expression expr = operands.get(i);
      if (TranslationUtil.hasSideEffect(expr)) {
        lastSideEffect = i;
      }
      Boolean knownVal = getKnownValue(expr);
      if (knownVal == null) {
        continue;
      }
      if (knownVal == (operator == CONDITIONAL_OR)) {
        // Whole expression evaluates to 'knownVal'.
        operands.subList(lastSideEffect + 1, operands.size()).clear();
        if (lastSideEffect < i) {
          operands.add(new BooleanLiteral(knownVal, typeEnv));
        }
        break;
      } else if (lastSideEffect < i) {
        // Else remove unnecessary constant value.
        operands.remove(i--);
      }
    }

    if (operands.size() == 0) {
      if (operator == CONDITIONAL_OR) {
        // All constants must have been false, because a true value would have
        // caused us to return in the loop above.
        node.replaceWith(new BooleanLiteral(false, typeEnv));
      } else {
        // Likewise, all constants must have been true.
        node.replaceWith(new BooleanLiteral(true, typeEnv));
      }
    } else if (operands.size() == 1) {
      node.replaceWith(operands.remove(0));
    }
  }

  /**
   * Invert ! boolean constant expressions.
   */
  @Override
  public void endVisit(PrefixExpression node) {
    Boolean value = getReplaceableValue(node.getOperand());
    if (node.getOperator() == PrefixExpression.Operator.NOT && value != null) {
      node.replaceWith(new BooleanLiteral(!value, typeEnv));
    }
  }

  /**
   * Remove parentheses around constant booleans.
   */
  @Override
  public void endVisit(ParenthesizedExpression node) {
    if (getReplaceableValue(node.getExpression()) != null) {
      node.replaceWith(node.getExpression().copy());
    }
  }

  @Override
  public void endVisit(WhileStatement node) {
    Expression expr = node.getExpression();
    if (getKnownValue(expr) == FALSE) {
      Statement sideEffects = getSideEffects(expr);
      if (sideEffects != null) {
        node.replaceWith(sideEffects);
      } else {
        node.remove();
      }
    }
  }

  /**
   * Returns TRUE or FALSE if expression is a boolean constant and has no side
   * effects, else null (unknown statically).
   */
  private Boolean getReplaceableValue(Expression expr) {
    return TranslationUtil.hasSideEffect(expr) ? null : getKnownValue(expr);
  }

  /**
   * Returns TRUE of FALSE if 'expr' is a boolean expression and its value is
   * known statically. The caller should be careful when replacing this
   * expression as it may have side effects.
   */
  private Boolean getKnownValue(Expression expr) {
    Object value = expr.getConstantValue();
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    switch (expr.getKind()) {
      case BOOLEAN_LITERAL:
        return ((BooleanLiteral) expr).booleanValue();
      case INFIX_EXPRESSION:
        {
          InfixExpression infixExpr = (InfixExpression) expr;
          InfixExpression.Operator op = infixExpr.getOperator();
          if (op == CONDITIONAL_AND || op == CONDITIONAL_OR) {
            // We assume that this node has already been visited and pruned so
            // if it has a known value, it will be equal to the last operand.
            List<Expression> operands = infixExpr.getOperands();
            Boolean lastOperand = getKnownValue(operands.get(operands.size() - 1));
            if (lastOperand != null && lastOperand.booleanValue() == (op == CONDITIONAL_OR)) {
              return lastOperand;
            }
          }
          return null;
        }
      case PARENTHESIZED_EXPRESSION:
        return getKnownValue(((ParenthesizedExpression) expr).getExpression());
      default:
        return null;
    }
  }

  /**
   * Extracts side effects from the given expression and returns the statement
   * to insert.
   */
  private Statement getSideEffects(Expression expr) {
    Expression sideEffectsExpr = extractSideEffects(expr);
    if (sideEffectsExpr == null) {
      return null;
    }
    return new ExpressionStatement(new CastExpression(
        typeEnv.resolveJavaType("void"), ParenthesizedExpression.parenthesize(sideEffectsExpr)));
  }

  /**
   * Returns an expression containing the side effects of the given expression.
   * The evaluated result of the expression may differ from the original.
   */
  private Expression extractSideEffects(Expression expr) {
    switch (expr.getKind()) {
      case INFIX_EXPRESSION:
        {
          List<Expression> operands = ((InfixExpression) expr).getOperands();
          Expression lastOperand = operands.remove(operands.size() - 1);
          lastOperand = extractSideEffects(lastOperand);
          if (lastOperand != null) {
            operands.add(lastOperand);
          }
          if (operands.size() == 1) {
            return operands.remove(0);
          }
          return TreeUtil.remove(expr);
        }
      case PARENTHESIZED_EXPRESSION:
        {
          Expression sideEffects = extractSideEffects(
              ((ParenthesizedExpression) expr).getExpression());
          if (sideEffects != null) {
            return ParenthesizedExpression.parenthesize(sideEffects);
          }
          return null;
        }
      default:
        return null;
    }
  }
}
