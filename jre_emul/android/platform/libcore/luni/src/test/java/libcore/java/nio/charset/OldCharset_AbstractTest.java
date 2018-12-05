/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import junit.framework.TestCase;

/**
 * Super class for concrete charset test suites.
 */
public abstract class OldCharset_AbstractTest extends TestCase {

    static String charsetName;
    static private Charset charset;
    static CharsetDecoder decoder;
    static CharsetEncoder encoder;

    static final int[] codes = Charset_TestGenerator.codes;

    static final char[] chars = new char[codes.length]; // Is filled with
                                                        // contents of codes.

    static char[] testChars;
    static byte[] testBytes;

    static char[] theseChars (int... codes) {
        char[] chars = new char[codes.length];
        for (int i = 0; i < codes.length; i++) chars[i] = (char) codes[i];
        return chars;
    }

    static byte[] theseBytes (int... codes) {
        byte[] bytes = new byte[codes.length];
        for (int i = 0; i < codes.length; i++) bytes[i] = (byte) codes[i];
        return bytes;
    }


    @Override
    protected void setUp() throws Exception {
        charset = charset.forName(charsetName);
        decoder = charset.newDecoder();
        encoder = charset.newEncoder();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test_nameMatch () {
        assertEquals("Name of charset must match!", charsetName, charset.name());
    }

    public void test_dumpEncodableChars () {
        if (testChars == null) return;
        if (testChars.length > 0) return;
        System.out.format("\ntest_dumpEncodableChars() for name %s => %s (class = %s)\n",
                charsetName, charset.name(), getClass().getName());
        Charset_TestGenerator.Dumper out = new Charset_TestGenerator.Dumper1(16);
        int code = 0;
        while (code < 256) {
            while (!encoder.canEncode((char) code)) code ++;
            if (code < 65536) {
                out.consume(code);
                code += 1;
            }
        }
        while (code < 65536) {
            while (!encoder.canEncode((char) code)) code ++;
            if (code < 65536) {
                out.consume(code);
                code += 20;
            }
        }
        System.out.println();
        System.out.println("Encodable Chars dumped for Test Class " + getClass().getName());
        fail("Encodable Chars dumped for Test Class " + getClass().getName());
    }

    public void test_dumpEncoded () throws CharacterCodingException {
        if (testChars == null) return;
        if (testChars.length == 0) return;
        if (testBytes != null) return;
        System.out.format("\ntest_dumpEncoded() for name %s => %s (class = %s)\n",
                charsetName, charset.name(), getClass().getName());
        Charset_TestGenerator.Dumper out = new Charset_TestGenerator.Dumper1();
        CharBuffer inputCB = CharBuffer.wrap(testChars);
        ByteBuffer outputBB;
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        outputBB = encoder.encode(inputCB);
        outputBB.rewind();
        while (outputBB.hasRemaining()) {
            out.consume(outputBB.get() & 0xff);
        }
        System.out.println();
        System.out.println("Encoded Bytes dumped for Test Class " + getClass().getName());
        fail("Encoded Bytes dumped for Test Class " + getClass().getName());
    }


    static void decode (byte[] input, char[] expectedOutput) throws CharacterCodingException {
        ByteBuffer inputBB = ByteBuffer.wrap(input);
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        CharBuffer outputCB = decoder.decode(inputBB);
        outputCB.rewind();
        assertEqualChars(expectedOutput, outputCB);
    }

    public void test_Decode () throws CharacterCodingException {
        decode(testBytes, testChars);
    }

    public void test_Encode () throws CharacterCodingException {
        CharBuffer inputCB = CharBuffer.wrap(testChars);
        ByteBuffer outputBB;
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        outputBB = encoder.encode(inputCB);
        outputBB.rewind();
//        assertTrue("Encoded bytes must match!",
//                Arrays.equals(testBytes, outputBB.array()));
        assertEqualBytes("Encoded bytes must match!", testBytes, outputBB);
    }


    public void NNtest_CodecDynamicIndividuals () throws CharacterCodingException {
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        decoder.onMalformedInput(CodingErrorAction.REPORT);

        for (int code = 32; code <= 65533; code ++) {
            if (encoder.canEncode((char) code)) {
//                inputCB.rewind();
                CharBuffer inputCB = CharBuffer.allocate(1);
                inputCB.put((char) code);
                inputCB.rewind();
                ByteBuffer intermediateBB = encoder.encode(inputCB);
                inputCB.rewind();
                intermediateBB.rewind();
                try {
                    CharBuffer outputCB = decoder.decode(intermediateBB);
                    outputCB.rewind();
                    assertEqualCBs("decode(encode(A)) must be identical with A = " + code,
                            inputCB, outputCB);
                    if (code == 165) {
                        outputCB.rewind();
                        System.out.println("WOW:" + outputCB.get());
                    }
                } catch (CharacterCodingException e) {
                    fail("failed to decode(encode(" + code + "))");
                }
            }
        }
    }

    public void test_CodecDynamic () throws CharacterCodingException {
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        CharBuffer inputCB = CharBuffer.allocate(65536);
        for (int code = 32; code <= 65533; ++code) {
            // icu4c seems to accept any surrogate as a sign that "more is coming",
            // even for charsets like US-ASCII. http://b/10310751
            if (code >= 0xd800 && code <= 0xdfff) {
                continue;
            }
            if (encoder.canEncode((char) code)) {
                inputCB.put((char) code);
            }
        }
        inputCB.rewind();
        ByteBuffer intermediateBB = encoder.encode(inputCB);
        inputCB.rewind();
        intermediateBB.rewind();
        CharBuffer outputCB = decoder.decode(intermediateBB);
        outputCB.rewind();
        assertEqualCBs("decode(encode(A)) must be identical with A!",
                inputCB, outputCB);
    }

    static void assertEqualCBs (String msg, CharBuffer expectedCB, CharBuffer actualCB) {
        boolean match = true;
        boolean lenMatch = true;
        char expected, actual;
        int len = actualCB.length();
        if (expectedCB.length() != len) {
            lenMatch = false;
            if (expectedCB.length() < len) len = expectedCB.length();
        }
        for (int i = 0; i < len; i++) {
            expected = expectedCB.get();
            actual = actualCB.get();
            if (actual != expected) {
                String detail = String.format(
                        "Mismatch at index %d: %d instead of expected %d.\n",
                        i, (int) actual, (int) expected);
                match = false;
                fail(msg + ": " + detail);
            }
//            else {
//                System.out.format("Match index %d: %d = %d\n",
//                        i, (int) actual[i], (int) expected[i]);
//            }
        }
        assertTrue(msg, match);
        assertTrue(msg + "(IN LENGTH ALSO!)", lenMatch);
//        assertTrue(msg, Arrays.equals(actual, expected));
    }

    static void assertEqualChars(char[] expected, CharBuffer actualCB) {
        assertEquals(expected.length, actualCB.length());
        for (int i = 0; i < actualCB.length(); ++i) {
            char actual = actualCB.get();
            if (actual != expected[i]) {
                String detail = String.format("Mismatch at index %d: %d instead of expected %d.\n",
                                              i, (int) actual, (int) expected[i]);
                fail(detail);
            }
        }
    }

    static void assertEqualBytes (String msg, byte[] expected, ByteBuffer actualBB) {
        boolean match = true;
        boolean lenMatch = true;
        byte actual;
        int len = actualBB.remaining();
        if (expected.length != len) {
            lenMatch = false;
            if (expected.length < len) len = expected.length;
        }
        for (int i = 0; i < len; i++) {
            actual = actualBB.get();
            if (actual != expected[i]) {
                String detail = String.format(
                        "Mismatch at index %d: %d instead of expected %d.\n",
                        i, actual & 0xff, expected[i] & 0xff);
                match = false;
                fail(msg + ": " + detail);
            }
        }
        assertTrue(msg, match);
        assertTrue(msg + "(IN LENGTH ALSO!)", lenMatch);
    }


    static abstract class CodesGenerator {
        int row = 0, col = 0;

        abstract void consume(int code);

        boolean isAccepted(int code) {
            return Character.isLetterOrDigit(code);
        }
    }

}
