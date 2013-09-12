/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

public class CharsetTest extends junit.framework.TestCase {
    public void test_guaranteedCharsetsAvailable() throws Exception {
        // All Java implementations must support these charsets.
        assertNotNull(Charset.forName("ISO-8859-1"));
        assertNotNull(Charset.forName("US-ASCII"));
        assertNotNull(Charset.forName("UTF-16"));
        assertNotNull(Charset.forName("UTF-16BE"));
        assertNotNull(Charset.forName("UTF-16LE"));
        assertNotNull(Charset.forName("UTF-8"));
    }

    public void test_allAvailableCharsets() throws Exception {
        // Check that we can instantiate every Charset, CharsetDecoder, and CharsetEncoder.
        for (String charsetName : Charset.availableCharsets().keySet()) {
            Charset cs = Charset.forName(charsetName);
            assertNotNull(cs.newDecoder());
            if (cs.canEncode()) {
                CharsetEncoder enc = cs.newEncoder();
                assertNotNull(enc);
                assertNotNull(enc.replacement());
            }
        }
    }

    public void test_UTF_16() throws Exception {
        Charset cs = Charset.forName("UTF-16");
        assertEncodes(cs, "a\u0666", 0xff, 0xfe, 'a', 0, 0x66, 0x06);
        assertDecodes(cs, "a\u0666", 0xff, 0xfe, 'a', 0, 0x66, 0x06);
    }

    public void test_UTF_32() throws Exception {
        Charset cs = Charset.forName("UTF-32");
        assertEncodes(cs, "a\u0666", -1, -2, 0, 0, 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
        assertDecodes(cs, "a\u0666", -1, -2, 0, 0, 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
    }

    public void test_preNioAliases() throws Exception {
        // Various pre-nio java.lang/java.io encoding names are translated to nio charsets.
        assertEquals("UTF-16BE", Charset.forName("UnicodeBigUnmarked").name());
        assertEquals("UTF-16LE", Charset.forName("UnicodeLittleUnmarked").name());
        assertEquals("UTF-16", Charset.forName("Unicode").name());
        assertEquals("UTF-16", Charset.forName("UnicodeBig").name());
    }

    private byte[] toByteArray(int[] ints) {
        byte[] result = new byte[ints.length];
        for (int i = 0; i < ints.length; ++i) {
            result[i] = (byte) ints[i];
        }
        return result;
    }

    private void assertEncodes(Charset cs, String s, int... expectedByteInts) throws Exception {
        ByteBuffer out = cs.encode(s);
        byte[] bytes = new byte[out.remaining()];
        out.get(bytes);
        assertEquals(Arrays.toString(toByteArray(expectedByteInts)), Arrays.toString(bytes));
    }

    private void assertDecodes(Charset cs, String s, int... byteInts) throws Exception {
        ByteBuffer in = ByteBuffer.wrap(toByteArray(byteInts));
        CharBuffer out = cs.decode(in);
        assertEquals(s, out.toString());
    }
}
