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
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.IOException;

/**
 * Unit tests for {@link OcniExtractor}.
 *
 * @author Tom Ball, Keith Stanger
 */
public class OcniExtractorTest extends GenerationTest {

  public void testBadNativeCodeBlock() throws IOException {
    // Bad native code blocks should just be ignored comments.
    String translation = translateSourceFile(
        "public class Example { native void test() /* -[ ]-*/; }",
        "Example", "Example.m");

    // Implementation should be functionized.
    assertTranslatedLines(translation,
        "- (void)test {",
        "  Example_test(self);",
        "}");
  }

  public void testBadNativeCodeBlock_badEndDelimiter() throws IOException {
    maybeCompileType("Example", "public class Example { native void test() /*-[ ]*/; }");
    assertTrue(ErrorUtil.getErrorMessages()
        .contains("Error finding OCNI closing delimiter for OCNI comment at line 1"));
  }

  public void testHeaderOcniBlock() throws IOException {
    String translation = translateSourceFile(
        "/*-HEADER[ outside OCNI ]-*/ class Test { /*-HEADER[ inside OCNI ]-*/ }",
        "Test", "Test.h");
    assertTranslatedSegments(translation, "outside OCNI", "@interface Test", "inside OCNI", "@end");
  }
}
