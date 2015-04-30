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
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.ImplementationImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generates Objective-C implementation (.m) files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCImplementationGenerator extends ObjectiveCSourceFileGenerator {

  private final String suffix;

  /**
   * Generate an Objective-C implementation file for each type declared in a
   * specified compilation unit.
   */
  public static void generate(GenerationUnit unit) {
    new ObjectiveCImplementationGenerator(unit).generate();
  }

  private ObjectiveCImplementationGenerator(GenerationUnit unit) {
    super(unit, Options.emitLineDirectives());
    suffix = Options.getImplementationFileSuffix();
  }

  @Override
  protected String getSuffix() {
    return suffix;
  }

  public void generate() {
    List<CompilationUnit> units = getGenerationUnit().getCompilationUnits();
    List<AbstractTypeDeclaration> types = getOrderedTypes();
    List<CompilationUnit> packageInfos = collectPackageInfos(units);

    println(J2ObjC.getFileHeader(getGenerationUnit().getSourceName()));
    if (!types.isEmpty() || !packageInfos.isEmpty()) {
      printImports(packageInfos);
      for (CompilationUnit packageInfo : packageInfos) {
        syncFilename(packageInfo.getSourceFilePath());
        generatePackageInfo(packageInfo);
      }

      if (!types.isEmpty()) {
        printIgnoreIncompletePragmas(units);
        pushIgnoreDeprecatedDeclarationsPragma();
        for (AbstractTypeDeclaration type : types) {
          setGenerationContext(type);
          TypePrivateDeclarationGenerator.generate(getBuilder(), type);
        }
        for (AbstractTypeDeclaration type : types) {
          setGenerationContext(type);
          TypeImplementationGenerator.generate(getBuilder(), type);
        }

        syncFilename(getGenerationUnit().getSourceName());
        popIgnoreDeprecatedDeclarationsPragma();
      }
    }

    save(getOutputPath());
  }

  private List<CompilationUnit> collectPackageInfos(List<CompilationUnit> units) {
    List<CompilationUnit> packageInfos = new ArrayList<CompilationUnit>();

    for (CompilationUnit unit : units) {
      if (unit.getMainTypeName().endsWith(NameTable.PACKAGE_INFO_MAIN_TYPE)) {
        PackageDeclaration pkg = unit.getPackage();
        if (TreeUtil.getRuntimeAnnotationsList(pkg.getAnnotations()).size() > 0
            && TranslationUtil.needsReflection(pkg)) {
          packageInfos.add(unit);
        }
      }
    }

    return packageInfos;
  }

  private void printIgnoreIncompletePragmas(List<CompilationUnit> units) {
    boolean needsNewline = true;

    for (CompilationUnit unit : units) {
      if (unit.hasIncompleteProtocol()) {
        newline();
        needsNewline = false;
        println("#pragma clang diagnostic ignored \"-Wprotocol\"");
        break;
      }
    }

    for (CompilationUnit unit : units) {
      if (unit.hasIncompleteImplementation()) {
        if (needsNewline) {
          newline();
        }
        println("#pragma clang diagnostic ignored \"-Wincomplete-implementation\"");
        break;
      }
    }
  }

  private void generatePackageInfo(CompilationUnit unit) {
    PackageDeclaration node = unit.getPackage();
    newline();
    String typeName = NameTable.camelCaseQualifiedName(node.getPackageBinding().getName())
        + NameTable.PACKAGE_INFO_MAIN_TYPE;
    printf("@interface %s : NSObject\n", typeName);
    printf("@end\n\n");
    printf("@implementation %s\n", typeName);
    RuntimeAnnotationGenerator.printPackageAnnotationMethod(getBuilder(), node);
    println("\n@end");
  }

  private void printNativeDefinition(NativeDeclaration declaration) {
    newline();
    String code = declaration.getImplementationCode();
    if (code != null) {
      println(reindent(code));
    }
  }

  private void printImports(List<CompilationUnit> packageInfos) {
    HeaderImportCollector declarationCollector =
        new HeaderImportCollector(HeaderImportCollector.Filter.PRIVATE_ONLY);
    declarationCollector.collect(getOrderedTypes());

    ImplementationImportCollector collector = new ImplementationImportCollector();
    collector.collect(packageInfos);
    collector.collect(getOrderedTypes());

    Set<Import> imports = Sets.newHashSet();
    imports.addAll(declarationCollector.getSuperTypes());
    imports.addAll(collector.getImports());

    Set<String> importFiles = Sets.newTreeSet();
    importFiles.add("J2ObjC_source.h");
    importFiles.add(getGenerationUnit().getOutputPath() + ".h");
    for (Import imp : imports) {
      // Local types are handled by including the current file's header.
      if (!isLocalType(imp.getType())) {
        importFiles.add(imp.getImportFileName());
      }
    }

    newline();
    for (String header : importFiles) {
      printf("#include \"%s\"\n", header);
    }

    for (CompilationUnit node : getGenerationUnit().getCompilationUnits()) {
      for (NativeDeclaration decl : node.getNativeBlocks()) {
        printNativeDefinition(decl);
      }
    }

    Set<Import> forwardDecls = Sets.newHashSet();
    for (Import imp : declarationCollector.getForwardDeclarations()) {
      // Only need to forward declare private local types. All else is handled
      // by imports.
      AbstractTypeDeclaration localTypeNode = getLocalTypeNode(imp.getType());
      if (localTypeNode != null && localTypeNode.hasPrivateDeclaration()) {
        forwardDecls.add(imp);
      }
    }
    printForwardDeclarations(forwardDecls);
  }
}
