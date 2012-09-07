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
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@link ObjectiveCHeaderGenerator}.
 *
 * @author Tom Ball
 */
public class ObjectiveCHeaderGeneratorTest extends GenerationTest {

  public void testInnerEnumWithPackage() throws IOException {
    String translation = translateSourceFile(
        "package mypackage;" +
        "public class Example { MyClass myclass = new MyClass(); }" +
        "enum Abcd { A, B, C; }" +
        "class MyClass {}", "Example", "mypackage/Example.h");
    assertTranslation(translation, "@interface MypackageExample");
    assertTranslation(translation, "} MypackageAbcd;"); // enum declaration
    assertTranslation(translation, "@interface MypackageAbcdEnum");
    assertTranslation(translation, "@interface MypackageMyClass");
    assertTranslation(translation, "MypackageMyClass *myclass_;");
    assertTranslation(translation, "@property (nonatomic, retain) MypackageMyClass *myclass;");
  }

  public void testTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example {}", "Example", "Example.h");
    assertTranslation(translation, "@interface Example ");
  }

  public void testPackageTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example {}", "Example", "unit/test/Example.h");
    assertTranslation(translation, "@interface UnitTestExample ");
  }

  public void testPackageTypeNameTranslationWithInnerClass() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { class Inner {}}",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "@interface UnitTestExample ");
    assertTranslation(translation, "Example_Inner");
    assertTranslation(translation, "@interface UnitTestExample_Inner ");
  }

  public void testSuperclassTypeTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception {}", "MyException", "MyException.h");
    assertTranslation(translation, "@interface MyException : JavaLangException");
  }

  public void testImplementsTypeTranslation() throws IOException {
    String translation = translateSourceFile(
        "import java.io.Serializable; public class Example implements Serializable {}",
        "Example", "Example.h");
    assertTranslation(translation, "@interface Example : NSObject < JavaIoSerializable >");
  }

  public void testImportTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception { MyException(Throwable t) {super(t);}}",
        "MyException", "MyException.h");
    assertTranslation(translation, "@class JavaLangThrowable;");
    assertTranslation(translation, "#import \"java/lang/Exception.h\"");
  }

  public void testForwardDeclarationTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception { MyException(Throwable t) {super(t);}}",
        "MyException", "MyException.h");
    assertTranslation(translation, "@class JavaLangThrowable;");
  }

  public void testInstanceVariableTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Exception testException; }",
        "Example", "Example.h");
    assertTranslation(translation, "JavaLangException *testException;");
  }

  public void testInterfaceTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example {}",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "@protocol UnitTestExample");
  }

  public void testInterfaceWithMethodTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example { Example getExample(); }",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "(id<UnitTestExample>)getExample;");
  }

  public void testSuperInterfaceTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example extends Bar {} interface Bar {}",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "@protocol UnitTestExample < UnitTestBar >");
  }

  public void testConstTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { public static final int FOO=1; }",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "#define UnitTestExample_FOO 1");
    assertFalse(translation.contains("initialize"));
  }

  public void testStaticVariableTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today; }",
        "Example", "Example.h");
    assertTranslation(translation, "+ (JavaUtilDate *)today;");
    assertTranslation(translation, "+ (void)setTodayWithJavaUtilDate:(JavaUtilDate *)today;");
    assertFalse(translation.contains("initialize"));
    assertFalse(translation.contains("dealloc"));
  }

  public void testStaticVariableWithInitTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today = new java.util.Date(); }",
        "Example", "Example.h");
    assertTranslation(translation, "+ (JavaUtilDate *)today;");
    assertTranslation(translation, "+ (void)setTodayWithJavaUtilDate:(JavaUtilDate *)today;");
    assertFalse(translation.contains("+ (void)initialize;"));
    assertFalse(translation.contains("dealloc"));
  }

  public void testInitMessageTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { void init() {} }", "Example", "Example.h");
    assertTranslation(translation, "- (void)init__;");
  }

  public void testInitializeMessageTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { void initialize() {} }", "Example", "Example.h");
    assertTranslation(translation, "- (void)initialize__;");
  }

  public void testToStringRenaming() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public String toString() { return super.toString(); } }",
      "Example", "Example.h");
    assertTranslation(translation, "- (NSString *)description;");
  }

  public void testMultipleObjectDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Object one, two, three; }",
      "Example", "Example.h");
    assertTranslation(translation, "NSObject *one_, *two_, *three_;");
  }

  public void testMultiplePrimitiveDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { int one, two, three; }",
      "Example", "Example.h");
    assertTranslation(translation, "int one_, two_, three_;");
  }

  public void testMultipleInterfaceDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Comparable one, two, three; }",
      "Example", "Example.h");
    assertTranslation(translation, "id<JavaLangComparable> one_, two_, three_;");
  }

  public void testMultipleClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Class<?> one, two, three; }",
      "Example", "Example.h");
    assertTranslation(translation, "IOSClass *one_, *two_, *three_;");
  }

  public void testInnerClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { class Inner {} }",
      "Example", "Example.h");
    assertTranslation(translation, "@interface Example_Inner : NSObject");
    assertTranslation(translation, "Example *this$0_;");
    assertTranslation(translation, "- (id)initWithExample:(Example *)outer$0;");
  }

  public void testInnerClassDeclarationWithOuterReference() throws IOException {
    String translation = translateSourceFile(
      "public class Example { int i; class Inner { int j = i; } }",
      "Example", "Example.h");
    assertTranslation(translation, "@interface Example_Inner : NSObject");
    assertTranslation(translation, "Example *this$0;");
    assertTranslation(translation, "- (id)initWithExample:(Example *)outer$0;");
  }

  public void testAnonymousClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Runnable run = new Runnable() { public void run() {} }; }",
      "Example", "Example.h");
    assertTranslation(translation, "@interface Example_$1 : NSObject < JavaLangRunnable >");
    assertTranslation(translation, "- (void)run;");
    assertTranslation(translation, "Example *this$0_;");
    assertTranslation(translation, "- (id)initWithExample:(Example *)outer$0;");
  }

  public void testEnum() throws IOException {
    String translation = translateSourceFile(
      "public enum Color { RED, WHITE, BLUE }",
      "Color", "Color.h");
    assertTranslation(translation, "typedef enum {");
    assertTranslation(translation, "Color_RED = 0,");
    assertTranslation(translation, "Color_WHITE = 1,");
    assertTranslation(translation, "Color_BLUE = 2,");
    assertTranslation(translation, "} Color;");
    assertTranslation(translation, "@interface ColorEnum : JavaLangEnum < NSCopying > {");
    assertTranslation(translation, "+ (ColorEnum *)RED;");
    assertTranslation(translation, "+ (ColorEnum *)WHITE;");
    assertTranslation(translation, "+ (ColorEnum *)BLUE;");
    assertTranslation(translation, "+ (IOSObjectArray *)values;");
    assertTranslation(translation, "+ (ColorEnum *)valueOfWithNSString:(NSString *)name;");
  }

  public void testEnumWithParameters() throws IOException {
    String translation = translateSourceFile(
      "public enum Color { RED(0xff0000), WHITE(0xffffff), BLUE(0x0000ff); " +
      "private int rgb; private Color(int rgb) { this.rgb = rgb; } " +
      "public int getRgb() { return rgb; }}",
      "Color", "Color.h");
    assertTranslation(translation, "@interface ColorEnum : JavaLangEnum");
    assertTranslation(translation, "int rgb_;");
    assertTranslation(translation, "@property (nonatomic, assign) int rgb;");
    assertTranslation(translation, "- (id)initWithInt:(int)rgb");
    assertTranslation(translation, "withNSString:(NSString *)name");
    assertTranslation(translation, "withInt:(int)ordinal;");
  }

  public void testArrayFieldDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { char[] before; char after[]; }",
      "Example", "Example.h");
    assertTranslation(translation, "IOSCharArray *before;");
    assertTranslation(translation, "IOSCharArray *after;");
  }

  public void testTypeSortNoDependencies() throws IOException {
    CompilationUnit unit = translateType("Example",
        "public class Example { class Foo {} }");
    @SuppressWarnings("unchecked")
    List<AbstractTypeDeclaration> types = unit.types();
    assertEquals(2, types.size());
    Set<ITypeBinding> forwards = ObjectiveCHeaderGenerator.sortTypes(types);

    // Expecting no sort change, no forwards
    assertEquals("Example", types.get(0).getName().getIdentifier());
    assertEquals("Foo", types.get(1).getName().getIdentifier());
    assertEquals(0, forwards.size());
  }

  public void testTypeSortForward() throws IOException {
    CompilationUnit unit = translateType("Example",
        "public class Example { Foo foo; class Foo {} }");
    @SuppressWarnings("unchecked")
    List<AbstractTypeDeclaration> types = unit.types();
    assertEquals(2, types.size());
    Set<ITypeBinding> forwards = ObjectiveCHeaderGenerator.sortTypes(types);

    // Expecting no sort change, one forward.
    assertEquals("Example", types.get(0).getName().getIdentifier());
    assertEquals("Foo", types.get(1).getName().getIdentifier());
    assertEquals(1, forwards.size());
    assertTrue(containsType(forwards, "Example_Foo"));
  }

  public void testTypeSortInnerInterface() throws IOException {
    CompilationUnit unit = translateType("Example",
        "public class Example { Foo foo; Bar bar; class Bar implements Foo {} interface Foo {} }");
    @SuppressWarnings("unchecked")
    List<AbstractTypeDeclaration> types = unit.types();
    assertEquals(3, types.size());
    Set<ITypeBinding> forwards = ObjectiveCHeaderGenerator.sortTypes(types);

    // Expecting Foo before Bar, otherwise no sort change, one forward.
    assertEquals("Example", types.get(0).getName().getIdentifier());
    assertEquals("Foo", types.get(1).getName().getIdentifier());
    assertEquals("Bar", types.get(2).getName().getIdentifier());
    assertEquals(2, forwards.size());
    assertTrue(containsType(forwards, "Example_Bar"));
  }


  public void testTypeSortInnerInterfaceParameterReference() throws IOException {
    CompilationUnit unit = translateType("Example",
        "public class Example { Foo foo; interface Foo { void doSomething(Example e); } }");
    @SuppressWarnings("unchecked")
    List<AbstractTypeDeclaration> types = unit.types();
    assertEquals(2, types.size());
    Set<ITypeBinding> forwards = ObjectiveCHeaderGenerator.sortTypes(types);

    // Expecting no sort change, one forward.
    assertEquals("Example", types.get(0).getName().getIdentifier());
    assertEquals("Foo", types.get(1).getName().getIdentifier());
    assertEquals(1, forwards.size());
    assertTrue(containsType(forwards, "Example_Foo"));
  }

  public void testAnnotationGeneration() throws IOException {
    String translation = translateSourceFile(
      "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.CLASS) " +
      "public @interface Compatible { boolean fooable() default false; }",
      "Compatible", "foo/Compatible.h");

    // Test both that annotation was declared as a marker protocol,
    // and that it's empty.
    assertTranslation(translation, "@protocol FooCompatible < NSObject >\n@end");
  }

  private boolean containsType(Set<ITypeBinding> bindings, String typeName) {
    for (ITypeBinding binding : bindings) {
      if (NameTable.getFullName(binding).equals(typeName)) {
        return true;
      }
    }
    return false;
  }

  public void testCharacterEdgeValues() throws IOException {
    String translation = translateSourceFile(
      "public class Test { " +
      "  public static final char MIN = 0; " +
      "  public static final char MAX = '\uffff'; " +
      "}", "Test", "Test.h");
    assertTranslation(translation, "x00");
    assertTranslation(translation, "0xffff");
  }

  public void testOverriddenFieldTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int size; } " +
        "class Subclass extends Example { int size; }",
        "Example", "Example.h");
    assertTranslation(translation, "int size_;");
    assertTranslation(translation, "@property (nonatomic, assign) int size;");
    assertTranslation(translation, "int size__;");
    assertTranslation(translation, "@property (nonatomic, assign) int size_;");
  }

  public void testOverriddenNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int size; int size() { return size; }} " +
        "class Subclass extends Example { int size; int size() { return size; }}",
        "Example", "Example.h");
    assertTranslation(translation, "int size__;");
    assertTranslation(translation, "@property (nonatomic, assign) int size_;");
    assertTranslation(translation, "int size___;");
    assertTranslation(translation, "@property (nonatomic, assign) int size__;");
  }

  public void testEnumNaming() throws IOException {
    String translation = translateSourceFile(
        "public enum MyEnum { ONE, TWO, THREE }",
        "MyEnum", "MyEnum.h");
    assertTranslation(translation, "} MyEnum;");
    assertTranslation(translation, "@interface MyEnumEnum : JavaLangEnum");
    assertTranslation(translation, "+ (MyEnumEnum *)ONE;");
  }

  public void testNoImportForMappedTypes() throws IOException {
    String translation = translateSourceFile(
        "public class Test extends Object implements Cloneable { " +
        "  public String toString() { return \"\"; }" +
        "  public Class<?> myClass() { return getClass(); }}",
        "Test", "Test.h");
    assertFalse(translation.contains("#import \"java/lang/Class.h\""));
    assertFalse(translation.contains("#import \"java/lang/Cloneable.h\""));
    assertFalse(translation.contains("#import \"java/lang/Object.h\""));
    assertFalse(translation.contains("#import \"java/lang/String.h\""));
    assertFalse(translation.contains("#import \"Class.h\""));
    assertFalse(translation.contains("#import \"NSCopying.h\""));
    assertFalse(translation.contains("#import \"NSObject.h\""));
    assertFalse(translation.contains("#import \"NSString.h\""));
    assertTranslation(translation, "NSCopying");
  }

  public void testAnonymousConcreteSubclassOfGenericAbstractType() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  interface FooInterface<T> { public void foo1(T t); public void foo2(); }" +
        "  abstract static class Foo<T> implements FooInterface<T> { public void foo2() { } }" +
        "  Foo<Integer> foo = new Foo<Integer>() {" +
        "    public void foo1(Integer i) { } }; }",
        "Test", "Test.h");
    assertTranslation(translation, "foo1WithId:(JavaLangInteger *)i");
  }

  // Verify that an empty Java enum doesn't define an empty C enum,
  // which is illegal.
  public void testEmptyEnum() throws IOException {
    String header = translateSourceFile("public class A { enum Foo {} }", "A", "A.h");
    String impl = getTranslatedFile("A.m");

    // Verify there's no C enum.
    assertFalse(header.contains("typedef enum {\n} A_Foo;"));

    // Verify there's still a Java enum type.
    assertTranslation(header, "@interface A_FooEnum : JavaLangEnum");
    assertTranslation(impl, "@implementation A_FooEnum");
  }

  public void testEnumWithInterfaces() throws IOException {
    String translation = translateSourceFile(
        "public class A { interface I {} " +
        "enum Foo implements I, Runnable, Cloneable { " +
        "A, B, C; public void run() {}}}", "A", "A.h");
    assertTranslation(translation,
        "@interface A_FooEnum : JavaLangEnum < NSCopying, A_I, JavaLangRunnable >");
    assertTranslation(translation, "#import \"java/lang/Runnable.h\"");
  }
}
