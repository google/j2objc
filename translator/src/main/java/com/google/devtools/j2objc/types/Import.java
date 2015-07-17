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

package com.google.devtools.j2objc.types;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Collection;
import java.util.Set;

/**
 * Description of an imported type. Imports are equal if their fully qualified
 * type names are equal.
 *
 * @author Tom Ball
 */
public class Import implements Comparable<Import> {

  private static final Set<String> FOUNDATION_TYPES =
      ImmutableSet.of("id", "NSObject", "NSString", "NSNumber", "NSCopying", "NSZone");

  private final String typeName;
  private final String importFileName;
  private final String javaQualifiedName;
  private final boolean isInterface;

  private Import(ITypeBinding type, NameTable nameTable) {
    this.typeName = nameTable.getFullName(type);
    ITypeBinding mainType = type;
    while (!mainType.isTopLevel()) {
      mainType = mainType.getDeclaringClass();
    }
    this.importFileName = Options.getHeaderMap().get(mainType);
    this.javaQualifiedName =
        mainType instanceof IOSTypeBinding ? null : mainType.getQualifiedName();
    this.isInterface = type.isInterface();
  }

  /**
   * Gets the Objective-C name of the imported type.
   */
  public String getTypeName() {
    return typeName;
  }

  /**
   * Gets the header file to import for this type.
   */
  public String getImportFileName() {
    return importFileName;
  }

  /**
   * Gets the Java qualified name of the type, or null if it's an IOS type.
   */
  public String getJavaQualifiedName() {
    return javaQualifiedName;
  }

  public boolean isInterface() {
    return isInterface;
  }

  @Override
  public int compareTo(Import other) {
    return typeName.compareTo(other.typeName);
  }

  @Override
  public int hashCode() {
    return typeName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Import other = (Import) obj;
    return typeName.equals(other.typeName);
  }

  @Override
  public String toString() {
    return typeName;
  }

  public static Set<Import> getImports(ITypeBinding binding, CompilationUnit unit) {
    Set<Import> result = Sets.newLinkedHashSet();
    addImports(binding, result, unit);
    return result;
  }

  public static void addImports(
      ITypeBinding binding, Collection<Import> imports, CompilationUnit unit) {
    if (binding == null || binding.isPrimitive()) {
      return;
    }
    if (binding instanceof PointerTypeBinding) {
      addImports(((PointerTypeBinding) binding).getPointeeType(), imports, unit);
      return;
    }
    if (binding.isTypeVariable()) {
      for (ITypeBinding bound : binding.getTypeBounds()) {
        addImports(bound, imports, unit);
      }
      return;
    }
    binding = unit.getTypeEnv().mapType(binding.getErasure());
    // We don't need imports for foundation types or lambdas.
    if (FOUNDATION_TYPES.contains(binding.getName()) || BindingUtil.isLambda(binding)) {
      return;
    }
    imports.add(new Import(binding, unit.getNameTable()));
  }
}
