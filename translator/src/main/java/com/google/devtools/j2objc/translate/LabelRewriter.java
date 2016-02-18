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

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BreakStatement;
import com.google.devtools.j2objc.ast.ContinueStatement;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EmptyStatement;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.LabeledStatement;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.WhileStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * Rewrites multiple labels that have the same name.
 * Rewrites labeled break and continue statements, converting them to goto for
 * the correct control flow.
 *
 * @author Tom Ball, Keith Stanger
 */
public class LabelRewriter extends TreeVisitor {

  @Override
  public boolean visit(MethodDeclaration node) {
    // Rename any labels that have the same names; legal in Java but not C.
    final Map<String, Integer> labelCounts = new HashMap<>();
    node.accept(new TreeVisitor() {
      @Override
      public void endVisit(LabeledStatement labeledStatement) {
        final String name = labeledStatement.getLabel().getIdentifier();
        int value = labelCounts.containsKey(name) ? labelCounts.get(name) + 1 : 1;
        labelCounts.put(name, value);
        if (value > 1) {
          final String newName = name + '_' + value;
          labeledStatement.setLabel(new SimpleName(newName));
          // Update references to this label.
          labeledStatement.accept(new TreeVisitor() {
            @Override
            public void endVisit(ContinueStatement node) {
              if (node.getLabel() != null && node.getLabel().getIdentifier().equals(name)) {
                node.setLabel(new SimpleName(newName));
              }
            }
            @Override
            public void endVisit(BreakStatement node) {
              if (node.getLabel() != null && node.getLabel().getIdentifier().equals(name)) {
                node.setLabel(new SimpleName(newName));
              }
            }
          });

        }
      }
    });
    return true;
  }

  private static Statement getLoopBody(Statement s) {
    if (s instanceof DoStatement) {
      return ((DoStatement) s).getBody();
    } else if (s instanceof EnhancedForStatement) {
      return ((EnhancedForStatement) s).getBody();
    } else if (s instanceof ForStatement) {
      return ((ForStatement) s).getBody();
    } else if (s instanceof WhileStatement) {
      return ((WhileStatement) s).getBody();
    }
    return null;
  }

  @Override
  public void endVisit(LabeledStatement node) {
    Statement loopBody = getLoopBody(node.getBody());

    final String labelIdentifier = node.getLabel().getIdentifier();

    final boolean[] hasContinue = new boolean[1];
    final boolean[] hasBreak = new boolean[1];
    node.accept(new TreeVisitor() {
      @Override
      public void endVisit(ContinueStatement node) {
        if (node.getLabel() != null && node.getLabel().getIdentifier().equals(labelIdentifier)) {
          hasContinue[0] = true;
          node.setLabel(new SimpleName("continue_" + labelIdentifier));
        }
      }
      @Override
      public void endVisit(BreakStatement node) {
        if (node.getLabel() != null && node.getLabel().getIdentifier().equals(labelIdentifier)) {
          hasBreak[0] = true;
          node.setLabel(new SimpleName("break_" + labelIdentifier));
        }
      }
    });

    if (hasContinue[0]) {
      assert loopBody != null : "Continue statements must be inside a loop.";
      LabeledStatement newLabelStmt = new LabeledStatement("continue_" + labelIdentifier);
      newLabelStmt.setBody(new EmptyStatement());
      // Put the loop body into an inner block so the continue label is outside
      // the scope of any variable initializations.
      Block newBlock = new Block();
      loopBody.replaceWith(newBlock);
      newBlock.getStatements().add(loopBody);
      newBlock.getStatements().add(newLabelStmt);
    }
    if (hasBreak[0]) {
      LabeledStatement newLabelStmt = new LabeledStatement("break_" + labelIdentifier);
      newLabelStmt.setBody(new EmptyStatement());
      TreeUtil.insertAfter(node, newLabelStmt);
    }

    if (hasContinue[0] || hasBreak[0]) {
      // Replace this node with its statement, thus deleting the label.
      node.replaceWith(TreeUtil.remove(node.getBody()));
    }
  }
}
