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
    enableDebuggingSupport();
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
        "  boolean world(boolean a, int b, float c, double d,",
        "      boolean[] e, int[] f, float[] g, double[] h);",
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

  public void testHelloClass() throws IOException {
    String source = String.join("\n",
        "class Hello {",
        "  public static void main(String... args) {",
        // Use fully-qualified name since classfile types are always fully-qualified.
        "    java.lang.System.out.println(\"hello, world\");",
        "  }",
        "}"
    );
    assertEqualSrcClassfile("Hello", source);
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
        "  static final long f = 87539319L;",
        "  static final float g = 3.14F;",
       /* TODO(manvithn): format specifiers, casts, floating point roundoff
        * "  static final float h = (float) 3.14;",
        * "  static final double gh = 3.14F;", */
        "  static final double i = 2.718D;",
        "  static final String j = \"Hello\";",
        "  final boolean aa = true;",
        "  final char bb = 'H';",
        "  final byte cc = 17;",
        "  final short dd = 42;",
        "  final int ee = 1984;",
        "  final long ff = 87539319L;",
        "  final float gg = 3.14F;",
       /* TODO(manvithn): format specifiers, casts, floating point roundoff
        * "  final float hh = (float) 3.14;",
        * "  final double ghgh = 3.14F;", */
        "  final double ii = 2.718D;",
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

  public void testParameterizedClass() throws IOException {
    String source = String.join("\n",
        "package foo.bar;",
        "class StringList extends java.util.ArrayList<String> {}"
    );
    assertEqualSrcClassfile("foo.bar.StringList", source);
  }

//  public void testSimpleEnum() throws IOException {
//    String type = "foo.bar.Day";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "enum Day {",
//        "  SUNDAY,",
//        "  MONDAY,",
//        "  TUESDAY,",
//        "  WEDNESDAY,",
//        "  THURSDAY,",
//        "  FRIDAY,",
//        "  SATURDAY",
//        "}"
//    );
//    assertEqualSrcClassfile(type, source);
//  }
//
/* TODO(manvithn): enum constants are created in static initializer; executable pairs not complete
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
//    assertEqualSrcClassfile(type, source);
//  }
//
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
   assertEqualSrcClassfile(type, source);
 }

//  public void testSingleMemberAnnotations() throws IOException {
//    String type = "foo.bar.Test";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "class Test {",
//        "  /**",
//        "   * @deprecated",
//        "   * integer overflow concern",
//        "   */",
//        "  @Deprecated",
//        "  @SuppressWarningsClass(\"deprecation\")",
//        "  int find_middle(int a, int b) { return (a + b)/2; }",
//        "  @SuppressWarningsClass(\"fallthrough\")",
//        "  int fibonacci(int i) {",
//        "    switch (i) {",
//        "      case 0:",
//        "      case 1:",
//        "        return 1;",
//        "      default:",
//        "        return fibonacci(i - 1) + fibonacci(i - 2);",
//        "    }",
//        "  }",
//        "}"
//    );
//    String annotationType = "foo.bar.SuppressWarningsClass";
//    String annotationSource =
//        "package foo.bar; @interface SuppressWarningsClass { String value(); }";
//    addSourceFile(annotationSource, typeNameToSource(annotationType));
//    assertEqualSrcClassfile(annotationType, annotationSource);
//    assertEqualSrcClassfile(type, source);
//  }

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
    addSourceFile(annotationSource, typeNameToSource(annotationType));
    assertEqualSrcClassfile(annotationType, annotationSource);
    assertEqualSrcClassfile(type, source);
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
    addSourceFile(annotationSource, typeNameToSource(annotationType));
    assertEqualSrcClassfile(annotationType, annotationSource);
    assertEqualSrcClassfile(type, source);
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
    addSourceFile(annotationSource, typeNameToSource(annotationType));
    assertEqualSrcClassfile(annotationType, annotationSource);
    assertEqualSrcClassfile(type, source);
  }

  public void testAnnotatedAnnotationType() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "@Preamble(",
        "    author = \"John Doe\",",
        "    date = \"3/17/2002\"",
        ")",
        "@ModificationData",
        "class Test {}"
    );
    String annotationType1 = "foo.bar.Preamble";
    String annotationSource1 = String.join("\n",
        "package foo.bar;",
        "import java.lang.annotation.Documented;",
        "import java.lang.annotation.Retention;",
        "import static java.lang.annotation.RetentionPolicy.RUNTIME;",
        "@Documented",
        "@Retention(RUNTIME)",
        "@interface Preamble {",
        "  String author();",
        "  String date();",
        "}"
    );
    String annotationType2 = "foo.bar.ModificationData";
    String annotationSource2 = String.join("\n",
        "package foo.bar;",
        "import java.lang.annotation.Documented;",
        "import java.lang.annotation.Retention;",
        "import java.lang.annotation.RetentionPolicy;",
        "@Documented",
        "@Retention(RetentionPolicy.CLASS)",
        "@interface ModificationData {",
        "  String lastModified() default \"N/A\";",
        "  String lastModifiedBy() default \"N/A\";",
        "}"
    );
    addSourceFile(annotationSource1, typeNameToSource(annotationType1));
    addSourceFile(annotationSource2, typeNameToSource(annotationType2));
    assertEqualSrcClassfile(annotationType1, annotationSource1);
    assertEqualSrcClassfile(annotationType2, annotationSource2);
    assertEqualSrcClassfile(type, source);
  }

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
    assertEqualSrcClassfile(type, source);
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
    assertEqualSrcClassfile(type, source);
  }

