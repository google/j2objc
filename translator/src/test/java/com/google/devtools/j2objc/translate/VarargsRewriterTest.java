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
 * Unit tests for {@link VarargsRewriter} class.
 *
 * @author Tom Ball, Keith Stanger
 */
public class VarargsRewriterTest extends GenerationTest {

  // Issue 360: a null argument for a varargs parameter should not be rewritten
  // as an array.
  public void testNilVarargs() throws IOException {
    String source =
        "public class Test { "
        + "  void foo(char... chars) {}"
        + "  void test() { foo(null); }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "[self fooWithCharArray:nil];");
    assertNotInTranslation(translation,
        "[self fooWithCharArray:[IOSCharArray arrayWithChars:(unichar[]){ nil } count:1]];");
  }

  // Verify that a single object array argument to an object varargs method is passed unchanged.
  public void testObjectArrayVarargs() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(Object[] array) { java.util.Arrays.asList(array); }}",
        "Test", "Test.m");
    assertTranslation(translation, "JavaUtilArrays_asListWithNSObjectArray_(array);");
  }

  // Verify that a single primitive array argument to a primitive varargs method is
  // passed unchanged.
  public void testPrimitiveArrayVarargs() throws IOException {
    String translation = translateSourceFile(
        "class Test { void doVarargs(int... ints) {}"
        + "void test(int[] array) { doVarargs(array); }}",
        "Test", "Test.m");
    assertTranslation(translation, "[self doVarargsWithIntArray:array];");
  }

  // Verify that a single primitive array argument to an object varargs method is just treated
  // like any other object.
  public void testPrimitiveArrayToObjectVarargs() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(float[] array) { java.util.Arrays.asList(array); }}",
        "Test", "Test.m");
    assertTranslation(translation, "JavaUtilArrays_asListWithNSObjectArray_("
        + "[IOSObjectArray arrayWithObjects:(id[]){ array } count:1 "
        + "type:IOSClass_floatArray(1)]);");
  }

  public void testMultiDimPrimitiveArrayPassedToTypeVariableVarargs() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int[][] array) { java.util.Arrays.asList(array); } }",
        "Test", "Test.m");
    // Array should be passed as it is.
    assertTranslation(translation, "JavaUtilArrays_asListWithNSObjectArray_(array);");
  }

  public void testTwoDimObjectArrayPassedToObjectVarargs() throws IOException {
    String translation = translateSourceFile(
        "class Test { void foo(Object... args) {} void test(Object[][] array) { foo(array); } }",
        "Test", "Test.m");
    // Array should be passed as it is.
    assertTranslation(translation, "[self fooWithNSObjectArray:array];");
  }

  // Verify cloning a single array argument doesn't cause it to get boxed in another array.
  public void testArrayCloneArgument() throws IOException {
    String translation = translateSourceFile(
        "class A { void varargs(String s, Object... objects) {}"
        + "void test() { Object[] objs = new Object[] { \"\", \"\" };"
        + "varargs(\"objects\", objs.clone()); }}", "A", "A.m");
    assertTranslation(translation,
        "[self varargsWithNSString:@\"objects\" withNSObjectArray:[objs clone]];");
  }
}
