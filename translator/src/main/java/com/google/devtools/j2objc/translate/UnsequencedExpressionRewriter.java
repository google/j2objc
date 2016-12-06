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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.AssertStatement;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.BreakStatement;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.SynchronizedStatement;
import com.google.devtools.j2objc.ast.ThrowStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Detects unsequenced modifications which are errors in ObjC and extracts
 * portions of the expression to eliminate the errors.
 *
 * This pass must occur after rewriting of labeled break and continue statements
 * otherwise extracted loop conditions will be out of order with the labels
 * inserted by the labeled break and continue rewriting.
 *
 * @author Keith Stanger
 */
public class UnsequencedExpressionRewriter extends UnitTreeVisitor {

  private ExecutableElement currentMethod = null;
  private int count = 1;
  private List<VariableAccess> orderedAccesses = Lists.newArrayList();
  private TreeNode currentTopNode = null;
  private boolean hasModification = false;

  public UnsequencedExpressionRewriter(CompilationUnit unit) {
    super(unit);
  }

  /**
   * Metadata for a given read or write access of a variable within an
   * expression.
   */
  private static class VariableAccess {

    private final VariableElement variable;
    private final Expression expression;
    private final boolean isModification;

    private VariableAccess(
        VariableElement variable, Expression expression, boolean isModification) {
      this.variable = variable;
      this.expression = expression;
      this.isModification = isModification;
    }
  }

  private void addVariableAccess(VariableElement var, Expression node, boolean isModification) {
    if (var != null && !ElementUtil.isInstanceVar(var)) {
      hasModification |= isModification;
      orderedAccesses.add(new VariableAccess(var, node, isModification));
    }
  }

  private void newExpression(TreeNode topNode) {
    orderedAccesses.clear();
    currentTopNode = topNode;
    hasModification = false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    currentMethod = node.getExecutableElement();
    count = 1;
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    currentMethod = null;
  }

  private List<VariableAccess> getUnsequencedAccesses() {
    if (!hasModification) {
      return Collections.emptyList();
    }
    ListMultimap<VariableElement, VariableAccess> accessesByVar =
        MultimapBuilder.hashKeys().arrayListValues().build();
    for (VariableAccess access : orderedAccesses) {
      accessesByVar.put(access.variable, access);
    }
    Set<VariableAccess> unsequencedAccesses = Sets.newHashSet();
    for (VariableElement var : accessesByVar.keySet()) {
      findUnsequenced(accessesByVar.get(var), unsequencedAccesses);
    }
    List<VariableAccess> orderedUnsequencedAccesses =
        Lists.newArrayListWithCapacity(unsequencedAccesses.size());
    for (VariableAccess access : orderedAccesses) {
      if (unsequencedAccesses.contains(access)) {
        orderedUnsequencedAccesses.add(access);
      }
    }
    return orderedUnsequencedAccesses;
  }

  private void extractUnsequenced(Statement stmt) {
    List<VariableAccess> accesses = getUnsequencedAccesses();
    if (!accesses.isEmpty()) {
      extractOrderedAccesses(
          TreeUtil.asStatementList(stmt).subList(0, 0), currentTopNode, accesses);
    }
  }

  private void visitAndExtract(Expression expr, Statement stmt) {
    if (expr != null) {
      newExpression(expr);
      expr.accept(this);
      extractUnsequenced(stmt);
    }
  }

  private void extractOrderedAccesses(
      List<Statement> stmtList, TreeNode subExpr, List<VariableAccess> toExtract) {
    for (int i = 0; i < toExtract.size(); i++) {
      VariableAccess access = toExtract.get(i);
      TreeNode topConditional = getTopConditional(access.expression, subExpr);
      if (topConditional != null) {
        // Conditional expressions require special handling when extracting the
        // access because execution of the access may not be guaranteed.
        // Here we collect all accesses that are decendant of the conditional
        // expression and pass them to an appropriate extraction method.
        int j = i + 1;
        for (; j < toExtract.size(); j++) {
          if (getTopConditional(toExtract.get(j).expression, subExpr) != topConditional) {
            break;
          }
        }
        if (topConditional instanceof InfixExpression) {
          extractInfixConditional(
              stmtList, (InfixExpression) topConditional, toExtract.subList(i, j));
        } else if (topConditional instanceof ConditionalExpression) {
          extractConditionalExpression(
              stmtList, (ConditionalExpression) topConditional, toExtract.subList(i, j));
        } else {
          throw new AssertionError(
              "Unexpected conditional node type: " + topConditional.getClass().toString());
        }
        i = j - 1;
      } else {
        VariableElement newVar = GeneratedVariableElement.newLocalVar(
            "unseq$" + count++, access.expression.getTypeMirror(), currentMethod);
        stmtList.add(new VariableDeclarationStatement(newVar, access.expression.copy()));
        access.expression.replaceWith(new SimpleName(newVar));
      }
    }
  }

