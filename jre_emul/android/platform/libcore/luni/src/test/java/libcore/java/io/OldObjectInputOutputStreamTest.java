/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package libcore.java.io;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import tests.support.Support_OutputStream;

public class OldObjectInputOutputStreamTest extends junit.framework.TestCase {

    private ObjectOutputStream os;

    private ObjectInputStream is;

    private Support_OutputStream sos;

    String unihw = "\u0048\u0065\u006C\u006C\u006F\u0020\u0057\u006F\u0072\u006C\u0064";

    public void test_read_writeBoolean() throws IOException {
        os.writeBoolean(true);

        os.close();
        openObjectInputStream();
        assertTrue("Test 1: Incorrect boolean written or read.",
                is.readBoolean());

        try {
            is.readBoolean();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readBoolean();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeByte() throws IOException {
        os.writeByte((byte) 127);

        os.close();
        openObjectInputStream();
        assertEquals("Test 1: Incorrect byte written or read;",
                (byte) 127, is.readByte());

        try {
            is.readByte();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readByte();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeChar() throws IOException {
        os.writeChar('b');

        os.close();
        openObjectInputStream();
        assertEquals("Test 1: Incorrect char written or read;",
                'b', is.readChar());

        try {
            is.readChar();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readChar();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeDouble() throws IOException {
        os.writeDouble(2345.76834720202);

        os.close();
        openObjectInputStream();
        assertEquals("Test 1: Incorrect double written or read;",
                2345.76834720202, is.readDouble());

        try {
            is.readDouble();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readDouble();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeFloat() throws IOException {
        os.writeFloat(29.08764f);

        os.close();
        openObjectInputStream();
        assertEquals("Test 1: Incorrect float written or read;",
                29.08764f, is.readFloat());

        try {
            is.readFloat();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readFloat();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeInt() throws IOException {
        os.writeInt(768347202);

        os.close();
        openObjectInputStream();
        assertEquals("Test 1: Incorrect int written or read;",
                768347202, is.readInt());

        try {
            is.readInt();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readInt();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeLong() throws IOException {
        os.writeLong(9875645283333L);

        os.close();
        openObjectInputStream();
        assertEquals("Test 1: Incorrect long written or read;",
                9875645283333L, is.readLong());

        try {
            is.readLong();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readLong();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeShort() throws IOException {
        os.writeShort(9875);

        os.close();
        openObjectInputStream();
        assertEquals("Test 1: Incorrect short written or read;",
                9875, is.readShort());

        try {
            is.readShort();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readShort();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeUTF() throws IOException {
        os.writeUTF(unihw);

        os.close();
        openObjectInputStream();
        assertTrue("Test 1: Incorrect UTF-8 string written or read.",
                is.readUTF().equals(unihw));

        try {
            is.readUTF();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        is.close();
        try {
            is.readUTF();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    private void openObjectInputStream() throws IOException {
        is = new ObjectInputStream(
                new ByteArrayInputStream(sos.toByteArray()));
    }

    protected void setUp() throws IOException {
        sos = new Support_OutputStream(256);
        os = new ObjectOutputStream(sos);
    }

    protected void tearDown() {
        try {
            os.close();
        } catch (Exception e) {
        }
        try {
            is.close();
        } catch (Exception e) {
        }
    }
}
