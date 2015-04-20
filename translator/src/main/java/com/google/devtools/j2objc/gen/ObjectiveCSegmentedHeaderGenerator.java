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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.NameTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates segmented Objective-C header files from compilation units. In a
 * segmented header each type is given separate header guards and can be
 * included without including the other types in the file.
 *
 * @author Keith Stanger
 */
public class ObjectiveCSegmentedHeaderGenerator extends ObjectiveCHeaderGenerator {

  private Map<AbstractTypeDeclaration, HeaderImportCollector> importCollectors = Maps.newHashMap();

  protected ObjectiveCSegmentedHeaderGenerator(GenerationUnit unit) {
    super(unit);
  }

  public static void generate(GenerationUnit unit) {
    new ObjectiveCSegmentedHeaderGenerator(unit).generate();
  }

  @Override
  protected void generateFileHeader() {
    String mainGuardName = getGenerationUnit().getName();
    println("#include \"J2ObjC_header.h\"");
    newline();
    printf("#pragma push_macro(\"%s_INCLUDE_ALL\")\n", mainGuardName);
    printf("#if %s_RESTRICT\n", mainGuardName);
    printf("#define %s_INCLUDE_ALL 0\n", mainGuardName);
    println("#else");
    printf("#define %s_INCLUDE_ALL 1\n", mainGuardName);
    println("#endif");
    printf("#undef %s_RESTRICT\n", mainGuardName);

    for (AbstractTypeDeclaration type : Lists.reverse(getOrderedTypes())) {
      HeaderImportCollector collector =
          new HeaderImportCollector(HeaderImportCollector.Filter.PUBLIC_ONLY);
      collector.collect(type);
      importCollectors.put(type, collector);
      printLocalIncludes(type, collector);
    }
    pushIgnoreDeprecatedDeclarationsPragma();
  }

  /**
   * Given a {@link com.google.devtools.j2objc.ast.AbstractTypeDeclaration}
   * and its collected {@link com.google.devtools.j2objc.types.Import}s,
   * print its 'local includes'; viz.,
   * {@code INCLUDE} directives for all supertypes that are defined in the current segmented header.
   */
  private void printLocalIncludes(AbstractTypeDeclaration type, HeaderImportCollector collector) {
    List<Import> localImports = Lists.newArrayList();
    for (Import imp : collector.getSuperTypes()) {
      if (isLocalType(imp.getType())) {
        localImports.add(imp);
      }
    }
    if (!localImports.isEmpty()) {
      NameTable nameTable = TreeUtil.getCompilationUnit(type).getNameTable();
      printf("#if %s_INCLUDE\n", nameTable.getFullName(type.getTypeBinding()));
      for (Import imp : localImports) {
        printf("#define %s_INCLUDE 1\n", imp.getTypeName());
      }
      println("#endif");
    }
  }

  @Override
  protected void generateFileFooter() {
    // Don't need #endif for file-level header guard.
    popIgnoreDeprecatedDeclarationsPragma();
    printf("#pragma pop_macro(\"%s_INCLUDE_ALL\")\n", getGenerationUnit().getName());
  }

  @Override
  public void generateType(AbstractTypeDeclaration node) {
    NameTable nameTable = TreeUtil.getCompilationUnit(node).getNameTable();
    String mainGuardName = getGenerationUnit().getName();
    String typeName = nameTable.getFullName(node.getTypeBinding());
    newline();
    printf("#if !defined (_%s_) && (%s_INCLUDE_ALL || %s_INCLUDE)\n", typeName, mainGuardName,
           typeName);
    printf("#define _%s_\n", typeName);

    HeaderImportCollector collector = importCollectors.get(node);
    assert collector != null;

    Set<Import> forwardDeclarations = Sets.newHashSet(collector.getForwardDeclarations());

    for (Import imp : collector.getSuperTypes()) {
      // Verify this import isn't declared in this source file.
      if (isLocalType(imp.getType())) {
        continue;
      }
      newline();
      printf("#define %s_RESTRICT 1\n", imp.getMainTypeName());
      printf("#define %s_INCLUDE 1\n", imp.getTypeName());
      printf("#include \"%s\"\n", imp.getImportFileName());
      forwardDeclarations.remove(imp);
    }

    printForwardDeclarations(forwardDeclarations);

    super.generateType(node);
    newline();
    println("#endif");
  }
}
