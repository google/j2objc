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

package libcore.java.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.UTFDataFormatException;
import java.util.Arrays;
import junit.framework.TestCase;

import libcore.util.HexEncoding;

public final class DataOutputStreamTest extends TestCase {
    private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private DataOutputStream os = new DataOutputStream(bytes);

    public void test_writeBoolean() throws Exception {
        os.writeBoolean(true);
        os.writeBoolean(false);
        assertEquals("0100", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeByte() throws Exception {
        os.writeByte(-1);
        os.writeByte(0);
        os.writeByte(1);
        os.writeByte(129);
        // writeByte takes only the bottom byte from its int parameter.
        os.writeByte(0x1234);
        assertEquals("FF00018134", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeBytes() throws Exception {
        // writeBytes takes only the bottom byte from each character.
        os.writeBytes("0\u12341");
        assertEquals("303431", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeChar() throws Exception {
        // writeChar writes two-byte big-endian characters.
        os.writeChar('0');
        os.writeChar(0x1234);
        assertEquals("00301234", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeChars() throws Exception {
        // writeChars writes two-byte big-endian characters.
        os.writeChars("0\u12341");
        assertEquals("003012340031", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeDouble() throws Exception {
        os.writeDouble(Double.longBitsToDouble(0x0123456789abcdefL));
        assertEquals("0123456789ABCDEF", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeFloat() throws Exception {
        os.writeFloat(Float.intBitsToFloat(0x01234567));
        assertEquals("01234567", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeInt() throws Exception {
        os.writeInt(0x01234567);
        assertEquals("01234567", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeLong() throws Exception {
        os.writeLong(0x0123456789abcdefL);
        assertEquals("0123456789ABCDEF", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeShort() throws Exception {
        // writeShort only writes the bottommost 16 bits of its int parameter.
        os.writeShort(0x01234567);
        assertEquals("4567", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeUTF() throws Exception {
        // The limit is 65535 *bytes* but we want to test 2- and 3-byte characters too.
        char[] chars = new char[65535 - 1 - 2];
        Arrays.fill(chars, 0, chars.length, 'a');
        chars[0] = '\u0666'; // a two-byte character
        chars[1] = '\u2603'; // a three-byte character
        String maxLength = new String(chars);
        os.writeUTF(maxLength);
        byte[] expected = new byte[2 + 65535];
        expected[0] = (byte) 0xff;
        expected[1] = (byte) 0xff;
        // U+0666 = 0xD9 0xA6
        expected[2] = (byte) 0xd9;
        expected[3] = (byte) 0xa6;
        // U+2603 = 0xE2 0x98 0x83
        expected[4] = (byte) 0xe2;
        expected[5] = (byte) 0x98;
        expected[6] = (byte) 0x83;
        Arrays.fill(expected, 7, expected.length, (byte) 'a');
        assertEquals(Arrays.toString(expected), Arrays.toString(bytes.toByteArray()));
    }

    public void test_writeUTF_NUL() throws Exception {
        // This is a special case, represented with two non-zero bytes.
        os.writeUTF("\u0000");
        assertEquals("0002C080", HexEncoding.encodeToString(bytes.toByteArray()));
    }

    public void test_writeUTF_too_long() throws Exception {
        String tooLong = new String(new char[65536]);
        try {
            os.writeUTF(tooLong);
            fail("should throw UTFDataFormatException");
        } catch (UTFDataFormatException expected) {
        }
        assertEquals("", HexEncoding.encodeToString(bytes.toByteArray()));
    }
}
