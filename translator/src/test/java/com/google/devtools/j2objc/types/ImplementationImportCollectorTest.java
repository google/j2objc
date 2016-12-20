/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.util.HeaderMap;
import java.io.IOException;

/**
 * Unit tests for the {@link ImplementationImportCollector} class.
 *
 * @author Tom Ball
 */
public class ImplementationImportCollectorTest extends GenerationTest {

  // Verify that invoked method's return value has associated header.
  public void testMethodReturnHasHeader() throws IOException {
    addSourceFile("class FooException extends Exception { AssertionError asAssertion() { "
        + "return new AssertionError(this); }}", "FooException.java");
    addSourceFile(
        "class FooMaker { static FooException makeException() { return new FooException(); }}",
        "FooMaker.java");
    String translation = translateSourceFile(
        "class A { void test() { "
        + "throw FooMaker.makeException().asAssertion(); }}", "A", "A.m");
    assertTranslation(translation, "#include \"FooException.h\"");
  }

  // http://b/7073329
  public void testVarargsMethodNoActualArguments() throws IOException {
    translateSourceFile(
        "class Test { Test(String ... values) { } Test test = new Test(); }",
        "Test", "Test.m");
    // Nothing to do; Successful translation is the test.
  }

  public void testOneArgumentAndVarargs() throws IOException {
    translateSourceFile(
        "class Test { Test(int count, int ... values) { } Test test = new Test(2, 42, 63); }",
        "Test", "Test.m");
    // Nothing to do; Successful translation is the test.
  }

  // http://b/7106570
  public void testVarargsMethodManyArguments() throws IOException {
    translateSourceFile(
        "class Test { Test(int... values) { } Test test = new Test(1,2,3); }",
        "Test", "Test.m");
    // Nothing to do; Successful translation is the test.
  }

  public void testBooleanArrayImport() throws IOException {
    addSourceFile("class A { boolean[] b; }", "A.java");
    String translation = translateSourceFile(
        "class B { int test() { return new A().b.length; }}", "B", "B.m");
    assertTranslation(translation, "#include \"IOSPrimitiveArray.h\"");
  }

  public void testPrimitiveArrayAsParameterImport() throws IOException {
    addSourceFile(
        "class B { protected char bits[]; public B(char[] bits_) { bits = bits_;} }", "B.java");
    String translation = translateSourceFile(
        "class A { public static final B test = new B(new char[]{'a', 'b'}); }", "A", "A.m");
    assertTranslation(translation, "#include \"IOSPrimitiveArray.h\"");
  }

  public void testObjectArrayImport() throws IOException {
    String translation = translateSourceFile(
        "import java.util.BitSet; class A { public BitSet[] test = new BitSet[3]; }", "A", "A.m");
    assertTranslation(translation, "#include \"IOSObjectArray.h\"");
    assertTranslation(translation, "#include \"java/util/BitSet.h\"");
  }

  public void testEnhancedForMethodInvocation() throws IOException {
    addSourceFile("import java.util.*; class A { "
        + "final Map<String,String> map = new HashMap<>(); }", "A.java");
    String translation = translateSourceFile(
        "import java.util.*; class B extends A { "
        + "void test() { for (String s : map.keySet()) {}}}", "B", "B.m");
    assertTranslation(translation, "#include \"java/util/Map.h\"");
  }

  public void testReturnTypeOfSuperclassMethod() throws IOException {
    addSourceFile("interface I {}", "I.java");
    addSourceFile("class A implements I {}", "A.java");
    addSourceFile("class B { A getAnA() { return new A(); } }", "B.java");
    String translation = translateSourceFile(
        "class C extends B { void test() { I i = getAnA(); } }", "C", "C.m");
    assertTranslation(translation, "#include \"A.h\"");
  }

