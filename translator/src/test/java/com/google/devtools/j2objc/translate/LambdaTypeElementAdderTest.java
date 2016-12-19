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
 * Unit tests for {@link LambdaTypeElementAdder} class.
 *
 * @author Keith Stanger
 */
public class LambdaTypeElementAdderTest extends GenerationTest {

  public void testWeakOuterLambda() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.WeakOuter;"
        + "interface Foo { int Foo(); }"
        + "class Test { int i; void test() {"
        + "@WeakOuter Foo f = () -> i;"
        + "f = () -> i + 1; } }", "Test", "Test.m");
    // Verify that both lambdas have weak outers. This tests both VariableDeclaration and Assignment
    // as parent nodes of the lambda.
    assertOccurrences(translation, "__unsafe_unretained Test *this$0_;", 2);
  }
}
