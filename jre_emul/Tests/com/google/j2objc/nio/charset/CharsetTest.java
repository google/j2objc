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

package com.google.j2objc.nio.charset;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Test encoding and decoding of J2ObjC's charset implementations.
 *
 * @author Keith Stanger
 */
public class CharsetTest extends TestCase {

  private void assertCorrectDecoding(String expected, byte[] bytes, String charsetName)
      throws IOException {
    Charset cs = Charset.forName(charsetName);
    CharsetDecoder decoder = cs.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    assertEquals(expected, decoder.decode(ByteBuffer.wrap(bytes)).toString());
    assertEquals(expected, cs.decode(ByteBuffer.wrap(bytes)).toString());
    assertEquals(expected, new String(bytes, charsetName));
    assertEquals(expected, new String(bytes, cs));
  }

  public void testDecoding() throws IOException {
    // UTF-8 with some invalid bytes.
    byte[] invalidUtf8 = {
      91, 92, -1, -40, -1, -32, 1, 16, 74, 0, 70, -27, -101, 73, 70, -28, -72, -83, -27, -101 };
    assertCorrectDecoding(
        "[\\\ufffd\ufffd\ufffd\ufffd\u0001\u0010J\0F\ufffdIF中\ufffd", invalidUtf8, "UTF-8");

    // UTF-16 with different byte order marks.
    assertCorrectDecoding("abc", new byte[] { -2, -1, 0, 97, 0, 98, 0, 99 }, "UTF-16");
    assertCorrectDecoding("abc", new byte[] { -1, -2, 97, 0, 98, 0, 99, 0 }, "UTF-16");
    assertCorrectDecoding("abc", new byte[] { 0, 97, 0, 98, 0, 99 }, "UTF-16");

    // UTF-16 with explicit endianness.
    assertCorrectDecoding("abc", new byte[] { 0, 97, 0, 98, 0, 99 }, "UTF-16BE");
    assertCorrectDecoding("\ufeffabc", new byte[] { -2, -1, 0, 97, 0, 98, 0, 99 }, "UTF-16BE");
    assertCorrectDecoding("abc", new byte[] { 97, 0, 98, 0, 99, 0 }, "UTF-16LE");
    assertCorrectDecoding("\ufeffabc", new byte[] { -1, -2, 97, 0, 98, 0, 99, 0 }, "UTF-16LE");

    // UTF-32
    assertCorrectDecoding("abc", new byte[] { 0, 0, 0, 97, 0, 0, 0, 98, 0, 0, 0, 99 }, "UTF-32");
    assertCorrectDecoding("abc", new byte[] { 0, 0, 0, 97, 0, 0, 0, 98, 0, 0, 0, 99 }, "UTF-32BE");
    assertCorrectDecoding("abc", new byte[] { 97, 0, 0, 0, 98, 0, 0, 0, 99, 0, 0, 0 }, "UTF-32LE");

    // Other encodings
    assertCorrectDecoding("abc", new byte[] { 97, 98, 99 }, "US-ASCII");
    assertCorrectDecoding("abc", new byte[] { 97, 98, 99 }, "ISO-8859-1");
    assertCorrectDecoding("abc", new byte[] { 97, 98, 99 }, "ISO-8859-2");
    assertCorrectDecoding("日本", new byte[] { -58, -4, -53, -36 }, "EUC-JP");
    assertCorrectDecoding("日本", new byte[] { -109, -6, -106, 123 }, "SHIFT_JIS");
    assertCorrectDecoding(
        "日本", new byte[] { 27, 36, 66, 70, 124, 75, 92, 27, 40, 66 }, "ISO-2022-JP");
    assertCorrectDecoding("ößŤ", new byte[] { -10, -33, -115 }, "WINDOWS-1250");
    assertCorrectDecoding("фЭЖ", new byte[] { -12, -35, -58 }, "WINDOWS-1251");
    assertCorrectDecoding("žºé", new byte[] { -98, -70, -23 }, "WINDOWS-1252");
    assertCorrectDecoding("ΔΣΨ", new byte[] { -60, -45, -40 }, "WINDOWS-1253");
    assertCorrectDecoding("Ğ¿ÿ", new byte[] { -48, -65, -1 }, "WINDOWS-1254");
    assertCorrectDecoding("√ˇà", new byte[] { -61, -1, -120 }, "X-MACROMAN");
    assertCorrectDecoding("觥秤", new byte[] { -10, -95, -77, -45 }, "GB2312");
  }

