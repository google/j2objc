/*
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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;

import java.io.IOException;

/**
 * Unit tests for {@link MetadataWriter}.
 *
 * @author Keith Stanger
 */
public class MetadataWriterTest extends GenerationTest {

  public void testMetadataHeaderGeneration() throws IOException {
    String translation = translateSourceFile("package foo; class Test {}", "Test", "foo/Test.m");
    assertTranslation(translation, "+ (const J2ObjcClassInfo *)__metadata");
    assertTranslation(translation,
        "static const J2ObjcClassInfo _FooTest = { \"Test\", \"foo\", NULL, methods, NULL, "
        + Integer.toString(MetadataWriter.METADATA_VERSION));
  }

  public void testConstructorsHaveNullJavaName() throws IOException {
    String translation = translateSourceFile("class Test {}", "Test", "Test.m");
    assertTranslatedLines(translation,
        "static J2ObjcMethodInfo methods[] = {",
        // The fourth field, "javaNameIdx", should be -1.
        "{ NULL, NULL, 0x0, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "methods[0].selector = @selector(init)");
  }

  public void testMethodMetadata() throws IOException {
    String translation = translateSourceFile(
        // Separate methods are used so each only has one modifier.
        "abstract class Test<T> { "
        + " Object test1() { return null; }"  // package-private
        + " private char test2() { return 'a'; }"
        + " protected void test3() { }"
        + " final long test4() { return 0L; }"
        + " synchronized boolean test5() { return false; }"
        + " String test6(String s, Object... args) { return null; }"
        + " native void test7() /*-[ exit(0); ]-*/; "
        + " abstract void test8() throws InterruptedException, Error; "
        + " abstract T test9();"
        + " abstract void test10(int i, T t);"
        + " abstract <V,X> void test11(V one, X two, T three);"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "{ NULL, \"LNSObject;\", 0x0, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"C\", 0x2, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"V\", 0x4, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"J\", 0x10, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"Z\", 0x20, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"LNSString;\", 0x80, 0, 1, -1, -1, -1, -1 }");
    assertTranslation(translation, "{ NULL, \"V\", 0x100, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"V\", 0x400, -1, -1, 2, -1, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"LNSObject;\", 0x400, -1, -1, -1, 3, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"V\", 0x400, 4, 5, -1, 6, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"V\", 0x400, 7, 8, -1, 9, -1, -1 },");
    // Implicit default constructor
    assertTranslation(translation, "methods[0].selector = @selector(init)");
    assertTranslation(translation, "methods[1].selector = @selector(test1)");
    assertTranslation(translation, "methods[2].selector = @selector(test2)");
    assertTranslation(translation, "methods[3].selector = @selector(test3)");
    assertTranslation(translation, "methods[4].selector = @selector(test4)");
    assertTranslation(translation, "methods[5].selector = @selector(test5)");
    assertTranslation(translation,
        "methods[6].selector = @selector(test6WithNSString:withNSObjectArray:)");
    assertTranslation(translation, "methods[7].selector = @selector(test7)");
    assertTranslation(translation, "methods[8].selector = @selector(test8)");
    assertTranslation(translation, "methods[9].selector = @selector(test9)");
    assertTranslation(translation, "methods[10].selector = @selector(test10WithInt:withId:)");
    assertTranslation(translation, "methods[11].selector = @selector(test11WithId:withId:withId:)");

    assertTranslation(translation,
        "static const void *ptrTable[] = { \"test6\", \"LNSString;[LNSObject;\", "
        + "\"LJavaLangInterruptedException;LJavaLangError;\", \"()TT;\", \"test10\", "
        + "\"ILNSObject;\", \"(ITT;)V\", \"test11\", \"LNSObject;LNSObject;LNSObject;\", "
        + "\"<V:Ljava/lang/Object;X:Ljava/lang/Object;>(TV;TX;TT;)V\", "
        + "\"<T:Ljava/lang/Object;>Ljava/lang/Object;\" };");
  }

  public void testFieldMetadata() throws IOException {
    String translation = translateSourceFile(
        "class Test<T extends Runnable> {"
        + "byte field1;"
        + "Object field2;"
        + "T field3;"
        + "}", "Test", "Test.m");
    assertTranslatedLines(translation,
        "static const J2ObjcFieldInfo fields[] = {",
        "  { \"field1_\", \"B\", .constantValue.asLong = 0, 0x0, -1, -1, -1, -1 },",
        "  { \"field2_\", \"LNSObject;\", .constantValue.asLong = 0, 0x0, -1, -1, -1, -1 },",
        "  { \"field3_\", \"LJavaLangRunnable;\", .constantValue.asLong = 0, 0x0, -1, -1, 0, -1 },",
        "};");
    assertTranslation(translation,
        "static const void *ptrTable[] = { \"TT;\", "
        + "\"<T::Ljava/lang/Runnable;>Ljava/lang/Object;\" };");
  }

  public void testAnnotationMetadata() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) @interface Test { "
        + " String foo() default \"bar\";"
        + " int num() default 5;"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "{ NULL, \"LNSString;\", 0x401, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "{ NULL, \"I\", 0x401, -1, -1, -1, -1, -1, -1 },");
    assertTranslation(translation, "methods[0].selector = @selector(foo)");
    assertTranslation(translation, "methods[1].selector = @selector(num)");
  }

