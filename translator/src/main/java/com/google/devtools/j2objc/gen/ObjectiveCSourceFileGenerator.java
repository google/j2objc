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

import com.google.common.io.Files;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Generates source files from AST types.  This class handles common actions
 * shared by the header and implementation generators.
 *
 * @author Tom Ball
 */
public abstract class ObjectiveCSourceFileGenerator extends AbstractSourceGenerator {

  private final GenerationUnit unit;
  private final Map<String, GeneratedType> typesByName;
  private final List<GeneratedType> orderedTypes;

  /**
   * Create a new generator.
   *
   * @param unit The AST of the source to generate
   * @param emitLineDirectives if true, generate CPP line directives
   */
  protected ObjectiveCSourceFileGenerator(GenerationUnit unit, boolean emitLineDirectives) {
    super(new SourceBuilder(emitLineDirectives));
    this.unit = unit;
    orderedTypes = getOrderedGeneratedTypes(unit);
    typesByName = new HashMap<>();
    for (GeneratedType type : orderedTypes) {
      String name = type.getTypeName();
      if (name != null) {
        typesByName.put(name, type);
      }
    }
  }

  /**
   * Returns the suffix for files created by this generator.
   */
  protected abstract String getSuffix();

  protected String getOutputPath() {
    return getGenerationUnit().getOutputPath() + getSuffix();
  }

  protected GenerationUnit getGenerationUnit() {
    return unit;
  }

  protected List<GeneratedType> getOrderedTypes() {
    return orderedTypes;
  }

  protected GeneratedType getLocalType(String name) {
    return typesByName.get(name);
  }

  protected boolean isLocalType(String name) {
    return typesByName.containsKey(name);
  }

  protected void save(String path, File outputDirectory) {
    try {
      File outputFile = new File(outputDirectory, path);
      File dir = outputFile.getParentFile();
      if (dir != null && !dir.exists()) {
        if (!dir.mkdirs()) {
          ErrorUtil.warning("cannot create output directory: " + outputDirectory);
        }
      }
      String source = getBuilder().toString();

      // Make sure file ends with a new-line.
      if (!source.endsWith("\n")) {
        source += '\n';
      }

      Files.asCharSink(outputFile, unit.options().fileUtil().getCharset()).write(source);
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    } finally {
      reset();
    }
  }

  /** Ignores deprecation warnings. Deprecation warnings should be visible for human authored code,
   *  not transpiled code. This method should be paired with popIgnoreDeprecatedDeclarationsPragma.
   */
  protected void pushIgnoreDeprecatedDeclarationsPragma() {
    if (unit.options().generateDeprecatedDeclarations()) {
      newline();
      println("#pragma clang diagnostic push");
      println("#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    }
  }

  /** Restores deprecation warnings after a call to pushIgnoreDeprecatedDeclarationsPragma. */
  protected void popIgnoreDeprecatedDeclarationsPragma() {
    if (unit.options().generateDeprecatedDeclarations()) {
      println("\n#pragma clang diagnostic pop");
    }
  }

  protected void printForwardDeclarations(Set<Import> forwardDecls) {
    Set<String> forwardStmts = new TreeSet<>();
    for (Import imp : forwardDecls) {
      forwardStmts.add(createForwardDeclaration(imp));
    }
    if (!forwardStmts.isEmpty()) {
      newline();
      for (String stmt : forwardStmts) {
        println(stmt);
      }
    }
  }

  private String createForwardDeclaration(Import imp) {
    // Type-specific forward declaration.
    if (imp.getForwardDeclaration() != null) {
      // Empty forward declaration can be ignored.
      if (imp.getForwardDeclaration().isEmpty()) {
        return "";
      } else {
        return UnicodeUtils.format("%s;", imp.getForwardDeclaration());
      }
    } else if (imp.isInterface()) {
      // Obj-C protocols do not support parameters.
      return UnicodeUtils.format("@protocol %s;", imp.getTypeName());
    } else {
      String params = "";
      if ((unit.options().asObjCGenericDecl() || imp.hasGenerateObjectiveCGenerics())
          && !imp.getParameterNamesForObjectiveCGenerics().isEmpty()) {
        params = "<" + String.join(", ", imp.getParameterNamesForObjectiveCGenerics()) + ">";
      }
      return UnicodeUtils.format("@class %s%s;", imp.getTypeName(), params);
    }
  }

  private static List<GeneratedType> getOrderedGeneratedTypes(GenerationUnit generationUnit) {
    // Ordered map because we iterate over it below.
    Collection<GeneratedType> generatedTypes = generationUnit.getGeneratedTypes();
    LinkedHashMap<String, GeneratedType> typeMap = new LinkedHashMap<>();
    for (GeneratedType generatedType : generatedTypes) {
      String name = generatedType.getTypeName();
      if (name != null) {
        if (typeMap.put(name, generatedType) != null) {
          throw new AssertionError("Duplicate type name: " + name);
        }
      }
    }

    LinkedHashSet<GeneratedType> orderedTypes = new LinkedHashSet<>();
    LinkedHashSet<String> typeHierarchy = new LinkedHashSet<>();

    for (GeneratedType generatedType : generatedTypes) {
      collectType(generatedType, orderedTypes, typeMap, typeHierarchy);
    }

    return new ArrayList<>(orderedTypes);
  }

  private static void collectType(
      GeneratedType generatedType, LinkedHashSet<GeneratedType> orderedTypes,
      Map<String, GeneratedType> typeMap, LinkedHashSet<String> typeHierarchy) {
    typeHierarchy.add(generatedType.getTypeName());
    for (String superType : generatedType.getSuperTypes()) {
      GeneratedType requiredType = typeMap.get(superType);
      if (requiredType != null) {
        if (typeHierarchy.contains(superType)) {
          ErrorUtil.error("Duplicate type name found in "
              + typeHierarchy.stream().collect(Collectors.joining("->")) + "->" + superType);
          return;
        }
        collectType(requiredType, orderedTypes, typeMap, typeHierarchy);
      }
    }
    typeHierarchy.remove(generatedType.getTypeName());
    orderedTypes.add(generatedType);
  }

  /**
   * Returns the output path for the given type, minus its suffix. Normally, the output path is the
   * same for an outer and its inner types. However, when separate headers are enabled, the inner
   * types are given their own header files. For example, if the outer type is "Foo.h" with an inner
   * type "Foo.Bar", the header file for the inner type would be "<header_directory>/Foo_Bar.h"
   * (when generating inner type names for Objective-C, inner types are separated by an underscore).
   */
  protected String getHeaderPath(GeneratedType generatedType, String outputPath) {
    if (!unit.options().generateSeparateHeaders()) {
      return outputPath;
    }

    // Extract outer type name from output path.
    String relativeOuterHeader = outputPath.substring(outputPath.lastIndexOf('/') + 1);
    int relativeOuterHeaderLength =
        relativeOuterHeader.endsWith(".h")
            ? relativeOuterHeader.length() - 2
            : relativeOuterHeader.length();
    String simpleOuterTypeName = relativeOuterHeader.substring(0, relativeOuterHeaderLength);

    // Extract inner type's suffix and return
    String innerTypeName = generatedType.getTypeName();
    int outerPathOffset = innerTypeName.lastIndexOf(simpleOuterTypeName);
    if (outerPathOffset == -1) {
      return outputPath;
    }
    String relativeInnerHeader = innerTypeName.substring(outerPathOffset);
    return outputPath.substring(0, outputPath.lastIndexOf('/') + 1) + relativeInnerHeader;
  }
}