  private TreeNode getTopConditional(TreeNode node, TreeNode limit) {
    TreeNode topConditional = null;
    while (node != limit) {
      node = node.getParent();
      if (isConditional(node)) {
        topConditional = node;
      }
    }
    return topConditional;
  }

  private void extractConditionalExpression(
      List<Statement> stmtList, ConditionalExpression conditional, List<VariableAccess> toExtract) {
    Expression condition = conditional.getExpression();
    Expression thenExpr = conditional.getThenExpression();
    Expression elseExpr = conditional.getElseExpression();
    List<VariableAccess> conditionAccesses = Lists.newArrayList();
    List<VariableAccess> thenAccesses = Lists.newArrayList();
    List<VariableAccess> elseAccesses = Lists.newArrayList();
    boolean needsExtraction = false;
    for (VariableAccess access : toExtract) {
      TreeNode node = access.expression;
      while (node.getParent() != conditional) {
        node = node.getParent();
      }
      if (node == condition) {
        conditionAccesses.add(access);
      } else if (node == thenExpr) {
        thenAccesses.add(access);
      } else if (node == elseExpr) {
        elseAccesses.add(access);
      } else {
        throw new AssertionError();
      }
      if (node != condition) {
        // We need to extract an if-statement if there is an access that
        // executes conditionally.
        needsExtraction = true;
      }
    }
    extractOrderedAccesses(stmtList, condition, conditionAccesses);
    // The recursive call might replace the condition child.
    condition = conditional.getExpression();
    if (needsExtraction) {
      VariableElement resultVar = GeneratedVariableElement.newLocalVar(
          "unseq$" + count++, conditional.getTypeMirror(), currentMethod);
      conditional.replaceWith(new SimpleName(resultVar));
      stmtList.add(new VariableDeclarationStatement(resultVar, null));
      IfStatement newIf = new IfStatement();
      newIf.setExpression(condition.copy());
      stmtList.add(newIf);
      Block thenBlock = new Block();
      newIf.setThenStatement(thenBlock);
      List<Statement> thenStmts = thenBlock.getStatements();
      extractOrderedAccesses(thenStmts, thenExpr, thenAccesses);
      // The recursive call might replace the then expression child.
      thenExpr = conditional.getThenExpression();
      thenStmts.add(new ExpressionStatement(
          new Assignment(new SimpleName(resultVar), thenExpr.copy())));
      Block elseBlock = new Block();
      newIf.setElseStatement(elseBlock);
      List<Statement> elseStmts = elseBlock.getStatements();
      extractOrderedAccesses(elseStmts, elseExpr, elseAccesses);
      // The recursive call might replace the else expression child.
      elseExpr = conditional.getElseExpression();
      elseStmts.add(new ExpressionStatement(
          new Assignment(new SimpleName(resultVar), elseExpr.copy())));
    } else {
      extractOrderedAccesses(stmtList, thenExpr, thenAccesses);
      extractOrderedAccesses(stmtList, elseExpr, elseAccesses);
    }
  }

