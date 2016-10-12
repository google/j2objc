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

import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EmptyStatement;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Rewrites switch statemennts to be more compatible with Objective-C code.
 *
 * @author Keith Stanger
 */
public class SwitchRewriter extends UnitTreeVisitor {

  public SwitchRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(SwitchStatement node) {
    fixVariableDeclarations(node);
    fixStringValue(node);
    fixEnumValue(node);

    List<Statement> stmts = node.getStatements();
    if (!stmts.isEmpty() && stmts.get(stmts.size() - 1) instanceof SwitchCase) {
      // Last switch case doesn't have an associated statement, so add an empty one.
      stmts.add(new EmptyStatement());
    }
  }

  @Override
  public void endVisit(SwitchCase node) {
    Expression expr = node.getExpression();
    VariableElement var = expr != null ? TreeUtil.getVariableElement(expr) : null;
    if (var == null) {
      return;
    }
    TypeMirror type = var.asType();
    if (TypeUtil.isEnum(type)) {
      String enumValue = NameTable.getNativeEnumName(nameTable.getFullName(type)) + "_"
          + nameTable.getVariableBaseName(var);
      node.setExpression(new NativeExpression(enumValue, typeEnv.resolveJavaTypeMirror("int")));
    } else if (type.getKind().isPrimitive() && var.getKind() == ElementKind.LOCAL_VARIABLE) {
      Object value = var.getConstantValue();
      if (value != null) {
        node.setExpression(TreeUtil.newLiteral(value, typeEnv));
      }
    }
  }

  /**
   * Moves all variable declarations above the first case statement.
   */
  private void fixVariableDeclarations(SwitchStatement node) {
    List<Statement> statements = node.getStatements();
    Block block = new Block();
    List<Statement> blockStmts = block.getStatements();
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      if (stmt instanceof VariableDeclarationStatement) {
        VariableDeclarationStatement declStmt = (VariableDeclarationStatement) stmt;
        statements.remove(i--);
        List<VariableDeclarationFragment> fragments = declStmt.getFragments();
        for (VariableDeclarationFragment decl : fragments) {
          Expression initializer = decl.getInitializer();
          if (initializer != null) {
            Assignment assignment = new Assignment(
                decl.getName().copy(), TreeUtil.remove(initializer));
            statements.add(++i, new ExpressionStatement(assignment));
          }
        }
        blockStmts.add(declStmt);
      }
    }
    if (blockStmts.size() > 0) {
      // There is at least one variable declaration, so copy this switch
      // statement into the new block and replace it in the parent list.
      node.replaceWith(block);
      blockStmts.add(node);
    }
  }

  private void fixStringValue(SwitchStatement node) {
    Expression expr = node.getExpression();
    TypeMirror type = expr.getTypeMirror();
    if (!typeEnv.isJavaStringType(type)) {
      return;
    }
    TypeMirror arrayType = env.typeUtilities().getArrayType(type);
    ArrayInitializer arrayInit = new ArrayInitializer(arrayType);
    int idx = 0;
    for (Statement stmt : node.getStatements()) {
      if (stmt instanceof SwitchCase) {
        SwitchCase caseStmt = (SwitchCase) stmt;
        if (!caseStmt.isDefault()) {
          arrayInit.addExpression(TreeUtil.remove(caseStmt.getExpression()));
          caseStmt.setExpression(NumberLiteral.newIntLiteral(idx++, typeEnv));
        }
      }
    }
    TypeMirror intType = typeEnv.resolveJavaTypeMirror("int");
    FunctionBinding indexOfFunc = new FunctionBinding("JreIndexOfStr", intType, null);
    indexOfFunc.addParameters(type, arrayType, intType);
    FunctionInvocation invocation = new FunctionInvocation(indexOfFunc, intType);
    invocation.addArgument(TreeUtil.remove(expr))
        .addArgument(arrayInit)
        .addArgument(NumberLiteral.newIntLiteral(idx, typeEnv));
    node.setExpression(invocation);
  }

  private void fixEnumValue(SwitchStatement node) {
    Expression expr = node.getExpression();
    TypeMirror type = expr.getTypeMirror();
    if (!TypeUtil.isEnum(type)) {
      return;
    }
    DeclaredType enumType = TypeUtil.getSuperclass(type, env);
    TypeElement enumElem = (TypeElement) enumType.asElement();
    ExecutableElement ordinalMethod = ElementUtil.findMethod(enumElem, "ordinal");
    ExecutableType ordinalType =
        (ExecutableType) env.typeUtilities().asMemberOf(enumType, ordinalMethod);
    MethodInvocation invocation =
        new MethodInvocation(ordinalMethod, ordinalType, TreeUtil.remove(expr));
    node.setExpression(invocation);
  }
}
