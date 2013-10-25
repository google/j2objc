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

import junit.framework.TestCase;

public class PipedInputStreamTest extends TestCase {

    static class PWriter implements Runnable {
        PipedOutputStream pos;

        public byte bytes[];

        public void run() {
            try {
                pos.write(bytes);
                synchronized (this) {
                    notify();
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
                System.out.println("Could not write bytes");
            }
        }

        public PWriter(PipedOutputStream pout, int nbytes) {
            pos = pout;
            bytes = new byte[nbytes];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (System.currentTimeMillis() % 9);
            }
        }
    }

    Thread t;

    PWriter pw;

    PipedInputStream pis;

    PipedOutputStream pos;

    /**
     * @tests java.io.PipedInputStream#PipedInputStream()
     */
    public void test_Constructor() {
        // Used in tests
    }

    /**
     * @tests java.io.PipedInputStream#PipedInputStream(java.io.PipedOutputStream)
     */
    public void test_ConstructorLjava_io_PipedOutputStream() throws IOException {
        pis = new PipedInputStream(new PipedOutputStream());
        pis.available();
    }


    /**
     * @test java.io.PipedInputStream#read()
     */
    public void test_readException() throws IOException {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();

        try {
            pis.connect(pos);
            t = new Thread(pw = new PWriter(pos, 1000));
            t.start();
            assertTrue(t.isAlive());
            while (true) {
                pis.read();
                t.interrupt();
            }
        } catch (IOException e) {
            if (!e.getMessage().contains("Pipe broken")) {
                throw e;
            }
        } finally {
            try {
                pis.close();
                pos.close();
            } catch (IOException ee) {}
        }
    }

    /**
     * @tests java.io.PipedInputStream#available()
     */
    public void test_available() throws Exception {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();

        pis.connect(pos);
        t = new Thread(pw = new PWriter(pos, 1000));
        t.start();

        synchronized (pw) {
            pw.wait(10000);
        }
        assertTrue("Available returned incorrect number of bytes: "
                + pis.available(), pis.available() == 1000);

        PipedInputStream pin = new PipedInputStream();
        PipedOutputStream pout = new PipedOutputStream(pin);
        // We know the PipedInputStream buffer size is 1024.
        // Writing another byte would cause the write to wait
        // for a read before returning
        for (int i = 0; i < 1024; i++) {
            pout.write(i);
        }
        assertEquals("Incorrect available count", 1024, pin.available());
    }

    /**
     * @tests java.io.PipedInputStream#close()
     */
    public void test_close() throws IOException {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        pis.connect(pos);
        pis.close();
        try {
            pos.write((byte) 127);
            fail("Failed to throw expected exception");
        } catch (IOException e) {
            // The spec for PipedInput saya an exception should be thrown if
            // a write is attempted to a closed input. The PipedOuput spec
            // indicates that an exception should be thrown only when the
            // piped input thread is terminated without closing
        }
    }

    /**
     * @tests java.io.PipedInputStream#connect(java.io.PipedOutputStream)
     */
    public void test_connectLjava_io_PipedOutputStream() throws Exception {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        assertEquals("Non-conected pipe returned non-zero available bytes", 0,
                pis.available());

        pis.connect(pos);
        t = new Thread(pw = new PWriter(pos, 1000));
        t.start();

        synchronized (pw) {
            pw.wait(10000);
        }
        assertEquals("Available returned incorrect number of bytes", 1000, pis
                .available());
    }

    /**
     * @tests java.io.PipedInputStream#read()
     */
    public void test_read() throws Exception {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();

        pis.connect(pos);
        t = new Thread(pw = new PWriter(pos, 1000));
        t.start();

        synchronized (pw) {
            pw.wait(10000);
        }
        assertEquals("Available returned incorrect number of bytes", 1000, pis
                .available());
        assertEquals("read returned incorrect byte", pw.bytes[0], (byte) pis
                .read());
    }

    /**
     * @tests java.io.PipedInputStream#read(byte[], int, int)
     */
    public void test_read$BII() throws Exception {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();

        pis.connect(pos);
        t = new Thread(pw = new PWriter(pos, 1000));
        t.start();

        byte[] buf = new byte[400];
        synchronized (pw) {
            pw.wait(10000);
        }
        assertTrue("Available returned incorrect number of bytes: "
                + pis.available(), pis.available() == 1000);
        pis.read(buf, 0, 400);
        for (int i = 0; i < 400; i++) {
            assertEquals("read returned incorrect byte[]", pw.bytes[i], buf[i]);
        }
    }

