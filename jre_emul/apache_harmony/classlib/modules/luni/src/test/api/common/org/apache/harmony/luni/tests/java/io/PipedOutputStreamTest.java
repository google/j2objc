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

package org.apache.harmony.luni.tests.java.io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class PipedOutputStreamTest extends TestCase {

    static class PReader implements Runnable {
        PipedInputStream reader;

        public PipedInputStream getReader() {
            return reader;
        }

        public PReader(PipedOutputStream out) {
            try {
                reader = new PipedInputStream(out);
            } catch (Exception e) {
                System.out.println("Couldn't start reader");
            }
        }

        public int available() {
            try {
                return reader.available();
            } catch (Exception e) {
                return -1;
            }
        }

        public void run() {
            try {
                while (true) {
                    Thread.sleep(1000);
                    Thread.yield();
                }
            } catch (InterruptedException e) {
            }
        }

        public String read(int nbytes) {
            byte[] buf = new byte[nbytes];
            try {
                reader.read(buf, 0, nbytes);
                return new String(buf, "UTF-8");
            } catch (IOException e) {
                System.out.println("Exception reading info");
                return "ERROR";
            }
        }
    }

    Thread rt;

    PReader reader;

    PipedOutputStream out;

    /**
     * @tests java.io.PipedOutputStream#PipedOutputStream()
     */
    public void test_Constructor() {
        // Used in tests
    }

    /**
     * @tests java.io.PipedOutputStream#PipedOutputStream(java.io.PipedInputStream)
     */
    public void test_ConstructorLjava_io_PipedInputStream() throws Exception {
        out = new PipedOutputStream(new PipedInputStream());
        out.write('b');
    }

    /**
     * @tests java.io.PipedOutputStream#close()
     */
    public void test_close() throws Exception {
        out = new PipedOutputStream();
        rt = new Thread(reader = new PReader(out));
        rt.start();
        out.close();
    }

    /**
     * @tests java.io.PipedOutputStream#connect(java.io.PipedInputStream)
     */
    public void test_connectLjava_io_PipedInputStream_Exception()
            throws IOException {
        out = new PipedOutputStream();
        out.connect(new PipedInputStream());
        try {
            out.connect(null);
            fail("should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.io.PipedOutputStream#connect(java.io.PipedInputStream)
     */
    public void test_connectLjava_io_PipedInputStream() {
        try {
            out = new PipedOutputStream();
            rt = new Thread(reader = new PReader(out));
            rt.start();
            out.connect(new PipedInputStream());
            fail("Failed to throw exception attempting connect on already connected stream");
        } catch (IOException e) {
            // Expected
        }
    }

    /**
     * @tests java.io.PipedOutputStream#flush()
     */
    public void test_flush() throws IOException, UnsupportedEncodingException {
        out = new PipedOutputStream();
        rt = new Thread(reader = new PReader(out));
        rt.start();
        out.write("HelloWorld".getBytes("UTF-8"), 0, 10);
        assertTrue("Bytes written before flush", reader.available() != 0);
        out.flush();
        assertEquals("Wrote incorrect bytes", "HelloWorld", reader.read(10));
    }

    /**
     * @tests java.io.PipedOutputStream#write(byte[], int, int)
     */
    public void test_write$BII() throws IOException, UnsupportedEncodingException {
        out = new PipedOutputStream();
        rt = new Thread(reader = new PReader(out));
        rt.start();
        out.write("HelloWorld".getBytes("UTF-8"), 0, 10);
        out.flush();
        assertEquals("Wrote incorrect bytes", "HelloWorld", reader.read(10));
    }

    /**
     * @tests java.io.PipedOutputStream#write(byte[], int, int) Regression for
     *        HARMONY-387
     */
    public void test_write$BII_2() throws IOException {
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = null;
        try {
            pos = new PipedOutputStream(pis);
            pos.write(new byte[0], -1, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
            assertEquals(
                    "IndexOutOfBoundsException rather than a subclass expected",
                    IndexOutOfBoundsException.class, t.getClass());
        }

        // Regression for HARMONY-4311
        try {
            pis = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(pis);
            out.write(null, -10, 10);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }

        pis = new PipedInputStream();
        pos = new PipedOutputStream(pis);
        pos.close();
        pos.write(new byte[0], 0, 0);

        try {
            pis = new PipedInputStream();
            pos = new PipedOutputStream(pis);
            pos.write(new byte[0], -1, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
            //expected
        }

        try {
            pis = new PipedInputStream();
            pos = new PipedOutputStream(pis);
            pos.write(null, -10, 0);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }

    }

    /**
     * @tests java.io.PipedOutputStream#write(int)
     */
    public void test_writeI() throws IOException {
        out = new PipedOutputStream();
        rt = new Thread(reader = new PReader(out));
        rt.start();
        out.write('c');
        out.flush();
        assertEquals("Wrote incorrect byte", "c", reader.read(1));
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    @Override
    protected void tearDown() {
        if (rt != null) {
            rt.interrupt();
        }
    }
}
