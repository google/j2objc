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
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.sun.tools.javac.code.Type.ClassType;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Rewrites instance of patterns.
 *
 * <p>Rewrites instance of patterns by extracting the variable declaration and rewriting {@code expr
 * instanceof Class c} as {@code (tmp = expr) instanceof C && (c = (C) tmp, true)}
 *
 * @author Roberto Lublinerman
 */
public class InstanceOfPatternRewriter extends UnitTreeVisitor {

  public InstanceOfPatternRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(InstanceofExpression node) {
    if (node.getPattern() == null) {
      return;
    }

    node.replaceWith(
        computePatternCondition(
            node.getLeftOperand(), node.getPattern(), /* allowsNulls= */ false));
  }

  private Expression computePatternCondition(
      Expression expression, Pattern pattern, boolean allowsNulls) {
    switch (pattern) {
      case BindingPattern bindingPattern -> {
        VariableElement patternVariable = bindingPattern.getVariable().getVariableElement();
        boolean isUnconditional =
            allowsNulls
                && typeUtil.isAssignable(expression.getTypeMirror(), patternVariable.asType());

        if (ElementUtil.isUnnamed(patternVariable)) {
          return isUnconditional
              // Unconditional patterns don't need any checking, however the expression might have
              // side effects that need to be preserved.
              ? new CommaExpression()
                  .addExpressions(
                      expression.copy(), new BooleanLiteral(true, typeUtil.getBoolean()))
              : new InstanceofExpression()
                  .setLeftOperand(expression.copy())
                  .setRightOperand(Type.newType(patternVariable.asType()))
                  .setPattern(null)
                  .setTypeMirror(typeUtil.getBoolean());
        }

        var result = new CommaExpression();

        Expression instanceofLhs = expression.copy();
        if (!(expression instanceof SimpleName || isUnconditional)) {
          // Use a temporary variable to avoid evaluating the expression more than once and make
          // sure that user written property getters that might have side-effects are only
          // evaluated once.
          VariableElement tempVariable =
              GeneratedVariableElement.newLocalVar("tmp", expression.getTypeMirror(), null);

          // Type tmp = null
          result.addExpression(
              new VariableDeclarationExpression(tempVariable, getDefaultValue(tempVariable)));

          // (prop = r.prop()) instanceof ...
          instanceofLhs = new Assignment(new SimpleName(tempVariable), expression.copy());
          expression = new SimpleName(tempVariable);
        }
        return result.addExpressions(
            // Type patternVariable = null
            new VariableDeclarationExpression(patternVariable, getDefaultValue(patternVariable)),
            //  expression instanceof T && (patternVariable = (T) expression, true)
            andCondition(
                isUnconditional
                    // Unconditional patterns don't needs any checking. In the future when/if
                    // primitive patterns are incorporated in the Java language, this should be
                    // updated.
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
                        new BooleanLiteral(true, typeUtil.getBoolean()))));
      }

      case DeconstructionPattern deconstructionPattern -> {
        VariableElement tempVariable =
            GeneratedVariableElement.newLocalVar(
                "rec", deconstructionPattern.getTypeMirror(), null);
        // (e instanceof Rec rec)
        Expression condition =
            computePatternCondition(
                expression,
                new BindingPattern(tempVariable),
                // This binding pattern resulting from the implementation of a deconstruction
                // pattern cannot allow nulls.
                /* allowsNulls= */ false);

        var recordClassType = (ClassType) deconstructionPattern.getTypeMirror();
        var recordTypeElement = (TypeElement) recordClassType.tsym;
        for (int i = 0; i < deconstructionPattern.getNestedPatterns().size(); i++) {
          var nestedPattern = deconstructionPattern.getNestedPatterns().get(i);
          var component = recordTypeElement.getRecordComponents().get(i);

          if (nestedPattern instanceof AnyPattern) {
            // match-all patterns are treated as unconditional binding patterns with an unnamed
            // variable.
            nestedPattern =
                new BindingPattern(
                    GeneratedVariableElement.newLocalVar(
                        "", component.getAccessor().getReturnType(), null));
          }

          // Retrieve the parameterized type for the component accessor.
          var accessorType = typeUtil.asMemberOf(recordClassType, component.getAccessor());

          // rec.component() instanceof Nested n
          Expression property =
              new MethodInvocation(
                  new ExecutablePair(component.getAccessor(), accessorType),
                  accessorType.getReturnType(),
                  new SimpleName(tempVariable));

          condition =
              andCondition(
                  condition,
                  computePatternCondition(property, nestedPattern, /* allowsNulls= */ true));
        }
        return condition;
      }

      default -> throw new IllegalArgumentException("Unknown pattern" + pattern);
    }
  }

  private static Expression getDefaultValue(VariableElement variable) {
    return switch (variable.asType().getKind()) {
      case BOOLEAN -> new BooleanLiteral(false, variable.asType());
      case BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE -> new NumberLiteral(0, variable.asType());
      default -> new NullLiteral(variable.asType());
    };
  }

  private Expression andCondition(Expression lhs, Expression rhs) {
    if (lhs == null) {
      return rhs;
    }
    return new InfixExpression(typeUtil.getBoolean(), Operator.CONDITIONAL_AND, lhs, rhs);
  }
}
