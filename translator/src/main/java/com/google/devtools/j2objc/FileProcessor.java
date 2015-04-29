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
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.TreeConverter;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Class for processing GenerationUnits in minimum increments of one GenerationUnit.
 *
 * @author Tom Ball, Keith Stanger, Mike Thvedt
 */
abstract class FileProcessor {

  private static final Logger logger = Logger.getLogger(FileProcessor.class.getName());

  private final JdtParser parser;
  protected final BuildClosureQueue closureQueue;
  private final NameTable.Factory nameTableFactory = NameTable.newFactory();

  private final int batchSize = Options.batchTranslateMaximum();
  private final Set<InputFile> batchFiles = Sets.newLinkedHashSetWithExpectedSize(batchSize);

  private final boolean doBatching = batchSize > 0;

  public FileProcessor(JdtParser parser) {
    this.parser = Preconditions.checkNotNull(parser);
    if (Options.buildClosure()) {
      // Should be an error if the user specifies this with --build-closure
      assert !Options.shouldMapHeaders();
      closureQueue = new BuildClosureQueue();
    } else {
      closureQueue = null;
    }
  }

  public void processFiles(Iterable<? extends InputFile> files) {
    for (InputFile inputFile : files) {
      processInputFile(inputFile);
    }
    processBatch();
  }

  public void processBuildClosureDependencies() {
    if (closureQueue != null) {
      while (true) {
        InputFile file = closureQueue.getNextFile();
        if (file == null) {
          processBatch();
          file = closureQueue.getNextFile();
        }
        if (file == null) {
          break;
        }
        processInputFile(file);
      }
    }
  }

  private void processInputFile(InputFile file) {
    if (isBatchable(file)) {
      batchFiles.add(file);
      if (batchFiles.size() == batchSize) {
        processBatch();
      }
      return;
    }

    logger.finest("parsing " + file);

    String source = null;
    CompilationUnit compilationUnit = null;
    try {
      source = FileUtil.readFile(file);
      compilationUnit = parser.parseWithBindings(file.getUnitName(), source);
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }

    if (compilationUnit == null) {
      handleError(file);
      return;
    }

    processCompiledSource(file, source, compilationUnit);
  }

  protected boolean isBatchable(InputFile file) {
    return doBatching && file.getContainingPath().endsWith(".java");
  }

  private void processBatch() {
    if (batchFiles.isEmpty()) {
      return;
    }

    JdtParser.Handler handler = new JdtParser.Handler() {
      @Override
      public void handleParsedUnit(InputFile file, CompilationUnit unit) {
        try {
          String source = FileUtil.readFile(file);
          processCompiledSource(file, source, unit);
          batchFiles.remove(file);
        } catch (IOException e) {
          ErrorUtil.error(e.getMessage());
        }
      }
    };
    logger.finest("Processing batch of size " + batchFiles.size());
    parser.parseFiles(batchFiles, handler);

    // Any remaining files in batchFiles has some kind of error.
    for (InputFile file : batchFiles) {
      handleError(file);
    }

    batchFiles.clear();
  }

  private void processCompiledSource(InputFile file, String source, CompilationUnit unit) {
    if (closureQueue != null) {
      closureQueue.addProcessedName(FileUtil.getQualifiedMainTypeName(file, unit));
    }
    com.google.devtools.j2objc.ast.CompilationUnit convertedUnit =
        TreeConverter.convertCompilationUnit(unit, file, source, nameTableFactory);
    processConvertedTree(convertedUnit);
  }

  protected abstract void processConvertedTree(com.google.devtools.j2objc.ast.CompilationUnit unit);

  protected abstract void handleError(InputFile file);
}
