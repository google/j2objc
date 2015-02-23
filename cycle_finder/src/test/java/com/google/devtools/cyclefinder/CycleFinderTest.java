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
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.devtools.j2objc.util.ErrorUtil;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

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

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = createTempDir();
    inputFiles = Lists.newArrayList();
    whitelistEntries = Lists.newArrayList();
    blacklistEntries = Lists.newArrayList();
  }

  public void testEasyCycle() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertCycle("LA;", "LB;");
  }

  public void testWeakField() throws Exception {
    addSourceFile("A.java", "import com.google.j2objc.annotations.Weak; class A { @Weak B b; }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertNoCycles();
  }

  public void testRecursiveWildcard() throws Exception {
    addSourceFile("A.java", "class A<T> { A<? extends T> a; }");
    addSourceFile("B.java", "class B<T> { B<? extends B<T>> b; }");
    findCycles();
    // This test passes if it doesn't hang or crash due to infinite recursion.
  }

  public void testExtendsWildcard() throws Exception {
    addSourceFile("A.java", "class A { B<? extends C> b; }");
    addSourceFile("B.java", "class B<T> { T t; }");
    addSourceFile("C.java", "class C { A a; }");
    findCycles();
    assertCycle("LA;", "LB<LB;{0}+LC;>;", "LB;{0}+LC;");
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
    addSourceFile("A.java", "class A<T> { int i; T t; class C { void test() { i++; } } }");
    addSourceFile("B.java", "class B { A<B>.C abc; }");
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
      List<String> cycleTypes = Lists.newArrayList();
      for (Edge e : cycle) {
        cycleTypes.add(e.getOrigin().getKey());
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

  private void findCycles() throws IOException {
    Options options = new Options();
    if (!whitelistEntries.isEmpty()) {
      File whitelistFile = new File(tempDir, "whitelist");
      Files.write(Joiner.on("\n").join(whitelistEntries), whitelistFile, Charset.defaultCharset());
      options.addWhitelistFile(whitelistFile.getAbsolutePath());
    }
    if (!blacklistEntries.isEmpty()) {
      File blacklistFile = new File(tempDir, "type_filter");
      Files.write(Joiner.on("\n").join(blacklistEntries), blacklistFile, Charset.defaultCharset());
      options.addBlacklistFile(blacklistFile.getAbsolutePath());
    }
    options.setSourceFiles(inputFiles);
    options.setClasspath(System.getProperty("java.class.path"));
    CycleFinder finder = new CycleFinder(options);
    cycles = finder.findCycles();
    if (ErrorUtil.errorCount() > 0) {
      fail("CycleFinder failed with errors:\n"
           + Joiner.on("\n").join(ErrorUtil.getErrorMessages()));
    }
  }

  private void addSourceFile(String fileName, String source) throws IOException {
    File file = new File(tempDir, fileName);
    file.getParentFile().mkdirs();
    Files.write(source, file, Charset.defaultCharset());
    inputFiles.add(file.getAbsolutePath());
  }

  private File createTempDir() throws IOException {
    File tempDir = File.createTempFile("cyclefinder_testout", "");
    tempDir.delete();
    tempDir.mkdir();
    return tempDir;
  }
}
