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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;

import java.io.IOException;

/**
 * Unit tests for the {@link HeaderImportCollector} class.
 *
 * @author Keith Stanger
 */
public class HeaderImportCollectorTest extends GenerationTest {

  public void testVarargsDeclarations() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test1(double... values) {} void test2(Runnable... values) {} }",
        "Test", "Test.h");
    assertTranslation(translation, "@class IOSDoubleArray");
    assertTranslation(translation, "@class IOSObjectArray");
    assertNotInTranslation(translation, "@protocol JavaLangRunnable");
  }

  // Same as above but with static methods and class methods removed.
  public void testVarargsDeclarationsNoClassMethods() throws IOException {
    Options.setRemoveClassMethods(true);
    String translation = translateSourceFile(
        "class Test { static void test1(double... values) {}"
        + " static void test2(Runnable... values) {} }",
        "Test", "Test.h");
    assertTranslation(translation, "@class IOSDoubleArray");
    assertTranslation(translation, "@class IOSObjectArray");
    assertNotInTranslation(translation, "@protocol JavaLangRunnable");
  }

  public void testNoSelfImports() throws IOException {
    String translation = translateSourceFile(
        "class Test { } class Test2 extends Test { }",
        "Test", "Test.h");
    assertNotInTranslation(translation, "#include \"Test.h\"");

    addSourceFile("package unit; public class Test2 extends Test { }",
        "unit/Test2.java");
    addSourceFile("package unit; public class Test { }",
        "unit/Test.java");

    // Tests that there is no self import when the subclass comes first.
    translation = translateCombinedFiles("unit/Test", ".h", "unit/Test2.java", "unit/Test.java");
    assertNotInTranslation(translation, "#include \"unit/Test.h\"");
  }

  public void testNoForwardDeclarationForPrivateDeclaration() throws IOException {
    String translation = translateSourceFile(
        "class Test { private void test(Runnable r) {} }", "Test", "Test.h");
    // We don't need to forward declare or include Runnable in the header
    // because the method is private.
    assertNotInTranslation(translation, "Runnable");
  }
}
