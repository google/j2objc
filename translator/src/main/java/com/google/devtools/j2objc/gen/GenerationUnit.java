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
import com.google.common.collect.MultimapBuilder;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.file.InputFile;
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
  private int numUnits;
  private int receivedUnits = 0;
  // It is useful for the generated code to be consistent. Therefore, the
  // ordering of generated code within this unit should be consistent. For this
  // we map units of generated code keyed by the Java class they come from,
  // using map implementations with ordered keys.
  private TreeMap<String, String> javadocBlocks = new TreeMap<>();
  private TreeMap<String, String> nativeHeaderBlocks = new TreeMap<>();
  private TreeMap<String, String> nativeImplementationBlocks = new TreeMap<>();
  private ListMultimap<String, GeneratedType> generatedTypes =
      MultimapBuilder.treeKeys().arrayListValues().build();
  private final String sourceName;
  private State state = State.ACTIVE;
  private boolean hasIncompleteProtocol = false;
  private boolean hasIncompleteImplementation = false;
  private boolean hasNullabilityAnnotations = false;
  private final Options options;

  private enum State {
    ACTIVE,   // Initial state, still collecting CompilationUnits.
    FAILED,   // One or more input files failed to compile.
    FINISHED  // Finished, object is now invalid.
  }

  @VisibleForTesting
  public GenerationUnit(String sourceName, int numUnits, Options options) {
    this.sourceName = sourceName;
    this.numUnits = numUnits;
    this.options = options;
  }

  public static GenerationUnit newSingleFileUnit(InputFile file, Options options) {
    GenerationUnit unit = new GenerationUnit(file.getPath(), 1, options);
    if (options.getHeaderMap().useSourceDirectories()) {
      String outputPath = file.getUnitName();
      outputPath = outputPath.substring(0, outputPath.lastIndexOf(".java"));
      unit.outputPath = outputPath;
    }
    return unit;
  }

  public static GenerationUnit newCombinedJarUnit(String filename, int numInputs, Options options) {
    String outputPath = filename;
    if (outputPath.lastIndexOf(File.separatorChar) < outputPath.lastIndexOf(".")) {
      outputPath = outputPath.substring(0, outputPath.lastIndexOf("."));
    }
    GenerationUnit unit = new GenerationUnit(filename, numInputs, options);
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

  public boolean hasNullabilityAnnotations() {
    return hasNullabilityAnnotations;
  }

  public Collection<String> getJavadocBlocks() {
    return javadocBlocks.values();
  }

  public Collection<String> getNativeHeaderBlocks() {
    return nativeHeaderBlocks.values();
  }

  public Collection<String> getNativeImplementationBlocks() {
    return nativeImplementationBlocks.values();
  }

  public Collection<GeneratedType> getGeneratedTypes() {
    return generatedTypes.values();
  }

  /**
   * Increments the number of inputs for this GenerationUnit. This is called
   * from the annotation preprocessor when an annotation processor has created
   * a new source file.
   */
  public void incrementInputs() {
    numUnits++;
  }

  public void addCompilationUnit(CompilationUnit unit) {
    assert state != State.FINISHED : "Adding to a finished GenerationUnit.";
    if (state != State.ACTIVE) {
      return;  // Ignore any added units.
    }
    assert receivedUnits < numUnits;
    receivedUnits++;

    if (outputPath == null) {
      // The outputPath is only null for units derived from Java source files,
      // not source jars. Since a Java source can contain annotations that
      // generate other sources associated with it, the output path must be
      // determined from the initial source file. Since processor-generated
      // sources are appended to the list of source files, their units are
      // returned after the initial sources have been compiled.
      //
      // NOTE: THIS IS NOT THREADSAFE! It requires that all files in a batch
      // be compiled and translated as a single task. When we support
      // parallelization, each parallel task needs to be constrained this way.
      assert receivedUnits == 1;
      outputPath = options.getHeaderMap().getOutputPath(unit);
    }

    hasIncompleteProtocol = hasIncompleteProtocol || unit.hasIncompleteProtocol();
    hasIncompleteImplementation = hasIncompleteImplementation || unit.hasIncompleteImplementation();
    if (unit.hasNullabilityAnnotations()) {
      hasNullabilityAnnotations = true;
    }

    String qualifiedMainType = TreeUtil.getQualifiedMainTypeName(unit);
    addPackageJavadoc(unit, qualifiedMainType);
    addNativeBlocks(unit, qualifiedMainType);

    for (AbstractTypeDeclaration type : unit.getTypes()) {
      generatedTypes.put(qualifiedMainType, GeneratedType.fromTypeDeclaration(type));
    }
  }

  // Collect javadoc from the package declarations to display in the header.
  private void addPackageJavadoc(CompilationUnit unit, String qualifiedMainType) {
    Javadoc javadoc = unit.getPackage().getJavadoc();
    if (javadoc == null) {
      return;
    }
    SourceBuilder builder = new SourceBuilder(false);
    JavadocGenerator.printDocComment(builder, javadoc);
    javadocBlocks.put(qualifiedMainType, builder.toString());
  }

  private void addNativeBlocks(CompilationUnit unit, String qualifiedMainType) {
    SourceBuilder headerBuilder = new SourceBuilder(false);
    SourceBuilder implBuilder = new SourceBuilder(false);
    for (NativeDeclaration decl : unit.getNativeBlocks()) {
      String headerCode = decl.getHeaderCode();
      if (headerCode != null) {
        headerBuilder.newline();
        headerBuilder.println(headerBuilder.reindent(headerCode));
      }
      String implCode = decl.getImplementationCode();
      if (implCode != null) {
        implBuilder.newline();
        implBuilder.println(implBuilder.reindent(implCode));
      }
    }
    if (headerBuilder.length() > 0) {
      nativeHeaderBlocks.put(qualifiedMainType, headerBuilder.toString());
    }
    if (implBuilder.length() > 0) {
      nativeImplementationBlocks.put(qualifiedMainType, implBuilder.toString());
    }
  }

  public boolean isFullyParsed() {
    return receivedUnits == numUnits;
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (sourceName != null) {
      sb.append(sourceName);
      sb.append(' ');
    }
    sb.append(generatedTypes);
    return sb.toString();
  }

  public Options options() {
    return options;
  }
}
