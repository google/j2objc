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
import com.google.common.io.Files;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.ErrorUtil;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates source files from AST types.  This class handles common actions
 * shared by the header and implementation generators.
 *
 * @author Tom Ball
 */
public abstract class ObjectiveCSourceFileGenerator extends AbstractSourceGenerator {

  private final GenerationUnit unit;
  private final List<AbstractTypeDeclaration> orderedTypes;
  private final Map<String, AbstractTypeDeclaration> typesByKey;

  /**
   * Create a new generator.
   *
   * @param unit The AST of the source to generate
   * @param emitLineDirectives if true, generate CPP line directives
   */
  protected ObjectiveCSourceFileGenerator(GenerationUnit unit, boolean emitLineDirectives) {
    super(new SourceBuilder(emitLineDirectives));
    this.unit = unit;
    orderedTypes = getOrderedTypes(unit);
    typesByKey = Maps.newHashMap();
    for (AbstractTypeDeclaration typeNode : orderedTypes) {
      typesByKey.put(typeNode.getTypeBinding().getKey(), typeNode);
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

  protected List<AbstractTypeDeclaration> getOrderedTypes() {
    return orderedTypes;
  }

  protected AbstractTypeDeclaration getLocalTypeNode(ITypeBinding type) {
    return typesByKey.get(type.getKey());
  }

  protected boolean isLocalType(ITypeBinding type) {
    return typesByKey.containsKey(type.getKey());
  }

  protected void setGenerationContext(AbstractTypeDeclaration type) {
    CompilationUnit unit = TreeUtil.getCompilationUnit(type);
    syncFilename(unit.getSourceFilePath());
  }

  protected void save(String path) {
    try {
      File outputDirectory = Options.getOutputDirectory();
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

      Files.write(source, outputFile, Options.getCharset());
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
    if (Options.generateDeprecatedDeclarations()) {
      newline();
      println("#pragma clang diagnostic push");
      println("#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    }
  }

  /** Restores deprecation warnings after a call to pushIgnoreDeprecatedDeclarationsPragma. */
  protected void popIgnoreDeprecatedDeclarationsPragma() {
    if (Options.generateDeprecatedDeclarations()) {
      println("\n#pragma clang diagnostic pop");
    }
  }

  protected void printForwardDeclarations(Set<Import> forwardDecls) {
    Set<String> forwardStmts = Sets.newTreeSet();
    for (Import imp : forwardDecls) {
      forwardStmts.add(createForwardDeclaration(imp.getTypeName(), imp.isInterface()));
    }
    if (!forwardStmts.isEmpty()) {
      newline();
      for (String stmt : forwardStmts) {
        println(stmt);
      }
    }
  }

  private String createForwardDeclaration(String typeName, boolean isInterface) {
    return String.format("@%s %s;", isInterface ? "protocol" : "class", typeName);
  }

  private static List<AbstractTypeDeclaration> getOrderedTypes(GenerationUnit generationUnit) {
    // Ordered map because we iterate over it below.
    // We use binding keys because the binding objects are not guaranteed to be
    // unique.
    LinkedHashMap<String, AbstractTypeDeclaration> nodeMap = Maps.newLinkedHashMap();
    for (CompilationUnit unit : generationUnit.getCompilationUnits()) {
      for (AbstractTypeDeclaration node : unit.getTypes()) {
        ITypeBinding typeBinding = node.getTypeBinding();
        String key = typeBinding.getKey();
        assert nodeMap.put(key, node) == null;
      }
    }

    LinkedHashSet<String> orderedKeys = Sets.newLinkedHashSet();

    for (Map.Entry<String, AbstractTypeDeclaration> entry : nodeMap.entrySet()) {
      collectType(entry.getValue().getTypeBinding(), orderedKeys, nodeMap);
    }

    LinkedHashSet<AbstractTypeDeclaration> orderedTypes = Sets.newLinkedHashSet();
    for (String key : orderedKeys) {
      orderedTypes.add(nodeMap.get(key));
    }
    return Lists.newArrayList(orderedTypes);
  }

  private static void collectType(
      ITypeBinding typeBinding, LinkedHashSet<String> orderedKeys,
      Map<String, AbstractTypeDeclaration> nodeMap) {
    if (typeBinding == null) {
      return;
    }
    typeBinding = typeBinding.getTypeDeclaration();
    String key = typeBinding.getKey();
    if (!nodeMap.containsKey(key)) {
      return;
    }
    collectType(typeBinding.getSuperclass(), orderedKeys, nodeMap);
    for (ITypeBinding superInterface : typeBinding.getInterfaces()) {
      collectType(superInterface, orderedKeys, nodeMap);
    }
    orderedKeys.add(key);
  }
}
