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

package com.google.devtools.j2objc.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import java.util.Stack;

/**
 * Extends ASTVisitor to report uncaught exceptions in subclasses as
 * errors with the ASTNode where the error was thrown.
 *
 * @author Tom Ball
 */
public class ErrorReportingASTVisitor extends ASTVisitor {
  private Stack<ASTNode> stack = new Stack<ASTNode>();

  /**
   * Executes this visitor on a specified node.  This entry point should
   * be used instead of ASTVisitor.visit(), so exception can be caught and
   * reported.
   *
   * @param node the top-level node to visit.
   */
  public void run(ASTNode node) throws ASTNodeException {
    try {
      node.accept(this);
    } catch (Throwable t) {
      throw new ASTNodeException(stack.peek(), t);
    }
  }

  @Override
  public void preVisit(ASTNode node) {
    stack.push(node);
  }

  @Override
  public void postVisit(ASTNode node) {
    assert stack.peek() == node;
    stack.pop();
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    return false;
  }

  @Override
  public boolean visit(ImportDeclaration node) {
    return false;
  }
}
