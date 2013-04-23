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

package com.google.devtools.cyclefinder;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

/**
 * A tool for finding possible reference cycles in a Java program.
 *
 * @author Keith Stanger
 */
public class CycleFinder {

  private final Options options;
  private final PrintStream outStream;
  private final PrintStream errStream;
  private int nErrors = 0;
  private List<String> errors = Lists.newArrayList();

  static {
    // Enable assertions in the cycle finder.
    ClassLoader loader = CycleFinder.class.getClassLoader();
    if (loader != null) {
      loader.setPackageAssertionStatus(CycleFinder.class.getPackage().getName(), true);
    }
  }

  public CycleFinder(Options options, PrintStream outStream, PrintStream errStream) {
    this.options = options;
    this.outStream = outStream;
    this.errStream = errStream;
  }

  private FileASTRequestor newASTRequestor(final TypeCollector typeCollector) {
    return new FileASTRequestor() {
      @Override
      public void acceptAST(String sourceFilePath, CompilationUnit ast) {
        outStream.println("acceptAST: " + sourceFilePath);
        for (IProblem problem : ast.getProblems()) {
          if (problem.isError()) {
            error(String.format("%s:%s: %s",
                sourceFilePath, problem.getSourceLineNumber(), problem.getMessage()));
          }
        }
        typeCollector.visitAST(ast);
      }
    };
  }

  private static String[] splitEntries(String entries) {
    if (entries == null) {
      return new String[0];
    }
    List<String> entriesList = Lists.newArrayList();
    for (String entry : Splitter.on(':').split(entries)) {
      if (new File(entry).exists()) {  // JDT fails with bad path entries.
        entriesList.add(entry);
      }
    }
    return entriesList.toArray(new String[0]);
  }

  private static ASTParser newParser(Options options) {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setCompilerOptions(ImmutableMap.of(
        org.eclipse.jdt.core.JavaCore.COMPILER_SOURCE, "1.6",
        org.eclipse.jdt.core.JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "1.6",
        org.eclipse.jdt.core.JavaCore.COMPILER_COMPLIANCE, "1.6"));
    parser.setResolveBindings(true);
    parser.setEnvironment(
        splitEntries(options.getBootclasspath() + ":" + options.getClasspath()),
        splitEntries(options.getSourcepath()), null, false);
    return parser;
  }

  private void error(String message) {
    errors.add(message);
    errStream.println("error: " + message);
    nErrors++;
  }

  private void printErrors() {
    for (String error : errors) {
      errStream.println("error: " + error);
    }
  }

  public int errorCount() {
    return nErrors;
  }

  public List<List<Edge>> findCycles() throws IOException {
    TypeCollector typeCollector = new TypeCollector();

    // Parse all the source and populate type data.
    ASTParser parser = newParser(options);
    FileASTRequestor astRequestor = newASTRequestor(typeCollector);
    String[] sourceFiles = options.getSourceFiles().toArray(new String[0]);
    parser.createASTs(sourceFiles, null, new String[0], astRequestor, null);

    if (nErrors > 0) {
      return null;
    }

    // Construct the graph and find cycles.
    ReferenceGraph graph = new ReferenceGraph(typeCollector,
        Whitelist.createFromFiles(options.getWhitelistFiles()));
    return graph.findCycles();
  }

  public static void printCycles(Collection<? extends Iterable<Edge>> cycles, PrintStream out) {
    for (Iterable<Edge> cycle : cycles) {
      out.println();
      out.println("***** Found reference cycle *****");
      for (Edge e : cycle) {
        out.println(e.toString());
      }
      out.println("----- Full Types -----");
      for (Edge e : cycle) {
        out.println(e.getOrigin().getKey());
      }
    }
    out.println();
    out.println(cycles.size() + " CYCLES FOUND.");
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      Options.help(true);
    }
    Options options = Options.parse(args);
    CycleFinder finder = new CycleFinder(options, System.out, System.err);
    List<List<Edge>> cycles = finder.findCycles();
    if (finder.errorCount() > 0) {
      System.out.println("Failed with " + finder.errorCount() + " errors:");
      finder.printErrors();
    } else {
      printCycles(cycles, System.out);
    }
    System.exit(finder.errorCount() + cycles.size());
  }
}
