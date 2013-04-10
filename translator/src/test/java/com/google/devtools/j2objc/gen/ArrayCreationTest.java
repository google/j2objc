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

import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Verifies that array creation, with or without initializer expressions, are
 * translated corrected.
 *
 * @author Tom Ball
 */
public class ArrayCreationTest extends GenerationTest {

  public void testBooleanArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("boolean[] foo = new boolean[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals(
      "IOSBooleanArray *foo = [[[IOSBooleanArray alloc] initWithLength:3] autorelease];",
      result);
  }

  public void testByteArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("byte[] foo = new byte[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSByteArray *foo = [[[IOSByteArray alloc] initWithLength:3] autorelease];",
        result);
  }

  public void testCharArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("char[] foo = new char[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSCharArray *foo = [[[IOSCharArray alloc] initWithLength:3] autorelease];",
        result);
  }

  public void testDoubleArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("double[] foo = new double[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSDoubleArray *foo = [[[IOSDoubleArray alloc] initWithLength:3] autorelease];",
        result);
  }

  public void testFloatArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("float[] foo = new float[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSFloatArray *foo = [[[IOSFloatArray alloc] initWithLength:3] autorelease];",
        result);
  }

  public void testIntArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("int[] foo = new int[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSIntArray *foo = [[[IOSIntArray alloc] initWithLength:3] autorelease];",
        result);
  }

  public void testLongArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("long[] foo = new long[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSLongArray *foo = [[[IOSLongArray alloc] initWithLength:3] autorelease];",
        result);
  }

  public void testShortArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("short[] foo = new short[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSShortArray *foo = [[[IOSShortArray alloc] initWithLength:3] autorelease];",
        result);
  }

  public void testObjectArrayCreationNoInitializer() {
    List<Statement> stmts = translateStatements("Integer[] foo = new Integer[3];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSObjectArray *foo = [[[IOSObjectArray alloc] " +
        "initWithLength:3 type:[IOSClass classWithClass:[JavaLangInteger class]]] autorelease];",
        result);
  }

  public void testBooleanArrayCreationNoDimension() {
    List<Statement> stmts = translateStatements("boolean[] foo = { true, false };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSBooleanArray *foo = " +
    		"[IOSBooleanArray arrayWithBooleans:(BOOL[]){ YES, NO } count:2];", result);
  }

  public void testByteArrayCreationNoDimension() {
    List<Statement> stmts = translateStatements("byte[] foo = { 1, -2 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals(
        "IOSByteArray *foo = [IOSByteArray arrayWithBytes:(char[]){ 1, -2 } count:2];", result);
  }

  public void testCharArrayCreationNoDimension() {
    List<Statement> stmts = translateStatements("char[] foo = { 'A', 'z' };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSCharArray *foo = " +
        "[IOSCharArray arrayWithCharacters:(unichar[]){ 'A', 'z' } count:2];", result);
  }

  public void testDoubleArrayCreationNoDimension() {
    List<Statement> stmts = translateStatements("double[] foo = { 123.45, 3.1416 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSDoubleArray *foo = " +
        "[IOSDoubleArray arrayWithDoubles:(double[]){ 123.45, 3.1416 } count:2];", result);
  }

  public void testFloatArrayCreationNoDimension() {
    List<Statement> stmts = translateStatements("float[] foo = { 123.45f, -0.0001f };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSFloatArray *foo = " +
    		"[IOSFloatArray arrayWithFloats:(float[]){ 123.45f, -0.0001f } count:2];", result);
  }

  public void testIntArrayCreationNoDimension() {
    List<Statement> stmts = translateStatements("int[] foo = { -123, 1280000 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSIntArray *foo = " +
        "[IOSIntArray arrayWithInts:(int[]){ -123, 1280000 } count:2];", result);
  }

  public void testLongArrayCreationNoDimension() {
    List<Statement> stmts = translateStatements("long[] foo = { 123*456, 456789 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSLongArray *foo = " +
        "[IOSLongArray arrayWithLongs:(long long int[]){ 123 * 456, 456789 } count:2];", result);
  }

  public void testShortArrayCreationNoDimension() {
    List<Statement> stmts = translateStatements("short[] foo = { 24, 42 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSShortArray *foo = " +
        "[IOSShortArray arrayWithShorts:(short int[]){ 24, 42 } count:2];", result);
  }

  public void testBooleanArrayCreation() {
    List<Statement> stmts = translateStatements("boolean[] foo = new boolean[] { true, false };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSBooleanArray *foo = " +
        "[IOSBooleanArray arrayWithBooleans:(BOOL[]){ YES, NO } count:2];", result);
  }

  public void testByteArrayCreation() {
    List<Statement> stmts = translateStatements("byte[] foo = new byte[] { 1, -2 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSByteArray *foo = " +
        "[IOSByteArray arrayWithBytes:(char[]){ 1, -2 } count:2];", result);
  }

  public void testCharArrayCreation() {
    List<Statement> stmts = translateStatements("char[] foo = new char[] { 'A', 'z' };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSCharArray *foo = " +
        "[IOSCharArray arrayWithCharacters:(unichar[]){ 'A', 'z' } count:2];", result);
  }

  public void testDoubleArrayCreation() {
    List<Statement> stmts = translateStatements("double[] foo = new double[] { 123.45, 3.1416 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSDoubleArray *foo = " +
        "[IOSDoubleArray arrayWithDoubles:(double[]){ 123.45, 3.1416 } count:2];", result);
  }

  public void testFloatArrayCreation() {
    List<Statement> stmts =
        translateStatements("float[] foo = new float[] { 123.45f, -0.0001f };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSFloatArray *foo = " +
        "[IOSFloatArray arrayWithFloats:(float[]){ 123.45f, -0.0001f } count:2];", result);
  }

  public void testIntArrayCreation() {
    List<Statement> stmts = translateStatements("int[] foo = new int[] { -123, 1280000 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSIntArray *foo = " +
        "[IOSIntArray arrayWithInts:(int[]){ -123, 1280000 } count:2];", result);
  }

  public void testLongArrayCreation() {
    List<Statement> stmts = translateStatements("long[] foo = new long[] { 123*456, 456789 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSLongArray *foo = " +
        "[IOSLongArray arrayWithLongs:(long long int[]){ 123 * 456, 456789 } count:2];", result);
  }

  public void testShortArrayCreation() {
    List<Statement> stmts = translateStatements("short[] foo = new short[] { 24, 42 };");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSShortArray *foo = " +
        "[IOSShortArray arrayWithShorts:(short int[]){ 24, 42 } count:2];", result);
  }

  public void testMultiDimArrayCreateWithSizes() {
    List<Statement> stmts = translateStatements("int foo[][][] = new int[2][3][4];");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSObjectArray *foo = " +
        "[IOSObjectArray arrayWithObjects:(id[]){ " +
          "[IOSObjectArray arrayWithObjects:(id[]){ " +
            "[[IOSIntArray alloc] initWithLength:4], " +
            "[[IOSIntArray alloc] initWithLength:4], " +
            "[[IOSIntArray alloc] initWithLength:4] } " +
           "count:3 type:[IOSClass classWithClass:[IOSIntArray class]]], " +
          "[IOSObjectArray arrayWithObjects:(id[]){ " +
            "[[IOSIntArray alloc] initWithLength:4], " +
            "[[IOSIntArray alloc] initWithLength:4], " +
            "[[IOSIntArray alloc] initWithLength:4] } " +
           "count:3 type:[IOSClass classWithClass:[IOSIntArray class]]] } " +
         "count:2 type:[IOSClass classWithClass:[IOSObjectArray class]]];", result);
  }
}
