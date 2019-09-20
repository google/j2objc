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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import tests.support.Support_OutputStream;

public class OldDataInputOutputStreamTest extends junit.framework.TestCase {

    private DataOutputStream os;

    private DataInputStream dis;

    private Support_OutputStream sos;

    String unihw = "\u0048\u0065\u006C\u006C\u006F\u0020\u0057\u006F\u0072\u006C\u0064";

    public void test_read_writeBoolean() throws IOException {
        os.writeBoolean(true);
        sos.setThrowsException(true);
        try {
            os.writeBoolean(false);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertTrue("Test 2: Incorrect boolean written or read.",
                dis.readBoolean());

        try {
            dis.readBoolean();
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readBoolean();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeByte() throws IOException {
        os.writeByte((byte) 127);
        sos.setThrowsException(true);
        try {
            os.writeByte((byte) 127);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertEquals("Test 2: Incorrect byte written or read;",
                (byte) 127, dis.readByte());

        try {
            dis.readByte();
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readByte();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeChar() throws IOException {
        os.writeChar('b');
        sos.setThrowsException(true);
        try {
            os.writeChar('k');
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertEquals("Test 2: Incorrect char written or read;",
                'b', dis.readChar());

        try {
            dis.readChar();
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readChar();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeDouble() throws IOException {
        os.writeDouble(2345.76834720202);
        sos.setThrowsException(true);
        try {
            os.writeDouble(2345.76834720202);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertEquals("Test 1: Incorrect double written or read;",
                2345.76834720202, dis.readDouble());

        try {
            dis.readDouble();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readDouble();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeFloat() throws IOException {
        os.writeFloat(29.08764f);
        sos.setThrowsException(true);
        try {
            os.writeFloat(29.08764f);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertEquals("Test 2: Incorrect float written or read;",
                29.08764f, dis.readFloat());

        try {
            dis.readFloat();
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readFloat();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeInt() throws IOException {
        os.writeInt(768347202);
        sos.setThrowsException(true);
        try {
            os.writeInt(768347202);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertEquals("Test 1: Incorrect int written or read;",
                768347202, dis.readInt());

        try {
            dis.readInt();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readInt();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeLong() throws IOException {
        os.writeLong(9875645283333L);
        sos.setThrowsException(true);
        try {
            os.writeLong(9875645283333L);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertEquals("Test 2: Incorrect long written or read;",
                9875645283333L, dis.readLong());

        try {
            dis.readLong();
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readLong();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeShort() throws IOException {
        os.writeShort(9875);
        sos.setThrowsException(true);
        try {
            os.writeShort(9875);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertEquals("Test 1: Incorrect short written or read;",
                9875, dis.readShort());

        try {
            dis.readShort();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readShort();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read_writeUTF() throws IOException {
        os.writeUTF(unihw);
        sos.setThrowsException(true);
        try {
            os.writeUTF(unihw);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        os.close();
        openDataInputStream();
        assertTrue("Test 1: Incorrect UTF-8 string written or read.",
                dis.readUTF().equals(unihw));

        try {
            dis.readUTF();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        dis.close();
        try {
            dis.readUTF();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    private void openDataInputStream() throws IOException {
        dis = new DataInputStream(new ByteArrayInputStream(sos.toByteArray()));
    }

    protected void setUp() {
        sos = new Support_OutputStream(256);
        os = new DataOutputStream(sos);
    }

    protected void tearDown() {
        try {
            os.close();
        } catch (Exception e) {
        }
        try {
            dis.close();
        } catch (Exception e) {
        }
    }
}
