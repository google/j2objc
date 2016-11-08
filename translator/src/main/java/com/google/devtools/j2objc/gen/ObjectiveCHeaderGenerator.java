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
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.Set;

/**
 * Generates Objective-C header files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCHeaderGenerator extends ObjectiveCSourceFileGenerator {

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
  }

  @Override
  protected String getSuffix() {
    return ".h";
  }

  public void generate() {
    println(J2ObjC.getFileHeader(getGenerationUnit().getSourceName()));
    for (String javadoc : getGenerationUnit().getJavadocBlocks()) {
      print(javadoc);
    }
    generateFileHeader();

    for (GeneratedType generatedType : getOrderedTypes()) {
      printTypeDeclaration(generatedType);
    }

    generateFileFooter();
    save(getOutputPath());
  }

  protected void printTypeDeclaration(GeneratedType generatedType) {
    print(generatedType.getPublicDeclarationCode());
  }

  protected void generateFileHeader() {
    printf("#ifndef %s_H\n", varPrefix);
    printf("#define %s_H\n", varPrefix);
    pushIgnoreDeprecatedDeclarationsPragma();
    pushIgnoreNullabilityCompletenessPragma();

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
    popIgnoreNullabilityCompletenessPragma();
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
   * Ignores nullability completeness warnings. If clang finds any nullability
   * annotations, it checks that all annotatable sites have annotations. Java
   * checker frameworks don't have that requirement.
   */
  protected void pushIgnoreNullabilityCompletenessPragma() {
    if (getGenerationUnit().hasNullabilityAnnotations()) {
      newline();
      println("#if __has_feature(nullability)");
      println("#pragma clang diagnostic push");
      println("#pragma GCC diagnostic ignored \"-Wnullability-completeness\"");
      println("#endif");
    }
  }

  protected void popIgnoreNullabilityCompletenessPragma() {
    if (getGenerationUnit().hasNullabilityAnnotations()) {
      newline();
      println("#if __has_feature(nullability)");
      println("#pragma clang diagnostic pop");
      println("#endif");
    }
  }
}
