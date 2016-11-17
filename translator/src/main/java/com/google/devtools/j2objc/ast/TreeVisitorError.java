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

package com.google.devtools.j2objc.ast;

/**
 * Wraps an assertion error with information about the node where the error occurred.
 */
public class TreeVisitorError extends AssertionError {

  TreeVisitorError(Throwable original, TreeNode node) {
    super(constructMessage(original, node), original.getCause());
    setStackTrace(original.getStackTrace());
    for (Throwable t : original.getSuppressed()) {
      addSuppressed(t);
    }
  }

  private static String constructMessage(Throwable original, TreeNode node) {
    CompilationUnit unit = TreeUtil.getCompilationUnit(node);
    String msg = original.getMessage();
    if (msg == null || msg.isEmpty()) {
      msg = original.getClass().getSimpleName();
    }
    if (unit == null) {
      return msg;
    }
    return String.format(
        "%s:%s: %s", unit.getSourceFilePath(), node.getLineNumber(), msg);
  }
}
