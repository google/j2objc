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

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.util.CodeReferenceMap;

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
    CodeReferenceMap codeMap = setup.getCodeMap();

    assertTrue(codeMap.containsClass("A"));
    assertTrue(codeMap.containsClass("A$B"));
    assertTrue(codeMap.containsMethod("A$B", "bar", "()Ljava/lang/String;"));
    assertTrue(codeMap.containsMethod("A", "baz", "()V"));
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
    CodeReferenceMap codeMap = setup.getCodeMap();

    assertTrue(codeMap.containsClass("A"));
    assertTrue(codeMap.containsClass("B"));
    assertTrue(codeMap.containsField("A", "b"));
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
    CodeReferenceMap codeMap = setup.getCodeMap();

    assertTrue(codeMap.containsClass("A"));
    assertTrue(codeMap.containsClass("A$B"));
    assertTrue(codeMap.containsMethod("A$B", "B", "(LA;I)V"));
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
    CodeReferenceMap codeMap = setup.getCodeMap();

    assertTrue(codeMap.containsClass("A"));
    assertTrue(codeMap.containsField("A", "foo"));
    assertTrue(codeMap.containsField("A", "bar"));
    assertTrue(codeMap.containsField("A", "pi"));
    assertTrue(codeMap.containsField("A", "baz"));
    assertTrue(codeMap.containsField("A", "bah"));
    assertTrue(codeMap.containsField("A", "abc"));
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
    CodeReferenceMap codeMap = setup.getCodeMap();

    assertTrue(codeMap.containsClass("A"));
    assertTrue(codeMap.containsField("A", "baz"));
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
    CodeReferenceMap codeMap = setup.getCodeMap();

    assertTrue(codeMap.containsClass("A"));
    assertTrue(codeMap.containsClass("A$Thing"));
    assertTrue(codeMap.containsMethod("A$Thing", "Thing", "()V"));
    assertTrue(codeMap.containsMethod("A$Thing", "Thing", "(I)V"));
  }
}
