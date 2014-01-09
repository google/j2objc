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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Detects unsequenced modifications which are errors in ObjC and extracts
 * portions of the expression to eliminate the errors.
 *
 * This pass must occur after rewriting of labeled break and continue statements
 * otherwise extracted loop conditions will be out of order with the labels
 * inserted by the labeled break and continue rewriting.
 *
 * TODO(kstanger): Add support for remaining statement types.
 *
 * @author Keith Stanger
 */
public class UnsequencedExpressionRewriter extends ErrorReportingASTVisitor {

  private IMethodBinding currentMethod = null;
  private int count = 1;
  private List<VariableAccess> orderedAccesses = Lists.newArrayList();
  private ASTNode currentTopNode = null;
  private boolean currentIsConditional = false;
  private boolean hasModification = false;

  /**
   * Metadata for a given read or write access of a variable within an
   * expression.
   */
  private class VariableAccess {

    private final IVariableBinding variable;
    private final Expression expression;
    private final boolean isModification;
    private final boolean isConditional;

    private VariableAccess(
        IVariableBinding variable, Expression expression, boolean isModification,
        boolean isConditional) {
      this.variable = variable;
      this.expression = expression;
      this.isModification = isModification;
      this.isConditional = isConditional;
    }
  }

  private void addVariableAccess(IVariableBinding var, Expression node, boolean isModification) {
    if (var != null) {
      hasModification |= isModification;
      orderedAccesses.add(new VariableAccess(var, node, isModification, currentIsConditional));
    }
  }