//  public void testExtendClass() throws IOException {
//    String type = "foo.bar.Range";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "import java.util.ArrayList;",
//        "class Range extends ArrayList<Integer> {",
//        "  Range(int start, int end, int skip) {",
//        "    super();",
//        "    for (int i = start; i < end; i += skip) {",
//        "      this.add(i);",
//        "    }",
//        "  }",
//        "  Range(int start, int end) {",
//        "    this(start, end, 1);",
//        "  }",
//        "  Range(int end) {",
//        "    this(0, end);",
//        "  }",
//        "}"
//    );
//    assertEqualSrcClassfile(type, source);
//  }
//
//  public void testExtendClassParametrized() throws IOException {
//    String type = "foo.bar.ChooseSet";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "import java.util.Iterator;",
//        "import java.util.HashSet;",
//        "class ChooseSet<E> extends HashSet<E> {",
//        "  E choose() {",
//        "    Iterator<E> iter = this.iterator();",
//        "    return iter.hasNext() ? iter.next() : null;",
//        "  }",
//        "}"
//    );
//    assertEqualSrcClassfile(type, source);
//  }

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
    assertEqualSrcClassfile(type, source);
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
    assertEqualSrcClassfile(type, source);
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
    assertEqualSrcClassfile(type, source);
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
    assertEqualSrcClassfile(type, source);
  }

  public void testInterfaceMethod() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "interface Test {",
        "  int run();",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testMethodReturn() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int run() { return 0; }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testPrimitiveExpressions() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  byte getbyte() { return 127; }",
        "  short getshort() { return 32767; }",
       /* We use 2^31 - 2 and 2^63 - 2 because 2^31 - 1 and 2^63 - 1 get decompiled to
        * Integer.MAX_VALUE and Long.MAX_VALUE (also for MIN_VALUE) */
        "  int getint() { return 2147483646; }",
        "  long getlong() { return 9223372036854775806L; }",
        "  float getfloat() { return -123.456F; }",
        "  double getdouble() { return -123.456D; }",
        "  boolean getboolean() { return true; }",
        "  char getchar() { return 'H'; }",
        "  String getString() { return \"Hello\"; }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testNull() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  String returnNull() { return null; }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testLookupVars() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int run(int hello) { return hello; }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testVarDecl() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int run(int yip) {",
        "    int i;",
        "    if (yip < 42) {",
        "      i = yip;",
        "    } else {",
        "      i = 42;",
        "    }",
        "    return i;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testVarDeclInit() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int run(int yip) {",
        "    int i = 0;",
        "    if (yip < 42) {",
        "      i = yip;",
        "    } else {",
        "      i = 42;",
        "    }",
        "    return i;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testVarDeclString() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  String run(String yip) {",
        "    String s;",
        "    if (yip == \"hello\") {",
        "      s = \"world\";",
        "    } else {",
        "      s = yip;",
        "    }",
        "    return s;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testVarDeclInitString() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  String run(String yip) {",
        "    String s = \"hello\";",
        "    if (yip == \"hello\") {",
        "      s = \"world\";",
        "    } else {",
        "      s = yip;",
        "    }",
        "    return s;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testMultiVariableDeclAssign() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int run(int i, int j, int k) {",
        "    final int a = i + 1, b = j + 2, c = k + 3;",
        "    final int x = a + i;",
        "    int y = b + j;",
        "    int z = c + k;",
        "    z += (y += x);",
        "    return a + b + c + x + y + z;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testAssignment() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int run(int j) {",
        "    int i = 42;",
        "    i += j;",
        "    i -= j;",
        "    i *= j;",
        "    i /= j;",
        "    i %= j;",
        "    i <<= j;",
        "    i >>= j;",
        "    i >>>= j;",
        "    i &= j;",
        "    i |= j;",
        "    i ^= j;",
        "    return i;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testUnaryOperators() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
