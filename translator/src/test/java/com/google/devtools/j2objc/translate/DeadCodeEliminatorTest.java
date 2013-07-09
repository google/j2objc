/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.util.DeadCodeMap;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.regex.Pattern;

/**
 * Unit tests for DeadCodeEliminator.
 *
 * @author Daniel Connelly
 */
public class DeadCodeEliminatorTest extends GenerationTest {

  @Override
  public void tearDown() throws Exception {
    Options.setDeadCodeMap(null);
    super.tearDown();
  }

  private String getStrippedCode(String typeName, String source) {
    CompilationUnit unit = compileType(typeName, source);
    unit.recordModifications();
    source = J2ObjC.removeDeadCode(unit, source);
    compileType(typeName, source);
    return source;
  }

  public void testDeadMethod() {
    String source = "class A {\n" +
        "  private static interface B {\n" +
        "    String bar();\n" +
        "  }\n" +
        "  private void baz() {\n" +
        "    // nothing\n" +
        "  }\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A$B", "bar", "()Ljava/lang/String;")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "A");
    assertTranslation(stripped, "interface B");
    assertRemoved(stripped, "String bar()");
    assertTranslation(stripped, "void baz()");
  }

  public void testDeadMethod_RemoveStaticImport() {
    String source = "import static java.util.Collections.max;\n" +
        "class A {}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod(
            "java.util.Collections",
            "max",
            "(Ljava/util/Collection;)Ljava/lang/Object;")
        .addDeadMethod(
            "java.util.Collections",
            "max",
            "(Ljava/util/Collection;Ljava/util/Comparator;)Ljava/lang/Object;")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "import static java.util.Collections.max");
  }

  public void testDeadMethod_DoNotRemoveStaticImport() {
    String source = "import static java.util.Collections.max;\n" +
        "class A {}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod(
            "java.util.Collections",
            "max",
            "(Ljava/util/Collection;Ljava/util/Comparator;)Ljava/lang/Object;")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "import static java.util.Collections.max");
  }

  public void testDeadMethod_RequiredByInterfaceMethod() {
    String source = "interface C {\n" +
        "  void foo();\n" +
        "}\n" +
        "interface B extends C {}\n" +
        "class A implements B {\n" +
        "  public void foo() {}\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A", "foo", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "void foo();");
    assertTranslation(stripped, "void foo() {");
    assertTranslation(stripped, "throw new AssertionError");
  }

  public void testDeadMethod_RequiredBySuperClassAbstractMethod() {
    String source = "abstract class C {\n" +
        "  abstract void foo();\n" +
        "}\n" +
        "abstract class B extends C {}\n" +
        "class A extends B {\n" +
        "  public void foo() {}\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A", "foo", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "void foo();");
    assertRemoved(stripped, "void foo() {}");
    assertTranslation(stripped, "void foo() {");
    assertTranslation(stripped, "throw new AssertionError");
  }

  public void testDeadMethod_RequiredByDeadInterfaceMethod() {
    String source = "interface C {\n" +
        "  void foo();\n" +
        "}\n" +
        "interface B extends C {}\n" +
        "class A implements B {\n" +
        "  public void foo() {}\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A", "foo", "()V")
        .addDeadMethod("C", "foo", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "void foo()");
  }

  public void testDeadMethod_RequiredByDeadSuperClassAbstractMethod() {
    String source = "abstract class C {\n" +
        "  abstract void foo();\n" +
        "}\n" +
        "abstract class B extends C {}\n" +
        "class A extends B {\n" +
        "  public void foo() {}\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A", "foo", "()V")
        .addDeadMethod("C", "foo", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "void foo()");
  }

  public void testDeadMethod_Abstract_ProvidedImplForSubclass() {
    String source = "abstract class C {\n" +
        "  abstract void foo();\n" +
        "}\n" +
        "interface D {\n" +
        "  void bar();\n" +
        "}\n" +
        "abstract class B extends C implements D {\n" +
        "  public void foo() {}\n" +
        "  public void bar() {}\n" +
        "}\n" +
        "class A extends B {}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("B", "foo", "()V")
        .addDeadMethod("B", "bar", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "void foo() {}");
    assertRemoved(stripped, "void bar() {}");
    assertTranslation(stripped, "void foo() {");
    assertTranslation(stripped, "void bar() {");
    assertTranslation(stripped, "throw new AssertionError");
  }

  public void testDeadMethod_Abstract_DoNotDuplicate() {
    String source = "abstract class C {\n" +
        "  abstract void foo();\n" +
        "}\n" +
        "abstract class B extends C {\n" +
        "  public void foo() {}\n" +
        "}\n" +
        "class A extends B {\n" +
        "  public void foo() { bar(); }\n" +
        "  private void bar() {}\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("B", "foo", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "void foo() {}");
    assertTranslation(stripped, "void foo() { bar(); }");
  }

  public void testDeadMethod_Abstract_DoNotOverrideSuperclass() {
    String source = "abstract class C {\n" +
        "  abstract void foo();\n" +
        "}\n" +
        "abstract class B extends C {\n" +
        "  public void foo() {}\n" +
        "}\n" +
        "class D extends B {\n" +
        "  private void bar() {}\n" +
        "  public final void foo() { bar(); }\n" +
        "}" +
        "class A extends D {\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("B", "foo", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "void foo() {}");
    assertTranslation(stripped, "void foo() { bar(); }");
    assertRemoved(stripped, "throw new AssertionError");
  }

  public void testDeadMethod_AnonymousClassMember() {
    String source = "abstract class B {}\n" +
        "class A {\n" +
        "  private B b = new B() {\n" +
        "    public void foo() {}\n" +
        "  };\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A$1", "foo", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "public void foo() {}");
  }

  public void testDeadMethod_InnerClassConstructor() {
    String source = "class A {\n" +
    	"  class B {\n" +
    	"    B() {}\n" +
    	"  }\n" +
    	"}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A$B", "A$B", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "B() {}");
  }

  public void testInitializeFinalFields() {
    String source = "class A {\n" +
        "  private final String a, b = \"foo\", c;\n" +
        "  private static final int x;\n" +
        "  static { x = 5; }\n" +
        "  A() { a = null; c = null; }\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A", "A", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "a = null");
    assertTranslation(stripped, "b = \"foo\"");
    assertTranslation(stripped, "c = null");
    assertTranslation(stripped, "private static final int x;");
  }

  public void testInitializeFinalFields_SkipIfConstructorPresent() {
    String source = "class A {\n" +
        "  private final String a, b = \"foo\", c;\n" +
        "  A() { a = null; c = null; }\n" +
        "}\n";
    Options.setDeadCodeMap(DeadCodeMap.builder().build());
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "A()");
    assertEquals(source, stripped);
  }

  public void testMethodGeneration() {
    String source = "interface B {\n" +
        "  <T extends Comparable<T>> void foo(" +
        "    int bar, String[][] baz, java.util.List<T> bah," +
        "        java.util.Map<? extends T, java.util.List<? super T>> x);\n" +
        "}\n" +
        "class A implements B {\n" +
        "  public <T extends Comparable<T>> void foo(" +
        "    int bar, String[][] baz, java.util.List<T> bah," +
        "        java.util.Map<? extends T, java.util.List<? super T>> x) {}\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("A", "foo", "(I[[Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "<T extends Comparable<T>> void foo(");
    assertTranslation(stripped, "int arg0");
    assertTranslation(stripped, "java.lang.String[][] arg1");
    assertTranslation(stripped, "java.util.List<T> arg2");
    assertTranslation(stripped, "java.util.Map<? extends T, java.util.List<? super T>> arg3");
    assertTranslation(stripped, "throw new AssertionError");
  }

  public void testMethodGeneration_DoNotQualifyInScope() {
    String source = "interface C {\n" +
    	"  A.B foo();\n" +
    	"}\n" +
    	"class D {\n" +
    	"  public A.B foo() { return null; }\n" +
    	"}\n" +
    	"class A extends D implements C{\n" +
    	"  class B {}\n" +
    	"}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("D")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "public A.B foo() { return null; }");
    assertTranslation(stripped, "public B foo()");
  }

  public void testMethodGeneration_DoNotDuplicateInheritedConcrete() {
    String source = "interface C<V> {\n" +
    	"  void foo(V x);\n" +
    	"}\n" +
    	"abstract class B<V> {\n" +
    	"  public final void foo(V x) {}\n" +
    	"}\n" +
    	"class A<V> extends B<V> implements C<V> {}\n";
    DeadCodeMap map = DeadCodeMap.builder().build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertOnce(stripped, "foo(V x) {");
  }

  public void testMethodGeneration_Overloading() {
    String source = "import java.io.IOException;\n" +
        "interface C {\n" +
        "  String foo(Object bar);\n" +
        "}\n" +
        "interface D {\n" +
        "  Object foo(String bar);\n" +
        "}\n" +
        "class B {\n" +
        "  public Object foo(String bar) throws IOException { return null; }\n" +
        "}\n" +
        "class E extends B {\n" +
        "  public Object foo(String bar) { return null; }\n" +
        "  public String foo(Object bar) { return null; }\n" +
        "}\n" +
        "class A extends E implements C, D {}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("E")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "public Object foo(String bar) { return null; }");
    assertRemoved(stripped, "public String foo(Object bar) { return null; }");
    assertTranslation(stripped, "public Object foo(java.lang.String arg0)");
    assertTranslation(stripped, "public java.lang.String foo(Object arg0)");
    assertTranslation(stripped, "throw new AssertionError");
    assertOnce(stripped, "throws IOException");
  }

  public void testMethodGeneration_InheritsMultipleOverrideEquivalent() {
    String source = "import java.io.IOException;\n" +
        "import java.text.ParseException;\n" +
        "interface X {\n" +
        "  CharSequence foo(String bar) throws IOException;\n" +
        "}\n" +
        "interface Y {\n" +
        "  Comparable foo(String bar) throws ParseException;\n" +
        "}\n" +
        "abstract class C {\n" +
        "  protected abstract Object foo(String bar);" +
        "}\n" +
        "class B extends C {\n" +
        "  public String foo(String bar) { return null; }\n" +
        "}\n" +
        "class A extends B implements X, Y {}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadMethod("B", "foo", "(Ljava/lang/String;)Ljava/lang/String;")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "public String foo(String bar)");
    assertTranslation(stripped, "abstract class J2OBJC_DUMMY_CLASS_0");
    assertPattern(stripped,
        "extends Object\\s+implements\\s+java.lang.CharSequence\\s*,\\s*java.lang.Comparable");
    assertTranslation(stripped, "public J2OBJC_DUMMY_CLASS_0 foo(java.lang.String arg0)");
    assertTranslation(stripped, "throw new AssertionError");
  }

  public void testDeadFields() {
    String source = "import static java.lang.System.out;\n" +
    	"import static java.lang.System.in;\n" +
    	"class A {\n" +
        "  private static final int foo = 1;\n" +
        "  public static final String bar = \"bar\";\n" +
        "  static final double pi = 3.2; // in Indiana only\n" +
        "  final String baz = null, bah = \"123\";\n" +
        "  private int abc = 9;\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadField("A", "foo")
        .addDeadField("A", "baz")
        .addDeadField("java.lang.System", "in")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "private static final int foo = 1;");
    assertTranslation(stripped, "public static final String bar = \"bar\";");
    assertTranslation(stripped, "static final double pi = 3.2;");
    assertTranslation(stripped, "private int abc = 9;");
    assertTranslation(stripped, "final String bah = \"123\";");
    assertRemoved(stripped, "baz");
    assertTranslation(stripped, "import static java.lang.System.out");
    assertRemoved(stripped, "import static java.lang.System.in");
  }

  public void testRemoveAnnotations() {
    String source = "import com.google.j2objc.annotations.Weak;\n" +
        "import com.google.j2objc.annotations.WeakOuter;\n" +
        "@interface C {\n" +
        "  String value() default \"foo\";\n" +
        "}\n" +
    	"@Deprecated class A {\n" +
    	"  @Weak @C private String x;\n" +
    	"  @WeakOuter private class B {}\n" +
        "  @SuppressWarnings(\"unchecked\") @Override public String toString() { return null; }\n" +
        "  @SuppressWarnings({\"unchecked\", \"unused\"}) void bar() {}\n" +
        "  @SuppressWarnings(value = \"unused\") void baz() {\n" +
        "    @SuppressWarnings({\"unchecked\"}) java.util.List list;\n" +
        "  }\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("C")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "@interface C");
    assertRemoved(stripped, "String value() default \"foo\"");
    assertRemoved(stripped, "@SuppressWarnings");
    assertRemoved(stripped, "@Deprecated");
    assertRemoved(stripped, "@Override");
    assertRemoved(stripped, "@C");
    assertTranslation(stripped, "@Weak");
    assertTranslation(stripped, "@WeakOuter");
  }

  public void testDeadInitializer() {
    String source = "class A {\n" +
        "  static final int baz = 9;\n" +
        "  static { System.out.println(\"foo\"); }\n" +
        "  { System.out.println(\"bar\"); }\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("A").build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "class A");
    assertTranslation(stripped, "static final int baz = 9");
    assertRemoved(stripped, "System.out.println");
  }

  public void testDeadEnum() {
    String source = "class A {\n" +
        "  private static void foo() {}\n" +
        "  public enum Thing {\n" +
        "    THING1(27),\n" +
        "    THING2(89) { void bar() {} },\n" +
        "    THING3 { void bar() { foo(); } };\n" +
        "    private Thing(int x) {}\n" +
        "    private Thing() {}\n" +
        "  }\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("A$Thing")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertTranslation(stripped, "class A");
    assertTranslation(stripped, "private static void foo() {}");
    assertTranslation(stripped, "public enum Thing");
    assertRemoved(stripped, "public Thing(int x) {}");
    assertRemoved(stripped, "THING1");
    assertRemoved(stripped, "THING2");
    assertRemoved(stripped, "THING3");
  }

  public void testConstructorGeneration() {
    String source = "class B {\n" +
        "  public B(int x, boolean y, String z, java.util.List w) {}\n" +
        "}\n" +
        "class A extends B {\n" +
        "  public A() { super(1, true, \"foo\", new java.util.ArrayList()); }\n" +
        "}\n" +
        "class C extends B {\n" +
        "  public C() { super(1, true, \"foo\", new java.util.ArrayList()); }\n" +
        "}\n";
    DeadCodeMap map = DeadCodeMap.builder()
        .addDeadClass("A")
        .addDeadMethod("C", "C", "()V")
        .build();
    Options.setDeadCodeMap(map);
    String stripped = getStrippedCode("A", source);
    assertRemoved(stripped, "public A() {");
    assertRemoved(stripped, "super(1, true, \"foo\", new java.util.ArrayList())");
    assertTranslation(stripped, "protected A() {");
    assertTranslation(stripped, "super(");
    assertTranslation(stripped, "(int) 0");
    assertTranslation(stripped, "(boolean) false");
    assertTranslation(stripped, "(java.lang.String) null");
    assertTranslation(stripped, "(java.util.List) null");
    assertTranslation(stripped, "throw new AssertionError");
  }

  private void assertOnce(String translation, String thing) {
    int index = translation.indexOf(thing);
    assertTrue(
        String.format("Expected to find %s in %s exactly once.", thing, translation),
        index >= 0 && index == translation.lastIndexOf(thing));
  }

  private void assertPattern(String translation, String pattern) {
    assertTrue(
        String.format("Expected to find pattern %s in %s", pattern, translation),
        Pattern.compile(pattern).matcher(translation).find());
  }

  private void assertRemoved(String translation, String code) {
    if (translation.contains(code)) {
      fail("Expected: " + code + " should have been removed from translation: " + translation);
    }
  }
}
