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

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import tests.support.Support_ASimpleReader;
import tests.support.Support_StringReader;
import tests.support.ThrowingReader;

public class OldBufferedReaderTest extends junit.framework.TestCase {

    BufferedReader br;

    String testString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    public void test_ConstructorLjava_io_Reader() {
        // Test for method java.io.BufferedReader(java.io.Reader)
        br = new BufferedReader(new Support_StringReader(testString));
        assertNotNull(br);
    }

    public void test_ConstructorLjava_io_ReaderI() {
        // Illegal negative size argument test.
        try {
            br = new BufferedReader(new Support_StringReader(testString), 0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
        br = new BufferedReader(new Support_StringReader(testString), 1024);
        assertNotNull(br);
    }

    public void test_close() {
        Support_ASimpleReader ssr = new Support_ASimpleReader(true);
        try {
            br = new BufferedReader(new Support_StringReader(testString));
            br.close();
            br.read();
            fail("Test 1: Read on closed stream.");
        } catch (IOException x) {
            // Expected.
        } catch (Exception e) {
            fail("Exception during close test " + e.toString());
        }

        br = new BufferedReader(ssr);
        try {
            br.close();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // Avoid IOException in tearDown().
        ssr.throwExceptionOnNextUse = false;
    }

	public void test_markI() throws IOException {
        // Test for method void java.io.BufferedReader.mark(int)
        char[] buf = null;
        try {
            br = new BufferedReader(new Support_StringReader(testString));
            br.skip(500);
            br.mark(1000);
            br.skip(250);
            br.reset();
            buf = new char[testString.length()];
            br.read(buf, 0, 500);
            assertTrue("Failed to set mark properly", testString.substring(500,
                    1000).equals(new String(buf, 0, 500)));
        } catch (java.io.IOException e) {
            fail("Exception during mark test");
        }
        try {
            br = new BufferedReader(new Support_StringReader(testString), 800);
            br.skip(500);
            br.mark(250);
            br.read(buf, 0, 1000);
            br.reset();
            fail("Failed to invalidate mark properly");
        } catch (IOException x) {
        }

        char[] chars = new char[256];
        for (int i = 0; i < 256; i++)
            chars[i] = (char) i;
        Reader in = new BufferedReader(new Support_StringReader(new String(
                chars)), 12);
        try {
            in.skip(6);
            in.mark(14);
            in.read(new char[14], 0, 14);
            in.reset();
            assertTrue("Wrong chars", in.read() == (char) 6
                    && in.read() == (char) 7);
        } catch (IOException e) {
            fail("Exception during mark test 2:" + e);
        }

        in = new BufferedReader(new Support_StringReader(new String(chars)), 12);
        try {
            in.skip(6);
            in.mark(8);
            in.skip(7);
            in.reset();
            assertTrue("Wrong chars 2", in.read() == (char) 6
                    && in.read() == (char) 7);
        } catch (IOException e) {
            fail("Exception during mark test 3:" + e);
        }
    }

    public void test_markSupported() {
        // Test for method boolean java.io.BufferedReader.markSupported()
        br = new BufferedReader(new Support_StringReader(testString));
        assertTrue("markSupported returned false.", br.markSupported());
    }

    public void test_read() throws IOException {
        Support_ASimpleReader ssr = new Support_ASimpleReader(true);
        try {
            br = new BufferedReader(new Support_StringReader(testString));
            int r = br.read();
            assertTrue("Char read improperly", testString.charAt(0) == r);
            br = new BufferedReader(new Support_StringReader(new String(
                    new char[] { '\u8765' })));
            assertTrue("Wrong double byte character", br.read() == '\u8765');
        } catch (java.io.IOException e) {
            fail("Exception during read test");
        }

        char[] chars = new char[256];
        for (int i = 0; i < 256; i++)
            chars[i] = (char) i;
        Reader in = new BufferedReader(new Support_StringReader(new String(
                chars)), 12);
        try {
            assertEquals("Wrong initial char", 0, in.read()); // Fill the
            // buffer
            char[] buf = new char[14];
            in.read(buf, 0, 14); // Read greater than the buffer
            assertTrue("Wrong block read data", new String(buf)
                    .equals(new String(chars, 1, 14)));
            assertEquals("Wrong chars", 15, in.read()); // Check next byte
        } catch (IOException e) {
            fail("Exception during read test 2:" + e);
        }

        // regression test for HARMONY-841
        assertTrue(new BufferedReader(new CharArrayReader(new char[5], 1, 0), 2).read() == -1);

        br.close();
        br = new BufferedReader(ssr);
        try {
            br.read();
            fail("IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // Avoid IOException in tearDown().
        ssr.throwExceptionOnNextUse = false;
    }

    public void test_read$CII_Exception() throws Exception {
        br = new BufferedReader(new Support_StringReader(testString));
        try{
            br.read(new char[10], -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try{
            br.read(new char[10], 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try{
            br.read(new char[10], 10, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        //regression for HARMONY-831
        try{
            new BufferedReader(new PipedReader(), 9).read(new char[] {}, 7, 0);
            fail("should throw IndexOutOfBoundsException");
        }catch(IndexOutOfBoundsException e){
        }
    }

    public void test_readLine() throws IOException {
        String line;
        br = new BufferedReader(new Support_StringReader("Lorem\nipsum\rdolor sit amet..."));

        line = br.readLine();
        assertTrue("Test 1: Incorrect line written or read: " + line,
                line.equals("Lorem"));
        line = br.readLine();
        assertTrue("Test 2: Incorrect line written or read: " + line,
                line.equals("ipsum"));
        line = br.readLine();
        assertTrue("Test 3: Incorrect line written or read: " + line,
                line.equals("dolor sit amet..."));

        br.close();
        try {
            br.readLine();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_ready() throws IOException {
        Support_ASimpleReader ssr = new Support_ASimpleReader(true);
        try {
            br = new BufferedReader(new Support_StringReader(testString));
            assertTrue("Test 1: ready() returned false", br.ready());
        } catch (java.io.IOException e) {
            fail("Exception during ready test" + e.toString());
        }

        br.close();
        br = new BufferedReader(ssr);
        try {
            br.close();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // Avoid IOException in tearDown().
        ssr.throwExceptionOnNextUse = false;
    }

    public void test_skipJ() throws IOException {
        Support_ASimpleReader ssr = new Support_ASimpleReader(true);
        br = new BufferedReader(new Support_StringReader(testString));

        try {
            br.skip(-1);
            fail("Test 1: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }

        br.skip(500);
        char[] buf = new char[testString.length()];
        br.read(buf, 0, 500);
        assertTrue("Test 2: Failed to set skip properly.",
                testString.substring(500, 1000).equals(
                        new String(buf, 0, 500)));

        br.close();
        br = new BufferedReader(ssr);
        try {
            br.skip(1);
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // Avoid IOException in tearDown().
        ssr.throwExceptionOnNextUse = false;
    }

    public void testReadZeroLengthArray() throws IOException {
        br = new BufferedReader(new Support_StringReader("ABCDEF"));
        br.read();
        br.read();
        assertEquals(0, br.read(new char[6], 3, 0));
    }

    public void testSourceThrowsWithMark() throws IOException {
        br = new BufferedReader(new ThrowingReader(
                new StringReader("ABCDEFGHI"), 4));

        br.read();
        br.read();
        br.mark(10);
        br.read();
        br.read();

        try {
            br.read();
            fail();
        } catch (IOException fromThrowingReader) {
        }

        assertEquals('E', br.read());
        assertEquals('F', br.read());
    }

    protected void tearDown() {
        try {
            br.close();
        } catch (Exception e) {
        }
    }

    public void test_readLine_all_line_endings() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("1\r2\n3\r\n4"));
        assertEquals("1", r.readLine());
        assertEquals("2", r.readLine());
        assertEquals("3", r.readLine());
        assertEquals("4", r.readLine());
        assertNull(r.readLine());
    }

    public void test_readLine_interaction_with_read() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("1\r\n2"));
        assertEquals('1', r.read());
        assertEquals('\r', r.read());
        assertEquals("", r.readLine()); // The '\r' we read() didn't count.
        assertEquals("2", r.readLine());
        assertNull(r.readLine());
    }

    public void test_readLine_interaction_with_array_read_1() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("1\r\n2"));
        assertEquals(2, r.read(new char[2], 0, 2));
        assertEquals("", r.readLine()); // The '\r' we read() didn't count.
        assertEquals("2", r.readLine());
        assertNull(r.readLine());
    }

    public void test_readLine_interaction_with_array_read_2() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("1\r\n2"));
        assertEquals("1", r.readLine());
        char[] chars = new char[1];
        assertEquals(1, r.read(chars, 0, 1)); // This read skips the '\n'.
        assertEquals('2', chars[0]);
        assertNull(r.readLine());
    }

    public void test_readLine_interaction_with_skip() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("1\r\n2"));
        assertEquals(2, r.skip(2));
        assertEquals("", r.readLine()); // The '\r' we skip()ed didn't count.
        assertEquals("2", r.readLine());
        assertNull(r.readLine());
    }

    public void test_readLine_interaction_with_mark_and_reset() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("1\r\n2\n3"));
        assertEquals("1", r.readLine());
        r.mark(256);
        assertEquals('2', r.read()); // This read skips the '\n'.
        assertEquals("", r.readLine());
        r.reset(); // Now we're back half-way through the "\r\n".
        assertEquals("2", r.readLine());
        assertEquals("3", r.readLine());
        assertNull(r.readLine());
    }

    public void test_8778372() throws Exception {
        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        final Thread t = new Thread() {
          @Override public void run() {
              PrintWriter pw = new PrintWriter(new OutputStreamWriter(pos));
              pw.print("hello, world\r");
              pw.flush();
            }
        };
        t.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(pis));
        assertEquals("hello, world", br.readLine());
    }

    public void test_closeException() throws Exception {
        final IOException testException = new IOException("kaboom!");
        Reader thrower = new Reader() {
            @Override
            public int read(char cbuf[], int off, int len) throws IOException {
                // Not used
                return 0;
            }

            @Override
            public void close() throws IOException {
                throw testException;
            }
        };
        BufferedReader br = new BufferedReader(thrower);

        try {
            br.close();
            fail();
        } catch(IOException expected) {
            assertSame(testException, expected);
        }

        try {
            // Pre-openJdk8 BufferedReader#close() with exception wouldn't
            // reset the input reader to null. This would still allow ready()
            // to succeed.
            br.ready();
            fail();
        } catch(IOException expected) {
        }
    }
}
