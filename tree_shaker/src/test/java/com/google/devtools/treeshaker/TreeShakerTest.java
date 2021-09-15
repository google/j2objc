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
    addSourceFile("A.java", "package p; class A { static void main() { new B().b(\"\"); } }");
    addSourceFile("B.java", "package p; class B { void b(String s) { new C().c(s); } }");
    addSourceFile("C.java", "package p; class C { void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.A", "p.B", "p.C");
    assertThat(getUnusedMethods(unused)).isEmpty();
  }

  public void testExportedStaticMethod() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B().b(\"\"); } }");
    addSourceFile("B.java", "package p; class B { void b(String s) {} }");
    addSourceFile("C.java", "package p; class C { void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.C");
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testExportedMethod() throws IOException {
    addTreeShakerRootsFile("p.A:\n    A()\n    main()");
    addSourceFile("A.java", "package p; class A { void main() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).isEmpty();
  }

  public void testExportedClass() throws IOException {
    addTreeShakerRootsFile("p.A\nb.c.C");
    addSourceFile("A.java", "package p; class A { static void main() { } }");
    addSourceFile("B.java", "package b; class B { void b(String s) {} }");
    addSourceFile("C.java", "package b.c; class C { void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("b.B");
    assertThat(getUnusedMethods(unused)).isEmpty();
  }

  public void testImportStaticField() throws IOException {
    addTreeShakerRootsFile("a.A:\n    main()");
    addSourceFile("A.java", "package a; class A { static void main() { C.of(); }}");
    addSourceFile("B.java",
        "package a;",
        "class B {",
        "  static final B X = new B();",
        "}");
    addSourceFile("C.java",
        "package a;",
        "import static a.B.X;",
        "class C {",
        "  static B of() { return X; }",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("a.A", "A", "()V"),
        getMethodName("a.C", "C", "()V"));
  }

  public void testImportStaticMethod() throws IOException {
    addTreeShakerRootsFile("a.A:\n    main()");
    addSourceFile("A.java", "package a; class A { static void main() { C.of(); }}");
    addSourceFile("B.java",
        "package a;",
        "class B {",
        "  static final B X = new B();",
        "  static B getX() { return X ; }",
        "}");
    addSourceFile("C.java",
        "package a;",
        "import static a.B.getX;",
        "class C {",
        "  static B of() { return getX(); }",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("a.A", "A", "()V"),
        getMethodName("a.C", "C", "()V"));
  }

  public void testConstructorOverloads() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B(\"\").b(); }}");
    addSourceFile("B.java", "package p; class B { B() {} B(String s) {} void b() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "B", "()V"));
  }

  public void testExplicitConstructorInvocation() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B(); }}");
    addSourceFile("B.java",
        "package p; class B { B() { this(\"foo\"); } B(String s) {} B(Integer i) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "B", "(Ljava/lang/Integer;)V"));
  }

  public void testMethodOverloads() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B().b(\"\"); }}");
    addSourceFile("B.java", "package p; class B { B() {} void b() {} void b(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "()V"));
  }

  public void testStaticInitializers() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B().b(); }}");
    addSourceFile("B.java",
        "package p;",
        "class B { static int i; static { i = 24; } void b() {} static { i = new C().c(); }}");
    addSourceFile("C.java",
        "package p; class C { static int i; static { i = 42; } int c() { return i; }}");
    addSourceFile("D.java",
        "package p; class D { static int i; static { i = 42; } int d() { return i; }}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.D");
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testVariableInitializers() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B(); }}");
    addSourceFile("B.java", "package p; class B { B() { C c = new C(); }}");
    addSourceFile("C.java", "package p; class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testStaticVariableInitializers() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B(); }}");
    addSourceFile("B.java", "package p; class B { static C c = new C(); }");
    addSourceFile("C.java", "package p; class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testNestedClasses() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B().new C(); }}");
    addSourceFile("B.java", "package p; class B { class C { class D { }}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.B$C$D");
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testStaticNestedClasses() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B.C(); }}");
    addSourceFile("B.java", "package p; class B { static class C { static class D {} void c(){}}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.B", "p.B$C$D");
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B$C", "c", "()V"));
  }

  public void testConstructorChaining() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new D(); } }");
    addSourceFile("B.java", "package p; class B { B(String s) {} }");
    // Test explicit constructor chaining.
    addSourceFile("C.java", "package p; class C extends B { C() { super(\"foo\"); } }");
    // Test implicit constructor chaining.
    addSourceFile("D.java", "package p; class D extends C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testMethodOverrides() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java",
        "package p; class A { static void main() { new C().b(); new C().c(); }}");
    addSourceFile("B.java", "package p; class B { void b() {} void c() {} }");
    addSourceFile("C.java", "package p; class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "c", "()V"));
  }

  public void testMethodOverridesIndirect() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java",
        "package p; class A { static void main() { B b = new C(); b.b(); b.c(); }}");
    addSourceFile("B.java", "package p; class B { void b() {} void c() {} }");
    addSourceFile("C.java", "package p; class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testSuperMethodInvocation() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().b(); } }");
    addSourceFile("B.java", "package p; class B { void b() {} }");
    addSourceFile("C.java", "package p; class C extends B { void b() { super.b(); } }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testStaticFieldWrite() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { B.b = 1; } }");
    addSourceFile("B.java", "package p; class B { static int b; }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "B", "()V"));
  }

  public void testStaticFieldAccess() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { int i = B.b; } }");
    addSourceFile("B.java", "package p; class B { static int b; }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "B", "()V"));
  }

  public void testStaticFieldMethodAccess() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { B.TWO.b(); } }");
    addSourceFile("B.java",
        "package p; class B { static B TWO = new B(); void b() {} void c() {} }");
    addSourceFile("C.java", "package p; class C { static C TWO = new C(); }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.C");
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "c", "()V"));
  }

  public void testEnums() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { B.TWO.b(); } }");
    addSourceFile("B.java", "package p; enum B { ONE, TWO, THREE; void b() {} void c() {} }");
    addSourceFile("C.java", "package p; enum C { ONE, TWO, THREE; }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.C");
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "c", "()V"));
  }

  public void testAbstractClasses() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c(); }}");
    addSourceFile("B.java", "package p; abstract class B { void b() {} abstract void c(); }");
    addSourceFile("C.java", "package p; class C extends B { void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "()V"),
        getMethodName("p.B", "c", "()V"));
  }

  public void testAbstractClassesIndirect() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java",
        "package p; class A { static void main() { B b = new C(); b.c(); }}");
    addSourceFile("B.java", "package p; abstract class B { void b() {} abstract void c(); }");
    addSourceFile("C.java", "package p; class C extends B { void c() {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "()V"));
  }

  public void testInterfaces() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().b(); }}");
    addSourceFile("B.java", "package p; interface B { void b(); }");
    addSourceFile("C.java", "package p; class C implements B { public void b() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();

    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "()V"));
  }

  public void testInterfacesIndirect() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { B b = new C(); b.c(); }}");
    addSourceFile("B.java", "package p; interface B { void c(); }");
    addSourceFile("C.java", "package p; class C implements B { public void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"));
  }

  public void testInterfaceInheritance() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java",
        "package p; class A { static void main() { D d = new D(); d.b(); d.c(); }}");
    addSourceFile("B.java", "package p; interface B { void b(); }");
    addSourceFile("C.java", "package p; interface C extends B { void c(); }");
    addSourceFile("D.java",
        "package p; class D implements C { public void b() {} public void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "()V"),
        getMethodName("p.C", "c", "()V"));
  }

  public void testInterfaceInheritanceIndirect() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { C c = new D(); c.b(); }}");
    addSourceFile("B.java", "package p; interface B { void b(); }");
    addSourceFile("C.java", "package p; interface C extends B { void c(); }");
    addSourceFile("D.java",
        "package p; class D implements C { public void b() {} public void c() {}}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.C", "c", "()V"),
        getMethodName("p.D", "c", "()V"));
  }

  public void testExternalInterfaces() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B().apply(1); }}");
    addSourceFile("B.java",
        "package p;",
        "import java.util.function.Function;",
        "class B implements Function<Integer,Integer> {",
        "  @Override ",
        "  public Integer apply(Integer i) { return i; }",
        "}");

    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAccidentalOverride() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { C c = new D(); c.b(); }}");
    addSourceFile("B.java", "package p; class B { public void b() {} }");
    addSourceFile("C.java", "package p; interface C { void b(); }");
    addSourceFile("D.java", "package p; class D extends B implements C {}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testUninstantiatedTypes() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main(p.B,p.C,p.D)");
    addSourceFile("A.java",
        "package p; class A {  static void main(B b, C c, D d) { b.b(); c.c(); d.d(); }}");
    addSourceFile("B.java", "package p; interface B { void b(); }");
    addSourceFile("C.java", "package p; abstract class C { abstract void c(); }");
    addSourceFile("D.java", "package p; class D { void d(){} }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsNoneOf("p.A", "p.B", "p.C", "p.D");

    // Note: While the types are live becaused they are referenced, all of the members are marked
    // unused because the types are never instantiated.
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "()V"),
        getMethodName("p.B", "<clinit>", "()V"),
        getMethodName("p.C", "C", "()V"),
        getMethodName("p.C", "c", "()V"),
        getMethodName("p.C", "<clinit>", "()V"),
        getMethodName("p.D", "D", "()V"),
        getMethodName("p.D", "d", "()V"),
        getMethodName("p.D", "<clinit>", "()V"));
  }

  public void testAnonymousAbstractTypesBasic() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c().b(); }}");
    addSourceFile("B.java", "package p; abstract class B { abstract B b(); }");
    addSourceFile("C.java",
        "package p; class C { B c() { return new B() { B b() { return this; }}; }}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnonymousAbstractTypesWithFields() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c.b(); }}");
    addSourceFile("B.java", "package p; abstract class B { abstract B b(); }");
    addSourceFile("C.java", "package p; class C { B c = new B() { B b() { return this; }}; }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnonymousAbstractTypesWithParam() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new D().d().b(); }}");
    addSourceFile("B.java", "package p; abstract class B { B(C c) {} abstract B b(); }");
    addSourceFile("C.java", "package p; class C { }");
    addSourceFile("D.java",
        "package p; class D { B d() { return new B(new C()) { B b() { return this; }}; }}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnonymousAbstractTypesNested() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c().b(); }}");
    addSourceFile("B.java", "package p; abstract class B { abstract B b(); }");
    addSourceFile("C.java",
        "package p;",
        "class C {",
        "  B c() {",
        "    return new B() {",
        "      B b() { return new B() { B b() { return d(); }}; }",
        "      B d() { return this; }",
        "    };",
        "  }",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnonymousAbstractTypesWithInternalCall() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c().b(); }}");
    addSourceFile("B.java", "package p; abstract class B { abstract B b(); }");
    addSourceFile("C.java",
        "package p;",
        "class C {",
        "  B c() {",
        "    return new B() {",
        "      B b() { return d(); } ",
        "      B d() { return this; } ",
        "    };",
        "  }",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnonymousAbstractTypesWithExternalCall() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new a.C().c().b(); }}");
    addSourceFile("B.java", "package a; public abstract class B { public abstract B b(); }");
    addSourceFile("C.java",
        "package a;",
        "public class C {",
        "  public B c() {",
        "    return new B() {",
        "      public B b() { return d(); } ",
        "      public B c() { return d(); } ",
        "      public B d() { return this; } ",
        "    }.d();",
        "  }",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("a.C$1", "c", "()La/B;"));
  }

  public void testAnonymousParametricAbstractTypes() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c(); }}");
    addSourceFile("B.java", "package p; abstract class B<V> { abstract void b(V v); }");
    addSourceFile("C.java",
        "package p;",
        "class C {",
        "  <V> B<V> c() { return new B<V>() { void b(V v) { }}; }",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "(Ljava/lang/Object;)V"),
        getMethodName("p.C$1", "b", "(Ljava/lang/Object;)V"));
  }

  public void testAnonymousInterfaceTypes() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c().b(); }}");
    addSourceFile("B.java", "package p; public interface B { B b(); }");
    addSourceFile("C.java",
        "package p;",
        "class C {",
        "  B c() {",
        "    return new B() { public B b() { return this; } };",
        "  }",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnonymousInterfaceTypesWithFields() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c.b(); }}");
    addSourceFile("B.java", "package p; interface B { B b(); }");
    addSourceFile("C.java",
        "package p;",
        "class C {",
        "  B c = new B() { public B b() { return this; } };",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnonymousParametricInterfaceTypes() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c(); }}");
    addSourceFile("B.java", "package p; interface B<V> { void b(V v); }");
    addSourceFile("C.java",
        "package p;",
        "class C {",
        "  <V> B<V> c() { return new B<V>() { public void b(V v) { }}; }",
        "}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "(Ljava/lang/Object;)V"),
        getMethodName("p.C$1", "b", "(Ljava/lang/Object;)V"));
  }

  public void testLocalTypesBasic() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B(); }}");
    addSourceFile("B.java",
        "package p;",
        "class B {",
        "  void b() {",
        "    class C { int c() { return 1; } }",
        "  }",
        "}");
    addSourceFile("C.java", "class C {}");
    addSourceFile("D.java", "package p; class D {}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("C", "p.D", "p.B$1C");
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "b", "()V"));
  }

  public void testLocalTypesWithExternalTypes() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B().b(); }}");
    addSourceFile("B.java",
        "package p;",
        "class B {",
        "  int b() {",
        "    class C { int c() { return new D().d(); } }",
        "    return new C().c();",
        "  }",
        "}");
    addSourceFile("C.java",
        "package p;",
        "class C {",
        "  int c() {",
        "    class B { int b() { return new E().e(); } }",
        "    return new B().b();",
        "  }",
        "}");
    addSourceFile("D.java", "package p; class D { int d() { return 1; }}");
    addSourceFile("E.java", "package p; class E { int e() { return 1; }}");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.C", "p.C$1B", "p.E");
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testLambdas() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java",
        "package p; class A { static void main() { B b = x -> x; b.op(5); } }");
    addSourceFile("B.java", "package p; interface B { int op(int x);  }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testLambdasUnused() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { B b = i -> i; } }");
    addSourceFile("B.java", "package p; interface B { int op(int x);  }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();

    // Note: B.op is marked unused, which is a non-issue since the generated iOS protocols
    // don't contain any code.
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "op", "(I)I"));
  }

  public void testMethodValues() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java",
        "package p;",
        "class A {",
        "  static void main() {",
        "    execute(A::log);",
        "  }",
        " static void execute (B b) { b.op(\"a\"); }",
        " static void log (Object s) { }",
        "}");
    addSourceFile("B.java", "package p; interface B { void op(String s);  }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testParametricTypes() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { B<C> b = new B<C>(); }}");
    addSourceFile("B.java", "package p; class B<X> { }");
    addSourceFile("C.java", "package p; class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.C");
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testParametricTypesIndirect() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { C.c(new B<D>()); }}");
    addSourceFile("B.java", "package p; class B<X> { }");
    addSourceFile("C.java", "package p; class C { static void c(B<D> b) {} }");
    addSourceFile("D.java", "package p; class D { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.D");
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.C", "C", "()V"));
  }

  public void testChainedParametricTypes() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java",
        "package p; class A { static void main() { B<D>.C<D> c = new B<D>().new C<D>(); } }");
    addSourceFile("B.java", "package p; class B<X> { class C<Y> { }}");
    addSourceFile("D.java", "package p; class D { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.D");
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testNativeMethods() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new B().b(); }}");
    addSourceFile("B.java", "package p; class B { native void b(); native void c(); }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(
        getMethodName("p.A", "A", "()V"),
        getMethodName("p.B", "c", "()V"));
  }

  public void testSimpleAnnotations() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C().c(); }}");
    addSourceFile("B.java", "package p; @interface B { int b(); int c() default 3; }");
    addSourceFile("C.java", "package p; @B(b=4) class C { @B(b=4,c=2) void c() { }}");
    addSourceFile("D.java", "package p; @interface D { }");
    addSourceFile("E.java",
        "package p;",
        "import java.lang.annotation.Retention;",
        "import java.lang.annotation.RetentionPolicy;",
        "@Retention(RetentionPolicy.SOURCE)",
        "@interface E { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).containsExactly("p.E");
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnnotationAnnotations() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new D(); }}");
    addSourceFile("B.java", "package p; @interface B { }");
    addSourceFile("C.java", "package p; @B @interface C { }");
    addSourceFile("D.java", "package p; @C class D { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testExternalAnnotationAnnotations() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new C(); }}");
    addSourceFile("B.java",
        "package p;",
        "import java.lang.annotation.Retention;",
        "import java.lang.annotation.RetentionPolicy;",
        "@Retention(RetentionPolicy.CLASS)",
        "@interface B { }");
    addSourceFile("C.java", "package p; @B class C { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnnotationsWithClassReferences() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { new D(); }}");
    addSourceFile("B.java", "package p; enum B { X, Y; }");
    addSourceFile("C.java", "package p; @interface C { B b() default B.X; }");
    addSourceFile("D.java", "package p; @C class D { }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testAnnotationsWithInnerClasses() throws IOException {
    addTreeShakerRootsFile("p.A:\n    main()");
    addSourceFile("A.java", "package p; class A { static void main() { }}");
    addSourceFile("B.java", "package p; @interface B { enum D { E; } D b() default D.E; }");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("p.A", "A", "()V"));
  }

  public void testPackageAnnotations() throws IOException {
    addTreeShakerRootsFile("a.A:\n    main()");
    addSourceFile("A.java", "package a; class A { static void main() { new a.B(); }}");
    addSourceFile("B.java", "package a; public class B { }");
    addSourceFile("C.java", "package a; @interface C { }");
    addSourceFile("package-info.java", "@C package a;");
    CodeReferenceMap unused = findUnusedCode();

    assertThat(getUnusedClasses(unused)).isEmpty();
    assertThat(getUnusedMethods(unused)).containsExactly(getMethodName("a.A", "A", "()V"));
  }

  public void testWriteUnusedClasses() throws IOException {
    addTreeShakerRootsFile("");
    addSourceFile("A.java", "package p; class A { }");
    addSourceFile("B.java", "package p; class B { }");
    addSourceFile("C.java", "package p; class C { }");
    String output = writeUnused(findUnusedCode());

    assertThat(classSplitter.split(output)).containsExactly("p.A", "p.B", "p.C");
  }

  public void testWriteUnusedConstructor() throws IOException {
    addTreeShakerRootsFile("p.A:\n    a()");
    addSourceFile("A.java", "package p; class A { static void a() {}}");
    String output = writeUnused(findUnusedCode());

    assertThat(output).startsWith("p.A:\n");
    assertThat(methodSplitter.split(output)).containsExactly("p.A:", "A()");
  }

  public void testWriteUnusedMethod() throws IOException {
    addTreeShakerRootsFile("p.A:\n    A()");
    addSourceFile("A.java", "package p; class A { int a(boolean b) { return 0; } }");
    String output = writeUnused(findUnusedCode());

    assertThat(output).startsWith("p.A:\n");
    assertThat(methodSplitter.split(output)).containsExactly("p.A:", "int a(boolean)");
  }

  public void testWriteUnusedOverloadedMethods() throws IOException {
    addTreeShakerRootsFile("p.A:\n    A()");
    addSourceFile("A.java", "package p; class A { void a() {} void a(int i) {} }");
    String output = writeUnused(findUnusedCode());

    assertThat(output).startsWith("p.A:\n");
    assertThat(methodSplitter.split(output)).containsExactly("p.A:", "a(int)",  "a()");
  }

  public void testWriteUnused() throws IOException {
    addTreeShakerRootsFile("p.A:\n    A()");
    addSourceFile("A.java", "package p; class A { boolean a(int i) { return true; } }");
    addSourceFile("B.java", "package p; class B { boolean b(int i) { return true; } }");
    String output = writeUnused(findUnusedCode());

    assertThat(output).isEqualTo("p.B\np.A:\n    boolean a(int)\n");
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

  private void addSourceFile(String fileName, String... sources) throws IOException {
    File file = new File(tempDir, fileName);
    file.getParentFile().mkdirs();
    StringBuilder source = new StringBuilder();
    for (String s : sources) {
      source.append(s);
    }
    Files.asCharSink(file, Charset.defaultCharset()).write(source);
    inputFiles.add(file.getAbsolutePath());
  }

  private File createTempDir() throws IOException {
    File tempDir = File.createTempFile("treeshaker_testout", "");
    tempDir.delete();
    tempDir.mkdir();
    return tempDir;
  }
}
