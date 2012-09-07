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

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

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
        "public class Example implements Cloneable { " +
        "int i;" +
        "public Example copy() { return (Example) clone(); }" +
        "public Object clone() { " +
        "  try { Example e = (Example) super.clone(); e.i = i; return e; } " +
        "  catch (CloneNotSupportedException e) { return null; }}}",
        "Example", "Example.h");
    assertTranslation(translation, "- (Example *)copy__;");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation, "return (Example *) [self clone];");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone {");
    assertTranslation(translation, "Example *e = (Example *) [super clone];");
    assertTranslation(translation, "((Example *) NIL_CHK(e)).i = i_");
  }

  public void testStringValueOfBoolean() throws IOException {
    String source =
        "String trueString = String.valueOf(true); String falseString = String.valueOf(false);";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSString *trueString = [NSString valueOfBool:YES];", result);
    result = generateStatement(stmts.get(1));
    assertEquals("NSString *falseString = [NSString valueOfBool:NO];", result);
  }

  /**
   * Verify that statements within a translated method get translated, too.
   */
  public void testMethodAndStatementTranslation() throws IOException {
    String source = "public String toString(boolean value) { return String.valueOf(value); }";
    MethodDeclaration method = translateMethod(source);
    assertNotNull(method);
    assertEquals("NSString", method.getReturnType2().toString());
    @SuppressWarnings("unchecked")
    List<Statement> stmts = method.getBody().statements();  // safe by design
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("return [NSString valueOfBool:value];", result);
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
    assertEquals("NSString *s = [NSString stringWithString:@\"test\"];", result);
  }

  public void testStringHashCode() throws IOException {
    String source = "int test = \"foo\".hashCode();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("int test = [@\"foo\" hash];", result);
  }

  public void testClassGetSuperclass() throws IOException {
    String source = "Class cls = getClass(); Class superClass = cls.getSuperclass();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSClass *cls = [self getClass];", result);
    result = generateStatement(stmts.get(1));
    assertEquals("IOSClass *superClass = [NIL_CHK(cls) getSuperclass];", result);
  }

  /**
   * Verify that Class.getName() and Class.getSimpleName() return the
   * Obj-C class name.  They are the same because Obj-C has a flat class
   * namespace.
   */
  public void testClassGetName() throws IOException {
    String source = "Class cls = getClass(); String s1 = cls.getName();" +
        "String s2 = cls.getSimpleName(); String s3 = cls.getCanonicalName();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(4, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("NSString *s1 = [NIL_CHK(cls) getName];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("NSString *s2 = [NIL_CHK(cls) getSimpleName];", result);
    result = generateStatement(stmts.get(3));
    assertEquals("NSString *s3 = [NIL_CHK(cls) getCanonicalName];", result);
  }

  public void testStringSubstring() throws IOException {
    String source = "String s1 = \"123456\"; String s2 = s1.substring(2); " +
        "String s3 = s1.substring(2, 4);";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("NSString *s2 = [NIL_CHK(s1) substring:2];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("NSString *s3 = [NIL_CHK(s1) substring:2 endIndex:4];", result);
  }

  public void testStringIndexOf() throws IOException {
    String source = "String s = \"'twas brillig, and the slithy toves\";" +
        "int idx = s.indexOf('g'); idx = s.indexOf(\"brillig\");" +
        "idx = s.lastIndexOf('v'); idx = s.lastIndexOf(\"the\");" +
        "idx = s.indexOf('g', 1); idx = s.indexOf(\"brillig\", 2);" +
        "idx = s.lastIndexOf('v', 3); idx = s.lastIndexOf(\"the\", 4);";
    List<Statement> stmts = translateStatements(source);
    assertEquals(9, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("int idx = [NIL_CHK(s) indexOf:'g'];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("idx = [NIL_CHK(s) indexOfString:@\"brillig\"];", result);
    result = generateStatement(stmts.get(3));
    assertEquals("idx = [NIL_CHK(s) lastIndexOf:'v'];", result);
    result = generateStatement(stmts.get(4));
    assertEquals("idx = [NIL_CHK(s) lastIndexOfString:@\"the\"];", result);
    result = generateStatement(stmts.get(5));
    assertEquals("idx = [NIL_CHK(s) indexOf:'g' fromIndex:1];", result);
    result = generateStatement(stmts.get(6));
    assertEquals("idx = [NIL_CHK(s) indexOfString:@\"brillig\" fromIndex:2];", result);
    result = generateStatement(stmts.get(7));
    assertEquals("idx = [NIL_CHK(s) lastIndexOf:'v' fromIndex:3];", result);
    result = generateStatement(stmts.get(8));
    assertEquals("idx = [NIL_CHK(s) lastIndexOfString:@\"the\" fromIndex:4];", result);
  }

  public void testStringToCharArray() throws IOException {
    String source = "String s = \"123456\"; char[] array = s.toCharArray();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("IOSCharArray *array = [NIL_CHK(s) toCharArray];", result);
  }

  public void testNewInstanceMapping() throws IOException {
    String source = "try { Class<?> clazz = Object.class; " +
        "Object o = clazz.newInstance(); } catch (Exception e) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertTranslation(result, "[NIL_CHK(clazz) newInstance]");
  }

  // Verify that a method named cloned in a class that doesn't
  // implement Cloneable is unchanged.
  public void testNonCloneableClone() throws IOException {
    String translation = translateSourceFile(
        "public class Example { " +
        "String s; " +
        "public Object clone() { " +
        "  Example e = new Example();" +
        "  e.s = s;" +
        "  return e;" +
        "}}",
        "Example", "Example.h");
    assertTranslation(translation, "- (id)clone;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation, "- (id)clone {");
  }

  // Verify that if a Cloneable class doesn't have a clone method,
  public void testCloneMethodAddedToCloneable() throws IOException {
    String translation = translateSourceFile(
        "public class Example implements Cloneable { int i; }",
        "Example", "Example.h");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone {");
    assertTranslation(translation, "return [self clone];");
  }

  public void testCloneRenamingWithSuperClone() throws IOException {
    String translation = translateSourceFile(
        "public class Example implements Cloneable { " +
        "class Inner extends Example {" +
        "  int i;" +
        "  public Example copy() { return (Example) clone(); }" +
        "  public Object clone() { " +
        "    try { Inner inner = (Inner) super.clone(); inner.i = i; return inner; } " +
        "    catch (CloneNotSupportedException e) { return null; }}}}",
        "Example", "Example.h");
    assertTranslation(translation, "- (Example *)copy__;");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation, "return (Example *) [self clone];");
    assertTranslation(translation, "- (id)copyWithZone:(NSZone *)zone {");
    assertTranslation(translation,
        "Example_Inner *inner = (Example_Inner *) [super clone];");
    assertTranslation(translation, "((Example_Inner *) NIL_CHK(inner)).i = i_;");
  }
}
