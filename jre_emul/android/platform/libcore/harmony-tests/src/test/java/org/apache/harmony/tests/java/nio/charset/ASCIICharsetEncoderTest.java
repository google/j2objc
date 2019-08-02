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

package org.apache.harmony.tests.java.nio.charset;

import junit.framework.TestCase;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

public class ASCIICharsetEncoderTest extends TestCase {

    // charset for ascii
    private final Charset cs = Charset.forName("ascii");
    private final CharsetEncoder encoder = cs.newEncoder();
    private static final int MAXCODEPOINT = 0x7F;

    /*
      * @see CharsetEncoderTest#setUp()
      */
    protected void setUp() throws Exception {
    }

    /*
      * @see CharsetEncoderTest#tearDown()
      */
    protected void tearDown() throws Exception {
    }

    public void testCanEncodeCharSequence() {
        // normal case for ascCS
        assertTrue(encoder.canEncode("\u0077"));
        assertFalse(encoder.canEncode("\uc2a3"));
        assertFalse(encoder.canEncode("\ud800\udc00"));
        try {
            encoder.canEncode(null);
        } catch (NullPointerException e) {
        }
        assertTrue(encoder.canEncode(""));
    }

    public void testCanEncodeSurrogate() {
        assertFalse(encoder.canEncode('\ud800'));
        assertFalse(encoder.canEncode("\udc00"));
    }

    public void testCanEncodechar() throws CharacterCodingException {
        assertTrue(encoder.canEncode('\u0077'));
        assertFalse(encoder.canEncode('\uc2a3'));
    }

    public void testSpecificDefaultValue() {
        assertEquals(1.0, encoder.averageBytesPerChar(), 0.0);
        assertEquals(1.0, encoder.maxBytesPerChar(), 0.0);
    }

    public void testMultiStepEncode() throws CharacterCodingException {
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            encoder.encode(CharBuffer.wrap("\ud800\udc00"));
            fail("should unmappable");
        } catch (UnmappableCharacterException e) {
        }
        encoder.reset();
        ByteBuffer out = ByteBuffer.allocate(10);
        assertEquals(CoderResult.UNDERFLOW,
                encoder.encode(CharBuffer.wrap("\ud800"), out, true));
        assertTrue(encoder.flush(out).isMalformed());
        encoder.reset();

        out = ByteBuffer.allocate(10);
        CharBuffer buffer1 = CharBuffer.wrap("\ud800");
        CharBuffer buffer2 = CharBuffer.wrap("\udc00");
        assertSame(CoderResult.UNDERFLOW, encoder.encode(buffer1, out, false));
        // We consume the entire input buffer because we're in an underflow
        // state. We can't make a decision on whether the char in this buffer
        // is unmappable or malformed without looking at the next input buffer.
        assertEquals(1, buffer1.position());
        assertTrue(encoder.encode(buffer2, out, true).isUnmappable());
        assertEquals(0, buffer2.position());
    }

    public void testEncodeMapping() throws CharacterCodingException {
        encoder.reset();

        for (int i = 0; i <= MAXCODEPOINT; i++) {
            char[] chars = Character.toChars(i);
            CharBuffer cb = CharBuffer.wrap(chars);
            ByteBuffer bb = encoder.encode(cb);
            assertEquals(i, bb.get(0));
        }

        CharBuffer cb = CharBuffer.wrap("\u0080");
        try {
            encoder.encode(cb);
        } catch (UnmappableCharacterException e) {
            //expected
        }

        cb = CharBuffer.wrap("\ud800");
        try {
            encoder.encode(cb);
        } catch (MalformedInputException e) {
            //expected
        }

        ByteBuffer bb = ByteBuffer.allocate(0x10);
        cb = CharBuffer.wrap("A");
        encoder.reset();
        encoder.encode(cb, bb, false);
        try {
            encoder.encode(cb);
        } catch (IllegalStateException e) {
            //expected
        }
    }

    public void testInternalState() {
        CharBuffer in = CharBuffer.wrap("A");
        ByteBuffer out = ByteBuffer.allocate(0x10);

        //normal encoding process
        encoder.reset();
        encoder.encode(in, out, false);
        in = CharBuffer.wrap("B");
        encoder.encode(in, out, true);
        encoder.flush(out);
    }

    //reset could be called at any time
    public void testInternalState_Reset() {
        CharsetEncoder newEncoder = cs.newEncoder();
        //Init - > reset
        newEncoder.reset();

        //reset - > reset
        newEncoder.reset();

        //encoding - >reset
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            newEncoder.reset();
        }

        //encoding end -> reset
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.reset();
        }
        //flused -> reset
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            newEncoder.reset();
        }
    }

    public void testInternalState_Encoding() {
        CharsetEncoder newEncoder = cs.newEncoder();
        //Init - > encoding
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
        }

        //reset - > encoding
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.reset();
            newEncoder.encode(in, out, false);
        }
        //reset - > encoding - > encoding
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, false);
        }

        //encoding_end - > encoding
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                //expected
            }
        }
        //flushed - > encoding
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                //expected
            }
        }
    }

    public void testInternalState_Encoding_END() {
        CharsetEncoder newEncoder = cs.newEncoder();

        //Init - >encoding_end
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
        }

        //Reset -> encoding_end
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.reset();
            newEncoder.encode(in, out, true);
        }

        //encoding -> encoding_end
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, true);
        }

        //Reset -> encoding_end
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, true);
        }

        //Flushed -> encoding_end
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, true);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                //expected
            }
        }
    }

    public void testInternalState_Flushed() {
        CharsetEncoder newEncoder = cs.newEncoder();

        // init -> flushed
        {
            ByteBuffer out = ByteBuffer.allocate(0x10);
            try {
                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                //expected
            }

        }

        // reset - > flushed
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.reset();
            try {
                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                //expected
            }
        }

        //encoding - > flushed
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            try {

                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }

        //encoding_end -> flushed
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
        }

        //flushd - > flushed
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            newEncoder.flush(out);
        }
    }

    public void testInternalState_Encode() throws CharacterCodingException {
        CharsetEncoder newEncoder = cs.newEncoder();
        //Init - > encode
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
        }

        //Reset - > encode
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
        }

        //Encoding -> encode
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }

        //Encoding_end -> encode
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }

        //Flushed -> reset
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.flush(out);
            out = newEncoder.encode(in);
        }
    }

    public void testInternalState_from_Encode() throws CharacterCodingException {
        CharsetEncoder newEncoder = cs.newEncoder();

        //Encode -> Reset
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            newEncoder.reset();
        }

        // Encode -> encoding
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            ByteBuffer out = ByteBuffer.allocate(0x10);
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }

        //Encode -> Encoding_end
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.reset();
            newEncoder.encode(in, out, false);
            newEncoder.encode(in, out, true);
        }

        //Encode -> Flushed
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = newEncoder.encode(in);
            newEncoder.flush(out);
        }

        //Encode - > encode
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }
    }
}
