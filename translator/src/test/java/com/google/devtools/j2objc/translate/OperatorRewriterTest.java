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
import com.google.devtools.j2objc.Options.MemoryManagementOption;
import com.google.devtools.j2objc.ast.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link OperatorRewriter}.
 *
 * @author Keith Stanger
 */
public class OperatorRewriterTest extends GenerationTest {

  public void testSetFieldOnResultOfExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { String s; static Test getTest() { return null; } "
        + "void test(boolean b) { (b ? new Test() : getTest()).s = \"foo\"; } }", "Test", "Test.m");
    assertTranslation(translation,
        "JreStrongAssign(&(b ? create_Test_init() : Test_getTest())->s_, @\"foo\");");
  }

  public void testModAssignOperator() throws IOException {
    String source = "float a = 4.2f; a %= 2.1f; double b = 5.6; b %= 1.2; byte c = 3; c %= 2.3; "
        + "short d = 4; d %= 3.4; int e = 5; e %= 4.5; long f = 6; f %= 5.6; char g = 'a'; "
        + "g %= 6.7;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(14, stmts.size());
    assertEquals("JreModAssignFloatF(&a, 2.1f);", generateStatement(stmts.get(1)));
    assertEquals("JreModAssignDoubleD(&b, 1.2);", generateStatement(stmts.get(3)));
    assertEquals("JreModAssignByteD(&c, 2.3);", generateStatement(stmts.get(5)));
    assertEquals("JreModAssignShortD(&d, 3.4);", generateStatement(stmts.get(7)));
    assertEquals("JreModAssignIntD(&e, 4.5);", generateStatement(stmts.get(9)));
    assertEquals("JreModAssignLongD(&f, 5.6);", generateStatement(stmts.get(11)));
    assertEquals("JreModAssignCharD(&g, 6.7);", generateStatement(stmts.get(13)));
  }

  public void testDoubleModulo() throws IOException {
    String translation = translateSourceFile(
      "public class A { "
      + "  double doubleMod(double one, double two) { return one % two; }"
      + "  float floatMod(float three, float four) { return three % four; }}",
      "A", "A.m");
    assertTranslation(translation, "return fmod(one, two);");
    assertTranslation(translation, "return fmodf(three, four);");
  }

  public void testLShift32WithExtendedOperands() throws IOException {
    String source = "int a; a = 1 << 2; a = 1 << 2 << 3; a = 1 << 2 << 3 << 4;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(4, stmts.size());
    assertEquals("a = JreLShift32(1, 2);", generateStatement(stmts.get(1)));
    assertEquals("a = JreLShift32(JreLShift32(1, 2), 3);", generateStatement(stmts.get(2)));
    assertEquals("a = JreLShift32(JreLShift32(JreLShift32(1, 2), 3), 4);",
                 generateStatement(stmts.get(3)));
  }

  public void testURShift64WithExtendedOperands() throws IOException {
    String source = "long a; a = 65535L >>> 2; a = 65535L >>> 2 >>> 3; "
        + "a = 65535L >>> 2 >>> 3 >>> 4;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(4, stmts.size());
    assertEquals("a = JreURShift64(65535LL, 2);", generateStatement(stmts.get(1)));
    assertEquals("a = JreURShift64(JreURShift64(65535LL, 2), 3);", generateStatement(stmts.get(2)));
    assertEquals("a = JreURShift64(JreURShift64(JreURShift64(65535LL, 2), 3), 4);",
            generateStatement(stmts.get(3)));
  }

  public void testStringAppendOperator() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak;"
        + " class Test { String ss; @Weak String ws; String[] as;"
        + " void test() { ss += \"foo\"; ws += \"bar\"; as[0] += \"baz\"; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "JreStrAppendStrong(&ss_, \"$\", @\"foo\");",
        "JreStrAppend(&ws_, \"$\", @\"bar\");",
        "JreStrAppendArray(IOSObjectArray_GetRef(nil_chk(as_), 0), \"$\", @\"baz\");");
  }

  public void testParenthesizedLeftHandSide() throws IOException {
    String translation = translateSourceFile(
        "class Test { String s; void test(String s2) { (s) = s2; } }", "Test", "Test.m");
    assertTranslation(translation, "JreStrongAssign(&(s_), s2);");
  }

  public void testVolatileLoadAndAssign() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak;"
        + " class Test { volatile int i; static volatile int si; volatile String s;"
        + " static volatile String vs; @Weak volatile String ws;"
        + " void test() { int li = i; i = 2; li = si; si = 3; String ls = s; s = \"foo\";"
        + " ls = vs; vs = \"foo\"; ls = ws; ws = \"foo\"; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "jint li = JreLoadVolatileInt(&i_);",
        "JreAssignVolatileInt(&i_, 2);",
        "li = JreLoadVolatileInt(&Test_si);",
        "JreAssignVolatileInt(&Test_si, 3);",
        "NSString *ls = JreLoadVolatileId(&s_);",
        "JreVolatileStrongAssign(&s_, @\"foo\");",
        "ls = JreLoadVolatileId(&Test_vs);",
        "JreVolatileStrongAssign(&Test_vs, @\"foo\");",
        "ls = JreLoadVolatileId(&ws_);",
        "JreAssignVolatileId(&ws_, @\"foo\");");
  }

  public void testPromotionTypesForCompundAssign() throws IOException {
    String translation = translateSourceFile(
        "class Test { volatile short s; int i; void test() {"
        + " s += 1; s -= 2l; s *= 3.0f; s /= 4.0; s %= 5l; i %= 6.0; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "JrePlusAssignVolatileShortI(&s_, 1);",
        "JreMinusAssignVolatileShortJ(&s_, 2l);",
        "JreTimesAssignVolatileShortF(&s_, 3.0f);",
        "JreDivideAssignVolatileShortD(&s_, 4.0);",
        "JreModAssignVolatileShortJ(&s_, 5l);",
        "JreModAssignIntD(&i_, 6.0);");
  }

  public void testStringAppendLocalVariableARC() throws IOException {
    options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "class Test { void test() { String str = \"foo\"; str += \"bar\"; } }", "Test", "Test.m");
    // Local variables in ARC have strong semantics.
    assertTranslation(translation, "JreStrAppendStrong(&str, \"$\", @\"bar\")");
  }

  public void testStringAppendInfixExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int x, int y) { "
        + "String str = \"foo\"; str += x + y; } }", "Test", "Test.m");
    assertTranslation(translation, "JreStrAppend(&str, \"I\", x + y);");
    translation = translateSourceFile(
        "class Test { void test(int x) { "
        + "String str = \"foo\"; str += \"bar\" + x; } }", "Test", "Test.m");
    assertTranslation(translation, "JreStrAppend(&str, \"$I\", @\"bar\", x);");
  }

  public void testRetainedWithAnnotation() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.RetainedWith;"
        + "class Test { @RetainedWith Object rwo; @RetainedWith volatile Object rwvo;"
        + "Test getTest() { return new Test(); }"
        + "void test() { rwo = new Object(); rwvo = new Object(); }"
        + "void test2() { getTest().rwo = new Object(); } }", "Test", "Test.m");
    assertTranslation(translation, "JreRetainedWithAssign(self, &rwo_, create_NSObject_init());");
    assertTranslation(translation,
        "JreVolatileRetainedWithAssign(self, &rwvo_, create_NSObject_init());");
    assertTranslatedLines(translation,
        // The getTest() call must be extracted so that it can be passed as the parent ref without
        // duplicating the expression.
        "t *__rw$0;",
        "(__rw$0 = nil_chk([self getTest]), "
          + "JreRetainedWithAssign(__rw$0, &__rw$0->rwo_, create_NSObject_init()));");
    // Test the dealloc calls too.
    assertTranslation(translation, "JreRetainedWithRelease(self, rwo_);");
    assertTranslation(translation, "JreVolatileRetainedWithRelease(self, &rwvo_);");
  }

  public void testRetainedLocalRef() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "  boolean test1(String s1, String s2) {"
        + "    @com.google.j2objc.annotations.RetainedLocalRef"
        + "    java.util.Comparator<String> c = String.CASE_INSENSITIVE_ORDER;"
        + "    return c.compare(s1, s2) == 0;"
        + "    }   "
        + "  boolean test2(Thing t, Thing t2, String s1, String s2) {"
        + "    @com.google.j2objc.annotations.RetainedLocalRef"
        + "    Thing thing = t;"
        + "    thing = t2;"
        + "    return thing.comp.compare(s1, s2) == 0;"
        + "  }"
        + "  private static class Thing { public java.util.Comparator<String> comp; }}",
        "Test", "Test.m");
    assertNotInTranslation(translation, "RetainedLocalRef");
    assertTranslatedLines(translation,
        "id<JavaUtilComparator> c = JreRetainedLocalValue(JreLoadStatic("
        + "NSString, CASE_INSENSITIVE_ORDER));",
        "return [((id<JavaUtilComparator>) nil_chk(c)) compareWithId:s1 withId:s2] == 0;");
    assertTranslatedLines(translation,
        "Test_Thing *thing = JreRetainedLocalValue(t);",
        "thing = JreRetainedLocalValue(t2);",
        "return [((id<JavaUtilComparator>) nil_chk(((Test_Thing *) nil_chk(thing))->comp_)) "
          + "compareWithId:s1 withId:s2] == 0;");
  }

  public void testLazyInitFields() throws IOException {
    addSourcesToSourcepaths();
    addSourceFile("package com.google.errorprone.annotations.concurrent;"
        + "public @interface LazyInit {}",
        "com/google/errorprone/annotations/concurrent/LazyInit.java");
    String translation = translateSourceFile(
        "import com.google.errorprone.annotations.concurrent.LazyInit;"
        + "class Test { @LazyInit String lazyStr; @LazyInit static String lazyStaticStr; }",
        "Test", "Test.h");
    assertTranslation(translation, "volatile_id lazyStr_;");
    assertTranslatedLines(translation,
        "FOUNDATION_EXPORT volatile_id Test_lazyStaticStr;",
        "J2OBJC_STATIC_FIELD_OBJ_VOLATILE(Test, lazyStaticStr, NSString *)");
  }

  public void testRetaineLocal_synchronizedBlock() throws IOException {
    String translation = translateSourceFile(
        "class Test {"
        + "  class Foo {}"
        + "  Foo f = new Foo();"
        + "  void test(String s1, char c1) {"
        + "    Foo f1 = new Foo(), f2 = f;"
        + "    synchronized(f1) {"
        + "      f1 = f2;"
        + "      Foo f3 = f2;"
        + "      s1 = \"foo\";"
        + "      c1 = 'a';"
        + "      synchronized(f3) {"
        + "        f3 = f1;"
        + "      }"
        + "      synchronized(f3) {"
        + "        f3 = f2;"
        + "      }"
        + "    }"
        + "    f2 = f1;"
        + "  }"
        + "}", "Test", "Test.m");
    assertTranslation(translation, "Test_Foo *f2 = f_;");
    assertTranslation(translation, "f1 = JreRetainedLocalValue(f2);");
    assertTranslation(translation, "Test_Foo *f3 = f2;");
    assertTranslation(translation, "s1 = @\"foo\";");
    assertTranslation(translation, "c1 = 'a';");
    assertTranslation(translation, "f3 = JreRetainedLocalValue(f1);");
    assertTranslation(translation, "f3 = JreRetainedLocalValue(f2);");
    assertTranslation(translation, "f2 = f1;");
  }

  public void testRetainedLocal_returnWithinSynchronizedMethodOrBlock() throws IOException {
    String translation = translateSourceFile(
        "class Test {"
        + "  class Foo {}"
        + "  Foo f = new Foo();"
        + "  String test1(String s1, char c1) {"
        + "    synchronized(s1) {"
        + "      return s1;"
        + "    }"
        + "  }"
        + "  synchronized Foo test2() {"
        + "    Foo f1 = f;"
        + "    return f1;"
        + "  }"
        + "  synchronized int test3() {"
        + "    int val = 1;"
        + "    return val;"
        + "  }"
        + "}", "Test", "Test.m");
    assertTranslation(translation, "return JreRetainedLocalValue(s1);");
    assertTranslation(translation, "return JreRetainedLocalValue(f1)");
    assertTranslation(translation, "return val;");
  }
}
