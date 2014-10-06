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
 * Unit tests for {@link CastResolver}.
 *
 * @author Keith Stanger
 */
public class CastResolverTest extends GenerationTest {

  public void testFieldOfGenericParameter() throws IOException {
    String translation = translateSourceFile(
        "class Test { int foo; static class Other<T extends Test> {"
        + " int test(T t) { return t.foo + t.foo; } } }", "Test", "Test.m");
    assertTranslation(translation, "return ((Test *) nil_chk(t))->foo_ + ((Test *) t)->foo_;");
  }
}