  private void assertCorrectEncoding(byte[] expected, String input, String charsetName)
      throws IOException {
    Charset cs = Charset.forName(charsetName);
    CharsetEncoder encoder = cs.newEncoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    ByteBuffer bb = encoder.encode(CharBuffer.wrap(input.toCharArray()));
    byte[] result = new byte[bb.remaining()];
    bb.get(result);
    Assert.assertArrayEquals(expected, result);
    bb = cs.encode(CharBuffer.wrap(input.toCharArray()));
    result = new byte[bb.remaining()];
    bb.get(result);
    Assert.assertArrayEquals(expected, result);
    Assert.assertArrayEquals(expected, input.getBytes(charsetName));
    Assert.assertArrayEquals(expected, input.getBytes(cs));
  }

  public void testEncoding() throws IOException {
    // UTF-16
    assertCorrectEncoding(new byte[] { -2, -1, 0, 97, 0, 98, 0, 99 }, "abc", "UTF-16");
    assertCorrectEncoding(new byte[] { 0, 97, 0, 98, 0, 99 }, "abc", "UTF-16BE");
    assertCorrectEncoding(new byte[] { 97, 0, 98, 0, 99, 0 }, "abc", "UTF-16LE");

    // UTF-32
    assertCorrectEncoding(new byte[] { 0, 0, 0, 97, 0, 0, 0, 98, 0, 0, 0, 99 }, "abc", "UTF-32");
    assertCorrectEncoding(new byte[] { 0, 0, 0, 97, 0, 0, 0, 98, 0, 0, 0, 99 }, "abc", "UTF-32BE");
    assertCorrectEncoding(new byte[] { 97, 0, 0, 0, 98, 0, 0, 0, 99, 0, 0, 0 }, "abc", "UTF-32LE");

    // Other encodings
    assertCorrectEncoding(new byte[] { 97, 98, 99 }, "abc", "US-ASCII");
    assertCorrectEncoding(new byte[] { 97, 98, 99 }, "abc", "ISO-8859-1");
    assertCorrectEncoding(new byte[] { 97, 98, 99 }, "abc", "ISO-8859-2");
    assertCorrectEncoding(new byte[] { -58, -4, -53, -36 }, "日本", "EUC-JP");
    assertCorrectEncoding(new byte[] { -109, -6, -106, 123 }, "日本", "SHIFT_JIS");
    assertCorrectEncoding(
        new byte[] { 27, 36, 66, 70, 124, 75, 92, 27, 40, 66 }, "日本", "ISO-2022-JP");
    assertCorrectEncoding(new byte[] { -10, -33, -115 }, "ößŤ", "WINDOWS-1250");
    assertCorrectEncoding(new byte[] { -12, -35, -58 }, "фЭЖ", "WINDOWS-1251");
    assertCorrectEncoding(new byte[] { -98, -70, -23 }, "žºé", "WINDOWS-1252");
    assertCorrectEncoding(new byte[] { -60, -45, -40 }, "ΔΣΨ", "WINDOWS-1253");
    assertCorrectEncoding(new byte[] { -48, -65, -1 }, "Ğ¿ÿ", "WINDOWS-1254");
    assertCorrectEncoding(new byte[] { -61, -1, -120 }, "√ˇà", "X-MACROMAN");
    assertCorrectEncoding(new byte[] { -10, -95, -77, -45 }, "觥秤", "GB2312");

    // Unmappable character
    assertCorrectEncoding(new byte[] { 97, 98, 63, 99, 100 }, "ab\uD7C5cd", "ISO-8859-1");
  }
}
