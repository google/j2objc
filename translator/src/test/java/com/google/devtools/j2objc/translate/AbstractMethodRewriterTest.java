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

import java.io.IOException;

/**
 * Unit tests for {@link AbstractMethodRewriter}.
 *
 * @author Tom Ball
 */
public class AbstractMethodRewriterTest extends GenerationTest {

  /**
   * Verifies that a pragma ignored is added when an abstract class does not
   * implement all interface methods.
   */
  public void testAbstractMethodsAdded() throws IOException {
    String source =
        "import java.util.Iterator; public abstract class Test implements Iterator<Test> { "
            + "public boolean hasNext() { return true; } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "#pragma clang diagnostic ignored \"-Wprotocol\"");
  }

  /**
   * Verify that super-interface methods are checked.
   */
  public void testAbstractClassGrandfatherInterface() throws IOException {
    String source =
        "public class Test {"
        + "  public interface I1 { void foo(); } "
        + "  public interface I2 extends I1 { } "
        + "  public abstract class Inner implements I2 { } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "#pragma clang diagnostic ignored \"-Wprotocol\"");
  }

  public void testAddsPragmaToAbstractEnum() throws IOException {
    String interfaceSource = "interface I { public int foo(); }";
    String enumSource =
        "enum E implements I { "
        + "  A { public int foo() { return 42; } },"
        + "  B { public int foo() { return -1; } } }";
    addSourceFile(interfaceSource, "I.java");
    addSourceFile(enumSource, "E.java");
    String translation = translateSourceFile("E", "E.m");
    assertTranslation(translation, "#pragma clang diagnostic ignored \"-Wprotocol\"");
  }

  public void testMethodAddedForSpecifiedTypeArg() throws IOException {
    addSourceFile("interface A<T> { T foo(); }", "A.java");
    addSourceFile("interface B extends A<String> {}", "B.java");
    addSourceFile("abstract class C implements A<String> {}", "C.java");
    String aHeader = translateSourceFile("A", "A.h");
    String bHeader = translateSourceFile("B", "B.h");
    String bSource = getTranslatedFile("B.m");
    String cHeader = translateSourceFile("C", "C.h");
    String cSource = getTranslatedFile("C.m");
    assertTranslation(aHeader, "- (id)foo;");
    assertTranslation(bHeader, "- (NSString *)foo;");
    // The added "foo" method should not appear in metadata.
    assertNotInTranslation(bSource, "foo");
    assertTranslation(cHeader, "- (NSString *)foo;");
    // The added "foo" method should not appear in metadata or have an implementation.
    assertNotInTranslation(cSource, "foo");
  }

  public void testMethodAddedForMethodInheritedFromMultipleInterfaces() throws IOException {
    addSourceFile("interface A { java.io.Serializable foo(); }", "A.java");
    addSourceFile("interface B { String foo(); }", "B.java");
    addSourceFile("abstract class C implements A, B {}", "C.java");
    String cHeader = translateSourceFile("C", "C.h");
    String cSource = translateSourceFile("C", "C.m");
    assertTranslation(cHeader, "- (NSString *)foo;");
    assertNotInTranslation(cSource, "foo");
  }

  public void testGenericPrivateMethodNotAdded() throws IOException {
    String superSource = "abstract class Super<T> { "
        + "  private T returnT() { return null; } "
        + "}";
    String subSource = "class Sub extends Super<Void> {}";
    addSourceFile(superSource, "Super.java");
    addSourceFile(subSource, "Sub.java");
    String superTranslation = translateSourceFile("Super", "Super.m");
    String subTranslation = translateSourceFile("Sub", "Sub.m");
    // Super translation should contain the generic private method declaration.
    assertTranslation(superTranslation, "- (id)returnT;");
    // But Sub translation should not even though the return type is now resolved.
    assertNotInTranslation(subTranslation, "returnT");
  }
}
