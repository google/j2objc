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
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.MultimapBuilder;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.NameTable;

import java.io.File;
import java.util.Collection;
import java.util.TreeMap;

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

  private String outputPath;
  private final int numUnits;
  private int receivedUnits = 0;
  // It is useful for the generated code to be consistent. Therefore, the
  // ordering of generated code within this unit should be consistent. For this
  // we map units of generated code keyed by the Java class they come from,
  // using map implementations with ordered keys.
  private TreeMap<String, String> nativeImplementationBlocks = Maps.newTreeMap();
  private ListMultimap<String, GeneratedType> generatedTypes =
      MultimapBuilder.treeKeys().arrayListValues().build();
  private final String sourceName;
  private State state = State.ACTIVE;
  private boolean hasIncompleteProtocol = false;
  private boolean hasIncompleteImplementation = false;

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
    return unit;
  }

  /**
   * Gets the 'source name' of this GenerationUnit. Might not be a .java file,
   * but if given, should probably be an actual file somewhere, like a .jar.
   */
  public String getSourceName() {
    return sourceName;
  }

  public boolean hasIncompleteProtocol() {
    return hasIncompleteProtocol;
  }

  public boolean hasIncompleteImplementation() {
    return hasIncompleteImplementation;
  }

  public Collection<String> getNativeImplementationBlocks() {
    return nativeImplementationBlocks.values();
  }

  public Collection<GeneratedType> getGeneratedTypes() {
    return generatedTypes.values();
  }

  public void addCompilationUnit(CompilationUnit unit) {
    assert state != State.FINISHED : "Adding to a finished GenerationUnit.";
    if (state != State.ACTIVE) {
      return;  // Ignore any added units.
    }
    assert receivedUnits < numUnits;
    receivedUnits++;

    if (outputPath == null) {
      // We can only infer the output path if there's one compilation unit.
      assert numUnits == 1;
      outputPath = getDefaultOutputPath(unit);
    }

    hasIncompleteProtocol = hasIncompleteProtocol || unit.hasIncompleteProtocol();
    hasIncompleteImplementation = hasIncompleteImplementation || unit.hasIncompleteImplementation();

    String qualifiedMainType = TreeUtil.getQualifiedMainTypeName(unit);

    SourceBuilder builder = new SourceBuilder(false);
    for (NativeDeclaration decl : unit.getNativeBlocks()) {
      String code = decl.getImplementationCode();
      if (code != null) {
        builder.newline();
        builder.println(builder.reindent(code));
      }
    }
    if (builder.length() > 0) {
      nativeImplementationBlocks.put(qualifiedMainType, builder.toString());
    }

    generatedTypes.put(qualifiedMainType, GeneratedType.forPackageDeclaration(unit));

    for (AbstractTypeDeclaration type : unit.getTypes()) {
      generatedTypes.put(qualifiedMainType, GeneratedType.fromTypeDeclaration(type));
    }
  }

  public boolean isFullyParsed() {
    return receivedUnits == numUnits;
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
    state = State.FAILED;
  }

  public void finished() {
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
