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
import com.google.devtools.treeshaker.ElementReferenceMapper.MethodReferenceNode;
import com.google.devtools.treeshaker.ElementReferenceMapper.ReferenceNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
    final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementReferenceMap, staticSet,
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementReferenceMap.keySet();

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
    String source = "abstract class B { public void foo() {} }\n"
        + "class A {\n"
        + "  public void launch() {"
        + "    final B b = new B() {\n"
        + "      @Override"
        + "      public void foo() {}\n"
        + "    };\n" 
        + "    b.foo();\n"
        + "  }\n"
        + "  static { new A().launch(); }"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementReferenceMap, staticSet,
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementReferenceMap.keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("B")));
    //TODO(malvania): Enable testing for unused fields when ElementUtil glitch is fixed
    //assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "b")));
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
    final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementReferenceMap, staticSet,
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementReferenceMap.keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A$B")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A$B", "A$B", "(LA;I)V")));
  }

  //TODO(malvania): Enable testing for unused fields when ElementUtil glitch is fixed and fields
  //                are tracked again. (See ElementReferenceMapper line 183, fields comment.)
  //public void testFieldsReference() throws IOException {
  //  String source = "import static java.lang.System.out;\n"
  //      + "import static java.lang.System.in;\n"
  //      + "class A {\n"
  //      + "  private static final int foo = 1;\n"
  //      + "  public static final String bar = \"bar\";\n"
  //      + "  static final double pi = 3.2; // in Indiana only\n"
  //      + "  final String baz = null, bah = \"123\";\n"
  //      + "  private int abc = 9;\n"
  //      + "}\n";
  //
  //  CompilationUnit unit = compileType("test", source);
  //  final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
  //  final HashMap<String, Set<String>> overrideMap = new HashMap<>();
  //  final Set<String> staticSet = new HashSet<>();
  //  ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementReferenceMap,
  //  staticSet, overrideMap);
  //  mapper.run();
  //  Set<String> elementSet = elementReferenceMap.keySet();
  //
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "foo")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "bar")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "pi")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "baz")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "bah")));
  //  assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "abc")));
  //}

  public void testInitializerReference() throws IOException {
    String source = "class A {\n"
        + "  static final int baz = 9;\n"
        + "  static { System.out.println(\"foo\"); }\n"
        + "  { System.out.println(\"bar\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementReferenceMap, staticSet,
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementReferenceMap.keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    //TODO(malvania): Enable testing for unused fields when ElementUtil glitch is fixed
    //assertTrue(elementSet.contains(ElementReferenceMapper.stitchFieldIdentifier("A", "baz")));
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
    final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementReferenceMap, staticSet,
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementReferenceMap.keySet();

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
        + "  private static void foo(String s) {final int bro = 1;}\n"
        + "  private static void bar(String s) {foo(\"boo\");}\n"
        + "  static { bar(\"bro\"); }\n"
        + "}\n";

    CompilationUnit unit = compileType("test", source);
    final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementReferenceMap, staticSet,
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementReferenceMap.keySet();

    assertTrue(elementSet.contains(ElementReferenceMapper.stitchClassIdentifier("A")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "foo", "(Ljava/lang/String;)V")));
    assertTrue(elementSet.contains(ElementReferenceMapper
        .stitchMethodIdentifier("A", "bar", "(Ljava/lang/String;)V")));
    assertTrue(((MethodReferenceNode) elementReferenceMap.get(ElementReferenceMapper
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
    final HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
    final HashMap<String, Set<String>> overrideMap = new HashMap<>();
    final Set<String> staticSet = new HashSet<>();
    ElementReferenceMapper mapper = new ElementReferenceMapper(unit, elementReferenceMap, staticSet,
        overrideMap);
    mapper.run();
    Set<String> elementSet = elementReferenceMap.keySet();

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
}
