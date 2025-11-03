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

import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EmbeddedStatementExpression;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclaration.ObjectiveCModifier;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.VariableElement;

/**
 * Determines the modifiers for variable declarations.
 *
 * <p>Variables that are modified across variable declarations need to be have the modifier
 * "_block".
 *
 * @author Roberto Lublinerman
 */
@SuppressWarnings("UngroupedOverloads")
public class ComputeVariableModifiers extends UnitTreeVisitor {
  private final Deque<EmbeddedStatementExpression> blockScopes = new ArrayDeque<>();
  private final Map<VariableElement, TreeNode> declarations = new HashMap<>();
  private final Map<VariableElement, TreeNode> variableScopes = new HashMap<>();

  public ComputeVariableModifiers(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public boolean visit(EmbeddedStatementExpression node) {
    blockScopes.push(node);
    return true;
  }

  @Override
  public void endVisit(EmbeddedStatementExpression node) {
    blockScopes.pop();
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    for (var fragment : node.getFragments()) {
      processVariableDeclaration(fragment.getVariableElement(), node);
    }
    return true;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    for (var fragment : node.getFragments()) {
      processVariableDeclaration(fragment.getVariableElement(), node);
    }
    return true;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    processVariableDeclaration(node.getVariableElement(), node);
    return true;
  }

  private void processVariableDeclaration(
      VariableElement variable, TreeNode declarationExpression) {
    declarations.put(variable, declarationExpression);
    TreeNode scope = blockScopes.peek();
    if (scope != null) {
      variableScopes.put(variable, scope);
    }
  }

  @Override
  public void endVisit(Assignment node) {
    if (node.getLeftHandSide() instanceof SimpleName name
        && name.getElement() instanceof VariableElement var) {
      processVariableMutation(var);
    }
  }

  private static final ImmutableSet<PostfixExpression.Operator>
      POSTFIX_INCREMENT_DECREMENT_OPERATORS =
          ImmutableSet.of(
              PostfixExpression.Operator.INCREMENT, PostfixExpression.Operator.DECREMENT);

  @Override
  public void endVisit(PostfixExpression node) {
    if (POSTFIX_INCREMENT_DECREMENT_OPERATORS.contains(node.getOperator())
        && node.getOperand() instanceof SimpleName name
        && name.getElement() instanceof VariableElement var) {
      processVariableMutation(var);
    }
  }

  private static final ImmutableSet<PrefixExpression.Operator>
      PREFIX_INCREMENT_DECREMENT_OPERATORS =
          ImmutableSet.of(PrefixExpression.Operator.INCREMENT, PrefixExpression.Operator.DECREMENT);

  @Override
  public void endVisit(PrefixExpression node) {
    if (PREFIX_INCREMENT_DECREMENT_OPERATORS.contains(node.getOperator())
        && node.getOperand() instanceof SimpleName name
        && name.getElement() instanceof VariableElement var) {
      processVariableMutation(var);
    }
  }

  private void processVariableMutation(VariableElement variable) {
    EmbeddedStatementExpression scope = blockScopes.peek();
    if (scope == variableScopes.get(variable)) {
      // Same scope no need to add the _block modifier.
      return;
    }
    TreeNode declaration = declarations.get(variable);
    if (declaration instanceof SingleVariableDeclaration variableDeclaration) {
      variableDeclaration.addModifier(ObjectiveCModifier.BLOCK);
      // TODO(b/457462564): Since the declaration might declare many variables they will all be
      // declared with __block as a modifier which might trigger a compilation error.
    } else if (declaration instanceof VariableDeclarationExpression variableDeclaration) {
      variableDeclaration.addModifier(ObjectiveCModifier.BLOCK);
    } else if (declaration instanceof VariableDeclarationStatement variableDeclaration) {
      variableDeclaration.addModifier(ObjectiveCModifier.BLOCK);
    }
  }
}
