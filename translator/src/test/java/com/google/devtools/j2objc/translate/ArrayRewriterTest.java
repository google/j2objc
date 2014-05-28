/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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
 * Unit tests for {@link ArrayRewriter} class.
 *
 * @author Tom Ball
 */
public class ArrayRewriterTest extends GenerationTest {

  // Since ArrayRewriter was created from code scattered throughout the package,
  // so are its tests. These should be moved here as they are updated.

  // Issue 360: a null argument for a varargs parameter should not be rewritten
  // as an array.
  public void testNilVarargs() throws IOException {
    String source =
        "public class Test { " +
        "  void foo(char... chars) {}" +
        "  void test() { foo(null); }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "[self fooWithCharArray:nil];");
    assertNotInTranslation(translation,
        "[self fooWithCharArray:[IOSCharArray arrayWithChars:(unichar[]){ nil } count:1]];");
  }
}