    /**
     * @tests java.io.PipedInputStream#read(byte[], int, int) Regression for
     *        HARMONY-387
     */
    public void test_read$BII_2() throws IOException {
        PipedInputStream obj = new PipedInputStream();
        try {
            obj.read(new byte[0], 0, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
            assertEquals(
                    "IndexOutOfBoundsException rather than a subclass expected",
                    IndexOutOfBoundsException.class, t.getClass());
        }
    }

    /**
     * @tests java.io.PipedInputStream#read(byte[], int, int)
     */
    public void test_read$BII_3() throws IOException {
        PipedInputStream obj = new PipedInputStream();
        try {
            obj.read(new byte[0], -1, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException t) {
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
        }
    }

    /**
     * @tests java.io.PipedInputStream#read(byte[], int, int)
     */
    public void test_read$BII_4() throws IOException {
        PipedInputStream obj = new PipedInputStream();
        try {
            obj.read(new byte[0], -1, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException t) {
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
        }
    }

    /**
     * @tests java.io.PipedInputStream#receive(int)
     */
    public void test_receive() throws IOException {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();

        // test if writer recognizes dead reader
        pis.connect(pos);
        class WriteRunnable implements Runnable {

            boolean pass = false;

            volatile boolean readerAlive = true;

            public void run() {
                try {
                    pos.write(1);
                    while (readerAlive) {
                        ;
                    }
                    try {
                        // should throw exception since reader thread
                        // is now dead
                        pos.write(1);
                    } catch (IOException e) {
                        pass = true;
                    }
                } catch (IOException e) {
                }
            }
        }
        WriteRunnable writeRunnable = new WriteRunnable();
        Thread writeThread = new Thread(writeRunnable);
        class ReadRunnable implements Runnable {

            boolean pass;

            public void run() {
                try {
                    pis.read();
                    pass = true;
                } catch (IOException e) {
                }
            }
        }
        ;
        ReadRunnable readRunnable = new ReadRunnable();
        Thread readThread = new Thread(readRunnable);
        writeThread.start();
        readThread.start();
        while (readThread.isAlive()) {
            ;
        }
        writeRunnable.readerAlive = false;
        assertTrue("reader thread failed to read", readRunnable.pass);
        while (writeThread.isAlive()) {
            ;
        }
        assertTrue("writer thread failed to recognize dead reader",
                writeRunnable.pass);

        // attempt to write to stream after writer closed
        pis = new PipedInputStream();
        pos = new PipedOutputStream();

        pis.connect(pos);
        class MyRunnable implements Runnable {

            boolean pass;

            public void run() {
                try {
                    pos.write(1);
                } catch (IOException e) {
                    pass = true;
                }
            }
        }
        MyRunnable myRun = new MyRunnable();
        synchronized (pis) {
            t = new Thread(myRun);
            // thread t will be blocked inside pos.write(1)
            // when it tries to call the synchronized method pis.receive
            // because we hold the monitor for object pis
            t.start();
            try {
                // wait for thread t to get to the call to pis.receive
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            // now we close
            pos.close();
        }
        // we have exited the synchronized block, so now thread t will make
        // a call to pis.receive AFTER the output stream was closed,
        // in which case an IOException should be thrown
        while (t.isAlive()) {
            ;
        }
        assertTrue(
                "write failed to throw IOException on closed PipedOutputStream",
                myRun.pass);
    }

    static class Worker extends Thread {
        PipedOutputStream out;

        Worker(PipedOutputStream pos) {
            this.out = pos;
        }

        public void run() {
            try {
                out.write(20);
                out.close();
                Thread.sleep(5000);
            } catch (Exception e) {
            }
        }
    }

    public void test_read_after_write_close() throws Exception{
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream();
        in.connect(out);
        Thread worker = new Worker(out);
        worker.start();
        Thread.sleep(2000);
        assertEquals("Should read 20.", 20, in.read());
        worker.join();
        assertEquals("Write end is closed, should return -1", -1, in.read());
        byte[] buf = new byte[1];
        assertEquals("Write end is closed, should return -1", -1, in.read(buf, 0, 1));
        assertEquals("Buf len 0 should return first", 0, in.read(buf, 0, 0));
        in.close();
        out.close();
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    @Override
    protected void tearDown() throws Exception {
        try {
            if (t != null) {
                t.interrupt();
            }
        } catch (Exception ignore) {
        }
        super.tearDown();
    }
}
