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
package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

/**
 * Unit tests for {@link LambdaExpression}.
 *
 * @author Seth Kirby
 */
public class LambdaExpressionTest extends GenerationTest {

  private String functionHeader = "interface Function<T, R> { R apply(T t); }";
  private String fourToOneHeader = "interface FourToOne<F, G, H, I, R> {"
      + "  R apply(F f, G g, H h, I i); }";

  public void testCaptureDetection() throws IOException {
    translateSourceFile(functionHeader + "interface A { boolean r(boolean a);"
        + "default int ret1() { return 2; }} interface B { default int ret() { return 1; } }"
        + "class Test { void f() { ((A & B)(a) -> a).ret(); } }", "Test", "Test.m");
    translateSourceFile(functionHeader + "interface A2 { boolean r();"
        + "default int ret1() { return 2; }} interface B { default int ret() { return 1; } }"
        + "class Test { void f() { ((A2 & B)() -> true).ret(); } }", "Test", "Test.m");
    String nonCaptureTranslation = translateSourceFile(
        functionHeader + "class Test { Function f = x -> x;}", "Test", "Test.m");
    String captureTranslationOuter = translateSourceFile(
        functionHeader + "class Test { int y; Function f = x -> y;}", "Test", "Test.m");
    String captureTranslation = translateSourceFile(
        functionHeader + "class Test { Function<Function, Function> f = y -> x -> y;}", "Test",
        "Test.m");
    assertTranslation(nonCaptureTranslation, "Test_$Lambda$1_get_instance()");
    assertTranslatedLines(captureTranslationOuter,
        "@interface Test_$Lambda$1 : NSObject < Function > {",
        " @public",
        "  Test *this$0_;",
        "}");
    assertTranslation(captureTranslation, "Test_$Lambda$1_get_instance()");
    assertTranslatedLines(captureTranslation,
        "@interface Test_$Lambda$2 : NSObject < Function > {",
        " @public",
        "  id<Function> val$y_;",
        "}");
  }

  public void testTypeInference() throws IOException {
    String quadObjectTranslation = translateSourceFile(
        fourToOneHeader + "class Test { FourToOne f = (a, b, c, d) -> 1;}", "Test", "Test.m");
    assertTranslatedLines(quadObjectTranslation,
        "- (id)applyWithId:(id)a",
        "           withId:(id)b",
        "           withId:(id)c",
        "           withId:(id)d {",
        "  return JavaLangInteger_valueOfWithInt_(1);",
        "}");
    String mixedObjectTranslation = translateSourceFile(fourToOneHeader
        + "class Test { FourToOne<String, Double, Integer, Boolean, String> f = "
        + "(a, b, c, d) -> \"1\";}", "Test", "Test.m");
    assertTranslatedLines(mixedObjectTranslation,
        "- (id)applyWithId:(NSString *)a",
        "           withId:(JavaLangDouble *)b",
        "           withId:(JavaLangInteger *)c",
        "           withId:(JavaLangBoolean *)d {",
        "  return @\"1\";",
        "}");
  }

  public void testOuterFunctions() throws IOException {
    String translation = translateSourceFile(
        functionHeader + "class Test { Function outerF = (x) -> x;}", "Test", "Test.m");
    assertTranslation(translation,
        "JreStrongAssign(&self->outerF_, JreLoadStatic(Test_$Lambda$1, instance));");
  }

  public void testStaticFunctions() throws IOException {
    String translation = translateSourceFile(
        functionHeader + "class Test { static Function staticF = (x) -> x;}", "Test", "Test.m");
    assertTranslatedSegments(translation,
        "id<Function> Test_staticF;",
        "if (self == [Test class]) {",
        "JreStrongAssign(&Test_staticF, JreLoadStatic(Test_$Lambda$1, instance))");
  }

  public void testNestedLambdas() throws IOException {
    String outerCapture = translateSourceFile(functionHeader
        + "class Test { Function<String, Function<String, String>> f = x -> y -> x;}", "Test",
        "Test.m");
    assertTranslation(outerCapture, "Test_$Lambda$1_get_instance()");
    assertTranslatedLines(outerCapture,
        "- (id)applyWithId:(NSString *)x {",
        "  return create_Test_$Lambda$2_initWithNSString_(x);",
        "}");
    assertTranslatedLines(outerCapture,
        "@interface Test_$Lambda$2 : NSObject < Function > {",
        " @public",
        "  NSString *val$x_;",
        "}");
    assertTranslatedLines(outerCapture,
        "- (id)applyWithId:(NSString *)y {",
        "  return val$x_;",
        "}");
    String noCapture = translateSourceFile(functionHeader
        + "class Test { Function<String, Function<String, String>> f = x -> y -> y;}", "Test",
        "Test.m");
    assertTranslation(noCapture, "Test_$Lambda$1_get_instance()");
    assertTranslation(noCapture, "Test_$Lambda$2_get_instance()");
    assertTranslatedLines(noCapture,
        "- (id)applyWithId:(NSString *)x {",
        "  return JreLoadStatic(Test_$Lambda$2, instance);",
        "}");
    assertTranslatedLines(noCapture,
        "- (id)applyWithId:(NSString *)y {",
        "  return y;",
        "}");
  }

