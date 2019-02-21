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

package com.google.devtools.j2objc.pipeline;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * A set of input files for J2ObjC to process,
 * together with behavior for scanning input files and adding more files.
 * This class also contains a queue that can be used by processors that dynamically
 * add more files while they process.
 *
 * @author Tom Ball, Keith Stanger, Mike Thvedt
 */
public class GenerationBatch {

  private static final Logger logger = Logger.getLogger(GenerationBatch.class.getName());
  private static final String J2OBJC_TEMP_DIR_PREFIX = "J2ObjCTempDir";
  private final Options options;

  private final List<ProcessingContext> inputs = Lists.newArrayList();

  private GenerationUnit globalCombinedUnit = null;

  public GenerationBatch(Options options){
    this.options = options;
    if (options.globalCombinedOutput() != null) {
      globalCombinedUnit = options.globalCombinedOutput().globalGenerationUnit();
    }
  }

  public List<ProcessingContext> getInputs() {
    return inputs;
  }

  public void processFileArgs(Iterable<String> args) {
    for (String arg : args) {
      processSourceFile(arg);
    }
  }

  private void processSourceFile(String filename) {
    logger.finest("processing " + filename);
    if (filename.endsWith(".java")
        || (options.translateClassfiles() && filename.endsWith(".class"))) {
      processJavaFile(filename);
    } else {
      processJarFile(filename);
    }
  }

  private void processJavaFile(String filename) {
    InputFile inputFile;

    try {
      inputFile = new RegularInputFile(filename, filename);

      if (!inputFile.exists()) {
        // Check source path for regular file.
        inputFile = options.fileUtil().findFileOnSourcePath(filename);
        if (inputFile == null) {
          ErrorUtil.error("No such file: " + filename);
          return;
        }
      }
    } catch (IOException e) {
      ErrorUtil.warning(e.getMessage());
      return;
    }

    addSource(inputFile);
  }

  private File findJarFile(String filename) {
    File f = new File(filename);
    if (f.exists() && f.isFile()) {
      return f;
    }
    // Checking the sourcepath is helpful for our unit tests where the source
    // jars aren't relative to the current working directory.
    for (String path : options.fileUtil().getSourcePathEntries()) {
      File dir = new File(path);
      if (dir.isDirectory()) {
        f = new File(dir, filename);
        if (f.exists() && f.isFile()) {
          return f;
        }
      }
    }
    return null;
  }

  private void processJarFile(String filename) {
    File f = findJarFile(filename);
    if (f == null) {
      ErrorUtil.error("No such file: " + filename);
      return;
    }

    // Warn if source debugging is specified for a jar file, since native debuggers
    // don't support Java-like source paths.
    if (options.emitLineDirectives()) {
      ErrorUtil.warning("source debugging of jar files is not supported: " + filename);
    }

    GenerationUnit combinedUnit = null;
    if (globalCombinedUnit != null) {
      combinedUnit = globalCombinedUnit;
    } else if (options.getHeaderMap().combineSourceJars()) {
      combinedUnit = GenerationUnit.newCombinedJarUnit(filename, options);
    }
    try {
      ZipFile zfile = new ZipFile(f);
      try {
        Enumeration<? extends ZipEntry> enumerator = zfile.entries();
        File tempDir = FileUtil.createTempDir(J2OBJC_TEMP_DIR_PREFIX);
        String tempDirPath = tempDir.getAbsolutePath();
        options.fileUtil().addTempDir(tempDirPath);
        options.fileUtil().appendSourcePath(tempDirPath);

        while (enumerator.hasMoreElements()) {
          ZipEntry entry = enumerator.nextElement();
          String internalPath = entry.getName();
          if (internalPath.endsWith(".java")
              || (options.translateClassfiles() && internalPath.endsWith(".class"))) {
            // Extract JAR file to a temporary directory
            File outputFile = options.fileUtil().extractZipEntry(tempDir, zfile, entry);
            InputFile newFile = new RegularInputFile(outputFile.getAbsolutePath(), internalPath);
            if (combinedUnit != null) {
              inputs.add(new ProcessingContext(newFile, combinedUnit));
            } else {
              addExtractedJarSource(newFile, filename, internalPath);
            }
          }
        }
      } finally {
        zfile.close();  // Also closes input stream.
      }
    } catch (ZipException e) { // Also catches JarExceptions
      logger.fine(e.getMessage());
      ErrorUtil.error("Error reading file " + filename + " as a zip or jar file.");
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  private void addExtractedJarSource(InputFile file, String jarFileName, String internalPath) {
    String sourceName = "jar:file:" + jarFileName + "!" + internalPath;
    inputs.add(ProcessingContext.fromExtractedJarEntry(file, sourceName, options));
  }

  /**
   * Adds the given InputFile to this GenerationBatch,
   * creating GenerationUnits and inferring unit names/output paths as necessary.
   */
  @VisibleForTesting
  public void addSource(InputFile file) {
    if (globalCombinedUnit != null) {
      inputs.add(new ProcessingContext(file, globalCombinedUnit));
    } else {
      inputs.add(ProcessingContext.fromFile(file, options));
    }
  }
}
