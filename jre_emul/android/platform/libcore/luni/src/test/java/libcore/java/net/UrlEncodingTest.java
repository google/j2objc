/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.java.net;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import junit.framework.TestCase;

public final class UrlEncodingTest extends TestCase {

    public void testUriRetainsOriginalEncoding() throws Exception {
        assertEquals("%61", new URI("http://foo#%61").getRawFragment());
    }

    /**
     * URLDecoder and URI disagree on what '+' should decode to.
     */
    public void testDecodingPlus() throws Exception {
        assertEquals("a b", URLDecoder.decode("a+b"));
        assertEquals("a b", URLDecoder.decode("a+b", "UTF-8"));
        assertEquals("a+b", new URI("http://foo#a+b").getFragment());
    }

    public void testEncodingPlus() throws Exception {
        assertEquals("a%2Bb", URLEncoder.encode("a+b"));
        assertEquals("a%2Bb", URLEncoder.encode("a+b", "UTF-8"));
        assertEquals("a+b", new URI("http", "foo", "/", "a+b").getRawFragment());
    }

    public void testDecodingSpace() throws Exception {
        assertEquals("a b", URLDecoder.decode("a b"));
        assertEquals("a b", URLDecoder.decode("a b", "UTF-8"));
        try {
            new URI("http://foo#a b");
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testEncodingSpace() throws Exception {
        assertEquals("a+b", URLEncoder.encode("a b"));
        assertEquals("a+b", URLEncoder.encode("a b", "UTF-8"));
        assertEquals("a%20b", new URI("http", "foo", "/", "a b").getRawFragment());
    }

    public void testUriDecodingPartial() throws Exception {
        try {
            new URI("http://foo#%");
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http://foo#%0");
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testUrlDecoderDecodingPartial() throws Exception {
        try {
            URLDecoder.decode("%");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            URLDecoder.decode("%0");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testUriDecodingInvalid() {
        try {
            new URI("http://foo#%0g");
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testUrlDecoderDecodingInvalid() {
        try {
            URLDecoder.decode("%0g");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testUrlDecoderFailsOnNullCharset() throws Exception {
        try {
            URLDecoder.decode("ab", null);
            fail();
        } catch (IllegalCharsetNameException expected) {
        } catch (NullPointerException expected) {
        }
    }

    public void testUrlDecoderFailsOnEmptyCharset() {
        try {
            URLDecoder.decode("ab", "");
            fail();
        } catch (IllegalCharsetNameException expected) {
        } catch (UnsupportedEncodingException expected) {
        }
    }

    public void testUrlEncoderFailsOnNullCharset() throws Exception {
        try {
            URLEncoder.encode("ab", null);
            fail();
        } catch (IllegalCharsetNameException expected) {
        } catch (NullPointerException expected) {
        }
    }

    public void testUrlEncoderFailsOnEmptyCharset() {
        try {
            URLEncoder.encode("ab", "");
            fail();
        } catch (IllegalCharsetNameException expected) {
        } catch (UnsupportedEncodingException expected) {
        }
    }

    /**
     * The RI looks up the charset lazily; Android looks it up eagerly. Either
     * behavior is acceptable.
     */
    public void testUrlDecoderIgnoresUnnecessaryCharset() throws Exception {
        try {
            assertEquals("ab", URLDecoder.decode("ab", "no-such-charset"));
            // no fail()
        } catch (UnsupportedCharsetException expected) {
        }
    }

    public void testUrlEncoderFailsOnInvalidCharset() throws Exception {
        try {
            URLEncoder.encode("ab", "no-such-charset");
            fail();
        } catch (UnsupportedCharsetException expected) {
        } catch (UnsupportedEncodingException expected) {
        }
    }

    public void testDecoding() throws Exception {
        assertDecoded("a\u0000b", "a%00b");
        assertDecoded("a b", "a%20b");
        assertDecoded("a+b", "a%2bb");
        assertDecoded("a%b", "a%25b");
        assertDecoded("a\u007fb", "a%7fb");
    }

    public void testEncoding() throws Exception {
        assertEncoded("a%25b", "a%b");
        assertEncoded("a%7Fb", "a\u007fb");
    }

    public void testDecodingLiterals() throws Exception {
        assertDecoded("\ud842\udf9f", "\ud842\udf9f");
    }

    public void testDecodingBrokenUtf8SequenceYieldsReplacementCharacter() throws Exception {
        assertDecoded("a\ufffdb", "a%ffb");
    }

    public void testDecodingBrokenUtf8SequenceYieldsReplacementCharacterSequence()
            throws Exception {
        assertDecoded("a%\ufffd%b", "a%25%ff%25b");
    }

    public void testDecodingUtf8Octets() throws Exception {
        assertDecoded("\u20AC", "%e2%82%ac");
        assertDecoded("\ud842\udf9f", "%f0%a0%ae%9f");
    }

    public void testDecodingNonUsDigits() throws Exception {
        try {
            new URI("http://foo#" + "%\u0664\u0661");
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            URLDecoder.decode("%\u0664\u0661");
            fail(); // RI fails this test returning "A"
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * Android's URLEncoder.encode() failed for surrogate pairs, encoding them
     * as two replacement characters ("??"). http://b/3436051
     */
    public void testUrlEncoderEncodesNonPrintableNonAsciiCharacters() throws Exception {
        assertEquals("%00", URLEncoder.encode("\u0000", "UTF-8"));
        assertEquals("%00", URLEncoder.encode("\u0000"));
        assertEquals("%E2%82%AC", URLEncoder.encode("\u20AC", "UTF-8"));
        assertEquals("%E2%82%AC", URLEncoder.encode("\u20AC"));
        assertEquals("%F0%A0%AE%9F", URLEncoder.encode("\ud842\udf9f", "UTF-8"));
        assertEquals("%F0%A0%AE%9F", URLEncoder.encode("\ud842\udf9f"));
    }

    public void testUriDoesNotEncodeNonPrintableNonAsciiCharacters() throws Exception {
        assertEquals("\u20AC", new URI("http", "foo", "/", "\u20AC").getRawFragment());
        assertEquals("\ud842\udf9f", new URI("http", "foo", "/", "\ud842\udf9f").getRawFragment());
    }

    public void testUriEncodesControlCharacters() throws Exception {
        assertEquals("%01", new URI("http", "foo", "/", "\u0001").getRawFragment());

        // The RI fails this, encoding \u0001 but not \u0000
        assertEquals("%00", new URI("http", "foo", "/", "\u0000").getRawFragment());
    }

    public void testEncodeAndDecode() throws Exception {
        assertRoundTrip("http://jcltest.apache.org/test?hl=en&q=te st",
                "http%3A%2F%2Fjcltest.apache.org%2Ftest%3Fhl%3Den%26q%3Dte+st");
        assertRoundTrip ("file://a b/c/d.e-f*g_ l",
                "file%3A%2F%2Fa+b%2Fc%2Fd.e-f*g_+l");
        assertRoundTrip("jar:file://a.jar !/b.c/\u1052",
                "jar%3Afile%3A%2F%2Fa.jar+%21%2Fb.c%2F%E1%81%92");
        assertRoundTrip("ftp://test:pwd@localhost:2121/%D0%9C",
                "ftp%3A%2F%2Ftest%3Apwd%40localhost%3A2121%2F%25D0%259C");
    }

    /**
     * Asserts that {@code original} decodes to {@code decoded} using both URI
     * and UrlDecoder.
     */
    private void assertDecoded(String decoded, String original) throws Exception {
        assertEquals(decoded, new URI("http://foo#" + original).getFragment());
        assertEquals(decoded, URLDecoder.decode(original));
        assertEquals(decoded, URLDecoder.decode(original, "UTF-8"));
    }

    /**
     * Asserts that {@code original} encodes to {@code encoded} using both URI
     * and URLEncoder.
     */
    private void assertEncoded(String encoded, String original) throws Exception {
        assertEquals(encoded, URLEncoder.encode(original, "UTF-8"));
        assertEquals(encoded, URLEncoder.encode(original));
        assertEquals(encoded, new URI("http", "foo", "/", original).getRawFragment());
    }

    private void assertRoundTrip(String original, String encoded) throws Exception {
        assertEquals(encoded, URLEncoder.encode(original, "UTF-8"));
        assertEquals(original, URLDecoder.decode(encoded, "UTF-8"));
    }
}
