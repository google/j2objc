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
 * Unit tests for {@link MethodReference}.
 *
 * @author Seth Kirby
 */
public class MethodReferenceTest extends GenerationTest {
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

  // Test the creation of explicit blocks for lambdas with expression bodies.
  public void testCreationReferenceBlockWrapper() throws IOException {
    String creationReferenceHeader =
        "class I { I() { } I(int x) { } I(int x, I j, String s, Object o) { } }\n"
        + "interface FunInt<T> { T apply(int x); }"
        + "interface FunInt4<T> { T apply(int x, I j, String s, Object o); }"
        + "interface Call<T> { T call(); }";

    String noArgumentTranslation = translateSourceFile(
        creationReferenceHeader + "class Test { Call<I> iInit = I::new; }",
        "Test", "Test.m");
    assertTranslatedSegments(noArgumentTranslation,
 "GetNonCapturingLambda(@protocol(Call)",
        "@\"I_init\"", "^I *(id _self) {", "return new_I_init();");
    String oneArgumentTranslation = translateSourceFile(
        creationReferenceHeader + "class Test { FunInt<I> iInit2 = I::new; }", "Test", "Test.m");
    assertTranslatedSegments(oneArgumentTranslation,
 "GetNonCapturingLambda(@protocol(FunInt)",
        "@\"I_initWithInt_\"",
        "^I *(id _self, jint a) {", "return new_I_initWithInt_(a);");
    String mixedArgumentTranslation = translateSourceFile(
        creationReferenceHeader + "class Test { FunInt4<I> iInit3 = I::new; }", "Test", "Test.m");
    assertTranslatedSegments(mixedArgumentTranslation,
 "GetNonCapturingLambda(@protocol(FunInt4)",
        "@\"I_initWithInt_withI_withNSString_withId_\"",
        "^I *(id _self, jint a, I * b, NSString * c, id d) {",
        "return new_I_initWithInt_withI_withNSString_withId_(a, b, c, d);");
  }

  // Test that expression method references resolve correctly for static and non-static methods.
  public void testExpressionReferenceStaticResolution() throws IOException {
    String expressionReferenceHeader = "class Q { static Object o(Object x) { return x; }\n"
        + "    Object o2(Object x) { return x; }}" + "interface F<T, R> { R f(T t); }";
    String staticTranslation = translateSourceFile(
        expressionReferenceHeader + "class Test { F fun = Q::o; }",
        "Test", "Test.m");
    assertTranslatedSegments(staticTranslation, "GetNonCapturingLambda", "@\"Q_oWithId_\"",
        "@selector(fWithId:)", "return Q_oWithId_(a);");
    String instanceTranslation = translateSourceFile(
        expressionReferenceHeader + "class Test { F fun = new Q()::o2; }",
        "Test", "Test.m");
    assertTranslatedSegments(instanceTranslation, "GetNonCapturingLambda", "@\"Q_o2WithId_\"",
        "@selector(fWithId:)", "return [[new_Q_init() autorelease] o2WithId:a];");
  }

  public void testTypeReference() throws IOException {
    // TODO(kirbs): Find and test more examples of type method references.
    String typeReferenceHeader = "interface H { Object copy(int[] i); }";
    String translation = translateSourceFile(
        typeReferenceHeader + "class Test { H h = int[]::clone; }", "Test", "Test.m");
    assertTranslatedSegments(translation, "GetNonCapturingLambda", "@selector(copy__WithIntArray:)",
        "^IOSIntArray *(id _self, IOSIntArray * a)", "return [a clone];");
  }

  public void testVarArgs() throws IOException {
    String varArgsHeader = "interface I { void foo(int a1, String a2, String a3); }"
        + "interface I2 { void foo(int a1, String a2, String a3, String a4); }"
        + "class Y { static void m(int a1, String... rest) { } }";
    String translation = translateSourceFile(
        varArgsHeader + "class Test { I i = Y::m; I2 i2 = Y::m; }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "JreStrongAssign(&self->i_, GetNonCapturingLambda(@protocol(I), "
        + "@\"Y_mWithInt_withNSString_withNSString_\", "
        + "@selector(fooWithInt:withNSString:withNSString:),",
        "^void(id _self, jint a, NSString * b, NSString * c) {",
        "Y_mWithInt_withNSStringArray_(a, [IOSObjectArray arrayWithObjects:(id[]){ b, c } "
        + "count:2 type:NSString_class_()]);",
        "}));",
        "JreStrongAssign(&self->i2_, GetNonCapturingLambda(@protocol(I2), "
        + "@\"Y_mWithInt_withNSString_withNSString_withNSString_\", "
        + "@selector(fooWithInt:withNSString:withNSString:withNSString:),",
        "^void(id _self, jint a, NSString * b, NSString * c, NSString * d) {",
        "Y_mWithInt_withNSStringArray_(a, [IOSObjectArray arrayWithObjects:(id[]){ b, c, d } "
        + "count:3 type:NSString_class_()]);");
  }
}
