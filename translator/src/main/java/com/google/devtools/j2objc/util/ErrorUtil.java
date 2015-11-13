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
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides convenient static error and warning methods.
 *
 * @author Tom Ball, Keith Stanger
 */
public class ErrorUtil {

  private static int errorCount = 0;
  private static int warningCount = 0;
  private static int functionizedMethodCount = 0;
  private static PrintStream errorStream = System.err;
  private static List<String> errorMessages = Lists.newArrayList();
  private static List<String> warningMessages = Lists.newArrayList();
  // Captures whether the translator should emit clang style message. Clang style messages
  // are particularly useful when the translator is being invoked by Xcode build rules.
  // Xcode will be able to pick the file path and line number, hence make it easy to address
  // compilation errors from within Xcode.
  // Ideally this should be set by a command line switch, but for now we tell that by checking
  // the DEVELOPER_DIR environment variable set by Xcode.
  private static final boolean CLANG_STYLE_ERROR_MSG = (null != System.getenv("DEVELOPER_DIR"));
  private static Pattern pathAndLinePattern = null;

  public static void reset() {
    errorCount = 0;
    warningCount = 0;
    errorMessages = Lists.newArrayList();
    warningMessages = Lists.newArrayList();
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

  public static List<String> getWarningMessages() {
    return warningMessages;
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

  public static String getFullMessage(String tag, String message, boolean clangStyle) {
    String fullMessage = null;
    if (clangStyle) {
      // Try to find the file path and line number, and then insert the tag after that,
      // in order to get a message in the following format.
      // <file_path>:<line_number>: error: <detailed_message>
      if (pathAndLinePattern == null) {
        pathAndLinePattern = Pattern.compile(".+?\\.java:\\d+: ");
      }
      Matcher matcher = pathAndLinePattern.matcher(message);
      if (matcher.find()) {
        fullMessage = matcher.group(0) + matcher.replaceFirst(tag);
      }
      // Fall back to default message style if pattern was not matched.
    }
    if (fullMessage == null) {
      fullMessage = tag + message;
    }
    return fullMessage;
  }

  // TODO(tball): Consider more ways to associate errors with GenerationUnits to aid debugging.
  public static void error(String message) {
    errorMessages.add(message);
    errorStream.println(getFullMessage("error: ", message, CLANG_STYLE_ERROR_MSG));
    errorCount++;
  }

  public static void warning(String message) {
    warningMessages.add(message);
    errorStream.println(getFullMessage("warning: ", message, CLANG_STYLE_ERROR_MSG));
    warningCount++;
  }

  /**
   * Report an error with a specific AST node.
   */
  public static void error(TreeNode node, String message) {
    error(formatMessage(node, message));
  }

  /**
   * Report a warning with a specific AST node.
   */
  public static void warning(TreeNode node, String message) {
    warning(formatMessage(node, message));
  }

  /**
   * Report that an internal error happened when translating a specific source.
   */
  public static void fatalError(Throwable error, String path) {
    StringWriter msg = new StringWriter();
    PrintWriter writer = new PrintWriter(msg);
    writer.println(String.format("internal error translating \"%s\"", path));
    error.printStackTrace(writer);
    writer.flush();
    error(msg.toString());
  }

  private static String formatMessage(TreeNode node, String message) {
    CompilationUnit unit = TreeUtil.getCompilationUnit(node);
    return String.format("%s:%s: %s", unit.getSourceFilePath(), node.getLineNumber(), message);
  }

  public static void functionizedMethod() {
    ++functionizedMethodCount;
  }

  public static int functionizedMethodCount() {
    return functionizedMethodCount;
  }
}
