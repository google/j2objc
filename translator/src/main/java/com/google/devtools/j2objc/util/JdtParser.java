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
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.Options.LintOption;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Adapts JDT's ASTParser to a more convenient interface for parsing source
 * strings into CompilationUnit trees.
 *
 * @author Tom Ball, Keith Stanger
 */
public class JdtParser {

  private static final Logger logger = Logger.getLogger(JdtParser.class.getName());

  private Map<String, String> compilerOptions = initCompilerOptions(Options.getSourceVersion());
  private List<String> classpathEntries = Lists.newArrayList();
  private List<String> sourcepathEntries = Lists.newArrayList();
  private String encoding = null;
  private boolean includeRunningVMBootclasspath = true;

  private static Map<String, String> initCompilerOptions(SourceVersion sourceVersion) {
    Map<String, String> compilerOptions = Maps.newHashMap();
    String version = sourceVersion.flag();
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_SOURCE, version);
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, version);
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_COMPLIANCE, version);

    // Turn on any specified lint warnings.
    EnumSet<LintOption> lintOptions = Options.lintOptions();
    for (Options.LintOption lintOption : lintOptions) {
      compilerOptions.put(lintOption.jdtFlag(), "warning");
    }

    // Turn off any warnings on by default but not requested.
    for (Options.LintOption lintOption : EnumSet.complementOf(lintOptions)) {
      compilerOptions.put(lintOption.jdtFlag(), "ignore");
    }
    return compilerOptions;
  }

  public void addClasspathEntry(String entry) {
    if (isValidPathEntry(entry)) {
      classpathEntries.add(entry);
    }
  }

  public void addClasspathEntries(Iterable<String> entries) {
    for (String entry : entries) {
      addClasspathEntry(entry);
    }
  }

  private static final Splitter PATH_SPLITTER = Splitter.on(":").omitEmptyStrings();

  public void addClasspathEntries(String entries) {
    addClasspathEntries(PATH_SPLITTER.split(entries));
  }

  public void addSourcepathEntry(String entry) {
    if (isValidPathEntry(entry)) {
      sourcepathEntries.add(entry);
    }
  }

  public void prependSourcepathEntry(String entry) {
    if (isValidPathEntry(entry)) {
      sourcepathEntries.add(0, entry);
    }
  }

  public void addSourcepathEntries(Iterable<String> entries) {
    for (String entry : entries) {
      addSourcepathEntry(entry);
    }
  }

  public void addSourcepathEntries(String entries) {
    addSourcepathEntries(PATH_SPLITTER.split(entries));
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setIncludeRunningVMBootclasspath(boolean includeVMBootclasspath) {
    includeRunningVMBootclasspath = includeVMBootclasspath;
  }

  public void setEnableDocComments(boolean enable) {
    // BodyDeclaration.getJavadoc() always returns null without this option enabled,
    // so by default no doc comments are generated.
    compilerOptions.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_DOC_COMMENT_SUPPORT,
        enable ? "enabled" : "disabled");
  }

  public CompilationUnit parseWithoutBindings(String unitName, String source) {
    return parse(unitName, source, false);
  }

  public CompilationUnit parseWithBindings(String unitName, String source) {
    return parse(unitName, source, true);
  }

  private CompilationUnit parse(String unitName, String source, boolean resolveBindings) {
    ASTParser parser = newASTParser(resolveBindings, Options.getSourceVersion());
    parser.setUnitName(unitName);
    parser.setSource(source.toCharArray());
    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
    if (checkCompilationErrors(unitName, unit)) {
      return unit;
    } else {
      return null;
    }
  }

  /**
   * Handler to be provided when parsing multiple files. The provided
   * implementation is called with the parsed units.
   */
  public interface Handler {
    public void handleParsedUnit(String path, CompilationUnit unit);
  }

  public void parseFiles(Collection<String> paths, final Handler handler,
      SourceVersion sourceVersion) {
    ASTParser parser = newASTParser(true, sourceVersion);
    FileASTRequestor astRequestor = new FileASTRequestor() {
      @Override
      public void acceptAST(String sourceFilePath, CompilationUnit ast) {
        logger.fine("acceptAST: " + sourceFilePath);
        if (checkCompilationErrors(sourceFilePath, ast)) {
          handler.handleParsedUnit(sourceFilePath, ast);
        }
      }
    };
    // JDT fails to resolve all secondary bindings unless there are the same
    // number of "binding key" strings as source files. It doesn't appear to
    // matter what the binding key strings should be (as long as they're non-
    // null), so the paths array is reused.
    String[] pathsArray = paths.toArray(new String[paths.size()]);
    parser.createASTs(pathsArray, getEncodings(pathsArray.length), pathsArray, astRequestor, null);
  }

  @SuppressWarnings("deprecation")
  private ASTParser newASTParser(boolean resolveBindings, SourceVersion sourceVersion) {
    ASTParser parser;
    if (SourceVersion.java8Minimum(sourceVersion)) {
      parser = ASTParser.newParser(AST.JLS8);
    } else {
      parser = ASTParser.newParser(AST.JLS4); // Java 7
    }

    parser.setCompilerOptions(compilerOptions);
    parser.setResolveBindings(resolveBindings);
    parser.setEnvironment(
        toArray(classpathEntries), toArray(sourcepathEntries),
        getEncodings(sourcepathEntries.size()), includeRunningVMBootclasspath);
    return parser;
  }

  private String[] toArray(List<String> list) {
    return list.toArray(new String[list.size()]);
  }

  private boolean isValidPathEntry(String path) {
    // JDT requires that all path elements exist and can hold class files.
    File f = new File(path);
    return f.exists() && (f.isDirectory() || path.endsWith(".jar"));
  }

  private String[] getEncodings(int length) {
    if (encoding == null) {
      return null;
    }
    String[] encodings = new String[length];
    Arrays.fill(encodings, encoding);
    return encodings;
  }

  private boolean checkCompilationErrors(String filename, CompilationUnit unit) {
    boolean hasErrors = false;
    for (IProblem problem : unit.getProblems()) {
      if (problem.isError()) {
        ErrorUtil.error(String.format(
            "%s:%s: %s", filename, problem.getSourceLineNumber(), problem.getMessage()));
        hasErrors = true;
      }
      if (problem.isWarning()) {
        ErrorUtil.warning(String.format(
            "%s:%s: warning: %s", filename, problem.getSourceLineNumber(), problem.getMessage()));
      }
    }
    return !hasErrors;
  }
}
