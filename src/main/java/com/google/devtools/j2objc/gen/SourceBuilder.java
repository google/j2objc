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

package com.google.devtools.j2objc.gen;

import com.google.common.io.LineReader;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.io.StringReader;

/**
 * Builds source text.  This is similar to a StringBuilder, but tracks line
 * numbers and outputs them as CPP line directives when directed.
 *
 * @author Tom Ball
 */
public class SourceBuilder {
  private final StringBuilder buffer = new StringBuilder();
  private final CompilationUnit unit;
  private int indention = 0;
  private int currentLine = -1;

  /**
   * If true, generate CPP line directives.  It's necessary to store this
   * here rather than directly use Options.getLineDirectives(), so that the
   * header generator doesn't generate them.
   */
  private final boolean emitLineDirectives;

  public static final int DEFAULT_INDENTION = 2;
  public static final int BEGINNING_OF_FILE = -1;

  /**
   * Create a new SourceBuilder.
   *
   * @param unit the compilation unit this source is based upon
   * @param emitLineDirectives if true, generate CPP line directives
   */
  public SourceBuilder(CompilationUnit unit, boolean emitLineDirectives) {
    this(unit, emitLineDirectives, BEGINNING_OF_FILE);
  }

  /**
   * Create a new SourceBuilder, specifying the initial line number to begin
   * syncing.  This is normally only used for building complex statements,
   * where the generated source is used in another builder.
   *
   * @param unit the compilation unit this source is based upon
   * @param emitLineDirectives if true, generate CPP line directives
   * @param int startLine the initial line number, or -1 if at start of file
   */
  public SourceBuilder(CompilationUnit unit, boolean emitLineDirectives, int startLine) {
    this.unit = unit;
    this.emitLineDirectives = emitLineDirectives;
    this.currentLine = startLine;
  }

  /**
   * Constructor used when line numbers are never needed, such as tests.
   */
  public SourceBuilder() {
    this(null, false);
  }

  /**
   * Copy constructor.
   */
  public SourceBuilder(SourceBuilder original) {
    this(original.unit, original.emitLineDirectives, original.currentLine);
  }

  @Override
  public String toString() {
    return buffer.toString();
  }

  public void print(String s) {
    buffer.append(s);
  }

  public void print(char c) {
    buffer.append(c);
    if (c == '\n') {
      currentLine++;
    }
  }

  public void print(int i) {
    buffer.append(i);
  }

  public void printf(String format, Object... args) {
    print(String.format(format, args));
    currentLine += countNewLines(format);
  }

  public void println(String s) {
    print(s);
    newline();
  }

  public void newline() {
    buffer.append('\n');
    currentLine++;
  }

  public void indent() {
    indention++;
  }

  public void unindent() {
    indention--;
    if (indention < 0) {
      throw new AssertionError("unbalanced indents");
    }
  }

  public void printIndent() {
    buffer.append(pad(indention * DEFAULT_INDENTION));
  }

  // StringBuilder compatibility.
  public SourceBuilder append(char c) {
    print(c);
    return this;
  }

  public SourceBuilder append(int i) {
    print(i);
    return this;
  }

  public SourceBuilder append(String s) {
    print(s);
    return this;
  }

  public char charAt(int i) {
    return buffer.charAt(i);
  }

  public int length() {
    return buffer.length();
  }

  public char[] pad(int n) {
    if (n < 0) {
      n = 0;
    }
    char[] result = new char[n];
    for (int i = 0; i < n; i++) {
      result[i] = ' ';
    }
    return result;
  }

  public void reset() {
    buffer.setLength(0);
  }

  public void syncLineNumbers(ASTNode node) {
    if (emitLineDirectives) {
      int position = node.getStartPosition();
      if (position != -1) {
        int sourceLine = unit.getLineNumber(position);
        if (currentLine != sourceLine) {
          buffer.append(String.format("#line %d\n", sourceLine));
          currentLine = sourceLine;
        }
      }
    }
  }

  public void printStart(String path) {
    if (emitLineDirectives) {
      buffer.append(String.format("#line 1 \"%s\"\n\n", path));
    }
  }

  /**
   * Returns the number of line endings in a string.
   */
  private int countNewLines(String s) {
    int c = 0;
    int i = -1;
    while ((i = s.indexOf('\n', i + 1)) != -1) {
      c++;
    }
    return c;
  }

  /**
   * Fix line indention, based on brace count.
   */
  public String reindent(String code) {
    try {
      // Remove indention from each line.
      StringBuffer sb = new StringBuffer();
      LineReader lr = new LineReader(new StringReader(code));
      String line = lr.readLine();
      while (line != null) {
        sb.append(line.trim());
        line = lr.readLine();
        if (line != null) {
          sb.append('\n');
        }
      }
      String strippedCode = sb.toString();

      // Now indent it again.
      int indent = indention * DEFAULT_INDENTION;
      sb.setLength(0);  // reset buffer
      lr = new LineReader(new StringReader(strippedCode));
      line = lr.readLine();
      while (line != null) {
        if (line.startsWith("}")) {
          indent -= DEFAULT_INDENTION;
        }
        if (!line.startsWith("#line")) {
          sb.append(pad(indent));
        }
        sb.append(line);
        if (line.endsWith("{")) {
          indent += DEFAULT_INDENTION;
        }
        line = lr.readLine();
        if (line != null) {
          sb.append('\n');
        }
      }
      return sb.toString();
    } catch (IOException e) {
      // Should never happen with string readers.
      throw new AssertionError(e);
    }
  }

  public int getCurrentLine() {
    return currentLine;
  }
}
