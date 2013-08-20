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
 * Tests for {@link CopyAllFieldsWriter}.
 *
 * @author Keith Stanger
 */
public class CopyAllFieldsWriterTest extends GenerationTest {

  public void testCopyAllFieldsMethod() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  int var1, var2;" +
        "  static int var3;" +
        "}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)copyAllFieldsTo:(Test *)other {",
        "[super copyAllFieldsTo:other];",
        "other->var1_ = var1_;",
        "other->var2_ = var2_;",
        "}");
  }
}
