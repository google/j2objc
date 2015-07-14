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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.TranslationUtil;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Converts static variable access to static method calls where necessary.
 *
 * @author Keith Stanger
 */
public class StaticVarRewriter extends TreeVisitor {

  private boolean needsStaticLoad(TreeNode currentNode, IVariableBinding var) {
    if (!BindingUtil.isStatic(var) || BindingUtil.isPrimitiveConstant(var)
        || BindingUtil.isStringConstant(var)) {
      return false;
    }
    AbstractTypeDeclaration owningType = TreeUtil.getOwningType(currentNode);
    return owningType == null || !owningType.getTypeBinding().getTypeDeclaration().isEqualTo(
        var.getDeclaringClass().getTypeDeclaration());
  }

  private void rewriteStaticAccess(Expression node) {
    IVariableBinding var = TreeUtil.getVariableBinding(node);
    if (var == null || !needsStaticLoad(node, var)) {
      return;
    }

    boolean assignable = TranslationUtil.isAssigned(node);
    StringBuilder code = new StringBuilder("JreLoadStatic");
    ITypeBinding exprType = var.getType();
    if (assignable) {
      code.append("Ref");
      exprType = typeEnv.getPointerType(exprType);
    }
    code.append("(");
    code.append(nameTable.getFullName(var.getDeclaringClass()));
    code.append(", ");
    code.append(nameTable.getVariableShortName(var));
    code.append(")");
    NativeExpression nativeExpr = new NativeExpression(code.toString(), exprType);
    nativeExpr.getImportTypes().add(var.getDeclaringClass());
    Expression newNode = nativeExpr;
    if (assignable) {
      newNode = new PrefixExpression(
          var.getType(), PrefixExpression.Operator.DEREFERENCE, newNode);
    }
    node.replaceWith(newNode);
  }

  /**
   * Reterns whether the expression of a FieldAccess node has any side effects.
   * If false, the expression can be removed from the tree.
   * @param expr The expression of a FieldAccess node for a static variable.
   */
  private boolean fieldAccessExpressionHasSideEffect(Expression expr) {
    switch (expr.getKind()) {
      case QUALIFIED_NAME:
      case SIMPLE_NAME:
      case THIS_EXPRESSION:
        return false;
      default:
        return true;
    }
  }

  @Override
  public boolean visit(FieldAccess node) {
    IVariableBinding var = node.getVariableBinding();
    if (BindingUtil.isInstanceVar(var)) {
      node.getExpression().accept(this);
      return false;
    }

    Expression expr = TreeUtil.remove(node.getExpression());
    Expression varNode = TreeUtil.remove(node.getName());
    if (!fieldAccessExpressionHasSideEffect(expr)) {
      node.replaceWith(varNode);
      varNode.accept(this);
      return false;
    }

    CommaExpression commaExpr = new CommaExpression(expr);
    if (TranslationUtil.isAssigned(node)) {
      commaExpr.getExpressions().add(new PrefixExpression(
          typeEnv.getPointerType(var.getType()), PrefixExpression.Operator.ADDRESS_OF, varNode));
      node.replaceWith(new PrefixExpression(
          var.getType(), PrefixExpression.Operator.DEREFERENCE, commaExpr));
    } else {
      commaExpr.getExpressions().add(varNode);
      node.replaceWith(commaExpr);
    }
    commaExpr.accept(this);
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    rewriteStaticAccess(node);
    return false;
  }

  @Override
  public boolean visit(QualifiedName node) {
    rewriteStaticAccess(node);
    return false;
  }

  @Override
  public boolean visit(SwitchCase node) {
    // Avoid using an accessor method for enums in a switch case.
    return false;
  }
}
