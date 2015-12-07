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
 * Unit tests for {@link ConstantBranchPruner}.
 *
 * @author Tom Ball
 */
public class ConstantBranchPrunerTest extends GenerationTest {

  // Verify that statements with non-constant boolean expressions aren't modified.
  public void testNoPruning() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "int test(boolean b) { if (b) { return 1; } else { return 0; }}"
        + "String describe() { return \"is true? \" + true; }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "if (b) {", "return 1;", "}", "else {", "return 0;", "}");
    assertTranslation(translation, "return @\"is true? true\";");
    translation = translateSourceFile(
        "class Test { void tick() {} void test(boolean b) { while (b) { tick(); } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "while (b) {", "[self tick];", "}");
    translation = translateSourceFile(
        "class Test { void tick() {} void test(boolean b) { do { tick(); } while (b); }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "do {", "[self tick];", "}", "while (b);");
  }

  // Do statements should not be replaced with their body because they might contain break
  // statements.
  public void testFalseDoExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test() { foo: do { return 1; } while (false); }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (jint)test {", "foo: do {", "return 1;", "}", "while (false);", "}");
    translation = translateSourceFile(
        "class Test { static final boolean debug = false;"
            + "  int test() { foo: do { return 1; } while (debug); }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (jint)test {", "foo: do {", "return 1;", "}", "while (Test_debug);", "}");
    // Can't remove loop construct while it contains a break statement.
    translation = translateSourceFile(
        "class Test { void test(int i) { do { if (i == 5) break; } while (false); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation, "do {", "if (i == 5) break;", "}", "while (false);");
  }

  // Verify then block replaces if statement when true.
  public void testTrueIfExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test() { if (true) { return 1; } else { return 0; } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "- (jint)test {", "{", "return 1;", "}", "}");
    translation = translateSourceFile(
        "class Test { static final boolean debug = true;"
        + "  int test() { if (debug) { return 1; } else { return 0; } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "- (jint)test {", "{", "return 1;", "}", "}");
  }

  // Verify else block replaces if statement when false.
  public void testFalseIfExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test() { if (false) { return 1; } else { return 0; } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "- (jint)test {", "{", "return 0;", "}", "}");
    translation = translateSourceFile(
        "class Test { static final boolean debug = false;"
        + "  int test() { if (debug) { return 1; } else { return 0; } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "- (jint)test {", "{", "return 0;", "}", "}");
  }

  // Verify parentheses surround boolean constants are removed.
  public void testParentheses() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test() { if (((false))) { return 1; } else { return 0; } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "- (jint)test {", "{", "return 0;", "}", "}");
  }

  // Verify ! boolean constant is inverted.
  public void testBooleanInversion() throws IOException {
    String translation = translateSourceFile(
        "class Test { static final boolean debug = true;"
        + "  int test() { if (!(debug)) { return 1; } else { return 0; } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "- (jint)test {", "{", "return 0;", "}", "}");
  }

  // Verify && expressions are pruned correctly.
  public void testAnd() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "static final boolean DEBUG = true; "
        + "static final boolean NDEBUG = false;"
        + "int test() { int result; "
        + "  if (DEBUG && true) { result = 1; } else { result = 2; }"
        + "  if (DEBUG && false) { result = 3; } else { result = 4; }"
        + "  if (true && NDEBUG) { result = 5; } else { result = 6; }"
        + "  if (false && NDEBUG) { result = 7; } else { result = 8; }"
        + "  return result; }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "{", "result = 1;", "}",
        "{", "result = 4;", "}",
        "{", "result = 6;", "}",
        "{", "result = 8;", "}");
  }

  // Verify || expressions are pruned correctly.
  public void testOr() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "static final boolean DEBUG = true; "
        + "static final boolean NDEBUG = false;"
        + "int test() { int result; "
        + "  if (DEBUG || true) { result = 1; } else { result = 2; }"
        + "  if (DEBUG || false) { result = 3; } else { result = 4; }"
        + "  if (true || NDEBUG) { result = 5; } else { result = 6; }"
        + "  if (false || NDEBUG) { result = 7; } else { result = 8; }"
        + "  return result; }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "{", "result = 1;", "}",
        "{", "result = 3;", "}",
        "{", "result = 5;", "}",
        "{", "result = 8;", "}");
  }

  // Verify method invocation paired with constant is not pruned because methods
  // may have side effects.
  public void testMethodAnd() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "static final boolean DEBUG = true; "
        + "static final boolean NDEBUG = false;"
        + "boolean b;"
        + "boolean enabled() { return true; }"
        + "int test() { int result; "
        // The "b" operand should be pruned since it has no side effect.
        + "  if (enabled() && b && NDEBUG) { result = 1; } else { result = 2; }"
        + "  if (enabled() || DEBUG) { result = 3; } else { result = 4; }"
        + "  return result; }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "(void) ([self enabled]);",
        "{",
        "  result = 2;",
        "}",
        "(void) ([self enabled]);",
        "{",
        "  result = 3;",
        "}");
  }

  public void testWhileStatementPrunedWithSideEffects() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean getB() { return true; } int test(boolean b) { "
        + "while (b && (getB() && false)) { return 1; } return 0; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (jint)testWithBoolean:(jboolean)b {",
        "  (void) (b && ([self getB]));",
        "  return 0;",
        "}");
  }

  public void testExpressionPruning() throws IOException {
    String translation = translateSourceFile(
        "class A { "
        + "static final boolean DEBUG = true; "
        + "static final boolean TEST = true; "
        + "private static boolean nonConstant = false; "
        + "boolean test() { "

        // DEBUG and TEST constants should be pruned.
        + "  if (DEBUG && TEST && nonConstant) return false; "
        + "  return true; }}", "A", "A.m");
    assertTranslatedLines(translation,
        "- (jboolean)test {", "if (A_nonConstant) return false;", "return true;", "}");
  }

  // Verify that volatile loads aren't pruned because they provide a memory
  // barrier.
  public void testVolatileLoad() throws IOException {
    String translation = translateSourceFile(
        "class Test { volatile int i; boolean test() { return i == 1 && false; } }",
        "Test", "Test.m");
    assertTranslation(translation, "return JreLoadVolatileInt(&i_) == 1 && false;");
  }

  // The JDT parser provides limited type information for local types declared
  // in unreachable code, resulting in NPEs. ConstantBranchPruner should remove
  // this unreachable code.
  public void testUnreachableLocalClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test() { if (true) { return 5; } "
        + "class Foo {}; return new Foo().hashCode(); } }", "Test", "Test.m");
    assertNotInTranslation(translation, "Foo");
    assertTranslatedLines(translation,
        "- (jint)test {",
        "  {",
        "    return 5;",
        "  }",
        "}");
  }
}
