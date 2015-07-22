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

  private String classIHeader =
      "class I { I() { } I(int x) { } I(int x, I j, String s, Object o) { } }\n"
      + "interface FunInt<T> { T apply(int x); }"
      + "interface FunInt4<T> { T apply(int x, I j, String s, Object o); }"
      + "interface Call<T> { T call(); }";

  // Test the creation of explicit blocks for lambdas with expression bodies.
  public void testCreationReferenceBlockWrapper() throws IOException {
    String noArgumentTranslation = translateSourceFile(
        classIHeader + "class Test { Call<I> iInit = I::new; }",
        "Test", "Test.m");
    assertTranslatedSegments(noArgumentTranslation,
        "GetNonCapturingLambda([Call class], @protocol(Call)",
        "@\"I_init\"", "^I *(id _self) {", "return new_I_init();");
    String oneArgumentTranslation = translateSourceFile(
        classIHeader + "class Test { FunInt<I> iInit2 = I::new; }", "Test", "Test.m");
    assertTranslatedSegments(oneArgumentTranslation,
        "GetNonCapturingLambda([FunInt class], @protocol(FunInt)", "@\"I_initWithInt_\"",
        "^I *(id _self, jint a) {", "return new_I_initWithInt_(a);");
    String mixedArgumentTranslation = translateSourceFile(
        classIHeader + "class Test { FunInt4<I> iInit3 = I::new; }", "Test", "Test.m");
    assertTranslatedSegments(mixedArgumentTranslation,
        "GetNonCapturingLambda([FunInt4 class], @protocol(FunInt4)",
        "@\"I_initWithInt_withI_withNSString_withId_\"",
        "^I *(id _self, jint a, I * b, NSString * c, id d) {",
        "return new_I_initWithInt_withI_withNSString_withId_(a, b, c, d);");
  }
}
