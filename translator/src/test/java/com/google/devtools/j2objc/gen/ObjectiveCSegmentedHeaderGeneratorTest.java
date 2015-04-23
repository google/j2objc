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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;

import java.io.IOException;

/**
 * Tests for {@link ObjectiveCSegmentedHeaderGenerator}.
 *
 * @author Keith Stanger
 */
public class ObjectiveCSegmentedHeaderGeneratorTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    Options.enableSegmentedHeaders();
  }

  public void testTypicalPreprocessorStatements() throws IOException {
    String translation = translateSourceFile(
        "class Test { static class Inner {} }", "Test", "Test.h");
    assertTranslatedLines(translation,
        "#pragma push_macro(\"Test_INCLUDE_ALL\")",
        "#if Test_RESTRICT",
        "#define Test_INCLUDE_ALL 0",
        "#else",
        "#define Test_INCLUDE_ALL 1",
        "#endif",
        "#undef Test_RESTRICT");
    assertTranslation(translation, "#pragma pop_macro(\"Test_INCLUDE_ALL\")");
    assertTranslatedLines(translation,
        "#if !defined (_Test_) && (Test_INCLUDE_ALL || Test_INCLUDE)",
        "#define _Test_");
    assertTranslatedLines(translation,
        "#if !defined (_Test_Inner_) && (Test_INCLUDE_ALL || Test_Inner_INCLUDE)",
        "#define _Test_Inner_");
  }

  public void testIncludedType() throws IOException {
    String translation = translateSourceFile(
        "class Test implements Runnable { public void run() {} }", "Test", "Test.h");
    assertTranslatedLines(translation,
        "#define JavaLangRunnable_RESTRICT 1",
        "#define JavaLangRunnable_INCLUDE 1",
        "#include \"java/lang/Runnable.h\"");
  }

  public void testLocalInclude() throws IOException {
    String translation = translateSourceFile(
        "class Test { static class Inner extends Test {} }", "Test", "Test.h");
    assertTranslatedLines(translation,
        "#if Test_Inner_INCLUDE",
        "#define Test_INCLUDE 1",
        "#endif");
  }

  public void testLocalIncludeOfBaseClass() throws IOException {
    String translation = translateSourceFile(
        "class Test extends Foo { } class Foo {}", "Test", "Test.h");
    assertTranslatedLines(translation,
        "#if Test_INCLUDE",
        "#define Foo_INCLUDE 1",
        "#endif");
  }

  // Verify that when a class is referenced in the same source file, a header
  // isn't included for it.
  public void testPackagePrivateBaseClass() throws IOException {
    String translation = translateSourceFile(
        "package bar; public class Test extends Foo {} "
        + "abstract class Foo {}", "Test", "bar/Test.h");
    assertNotInTranslation(translation, "#include \"Foo.h\"");
  }

  public void testAddIgnoreDeprecationWarningsPragmaIfDeprecatedDeclarationsIsEnabled()
      throws IOException {
    Options.enableDeprecatedDeclarations();

    String translation = translateSourceFile("class Test {}", "Test", "Test.h");

    assertTranslation(translation, "#pragma clang diagnostic push");
    assertTranslation(translation, "#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    assertTranslation(translation, "#pragma clang diagnostic pop");
  }

  public void testForwardDeclarationForTypeInSameIncludeAsSuperclass() throws IOException {
    addSourceFile("class Foo { static class Bar { } }", "Foo.java");
    String translation = translateSourceFile(
        "class Test extends Foo { Foo.Bar bar; }", "Test", "Test.h");
    assertTranslatedLines(translation,
        "#define Foo_RESTRICT 1",
        "#define Foo_INCLUDE 1",
        "#include \"Foo.h\"");
    // Forward declaration for Foo_Bar is needed because the include of Foo.h
    // is restricted to only the Foo type.
    assertTranslation(translation, "@class Foo_Bar");
  }
}
