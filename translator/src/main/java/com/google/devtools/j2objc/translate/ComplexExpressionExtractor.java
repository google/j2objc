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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Detects deep expression trees and extracts them into separate statements.
 *
 * @author Keith Stanger
 */
public class ComplexExpressionExtractor extends ErrorReportingASTVisitor {

  // The ObjC compiler tends to fail with roughly 100 chained method calls.
  private static final int DEFAULT_MAX_DEPTH = 50;

  private static int maxDepth = DEFAULT_MAX_DEPTH;
  private Map<Expression, Integer> depths = Maps.newHashMap();
  private IMethodBinding currentMethod;
  private Statement currentStatement;
  private int count = 1;

  @VisibleForTesting
  static void setMaxDepth(int newMaxDepth) {
    maxDepth = newMaxDepth;
  }

  @VisibleForTesting
  static void resetMaxDepth() {
    maxDepth = DEFAULT_MAX_DEPTH;
  }

  private void handleNode(Expression node, Collection<Expression> children) {
    if (node.getParent() instanceof Statement) {
      return;
    }
    int depth = 0;
    for (Expression child : children) {
      Integer childDepth = depths.get(child);
      depth = Math.max(depth, childDepth != null ? childDepth : 1);
    }
    if (depth >= maxDepth) {
      AST ast = node.getAST();
      ITypeBinding type = Types.getTypeBinding(node);
      assert currentMethod != null; // Should be OK if run after InitializationNormalizer.
      IVariableBinding newVar = new GeneratedVariableBinding(
          "complex$" + count++, 0, type, false, false, null, currentMethod);
      Statement newStmt = ASTFactory.newVariableDeclarationStatement(
          ast, newVar, NodeCopier.copySubtree(ast, node));
      assert currentStatement != null;
      ASTUtil.insertBefore(currentStatement, newStmt);
      ASTUtil.setProperty(node, ASTFactory.newSimpleName(ast, newVar));
    } else {
      depths.put(node, depth + 1);
    }
  }

  @Override
  public void preVisit(ASTNode node) {
    super.preVisit(node);
    if (node instanceof Statement) {
      currentStatement = (Statement) node;
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    currentMethod = Types.getMethodBinding(node);
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    currentMethod = null;
  }

  @Override
  public void endVisit(InfixExpression node) {
    handleNode(node, ImmutableList.of(node.getLeftOperand(), node.getRightOperand()));
  }

  @Override
  public void endVisit(MethodInvocation node) {
    Expression receiver = node.getExpression();
    List<Expression> args = ASTUtil.getArguments(node);
    List<Expression> children = Lists.newArrayListWithCapacity(args.size() + 1);
    if (receiver != null) {
      children.add(receiver);
    }
    handleNode(node, children);
  }
}
