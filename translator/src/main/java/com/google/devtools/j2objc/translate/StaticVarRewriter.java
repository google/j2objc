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

import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.PointerType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TranslationUtil;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Converts static variable access to static method calls where necessary.
 *
 * @author Keith Stanger
 */
public class StaticVarRewriter extends UnitTreeVisitor {

  public StaticVarRewriter(CompilationUnit unit) {
    super(unit);
  }

  private boolean needsStaticLoad(TreeNode currentNode, VariableElement var) {
    if (!ElementUtil.isStatic(var) || ElementUtil.isConstant(var)) {
      return false;
    }
    TypeElement enclosingType = TreeUtil.getEnclosingTypeElement(currentNode);
    return enclosingType == null || !enclosingType.equals(ElementUtil.getDeclaringClass(var));
  }

  private void rewriteStaticAccess(Expression node) {
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var == null || !needsStaticLoad(node, var)) {
      return;
    }

    TypeElement declaringClass = ElementUtil.getDeclaringClass(var);
    boolean assignable = TranslationUtil.isAssigned(node);
    StringBuilder code = new StringBuilder(
        ElementUtil.isEnumConstant(var) ? "JreLoadEnum" : "JreLoadStatic");
    TypeMirror exprType = var.asType();
    if (assignable) {
      code.append("Ref");
      exprType = new PointerType(exprType);
    }
    code.append("(");
    code.append(nameTable.getFullName(declaringClass));
    code.append(", ");
    code.append(nameTable.getVariableShortName(var));
    code.append(")");
    NativeExpression nativeExpr = new NativeExpression(code.toString(), exprType);
    nativeExpr.addImportType(declaringClass.asType());
    Expression newNode = nativeExpr;
    if (assignable) {
      newNode = new PrefixExpression(var.asType(), PrefixExpression.Operator.DEREFERENCE, newNode);
    }
    node.replaceWith(newNode);
  }

  @Override
  public boolean visit(FieldAccess node) {
    VariableElement var = node.getVariableElement();
    if (ElementUtil.isInstanceVar(var)) {
      node.getExpression().accept(this);
      return false;
    }

    Expression expr = TreeUtil.remove(node.getExpression());
    Expression varNode = TreeUtil.remove(node.getName());
    if (!TranslationUtil.hasSideEffect(expr)) {
      node.replaceWith(varNode);
      varNode.accept(this);
      return false;
    }

    CommaExpression commaExpr = new CommaExpression(expr);
    if (TranslationUtil.isAssigned(node)) {
      commaExpr.addExpression(new PrefixExpression(
          new PointerType(var.asType()), PrefixExpression.Operator.ADDRESS_OF, varNode));
      node.replaceWith(new PrefixExpression(
          var.asType(), PrefixExpression.Operator.DEREFERENCE, commaExpr));
    } else {
      commaExpr.addExpression(varNode);
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
