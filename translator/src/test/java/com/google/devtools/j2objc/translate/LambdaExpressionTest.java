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
 * Unit tests for {@link LambdaExpression}.
 *
 * @author Seth Kirby
 */
// TODO(kirbs): Add translation golden master tests when Lambda output is more well defined.
public class LambdaExpressionTest extends GenerationTest {

  private String functionHeader = "interface Function<T, R> {"
      + "R apply(T t);"
      + "}";

  // Test the creation of explicit blocks for lambdas with expression bodies.
  public void testBlockBodyCreation() throws IOException {
    String translation = translateSourceFile(
     functionHeader + "class Test { Function f = x -> x;}",
     "Test", "Test.m");
    String translation2 = translateSourceFile(
        functionHeader + "class Test { Function f = x -> { return x;}; }",
        "Test", "Test.m");
    assertEquals(translation, translation2);
  }
}
