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

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

  @Override
  protected void setUp() throws IOException {
    tempDir = createTempDir();
    inputFiles = Lists.newArrayList();
    whitelistEntries = Lists.newArrayList();
  }

  public void testEasyCycle() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertCycle("LA;", "LB;");
  }

  public void testRecursiveWildcard() throws Exception {
    addSourceFile("A.java", "class A<T> { A<? extends T> a; }");
    findCycles();
    // This test passes if it doesn't hang or crash due to infinite recursion.
  }

  public void testSuperWildcard() throws Exception {
    addSourceFile("A.java", "class A { B<? super C> b; }");
    addSourceFile("B.java", "class B<T> { T t; }");
    addSourceFile("C.java", "class C { A a; }");
    findCycles();
    assertCycle("LA;", "LB<LB;{0}-LC;>;", "LC;");
  }

  public void testWhitelistedField() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B { A a; }");
    whitelistEntries.add("field A.b");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistedType() throws Exception {
    addSourceFile("test/foo/A.java", "package test.foo; class A { C c; }");
    addSourceFile("test/foo/B.java", "package test.foo; class B { A a; }");
    addSourceFile("test/foo/C.java", "package test.foo; class C extends B { }");
    whitelistEntries.add("type test.foo.C");
    findCycles();
    assertNoCycles();
    whitelistEntries.set(0, "type test.foo.A");
    findCycles();
    assertNoCycles();
    whitelistEntries.set(0, "type test.foo.B");
    findCycles();
    assertCycle("Ltest/foo/C;", "Ltest/foo/A;");
  }

  public void testWhitelistedPackage() throws Exception {
    addSourceFile("test/foo/A.java",
                  "package test.foo; import test.bar.B; public class A { B b; }");
    addSourceFile("test/bar/B.java",
                  "package test.bar; import test.foo.A; public class B { A a; }");
    whitelistEntries.add("namespace test.bar");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistedSubtype() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B {}");
    addSourceFile("C.java", "class C extends B {}");
    addSourceFile("D.java", "class D extends C { A a; }");
    whitelistEntries.add("field A.b C");
    findCycles();
    assertNoCycles();
  }

  public void testWhitelistComment() throws Exception {
    addSourceFile("A.java", "class A { B b; }");
    addSourceFile("B.java", "class B { A a; }");
    whitelistEntries.add("# A.b");
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
        + " final B b;"
        + " A a = new A() {}; } }");
    addSourceFile("B.java", "class B { A a; }");
    findCycles();
    assertCycle("LB;");
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
    options.setFiles(inputFiles.toArray(new String[0]));
    ByteArrayOutputStream errorMessages = new ByteArrayOutputStream();
    CycleFinder finder = new CycleFinder(options, new PrintStream(new NullOutputStream()),
                                         new PrintStream(errorMessages));
    cycles = finder.findCycles();
    if (finder.errorCount() > 0) {
      fail("CycleFinder failed with errors:\n" + errorMessages.toString());
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

  private static class NullOutputStream extends OutputStream {

    public void write(int b) {
    }
  }
}
