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

package com.google.devtools.j2objc.javac;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

/**
 * Tests for {@link ClassFileConverter}.
 *
 * Note: classfile conversion is experimental and not supported.
 */
public class ClassFileConverterTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    options.setTranslateClassfiles(true);
  }

  public void testEmptyInterface() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "interface Test {}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testMethodInterface() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "interface Test {",
        "  void hello();",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testMethodParamsInterface() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "interface Test {",
        "  void hello(boolean a);",
// TODO(tball): javac gives incorrect parameter names
//        "  boolean world(boolean a, int b, float c, double d,",
//        "      boolean[] e, int[] f, float[] g, double[] h);",
        "  boolean world(boolean a, int... b);",
        "  boolean world(boolean a, int[]... b);",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

// TODO(user): will uncomment after finishing AST toString method that ignores method bodies
//  public void testStaticMethodInterface() throws IOException {
//    String type = "foo.bar.Test";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "interface Test {",
//        "  void hello();",
//        "  static void world() {}",
//        "  static boolean world(boolean a) { return a; }",
//        "}"
//    );
//    assertEqualSrcClassfile(type, source);
//  }
}