  private void extractInfixConditional(
      List<Statement> stmtList, InfixExpression conditional, List<VariableAccess> toExtract) {
    InfixExpression.Operator op = conditional.getOperator();
    List<Expression> branches = conditional.getOperands();
    int lastIfExtractIdx = 0;
    VariableElement conditionalVar = null;
    int lastExtracted = 0;
    Expression lastBranch = null;
    for (int i = 0; i < toExtract.size(); i++) {
      VariableAccess access = toExtract.get(i);
      TreeNode node = access.expression;
      while (node.getParent() != conditional) {
        node = node.getParent();
      }
      assert node instanceof Expression;
      Expression branch = (Expression) node;

      // Extract all accesses from the previous branch.
      if (lastBranch != null && branch != lastBranch) {
        extractOrderedAccesses(stmtList, lastBranch, toExtract.subList(lastExtracted, i));
        lastExtracted = i;
      }
      lastBranch = branch;

      // If there's a new access in a new branch, then we extract an if-statement.
      if (branch != branches.get(lastIfExtractIdx)) {
        TypeMirror boolType = typeUtil.getBoolean();
        if (conditionalVar == null) {
          conditionalVar = GeneratedVariableElement.newLocalVar(
              "unseq$" + count++, boolType, currentMethod);
          conditional.replaceWith(new SimpleName(conditionalVar));
          stmtList.add(new VariableDeclarationStatement(conditionalVar, null));
        }
        List<Expression> subBranches = branches.subList(lastIfExtractIdx, branches.indexOf(branch));
        IfStatement newIf = new IfStatement();
        Expression ifExpr = new Assignment(
            new SimpleName(conditionalVar), conditionalFromSubBranches(subBranches, op));
        if (op == InfixExpression.Operator.CONDITIONAL_OR) {
          ifExpr = new PrefixExpression(
              boolType, PrefixExpression.Operator.NOT,
              ParenthesizedExpression.parenthesize(ifExpr));
        }
        newIf.setExpression(ifExpr);
        stmtList.add(newIf);
        Block thenBlock = new Block();
        stmtList = thenBlock.getStatements();
        newIf.setThenStatement(thenBlock);
        lastIfExtractIdx = branches.indexOf(branch);
      }
    }
    extractOrderedAccesses(
        stmtList, lastBranch, toExtract.subList(lastExtracted, toExtract.size()));
    if (conditionalVar != null) {
      List<Expression> remainingBranches = Lists.newArrayList();
      remainingBranches.add(new SimpleName(conditionalVar));
      remainingBranches.addAll(branches.subList(lastIfExtractIdx, branches.size()));
      stmtList.add(new ExpressionStatement(new Assignment(
          new SimpleName(conditionalVar), conditionalFromSubBranches(remainingBranches, op))));
    }
  }

  private Expression conditionalFromSubBranches(
      List<Expression> branches, InfixExpression.Operator op) {
    assert branches.size() >= 1;
    if (branches.size() == 1) {
      return branches.get(0).copy();
    } else {
      InfixExpression result = new InfixExpression(typeUtil.getBoolean(), op);
      TreeUtil.copyList(branches, result.getOperands());
      return result;
    }
  }

  private boolean isConditional(TreeNode node) {
    if (node instanceof InfixExpression) {
      InfixExpression infixExpr = (InfixExpression) node;
      InfixExpression.Operator op = infixExpr.getOperator();
      if (op == InfixExpression.Operator.CONDITIONAL_AND
          || op == InfixExpression.Operator.CONDITIONAL_OR) {
        return true;
      }
    } else if (node instanceof ConditionalExpression) {
      return true;
    }
    return false;
  }

  private void findUnsequenced(List<VariableAccess> accesses, Set<VariableAccess> toExtract) {
    // No conflicts with only one access.
    if (accesses.size() == 1) {
      return;
    }
    Set<VariableAccess> modifications = Sets.newHashSet();
    for (VariableAccess access : accesses) {
      if (access.isModification) {
        modifications.add(access);
      }
    }
    for (VariableAccess modification : modifications) {
      Set<TreeNode> ancestors = getAncestors(modification.expression);
      boolean seenMod = false;
      for (VariableAccess access : accesses) {
        if (modification == access) {
          seenMod = true;
        } else if (isUnsequenced(modification, ancestors, access)) {
          // Only need to extract the first access.
          toExtract.add(seenMod ? modification : access);
        }
      }
    }
  }

  private Set<TreeNode> getAncestors(TreeNode node) {
    Set<TreeNode> ancestors = Sets.newHashSet();
    while (node != currentTopNode) {
      ancestors.add(node);
      node = node.getParent();
    }
    return ancestors;
  }

  private boolean isUnsequenced(
      VariableAccess modification, Set<TreeNode> modificationAncestors, VariableAccess access) {
    TreeNode commonAncestor = currentTopNode;
    TreeNode node = access.expression;
    while (node != currentTopNode) {
      if (modificationAncestors.contains(node)) {
        commonAncestor = node;
        break;
      }
      node = node.getParent();
    }
    // If either access is executed in a conditional branch that does not
    // contain the other access, then they are not unsequenced.
    if (isWithinConditionalBranch(modification.expression, commonAncestor)
        || isWithinConditionalBranch(access.expression, commonAncestor)) {
      return false;
    } else if (commonAncestor instanceof CommaExpression) {
      return false;
    } else if (commonAncestor instanceof Assignment && modification.expression == commonAncestor) {
      // "i = 1 + (i = 2);" is not unsequenced.
      // "i = 1 + i++;" is unsequenced (according to clang).
      return access.expression instanceof PrefixExpression
          || access.expression instanceof PostfixExpression;
    }
    return true;
  }

