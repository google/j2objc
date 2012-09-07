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
import java.io.PrintStream;
import java.io.StringWriter;

/**
 * Tests for {@link ObjectiveCImplementationGenerator}.
 *
 * @author Tom Ball
 */
public class ObjectiveCImplementationGeneratorTest extends GenerationTest {

  public void testOuterVariableAccess() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int foo; class Inner { int test() { return foo; }}}",
        "Example", "Example.m");
    assertTranslation(translation, "return this$0_.foo;");
  }

  public void testTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example {}", "Example", "Example.m");
    assertTranslation(translation, "#import \"Example.h\"");
  }

  public void testPackageTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example {}", "Example", "unit/test/Example.m");
    assertTranslation(translation, "#import \"unit/test/Example.h\"");
  }

  public void testPackageTypeNameTranslationWithInnerClass() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { class Inner {}}",
        "Example", "unit/test/Example.m");
    assertTranslation(translation, "#import \"unit/test/Example.h\"");
    assertFalse(translation.contains("#import \"unit/test/Example_Inner.h\""));
  }

  public void testSameClassMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { void foo() {} void test() { foo(); }}",
        "Example", "unit/test/Example.m");
    assertTranslation(translation, "[self foo];");
  }

  public void testSameClassStaticMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { static void foo() {} void test() { foo(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "[Example foo];");
  }

  public void testSameClassStaticMethodPackageInvocation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { static void foo() {} void test() { foo(); }}",
        "Example", "unit/test/Example.m");
    assertTranslation(translation, "[UnitTestExample foo];");
  }

  public void testConstStaticIntTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static final int FOO=1; int test() { return FOO; }}",
        "Example", "Example.h");
    assertTranslation(translation, "#define Example_FOO 1");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation, "+ (int)FOO {");
    assertTranslation(translation, "return Example_FOO;");
    assertFalse(translation.contains("initialize"));
    assertFalse(translation.contains("setFOO"));
  }

  public void testConstVariableInOtherClassTranslation() throws IOException {
    String translation = translateSourceFile("public class Example { "
        + "int test() { return Bool.FOO; } } class Bool { public static final int FOO=1; }",
        "Example", "Example.m");
    assertTranslation(translation, "return Bool_FOO;");
  }

  public void testStaticVariableInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today; }",
        "Example", "Example.m");
    assertTranslation(translation, "static JavaUtilDate * Example_today_;");
    assertTranslation(translation, "+ (JavaUtilDate *)today {");
    assertTranslation(translation, "return Example_today_;");
    assertTranslation(translation, "+ (void)setTodayWithJavaUtilDate:(JavaUtilDate *)today {");
    assertTranslation(translation, "Example_today_ = [today retain];");
    assertFalse(translation.contains("initialize"));
  }

  public void testStaticVariableWithInitInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today = new java.util.Date();}",
        "Example", "Example.m");
    assertTranslation(translation, "static JavaUtilDate * Example_today_;");
    assertTranslation(translation, "+ (void)initialize {");
    assertTranslation(translation,
        "Example_today_ = [[JavaUtilDate alloc] init];");
    assertTranslation(translation, "+ (JavaUtilDate *)today {");
    assertTranslation(translation, "return Example_today_;");
    assertTranslation(translation, "Example_today_ = [today retain];");
  }

  public void testStaticVariableWithNonInitInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { " +
        "  public static java.util.logging.Logger logger =" +
        "      java.util.logging.Logger.getLogger(\"Test\");}",
        "Example", "Example.m");
    assertTranslation(translation,
        "Example_logger_ = [[JavaUtilLoggingLogger getLoggerWithNSString:@\"Test\"] retain];");
  }

  public void testStaticVariableInGenericInnerClass() throws IOException {
    String translation = translateSourceFile(
        "public class Example { " +
        "  static class Inner<T> { " +
        "    static int foo = 1; " +
        "    final int myFoo = foo++; }}",
        "Example", "Example.m");
    assertTranslation(translation, "static int Example_Inner_foo_");
    assertTranslation(translation, "myFoo_ = Example_Inner_foo_++");
  }

  public void testStaticVariableWithGenericTypeCast() throws IOException {
    String translation = translateSourceFile(
        "public class Example<B> { " +
        "  public <B> Example<B> foo() { return (Example<B>) FOO; } " +
        "  public static final Example<?> FOO = new Example(); }",
        "Example", "Example.m");
    assertTranslation(translation, "return (Example *) Example_FOO_");
    assertTranslation(translation, "return Example_FOO_");
  }

  public void testStaticVariableInOtherVariable() throws IOException {
    String translation = translateSourceFile("public class Example { "
        + "void test() { Bar.FOO=2; } } class Bar { public static int FOO=1; }",
       "Example", "Example.m");
    assertTranslation(translation, "static int Bar_FOO_;");
    assertTranslation(translation, "+ (int)FOO {");
    assertTranslation(translation, "return Bar_FOO_;");
    assertTranslation(translation, "+ (void)initialize {");
    assertTranslation(translation, "Bar_FOO_ = 1;");
    assertTranslation(translation, "+ (void)setFOOWithInt:(int)FOO {");
    assertTranslation(translation, "Bar_FOO_ = FOO;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation, "[Bar setFOOWithInt:2];");
  }

  public void testNSObjectMessageRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int load() { return 1; } int test() { return load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "- (int)load__ {");
    assertTranslation(translation, "return [self load__];");
  }

  public void testNSObjectMessageQualifiedNameRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int load() { return 1; } int test() { return this.load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "return [self load__];");
  }

  public void testNSObjectMessageSuperRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int load() { return 1; }} "
        + "class SubClass extends Example { int load() { return super.load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "return (int) [super load__];");
  }

  public void testNSObjectMessageStaticRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { static int load() { return 1; }} "
        + "class Other { int test() { return Example.load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "return [Example load__];");
  }

  public void testToStringRenaming() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public String toString() { return super.toString(); } }",
      "Example", "Example.m");
    assertTranslation(translation, "- (NSString *)description {");
  }

  public void testInnerClassAccessToOuterMethods() throws IOException {
    String translation = translateSourceFile(
        "public class Example {" +
        "public int size() { return 0; } public void add(int n) {} " +
        "class Inner {} " +
        "class Innermost { void test() { Example.this.add(size()); }}}",
        "Example", "Example.m");
    assertTranslation(translation, "[this$0_ addWithInt:[this$0_ size]];");
  }

  public void testEnum() throws IOException {
    String translation = translateSourceFile(
      "public enum Color { RED, WHITE, BLUE }",
      "Color", "Color.m");
    assertTranslation(translation, "ColorEnum *ColorEnum_RED;");
    assertTranslation(translation, "@implementation ColorEnum");
    assertTranslation(translation, "return ColorEnum_RED;");
    assertTranslation(translation,
        "ColorEnum_RED = [[ColorEnum alloc] initWithNSString:@\"Color_RED\" withInt:0];");
    assertTranslation(translation,
        "ColorEnum_values = [[IOSObjectArray alloc] initWithObjects:(id[]){ " +
        "ColorEnum_RED, ColorEnum_WHITE, ColorEnum_BLUE, nil } " +
        "count:3 type:[IOSClass classWithClass:[ColorEnum class]]];");
    assertTranslation(translation, "for (int i = 0; i < [ColorEnum_values count]; i++) {");
    assertTranslation(translation, "ColorEnum *e = [ColorEnum_values objectAtIndex:i];");
  }

  public void testEnumWithParameters() throws IOException {
    String translation = translateSourceFile(
      "public enum Color { RED(0xff0000), WHITE(0xffffff), BLUE(0x0000ff); " +
      "private int rgb; private Color(int rgb) { this.rgb = rgb; } " +
      "public int getRgb() { return rgb; }}",
      "Color", "Color.m");
    assertTranslation(translation, "ColorEnum *ColorEnum_RED;");
    assertTranslation(translation, "@implementation ColorEnum");
    assertTranslation(translation, "return ColorEnum_RED;");
    assertTranslation(translation,
        "ColorEnum_RED = [[ColorEnum alloc] " +
        "initWithInt:(int) 0xff0000 withNSString:@\"Color_RED\" withInt:0];");
    assertTranslation(translation,
      "ColorEnum_WHITE = [[ColorEnum alloc] " +
      "initWithInt:(int) 0xffffff withNSString:@\"Color_WHITE\" withInt:1];");
    assertTranslation(translation,
      "ColorEnum_BLUE = [[ColorEnum alloc] " +
      "initWithInt:(int) 0x0000ff withNSString:@\"Color_BLUE\" withInt:2];");
    assertTranslation(translation, "- (int)getRgb {");
    assertTranslation(translation, "return rgb_;");
    assertTranslation(translation, "- (int)rgb {");
    assertTranslation(translation, "return rgb_;");
    assertTranslation(translation, "- (void)setRgb:(int)newRgb {");
    assertTranslation(translation, "rgb_ = newRgb;");
  }

  public void testEmptyInterfaceGeneration() throws IOException {
    String translation = translateSourceFile(
      "package foo; public interface Compatible {}",
      "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "void FooCompatible_unused() {}");
  }

  public void testInterfaceConstantGeneration() throws IOException {
    String translation = translateSourceFile(
      "package foo; public interface Compatible { public static final Object FOO = new Object(); }",
      "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "static id FooCompatible_FOO_;");
    assertTranslation(translation,
        "FooCompatible_FOO_ = [[NSObject alloc] init];");
  }

  public void testEmptyAnnotationGeneration() throws IOException {
    String translation = translateSourceFile(
      "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.CLASS) " +
      "public @interface Compatible { boolean fooable() default false; }",
      "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "void FooCompatible_unused() {}");
  }

  public void testMethodsWithTypeParameters() throws IOException {
    String translation = translateSourceFile(
        "public class Example implements Foo<String> { public void doFoo(String foo) {}} " +
        "interface Foo<T> { void doFoo(T foo); }",
        "Example", "Example.m");
    assertTranslation(translation, "- (void)doFooWithId:(NSString *)foo {");
  }

  public void testTopLevelClassesNotImported() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Bar bar; } class Bar {}",
        "Example", "Example.m");
    assertFalse(translation.contains("#import \"Bar.h\""));
  }

  public void testEnumWithStaticVar() throws IOException {
    String translation = translateSourceFile(
        "public enum Example { ONE, TWO; public static int foo = 42; }",
        "Example", "Example.m");

    // Verify that there's only one initialize() method.
    String initializeSignature = "+ (void)initialize";
    int initializeOffset = translation.indexOf(initializeSignature);
    assertTrue(initializeOffset != -1);
    initializeOffset = translation.indexOf(initializeSignature,
        initializeOffset + initializeSignature.length());
    assertTrue(initializeOffset == -1);

    assertTranslation(translation, "static int ExampleEnum_foo_;");
    assertTranslation(translation, "ExampleEnum_foo_ = 42;");
  }

  public void testNativeCodeBlock() throws IOException {
    String translation = translateSourceFile(
        "public class Example { native void test() /*-[ good native code block ]-*/; }",
        "Example", "Example.m");
    assertTranslation(translation, "good native code block");
  }

  public void testBadNativeCodeBlock() throws IOException {
    PrintStream errStream = System.err;
    try {
      // Capture error message.
      final StringWriter stringWriter = new StringWriter();
      System.setErr(new PrintStream(System.err) {
        @Override
        public void println(String msg) {
          stringWriter.append(msg);
        }
      });
      String translation = translateSourceFile(
          "public class Example { native void test() /* -[ ]-*/; }",
          "Example", "Example.m");
      assertTranslation(translation, "- (void)test ERROR");
      assertTrue(stringWriter.toString().contains("no native code found"));
    } finally {
      System.setErr(errStream);
    }
  }

  public void testImportDerivedTypeInMethodParams() throws IOException {
    addSourceFile("abstract class Foo implements java.util.List { }", "Foo.java");
    addSourceFile("class Bar { Foo foo() { return null; } }", "Bar.java");
    String translation = translateSourceFile(
        "class Test { " +
        "  void baz(java.util.List list) { }" +
        "  void foobar() { baz(new Bar().foo()); } }",
        "Test", "Test.m");
    assertTranslation(translation, "#import \"Foo.h\"");
  }

  public void testImportDerivedTypeInConstructorParams() throws IOException {
    addSourceFile("abstract class Foo implements java.util.List { }", "Foo.java");
    addSourceFile("class Bar { Foo foo() { return null; } }", "Bar.java");
    addSourceFile("class Baz { Baz(java.util.List l) { } }", "Baz.java");
    String translation = translateSourceFile(
        "class Test { " +
        "  void foobar() { new Baz(new Bar().foo()); } }",
        "Test", "Test.m");
    assertTranslation(translation, "#import \"Foo.h\"");
  }

  public void testImportJavaLangBooleanPlusAssign() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  void foo() { boolean b = true; String s = \"\"; s += b; } }",
        "Test", "Test.m");
    assertTranslation(translation, "#import \"java/lang/Boolean.h\"");
  }

  public void testImportJavaLangBooleanInfixLeft() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  void foo() { boolean b = true; String s = b + \"\"; } }",
        "Test", "Test.m");
    assertTranslation(translation, "#import \"java/lang/Boolean.h\"");
  }

  public void testImportJavaLangBooleanInfixRight() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  void foo() { boolean b = true; String s = \"\" + b; } }",
        "Test", "Test.m");
    assertTranslation(translation, "#import \"java/lang/Boolean.h\"");
  }

  public void testImportJavaLangBooleanInfixExtendedOperands() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  void foo() { boolean b = true; String s = \"\" + \"\" + b; } }",
        "Test", "Test.m");
    assertTranslation(translation, "#import \"java/lang/Boolean.h\"");
  }

  public void testEnumWithEnumField() throws IOException {
    String header = translateSourceFile(
        "public class Test { " +
        "enum Type { BOOLEAN(false), INT(0), STRING(\"\"); " +
        "Type(Object value) { this.value = value; } private Object value; } " +
        "enum Field { BOOL(Type.BOOLEAN), INT32(Type.INT), " +
        "STRING(Type.STRING) { public boolean isPackable() { return false; }}; " +
        "Field(Type type) { this.type = type; } private Type type;" +
        "public boolean isPackable() { return true; }}}",
        "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");
    assertFalse(header.contains("isPackableWithTest_TypeEnum"));
    assertFalse(impl.contains("\n  return NO;\n  [super initWithTest_TypeEnum:arg$0]}"));
    assertTranslation(impl,
        "initWithTest_TypeEnum:[Test_TypeEnum STRING] " +
        "withNSString:@\"Test_Field_STRING\" withInt:2");
  }

  public void testAutoreleasePoolMethod() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;" +
        "public class Test {" +
        "  @AutoreleasePool\n" +
        "  public void foo() { }" +
        "}",
        "Test", "Test.m");
    assertTranslation(translation, "- (void)foo {\n" +
        "  NSAutoreleasePool *pool__ = [[NSAutoreleasePool alloc] init];\n" +
        "  {\n  }\n" +
        "  [pool__ release];\n" +
        "}");
  }
}
