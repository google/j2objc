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
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchExpression;
import com.google.devtools.j2objc.ast.SwitchExpressionCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.jspecify.annotations.Nullable;

/**
 * Rewrites switch statements to be more compatible with Objective-C code.
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
    if (!stmts.isEmpty()) {
      Statement lastStmt = stmts.get(stmts.size() - 1);
      if (lastStmt instanceof SwitchCase) {
        // Last switch case doesn't have an associated statement, so add an empty one
        // with the same line number as the switch case to keep line numbers synced.
        EmptyStatement emptyStmt = new EmptyStatement();
        emptyStmt.setLineNumber(lastStmt.getLineNumber());
        stmts.add(emptyStmt);
      }
    }
  }
  
  @Override
  public void endVisit(SwitchExpression node) {
    fixStringValue(node);
    fixEnumValue(node);
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
      String enumValue = nameTable.getNativeEnumConstantName(TypeUtil.asTypeElement(type), var);
      node.setExpression(new NativeExpression(enumValue, typeUtil.getInt()));
    } else if (type.getKind().isPrimitive() && var.getKind() == ElementKind.LOCAL_VARIABLE) {
      Object value = var.getConstantValue();
      if (value != null) {
        node.setExpression(TreeUtil.newLiteral(value, typeUtil));
      }
    }
  }

  @Override
  public void endVisit(SwitchExpressionCase node) {
    List<Expression> exprs = node.getExpressions();
    for (int i = 0; i < exprs.size(); i++) {
      Expression expr = exprs.get(i);
      VariableElement var = expr != null ? TreeUtil.getVariableElement(expr) : null;
      if (var != null) {
        TypeMirror type = var.asType();
        if (TypeUtil.isEnum(type)) {
          String enumValue = nameTable.getNativeEnumConstantName(TypeUtil.asTypeElement(type), var);
          exprs.set(i, new NativeExpression(enumValue, typeUtil.getInt()));
        } else if (type.getKind().isPrimitive() && var.getKind() == ElementKind.LOCAL_VARIABLE) {
          Object value = var.getConstantValue();
          if (value != null) {
            exprs.set(i, TreeUtil.newLiteral(value, typeUtil));
          }
        }
      }
    }
  }

  @Override
  public void endVisit(Block node) {
    List<Statement> statements = node.getStatements();
    Block block = new Block();
    List<Statement> blockStmts = block.getStatements();
    boolean blockChanged = false;
    for (Statement stmt : statements) {
      // If statement declares a local variable and its initializer is a switch
      // expression, move the initializer into a separate expression statement.
      if (stmt instanceof VariableDeclarationStatement) {
        VariableDeclarationStatement declStmt = (VariableDeclarationStatement) stmt;
        if (!declStmt.getFragments().isEmpty()) {
          VariableDeclarationFragment fragment = declStmt.getFragments().get(0);
          SwitchExpression switchExpression = null;
          Expression initializer = fragment.getInitializer();
          if (initializer instanceof SwitchExpression) {
            switchExpression = ((SwitchExpression) initializer).copy();
            VariableDeclarationFragment newDecl =
                new VariableDeclarationFragment(fragment.getVariableElement(), null);
            blockStmts.add(new VariableDeclarationStatement(newDecl));
            blockStmts.add(new ExpressionStatement(switchExpression));
            blockChanged = true;
          } else {
            blockStmts.add(declStmt.copy());
          }
        }
      } else {
        blockStmts.add(stmt.copy());
      }
    }
    if (blockChanged) {
      node.replaceWith(block);
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
                new SimpleName(decl.getVariableElement()), TreeUtil.remove(initializer));
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

  private @Nullable Expression fixStringValue(Expression expr, List<Statement> statements) {
    TypeMirror type = expr.getTypeMirror();
    if (!typeUtil.isString(type)) {
      return null;
    }
    ArrayType arrayType = typeUtil.getArrayType(type);
    ArrayInitializer arrayInit = new ArrayInitializer(arrayType);
    int idx = 0;
    for (Statement stmt : statements) {
      if (stmt.getKind() == TreeNode.Kind.SWITCH_CASE) {
        SwitchCase caseStmt = (SwitchCase) stmt;
        if (!caseStmt.isDefault()) {
          arrayInit.addExpression(TreeUtil.remove(caseStmt.getExpression()));
          caseStmt.setExpression(NumberLiteral.newIntLiteral(idx++, typeUtil));
        }
      } else if (stmt.getKind() == TreeNode.Kind.SWITCH_EXPRESSION_CASE) {
        SwitchExpressionCase caseStmt = (SwitchExpressionCase) stmt;
        if (!caseStmt.isDefault()) {
          List<Expression> caseExprs = caseStmt.getExpressions();
          for (int i = 0; i < caseExprs.size(); i++) {
            arrayInit.addExpression(caseExprs.get(i).copy());
            caseExprs.set(i, NumberLiteral.newIntLiteral(idx++, typeUtil));
          }
        }
      }
    }
    TypeMirror intType = typeUtil.getInt();
    FunctionElement indexOfFunc = new FunctionElement("JreIndexOfStr", intType, null)
        .addParameters(type, arrayType, intType);
    FunctionInvocation invocation = new FunctionInvocation(indexOfFunc, intType);
    invocation.addArgument(TreeUtil.remove(expr))
        .addArgument(arrayInit)
        .addArgument(NumberLiteral.newIntLiteral(idx, typeUtil));
    return invocation;
  }

  private void fixStringValue(SwitchStatement node) {
    Expression expr = fixStringValue(node.getExpression(), node.getStatements());
    if (expr != null) {
      SwitchStatement unused = node.setExpression(expr);
    }
  }

  private void fixStringValue(SwitchExpression node) {
    Expression expr = fixStringValue(node.getExpression(), node.getStatements());
    if (expr != null) {
      SwitchExpression unused = node.setExpression(expr);
    }
  }

  private void fixEnumValue(SwitchStatement node) {
    Expression expr = node.getExpression();
    TypeMirror type = expr.getTypeMirror();
    if (!TypeUtil.isEnum(type)) {
      return;
    }
    DeclaredType enumType = typeUtil.getSuperclass(type);
    ExecutablePair ordinalMethod = typeUtil.findMethod(enumType, "ordinal");
    MethodInvocation invocation = new MethodInvocation(ordinalMethod, TreeUtil.remove(expr));
    node.setExpression(invocation);
  }

  private void fixEnumValue(SwitchExpression node) {
    Expression expr = node.getExpression();
    TypeMirror type = expr.getTypeMirror();
    if (!TypeUtil.isEnum(type)) {
      return;
    }
    DeclaredType enumType = typeUtil.getSuperclass(type);
    ExecutablePair ordinalMethod = typeUtil.findMethod(enumType, "ordinal");
    MethodInvocation invocation = new MethodInvocation(ordinalMethod, TreeUtil.remove(expr));
    var unused = node.setExpression(invocation);
  }
}
