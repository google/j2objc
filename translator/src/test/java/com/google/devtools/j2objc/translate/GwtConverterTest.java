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
import com.google.devtools.j2objc.Options;

import java.io.IOException;

/**
 * Unit tests for {@link Rewriter}.
 *
 * @author Keith Stanger
 */
public class GwtConverterTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    addSourceFile(
        "package com.google.gwt.core.client;"
        + "public class GWT { public static <T> T create(Class<T> classLiteral) { return null; } "
        + "  public static boolean isClient() { return false; }"
        + "  public static boolean isScript() { return false; } }",
        "com/google/gwt/core/client/GWT.java");
    addSourceFile(
        "package com.google.common.annotations; "
        + "import java.lang.annotation.*; "
        + "@Retention(RetentionPolicy.CLASS) "
        + "@Target({ ElementType.METHOD }) "
        + "public @interface GwtIncompatible { "
        + "  String value(); }",
        "com/google/common/annotations/GwtIncompatible.java");
  }

  public void testGwtCreate() throws IOException {
    String translation = translateSourceFile(
        "import com.google.gwt.core.client.GWT;"
        + "class Test { "
        + "  Test INSTANCE = GWT.create(Test.class);"
        + "  String FOO = foo();"  // Regression requires subsequent non-mapped method invocation.
        + "  static String foo() { return \"foo\"; } }", "Test", "Test.m");
    assertTranslation(translation,
        "JreStrongAssign(&self->INSTANCE_, [Test_class_() newInstance]);");
  }

  public void testGwtIsScript() throws IOException {
    String translation = translateSourceFile(
        "import com.google.gwt.core.client.GWT;"
        + "class Test { boolean test() { "
        + "  if (GWT.isClient() || GWT.isScript()) { return true; } return false; }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "- (jboolean)test {", "return false;", "}");
  }

  // Verify GwtIncompatible method is not stripped by default.
  public void testGwtIncompatibleStrip() throws IOException {
    Options.setStripGwtIncompatibleMethods(true);
    String translation = translateSourceFile(
        "import com.google.common.annotations.GwtIncompatible;"
        + "class Test { "
        + "  @GwtIncompatible(\"don't use\") boolean test() { return false; }}",
        "Test", "Test.h");
    assertNotInTranslation(translation, "- (BOOL)test;");
  }

  // Verify GwtIncompatible method is stripped with flag.
  public void testGwtIncompatibleNoStrip() throws IOException {
    String translation = translateSourceFile(
        "import com.google.common.annotations.GwtIncompatible;"
        + "class Test { "
        + "  @GwtIncompatible(\"don't use\") boolean test() { return false; }}",
        "Test", "Test.h");
    assertTranslation(translation, "- (jboolean)test;");
  }

  // Verify GwtIncompatible method is not stripped with flag, if
  // value is in GwtConverter.compatibleAPIs list.
  public void testGwtIncompatibleNoStripKnownValue() throws IOException {
    Options.setStripGwtIncompatibleMethods(true);
    String translation = translateSourceFile(
        "import com.google.common.annotations.GwtIncompatible;"
        + "class Test { "
        + "  @GwtIncompatible(\"reflection\") boolean test() { return false; }}",
        "Test", "Test.h");
    assertTranslation(translation, "- (jboolean)test;");
  }

  // Regression test: GwtConverter.visit(IfStatement) threw an NPE.
  public void testGwtIsScriptElseBlock() throws IOException {
    String translation = translateSourceFile("import com.google.gwt.core.client.GWT;"
        + "class Test { String test() { "
        + "  if (GWT.isScript()) { "
        + "    return \"one\"; "
        + "  } else { "
        + "    return \"two\"; "
        + "  }}}",  "Test", "Test.m");
    assertNotInTranslation(translation, "return @\"one\";");
    assertTranslation(translation, "return @\"two\";");
  }
}
