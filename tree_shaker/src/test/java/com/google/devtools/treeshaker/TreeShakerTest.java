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
import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
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
  static Splitter classSplitter = Splitter.on('\n').omitEmptyStrings();
  static Splitter methodSplitter =
      Splitter.on("\n    ").omitEmptyStrings().trimResults(CharMatcher.is('\n'));

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

    assertThat(getUnusedClasses(unused)).containsExactly("A", "B", "C");
    assertThat(getUnusedMethods(unused)).isEmpty();
  }

  public void testExportedStaticMethod() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().b(args[0]); } }");
    addSourceFile("B.java", "class B { void b(String s) {} }");
    addSourceFile("C.java", "class C { void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B");
    assertThat(getUnusedClasses(unused)).containsExactly("C");

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "b", "(Ljava/lang/String;)V"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testExportedMethod() throws IOException {
    addTreeShakerRootsFile("A:\n    A()\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { void main(String[] args) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).doesNotContain("A");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "A", "()V"),
        getMethodName("A", "main", "([Ljava/lang/String;)V"));
    assertThat(getUnusedMethods(unused)).isEmpty();
  }

  public void testExportedClass() throws IOException {
    addTreeShakerRootsFile("A\nb.c.C");
    addSourceFile("A.java", "class A { static void main(String[] args) { } }");
    addSourceFile("B.java", "package b; class B { void b(String s) {} }");
    addSourceFile("C.java", "package b.c; class C { void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "b.c.C");
    assertThat(getUnusedClasses(unused)).containsExactly("b.B");

    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("b.c.C", "C", "()V"),
        getMethodName("b.c.C", "c", "(Ljava/lang/String;)V"));
  }

  public void testConstructorOverloads() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B(args[0]).b(); }}");
    addSourceFile("B.java", "class B { B() {} B(String s) {} void b() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "b", "(Ljava/lang/String;)V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "B", "()V"));
  }

  public void testExplicitConstructorInvocation() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B(); }}");
    addSourceFile("B.java", "class B { B() { this(\"foo\"); } B(String s) {} B(Integer i) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "B", "(Ljava/lang/String;)V"),
        getMethodName("B", "B", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "B", "(Ljava/lang/Integer;)V"));
  }

  public void testMethodOverloads() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().b(args[1]); }}");
    addSourceFile("B.java", "class B { B() {} void b() {} void b(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "b", "(Ljava/lang/String;)V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "b", "()V"));
  }

  public void testStaticInitializers() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().b(); }}");
    addSourceFile("B.java",
        "class B { static int i; static { i = 24; } void b() {} static { i = new C().c(); }}");
    addSourceFile("C.java", "class C { static int i; static { i = 42; } int c() { return i; }}");
    addSourceFile("D.java", "class D { static int i; static { i = 42; } int d() { return i; }}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).containsExactly("D");

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "b", "(Ljava/lang/String;)V"),
        getMethodName("C", "c", "(Ljava/lang/String;)I"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testVariableInitializers() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B(); }}");
    addSourceFile("B.java", "class B { B() { C c = new C(); }}");
    addSourceFile("C.java", "class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "B", "()V"),
        getMethodName("C", "C", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testStaticVariableInitializers() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B(); }}");
    addSourceFile("B.java", "class B { static C c = new C(); }");
    addSourceFile("C.java", "class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "B", "()V"),
        getMethodName("C", "C", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testNestedClasses() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B().new C(); }}");
    addSourceFile("B.java", "class B { class C { class D { }}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "B.C");
    assertThat(getUnusedClasses(unused)).containsExactly("B.C.D");

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "B", "()V"),
        getMethodName("B.C", "B$C", "(LB;)V"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testStaticNestedClasses() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new B.C(); }}");
    addSourceFile("B.java", "class B { static class C { static class D { }}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B.C");
    assertThat(getUnusedClasses(unused)).containsExactly("B", "B.C.D");

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B.C", "B$C", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
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

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C", "D");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("A", "main", "([Ljava/lang/String;)V"),
        getMethodName("B", "B", "(Ljava/lang/String;)V"),
        getMethodName("C", "C", "()V"),
        getMethodName("D", "D", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testMethodOverrides() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { new C().b(); new C().c(); }}");
    addSourceFile("B.java", "class B { void b() {} void c() {} }");
    addSourceFile("C.java", "class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("B", "b", "()V"),
        getMethodName("C", "c", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "c", "()V"));
  }

  public void testMethodOverridesIndirect() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { B b = new C(); b.b(); b.c(); }}");
    addSourceFile("B.java", "class B { void b() {} void c() {} }");
    addSourceFile("C.java", "class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("B", "b", "()V"),
        getMethodName("B", "c", "()V"),
        getMethodName("C", "c", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testSuperMethodInvocation() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new C().b(); } }");
    addSourceFile("B.java", "class B { void b() {} }");
    addSourceFile("C.java", "class C extends B { void b() { super.b(); } }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("B", "b", "()V"),
        getMethodName("C", "b", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testStaticFieldAccess() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { B.TWO.b(); } }");
    addSourceFile("B.java", "class B { static B TWO = new B(); void b() {} void c() {} }");
    addSourceFile("C.java", "class C { static C TWO = new C(); }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B");
    assertThat(getUnusedClasses(unused)).containsExactly("C");

    assertThat(getUnusedMethods(unused)).doesNotContain(getMethodName("B", "b", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "c", "()V"));
  }

  public void testEnums() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { B.TWO.b(); } }");
    addSourceFile("B.java", "enum B { ONE, TWO, THREE; void b() {} void c() {} }");
    addSourceFile("C.java", "enum C { ONE, TWO, THREE; }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B");
    assertThat(getUnusedClasses(unused)).containsExactly("C");

    assertThat(getUnusedMethods(unused)).doesNotContain(getMethodName("B", "b", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "c", "()V"));
  }

  public void testAbstractClasses() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new C().c(); }}");
    addSourceFile("B.java", "abstract class B { void b() {} abstract void c(); }");
    addSourceFile("C.java", "class C extends B { void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).doesNotContain(getMethodName("C", "c", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "b", "()V"),
        getMethodName("B", "c", "()V"));
  }

  public void testAbstractClassesIndirect() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { B b = new C(); b.c(); }}");
    addSourceFile("B.java", "abstract class B { void b() {} abstract void c(); }");
    addSourceFile("C.java", "class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("B", "c", "()V"),
        getMethodName("C", "c", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "b", "()V"));
  }

  public void testInterfaces() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { new C().b(); }}");
    addSourceFile("B.java", "interface B { void b(); }");
    addSourceFile("C.java", "class C implements B { public void b() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).doesNotContain(getMethodName("C", "b", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "b", "()V"));
  }

  public void testInterfacesIndirect() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { B b = new C(); b.c(); }}");
    addSourceFile("B.java", "interface B { void c(); }");
    addSourceFile("C.java", "class C implements B { public void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("B", "c", "()V"),
        getMethodName("C", "c", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"));
  }

  public void testInterfaceInheritance() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { D d = new D(); d.b(); d.c(); }}");
    addSourceFile("B.java", "interface B { void b(); }");
    addSourceFile("C.java", "interface C extends B { void c(); }");
    addSourceFile("D.java", "class D implements C { public void b() {} public void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C", "D");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("D", "b", "()V"),
        getMethodName("D", "c", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("B", "b", "()V"),
        getMethodName("C", "c", "()V"));
  }

  public void testInterfaceInheritanceIndirect() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { C c = new D(); c.b(); }}");
    addSourceFile("B.java", "interface B { void b(); }");
    addSourceFile("C.java", "interface C extends B { void c(); }");
    addSourceFile("D.java", "class D implements C { public void b() {} public void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C", "D");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsNoneOf(
        getMethodName("B", "b", "()V"),
        getMethodName("D", "b", "()V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("C", "c", "()V"),
        getMethodName("D", "c", "()V"));
  }

  public void testLambdas() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { B b = (x) -> x; b.op(5); } }");
    addSourceFile("B.java", "interface B { int op(int x);  }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B");
    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).doesNotContain(getMethodName("B", "op", "(I)I"));
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testParametricTypes() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { B<C> b = new B<C>(); }}");
    addSourceFile("B.java", "class B<X> { }");
    addSourceFile("C.java", "class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B");
    assertThat(getUnusedClasses(unused)).containsExactly("C");

    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testParametricTypesIndirect() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java", "class A { static void main(String[] args) { C.c(new B<D>()); }}");
    addSourceFile("B.java", "class B<X> { }");
    addSourceFile("C.java", "class C { static void c(B<D> b) {} }");
    addSourceFile("D.java", "class D { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "C");
    assertThat(getUnusedClasses(unused)).containsExactly("D");

    assertThat(getUnusedMethods(unused)).doesNotContain(getMethodName("C", "c", "(LB;)V"));
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("A", "A", "()V"),
        getMethodName("C", "C", "()V"));
  }

  public void testChainedParametricTypes() throws IOException {
    addTreeShakerRootsFile("A:\n    main(java.lang.String[])");
    addSourceFile("A.java",
        "class A { static void main(String[] args) { B<D>.C<D> c = new B<D>().new C<D>(); } }");
    addSourceFile("B.java", "class B<X> { class C<Y> { }}");
    addSourceFile("D.java", "class D { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("A", "B", "B.C");
    assertThat(getUnusedClasses(unused)).containsExactly("D");

    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("A", "A", "()V"));
  }

  public void testWriteUnusedClasses() throws IOException {
    addTreeShakerRootsFile("");
    addSourceFile("A.java", "class A { }");
    addSourceFile("B.java", "class B { }");
    addSourceFile("C.java", "class C { }");
    String output = writeUnused(findUnusedCode());

    assertThat(classSplitter.split(output)).containsExactly("A", "B", "C");
  }

  public void testWriteUnusedConstructor() throws IOException {
    addTreeShakerRootsFile("A");
    addSourceFile("A.java", "class A { }");
    String output = writeUnused(findUnusedCode());

    assertThat(output).startsWith("A:\n");
    assertThat(methodSplitter.split(output)).containsExactly("A:", "A()");
  }

  public void testWriteUnusedMethod() throws IOException {
    addTreeShakerRootsFile("A:\n    A()");
    addSourceFile("A.java", "class A { int a(boolean b) { return 0; } }");
    String output = writeUnused(findUnusedCode());

    assertThat(output).startsWith("A:\n");
    assertThat(methodSplitter.split(output)).containsExactly("A:", "int a(boolean)");
  }

  public void testWriteUnusedOverloadedMethods() throws IOException {
    addTreeShakerRootsFile("A:\n    A()");
    addSourceFile("A.java", "class A { void a() {} void a(int i) {} }");
    String output = writeUnused(findUnusedCode());

    assertThat(output).startsWith("A:\n");
    assertThat(methodSplitter.split(output)).containsExactly("A:", "a(int)",  "a()");
  }

  public void testWriteUnused() throws IOException {
    addTreeShakerRootsFile("A:\n    A()");
    addSourceFile("A.java", "class A { boolean a(int i) { return true; } }");
    addSourceFile("B.java", "class B { boolean b(int i) { return true; } }");
    String output = writeUnused(findUnusedCode());

    assertThat(output).isEqualTo("B\nA:\n    boolean a(int)\n");
  }

  public void testEraseParametricTypes() throws IOException {
    assertThat(UsedCodeMarker.eraseParametricTypes("")).isEmpty();
    assertThat(UsedCodeMarker.eraseParametricTypes("C")).isEqualTo("C");
    assertThat(UsedCodeMarker.eraseParametricTypes("C<D>")).isEqualTo("C");
    assertThat(UsedCodeMarker.eraseParametricTypes("C<D<A>>")).isEqualTo("C");
    assertThat(UsedCodeMarker.eraseParametricTypes("C<A>.D<A>")).isEqualTo("C.D");
  }

  public void testGetType() throws IOException {
    assertThat(getType("V")).isEqualTo("void");
    assertThat(getType("Z")).isEqualTo("boolean");
    assertThat(getType("C")).isEqualTo("char");
    assertThat(getType("B")).isEqualTo("byte");
    assertThat(getType("S")).isEqualTo("short");
    assertThat(getType("I")).isEqualTo("int");
    assertThat(getType("F")).isEqualTo("float");
    assertThat(getType("J")).isEqualTo("long");
    assertThat(getType("D")).isEqualTo("double");
    assertThat(getType("[I")).isEqualTo("int[]");
    assertThat(getType("Ljava/lang/String;")).isEqualTo("java.lang.String");
    assertThat(getType("[I")).isEqualTo("int[]");
    assertThat(getType("[[I")).isEqualTo("int[][]");
    assertThat(getType("[Ljava/lang/String;")).isEqualTo("java.lang.String[]");
    assertThat(getType("[[Ljava/lang/String;")).isEqualTo("java.lang.String[][]");
    assertThat(getType("()V")).isEqualTo("void()");
    assertThat(getType("(I)V")).isEqualTo("void(int)");
    assertThat(getType("(II)V")).isEqualTo("void(int,int)");
    assertThat(getType("(III)V")).isEqualTo("void(int,int,int)");
    assertThat(getType("(Ljava/lang/String;II)V")).isEqualTo("void(java.lang.String,int,int)");
    assertThat(getType("(ILjava/lang/String;I)V")).isEqualTo("void(int,java.lang.String,int)");
    assertThat(getType("(IILjava/lang/String;)V")).isEqualTo("void(int,int,java.lang.String)");
  }

  private static String writeUnused(CodeReferenceMap unused) {
    StringBuilder result = new StringBuilder();
    TreeShaker.writeUnused(unused, result::append);
    return result.toString();
  }

  private static String getType(String descriptor) {
    StringBuilder result = new StringBuilder();
    int offset = TreeShaker.getType(descriptor, 0, result);
    return (offset == descriptor.length()) ? result.toString() : null;
  }

  private static String getMethodName(String type, String name, String signature) {
    return UsedCodeMarker.getQualifiedMethodName(type, name, signature);
  }

  private static ImmutableSet<String> getUnusedClasses(CodeReferenceMap unused) {
    return unused.getReferencedClasses();
  }

  private static ImmutableSet<String> getUnusedMethods(CodeReferenceMap unused) {
    ImmutableSet.Builder<String> methods = new ImmutableSet.Builder<>();
    unused.getReferencedMethods().cellSet().forEach(cell ->
        cell.getValue().forEach(signature ->
            methods.add(getMethodName(cell.getRowKey(), cell.getColumnKey(), signature))));
    return methods.build();
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
