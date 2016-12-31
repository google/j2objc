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

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.CodeReferenceMap.Builder;
import com.google.devtools.treeshaker.ElementReferenceMapper.ReferenceNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for UnusedCodeTracker.
 *
 * @author Priyank Malvania
 */
public class UnusedCodeTrackerTest  extends GenerationTest {

  public void testUnusedMethod() throws IOException {
    String source = "class A {\n"
        + "  private static void abc(String s) {}\n"
        + "  private static void xyz(String s) {abc(\"goo\");}\n"
        + "  private static void foo(String s) {xyz(\"woo\");}\n"
        + "  private static void unused(String s) {foo(\"boo\");}\n"
        + "  static { foo(\"zoo\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused", "(Ljava/lang/String;)V")).reachable);
  }

  //Check for assumptions: In this case, a static block's class does not get marked as true
  //TODO(malvania): Add static block used test
  public void testUnusedClass() throws IOException {
    String source = "class A {\n"
        + "  public static void abc(String s) {}\n"
        + "}"
        + "class B {\n"
        + "  static { A.abc(\"zoo\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();

    assertTrue(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("B")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).reachable);
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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();

    assertFalse(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A$B")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A$C")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A$B", "abc", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A$C", "xyz", "(Ljava/lang/String;)V")).reachable);
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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();

    assertTrue(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A$B")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper.stitchClassIdentifier("A$C")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A$B", "abc", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A$C", "xyz", "(Ljava/lang/String;)V")).reachable);
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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();
    CodeReferenceMap unusedCodeMap = tracker.buildTreeShakerMap();

    assertTrue(unusedCodeMap.containsClass("A$C"));
    assertTrue(unusedCodeMap.containsMethod("A$C", "xyz", "(Ljava/lang/String;)V"));
  }

  //TODO(malvania): Enable testing for unused fields when ElementUtil glitch is fixed and fields
  //                are tracked again. (See ElementReferenceMapper line 183, fields comment.)
  //public void testUnusedField() throws IOException {
  //  String source = "import static java.lang.System.out;\n"
  //      + "import static java.lang.System.in;\n"
  //      + "class A {\n"
  //      + "  private static final int foo = 1;\n"
  //      + "  public static final String bar = \"bar\";\n"
  //      + "  static final double pi = 3.2; // in Indiana only\n"
  //      + "  final String baz = null, bah = \"123\";\n"
  //      + "  private static int abc = 9;\n"
  //      + "  private static void foo() {abc = 10;}"
  //      + "  static {foo();}"
  //      + "}\n";
  //
  //  CompilationUnit unit = compileType("test", source);
  //  final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
  //  final HashMap<String, Set<String>> overrideMap = new HashMap<>();
  //  final Set<String> staticSet = new HashSet<>();
  //  ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
  //      overrideMap);
  //  mapper.run();
  //  Set<String> elementSet = elementMap.keySet();
  //  UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
  //      overrideMap);
  //  tracker.markUsedElements();
  //
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "foo")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "bar")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "pi")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "baz")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "bah")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "abc")));
  //
  //  //TODO(malvania): Add necessary checks after visiting FieldAccess to check used/unused fields
  //}

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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();
    CodeReferenceMap unusedCodeMap = tracker.buildTreeShakerMap();

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsMethod("B", "foo", "(LA;)V"));
  }

  public void testPublicFieldInUnusedClass() throws IOException {
    String source = "class A {\n"
        + "  public int bar = 1;\n"
        + "}\n"
        + "class B {\n"
        + "  private int baz = 2;\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();
    CodeReferenceMap unusedCodeMap = tracker.buildTreeShakerMap();

    assertFalse(unusedCodeMap.containsClass("A"));
    assertTrue(unusedCodeMap.containsClass("B"));
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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementMap.keySet();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();
    CodeReferenceMap unusedCodeMap = tracker.buildTreeShakerMap();

    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "(I)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("B", "foo", "()V")));

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsMethod("B", "foo", "()V"));
    assertFalse(unusedCodeMap.containsMethod("A", "<init>", "()V"));
    assertTrue(unusedCodeMap.containsMethod("A", "<init>", "(I)V"));
  }

  public void testConstructorInvocation() throws IOException {
    String source = "class A {\n"
        + "  public A() {this(true);}"
        + "  public A(boolean b) {bar = 0;}"
        + "  public A(int i) {bar = i;}"
        + "  private int bar = 1;\n"
        + "}\n"
        + "class B {\n"
        + "  public static void foo() {new A();}\n"
        + "  static {foo();}" 
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementMap.keySet();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();
    CodeReferenceMap unusedCodeMap = tracker.buildTreeShakerMap();

    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "(Z)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "(I)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("B", "foo", "()V")));

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsMethod("B", "foo", "()V"));
    assertFalse(unusedCodeMap.containsMethod("A", "<init>", "()V"));
    assertFalse(unusedCodeMap.containsMethod("A", "<init>", "(Z)V"));
    assertTrue(unusedCodeMap.containsMethod("A", "<init>", "(I)V"));
  }

  public void testSuperConstructorInvocation() throws IOException {
    String source = "class A {\n"
        + "  public A() {this(true);}"
        + "  public A(boolean b) {bar = 0;}"
        + "  public A(int i) {bar = i;}"
        + "  private int bar = 1;\n"
        + "}\n"
        + "class B extends A {\n"
        + "  public B() {super(1);}\n"
        + "  static {new B();}" 
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementMap.keySet();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();
    CodeReferenceMap unusedCodeMap = tracker.buildTreeShakerMap();

    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "(Z)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "(I)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("B", "<init>", "()V")));

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsMethod("B", "<init>", "()V"));
    assertFalse(unusedCodeMap.containsMethod("A", "<init>", "(I)V"));
    assertTrue(unusedCodeMap.containsMethod("A", "<init>", "(Z)V"));
    assertTrue(unusedCodeMap.containsMethod("A", "<init>", "()V"));
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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementMap.keySet();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("B")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("C")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("D")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("E")));

    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "<init>", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("B", "<init>", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("C", "<init>", "()V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("D", "<init>", "()V")));

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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused2", "(Ljava/lang/String;)V")).reachable);
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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet,
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements();

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "unused2", "(Ljava/lang/String;)V")).reachable);
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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements(inputCodeMap);

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).reachable);
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
    final HashMap<String, ReferenceNode> elementMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementMap, staticSet, 
        overrideMap);
    mapper.run();
    UnusedCodeTracker tracker = new UnusedCodeTracker(unit.getEnv(), elementMap, staticSet, 
        overrideMap);
    tracker.markUsedElements(inputCodeMap);
    CodeReferenceMap unusedCodeMap = tracker.buildTreeShakerMap();

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsMethod("A", "abc", "(Ljava/lang/String;)V"));
    assertFalse(unusedCodeMap.containsMethod("A", "xyz", "(Ljava/lang/String;)V"));
    assertTrue(unusedCodeMap.containsMethod("A", "foo", "(Ljava/lang/String;)V"));

    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "abc", "(Ljava/lang/String;)V")).reachable);
    assertTrue(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "xyz", "(Ljava/lang/String;)V")).reachable);
    assertFalse(elementMap.get(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")).reachable);
  }

  //TODO(malvania): Consult:
  //Any custom type in parameters must be created by calling some constructor/initializer?
  public void testUnusedType() throws IOException {
    //TODO(malvania): Add test for unused types after visiting VariableDeclarationExpression
  }
}
