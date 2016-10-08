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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import java.util.Collection;
import java.util.List;

/**
 * Interface for interacting with the Java compiler front-end.
 */
public abstract class Parser {

  protected final List<String> classpathEntries = Lists.newArrayList();
  protected final List<String> sourcepathEntries = Lists.newArrayList();
  protected String encoding = null;
  protected final Options options;
  protected boolean includeRunningVMBootclasspath = true;

  protected static final Splitter PATH_SPLITTER = Splitter.on(":").omitEmptyStrings();

  public Parser(Options options){
    this.options = options;
  }

  /**
   * Returns a new Parser instance.
   */
  public static Parser newParser() {
    return newParser(new Options());
  }

  /**
   * Return a new Parser instance with the provided Options.
   */
  public static Parser newParser(Options options) {
    // TODO(tball): remove after front-end conversion is complete.
    if (options.isJDT()) {
      return new com.google.devtools.j2objc.jdt.JdtParser(options);
    }
    return new com.google.devtools.j2objc.javac.JavacParser(options);
  }

  /**
   * Handler to be provided when parsing multiple files. The provided
   * implementation is called with the parsed units.
   */
  public interface Handler {
    void handleParsedUnit(String path, CompilationUnit unit);
  }

  /**
   * Adds a single path to the classpath for the next compilation.
   */
  public void addClasspathEntry(String entry) {
    classpathEntries.add(entry);
  }

  /**
   * Add paths to the classpath for the next compilation.
   */
  public void addClasspathEntries(Iterable<String> entries) {
    for (String entry : entries) {
      addClasspathEntry(entry);
    }
  }

  /**
   * Split a Java path string and add its entries to the classpath for the
   * next compilation.
   */
  public void addClasspathEntries(String entries) {
    addClasspathEntries(PATH_SPLITTER.split(entries));
  }

  /**
   * Add a path to the end of the source path.
   */
  public void addSourcepathEntry(String entry) {
    sourcepathEntries.add(entry);
  }

  /**
   * Add paths to the sourcepath for the next compilation.
   */
  public void addSourcepathEntries(Iterable<String> entries) {
    for (String entry : entries) {
      addSourcepathEntry(entry);
    }
  }

  /**
   * Split a Java path string and add its entries to the classpath for the
   * next compilation.
   */
  public void addSourcepathEntries(String entries) {
    addSourcepathEntries(PATH_SPLITTER.split(entries));
  }

  /**
   * Add a path to the beginning of the source path.
   */
  public void prependSourcepathEntry(String entry) {
    sourcepathEntries.add(0, entry);
  }

  /**
   * Sets the source file encoding, equivalent to javac's -encoding flag.
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setIncludeRunningVMBootclasspath(boolean includeVMBootclasspath) {
    includeRunningVMBootclasspath = includeVMBootclasspath;
  }

  /**
   * Set whether to include doc comment AST nodes.
   */
  public abstract void setEnableDocComments(boolean enable);

  /**
   * Parse Java source into an AST with bindings.
   */
  public abstract CompilationUnit parse(InputFile file);

  /**
   * Parse Java source into an AST with bindings.
   */
  public abstract CompilationUnit parse(String mainType, String path, String source);

  /**
   * Parse one or more source files, calling a handler with a compilation unit.
   */
  public abstract void parseFiles(
      Collection<String> paths, Parser.Handler handler, SourceVersion sourceVersion);

  /**
   * Parses source without performing any type or element attribution.
   * A front-end specific compilation unit is returned via a ParseResult
   * to avoid the cost of tree conversion, since conversion requires types
   * and element attribution.
   *
   * @return a parse result, or null if there were parse errors.
   */
  public abstract ParseResult parseWithoutBindings(InputFile file, String source);

  /**
   * The ParseResult provides parser actions that can be performed without
   * client code needing to know the front-end compiler's private API.
   */
  public interface ParseResult {

    /**
     * Strip Java source of any elements marked with a J2ObjCIncompatible annotation.
     * If the source code has no J2ObjCIncompatible annotations, it is unchanged.
     */
    void stripIncompatibleSource();

    /**
     * The original source code, or stripped of any J2ObjCIncompatible code if
     * stripIncompatibleSource() was first called.
     */
    String getSource();

    /**
     * Returns the fully qualified name of the main type in this source.
     */
    String mainTypeName();

    /**
     * Returns a source-like representation of the internal compilation unit.
     * This result is similar to the actual source returned by getSource(),
     * but is normally only used when debugging.
     */
    @Override
    String toString();
  }
}
