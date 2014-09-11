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

import com.google.common.io.Files;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.util.ErrorUtil;

import java.io.File;
import java.io.IOException;

/**
 * This class handles common actions shared by the header, implementation, and
 * statement generators.
 *
 * @author Tom Ball
 */
public abstract class SourceFileGenerator {
  private final SourceBuilder builder;
  private final CompilationUnit unit;
  private final File outputDirectory;

  public SourceFileGenerator(CompilationUnit unit, boolean emitLineDirectives) {
    builder = new SourceBuilder(emitLineDirectives);
    this.unit = unit;
    this.outputDirectory = Options.getOutputDirectory();
  }

  /**
   * Returns the name this source file should be saved as.  For example,
   * foo/bar/Mumble.java translates to $(OUTPUT_DIR)/foo/bar/Mumble.m for
   * Objective-C implementation files.  If --no-package-directories is
   * specified, though, the output file is $(OUTPUT_DIR)/Mumble.m.
   * <p>
   * Note: class names are still camel-cased to avoid name collisions.
   */
  protected String getOutputFileName(CompilationUnit node) {
    PackageDeclaration pkg = node.getPackage();
    if (!pkg.isDefaultPackage() && Options.usePackageDirectories()) {
      return pkg.getName().getFullyQualifiedName().replace('.', File.separatorChar)
          + File.separatorChar + node.getMainTypeName() + getSuffix();
    } else {
      return node.getMainTypeName() + getSuffix();
    }
  }

  /**
   * Returns the suffix for files created by this generator.
   */
  protected abstract String getSuffix();

  protected void save(String path) {
    try {
      File outputFile = new File(outputDirectory, path);
      File dir = outputFile.getParentFile();
      if (dir != null && !dir.exists()) {
        if (!dir.mkdirs()) {
          ErrorUtil.warning("cannot create output directory: " + outputDirectory);
        }
      }
      String source = builder.toString();

      // Make sure file ends with a new-line.
      if (!source.endsWith("\n")) {
        source += '\n';
      }

      Files.write(source, outputFile, Options.getCharset());
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    } finally {
      reset();
    }
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

  protected void printStart(String path) {
    builder.printStart(path);
  }

  protected String reindent(String code) {
    return builder.reindent(code);
  }

  protected CompilationUnit getUnit() {
    return unit;
  }

  protected SourceBuilder getBuilder() {
    return builder;
  }
}
