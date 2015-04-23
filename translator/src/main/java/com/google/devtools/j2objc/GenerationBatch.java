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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

  private final List<GenerationUnit> units = new ArrayList<GenerationUnit>();

  public GenerationBatch() {
  }

  @VisibleForTesting
  public static GenerationBatch fromFile(InputFile file) {
    GenerationBatch newBatch = new GenerationBatch();
    newBatch.addSource(file);
    return newBatch;
  }

  @VisibleForTesting
  public static GenerationBatch fromUnit(CompilationUnit node, String name) {
    GenerationBatch newBatch = new GenerationBatch();
    newBatch.addSource(new RegularInputFile(name)); // Might or might not actually exist
    assert newBatch.units.size() == 1;  // One input file -> one GenerationUnit.
    GenerationUnit unit = newBatch.units.get(0);
    unit.addCompilationUnit(node);
    return newBatch;
  }

  public List<GenerationUnit> getGenerationUnits() {
    return Collections.unmodifiableList(units);
  }

  public void processFileArgs(Iterable<String> args) {
    for (String arg : args) {
      processFileArg(arg);
    }
  }

  public void processFileArg(String arg) {
    if (arg.startsWith("@")) {
      processManifestFile(arg.substring(1));
    } else {
      processSourceFile(arg);
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
      if (fileList.isEmpty()) {
        return;
      }
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
    } else {
      processJarFile(filename);
    }
  }

  private void processJavaFile(String filename) {
    InputFile inputFile;

    try {
      inputFile = new RegularInputFile(filename, filename);

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

    addSource(inputFile);
  }

  private void processJarFile(String filename) {
    File f = new File(filename);
    if (!f.exists() || !f.isFile()) {
      ErrorUtil.error("No such file: " + filename);
      return;
    }
    try {
      List<InputFile> inputFiles = Lists.newArrayList();
      ZipFile zfile = new ZipFile(f);
      try {
        Enumeration<? extends ZipEntry> enumerator = zfile.entries();
        while (enumerator.hasMoreElements()) {
          ZipEntry entry = enumerator.nextElement();
          String internalPath = entry.getName();
          if (internalPath.endsWith(".java")) {
            inputFiles.add(new JarredInputFile(filename, internalPath));
          }
        }
      } finally {
        zfile.close();  // Also closes input stream.
      }
      if (Options.combineSourceJars()) {
        units.add(GenerationUnit.newCombinedJarUnit(filename, inputFiles));
      } else {
        for (InputFile file : inputFiles) {
          addSource(file);
        }
      }
    } catch (ZipException e) { // Also catches JarExceptions
      logger.fine(e.getMessage());
      ErrorUtil.error("Error reading file " + filename + " as a zip or jar file.");
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  /**
   * Adds the given InputFile to this GenerationBatch,
   * creating GenerationUnits and inferring unit names/output paths as necessary.
   */
  public void addSource(InputFile file) {
    units.add(GenerationUnit.newSingleFileUnit(file));
  }

  @VisibleForTesting
  void addGenerationUnit(GenerationUnit unit) {
    units.add(unit);
  }
}
