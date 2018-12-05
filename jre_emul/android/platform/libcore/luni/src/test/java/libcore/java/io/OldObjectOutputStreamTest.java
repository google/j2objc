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
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import junit.framework.TestCase;
import tests.support.Support_ASimpleOutputStream;
import tests.support.Support_OutputStream;

public class OldObjectOutputStreamTest extends TestCase implements Serializable {

    static final long serialVersionUID = 1L;

    java.io.File f;

    public class SerializableTestHelper implements Serializable {
        public String aField1;

        public String aField2;

        SerializableTestHelper(String s, String t) {
            aField1 = s;
            aField2 = t;
        }

        private void readObject(ObjectInputStream ois) throws IOException {
            // note aField2 is not read
            try {
                ObjectInputStream.GetField fields = ois.readFields();
                aField1 = (String) fields.get("aField1", "Zap");
            } catch (Exception e) {
            }
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            // note aField2 is not written
            ObjectOutputStream.PutField fields = oos.putFields();
            fields.put("aField1", aField1);
            oos.writeFields();
        }

        public String getText1() {
            return aField1;
        }

        public String getText2() {
            return aField2;
        }
    }

    private static class BasicObjectOutputStream extends ObjectOutputStream {
        public boolean writeStreamHeaderCalled;

        public BasicObjectOutputStream() throws IOException, SecurityException {
            super();
            writeStreamHeaderCalled = false;
        }

        public BasicObjectOutputStream(OutputStream output) throws IOException {
            super(output);
        }

        public void drain() throws IOException {
            super.drain();
        }

        public boolean enableReplaceObject(boolean enable)
                throws SecurityException {
            return super.enableReplaceObject(enable);
        }

        public void writeObjectOverride(Object object) throws IOException {
            super.writeObjectOverride(object);
        }

        public void writeStreamHeader() throws IOException {
            super.writeStreamHeader();
            writeStreamHeaderCalled = true;
        }
}

    private static class NoFlushTestOutputStream extends ByteArrayOutputStream {
        public boolean flushCalled;

        public NoFlushTestOutputStream() {
            super();
            flushCalled = false;
        }

        public void flush() throws IOException {
            super.flush();
            flushCalled = true;
        }
    }

    protected static final String MODE_XLOAD = "xload";

    protected static final String MODE_XDUMP = "xdump";

    static final String FOO = "foo";

    static final String MSG_WITE_FAILED = "Failed to write: ";

    private static final boolean DEBUG = false;

    protected static boolean xload = false;

    protected static boolean xdump = false;

    protected static String xFileName = null;

    protected ObjectInputStream ois;

    protected ObjectOutputStream oos;

    protected ObjectOutputStream oos_ioe;

    protected Support_OutputStream sos;

    protected ByteArrayOutputStream bao;

    static final int INIT_INT_VALUE = 7;

    static final String INIT_STR_VALUE = "a string that is blortz";

