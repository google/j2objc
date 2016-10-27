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
import com.google.devtools.j2objc.ast.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link JavaToIOSMethodTranslatorTest}.
 *
 * @author Tom Ball
 */
public class JavaToIOSMethodTranslatorTest extends GenerationTest {

  public void testCopyCloneRenaming() throws IOException {
    String translation = translateSourceFile(
        "public class Example implements Cloneable { "
        + "int i;"
        + "public Example copy() { return (Example) clone(); }"
        + "public Object clone() { "
        + "  try { Example e = (Example) super.clone(); e.i = i; return e; } "
        + "  catch (CloneNotSupportedException e) { return null; }}}",
        "Example", "Example.h");
    assertTranslation(translation, "- (Example *)copy__ OBJC_METHOD_FAMILY_NONE;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation,
        "return (Example *) cast_chk([self java_clone], [Example class]);");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone {");
    assertTranslation(translation,
        "Example *e = (Example *) cast_chk([super java_clone], [Example class]);");
    assertTranslation(translation, "((Example *) nil_chk(e))->i_ = i_");
  }

  public void testStringValueOfBoolean() throws IOException {
    String source =
        "String trueString = String.valueOf(true); String falseString = String.valueOf(false);";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSString *trueString = NSString_java_valueOfBool_(true);", result);
    result = generateStatement(stmts.get(1));
    assertEquals("NSString *falseString = NSString_java_valueOfBool_(false);", result);
  }

  /**
   * Verify that statements within a translated method get translated, too.
   */
  public void testMethodAndStatementTranslation() throws IOException {
    String translation = translateSourceFile(
        "class Test { public String toString(boolean value) { return String.valueOf(value); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (NSString *)toStringWithBoolean:(jboolean)value {",
        "return NSString_java_valueOfBool_(value);");
  }

  public void testStringDefaultConstructor() throws IOException {
    String source = "String s = new String();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSString *s = [NSString string];", result);
  }

