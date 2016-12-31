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
 * Tests Java 8 type annotations.
 *
 * @author Keith Stanger
 */
public class TypeUseAnnotationTest extends GenerationTest {

  // Regression for Issue #730.
  public void testAnnotatedStringType() throws IOException {
    addSourceFile(
        "import java.lang.annotation.*;\n"
        + "@Target(ElementType.TYPE_USE) public @interface A {}", "A.java");
    String translation = translateSourceFile(
        "class Test { @A String str; @A String foo() { return null; } }",
        "Test", "Test.m");
    assertNotInTranslation(translation, "java/lang/String.h");
    assertNotInTranslation(translation, "JavaLangString");
  }

  // TODO(nbraswell): Use com.google.j2objc.annotations.WeakOuter when transitioned to Java 8
  String testWeakOuterSetup = "import java.lang.annotation.*;\n"
      + "@Target(ElementType.TYPE_USE) @interface WeakOuter {}"
      + "interface Simple { public int run(); }"
      + "class SimpleClass { public int run() {return 1;}; }"
      + "abstract class SimpleAbstractClass { public int run(){return 2;}; }"
      + "class Test { int member = 7; Object o;";

  public void testWeakOuterInterface() throws IOException {
    String translationInterfaceWithWeak = translateSourceFile(testWeakOuterSetup
        + "void f() { o = new @WeakOuter Simple() { public int run() { return member; } }; } }",
        "Test", "Test.m");
    assertTranslation(translationInterfaceWithWeak, "__unsafe_unretained Test *this$0_;");

    String translationInterfaceWithoutWeak = translateSourceFile(testWeakOuterSetup
        + "void f() { o = new Simple() { public int run() { return member; } }; } }",
        "Test", "Test.m");
    assertNotInTranslation(translationInterfaceWithoutWeak, "__unsafe_unretained Test *this$0_;");
  }

  public void testWeakOuterClass() throws IOException {
    String translationInterfaceWithWeak = translateSourceFile(testWeakOuterSetup
        + "void f() { o = new @WeakOuter SimpleClass() {"
        + "public int run() { return member; } }; } }",
        "Test", "Test.m");
    assertTranslation(translationInterfaceWithWeak, "__unsafe_unretained Test *this$0_;");

    String translationInterfaceWithoutWeak = translateSourceFile(testWeakOuterSetup
        + "void f() { o = new SimpleClass() { public int run() { return member; } }; } }",
        "Test", "Test.m");
    assertNotInTranslation(translationInterfaceWithoutWeak, "__unsafe_unretained Test *this$0_;");
  }

  public void testWeakOuterAbstractClass() throws IOException {
    String translationInterfaceWithWeak = translateSourceFile(testWeakOuterSetup
        + "void f() { o = new @WeakOuter SimpleAbstractClass() {"
        + "public int run() { return member; } }; } }",
        "Test", "Test.m");
    assertTranslation(translationInterfaceWithWeak, "__unsafe_unretained Test *this$0_;");

    String translationInterfaceWithoutWeak = translateSourceFile(testWeakOuterSetup
        + "void f() { o = new SimpleAbstractClass() { public int run() { return member; } }; } }",
        "Test", "Test.m");
    assertNotInTranslation(translationInterfaceWithoutWeak, "__unsafe_unretained Test *this$0_;");
  }
}
