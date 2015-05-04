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

  private final ITypeBinding type;
  private final String typeName;
  private final ITypeBinding mainType;
  private final String mainTypeName;
  private final String importFileName;

  private Import(ITypeBinding type, NameTable nameTable) {
    this.type = type;
    this.typeName = nameTable.getFullName(type);
    ITypeBinding mainType = type;
    while (!mainType.isTopLevel()) {
      mainType = mainType.getDeclaringClass();
    }
    this.mainType = mainType;
    this.mainTypeName = nameTable.getFullName(mainType);
    this.importFileName = Options.getHeaderMap().get(mainType);
  }

  public ITypeBinding getType() {
    return type;
  }

  public String getTypeName() {
    return typeName;
  }

  public ITypeBinding getMainType() {
    return mainType;
  }

  public String getMainTypeName() {
    return mainTypeName;
  }

  public String getImportFileName() {
    return importFileName;
  }

  public boolean isInterface() {
    return type.isInterface();
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
    if (FOUNDATION_TYPES.contains(binding.getName())) {
      return;
    }
    imports.add(new Import(binding, unit.getNameTable()));
  }
}
