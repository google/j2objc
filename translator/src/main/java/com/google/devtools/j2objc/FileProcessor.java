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
import com.google.devtools.j2objc.ast.TreeConverter;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.dom.CompilationUnit;

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
  private final NameTable.Factory nameTableFactory = NameTable.newFactory();

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

    String source;
    try {
      source = FileUtil.readFile(file);
    } catch (IOException e) {
      generationUnit.error(e.getMessage());
      return;
    }

    CompilationUnit compilationUnit = parser.parseWithBindings(file.getUnitName(), source);
    if (compilationUnit == null) {
      return;
    }

    processCompilationUnit(generationUnit, compilationUnit, file);
  }

  /**
   * Callback invoked when a file is parsed.
   */
  protected void processCompilationUnit(
      GenerationUnit genUnit, CompilationUnit unit, InputFile file) {
    try {
      String source = FileUtil.readFile(file);
      com.google.devtools.j2objc.ast.CompilationUnit translatedUnit
          = TreeConverter.convertCompilationUnit(unit, file, source, nameTableFactory);
      genUnit.addCompilationUnit(translatedUnit);

      if (genUnit.isFullyParsed()) {
        logger.finest("Processing compiled unit " + genUnit.getName()
            + " of size " + genUnit.getCompilationUnits().size());
        processCompiledGenerationUnit(genUnit);
      }
    } catch (IOException e) {
      genUnit.error(e.getMessage());
    }
  }

  protected abstract void processCompiledGenerationUnit(GenerationUnit unit);

  protected TimeTracker getTicker(String name) {
    if (logger.isLoggable(Level.FINEST)) {
      return TimeTracker.start(name);
    } else {
      return TimeTracker.noop();
    }
  }

  public JdtParser getParser() {
    return parser;
  }
}
