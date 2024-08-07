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

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.Import;
import java.util.Set;

/**
 * Generates Objective-C implementation (.m) files from compilation units.
 *
 * @author Tom Ball
 */
@SuppressWarnings("UngroupedOverloads")
public class ObjectiveCImplementationGenerator extends ObjectiveCSourceFileGenerator {

  private final Options options;

  /**
   * Generate an Objective-C implementation file for each type declared in a
   * specified compilation unit.
   */
  public static void generate(GenerationUnit unit) {
    new ObjectiveCImplementationGenerator(unit).generate();
  }

  private ObjectiveCImplementationGenerator(GenerationUnit unit) {
    super(unit, unit.options().emitLineDirectives());
    options = unit.options();
  }

  @Override
  protected String getSuffix() {
    return options.getLanguage().suffix();
  }

  public void generate() {
    print(J2ObjC.getFileHeader(options, getGenerationUnit().getSourceName()));
    printImportedByJavaImplementation();
    printOptionBuildFlags();
    printImports();
    printMemoryManagement();
    printCompilerSimilarityPragmas();
    printIgnoreIncompletePragmas();
    pushIgnoreDeprecatedDeclarationsPragma();
    for (GeneratedType generatedType : getOrderedTypes()) {
      print(generatedType.getPrivateDeclarationCode());
    }
    for (GeneratedType generatedType : getOrderedTypes()) {
      print(generatedType.getImplementationCode());
    }
    popIgnoreDeprecatedDeclarationsPragma();
    save(getOutputPath(), options.fileUtil().getOutputDirectory());
  }

  private void printCompilerSimilarityPragmas() {
    newline();

    // Ensure that code paths not returning are errors as they would be with Java.
    println("#pragma clang diagnostic error \"-Wreturn-type\"");

    // Java handles incomplete switch enum coverage as a function of a missing return so
    // disable the Clang warning for this (which is commonly enabled). This is a little
    // over-broad, but we do not expect generated code to trigger the other warnings covered
    // by this switch.
    println("#pragma clang diagnostic ignored \"-Wswitch\"");

    newline();
  }

  private void printIgnoreIncompletePragmas() {
    GenerationUnit unit = getGenerationUnit();
    if (unit.hasIncompleteProtocol() || unit.hasIncompleteImplementation()) {
      newline();
    }
    if (unit.hasIncompleteProtocol()) {
      println("#pragma clang diagnostic ignored \"-Wprotocol\"");
    }
    if (unit.hasIncompleteImplementation()) {
      println("#pragma clang diagnostic ignored \"-Wincomplete-implementation\"");
    }
  }

  private void printImportedByJavaImplementation() {
    newline();
    println("#define J2OBJC_IMPORTED_BY_JAVA_IMPLEMENTATION 1");
    newline();
  }

  private void printOptionBuildFlags() {
    newline();
    if (options.useStrictFieldAssign()) {
      println("#define J2OBJC_STRICT_FIELD_ASSIGN 1");
    }
    if (options.useStrictFieldLoad()) {
      println("#define J2OBJC_STRICT_FIELD_LOAD 1");
    }
    if (options.useRetainAutoreleaseReturns()) {
      println("#define J2OBJC_RETAIN_AUTORELEASE_RETURNS 1");
    }
    if (options.useARCAutoreleaseReturns()) {
      println("#define J2OBJC_ARC_AUTORELEASE_RETURNS 1");
    }
    newline();
  }

  private void printImports() {
    Set<String> includeFiles = Sets.newTreeSet();
    includeFiles.add("J2ObjC_source.h");
    String outputPath = getGenerationUnit().getOutputPath();
    if (options.generateSeparateHeaders()) {
      for (GeneratedType generatedType : getOrderedTypes()) {
        includeFiles.add(getHeaderPath(generatedType, outputPath) + ".h");
      }
    } else {
      includeFiles.add(outputPath + ".h");
    }
    for (GeneratedType generatedType : getOrderedTypes()) {
      for (Import imp : generatedType.getImplementationIncludes()) {
        if (!isLocalType(imp.getTypeName()) && !imp.getImportFileName().isEmpty()) {
          includeFiles.add(imp.getImportFileName());
        }
      }
    }

    newline();
    String directive = options.generateSeparateHeaders() ? "#import" : "#include";
    for (String header : includeFiles) {
      printf("%s \"%s\"\n", directive, header);
    }

    for (String code : getGenerationUnit().getNativeImplementationBlocks()) {
      print(code);
    }

    Set<String> seenTypes = Sets.newHashSet();
    Set<Import> forwardDecls = Sets.newHashSet();
    for (GeneratedType generatedType : getOrderedTypes()) {
      String name = generatedType.getTypeName();
      seenTypes.add(name);
      for (Import imp : generatedType.getImplementationForwardDeclarations()) {
        String typeName = imp.getTypeName();
        GeneratedType localType = getLocalType(typeName);
        // For local types, only forward declare private types that haven't been seen yet.
        // For non-local types, only forward declare types that haven't been imported.
        if (localType != null ? (localType.isPrivate() && !seenTypes.contains(typeName))
            : !includeFiles.contains(imp.getImportFileName())) {
          forwardDecls.add(imp);
        }
      }
    }

    printForwardDeclarations(forwardDecls);
    newline();
  }

  private void printMemoryManagement() {
    Options.MemoryManagementOption memoryManagementOption = options.getMemoryManagementOption();
    String filename = getGenerationUnit().getOutputPath();

    if (memoryManagementOption == Options.MemoryManagementOption.ARC) {
      println("#if !__has_feature(objc_arc)");
      println(String.format("#error \"%s must be compiled with ARC (-fobjc-arc)\"", filename));
    } else {
      println("#if __has_feature(objc_arc)");
      println(String.format("#error \"%s must not be compiled with ARC (-fobjc-arc)\"", filename));
      if (getGenerationUnit().hasWeakFields()) {
        println("#if !__has_feature(objc_arc_weak)");
        println(
            String.format(
                "#error \"%s must be compiled with weak references support (-fobjc-weak)\"",
                filename));
        println("#endif");
      }
    }

    println("#endif");
  }
}
