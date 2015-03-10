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

import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;

import java.util.ArrayList;
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
  private final List<InputFile> inputFiles = new ArrayList<InputFile>();
  private List<CompilationUnit> compilationUnits = new ArrayList<CompilationUnit>();
  private final String sourceName;
  private final List<String> errors = new ArrayList<String>();

  public GenerationUnit(String sourceName) {
    this.sourceName = sourceName;
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
   * Sets the name of this GenerationUnit.
   * This should be a name appropriate for use in Obj-C output code.
   */
  public void setName(String name) {
    this.name = name;
  }

  public void addInputFile(InputFile file) {
    assert compilationUnits.isEmpty();  // Probably shouldn't be adding files when this isn't empty.
    inputFiles.add(file);
  }

  public List<InputFile> getInputFiles() {
    return inputFiles;
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
    assert compilationUnits.size() < inputFiles.size();
    compilationUnits.add(unit);
  }

  /**
   * Clear the temporary state of this GenerationUnit.
   */
  public void clear() {
    compilationUnits.clear();
  }

  /**
   * Gets the output path for this GenerationUnit.
   */
  @Nullable
  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  /**
   * A unit is broken if we couldn't read all of its input files.
   * We mark it as broken so some processors may decide to halt for broken GenerationUnits.
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public void error(String message) {
    ErrorUtil.error(message);
    errors.add(message);
  }
}
