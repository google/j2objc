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
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Description of an imported type. Imports are equal if their fully qualified
 * type names are equal.
 *
 * @author Tom Ball
 */
public class Import implements Comparable<Import> {

  private final String typeName;
  private final String importFileName;
  private final String javaQualifiedName;
  private final boolean isInterface;

  private Import(TypeElement type, NameTable nameTable, Options options) {
    this.typeName = nameTable.getFullName(type);
    TypeElement mainType = type;
    while (!ElementUtil.isTopLevel(mainType)) {
      mainType = ElementUtil.getDeclaringClass(mainType);
    }
    this.importFileName = options.getHeaderMap().get(mainType);
    this.javaQualifiedName =
        ElementUtil.isIosType(mainType) ? null : ElementUtil.getQualifiedName(mainType);
    this.isInterface = type.getKind().isInterface();
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

  public static Set<Import> getImports(TypeMirror type, TranslationEnvironment env) {
    Set<Import> result = Sets.newLinkedHashSet();
    addImports(type, result, env);
    return result;
  }

  public static void addImports(
      TypeMirror type, Collection<Import> imports, TranslationEnvironment env) {
    if (type instanceof PointerType) {
      addImports(((PointerType) type).getPointeeType(), imports, env);
    }
    for (TypeElement objcClass : env.typeUtil().getObjcUpperBounds(type)) {
      Import newImport = new Import(objcClass, env.nameTable(), env.options());
      // An empty header indicates a Foundation type that doesn't require an import or forward
      // declaration.
      if (!newImport.getImportFileName().isEmpty()) {
        imports.add(newImport);
      }
    }
  }
}
