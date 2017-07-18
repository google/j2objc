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
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testMethodInterface() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "interface Test {",
        "  void hello();",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testMethodParamsInterface() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "interface Test {",
        "  void hello(String a);",
        "  boolean world(boolean a, int b, float c, double d,",
        "      boolean[] e, int[] f, float[] g, double[] h);",
        "  boolean world(boolean a, int... b);",
        "  boolean world(boolean a, int[]... b);",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
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
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testEmptyClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testMethodClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  String s;",
        "  int i, j, k;",
        "  void hello(String a) {}",
        "  boolean world(boolean a, int b, float c, double d,",
        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
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
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testStaticMethodClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  static String s;",
        "  static int i, j, k;",
        "  static void hello(String a) {}",
        "  static boolean world(boolean a, int b, float c, double d,",
        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
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
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testEmptyAbstractClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testMethodAbstractClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {",
        "  String s;",
        "  int i, j, k;",
        "  void hello(String a) {}",
        "  boolean world(boolean a, int b, float c, double d,",
        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
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
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testStaticMethodAbstractClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {",
        "  static String s;",
        "  static int i, j, k;",
        "  void hello(String a) {}",
        "  boolean world(boolean a, int b, float c, double d,",
        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
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
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testMixedAbstractClass() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "abstract class Test {",
        "  static String s;",
        "  int i, j, k;",
        "  static void hello(String a) {}",
        "  static boolean world(boolean a, int b, float c, double d,",
        "      boolean[] e, int[] f, float[] g, double[] h) { return a; }",
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
    assertEqualSignatureSrcClassfile(type, source);
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
    assertEqualSignatureSrcClassfile(type, source);
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
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testSimpleEnum() throws IOException {
    String type = "foo.bar.Day";
    String source = String.join("\n",
        "package foo.bar;",
        "enum Day {",
        "  SUNDAY,",
        "  MONDAY,",
        "  TUESDAY,",
        "  WEDNESDAY,",
        "  THURSDAY,",
        "  FRIDAY,",
        "  SATURDAY",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

/* TODO(user): enum constants are created in static initializer; executable pairs not complete
 * cite example: https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html */
//  public void testInitializedEnum() throws IOException {
//    String type = "foo.bar.Planet";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "enum Planet {",
//        "  MERCURY (3.303e+23, 2.4397e6),",
//        "  VENUS   (4.869e+24, 6.0518e6),",
//        "  EARTH   (5.976e+24, 6.37814e6),",
//        "  MARS    (6.421e+23, 3.3972e6),",
//        "  JUPITER (1.9e+27,   7.1492e7),",
//        "  SATURN  (5.688e+26, 6.0268e7),",
//        "  URANUS  (8.686e+25, 2.5559e7),",
//        "  NEPTUNE (1.024e+26, 2.4746e7);",
//        "  private final double mass;   // in kilograms",
//        "  private final double radius; // in meters",
//        "  Planet(double mass, double radius) {",
//        "    this.mass = mass;",
//        "    this.radius = radius;",
//        "  }",
//        "  private double mass() { return mass; }",
//        "  private double radius() { return radius; }",
//        "  /* universal gravitational constant  (m3 kg-1 s-2) */",
//        "  public static final double G = 6.67300E-11;",
//        "  double surfaceGravity() {",
//        "    return G * mass / (radius * radius);",
//        "  }",
//        "  double surfaceWeight(double otherMass) {",
//        "    return otherMass * surfaceGravity();",
//        "  }",
//        "}"
//    );
//    assertEqualSignatureSrcClassfile(type, source);
//  }

  private String nameToPath(String name) {
    return name.replace('.', '/') + ".java";
  }

  public void testPredefinedAnnotations() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  /**",
        "   * @deprecated",
        "   * integer overflow concern",
        "   */",
        "  @Deprecated",
        "  int find_middle(int a, int b) { return (a + b)/2; }",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testSingleMemberAnnotations() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  /**",
        "   * @deprecated",
        "   * integer overflow concern",
        "   */",
        "  @Deprecated",
        "  @SuppressWarningsClass(\"deprecation\")",
        "  int find_middle(int a, int b) { return (a + b)/2; }",
        "  @SuppressWarningsClass(\"fallthrough\")",
        "  int fibonacci(int i) {",
        "    switch (i) {",
        "      case 0:",
        "      case 1:",
        "        return 1;",
        "      default:",
        "        return fibonacci(i - 1) + fibonacci(i - 2);",
        "    }",
        "  }",
        "}"
    );
    String annotationType = "foo.bar.SuppressWarningsClass";
    String annotationSource =
        "package foo.bar; @interface SuppressWarningsClass { String value(); }";
    addSourceFile(annotationSource, nameToPath(annotationType));
    assertEqualSignatureSrcClassfile(annotationType, annotationSource);
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testArrayAnnotations() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "@ArrayAnnot(",
        "  strings = {\"Hello\", \"World\", \"Goodbye\"}",
        ")",
        "class Test {}"
    );
    String annotationType = "foo.bar.ArrayAnnot";
    String annotationSource =
        "package foo.bar; @interface ArrayAnnot { String[] strings(); }";
    addSourceFile(annotationSource, nameToPath(annotationType));
    assertEqualSignatureSrcClassfile(annotationType, annotationSource);
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testAnnotationType() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "@Preamble(",
        "  author = \"John Doe\",",
        "  date = \"3/17/2002\",",
        "  currentRevision = 6,",
        "  lastModified = \"4/12/2004\",",
        "  lastModifiedBy = \"Jane Doe\",",
        "  // Note array notation",
        "  reviewers = {\"Alice\", \"Bob\", \"Cindy\"}",
        ")",
        "class Test {}"
    );
    String annotationType = "foo.bar.Preamble";
    String annotationSource = String.join("\n",
        "package foo.bar;",
        "@interface Preamble {",
        "  String author();",
        "  String date();",
        "  int currentRevision() default 1;",
        "  String lastModified() default \"N/A\";",
        "  String lastModifiedBy() default \"N/A\";",
        "  String[] reviewers();",
        "}");
    addSourceFile(annotationSource, nameToPath(annotationType));
    assertEqualSignatureSrcClassfile(annotationType, annotationSource);
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testDefaultsAnnotationType() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "@Preamble(",
        "  author = \"John Doe\",",
        "  date = \"3/17/2002\",",
        "  currentRevision = 6,",
        "  // Note array notation",
        "  reviewers = {\"Alice\", \"Bob\", \"Cindy\"}",
        ")",
        "class Test {}"
    );
    String annotationType = "foo.bar.Preamble";
    String annotationSource = String.join("\n",
        "package foo.bar;",
        "@interface Preamble {",
        "  String author();",
        "  String date();",
        "  int currentRevision() default 1;",
        "  String lastModified() default \"N/A\";",
        "  String lastModifiedBy() default \"N/A\";",
        "  String[] reviewers();",
        "}");
    addSourceFile(annotationSource, nameToPath(annotationType));
    assertEqualSignatureSrcClassfile(annotationType, annotationSource);
    assertEqualSignatureSrcClassfile(type, source);
  }

