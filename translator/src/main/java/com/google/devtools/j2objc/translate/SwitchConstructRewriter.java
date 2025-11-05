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

import static com.google.common.base.Preconditions.checkState;

import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EmbeddedStatementExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InfixExpression.Operator;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.Pattern;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchConstruct;
import com.google.devtools.j2objc.ast.SwitchExpression;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.ast.YieldStatement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import java.util.List;
import javax.lang.model.element.VariableElement;

/**
 * Rewrites switch constructs flattening expressions into multiple cases and rescopes statements.
 *
 * @author Roberto Lublinerman
 */
public class SwitchConstructRewriter extends UnitTreeVisitor {
  public SwitchConstructRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(SwitchStatement node) {
    if (hasPatternsOrGuards(node.getStatements())) {
      // The switch expression with patterns or guards will be replaced by a nest of if statements
      // followed by a switch statement on an integer.
      Block replacement = new Block();
      generateSelectorLogic(node, replacement);
      node.replaceWith(replacement);
    }
  }

  @Override
  public void endVisit(SwitchExpression node) {
    if (hasPatternsOrGuards(node.getStatements())) {
      // The switch expression with patterns or guards will be replaced by a nest of if statements
      // followed by a switch statement on an integer.
      Block implementationBlock = new Block();
      generateSelectorLogic(node, implementationBlock);

      node.replaceWith(
          new EmbeddedStatementExpression()
              .setStatement(implementationBlock)
              .setTypeMirror(node.getTypeMirror()));
      return;
    }

    // Simple switch expressions are just converted into a switch statement inside an block
    // expression evaluated immediately.
    //
    //  ^{
    //      switch(...) {
    //        ...
    //      }
    //   }()
    node.replaceWith(
        new EmbeddedStatementExpression()
            .setStatement(new SwitchStatement(node))
            .setTypeMirror(node.getTypeMirror()));
  }

  @Override
  public void endVisit(YieldStatement node) {
    // Yield statements become returns from the block expression.
    node.replaceWith(new ReturnStatement(node.getExpression().copy()));
  }

  private boolean hasPatternsOrGuards(List<Statement> stmts) {
    for (Statement stmt : stmts) {
      if (stmt instanceof SwitchCase switchCase) {
        if (switchCase.getPattern() != null || switchCase.getGuard() != null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Transforms a switch with patterns or guards into an if/else nest that decides which case to
   * enter, and a switch on int that has the switch logic preserving the meaning of the unlabeled
   * breaks etc.
   */
  private void generateSelectorLogic(SwitchConstruct node, Block implementationBlock) {
    Expression expression = node.getExpression();
    if (!(expression instanceof SimpleName)) {
      // Generate a temporary variable to preserve evaluation semantics since we can't guarantee
      // that the expression doesn't have side effects and can be evaluated multiple times.
      VariableElement tempVariable =
          GeneratedVariableElement.newLocalVar(
              "tmp", expression.getTypeMirror(), TreeUtil.getEnclosingElement((TreeNode) node));
      // Type tmp = expr
      implementationBlock.addStatement(
          new VariableDeclarationStatement(tempVariable, expression.copy()));
      expression = new SimpleName(tempVariable);
    }

    // Generate an integer variable that will be the expression of the transformed switch.
    VariableElement selectorVariable =
        GeneratedVariableElement.newLocalVar(
            "selector", typeUtil.getInt(), TreeUtil.getEnclosingElement((TreeNode) node));
    // int selector = 0;
    // If the selector remains as 0, it means that no case matched and the default case is
    // executed.
    implementationBlock.addStatement(
        new VariableDeclarationStatement(selectorVariable, new NumberLiteral(0, typeUtil)));

    IfStatement lastIfStatement = null;
    int caseNumber = 1; // Start case numbers at 1 since 0 will be the default case.
    for (Statement statement : node.getStatements()) {
      if (!(statement instanceof SwitchCase switchCase)) {
        // Skip the statements, the if/else logic only needs to worry about the case expressions.
        continue;
      }
      if (switchCase.isDefault()) {
        // No need to handle the default case, the selector variable will be already 0 in that case.
        continue;
      }

      Expression condition = buildCondition(expression, switchCase);

      // if (condition) {
      //   selector = caseNumber;
      // } else ....
      IfStatement newCase =
          new IfStatement()
              .setExpression(condition)
              .setThenStatement(
                  new ExpressionStatement()
                      .setExpression(
                          new Assignment(
                              new SimpleName(selectorVariable),
                              new NumberLiteral(caseNumber, typeUtil))));

      if (lastIfStatement == null) {
        // This the first case, so we need to add it to the implementation block.
        implementationBlock.addStatement(newCase);
      } else {
        // Otherwise, we need to add it as the else statement of the previous case.
        lastIfStatement.setElseStatement(newCase);
      }

      lastIfStatement = newCase;

      // Replace the case expression with the caseNumber.
      switchCase.getExpressions().clear();
      switchCase.addExpression(new NumberLiteral(caseNumber, typeUtil));
      switchCase.setGuard(null);
      switchCase.setPattern(null);

      caseNumber++;
    }
    // Last, replace switch with one that uses the selector variable as the expression and with the
    // rewritten cases.
    implementationBlock.addStatement(
        new SwitchStatement()
            .setExpression(new SimpleName(selectorVariable))
            .copyStatements(node.getStatements()));
  }

  private Expression buildCondition(Expression switchExpression, SwitchCase switchCase) {
    Expression condition = null;
    checkState(switchCase.getExpressions().isEmpty());
    Pattern.BindingPattern pattern = (Pattern.BindingPattern) switchCase.getPattern();
    if (pattern != null) {
      condition =
          andCondition(
              condition,
              new InstanceofExpression()
                  .setTypeMirror(typeUtil.getBoolean())
                  .setLeftOperand(switchExpression.copy())
                  .setRightOperand(pattern.getVariable().getType().copy())
                  .setPattern(pattern.copy()));
    }
    if (switchCase.getGuard() != null) {
      condition = andCondition(condition, switchCase.getGuard().copy());
    }
    return condition;
  }

  private Expression andCondition(Expression lhs, Expression rhs) {
    if (lhs == null) {
      return rhs;
    }
    return new InfixExpression(typeUtil.getBoolean(), Operator.CONDITIONAL_AND, lhs, rhs);
  }
}
