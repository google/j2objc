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

public class ArrayGenericTest extends GenerationTest {
  public void testPrimitiveTypeArrayGenerics() throws IOException {
    String translation =
        translateSourceFile(
            "class Test {"
                + "  int[] testIntegerArryGenerics(int[] ints) {"
                + "    return ints;"
                + "  }"
                + "  byte[] testByteArryGenerics(byte[] bytes) {"
                + "    return bytes;"
                + "  }"
                + "  short[] testShortArryGenerics(short[] shorts) {"
                + "    return shorts;"
                + "  }"
                + "  boolean[] testBooleanArryGenerics(boolean[] booleans) {"
                + "    return booleans;"
                + "  }"
                + "  long[] testLongArryGenerics(long[] longs) {"
                + "    return longs;"
                + "  }"
                + "  float[] testFloatArryGenerics(float[] floats) {"
                + "    return floats;"
                + "  }"
                + "  double[] testDoubleArryGenerics(double[] doubles) {"
                + "    return doubles;"
                + "  }"
                + "  char[] testCharArryGenerics(char[] chars) {"
                + "    return chars;"
                + "  }"
                + "}",
            "Test",
            "Test.m");
    // Function signatures.
    // TODO(minsheng): after array generic support, the input & output should be using
    // NSArray<type>.
    assertTranslation(
        translation, "(IOSIntArray *)testIntegerArryGenericsWithIntArray:(IOSIntArray *)ints");
    assertTranslation(
        translation, "(IOSByteArray *)testByteArryGenericsWithByteArray:(IOSByteArray *)bytes");
    assertTranslation(
        translation,
        "(IOSShortArray *)testShortArryGenericsWithShortArray:(IOSShortArray *)shorts");
    assertTranslation(
        translation,
        "(IOSBooleanArray *)testBooleanArryGenericsWithBooleanArray:(IOSBooleanArray *)booleans");
    assertTranslation(
        translation, "(IOSLongArray *)testLongArryGenericsWithLongArray:(IOSLongArray *)longs");
    assertTranslation(
        translation,
        "(IOSFloatArray *)testFloatArryGenericsWithFloatArray:(IOSFloatArray *)floats");
    assertTranslation(
        translation,
        "(IOSDoubleArray *)testDoubleArryGenericsWithDoubleArray:(IOSDoubleArray *)doubles");
    assertTranslation(
        translation, "(IOSCharArray *)testCharArryGenericsWithCharArray:(IOSCharArray *)chars");
  }

  public void testInterfaceArrayGenerics() throws IOException {
    String translation =
        translateSourceFile(
            "class Test {"
                + "  interface Interface {}"
                + "  Interface[] testIntegerArryGenerics(Interface[] faces) {"
                + "    return faces;"
                + "  }"
                + "}",
            "Test",
            "Test.m");
    assertTranslation(
        translation,
        "(IOSObjectArray *)testIntegerArryGenericsWithTest_InterfaceArray:(IOSObjectArray *)faces");
  }

  public void testObjectArrayGenerics() throws IOException {
    addSourceFile("class A { class Inner { } }", "A.java");
    String translation =
        translateSourceFile(
            "class Test {"
                + "  A[] testIntegerArryGenerics(A[] a_array) {"
                + "    return a_array;"
                + "  }"
                + "}",
            "Test",
            "Test.m");
    // Function signatures.
    assertTranslation(
        translation,
        "(IOSObjectArray *)testIntegerArryGenericsWithAArray:(IOSObjectArray *)a_array");
  }
}
