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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Basic test to verify UTF-16 encoding matches Java's format. The iOS
 * UTF-16 encoding is correct, but is little-endian while Java's is
 * big-endian.
 *
 * @author Tom Ball
 */
public class UTF16EncodingTest extends TestCase {

  private void assertUTF16Encoding(String s, byte[] bytes) {
    int strLength = s.length();

    // Encoded length for UTF-16 is 2 bytes for each char, plus a two-byte BOM.
    int bytesLength = (strLength + 1) * 2;
    assertEquals(bytesLength, bytes.length);

    // Verify characters are encoded correctly. This test only works for ASCII
    // characters (due to the high-byte always being zero), but this test just
    // needs to verify big-endian byte order.
    int i = 0;
    assertEquals("invalid BOM", 0xFE, bytes[i++] & 0xFF);
    assertEquals("invalid BOM", 0xFF, bytes[i++] & 0xFF);
    for (int j = 0; j < strLength; j++) {
      char c = s.charAt(j);
      assertEquals(0, bytes[i++]);
      assertEquals(c, bytes[i++]);
    }
  }

  public void testStringGetBytesUTF16() throws UnsupportedEncodingException {
    String s = "hello";
    byte[] bytes = s.getBytes("UTF-16");
    assertUTF16Encoding(s, bytes);
  }

  public void testCharsetEncodingUTF16() throws UnsupportedEncodingException, IOException {
    String s = "goodbye";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(baos, "UTF-16");
    writer.write(s);
    writer.close();
    byte[] bytes = baos.toByteArray();
    assertUTF16Encoding(s, bytes);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(UTF16EncodingTest.class);
  }
}

