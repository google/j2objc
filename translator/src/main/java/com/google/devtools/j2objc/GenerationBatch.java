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
import com.google.common.io.Files;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

  // Map: Output path -> GenerationUnit. Linked to preserve order, which isn't necessary but is nice
  private final HashMap<String, GenerationUnit> unitMap
      = new LinkedHashMap<String, GenerationUnit>();

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
    FileProcessor.ensureOutputPath(unit);
    if (unit.getName() == null) {
      unit.setName(NameTable.camelCaseQualifiedName(NameTable.getMainTypeFullName(node)));
    }
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
      ZipFile zfile = new ZipFile(f);
      try {
        Enumeration<? extends ZipEntry> enumerator = zfile.entries();
        while (enumerator.hasMoreElements()) {
          ZipEntry entry = enumerator.nextElement();
          String internalPath = entry.getName();
          if (internalPath.endsWith(".java")) {
            InputFile inputFile = new JarredInputFile(filename, filename, internalPath);
            addSource(inputFile);
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

  private GenerationUnit createGenerationUnit(String sourceName, String outputPath) {
    GenerationUnit unit = new GenerationUnit(sourceName);
    unit.setOutputPath(outputPath);
    GenerationUnit prev = unitMap.put(outputPath, unit);
    assert prev == null;
    units.add(unit);
    return unit;
  }

  /**
   * Adds the given InputFile to this GenerationBatch,
   * creating GenerationUnits and inferring unit names/output paths as necessary.
   */
  protected void addSource(InputFile file) {
    GenerationUnit unit;

    if (Options.combineSourceJars() && !file.getSpecifiedPath().endsWith(".java")) {
      String outputPath = file.getSpecifiedPath();
      // If there's no separator, this results in 0
      int lastPathComponentIndex = outputPath.lastIndexOf(File.separatorChar) + 1;
      outputPath = outputPath.substring(lastPathComponentIndex, outputPath.lastIndexOf("."));
      unit = unitMap.get(outputPath);
      if (unit == null) {
        unit = createGenerationUnit(file.getSpecifiedPath(), outputPath);
        unit.setName(NameTable.camelCasePath(outputPath));
      }
    } else if (Options.useSourceDirectories()) {
      String outputPath = file.getUnitName();
      outputPath = outputPath.substring(0, outputPath.lastIndexOf(".java"));
      if (unitMap.containsKey(outputPath)) {
        // The idiomatic behavior while compiling Java files is
        // to proceed if there are colliding input files.
        ErrorUtil.warning("Duplicate input file: "
            + file.getUnitName() + " duplicated on path " + file.getPath());
        return;
      }
      unit = createGenerationUnit(file.getPath(), outputPath);
    } else {
      // GenerationUnit with singleton file and not-yet-known name and output path.
      unit = new GenerationUnit(file.getPath());
      units.add(unit);
    }

    unit.addInputFile(file);
  }

  /**
   * Testing method. Add a source forcing some output path.
   * Sets the 'sourcefile' to the given output path plus ".testfile".
   * In normal operation, addSource consults {@link Options} and determines the correct
   * output path.
   */
  @VisibleForTesting
  void addSource(InputFile file, String outputPath) {
    GenerationUnit unit = unitMap.get(outputPath);
    if (unit == null) {
      unit = createGenerationUnit(outputPath + ".testfile", outputPath);
      // Get a nice looking name for testing purposes
      unit.setName(NameTable.camelCasePath(outputPath));
    }
    unit.addInputFile(file);
  }
}