  // Verify that a primitive type literal has a wrapper class import.
  public void testPrimitiveTypeLiteral() throws IOException {
    // Use assignment to a @Weak object to ensure the IOSClass import isn't
    // picked up by visiting a method or function invocation.
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak; "
        + "class Test { @Weak Object o; void test() { o = double.class; } }",
        "Test", "Test.m");
    assertTranslation(translation, "#include \"IOSClass.h\"");
  }

  // Verify that an object array type literal imports IOSClass.
  public void testArrayTypeLiteralImport() throws IOException {
    String translation = translateSourceFile(
        "class Test { Class arrayType() { return Object[].class; }}",
        "Test", "Test.m");
    assertTranslation(translation, "#include \"IOSClass.h\"");
  }

  // Verify that a multi-dimensional array declaration imports IOSObjectArray.
  public void testMultiArrayTypeLiteralImport() throws IOException {
    String translation = translateSourceFile(
        "class Test { String[][] map = { { \"1\", \"one\" }, { \"2\", \"two\" } }; }",
        "Test", "Test.m");
    assertTranslation(translation, "#include \"IOSObjectArray.h\"");
  }

  // Verify that a multi-catch clause imports are all collected.
  public void testMultiCatchClauses() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() {"
        + "  try { System.out.println(); } catch (ArithmeticException | AssertionError | "
        + "      ClassCastException | SecurityException e) {} }}",
        "Test", "Test.m");
    assertTranslation(translation, "#include \"java/lang/ArithmeticException.h\"");
    assertTranslation(translation, "#include \"java/lang/AssertionError.h\"");
    assertTranslation(translation, "#include \"java/lang/ClassCastException.h\"");
    assertTranslation(translation, "#include \"java/lang/SecurityException.h\"");
  }

  // Verify that platform class packages aren't truncated with --no-package-directories.
  public void testPlatformImports() throws IOException {
    options.getHeaderMap().setOutputStyle(HeaderMap.OutputStyleOption.NONE);
    String translation = translateSourceFile(
        "package foo.bar; import org.xml.sax.*; import org.xml.sax.helpers.*; "
        + "class Test { XMLReader test() { "
        + "  try { return XMLReaderFactory.createXMLReader(); } catch (SAXException e) {} "
        + "  return null; }}",
        "Test", "Test.m");

    // Test file's import should not have package.
    assertTranslation(translation, "#include \"Test.h\"");

    // Platform file's imports should.
    assertTranslation(translation, "#include \"org/xml/sax/SAXException.h\"");
    assertTranslation(translation, "#include \"org/xml/sax/XMLReader.h\"");
    assertTranslation(translation, "#include \"org/xml/sax/helpers/XMLReaderFactory.h\"");
  }

  // Verify that platform class packages aren't changed with --preserve-full-paths.
  public void testPlatformImportsSourceDirs() throws IOException {
    options.getHeaderMap().setOutputStyle(HeaderMap.OutputStyleOption.SOURCE);
    String translation = translateSourceFile(
        "package foo.bar; import org.xml.sax.*; import org.xml.sax.helpers.*; "
        + "class Test { XMLReader test() { "
        + "  try { return XMLReaderFactory.createXMLReader(); } catch (SAXException e) {} "
        + "  return null; }}",
        "Test", "Test.m");

    // Test file's import should not have package.
    assertTranslation(translation, "#include \"Test.h\"");

    // Platform file's imports should.
    assertTranslation(translation, "#include \"org/xml/sax/SAXException.h\"");
    assertTranslation(translation, "#include \"org/xml/sax/XMLReader.h\"");
    assertTranslation(translation, "#include \"org/xml/sax/helpers/XMLReaderFactory.h\"");
  }

  public void testAddsHeaderForRenamedMainType() throws IOException {
    String translation = translateSourceFile(
        "package foo; import com.google.j2objc.annotations.ObjectiveCName;"
        + " @ObjectiveCName(\"Bar\") class Test {}", "foo.Test", "foo/Test.m");
    assertTranslation(translation, "#include \"foo/Test.h\"");
  }

  public void testImportsNativeExpressionType() throws IOException {
    addSourceFile("class Foo { "
        + "  static java.util.List list = java.util.Collections.EMPTY_LIST; "
        + "}", "Foo.java");
    String translation = translateSourceFile("class Test { void test() { "
        + "java.util.Collection c = null; c = Foo.list; }}", "Test", "Test.m");
    assertTranslation(translation, "#include \"java/util/List.h\"");
  }

  public void testNoImportsForAbstractMethod() throws IOException {
    String translation = translateSourceFile(
        "interface Test { java.util.Map foo(java.util.List l); }", "Test", "Test.m");
    assertNotInTranslation(translation, "Map.h");
    assertNotInTranslation(translation, "List.h");
  }
}
