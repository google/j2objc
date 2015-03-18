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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;
import java.util.Map;
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

    generateFileHeader();

    Map<ITypeBinding, AbstractTypeDeclaration> declaredTypes = Maps.newHashMap();
    Map<String, ITypeBinding> declaredTypeNames = Maps.newHashMap();
    Map<AbstractTypeDeclaration, CompilationUnit> decls = Maps.newLinkedHashMap();
    Set<PackageDeclaration> packagesToDoc = Sets.newLinkedHashSet();

    // First, gather everything we need to generate.
    // We do this first because we'll be reordering it later.
    for (CompilationUnit unit : getGenerationUnit().getCompilationUnits()) {
      unit.setGenerationContext();

      // It would be nice if we could put the PackageDeclarations and AbstractTypeDeclarations
      // in the same list of 'things to generate'.
      // TODO(mthvedt): Puzzle--figure out a way to do that in Java's type system
      // that is worth the effort.
      PackageDeclaration pkg = unit.getPackage();
      if (pkg.getJavadoc() != null && Options.docCommentsEnabled()) {
        packagesToDoc.add(pkg);
      }

      for (AbstractTypeDeclaration type : unit.getTypes()) {
        decls.put(type, unit);
        declaredTypes.put(type.getTypeBinding(), type);
        declaredTypeNames.put(NameTable.getFullName(type.getTypeBinding()), type.getTypeBinding());
      }
    }

    // We order the type declarations so that the inheritance tree appears in the correct order.
    // The ordering is minimal; a type is reordered only if a subtype is immediately following.
    List<ITypeBinding> orderedDeclarationBindings = Lists.newArrayList();
    for (Map.Entry<AbstractTypeDeclaration, CompilationUnit> e : decls.entrySet()) {
      e.getValue().setGenerationContext();
      orderSuperinterfaces(
          e.getKey().getTypeBinding(), orderedDeclarationBindings, declaredTypeNames);
    }

    Set<AbstractTypeDeclaration> seenDecls = Sets.newHashSet();
    for (ITypeBinding declBinding : orderedDeclarationBindings) {
      AbstractTypeDeclaration decl = declaredTypes.get(declBinding);
      CompilationUnit unit = decls.get(decl);
      if (!seenDecls.add(decl)) {
        continue;
      }

      unit.setGenerationContext();

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

  private void orderSuperinterfaces(ITypeBinding type, List<ITypeBinding> sortedDecls,
      Map<String, ITypeBinding> declaredTypeNames) {
    // In Objective-C, you can't declare a protocol or interface
    // forward of its implementing interfaces.
    if (!type.isAnnotation()) {
      // Annotations don't have overridable supertypes in generated Objective-C code
      ITypeBinding superBinding = type.getSuperclass();
      if (superBinding != null) {
        // The map lookup ensures we get the correct ITypeBinding corresponding to a given
        // CompilationUnit. The Eclipse parser may generate alternate
        // definitions of this ITypeBinding that aren't equal to the one we want.
        superBinding = declaredTypeNames.get(NameTable.getFullName(superBinding));
        if (superBinding != null) {
          orderSuperinterfaces(superBinding, sortedDecls, declaredTypeNames);
        }
      }

      for (ITypeBinding superinterface : type.getInterfaces()) {
        superinterface = declaredTypeNames.get(NameTable.getFullName(superinterface));
        if (superinterface != null) {
          orderSuperinterfaces(superinterface, sortedDecls, declaredTypeNames);
        }
      }
    }

    sortedDecls.add(type);
  }

  protected void generateType(AbstractTypeDeclaration node) {
    TypeDeclarationGenerator.generate(getBuilder(), node);
  }

  protected void printForwardDeclarations(Set<Import> forwardDecls) {
    Set<String> forwardStmts = Sets.newTreeSet();
    for (Import imp : forwardDecls) {
      forwardStmts.add(createForwardDeclaration(imp.getTypeName(), imp.isInterface()));
    }
    if (!forwardStmts.isEmpty()) {
      for (String stmt : forwardStmts) {
        println(stmt);
      }
      newline();
    }
  }

  protected void generateFileHeader() {
    printf("#ifndef _%s_H_\n", getGenerationUnit().getName());
    printf("#define _%s_H_\n", getGenerationUnit().getName());
    pushIgnoreDeprecatedDeclarationsPragma();
    newline();

    HeaderImportCollector collector = new HeaderImportCollector();
    collector.collect(getGenerationUnit().getCompilationUnits());

    printForwardDeclarations(collector.getForwardDeclarations());

    // Print collected includes.
    Set<Import> superTypes = collector.getSuperTypes();
    Set<String> includeStmts = Sets.newTreeSet();
    includeStmts.add("#include \"J2ObjC_header.h\"");
    for (Import imp : superTypes) {
      includeStmts.add(String.format("#include \"%s.h\"", imp.getImportFileName()));
    }
    for (String stmt : includeStmts) {
      println(stmt);
    }
  }

  protected String createForwardDeclaration(String typeName, boolean isInterface) {
    return String.format("@%s %s;", isInterface ? "protocol" : "class", typeName);
  }

  protected void generateFileFooter() {
    newline();
    popIgnoreDeprecatedDeclarationsPragma();
    printf("#endif // _%s_H_\n", getGenerationUnit().getName());
  }
}
