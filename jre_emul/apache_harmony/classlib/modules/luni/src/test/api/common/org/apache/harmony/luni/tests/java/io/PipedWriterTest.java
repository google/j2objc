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
import java.io.PipedReader;
import java.io.PipedWriter;

public class PipedWriterTest extends junit.framework.TestCase {

	static class PReader implements Runnable {
		public PipedReader pr;

		public char[] buf = new char[10];

		public PReader(PipedWriter pw) {
			try {
				pr = new PipedReader(pw);
			} catch (IOException e) {
				System.out.println("Exception setting up reader: "
						+ e.toString());
			}
		}

		public PReader(PipedReader pr) {
			this.pr = pr;
		}

		public void run() {
			try {
				int r = 0;
				for (int i = 0; i < buf.length; i++) {
					r = pr.read();
					if (r == -1)
						break;
					buf[i] = (char) r;
				}
			} catch (Exception e) {
				System.out.println("Exception reading ("
						+ Thread.currentThread().getName() + "): "
						+ e.toString());
			}
		}
	}

	Thread rdrThread;

	PReader reader;

	PipedWriter pw;

	/**
	 * @tests java.io.PipedWriter#PipedWriter()
	 */
	public void test_Constructor() {
		// Test for method java.io.PipedWriter()
		// Used in tests
	}

	/**
     * @tests java.io.PipedWriter#PipedWriter(java.io.PipedReader)
     */
    public void test_ConstructorLjava_io_PipedReader() throws Exception {
        // Test for method java.io.PipedWriter(java.io.PipedReader)
        char[] buf = new char[10];
        "HelloWorld".getChars(0, 10, buf, 0);
        PipedReader rd = new PipedReader();
        pw = new PipedWriter(rd);
        rdrThread = new Thread(reader = new PReader(rd), "Constructor(Reader)");
        rdrThread.start();
        pw.write(buf);
        pw.close();
        rdrThread.join(500);
        assertEquals("Failed to construct writer", "HelloWorld", new String(
                reader.buf));
    }

    /**
     * @tests java.io.PipedWriter#close()
     */
    public void test_close() throws Exception {
        // Test for method void java.io.PipedWriter.close()
        char[] buf = new char[10];
        "HelloWorld".getChars(0, 10, buf, 0);
        PipedReader rd = new PipedReader();
        pw = new PipedWriter(rd);
        reader = new PReader(rd);
        pw.close();
        try {
            pw.write(buf);
            fail("Should have thrown exception when attempting to write to closed writer.");
        } catch (Exception e) {
            // correct
        }
    }

    /**
     * @tests java.io.PipedWriter#connect(java.io.PipedReader)
     */
    public void test_connectLjava_io_PipedReader() throws Exception {
        // Test for method void java.io.PipedWriter.connect(java.io.PipedReader)
        char[] buf = new char[10];
        "HelloWorld".getChars(0, 10, buf, 0);
        PipedReader rd = new PipedReader();
        pw = new PipedWriter();
        pw.connect(rd);
        rdrThread = new Thread(reader = new PReader(rd), "connect");
        rdrThread.start();
        pw.write(buf);
        pw.close();
        rdrThread.join(500);
        assertEquals("Failed to write correct chars", "HelloWorld", new String(
                reader.buf));
    }

    /**
     * @tests java.io.PipedWriter#flush()
     */
    public void test_flush() throws Exception {
        // Test for method void java.io.PipedWriter.flush()
        char[] buf = new char[10];
        "HelloWorld".getChars(0, 10, buf, 0);
        pw = new PipedWriter();
        rdrThread = new Thread(reader = new PReader(pw), "flush");
        rdrThread.start();
        pw.write(buf);
        pw.flush();
        rdrThread.join(700);
        assertEquals("Failed to flush chars", "HelloWorld", new String(
                reader.buf));
    }

