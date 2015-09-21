/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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
 * Verifies Javadoc comments are generated in a format compatible
 * with Xcode Quick Help, HeaderDoc and Doxygen.
 *
 * @author Tom Ball
 */
public class JavadocGeneratorTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    Options.setDocCommentsEnabled(true);
  }

  public void testBasicDocComments() throws IOException {
    String translation = translateSourceFile(
        "/** Class javadoc for Test. */ class Test { \n"
        + "/** Field javadoc. */\n"
        + "int i;"
        + "/** Method javadoc.\n"
        + "  * @param foo Unused.\n"
        + "  * @return always false.\n"
        + "  */\n"
        + "boolean test(int foo) { return false; } }", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for Test.");
    assertTranslation(translation, "@brief Field javadoc.");
    assertTranslatedLines(translation,
        "@brief Method javadoc.",
        "@param foo Unused.",
        "@return always false.");
  }

  public void testLinkTag() throws IOException {
    String translation = translateSourceFile(
        "/** Class javadoc for {@link Test}. */ class Test {"
        + " /** See {@link #bar}. */ void foo() {}"
        + " /** See {@link Test#bar}. */ void foo2() {}}", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for <code>Test</code>.");
    assertTranslation(translation, "@brief See <code>bar</code>.");
    assertTranslation(translation, "@brief See <code>Test.bar</code>.");
  }

  // TODO(tball): enable when we can use Guava's HtmlEscapers, or write custom escaping.
//  public void testLiteralTag() throws IOException {
//    String translation = translateSourceFile(
//        "/** Class javadoc for {@literal <Test>}. */ class Test {}", "Test", "Test.h");
//    assertTranslation(translation, "@brief Class javadoc for &lt;Test&gt;.");
//  }

  // Javadoc supports @param tags on classes, to document type parameters. Since there's
  // no equivalent in Objective C, these tags need to be removed.
  public void testClassParamTagRemoval() throws IOException {
    String translation = translateSourceFile(
        "/** Class javadoc for Test.\n"
        + " * @param <T> the test name\n"
        + " */ class Test <T> {}", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for Test.");
    assertNotInTranslation(translation, "@param");
    assertNotInTranslation(translation, "<T>");
  }

  public void testPreTags() throws IOException {
    String translation = translateSourceFile(
        // Include two <pre> tags to make sure the second isn't skipped.
        "/** Comment fragment from JSONObject.java.\n"
        + " * Encodes this object as a compact JSON string, such as:\n"
        + " * <pre>{\"query\":\"Pizza\",\"locations\":[94043,90210]}</pre>\n"
        + " * or maybe <pre>{\"query\":\"Fuel\",\"locations\":[96011]}</pre>\n"
        + " */ class Test {}", "Test", "Test.h");
    assertTranslatedLines(translation,
        "@brief Comment fragment from JSONObject.java.",
        "Encodes this object as a compact JSON string, such as:",
        "@code",
        "{\"query\":\"Pizza\",\"locations\":[94043,90210]}",
        "@endcode",
        "or maybe",
        "@code",
        "{\"query\":\"Fuel\",\"locations\":[96011]}",
        "@endcode",
        "*/");
  }

  // Verify that code tags inside of a pre tag are erased.
  public void testPreAndCodeTags() throws IOException {
    String translation = translateSourceFile(
        "/** Comment fragment from DelayQueue.java.\n"
        + " * The following code can be used to dump a delay queue into a newly\n"
        + " * allocated array of {@code Delayed}:\n"
        + "* <pre> {@code Delayed[] a = q.toArray(new Delayed[0]);}</pre>\n"
        + "* Note that {@code toArray(new Object[0])} is identical in function to\n"
        + "* {@code toArray()}.\n"
        + "*/ class Test {}", "Test", "Test.h");
    assertTranslatedLines(translation,
        "@brief Comment fragment from DelayQueue.java.",
        "The following code can be used to dump a delay queue into a newly",
        "allocated array of <code>Delayed</code>:",
        "@code",
        "Delayed[] a = q.toArray(new Delayed[0]);",
        "@endcode",
        "Note that <code>toArray(new Object[0])</code> is identical in function to",
        "<code>toArray()</code>.",
        "*/");
  }

  // Verify that the @param name text is updated if NameTable renames a parameter.
  public void testReservedParamName() throws IOException {
    String translation = translateSourceFile(
        "class Test { \n"
        + "/** Method javadoc.\n"
        + "  * @param out Unused.\n"
        + "  * @param description Unused."
        + "  */\n"
        + "void test(int out, String description) {}}", "Test", "Test.h");
    assertTranslation(translation, "@param outArg Unused.");
    assertTranslation(translation, "@param description_ Unused.");
  }
}
