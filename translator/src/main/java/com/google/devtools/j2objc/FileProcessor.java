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
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
  private final List<String> batchSources = Lists.newArrayList();
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

  public void processFile(String file) {
    if (file.startsWith("@")) {
      processManifestFile(file.substring(1));
    } else {
      processSourceFile(file);
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
    File f = getFileOrNull(filename);
    if (f != null) {
      if (doBatching) {
        batchSources.add(filename);
      } else {
        processSource(filename);
      }
      return;
    }
    if (f == null) {
      for (String pathEntry : Options.getSourcePathEntries()) {
        if (pathEntry.endsWith(".jar")) {
          String source = getJarEntryOrNull(pathEntry, filename);
          if (source != null) {
            processSource(filename, source);
            return;
          }
        } else {
          f = getFileOrNull(pathEntry + File.separatorChar + filename);
          if (f != null) {
            if (doBatching) {
              batchSources.add(f.getPath());
            } else {
              processSource(f.getPath());
            }
            return;
          }
        }
      }
    }
    ErrorUtil.error("No such file: " + filename);
  }

  private String getJarEntryOrNull(String jarFile, String path) {
    File f = new File(jarFile);
    if (!f.exists() || !f.isFile()) {
      return null;
    }
    try {
      ZipFile zfile = new ZipFile(f);
      try {
        ZipEntry entry = zfile.getEntry(path);
        if (entry != null) {
          Reader in = new InputStreamReader(zfile.getInputStream(entry));
          return CharStreams.toString(in);
        }
      } finally {
        zfile.close();  // Also closes input stream.
      }
    } catch (IOException e) {
      ErrorUtil.warning(e.getMessage());
    }
    return null;
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
          String path = entry.getName();
          if (path.endsWith(".java")) {
            Reader in = new InputStreamReader(zfile.getInputStream(entry));
            processSource(path, CharStreams.toString(in));
          }
        }
      } finally {
        zfile.close();  // Also closes input stream.
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  protected void processSource(String path) {
    try {
      processSource(path, Files.toString(new File(path), Options.getCharset()));
    } catch (IOException e) {
      ErrorUtil.warning(e.getMessage());
    }
  }

  protected void processSource(String path, String source) {
    logger.finest("parsing " + path);
    TimeTracker ticker = getTicker(path);
    ticker.push();

    int errorCount = ErrorUtil.errorCount();
    CompilationUnit unit = parser.parse(path, source);
    if (ErrorUtil.errorCount() > errorCount) {
      return;
    }

    ticker.tick("Parsing file");

    ErrorUtil.setCurrentFileName(path);
    NameTable.initialize();
    Types.initialize(unit);
    processUnit(path, source, unit, ticker);
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
      public void handleParsedUnit(String path, CompilationUnit unit) {
        if (logger.isLoggable(Level.INFO)) {
          System.out.println("translating " + path);
        }
        TimeTracker ticker = getTicker(path);
        ticker.push();
        processUnit(path, unit, ticker);
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

  private void processUnit(String path, CompilationUnit unit, TimeTracker ticker) {
    try {
      ErrorUtil.setCurrentFileName(path);
      NameTable.initialize();
      Types.initialize(unit);
      processUnit(path, Files.toString(new File(path), Options.getCharset()), unit, ticker);
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

  protected abstract void processUnit(
      String path, String source, CompilationUnit unit, TimeTracker ticker);

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

  private static File getFileOrNull(String fileName) {
    File f = new File(fileName);
    return f.exists() ? f : null;
  }
}
