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

package com.google.devtools.cyclefinder;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * System tests for the CycleFinder tool.
 *
 * @author Keith Stanger
 */
public class CycleFinderTest extends TestCase {

  File tempDir;
  List<String> inputFiles;
  List<List<Edge>> cycles;
  List<String> whitelistEntries;
  List<String> blacklistEntries;
  boolean printReferenceGraph;
  ReferenceGraph referenceGraph;

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = createTempDir();
    inputFiles = new ArrayList<>();
    whitelistEntries = new ArrayList<>();
    blacklistEntries = new ArrayList<>();
    printReferenceGraph = false;
    referenceGraph = null;
  }

  @Override
  protected void tearDown() {
    ErrorUtil.reset();
  }

  public void testEasyCycle() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertCycle("LA;", "LB;");
  }

  // TODO(nbraswell): Use com.google.j2objc.annotations.WeakOuter when transitioned to Java 8
  private static String weakOuterAndInterface = "import java.lang.annotation.*;\n"
        + "@Target(ElementType.TYPE_USE) @interface WeakOuter {}"
        + "interface Simple { public int run(); }";

  public void testAnonymousClassOuterRefCycle() throws Exception {
    addSourceFile("Simple.java", weakOuterAndInterface
        + "class Test { int member = 7; Simple o;"
        + "void f() { o = new Simple() { public int run() { return member; } }; } }");
    findCycles();

    // Assert that we have one cycle that contains LTest and a LTest anonymous class.
    assertEquals(1, cycles.size());
    assertCycle("LTest;");
    assertContains("LTest.1", printCyclesToString());
  }

  public void testAnonymousClassWithWeakOuter() throws Exception {
    addSourceFile("Simple.java", weakOuterAndInterface
        + "class Test { int member = 7; Simple o;"
        + "void f() { new @WeakOuter Simple() { public int run() { return member; } }; } }");
    findCycles();
    assertNoCycles();
  }

  public void testInnerClassWithWeakOuter() throws Exception {
    String source = "import com.google.j2objc.annotations.WeakOuter; "
        + "public class A { @WeakOuter class B { int test() { return o.hashCode(); }} B o; }";
    addSourceFile("A.java", source);
    findCycles();
    assertNoCycles();
  }

  public void testInnerClassOuterRefCycle() throws Exception {
    String source = "import com.google.j2objc.annotations.WeakOuter; "
        + "public class A { class B {int test(){return o.hashCode();}} B o;}";
    addSourceFile("A.java", source);
    findCycles();
    assertCycle("LA;", "LA.B;");
  }

  public void testWeakField() throws Exception {
    addSourceFile("A.java", "import com.google.j2objc.annotations.Weak; class A { @Weak B b; }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testRetainedWithField() throws Exception {
    addSourceFile("A.java",
        "import com.google.j2objc.annotations.RetainedWith; class A { @RetainedWith B b; }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testRecursiveTypeVariable() throws Exception {
    addSourceFile("A.java", "class A<T> { A<? extends T> a; }");
    addSourceFile("B.java", "class B<T> { B<? extends B<T>> b; }");
    addSourceFile("C.java", "class C<T> { C<java.util.List<T>> c; }");
    findCycles();
    // This test passes if it doesn't hang or crash due to infinite recursion.
  }

  public void testExtendsWildcard() throws Exception {
    addSourceFile("A.java", "class A { B<? extends C> b; }");
    addSourceFile("B.java", "class B<T> { T t; }");
    addSourceFile("C.java", "class C { A a; }");
    findCycles();
    assertCycle("LA;", "LB<+LC;>;", "+LC;");
  }

  public void testWhitelistedField() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B { A a; }");
    whitelistEntries.add("FIELD A.b");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistedType() throws Exception {
    addSourceFile("test/foo/A.java", "package test.foo; class A { C c; }");
    addSourceFile("test/foo/B.java", "package test.foo; class B { A a; }");
    addSourceFile("test/foo/C.java", "package test.foo; class C extends B { }");
    whitelistEntries.add("TYPE test.foo.C");
    findCycles();
    assertNoCycles();
    whitelistEntries.set(0, "TYPE test.foo.A");
    findCycles();
    assertNoCycles();
    whitelistEntries.set(0, "TYPE test.foo.B");
    findCycles();
    assertCycle("Ltest/foo/C;", "Ltest/foo/A;");
  }

  public void testWhitelistedLocalType() throws Exception {
    addSourceFile("test/foo/A.java",
      "package test.foo; class A { B b; void test() { "
      + "class Inner extends B { void foo() { A a = A.this; } } } }");
    addSourceFile("test/foo/B.java", "package test.foo; class B {}");
    whitelistEntries.add("TYPE test.foo.A.test.Inner");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistedAnonymousType() throws Exception {
    addSourceFile("test/foo/A.java",
      "package test.foo; class A { B b; B test() { "
      + "return new B() { void foo() { A a = A.this; } }; } }");
    addSourceFile("test/foo/B.java", "package test.foo; class B {}");
    whitelistEntries.add("TYPE test.foo.A.test.$");
    findCycles();
    assertNoCycles();
  }

  public void testSubtypeOfWhitelistedType() throws Exception {
    addSourceFile("test/foo/A.java", "package test.foo; class A { B b; }");
    addSourceFile("test/foo/B.java", "package test.foo; class B { A a; }");
    addSourceFile("test/foo/C.java", "package test.foo; class C extends B { }");
    whitelistEntries.add("TYPE test.foo.B");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistedPackage() throws Exception {
    addSourceFile("test/foo/A.java",
                  "package test.foo; import test.bar.B; public class A { B b; }");
    addSourceFile("test/bar/B.java",
                  "package test.bar; import test.foo.A; public class B { A a; }");
    whitelistEntries.add("NAMESPACE test.bar");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistedSubtype() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B {}");
    addSourceFile("C.java", "class C extends B {}");
    addSourceFile("D.java", "class D extends C { A a; }");
    whitelistEntries.add("FIELD A.b C");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistedOuterReference() throws Exception {
    addSourceFile("A.java", "class A { Inner i; class Inner { void test() { A a = A.this; } } }");
    whitelistEntries.add("OUTER A.Inner");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistComment() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B { A a; }");
    whitelistEntries.add("# FIELD A.b");
    findCycles();
    assertCycle("LA;", "LB;");
  }

  public void testStaticField() throws Exception {
    addSourceFile("A.java", "class A { static B b; }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testArrayField() throws Exception {
    addSourceFile("A.java", "class A { B[] b; }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertCycle("LA;", "LB;");
  }

  public void testNonStaticInnerInterface() throws Exception {
    addSourceFile("A.java", "class A { interface I {} I i;}");
    findCycles();
    assertNoCycles();
  }

  public void testCapturedVariable() throws Exception {
    addSourceFile("A.java", "class A { void test() {"
        + " final B b = new B();"
        + " A a = new A() { void test() { b.hashCode(); } }; } }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertCycle("LB;");
  }

  public void testCapturedVariableNotUsed() throws Exception {
    addSourceFile("A.java", "class A { void test() {"
        + " final B b = new B();"
        + " A a = new A() { void test() { } }; } }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testWeakCapturedVariable() throws Exception {
    addSourceFile("A.java", "import com.google.j2objc.annotations.Weak;"
        + "class A { void test() {"
        + " @Weak final B b = new B();"
        + " A a = new A() { void test() { b.hashCode(); } }; } }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testFinalVarAfterAnonymousClassNotCaptured() throws Exception {
    addSourceFile("A.java", "class A { void test() {"
        + " A a = new A() {};"
        + " final B b; } }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testOutOFScopeFinalVarNotCaptured() throws Exception {
    addSourceFile("A.java", "class A { void test() {"
        + " { final B b; }"
        + " A a = new A() {}; } }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testAnonymousClassAssignedToStaticField() throws Exception {
    addSourceFile("A.java",
        "class A { B b; static Runnable r = new Runnable() { public void run() {} }; }");
    addSourceFile("B.java", "class B { Runnable r; }");
    findCycles();
    assertNoCycles();
  }

  public void testAnonymousClassInStaticMethod() throws Exception {
    addSourceFile("A.java", "class A { B b; static void test() { "
        + "Runnable r = new Runnable() { public void run() {} }; } }");
    addSourceFile("B.java", "class B { Runnable r; }");
    findCycles();
    assertNoCycles();
  }

  public void testNoOuterReferenceIfNotNeeded() throws Exception {
    addSourceFile("A.java", "class A { Runnable r = new Runnable() { public void run() {} }; }");
    findCycles();
    assertNoCycles();
  }

  public void testOuterReferenceToGenericClass() throws Exception {
    // B.java is added before A.java to test that the outer edge A<B>.C -> A<B> is still added
    // despite B being visited before A. The outer reference cannot be known until A is visited.
    addSourceFile("B.java", "class B { A<B>.C abc; }");
    addSourceFile("A.java", "class A<T> { int i; T t; class C { void test() { i++; } } }");
    findCycles();
    assertCycle("LA<LB;>;", "LB;", "LA<LB;>.C;");
  }

  public void testBlacklist() throws Exception {
    addSourceFile("A.java", "class A { B b; C c; }");
    addSourceFile("B.java", "class B { A a; }");
    addSourceFile("C.java", "class C { A a; }");
    blacklistEntries.add("TYPE C");
    findCycles();
    assertEquals(1, cycles.size());
    assertCycle("LA;", "LC;");
  }

  public void testAnonymousLineNumbers() throws Exception {
    addSourceFile("Test.java",
        "class Test {\n"
        + " void dummy() {}\n"
        + " Runnable r = new Runnable() { public void run() { dummy(); } }; }");
    findCycles();
    assertEquals(1, cycles.size());
    assertCycle("LTest;");
    for (Edge e : cycles.get(0)) {
      assertContains("anonymous:3", e.toString());
    }
  }

  public void testWhitelistedAnonymousTypesInClassScope() throws Exception {
    addSourceFile("bar/AbstractA.java", "package bar; public class AbstractA {}");
    addSourceFile("bar/AbstractB.java", "package bar; public class AbstractB {}");
    addSourceFile("foo/Test.java",
        "package foo; import bar.AbstractA; import bar.AbstractB;"
        + " class Test { AbstractA a = new AbstractA() { void dummyA() {}"
        + " AbstractB b = new AbstractB() { void dummyB() { dummyA(); } }; }; }");
    whitelistEntries.add("NAMESPACE foo");
    findCycles();
    assertNoCycles();
  }

  public void testIncompatibleStripping() throws Exception {
    addSourceFile("Test.java",
        "import com.google.j2objc.annotations.J2ObjCIncompatible; "
        + "import java.garbage.Foo; class Test { @J2ObjCIncompatible Foo foo; }");
    // We just care that there are no compile errors.
    findCycles();
    assertNoCycles();
  }

  public void testIgnoreRawTypes() throws Exception {
    addSourceFile("A.java", "class A<T> { B b; }");
    addSourceFile("B.java", "class B<T> { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testSimpleLambdaWithCycle() throws Exception {
    addSourceFile("I.java", "interface I { int foo(); }");
    addSourceFile("A.java", "class A { int j = 1; I i = () -> j; }");
    findCycles();
    assertCycle("LA.$Lambda$1;", "LA;");
  }

  public void testMethodReferenceCycle() throws Exception {
    addSourceFile("I.java", "interface I { int foo(); }");
    addSourceFile("A.java", "class A { int bar() { return 1; } I i = this::bar; }");
    findCycles();
    assertCycle("LA.$Lambda$1;", "LA;");
  }

  public void testPrintReferenceGraph() throws Exception {
    addSourceFile("A.java", "class A { B<? extends C> b; }");
    addSourceFile("B.java", "class B<T> { T t; }");
    addSourceFile("C.java", "class C { A a; }");
    printReferenceGraph = true;
    findCycles();
    String graph = printReferenceGraphToString();
    assertContains("class: LB<+LC;>;", graph);
    assertContains("A -> (field b with type B<? extends C>)", graph);
    assertContains("C -> (field a with type A)", graph);
  }

  private void assertContains(String substr, String str) {
    assertTrue("Expected \"" + substr + "\" within \"" + str + "\"", str.contains(substr));
  }

  private void assertNoCycles() {
    assertNotNull(cycles);
    assertTrue("Expected no cycles: " + printCyclesToString(), cycles.isEmpty());
  }

  private void assertCycle(String... types) {
    assertNotNull(cycles);
    outer: for (List<Edge> cycle : cycles) {
      List<String> cycleTypes = new ArrayList<>();
      for (Edge e : cycle) {
        cycleTypes.add(e.getOrigin().getSignature());
      }
      for (String type : types) {
        if (!cycleTypes.contains(type)) {
          continue outer;
        }
      }
      return;
    }
    fail("No cycle found with types: " + Joiner.on(", ").join(types) + "\n"
         + printCyclesToString());
  }

  private String printCyclesToString() {
    ByteArrayOutputStream cyclesOut = new ByteArrayOutputStream();
    CycleFinder.printCycles(cycles, new PrintStream(cyclesOut));
    return cyclesOut.toString();
  }

  private String printReferenceGraphToString() {
    ByteArrayOutputStream referenceGraphOut = new ByteArrayOutputStream();
    referenceGraph.print(new PrintStream(referenceGraphOut));
    return referenceGraphOut.toString();
  }

  private void findCycles() throws IOException {
    Options options = new Options();
    if (!whitelistEntries.isEmpty()) {
      File whitelistFile = new File(tempDir, "whitelist");
      Files.asCharSink(whitelistFile, Charset.defaultCharset())
          .write(Joiner.on("\n").join(whitelistEntries));
      options.addWhitelistFile(whitelistFile.getAbsolutePath());
    }
    if (!blacklistEntries.isEmpty()) {
      File blacklistFile = new File(tempDir, "type_filter");
      Files.asCharSink(blacklistFile, Charset.defaultCharset())
          .write(Joiner.on("\n").join(blacklistEntries));
      options.addBlacklistFile(blacklistFile.getAbsolutePath());
    }
    options.setSourceFiles(inputFiles);
    options.setClasspath(System.getProperty("java.class.path"));
    if (printReferenceGraph) {
      options.setPrintReferenceGraph();
    }
    CycleFinder finder = new CycleFinder(options);
    finder.constructGraph();
    cycles = finder.findCycles();
    if (printReferenceGraph) {
      referenceGraph = finder.getReferenceGraph();
    }
    if (ErrorUtil.errorCount() > 0) {
      fail("CycleFinder failed with errors:\n"
           + Joiner.on("\n").join(ErrorUtil.getErrorMessages()));
    }
  }

  private void addSourceFile(String fileName, String source) throws IOException {
    File file = new File(tempDir, fileName);
    file.getParentFile().mkdirs();
    Files.asCharSink(file, Charset.defaultCharset()).write(source);
    inputFiles.add(file.getAbsolutePath());
  }

  private File createTempDir() throws IOException {
    File tempDir = File.createTempFile("cyclefinder_testout", "");
    tempDir.delete();
    tempDir.mkdir();
    return tempDir;
  }
}
