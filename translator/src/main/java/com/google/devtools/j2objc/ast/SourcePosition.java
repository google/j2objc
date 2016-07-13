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
 * Source position information for an AST node.
 */
public class SourcePosition {
  private int startPosition;
  private int length;
  private int lineNumber;

  /**
   * A pseudo-position used for nodes that are not derived from source code.
   */
  public static final SourcePosition NO_POSITION = new SourcePosition(-1, 0, -1);

  public SourcePosition(int startPosition, int length) {
    this(startPosition, length, -1);
  }

  public SourcePosition(int startPosition, int length, int lineNumber) {
    this.startPosition = startPosition;
    this.length = length;
    this.lineNumber = lineNumber;
  }

  public final int getStartPosition() {
    return startPosition;
  }

  public final int getLength() {
    return length;
  }

  public void setSourceRange(int newStartPosition, int newLength) {
    startPosition = newStartPosition;
    length = newLength;
  }

  public final int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }
}
