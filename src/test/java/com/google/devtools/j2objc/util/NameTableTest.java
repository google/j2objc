/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Unit tests for {@link NameTable}.
 *
 * @author Tom Ball
 */
public class NameTableTest extends GenerationTest {

  @Override
  protected void tearDown() throws Exception {
    Options.clearPackagePrefixes();
  }

  // Verify class name with prefix.
  public void testGetFullNameWithPrefix() {
    String source = "package foo.bar; public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    TypeDeclaration decl = (TypeDeclaration) unit.types().get(0);
    NameTable.mapPackageToPrefix("foo.bar", "FB");
    assertEquals("FBSomeClass", NameTable.getFullName(decl));
    ITypeBinding binding = Types.getTypeBinding(decl);
    assertEquals("FBSomeClass", NameTable.getFullName(binding));
  }

  // Verify inner class name with prefix.
  public void testGetFullNameWithInnerClassAndPrefix() {
    String source = "package foo.bar; public class SomeClass { static class Inner {}}";
    CompilationUnit unit = translateType("SomeClass", source);
    TypeDeclaration decl = (TypeDeclaration) unit.types().get(1);
    NameTable.mapPackageToPrefix("foo.bar", "FB");
    assertEquals("FBSomeClass_Inner", NameTable.getFullName(decl));
    ITypeBinding binding = Types.getTypeBinding(decl);
    assertEquals("FBSomeClass_Inner", NameTable.getFullName(binding));
  }

  // Verify class name without package is unchanged.
  public void testGetFullNameNoPackage() {
    String source = "public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    TypeDeclaration decl = (TypeDeclaration) unit.types().get(0);
    assertEquals("SomeClass", NameTable.getFullName(decl));
    ITypeBinding binding = Types.getTypeBinding(decl);
    assertEquals("SomeClass", NameTable.getFullName(binding));
  }

  // Verify class name with package is camel-cased.
  public void testGetFullNameWithPackage() {
    String source = "package foo.bar; public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    TypeDeclaration decl = (TypeDeclaration) unit.types().get(0);
    assertEquals("FooBarSomeClass", NameTable.getFullName(decl));
    ITypeBinding binding = Types.getTypeBinding(decl);
    assertEquals("FooBarSomeClass", NameTable.getFullName(binding));
  }

  // Verify inner class name with package is camel-cased.
  public void testGetFullNameWithInnerClass() {
    String source = "package foo.bar; public class SomeClass { static class Inner {}}";
    CompilationUnit unit = translateType("SomeClass", source);
    TypeDeclaration decl = (TypeDeclaration) unit.types().get(1);
    assertEquals("FooBarSomeClass_Inner", NameTable.getFullName(decl));
    ITypeBinding binding = Types.getTypeBinding(decl);
    assertEquals("FooBarSomeClass_Inner", NameTable.getFullName(binding));
  }
}
