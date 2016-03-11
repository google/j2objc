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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.ImplementationImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Contains the generated source code and additional context for a single Java
 * type. Must not hold references to any AST nodes or bindings because we need
 * those to be cleaned up by the garbage collector.
 */
public class GeneratedType {

  private final String typeName;
  private final boolean isPrivate;
  private final List<String> superTypes;
  private final Set<Import> headerForwardDeclarations;
  private final Set<Import> headerIncludes;
  private final Set<Import> implementationForwardDeclarations;
  private final Set<Import> implementationIncludes;
  private final String publicDeclarationCode;
  private final String privateDeclarationCode;
  private final String implementationCode;

  private GeneratedType(
      String typeName,
      boolean isPrivate,
      List<String> superTypes,
      Set<Import> headerForwardDeclarations,
      Set<Import> headerIncludes,
      Set<Import> implementationForwardDeclarations,
      Set<Import> implementationIncludes,
      String publicDeclarationCode,
      String privateDeclarationCode,
      String implementationCode) {
    this.typeName = Preconditions.checkNotNull(typeName);
    this.isPrivate = isPrivate;
    this.superTypes = Preconditions.checkNotNull(superTypes);
    this.headerForwardDeclarations = Preconditions.checkNotNull(headerForwardDeclarations);
    this.headerIncludes = Preconditions.checkNotNull(headerIncludes);
    this.implementationForwardDeclarations =
        Preconditions.checkNotNull(implementationForwardDeclarations);
    this.implementationIncludes = Preconditions.checkNotNull(implementationIncludes);
    this.publicDeclarationCode = Preconditions.checkNotNull(publicDeclarationCode);
    this.privateDeclarationCode = Preconditions.checkNotNull(privateDeclarationCode);
    this.implementationCode = Preconditions.checkNotNull(implementationCode);
  }

  public static GeneratedType fromTypeDeclaration(AbstractTypeDeclaration typeNode) {
    ITypeBinding typeBinding = typeNode.getTypeBinding();
    CompilationUnit unit = TreeUtil.getCompilationUnit(typeNode);
    NameTable nameTable = unit.getNameTable();

    ImmutableList.Builder<String> superTypes = ImmutableList.builder();
    ITypeBinding superclass = typeBinding.getSuperclass();
    if (superclass != null) {
      superTypes.add(nameTable.getFullName(superclass));
    }
    for (ITypeBinding superInterface : typeBinding.getInterfaces()) {
      superTypes.add(nameTable.getFullName(superInterface));
    }

    HeaderImportCollector headerCollector =
        new HeaderImportCollector(HeaderImportCollector.Filter.PUBLIC_ONLY);
    headerCollector.collect(typeNode);

    HeaderImportCollector privateDeclarationCollector =
        new HeaderImportCollector(HeaderImportCollector.Filter.PRIVATE_ONLY);
    privateDeclarationCollector.collect(typeNode);

    ImplementationImportCollector importCollector = new ImplementationImportCollector();
    importCollector.collect(typeNode);

    SourceBuilder builder = new SourceBuilder(Options.emitLineDirectives());
    TypeDeclarationGenerator.generate(builder, typeNode);
    String publicDeclarationCode = builder.toString();

    builder = new SourceBuilder(Options.emitLineDirectives());
    TypePrivateDeclarationGenerator.generate(builder, typeNode);
    String privateDeclarationCode = builder.toString();

    builder = new SourceBuilder(Options.emitLineDirectives());
    TypeImplementationGenerator.generate(builder, typeNode);
    String implementationCode = builder.toString();

    ImmutableSet.Builder<Import> implementationIncludes = ImmutableSet.builder();
    implementationIncludes.addAll(privateDeclarationCollector.getSuperTypes());
    implementationIncludes.addAll(importCollector.getImports());

    return new GeneratedType(
        nameTable.getFullName(typeBinding),
        typeNode.hasPrivateDeclaration(),
        superTypes.build(),
        ImmutableSet.copyOf(headerCollector.getForwardDeclarations()),
        ImmutableSet.copyOf(headerCollector.getSuperTypes()),
        ImmutableSet.copyOf(privateDeclarationCollector.getForwardDeclarations()),
        implementationIncludes.build(),
        publicDeclarationCode,
        privateDeclarationCode,
        implementationCode);
  }

  /**
   * The name of the ObjC type declared by this GeneratedType.
   */
  public String getTypeName() {
    return typeName;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  /**
   * The list of type names that must be declared prior to this type because it
   * inherits from them.
   */
  public List<String> getSuperTypes() {
    return superTypes;
  }

  public Set<Import> getHeaderForwardDeclarations() {
    return headerForwardDeclarations;
  }

  public Set<Import> getHeaderIncludes() {
    return headerIncludes;
  }

  public Set<Import> getImplementationForwardDeclarations() {
    return implementationForwardDeclarations;
  }

  public Set<Import> getImplementationIncludes() {
    return implementationIncludes;
  }

  public String getPublicDeclarationCode() {
    return publicDeclarationCode;
  }

  public String getPrivateDeclarationCode() {
    return privateDeclarationCode;
  }

  public String getImplementationCode() {
    return implementationCode;
  }

  @Override
  public String toString() {
    return typeName != null ? typeName : "<no-type>";
  }
}