    /**
     * java.io.ObjectOutputStream#ObjectOutputStream(java.io.OutputStream)
     */
    public void test_ConstructorLjava_io_OutputStream() throws IOException {
        oos.close();
        oos = new ObjectOutputStream(new ByteArrayOutputStream());
        oos.close();

        try {
            oos = new ObjectOutputStream(null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        Support_ASimpleOutputStream sos = new Support_ASimpleOutputStream(true);
        try {
            oos = new ObjectOutputStream(sos);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_close() throws IOException {
        int outputSize = bao.size();
        // Writing of a primitive type should be buffered.
        oos.writeInt(42);
        assertTrue("Test 1: Primitive data unexpectedly written to the target stream.",
                bao.size() == outputSize);
        // Closing should write the buffered data to the target stream.
        oos.close();
        assertTrue("Test 2: Primitive data has not been written to the the target stream.",
                bao.size() > outputSize);

        try {
            oos_ioe.close();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_drain() throws IOException {
        NoFlushTestOutputStream target = new NoFlushTestOutputStream();
        BasicObjectOutputStream boos = new BasicObjectOutputStream(target);
        int initialSize = target.size();
        boolean written = false;

        boos.writeBytes("Lorem ipsum");
        // If there is no buffer then the bytes have already been written.
        written = (target.size() > initialSize);

        boos.drain();
        assertTrue("Content has not been written to the target.",
                written || (target.size() > initialSize));
        assertFalse("flush() has been called on the target.",
                target.flushCalled);
    }

    public void test_enableReplaceObjectB() throws IOException {
        // Start testing without a SecurityManager.
        BasicObjectOutputStream boos = new BasicObjectOutputStream();
        assertFalse("Test 1: Object resolving must be disabled by default.",
                boos.enableReplaceObject(true));

        assertTrue("Test 2: enableReplaceObject did not return the previous value.",
                boos.enableReplaceObject(false));
    }

    public void test_flush() throws Exception {
        // Test for method void java.io.ObjectOutputStream.flush()
        int size = bao.size();
        oos.writeByte(127);
        assertTrue("Test 1: Data already flushed.", bao.size() == size);
        oos.flush();
        assertTrue("Test 2: Failed to flush data.", bao.size() > size);

        try {
            oos_ioe.flush();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_putFields() throws Exception {
        /*
         * "SerializableTestHelper" is an object created for these tests with
         * two fields (Strings) and simple implementations of readObject and
         * writeObject which simply read and write the first field but not the
         * second one.
         */
        SerializableTestHelper sth;

        try {
            oos.putFields();
            fail("Test 1: NotActiveException expected.");
        } catch (NotActiveException e) {
            // Expected.
        }

        oos.writeObject(new SerializableTestHelper("Gabba", "Jabba"));
        oos.flush();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        sth = (SerializableTestHelper) (ois.readObject());
        assertEquals("Test 2: readFields or writeFields failed; first field not set.",
                "Gabba", sth.getText1());
        assertNull("Test 3: readFields or writeFields failed; second field should not have been set.",
                sth.getText2());
    }

    public void test_reset() throws Exception {
        String o = "HelloWorld";
        sos = new Support_OutputStream(200);
        oos.close();
        oos = new ObjectOutputStream(sos);
        oos.writeObject(o);
        oos.writeObject(o);
        oos.reset();
        oos.writeObject(o);

        sos.setThrowsException(true);
        try {
            oos.reset();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);

        ois = new ObjectInputStream(new ByteArrayInputStream(sos.toByteArray()));
        assertEquals("Test 2: Incorrect object read.", o, ois.readObject());
        assertEquals("Test 3: Incorrect object read.", o, ois.readObject());
        assertEquals("Test 4: Incorrect object read.", o, ois.readObject());
        ois.close();
    }

    public void test_write$BII() throws Exception {
        byte[] buf = new byte[10];
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        try {
            ois.read(buf, 0, -1);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            ois.read(buf, -1, 1);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            ois.read(buf, 10, 1);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        ois.close();

    }

    public void test_writeObjectOverrideLjava_lang_Object() throws IOException {
        BasicObjectOutputStream boos =
                new BasicObjectOutputStream(new ByteArrayOutputStream());

        try {
            boos.writeObjectOverride(new Object());
            fail("IOException expected.");
        }
        catch (IOException e) {
        }
        finally {
            boos.close();
        }
    }

    public void test_writeStreamHeader() throws IOException {
        BasicObjectOutputStream boos;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        short s;
        byte[] buffer;

        // Test 1: Make sure that writeStreamHeader() has been called.
        boos = new BasicObjectOutputStream(baos);
        try {
            assertTrue("Test 1: writeStreamHeader() has not been called.",
                         boos.writeStreamHeaderCalled);

            // Test 2: Check that at least four bytes have been written.
            buffer = baos.toByteArray();
            assertTrue("Test 2: At least four bytes should have been written",
                        buffer.length >= 4);

            // Test 3: Check the magic number.
            s = buffer[0];
            s <<= 8;
            s += ((short) buffer[1] & 0x00ff);
            assertEquals("Test 3: Invalid magic number written.",
                        java.io.ObjectStreamConstants.STREAM_MAGIC, s);

            // Test 4: Check the stream version number.
            s = buffer[2];
            s <<= 8;
            s += ((short) buffer[3] & 0x00ff);
            assertEquals("Invalid stream version number written.",
                        java.io.ObjectStreamConstants.STREAM_VERSION, s);
        }
        finally {
            boos.close();
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
        oos_ioe = new ObjectOutputStream(sos = new Support_OutputStream());
        sos.setThrowsException(true);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        if (oos != null) {
            try {
                oos.close();
            } catch (Exception e) {}
        }
        if (oos_ioe != null) {
            try {
                oos_ioe.close();
            } catch (Exception e) {}
        }
        if (f != null && f.exists()) {
            if (!f.delete()) {
                fail("Error cleaning up files during teardown");
            }
        }
    }

    protected Object reload() throws IOException, ClassNotFoundException {

        // Choose the load stream
        if (xload || xdump) {
            // Load from pre-existing file
            ois = new ObjectInputStream(new FileInputStream(xFileName + "-"
                    + getName() + ".ser"));
        } else {
            // Just load from memory, we dumped to memory
            ois = new ObjectInputStream(new ByteArrayInputStream(bao
                    .toByteArray()));
        }

        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }

    protected void dump(Object o) throws IOException, ClassNotFoundException {

        // Choose the dump stream
        if (xdump) {
            oos = new ObjectOutputStream(new FileOutputStream(
                    f = new java.io.File(xFileName + "-" + getName() + ".ser")));
        } else {
            oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
        }

        // Dump the object
        try {
            oos.writeObject(o);
        } finally {
            oos.close();
        }
    }
}
