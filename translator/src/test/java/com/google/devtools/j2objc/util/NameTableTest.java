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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

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
    ErrorUtil.reset();
  }

  // Verify class name with prefix.
  public void testGetFullNameWithPrefix() {
    String source = "package foo.bar; public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    NameTable.mapPackageToPrefix("foo.bar", "FB");
    assertEquals("FBSomeClass", NameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify inner class name with prefix.
  public void testGetFullNameWithInnerClassAndPrefix() {
    String source = "package foo.bar; public class SomeClass { static class Inner {}}";
    CompilationUnit unit = translateType("SomeClass", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    NameTable.mapPackageToPrefix("foo.bar", "FB");
    assertEquals("FBSomeClass_Inner", NameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify class name without package is unchanged.
  public void testGetFullNameNoPackage() {
    String source = "public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    assertEquals("SomeClass", NameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify class name with package is camel-cased.
  public void testGetFullNameWithPackage() {
    String source = "package foo.bar; public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    assertEquals("FooBarSomeClass", NameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify inner class name with package is camel-cased.
  public void testGetFullNameWithInnerClass() {
    String source = "package foo.bar; public class SomeClass { static class Inner {}}";
    CompilationUnit unit = translateType("SomeClass", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    assertEquals("FooBarSomeClass_Inner", NameTable.getFullName(decl.getTypeBinding()));
  }

  public void testTypeVariableWithTypeVariableBounds() {
    String source = "class A<T> { <E extends T> void foo(E e) {} }";
    CompilationUnit unit = translateType("A", source);
    final IMethodBinding[] methodBinding = new IMethodBinding[1];
    unit.accept(new TreeVisitor() {
      @Override public void endVisit(MethodDeclaration node) {
        IMethodBinding binding = node.getMethodBinding();
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
    String translation = translateSourceFile("public class A { "
        + "void foo(int[] value1) {}"
        + "void foo(Integer[] value2) {}"
        + "void foo(String[] value3) {}}", "A", "A.h");
    assertTranslation(translation, "- (void)fooWithIntArray:(IOSIntArray *)value1");
    assertTranslation(translation,
        "- (void)fooWithJavaLangIntegerArray:(IOSObjectArray *)value2");
    assertTranslation(translation,
        "- (void)fooWithNSStringArray:(IOSObjectArray *)value3");
  }

  public void testMultiDimArrayName() throws IOException {
    String translation = translateSourceFile("public class A { "
        + "void foo(int[] values) {}"
        + "void foo(int[][] values) {}"
        + "void foo(int[][][] values) {}}", "A", "A.h");
    assertTranslation(translation, "- (void)fooWithIntArray:(IOSIntArray *)values");
    assertTranslation(translation, "- (void)fooWithIntArray2:(IOSObjectArray *)values");
    assertTranslation(translation, "- (void)fooWithIntArray3:(IOSObjectArray *)values");
  }

  public void testRenameClassAnnotation() throws IOException {
    addSourceFile("@com.google.j2objc.annotations.ObjectiveCName(\"TestName\") "
        + "public class A { static void test() {}}", "A.java");
    addSourceFile(
        "public class B { void test() { A.test(); }}", "B.java");
    String translation = translateSourceFile("A", "A.h");
    assertTranslation(translation, "@interface TestName : NSObject");
    translation = translateSourceFile("B", "B.m");
    assertTranslation(translation, "[TestName test];");
  }

  public void testRenameMapping() throws IOException {
    Options.getClassMappings().put("foo.bar.A",  "Test2Name");
    try {
      addSourceFile("package foo.bar; public class A { static void test() {}}", "foo/bar/A.java");
      addSourceFile(
          "package foo.bar; public class B { void test() { A.test(); }}", "foo/bar/B.java");
      String translation = translateSourceFile("foo/bar/A", "foo/bar/A.h");
      assertTranslation(translation, "@interface Test2Name : NSObject");
      translation = translateSourceFile("foo/bar/B", "foo/bar/B.m");
      assertTranslation(translation, "[Test2Name test];");
    } finally {
      Options.getClassMappings().remove("foo.bar.A");
    }
  }

  public void testRenameMethodAnnotation() throws IOException {
    String objcName = "test:(NSString *)s offset:(int)n";
    String translation = translateSourceFile("public class A { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"" + objcName + "\") "
        + "void test(String s, int n) {}}", "A", "A.h");
    assertTranslation(translation, String.format("- (void)%s;", objcName));
    assertNotInTranslation(translation, "testWithNSString");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, String.format("- (void)%s {", objcName));
    assertNotInTranslation(translation, "testWithNSString");
  }

  public void testRenameConstructorAnnotation() throws IOException {
    String objcName = "init:(NSString *)s offset:(int)n";
    String translation = translateSourceFile("public class A { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"" + objcName + "\") "
        + "A(String s, int n) {}}", "A", "A.h");
    assertTranslation(translation, String.format("- (instancetype)%s;", objcName));
    assertNotInTranslation(translation, "testWithNSString");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, String.format("- (instancetype)%s {", objcName));
    assertNotInTranslation(translation, "testWithNSString");
  }

  public void testSuperMethodNotNamedWarning() throws IOException {
    translateSourceFile("class A { void test(String s, int n) {}"
        + "static class B extends A { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"test:(NSString *)s offset:(int)n\")"
        + "@Override void test(String s, int n) {}}}", "A", "A.m");
    assertWarningCount(1);
  }

  public void testMethodConflictingName() throws IOException {
    translateSourceFile("class A { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"foo:(NSString *)s bar:(int)n\")"
        + "void test(String s, int n) {}"
        + "static class B extends A { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"test:(NSString *)s offset:(int)n\")"
        + "@Override void test(String s, int n) {}}}", "A", "A.m");
    assertWarningCount(1);
  }
}
