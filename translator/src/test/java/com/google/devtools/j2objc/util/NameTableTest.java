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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Unit tests for {@link NameTable}.
 *
 * @author Tom Ball
 */
public class NameTableTest extends GenerationTest {

  // Verify class name without package is unchanged.
  public void testGetFullNameNoPackage() {
    String source = "public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getEnv().nameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    assertEquals("SomeClass", nameTable.getFullName(decl.getTypeElement()));
  }

  // Verify class name with package is camel-cased.
  public void testGetFullNameWithPackage() {
    String source = "package foo.bar; public class SomeClass {}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getEnv().nameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    assertEquals("FooBarSomeClass", nameTable.getFullName(decl.getTypeElement()));
  }

  // Verify inner class name with package is camel-cased.
  public void testGetFullNameWithInnerClass() {
    String source = "package foo.bar; public class SomeClass { static class Inner {}}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getEnv().nameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    assertEquals("FooBarSomeClass_Inner", nameTable.getFullName(decl.getTypeElement()));
  }

  // Verify the name of an inner class of an enum.
  public void testGetFullNameEnumWithInnerClasses() {
    String source = "package foo.bar; "
        + "public enum SomeClass { A; static class Inner {} static enum Inner2 { B; }}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getEnv().nameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    // Outer type should not have "Enum" added to name.
    assertEquals("FooBarSomeClass_Inner", nameTable.getFullName(decl.getTypeElement()));
    // Inner enum should have "Enum" added to name.
    decl = unit.getTypes().get(2);
    assertEquals("FooBarSomeClass_Inner2", nameTable.getFullName(decl.getTypeElement()));
  }

  // Verify local class name.
  public void testGetFullNameWithLocalClass() {
    String source = "package foo.bar; class SomeClass { void test() { "
        // Put each Foo in a separate scope, so leading index number changes.
        // This matches JVM naming, once '$' is substituted for the '_' characters.
        + "{ class Foo {}} { class Foo {}}}}";
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getEnv().nameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    assertEquals("FooBarSomeClass_1Foo", nameTable.getFullName(decl.getTypeElement()));
    decl = unit.getTypes().get(2);
    assertEquals("FooBarSomeClass_2Foo", nameTable.getFullName(decl.getTypeElement()));
  }

  public void testTypeVariableWithTypeVariableBounds() {
    String source = "class A<T> { <E extends T> void foo(E e) {} }";
    CompilationUnit unit = translateType("A", source);
    NameTable nameTable = unit.getEnv().nameTable();
    final ExecutableElement[] methodElement = new ExecutableElement[1];
    unit.accept(new TreeVisitor() {
      @Override public void endVisit(MethodDeclaration node) {
        ExecutableElement element = node.getExecutableElement();
        if (ElementUtil.getName(element).equals("foo")) {
          methodElement[0] = element;
        }
      }
    });
    assertNotNull(methodElement[0]);
    TypeMirror paramType = methodElement[0].getParameters().get(0).asType();
    assertEquals("id", nameTable.getObjCType(paramType));
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
    addSourceFile("package foo; "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"TestName\") "
        + "public class A { public static void test() {} "
        + "@com.google.j2objc.annotations.ObjectiveCName(\"TheInner\") "
        + "public static class C { public static void test2() {} } }", "foo/A.java");
    addSourceFile(
        "public class B { void test() { foo.A.test(); foo.A.C.test2(); }}", "B.java");
    String translation = translateSourceFile("foo.A", "foo/A.h");
    assertTranslation(translation, "@interface TestName : NSObject");
    assertTranslation(translation, "@interface TheInner : NSObject");
    translation = translateSourceFile("B", "B.m");
    assertTranslatedLines(translation, "TestName_test();", "TheInner_test2();");
  }

  public void testRenameMapping() throws IOException {
    options.getMappings().addClass("foo.bar.A",  "Test2Name");
    addSourceFile("package foo.bar; public class A { static void test() {}}", "foo/bar/A.java");
    addSourceFile("package foo.bar; public class B { void test() { A.test(); }}", "foo/bar/B.java");
    String translation = translateSourceFile("foo.bar.A", "foo/bar/A.h");
    assertTranslation(translation, "@interface Test2Name : NSObject");
    translation = translateSourceFile("foo.bar.B", "foo/bar/B.m");
    assertTranslation(translation, "Test2Name_test();");
  }

