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

package com.google.j2objc;

import junit.framework.TestCase;

public class AssertTest extends TestCase {
  String evaluationResult;

  String setMessage(String msg) {
    evaluationResult = msg;
    return evaluationResult;
  }

  /**
   * Ensure that JreAssert only evaluates Expression2 when Expression1 is false, in accordance with
   * the Java Language Specification.
   */
  public void testNonEvaluationOfExpression2() throws Exception {
    assertNull(evaluationResult);

    assert true: "msg: " + setMessage("error");
    assertNull("evalutationResult should not be set", evaluationResult);

    try {
      assert false: "msg: " + setMessage("real error");
    } catch (AssertionError e) {
      assertEquals("msg: real error", e.getMessage());
    }
    assertEquals("real error", evaluationResult);

    // A case where eager evalutaion of Expression2 would crash the program.
    String x = null;
    assert x == null: "x should be null, but is a string of length " + x.length();
  }

  /**
   * Ensure that the condition and the expression passed to the JreAssert macro are wrapped with
   * parentheses in the translated code.
   */
  public void testTranslationCorrectness() throws Exception {
    assert new Integer[]{1, 2} != null;
    assert true: new Integer[]{1, 2};
  }
}
