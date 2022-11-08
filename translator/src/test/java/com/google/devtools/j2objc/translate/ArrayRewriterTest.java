/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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
import com.google.devtools.j2objc.ast.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link ArrayRewriter} class.
 *
 * @author Tom Ball
 */
public class ArrayRewriterTest extends GenerationTest {

  // Since ArrayRewriter was created from code scattered throughout the package,
  // so are its tests. These should be moved here as they are updated.

  // Verify that the "SetAndConsume" setter is used.
  public void testAssignmentToNewObject() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(Object[] o) { o[0] = new Object(); o[0] = new int[1]; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "IOSObjectArray_SetAndConsume(nil_chk(o), 0, new_NSObject_init());");
    assertTranslation(translation,
        "IOSObjectArray_SetAndConsume(o, 0, [IOSIntArray newArrayWithLength:1]);");
  }

  public void testPreAndPostincrementOfArrayAccess() throws IOException {
    String source = "int x[] = { 0 }; ++x[0]; x[0]++; ++(x[0]); (x[0])++; ++((x[0])); ((x[0]))++;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(7, stmts.size());
    assertEquals("++(*IOSIntArray_GetRef(x, 0));", generateStatement(stmts.get(1)));
    assertEquals("(*IOSIntArray_GetRef(x, 0))++;", generateStatement(stmts.get(2)));
    assertEquals("++(*IOSIntArray_GetRef(x, 0));", generateStatement(stmts.get(3)));
    assertEquals("(*IOSIntArray_GetRef(x, 0))++;", generateStatement(stmts.get(4)));
    assertEquals("++((*IOSIntArray_GetRef(x, 0)));", generateStatement(stmts.get(5)));
    assertEquals("((*IOSIntArray_GetRef(x, 0)))++;", generateStatement(stmts.get(6)));
  }

  public void testArrayCastFromGenericMethodReturn() throws IOException {
    options.setAsObjCGenericDecl(true);
    String translation = translateSourceFile(
        "class Test { "
            + "  public int[] newIntArray() { return new int[0]; } "
            + "  public String[] newObjArray() { return new String[0]; }"
            + "  public void test() { "
            + "    int[] ints = newIntArray(); "
            + "    String[] objs = newObjArray(); "
            + "  }"
            + "  public int[] test2() { "
            + "    return newIntArray(); "
            + "  }"
            + "  public static class SubTest extends Test {"
            + "    public String[] newObjArray() {"
            + "      return super.newObjArray();"
            + "    }"
            + "  }"
            + "}", "Test", "Test.m");
    assertTranslatedLines(translation,
        "IOSIntArray *ints = (IOSIntArray *) [self newIntArray];",
        "IOSObjectArray *objs = (IOSObjectArray *) [self newObjArray];");
    assertTranslation(translation, "return (IOSIntArray *) [self newIntArray];");
    assertTranslation(translation, "return (IOSObjectArray *) Test_newObjArray(self);");
  }

  public void testArrayNotCastFromUnusedGenericMethodReturn() throws IOException {
    options.setAsObjCGenericDecl(true);
    String translation = translateSourceFile(
        "class Test { "
            + "  public int[] newIntArray() { return new int[0]; } "
            + "  public String[] newObjArray() { return new String[0]; }"
            + "  public void test() { "
            + "    newIntArray(); "
            + "    newObjArray(); "
            + "  }}", "Test", "Test.m");
    assertTranslatedLines(translation, "[self newIntArray];", "[self newObjArray];");
  }

  public void testArrayCastInEnhancedForLoop() throws IOException {
    options.setAsObjCGenericDecl(true);
    String translation = translateSourceFile(
        "class Test { "
            + "  public int[] newIntArray() { return new int[0]; } "
            + "  public void test() { "
            + "    for (int i : newIntArray()) {}"
            + "  }}", "Test", "Test.m");
    assertTranslatedLines(translation,
        "IOSIntArray *a__ = (IOSIntArray *) [self newIntArray];");
  }

  public void testNoCastInLambda() throws IOException {
    // Test from Base64Test.java
    options.setAsObjCGenericDecl(true);
    String translation = translateSourceFile(
        "import java.util.Base64.Decoder; "
            + "class Test { "
            + "  public void test(Decoder decoder) { "
            + "    Runnable r =  () -> decoder.decode((byte[]) null);"
            + "  }}", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)run {",
        "[((JavaUtilBase64_Decoder *) nil_chk(val$decoder_)) "
            + "decodeWithByteArray:(IOSByteArray *) nil];");
  }
}
