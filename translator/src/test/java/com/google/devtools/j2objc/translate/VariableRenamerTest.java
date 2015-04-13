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
 * Unit tests for {@link VariableRenamer}.
 *
 * @author Tom Ball, Keith Stanger
 */
public class VariableRenamerTest extends GenerationTest {

  public void testFieldHidingParameter() throws IOException {
    String source = "import java.util.*; public class Test {"
        + "private static class CheckedCollection<E> extends AbstractCollection<E> {"
        + "  Collection<E> c;"
        + "  public CheckedCollection(Collection<E> c_) { this.c = c_; }"
        + "  public int size() { return 0; }"
        + "  public Iterator<E> iterator() { return null; }}}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "- (instancetype)initWithJavaUtilCollection:(id<JavaUtilCollection>)c_Arg {");
    assertTranslation(translation, "Test_CheckedCollection_set_c_(self, c_Arg);");
  }

  public void testOverriddenFieldTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int size; } "
        + "class Subclass extends Example { int size; }"
        + "class Subsubclass extends Subclass { int size; }",
        "Example", "Example.h");
    assertTranslation(translation, "int size_;");
    assertTranslation(translation, "int size_Subclass_;");
    assertTranslation(translation, "int size_Subsubclass_;");
  }

  public void testOverriddenGenericClass() throws IOException {
    addSourceFile("class A { int foo; }", "A.java");
    addSourceFile("class C extends B<Object> { static int I; }", "C.java");
    String translation = translateSourceFile(
        "class B<T> extends A { int foo; int test() { return C.I; } }", "B", "B.h");
    // Make sure that "C" does not cause "foo" to ge renamed to "foo_B<Object>_".
    assertTranslation(translation, "int foo_B_;");
  }
}
