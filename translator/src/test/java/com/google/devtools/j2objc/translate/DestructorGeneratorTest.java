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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;

import java.io.IOException;

/**
 * Unit tests for {@link DestructorGenerator}.
 *
 * @author Tom Ball
 */
public class DestructorGeneratorTest extends GenerationTest {

  public void testFinalizeMethodRenamed() throws IOException {
    String translation = translateSourceFile(
        "public class Test { public void finalize() { "
        + "  try { super.finalize(); } catch (Throwable t) {} }}", "Test", "Test.h");
    assertTranslation(translation, "- (void)dealloc;");
    assertFalse(translation.contains("finalize"));
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (void)dealloc ");
    assertTranslation(translation, "[super dealloc];");
    assertFalse(translation.contains("- (void)finalize "));
  }

  public void testFinalizeMethodRenamedWithReleasableFields() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  private Object o = new Object();"
        + "  public void finalize() { "
        + "    try { super.finalize(); } catch (Throwable t) {} }}", "Test", "Test.h");
    assertTranslation(translation, "- (void)dealloc;");
    assertFalse(translation.contains("finalize"));
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (void)dealloc ");
    assertTranslation(translation, "[super dealloc];");
    assertFalse(translation.contains("- (void)finalize "));
  }

  public void testReleaseStatementsBeforeSuperDealloc() throws IOException {
    String translation = translateSourceFile(
        "public class Test { Object o; public void finalize() throws Throwable { "
        + "super.finalize(); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "RELEASE_(o_);",
        "[super dealloc];");
  }

  /**
   * Verify fields are released in a dealloc for reference counted code.
   */
  public void testFieldReleaseReferenceCounting() throws IOException {
    Options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String translation = translateSourceFile("class Test { Object o; Runnable r; }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)dealloc {",
        "RELEASE_(o_);",
        "RELEASE_(r_);",
        "[super dealloc];",
        "}");
  }

  /**
   * Verify fields are not released for ARC code, and a dealloc method is not created.
   */
  public void testFieldReleaseARC() throws IOException {
    Options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    String translation = translateSourceFile("class Test { Object o; Runnable r; }",
        "Test", "Test.m");
    assertNotInTranslation(translation, "dealloc");
  }

  /**
   * Verify fields are released for reference counted code when a finalize() method is defined.
   */
  public void testFieldReleaseFinalizeReferenceCounting() throws IOException {
    Options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String translation = translateSourceFile("class Test { Object o; Runnable r; "
        + "public void finalize() throws Throwable { System.out.println(this); }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)dealloc {",
        "[((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out_))) "
          + "printlnWithId:self];",
        "RELEASE_(o_);",
        "RELEASE_(r_);",
        "[super dealloc];",
        "}");
  }

  /**
   * Verify fields are not released for ARC code when a finalize() method is defined.
   */
  public void testFieldReleaseFinalizeARC() throws IOException {
    Options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    String translation = translateSourceFile("class Test { Object o; Runnable r;"
        + "public void finalize() throws Throwable { System.out.println(this); }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)dealloc {",
        "[((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out_))) "
          + "printlnWithId:self];",
        "}");
  }
}
