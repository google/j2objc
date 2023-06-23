package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.IOException;

/**
 * Unit tests for ReflectionCodeDetector.
 *
 * @author Gabriel Curtis
 */
public class ReflectionCodeDetectorTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    options.setStripReflectionErrors(true);
  }

  public void testNoReflectionSupportWithoutStrippedMetadata_unsafeReflectionCalls()
      throws IOException {
    options.setStripReflection(false);
    translateSourceFile(
        "package p; "
            + "public class Test { "
            + "class Foo {}; "
            + "Class ec = Foo.class.getEnclosingClass();"
            + "}",
        "p.Test",
        "p/Test.m");
    assertEquals(0, ErrorUtil.errorCount());
  }

  public void testReflectionSupportWithStrippedMetadata_unsafeReflectionCalls() throws IOException {
    options.setStripReflection(true);
    translateSourceFile(
        "package p; import com.google.j2objc.annotations.ReflectionSupport; "
            + "@ReflectionSupport(value = ReflectionSupport.Level.FULL) public class Test { "
            + " class Foo {}; "
            + " Class ec = Foo.class.getEnclosingClass();"
            + "}",
        "p.Test",
        "p/Test.m");
    assertEquals(0, ErrorUtil.errorCount());
  }

  public void testReflectionSupportWithStrippedMetadata_nestedUnsafeCalls() throws IOException {
    options.setStripReflection(true);
    translateSourceFile(
        "package p; import com.google.j2objc.annotations.ReflectionSupport; "
            + "@ReflectionSupport(value = ReflectionSupport.Level.FULL) public class Test { "
            + " class Foo { "
            + "public void bar() { "
            + "Foo.class.getEnclosingClass(); "
            + " } "
            + " } "
            + " }",
        "p.Test",
        "p/Test.m");
    assertEquals(0, ErrorUtil.errorCount());
  }

  // TODO: b/277934135 - Add support for reflection code detection across different files.
  /*
  public void testReflectionSupportWithStrippedMetadata_unsafeCallsFromDifferentClass()
      throws IOException {
    options.setStripReflection(true);
    addSourceFile("public class Foo {}", "Foo.java");
    addSourceFile(
        "import com.google.j2objc.annotations.ReflectionSupport; "
            + "@ReflectionSupport(value = ReflectionSupport.Level.FULL) public class Test { "
            + "public void bar() { "
            + "Foo.class.getEnclosingClass(); "
            + " } "
            + " }",
        "Test.java");
    String testTranslation = translateSourceFile("Test", "Test.h");
    assertNotNull(testTranslation);
    assertReflectionStrippedError("p/Test.java", "1", "getEnclosingClass");
  }
  */

  public void testNoReflectionSupportWithStrippedMetadata_safeReflectionCalls() throws IOException {
    options.setStripReflection(true);
    translateSourceFile(
        "package p; "
            + "public class Test { "
            + "private void test() { "
            + "try { "
            + " Class.forName(\"java.lang.Integer\"); "
            + " } "
            + "finally { return; } "
            + " }"
            + " }",
        "p.Test",
        "p/Test.m");
    assertEquals(0, ErrorUtil.errorCount());
    ;
  }

  public void testNoReflectionSupportWithStrippedMetadata_unsafeReflectionCalls()
      throws IOException {
    options.setStripReflection(true);
    translateSourceFile(
        "package p; "
            + "public class Test { "
            + "class Foo {}; "
            + "Class ec = Foo.class.getEnclosingClass();"
            + " }",
        "p.Test",
        "p/Test.m");
    assertReflectionStrippedError("p/Test.java", "1", "getEnclosingClass");
  }


  protected void assertReflectionStrippedError(
      String sourcePath, String lineNumber, String methodName) {
    assertError(
        String.format(
            "%s:%s: %s",
            sourcePath,
            lineNumber,
            "[" + methodName + "] " + ReflectionCodeDetector.UNSAFE_REFLECTION_CODE_MESSAGE));
  }
}
