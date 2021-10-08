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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.flogger.GoogleLogger;
import com.google.common.io.Files;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.pipeline.GenerationBatch;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.ProGuardUsageParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A tool for finding unused code in a Java program.
 */
public class TreeShaker {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final Options options;
  private final com.google.devtools.j2objc.Options j2objcOptions;

  static {
    // Enable assertions in the tree shaker.
    ClassLoader loader = TreeShaker.class.getClassLoader();
    if (loader != null) {
      loader.setPackageAssertionStatus(TreeShaker.class.getPackage().getName(), true);
    }
  }

  @VisibleForTesting
  TreeShaker(Options options) throws IOException {
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
          logger.atSevere().log("Treating warnings as errors.");
          logger.atSevere().log("Failed with %d warnings:", nWarnings);
        } else {
          logger.atWarning().log("TreeShaker ran with %d warnings:", nWarnings);
        }
        for (String warning : ErrorUtil.getWarningMessages()) {
          logger.atWarning().log("  warning: %s", warning);
        }
      }
      if (nErrors > 0) {
        logger.atSevere().log("Failed with %d errors:", nErrors);
        for (String error : ErrorUtil.getErrorMessages()) {
          logger.atSevere().log("  error: %s", error);
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
      Files.asCharSink(strippedFile, j2objcOptions.fileUtil().getCharset())
          .write(parseResult.getSource());
      sourceFileNames.set(i, strippedFile.getPath());
    }
    return strippedDir;
  }

  @VisibleForTesting
  CodeReferenceMap findUnusedCode() throws IOException {
    UsedCodeMarker.Context context = new UsedCodeMarker.Context(
        ProGuardUsageParser.parseDeadCodeFile(options.getTreeShakerRoots()));
    Parser parser = createParser(options);
    List<String> sourceFiles = getSourceFiles();
    if (ErrorUtil.errorCount() > 0) {
      return null;
    }
    File strippedDir = stripIncompatible(sourceFiles, parser);
    Parser.Handler handler = new Parser.Handler() {
      @Override
      public void handleParsedUnit(String path, CompilationUnit unit) {
        new UsedCodeMarker(unit, context).run();
      }
    };

    parser.parseFiles(sourceFiles, handler, options.sourceVersion());
    FileUtil.deleteTempDir(strippedDir);
    if (ErrorUtil.errorCount() > 0) {
      return null;
    }
    TypeGraphBuilder tgb = new TypeGraphBuilder(context.getLibraryInfo());
    logger.atFine().log("External Types: %s", String.join(", ", tgb.getExternalTypeReferences()));
    Collection<String> unknownMethodReferences = tgb.getUnknownMethodReferences();
    if (!unknownMethodReferences.isEmpty()) {
      logger.atWarning().log("Unknown Methods: %s", String.join(", ", unknownMethodReferences));
    }
    if (options.useClassHierarchyAnalyzer()) {
      return ClassHierarchyAnalyzer.analyze(tgb.getTypes());
    } else {
      return RapidTypeAnalyser.analyse(tgb.getTypes());
    }
  }

  private List<String> getSourceFiles() {
    GenerationBatch batch = new GenerationBatch(j2objcOptions);
    batch.processFileArgs(options.getSourceFiles());
    return batch.getInputs().stream()
        .map(input -> input.getFile().getAbsolutePath())
        .collect(Collectors.toList());
  }

  private static void writeToFile(Options options, CodeReferenceMap unused) {
    try (BufferedWriter writer
        = Files.newWriter(options.getOutputFile(), Charset.defaultCharset())) {
      writeUnused(unused, s -> {
        try {
          writer.write(s);
        } catch (IOException e) {
          ErrorUtil.error(e.getMessage());
        }
      });
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  @VisibleForTesting
  static void writeUnused(CodeReferenceMap unused, Consumer<String> writer) {
    for (String clazz : unused.getReferencedClasses()) {
      writer.accept(clazz + "\n");
    }
    unused.getReferencedMethods().cellSet().forEach(cell -> {
      String type = cell.getRowKey();
      String name = cell.getColumnKey();
      writer.accept(type + ":\n");
      cell.getValue().forEach (signature -> {
        StringBuilder argTypes = new StringBuilder();
        StringBuilder returnTypeBuilder = new StringBuilder();
        int offset = getArgTypes(signature, 0, argTypes);
        getType(signature, offset, returnTypeBuilder);
        String returnType = returnTypeBuilder.toString();
        writer.accept("    ");
        if (!returnType.equals("void")) {
          writer.accept(returnType);
          writer.accept(" ");
        }
        writer.accept(name);
        writer.accept(argTypes.toString());
        writer.accept("\n");
      });
    });
  }

  private static int getArgTypes(String type, int offset, StringBuilder result) {
    result.append('(');
    // consume '('
    offset++;
    boolean first = true;
    while (type.charAt(offset) != ')') {
      if (first) {
        first = false;
      } else {
        result.append(',');
      }
      offset = getType(type, offset, result);
    }
    // consume ')'
    offset++;
    result.append(')');
    return offset;
  }

  @VisibleForTesting
  static int getType(String type, int offset, StringBuilder result) {
    switch (type.charAt(offset)) {
      case 'V':
        result.append("void");
        return offset + 1;
      case 'Z':
        result.append("boolean");
        return offset + 1;
      case 'C':
        result.append("char");
        return offset + 1;
      case 'B':
        result.append("byte");
        return offset + 1;
      case 'S':
        result.append("short");
        return offset + 1;
      case 'I':
        result.append("int");
        return offset + 1;
      case 'F':
        result.append("float");
        return offset + 1;
      case 'J':
        result.append("long");
        return offset + 1;
      case 'D':
        result.append("double");
        return offset + 1;
      case '[':
        offset = getType(type, offset + 1, result);
        result.append("[]");
        return offset;
      case 'L':
        int end = type.indexOf(';', offset + 1);
        result.append(type.substring(offset + 1, end).replace('/', '.'));
        return end + 1;
        // case '(':
      default:
        StringBuilder argTypes = new StringBuilder();
        offset = getArgTypes(type, offset, argTypes);
        offset = getType(type, offset, result);
        result.append(argTypes);
        return offset;
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
      TreeShaker shaker = new TreeShaker(options);
      shaker.testFileExistence();
      exitOnErrorsOrWarnings(treatWarningsAsErrors);
      writeToFile(options, shaker.findUnusedCode());
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
    exitOnErrorsOrWarnings(treatWarningsAsErrors);
  }
}
