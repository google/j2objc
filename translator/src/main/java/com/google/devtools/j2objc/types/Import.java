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

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
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
  private final String typeName;
  private final String javaFileName;
  private final boolean isInterface;

  public Import(String typeName, String javaFileName, boolean isInterface) {
    this.typeName = typeName;
    this.javaFileName = javaFileName;
    this.isInterface = isInterface;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getImportFileName() {
    // Always use JRE and JUnit package directories, since the j2objc
    // distribution is (currently) built with package directories.
    if (Options.usePackageDirectories() || javaFileName.startsWith("java") ||
        javaFileName.startsWith("junit")) {
      return javaFileName.replace('.', '/');
    }
    return javaFileName.substring(javaFileName.lastIndexOf('.') + 1);
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

  public static Set<Import> getImports(ITypeBinding binding) {
    Set<Import> result = Sets.newLinkedHashSet();
    addImports(binding, result);
    return result;
  }

  public static void addImports(ITypeBinding binding, Collection<Import> imports) {
    if (binding == null) {
      return;
    }
    if (!binding.isTypeVariable() && !binding.isPrimitive() && !binding.isAnnotation()
        // Don't import IOS types, other than the IOS array types,
        // since they have header files.
        && (binding instanceof IOSArrayTypeBinding
            || !(binding instanceof IOSTypeBinding))) {
      binding = Types.mapType(binding).getErasure();
      String typeName = NameTable.getFullName(binding);
      boolean isInterface = binding.isInterface();
      while (!binding.isTopLevel()) {
        binding = binding.getDeclaringClass();
      }
      if (!Types.isIOSType(typeName)) {
        imports.add(new Import(typeName, binding.getErasure().getQualifiedName(), isInterface));
      }
    } else if (binding.isTypeVariable()) {
      for (ITypeBinding bound : binding.getTypeBounds()) {
        addImports(bound, imports);
      }
    }
  }
}