  // There's no need for a cast_check call on a lambda whose type matches the assigned type.
  public void testNoCastCheck() throws IOException {
    String translation = translateSourceFile(
        functionHeader + "class Test { Function f = (Function) (x) -> x;}", "Test", "Test.m");
    assertNotInTranslation(translation, "cast_check");
  }

  // Test that we aren't trying to import lambda types.
  public void testImportExclusion() throws IOException {
    String translation = translateSourceFile(
        functionHeader + "class Test { Function f = (Function) (x) -> x;}", "Test", "Test.m");
    assertNotInTranslation(translation, "lambda$0.h");
  }

  // Check that lambdas are uniquely named.
  public void testLambdaUniquify() throws IOException {
    String translation = translateSourceFile(functionHeader
        + "class Test { class Foo{ class Bar { Function f = x -> x; }}\n"
        + "Function f = x -> x;}",
        "Test", "Test.m");
    assertTranslation(translation, "@interface Test_Foo_Bar_$Lambda$1 : NSObject < Function >");
    assertTranslation(translation, "@interface Test_$Lambda$1 : NSObject < Function >");
  }

  // Check that lambda captures respect reserved words.
  public void testLambdaCloseOverReservedWord() throws IOException {
    String translation = translateSourceFile(functionHeader
        + "class Test { void f(int operator) { Function l = (a) -> operator; } }",
        "Test", "Test.m");
    assertTranslation(translation, "val$operator_");
  }

  public void testLargeArgumentCount() throws IOException {
    String interfaceHeader = "interface TooManyArgs<T> { T f(T a, T b, T c, T d, T e, T f, T g,"
        + " T h, T i, T j, T k, T l, T m, T n, T o, T p, T q, T r, T s, T t, T u, T v, T w, T x,"
        + " T y, T z, T aa, T ab, T ac, T ad, T ae, T af, T ag, T ah, T ai, T aj, T ak, T al,"
        + " T am, T an, T ao, T ap, T aq, T ar, T as, T at, T au, T av, T aw, T ax, T ay, T az,"
        + " T ba, T bb, T bc, T bd, T be, T bf, T bg, T bh, T bi, T bj, T bk, T bl, T bm, T bn,"
        + " T bo, T bp, T bq, T br, T bs, T bt, T bu, T bv, T bw, T bx, T by, T bz, T foo);}";
    String translation = translateSourceFile(interfaceHeader + "class Test { void a() {"
        + "Object foo = \"Foo\";"
        + "TooManyArgs fun = (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w,"
        + " x, y, z, aa, ab, ac, ad, ae, af, ag, ah, ai, aj, ak, al, am, an, ao, ap, aq, ar, as,"
        + " at, au, av, aw, ax, ay, az, ba, bb, bc, bd, be, bf, bg, bh, bi, bj, bk, bl, bm, bn,"
        + " bo, bp, bq, br, bs, bt, bu, bv, bw, bx, by, bz, bar) -> foo;}}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (id)fWithId:(id)a",
        "       withId:(id)b",
        "       withId:(id)c",
        "       withId:(id)d",
        "       withId:(id)e");
    assertTranslatedLines(translation,
        "       withId:(id)bw",
        "       withId:(id)bx",
        "       withId:(id)by",
        "       withId:(id)bz",
        "       withId:(id)bar {",
        "  return val$foo_;",
        "}");
  }

  public void testCapturingBasicTypeReturn() throws IOException {
    String header = "interface I { int foo(); }";
    String translation = translateSourceFile(
        header + "class Test { int f = 1234; void foo() { I i = () -> f; } }", "Test",
        "Test.m");
    assertTranslatedLines(translation,
        "- (jint)foo {",
        "  return this$0_->f_;",
        "}");
  }

  // Verify that an #include is generated for the lambda's functionalType.
  public void testLambdaFunctionalTypeImport() throws IOException {
    String translation = translateSourceFile("import java.util.*;"
        + "class Test { "
        + "  public void test(List<String> names) { "
        + "    Collections.sort(names, (p1, p2) -> p1.compareTo(p2)); "
        + "  }"
        + "}", "Test", "Test.m");
    assertTranslation(translation, "#include \"java/util/Comparator.h\"");
  }

  public void testClassLiteralNamesInLambda() throws IOException {
    String translation = translateSourceFile("package foo.bar; import java.io.Serializable; "
        + "interface Comparator<T> {"
        + "  int compare(T o1, T o2);"
        + "  default Comparator<T> thenComparing(Comparator<? super T> other) {"
        + "    return (Comparator<T> & Serializable) (c1, c2) -> {"
        + "      int res = compare(c1, c2);"
        + "      return (res != 0) ? res : other.compare(c1, c2);"
        + "    }; "
        + "  }"
        + "}", "Comparator", "foo/bar/Comparator.m");
    assertTranslatedLines(translation,
        "@interface FooBarComparator_$Lambda$1 : "
            + "NSObject < FooBarComparator, JavaIoSerializable > {",
        " @public",
        "  id<FooBarComparator> this$0_;",
        "  id<FooBarComparator> val$other_;",
        "}");
    assertTranslatedLines(translation,
        "- (jint)compareWithId:(id)c1",
        "               withId:(id)c2 {",
        "  jint res = [this$0_ compareWithId:c1 withId:c2];",
        "  return (res != 0) ? res : [((id<FooBarComparator>) nil_chk(val$other_)) "
            + "compareWithId:c1 withId:c2];",
        "}");
  }
}
