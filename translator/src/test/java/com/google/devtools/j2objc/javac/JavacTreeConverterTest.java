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

package com.google.devtools.j2objc.javac;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

/**
 * Tests for {@link TreeConverter}.
 */
public class JavacTreeConverterTest extends GenerationTest {

  public void testThisDotConstant() throws IOException {
    // Verify this.CONSTANT translates without a ClassCastException thrown.
    String translation = translateSourceFile("package foo.bar;"
        + "class Test { static final int T_ENUM = 11;"
        + "int test() { return this.T_ENUM; }}", "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(translation, "#define FooBarTest_T_ENUM 11");
  }

  public void testThisDotFinal() throws IOException {
    // Verify this.finalField translates without a ClassCastException thrown.
    String translation = translateSourceFile("class Test { final int slotsize = 4;"
        + "Test(int slotsize) {"
        + " if (this.slotsize < slotsize) { throw new AssertionError(); }}}",
        "Test", "Test.m");
    assertTranslation(translation, "if (Test_slotsize < slotsizeArg)");
  }

  public void testConstantFromMethodInvocation() throws IOException {
    String translation = translateSourceFile("class Test { "
        + "int sizeOfInt() { return Integer.valueOf(42).SIZE; }}", "Test", "Test.m");
    assertTranslation(translation,
        "return (JavaLangInteger_valueOfWithInt_(42), JavaLangInteger_SIZE);");
  }

  // javac qualifies members imported via non-canonical static imports by the
  // the type of the import, not the type the member is declared in.
  // In the example below, FOO's enclosing element is B rather than A.
  // See: https://bugs.openjdk.java.net/browse/JDK-6225935.
  public void testIncorrectEnclosingElementBug_staticOnDemandImport() throws IOException {
    addSourceFile(
        "package foo; public class A { "
        + "public static final int FOO = 1; public static void bar() {} }", "foo/A.java");
    addSourceFile("package foo; public class B extends A {}", "foo/B.java");
    String translation = translateSourceFile(
        "import static foo.B.*; class Test { void test() { int i = FOO; bar(); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "jint i = FooA_FOO;",
        "FooA_bar();");
  }

  public void testIncorrectEnclosingElementBug_staticNamedImport() throws IOException {
    addSourceFile(
        "package foo; public class A { "
            + "public static final int FOO = 1; public static void bar() {} }",
        "foo/A.java");
    addSourceFile("package foo; public class B extends A {}", "foo/B.java");
    String translation =
        translateSourceFile(
            "import static foo.B.FOO; import static foo.B.bar;"
                + "class Test { void test() { int i = FOO; bar(); } }",
            "Test",
            "Test.m");
    assertTranslatedLines(translation, "jint i = FooA_FOO;", "FooA_bar();");
  }
}
