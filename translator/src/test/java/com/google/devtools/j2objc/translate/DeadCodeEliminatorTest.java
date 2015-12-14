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
import com.google.devtools.j2objc.util.DeadCodeMap;

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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A$B", "bar", "()Ljava/lang/String;")
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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A$1", "foo", "()V")
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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A$B", "A$B", "(LA;I)V")
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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadField("A", "foo")
        .addDeadField("A", "baz")
        .addDeadField("java.lang.System", "in")
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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("A").build();
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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("A$Thing")
        .build();
    setDeadCodeMap(map);
    String source = "class A {\n"
        + "  private static void foo() {}\n"
        + "  public enum Thing {\n"
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
  }

  public void testConstructorGeneration() throws IOException {
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("A")
        .addDeadMethod("C", "C", "(I)V")
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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("Foo")
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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("Foo")
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
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("Foo")
        .addDeadClass("Foo$Bar")
        .addDeadMethod("Foo$Baz", "g", "()V")
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
    assertTranslation(translation, "Foo_Bar_init");
    assertNotInTranslation(translation, "- (void)f");
    assertTranslation(translation, "Foo_Baz_init");
    assertNotInTranslation(translation, "- (void)g");
  }

  public void testDeadClass_DeadInnerClassConstructor() throws IOException {
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadField("Foo$A", "z")
        .addDeadField("Foo$A", "this$0")
        .addDeadMethod("Foo$A", "Foo$A", "(LFoo;I)V")
        .addDeadMethod("Foo$A", "f", "()I")
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
}