  public void testInnerClassesMetadata() throws IOException {
    String translation = translateSourceFile(
        " class A {"
        + "class B {"
        + "  class InnerInner{}}"
        + "static class C {"
        + "  Runnable test() {"
        + "    return new Runnable() { public void run() {}};}}"
        + "interface D {}"
        + "@interface E {}"
        + "}"
        , "A", "A.m");
    assertTranslation(translation, "static const void *ptrTable[] = { \"LA_B;LA_C;LA_D;LA_E;\" };");
    assertTranslation(translation,
        "static const J2ObjcClassInfo _A = { \"A\", NULL, ptrTable, methods, NULL, 7, 0x0, 1, 0, "
        + "-1, 0, -1, -1, -1 };");
  }

  public void testEnclosingMethodAndConstructor() throws IOException {
    String translation = translateSourceFile(
        "class A { A(String s) { class B {}} void test(int i, long l) { class C { class D {}}}}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "static const void *ptrTable[] = { \"LA;\", \"initWithNSString:\" };",
        "static const J2ObjcClassInfo _A_1B = { \"B\", NULL, ptrTable, methods, NULL, 7, 0x0, 1, "
        + "0, 0, -1, 1, -1, -1 };");
    assertTranslatedLines(translation,
        "static const void *ptrTable[] = { \"LA;\", \"LA_1C_D;\", \"testWithInt:withLong:\" };",
        "static const J2ObjcClassInfo _A_1C = { \"C\", NULL, ptrTable, methods, NULL, 7, 0x0, 1, "
        + "0, 0, 1, 2, -1, -1 };");

    // Verify D is not enclosed by test(), as it's enclosed by C.
    assertTranslatedLines(translation,
        "static const void *ptrTable[] = { \"LA_1C;\" };",
        "static const J2ObjcClassInfo _A_1C_D = { \"D\", NULL, ptrTable, methods, NULL, 7, 0x0, 1, "
        + "0, 0, -1, -1, -1, -1 };");
  }

  public void testMethodAnnotationNoParameters() throws IOException {
    String translation = translateSourceFile(
        "import org.junit.*;"
        + "public class Test { @After void foo() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "IOSObjectArray *Test__Annotations$0() {",
        "return [IOSObjectArray arrayWithObjects:(id[]){ create_OrgJunitAfter() } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testMethodAnnotationWithParameter() throws IOException {
    String translation = translateSourceFile(
        "import org.junit.*;"
        + "public class Test { @After void foo(int i) {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "IOSObjectArray *Test__Annotations$0() {",
        "return [IOSObjectArray arrayWithObjects:(id[]){ create_OrgJunitAfter() } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testConstructorAnnotationNoParameters() throws IOException {
    String translation = translateSourceFile(
        "public class Test { @Deprecated Test() {} }",
        "Test", "Test.m");
    assertTranslation(translation, "IOSObjectArray *Test__Annotations$0()");
    assertTranslation(translation, "create_JavaLangDeprecated");
  }

  public void testConstructorAnnotationWithParameter() throws IOException {
    String translation = translateSourceFile(
        "public class Test { @Deprecated Test(int i) {} }",
        "Test", "Test.m");
    assertTranslation(translation, "IOSObjectArray *Test__Annotations$0()");
    assertTranslation(translation, "create_JavaLangDeprecated");
  }

  public void testTypeAnnotationDefaultParameter() throws IOException {
    String translation = translateSourceFile(
        "import org.junit.*;"
        + "@Ignore public class Test { void test() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "IOSObjectArray *Test__Annotations$0() {",
        "return [IOSObjectArray arrayWithObjects:(id[]){ create_OrgJunitIgnore(@\"\") } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testTypeAnnotationWithParameter() throws IOException {
    String translation = translateSourceFile(
        "import org.junit.*;"
        + "@Ignore(\"some \\\"escaped\\n comment\") public class Test { void test() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "IOSObjectArray *Test__Annotations$0() {",
        "return [IOSObjectArray arrayWithObjects:(id[])"
        + "{ create_OrgJunitIgnore(@\"some \\\"escaped\\n comment\") } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  // Verify that a class with an annotation with a reserved name property is
  // created in the __annotations support method with that reserved name in the
  // constructor.
  public void testReservedWordAsAnnotationConstructorParameter() throws IOException {
    String translation = translateSourceFile(
        "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "public @interface Bar { String namespace() default \"\"; } "
        + "@Bar(namespace=\"mynames\") class Test {}",
        "Bar", "foo/Bar.m");
    assertTranslatedLines(translation,
        "IOSObjectArray *FooTest__Annotations$0() {",
        "return [IOSObjectArray arrayWithObjects:(id[]){ create_FooBar(@\"mynames\") } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testOuterAndCaptureFieldsInMetadata() throws IOException {
    String translation = translateSourceFile(
        "class Test { int i; void test(int j) { class Inner { int foo() { return i + j; } } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "static const J2ObjcFieldInfo fields[] = {",
        "  { \"this$0_\", \"LTest;\", .constantValue.asLong = 0, 0x1012, -1, -1, -1, -1 },",
        "  { \"val$j_\", \"I\", .constantValue.asLong = 0, 0x1012, -1, -1, -1, -1 },",
        "};");
  }
}
