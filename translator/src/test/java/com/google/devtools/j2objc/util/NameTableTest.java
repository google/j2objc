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

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.IOException;

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

  public void testTypeVariableWithTypeVariableBounds() {
    String source = "class A<T> { <E extends T> void foo(E e) {} }";
    CompilationUnit unit = translateType("A", source);
    final IMethodBinding[] methodBinding = new IMethodBinding[1];
    unit.accept(new ASTVisitor() {
      @Override public void endVisit(MethodDeclaration node) {
        IMethodBinding binding = Types.getMethodBinding(node);
        if (binding.getName().equals("foo")) {
          methodBinding[0] = binding;
        }
      }
    });
    assertNotNull(methodBinding[0]);
    ITypeBinding paramType = methodBinding[0].getParameterTypes()[0];
    assertEquals("id", NameTable.getSpecificObjCType(paramType));
  }

  public void testPrimitiveArrayParameterName() throws IOException {
    String translation = translateSourceFile("public class A { " +
        "void foo(int[] value1) {}" +
        "void foo(Integer[] value2) {}" +
        "void foo(String[] value3) {}}", "A", "A.h");
    assertTranslation(translation, "- (void)fooWithIntArray:(IOSIntArray *)value1");
    assertTranslation(translation,
        "- (void)fooWithJavaLangIntegerArray:(IOSObjectArray *)value2");
    assertTranslation(translation,
        "- (void)fooWithNSStringArray:(IOSObjectArray *)value3");
  }

  public void testMultiDimArrayName() throws IOException {
    String translation = translateSourceFile("public class A { " +
        "void foo(int[] values) {}" +
        "void foo(int[][] values) {}" +
        "void foo(int[][][] values) {}}", "A", "A.h");
    assertTranslation(translation, "- (void)fooWithIntArray:(IOSIntArray *)values");
    assertTranslation(translation, "- (void)fooWithIntArray2:(IOSObjectArray *)values");
    assertTranslation(translation, "- (void)fooWithIntArray3:(IOSObjectArray *)values");
  }
}
