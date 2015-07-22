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
    assertTranslatedSegments(translation, "id<Function> Test_staticF_;",
        "if (self == [Test class]) {", "JreStrongAssign(&Test_staticF_, GetNonCapturingLambda");
  }

  public void testNestedLambdas() throws IOException {
    String outerCapture = translateSourceFile(functionHeader
        + "class Test { Function<String, Function<String, String>> f = x -> y -> x;}", "Test",
        "Test.m");
    assertTranslatedSegments(outerCapture, "GetNonCapturingLambda", "[Function class]",
        "@selector(applyWithId:)", "^id<Function>(id _self, NSString * x)",
        "return GetCapturingLambda", "[Function class]", "@selector(applyWithId:)",
        "^NSString *(id _self, NSString * y)", "return x;");
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
        "(id<Function>) check_protocol_cast(GetNonCapturingLambda([Function class], @protocol(Function), ",
        "@protocol(Function)");
  }

  // Test that we aren't trying to import lambda types.
  public void testImportExclusion() throws IOException {
    String translation = translateSourceFile(
        functionHeader + "class Test { Function f = (Function) (x) -> x;}", "Test", "Test.m");
    assertNotInTranslation(translation, "lambda$0.h");
  }

  // Check that lambdas are uniquely named.
  public void testLambdaUniquify() throws IOException {
    String translation = translateSourceFile(
functionHeader
        + "class Test { class Foo{ class Bar { Function f = x -> x; }}\n"
        + "Function f = x -> x;}",
        "Test", "Test.m");
    assertTranslatedSegments(translation, "@\"Test_lambda$", "@\"Test_Foo_Bar_lambda");
  }
}
