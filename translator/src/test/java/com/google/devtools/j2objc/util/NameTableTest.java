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

  // Verify class name with prefix.
  public void testGetFullNameWithPrefix() {
    String source = "package foo.bar; public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    nameTable.mapPackageToPrefix("foo.bar", "FB");
    assertEquals("FBSomeClass", nameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify inner class name with prefix.
  public void testGetFullNameWithInnerClassAndPrefix() {
    String source = "package foo.bar; public class SomeClass { static class Inner {}}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    nameTable.mapPackageToPrefix("foo.bar", "FB");
    assertEquals("FBSomeClass_Inner", nameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify class name without package is unchanged.
  public void testGetFullNameNoPackage() {
    String source = "public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    assertEquals("SomeClass", nameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify class name with package is camel-cased.
  public void testGetFullNameWithPackage() {
    String source = "package foo.bar; public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    assertEquals("FooBarSomeClass", nameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify inner class name with package is camel-cased.
  public void testGetFullNameWithInnerClass() {
    String source = "package foo.bar; public class SomeClass { static class Inner {}}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    assertEquals("FooBarSomeClass_Inner", nameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify the name of an inner class of an enum.
  public void testGetFullNameEnumWithInnerClasses() {
    String source = "package foo.bar; "
        + "public enum SomeClass { A; static class Inner {} static enum Inner2 { B; }}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    // Outer type should not have "Enum" added to name.
    assertEquals("FooBarSomeClass_Inner", nameTable.getFullName(decl.getTypeBinding()));
    // Inner enum should have "Enum" added to name.
    decl = unit.getTypes().get(2);
    assertEquals("FooBarSomeClass_Inner2Enum", nameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify local class name.
  public void testGetFullNameWithLocalClass() {
    String source = "package foo.bar; class SomeClass { void test() { "
        // Put each Foo in a separate scope, so leading index number changes.
        // This matches JVM naming, once '$' is substituted for the '_' characters.
        + "{ class Foo {}} { class Foo {}}}}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    assertEquals("FooBarSomeClass_1Foo", nameTable.getFullName(decl.getTypeBinding()));
    decl = unit.getTypes().get(2);
    assertEquals("FooBarSomeClass_2Foo", nameTable.getFullName(decl.getTypeBinding()));
  }

  public void testTypeVariableWithTypeVariableBounds() {
    String source = "class A<T> { <E extends T> void foo(E e) {} }";
    CompilationUnit unit = translateType("A", source);
    NameTable nameTable = unit.getNameTable();
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
    assertEquals("id", nameTable.getSpecificObjCType(paramType));
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
    assertTranslation(translation, "TestName_test();");
  }

  public void testRenameMapping() throws IOException {
    Options.getClassMappings().put("foo.bar.A",  "Test2Name");
    try {
      addSourceFile("package foo.bar; public class A { static void test() {}}", "foo/bar/A.java");
      addSourceFile(
          "package foo.bar; public class B { void test() { A.test(); }}", "foo/bar/B.java");
      String translation = translateSourceFile("foo.bar.A", "foo/bar/A.h");
      assertTranslation(translation, "@interface Test2Name : NSObject");
      translation = translateSourceFile("foo.bar.B", "foo/bar/B.m");
      assertTranslation(translation, "Test2Name_test();");
    } finally {
      Options.getClassMappings().remove("foo.bar.A");
    }
  }

  public void testRenameMethodAnnotationWithFullSignature() throws IOException {
    checkRenameMethodAnnotation("test:(NSString *)s offset:(int)n");
  }

  public void testRenameMethodAnnotationWithSelector() throws IOException {
    checkRenameMethodAnnotation("test:offset:");
  }

  public void checkRenameMethodAnnotation(String objcName) throws IOException {
    addSourceFile("public class A { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"" + objcName + "\") "
        + "void test(String s, int n) {}}", "A.java");
    String translation = translateSourceFile("A", "A.h");
    assertTranslatedLines(translation,
        "- (void)test:(NSString *)s",
        "offset:(jint)n;");
    assertNotInTranslation(translation, "testWithNSString:");
    translation = getTranslatedFile("A.m");
    assertTranslatedLines(translation,
        "- (void)test:(NSString *)s",
        "offset:(jint)n {");
    assertNotInTranslation(translation, "testWithNSString:");

    // Test invocation of renamed method.
    translation = translateSourceFile(
        "class Test { void test(A a) { a.test(\"foo\", 4); } }", "Test", "Test.m");
    assertTranslation(translation, "[((A *) nil_chk(a)) test:@\"foo\" offset:4];");
  }

  public void testRenameStaticMethod() throws IOException {
    String translation = translateSourceFile("public class Test { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"foo\") "
        + "static void test(String s, int n) {}}", "Test", "Test.h");
    assertTranslatedLines(translation,
        "+ (void)fooWithNSString:(NSString *)s",
        "                withInt:(jint)n;");
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_foo(NSString *s, jint n);");
    translation = getTranslatedFile("Test.m");
    assertTranslatedLines(translation,
        "+ (void)fooWithNSString:(NSString *)s",
        "                withInt:(jint)n {",
        "  Test_foo(s, n);",
        "}");
    assertTranslatedLines(translation,
        "t_foo(NSString *s, jint n) {",
        "  Test_initialize();",
        "}");
  }

  public void testRenameConstructorAnnotationWithFullSignature() throws IOException {
    checkRenameConstructorAnnotation("init:(NSString *)s offset:(int)n");
  }

  public void testRenameConstructorAnnotationWithSelector() throws IOException {
    checkRenameConstructorAnnotation("init:offset:");
  }

  public void checkRenameConstructorAnnotation(String objcName) throws IOException {
    addSourceFile("public class A { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"" + objcName + "\") "
        + "A(String s, int n) {}}", "A.java");
    String translation = translateSourceFile("A", "A.h");
    assertTranslatedLines(translation,
        "- (instancetype)init:(NSString *)s",
        "offset:(jint)n;");
    assertNotInTranslation(translation, "testWithNSString");
    translation = getTranslatedFile("A.m");
    assertTranslatedLines(translation,
        "- (instancetype)init:(NSString *)s",
        "offset:(jint)n {");
    assertNotInTranslation(translation, "testWithNSString");

    // Test invocation of renamed constructor.
    translation = translateSourceFile(
        "class Test { A test() { return new A(\"foo\", 5); } }", "Test", "Test.m");
    assertTranslation(translation, "return [new_A_init_offset_(@\"foo\", 5) autorelease];");
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
        + "@com.google.j2objc.annotations.ObjectiveCName(\"foo:bar:\")"
        + "void test(String s, int n) {}"
        + "static class B extends A { "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"test:offset:\")"
        + "@Override void test(String s, int n) {}}}", "A", "A.m");
    assertWarningCount(1);
  }

  // Verify enum constant names are not modified, even if they use a reserved word.
  // This is necessary for compatibility with proto compiler output.
  public void testGetReservedEnumConstantName() throws IOException {
    String translation = translateSourceFile("enum E { HUGE }", "E", "E.h");
    assertTranslation(translation, "HUGE");
    assertNotInTranslation(translation, "HUGE_");
  }

  public void testRenamePackageAnnotation() throws IOException {
    addSourcesToSourcepaths();
    addSourceFile("@com.google.j2objc.annotations.ObjectiveCName(\"FB\") "
        + "package foo.bar;", "foo/bar/package-info.java");
    addSourceFile("package foo.bar; public class Test {}", "foo/bar/Test.java");
    String translation = translateSourceFile("foo.bar.Test", "foo/bar/Test.h");
    assertTranslation(translation, "@interface FBTest : NSObject");
    assertTranslation(translation, "J2OBJC_EMPTY_STATIC_INIT(FBTest)");
    assertTranslation(translation, "typedef FBTest FooBarTest;");

    translation = getTranslatedFile("foo/bar/Test.m");
    assertTranslation(translation, "#include \"foo/bar/Test.h\""); // should be full path.
    assertTranslation(translation, "@implementation FBTest");
    assertTranslation(translation, "J2ObjcClassInfo _FBTest = { 2, \"Test\", \"foo.bar\", ");
  }

  public void testRenamePackageAnnotationEnum() throws IOException {
    addSourcesToSourcepaths();
    addSourceFile("@com.google.j2objc.annotations.ObjectiveCName(\"FB\") "
        + "package foo.bar;", "foo/bar/package-info.java");
    addSourceFile("package foo.bar; public enum Test { FOO, BAR }", "foo/bar/Test.java");
    String translation = translateSourceFile("foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(translation,
        "typedef NS_ENUM(NSUInteger, FBTest) {", "FBTest_FOO = 0,", "FBTest_BAR = 1,", "};");
    assertTranslation(translation, "@interface FBTestEnum : JavaLangEnum");
    assertTranslation(translation, "FBTestEnum_values()");
    assertTranslation(translation, "+ (FBTestEnum *)valueOfWithNSString:(NSString *)name;");
    assertTranslation(translation, "FBTestEnum *FBTestEnum_valueOfWithNSString_");
    assertTranslation(translation, "J2OBJC_STATIC_INIT(FBTestEnum");
    assertTranslation(translation, "typedef FBTestEnum FooBarTestEnum;");

    translation = getTranslatedFile("foo/bar/Test.m");
    assertTranslation(translation, "#include \"foo/bar/Test.h\""); // should be full path.
    assertTranslation(translation, "@implementation FBTestEnum");
    assertTranslation(translation, "J2ObjcClassInfo _FBTestEnum = { 2, \"Test\", \"foo.bar\", ");

    // Make sure package-info class doesn't use prefix for its own type name.
    translation = translateSourceFile("foo.bar.package-info", "foo/bar/package-info.m");
    assertTranslation(translation, "@interface FooBarpackage_info");
    assertTranslation(translation, "@implementation FooBarpackage_info");
    assertNotInTranslation(translation, "FBpackage_info");
  }
}
