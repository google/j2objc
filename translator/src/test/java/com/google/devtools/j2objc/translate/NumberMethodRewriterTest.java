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
 * Tests for {@link NumberMethodRewriter}.
 *
 * @author Tom Ball
 */
public class NumberMethodRewriterTest extends GenerationTest {

  // Verify equals/hashCode are added when not declared.
  public void testNumberMethodsAdded() throws IOException {
    String translation = translateSourceFile(
      "public class A extends Number { "
      + "  public double doubleValue() { return 0.0; }"
      + "  public float floatValue() { return 0.0f; }"
      + "  public int intValue() { return 0; }"
      + "  public long longValue() { return 0L; }}", "A", "A.m");
    assertTranslatedLines(translation, "- (bool)isEqual:(id)obj {", "return self == obj;");
    assertTranslatedLines(translation, "- (NSUInteger)hash {", "return (NSUInteger)self;");
  }

  // Verify equals/hashCode are not added when declared.
  public void testNumberMethodsNotAdded() throws IOException {
    String translation = translateSourceFile(
      "public class A extends Number { "
      + "  public double doubleValue() { return 0.0; }"
      + "  public float floatValue() { return 0.0f; }"
      + "  public int intValue() { return 0; }"
      + "  public long longValue() { return 0L; }"
      + "  public boolean equals(Object obj) { return this == obj; }"
      + "  public int hashCode() { return 0; }}", "A", "A.m");
    assertOccurrences(translation, "- (bool)isEqual:(id)obj", 1);
    assertOccurrences(translation, "- (NSUInteger)hash", 1);
  }

  // Verify methods are not added if defined in a superclass that is a Number subclass.
  // https://github.com/google/j2objc/issues/1645
  public void testNumberMethodsAlreadyAdded() throws IOException {
    String testNumberTypeSource = addSourceFile(
      "public abstract class TestNumberType extends Number {"
      + "  public boolean equals(Object obj) { return this == obj; }"
      + "  public int hashCode() { return 0; }}", "TestNumberType.java");
    String aSource = addSourceFile(
      "public class A extends TestNumberType {"
      + "  public double doubleValue() { return 0.0; }"
      + "  public float floatValue() { return 0.0f; }"
      + "  public int intValue() { return 0; }"
      + "  public long longValue() { return 0L; }}", "A.java");
    runPipeline(testNumberTypeSource, aSource);
    String testNumberGen = getTranslatedFile("TestNumberType.m");
    assertOccurrences(testNumberGen, "- (bool)isEqual:(id)obj", 1);
    assertOccurrences(testNumberGen, "- (NSUInteger)hash", 1);
    String subClassGen = getTranslatedFile("A.m");
    assertNotInTranslation(subClassGen, "- (bool)isEqual:(id)obj");
    assertNotInTranslation(subClassGen, "- (NSUInteger)hash");
  }

  public void testLongConstructor() throws IOException {
    String translation = translateSourceFile(
        "class A extends Number { "
            + "  private final long value;"
            + "  public A(long value) { this.value = value; }"
            + "  A valueOf(long value) { return new A(value); }"
            + "  public double doubleValue() { return 0.0; }"
            + "  public float floatValue() { return 0.0f; }"
            + "  public int intValue() { return 0; }"
            + "  public long longValue() { return 0L; }}", "A", "A.m");

    assertInTranslation(translation, "- (instancetype)initWithLongLong:(int64_t)value {");
    assertInTranslation(translation, "void A_initWithLongLong_(A *self, int64_t value) {");
    assertInTranslation(translation, "A *new_A_initWithLongLong_(int64_t value) {");
    assertInTranslation(translation, "A *create_A_initWithLongLong_(int64_t value) {");
    assertTranslatedLines(translation,
        "- (A *)valueOfWithLong:(int64_t)value {",
        "return create_A_initWithLongLong_(value);",
        "}");
  }

  public void testLongSuperConstructor() throws IOException {
    String translation =
        translateSourceFile(
            "import java.math.*;"
                + "class A extends BigDecimal { "
                + "  public A(long value) { super(value); }"
                + "  public A(long value, MathContext context) { super(value, context); }"
                + "}",
            "A",
            "A.m");

    // Verify that long constructor uses longlong type.
    assertTranslatedLines(
        translation,
        "- (instancetype)initWithLongLong:(int64_t)value {",
        "  A_initWithLongLong_(self, value);",
        "  return self;",
        "}");
    assertTranslatedLines(
        translation,
        "void A_initWithLongLong_(A *self, int64_t value) {",
        "  JavaMathBigDecimal_initWithLongLong_(self, value);",
        "}");

    // Verify that the other constructor is not affected (long used instead of longlong).
    assertTranslatedLines(
        translation,
        "- (instancetype)initWithLong:(int64_t)value",
        "    withJavaMathMathContext:(JavaMathMathContext *)context {",
        "  A_initWithLong_withJavaMathMathContext_(self, value, context);",
        "  return self;",
        "}");
    assertTranslatedLines(
        translation,
        "void A_initWithLong_withJavaMathMathContext_(A *self, int64_t value, JavaMathMathContext"
            + " *context) {",
        "  JavaMathBigDecimal_initWithLong_withJavaMathMathContext_(self, value, context);",
        "}");
  }
}
