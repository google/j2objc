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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.ast.TreeNode;

/**
 * This is a base class for all generator types, containing a SourceBuilder and
 * prividing convenient access to it.
 *
 * @author Tom Ball, Keith Stanger
 */
public abstract class AbstractSourceGenerator {

  private final SourceBuilder builder;

  protected AbstractSourceGenerator(SourceBuilder builder) {
    this.builder = builder;
  }

  protected SourceBuilder getBuilder() {
    return builder;
  }

  protected void print(String s) {
    builder.print(s);
  }

  protected void print(char c) {
    builder.print(c);
  }

  protected void printf(String format, Object... args) {
    builder.printf(format, args);
  }

  protected void println(String s) {
    builder.println(s);
  }

  protected void newline() {
    builder.newline();
  }

  protected void indent() {
    builder.indent();
  }

  protected void unindent() {
    builder.unindent();
  }

  protected void printIndent() {
    builder.printIndent();
  }

  protected char[] pad(int n) {
    return builder.pad(n);
  }

  protected void reset() {
    builder.reset();
  }

  protected void syncLineNumbers(TreeNode node) {
    builder.syncLineNumbers(node);
  }

  protected void syncFilename(String filename) {
    builder.syncFilename(filename);
  }

  protected String reindent(String code) {
    return builder.reindent(code);
  }
}