//        //TODO(manvithn): decompiled code seems needlessly verbose for this test
//        "  int unaryPlusMinus(byte i) {",
//        "    int j = +i;",
//        "    int k = -j;",
//        "    return j + k;",
//        "  }",
//        "  int prePostIncrementDecrement() {",
//        "    int i = 0;",
//        "    ++i;",
//        "    --i;",
//        "    i++;",
//        "    i--;",
//        "    int j = ++i;",
//        "    j = --i;",
//        "    j = i++;",
//        "    j = i--;",
//        "    final int[] k = {0};",
//        "    ++k[0];",
//        "    --k[0];",
//        "    k[0]++;",
//        "    k[0]--;",
//        "    return k[0] + i + j;",
//        "  }",
        "  int bitwiseComplement(int a) {",
        "    final int b = ~a;",
        "    return ~(a + b);",
        "  }",
        "  boolean logicalNegation(boolean a) {",
        "    final boolean b = !a;",
        "    return (!a || !b) && !b;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testBinaryOperator() throws IOException {
    String type = "foo.bar.Binary";
    String source = String.join("\n",
        "package foo.bar;",
        "class Binary {",
        "  boolean coverage(int a, int b) {",
        "    final int c = (a & b) | (a ^ b);",
        "    final int d = a + b - c * a / b % c << a >> b >>> c;",
        "    final boolean e = (a < b && b <= c) || (c > b && b >= a);",
        "    return a == b || (c != d && e);",
        "  }",
        "  int parenthesis(int a, int b, int c, int d, int e, int f, int g) {",
        "    return a * (b + (c << (d & (e | (f ^ g)))));",
        "  }",
        "  double promotion(byte a, short b, int c, long d, float e, double f) {",
        "    final long g = a * b / c * d;",
        "    final long h = d * (c / (b * a));",
        "    final float i = g * e;",
        "    final double j = h / f;",
        "    final float k = e * g;",
        "    final double l = f / h;",
        "    final double m = i + j;",
        "    final double n = l + k;",
        "    return m + n;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testTernaryOperator() throws IOException {
    String type = "foo.bar.Ternary";
    String source = String.join("\n",
        "package foo.bar;",
        "class Ternary {",
        "  int ternary(int a, int b, boolean c) {",
        "    return c ? a : b;",
        "  }",
        "  int promotion1(byte a, short b, boolean c) {",
        "    final short x = c ? a : b;",
        "    final short y = c ? b : a;",
        "    return x + y;",
        "  }",
        "  int promotion2(short a, int b, boolean c) {",
        "    final int x = c ? a : b;",
        "    final int y = c ? b : a;",
        "    return x + y;",
        "  }",
        "  long promotion3(int a, long b, boolean c) {",
        "    final long x = c ? a : b;",
        "    final long y = c ? b : a;",
        "    return x + y;",
        "  }",
        "  double promotion4(float a, double b, boolean c) {",
        "    final double x = c ? a : b;",
        "    final double y = c ? b : a;",
        "    return x + y;",
        "  }",
        "  float promotion5(long a, float b, boolean c) {",
        "    final float x = c ? a : b;",
        "    final float y = c ? b : a;",
        "    return x + y;",
        "  }",
        "  double promotion6(long a, double b, boolean c) {",
        "    final double x = c ? a : b;",
        "    final double y = c ? b : a;",
        "    return x + y;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testWrapperClasses() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  Integer unary(Integer i) {",
        "    final Integer j = -i;",
        "    final Integer k = -j;",
        "    return j + k;",
        "  }",
        "  Long binary(Long i, Byte j) {",
        "    final Long k = i + j;",
        "    return j + k;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testBasicArrays() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  void createArrayInt(int[] arr, int size) {",
        "    final int[] arrnew = new int[size];",
        "    arrnew[0] = arr[0];",
        "  }",
        "  void createArrayString(String[] arr, int size) {",
        "    final String[] arrnew = new String[size];",
        "    arrnew[0] = arr[0];",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testMultidimensionalArrays() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  void createArray1(int[][][] arr, int size) {",
        "    final int[][][] arrnew = new int[size][size + 1][size + 2];",
        "    arrnew[0] = arr[0];",
        "    arrnew[size - 1] = arr[size - 1];",
        "    arrnew[size - 1][size - 1][size - 1] = arr[size - 1][size - 1][size - 1];",
        "  }",
        "  void createArray2(int[][][][] arr, int size) {",
        "    final int[][][][] arrnew = new int[size][size][][];",
        "    arrnew[0] = new int[size][][];",
        "    arrnew[size - 1] = arr[size - 1];",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testArrayInitializers() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int[] createArrayInit1(int[] arr) {",
        "    final int[] arrnew = {1, 2, 3, 4, 5};",
        "    arr = new int[] {6, 7, 8, 9, 10};",
        "    return arrnew;",
        "  }",
        "  int[][][] createArrayInit2(int[][][] arr) {",
        "    final int[][][] arrnew = {{{1, 2}, {3}}, {{4, 5}}};",
        "    arr = new int[][][] {{{6, 7}, {8}}, {{9, 10}}};",
        "    return arrnew;",
        "  }",
        "  int[][][] createArrayInit3(int[][][] arr) {",
        "    final int[][][] arrnew = {{new int[2], null}, new int[1][]};",
        "    arr = new int[][][] {{new int[2], null}, new int[1][]};",
        "    return arrnew;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testArraySubtyping() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  Object[] createArray(int size) {",
        "    final Object[] arrnew = new String[size];",
        "    return arrnew;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testIf() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int run(int i, int j) {",
        "    int k = 0;",
        "    if (i < j) {",
        "      k = 1;",
        "    }",
        "    return k;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testElseIf() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int run(int i, int j) {",
        "    int k = 0;",
        "    if (i < j) {",
        "      k = -1;",
        "    } else if (j < i) {",
        "      k = 1;",
        "    } else if (i < 0) {",
        "      k = -1;",
        "    } else if (0 < i) {",
        "      k = 1;",
        "    } else {",
        "      k = 0;",
        "    }",
        "    return k;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testWhile() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  void while1() {",
        "    while (true) {}",
        "  }",
        "  void while2(int i) {",
        "    while (i < 100) {",
        "      ++i;",
        "    }",
        "  }",
        "  void while3(int i) {",
        "    while (i < 100) {",
        "      if (++i == 50) {",
        "        continue;",
        "      }",
        "      if (i == 42) {",
        "        break;",
        "      }",
        "    }",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testDoWhile() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  void doWhile1(int i, int j) {",
        "    do {",
        "      i += j;",
        "    } while (i < 100);",
        "  }",
        "  void doWhile2(int i) {",
        "    do {",
        "      if (++i == 50) {",
        "        continue;",
        "      }",
        "      if (i == 42) {",
        "        break;",
        "      }",
        "    } while (i < 100);",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testFor() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        // for loops with empty initializers, conditions, or updaters are converted to while loops
        "  void for1 () {",
        "    for (int i = 0; i < 10; ++i) {}",
        "  }",
        "  void for2 (int j) {",
        "    for (int i = 0; i < 10; ++i) {",
        "      j += i;",
        "    }",
        "  }",
        "  void for3 (int k) {",
        "    for (int i = 0, j = 10; i < 10 && j > 0; ++i, --j) {",
        "      k += i + j;",
        "    }",
        "  }",
        "  void for4 (int i, int j, int k) {",
        "    for (i = 0, j = 10; i < 10 && j > 0; ++i, --j) {",
        "      k += i + j;",
        "    }",
        "  }",
        "  void for5 (int k) {",
        "    for (int i = 0, j = 10; i < 10 && j > 0; i += 5, j /= 5) {",
        "      k += i + j;",
        "    }",
        "  }",
        "  void for6 (int i, int j, int k) {",
        "    for (i = 0, j = 10; i < 10 && j > 0; i += 5, j /= 5) {",
        "      k += i + j;",
        "    }",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testForEach() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  int foreach1(int[] arr) {",
        "    int sum = 0;",
        "    for (final int i : arr) {",
        "      sum += i;",
        "    }",
        "    return sum;",
        "  }",
        "  int foreach2(String[] arr) {",
        "    int sum = 0;",
        "    for (final String i : arr) {",
        "      ++sum;",
        "    }",
        "    return sum;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testNestedLoops() throws IOException {
    String type = "foo.bar.Test";
    String source = String.join("\n",
        "package foo.bar;",
        "class Test {",
        "  void nested1(boolean[][] arr, int i, int j, int len) {",
        "    int sum = 0;",
        "    while (i < len) {",
        "      while (j < len) {",
        "        sum += i*j;",
        "        if (arr[i][j]) {",
        "          break;",
        "        }",
        "        ++j;",
        "      }",
        "      ++i;",
        "    }",
        "  }",
        "  void nested2(boolean[][] arr, int len) {",
        "    int sum = 0;",
        "    for (int i = 0; i < len; ++i) {",
        "      for (int j = 0; j < len; ++j) {",
        "        sum += i*j;",
        "        if (arr[i][j]) {",
        "          break;",
        "        }",
        "      }",
        "    }",
        "  }",
        "  void nested3(boolean[][] arr, int i, int j, int len) {",
        "    int sum = 0;",
        "    while (i < len) {",
        "      while (j < len) {",
        "        sum += i*j;",
        "        if (arr[i][j]) {",
        "          continue;",
        "        }",
        "        ++j;",
        "      }",
        "      ++i;",
        "    }",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

//  //TODO(manvithn): run above nested loop test but with labeled outer loops
//  public void testLabeledBranches() throws IOException {
//    String type = "foo.bar.Test";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "class Test {",
//        "  int run() {",
//        "  }",
//        "}"
//    );
//    assertEqualSrcClassfile(type, source);
//  }
//
//  //TODO(manvithn): may not be decompiled properly
//  public void testSwitch() throws IOException {
//    String type = "foo.bar.Test";
//    String source = String.join("\n",
//        "package foo.bar;",
//        "class Test {",
//        "  int run() {",
//        "  }",
//        "}"
//    );
//    assertEqualSrcClassfile(type, source);
//  }

  public void testThis() throws IOException {
    String type = "foo.bar.Point";
    String source = String.join("\n",
        "package foo.bar;",
        "class Point {",
        "  int x;",
        "  int y;",
        "  Point(int x, int y) {",
        "    this.x = x;",
        "    this.y = y;",
        "  }",
        "  Point(int z) {",
        "    this(z, z);",
        "  }",
        "  int getX() {",
        "    return this.x;",
        "  }",
        "  int getY() {",
        "    return this.y;",
        "  }",
        "  Point identity() {",
        "    return this;",
        "  }",
        "}"
    );
    assertEqualSrcClassfile(type, source);
  }

  public void testStaticFields() throws IOException {
    String source = String.join("\n",
        "class Test {",
        // Constant.
        "  static final double PI = 3.1416;",

        // Primitive static field.
        // TODO(tball): enable when mutator moves initializer expressions to declarations.
        // "  static int version = 42;",

        // Static field with constructor that takes an arg.
        "  static Number number = new Integer(42);",
        "}");
    assertEqualSrcClassfile("Test", source);
  }
}
