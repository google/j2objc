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
import com.google.common.collect.Maps;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Adapts JDT's ASTParser to a more convenient interface for parsing source
 * strings into CompilationUnit trees.
 *
 * @author Tom Ball, Keith Stanger
 */
public class JdtParser {

  private Map<String, String> compilerOptions = initCompilerOptions();
  private String[] classpathEntries;
  private String[] sourcepathEntries;
  private String encoding = null;
  private boolean ignoreMissingImports = false;
  private boolean includeRunningVMBootclasspath = true;

  private static Map<String, String> initCompilerOptions() {
    Map<String, String> compilerOptions = Maps.newHashMap();
    // TODO(kstanger): Make the version configurable with -source like javac.
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_SOURCE, "1.7");
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "1.7");
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_COMPLIANCE, "1.7");
    return compilerOptions;
  }

  public void setClasspath(String[] classpath) {
    classpathEntries = classpath;
  }

  public void setSourcepath(String[] sourcepath) {
    sourcepathEntries = sourcepath;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setIgnoreMissingImports(boolean ignoreMissingImports) {
    this.ignoreMissingImports = ignoreMissingImports;
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

  public CompilationUnit parse(String filename, String source) {
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setCompilerOptions(compilerOptions);
    parser.setResolveBindings(true);
    parser.setEnvironment(
        classpathEntries, sourcepathEntries, getEncodings(), includeRunningVMBootclasspath);
    parser.setUnitName(filename);
    parser.setSource(source.toCharArray());
    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
    checkCompilationErrors(filename, unit);
    return unit;
  }

  private String[] getEncodings() {
    if (encoding == null) {
      return null;
    }
    String[] encodings = new String[sourcepathEntries.length];
    Arrays.fill(encodings, encoding);
    return encodings;
  }

  private void checkCompilationErrors(String filename, CompilationUnit unit) {
    List<IProblem> errors = Lists.newArrayList();
    for (IProblem problem : unit.getProblems()) {
      if (problem.isError()) {
        if (((problem.getID() & IProblem.ImportRelated) != 0) && ignoreMissingImports) {
          continue;
        } else {
          ErrorUtil.error(String.format(
              "%s:%s: %s", filename, problem.getSourceLineNumber(), problem.getMessage()));
        }
      }
    }
  }
}