  private boolean isWithinConditionalBranch(TreeNode node, TreeNode limit) {
    while (node != limit) {
      TreeNode parent = node.getParent();
      if (isConditional(parent) && getConditionChild(parent) != node) {
        return true;
      }
      node = parent;
    }
    return false;
  }

  private Expression getConditionChild(TreeNode conditional) {
    if (conditional instanceof InfixExpression) {
      return ((InfixExpression) conditional).getOperand(0);
    } else if (conditional instanceof ConditionalExpression) {
      return ((ConditionalExpression) conditional).getExpression();
    } else {
      throw new AssertionError(
          "Unexpected conditional node type: " + conditional.getClass().toString());
    }
  }

  @Override
  public void endVisit(SimpleName node) {
    addVariableAccess(TreeUtil.getVariableElement(node), node, false);
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    visitAndExtract(node.getExpression(), node);
    return false;
  }

  @Override
  public boolean visit(ReturnStatement node) {
    visitAndExtract(node.getExpression(), node);
    return false;
  }

  @Override
  public boolean visit(AssertStatement node) {
    Expression expr = node.getExpression();
    visitAndExtract(expr, node);
    Expression msg = node.getMessage();
    if (msg != null) {
      newExpression(msg);
      msg.accept(this);
      List<VariableAccess> toExtract = getUnsequencedAccesses();
      if (!toExtract.isEmpty()) {
        // If the message expression needs any extraction, then we first extract
        // the entire boolean expression to preserve ordering between the two.
        VariableElement exprVar = GeneratedVariableElement.newLocalVar(
            "unseq$" + count++, expr.getTypeMirror(), currentMethod);
        TreeUtil.insertBefore(node, new VariableDeclarationStatement(
            exprVar, node.getExpression().copy()));
        node.setExpression(new SimpleName(exprVar));
        extractOrderedAccesses(
            TreeUtil.asStatementList(node).subList(0, 0), currentTopNode, toExtract);
      }
    }
    return false;
  }

  @Override
  public boolean visit(ConstructorInvocation node) {
    newExpression(node);
    for (Expression arg : node.getArguments()) {
      arg.accept(this);
    }
    extractUnsequenced(node);
    return false;
  }

  @Override
  public boolean visit(SuperConstructorInvocation node) {
    newExpression(node);
    for (Expression arg : node.getArguments()) {
      arg.accept(this);
    }
    extractUnsequenced(node);
    return false;
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    visitAndExtract(node.getExpression(), node);
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    extractVariableDeclarationFragments(
        node.getFragments(), TreeUtil.asStatementList(node).subList(0, 0));
    return false;
  }

