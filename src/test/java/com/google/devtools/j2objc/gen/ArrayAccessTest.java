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
 * Verifies access to elements in IOS array types.
 *
 * @author Tom Ball
 */
public class ArrayAccessTest extends GenerationTest {

  public void testGetElement() {
    List<Statement> stmts = translateStatements("int[] arr = { 1, 2 }; " +
        "int one = arr[0]; int two = arr[1];");
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSIntArray *arr = [IOSIntArray arrayWithInts:(int[]){ 1, 2 } count:2];", result);
    result = generateStatement(stmts.get(1));
    assertEquals("int one = [((IOSIntArray *) NIL_CHK(arr)) intAtIndex:0];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("int two = [((IOSIntArray *) NIL_CHK(arr)) intAtIndex:1];", result);
  }

  public void testSetElementWithLiteral() {
    List<Statement> stmts = translateStatements("int[] arr = { 1, 2 }; arr[0] = -1; arr[1] = -2;");
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("[((IOSIntArray *) NIL_CHK(arr)) replaceIntAtIndex:0 withInt:-1];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("[((IOSIntArray *) NIL_CHK(arr)) replaceIntAtIndex:1 withInt:-2];", result);
  }

  public void testSetElementWithExpression() {
    List<Statement> stmts = translateStatements("int[] arr = { 1, 2 }; " +
        "arr[0] = 2 * 5; arr[1] = 6 / 3;");
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("[((IOSIntArray *) NIL_CHK(arr)) replaceIntAtIndex:0 withInt:2 * 5];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("[((IOSIntArray *) NIL_CHK(arr)) replaceIntAtIndex:1 withInt:6 / 3];", result);
  }

  public void testPrefixOperator() {
    List<Statement> stmts = translateStatements("int[] arr = { 1, 2 }; ++arr[0]; --arr[1];");
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("[((IOSIntArray *) NIL_CHK(arr)) incr:0];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("[((IOSIntArray *) NIL_CHK(arr)) decr:1];", result);
  }

  public void testPostfixOperator() {
    List<Statement> stmts = translateStatements("int[] arr = { 1, 2 }; arr[0]++; arr[1]--;");
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("[((IOSIntArray *) NIL_CHK(arr)) postIncr:0];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("[((IOSIntArray *) NIL_CHK(arr)) postDecr:1];", result);
  }
}
