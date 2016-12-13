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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.util.CodeReferenceMap;

import java.io.IOException;

/**
 * Unit tests for DeadCodeEliminator.
 *
 * @author Daniel Connelly
 */
public class DeadCodeEliminatorTest extends GenerationTest {

  public void testDeadMethod() throws IOException {
    String source = "class A {\n"
        + "  private static interface B {\n"
        + "    String bar();\n"
        + "  }\n"
        + "  private void baz() {\n"
        + "    // nothing\n"
        + "  }\n"
        + "}\n";
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addMethod("A$B", "bar", "()Ljava/lang/String;")
        .build();
    setDeadCodeMap(map);
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "@interface A_B");
    assertNotInTranslation(translation, "bar");
    assertTranslation(translation, "baz");
  }

  public void testDeadMethod_AnonymousClassMember() throws IOException {
    String source = "abstract class B {}\n"
        + "class A {\n"
        + "  private B b = new B() {\n"
        + "    public void foo() {}\n"
        + "  };\n"
        + "}\n";
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addMethod("A$1", "foo", "()V")
        .build();
    setDeadCodeMap(map);
    String translation = translateSourceFile(source, "A", "A.m");
    assertNotInTranslation(translation, "foo");
  }

  public void testDeadMethod_InnerClassConstructor() throws IOException {
    String source = "class A {\n"
        + "  class B {\n"
        + "    B(int i) {}\n"
        + "  }\n"
        + "}\n";
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addMethod("A$B", "A$B", "(LA;I)V")
        .build();
    setDeadCodeMap(map);
    String translation = translateSourceFile(source, "A", "A.m");
    assertNotInTranslation(translation, "withInt");
  }

  public void testDeadFields() throws IOException {
    String source = "import static java.lang.System.out;\n"
        + "import static java.lang.System.in;\n"
        + "class A {\n"
        + "  private static final int foo = 1;\n"
        + "  public static final String bar = \"bar\";\n"
        + "  static final double pi = 3.2; // in Indiana only\n"
        + "  final String baz = null, bah = \"123\";\n"
        + "  private int abc = 9;\n"
        + "}\n";
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addField("A", "foo")
        .addField("A", "baz")
        .addField("java.lang.System", "in")
        .build();
    setDeadCodeMap(map);
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "#define A_pi 3.2");
    assertTranslation(translation, "NSString *bah_;");
    assertNotInTranslation(translation, "baz");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "#define A_foo 1");
    assertTranslation(translation, "NSString *A_bar = @\"bar\";");
    assertTranslation(translation, "abc_ = 9;");
    assertTranslation(translation, "JreStrongAssign(&self->bah_, @\"123\");");
    assertNotInTranslation(translation, "baz");
  }

  public void testDeadInitializer() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("A").build();
    setDeadCodeMap(map);
    String source = "class A {\n"
        + "  static final int baz = 9;\n"
        + "  static { System.out.println(\"foo\"); }\n"
        + "  { System.out.println(\"bar\"); }\n"
        + "}\n";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "#define A_baz 9");
    translation = getTranslatedFile("A.m");
    assertNotInTranslation(translation, "println");
    assertNotInTranslation(translation, "initialize");
    assertNotInTranslation(translation, "foo");
    assertNotInTranslation(translation, "bar");
  }

  public void testDeadEnum() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("A$Thing")
        .build();
    setDeadCodeMap(map);
    String source = "class A {\n"
        + "  private static void foo() {}\n"
        + "  public enum Thing implements java.io.Serializable {\n"
        + "    THING1(27),\n"
        + "    THING2(89) { void bar() {} },\n"
        + "    THING3 { void bar() { foo(); } };\n"
        + "    private Thing(int x) {}\n"
        + "    private Thing() {}\n"
        + "  }\n"
        + "}\n";
    String translation = translateSourceFile(source, "A", "A.m");
    assertNotInTranslation(translation, "initWithInt");
    assertNotInTranslation(translation, "THING1");
    assertNotInTranslation(translation, "THING2");
    assertNotInTranslation(translation, "THING3");
    String header = getTranslatedFile("A.h");
    assertNotInTranslation(header, "Serializable");
  }

  public void testConstructorGeneration() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("A")
        .addMethod("C", "<init>", "(I)V")
        .build();
    setDeadCodeMap(map);
    addSourceFile("class B {\n"
        + "  public B(int x, boolean y, String z, java.util.List w) {}\n"
        + "}", "B.java");
    String translation = translateSourceFile("class A extends B {\n"
        + "  public A(int i) { super(i, true, \"foo\", new java.util.ArrayList()); }\n"
        + "}\n", "A", "A.m");
    assertNotInTranslation(translation, "initWithInt");
    assertNotInTranslation(translation, "B_init");
    translation = translateSourceFile("class C extends B {\n"
        + "  public C(int i) { super(i, true, \"foo\", new java.util.ArrayList()); }\n"
        + "}\n", "C", "C.m");
    assertNotInTranslation(translation, "initWithInt");
    assertNotInTranslation(translation, "B_init");
  }

  public void testDeadClass_FieldRemoval() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("Foo")
        .build();
    setDeadCodeMap(map);
    String source = "class Foo {\n"
        + "  static final int x = f();\n"
        + "  static final int y = 0;\n"
        + "  static int f() { return 0; }\n"
        + "}\n";
    String translation = translateSourceFile(source, "Foo", "Foo.h");
    assertTranslation(translation, "#define Foo_y 0");
    translation = getTranslatedFile("Foo.m");
    assertNotInTranslation(translation, "jint Foo_x_");
    assertNotInTranslation(translation, "Foo_x_ = Foo_f()");
    assertNotInTranslation(translation, "+ (jint)f");
    assertNotInTranslation(translation, "bar");
  }

  public void testDeadClass_StaticNestedClass() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("Foo")
        .build();
    setDeadCodeMap(map);
    String source = "class Foo {\n"
        + "  static class Bar {}\n"
        + "}\n"
        + "class Baz extends Foo.Bar {\n"
        + "}\n";
    String translation = translateSourceFile(source, "Foo", "Foo.h");
    assertTranslation(translation, "@interface Foo_Bar : NSObject");
    translation = getTranslatedFile("Foo.m");
    assertTranslation(translation, "Foo_Bar_init");
  }

  public void testDeadClass_DeadStaticNestedClass() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("Foo")
        .addClass("Foo$Bar")
        .addMethod("Foo$Baz", "g", "()V")
        .build();
    setDeadCodeMap(map);
    String source = "class Foo {\n"
        + "  static class Bar { void f() {} }\n"
        + "  static class Baz { void g() {} }\n"
        + "}\n";
    String translation = translateSourceFile(source, "Foo", "Foo.h");
    assertTranslation(translation, "@interface Foo_Bar");
    assertNotInTranslation(translation, "- (void)f");
    assertTranslation(translation, "@interface Foo_Baz");
    assertNotInTranslation(translation, "- (void)g");
    translation = getTranslatedFile("Foo.m");
    assertNotInTranslation(translation, "Foo_Bar_init");
    assertNotInTranslation(translation, "- (void)f");
    assertTranslation(translation, "Foo_Baz_init");
    assertNotInTranslation(translation, "- (void)g");
  }

  public void testDeadClass_DeadInnerClassConstructor() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addField("Foo$A", "z")
        .addField("Foo$A", "this$0")
        .addMethod("Foo$A", "Foo$A", "(LFoo;I)V")
        .addMethod("Foo$A", "f", "()I")
        .build();
    setDeadCodeMap(map);
    String source = "public class Foo {\n"
        + "  int y;\n"
        + "  public Foo(int x) { y = x; }\n"
        + "\n"
        + "  class A {\n"
        + "    int z;\n"
        + "    A(int x) { z = x; }\n"
        + "    int f() { return z + y; }\n"
        + "  }\n"
        + "}\n";
    String translation = translateSourceFile(source, "Foo", "Foo.h");
    assertTranslation(translation, "@interface Foo_A");
    assertNotInTranslation(translation, "z_;");
    translation = getTranslatedFile("Foo.m");
    assertNotInTranslation(translation, "Foo *this$0_;");
    assertNotInTranslation(translation, "JreStrongAssign(&self->this$0_, outer$");
    assertNotInTranslation(translation, "self->z_ = x;");
    assertNotInTranslation(translation, "- (jint)f");
  }

  public void testDeadClass_SupertypeRemoval() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("Foo")
        .build();
    setDeadCodeMap(map);
    addSourceFile("class SuperClass {}", "SuperClass.java");
    addSourceFile("interface SuperI { void f(); }", "SuperI.java");
    String source = "class Foo extends SuperClass implements SuperI {\n"
        + "  public void f() {}\n"
        + "}\n";
    String translation = translateSourceFile(source, "Foo", "Foo.h");
    assertNotInTranslation(translation, "SuperClass");
    assertNotInTranslation(translation, "SuperI");
  }

  // Verify that annotation bodies aren't stripped when specified in a dead code report.
  public void testDeadAnnotation() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("Foo")
        .build();
    setDeadCodeMap(map);
    String source = "import java.lang.annotation.Retention;\n"
        + "import java.lang.annotation.RetentionPolicy;\n"
        + "@Retention(RetentionPolicy.RUNTIME)\n"
        + "public @interface Foo {\n"
        + "  String value() default \"\";\n"
        + "}\n";
    String translation = translateSourceFile(source, "Foo", "Foo.h");
    assertTranslation(translation, "@property (readonly) NSString *value;");
  }

  public void testDeadDefaultConstructor() throws IOException {
    CodeReferenceMap map = CodeReferenceMap.builder()
        .addMethod("Test", "<init>", "()V")
        .build();
    setDeadCodeMap(map);
    String translation = translateSourceFile("class Test {}", "Test", "Test.h");
    // Make sure the default constructor is not added.
    assertNotInTranslation(translation, "init");
  }

  public void testDeadFastEnumerationImplementation() throws IOException {
    String source = "import java.util.Iterator;\n"
        + "interface SomeInterface extends Iterable<String> {}\n"
        + "class Base {}\n"
        + "class Foo extends Base implements SomeInterface {\n"
        + "  @Override\n"
        + "  public Iterator<String> iterator() { return null; }\n"
        + "}";

    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("Foo")
        .build();
    setDeadCodeMap(map);

    String header = translateSourceFile(source, "Foo", "Foo.h");
    String impl = getTranslatedFile("Foo.m");

    assertNotInTranslation(header, "@interface Foo : Base < SomeInterface >");
    assertTranslation(header, "@interface Foo : NSObject");
    assertNotInTranslation(impl, "countByEnumeratingWithState:");
  }

  public void testTypeNarrowingMethodsNotShowingInDeadClasses() throws IOException {
    String source = "class Base<T> { \n"
        + "  T someMethod() { return null; }\n"
        + "}\n"
        + "class Foo extends Base<String> {}";

    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("Foo")
        .build();
    setDeadCodeMap(map);

    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertNotInTranslation(header, "@interface Foo : Base");
    assertTranslation(header, "@interface Foo : NSObject");
    assertTranslation(header, "- (id)someMethod;");
    assertNotInTranslation(header, "- (NSString *)someMethod;");
  }

  public void testDeadMethodInBaseClassNotShowingInChildClasses() throws IOException {
    String source = "class Base<T> { \n"
        + "  T someDeadMethod() { return null; }\n"
        + "}\n"
        + "class Foo extends Base<String> {}";

    CodeReferenceMap map = CodeReferenceMap.builder()
        .addMethod("Base", "someDeadMethod", "()Ljava/lang/Object;")
        .build();
    setDeadCodeMap(map);

    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertNotInTranslation(header, "- (id)someDeadMethod;");
    assertNotInTranslation(header, "- (NSString *)someDeadMethod;");
  }

  public void testDeadMethodInBaseClassNotShowingInDeadChildClasses() throws IOException {
    String source = "class Base<T> { \n"
        + "  T someDeadMethod() { return null; }\n"
        + "}\n"
        + "class Foo extends Base<String> {}";

    CodeReferenceMap map = CodeReferenceMap.builder()
        .addClass("Foo")
        .addMethod("Base", "someDeadMethod", "()Ljava/lang/Object;")
        .build();
    setDeadCodeMap(map);

    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertNotInTranslation(header, "- (id)someDeadMethod;");
    assertNotInTranslation(header, "- (NSString *)someDeadMethod;");
  }
}