  public void testRenameMethodAnnotation() throws IOException {
    String objcName = "test:offset:";
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

  public void testRenameConstructorAnnotation() throws IOException {
    String objcName = "init:offset:";
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
    assertTranslation(translation, "return create_A_init_offset_(@\"foo\", 5);");
  }

  public void testRenamePackagePrivateClassConstructor() throws IOException {
    String translation = translateSourceFile("package foo.bar; class Test { Test(int unused) {} }",
        "foo.bar.Test", "foo/bar/Test.h");
    assertTranslation(translation, "initPackagePrivateWithInt_");
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
    assertTranslation(translation, "@compatibility_alias FooBarTest FBTest;");

    translation = getTranslatedFile("foo/bar/Test.m");
    assertTranslation(translation, "#include \"foo/bar/Test.h\""); // should be full path.
    assertTranslation(translation, "@implementation FBTest");
    assertTranslation(translation, "J2ObjcClassInfo _FBTest = { \"Test\", \"foo.bar\", ");
  }

  public void testRenamePackageAnnotationEnum() throws IOException {
    addSourcesToSourcepaths();
    addSourceFile("@com.google.j2objc.annotations.ObjectiveCName(\"FB\") "
        + "package foo.bar;", "foo/bar/package-info.java");
    addSourceFile("package foo.bar; public enum Test { FOO, BAR }", "foo/bar/Test.java");
    String translation = translateSourceFile("foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(translation,
        "typedef NS_ENUM(NSUInteger, FBTest_Enum) {",
        "  FBTest_Enum_FOO = 0,",
        "  FBTest_Enum_BAR = 1,",
        "};");
    assertTranslation(translation, "@interface FBTest : JavaLangEnum");
    assertTranslation(translation, "FBTest_values(void);");
    assertTranslation(translation, "+ (FBTest *)valueOfWithNSString:(NSString *)name;");
    assertTranslation(translation, "FBTest *FBTest_valueOfWithNSString_");
    assertTranslation(translation, "J2OBJC_STATIC_INIT(FBTest)");
    assertTranslation(translation, "@compatibility_alias FooBarTest FBTest;");

    translation = getTranslatedFile("foo/bar/Test.m");
    assertTranslation(translation, "#include \"foo/bar/Test.h\""); // should be full path.
    assertTranslation(translation, "@implementation FBTest");
    assertTranslation(translation, "J2ObjcClassInfo _FBTest = { \"Test\", \"foo.bar\", ");

    // Make sure package-info class doesn't use prefix for its own type name.
    translation = translateSourceFile("foo.bar.package-info", "foo/bar/package-info.m");
    assertTranslation(translation, "@interface FooBarpackage_info");
    assertTranslation(translation, "@implementation FooBarpackage_info");
    assertNotInTranslation(translation, "FBpackage_info");
  }

  public void testIsValidClassName() {
    assertTrue(NameTable.isValidClassName("Test"));
    assertTrue(NameTable.isValidClassName("foo.bar.Test"));
    assertTrue(NameTable.isValidClassName("foo.bar.Test.InnerClass"));
    assertTrue(NameTable.isValidClassName("foo.bar.Test$InnerClass"));

    // A package name can also be a class name.
    assertTrue(NameTable.isValidClassName("java.util"));

    // Unicode names are valid package or class names.
    assertTrue(NameTable.isValidClassName("数据.字符串"));

    // File names without path separators are valid!
    assertTrue(NameTable.isValidClassName("Test.java"));

    assertFalse(NameTable.isValidClassName("foo/bar/Test.java"));
    assertFalse(NameTable.isValidClassName("test-src.jar"));
  }

  public void testUserDefinedReservedNames() throws IOException {
    String file = addSourceFile("aReservedMethodName aReservedParamName", "user_defined.txt");
    options.load(new String[] {"--reserved-names", file});
    // stdin is a predefined reserved name.
    String source = "public class Test { "
        + "  public void aReservedMethodName() {} "
        + "  public void test(int noReserved, int stdin, int aReservedParamName) {} "
        + "} ";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "- (void)aReservedMethodName__;");
    assertTranslatedLines(translation,
        "- (void)testWithInt:(jint)noReserved",
        "            withInt:(jint)stdin_",
        "            withInt:(jint)aReservedParamName_;");
  }
}
