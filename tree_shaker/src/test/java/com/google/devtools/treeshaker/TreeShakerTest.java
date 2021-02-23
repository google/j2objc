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

package com.google.devtools.treeshaker;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * System tests for the TreeShaker.
 */
public class TreeShakerTest extends TestCase {
  File tempDir;
  File treeShakerRoots;
  List<String> inputFiles;

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = createTempDir();
    treeShakerRoots = null;
    inputFiles = new ArrayList<>();
  }

  @Override
  protected void tearDown() {
    ErrorUtil.reset();
  }

  private CodeReferenceMap findUnusedCode() throws IOException {
    Options options = new Options();
    options.setClasspath(System.getProperty(JAVA_CLASS_PATH.value()));
    options.setTreeShakerRoots(treeShakerRoots);
    options.setSourceFiles(inputFiles);
    TreeShaker shaker = new TreeShaker(options);
    CodeReferenceMap unused = shaker.findUnusedCode();
    if (ErrorUtil.errorCount() > 0) {
      fail("TreeShaker failed with errors:\n" + Joiner.on("\n").join(ErrorUtil.getErrorMessages()));
    }
    return unused;
  }

  public void testNoExportedRoots() throws IOException {
    addTreeShakerRootsFile("");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().b(args[0]); } }");
    addSourceFile("B.java", "class B { void b(String s) { new C().c(s); } }");
    addSourceFile("C.java", "class C { void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertTrue(unused.containsClass("A"));
    assertTrue(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertTrue(unused.containsClass("B"));
    assertTrue(unused.containsMethod("B", "abc", "(Ljava/lang/String;)V"));
    assertTrue(unused.containsClass("C"));
    assertTrue(unused.containsMethod("C", "xyz", "(Ljava/lang/String;)V"));
  }

  public void testExportedStaticMethod() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().b(args[0]); } }");
    addSourceFile("B.java", "class B { void b(String s) {} }");
    addSourceFile("C.java", "class C { void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "b", "(Ljava/lang/String;)V"));

    assertTrue(unused.containsClass("C"));
    assertTrue(unused.containsMethod("C", "c", "(Ljava/lang/String;)V"));
  }

  public void testExportedMethod() throws IOException {
    addTreeShakerRootsFile("A:\n    A()\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { void main(String[] args) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsMethod("A", "A", "()V"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
  }

  public void testExportedClass() throws IOException {
    addTreeShakerRootsFile("A\nb.c.C");
    addSourceFile("A.java", "class A { static void main(String[] args) { } }");
    addSourceFile("B.java", "package b; class B { void b(String s) {} }");
    addSourceFile("C.java", "package b.c; class C { void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("b.c.C"));

    assertTrue(unused.containsClass("b.B"));
  }

  public void testConstructorOverloads() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B(args[0]).b(); }}");
    addSourceFile("B.java", "class B { B() {} B(String s) {} void b() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "b", "(Ljava/lang/String;)V"));

    assertTrue(unused.containsMethod("B", "B", "()V"));
  }

  public void testExplicitConstructorInvocation() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B(); }}");
    addSourceFile("B.java", "class B { B() { this(\"foo\"); } B(String s) {} B(Integer i) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "B", "(Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "B", "()V"));

    assertTrue(unused.containsMethod("B", "B", "(Ljava/lang/Integer;)V"));
  }

  public void testMethodOverloads() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().b(args[1]); }}");
    addSourceFile("B.java", "class B { B() {} void b() {} void b(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "b", "(Ljava/lang/String;)V"));

    assertTrue(unused.containsMethod("B", "b", "()V"));
  }

  public void testStaticInitializers() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().b(); }}");
    addSourceFile("B.java",
        "class B { static int i; static { i = 24; } void b() {} static { i = new C().c(); }}");
    addSourceFile("C.java", "class C { static int i; static { i = 42; } int c() { return i; }}");
    addSourceFile("D.java", "class D { static int i; static { i = 42; } int d() { return i; }}");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "b", "(Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("C", "c", "(Ljava/lang/String;)I"));

    assertTrue(unused.containsClass("D"));
    assertTrue(unused.containsMethod("D", "d", "(Ljava/lang/String;)I"));
  }

  public void testVariableInitializers() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B(); }}");
    addSourceFile("B.java", "class B { B() { C c = new C(); }}");
    addSourceFile("C.java", "class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "B", "()V"));
    assertFalse(unused.containsMethod("C", "C", "()V"));
  }

  public void testStaticVariableInitializers() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B(); }}");
    addSourceFile("B.java", "class B { static C c = new C(); }");
    addSourceFile("C.java", "class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "B", "()V"));
    assertFalse(unused.containsMethod("C", "C", "()V"));
  }

  public void testNestedClasses() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().new C(); }}");
    addSourceFile("B.java", "class B { class C { class D { }}}");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("B.C"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "B", "()V"));
    assertFalse(unused.containsMethod("B.C", "B$C", "(LB;)V"));

    assertTrue(unused.containsClass("B.C.D"));
    assertTrue(unused.containsMethod("B.C.D", "B$C$D", "(LB$C;)V"));
  }

  public void testStaticNestedClasses() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B.C(); }}");
    addSourceFile("B.java", "class B { static class C { static class D { }}}");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B.C"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B.C", "B$C", "()V"));

    assertTrue(unused.containsClass("B"));
    assertTrue(unused.containsClass("B.C.D"));
    assertTrue(unused.containsMethod("B", "B", "()V"));
    assertTrue(unused.containsMethod("B.C.D", "B$C$D", "()V"));
  }

  public void testConstructorChaining() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new D(); } }");
    addSourceFile("B.java", "class B { B(String s) {} }");
    // Test explicit constructor chaining.
    addSourceFile("C.java", "class C extends B { C() { super(\"foo\"); } }");
    // Test implicit constructor chaining.
    addSourceFile("D.java", "class D extends C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsClass("D"));
    assertFalse(unused.containsMethod("A", "main", "([Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("B", "B", "(Ljava/lang/String;)V"));
    assertFalse(unused.containsMethod("C", "C", "()V"));
    assertFalse(unused.containsMethod("D", "D", "()V"));
  }

  public void testMethodOverrides() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { new C().b(); new C().c(); }}");
    addSourceFile("B.java", "class B { void b() {} void c() {} }");
    addSourceFile("C.java", "class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("B", "b", "()V"));
    assertFalse(unused.containsMethod("C", "c", "()V"));

    assertTrue(unused.containsMethod("B", "c", "()V"));
  }

  public void testMethodOverridesIndirect() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { B b = new C(); b.b(); b.c(); }}");
    addSourceFile("B.java", "class B { void b() {} void c() {} }");
    addSourceFile("C.java", "class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("B", "b", "()V"));
    assertFalse(unused.containsMethod("B", "c", "()V"));
    assertFalse(unused.containsMethod("C", "c", "()V"));
  }

  public void testSuperMethodInvocation() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new C().b(); } }");
    addSourceFile("B.java", "class B { void b() {} }");
    addSourceFile("C.java", "class C extends B { void b() { super.b(); } }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("B", "b", "()V"));
    assertFalse(unused.containsMethod("C", "b", "()V"));
  }

  public void testStaticFieldAccess() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { B.TWO.b(); } }");
    addSourceFile("B.java", "class B { static B TWO = new B(); void b() {} void c() {} }");
    addSourceFile("C.java", "class C { static C TWO = new C(); }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsMethod("B", "b", "()V"));

    assertTrue(unused.containsClass("C"));
    assertTrue(unused.containsMethod("B", "c", "()V"));
  }

  public void testEnums() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { B.TWO.b(); } }");
    addSourceFile("B.java", "enum B { ONE, TWO, THREE; void b() {} void c() {} }");
    addSourceFile("C.java", "enum C { ONE, TWO, THREE; }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsMethod("B", "b", "()V"));

    assertTrue(unused.containsClass("C"));
    assertTrue(unused.containsMethod("B", "c", "()V"));
  }

  public void testAbstractClasses() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new C().c(); }}");
    addSourceFile("B.java", "abstract class B { void b() {}; abstract void c(); }");
    addSourceFile("C.java", "class C extends B { void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("C", "c", "()V"));

    assertTrue(unused.containsMethod("B", "b", "()V"));
    assertTrue(unused.containsMethod("B", "c", "()V"));
  }

  public void testAbstractClassesIndirect() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { B b = new C(); b.c(); }}");
    addSourceFile("B.java", "abstract class B { void b() {} abstract void c(); }");
    addSourceFile("C.java", "class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("B", "c", "()V"));
    assertFalse(unused.containsMethod("C", "c", "()V"));

    assertTrue(unused.containsMethod("B", "b", "()V"));
  }

  public void testInterfaces() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new C().b(); }}");
    addSourceFile("B.java", "interface B { void b(); }");
    addSourceFile("C.java", "class C implements B { public void b() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("C", "b", "()V"));

    assertTrue(unused.containsMethod("B", "b", "()V"));
  }

  public void testInterfacesIndirect() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { B b = new C(); b.c(); }}");
    addSourceFile("B.java", "interface B { void c(); }");
    addSourceFile("C.java", "class C implements B { public void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsClass("C"));
    assertFalse(unused.containsMethod("B", "c", "()V"));
    assertFalse(unused.containsMethod("C", "c", "()V"));
  }

  private void addTreeShakerRootsFile(String source) throws IOException {
    treeShakerRoots = new File(tempDir, "roots.cfg");
    treeShakerRoots.getParentFile().mkdirs();
    Files.asCharSink(treeShakerRoots, Charset.defaultCharset()).write(source);
  }

  private void addSourceFile(String fileName, String source) throws IOException {
    File file = new File(tempDir, fileName);
    file.getParentFile().mkdirs();
    Files.write(source, file, Charset.defaultCharset());
    inputFiles.add(file.getAbsolutePath());
  }

  private File createTempDir() throws IOException {
    File tempDir = File.createTempFile("treeshaker_testout", "");
    tempDir.delete();
    tempDir.mkdir();
    return tempDir;
  }
}
