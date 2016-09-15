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
        "class Test { void test(Object o, boolean b) { o.toString(); if (b) { o = null; } "
        + "o.toString(); o = new Object(); o.toString(); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "nil_chk(o) description];",
        "if (b) {",
        "  o = nil;",
        "}",
        "[nil_chk(o) description];",
        "o = create_NSObject_init();",
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

  // Method invocations can have side-effects
  public void testFieldsNeedCheckAfterInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class Test { class Foo { int i; } Foo foo;"
        + " void test() { int i; i = foo.i; i = foo.i; System.gc(); i = foo.i; super.toString();"
        + " i = foo.i; new Test(); i = foo.i; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "i = ((Test_Foo *) nil_chk(foo_))->i_;",
        "i = foo_->i_;",
        "JavaLangSystem_gc();",
        "i = ((Test_Foo *) nil_chk(foo_))->i_;",
        "[super description];",
        "i = ((Test_Foo *) nil_chk(foo_))->i_;",
        "create_Test_init();",
        "i = ((Test_Foo *) nil_chk(foo_))->i_;");
  }

  public void testReassignedVariableInLoop() throws IOException {
    String translation = translateSourceFile(
        "public abstract class Test { abstract boolean getB();"
        + " void test(Object o, Iterable<Integer> ints) {"
        + " o.toString(); do { o.toString(); o = null; } while(getB());"
        + " o.toString(); while(getB()) { o.toString(); o = null; }"
        + " o.toString(); for(; getB();) { o.toString(); o = null; }"
        + " o.toString(); for(Integer i : ints) { o.toString(); o = null; } } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "[nil_chk(o) description];",
        "do {",
        "  [nil_chk(o) description];",
        "  o = nil;",
        "}",
        "while ([self getB]);",
        "[nil_chk(o) description];",
        "while ([self getB]) {",
        "  [nil_chk(o) description];",
        "  o = nil;",
        "}",
        "[nil_chk(o) description];",
        "for (; [self getB]; ) {",
        "  [nil_chk(o) description];",
        "  o = nil;",
        "}",
        "[nil_chk(o) description];",
        "for (JavaLangInteger * __strong i in nil_chk(ints)) {",
        "  [nil_chk(o) description];",
        "  o = nil;",
        "}");
  }

  public void testBreakAndContinue() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(Object o, boolean b, boolean b2) { o.toString();"
        + " dummy: do { o.toString(); if (b) { o = null; break; } if (b2) { o = null; continue; }"
        + " o.toString(); } while(Math.random() < 0.5); o.toString(); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "[nil_chk(o) description];",
        "dummy: do {",
        "  [nil_chk(o) description];",  // Needs nil_chk because of continue statement.
        "  if (b) {",
        "    o = nil;",
        "    break;",
        "  }",
        "  if (b2) {",
        "    o = nil;",
        "    continue;",
        "  }",
        // No nil_chk because of reassignments of "o" are followed by control
        // flow breaking statements.
        "  [o description];",
        "}",
        "while (JavaLangMath_random() < 0.5);",
        "[nil_chk(o) description];");  // Needs nil_chk because of break statement.
  }

  public void testTryCatch() throws IOException {
    String translation = translateSourceFile(
        "public class Test { class Foo { int i; } void test(Foo f) { int i; i = f.i; try {"
        + " f = null; System.gc(); i = f.i; } catch (Throwable t) { i = f.i; }"
        + " finally { i = f.i; } i = f.i; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "i = ((Test_Foo *) nil_chk(f))->i_;",
        "@try {",
        "  f = nil;",
        "  JavaLangSystem_gc();",
        "  i = ((Test_Foo *) nil_chk(f))->i_;",
        "}",
        "@catch (NSException *t) {",
        "  i = ((Test_Foo *) nil_chk(f))->i_;",
        "}",
        "@finally {",
        "  i = ((Test_Foo *) nil_chk(f))->i_;",
        "}",
        "i = f->i_;");
  }

  public void testScopeTermination() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(Object o1, Object o2, boolean b) {"
        + " if (o1 == null) { return; } o1.toString();"
        + " if (o2 != null) System.gc(); else if (b) throw new RuntimeException(); else return;"
        + " o2.toString(); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "if (o1 == nil) {",
        "  return;",
        "}",
        "[o1 description];",
        "if (o2 != nil) JavaLangSystem_gc();",
        "else if (b) @throw create_JavaLangRuntimeException_init();",
        "else return;",
        "[o2 description];");
  }

  public void testTrickyEqualsNullChecks() throws IOException {
    // Try to trick the translator into thinking that "o" is safe.
    String translation = translateSourceFile(
        "class Test { boolean foo(boolean b) { return true; } void test(Object o) {"
        + " if (o != null ? false : true) { o.toString(); }"
        + " if (foo(o != null)) { o.toString(); } } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "if (o != nil ? false : true) {",
        "  [nil_chk(o) description];",
        "}",
        "if ([self fooWithBoolean:o != nil]) {",
        "  [nil_chk(o) description];",
        "}");
  }

  public void testNullCheckInLoopContition() throws IOException {
    String translation = translateSourceFile(
        "abstract class Test { abstract Object getObj(); void test(Object o) {"
        + " while (o == null) { o = getObj(); } o.toString();"
        + " for (; o != null; ) { o.toString(); o = getObj(); } o.toString(); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "while (o == nil) {",
        "  o = [self getObj];",
        "}",
        "[o description];",
        "for (; o != nil; ) {",
        "  [o description];",
        "  o = [self getObj];",
        "}",
        "[nil_chk(o) description];");
  }

  public void testSwitchStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(Object o1, Object o2, int i) { o2.toString(); switch(i) {"
        + " case 1: o1.toString(); case 2: o1.toString(); break;"
        + " case 3: o2 = null; case 4: o2.toString(); } } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "[nil_chk(o2) description];",
        "switch (i) {",
        "  case 1:",
        "  [nil_chk(o1) description];",
        "  case 2:",
        "  [nil_chk(o1) description];",
        "  break;",
        "  case 3:",
        "  o2 = nil;",
        "  case 4:",
        "  [nil_chk(o2) description];",
        "}");
  }

  // Volatile fields should always nil_chk'ed.
  public void testVolatileField() throws IOException {
    String translation = translateSourceFile(
        "class Test { static class Foo { int i; } volatile Foo f;"
        + " void test() { int i; i = f.i; i = f.i; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "i = ((Test_Foo *) nil_chk(JreLoadVolatileId(&f_)))->i_;",
        "i = ((Test_Foo *) nil_chk(JreLoadVolatileId(&f_)))->i_;");
  }

  public void testLabeledBlock() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(Object o, boolean b) { test_label: { o.toString();"
        + " if (b) { o = null; break test_label; } o.toString(); } o.toString(); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "{",
        "  [nil_chk(o) description];",
        "  if (b) {",
        "    o = nil;",
        "    goto break_test_label;",
        "  }",
        "  [o description];",
        "}",
        "break_test_label: ;",
        "[nil_chk(o) description];");
  }

  public void testOuterExpressionOfClassInstanceCreation() throws IOException {
    String translation = translateSourceFile(
        "class Test { class Inner {} public Inner test(Test t) { return t.new Inner(); } }",
        "Test", "Test.m");
    // "t" needs a nil_chk.
    assertTranslation(translation, "return create_Test_Inner_initWithTest_(nil_chk(t));");
  }
}
