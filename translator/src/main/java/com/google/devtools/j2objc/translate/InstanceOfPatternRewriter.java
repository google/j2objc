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
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InfixExpression.Operator;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.Pattern;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.ArrayDeque;
import java.util.Deque;
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

    VariableElement patternVariable =
        ((Pattern.BindingPattern) node.getPattern()).getVariable().getVariableElement();

    if (ElementUtil.isUnnamed(patternVariable)) {
      // If the pattern variable is unnamed this is a regular instanceof expression.
      node.setPattern(null);
      return;
    }

    enclosingScopes.peek().addStatement(0, new VariableDeclarationStatement(patternVariable, null));
    CommaExpression replacement = new CommaExpression();

    Expression expression = node.getLeftOperand();
    // No need to generate a temporary variable if it is already a SimpleName.
    if (!(expression instanceof SimpleName)) {
      // Generate a temporary variable to preserve evaluation semantics since we can't guarantee
      // that the expression doesn't have side effects and can be evaluated multiple times.
      VariableElement tempVariable =
          GeneratedVariableElement.newLocalVar(
              "tmp", node.getLeftOperand().getTypeMirror(), patternVariable.getEnclosingElement());
      enclosingScopes.peek().addStatement(0, new VariableDeclarationStatement(tempVariable, null));
      // tmp = expr
      replacement.addExpression(
          new Assignment(new SimpleName(tempVariable), node.getLeftOperand().copy()));
      expression = new SimpleName(tempVariable);
    }

    replacement.addExpressions(
        // patternVariable = expression instanceof T ? (T) expression : null
        new Assignment(
            new SimpleName(patternVariable),
            new ConditionalExpression()
                .setExpression(
                    new InstanceofExpression(node)
                        .setLeftOperand(expression.copy())
                        .setPattern(null))
                .setThenExpression(
                    new CastExpression(patternVariable.asType(), expression.copy())
                        .setNeedsCastChk(false))
                .setElseExpression(new NullLiteral(patternVariable.asType()))
                .setTypeMirror(patternVariable.asType())),
        // patternVariable != null
        new InfixExpression()
            .setTypeMirror(typeUtil.getBoolean())
            .setOperator(Operator.NOT_EQUALS)
            .addOperand(new SimpleName(patternVariable))
            .addOperand(new NullLiteral(patternVariable.asType())));
    node.replaceWith(replacement);
  }
}
