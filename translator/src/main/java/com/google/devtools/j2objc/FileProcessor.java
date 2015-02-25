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
import com.google.common.io.Files;
import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Abstract base class for processing a list of files. Supports .jar and
 * @manifest files.
 *
 * @author Tom Ball, Keith Stanger
 */
abstract class FileProcessor {

  private static final Logger logger = Logger.getLogger(FileProcessor.class.getName());

  private final JdtParser parser;
  protected final List<InputFile> batchSources = Lists.newArrayList();
  private final boolean doBatching = Options.batchTranslateMaximum() > 0;

  public FileProcessor(JdtParser parser) {
    this.parser = Preconditions.checkNotNull(parser);
  }

  public void processFiles(Iterable<String> files) {
    for (String file : files) {
      processFile(file);
    }
    processBatchSources();
  }

  protected boolean isBatchable(InputFile file) {
    return doBatching && file.getContainingPath().endsWith(".java");
  }

  public void processFile(String filename) {
    if (filename.startsWith("@")) {
      processManifestFile(filename.substring(1));
    } else {
      processSourceFile(filename);
    }
  }

  private void processManifestFile(String filename) {
    if (filename.isEmpty()) {
      ErrorUtil.error("no @ file specified");
      return;
    }
    File f = new File(filename);
    if (!f.exists()) {
      ErrorUtil.error("no such file: " + filename);
      return;
    }
    try {
      String fileList = Files.toString(f, Options.getCharset());
      String[] files = fileList.split("\\s+");  // Split on any whitespace.
      for (String file : files) {
        processSourceFile(file);
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  private void processSourceFile(String filename) {
    logger.finest("processing " + filename);
    if (filename.endsWith(".java")) {
      processJavaFile(filename);
    } else if (filename.endsWith(".jar")) {
      processJarFile(filename);
    } else {
      ErrorUtil.error("Unknown file type: " + filename);
    }
  }

  protected void processJavaFile(String filename) {
    InputFile inputFile;

    try {
      inputFile = new RegularInputFile(filename);

      if (!inputFile.exists()) {
        inputFile = FileUtil.findOnSourcePath(filename);

        if (inputFile == null) {
          ErrorUtil.error("No such file: " + filename);
          return;
        }
      }
    } catch (IOException e) {
      ErrorUtil.warning(e.getMessage());
      return;
    }

    if (isBatchable(inputFile)) {
      batchSources.add(inputFile);
    } else {
      processSource(inputFile);
    }
  }

  protected void processJarFile(String filename) {
    File f = new File(filename);
    if (!f.exists() || !f.isFile()) {
      ErrorUtil.error("No such file: " + filename);
      return;
    }
    try {
      ZipFile zfile = new ZipFile(f);
      try {
        Enumeration<? extends ZipEntry> enumerator = zfile.entries();
        while (enumerator.hasMoreElements()) {
          ZipEntry entry = enumerator.nextElement();
          String internalPath = entry.getName();
          if (internalPath.endsWith(".java")) {
            InputFile file = new JarredInputFile(filename, internalPath);
            processSource(file);
          }
        }
      } finally {
        zfile.close();  // Also closes input stream.
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  protected void processSource(InputFile file) {
    try {
      processSource(file, FileUtil.readFile(file));
    } catch (IOException e) {
      ErrorUtil.warning(e.getMessage());
    }
  }

  protected void processSource(InputFile file, String source) {
    logger.finest("parsing " + file);
    TimeTracker ticker = getTicker(file.getPath());
    ticker.push();

    int errorCount = ErrorUtil.errorCount();
    CompilationUnit unit = parser.parse(file.getUnitName(), source);
    if (ErrorUtil.errorCount() > errorCount) {
      return;
    }

    ticker.tick("Parsing file");

    ErrorUtil.setCurrentFileName(file.getPath());
    NameTable.initialize();
    Types.initialize(unit);
    processUnit(file, source, unit, ticker);
    NameTable.cleanup();
    Types.cleanup();

    ticker.pop();
    ticker.tick("Total processing time");
    ticker.printResults(System.out);
  }

  protected void processBatchSources() {
    if (batchSources.isEmpty()) {
      return;
    }

    JdtParser.Handler handler = new JdtParser.Handler() {
      @Override
      public void handleParsedUnit(InputFile file, CompilationUnit unit) {
        if (logger.isLoggable(Level.INFO)) {
          System.out.println("translating " + file.getPath());
        }
        TimeTracker ticker = getTicker(file.getPath());
        ticker.push();
        processUnit(file, unit, ticker);
      }
    };
    final int maxBatchSize = Options.batchTranslateMaximum();
    for (int from = 0; from < batchSources.size(); from += maxBatchSize) {
      int to = from + maxBatchSize;
      if ( to > batchSources.size()) {
        to = batchSources.size();
      }
      parser.parseFiles(batchSources.subList(from, to), handler);
    }
  }

  private void processUnit(InputFile input, CompilationUnit unit, TimeTracker ticker) {
    try {
      ErrorUtil.setCurrentFileName(input.getPath());
      NameTable.initialize();
      Types.initialize(unit);
      processUnit(input, FileUtil.readFile(input), unit, ticker);
      NameTable.cleanup();
      Types.cleanup();

      ticker.pop();
      ticker.tick("Total processing time");
      ticker.printResults(System.out);
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
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
    PackageDeclaration pkg = unit.getPackage();
    if (pkg == null) {
      return name;
    } else {
      return pkg.getName().getFullyQualifiedName().replace('.', File.separatorChar)
          + File.separatorChar + name;
    }
  }

  protected abstract void processUnit(
      InputFile file, String source, CompilationUnit unit, TimeTracker ticker);

  public JdtParser getParser() {
    return parser;
  }
}