  private void newExpression(ASTNode topNode) {
    orderedAccesses.clear();
    currentTopNode = topNode;
    hasModification = false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    currentMethod = Types.getMethodBinding(node);
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
    ListMultimap<IVariableBinding, VariableAccess> accessesByVar = ArrayListMultimap.create();
    for (VariableAccess access : orderedAccesses) {
      accessesByVar.put(access.variable, access);
    }
    Set<VariableAccess> unsequencedAccesses = Sets.newHashSet();
    for (IVariableBinding var : accessesByVar.keySet()) {
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
          stmt.getAST(), ASTUtil.asStatementList(stmt).subList(0, 0), currentTopNode, accesses);
    }
  }

  private void extractOrderedAccesses(
      AST ast, List<Statement> stmtList, ASTNode subExpr, List<VariableAccess> toExtract) {
    for (int i = 0; i < toExtract.size(); i++) {
      VariableAccess access = toExtract.get(i);
      ASTNode topConditional = getTopConditional(access.expression, subExpr);
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
              ast, stmtList, (InfixExpression) topConditional, toExtract.subList(i, j));
        } else if (topConditional instanceof ConditionalExpression) {
          extractConditionalExpression(
              ast, stmtList, (ConditionalExpression) topConditional, toExtract.subList(i, j));
        } else {
          throw new AssertionError(
              "Unexpected conditional node type: " + topConditional.getClass().toString());
        }
        i = j - 1;
      } else {
        IVariableBinding newVar = new GeneratedVariableBinding(
            "unseq$" + count++, 0, Types.getTypeBinding(access.expression), false, false, null,
            currentMethod);
        stmtList.add(ASTFactory.newVariableDeclarationStatement(
            ast, newVar, NodeCopier.copySubtree(ast, access.expression)));
        ASTUtil.setProperty(access.expression, ASTFactory.newSimpleName(ast, newVar));
      }
    }
  }

  private ASTNode getTopConditional(ASTNode node, ASTNode limit) {
    ASTNode topConditional = null;
    while (node != limit) {
      node = node.getParent();
      if (isConditional(node)) {
        topConditional = node;
      }
    }
    return topConditional;
  }

  private void extractConditionalExpression(
      AST ast, List<Statement> stmtList, ConditionalExpression conditional,
      List<VariableAccess> toExtract) {
    Expression condition = conditional.getExpression();
    Expression thenExpr = conditional.getThenExpression();
    Expression elseExpr = conditional.getElseExpression();
    List<VariableAccess> conditionAccesses = Lists.newArrayList();
    List<VariableAccess> thenAccesses = Lists.newArrayList();
    List<VariableAccess> elseAccesses = Lists.newArrayList();
    boolean needsExtraction = false;
    for (VariableAccess access : toExtract) {
      ASTNode node = access.expression;
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
      if (node != condition && access.isModification) {
        // We only need to extract an if-statement if there is a modification
        // that executes conditionally.
        needsExtraction = true;
      }
    }
    extractOrderedAccesses(ast, stmtList, condition, conditionAccesses);
    // The recursive call might replace the condition child.
    condition = conditional.getExpression();
    if (needsExtraction) {
      IVariableBinding resultVar = new GeneratedVariableBinding(
          "unseq$" + count++, 0, Types.getTypeBinding(conditional), false, false, null,
          currentMethod);
      ASTUtil.setProperty(conditional, ASTFactory.newSimpleName(ast, resultVar));
      stmtList.add(ASTFactory.newVariableDeclarationStatement(ast, resultVar, null));
      IfStatement newIf = ast.newIfStatement();
      newIf.setExpression(NodeCopier.copySubtree(ast, condition));
      stmtList.add(newIf);
      Block thenBlock = ast.newBlock();
      newIf.setThenStatement(thenBlock);
      List<Statement> thenStmts = ASTUtil.getStatements(thenBlock);
      extractOrderedAccesses(ast, thenStmts, thenExpr, thenAccesses);
      // The recursive call might replace the then expression child.
      thenExpr = conditional.getThenExpression();
      thenStmts.add(ast.newExpressionStatement(ASTFactory.newAssignment(
          ast, ASTFactory.newSimpleName(ast, resultVar), NodeCopier.copySubtree(ast, thenExpr))));
      Block elseBlock = ast.newBlock();
      newIf.setElseStatement(elseBlock);
      List<Statement> elseStmts = ASTUtil.getStatements(elseBlock);
      extractOrderedAccesses(ast, elseStmts, elseExpr, elseAccesses);
      // The recursive call might replace the else expression child.
      thenExpr = conditional.getElseExpression();
      elseStmts.add(ast.newExpressionStatement(ASTFactory.newAssignment(
          ast, ASTFactory.newSimpleName(ast, resultVar), NodeCopier.copySubtree(ast, elseExpr))));
    } else {
      extractOrderedAccesses(ast, stmtList, thenExpr, thenAccesses);
      extractOrderedAccesses(ast, stmtList, elseExpr, elseAccesses);
    }
  }

  private void extractInfixConditional(
      AST ast, List<Statement> stmtList, InfixExpression conditional,
      List<VariableAccess> toExtract) {
    InfixExpression.Operator op = conditional.getOperator();
    List<Expression> branches = getBranches(conditional);
    int lastIfExtractIdx = 0;
    IVariableBinding conditionalVar = null;
    int lastExtracted = 0;
    Expression lastBranch = null;
    for (int i = 0; i < toExtract.size(); i++) {
      VariableAccess access = toExtract.get(i);
      ASTNode node = access.expression;
      while (node.getParent() != conditional) {
        node = node.getParent();
      }
      assert node instanceof Expression;
      Expression branch = (Expression) node;

      // Extract all accesses from the previous branch.
      if (lastBranch != null && branch != lastBranch) {
        extractOrderedAccesses(ast, stmtList, lastBranch, toExtract.subList(lastExtracted, i));
        // The recursive call might replace some of the children.
        branches = getBranches(conditional);
        lastExtracted = i;
      }
      lastBranch = branch;

      // If there's a new modification in a new branch, then we extract an if-statement.
      if (access.isModification && branch != branches.get(lastIfExtractIdx)) {
        if (conditionalVar == null) {
          conditionalVar = new GeneratedVariableBinding(
              "unseq$" + count++, 0, Types.resolveJavaType("boolean"), false, false, null,
              currentMethod);
          ASTUtil.setProperty(conditional, ASTFactory.newSimpleName(ast, conditionalVar));
          stmtList.add(ASTFactory.newVariableDeclarationStatement(ast, conditionalVar, null));
        }
        List<Expression> subBranches = branches.subList(lastIfExtractIdx, branches.indexOf(branch));
        IfStatement newIf = ast.newIfStatement();
        Expression ifExpr = ASTFactory.newAssignment(
            ast, ASTFactory.newSimpleName(ast, conditionalVar),
            conditionalFromSubBranches(ast, subBranches, op));
        if (op == InfixExpression.Operator.CONDITIONAL_OR) {
          ifExpr = ASTFactory.newPrefixExpression(
              ast, PrefixExpression.Operator.NOT,
              ASTFactory.newParenthesizedExpression(ast, ifExpr), "boolean");
        }
        newIf.setExpression(ifExpr);
        stmtList.add(newIf);
        Block thenBlock = ast.newBlock();
        stmtList = ASTUtil.getStatements(thenBlock);
        newIf.setThenStatement(thenBlock);
        lastIfExtractIdx = branches.indexOf(branch);
      }
    }
    extractOrderedAccesses(
        ast, stmtList, lastBranch, toExtract.subList(lastExtracted, toExtract.size()));
    // The recursive call might replace some of the children.
    branches = getBranches(conditional);
    if (conditionalVar != null) {
      List<Expression> remainingBranches = Lists.newArrayList();
      remainingBranches.add(ASTFactory.newSimpleName(ast, conditionalVar));
      remainingBranches.addAll(branches.subList(lastIfExtractIdx, branches.size()));
      stmtList.add(ast.newExpressionStatement(ASTFactory.newAssignment(
          ast, ASTFactory.newSimpleName(ast, conditionalVar),
          conditionalFromSubBranches(ast, remainingBranches, op))));
    }
  }

  private List<Expression> getBranches(InfixExpression expr) {
    List<Expression> result = Lists.newArrayList();
    result.add(expr.getLeftOperand());
    result.add(expr.getRightOperand());
    result.addAll(ASTUtil.getExtendedOperands(expr));
    return result;
  }

  private Expression conditionalFromSubBranches(
      AST ast, List<Expression> branches, InfixExpression.Operator op) {
    assert branches.size() >= 1;
    if (branches.size() == 1) {
      return NodeCopier.copySubtree(ast, branches.get(0));
    } else {
      InfixExpression result = ASTFactory.newInfixExpression(
          ast, NodeCopier.copySubtree(ast, branches.get(0)), op,
          NodeCopier.copySubtree(ast, branches.get(1)), Types.resolveJavaType("boolean"));
      for (int i = 2; i < branches.size(); i++) {
        ASTUtil.getExtendedOperands(result).add(branches.get(i));
      }
      return result;
    }
  }

  private boolean isConditional(ASTNode node) {
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
      Set<ASTNode> ancestors = getAncestors(modification.expression);
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

  private Set<ASTNode> getAncestors(ASTNode node) {
    Set<ASTNode> ancestors = Sets.newHashSet();
    while (node != currentTopNode) {
      ancestors.add(node);
      node = node.getParent();
    }
    return ancestors;
  }

  private boolean isUnsequenced(
      VariableAccess modification, Set<ASTNode> modificationAncestors, VariableAccess access) {
    ASTNode commonAncestor = currentTopNode;
    ASTNode node = access.expression;
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
    } else if (commonAncestor instanceof Assignment) {
      // "i = 1 + (i = 2);" is not unsequenced.
      // "i = 1 + i++;" is unsequenced (according to clang).
      return access.expression instanceof PrefixExpression
          || access.expression instanceof PostfixExpression;
    }
    return true;
  }

  private boolean isWithinConditionalBranch(ASTNode node, ASTNode limit) {
    while (node != limit) {
      ASTNode parent = node.getParent();
      if (isConditional(parent) && getConditionChild(parent) != node) {
        return true;
      }
      node = parent;
    }
    return false;
  }

  private Expression getConditionChild(ASTNode conditional) {
    if (conditional instanceof InfixExpression) {
      return ((InfixExpression) conditional).getLeftOperand();
    } else if (conditional instanceof ConditionalExpression) {
      return ((ConditionalExpression) conditional).getExpression();
    } else {
      throw new AssertionError(
          "Unexpected conditional node type: " + conditional.getClass().toString());
    }
  }

  @Override
  public void endVisit(SimpleName node) {
    IVariableBinding var = Types.getVariableBinding(node);
    addVariableAccess(var, node, false);
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    Expression expr = node.getExpression();
    newExpression(expr);
    expr.accept(this);
    extractUnsequenced(node);
    return false;
  }

  @Override
  public boolean visit(ReturnStatement node) {
    Expression expr = node.getExpression();
    if (expr != null) {
      newExpression(expr);
      expr.accept(this);
      extractUnsequenced(node);
    }
    return false;
  }

  @Override
  public boolean visit(AssertStatement node) {
    Expression expr = node.getExpression();
    newExpression(expr);
    expr.accept(this);
    extractUnsequenced(node);
    Expression msg = node.getMessage();
    newExpression(msg);
    msg.accept(this);
    List<VariableAccess> toExtract = getUnsequencedAccesses();
    if (!toExtract.isEmpty()) {
      // If the message expression needs any extraction, then we first extract
      // the entire boolean expression to preserve ordering between the two.
      AST ast = node.getAST();
      IVariableBinding exprVar = new GeneratedVariableBinding(
          "unseq$" + count++, 0, Types.getTypeBinding(expr), false, false, null,
          currentMethod);
      ASTUtil.insertBefore(node, ASTFactory.newVariableDeclarationStatement(
          ast, exprVar, NodeCopier.copySubtree(ast, node.getExpression())));
      node.setExpression(ASTFactory.newSimpleName(ast, exprVar));
      extractOrderedAccesses(
          ast, ASTUtil.asStatementList(node).subList(0, 0), currentTopNode, toExtract);
    }
    return false;
  }

  @Override
  public boolean visit(ConstructorInvocation node) {
    newExpression(node);
    for (Expression arg : ASTUtil.getArguments(node)) {
      arg.accept(this);
    }
    extractUnsequenced(node);
    return false;
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    Expression expr = node.getExpression();
    newExpression(expr);
    expr.accept(this);
    extractUnsequenced(node);
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    extractVariableDeclarationFragments(
        node.getAST(), ASTUtil.getFragments(node), ASTUtil.asStatementList(node).subList(0, 0));
    return false;
  }

  private IfStatement createLoopTermination(Expression loopCondition) {
    AST ast = loopCondition.getAST();
    IfStatement newIf = ast.newIfStatement();
    newIf.setExpression(ASTFactory.newPrefixExpression(
        ast, PrefixExpression.Operator.NOT,
        ASTFactory.newParenthesizedExpression(ast, NodeCopier.copySubtree(ast, loopCondition)),
        "boolean"));
    newIf.setThenStatement(ast.newBreakStatement());
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
      AST ast = node.getAST();
      List<Statement> stmtList = ASTUtil.asStatementList(node.getBody()).subList(0, 0);
      extractOrderedAccesses(ast, stmtList, currentTopNode, toExtract);
      stmtList.add(createLoopTermination(node.getExpression()));
      node.setExpression(ASTFactory.newBooleanLiteral(ast, true));
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
      AST ast = node.getAST();
      List<Statement> stmtList = ASTUtil.asStatementList(node.getBody());
      extractOrderedAccesses(ast, stmtList, currentTopNode, toExtract);
      stmtList.add(createLoopTermination(node.getExpression()));
      node.setExpression(ASTFactory.newBooleanLiteral(ast, true));
    }
    return false;
  }

  private void extractVariableDeclarationFragments(
      AST ast, List<VariableDeclarationFragment> fragments, List<Statement> stmtList) {
    for (int i = 0; i < fragments.size(); i++) {
      VariableDeclarationFragment frag = fragments.get(i);
      Expression init = frag.getInitializer();
      newExpression(init);
      init.accept(this);
      List<VariableAccess> toExtract = getUnsequencedAccesses();
      if (!toExtract.isEmpty()) {
        if (i > 0) {
          // Extract all fragments before the current one to preserve ordering.
          VariableDeclarationStatement newDecl = ASTFactory.newVariableDeclarationStatement(
              ast, NodeCopier.copySubtree(ast, fragments.get(0)));
          for (int j = 1; j < i; j++) {
            ASTUtil.getFragments(newDecl).add(NodeCopier.copySubtree(ast, fragments.get(j)));
          }
          stmtList.add(newDecl);
          fragments.subList(0, i).clear();
        }
        extractOrderedAccesses(ast, stmtList, currentTopNode, toExtract);
        i = 0;
      }
    }
  }

  private void extractExpressionList(
      AST ast, List<Expression> expressions, List<Statement> stmtList,
      boolean extractModifiedExpression) {
    for (int i = 0; i < expressions.size(); i++) {
      Expression expr = expressions.get(i);
      newExpression(expr);
      expr.accept(this);
      List<VariableAccess> unsequencedAccesses = getUnsequencedAccesses();
      if (!unsequencedAccesses.isEmpty()) {
        int numToExtract = extractModifiedExpression ? i + 1 : i;
        for (int j = 0; j < i; j++) {
          stmtList.add(ast.newExpressionStatement(
              NodeCopier.copySubtree(ast, expressions.get(j))));
        }
        expressions.subList(0, i).clear();
        extractOrderedAccesses(ast, stmtList, currentTopNode, unsequencedAccesses);
        i = 0;
        if (extractModifiedExpression) {
          stmtList.add(ast.newExpressionStatement(
              NodeCopier.copySubtree(ast, expressions.get(0))));
          expressions.remove(0);
          i = -1;
        }
      }
    }
  }

  @Override
  public boolean visit(ForStatement node) {
    AST ast = node.getAST();
    List<Expression> initializers = ASTUtil.getInitializers(node);
    // The for-loop initializers can either be a single variable declaration
    // expression or a list of initializer expressions.
    if (initializers.size() == 1 && initializers.get(0) instanceof VariableDeclarationExpression) {
      VariableDeclarationExpression decl = (VariableDeclarationExpression) initializers.get(0);
      extractVariableDeclarationFragments(
          ast, ASTUtil.getFragments(decl), ASTUtil.asStatementList(node).subList(0, 0));
    } else {
      extractExpressionList(ast, initializers, ASTUtil.asStatementList(node).subList(0, 0), false);
    }
    Expression expr = node.getExpression();
    if (expr != null) {
      newExpression(expr);
      expr.accept(this);
      List<VariableAccess> toExtract = getUnsequencedAccesses();
      if (!toExtract.isEmpty()) {
        // Convert "if (;cond;)" into "if (;;) { if (!(cond)) break; ...}".
        List<Statement> stmtList = ASTUtil.asStatementList(node.getBody()).subList(0, 0);
        extractOrderedAccesses(ast, stmtList, currentTopNode, toExtract);
        stmtList.add(createLoopTermination(node.getExpression()));
        node.setExpression(null);
      }
    }
    extractExpressionList(
        ast, ASTUtil.getUpdaters(node), ASTUtil.asStatementList(node.getBody()), true);
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(Assignment node) {
    // We can't visit the left hand side otherwise the SimpleName visitor will
    // add an additional access for the assigned variable.
    node.getRightHandSide().accept(this);
    addVariableAccess(Types.getVariableBinding(node.getLeftHandSide()), node, true);
    return false;
  }

  @Override
  public boolean visit(PrefixExpression node) {
    PrefixExpression.Operator op = node.getOperator();
    if (op == PrefixExpression.Operator.INCREMENT || op == PrefixExpression.Operator.DECREMENT) {
      addVariableAccess(Types.getVariableBinding(node.getOperand()), node, true);
    } else {
      node.getOperand().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(PostfixExpression node) {
    PostfixExpression.Operator op = node.getOperator();
    assert op == PostfixExpression.Operator.INCREMENT || op == PostfixExpression.Operator.DECREMENT;
    addVariableAccess(Types.getVariableBinding(node.getOperand()), node, true);
    return false;
  }
}
