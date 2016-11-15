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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.translate.ElementReferenceMapper.MethodReferenceNode;
import com.google.devtools.j2objc.translate.ElementReferenceMapper.ReferenceNode;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.CodeReferenceMap.Builder;
import java.io.IOException;

/**
 * Unit tests for ElementReferenceMapper.
 *
 * @author Priyank Malvania
 */
public class ElementReferenceMapperTest extends GenerationTest {

  public void testMethodReference() throws IOException {
    String source = "class A {\n"
        + "  private static interface B {\n"
        + "    String bar();\n"
        + "  }\n"
        + "  private void baz() {\n"
        + "    // nothing\n"
        + "  }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    ImmutableSet<String> elementSet = setup.getElementReferenceMap().keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A$B")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A$B", "bar", "()Ljava/lang/String;")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "baz", "()V")));
    assertFalse(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "()V")));
  }

  public void testMethod_AnonymousClassMemberReference() throws IOException {
    String source = "abstract class B {}\n"
        + "class A {\n"
        + "  private B b = new B() {\n"
        + "    public void foo() {}\n"
        + "  };\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    ImmutableSet<String> elementSet = setup.getElementReferenceMap().keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("B")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "b")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A$1", "foo", "()V")));
  }

  public void testMethod_InnerClassConstructorReference() throws IOException {
    String source = "class A {\n"
        + "  class B {\n"
        + "    B(int i) {}\n"
        + "  }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    ImmutableSet<String> elementSet = setup.getElementReferenceMap().keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A$B")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A$B", "A$B", "(LA;I)V")));
  }

  public void testFieldsReference() throws IOException {
    String source = "import static java.lang.System.out;\n"
        + "import static java.lang.System.in;\n"
        + "class A {\n"
        + "  private static final int foo = 1;\n"
        + "  public static final String bar = \"bar\";\n"
        + "  static final double pi = 3.2; // in Indiana only\n"
        + "  final String baz = null, bah = \"123\";\n"
        + "  private int abc = 9;\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    ImmutableSet<String> elementSet = setup.getElementReferenceMap().keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "foo")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "bar")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "pi")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "baz")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "bah")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "abc")));
  }

  public void testInitializerReference() throws IOException {
    String source = "class A {\n"
        + "  static final int baz = 9;\n"
        + "  static { System.out.println(\"foo\"); }\n"
        + "  { System.out.println(\"bar\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    ImmutableSet<String> elementSet = setup.getElementReferenceMap().keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "baz")));
  }

  public void testEnumReference() throws IOException {
    String source = "class A {\n"
        + "  private static void foo() {}\n"
        + "  public enum Thing implements java.io.Serializable {\n"
        + "    THING1(27),\n"
        + "    THING2(89) { void bar() {} },\n"
        + "    THING3 { void bar() { foo(); } };\n"
        + "    private Thing(int x) {}\n"
        + "    private Thing() {}\n"
        + "  }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    ImmutableSet<String> elementSet = setup.getElementReferenceMap().keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A$Thing")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A$Thing", "A$Thing", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A$Thing", "A$Thing", "(I)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A$Thing$1", "bar", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A$Thing$2", "bar", "()V")));
  }

  public void testMethodInvocation() throws IOException {
    String source = "class A {\n"
        + "  private static void foo(String s) {}\n"
        + "  private static void bar(String s) {foo(\"boo\");}\n"
        + "  static { bar(\"bro\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();
    ImmutableSet<String> elementSet = elementMap.keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "bar", "(Ljava/lang/String;)V")));
    assertTrue(((MethodReferenceNode) elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "bar", "(Ljava/lang/String;)V")))
        .invokedMethods.contains(ElementReferenceMapper
            .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")));
  }

  public void testMethodTraversal() throws IOException {
    String source = "class A {\n"
        + "  private static void abc(String s) {}\n"
        + "  private static void xyz(String s) {abc(\"goo\");}\n"
        + "  private static void foo(String s) {xyz(\"woo\");}\n"
        + "  private static void bar(String s) {foo(\"boo\");}\n"
        + "  static { bar(\"zoo\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    ImmutableSet<String> elementSet = setup.getElementReferenceMap().keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "bar", "(Ljava/lang/String;)V")));
    assertFalse(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "falseCase", "(Ljava/lang/String;)V")));
  }

  public void testUnusedMethod() throws IOException {
    String source = "class A {\n"
        + "  private static void abc(String s) {}\n"
        + "  private static void xyz(String s) {abc(\"goo\");}\n"
        + "  private static void foo(String s) {xyz(\"woo\");}\n"
        + "  private static void unused(String s) {foo(\"boo\");}\n"
        + "  static { foo(\"zoo\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused", "(Ljava/lang/String;)V")).used);
  }

  //Check for assumptions: In this case, a static block's class does not get marked as true
  //TODO(user): Add static block used test
  public void testUnusedClass() throws IOException {
    String source = "class A {\n"
        + "  public static void abc(String s) {}\n"
        + "}"
        + "class B {\n"
        + "  static { A.abc(\"zoo\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();

    assertTrue(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A")).used);
    assertFalse(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("B")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).used);
  }

  public void testUsedStaticNestedClasses() throws IOException {
    String source = "class A {\n"
        + "  static { B.abc(\"zoo\"); }\n"
        + "  static class B {\n"
        + "    public static void abc(String s) {}\n"
        + "  }\n"
        + "  class C {\n"
        + "    public void xyz(String s) {}\n"
        + "  }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();

    assertFalse(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A")).used);
    assertTrue(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A$B")).used);
    assertFalse(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A$C")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A$B", "abc", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A$C", "xyz", "(Ljava/lang/String;)V")).used);
  }

  public void testUsedNonStaticNestedClasses() throws IOException {
    String source = "class A {\n"
        + "  static { new A().new B().abc(\"zoo\"); }\n"
        + "  class B {\n"
        + "    public void abc(String s) {}\n"
        + "  }\n"
        + "  class C {\n"
        + "    public void xyz(String s) {}\n"
        + "  }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();

    assertTrue(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A")).used);
    assertTrue(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A$B")).used);
    assertFalse(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A$C")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A$B", "abc", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A$C", "xyz", "(Ljava/lang/String;)V")).used);
  }

  public void testUnusedSubclassInCodeReferenceMap() throws IOException {
    String source = "class A {\n"
        + "  static { B.abc(\"zoo\"); }\n"
        + "  static class B {\n"
        + "    public static void abc(String s) {}\n"
        + "  }\n"
        + "  class C {\n"
        + "    public void xyz(String s) {}\n"
        + "  }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    CodeReferenceMap unusedCodeMap = setup.buildTreeShakerMap();

    assertTrue(unusedCodeMap.containsClass("A$C"));
    assertTrue(unusedCodeMap.containsMethod("A$C", "xyz", "(Ljava/lang/String;)V"));
  }

  public void testUnusedField() throws IOException {
    String source = "import static java.lang.System.out;\n"
        + "import static java.lang.System.in;\n"
        + "class A {\n"
        + "  private static final int foo = 1;\n"
        + "  public static final String bar = \"bar\";\n"
        + "  static final double pi = 3.2; // in Indiana only\n"
        + "  final String baz = null, bah = \"123\";\n"
        + "  private static int abc = 9;\n"
        + "  private static void foo() {abc = 10;}"
        + "  static {foo();}"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();
    ImmutableSet<String> elementSet = elementMap.keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "foo")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "bar")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "pi")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "baz")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "bah")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "abc")));

    //TODO(user): Add necessary checks after visiting FieldAccess to check used/unused fields
  }

  public void testUsedStaticlyCalledConstructor() throws IOException {
    String source = "class A {\n"
        + "  public A() {bar = 2;}"
        + "  private int bar = 1;\n"
        + "}\n"
        + "class B {\n"
        + "  public static void foo(A a) {}\n"
        + "  static {foo(new A());}"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    CodeReferenceMap unusedCodeMap = setup.buildTreeShakerMap();
    System.out.println(unusedCodeMap);

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsMethod("B", "foo", "(LA;)V"));
  }

  public void testUnusedConstructor() throws IOException {
    String source = "class A {\n"
        + "  public A() {bar = 2;}"
        + "  public A(int i) {bar = i;}"
        + "  private int bar = 1;\n"
        + "}\n"
        + "class B {\n"
        + "  public static void foo() {new A();}\n"
        + "  static {foo();}"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();
    ImmutableSet<String> elementSet = elementMap.keySet();
    CodeReferenceMap unusedCodeMap = setup.buildTreeShakerMap();

    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "A", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "A", "(I)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("B", "foo", "()V")));

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsMethod("B", "foo", "()V"));
    assertFalse(unusedCodeMap.containsMethod("A", "A", "()V"));
    assertTrue(unusedCodeMap.containsMethod("A", "A", "(I)V"));
  }

  public void testChainedMethodCalls() throws IOException {
    String source = "class A {\n"
        + "  public static void foo() {}\n"
        + "  public static void launch() {new D().xyz().bar().abc().foo();\n"
        + "     new E();}\n"
        + "  static {launch();}"
        + "}\n"
        + "class B {\n"
        + "  public static A abc() {return new A();}\n"
        + "}\n"
        + "class C {\n"
        + "  public static B bar() {return new B();}\n"
        + "}\n"
        + "class D {\n"
        + "  public static C xyz() {return new C();}\n"
        + "}\n"
        + "class E {\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();
    ImmutableSet<String> elementSet = elementMap.keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("B")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("C")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("D")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("E")));

    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "A", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("B", "B", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("C", "C", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("D", "D", "()V")));

    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("B", "abc", "()LA;")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("C", "bar", "()LB;")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("D", "xyz", "()LC;")));
  }

  public void testUnusedMethodCallingMethods() throws IOException {
    String source = "class A {\n"
        + "  private static void abc(String s) {}\n"
        + "  private static void xyz(String s) {abc(\"goo\");}\n"
        + "  private static void foo(String s) {xyz(\"woo\");}\n"
        + "  private static void unused2(String s) {foo(\"boo\");}\n"
        + "  private static void unused(String s) {unused2(\"boo\");}\n"
        + "  static { foo(\"zoo\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused2", "(Ljava/lang/String;)V")).used);
  }

  public void testUnusedMethodsCycle() throws IOException {
    String source = "class A {\n"
        + "  private static void abc(String s) {}\n"
        + "  private static void xyz(String s) {abc(\"goo\");}\n"
        + "  private static void foo(String s) {xyz(\"woo\");}\n"
        + "  private static void unused2(String s) {unused(\"boo\");}\n"
        + "  private static void unused(String s) {unused2(\"boo\");}\n"
        + "  static { foo(\"zoo\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused2", "(Ljava/lang/String;)V")).used);
  }

  public void testInputMethodsKept() throws IOException {
    String source = "class A {\n"
        + "  private static void abc(String s) {}\n"
        + "  private static void xyz(String s) {}\n"
        + "  private static void foo(String s) {xyz(\"woo\");}\n"
        + "}\n";

    Builder  builder = CodeReferenceMap.builder();
    builder.addMethod("A", "abc", "(Ljava/lang/String;)V");
    builder.addMethod("A", "xyz", "(Ljava/lang/String;)V");
    CodeReferenceMap inputCodeMap = builder.build();

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree(inputCodeMap);
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).used);
  }

  public void testPublicMethodsInInputClasses() throws IOException {
    String source = "class A {\n"
        + "  public static void abc(String s) {}\n"
        + "  public static void xyz(String s) {}\n"
        + "  private static void foo(String s) {xyz(\"woo\");}\n"
        + "}\n";

    Builder  builder = CodeReferenceMap.builder();
    builder.addClass("A");
    CodeReferenceMap inputCodeMap = builder.build();

    CompilationUnit unit = compileType("test", source);
    ElementReferenceMapper setup = new ElementReferenceMapper(unit);
    setup.run();
    setup.shakeTree(inputCodeMap);
    CodeReferenceMap unusedCodeMap = setup.buildTreeShakerMap();
    ImmutableMap<String, ReferenceNode> elementMap = setup.getElementReferenceMap();

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsMethod("A", "abc", "(Ljava/lang/String;)V"));
    assertFalse(unusedCodeMap.containsMethod("A", "xyz", "(Ljava/lang/String;)V"));
    assertTrue(unusedCodeMap.containsMethod("A", "foo", "(Ljava/lang/String;)V"));

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).used);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).used);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).used);
  }

  //TODO(user): Consult:
  //Any custom type in parameters must be created by calling some constructor/initializer?
  public void testUnusedType() throws IOException {
    //TODO(user): Add test for unused types after visiting VariableDeclarationExpression
  }
}
