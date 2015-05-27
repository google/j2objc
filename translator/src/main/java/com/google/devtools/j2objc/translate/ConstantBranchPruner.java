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
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InfixExpression.Operator;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.ThrowStatement;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.WhileStatement;

import java.util.List;

/**
 * Removes branches that are tested with boolean constant expressions
 * (like javac does).
 *
 * @author Tom Ball
 */
public class ConstantBranchPruner extends TreeVisitor {

  @Override
  public void endVisit(Block node) {
    // Truncate statement list if constant pruning causes early return.
    List<Statement> stmts = node.getStatements();
    int iReturn = -1;
    for (int i = 0; i < stmts.size(); i++) {
      Statement s = stmts.get(i);
      if (s instanceof ReturnStatement || s instanceof ThrowStatement) {
        iReturn = i;
        break;
      }
    }
    if (iReturn >= 0 && iReturn < stmts.size() - 1) {
      for (int i = iReturn + 1; i < stmts.size(); i++) {
        stmts.get(i).remove();
      }
      assert iReturn == stmts.size() - 1;
    }
  }

  @Override
  public void endVisit(DoStatement node) {
    if (getValue(node.getExpression()) == Boolean.FALSE) {
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
    if (operator != Operator.CONDITIONAL_AND && operator != Operator.CONDITIONAL_OR) {
      return;
    }
    List<Expression> operands = node.getOperands();
    int i = 0;
    boolean modified = false;
    while (i < operands.size()) {
      Boolean operand = getValue(operands.get(i));
      if (operand != null) {
        if (operand && operator == Operator.CONDITIONAL_OR) {
          // Whole expression is true.
          node.replaceWith(new BooleanLiteral(true, typeEnv));
          return;
        }
        if (!operand && operator == Operator.CONDITIONAL_AND) {
          // Whole expression is false.
          node.replaceWith(new BooleanLiteral(false, typeEnv));
          return;
        }
        else {
          // Remove unnecessary constant value.
          operands.remove(i);
          modified = true;
        }
        if (operands.size() == 1) {
          break;
        }
      } else {
        i++;
      }
    }
    if (!modified) {
      return;
    }
    assert !operands.isEmpty();

    if (operands.size() == 1) {
      Boolean b = getValue(operands.get(i));
      if (b != null) {
        node.replaceWith(new BooleanLiteral(b, typeEnv));
      } else {
        node.replaceWith(operands.get(i).copy());
      }
    } else {
      node.setOperands(operands);
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
    if (getValue(node.getExpression()) == Boolean.FALSE) {
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
