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

import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import java.util.Collection;

/**
 * Interface for interacting with the Java compiler front-end.
 */
public interface Parser {

  /**
   * Handler to be provided when parsing multiple files. The provided
   * implementation is called with the parsed units.
   */
  public interface Handler {
    void handleParsedUnit(String path, CompilationUnit unit);
  }

  /**
   * Add paths to the classpath for the next compilation.
   */
  void addClasspathEntries(Iterable<String> entries);

  /**
   * Split a Java path string and add its entries to the classpath for the
   * next compilation.
   */
  void addClasspathEntries(String entries);

  /**
   * Add paths to the sourcepath for the next compilation.
   */
  void addSourcepathEntries(Iterable<String> entries);

  /**
   * Split a Java path string and add its entries to the classpath for the
   * next compilation.
   */
  void addSourcepathEntries(String entries);

  /**
   * Add a path to the beginning of the source path.
   */
  void prependSourcepathEntry(String entry);

  /**
   * Add a path to the end of the source path.
   */
  void addSourcepathEntry(String entry);

  /**
   * Sets the source file encoding, equivalent to javac's -encoding flag.
   */
  void setEncoding(String encoding);

  void setIncludeRunningVMBootclasspath(boolean includeVMBootclasspath);

  /**
   * Set whether to include doc comment AST nodes.
   */
  void setEnableDocComments(boolean enable);

  /**
   * Parse Java source into an AST with bindings.
   */
  CompilationUnit parse(InputFile file);

  /**
   * Parse Java source into an AST with bindings.
   */
  CompilationUnit parse(String mainType, String path, String source);

  /**
   * Parse one or more source files, calling a handler with a compilation unit.
   */
  void parseFiles(Collection<String> paths, Parser.Handler handler, SourceVersion sourceVersion);

  // Convert to return j2objc AST trees instead of JDT.
  org.eclipse.jdt.core.dom.CompilationUnit parseWithoutBindings(String unitName, String source);
}