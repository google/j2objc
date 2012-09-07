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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.J2ObjC;

/**
 * Unit tests for {@link UnicodeUtils}.
 *
 * @author Tom Ball
 */
public class UnicodeUtilsTest extends GenerationTest {

  // Verify that a string with just a Unicode escape sequence is converted.
  public void testSingleUnicodeEscapeSequence() {
    String fragment = "\\u1234";
    String escaped = UnicodeUtils.escapeUnicodeSequences(fragment);
    assertEquals(fragment, escaped);
  }

  // Verify that a partial Unicode escape sequence, such as "\\u" is ignored.
  public void testUnicodeEscapeSequenceFragment() {
    String fragment = "\\u";
    String escaped = UnicodeUtils.escapeUnicodeSequences(fragment);
    assertEquals(fragment, escaped);
  }

  // Verify that a string with a Unicode escape that isn't a legal C99
  // sequence is reported as an error.
  public void testIllegalUnicodeEscapeSequence() {
    String fragment = "abc\\uffff";
    String escaped = UnicodeUtils.escapeUnicodeSequences(fragment, false);
    assertEquals(1, J2ObjC.getErrorCount());

    // Verify string wasn't modified (it's useful as a diagnostic).
    assertEquals(fragment, escaped);
  }

  public void testHasValidCppCharacters() {
    String fragment = "\u1234";
    assertTrue(UnicodeUtils.hasValidCppCharacters(fragment));
    fragment = "abcd\n\f\r123";
    assertTrue(UnicodeUtils.hasValidCppCharacters(fragment));
    fragment = "123\uffff";
    assertFalse(UnicodeUtils.hasValidCppCharacters(fragment));
  }
}
