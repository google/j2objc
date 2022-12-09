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
    assertTranslation(translation, "- (void)java_finalize;");
    assertFalse(translation.contains("dealloc"));
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (void)java_finalize {");
    assertTranslatedLines(translation,
        "- (void)dealloc {",
        "  JreCheckFinalize(self, [Test class]);",
        "  [super dealloc];",
        "}");
  }

  public void testFinalizeMethodRenamedWithReleasableFields() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String translation =
        translateSourceFile(
            "public class Test {"
                + "  private Object o = new Object();"
                + "  public void finalize() { "
                + "    try { super.finalize(); } catch (Throwable t) {} }}",
            "Test",
            "Test.h");
    assertTranslation(translation, "- (void)java_finalize;");
    assertFalse(translation.contains("dealloc"));
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (void)java_finalize {");
    assertTranslatedLines(translation,
        "- (void)dealloc {",
        "  JreCheckFinalize(self, [Test class]);",
        "  RELEASE_(o_);",
        "  [super dealloc];",
        "}");
  }

  public void testFinalizeMethodRenamedWithReleasableFieldsStrictField() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    options.setStrictFieldAssign(true);
    options.setStrictFieldLoad(true);
    String translation =
        translateSourceFile(
            "public class Test {"
                + "  private Object o = new Object();"
                + "  public void finalize() { "
                + "    try { super.finalize(); } catch (Throwable t) {} }}",
            "Test",
            "Test.h");
    assertTranslation(translation, "- (void)java_finalize;");
    assertFalse(translation.contains("dealloc"));
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (void)java_finalize {");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "  JreCheckFinalize(self, [Test class]);",
        "  JreStrictFieldStrongRelease(&o_);",
        "  [super dealloc];",
        "}");
  }

  public void testReleaseStatementsBeforeSuperDealloc() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String translation =
        translateSourceFile(
            "public class Test { Object o; public void finalize() throws Throwable { "
                + "super.finalize(); } }",
            "Test",
            "Test.m");
    assertTranslatedLines(translation,
        "RELEASE_(o_);",
        "[super dealloc];");
  }

  public void testReleaseStatementsBeforeSuperDeallocStrictField() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    options.setStrictFieldAssign(true);
    options.setStrictFieldLoad(true);
    String translation =
        translateSourceFile(
            "public class Test { Object o; public void finalize() throws Throwable { "
                + "super.finalize(); } }",
            "Test",
            "Test.m");
    assertTranslatedLines(translation, "JreStrictFieldStrongRelease(&o_);", "[super dealloc];");
  }

  /** Verify fields are released in a dealloc for reference counted code. */
  public void testFieldReleaseReferenceCounting() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String source =
        "import com.google.j2objc.annotations.RetainedWith; "
            + "class Test { "
            + "  Object o; "
            + "  @RetainedWith Runnable r; "
            + "}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "RELEASE_(o_);",
        "JreRetainedWithRelease(self, r_);",
        "[super dealloc];",
        "}");
  }

  public void testFieldReleaseReferenceCountingStrictField() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    options.setStrictFieldAssign(true);
    options.setStrictFieldLoad(true);
    String source =
        "import com.google.j2objc.annotations.RetainedWith; "
            + "class Test { "
            + "  Object o; "
            + "  @RetainedWith Runnable r; "
            + "}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "JreStrictFieldStrongRelease(&o_);",
        "JreStrictFieldRetainedWithRelease(self, &r_);",
        "[super dealloc];",
        "}");
  }

  /** Verify fields are not released for ARC code, and a dealloc method is not created. */
  public void testFieldReleaseARC() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    String source =
        "import com.google.j2objc.annotations.RetainedWith; "
            + "class Test { "
            + "  Object o; "
            + "  @RetainedWith Runnable r; "
            + "}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertNotInTranslation(translation, "dealloc");
  }

  /**
   * Verify fields still use JreStrictFieldStrongRelease() in ARC mode with strict field assignment.
   */
  public void testFieldReleaseARCStrictField() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    options.setStrictFieldAssign(true);
    options.setStrictFieldLoad(true);
    String source =
        "import com.google.j2objc.annotations.RetainedWith; "
            + "class Test { "
            + "  Object o; "
            + "}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation, "- (void)dealloc {", "JreStrictFieldStrongRelease(&o_);", "}");
  }

  /** Verify volatile fields are released in a dealloc for reference counted code. */
  public void testVolatileFieldReleaseReferenceCounting() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String source =
        "import com.google.j2objc.annotations.RetainedWith; "
            + "class Test { "
            + "  volatile Object v; "
            + "}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "JreReleaseVolatile(&v_);",
        "[super dealloc];",
        "}");
  }

  /**
   * Verify only volatile fields are released for ARC code, and a dealloc method still created.
   */
  public void testVolatileFieldReleaseARC() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    String source =
        "import com.google.j2objc.annotations.RetainedWith; "
            + "class Test { "
            + "  volatile Object v; "
            + "}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "JreReleaseVolatile(&v_);",
        "}");
  }

  /**
   * Verify fields are released for reference counted code when a finalize() method is defined.
   */
  public void testFieldReleaseFinalizeReferenceCounting() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String translation =
        translateSourceFile(
            "class Test { Object o; Runnable r; "
                + "public void finalize() throws Throwable { System.out.println(this); }}",
            "Test",
            "Test.m");
    assertTranslatedLines(translation,
        "- (void)java_finalize {",
        "  [((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out))) "
          + "printlnWithId:self];",
        "}");
    assertTranslatedLines(translation,
        "- (void)dealloc {",
        "  JreCheckFinalize(self, [Test class]);",
        "  RELEASE_(o_);",
        "  RELEASE_(r_);",
        "  [super dealloc];",
        "}");
  }

  public void testFieldReleaseFinalizeReferenceCountingStrictField() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    options.setStrictFieldAssign(true);
    options.setStrictFieldLoad(true);
    String translation =
        translateSourceFile(
            "class Test { Object o; Runnable r; "
                + "public void finalize() throws Throwable { System.out.println(this); }}",
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)java_finalize {",
        "  [((JavaIoPrintStream *)"
            + " nil_chk(JreStrictFieldStrongLoad(JreLoadStaticRef(JavaLangSystem, out)))) "
            + "printlnWithId:self];",
        "}");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "  JreCheckFinalize(self, [Test class]);",
        "  JreStrictFieldStrongRelease(&o_);",
        "  JreStrictFieldStrongRelease(&r_);",
        "  [super dealloc];",
        "}");
  }

  /** Verify fields are not released for ARC code when a finalize() method is defined. */
  public void testFieldReleaseFinalizeARC() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    String translation =
        translateSourceFile(
            "class Test { Object o; Runnable r;"
                + "public void finalize() throws Throwable { System.out.println(this); }}",
            "Test",
            "Test.m");
    assertTranslatedLines(translation,
        "- (void)java_finalize {",
        "  [((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out))) "
          + "printlnWithId:self];",
        "}");
    assertTranslatedLines(translation,
        "- (void)dealloc {",
        "  JreCheckFinalize(self, [Test class]);",
        "}");
  }

  public void testFieldReleaseFinalizeARCStrictField() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    options.setStrictFieldAssign(true);
    options.setStrictFieldLoad(true);
    String translation =
        translateSourceFile(
            "class Test { Object o; Runnable r;"
                + "public void finalize() throws Throwable { System.out.println(this); }}",
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)java_finalize {",
        "  [((JavaIoPrintStream *)"
            + " nil_chk(JreStrictFieldStrongLoad(JreLoadStaticRef(JavaLangSystem, out)))) "
            + "printlnWithId:self];",
        "}");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "  JreCheckFinalize(self, [Test class]);",
        "  JreStrictFieldStrongRelease(&o_);",
        "  JreStrictFieldStrongRelease(&r_);",
        "}");
  }

  static final String ON_DEALLOC_SOURCE =
      "import com.google.j2objc.annotations.OnDealloc;"
          + "class Test {"
          + "  @OnDealloc private void close() {}"
          + "  protected void finalize() {}"
          + "}";

  public void testOnDeallocReferenceCounting() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String translation = translateSourceFile(ON_DEALLOC_SOURCE, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "  Test_close(self);",
        "  JreCheckFinalize(self, [Test class]);",
        "  [super dealloc];",
        "}");
  }

  public void testOnDeallocARC() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    String translation = translateSourceFile(ON_DEALLOC_SOURCE, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)dealloc {",
        "  Test_close(self);",
        "  JreCheckFinalize(self, [Test class]);",
        "}");
  }
}
