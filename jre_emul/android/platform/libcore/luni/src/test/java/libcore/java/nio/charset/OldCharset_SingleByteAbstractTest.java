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
import java.nio.charset.CodingErrorAction;

/**
 * Super class for concrete charset test suites.
 */
public abstract class OldCharset_SingleByteAbstractTest extends OldCharset_AbstractTest {

    static byte[] allBytes;
    static char[] allChars;

    @Override
    protected void setUp() throws Exception {
        allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) i;
        }
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }



    public static void dumpDecoded () {
        Charset_TestGenerator.Dumper out = new Charset_TestGenerator.Dumper1();
        ByteBuffer inputBB = ByteBuffer.wrap(allBytes);
        CharBuffer outputCB;
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        try {
            outputCB = decoder.decode(inputBB);
            outputCB.rewind();
            while (outputCB.hasRemaining()) {
                out.consume(outputCB.get());
            }
        } catch (CharacterCodingException e) {
            System.out.println(e);
//                e.printStackTrace();
        }
    }

    public static void decodeReplace (byte[] input, char[] expectedOutput) throws CharacterCodingException {
        ByteBuffer inputBB = ByteBuffer.wrap(input);
        CharBuffer outputCB;
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        outputCB = decoder.decode(inputBB);
        outputCB.rewind();
        assertEqualChars2("Decoded charactes must match!",
                expectedOutput,
                outputCB.array(),
                input);
//        assertTrue("Decoded charactes (REPLACEed ones INCLUSIVE) must match!",
//                Arrays.equals(expectedOutput, outputCB.array()));

//        assertEqualChars("Decoded charactes (REPLACEed ones INCLUSIVE) must match!",
//                expectedOutput,
//                outputCB.array());

//        assertEquals("Decoded charactes must match!",
//                String.valueOf(allChars),
//                outputCB.toString());
    }

    @Override
    public void test_Decode () throws CharacterCodingException {
        decodeReplace(allBytes, allChars);
//        ByteBuffer inputBB = ByteBuffer.wrap(allBytes);
//        CharBuffer outputCB;
//        decoder.onMalformedInput(CodingErrorAction.REPLACE);
//        outputCB = decoder.decode(inputBB);
//        outputCB.rewind();
//        assertEqualChars("Decoded charactes must match!",
//                allChars,
//                outputCB.array());
////        assertEquals("Decoded charactes must match!",
////                String.valueOf(allChars),
////                outputCB.toString());
    }

    @Override
    public void test_Encode () throws CharacterCodingException {
        CharBuffer inputCB = CharBuffer.wrap(allChars);
        ByteBuffer outputBB;
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        outputBB = encoder.encode(inputCB);
        outputBB.rewind();
        assertEqualBytes2("Encoded bytes must match!", allBytes, outputBB.array(), allChars);
    }

//    static void assertEqualChars (String msg, char[] expected, char[] actual) {
//        int len = expected.length;
//        if (actual.length < len) len = actual.length;
//        for (int i = 0; i < len; i++) {
//            if (actual[i] != expected[i]) {
//                System.out.format("Mismatch at index %d: %d instead of expected %d.\n",
//                        i, (int) actual[i], (int) expected[i]);
//            }
////            else {
////                System.out.format("Match index %d: %d = %d\n",
////                        i, (int) actual[i], (int) expected[i]);
////            }
//        }
//        assertTrue(msg, Arrays.equals(actual, expected));
//    }

    static void assertEqualChars2 (String msg, char[] expected, char[] actual, byte[] bytes) {
        boolean match = true;
        boolean replaceMatch = true;
        int len = expected.length;
        if (actual.length < len) len = actual.length;
        for (int i = 0; i < len; i++) {
            if (actual[i] == expected[i]) {
                // Fine!
            }
            else {
                if (expected[i] == 65533) {
                    if (actual[i] == (bytes[i] & 0xff)) {
//                        System.out.format("REPLACE mismatch at index %d (byte %d): %d instead of expected %d.\n",
//                                i, bytes[i] & 0xff, (int) actual[i], (int) expected[i]);
                    } else {
//                        System.out.format("REPLACE mismatch at index %d (byte %d): %d instead of expected %d.\n",
//                                i, bytes[i] & 0xff, (int) actual[i], (int) expected[i]);
                    }
                    replaceMatch = false;
                } else {
//                    System.out.format("MISMATCH at index %d (byte %d): %d instead of expected %d.\n",
//                            i, bytes[i] & 0xff, (int) actual[i], (int) expected[i]);
                    match = false;
                }
            }
//            if ((actual[i] != expected[i]) &&
//                    !((actual[i] == bytes[i]) && (expected[i] == 65533))) {
//
//                match = false;
//            }
        }
        assertTrue(msg, match);
        if (!replaceMatch) {
//            System.out.println("for charset " + charsetName);
        }
    }

//    static void assertEqualBytes (String msg, byte[] expected, byte[] actual) {
//        int len = expected.length;
//        if (actual.length < len) len = actual.length;
//        for (int i = 0; i < len; i++) {
//            if (actual[i] != expected[i]) {
//                System.out.format("MISMATCH at index %d: %d instead of expected %d.\n",
//                        i, actual[i], expected[i]);
//            }
//        }
//        assertTrue(msg, Arrays.equals(actual, expected));
//    }

    static void assertEqualBytes2 (String msg, byte[] expected, byte[] actual, char[] chars) {
        boolean match = true;
        int len = expected.length;
        if (actual.length < len) len = actual.length;
        for (int i = 0; i < len; i++) {
            if ((actual[i] != expected[i]) &&
                    !((chars[i] == 65533)) && (actual[i] == 63)) {
//              System.out.format("MISMATCH at index %d: %d instead of expected %d.\n",
//                      i, actual[i], expected[i]);
                match = false;
            }
        }
        assertTrue(msg, match);
    }

}
