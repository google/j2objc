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
import com.google.devtools.j2objc.Options.MemoryManagementOption;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Tests for {@link TypeDeclarationGenerator}.
 *
 * @author Keith Stanger
 */
public class TypeDeclarationGeneratorTest extends GenerationTest {

  public void testAnonymousClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Runnable run = new Runnable() { public void run() {} }; }",
      "Example", "Example.m");
    assertTranslation(translation, "@interface Example_1 : NSObject < JavaLangRunnable >");
    assertTranslation(translation, "- (void)run;");
    // Outer reference is not required.
    assertNotInTranslation(translation, "Example *this");
    assertNotInTranslation(translation, "- (id)initWithExample:");
  }

  public void testAnonymousConcreteSubclassOfGenericAbstractType() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  interface FooInterface<T> { public void foo1(T t); public void foo2(); }"
        + "  abstract static class Foo<T> implements FooInterface<T> { public void foo2() { } }"
        + "  Foo<Integer> foo = new Foo<Integer>() {"
        + "    public void foo1(Integer i) { } }; }",
        "Test", "Test.m");
    assertTranslation(translation, "foo1WithId:(JavaLangInteger *)i");
  }

  public void testAccessorForStaticPrimitiveConstant() throws IOException {
    // Even though it's safe to access the define directly, we should add an
    // accessor to be consistent with other static variables.
    String translation = translateSourceFile(
        "class Test { static final int FOO = 1; }", "Test", "Test.h");
    assertTranslation(translation, "#define Test_FOO 1");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_CONSTANT(Test, FOO, int32_t)");
  }

  // Verify that accessor methods for static vars and constants are generated on request.
  public void testStaticFieldAccessorMethods() throws IOException {
    options.setStaticAccessorMethods(true);
    String source = "class Test { "
        + "static String ID; "
        + "private static int i; "
        + "static final Test DEFAULT = new Test(); "
        + "static boolean DEBUG; }";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "+ (NSString *)ID;");
    assertTranslation(translation, "+ (void)setID:(NSString *)value;");
    assertTranslation(translation, "+ (Test *)DEFAULT;");
    assertTranslation(translation, "+ (bool)DEBUG_;");
    assertTranslation(translation, "+ (void)setDEBUG_:(bool)value");
    assertNotInTranslation(translation, "+ (int32_t)i");
    assertNotInTranslation(translation, "+ (void)setI:(int32_t)value");
    assertNotInTranslation(translation, "+ (void)setDEFAULT:(Test *)value");
  }

  // Verify accessor methods are properly annotated for nullability when the class
  // is annotated with @NullMarked.
  public void testStaticFieldAccessorMethodWithNullMarkedClass() throws IOException {
    String source =
        "import javax.annotation.*; import org.jspecify.annotations.NullMarked;"
            + "@NullMarked public class Test {"
            + "  @Nullable public static String nullableStr;"
            + "  public static String nonNullStr;"
            + "  Test() {}"
            + "}";
    options.setClassProperties(false);
    options.setStaticAccessorMethods(true);
    options.setNullMarked(true);
    options.setNullability(true);
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "+ (NSString *_Nullable)nullableStr;");
    assertTranslation(translation, "+ (void)setNullableStr:(NSString *_Nullable)value;");
    assertTranslation(translation, "+ (NSString *)nonNullStr;");
    assertTranslation(translation, "+ (void)setNonNullStr:(NSString *)value;");
  }

  // Verify accessor methods are properly annotated for nullability when the package
  // is annotated with @NullMarked.
  public void testStaticFieldAccessorMethodWithNullMarkedPackage() throws IOException {
    addSourceFile(
        "@NullMarked package foo.bar; import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");
    String source =
        "package foo.bar; "
            + "import javax.annotation.*;"
            + "public class Test {"
            + "  @Nullable public static String nullableStr;"
            + "  public static String nonNullStr;"
            + "  Test() {}"
            + "}";
    options.setClassProperties(false);
    options.setStaticAccessorMethods(true);
    options.setNullMarked(true);
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslation(translation, "+ (NSString *_Nullable)nullableStr;");
    assertTranslation(translation, "+ (void)setNullableStr:(NSString *_Nullable)value;");
    assertTranslation(translation, "+ (NSString *)nonNullStr;");
    assertTranslation(translation, "+ (void)setNonNullStr:(NSString *)value;");
  }

  // Verify that accessor methods for static vars and constants aren't generated by default.
  public void testNoStaticFieldAccessorMethods() throws IOException {
    String source = "class Test { "
        + "static String ID; "
        + "private static int i; "
        + "static final Test DEFAULT = new Test(); }";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertNotInTranslation(translation, "+ (NSString *)ID");
    assertNotInTranslation(translation, "+ (void)setID:(NSString *)value");
    assertNotInTranslation(translation, "+ (Test *)DEFAULT");
    assertNotInTranslation(translation, "+ (int32_t)i");
    assertNotInTranslation(translation, "+ (void)setI:(int32_t)value");
    assertNotInTranslation(translation, "+ (void)setDEFAULT:(Test *)value");
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
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertNotInTranslation(translation, "+ (NSString *)ID;");
    assertNotInTranslation(translation, "+ (void)setID:(NSString *)value;");
    assertTranslation(translation, "+ (NSString *)getID;");
    assertTranslation(translation, "+ (void)setIDWithNSString:(NSString *)ID;");
  }

  // Verify that accessor methods for enum constants are generated on request.
  public void testEnumConstantAccessorMethods() throws IOException {
    options.setStaticAccessorMethods(true);
    String source = "enum Test { ONE, TWO, EOF }";  // EOF is a reserved name.
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "+ (Test *)ONE;");
    assertTranslation(translation, "+ (Test *)TWO;");
    assertTranslation(translation, "+ (Test *)EOF_;");
  }

  // Verify that accessor methods for enum constants are generated with nonnull
  // annotations.
  public void testEnumConstantAccessorMethodsWithNullability() throws IOException {
    options.setStaticAccessorMethods(true);
    options.setNullability(true);
    String source = "enum Test { ONE, TWO }";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertNotInTranslation(translation, "NS_ASSUME_NONNULL_BEGIN");
    assertTranslation(translation, "+ (Test * _Nonnull)ONE;");
    assertTranslation(translation, "+ (Test * _Nonnull)TWO;");
    assertNotInTranslation(translation, "NS_ASSUME_NONNULL_END");
  }

  // Verify that accessor methods for enum constants are generated on request with
  // proper nullability annotations.
  public void testEnumConstantAccessorMethodsAreImplicitlyNonnullWhenNullMarked()
      throws IOException {
    options.setStaticAccessorMethods(true);
    options.setNullMarked(true);
    options.setNullability(true);
    String source =
        "import javax.annotation.*;"
            + "@Nullable enum NullableTest { ONE, TWO }"
            + "enum NonnullTest { THREE, FOUR }";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslatedLines(
        translation, "NS_ASSUME_NONNULL_BEGIN", "@interface NullableTest : JavaLangEnum");
    assertTranslation(translation, "+ (NullableTest * _Nullable)ONE;");
    assertTranslation(translation, "+ (NullableTest * _Nullable)TWO;");
    assertTranslation(translation, "+ (NonnullTest *)THREE;");
    assertTranslation(translation, "+ (NonnullTest *)FOUR;");
    assertTranslation(translation, "NS_ASSUME_NONNULL_END");
  }

  // Verify that class properties for enum constants are generated on request.
  public void testEnumConstantClassProperties() throws IOException {
    options.setClassProperties(true);
    options.setNullability(true);
    options.setSwiftEnums(false);
    String source = "enum Test { ONE, TWO, EOF }"; // EOF is a reserved name.
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(
        translation, "@property (readonly, class, nonnull) Test *ONE NS_SWIFT_NAME(ONE);");
    assertTranslation(
        translation, "@property (readonly, class, nonnull) Test *TWO NS_SWIFT_NAME(TWO);");
    assertTranslation(
        translation, "@property (readonly, class, nonnull) Test *EOF_ NS_SWIFT_NAME(EOF_);");
  }

  // Verify that accessor methods for enum constants are generated by --swift-friendly flag.
  public void testSwiftFriendlyEnumConstantAccessorMethods() throws IOException {
    options.setSwiftFriendly(true);
    String source = "enum Test { ONE, TWO, EOF, TWO_WORDS }"; // EOF is a reserved name.
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(
        translation, "@property (readonly, class, nonnull) Test *ONE NS_SWIFT_NAME(one);");
    assertTranslation(
        translation, "@property (readonly, class, nonnull) Test *TWO NS_SWIFT_NAME(two);");
    assertTranslation(
        translation, "@property (readonly, class, nonnull) Test *EOF_ NS_SWIFT_NAME(eof);");
    assertTranslation(
        translation,
        "@property (readonly, class, nonnull) Test *TWO_WORDS NS_SWIFT_NAME(twoWords);");

    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "+ (Test *)ONE ");
    assertTranslation(translation, "+ (Test *)TWO ");
    assertTranslation(translation, "+ (Test *)EOF_ ");
    assertTranslation(translation, "+ (Test *)TWO_WORDS ");
  }

  // Verify that NS_SWIFT_NAME is added to emums when --swift-enum flag is enabled.
  public void testSwiftEnumSwiftName() throws IOException {
    options.setSwiftEnums(true);
    String source = "enum Test { ONE, TWO, EOF, TWO_WORDS }"; // EOF is a reserved name.
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "Test_Enum_ONE NS_SWIFT_NAME(one) = 0,");
    assertTranslation(translation, "Test_Enum_TWO NS_SWIFT_NAME(two) = 1,");
    assertTranslation(translation, "Test_Enum_EOF NS_SWIFT_NAME(eof) = 2,");
    assertTranslation(translation, "Test_Enum_TWO_WORDS NS_SWIFT_NAME(twoWords) = 3,");
  }

  // Verify that accessor methods for enum constants are not generated by default.
  public void testNoEnumConstantAccessorMethods() throws IOException {
    String source = "enum Test { ONE, TWO, EOF_ }";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertNotInTranslation(translation, "+ (TestEnum *)ONE");
    assertNotInTranslation(translation, "+ (TestEnum *)TWO");
    assertNotInTranslation(translation, "+ (TestEnum *)EOF");
  }

  public void testNoStaticFieldAccessorForPrivateInnerType() throws IOException {
    options.setStaticAccessorMethods(true);
    String translation = translateSourceFile(
        "class Test { private static class Inner1 { "
        + "public static class Inner2 { static String ID; } } }", "Test", "Test.m");
    assertNotInTranslation(translation, "+ (NSString *)ID");
    assertNotInTranslation(translation, "+ (void)setID:");
  }

  public void testStaticFieldAccessorInInterfaceType() throws IOException {
    options.setStaticAccessorMethods(true);
    String translation =
        translateSourceFile(
            "interface Test { public static final boolean FOO = true; }", "Test", "Test.h");
    // The static accessor must go in the companion class, not the @protocol.
    assertTranslatedLines(
        translation,
        "@protocol Test < JavaObject >",
        "",
        "@end",
        "",
        "@interface Test : NSObject",
        "",
        "+ (bool)FOO;");
  }

  public void testProperties() throws IOException {
    String source =
        "import com.google.j2objc.annotations.Property; "
            + "public class FooBar<T extends Throwable> {"
            + "  @Property(\"readonly, nonatomic\") private int fieldBar, fieldBaz;"
            + "  @Property(\"readwrite\") private String fieldCopy;"
            + "  @Property private boolean fieldBool;"
            + "  @Property(\"nonatomic, readonly, weak\") private int fieldReorder;"
            + "  @Property T aGenericProperty;"
            + "  @Property private final int fieldFinal = 724;"
            + "  public int getFieldBaz() { return 1; }"
            + "  public void setFieldNonAtomic(int value) { }"
            + "  public void setFieldBaz(int value, int option) { }"
            + "  public boolean isFieldBool() { return fieldBool; }"
            + "  public T getAGenericProperty() { return aGenericProperty; } "
            + "  public void setAGenericProperty(T value) { }"
            + "}";
    String translation = translateSourceFile(source, "FooBar", "FooBar.h");
    assertTranslation(translation, "@property (readonly, nonatomic) int32_t fieldBar;");

    // Should split out fieldBaz and include the declared getter.
    assertTranslation(translation,
        "@property (readonly, nonatomic, getter=getFieldBaz) int32_t fieldBaz;");

    // Set copy for strings and drop readwrite.
    assertTranslation(translation,
        "@property (copy) NSString *fieldCopy;");

    // Test boolean getter.
    assertTranslation(translation, "@property (nonatomic, getter=isFieldBool) bool fieldBool;");

    // Reorder property attributes and pass setter through.
    assertTranslation(translation,
        "@property (weak, readonly, nonatomic) int32_t fieldReorder;");

    // Test generic property.
    assertTranslation(
        translation,
        "@property (nonatomic, getter=getAGenericProperty, "
            + "setter=setAGenericPropertyWithJavaLangThrowable:, strong) "
            + "JavaLangThrowable *aGenericProperty;");

    // Test readonly property.
    assertTranslation(translation, "@property (readonly) int32_t fieldFinal;");
  }

  public void testSynchronizedPropertyGetter() throws IOException {
    String source = "import com.google.j2objc.annotations.Property; "
        + "public class FooBar {"
        + "  @Property(\"getter=getFieldBar\") private int fieldBar;"
        + "  public synchronized int getFieldBar() { return fieldBar; }"
        + "}";
    String translation = translateSourceFile(source, "FooBar", "FooBar.h");
    assertTranslation(translation, "@property (getter=getFieldBar) int32_t fieldBar;");
  }

  public void testBadPropertyAttribute() throws IOException {
    String source = "import com.google.j2objc.annotations.Property; "
        + "public class FooBar {"
        + "  @Property(\"cause_exception\") private int fieldBar;"
        + "}";
    translateSourceFile(source, "FooBar", "FooBar.h");
    assertError("FooBar.java:1: Invalid @Property attribute: cause_exception");
  }

  public void testPropertySetterSelector() throws IOException {
    String source = "import com.google.j2objc.annotations.Property; "
        + "public class FooBar {"
        + "  @Property(\"setter=setFieldBar\") private int fieldBar;"
        + "  public void setFieldBar(int val) { fieldBar = val; }"
        + "}";
    String translation = translateSourceFile(source, "FooBar", "FooBar.h");
    assertTranslation(translation, "setter=setFieldBarWithInt:");
  }

  public void testBadPropertySetterSelector() throws IOException {
    String source = "import com.google.j2objc.annotations.Property; "
        + "public class FooBar {"
        + "  @Property(\"setter=needs_colon\") private int fieldBar;"
        + "}";
    translateSourceFile(source, "FooBar", "FooBar.h");
    assertError("FooBar.java:1: Non-existent setter specified: needs_colon");
  }

  public void testNonexistentPropertySetter() throws IOException {
    String source = "import com.google.j2objc.annotations.Property; "
        + "public class FooBar {"
        + "  @Property(\"setter=nonexistent:\") private int fieldBar;"
        + "}";
    translateSourceFile(source, "FooBar", "FooBar.h");
    assertError("FooBar.java:1: Non-existent setter specified: nonexistent:");
  }

  public void testPropertyWeakAssignment() throws IOException {
    String source = "import com.google.j2objc.annotations.Property; "
        + "import com.google.j2objc.annotations.Weak; "
        + "public class Foo {"
        + "  @Property(\"weak\") Foo barA;"
        + "  @Property(\"readonly\") @Weak Foo barB;"
        + "  @Property(\"weak, readonly\") @Weak Foo barC;"
        + "}";
    String translation = translateSourceFile(source, "Foo", "Foo.h");
    // Add __weak instance variable
    assertTranslation(translation, "WEAK_ Foo *barA_;");
    assertTranslation(translation, "@property (weak) Foo *barA;");
    assertNotInTranslation(translation, "J2OBJC_FIELD_SETTER(Foo, barA_, Foo *)");
    // Add weak property attribute
    assertTranslation(translation, "WEAK_ Foo *barB_;");
    assertTranslation(translation, "@property (weak, readonly) Foo *barB;");
    assertNotInTranslation(translation, "J2OBJC_FIELD_SETTER(Foo, barB_, Foo *)");
    // Works with both
    assertTranslation(translation, "WEAK_ Foo *barC_;");
    assertTranslation(translation, "@property (weak, readonly) Foo *barC;");
    assertNotInTranslation(translation, "J2OBJC_FIELD_SETTER(Foo, barC_, Foo *)");
  }

  public void testWeakPropertyWithStrongAttribute() throws IOException {
    String source = "import com.google.j2objc.annotations.Property; "
        + "import com.google.j2objc.annotations.Weak; "
        + "public class Foo {"
        + "  @Property(\"strong\") @Weak Foo barA;"
        + "}";
    translateSourceFile(source, "Foo", "Foo.h");
    assertError("Foo.java:1: Weak field annotation conflicts with strong Property attribute");
  }

  public void testStrongProperties() throws IOException {
    String source =
        "import com.google.j2objc.annotations.Property; "
            + "public class Test {  "
            + "@Property(\"strong\") Thread explicitlyStrong; "
            + "@Property Thread implicitlyStrong; }";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslatedLines(
        translation,
        "@property (strong) JavaLangThread *explicitlyStrong;",
        "@property (strong) JavaLangThread *implicitlyStrong;");

    options.setMemoryManagementOption(MemoryManagementOption.ARC);
    translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslatedLines(
        translation,
        "@property JavaLangThread *explicitlyStrong;",
        "@property JavaLangThread *implicitlyStrong;");
  }

  public void testClassProperties() throws IOException {
    options.setStaticAccessorMethods(true);
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Property; "
        + "public class Test {  "
        + "@Property static int test; "
        + "@Property(\"nonatomic\") static double d; "
        + "@Property static final boolean flag = true; }", "Test", "Test.h");
    assertTranslatedLines(
        translation,
        "@property (class) int32_t test;",
        "@property (nonatomic, class) double d;",
        "@property (readonly, class) bool flag;");

    // Verify class attributes aren't assigned to instance fields.
    translateSourceFile(
        "import com.google.j2objc.annotations.Property; "
        + "public class Test {  "
        + "@Property(\"class\") int test; }", "Test", "Test.h");
    assertError("Test.java:-1: Only static fields can be translated to class properties");

    // Verify static accessor generation must be enabled for class properties.
    ErrorUtil.reset();
    options.setStaticAccessorMethods(false);
    translateSourceFile(
        "import com.google.j2objc.annotations.Property; "
        + "public class Test {  "
        + "@Property static int test; }", "Test", "Test.h");
    assertError(
        "Test.java:-1: Class properties require any of these flags: --swift-friendly,"
            + " --class-properties or --static-accessor-methods");

    // Verify class properties are not supported for private static fields.
    ErrorUtil.reset();
    options.setStaticAccessorMethods(true);
    translateSourceFile(
        "import com.google.j2objc.annotations.Property; "
            + "public class Test {  "
            + "@Property private static int test; }", "Test", "Test.h");
    assertError("Test.java:-1: Properties are not supported for private static fields.");
  }

  public void testClassPropertiesFlag() throws IOException {
    options.setClassProperties(true);
    String translation =
        translateSourceFile(
            "public class Test {  "
                + "static int test; "
                + "static double d; "
                + "static final boolean flag = true; "
                + "static String TRUE; " // reserved word.
                + "private static String s; " // private.
                // verify that this instance getter does not prevent generating the class getter.
                + "int getTest() { return test; } }"
            ,
            "Test",
            "Test.h");
    assertTranslatedLines(
        translation,
        "@property (class) int32_t test NS_SWIFT_NAME(test);",
        "@property (class) double d NS_SWIFT_NAME(d);",
        "@property (readonly, class) bool flag NS_SWIFT_NAME(flag);",
        "@property (copy, class) NSString *TRUE_ NS_SWIFT_NAME(TRUE_);");
    assertTranslation(translation, "- (int32_t)getTest;");
    assertNotInTranslation(translation, "+ (int32_t)test;");
    assertNotInTranslation(translation, "@property (copy, class) NSString *s NS_SWIFT_NAME(s);");

    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "+ (int32_t)test {");
  }

  public void testPropertiesInInterfaceType() throws IOException {
    options.setClassProperties(true);
    String translation =
        translateSourceFile(
            "interface Test { public static final boolean FOO = true; }", "Test", "Test.h");
    // The property must go in the companion class, not the @protocol.
    assertTranslatedLines(
        translation,
        "@protocol Test < JavaObject >",
        "",
        "@end",
        "",
        "@interface Test : NSObject",
        "@property (readonly, class) bool FOO NS_SWIFT_NAME(FOO);");
  }

  public void testNullabilityAttributes() throws IOException {
    String source = "import javax.annotation.*; "
        + "@ParametersAreNonnullByDefault public class Test {"
        + "  @Nullable String test(@Nonnull String msg, Object var) { "
        + "    return msg.isEmpty() ? null : msg; }"
        + "  String test2() { "
        + "    return \"\"; }"
        + "  @Nonnull String test3() { "
        + "    return \"\"; }"
        + "}";
    options.setNullability(true);
    String translation = translateSourceFile(source, "Test", "Test.h");

    // Verify return type and parameters are all annotated.
    assertTranslatedLines(
        translation,
        "- (NSString * _Nullable)testWithNSString:(NSString * _Nonnull)msg",
        // var is also nonnull because of the default annotation on the class.
        "withId:(id _Nonnull)var;");

    // Verify return type isn't annotated, as only parameters should be by default.
    assertTranslatedLines(translation, "- (NSString *)test2;");

    // Verify return type is annotated.
    assertTranslation(translation, "- (NSString * _Nonnull)test3;");
  }

  // Verify ParametersAreNonnullByDefault sets unspecified parameter as non-null.
  public void testDefaultNonnullParameters() throws IOException {
    String source = "package foo.bar; import javax.annotation.*; "
        + "@ParametersAreNonnullByDefault public class Test {"
        + "  @Nullable String test(@Nullable String msg, Object var, int count) { "
        + "    return msg.isEmpty() ? null : msg; }"
        + "}";
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    // var is also nonnull because of the default annotation on the class.
    assertTranslatedLines(
        translation,
        // Verify parameter isn't affected by default.
        "- (NSString * _Nullable)testWithNSString:(NSString * _Nullable)msg",
        // Verify default nonnull is specified.
        "withId:(id _Nonnull)var",
        // Default should not apply to primitive type.
        "withInt:(int32_t)count;");
  }

  // Verify a ParametersAreNonnullByDefault package annotation sets unspecified
  // parameter as non-null.
  public void testDefaultNonnullParametersPackage() throws IOException {
    addSourceFile("@ParametersAreNonnullByDefault package foo.bar; "
        + "import javax.annotation.ParametersAreNonnullByDefault;", "foo/bar/package-info.java");
    String source = "package foo.bar; import javax.annotation.*; "
        + "public class Test {"
        + "  @Nullable String test(@Nullable String msg, Object var, int count) { "
        + "    return msg.isEmpty() ? null : msg; }"
        + "}";
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    // var is also nonnull because of the default annotation on the class.
    assertTranslatedLines(
        translation,
        // Verify parameter isn't affected by default.
        "- (NSString * _Nullable)testWithNSString:(NSString * _Nullable)msg",
        // Verify default nonnull is specified.
        "withId:(id _Nonnull)var",
        // Default should not apply to primitive type.
        "withInt:(int32_t)count;");
  }

  public void testNullabilityPragmas() throws IOException {
    String source = "package foo.bar; import javax.annotation.*; "
        + "public class Test {"
        + "  String test(@Nullable String msg, Object var, int count) { "
        + "    return msg.isEmpty() ? null : msg; }"
        + "}";
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(
        translation,
        "#if __has_feature(nullability)",
        "#pragma clang diagnostic push",
        "#pragma GCC diagnostic ignored \"-Wnullability\"",
        "#pragma GCC diagnostic ignored \"-Wnullability-completeness\"",
        "#endif");
    assertTranslatedLines(translation,
        "#if __has_feature(nullability)",
        "#pragma clang diagnostic pop",
        "#endif");
  }

  public void testPrivateNullabilityPragmas() throws IOException {
    String source = "package foo.bar; import javax.annotation.*; "
        + "public class Test {"
        + " private static class Inner {"
        + "  String test(@Nullable String msg, Object var, int count) { "
        + "    return msg.isEmpty() ? null : msg;"
        + "  }"
        + " }"
        + "}";
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(
        translation,
        "#if __has_feature(nullability)",
        "#pragma clang diagnostic push",
        "#pragma GCC diagnostic ignored \"-Wnullability\"",
        "#pragma GCC diagnostic ignored \"-Wnullability-completeness\"",
        "#endif");
    assertTranslatedLines(translation,
        "#if __has_feature(nullability)",
        "#pragma clang diagnostic pop",
        "#endif");
  }

  // Verify that enums always have nullability completeness suppressed.
  public void testEnumNullabilityPragmas() throws IOException {
    options.setNullability(true);
    String translation = translateSourceFile("enum Test { A, B, C; }", "Test", "Test.h");
    assertTranslatedLines(
        translation,
        "#if __has_feature(nullability)",
        "#pragma clang diagnostic push",
        "#pragma GCC diagnostic ignored \"-Wnullability\"",
        "#pragma GCC diagnostic ignored \"-Wnullability-completeness\"",
        "#endif");
    assertTranslatedLines(translation,
        "#if __has_feature(nullability)",
        "#pragma clang diagnostic pop",
        "#endif");
  }

  public void testPropertyNullability() throws IOException {
    String source =
        "import javax.annotation.*;"
            + "import com.google.j2objc.annotations.Property;"
            + "@ParametersAreNonnullByDefault public class Test {"
            + "  @Nullable @Property String test;"
            + "  @CheckForNull @Property String test1;"
            + "  @Property String test2;"
            + "  @Property @Nonnull String test3;"
            + "  @Property(\"nonatomic\") String test4;"
            + "  @Property(\"null_resettable\") String test5;"
            + "  @Property(\"null_unspecified\") String test6;"
            + "  @Property int test7;"
            + "  @Property (\"readonly, nonatomic\") double test8;"
            + "}";
    options.setNullability(true);
    String translation = translateSourceFile(source, "Test", "Test.h");

    assertTranslatedLines(
        translation,
        "@property (copy, nullable) NSString *test;",
        "@property (copy, nullable) NSString *test1;",
        "@property (copy) NSString *test2;",
        "@property (copy, nonnull) NSString *test3;",
        "@property (copy, nonatomic) NSString *test4;");

    // Verify explicit nullability parameters override default.
    assertTranslatedLines(translation,
        "@property (copy, null_resettable) NSString *test5;",
        "@property (copy, null_unspecified) NSString *test6;");

    // Verify primitive properties don't have nullability parameters.
    assertTranslatedLines(translation,
        "@property int32_t test7;",
        "@property (readonly, nonatomic) double test8;");
  }

  // Verify that constructors are always nonnull (issue #960).
  public void testNonnullConstructors() throws IOException {
    options.setNullability(true);
    String translation = translateSourceFile(
        "class Test { "
        + "  int i; "
        + "  public Test() { this(0); } "
        + "  private Test(int i) { this.i = i; }}", "Test", "Test.h");
    assertTranslation(translation, "- (instancetype _Nonnull)init;");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (instancetype)initWithInt:(int32_t)i;");
  }

  // Verify type nullability annotations.
  public void testNullableTypeAnnotation() throws IOException {
    String source =
        "package foo.bar;"
            + "@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE_USE})"
            + "@interface NonNull {}"
            + "@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE_USE})"
            + "@interface Nullable {}"
            + "public class Test {"
            + "  @Nullable String test(@NonNull String msg) { return null; }"
            + "  interface Test2<V> {"
            + "    void test(@Nullable V newValue);"
            + "  }"
            + "}";
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(
        translation, "- (NSString * _Nullable)testWithNSString:(NSString * _Nonnull)msg");
  }

  /*
   * Temporarily, Guava is annotating usages of type variable's like `E next()` with a
   * `@ParametricNullness` annotation. This annotation means "the value may be null for an
   * `Iterator<@Nullable Foo>` but not for an `Iterator<@Nonnull Foo>`." For our purposes, that
   * means "It might be null."
   *
   * TODO(b/188212777): Understand Guava's new nullness annotations on a deeper level so that we can
   * determine whether a usage of a type variable might or might not be null based on whether it is
   * declared as `class Iterator<E extends @Nullable Object> { ... }` or `class Iterator<E> { ...
   * }`. Then, Guava can remove @ParametricNullness (and also replace its usages of
   * @ParametersAreNonnullByDefault with usages of a new annotation).
   */
  public void testGuavaTransitionalParametricNullnessAnnotation() throws IOException {
    String source =
        "package foo.bar; import javax.annotation.*; "
            + "@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE_USE})"
            + "@interface Nullable {}"
            + "@interface ParametricNullness {}"
            + "@ParametersAreNonnullByDefault public class Test<T extends @Nullable Object> {"
            + "  @ParametricNullness T passthrough(@ParametricNullness T t) { "
            + "    return t; }"
            + "}";
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(translation, "- (id _Nullable)passthroughWithId:(id _Nullable)t;");
  }

  public void testNullMarked() throws IOException {
    addSourceFile(
        "@NullMarked package foo.bar; import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");
    String source =
        "package foo.bar; import javax.annotation.*; "
            + "public class Test {  "
            + "  public @Nullable String identifier; "
            + "  private final @Nullable String token; "
            + "  Test(@Nullable String token) { this.token = token; }"
            + "  public @Nullable String getToken() { return token; } "
            + "  @Nullable String testNullableInstanceMethod(@Nullable String msg,"
            + "    Object var, int count) {"
            + "      return msg.isEmpty() ? null : msg;"
            + "  } "
            + "  public static @Nullable String testNullableStaticMethod(@Nullable String firstArg,"
            + "    Object var, int count) {"
            + "      return null;"
            + "    }"
            + "}";
    options.setNullMarked(true);
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(
        translation, "NS_ASSUME_NONNULL_BEGIN", "@interface FooBarTest : NSObject {");
    assertTranslatedLines(translation, "@public", "NSString *_Nullable identifier_;");
    assertTranslation(translation, "- (NSString * _Nullable)getToken;");
    assertTranslation(translation, "- (instancetype)initWithNSString:(NSString * _Nullable)token;");
    assertTranslatedLines(
        translation,
        "- (NSString * _Nullable)testNullableInstanceMethodWithNSString:(NSString *"
            + " _Nullable)msg",
        "withId:(id)var",
        "withInt:(int32_t)count;");
    assertTranslatedLines(
        translation,
        "+ (NSString * _Nullable)testNullableStaticMethodWithNSString:(NSString *"
            + " _Nullable)firstArg",
        "withId:(id)var",
        "withInt:(int32_t)count;");
    assertTranslatedLines(
        translation, "J2OBJC_TYPE_LITERAL_HEADER(FooBarTest)", "", "NS_ASSUME_NONNULL_END");
  }

  public void testNullMarkedTypeAnnotation() throws IOException {
    String source =
        "import javax.annotation.*;"
            + "import org.jspecify.annotations.NullMarked;"
            + "@NullMarked public class Test {"
            + "  private @Nullable String nullableStr;"
            + "  private String nonNullStr;"
            + "  public @Nullable String test(String a,"
            + "    @Nullable String b) {"
            + "      return null;"
            + "  }"
            + "  public @Nullable String getNullableStr() { return nullableStr; }"
            + "  public String getNonNullStr() { return nonNullStr; }"
            + "}";
    options.setNullMarked(true);
    options.setNullability(true);
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslatedLines(translation, "NS_ASSUME_NONNULL_BEGIN", "@interface Test : NSObject");
    assertTranslation(translation, "- (instancetype)init;");
    assertTranslatedLines(
        translation,
        "- (NSString * _Nullable)testWithNSString:(NSString *)a",
        "withNSString:(NSString * _Nullable)b;");
    assertTranslation(translation, "- (NSString *)getNonNullStr;");
    assertTranslation(translation, "- (NSString * _Nullable)getNullableStr;");
    assertTranslation(translation, "NS_ASSUME_NONNULL_END");
  }

  public void testNullMarkedPackageAnnotation() throws IOException {
    addSourceFile(
        "@NullMarked package foo.bar;" + "import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");
    String source =
        "package foo.bar;"
            + "import javax.annotation.*;"
            + "public class Test {"
            + "  private @Nullable String nullableStr;"
            + "  private String nonNullStr;"
            + "  public @Nullable String test(String a,"
            + "    @Nullable String b) {"
            + "      return null;"
            + "  }"
            + "  public @Nullable String getNullableStr() { return nullableStr; }"
            + "  public String getNonNullStr() { return nonNullStr; }"
            + "}";
    options.setNullMarked(true);
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(
        translation, "NS_ASSUME_NONNULL_BEGIN", "@interface FooBarTest : NSObject");
    assertTranslation(translation, "- (instancetype)init;");
    assertTranslatedLines(
        translation,
        "- (NSString * _Nullable)testWithNSString:(NSString *)a",
        "withNSString:(NSString * _Nullable)b;");
    assertTranslation(translation, "- (NSString *)getNonNullStr;");
    assertTranslation(translation, "- (NSString * _Nullable)getNullableStr;");
    assertTranslation(translation, "NS_ASSUME_NONNULL_END");
  }

  public void testNullMarkedWithoutGenerics() throws IOException {
    addSourceFile(
        "@NullMarked package foo.bar;" + "import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");
    String source =
        "package foo.bar;"
            + "import javax.annotation.*;"
            + "import org.jspecify.annotations.Nullable;"
            + "public class Test <T extends @Nullable String> {"
            + "  public T getNullableStr() { return null; }"
            + "  public void setNullableStr(T thing) { return; }"
            + "}";
    options.setNullMarked(true);
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(
        translation, "NS_ASSUME_NONNULL_BEGIN", "@interface FooBarTest : NSObject");
    assertTranslation(translation, "- (NSString * _Nullable)getNullableStr;");
    assertTranslation(
        translation, "- (void)setNullableStrWithNSString:(NSString * _Nullable)thing;");
    assertTranslation(translation, "NS_ASSUME_NONNULL_END");
  }

  public void testNullMarkedWithGenerics() throws IOException {
    addSourceFile(
        "@NullMarked package foo.bar;" + "import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");
    String source =
        "package foo.bar;"
            + "import javax.annotation.*;"
            + "import org.jspecify.annotations.Nullable;"
            + "public class Test <@Nullable T> {"
            + "  public T getNullableStr() { return null; }"
            + "  public void setNullableStr(T thing) { return; }"
            + "}";
    options.setNullMarked(true);
    options.setNullability(true);
    options.setAsObjCGenericDecl(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(
        translation, "NS_ASSUME_NONNULL_BEGIN", "@interface FooBarTest<T> : NSObject");
    assertTranslation(translation, "- (T _Nullable)getNullableStr;");
    assertTranslation(translation, "- (void)setNullableStrWithId:(T _Nullable)thing;");
    assertTranslation(translation, "NS_ASSUME_NONNULL_END");
  }

  public void testFieldWithIntersectionType() throws IOException {
    String translation = translateSourceFile(
        "class Test <T extends Comparable & Runnable> { T foo; }", "Test", "Test.h");
    // Test that J2OBJC_ARG is used to wrap the type containing a comma.
    assertTranslation(translation,
        "J2OBJC_FIELD_SETTER(Test, foo_, J2OBJC_ARG(id<JavaLangComparable, JavaLangRunnable>))");
  }

  public void testSortMethods() throws IOException {
    String source = "class A {"
        + "void zebra() {}"
        + "void gnu(String s, int i, Runnable r) {}"
        + "A(int i) {}"
        + "void gnu() {}"
        + "void gnu(int i, Runnable r) {}"
        + "void yak() {}"
        + "A(String s) {}"
        + "A() {}"
        + "A(int i, Runnable r) {}"
        + "void gnu(String s, int i) {}}";
    CompilationUnit unit = translateType("A", source);
    final ArrayList<MethodDeclaration> methods = new ArrayList<>();
    unit.accept(new TreeVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (!ElementUtil.isSynthetic(node.getExecutableElement())) {
          methods.add(node);
        }
      }
    });
    Collections.sort(methods, TypeDeclarationGenerator.METHOD_DECL_ORDER);
    assertTrue(methods.get(0).toString().startsWith("<init>()"));
    assertTrue(methods.get(1).toString().startsWith("<init>(int i)"));
    assertTrue(methods.get(2).toString().startsWith("<init>(int i,java.lang.Runnable r)"));
    assertTrue(methods.get(3).toString().startsWith("<init>(java.lang.String s)"));
    assertTrue(methods.get(4).toString().startsWith("void gnu()"));
    assertTrue(methods.get(5).toString().startsWith("void gnu(int i,java.lang.Runnable r)"));
    assertTrue(methods.get(6).toString().startsWith("void gnu(java.lang.String s,int i)"));
    assertTrue(methods.get(7).toString().startsWith(
        "void gnu(java.lang.String s,int i,java.lang.Runnable r)"));
    assertTrue(methods.get(8).toString().startsWith("void yak()"));
    assertTrue(methods.get(9).toString().startsWith("void zebra()"));
  }

  // Verify that an empty statement following a type declaration is ignored.
  // The JDT parser discards them, while javac includes them in the compilation unit.
  public void testEmptyStatementsIgnored() throws IOException {
    String source = "public interface A { void bar(); };";
    CompilationUnit unit = translateType("A", source);
    assertEquals(1, unit.getTypes().size());
  }

  // Verify that a boolean constant initialized with a constant expression like
  // "true || false" does not cause class initialization code to be generated
  // for it.
  public void testBooleanExpressionConstants() throws IOException {
    String translation = translateSourceFile("class Test {"
        + "  static final boolean FOO = true && false;"
        + "  static final boolean BAR = true || false;"
        + "}", "Test", "Test.h");
    // Verify boolean expressions are simplified.
    assertTranslation(translation, "#define Test_FOO false");
    assertTranslation(translation, "#define Test_BAR true");

    // This class should not have an initialize method or reinitialize the constants.
    assertTranslation(translation, "J2OBJC_EMPTY_STATIC_INIT(Test)");
    translation = getTranslatedFile("Test.m");
    assertNotInTranslation(translation, "+ (void)initialize {");
    assertNotInTranslation(translation, "Test_FOO = false;");
    assertNotInTranslation(translation, "Test_BAR = true;");
  }

  // Verify that generated source mappings are correct for methods that take no arguments.
  public void testMethodSignatureMappingWithoutParameters() throws IOException {
    String source = "class A { void zebra() {} }";
    CompilationUnit compilationUnit = translateType("A", source);
    GeneratedType generatedType =
        GeneratedType.fromTypeDeclaration(compilationUnit.getTypes().get(0));
    GeneratedSourceMappings mappings = generatedType.getGeneratedSourceMappings();

    boolean foundZebra = false;
    for (GeneratedSourceMappings.Mapping mapping : mappings.getMappings()) {
      if (mapping.getIdentifier().equals("zebra")) {
        foundZebra = true;
        assertEquals("zebra", source.substring(mapping.getSourceBegin(), mapping.getSourceEnd()));
        assertEquals(
            "zebra",
            generatedType
                .getPublicDeclarationCode()
                .substring(mapping.getTargetBegin(), mapping.getTargetEnd()));
      }
    }

    if (!foundZebra) {
      fail("No mapping found for zebra() method");
    }
  }

  // Verify that generated source mappings are correct for methods that take arguments.
  public void testMethodSignatureMappingWithParameters() throws IOException {
    String source = "class A { void zebra(int foo, int bar) {} }";
    CompilationUnit compilationUnit = translateType("A", source);
    GeneratedType generatedType =
        GeneratedType.fromTypeDeclaration(compilationUnit.getTypes().get(0));
    GeneratedSourceMappings mappings = generatedType.getGeneratedSourceMappings();

    boolean foundZebra = false;
    for (GeneratedSourceMappings.Mapping mapping : mappings.getMappings()) {
      if (mapping.getIdentifier().equals("zebra")) {
        foundZebra = true;
        assertEquals("zebra", source.substring(mapping.getSourceBegin(), mapping.getSourceEnd()));
        assertEquals(
            "zebraWithInt",
            generatedType
                .getPublicDeclarationCode()
                .substring(mapping.getTargetBegin(), mapping.getTargetEnd()));
      }
    }

    if (!foundZebra) {
      fail("No mapping found for zebra(int, int) method");
    }
  }

  // Verify that generated source mappings are correct for constructors.
  public void testConstructorMapping() throws IOException {
    String source = "class MyClass { public MyClass() {} }";
    CompilationUnit compilationUnit = translateType("MyClass", source);
    GeneratedType generatedType =
        GeneratedType.fromTypeDeclaration(compilationUnit.getTypes().get(0));
    GeneratedSourceMappings mappings = generatedType.getGeneratedSourceMappings();

    boolean foundType = false;
    for (GeneratedSourceMappings.Mapping mapping : mappings.getMappings()) {
      if (mapping.getIdentifier().equals("<init>")) {
        foundType = true;
        assertEquals("MyClass", source.substring(mapping.getSourceBegin(), mapping.getSourceEnd()));
        assertEquals(
            "init",
            generatedType
                .getPublicDeclarationCode()
                .substring(mapping.getTargetBegin(), mapping.getTargetEnd()));
      }
    }

    if (!foundType) {
      fail("No mapping found for MyClass() constructor");
    }
  }

  // https://github.com/google/j2objc/issues/1664
  public void testStaticInitializeField() throws IOException {
    String translation = translateSourceFile("final class State\n"
        + "{\n"
        + "    public static final int initialize=0;\n"
        + "    public static final int ready=1;\n"
        + "    public static final int anotherState=2;\n"
        + "}\n", "State", "State.h");
    // Should have trailing underscore, to avoid class with class initialize function name.
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_CONSTANT(State, initialize_, int32_t)");
    assertTranslation(translation, "inline int32_t State_get_initialize_(void);");
    assertTranslation(translation, "State_initialize_");
  }

  public void testAddTextSegmentAttribute() throws IOException {
    options.setAddTextSegmentAttribute(true);
    String source =
        "class Test { \n"
            + "  void methodTest() { \n"
            + "    System.out.println(\"test\"); \n"
            + "  } \n"
            + "  native void nativeTest() /*-[ \n"
            + "     printf(\"hello\\n\");"
            + "  ]-*/;"
            + "  static enum TestEnum { FOO, BAR }"
            + "  static @interface TestAnnotation {}"
            + "}";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslatedLines(translation, "- (void)methodTest J2OBJC_TEXT_SEGMENT;");
    assertTranslatedLines(translation, "- (void)nativeTest J2OBJC_TEXT_SEGMENT;");
    assertTranslatedLines(translation, "- (instancetype)init J2OBJC_TEXT_SEGMENT;");

    // Automatically generated enum methods.
    assertTranslatedLines(
        translation,
        "+ (Test_TestEnum *)valueOfWithNSString:(NSString *)name J2OBJC_TEXT_SEGMENT;");
    assertTranslatedLines(translation, "+ (IOSObjectArray *)values J2OBJC_TEXT_SEGMENT;");
    assertTranslatedLines(translation, "- (Test_TestEnum_Enum)toNSEnum J2OBJC_TEXT_SEGMENT;");

    // Automatically generated annotation methods.
    assertTranslatedLines(translation, "- (bool)isEqual:(id)obj J2OBJC_TEXT_SEGMENT;");
    assertTranslatedLines(translation, "- (NSUInteger)hash J2OBJC_TEXT_SEGMENT;");
  }
}
