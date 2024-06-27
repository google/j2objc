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

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.Import;
import java.util.Iterator;
import java.util.Set;

/**
 * Generates independent Objective-C header files for each type of a compilation unit. These headers
 * don't define header segments, and so can be `#import`ed by other files, removing the restriction
 * that j2objc-generated headers can only be included using `#include`. Because the number of
 * headers can't be determined at build time (prior to the source file being compiled), though this
 * generator doesn't work with build tools that require a known set of outputs.
 *
 * @author Tom Ball
 */
@SuppressWarnings("UngroupedOverloads")
public class ObjectiveCMultiHeaderGenerator extends ObjectiveCHeaderGenerator {

  private final Options options;

  protected ObjectiveCMultiHeaderGenerator(GenerationUnit unit) {
    super(unit);
    this.options = unit.options();
  }

  public static void generate(GenerationUnit unit) {
    new ObjectiveCMultiHeaderGenerator(unit).generate();
  }

  /** Generates the file header for the outer type. */
  @Override
  protected void generateFileHeader() {
    Iterator<GeneratedType> typeIterator = getOrderedTypes().iterator();
    if (typeIterator.hasNext()) {
      GeneratedType outerType = typeIterator.next();
      generateFileHeader(outerType);
      generateTypeDeclaration(outerType);

      while (typeIterator.hasNext()) {
        GeneratedType innerType = typeIterator.next();
        String outputPath = getHeaderPath(innerType, getOutputPath()) + ".h";
        generateInnerHeader(innerType, outputPath, getGenerationUnit(), options);
      }
    }
  }

  @Override
  protected void generateFileFooter() {
    newline();
    popIgnoreNullabilityPragmas();
    popIgnoreDeprecatedDeclarationsPragma();
    if (!getOrderedTypes().isEmpty()) {
      // package-info files don't have any types.
      printf("#endif // %s_H\n", varPrefix);
    }
  }

  private static void generateInnerHeader(
      GeneratedType generatedType, String outputPath, GenerationUnit unit, Options options) {
    ObjectiveCMultiHeaderGenerator generator = new ObjectiveCMultiHeaderGenerator(unit);
    generator.varPrefix = generatedType.getTypeName();
    generator.println(J2ObjC.getFileHeader(options, unit.getSourceName()));
    generatedType.getGeneratedSourceMappings().setTargetOffset(generator.getBuilder().length());
    generator.generateFileHeader(generatedType);
    generator.generateTypeDeclaration(generatedType);
    generator.generateFileFooter();
    generator.save(outputPath, options.fileUtil().getHeaderOutputDirectory());
  }

  /** Generate a file header for a single generated type. */
  protected void generateFileHeader(GeneratedType type) {
    printf("#ifndef %s_H\n", type.getTypeName());
    printf("#define %s_H\n", type.getTypeName());
    pushIgnoreDeprecatedDeclarationsPragma();
    pushIgnoreNullabilityPragmas();

    Set<String> seenTypes = Sets.newHashSet();
    Set<String> includeFiles = Sets.newTreeSet();
    Set<Import> forwardDeclarations = Sets.newHashSet();

    includeFiles.add("J2ObjC_header.h");

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
          && (imp.getImportFileName().isEmpty()
              || !includeFiles.contains(imp.getImportFileName()))) {
        forwardDeclarations.add(imp);
      }
    }

    // Print collected includes.
    newline();
    print("#import <Foundation/Foundation.h>");
    newline();

    for (String header : includeFiles) {
      printf("#import \"%s\"\n", header);
    }
    printForwardDeclarations(forwardDeclarations);

    // Print OCNI blocks
    for (String code : getGenerationUnit().getNativeHeaderBlocks()) {
      print(code);
    }
  }

  @Override
  protected void printTypeDeclaration(GeneratedType generatedType) {
    // Do nothing, as generation is handled in generateFileHeader().
  }

  private void generateTypeDeclaration(GeneratedType generatedType) {
    generatedType.getGeneratedSourceMappings().setTargetOffset(getBuilder().length());
    print(generatedType.getPublicDeclarationCode());
  }
}
