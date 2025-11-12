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
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.Pattern;
import com.google.devtools.j2objc.ast.Pattern.AnyPattern;
import com.google.devtools.j2objc.ast.Pattern.BindingPattern;
import com.google.devtools.j2objc.ast.Pattern.DeconstructionPattern;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.sun.tools.javac.code.Type.ClassType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.lang.model.element.TypeElement;
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

    int variableInsertionIndex = 0;
    Expression expression = node.getLeftOperand();
    // No need to generate a temporary variable if it is already a SimpleName.
    if (!(expression instanceof SimpleName)) {
      // Generate a temporary variable to preserve evaluation semantics since we can't guarantee
      // that the expression doesn't have side effects and can be evaluated multiple times.
      VariableElement tempVariable =
          GeneratedVariableElement.newLocalVar("tmp", node.getLeftOperand().getTypeMirror(), null);
      enclosingScopes
          .peek()
          .addStatement(
              variableInsertionIndex++,
              new VariableDeclarationStatement(tempVariable, node.getLeftOperand().copy()));
      // tmp = expr
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
      Expression initializer =
          switch (variableToDeclare.asType().getKind()) {
            case BOOLEAN -> new BooleanLiteral(false, variableToDeclare.asType());
            case BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE ->
                new NumberLiteral(0, variableToDeclare.asType());
            default -> new NullLiteral(variableToDeclare.asType());
          };
      enclosingScopes
          .peek()
          .addStatement(
              variableInsertionIndex++,
              new VariableDeclarationStatement(variableToDeclare, initializer));
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

        Expression instanceofLhs = expression.copy();
        if (!(expression instanceof SimpleName)
            && !patternVariable.asType().getKind().isPrimitive()) {
          // Use a temporary variable to avoid evaluating the expression more than once and make
          // sure that user written property getters that might have side-effects are only
          // evaluated once.
          VariableElement tempVariable =
              GeneratedVariableElement.newLocalVar("comp", expression.getTypeMirror(), null);
          variablesToDeclare.add(tempVariable);
          // (prop = r.prop()) instanceof ...
          instanceofLhs = new Assignment(new SimpleName(tempVariable), expression);
          expression = new SimpleName(tempVariable);
        }

        variablesToDeclare.add(patternVariable);
        //  expression instanceof T && (patternVariable = (T) expression, true)
        return andCondition(
            patternVariable.asType().getKind().isPrimitive()
                // Primitives don't needs any checking. In the future when/if primitive patterns are
                // incorporated in the Java language, this should be updated.
                ? null
                : new InstanceofExpression()
                    .setLeftOperand(instanceofLhs)
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

      case DeconstructionPattern deconstructionPattern -> {
        VariableElement tempVariable =
            GeneratedVariableElement.newLocalVar(
                "rec", deconstructionPattern.getTypeMirror(), null);
        // (e instanceof Rec rec)
        Expression condition =
            computePatternCondition(
                expression, new BindingPattern(tempVariable), variablesToDeclare);

        var recordType = (TypeElement) ((ClassType) deconstructionPattern.getTypeMirror()).tsym;
        for (int i = 0; i < deconstructionPattern.getNestedPatterns().size(); i++) {
          var nestedPattern = deconstructionPattern.getNestedPatterns().get(i);
          if (nestedPattern instanceof AnyPattern) {
            // AnyPatterns do not contribute anything to the condition.
            continue;
          }

          var component = recordType.getRecordComponents().get(i);
          // rec.component instanceof Nested n
          Expression property =
              new MethodInvocation(
                  new ExecutablePair(component.getAccessor()),
                  component.getAccessor().getReturnType(),
                  new SimpleName(tempVariable));

          condition =
              andCondition(
                  condition, computePatternCondition(property, nestedPattern, variablesToDeclare));
        }
        return condition;
      }

      default -> throw new IllegalArgumentException("Unknown pattern" + pattern);
    }
  }

  private Expression andCondition(Expression lhs, Expression rhs) {
    if (lhs == null) {
      return rhs;
    }
    return new InfixExpression(typeUtil.getBoolean(), Operator.CONDITIONAL_AND, lhs, rhs);
  }
}