    /**
     * @tests java.io.PipedWriter#write(char[], int, int)
     */
    public void test_write$CII() throws Exception {
        // Test for method void java.io.PipedWriter.write(char [], int, int)
        char[] buf = new char[10];
        "HelloWorld".getChars(0, 10, buf, 0);
        pw = new PipedWriter();
        rdrThread = new Thread(reader = new PReader(pw), "writeCII");
        rdrThread.start();
        pw.write(buf, 0, 10);
        pw.close();
        rdrThread.join(1000);
        assertEquals("Failed to write correct chars", "HelloWorld", new String(
                reader.buf));
    }

    /**
     * @tests java.io.PipedWriter#write(char[], int, int) Regression for
     *        HARMONY-387
     */
    public void test_write$CII_2() throws IOException {
        PipedReader pr = new PipedReader();
        PipedWriter obj = null;
        try {
            obj = new java.io.PipedWriter(pr);
            obj.write(new char[0], (int) 0, (int) -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
            assertEquals(
                    "IndexOutOfBoundsException rather than a subclass expected",
                    IndexOutOfBoundsException.class, t.getClass());
        }
    }

    /**
     * @tests java.io.PipedWriter#write(char[], int, int)
     */
    public void test_write$CII_3() throws IOException {
        PipedReader pr = new PipedReader();
        PipedWriter obj = null;
        try {
            obj = new java.io.PipedWriter(pr);
            obj.write(new char[0], (int) -1, (int) 0);
            fail("IndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException t) {
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {}
    }

    /**
     * @tests java.io.PipedWriter#write(char[], int, int)
     */
    public void test_write$CII_4() throws IOException {
        PipedReader pr = new PipedReader();
        PipedWriter obj = null;
        try {
            obj = new java.io.PipedWriter(pr);
            obj.write(new char[0], (int) -1, (int) -1);
            fail("IndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException t) {
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {}
    }

    /**
     * @tests java.io.PipedWriter#write(char[], int, int)
     */
    public void test_write$CII_5() throws IOException {
        PipedReader pr = new PipedReader();
        PipedWriter obj = null;
        try {
            obj = new PipedWriter(pr);
            obj.write((char[]) null, (int) -1, (int) 0);
            fail("NullPointerException expected");
        } catch (IndexOutOfBoundsException t) {
            fail("NullPointerException expected");
        } catch (NullPointerException t) {}
    }

    /**
     * @tests java.io.PipedWriter#write(char[], int, int)
     */
    public void test_write$CII_6() throws IOException {
        PipedReader pr = new PipedReader();
        PipedWriter obj = null;
        try {
            obj = new PipedWriter(pr);
            obj.write((char[]) null, (int) -1, (int) -1);
            fail("NullPointerException expected");
        } catch (IndexOutOfBoundsException t) {
            fail("NullPointerException expected");
        } catch (NullPointerException t) {}
    }

    /**
     * @tests java.io.PipedWriter#write(char[], int, int)
     */
    public void test_write$CII_notConnected() throws IOException {
        // Regression test for Harmony-2404
        // create not connected pipe
        PipedWriter obj = new PipedWriter();

        // char array is null
        try {
            obj.write((char[]) null, 0, 1);
            fail("IOException expected");
        } catch (IOException ioe) {
            // expected
        }

        // negative offset
        try {
            obj.write( new char[] { 1 }, -10, 1);
            fail("IOException expected");
        } catch (IOException ioe) {
            // expected
        }

        // wrong offset
        try {
            obj.write( new char[] { 1 }, 10, 1);
            fail("IOException expected");
        } catch (IOException ioe) {
            // expected
        }

        // negative length
        try {
            obj.write( new char[] { 1 }, 0, -10);
            fail("IOException expected");
        } catch (IOException ioe) {
            // expected
        }

        // all valid params
        try {
            obj.write( new char[] { 1, 1 }, 0, 1);
            fail("IOException expected");
        } catch (IOException ioe) {
            // expected
        }
    }
    
    /**
     * @tests java.io.PipedWriter#write(int)
     */
    public void test_write_I_MultiThread() throws IOException {
        final PipedReader pr = new PipedReader();
        final PipedWriter pw = new PipedWriter();
        // test if writer recognizes dead reader
        pr.connect(pw);

        class WriteRunnable implements Runnable {
            boolean pass = false;
            volatile boolean readerAlive = true;
            public void run() {
                try {
                    pw.write(1);
                    while (readerAlive) {
                    // wait the reader thread dead
                    }
                    try {
                        // should throw exception since reader thread
                        // is now dead
                        pw.write(1);
                    } catch (IOException e) {
                        pass = true;
                    }
                } catch (IOException e) {
                  //ignore
                }
            }
        }
        WriteRunnable writeRunnable = new WriteRunnable();
        Thread writeThread = new Thread(writeRunnable);
        class ReadRunnable implements Runnable {
            boolean pass;
            public void run() {
                try {
                    pr.read();
                    pass = true;
                } catch (IOException e) {
                  //ignore
                }
            }
        }
        ReadRunnable readRunnable = new ReadRunnable();
        Thread readThread = new Thread(readRunnable);
        writeThread.start();
        readThread.start();
        while (readThread.isAlive()) {
           //wait the reader thread dead
        }
        writeRunnable.readerAlive = false;
        assertTrue("reader thread failed to read", readRunnable.pass);
        while (writeThread.isAlive()) {
           //wait the writer thread dead
        }
        assertTrue("writer thread failed to recognize dead reader",
                writeRunnable.pass);
    }
    
    /**
     * @tests java.io.PipedWriter#write(char[],int,int)
     */
    public void test_write_$CII_MultiThread() throws Exception {
        final PipedReader pr = new PipedReader();
        final PipedWriter pw = new PipedWriter();

        // test if writer recognizes dead reader
        pr.connect(pw);

        class WriteRunnable implements Runnable {
            boolean pass = false;

            volatile boolean readerAlive = true;

            public void run() {
                try {
                    pw.write(1);
                    while (readerAlive) {
                    // wait the reader thread dead
                    }
                    try {
                        // should throw exception since reader thread
                        // is now dead
                        char[] buf = new char[10];
                        pw.write(buf, 0, 10);
                    } catch (IOException e) {
                        pass = true;
                    }
                } catch (IOException e) {
                  //ignore
                }
            }
        }
        WriteRunnable writeRunnable = new WriteRunnable();
        Thread writeThread = new Thread(writeRunnable);
        class ReadRunnable implements Runnable {
            boolean pass;

            public void run() {
                try {
                    pr.read();
                    pass = true;
                } catch (IOException e) {
                  //ignore
                }
            }
        }
        ReadRunnable readRunnable = new ReadRunnable();
        Thread readThread = new Thread(readRunnable);
        writeThread.start();
        readThread.start();
        while (readThread.isAlive()) {
            //wait the reader thread dead
        }
        writeRunnable.readerAlive = false;
        assertTrue("reader thread failed to read", readRunnable.pass);
        while (writeThread.isAlive()) {
            //wait the writer thread dead
        }
        assertTrue("writer thread failed to recognize dead reader",
                writeRunnable.pass);
    }

    /**
     * @tests java.io.PipedWriter#write(int)
     */
    public void test_writeI() throws Exception {
        // Test for method void java.io.PipedWriter.write(int)

        pw = new PipedWriter();
        rdrThread = new Thread(reader = new PReader(pw), "writeI");
        rdrThread.start();
        pw.write(1);
        pw.write(2);
        pw.write(3);
        pw.close();
        rdrThread.join(1000);
        assertTrue("Failed to write correct chars: " + (int) reader.buf[0]
                + " " + (int) reader.buf[1] + " " + (int) reader.buf[2],
                reader.buf[0] == 1 && reader.buf[1] == 2 && reader.buf[2] == 3);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        try {
            if (rdrThread != null) {
                rdrThread.interrupt();
            }
        } catch (Exception ignore) {}
        try {
            if (pw != null) {
                pw.close();
            }
        } catch (Exception ignore) {}
        super.tearDown();
    }
}
