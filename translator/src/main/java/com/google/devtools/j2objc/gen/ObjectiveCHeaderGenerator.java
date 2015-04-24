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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.Import;

import java.util.Set;

/**
 * Generates Objective-C header files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCHeaderGenerator extends ObjectiveCSourceFileGenerator {

  /**
   * Generate an Objective-C header file for each type declared in the given {@link GenerationUnit}.
   */
  public static void generate(GenerationUnit unit) {
    new ObjectiveCHeaderGenerator(unit).generate();
  }

  protected ObjectiveCHeaderGenerator(GenerationUnit unit) {
    super(unit, false);
  }

  @Override
  protected String getSuffix() {
    return ".h";
  }

  public void generate() {
    println(J2ObjC.getFileHeader(getGenerationUnit().getSourceName()));

    Set<PackageDeclaration> packagesToDoc = Sets.newLinkedHashSet();

    // First, gather everything we need to generate.
    // We do this first because we'll be reordering it later.
    for (CompilationUnit unit : getGenerationUnit().getCompilationUnits()) {
      // It would be nice if we could put the PackageDeclarations and AbstractTypeDeclarations
      // in the same list of 'things to generate'.
      // TODO(mthvedt): Puzzle--figure out a way to do that in Java's type system
      // that is worth the effort.
      PackageDeclaration pkg = unit.getPackage();
      if (pkg.getJavadoc() != null && Options.docCommentsEnabled()) {
        packagesToDoc.add(pkg);
      }
    }

    generateFileHeader();

    for (AbstractTypeDeclaration decl : getOrderedTypes()) {
      CompilationUnit unit = TreeUtil.getCompilationUnit(decl);

      // Print package docs before the first type in the package. (See above comments and TODO.)
      if (Options.docCommentsEnabled() && packagesToDoc.contains(unit.getPackage())) {
        newline();
        JavadocGenerator.printDocComment(getBuilder(), unit.getPackage().getJavadoc());
        packagesToDoc.remove(unit.getPackage());
      }

      generateType(decl);
    }

    for (PackageDeclaration pkg : packagesToDoc) {
      newline();
      JavadocGenerator.printDocComment(getBuilder(), pkg.getJavadoc());
    }

    generateFileFooter();
    save(getOutputPath());
  }

  protected void generateType(AbstractTypeDeclaration node) {
    TypeDeclarationGenerator.generate(getBuilder(), node);
  }

  protected void generateFileHeader() {
    printf("#ifndef _%s_H_\n", getGenerationUnit().getName());
    printf("#define _%s_H_\n", getGenerationUnit().getName());
    pushIgnoreDeprecatedDeclarationsPragma();

    HeaderImportCollector collector =
        new HeaderImportCollector(HeaderImportCollector.Filter.PUBLIC_ONLY);
    // Order matters for finding forward declarations.
    collector.collect(getOrderedTypes());

    Set<String> includeFiles = Sets.newTreeSet();
    includeFiles.add("J2ObjC_header.h");
    for (Import imp : collector.getSuperTypes()) {
      if (!isLocalType(imp.getType())) {
        includeFiles.add(imp.getImportFileName());
      }
    }

    // Print collected includes.
    newline();
    for (String header : includeFiles) {
      printf("#include \"%s\"\n", header);
    }

    // Filter out any declarations that are resolved by an include.
    Set<Import> forwardDeclarations = Sets.newHashSet();
    for (Import imp : collector.getForwardDeclarations()) {
      if (!includeFiles.contains(imp.getImportFileName())) {
        forwardDeclarations.add(imp);
      }
    }
    printForwardDeclarations(forwardDeclarations);
  }

  protected void generateFileFooter() {
    newline();
    popIgnoreDeprecatedDeclarationsPragma();
    printf("#endif // _%s_H_\n", getGenerationUnit().getName());
  }
}
