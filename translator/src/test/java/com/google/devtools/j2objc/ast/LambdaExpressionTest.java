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
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.util.FileUtil;

import java.io.IOException;

/**
 * Unit tests for {@link LambdaExpression}.
 *
 * @author Seth Kirby
 */
public class LambdaExpressionTest extends GenerationTest {
  @Override
  protected void setUp() throws IOException {
    tempDir = FileUtil.createTempDir("testout");
    Options.load(new String[] { "-d", tempDir.getAbsolutePath(), "-sourcepath",
        tempDir.getAbsolutePath(), "-q", // Suppress console output.
        "-encoding", "UTF-8", // Translate strings correctly when encodings are nonstandard.
        "-source", "8", // Treat as Java 8 source.
        "-Xforce-incomplete-java8" // Internal flag to force Java 8 support.
    });
    parser = GenerationTest.initializeParser(tempDir);
  }

  private String functionHeader = "interface Function<T, R> { R apply(T t); }";
  private String callableHeader = "interface Callable<R> { R call(); }";
  private String fourToOneHeader = "interface FourToOne<F, G, H, I, R> {"
      + "  R apply(F f, G g, H h, I i); }";

  // Test the creation of explicit blocks for lambdas with expression bodies.
  public void testBlockBodyCreation() throws IOException {
    String translation = translateSourceFile(functionHeader + "class Test { Function f = x -> x;}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "id x){", "return x;");
  }

  public void testCaptureDetection() throws IOException {
    String nonCaptureTranslation = translateSourceFile(
        functionHeader + "class Test { Function f = x -> x;}", "Test", "Test.m");
    String nonCaptureTranslationOuter = translateSourceFile(
        functionHeader + "class Test { int y; Function f = x -> y;}", "Test", "Test.m");
    String captureTranslation = translateSourceFile(
        functionHeader + "class Test { Function<Function, Function> f = y -> x -> y;}", "Test",
        "Test.m");
    assertTranslation(nonCaptureTranslation, "GetNonCapturingLambda");
    assertTranslation(nonCaptureTranslationOuter, "GetNonCapturingLambda");
    assertTranslatedSegments(captureTranslation, "GetNonCapturingLambda", "GetCapturingLambda");
  }

  public void testObjectSelfAddition() throws IOException {
    String translation = translateSourceFile(callableHeader + "class Test { Callable f = () -> 1;}",
        "Test", "Test.m");
    assertTranslation(translation, "^id(id _self)");
  }

  public void testTypeInference() throws IOException {
    String quadObjectTranslation = translateSourceFile(
        fourToOneHeader + "class Test { FourToOne f = (a, b, c, d) -> 1;}", "Test", "Test.m");
    assertTranslatedSegments(quadObjectTranslation, "@selector(applyWithId:withId:withId:withId:)",
        "^id(id _self, id a, id b, id c, id d)");
    String mixedObjectTranslation = translateSourceFile(fourToOneHeader
        + "class Test { FourToOne<String, Double, Integer, Boolean, String> f = "
        + "(a, b, c, d) -> \"1\";}", "Test", "Test.m");
    assertTranslation(mixedObjectTranslation,
        "^NSString *(id _self, NSString * a, JavaLangDouble * b, JavaLangInteger * c, JavaLangBoolean * d)");
  }

  public void testOuterFunctions() throws IOException {
    String translation = translateSourceFile(
        functionHeader + "class Test { Function outerF = (x) -> x;}", "Test", "Test.m");
    assertTranslation(translation, "JreStrongAssign(&self->outerF_, GetNonCapturingLambda");
  }

  public void testStaticFunctions() throws IOException {
    String translation = translateSourceFile(
        functionHeader + "class Test { static Function staticF = (x) -> x;}", "Test", "Test.m");
    assertTranslatedSegments(translation, "id<Function> Test_staticF;",
        "if (self == [Test class]) {", "JreStrongAssign(&Test_staticF, GetNonCapturingLambda");
  }

  public void testNestedLambdas() throws IOException {
    String outerCapture = translateSourceFile(functionHeader
        + "class Test { Function<String, Function<String, String>> f = x -> y -> x;}", "Test",
        "Test.m");
    assertTranslatedSegments(outerCapture, "GetNonCapturingLambda", "@selector(applyWithId:)",
        "^id<Function>(id _self, NSString * x)", "return GetCapturingLambda",
        "@selector(applyWithId:)", "^NSString *(id _self, NSString * y)", "return x;");
    String noCapture = translateSourceFile(functionHeader
        + "class Test { Function<String, Function<String, String>> f = x -> y -> y;}", "Test",
        "Test.m");
    assertTranslatedSegments(noCapture, "GetNonCapturingLambda",
        "@selector(applyWithId:)", "^id<Function>(id _self, NSString * x)",
        "return GetNonCapturingLambda", "@selector(applyWithId:)",
        "^NSString *(id _self, NSString * y)", "return y;");
  }

  // Test that we are properly adding protocols for casting.
  public void testProtocolCast() throws IOException {
    String translation = translateSourceFile(
        functionHeader + "class Test { Function f = (Function) (x) -> x;}", "Test", "Test.m");
    assertTranslatedSegments(translation,
        "(id<Function>) cast_check(GetNonCapturingLambda(@protocol(Function), ",
        "Function_class_()");
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
    assertTranslatedSegments(translation, "@\"Test_lambda$", "@\"Test_Foo_Bar_lambda");
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
    assertTranslatedSegments(translation, "^id(id _self, id a, id b, id c, id d, id e, id f, id g,",
        " id bs, id bt, id bu, id bv, id bw, id bx, id by, id bz, id ca)",
        "return block(_self, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, ",
        "bn, bo, bp, bq, br, bs, bt, bu, bv, bw, bx, by, bz, ca);",
        "^id(id _self, id a, id b, id c, id d, id e, id f, id g, id h, id i, id j, id k, id l, ",
        "id bw, id bx, id by, id bz, id bar){");
  }

  public void testCapturingBasicTypeReturn() throws IOException {
    String header = "interface I { int foo(); }";
    String translation = translateSourceFile(
        header + "class Test { int f = 1234; " + "  void foo() { I i = () -> f; } }", "Test",
        "Test.m");
    assertTranslatedLines(translation, "^jint(id _self){", "return f_;");
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
}
