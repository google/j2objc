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
package com.google.devtools.j2objc.gen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A single unit of generated code, to be turned into a single pair of .h and .m files.
 * <p/>
 * Some attributes, like the name and output path, might not be known before parsing.
 * These are set by a {@link com.google.devtools.j2objc.FileProcessor}.
 *
 * @author Mike Thvedt
 */
public class GenerationUnit {

  private String name;
  private String outputPath;
  private final int numUnits;
  private List<CompilationUnit> compilationUnits = Lists.newArrayList();
  private final String sourceName;
  private State state = State.ACTIVE;

  private enum State {
    ACTIVE,   // Initial state, still collecting CompilationUnits.
    FAILED,   // One or more input files failed to compile.
    FINISHED  // Finished, object is now invalid.
  }

  @VisibleForTesting
  public GenerationUnit(String sourceName, int numUnits) {
    this.sourceName = sourceName;
    this.numUnits = numUnits;
  }

  public static GenerationUnit newSingleFileUnit(InputFile file) {
    GenerationUnit unit = new GenerationUnit(file.getPath(), 1);
    if (Options.useSourceDirectories()) {
      String outputPath = file.getUnitName();
      outputPath = outputPath.substring(0, outputPath.lastIndexOf(".java"));
      unit.outputPath = outputPath;
    }
    return unit;
  }

  public static GenerationUnit newCombinedJarUnit(String filename, int numInputs) {
    String outputPath = filename;
    if (outputPath.lastIndexOf(File.separatorChar) < outputPath.lastIndexOf(".")) {
      outputPath = outputPath.substring(0, outputPath.lastIndexOf("."));
    }
    GenerationUnit unit = new GenerationUnit(filename, numInputs);
    unit.outputPath = outputPath;
    unit.name = UnicodeUtils.asValidObjcIdentifier(NameTable.camelCasePath(outputPath));
    return unit;
  }

  /**
   * Gets the 'source name' of this GenerationUnit. Might not be a .java file,
   * but if given, should probably be an actual file somewhere, like a .jar.
   */
  public String getSourceName() {
    return sourceName;
  }

  /**
   * Gets the name of this GenerationUnit.
   * This will be a name appropriate for use in Obj-C output code.
   */
  @Nullable
  public String getName() {
    return name;
  }

  /**
   * A list of CompilationUnits generated from the input files in this GenerationUnit.
   * This list is mildly stateful in that some processors might add to it.
   * These should be later cleared with {@link #clear()}, to save memory and allow reuse
   * of GenerationUnits.
   */
  public List<CompilationUnit> getCompilationUnits() {
    return compilationUnits;
  }

  public void addCompilationUnit(CompilationUnit unit) {
    assert state != State.FINISHED : "Adding to a finished GenerationUnit.";
    if (state != State.ACTIVE) {
      return;  // Ignore any added units.
    }
    assert compilationUnits.size() < numUnits;
    compilationUnits.add(unit);

    if (name == null) {
      assert numUnits == 1;
      name = UnicodeUtils.asValidObjcIdentifier(NameTable.getMainTypeFullName(unit));
    }
    if (outputPath == null) {
      // We can only infer the output path if there's one compilation unit.
      assert numUnits == 1;
      outputPath = getDefaultOutputPath(unit);
    }
  }

  public boolean isFullyParsed() {
    return compilationUnits.size() == numUnits;
  }

  /**
   * Gets the output path if there isn't one already.
   * For example, foo/bar/Mumble.java translates to $(OUTPUT_DIR)/foo/bar/Mumble.
   * If --no-package-directories is specified, though, the output file is $(OUTPUT_DIR)/Mumble.
   */
  private static String getDefaultOutputPath(CompilationUnit unit) {
    String path = unit.getMainTypeName();
    if (path.equals(NameTable.PACKAGE_INFO_MAIN_TYPE)) {
      path = NameTable.PACKAGE_INFO_FILE_NAME;
    }
    PackageDeclaration pkg = unit.getPackage();
    if (Options.usePackageDirectories() && !pkg.isDefaultPackage()) {
      path = pkg.getName().getFullyQualifiedName().replace('.', File.separatorChar)
          + File.separatorChar + path;
    }
    return path;
  }

  public void failed() {
    compilationUnits.clear();
    state = State.FAILED;
  }

  public void finished() {
    compilationUnits.clear();
    state = State.FINISHED;
  }

  /**
   * Gets the output path for this GenerationUnit.
   */
  @Nullable
  public String getOutputPath() {
    return outputPath;
  }
}
