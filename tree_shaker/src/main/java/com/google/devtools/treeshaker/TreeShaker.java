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

package com.google.devtools.treeshaker;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table.Cell;
import com.google.common.io.Files;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.ProGuardUsageParser;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.devtools.treeshaker.ElementReferenceMapper.ReferenceNode;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A tool for finding unused code in a Java program.
 *
 * @author Priyank Malvania
 */
public class TreeShaker {

  private final Options options;
  private final com.google.devtools.j2objc.Options j2objcOptions;
  private TranslationEnvironment env = null;

  static {
    // Enable assertions in the tree shaker.
    ClassLoader loader = TreeShaker.class.getClassLoader();
    if (loader != null) {
      loader.setPackageAssertionStatus(TreeShaker.class.getPackage().getName(), true);
    }
  }

  public TreeShaker(Options options) throws IOException {
    this.options = options;
    j2objcOptions = new com.google.devtools.j2objc.Options();
    j2objcOptions.load(new String[] {
      "-sourcepath", Strings.nullToEmpty(options.getSourcepath()),
      "-classpath", Strings.nullToEmpty(options.getClasspath()),
      "-encoding", options.fileEncoding(),
      "-source",   options.sourceVersion().flag()
    });
  }

  private Parser createParser(Options options) throws IOException {
    Parser parser = Parser.newParser(j2objcOptions);
    parser.addSourcepathEntries(j2objcOptions.fileUtil().getSourcePathEntries());
    parser.addClasspathEntries(Strings.nullToEmpty(options.getBootclasspath()));
    parser.addClasspathEntries(j2objcOptions.fileUtil().getClassPathEntries());
    return parser;
  }

  private static void exitOnErrorsOrWarnings(boolean treatWarningsAsErrors) {
    int nErrors = ErrorUtil.errorCount();
    int nWarnings = ErrorUtil.warningCount();
    if (nWarnings > 0 || nErrors > 0) {
      if (nWarnings > 0) {
        if (treatWarningsAsErrors) {
          System.err.println("Treating warnings as errors.");
          System.err.println("Failed with " + nWarnings + " warnings:");
        } else {
          System.err.println("TreeShaker ran with " + nWarnings + " warnings:");
        }
        for (String warning : ErrorUtil.getWarningMessages()) {
          System.err.println("  warning: " + warning);
        }
      }
      if (nErrors > 0) {
        System.err.println("Failed with " + nErrors + " errors:");
        for (String error : ErrorUtil.getErrorMessages()) {
          System.err.println("  error: " + error);
        }
      }
      if (treatWarningsAsErrors) {
        nErrors += nWarnings;
      }
      System.exit(nErrors);
    }
  }

  private void testFileExistence() {
    for (String filePath : options.getSourceFiles()) {
      File f = new File(filePath);
      if (!f.exists()) {
        ErrorUtil.error("File not found: " + filePath);
      }
    }
  }

  private File stripIncompatible(List<String> sourceFileNames, Parser parser) throws IOException {
    File strippedDir = null;
    for (int i = 0; i < sourceFileNames.size(); i++) {
      String fileName = sourceFileNames.get(i);
      RegularInputFile file = new RegularInputFile(fileName);
      String source = j2objcOptions.fileUtil().readFile(file);
      if (!source.contains("J2ObjCIncompatible")) {
        continue;
      }
      if (strippedDir == null) {
        strippedDir = Files.createTempDir();
        parser.prependSourcepathEntry(strippedDir.getPath());
      }
      Parser.ParseResult parseResult = parser.parseWithoutBindings(file, source);
      String qualifiedName = parseResult.mainTypeName();
      parseResult.stripIncompatibleSource();
      String relativePath = qualifiedName.replace('.', File.separatorChar) + ".java";
      File strippedFile = new File(strippedDir, relativePath);
      Files.createParentDirs(strippedFile);
      Files.write(
          parseResult.getSource(), strippedFile, j2objcOptions.fileUtil().getCharset());
      sourceFileNames.set(i, strippedFile.getPath());
    }
    return strippedDir;
  }

  public CodeReferenceMap getUnusedCode(CodeReferenceMap inputRootSet) throws IOException {
    Parser parser = createParser(options);

    final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();

    List<String> sourceFiles = options.getSourceFiles();
    File strippedDir = stripIncompatible(sourceFiles, parser);

    Parser.Handler handler = new Parser.Handler() {
      @Override
      public void handleParsedUnit(String path, CompilationUnit unit) {
        if (env == null) {
          env = unit.getEnv();
        } else {
          //TODO(user): Assertion fails! Remove this once we're sure all env utils are the same.
          //assert(unit.getEnv() == env);
        }
        new ElementReferenceMapper(unit, elementReferenceMap, staticSet, overrideMap).run();
      }
    };
    parser.parseFiles(sourceFiles, handler, options.sourceVersion());

    FileUtil.deleteTempDir(strippedDir);
    if (ErrorUtil.errorCount() > 0) {
      return null;
    }

    UnusedCodeTracker tracker = new UnusedCodeTracker(env, elementReferenceMap, staticSet,
        overrideMap);
    tracker.mapOverridingMethods();
    tracker.markUsedElements(inputRootSet);
    CodeReferenceMap codeMap = tracker.buildTreeShakerMap();
    return codeMap;
  }

  private static CodeReferenceMap loadRootSetMap(Options options) {
    return ProGuardUsageParser.parseDeadCodeFile(options.getPublicRootSetFile());
  }

  public static void writeCodeReferenceMapInfo(BufferedWriter writer, CodeReferenceMap map)
      throws IOException {
    writer.write("Dead Classes:\n");
    for (String clazz : map.getReferencedClasses()) {
      writer.write(clazz + "\n");
    }
    //TODO(user): Add output formatting that can be easily read by the parser in translator.
    writer.write("Dead Methods:\n");
    for (Cell<String, String, ImmutableSet<String>> cell : map.getReferencedMethods().cellSet()) {
      writer.write(cell.toString() + "\n");
    }
  }

  public static void writeToFile(String fileName, CodeReferenceMap map) throws IOException {
    File file = new File(fileName);
    try {
      BufferedWriter writer = Files.newWriter(file, Charset.defaultCharset());
      writeCodeReferenceMapInfo(writer, map);
      writer.close();
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      Options.help(true);
    }
    boolean treatWarningsAsErrors = false;
    try {
      Options options = Options.parse(args);
      treatWarningsAsErrors = options.treatWarningsAsErrors();
      TreeShaker finder = new TreeShaker(options);
      finder.testFileExistence();
      exitOnErrorsOrWarnings(treatWarningsAsErrors);
      CodeReferenceMap unusedCodeMap = finder.getUnusedCode(loadRootSetMap(options));
      writeToFile("tree-shaker-report.txt", unusedCodeMap);
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }

    exitOnErrorsOrWarnings(treatWarningsAsErrors);
  }
}
