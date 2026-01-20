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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EmbeddedStatementExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InfixExpression.Operator;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.Pattern;
import com.google.devtools.j2objc.ast.Pattern.AnyPattern;
import com.google.devtools.j2objc.ast.Pattern.BindingPattern;
import com.google.devtools.j2objc.ast.Pattern.DeconstructionPattern;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.sun.tools.javac.code.Type.ClassType;
import java.util.ArrayList;
import java.util.List;
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

    List<VariableElement> variablesToDeclare = new ArrayList<>();
    Expression condition =
        computePatternCondition(
            node.getLeftOperand(), node.getPattern(), /* allowsNulls= */ false, variablesToDeclare);

    // TODO(b/477277380): Ideally we could keep the variable declarations in the comma expressions
    // and have a pass at the end that moves them to the right scope.
    Block enclosingBlock =
        variablesToDeclare.isEmpty() ? null : getBlockForVariableDeclarations(node);

    int variableInsertionIndex = 0;
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

      enclosingBlock.addStatement(
          variableInsertionIndex++,
          new VariableDeclarationStatement(variableToDeclare, initializer));
    }
    node.replaceWith(condition);
  }

  /** Returns an enclosing block where to declare the pattern and temporary variables. */
  private Block getBlockForVariableDeclarations(TreeNode node) {
    while (!(node instanceof Block block)) {
      node = node.getParent();
      if (node instanceof FieldDeclaration fieldDeclaration) {
        // The instanceof pattern is in a field initializer, no suitable block is found to
        // declare the temporary variables. Hence, create a new block where to declare the variables
        // and replace the field initializer with an embedded statement containing that block.
        var block = new Block();
        var embeddedStatement =
            new EmbeddedStatementExpression()
                .setTypeMirror(fieldDeclaration.getTypeMirror())
                .setStatement(block);

        // Reattach the initializer as the return value of the embedded statement.
        var initializer = fieldDeclaration.getFragment().getInitializer();
        initializer.remove();
        block.addStatement(new ReturnStatement().setExpression(initializer));
        fieldDeclaration.getFragment().setInitializer(embeddedStatement);

        // And return an enclosing block where variables can be declared.
        return block;
      } else if (node instanceof LambdaExpression lambdaExpression) {
        // This lambda has an expression in its rhs (as opposed to a block), otherwise theblock
        // would have been found and returned.
        var lambdaBody = (Expression) lambdaExpression.getBody();
        var block = new Block();

        // Reattach the body as the return value of the block.
        lambdaBody.remove();
        block.addStatement(new ReturnStatement().setExpression(lambdaBody));
        lambdaExpression.setBody(block);
        return block;
      }
    }
    return checkNotNull(block);
  }

  private Expression computePatternCondition(
      Expression expression,
      Pattern pattern,
      boolean allowsNulls,
      List<VariableElement> variablesToDeclare) {
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

        Expression instanceofLhs = expression.copy();
        if (!(expression instanceof SimpleName || isUnconditional)) {
          // Use a temporary variable to avoid evaluating the expression more than once and make
          // sure that user written property getters that might have side-effects are only
          // evaluated once.
          VariableElement tempVariable =
              GeneratedVariableElement.newLocalVar("comp", expression.getTypeMirror(), null);
          variablesToDeclare.add(tempVariable);
          // (prop = r.prop()) instanceof ...
          instanceofLhs = new Assignment(new SimpleName(tempVariable), expression.copy());
          expression = new SimpleName(tempVariable);
        }

        variablesToDeclare.add(patternVariable);
        //  expression instanceof T && (patternVariable = (T) expression, true)
        return andCondition(
            isUnconditional
                // Unconditional patterns don't needs any checking. In the future when/if primitive
                // patterns are incorporated in the Java language, this should be updated.
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
                expression,
                new BindingPattern(tempVariable),
                // This binding pattern resulting from the implementation of a deconstruction
                // pattern cannot allow nulls.
                /* allowsNulls= */ false,
                variablesToDeclare);

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
                  computePatternCondition(
                      property, nestedPattern, /* allowsNulls= */ true, variablesToDeclare));
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
