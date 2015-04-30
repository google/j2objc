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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.TreeConverter;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
  private final Set<ProcessingContext> batchInputs =
      Sets.newLinkedHashSetWithExpectedSize(batchSize);

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

  public void processInputs(Iterable<ProcessingContext> inputs) {
    for (ProcessingContext input : inputs) {
      processInput(input);
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
        processInput(ProcessingContext.fromFile(file));
      }
    }
  }

  private void processInput(ProcessingContext input) {
    InputFile file = input.getFile();

    if (isBatchable(file)) {
      batchInputs.add(input);
      if (batchInputs.size() == batchSize) {
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
      handleError(input);
      return;
    }

    processCompiledSource(input, source, compilationUnit);
  }

  protected boolean isBatchable(InputFile file) {
    return doBatching && file.getContainingPath().endsWith(".java");
  }

  private void processBatch() {
    if (batchInputs.isEmpty()) {
      return;
    }

    List<String> paths = Lists.newArrayListWithCapacity(batchInputs.size());
    final Map<String, ProcessingContext> inputMap =
        Maps.newHashMapWithExpectedSize(batchInputs.size());
    for (ProcessingContext input : batchInputs) {
      String path = input.getFile().getPath();
      paths.add(path);
      inputMap.put(path, input);
    }

    JdtParser.Handler handler = new JdtParser.Handler() {
      @Override
      public void handleParsedUnit(String path, CompilationUnit unit) {
        ProcessingContext input = inputMap.get(path);
        try {
          String source = FileUtil.readFile(input.getFile());
          processCompiledSource(input, source, unit);
          batchInputs.remove(input);
        } catch (IOException e) {
          ErrorUtil.error(e.getMessage());
        }
      }
    };
    logger.finest("Processing batch of size " + batchInputs.size());
    parser.parseFiles(paths, handler);

    // Any remaining files in batchFiles has some kind of error.
    for (ProcessingContext input : batchInputs) {
      handleError(input);
    }

    batchInputs.clear();
  }

  private void processCompiledSource(ProcessingContext input, String source, CompilationUnit unit) {
    InputFile file = input.getFile();
    if (closureQueue != null) {
      closureQueue.addProcessedName(FileUtil.getQualifiedMainTypeName(file, unit));
    }
    com.google.devtools.j2objc.ast.CompilationUnit convertedUnit =
        TreeConverter.convertCompilationUnit(
            unit, input.getOriginalSourcePath(), FileUtil.getMainTypeName(file), source,
            nameTableFactory);
    processConvertedTree(input, convertedUnit);
  }

  protected abstract void processConvertedTree(
      ProcessingContext input, com.google.devtools.j2objc.ast.CompilationUnit unit);

  protected abstract void handleError(ProcessingContext input);
}
