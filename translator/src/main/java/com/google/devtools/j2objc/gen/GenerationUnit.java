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
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.NameTable;

import java.util.Collections;
import java.util.List;

/**
 * A single unit of generated code, to be turned into a single pair of .h and .m files.
 *
 * @author Mike Thvedt
 */
public class GenerationUnit {
  private final List<CompilationUnit> work;
  private final String outputPath;
  private final String sourceName;
  private final String name;

  private GenerationUnit(
      List<CompilationUnit> work, String outputPath, String sourceName, String name) {
    this.work = work;
    this.outputPath = outputPath;
    this.sourceName = sourceName;
    this.name = name;
  }

  @VisibleForTesting
  public static GenerationUnit fromSingleUnit(CompilationUnit unit, String unitPath) {
    List<CompilationUnit> work = Collections.singletonList(unit);
    String name = NameTable.getMainTypeFullName(unit);
    return new GenerationUnit(work, unitPath, unitPath, name);
  }

  public static GenerationUnit fromSingleFile(
      InputFile inputFile, CompilationUnit unit, String outputPath) {
    List<CompilationUnit> work = Collections.singletonList(unit);
    String name = NameTable.getMainTypeFullName(unit);
    return new GenerationUnit(work, outputPath, inputFile.getPath(), name);
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
  public String getName() {
    return name;
  }

  public List<CompilationUnit> getCompilationUnits() {
    return work;
  }

  public String getOutputPath() {
    return outputPath;
  }
}
