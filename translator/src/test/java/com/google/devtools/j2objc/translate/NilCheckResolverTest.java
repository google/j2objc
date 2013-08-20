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
      "public class A {" +
      "  int length(char[] s) { return s.length; } void test() { length(null);}}",
      "A", "A.m");
    assertTranslation(translation, "return (int) [((IOSCharArray *) nil_chk(s)) count];");
  }

  public void testNilCheckOnCastExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int i; void test(Object o) { int i = ((Test) o).i; } }", "Test", "Test.m");
    assertTranslation(translation, "((Test *) nil_chk(o))->i_");
  }
}
