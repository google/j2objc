/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import junit.framework.TestCase;
import static libcore.util.HexEncoding.decode;
import static libcore.util.HexEncoding.encode;

public class HexEncodingTest extends TestCase {
  public void testEncode() {
    final byte[] avocados = "avocados".getBytes(StandardCharsets.UTF_8);

    assertArraysEqual("61766F6361646F73".toCharArray(), encode(avocados));
    assertArraysEqual(avocados, decode(encode(avocados), false));
    // Make sure we can handle lower case hex encodings as well.
    assertArraysEqual(avocados, decode("61766f6361646f73".toCharArray(), false));
  }

  public void testDecode_allow4Bit() {
    assertArraysEqual(new byte[] { 6 }, decode("6".toCharArray(), true));
    assertArraysEqual(new byte[] { 6, 'v' }, decode("676".toCharArray(), true));
  }

  public void testDecode_disallow4Bit() {
    try {
      decode("676".toCharArray(), false);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testDecode_invalid() {
    try {
      decode("DEADBARD".toCharArray(), false);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    // This demonstrates a difference in behaviour from apache commons : apache
    // commons uses Character.isDigit and would successfully decode a string with
    // arabic and devanagari characters.
    try {
      decode("६१٧٥٥F6361646F73".toCharArray(), false);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      decode("#%6361646F73".toCharArray(), false);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  private static void assertArraysEqual(char[] lhs, char[] rhs) {
    assertEquals(new String(lhs), new String(rhs));
  }

  private static void assertArraysEqual(byte[] lhs, byte[] rhs) {
    assertEquals(Arrays.toString(lhs), Arrays.toString(rhs));
  }
}
