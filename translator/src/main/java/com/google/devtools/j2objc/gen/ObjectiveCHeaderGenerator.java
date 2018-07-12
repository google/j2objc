/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.Base64;
import java.util.Set;

/**
 * Generates Objective-C header files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCHeaderGenerator extends ObjectiveCSourceFileGenerator {

  private final Options options;

  // The prefix to use for preprocessor variable names. Derived from the path of
  // the generated file. For example if "my/pkg/Foo.h" is being generated the
  // prefix would be "MyPkgFoo".
  protected final String varPrefix;

  /**
   * Generate an Objective-C header file for each type declared in the given {@link GenerationUnit}.
   */
  public static void generate(GenerationUnit unit) {
    new ObjectiveCHeaderGenerator(unit).generate();
  }

  protected ObjectiveCHeaderGenerator(GenerationUnit unit) {
    super(unit, false);
    varPrefix = getVarPrefix(unit.getOutputPath());
    options = unit.options();
  }

  @Override
  protected String getSuffix() {
    return options.getLanguage().headerSuffix();
  }

  public final void generate() {
    println(J2ObjC.getFileHeader(options, getGenerationUnit().getSourceName()));
    for (String javadoc : getGenerationUnit().getJavadocBlocks()) {
      print(javadoc);
    }
    generateFileHeader();

    if (getGenerationUnit().options().emitKytheMappings()) {
      generateKythePragma();
    }

    for (GeneratedType generatedType : getOrderedTypes()) {
      printTypeDeclaration(generatedType);
    }

    generateFileFooter();

    if (getGenerationUnit().options().emitKytheMappings()) {
      generateTypeMappings();
    }

    save(getOutputPath());
  }

  protected void printTypeDeclaration(GeneratedType generatedType) {
    generatedType.getGeneratedSourceMappings().setTargetOffset(getBuilder().length());
    print(generatedType.getPublicDeclarationCode());
  }

  protected void generateFileHeader() {
    printf("#ifndef %s_H\n", varPrefix);
    printf("#define %s_H\n", varPrefix);
    pushIgnoreDeprecatedDeclarationsPragma();
    pushIgnoreNullabilityPragmas();

    Set<String> seenTypes = Sets.newHashSet();
    Set<String> includeFiles = Sets.newTreeSet();
    Set<Import> forwardDeclarations = Sets.newHashSet();

    includeFiles.add("J2ObjC_header.h");

    for (GeneratedType type : getOrderedTypes()) {
      String name = type.getTypeName();
      if (!type.isPrivate()) {
        seenTypes.add(name);
      }
      for (Import imp : type.getHeaderIncludes()) {
        if (!isLocalType(imp.getTypeName())) {
          includeFiles.add(imp.getImportFileName());
        }
      }
      for (Import imp : type.getHeaderForwardDeclarations()) {
        // Filter out any declarations that are resolved by an include.
        if (!seenTypes.contains(imp.getTypeName())
            && !includeFiles.contains(imp.getImportFileName())) {
          forwardDeclarations.add(imp);
        }
      }
    }

    // Print collected includes.
    newline();
    for (String header : includeFiles) {
      printf("#include \"%s\"\n", header);
    }
    printForwardDeclarations(forwardDeclarations);

    // Print OCNI blocks
    for (String code : getGenerationUnit().getNativeHeaderBlocks()) {
      print(code);
    }
  }

  protected void generateFileFooter() {
    newline();
    popIgnoreNullabilityPragmas();
    popIgnoreDeprecatedDeclarationsPragma();
    printf("#endif // %s_H\n", varPrefix);
  }

  protected static String getVarPrefix(String header) {
    if (header.endsWith(".h")) {
      header = header.substring(0, header.length() - 2);
    }
    return UnicodeUtils.asValidObjcIdentifier(NameTable.camelCasePath(header));
  }

  /**
   * Ignores nullability warnings. This method should be paired with popIgnoreNullabilityPragmas.
   *
   * <p>-Wnullability: In Java, conflicting nullability annotations do not cause compilation issues
   * (e.g.changing a parameter from {@code @Nullable} to {@code @NonNull} in an overriding method).
   * In Objective-C, they generate compiler warnings. The transpiled code should be able to compile
   * in spite of conflicting/incomplete Java nullability annotations.
   *
   * <p>-Wnullability-completeness: if clang finds any nullability annotations, it checks that all
   * annotable sites have annotations. Java checker frameworks don't have that requirement.
   */
  protected void pushIgnoreNullabilityPragmas() {
    if (getGenerationUnit().options().nullability()
        || getGenerationUnit().hasNullabilityAnnotations()) {
      newline();
      println("#if __has_feature(nullability)");
      println("#pragma clang diagnostic push");
      println("#pragma GCC diagnostic ignored \"-Wnullability\"");
      println("#pragma GCC diagnostic ignored \"-Wnullability-completeness\"");
      println("#endif");
    }
  }

  /** Restores warnings after a call to pushIgnoreNullabilityPragmas. */
  protected void popIgnoreNullabilityPragmas() {
    if (getGenerationUnit().options().nullability()
        || getGenerationUnit().hasNullabilityAnnotations()) {
      newline();
      println("#if __has_feature(nullability)");
      println("#pragma clang diagnostic pop");
      println("#endif");
    }
  }

  private void generateKythePragma() {
    println("#ifdef KYTHE_IS_RUNNING");
    println("#pragma kythe_inline_metadata \"This file contains Kythe metadata.\"");
    println("#endif");
  }

  private void generateTypeMappings() {
    KytheIndexingMetadata metadata = new KytheIndexingMetadata();

    for (GeneratedType generatedType : getOrderedTypes()) {
      GeneratedSourceMappings sourceMappings = generatedType.getGeneratedSourceMappings();
      int offset = sourceMappings.getTargetOffset();
      for (GeneratedSourceMappings.Mapping mapping : sourceMappings.getMappings()) {
        metadata.addAnchorAnchor(
            mapping.getSourceBegin(),
            mapping.getSourceEnd(),
            mapping.getTargetBegin() + offset,
            mapping.getTargetEnd() + offset,
            "" /* sourceCorpus */,
            getGenerationUnit().getSourceName() /* sourcePath */);
      }
    }

    printKytheMappings(metadata);
  }

  private void printKytheMappings(KytheIndexingMetadata metadata) {
    // The Kythe indexer assumes the JSON metadata is base-64 encoded; we wrap it to 80 characters
    // for readability in the generated source.
    String encodedMetadata =
        new String(Base64.getEncoder().encode(metadata.toJson().getBytes(UTF_8)), UTF_8);
    StringBuilder wrappedMetadata = new StringBuilder();
    int lineWidth = 80;
    for (int i = 0; i <= encodedMetadata.length() / lineWidth; ++i) {
      wrappedMetadata.append(
          encodedMetadata, i * lineWidth, Math.min((i + 1) * lineWidth, encodedMetadata.length()));
      wrappedMetadata.append("\n");
    }

    newline();
    println("/* This file contains Kythe metadata.");
    print(wrappedMetadata.toString());
    println("*/");
  }
}