/* TODO(user): enum constants are written with their fully qualified names in classfiles */
//  public void testAnnotatedAnnotationType() throws IOException {
//    String type = "foo.bar.Test";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "@Preamble(",
//        "    author = \"John Doe\",",
//        "    date = \"3/17/2002\"",
//        ")",
//        "@ModificationData",
//        "class Test {}"
//    );
//    String annotationType1 = "foo.bar.Preamble";
//    String annotationSource1 = String.join("\n",
//        "package foo.bar;",
//        "import java.lang.annotation.Documented;",
//        "import java.lang.annotation.Retention;",
//        "import static java.lang.annotation.RetentionPolicy.RUNTIME;",
//        "@Documented",
//        "@Retention(RUNTIME)",
//        "@interface Preamble {",
//        "  String author();",
//        "  String date();",
//        "}"
//    );
//    String annotationType2 = "foo.bar.ModificationData";
//    String annotationSource2 = String.join("\n",
//        "package foo.bar;",
//        "import java.lang.annotation.Documented;",
//        "import java.lang.annotation.Retention;",
//        "import java.lang.annotation.RetentionPolicy;",
//        "@Documented",
//        "@Retention(RetentionPolicy.CLASS)",
//        "@interface ModificationData {",
//        "  String lastModified() default \"N/A\";",
//        "  String lastModifiedBy() default \"N/A\";",
//        "}"
//    );
//    addSourceFile(annotationSource1, nameToPath(annotationType1));
//    addSourceFile(annotationSource2, nameToPath(annotationType2));
//    assertEqualSignatureSrcClassfile(annotationType1, annotationSource1);
//    assertEqualSignatureSrcClassfile(annotationType2, annotationSource2);
//    assertEqualSignatureSrcClassfile(type, source);
//  }

  public void testExtendInterface() throws IOException {
    String type = "foo.bar.Range";
    String source = String.join("\n",
        "package foo.bar;",
        "import java.util.Iterator;",
        "interface Range extends Iterator<Integer> {",
        "  Integer start();",
        "  Integer end();",
        "  Integer skip();",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testExtendInterfaceParametrized() throws IOException {
    String type = "foo.bar.Range";
    String source = String.join("\n",
        "package foo.bar;",
        "import java.util.Iterator;",
        "interface Range<E> extends Iterator<E> {",
        "  E start();",
        "  E end();",
        "  E skip();",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testExtendClass() throws IOException {
    String type = "foo.bar.Range";
    String source = String.join("\n",
        "package foo.bar;",
        "import java.util.ArrayList;",
        "class Range extends ArrayList<Integer> {",
        "  Range(int start, int end, int skip) {",
        "    super();",
        "    for (int i = start; i < end; i += skip) {",
        "      this.add(i);",
        "    }",
        "  }",
        "  Range(int start, int end) {",
        "    this(start, end, 1);",
        "  }",
        "  Range(int end) {",
        "    this(0, end);",
        "  }",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testExtendClassParametrized() throws IOException {
    String type = "foo.bar.ChooseSet";
    String source = String.join("\n",
        "package foo.bar;",
        "import java.util.Iterator;",
        "import java.util.HashSet;",
        "class ChooseSet<E> extends HashSet<E> {",
        "  E choose() {",
        "    Iterator<E> iter = this.iterator();",
        "    return iter.hasNext() ? iter.next() : null;",
        "  }",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testImplement() throws IOException {
    String type = "foo.bar.Unit";
    String source = String.join("\n",
        "package foo.bar;",
        "import java.lang.Comparable;",
        "public class Unit implements Comparable<Unit> {",
        "  public int compareTo(Unit other) {",
        "    return 0;",
        "  }",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testImplementParametrized() throws IOException {
    String type = "foo.bar.Smallest";
    String source = String.join("\n",
        "package foo.bar;",
        "import java.lang.Comparable;",
        "public class Smallest<E> implements Comparable<E> {",
        "  public int compareTo(E other) {",
        "    return -1;",
        "  }",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testImplementParametrizedNested() throws IOException {
    String type = "foo.bar.Thing";
    String source = String.join("\n",
        "package foo.bar;",
        "import java.lang.Comparable;",
        "import java.lang.Runnable;",
        "public class Thing<S, T extends Comparable<T> & Runnable>",
        "    implements Comparable<Thing<S, T>> {",
        "  public int compareTo(Thing<S, T> other) {",
        "    return 1;",
        "  }",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

  public void testGenericMethods() throws IOException {
    String type = "foo.bar.Thing";
    String source = String.join("\n",
        "package foo.bar;",
        "import java.lang.Comparable;",
        "import java.util.Map;",
        "public class Thing<K, V> {",
        "  public void simple(Map<K, V> map) {}",
        "  public void simpleWildcard(Map<? extends K, ? super V> map) {}",
        "  public void nested(Map<Map<K, V>, Map<K, V>> map) {}",
        "  public void nestedWildcard(Map<? extends Map<? extends K, ? super V>,",
        "                                 ? super Map<? extends K, ? super V>> map) {}",
        "  public <S, T> void simpleParam(Map<S, T> map) {}",
        "  public <S extends Map<K, V> & Comparable<K>,",
        "          T extends Map<K, V> & Comparable<V>> void boundedParam(Map<S, T> map) {}",
        "  public <S extends Map<? extends K, ? super V> & Comparable<K>,",
        "          T extends Map<? extends K, ? super V> & Comparable<V>>",
        "      void boundedWildcardParam(Map<? extends S, ? super T> map) {}",
        "}"
    );
    assertEqualSignatureSrcClassfile(type, source);
  }

//  public void testMethodReturn() throws IOException {
//    String type = "foo.bar.Test";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "class Test {",
//        "  public int run() {",
//        "    return 0;",
//        "  }",
//        "}"
//    );
//    assertEqualASTSrcClassfile(type, source);
//  }
}
