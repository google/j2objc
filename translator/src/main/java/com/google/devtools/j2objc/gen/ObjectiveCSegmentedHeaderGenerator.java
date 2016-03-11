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
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.types.Import;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Generates segmented Objective-C header files from compilation units. In a
 * segmented header each type is given separate header guards and can be
 * included without including the other types in the file.
 *
 * @author Keith Stanger
 */
public class ObjectiveCSegmentedHeaderGenerator extends ObjectiveCHeaderGenerator {


  protected ObjectiveCSegmentedHeaderGenerator(GenerationUnit unit) {
    super(unit);
  }

  public static void generate(GenerationUnit unit) {
    new ObjectiveCSegmentedHeaderGenerator(unit).generate();
  }

  @Override
  protected void generateFileHeader() {
    println("#include \"J2ObjC_header.h\"");
    newline();
    printf("#pragma push_macro(\"INCLUDE_ALL_%s\")\n", varPrefix);
    printf("#ifdef RESTRICT_%s\n", varPrefix);
    printf("#define INCLUDE_ALL_%s 0\n", varPrefix);
    println("#else");
    printf("#define INCLUDE_ALL_%s 1\n", varPrefix);
    println("#endif");
    printf("#undef RESTRICT_%s\n", varPrefix);

    for (GeneratedType type : Lists.reverse(getOrderedTypes())) {
      printLocalIncludes(type);
    }
    pushIgnoreDeprecatedDeclarationsPragma();

    // Print OCNI blocks
    Collection<String> nativeBlocks = getGenerationUnit().getNativeHeaderBlocks();
    if (!nativeBlocks.isEmpty()) {
      // Use a normal header guard for OCNI code outside of a type declaration.
      printf("\n#ifndef %s_H\n", varPrefix);
      printf("#define %s_H\n", varPrefix);
      for (String code : nativeBlocks) {
        print(code);
      }
      printf("\n#endif // %s_H\n", varPrefix);
    }
  }

  /**
   * Given a {@link com.google.devtools.j2objc.gen.GeneratedType}
   * and its collected {@link com.google.devtools.j2objc.types.Import}s,
   * print its 'local includes'; viz.,
   * {@code INCLUDE} directives for all supertypes that are defined in the
   * current segmented header.
   */
  private void printLocalIncludes(GeneratedType type) {
    String typeName = type.getTypeName();
    Set<Import> includes = type.getHeaderIncludes();
    List<Import> localImports = Lists.newArrayList();
    for (Import imp : includes) {
      if (isLocalType(imp.getTypeName())) {
        localImports.add(imp);
      }
    }
    if (!localImports.isEmpty()) {
      printf("#ifdef INCLUDE_%s\n", typeName);
      for (Import imp : localImports) {
        printf("#define INCLUDE_%s 1\n", imp.getTypeName());
      }
      println("#endif");
    }
  }

  @Override
  protected void generateFileFooter() {
    // Don't need #endif for file-level header guard.
    newline();
    popIgnoreDeprecatedDeclarationsPragma();
    printf("#pragma pop_macro(\"INCLUDE_ALL_%s\")\n", varPrefix);
  }

  @Override
  protected void printTypeDeclaration(GeneratedType type) {
    String typeName = type.getTypeName();
    String code = type.getPublicDeclarationCode();
    if (code.length() == 0) {
      return;
    }

    newline();
    printf("#if !defined (%s_) && (INCLUDE_ALL_%s || defined(INCLUDE_%s))\n",
        typeName, varPrefix, typeName);
    printf("#define %s_\n", typeName);

    Set<Import> forwardDeclarations = Sets.newHashSet(type.getHeaderForwardDeclarations());

    for (Import imp : type.getHeaderIncludes()) {
      // Verify this import isn't declared in this source file.
      if (isLocalType(imp.getTypeName())) {
        continue;
      }
      newline();
      printf("#define RESTRICT_%s 1\n", getVarPrefix(imp.getImportFileName()));
      printf("#define INCLUDE_%s 1\n", imp.getTypeName());
      printf("#include \"%s\"\n", imp.getImportFileName());
      forwardDeclarations.remove(imp);
    }

    printForwardDeclarations(forwardDeclarations);

    print(code);
    newline();
    println("#endif");
  }
}
