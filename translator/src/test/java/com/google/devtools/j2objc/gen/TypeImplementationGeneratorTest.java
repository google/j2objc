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
import java.io.IOException;

/**
 * Tests for {@link TypeImplementationGenerator}.
 *
 * @author Keith Stanger
 */
public class TypeImplementationGeneratorTest extends GenerationTest {

  public void testFieldAnnotationMethodForAnnotationType() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "@interface A { @Deprecated int I = 5; }", "A", "A.m");
    assertTranslation(translation, "IOSObjectArray *A__Annotations$0()");
    assertTranslation(translation, "create_JavaLangDeprecated");
  }

  public void testFieldAnnotationMethodForInterfaceType() throws IOException {
    String translation = translateSourceFile(
        "interface I { @Deprecated int I = 5; }", "I", "I.m");
    assertTranslation(translation, "IOSObjectArray *I__Annotations$0()");
    assertTranslation(translation, "create_JavaLangDeprecated");
  }

  public void testFunctionLineNumbers() throws IOException {
    options.setEmitLineDirectives(true);
    String translation = translateSourceFile("class A {\n\n"
        + "  static void test() {\n"
        + "    System.out.println(A.class);\n"
        + "  }}", "A", "A.m");
    assertTranslatedLines(translation,
        "#line 3", "void A_test() {", "A_initialize();", "", "#line 4",
        "[((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out))) "
          + "printlnWithId:A_class_()];");
  }

  // Regression for non-static constants used in switch statements.
  // https://github.com/google/j2objc/issues/492
  public void testNonStaticPrimitiveConstant() throws IOException {
    String translation = translateSourceFile(
        "class Test { final int I = 1; void test(int i) { switch(i) { case I: return; } } }",
        "Test", "Test.h");
    assertTranslation(translation, "#define Test_I 1");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "case Test_I:");
  }

  public void testDesignatedInitializer() throws IOException {
    String translation = translateSourceFile(
        "class Test extends Number { Test(int i) {} public double doubleValue() { return 0; } "
        + " public float floatValue() { return 0; } public int intValue() { return 0; } "
        + " public long longValue() { return 0; }}", "Test", "Test.m");
    assertTranslatedLines(translation,
        "J2OBJC_IGNORE_DESIGNATED_BEGIN",
        "- (instancetype)initWithInt:(int32_t)i {",
        "  Test_initWithInt_(self, i);",
        "  return self;",
        "}",
        "J2OBJC_IGNORE_DESIGNATED_END");
  }

  // Verify that accessor methods for static vars and constants are generated on request.
  private void staticFieldAccessorMethodValidation() throws IOException {
    String source = "class Test { "
        + "static String ID; "
        + "private static int i; "
        + "public static final int VERSION = 1; "
        + "static final Test DEFAULT = new Test(); }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(translation, "+ (NSString *)ID {", "return Test_ID;");
    assertTranslatedLines(translation,
        "+ (void)setID:(NSString *)value {", "JreStrongAssign(&Test_ID, value);");
    assertTranslatedLines(translation, "+ (int32_t)VERSION {", "return Test_VERSION;");
    assertTranslatedLines(translation, "+ (Test *)DEFAULT {", "return Test_DEFAULT;");
    assertNotInTranslation(translation, "+ (void)setDEFAULT:(Test *)value"); // Read-only
    assertNotInTranslation(translation, "+ (int32_t)i");                        // Private
    assertNotInTranslation(translation, "+ (void)setI:(int32_t)value");         // Private
  }

  private void staticFieldAccessorMethodValidationStrictFieldAssign() throws IOException {
    String source =
        "class Test { "
            + "static String ID; "
            + "private static int i; "
            + "public static final int VERSION = 1; "
            + "static final Test DEFAULT = new Test(); }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(translation, "+ (NSString *)ID {", "return Test_ID;");
    assertTranslatedLines(
        translation,
        "+ (void)setID:(NSString *)value {",
        "JreStrictFieldStrongAssign(&Test_ID, value);");
    assertTranslatedLines(translation, "+ (int32_t)VERSION {", "return Test_VERSION;");
    assertTranslatedLines(translation, "+ (Test *)DEFAULT {", "return Test_DEFAULT;");
    assertNotInTranslation(translation, "+ (void)setDEFAULT:(Test *)value"); // Read-only
    assertNotInTranslation(translation, "+ (int32_t)i"); // Private
    assertNotInTranslation(translation, "+ (void)setI:(int32_t)value"); // Private
  }

  private void staticFieldAccessorMethodValidationStrictFieldLoad() throws IOException {
    String source =
        "class Test { "
            + "static String ID; "
            + "private static int i; "
            + "public static final int VERSION = 1; "
            + "static final Test DEFAULT = new Test(); }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation, "+ (NSString *)ID {", "return JreStrictFieldStrongLoad(&Test_ID);");
    assertTranslatedLines(
        translation,
        "+ (void)setID:(NSString *)value {",
        "JreStrictFieldStrongAssign(&Test_ID, value);");
    assertTranslatedLines(translation, "+ (int32_t)VERSION {", "return Test_VERSION;");
    assertTranslatedLines(
        translation, "+ (Test *)DEFAULT {", "return JreStrictFieldStrongLoad(&Test_DEFAULT);");
    assertNotInTranslation(translation, "+ (void)setDEFAULT:(Test *)value"); // Read-only
    assertNotInTranslation(translation, "+ (int32_t)i"); // Private
    assertNotInTranslation(translation, "+ (void)setI:(int32_t)value"); // Private
  }

  public void testStaticFieldsWithStaticAccessorMethodsFlag() throws IOException {
    options.setStaticAccessorMethods(true);
    staticFieldAccessorMethodValidation();
    options.setStrictFieldAssign(true);
    staticFieldAccessorMethodValidationStrictFieldAssign();
    options.setStrictFieldLoad(true);
    staticFieldAccessorMethodValidationStrictFieldLoad();
  }

  public void testStaticFieldsWithClassPropertiesFlag() throws IOException {
    options.setClassProperties(true);
    staticFieldAccessorMethodValidation();
    options.setStrictFieldAssign(true);
    staticFieldAccessorMethodValidationStrictFieldAssign();
    options.setStrictFieldLoad(true);
    staticFieldAccessorMethodValidationStrictFieldLoad();
  }

  // Verify that accessor methods for static vars and constants aren't generated by default.
  public void testNoStaticFieldAccessorMethods() throws IOException {
    String source = "class Test { "
        + "static String ID; "
        + "private static int i; "
        + "public static final int VERSION = 1; "
        + "static final Test DEFAULT = new Test(); }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertNotInTranslation(translation, "+ (NSString *)ID");
    assertNotInTranslation(translation, "+ (void)setID:(NSString *)value");
    assertNotInTranslation(translation, "+ (void)setI:(int32_t)value");
    assertNotInTranslation(translation, "+ (int32_t)VERSION");
    assertNotInTranslation(translation, "+ (Test *)DEFAULT");
  }

  // Verify that accessor methods for static vars and constants are not generated when they are
  // already provided.
  public void testNoStaticFieldAccessorMethodsWhenAlreadyProvided() throws IOException {
    options.setStaticAccessorMethods(true);
    String source =
        "class Test { "
            + "  static String ID; "
            + "  static String getID() { return ID; } "
            + "  static void setID(String ID) { Test.ID = ID; } "
            + "}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertNotInTranslation(translation, "+ (NSString *)ID {");
    assertNotInTranslation(translation, "+ (void)setID:(NSString *)value {");
    assertTranslation(translation, "+ (NSString *)getID {");
    assertTranslation(translation, "+ (void)setIDWithNSString:(NSString *)ID {");
  }

  // Verify that accessor methods for enum constants are generated on request.
  private void enumConstantAccessorMethodValidation() throws IOException {
    String source = "enum Test { ONE, TWO, EOF }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(translation, "+ (Test *)ONE {", "return JreEnum(Test, ONE);");
    assertTranslatedLines(translation, "+ (Test *)TWO {", "return JreEnum(Test, TWO);");
    assertTranslatedLines(translation, "+ (Test *)EOF_ {", "return JreEnum(Test, EOF);");
  }

  public void testEnumConstantWithStaticAccessorMethodsFlag() throws IOException {
    options.setStaticAccessorMethods(true);
    enumConstantAccessorMethodValidation();
  }

  public void testEnumConstantWithClassPropertiesFlag() throws IOException {
    options.setClassProperties(true);
    enumConstantAccessorMethodValidation();
  }

  // Verify that accessor methods for enum constants are not generated by default.
  public void testNoEnumConstantAccessorMethods() throws IOException {
    String source = "enum Test { ONE, TWO }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertNotInTranslation(translation, "+ (TestEnum *)ONE");
    assertNotInTranslation(translation, "+ (TestEnum *)TWO");
  }

  // Verify that specified properties are synthesized.
  public void testSynthesizeProperties() throws IOException {
    String source = "import com.google.j2objc.annotations.Property; class Test { "
        + "@Property(\"getter=getFoo\") private final Integer foo = 42;"
        + "private final Integer bar = 84; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "@synthesize foo = foo_;");
    assertNotInTranslation(translation, "@synthesize bar");
  }

  public void testPrivateClassesPackageSwiftName() throws IOException {
    addSourceFile(
        "@SwiftName " + "package bar;" + "" + "import com.google.j2objc.annotations.SwiftName;",
        "bar/package-info.java");
    String source =
        "package bar;"
            + "public class Test { "
            + "  private class Inner { "
            + "       public void foo() { }"
            + "   }"
            + "  private Inner bar; "
            + "}";

    String translation = translateSourceFile(source, "Test", "bar/Test.m");
    assertNotInTranslation(translation, "NS_SWIFT_NAME");
  }

  public void testPrivateClassesWithSwiftName() throws IOException {
    String source =
        "import com.google.j2objc.annotations.SwiftName;"
            + "@SwiftName public class Test { "
            + "  @SwiftName private class Inner { "
            + "       public void foo() { }"
            + "   }"
            + "  private Inner bar; "
            + "}";

    translateSourceFile(source, "Test", "Test.m");
    assertError("Test.java:1: Swift name annotation on private type");
  }

  public void testLinkProtocolsFunctions() throws IOException {
    options.setLinkProtocols(true);
    String source =
        "import java.io.Serializable;\n"
            + "class Test implements Runnable, Serializable { public void run() {} }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "+ (void)__linkProtocols {");
    assertTranslation(translation, "JavaIoSerializable_class_();");
    assertTranslation(translation, "JavaLangRunnable_class_();");
  }
}
