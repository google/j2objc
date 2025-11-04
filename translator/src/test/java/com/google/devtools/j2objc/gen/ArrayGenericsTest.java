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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

public class ArrayGenericsTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
    options.setAsObjCGenericDecl(true);
  }

  public void testObjectArrayGenerics() throws IOException {
    addSourceFile("class A { class Inner { } }", "A.java");
    addSourceFile(
        "class Test {"
            + "  A[] testObjectArrayGenerics(A[] a_array) {"
            + "    return a_array;"
            + "  }"
            + "}",
        "Test.java");
    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertInTranslation(
        testHeader,
        "(IOSObjectArray<A *> *)testObjectArrayGenericsWithAArray:(IOSObjectArray<A *> *)a_array");
    assertInTranslation(
        testSource,
        "(IOSObjectArray *)testObjectArrayGenericsWithAArray:(IOSObjectArray *)a_array");
  }

  public void testObjectArrayGenericsMultiDimensions() throws IOException {
    addSourceFile("class A { class Inner { } }", "A.java");
    addSourceFile(
        "class Test {"
            + "  A[][] testObjectArrayGenerics(A[][] a_array) {"
            + "    return a_array;"
            + "  }"
            + "}",
        "Test.java");
    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertInTranslation(
        testHeader,
        "(IOSObjectArray<IOSObjectArray<A *> *>"
            + " *)testObjectArrayGenericsWithAArray2:(IOSObjectArray<IOSObjectArray<A *> *>"
            + " *)a_array");
    assertInTranslation(
        testSource,
        "(IOSObjectArray *)testObjectArrayGenericsWithAArray2:(IOSObjectArray *)a_array ");
  }

  public void testInterfaceArrayGenerics() throws IOException {
    addSourceFile(
        "class Test {"
            + "  interface Interface {}"
            + "  Interface[] testInterfaceArrayGenerics(Interface[] faces) {"
            + "    return faces;"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertInTranslation(
        testHeader,
        "(IOSObjectArray<id<Test_Interface>>"
            + " *)testInterfaceArrayGenericsWithTest_InterfaceArray:(IOSObjectArray<id<Test_Interface>>"
            + " *)faces");
    assertInTranslation(
        testSource,
        "(IOSObjectArray *)testInterfaceArrayGenericsWithTest_InterfaceArray:(IOSObjectArray"
            + " *)faces");
  }

  public void testPrimitiveTypeArrayGenerics() throws IOException {
    addSourceFile(
        "class Test {"
            + "  int[] testIntegerArrayGenerics(int[] ints) {"
            + "    return ints;"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertInTranslation(
        testHeader, "(IOSIntArray *)testIntegerArrayGenericsWithIntArray:(IOSIntArray *)ints");

    assertInTranslation(
        testSource, "(IOSIntArray *)testIntegerArrayGenericsWithIntArray:(IOSIntArray *)ints");
  }

  public void testPrimitiveTypeArrayGenericsMultiDimensions() throws IOException {
    addSourceFile(
        "class Test {"
            + "  int[][] testObjectArrayGenerics(int[][] a_array) {"
            + "    return a_array;"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertInTranslation(
        testHeader,
        "(IOSObjectArray<IOSIntArray *>"
            + " *)testObjectArrayGenericsWithIntArray2:(IOSObjectArray<IOSIntArray *> *)a_array");
    assertInTranslation(
        testSource,
        "(IOSObjectArray *)testObjectArrayGenericsWithIntArray2:(IOSObjectArray *)a_array");
  }

  public void testStaticArrayVariableGenerics() throws IOException {
    String translation =
        translateSourceFile(
            "class Test {"
                + " static String[] array; "
                + " static { "
                + "   array = new String[2]; "
                + "   array[0] = \"Hello\"; "
                + "   array[1] = \"World\"; "
                + " } "
                + "}",
            "Test",
            "Test.m");
    // Static variables will be declared in the .m file.
    assertInTranslation(translation, "IOSObjectArray *Test_array");
  }

  public void testAnnotatedArrayParameter() throws IOException {
    String translation =
        translateSourceFile(
            "import javax.annotation.Nonnull;"
                + "public class Test {"
                + "  public Test(Test copyFrom, @Nonnull String[] names) {}"
                + "}",
            "Test",
            "Test.h");
    assertInTranslation(translation, "(IOSObjectArray<NSString *> *)names");
  }

  // Verify that there's a forward declaration for an array component type.
  public void testGenericArrayComponentForwardDecl() throws IOException {
    String translation =
        translateSourceFile(
            "public class Test {"
                + "  public StackTraceElement[] stackTrace() {"
                + "    return Thread.currentThread().getStackTrace();"
                + "  }"
                + "}",
            "Test",
            "Test.h");
    assertInTranslation(translation, "@class JavaLangStackTraceElement;");
  }

  public void testGenericPrimitiveArrayTypeForwardDecl() throws IOException {
    String translation =
        translateSourceFile(
            "public abstract class Test {"
                + "  abstract boolean[] readBooleans(); "
                + "  abstract byte[] readBytes(); "
                + "  abstract char[] readChars(); "
                + "  abstract double[] readDoubles(); "
                + "  abstract float[] readFloats(); "
                + "  abstract int[] readInts(); "
                + "  abstract long[] readLongs(); "
                + "  abstract short[] readShorts(); "
                + "}",
            "Test",
            "Test.h");
    assertInTranslation(translation, "@class JavaLangBoolean;");
    assertInTranslation(translation, "@class JavaLangByte;");
    assertInTranslation(translation, "@class JavaLangCharacter;");
    assertInTranslation(translation, "@class JavaLangDouble;");
    assertInTranslation(translation, "@class JavaLangFloat;");
    assertInTranslation(translation, "@class JavaLangInteger;");
    assertInTranslation(translation, "@class JavaLangLong;");
    assertInTranslation(translation, "@class JavaLangShort;");
  }

  public void testGenericArrayInterfaceOrObjectComponent() throws IOException {
    String translation =
        translateSourceFile(
            "public class Test {"
                + "  public void test(java.util.List[] listArgs) {}"
                + "  public void test2(Object[] objectArgs) {}"
                + "}",
            "Test",
            "Test.h");
    assertInTranslation(translation, "(IOSObjectArray<id<JavaUtilList>> *)listArgs");
    assertInTranslation(translation, "(IOSObjectArray<id> *)objectArgs");
  }
}
