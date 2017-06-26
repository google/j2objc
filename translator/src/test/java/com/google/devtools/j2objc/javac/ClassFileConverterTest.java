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
        "  void hello(String a);",
//        "  boolean world(boolean a, int b, float c, double d,",
//        "      boolean[] e, int[] f, float[] g, double[] h);",
        "  boolean world(boolean a, int... b);",
        "  boolean world(boolean a, int[]... b);",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testDefaultStaticInterface() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "interface Test {",
        "  void hello(String a);",
        "  static void world() {}",
        "  static String world(String a) { return a; }",
        "  default String bye(String a) { return a; }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testEmptyClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testMethodClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  String s;",
        "  int i, j, k;",
        "  void hello(String a) {}",
//        "  boolean world(boolean a, int b, float c, double d,",
//        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
        "  boolean world(boolean a, int... b) { return a; }",
        "  boolean world(boolean a, int[]... b) { return a; }",
        "  Test(String s, int i, int j, int k) {",
        "    this.s = s;",
        "    this.i = i;",
        "    this.j = j;",
        "    this.k = k;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testStaticMethodClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  static String s;",
        "  static int i, j, k;",
        "  static void hello(String a) {}",
//        "  static boolean world(boolean a, int b, float c, double d,",
//        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
        "  static boolean world(boolean a, int... b) { return a; }",
        "  static boolean world(boolean a, int[]... b) { return a; }",
        "  Test(String s, int i, int j, int k) {",
        "    this.s = s;",
        "    this.i = i;",
        "    this.j = j;",
        "    this.k = k;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testEmptyAbstractClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testMethodAbstractClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {",
        "  String s;",
        "  int i, j, k;",
        "  void hello(String a) {}",
//        "  boolean world(boolean a, int b, float c, double d,",
//        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
        "  abstract boolean world(boolean a, int... b);",
        "  abstract boolean world(boolean a, int[]... b);",
        "  Test(String s, int i, int j, int k) {",
        "    this.s = s;",
        "    this.i = i;",
        "    this.j = j;",
        "    this.k = k;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testStaticMethodAbstractClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {",
        "  static String s;",
        "  static int i, j, k;",
        "  void hello(String a) {}",
//        "  boolean world(boolean a, int b, float c, double d,",
//        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
        "  static boolean world(boolean a, int... b) { return a; }",
        "  static boolean world(boolean a, int[]... b) { return a; }",
        "  Test(String s, int i, int j, int k) {",
        "    this.s = s;",
        "    this.i = i;",
        "    this.j = j;",
        "    this.k = k;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testMixedAbstractClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {",
        "  static String s;",
        "  int i, j, k;",
        "  static void hello(String a) {}",
//        "  static boolean world(boolean a, int b, float c, double d,",
//        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
        "  abstract boolean world(boolean a, int... b);",
        "  boolean world(boolean a, int[]... b) { return a; }",
        "  Test() {",
        "  }",
        "  Test(String s, int i, int j, int k) {",
        "    this.s = s;",
        "    this.i = i;",
        "    this.j = j;",
        "    this.k = k;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testConstantFields() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  static final boolean a = true;",
        "  static final char b = 'H';",
        "  static final byte c = 17;",
        "  static final short d = 42;",
        "  static final int e = 1984;",
        "  static final long f = 87539319;",
       /* TODO(user): format specifiers, casts, floating point roundoff
        * "  static final float g = 3.14f;",
        * "  static final float h = (float) 3.14;",
        * "  static final double gh = 3.14f;", */
        "  static final double i = 2.718;",
        "  static final String j = \"Hello\";",
        "  final boolean aa = true;",
        "  final char bb = 'H';",
        "  final byte cc = 17;",
        "  final short dd = 42;",
        "  final int ee = 1984;",
        "  final long ff = 87539319;",
       /* TODO(user): format specifiers, casts, floating point roundoff
        * "  final float gg = 3.14f;",
        * "  final float hh = (float) 3.14;",
        * "  final double ghgh = 3.14f;", */
        "  final double ii = 2.718;",
        "  final String jj = \"Hello\";",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testFieldMethodModifiers() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {",
        "  private static final int sfa = 0;",
        "  static final int sfb = 0;",
        "  protected static final int sfc = 0;",
        "  public static final int sfd = 0;",
        "  abstract int bm();",
        "  abstract protected int cm();",
        "  abstract public int dm();",
        "  private static final int sfam() { return 0; }",
        "  static final int sfbm() { return 0; }",
        "  protected static final int sfcm() { return 0; }",
        "  public static final int sfdm() { return 0; }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }
}
