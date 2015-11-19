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
 * Tests for {@link NilCheckResolver}.
 *
 * @author Keith Stanger
 */
public class NilCheckResolverTest extends GenerationTest {

  public void testNilCheckArrayLength() throws IOException {
    String translation = translateSourceFile(
      "public class A { int length(char[] s) { return s.length; } void test() { length(null);} }",
      "A", "A.m");
    assertTranslation(translation, "return ((IOSCharArray *) nil_chk(s))->size_;");
  }

  public void testNilCheckOnCastExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int i; void test(Object o) { int i = ((Test) o).i; } }", "Test", "Test.m");
    assertTranslation(translation,
        "((Test *) nil_chk(((Test *) cast_chk(o, [Test class]))))->i_");
  }

  public void testNoNilCheckOnSecondDereference() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(Object o) { o.toString(); o.toString(); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "[nil_chk(o) description];",
        "[o description];");
  }

  public void testNilCheckAfterReassignment() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(Object o) { o.toString(); o = null; o.toString(); "
        + "o = new Object(); o.toString(); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "nil_chk(o) description];",
        "o = nil;",
        "[nil_chk(o) description];",
        "o = [new_NSObject_init() autorelease];",
        "[o description];");
  }

  public void testNilCheckAfterIfStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(Object o, boolean b) { if (b) { o.toString(); } o.toString(); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "if (b) {",
        "[nil_chk(o) description];",
        "}",
        "[nil_chk(o) description];");
  }

  public void testEqualsNullTest() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "void test1(Object o1) { if (o1 == null) { o1.toString(); } else { o1.toString(); } }"
        + "void test2(Object o2) { if (o2 != null) { o2.toString(); } else { o2.toString(); } }"
        + "void test3(Object o3, boolean b) { "
        + "if (o3 != null || b) { o3.toString(); } else { o3.toString(); } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "if (o1 == nil) {",
        "[nil_chk(o1) description];",
        "}",
        "else {",
        "[o1 description];",
        "}");
    assertTranslatedLines(translation,
        "if (o2 != nil) {",
        "[o2 description];",
        "}",
        "else {",
        "[nil_chk(o2) description];",
        "}");
    assertTranslatedLines(translation,
        "if (o3 != nil || b) {",
        "[nil_chk(o3) description];",
        "}",
        "else {",
        "[nil_chk(o3) description];",
        "}");
  }

  public void testNilCheckAfterWhileLoop() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(Object o, boolean b) { "
        + "while (b) { o.toString(); } o.toString(); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "while (b) {",
        "[nil_chk(o) description];",
        "}",
        "[nil_chk(o) description];");
  }

  public void testNilCheckAfterConditionalInfix() throws IOException {
    String translation = translateSourceFile(
        "abstract class Test { abstract boolean foo(); void test(Test t1, Test t2, boolean b) { "
        + "boolean b1 = b && t1.foo(); t1.foo(); boolean b2 = b || t2.foo(); t2.foo(); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "jboolean b1 = b && [((Test *) nil_chk(t1)) foo];",
        "[((Test *) nil_chk(t1)) foo];",
        "jboolean b2 = b || [((Test *) nil_chk(t2)) foo];",
        "[((Test *) nil_chk(t2)) foo];");
  }

  public void testNilCheckEnhancedForExpression() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  boolean test(String target, java.util.List<String> strings) {"
        + "    for (String s : strings) {"
        + "      if (s.equals(target)) return true;"
        + "    }"
        + "    return false;"
        + "  }}",
        "Test", "Test.m");
    assertTranslation(translation, "nil_chk(strings)");
  }
}