  public void testStringConstructorWithString() throws IOException {
    String source = "String s = new String(\"test\");";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSString *s = @\"test\";", result);
  }

  public void testStringHashCode() throws IOException {
    String source = "int test = \"foo\".hashCode();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("jint test = ((jint) [@\"foo\" hash]);", result);
  }

  public void testClassGetSuperclass() throws IOException {
    String source = "Class cls = getClass(); Class superClass = cls.getSuperclass();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSClass *cls = [self java_getClass];", result);
    result = generateStatement(stmts.get(1));
    assertEquals("IOSClass *superClass = [cls getSuperclass];", result);
  }

  /**
   * Verify that Class.getName() and Class.getSimpleName() return the
   * Obj-C class name.  They are the same because Obj-C has a flat class
   * namespace.
   */
  public void testClassGetName() throws IOException {
    String source = "Class cls = getClass(); String s1 = cls.getName();"
        + "String s2 = cls.getSimpleName(); String s3 = cls.getCanonicalName();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(4, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("NSString *s1 = [cls getName];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("NSString *s2 = [cls getSimpleName];", result);
    result = generateStatement(stmts.get(3));
    assertEquals("NSString *s3 = [cls getCanonicalName];", result);
  }

  public void testStringSubstring() throws IOException {
    String source = "String s1 = \"123456\"; String s2 = s1.substring(2); "
        + "String s3 = s1.substring(2, 4);";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("NSString *s2 = [s1 java_substring:2];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("NSString *s3 = [s1 java_substring:2 endIndex:4];", result);
  }

  public void testStringIndexOf() throws IOException {
    String source = "String s = \"'twas brillig, and the slithy toves\";"
        + "int idx = s.indexOf('g'); idx = s.indexOf(\"brillig\");"
        + "idx = s.lastIndexOf('v'); idx = s.lastIndexOf(\"the\");"
        + "idx = s.indexOf('g', 1); idx = s.indexOf(\"brillig\", 2);"
        + "idx = s.lastIndexOf('v', 3); idx = s.lastIndexOf(\"the\", 4);";
    List<Statement> stmts = translateStatements(source);
    assertEquals(9, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("jint idx = [s java_indexOf:'g'];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("idx = [s java_indexOfString:@\"brillig\"];", result);
    result = generateStatement(stmts.get(3));
    assertEquals("idx = [s java_lastIndexOf:'v'];", result);
    result = generateStatement(stmts.get(4));
    assertEquals("idx = [s java_lastIndexOfString:@\"the\"];", result);
    result = generateStatement(stmts.get(5));
    assertEquals("idx = [s java_indexOf:'g' fromIndex:1];", result);
    result = generateStatement(stmts.get(6));
    assertEquals("idx = [s java_indexOfString:@\"brillig\" fromIndex:2];", result);
    result = generateStatement(stmts.get(7));
    assertEquals("idx = [s java_lastIndexOf:'v' fromIndex:3];", result);
    result = generateStatement(stmts.get(8));
    assertEquals("idx = [s java_lastIndexOfString:@\"the\" fromIndex:4];", result);
  }

  public void testStringToCharArray() throws IOException {
    String source = "String s = \"123456\"; char[] array = s.toCharArray();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("IOSCharArray *array = [s java_toCharArray];", result);
  }

  public void testNewInstanceMapping() throws IOException {
    String source = "try { Class<?> clazz = Object.class; "
        + "Object o = clazz.newInstance(); } catch (Exception e) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertTranslation(result, "[clazz newInstance]");
  }

  // Verify that a method named cloned in a class that doesn't
  // implement Cloneable is unchanged.
  public void testNonCloneableClone() throws IOException {
    String translation = translateSourceFile(
        "public class Example { "
        + "String s; "
        + "public Object clone() { "
        + "  Example e = new Example();"
        + "  e.s = s;"
        + "  return e;"
        + "}}",
        "Example", "Example.h");
    assertTranslation(translation, "- (id)java_clone;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation, "- (id)java_clone {");
  }

  // Verify that if a Cloneable class doesn't have a clone method,
  public void testCloneMethodAddedToCloneable() throws IOException {
    String translation = translateSourceFile(
        "public class Example implements Cloneable { int i; }",
        "Example", "Example.m");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone {");
    assertTranslation(translation, "return [[self java_clone] retain];");
  }

  public void testCloneRenamingWithSuperClone() throws IOException {
    String translation = translateSourceFile(
        "public class Example implements Cloneable { "
        + "class Inner extends Example {"
        + "  int i;"
        + "  public Example copy() { return (Example) clone(); }"
        + "  public Object clone() { "
        + "    try { Inner inner = (Inner) super.clone(); inner.i = i; return inner; } "
        + "    catch (CloneNotSupportedException e) { return null; }}}}",
        "Example", "Example.h");
    assertTranslation(translation, "- (Example *)copy__ OBJC_METHOD_FAMILY_NONE;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation,
        "return (Example *) cast_chk([self java_clone], [Example class]);");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone {");
    assertTranslation(translation, "Example_Inner *inner = "
        + "(Example_Inner *) cast_chk([super java_clone], [Example_Inner class]);");
    assertTranslation(translation, "((Example_Inner *) nil_chk(inner))->i_ = i_;");
  }

  // Ensure using the builder pattern does not invoke O(2^N) running time.
  // The test below would have taken about 100 days to run before this was fixed.
  public void testHugeBuilderExpression() throws IOException {
    translateSourceFile(
        "public class Example { "
        + "  static class Builder { "
        + "    Example build() { return new Example(); } "
        + "    Builder add(int i) { return this; } "
        + "  } "
        + "  static Example create() { "
        + "    return new Builder()"
        + "      .add(10).add(11).add(12).add(13).add(14).add(15).add(16).add(17).add(18).add(19)"
        + "      .add(20).add(21).add(22).add(23).add(24).add(25).add(26).add(27).add(28).add(29)"
        + "      .add(30).add(31).add(32).add(33).add(34).add(35).add(36).add(37).add(38).add(39)"
        + "      .add(40).add(41).add(42).add(43).add(44).add(45).add(46).add(47).add(48).add(49)"
        + "      .build(); "
        + "} }",
        "Example", "Example.m");
  }
}
