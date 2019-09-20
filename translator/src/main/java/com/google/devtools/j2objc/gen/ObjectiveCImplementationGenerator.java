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
    printImports();
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

  private void printImports() {
    Set<String> includeFiles = Sets.newTreeSet();
    includeFiles.add("J2ObjC_source.h");
    includeFiles.add(getGenerationUnit().getOutputPath() + ".h");
    for (GeneratedType generatedType : getOrderedTypes()) {
      for (Import imp : generatedType.getImplementationIncludes()) {
        if (!isLocalType(imp.getTypeName())) {
          includeFiles.add(imp.getImportFileName());
        }
      }
    }

    newline();
    for (String header : includeFiles) {
      printf("#include \"%s\"\n", header);
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
  }
}
