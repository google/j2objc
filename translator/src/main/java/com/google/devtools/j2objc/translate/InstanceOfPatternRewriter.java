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

import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InfixExpression.Operator;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.Pattern;
import com.google.devtools.j2objc.ast.Pattern.BindingPattern;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.lang.model.element.VariableElement;

/**
 * Rewrites instance of patterns.
 *
 * <p>Rewrites instance of patterns by extracting the variable declaration and rewriting {@code expr
 * instanceof Class c} as {@code (X tmp = expr, c = tmp instanceof C ? (c = (C) tmp): null, c !=
 * null)}
 *
 * @author Roberto Lublinerman
 */
public class InstanceOfPatternRewriter extends UnitTreeVisitor {

  private Deque<Block> enclosingScopes = new ArrayDeque<>();

  public InstanceOfPatternRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public boolean visit(Block node) {
    enclosingScopes.push(node);
    return true;
  }

  @Override
  public void endVisit(Block node) {
    enclosingScopes.pop();
  }

  @Override
  public void endVisit(InstanceofExpression node) {
    if (node.getPattern() == null) {
      return;
    }

    Expression expression = node.getLeftOperand();
    // No need to generate a temporary variable if it is already a SimpleName.
    if (!(expression instanceof SimpleName)) {
      // Generate a temporary variable to preserve evaluation semantics since we can't guarantee
      // that the expression doesn't have side effects and can be evaluated multiple times.
      VariableElement tempVariable =
          GeneratedVariableElement.newLocalVar("tmp", node.getLeftOperand().getTypeMirror(), null);
      enclosingScopes
          .peek()
          // Type tmp = expr
          .addStatement(
              0, new VariableDeclarationStatement(tempVariable, node.getLeftOperand().copy()));
      expression = new SimpleName(tempVariable);
    }

    List<VariableElement> variablesToDeclare = new ArrayList<>();
    Expression condition =
        computePatternCondition(expression, node.getPattern(), variablesToDeclare);

    for (var variableToDeclare : variablesToDeclare) {
      // Initialize the patternVariables to null. The implementation of patterns in switches creates
      // logic that prevents the objective-c compiler from determining that the variable is never
      // accessed uninitialized.

      // Type patternVariable = null;
      enclosingScopes
          .peek()
          .addStatement(
              0,
              new VariableDeclarationStatement(
                  variableToDeclare, new NullLiteral(variableToDeclare.asType())));
    }
    node.replaceWith(condition);
  }

  private Expression computePatternCondition(
      Expression expression, Pattern pattern, List<VariableElement> variablesToDeclare) {
    switch (pattern) {
      case BindingPattern bindingPattern -> {
        VariableElement patternVariable = bindingPattern.getVariable().getVariableElement();

        if (ElementUtil.isUnnamed(patternVariable)) {
          return new InstanceofExpression()
              .setLeftOperand(expression.copy())
              .setRightOperand(Type.newType(patternVariable.asType()))
              .setPattern(null)
              .setTypeMirror(typeUtil.getBoolean());
        }

        variablesToDeclare.add(patternVariable);
        //  expression instanceof T  && (patternVariable = (T) expression, true)
        return andCondition(
            new InstanceofExpression()
                .setLeftOperand(expression.copy())
                .setRightOperand(Type.newType(patternVariable.asType()))
                .setTypeMirror(typeUtil.getBoolean()),
            new CommaExpression()
                .addExpressions(
                    new Assignment(
                        new SimpleName(patternVariable),
                        new CastExpression(patternVariable.asType(), expression.copy())
                            .setNeedsCastChk(false)),
                    new BooleanLiteral(true, typeUtil.getBoolean())));
      }
      default -> throw new IllegalArgumentException("Unhandled pattern" + pattern);
    }
  }

  private Expression andCondition(Expression lhs, Expression rhs) {
    return new InfixExpression(typeUtil.getBoolean(), Operator.CONDITIONAL_AND, lhs, rhs);
  }
}
