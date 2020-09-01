/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.element.VariableElement;

/**
 * Rewrites variables annotated with @ZeroingWeak.
 *
 * <p>Manual reference counting (MRC): all variables of type T annotated with @ZeroingWeak are
 * converted to WeakReference<T>. All reads from these variables are implicitly de-referenced by
 * calling JreZeroingWeakGet(), and all writes to these variables are implicitly wrapped with
 * JreMakeZeroingWeak().
 *
 * <p>Automatic reference counting (ARC): this rewriter does nothing in ARC. Variables annotated
 * with @ZeroingWeak will be translated to "weak" inside TypeDeclarationGenerator.
 *
 * @author Michał Pociecha-Łoś
 */
public class ZeroingWeakRewriter extends UnitTreeVisitor {

  private static final FunctionElement JRE_ZEROING_WEAK_GET_FUNCTION_ELEMENT =
      new FunctionElement("JreZeroingWeakGet", TypeUtil.ID_TYPE, null)
          .addParameters(TypeUtil.ID_TYPE);

  private static final FunctionElement JRE_MAKE_ZEROING_WEAK_FUNCTION_ELEMENT =
      new FunctionElement("JreMakeZeroingWeak", TypeUtil.ID_TYPE, null)
          .addParameters(TypeUtil.ID_TYPE);

  public ZeroingWeakRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(FieldDeclaration fieldDeclaration) {
    if (options.useARC()) {
      return;
    }

    VariableDeclarationFragment variableDeclarationFragment = fieldDeclaration.getFragment();
    VariableElement variableElement = variableDeclarationFragment.getVariableElement();
    if (!ElementUtil.isZeroingWeakReference(variableElement)) {
      return;
    }

    if (ElementUtil.isWeakReference(variableElement)) {
      ErrorUtil.error(fieldDeclaration, "@ZeroingWeak must not be used in combination with @Weak.");
      return;
    }

    if (!ElementUtil.hasNullableAnnotation(variableElement)) {
      ErrorUtil.error(fieldDeclaration, "@ZeroingWeak must be used in combination with @Nullable.");
      return;
    }

    if (ElementUtil.hasNonnullAnnotation(variableElement)) {
      ErrorUtil.error(
          fieldDeclaration, "@ZeroingWeak must not be used in combination with @NonNull.");
      return;
    }

    Expression initializer = variableDeclarationFragment.getInitializer();
    if (initializer != null) {
      replaceWithMakeZeroingWeak(initializer);
    }
  }

  @Override
  public void endVisit(Assignment assignment) {
    if (options.useARC()) {
      return;
    }

    Expression lhsExpression = assignment.getLeftHandSide();
    if (lhsExpression == null) {
      return;
    }

    VariableElement lhsVariableElement = TreeUtil.getVariableElement(lhsExpression);
    if (lhsVariableElement == null) {
      return;
    }

    if (!ElementUtil.isZeroingWeakReference(lhsVariableElement)) {
      return;
    }

    Assignment.Operator operator = assignment.getOperator();
    if (operator != Assignment.Operator.ASSIGN) {
      // TODO: Add support for compound assignment operator if necessary.
      // It's needed if the annotated variable is String.
      ErrorUtil.error(assignment, "@ZeroingWeak does not support " + operator + "assignment.");
      return;
    }

    replaceWithMakeZeroingWeak(assignment.getRightHandSide());
  }

  @Override
  public void postVisit(TreeNode treeNode) {
    if (options.useARC()) {
      return;
    }

    if (treeNode instanceof Expression) {
      rewrite((Expression) treeNode);
    }
  }

  private static void rewrite(Expression expression) {
    if (!expression.canReplaceWith(Expression.class)) {
      return;
    }

    if (TreeUtil.isAssignmentLeftHandSide(expression)) {
      return;
    }

    VariableElement variableElement = TreeUtil.getVariableElement(expression);
    if (variableElement == null) {
      return;
    }

    if (!ElementUtil.isZeroingWeakReference(variableElement)) {
      return;
    }

    replaceWithGetZeroingWeak(expression);
  }

  private static void replaceWithMakeZeroingWeak(Expression expression) {
    expression.replaceWith(
        () -> {
          FunctionElement functionElement = JRE_MAKE_ZEROING_WEAK_FUNCTION_ELEMENT;
          return new FunctionInvocation(functionElement, expression.getTypeMirror())
              .addArgument(expression);
        });
  }

  private static void replaceWithGetZeroingWeak(Expression expression) {
    expression.replaceWith(
        () -> {
          FunctionElement functionElement = JRE_ZEROING_WEAK_GET_FUNCTION_ELEMENT;
          return new FunctionInvocation(functionElement, expression.getTypeMirror())
              .addArgument(expression);
        });
  }
}
