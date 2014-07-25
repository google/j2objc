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

package com.google.devtools.j2objc.util;

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Provides convenient static error and warning methods.
 *
 * @author Tom Ball, Keith Stanger
 */
public class ErrorUtil {

  private static int errorCount = 0;
  private static int warningCount = 0;
  private static int functionizedMethodCount = 0;
  private static String currentFileName = null;
  private static PrintStream errorStream = System.err;
  private static List<String> errorMessages = Lists.newArrayList();

  public static void reset() {
    errorCount = 0;
    warningCount = 0;
    currentFileName = null;
    errorMessages = Lists.newArrayList();
  }

  public static void setCurrentFileName(String name) {
    currentFileName = name;
  }

  public static int errorCount() {
    return errorCount;
  }

  public static int warningCount() {
    return warningCount;
  }

  public static List<String> getErrorMessages() {
    return errorMessages;
  }

  /**
   * To be called by unit tests. In test mode errors and warnings are not
   * printed to System.err.
   */
  public static void setTestMode() {
    errorStream = new PrintStream(new OutputStream() {
      public void write(int b) {}
    });
  }

  public static void error(String message) {
    errorMessages.add(message);
    errorStream.println("error: " + message);
    errorCount++;
  }

  public static void warning(String message) {
    errorStream.println("warning: " + message);
    warningCount++;
  }

  /**
   * Report an error with a specific AST node.
   */
  public static void error(ASTNode node, String message) {
    int line = getNodeLine(node);
    error(String.format("%s:%s: %s", currentFileName, line, message));
  }

  public static void error(TreeNode node, String message) {
    error(String.format("%s:%s: %s", currentFileName, node.getLineNumber(), message));
  }

  /**
   * Report a warning with a specific AST node.
   */
  public static void warning(ASTNode node, String message) {
    int line = getNodeLine(node);
    warning(String.format("%s:%s: %s", currentFileName, line, message));
  }

  public static void warning(TreeNode node, String message) {
    warning(String.format("%s:%s: %s", currentFileName, node.getLineNumber(), message));
  }

  private static int getNodeLine(ASTNode node) {
    CompilationUnit unit = (CompilationUnit) node.getRoot();
    return unit.getLineNumber(node.getStartPosition());
  }

  public static void functionizedMethod() {
    ++functionizedMethodCount;
  }

  public static int functionizedMethodCount() {
    return functionizedMethodCount;
  }
}
