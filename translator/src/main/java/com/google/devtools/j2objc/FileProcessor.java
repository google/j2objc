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

package com.google.devtools.j2objc;

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.TreeConverter;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for processing GenerationUnits in minimum increments of one GenerationUnit.
 *
 * @author Tom Ball, Keith Stanger, Mike Thvedt
 */
abstract class FileProcessor {

  private static final Logger logger = Logger.getLogger(FileProcessor.class.getName());

  private final JdtParser parser;

  private final List<GenerationUnit> batchUnits = new ArrayList<GenerationUnit>();
  private int batchSourceCount = 0;

  private final boolean doBatching = Options.batchTranslateMaximum() > 0;

  public FileProcessor(JdtParser parser) {
    this.parser = Preconditions.checkNotNull(parser);
  }

  public void processBatch(GenerationBatch batch) {
    for (GenerationUnit unit : batch.getGenerationUnits()) {
      processGenerationUnit(unit);
    }

    processBatch();
  }

  protected void processGenerationUnit(GenerationUnit unit) {
    if (unit.hasErrors()) {
      return;
    }

    int fileCount = unit.getInputFiles().size();
    batchUnits.add(unit);
    batchSourceCount += fileCount;
    if (batchSourceCount > Options.batchTranslateMaximum()) {
      processBatch();
    }
  }

  protected boolean isBatchable(InputFile file) {
    return doBatching && file.getContainingPath().endsWith(".java");
  }

  protected void processBatch() {
    try {
      if (batchUnits.isEmpty()) {
        return;
      }

      // We need to track all this for the parser callback.
      final Map<InputFile, GenerationUnit> unitMap = new HashMap<InputFile, GenerationUnit>();
      List<InputFile> batchFiles = new ArrayList<InputFile>();
      for (GenerationUnit unit : batchUnits) {
        if (unit.hasErrors()) {
          continue;
        }

        for (InputFile file : unit.getInputFiles()) {
          if (isBatchable(file)) {
            batchFiles.add(file);
            unitMap.put(file, unit);
          } else {
            processSource(file, unit);
          }
        }
      }

      logger.finest("Processing batch of size " + batchFiles.size());
      JdtParser.Handler handler = new JdtParser.Handler() {
        @Override
        public void handleParsedUnit(InputFile inputFile, CompilationUnit unit) {
          ErrorUtil.setCurrentFileName(inputFile.getPath());
          processCompilationUnit(unitMap.get(inputFile), unit, inputFile);
        }
      };

      if (batchFiles.size() > 0) {
        parser.parseFiles(batchFiles, handler);
      }
    } finally {
      for (GenerationUnit unit : batchUnits) {
        // Always clear, and don't leak memory on an exception.
        unit.clear();
      }
      batchUnits.clear();
      batchSourceCount = 0;
    }
  }

  protected void processSource(InputFile file, GenerationUnit generationUnit) {
    logger.finest("parsing " + file);

    ErrorUtil.setCurrentFileName(file.getPath());

    String source;
    try {
      source = FileUtil.readFile(file);
    } catch (IOException e) {
      generationUnit.error(e.getMessage());
      return;
    }

    int errorCount = ErrorUtil.errorCount();
    CompilationUnit compilationUnit = parser.parse(file.getUnitName(), source);
    if (ErrorUtil.errorCount() > errorCount) {
      return;
    }

    processCompilationUnit(generationUnit, compilationUnit, file);
  }

  private boolean isFullyParsed(GenerationUnit unit) {
    return unit.getCompilationUnits().size() == unit.getInputFiles().size();
  }

  /**
   * Callback invoked when a file is parsed.
   */
  protected void processCompilationUnit(
      GenerationUnit genUnit, CompilationUnit unit, InputFile file) {
    try {
      Types.initialize(unit);
      NameTable.initialize();

      String source = FileUtil.readFile(file);
      com.google.devtools.j2objc.ast.CompilationUnit translatedUnit
          = TreeConverter.convertCompilationUnit(unit, file, source);
      genUnit.addCompilationUnit(translatedUnit);

      if (isFullyParsed(genUnit)) {
        ensureOutputPath(genUnit);
        if (genUnit.getName() == null) {
          // We infer names from the AST. If size > 1 we shouldn't reach here.
          assert genUnit.getCompilationUnits().size() == 1;
          genUnit.setName(
              NameTable.camelCaseQualifiedName(
                  NameTable.getMainTypeFullName(translatedUnit)));
        }

        logger.finest("Processing compiled unit " + genUnit.getName()
            + " of size " + genUnit.getCompilationUnits().size());
        processCompiledGenerationUnit(genUnit);
      }
    } catch (IOException e) {
      genUnit.error(e.getMessage());
    } finally {
      Types.cleanup();
      NameTable.cleanup();
    }
  }

  protected abstract void processCompiledGenerationUnit(GenerationUnit unit);

  /**
   * Sets the output path if there isn't one already.
   * For example, foo/bar/Mumble.java translates to $(OUTPUT_DIR)/foo/bar/Mumble.
   * If --no-package-directories is specified, though, the output file is $(OUTPUT_DIR)/Mumble.
   * <p>
   * Note: class names are still camel-cased to avoid name collisions.
   */
  static void ensureOutputPath(GenerationUnit unit) {
    String result = unit.getOutputPath();
    if (result == null) {
      // We can only infer the output path if there's one compilation unit.
      assert unit.getCompilationUnits().size() == 1;
      com.google.devtools.j2objc.ast.CompilationUnit node = unit.getCompilationUnits().get(0);
      PackageDeclaration pkg = node.getPackage();
      if (Options.usePackageDirectories() && !pkg.isDefaultPackage()) {
        result = pkg.getName().getFullyQualifiedName().replace('.', File.separatorChar);
        result += File.separatorChar + node.getMainTypeName();
      } else {
        result = node.getMainTypeName();
      }

      // Make sure the name is legal...
      if (node.getMainTypeName().equals(NameTable.PACKAGE_INFO_MAIN_TYPE)) {
        result = result.replace(NameTable.PACKAGE_INFO_MAIN_TYPE, NameTable.PACKAGE_INFO_FILE_NAME);
      }
    }

    unit.setOutputPath(result);
  }

  protected TimeTracker getTicker(String name) {
    if (logger.isLoggable(Level.FINEST)) {
      return TimeTracker.start(name);
    } else {
      return TimeTracker.noop();
    }
  }

  /**
   * Returns a path equal to the canonical compilation unit name
   * for the given file, which is something like path/to/package/Filename.java.
   */
  protected static String getRelativePath(String path, CompilationUnit unit) {
    int index = path.lastIndexOf(File.separatorChar);
    String name = index >= 0 ? path.substring(index + 1) : path;
    org.eclipse.jdt.core.dom.PackageDeclaration pkg = unit.getPackage();
    if (pkg == null) {
      return name;
    } else {
      return pkg.getName().getFullyQualifiedName().replace('.', File.separatorChar)
          + File.separatorChar + name;
    }
  }

  public JdtParser getParser() {
    return parser;
  }
}