  @Override
  public boolean visit(IfStatement node) {
    visitAndExtract(node.getExpression(), node);
    node.getThenStatement().accept(this);
    Statement elseStmt = node.getElseStatement();
    if (elseStmt != null) {
      elseStmt.accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(SwitchStatement node) {
    visitAndExtract(node.getExpression(), node);
    for (Statement stmt : node.getStatements()) {
      stmt.accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(SynchronizedStatement node) {
    visitAndExtract(node.getExpression(), node);
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(ThrowStatement node) {
    visitAndExtract(node.getExpression(), node);
    return false;
  }

  private IfStatement createLoopTermination(Expression loopCondition) {
    IfStatement newIf = new IfStatement();
    newIf.setExpression(new PrefixExpression(
        typeUtil.getBoolean(), PrefixExpression.Operator.NOT,
        ParenthesizedExpression.parenthesize(loopCondition.copy())));
    newIf.setThenStatement(new BreakStatement());
    return newIf;
  }

  @Override
  public boolean visit(WhileStatement node) {
    node.getBody().accept(this);
    newExpression(node.getExpression());
    node.getExpression().accept(this);
    List<VariableAccess> toExtract = getUnsequencedAccesses();
    if (!toExtract.isEmpty()) {
      // Convert "while (cond)" into "while (true) { if (!(cond)) break; ... }".
      List<Statement> stmtList = TreeUtil.asStatementList(node.getBody()).subList(0, 0);
      extractOrderedAccesses(stmtList, currentTopNode, toExtract);
      stmtList.add(createLoopTermination(node.getExpression()));
      node.setExpression(new BooleanLiteral(true, typeUtil));
    }
    return false;
  }

  @Override
  public boolean visit(DoStatement node) {
    node.getBody().accept(this);
    newExpression(node.getExpression());
    node.getExpression().accept(this);
    List<VariableAccess> toExtract = getUnsequencedAccesses();
    if (!toExtract.isEmpty()) {
      // Convert "while (cond)" into "while (true) { if (!(cond)) break; ... }".
      List<Statement> stmtList = TreeUtil.asStatementList(node.getBody());
      extractOrderedAccesses(stmtList, currentTopNode, toExtract);
      stmtList.add(createLoopTermination(node.getExpression()));
      node.setExpression(new BooleanLiteral(true, typeUtil));
    }
    return false;
  }

  private void extractVariableDeclarationFragments(
      List<VariableDeclarationFragment> fragments, List<Statement> stmtList) {
    for (int i = 0; i < fragments.size(); i++) {
      VariableDeclarationFragment frag = fragments.get(i);
      Expression init = frag.getInitializer();
      if (init == null) {
        continue;
      }
      newExpression(init);
      init.accept(this);
      List<VariableAccess> toExtract = getUnsequencedAccesses();
      if (!toExtract.isEmpty()) {
        if (i > 0) {
          // Extract all fragments before the current one to preserve ordering.
          VariableDeclarationStatement newDecl =
              new VariableDeclarationStatement(fragments.get(0).copy());
          for (int j = 1; j < i; j++) {
            newDecl.addFragment(fragments.get(j).copy());
          }
          stmtList.add(newDecl);
          fragments.subList(0, i).clear();
        }
        extractOrderedAccesses(stmtList, currentTopNode, toExtract);
        i = 0;
      }
    }
  }

  private void extractExpressionList(
      List<Expression> expressions, List<Statement> stmtList, boolean extractModifiedExpression) {
    for (int i = 0; i < expressions.size(); i++) {
      Expression expr = expressions.get(i);
      newExpression(expr);
      expr.accept(this);
      List<VariableAccess> unsequencedAccesses = getUnsequencedAccesses();
      if (!unsequencedAccesses.isEmpty()) {
        for (int j = 0; j < i; j++) {
          stmtList.add(new ExpressionStatement(expressions.get(j).copy()));
        }
        expressions.subList(0, i).clear();
        extractOrderedAccesses(stmtList, currentTopNode, unsequencedAccesses);
        i = 0;
        if (extractModifiedExpression) {
          stmtList.add(new ExpressionStatement(expressions.get(0).copy()));
          expressions.remove(0);
          i = -1;
        }
      }
    }
  }

  @Override
  public boolean visit(ForStatement node) {
    List<Expression> initializers = node.getInitializers();
    // The for-loop initializers can either be a single variable declaration
    // expression or a list of initializer expressions.
    if (initializers.size() == 1 && initializers.get(0) instanceof VariableDeclarationExpression) {
      VariableDeclarationExpression decl = (VariableDeclarationExpression) initializers.get(0);
      extractVariableDeclarationFragments(
          decl.getFragments(), TreeUtil.asStatementList(node).subList(0, 0));
    } else {
      extractExpressionList(initializers, TreeUtil.asStatementList(node).subList(0, 0), false);
    }
    Expression expr = node.getExpression();
    if (expr != null) {
      newExpression(expr);
      expr.accept(this);
      List<VariableAccess> toExtract = getUnsequencedAccesses();
      if (!toExtract.isEmpty()) {
        // Convert "if (;cond;)" into "if (;;) { if (!(cond)) break; ...}".
        List<Statement> stmtList = TreeUtil.asStatementList(node.getBody()).subList(0, 0);
        extractOrderedAccesses(stmtList, currentTopNode, toExtract);
        stmtList.add(createLoopTermination(node.getExpression()));
        node.setExpression(null);
      }
    }
    extractExpressionList(node.getUpdaters(), TreeUtil.asStatementList(node.getBody()), true);
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    VariableElement lhsVar = TreeUtil.getVariableElement(lhs);
    // Access order is important. If the lhs is a variable, then we must record
    // its access after visiting the rhs. Otherwise, visit both sides.
    if (lhsVar == null) {
      lhs.accept(this);
    }
    node.getRightHandSide().accept(this);
    addVariableAccess(lhsVar, node, true);
    return false;
  }

  @Override
  public boolean visit(PrefixExpression node) {
    PrefixExpression.Operator op = node.getOperator();
    if (op == PrefixExpression.Operator.INCREMENT || op == PrefixExpression.Operator.DECREMENT) {
      addVariableAccess(TreeUtil.getVariableElement(node.getOperand()), node, true);
    } else {
      node.getOperand().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(PostfixExpression node) {
    PostfixExpression.Operator op = node.getOperator();
    assert op == PostfixExpression.Operator.INCREMENT || op == PostfixExpression.Operator.DECREMENT;
    addVariableAccess(TreeUtil.getVariableElement(node.getOperand()), node, true);
    return false;
  }
}
