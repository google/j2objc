/*
 * Copyright (C) 2016 The Android Open Source Project
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
 *
 */
package libcore.java.util;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import libcore.util.HexEncoding;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.copyOfRange;

public class Base64Test extends TestCase {

    /**
     * The base 64 alphabet from RFC 4648 Table 1.
     */
    private static final Set<Character> TABLE_1 =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
            )));

    /**
     * The "URL and Filename safe" Base 64 Alphabet from RFC 4648 Table 2.
     */
    private static final Set<Character> TABLE_2 =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
            )));

    public void testAlphabet_plain() {
        checkAlphabet(TABLE_1, "", Base64.getEncoder());
    }

    public void testAlphabet_mime() {
        checkAlphabet(TABLE_1, "\r\n", Base64.getMimeEncoder());
    }

    public void testAlphabet_url() {
        checkAlphabet(TABLE_2, "", Base64.getUrlEncoder());
    }

    private static void checkAlphabet(Set<Character> expectedAlphabet, String lineSeparator,
            Encoder encoder) {
        assertEquals("Base64 alphabet size must be 64 characters", 64, expectedAlphabet.size());
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        Set<Character> actualAlphabet = new HashSet<>();

        byte[] encodedBytes = encoder.encode(bytes);
        // ignore the padding
        int endIndex = encodedBytes.length;
        while (endIndex > 0 && encodedBytes[endIndex - 1] == '=') {
            endIndex--;
        }
        for (byte b : Arrays.copyOfRange(encodedBytes, 0, endIndex)) {
            char c = (char) b;
            actualAlphabet.add(c);
        }
        for (char c : lineSeparator.toCharArray()) {
            assertTrue(actualAlphabet.remove(c));
        }
        assertEquals(expectedAlphabet, actualAlphabet);
    }

    /**
     * Checks decoding of bytes containing a value outside of the allowed
     * {@link #TABLE_1 "basic" alphabet}.
     */
    public void testDecoder_extraChars_basic() throws Exception {
        Decoder basicDecoder = Base64.getDecoder(); // uses Table 1
        // Check failure cases common to both RFC4648 Table 1 and Table 2 decoding.
        checkDecoder_extraChars_common(basicDecoder);

        // Tests characters that are part of RFC4848 Table 2 but not Table 1.
        assertDecodeThrowsIAe(basicDecoder, "_aGVsbG8sIHdvcmx");
        assertDecodeThrowsIAe(basicDecoder, "aGV_sbG8sIHdvcmx");
        assertDecodeThrowsIAe(basicDecoder, "aGVsbG8sIHdvcmx_");
    }

    /**
     * Checks decoding of bytes containing a value outside of the allowed
     * {@link #TABLE_2 url alphabet}.
     */
    public void testDecoder_extraChars_url() throws Exception {
        Decoder urlDecoder = Base64.getUrlDecoder(); // uses Table 2
        // Check failure cases common to both RFC4648 table 1 and table 2 decoding.
        checkDecoder_extraChars_common(urlDecoder);

        // Tests characters that are part of RFC4848 Table 1 but not Table 2.
        assertDecodeThrowsIAe(urlDecoder, "/aGVsbG8sIHdvcmx");
        assertDecodeThrowsIAe(urlDecoder, "aGV/sbG8sIHdvcmx");
        assertDecodeThrowsIAe(urlDecoder, "aGVsbG8sIHdvcmx/");
    }

    /**
     * Checks characters that are bad both in RFC4648 {@link #TABLE_1} and
     * in {@link #TABLE_2} based decoding.
     */
    private static void checkDecoder_extraChars_common(Decoder decoder) throws Exception {
        // Characters outside alphabet before padding.
        assertDecodeThrowsIAe(decoder, " aGVsbG8sIHdvcmx");
        assertDecodeThrowsIAe(decoder, "aGV sbG8sIHdvcmx");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmx ");
        assertDecodeThrowsIAe(decoder, "*aGVsbG8sIHdvcmx");
        assertDecodeThrowsIAe(decoder, "aGV*sbG8sIHdvcmx");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmx*");
        assertDecodeThrowsIAe(decoder, "\r\naGVsbG8sIHdvcmx");
        assertDecodeThrowsIAe(decoder, "aGV\r\nsbG8sIHdvcmx");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmx\r\n");
        assertDecodeThrowsIAe(decoder, "\naGVsbG8sIHdvcmx");
        assertDecodeThrowsIAe(decoder, "aGV\nsbG8sIHdvcmx");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmx\n");

        // padding 0
        assertEquals("hello, world", decodeToAscii(decoder, "aGVsbG8sIHdvcmxk"));
        // Extra padding
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxk=");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxk==");
        // Characters outside alphabet intermixed with (too much) padding.
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxk =");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxk = = ");

        // padding 1
        assertEquals("hello, world?!", decodeToAscii(decoder, "aGVsbG8sIHdvcmxkPyE="));
        // Missing padding
        assertEquals("hello, world?!", decodeToAscii(decoder, "aGVsbG8sIHdvcmxkPyE"));
        // Characters outside alphabet before padding.
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE =");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE*=");
        // Trailing characters, otherwise valid.
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE= ");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE=*");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE=X");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE=XY");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE=XYZ");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE=XYZA");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE=\n");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE=\r\n");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE= ");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE==");
        // Characters outside alphabet intermixed with (too much) padding.
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE ==");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkPyE = = ");

        // padding 2
        assertEquals("hello, world.", decodeToAscii(decoder, "aGVsbG8sIHdvcmxkLg=="));
        // Missing padding
        assertEquals("hello, world.", decodeToAscii(decoder, "aGVsbG8sIHdvcmxkLg"));
        // Partially missing padding
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg=");
        // Characters outside alphabet before padding.
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg ==");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg*==");
        // Trailing characters, otherwise valid.
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg== ");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg==*");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg==X");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg==XY");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg==XYZ");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg==XYZA");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg==\n");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg==\r\n");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg== ");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg===");
        // Characters outside alphabet inside padding.
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg= =");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg=*=");
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg=\r\n=");
        // Characters inside alphabet inside padding.
        assertDecodeThrowsIAe(decoder, "aGVsbG8sIHdvcmxkLg=X=");
    }

    public void testDecoder_extraChars_mime() throws Exception {
        Decoder mimeDecoder = Base64.getMimeDecoder();

        // Characters outside alphabet before padding.
        assertEquals("hello, world", decodeToAscii(mimeDecoder, " aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGV sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxk "));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "_aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGV_sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxk_"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "*aGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGV*sbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxk*"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "\r\naGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGV\r\nsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxk\r\n"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "\naGVsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGV\nsbG8sIHdvcmxk"));
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxk\n"));

        // padding 0
        assertEquals("hello, world", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxk"));
        // Extra padding
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxk=");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxk==");
        // Characters outside alphabet intermixed with (too much) padding.
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxk =");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxk = = ");

        // padding 1
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE="));
        // Missing padding
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE"));
        // Characters outside alphabet before padding.
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE ="));
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE*="));
        // Trailing characters, otherwise valid.
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE= "));
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=*"));
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=X");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=XY");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=XYZ");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=XYZA");
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=\n"));
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=\r\n"));
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE= "));
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE=="));
        // Characters outside alphabet intermixed with (too much) padding.
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE =="));
        assertEquals("hello, world?!", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkPyE = = "));

        // padding 2
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg=="));
        // Missing padding
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg"));
        // Partially missing padding
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg=");
        // Characters outside alphabet before padding.
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg =="));
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg*=="));
        // Trailing characters, otherwise valid.
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg== "));
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg==*"));
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg==X");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg==XY");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg==XYZ");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg==XYZA");
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg==\n"));
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg==\r\n"));
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg== "));
        assertEquals("hello, world.", decodeToAscii(mimeDecoder, "aGVsbG8sIHdvcmxkLg==="));

        // Characters outside alphabet inside padding are not allowed by the MIME decoder.
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg= =");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg=*=");
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg=\r\n=");

        // Characters inside alphabet inside padding.
        assertDecodeThrowsIAe(mimeDecoder, "aGVsbG8sIHdvcmxkLg=X=");
    }

    public void testDecoder_nonPrintableBytes_basic() throws Exception {
        checkDecoder_nonPrintableBytes_table1(Base64.getDecoder());
    }

    public void testDecoder_nonPrintableBytes_mime() throws Exception {
        checkDecoder_nonPrintableBytes_table1(Base64.getMimeDecoder());
    }

    /**
     * Check decoding sample non-ASCII byte[] values from a {@link #TABLE_1}
     * encoded String.
     */
    private static void checkDecoder_nonPrintableBytes_table1(Decoder decoder) throws Exception {
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 0, decoder.decode(""));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 1, decoder.decode("/w=="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 2, decoder.decode("/+4="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 3, decoder.decode("/+7d"));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 4, decoder.decode("/+7dzA=="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 5, decoder.decode("/+7dzLs="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 6, decoder.decode("/+7dzLuq"));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 7, decoder.decode("/+7dzLuqmQ=="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 8, decoder.decode("/+7dzLuqmYg="));
    }

    /**
     * Check decoding sample non-ASCII byte[] values from a {@link #TABLE_2}
     * (url safe) encoded String.
     */
    public void testDecoder_nonPrintableBytes_url() throws Exception {
        Decoder decoder = Base64.getUrlDecoder();
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 0, decoder.decode(""));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 1, decoder.decode("_w=="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 2, decoder.decode("_-4="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 3, decoder.decode("_-7d"));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 4, decoder.decode("_-7dzA=="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 5, decoder.decode("_-7dzLs="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 6, decoder.decode("_-7dzLuq"));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 7, decoder.decode("_-7dzLuqmQ=="));
        assertArrayPrefixEquals(SAMPLE_NON_ASCII_BYTES, 8, decoder.decode("_-7dzLuqmYg="));
    }

    private static final byte[] SAMPLE_NON_ASCII_BYTES = { (byte) 0xff, (byte) 0xee, (byte) 0xdd,
            (byte) 0xcc, (byte) 0xbb, (byte) 0xaa,
            (byte) 0x99, (byte) 0x88, (byte) 0x77 };

    public void testDecoder_closedStream() {
        try {
            closedDecodeStream().available();
            fail("Should have thrown");
        } catch (IOException expected) {
        }
        try {
            closedDecodeStream().read();
            fail("Should have thrown");
        } catch (IOException expected) {
        }
        try {
            closedDecodeStream().read(new byte[23]);
            fail("Should have thrown");
        } catch (IOException expected) {
        }

        try {
            closedDecodeStream().read(new byte[23], 0, 1);
            fail("Should have thrown");
        } catch (IOException expected) {
        }
    }

    private static InputStream closedDecodeStream() {
        InputStream result = Base64.getDecoder().wrap(new ByteArrayInputStream(new byte[0]));
        try {
            result.close();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return result;
    }

    /**
     * Tests {@link Decoder#decode(byte[], byte[])} for correctness as well as
     * for consistency with other methods tested elsewhere.
     */
    public void testDecoder_decodeArrayToArray() {
        Decoder decoder = Base64.getDecoder();

        // Empty input
        assertEquals(0, decoder.decode(new byte[0], new byte[0]));

        // Test data for non-empty input
        String inputString = "YWJjZWZnaGk=";
        byte[] input = inputString.getBytes(US_ASCII);
        String expectedString = "abcefghi";
        byte[] decodedBytes = expectedString.getBytes(US_ASCII);
        // check test data consistency with other methods that are tested elsewhere
        assertRoundTrip(Base64.getEncoder(), decoder, inputString, decodedBytes);

        // Non-empty input: output array too short
        byte[] tooShort = new byte[decodedBytes.length - 1];
        try {
            decoder.decode(input, tooShort);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        // Non-empty input: output array longer than required
        byte[] tooLong = new byte[decodedBytes.length + 1];
        int tooLongBytesDecoded = decoder.decode(input, tooLong);
        assertEquals(decodedBytes.length, tooLongBytesDecoded);
        assertEquals(0, tooLong[tooLong.length - 1]);
        assertArrayPrefixEquals(tooLong, decodedBytes.length, decodedBytes);

        // Non-empty input: output array has exact minimum required size
        byte[] justRight = new byte[decodedBytes.length];
        int justRightBytesDecoded = decoder.decode(input, justRight);
        assertEquals(decodedBytes.length, justRightBytesDecoded);
        assertArrayEquals(decodedBytes, justRight);

    }

    public void testDecoder_decodeByteBuffer() {
        Decoder decoder = Base64.getDecoder();

        byte[] emptyByteArray = new byte[0];
        ByteBuffer emptyByteBuffer = ByteBuffer.wrap(emptyByteArray);
        ByteBuffer emptyDecodedBuffer = decoder.decode(emptyByteBuffer);
        assertEquals(emptyByteBuffer, emptyDecodedBuffer);
        assertNotSame(emptyByteArray, emptyDecodedBuffer);

        // Test the two types of byte buffer.
        String inputString = "YWJjZWZnaGk=";
        byte[] input = inputString.getBytes(US_ASCII);
        String expectedString = "abcefghi";
        byte[] expectedBytes = expectedString.getBytes(US_ASCII);

        ByteBuffer inputBuffer = ByteBuffer.allocate(input.length);
        inputBuffer.put(input);
        inputBuffer.position(0);
        checkDecoder_decodeByteBuffer(decoder, inputBuffer, expectedBytes);

        inputBuffer = ByteBuffer.allocateDirect(input.length);
        inputBuffer.put(input);
        inputBuffer.position(0);
        checkDecoder_decodeByteBuffer(decoder, inputBuffer, expectedBytes);
    }

    private static void checkDecoder_decodeByteBuffer(
            Decoder decoder, ByteBuffer inputBuffer, byte[] expectedBytes) {
        assertEquals(0, inputBuffer.position());
        assertEquals(inputBuffer.remaining(), inputBuffer.limit());
        int inputLength = inputBuffer.remaining();

        ByteBuffer decodedBuffer = decoder.decode(inputBuffer);

        assertEquals(inputLength, inputBuffer.position());
        assertEquals(0, inputBuffer.remaining());
        assertEquals(inputLength, inputBuffer.limit());
        assertEquals(0, decodedBuffer.position());
        assertEquals(expectedBytes.length, decodedBuffer.remaining());
        assertEquals(expectedBytes.length, decodedBuffer.limit());
    }

    public void testDecoder_decodeByteBuffer_invalidData() {
        Decoder decoder = Base64.getDecoder();

        // Test the two types of byte buffer.
        String inputString = "AAAA AAAA";
        byte[] input = inputString.getBytes(US_ASCII);

        ByteBuffer inputBuffer = ByteBuffer.allocate(input.length);
        inputBuffer.put(input);
        inputBuffer.position(0);
        checkDecoder_decodeByteBuffer_invalidData(decoder, inputBuffer);

        inputBuffer = ByteBuffer.allocateDirect(input.length);
        inputBuffer.put(input);
        inputBuffer.position(0);
        checkDecoder_decodeByteBuffer_invalidData(decoder, inputBuffer);
    }

    private static void checkDecoder_decodeByteBuffer_invalidData(
            Decoder decoder, ByteBuffer inputBuffer) {
        assertEquals(0, inputBuffer.position());
        assertEquals(inputBuffer.remaining(), inputBuffer.limit());
        int limit = inputBuffer.limit();

        try {
            decoder.decode(inputBuffer);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        assertEquals(0, inputBuffer.position());
        assertEquals(limit, inputBuffer.remaining());
        assertEquals(limit, inputBuffer.limit());
    }

    public void testDecoder_nullArgs() {
        checkDecoder_nullArgs(Base64.getDecoder());
        checkDecoder_nullArgs(Base64.getMimeDecoder());
        checkDecoder_nullArgs(Base64.getUrlDecoder());
    }

    private static void checkDecoder_nullArgs(Decoder decoder) {
        assertThrowsNpe(() -> decoder.decode((byte[]) null));
        assertThrowsNpe(() -> decoder.decode((String) null));
        assertThrowsNpe(() -> decoder.decode(null, null));
        assertThrowsNpe(() -> decoder.decode((ByteBuffer) null));
        assertThrowsNpe(() -> decoder.wrap(null));
    }

    public void testEncoder_nullArgs() {
        checkEncoder_nullArgs(Base64.getEncoder());
        checkEncoder_nullArgs(Base64.getMimeEncoder());
        checkEncoder_nullArgs(Base64.getUrlEncoder());
        checkEncoder_nullArgs(Base64.getMimeEncoder(20, new byte[] { '*' }));
        checkEncoder_nullArgs(Base64.getEncoder().withoutPadding());
        checkEncoder_nullArgs(Base64.getMimeEncoder().withoutPadding());
        checkEncoder_nullArgs(Base64.getUrlEncoder().withoutPadding());
        checkEncoder_nullArgs(Base64.getMimeEncoder(20, new byte[] { '*' }).withoutPadding());

    }

    private static void checkEncoder_nullArgs(Encoder encoder) {
        assertThrowsNpe(() -> encoder.encode((byte[]) null));
        assertThrowsNpe(() -> encoder.encodeToString(null));
        assertThrowsNpe(() -> encoder.encode(null, null));
        assertThrowsNpe(() -> encoder.encode((ByteBuffer) null));
        assertThrowsNpe(() -> encoder.wrap(null));
    }

    public void testEncoder_nonPrintableBytes() throws Exception {
        Encoder encoder = Base64.getUrlEncoder();
        assertEquals("", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 0)));
        assertEquals("_w==", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 1)));
        assertEquals("_-4=", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 2)));
        assertEquals("_-7d", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 3)));
        assertEquals("_-7dzA==", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 4)));
        assertEquals("_-7dzLs=", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 5)));
        assertEquals("_-7dzLuq", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 6)));
        assertEquals("_-7dzLuqmQ==", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 7)));
        assertEquals("_-7dzLuqmYg=", encoder.encodeToString(copyOfRange(SAMPLE_NON_ASCII_BYTES, 0, 8)));
    }

    public void testEncoder_lineLength() throws Exception {
        String in_56 = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcd";
        String in_57 = in_56 + "e";
        String in_58 = in_56 + "ef";
        String in_59 = in_56 + "efg";
        String in_60 = in_56 + "efgh";
        String in_61 = in_56 + "efghi";

        String prefix = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXphYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5emFi";
        String out_56 = prefix + "Y2Q=";
        String out_57 = prefix + "Y2Rl";
        String out_58 = prefix + "Y2Rl\r\nZg==";
        String out_59 = prefix + "Y2Rl\r\nZmc=";
        String out_60 = prefix + "Y2Rl\r\nZmdo";
        String out_61 = prefix + "Y2Rl\r\nZmdoaQ==";

        Encoder encoder = Base64.getMimeEncoder();
        Decoder decoder = Base64.getMimeDecoder();
        assertEquals("", encodeFromAscii(encoder, decoder, ""));
        assertEquals(out_56, encodeFromAscii(encoder, decoder, in_56));
        assertEquals(out_57, encodeFromAscii(encoder, decoder, in_57));
        assertEquals(out_58, encodeFromAscii(encoder, decoder, in_58));
        assertEquals(out_59, encodeFromAscii(encoder, decoder, in_59));
        assertEquals(out_60, encodeFromAscii(encoder, decoder, in_60));
        assertEquals(out_61, encodeFromAscii(encoder, decoder, in_61));

        encoder = Base64.getUrlEncoder();
        decoder = Base64.getUrlDecoder();
        assertEquals(out_56.replaceAll("\r\n", ""), encodeFromAscii(encoder, decoder, in_56));
        assertEquals(out_57.replaceAll("\r\n", ""), encodeFromAscii(encoder, decoder, in_57));
        assertEquals(out_58.replaceAll("\r\n", ""), encodeFromAscii(encoder, decoder, in_58));
        assertEquals(out_59.replaceAll("\r\n", ""), encodeFromAscii(encoder, decoder, in_59));
        assertEquals(out_60.replaceAll("\r\n", ""), encodeFromAscii(encoder, decoder, in_60));
        assertEquals(out_61.replaceAll("\r\n", ""), encodeFromAscii(encoder, decoder, in_61));
    }

    public void testGetMimeEncoder_invalidLineSeparator() {
        byte[] invalidLineSeparator = { 'A' };
        try {
            Base64.getMimeEncoder(20, invalidLineSeparator);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            Base64.getMimeEncoder(0, invalidLineSeparator);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            Base64.getMimeEncoder(20, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Base64.getMimeEncoder(0, null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testEncoder_closedStream() {
        try {
            closedEncodeStream().write(100);
            fail("Should have thrown");
        } catch (IOException expected) {
        }
        try {
            closedEncodeStream().write(new byte[100]);
            fail("Should have thrown");
        } catch (IOException expected) {
        }

        try {
            closedEncodeStream().write(new byte[100], 0, 1);
            fail("Should have thrown");
        } catch (IOException expected) {
        }
    }

    private static OutputStream closedEncodeStream() {
        OutputStream result = Base64.getEncoder().wrap(new ByteArrayOutputStream());
        try {
            result.close();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return result;
    }


    /**
     * Tests {@link Decoder#decode(byte[], byte[])} for correctness.
     */
    public void testEncoder_encodeArrayToArray() {
        Encoder encoder = Base64.getEncoder();

        // Empty input
        assertEquals(0, encoder.encode(new byte[0], new byte[0]));

        // Test data for non-empty input
        byte[] input = "abcefghi".getBytes(US_ASCII);
        String expectedString = "YWJjZWZnaGk=";
        byte[] encodedBytes = expectedString.getBytes(US_ASCII);

        // Non-empty input: output array too short
        byte[] tooShort = new byte[encodedBytes.length - 1];
        try {
            encoder.encode(input, tooShort);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        // Non-empty input: output array longer than required
        byte[] tooLong = new byte[encodedBytes.length + 1];
        int tooLongBytesEncoded = encoder.encode(input, tooLong);
        assertEquals(encodedBytes.length, tooLongBytesEncoded);
        assertEquals(0, tooLong[tooLong.length - 1]);
        assertArrayPrefixEquals(tooLong, encodedBytes.length, encodedBytes);

        // Non-empty input: output array has exact minimum required size
        byte[] justRight = new byte[encodedBytes.length];
        int justRightBytesEncoded = encoder.encode(input, justRight);
        assertEquals(encodedBytes.length, justRightBytesEncoded);
        assertArrayEquals(encodedBytes, justRight);
    }

    public void testEncoder_encodeByteBuffer() {
        Encoder encoder = Base64.getEncoder();

        byte[] emptyByteArray = new byte[0];
        ByteBuffer emptyByteBuffer = ByteBuffer.wrap(emptyByteArray);
        ByteBuffer emptyEncodedBuffer = encoder.encode(emptyByteBuffer);
        assertEquals(emptyByteBuffer, emptyEncodedBuffer);
        assertNotSame(emptyByteArray, emptyEncodedBuffer);

        // Test the two types of byte buffer.
        byte[] input = "abcefghi".getBytes(US_ASCII);
        String expectedString = "YWJjZWZnaGk=";
        byte[] expectedBytes = expectedString.getBytes(US_ASCII);

        ByteBuffer inputBuffer = ByteBuffer.allocate(input.length);
        inputBuffer.put(input);
        inputBuffer.position(0);
        testEncoder_encodeByteBuffer(encoder, inputBuffer, expectedBytes);

        inputBuffer = ByteBuffer.allocateDirect(input.length);
        inputBuffer.put(input);
        inputBuffer.position(0);
        testEncoder_encodeByteBuffer(encoder, inputBuffer, expectedBytes);
    }

    private static void testEncoder_encodeByteBuffer(
            Encoder encoder, ByteBuffer inputBuffer, byte[] expectedBytes) {
        assertEquals(0, inputBuffer.position());
        assertEquals(inputBuffer.remaining(), inputBuffer.limit());
        int inputLength = inputBuffer.remaining();

        ByteBuffer encodedBuffer = encoder.encode(inputBuffer);

        assertEquals(inputLength, inputBuffer.position());
        assertEquals(0, inputBuffer.remaining());
        assertEquals(inputLength, inputBuffer.limit());
        assertEquals(0, encodedBuffer.position());
        assertEquals(expectedBytes.length, encodedBuffer.remaining());
        assertEquals(expectedBytes.length, encodedBuffer.limit());
    }

    /**
     * Checks that all encoders/decoders map {@code new byte[0]} to "" and vice versa.
     */
    public void testRoundTrip_empty() {
        checkRoundTrip_empty(Base64.getEncoder(), Base64.getDecoder());
        checkRoundTrip_empty(Base64.getMimeEncoder(), Base64.getMimeDecoder());
        byte[] sep = new byte[] { '\r', '\n' };
        checkRoundTrip_empty(Base64.getMimeEncoder(-1, sep), Base64.getMimeDecoder());
        checkRoundTrip_empty(Base64.getMimeEncoder(20, new byte[0]), Base64.getMimeDecoder());
        checkRoundTrip_empty(Base64.getMimeEncoder(23, sep), Base64.getMimeDecoder());
        checkRoundTrip_empty(Base64.getMimeEncoder(76, sep), Base64.getMimeDecoder());
        checkRoundTrip_empty(Base64.getUrlEncoder(), Base64.getUrlDecoder());
    }

    private static void checkRoundTrip_empty(Encoder encoder, Decoder decoder) {
        assertRoundTrip(encoder, decoder, "", new byte[0]);
    }

    /**
     * Encoding of byte values 0..255 using the non-URL alphabet.
     */
    private static final String ALL_BYTE_VALUES_ENCODED =
            "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4" +
                    "OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3Bx" +
                    "cnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmq" +
                    "q6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj" +
                    "5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==";

    public void testRoundTrip_allBytes_plain() {
        checkRoundTrip_allBytes_singleLine(Base64.getEncoder(), Base64.getDecoder());
    }

    /**
     * Checks that if the lineSeparator is empty or the line length is {@code <= 3}
     * or larger than the data to be encoded, a single line is returned.
     */
    public void testRoundTrip_allBytes_mime_singleLine() {
        Decoder decoder = Base64.getMimeDecoder();
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(76, new byte[0]), decoder);

        // Line lengths <= 3 mean no wrapping; the separator is ignored in that case.
        byte[] separator = new byte[] { '*' };
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(Integer.MIN_VALUE, separator),
                decoder);
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(-1, separator), decoder);
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(0, separator), decoder);
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(1, separator), decoder);
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(2, separator), decoder);
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(3, separator), decoder);

        // output fits into the permitted line length
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(
                ALL_BYTE_VALUES_ENCODED.length(), separator), decoder);
        checkRoundTrip_allBytes_singleLine(Base64.getMimeEncoder(Integer.MAX_VALUE, separator),
                decoder);
    }

    /**
     * Checks round-trip encoding/decoding for a few simple examples that
     * should work the same across three Encoder/Decoder pairs: This is
     * because they only use characters that are in both RFC 4648 Table 1
     * and Table 2, and are short enough to fit into a single line.
     */
    public void testRoundTrip_simple_basic() throws Exception {
        // uses Table 1, never adds linebreaks
        checkRoundTrip_simple(Base64.getEncoder(), Base64.getDecoder());
        // uses Table 1, allows 76 chars in a line
        checkRoundTrip_simple(Base64.getMimeEncoder(), Base64.getMimeDecoder());
        // uses Table 2, never adds linebreaks
        checkRoundTrip_simple(Base64.getUrlEncoder(), Base64.getUrlDecoder());
    }

    private static void checkRoundTrip_simple(Encoder encoder, Decoder decoder) throws Exception {
        assertRoundTrip(encoder, decoder, "YQ==", "a".getBytes(US_ASCII));
        assertRoundTrip(encoder, decoder, "YWI=", "ab".getBytes(US_ASCII));
        assertRoundTrip(encoder, decoder, "YWJj", "abc".getBytes(US_ASCII));
        assertRoundTrip(encoder, decoder, "YWJjZA==", "abcd".getBytes(US_ASCII));
    }

    /** check a range of possible line lengths */
    public void testRoundTrip_allBytes_mime_lineLength() {
        Decoder decoder = Base64.getMimeDecoder();
        byte[] separator = new byte[] { '*' };
        checkRoundTrip_allBytes(Base64.getMimeEncoder(4, separator), decoder,
                wrapLines("*", ALL_BYTE_VALUES_ENCODED, 4));
        checkRoundTrip_allBytes(Base64.getMimeEncoder(8, separator), decoder,
                wrapLines("*", ALL_BYTE_VALUES_ENCODED, 8));
        checkRoundTrip_allBytes(Base64.getMimeEncoder(20, separator), decoder,
                wrapLines("*", ALL_BYTE_VALUES_ENCODED, 20));
        checkRoundTrip_allBytes(Base64.getMimeEncoder(100, separator), decoder,
                wrapLines("*", ALL_BYTE_VALUES_ENCODED, 100));
        checkRoundTrip_allBytes(Base64.getMimeEncoder(Integer.MAX_VALUE & ~3, separator), decoder,
                wrapLines("*", ALL_BYTE_VALUES_ENCODED, Integer.MAX_VALUE & ~3));
    }

    public void testRoundTrip_allBytes_mime_lineLength_defaultsTo76Chars() {
        checkRoundTrip_allBytes(Base64.getMimeEncoder(), Base64.getMimeDecoder(),
                wrapLines("\r\n", ALL_BYTE_VALUES_ENCODED, 76));
    }

    /**
     * checks that the specified line length is rounded down to the nearest multiple of 4.
     */
    public void testRoundTrip_allBytes_mime_lineLength_isRoundedDown() {
        Decoder decoder = Base64.getMimeDecoder();
        byte[] separator = new byte[] { '\r', '\n' };
        checkRoundTrip_allBytes(Base64.getMimeEncoder(60, separator), decoder,
                wrapLines("\r\n", ALL_BYTE_VALUES_ENCODED, 60));
        checkRoundTrip_allBytes(Base64.getMimeEncoder(63, separator), decoder,
                wrapLines("\r\n", ALL_BYTE_VALUES_ENCODED, 60));
        checkRoundTrip_allBytes(Base64.getMimeEncoder(10, separator), decoder,
                wrapLines("\r\n", ALL_BYTE_VALUES_ENCODED, 8));
    }

    public void testRoundTrip_allBytes_url() {
        String encodedUrl = ALL_BYTE_VALUES_ENCODED.replace('+', '-').replace('/', '_');
        checkRoundTrip_allBytes(Base64.getUrlEncoder(), Base64.getUrlDecoder(), encodedUrl);
    }

    /**
     * Checks round-trip encoding/decoding of all byte values 0..255 for
     * the case where the Encoder doesn't add any linebreaks.
     */
    private static void checkRoundTrip_allBytes_singleLine(Encoder encoder, Decoder decoder) {
        checkRoundTrip_allBytes(encoder, decoder, ALL_BYTE_VALUES_ENCODED);
    }

    /**
     * Checks that byte values 0..255, in order, are encoded to exactly
     * the given String (including any linebreaks, if present) and that
     * that String can be decoded back to the same byte values.
     *
     * @param encoded the expected encoded representation of the (unsigned)
     *        byte values 0..255, in order.
     */
    private static void checkRoundTrip_allBytes(Encoder encoder, Decoder decoder, String encoded) {
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        assertRoundTrip(encoder, decoder, encoded, bytes);
    }

    public void testRoundTrip_variousSizes_plain() {
        checkRoundTrip_variousSizes(Base64.getEncoder(), Base64.getDecoder());
    }

    public void testRoundTrip_variousSizes_mime() {
        checkRoundTrip_variousSizes(Base64.getMimeEncoder(), Base64.getMimeDecoder());
    }

    public void testRoundTrip_variousSizes_url() {
        checkRoundTrip_variousSizes(Base64.getUrlEncoder(), Base64.getUrlDecoder());
    }

    /**
     * Checks that various-sized inputs survive a round trip.
     */
    private static void checkRoundTrip_variousSizes(Encoder encoder, Decoder decoder) {
        Random random = new Random(7654321);
        for (int numBytes : new int [] { 0, 1, 2, 75, 76, 77, 80, 100, 1234 }) {
            byte[] bytes = new byte[numBytes];
            random.nextBytes(bytes);
            byte[] result = decoder.decode(encoder.encode(bytes));
            assertArrayEquals(bytes, result);
        }
    }

    public void testRoundtrip_wrap_basic() throws Exception {
        Encoder encoder = Base64.getEncoder();
        Decoder decoder = Base64.getDecoder();
        checkRoundTrip_wrapInputStream(encoder, decoder);
    }

    public void testRoundtrip_wrap_mime() throws Exception {
        Encoder encoder = Base64.getMimeEncoder();
        Decoder decoder = Base64.getMimeDecoder();
        checkRoundTrip_wrapInputStream(encoder, decoder);
    }

    public void testRoundTrip_wrap_url() throws Exception {
        Encoder encoder = Base64.getUrlEncoder();
        Decoder decoder = Base64.getUrlDecoder();
        checkRoundTrip_wrapInputStream(encoder, decoder);
    }

    /**
     * Checks that the {@link Decoder#wrap(InputStream) wrapping} an
     * InputStream of encoded data yields the plain data that was
     * previously {@link Encoder#encode(byte[]) encoded}.
     */
    private static void checkRoundTrip_wrapInputStream(Encoder encoder, Decoder decoder)
            throws IOException {
        Random random = new Random(32176L);
        int[] writeLengths = { -10, -5, -1, 0, 1, 1, 2, 2, 3, 10, 100 };

        // Test input needs to be at least 2048 bytes to fill up the
        // read buffer of Base64InputStream.
        byte[] plain = new byte[4567];
        random.nextBytes(plain);
        byte[] encoded = encoder.encode(plain);
        byte[] actual = new byte[plain.length * 2];
        int b;

        // ----- test decoding ("encoded" -> "plain") -----

        // read as much as it will give us in one chunk
        ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
        InputStream b64is = decoder.wrap(bais);
        int ap = 0;
        while ((b = b64is.read(actual, ap, actual.length - ap)) != -1) {
            ap += b;
        }
        assertArrayPrefixEquals(actual, ap, plain);

        // read individual bytes
        bais = new ByteArrayInputStream(encoded);
        b64is = decoder.wrap(bais);
        ap = 0;
        while ((b = b64is.read()) != -1) {
            actual[ap++] = (byte) b;
        }
        assertArrayPrefixEquals(actual, ap, plain);

        // mix reads of variously-sized arrays with one-byte reads
        bais = new ByteArrayInputStream(encoded);
        b64is = decoder.wrap(bais);
        ap = 0;
        while (true) {
            int l = writeLengths[random.nextInt(writeLengths.length)];
            if (l >= 0) {
                b = b64is.read(actual, ap, l);
                if (b == -1) {
                    break;
                }
                ap += b;
            } else {
                for (int i = 0; i < -l; ++i) {
                    if ((b = b64is.read()) == -1) {
                        break;
                    }
                    actual[ap++] = (byte) b;
                }
            }
        }
        assertArrayPrefixEquals(actual, ap, plain);
    }

    public void testDecoder_wrap_singleByteReads() throws IOException {
        InputStream in = Base64.getDecoder().wrap(new ByteArrayInputStream("/v8=".getBytes()));
        assertEquals(254, in.read());
        assertEquals(255, in.read());
        assertEquals(-1, in.read());
    }

    public void testEncoder_withoutPadding() {
        byte[] bytes = new byte[] { (byte) 0xFE, (byte) 0xFF };
        assertEquals("/v8=", Base64.getEncoder().encodeToString(bytes));
        assertEquals("/v8", Base64.getEncoder().withoutPadding().encodeToString(bytes));

        assertEquals("/v8=", Base64.getMimeEncoder().encodeToString(bytes));
        assertEquals("/v8", Base64.getMimeEncoder().withoutPadding().encodeToString(bytes));

        assertEquals("_v8=", Base64.getUrlEncoder().encodeToString(bytes));
        assertEquals("_v8", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    public void testEncoder_wrap_plain() throws Exception {
        checkWrapOutputStreamConsistentWithEncode(Base64.getEncoder());
    }

    public void testEncoder_wrap_url() throws Exception {
        checkWrapOutputStreamConsistentWithEncode(Base64.getUrlEncoder());
    }

    public void testEncoder_wrap_mime() throws Exception {
        checkWrapOutputStreamConsistentWithEncode(Base64.getMimeEncoder());
    }

    /** A way of writing bytes to an OutputStream. */
    interface WriteStrategy {
        void write(byte[] bytes, OutputStream out) throws IOException;
    }

    private static void checkWrapOutputStreamConsistentWithEncode(Encoder encoder)
            throws Exception {
        final Random random = new Random(32176L);

        // one large write(byte[]) of the whole input
        WriteStrategy allAtOnce = (bytes, out) -> out.write(bytes);
        checkWrapOutputStreamConsistentWithEncode(encoder, allAtOnce);

        // many calls to write(int)
        WriteStrategy byteWise = (bytes, out) -> {
            for (byte b : bytes) {
                out.write(b);
            }
        };
        checkWrapOutputStreamConsistentWithEncode(encoder, byteWise);

        // intermixed sequences of write(int) with
        // write(byte[],int,int) of various lengths.
        WriteStrategy mixed = (bytes, out) -> {
            int[] writeLengths = { -10, -5, -1, 0, 1, 1, 2, 2, 3, 10, 100 };
            int p = 0;
            while (p < bytes.length) {
                int l = writeLengths[random.nextInt(writeLengths.length)];
                l = Math.min(l, bytes.length - p);
                if (l >= 0) {
                    out.write(bytes, p, l);
                    p += l;
                } else {
                    l = Math.min(-l, bytes.length - p);
                    for (int i = 0; i < l; ++i) {
                        out.write(bytes[p + i]);
                    }
                    p += l;
                }
            }
        };
        checkWrapOutputStreamConsistentWithEncode(encoder, mixed);
    }

    /**
     * Checks that writing to a wrap()ping OutputStream produces the same
     * output on the wrapped stream as {@link Encoder#encode(byte[])}.
     */
    private static void checkWrapOutputStreamConsistentWithEncode(Encoder encoder,
            WriteStrategy writeStrategy) throws IOException {
        Random random = new Random(32176L);
        // Test input needs to be at least 1024 bytes to test filling
        // up the write(int) buffer of Base64OutputStream.
        byte[] plain = new byte[1234];
        random.nextBytes(plain);
        byte[] encodeResult = encoder.encode(plain);
        ByteArrayOutputStream wrappedOutputStream = new ByteArrayOutputStream();
        try (OutputStream plainOutputStream = encoder.wrap(wrappedOutputStream)) {
            writeStrategy.write(plain, plainOutputStream);
        }
        assertArrayEquals(encodeResult, wrappedOutputStream.toByteArray());
    }

    /** Decodes a string, returning the resulting bytes interpreted as an ASCII String. */
    private static String decodeToAscii(Decoder decoder, String encoded) throws Exception {
        byte[] plain = decoder.decode(encoded);
        return new String(plain, US_ASCII);
    }

    /**
     * Checks round-trip encoding/decoding of {@code plain}.
     *
     * @param plain an ASCII String
     * @return the Base64-encoded value of the ASCII codepoints from {@code plain}
     */
    private static String encodeFromAscii(Encoder encoder, Decoder decoder, String plain)
            throws Exception {
        String encoded = encoder.encodeToString(plain.getBytes(US_ASCII));
        String decoded = decodeToAscii(decoder, encoded);
        assertEquals(plain, decoded);
        return encoded;
    }

    /**
     * Rewraps {@code s} by inserting {@lineSeparator} every {@code lineLength} characters,
     * but not at the end.
     */
    private static String wrapLines(String lineSeparator, String s, int lineLength) {
        return String.join(lineSeparator, breakLines(s, lineLength));
    }

    /**
     * Splits {@code s} into a list of substrings, each except possibly the last one
     * exactly {@code lineLength} characters long.
     */
    private static List<String> breakLines(String longString, int lineLength) {
        List<String> lines = new ArrayList<>();
        for (int pos = 0; pos < longString.length(); pos += lineLength) {
            lines.add(longString.substring(pos, Math.min(longString.length(), pos + lineLength)));
        }
        return lines;
    }

    /** Assert that decoding the specific String throws IllegalArgumentException. */
    private static void assertDecodeThrowsIAe(Decoder decoder, String invalidEncoded)
            throws Exception {
        try {
            decoder.decode(invalidEncoded);
            fail("should have failed to decode");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Asserts that the given String decodes to the bytes, and that the bytes encode
     * to the given String.
     */
    private static void assertRoundTrip(Encoder encoder, Decoder decoder, String encoded,
            byte[] bytes) {
        assertEquals(encoded, encoder.encodeToString(bytes));
        assertArrayEquals(bytes, decoder.decode(encoded));
    }

    /** Asserts that actual equals the first len bytes of expected. */
    private static void assertArrayPrefixEquals(byte[] expected, int len, byte[] actual) {
        assertArrayEquals(copyOfRange(expected, 0, len), actual);
    }

    /** Checks array contents. */
    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            fail("Expected " + HexEncoding.encodeToString(expected)
                    + ", got " + HexEncoding.encodeToString(actual));
        }
    }

    private static void assertThrowsNpe(Runnable runnable) {
        try {
            runnable.run();
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

}
