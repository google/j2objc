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
 * Unit tests for {@link Rewriter}.
 *
 * @author Keith Stanger
 */
public class GwtConverterTest extends GenerationTest {

  public void testGwtCreate() throws IOException {
    addSourceFile(
        "package com.google.gwt.core.client;" +
        "public class GWT { public static <T> T create(Class<T> classLiteral) { return null; } }",
        "com/google/gwt/core/client/GWT.java");
    String translation = translateSourceFile(
        "import com.google.gwt.core.client.GWT;" +
        "class Test { " +
        "  Test INSTANCE = GWT.create(Test.class);" +
        "  String FOO = foo();" +  // Regression requires subsequent non-mapped method invocation.
        "  static String foo() { return \"foo\"; } }", "Test", "Test.m");
    assertTranslation(translation, "JreOperatorRetainedAssign(&INSTANCE_, " +
        "[[IOSClass classWithClass:[Test class]] newInstance]);");
  }
}
