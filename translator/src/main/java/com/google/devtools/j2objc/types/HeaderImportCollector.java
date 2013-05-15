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

import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Iterator;

/**
 * Collects the set of imports needed to resolve type references in a header.
 *
 * @author Tom Ball
 */
public class HeaderImportCollector extends ImportCollector {

  private boolean includeMainType;

  public HeaderImportCollector() {
    this(false);
  }

  protected HeaderImportCollector(boolean includeMainType) {
    this.includeMainType = includeMainType;
  }

  public void collect(CompilationUnit unit, String sourceName) {
    super.collect(unit);
    if (includeMainType) {
      return;
    }
    String mainTypeName = NameTable.getMainTypeName(unit, sourceName);
    removeTypeWithName(mainTypeName, imports);
    removeTypeWithName(mainTypeName, superTypes);
  }

  private static void removeTypeWithName(String typeName, Iterable<Import> imports) {
    Iterator<Import> iter = imports.iterator();
    while (iter.hasNext()) {
      if (iter.next().getTypeName().equals(typeName)) {
        iter.remove();
      }
    }
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    addImports(node.getType());
    return super.visit(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    addImports(node.getReturnType2());
    for (Iterator<?> iterator = node.parameters().iterator(); iterator.hasNext(); ) {
      Object o = iterator.next();
      if (o instanceof SingleVariableDeclaration) {
        addImports(((SingleVariableDeclaration) o).getType());
      } else {
        throw new AssertionError("unknown AST type: " + o.getClass());
      }
    }
    return super.visit(node);
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    ITypeBinding binding = Types.getTypeBinding(node);
    if (binding.isEqualTo(Types.getNSObject())) {
      return false;
    }
    addSuperType(node.getSuperclassType());
    for (Iterator<?> iterator = node.superInterfaceTypes().iterator(); iterator.hasNext();) {
      Object o = iterator.next();
      if (o instanceof Type) {
        addSuperType((Type) o);
      } else {
        throw new AssertionError("unknown AST type: " + o.getClass());
      }
    }
    return true;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    addSuperType("JavaLangEnum", "java.lang.Enum", false);
    for (Iterator<?> iterator = node.superInterfaceTypes().iterator(); iterator.hasNext();) {
      Object o = iterator.next();
      if (o instanceof Type) {
        addSuperType((Type) o);
      } else {
        throw new AssertionError("unknown AST type: " + o.getClass());
      }
    }
    return true;
  }
}
