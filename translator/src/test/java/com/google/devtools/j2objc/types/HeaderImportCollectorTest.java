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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.GenerationTest;

import java.io.IOException;

/**
 * Unit tests for the {@link HeaderImportCollector} class.
 *
 * @author Keith Stanger
 */
public class HeaderImportCollectorTest extends GenerationTest {

  public void testVarargsDeclarations() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test1(double... values) {} void test2(Runnable... values) {} }",
        "Test", "Test.h");
    assertTranslation(translation, "@class IOSDoubleArray");
    assertTranslation(translation, "@class IOSObjectArray");
    assertNotInTranslation(translation, "@protocol JavaLangRunnable");
  }
}
