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
    options.setDocCommentsEnabled(true);
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
        + " /** See {@linkplain #bar}. */ void foo2() {}"
        + " /** See {@link Test#bar()}. */ void foo3() {}"
        + " /** See {@link foo.bar.Mumble Mumble}.*/ void foo4() {}}", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for <code>Test</code>.");
    assertTranslation(translation, "@brief See <code>bar</code>.");
    assertTranslation(translation, "@brief See bar.");
    assertTranslation(translation, "@brief See <code>Test.bar()</code>.");
    assertTranslation(translation, "@brief See <code>Mumble</code>.");
  }

  public void testLiteralTag() throws IOException {
    String translation = translateSourceFile(
        "/** Class javadoc for {@literal <Test>}. */ class Test {}", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for &lt;Test&gt;.");
  }

  // Javadoc supports @param tags on classes, to document type parameters. Since there's
  // no equivalent in Objective C, these tags need to be removed.
  public void testTypeParamTagRemoval() throws IOException {
    String translation = translateSourceFile(
        "/** Class javadoc for Test.\n"
        + " * @param <T> the test name\n"
        + " */ class Test <T> {\n"
        + "  /** Method javadoc.\n"
        + "   * @param <T> the type to be returned.\n"
        + "   */ T test() { return null; }}", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for Test.");
    assertTranslation(translation, "@brief Method javadoc.");
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
    assertTranslatedSegments(translation,
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

  // Verify that tags without following text are skipped, such as "@param\n".
  public void testSkipEmptyTags() throws IOException {
    String translation = translateSourceFile(
        "/** Class javadoc for Test.\n"
        + " * @see\n"
        + " * @since\n"
        + " */ class Test { \n"
        + "/** Method javadoc.\n"
        + "  * @param \n"
        + "  * @return\n"
        + "  * @throws\n"
        + "  */\n"
        + "boolean test(int foo) { return false; } }", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for Test.");
    assertNotInTranslation(translation, "@see");
    assertNotInTranslation(translation, "@since");
    assertNotInTranslation(translation, "@param");
    assertNotInTranslation(translation, "@return");
    assertNotInTranslation(translation, "@throws");
  }

  public void testBadPreTag() throws IOException {
    String translation = translateSourceFile(
        "/** Example:\n"
        + " * </pre>\n"       // Closing tag before opening one below.
        + " * class Foo {\n"
        + " *   Foo bar;\n"
        + " * }<pre>\n"
        + " */\n"
        + "class Test {}", "Test", "Test.h");
    assertTranslation(translation, "@code");
  }

  // Verify that the formatting inside <pre>...</pre> and @{code ...} isn't reformatted.
  public void testPreserveLiteralFormatting() throws IOException {
    String translation = translateSourceFile(
        "/** Example:\n"
        + " * <pre>\n"
        + " * class Foo {\n"
        + " *   &#64;Property(\"copy, nonatomic\")\n"
        + " *       protected String bar;\n"
        + " * }</pre>\n"
        + " *\n"           // Make sure "short" lines are handled correctly.
        + " */\n"
        + "class Test {}", "Test", "Test.h");
    assertTranslation(translation, "  &#64;Property(\"copy, nonatomic\")\n");
    assertTranslation(translation, "      protected String bar;\n");

    // Same test, but without leading '*' in comment lines.
    translation = translateSourceFile(
        "/** Example:\n"
        + "<pre>\n"
        + "class Foo {\n"
        + "  &#64;Property(\"copy, nonatomic\")\n"
        + "      protected String bar;\n"
        + "}</pre>\n"
        + " */\n"
        + "class Test {}", "Test", "Test.h");
    assertTranslation(translation, "  &#64;Property(\"copy, nonatomic\")\n");
    assertTranslation(translation, "      protected String bar;\n");
  }

  // Verify style tags are skipped, since Quick Help displays them.
  public void testStyleTagsSkipped() throws IOException {
    String translation = translateSourceFile(
        "/** <h3>Regular expression syntax</h3>\n"
        + " * <span class=\"datatable\">\n"
        + " * <style type=\"text/css\">\n"
        + " * .datatable td { padding-right: 20px; }\n"
        + " * </style>\n"
        + " */\n"
        + "class Test {}", "Test", "Test.h");
    assertNotInTranslation(translation, "<style");
    assertTranslatedLines(translation,
        "/*!",
        "@brief <h3>Regular expression syntax</h3>",
        "<span class=\"datatable\">",
        "*/");
  }

  public void testSeeTag() throws IOException {
    String translation = translateSourceFile(
        "/** Class javadoc for Test.\n"
        + " * @see <a href=\"http://developers.facebook.com/docs/reference/javascript/FB.init/\">"
        + "FB.init</a>\n"
        + " */ class Test {}", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for Test.");
    assertTranslation(translation,
        "- seealso: "
        + "<a href=\"http://developers.facebook.com/docs/reference/javascript/FB.init/\">");
  }

  // Verify that unknown tags are not printed.
  public void testUnknownTag() throws IOException {
    String translation = translateSourceFile(
        "/** Class javadoc for Test.\n"
        + " * @jls 11.2 Some JLS reference.\n"
        + " */ class Test {}", "Test", "Test.h");
    assertTranslation(translation, "@brief Class javadoc for Test.");
    assertNotInTranslation(translation, "11.2 Some JLS reference.");
  }
}
