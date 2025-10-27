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
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InfixExpression.Operator;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.Pattern;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
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
  private int tempCount;
  private int patternCount;

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

    // TODO(b/454053746): Have a general pass that renames variables to avoid collisions.
    // Create a unique name to avoid collisions.
    nameTable.setVariableName(
        patternVariable,
        nameTable.getVariableShortName(patternVariable) + "$pattern$" + patternCount++);

    // Generate a temporary variable to preserve evaluation semantics.
    VariableElement tempVariable =
        GeneratedVariableElement.newLocalVar(
            "tmp$" + tempCount++,
            node.getLeftOperand().getTypeMirror(),
            patternVariable.getEnclosingElement());

    enclosingScopes.peek().addStatement(0, new VariableDeclarationStatement(tempVariable, null));
    enclosingScopes.peek().addStatement(0, new VariableDeclarationStatement(patternVariable, null));

    CommaExpression replacement =
        new CommaExpression(
            // tmp = expr
            new Assignment(new SimpleName(tempVariable), node.getLeftOperand().copy()),
            // patternVariable = tmp instanceof T ? (T) tmp : null
            new Assignment(
                getResolvedSimpleName(patternVariable),
                new ConditionalExpression()
                    .setExpression(
                        new InstanceofExpression(node)
                            .setLeftOperand(new SimpleName(tempVariable))
                            .setPattern(null))
                    .setThenExpression(
                        new CastExpression(patternVariable.asType(), new SimpleName(tempVariable))
                            .setNeedsCastChk(false))
                    .setElseExpression(new NullLiteral(patternVariable.asType()))
                    .setTypeMirror(patternVariable.asType())),
            // patternVariable != null
            new InfixExpression()
                .setTypeMirror(typeUtil.getBoolean())
                .setOperator(Operator.NOT_EQUALS)
                .addOperand(new SimpleName(getResolvedSimpleName(patternVariable)))
                .addOperand(new NullLiteral(patternVariable.asType())));
    node.replaceWith(replacement);
  }

  /**
   * Return the unique simple name for a pattern variable.
   *
   * <p>Since pattern variables end up being declared in an enclosing scope, to avoid name clashes a
   * unique name is synthesized for them.
   */
  private SimpleName getResolvedSimpleName(VariableElement patternVariable) {
    return new SimpleName(patternVariable)
        .setIdentifier(nameTable.getVariableShortName(patternVariable));
  }
}
