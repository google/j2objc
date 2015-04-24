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
import static java.lang.Boolean.TRUE;

import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.WhileStatement;

/**
 * Removes branches that are tested with boolean constant expressions
 * (like javac does).
 *
 * @author Tom Ball
 */
public class ConstantBranchPruner extends TreeVisitor {

  @Override
  public void endVisit(DoStatement node) {
    if (getValue(node.getExpression()) == FALSE) {
      node.remove();
    }
  }

  @Override
  public void endVisit(IfStatement node) {
    Boolean value = getValue(node.getExpression());
    if (value != null) {
      if (value) {
        node.replaceWith(node.getThenStatement().copy());
      } else if (node.getElseStatement() != null) {
        node.replaceWith(node.getElseStatement().copy());
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
    Boolean left = getValue(node.getLeftOperand());
    Boolean right = getValue(node.getRightOperand());
    if (left != null && right != null) {
      if (operator == CONDITIONAL_AND) {
        node.replaceWith(new BooleanLiteral(left && right, typeEnv));
      } else {
        node.replaceWith(new BooleanLiteral(left || right, typeEnv));
      }
    } else if (left != null || right != null) {
      if (operator == CONDITIONAL_AND) {
        if (left == TRUE) {
          node.replaceWith(node.getRightOperand().copy());
        } else if (right == TRUE) {
          node.replaceWith(node.getLeftOperand().copy());
        } else {
          node.replaceWith(new BooleanLiteral(false, typeEnv));
        }
      } else {
        if (left == FALSE) {
          node.replaceWith(node.getRightOperand().copy());
        } else if (right == FALSE) {
          node.replaceWith(node.getLeftOperand().copy());
        } else {
          node.replaceWith(new BooleanLiteral(true, typeEnv));
        }
      }
    }
  }

  /**
   * Invert ! boolean constant expressions.
   */
  @Override
  public void endVisit(PrefixExpression node) {
    Boolean value = getValue(node.getOperand());
    if (node.getOperator() == PrefixExpression.Operator.NOT && value != null) {
      node.replaceWith(new BooleanLiteral(!value, typeEnv));
    }
  }

  /**
   * Remove parentheses around constant booleans.
   */
  @Override
  public void endVisit(ParenthesizedExpression node) {
    if (getValue(node.getExpression()) != null) {
      node.replaceWith(node.getExpression().copy());
    }
  }

  @Override
  public void endVisit(WhileStatement node) {
    if (getValue(node.getExpression()) == FALSE) {
      node.remove();
    }
  }

  /**
   * Returns TRUE or FALSE if expression is a boolean constant, else
   * null (unknown statically).
   */
  private Boolean getValue(Expression expr) {
    Object value = expr.getConstantValue();
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (expr instanceof BooleanLiteral) {
      return ((BooleanLiteral) expr).booleanValue();
    }
    return null;
  }
}
