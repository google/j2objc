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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import junit.framework.TestCase;
import tests.support.Support_ASimpleInputStream;
import tests.support.Support_PlatformFile;

public class OldBufferedInputStreamTest extends TestCase {

    public String fileName;
    private BufferedInputStream is;
    private FileInputStream isFile;
    public String fileString = "Test_All_Tests\nTest_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    public void test_ConstructorLjava_io_InputStream() {
        is = new BufferedInputStream(isFile);

        try {
            is.read();
        } catch (Exception e) {
            fail("Test 1: Read failed on a freshly constructed buffer.");
        }
    }

    public void test_ConstructorLjava_io_InputStreamI() throws IOException {
        // regression test for harmony-2407
        new testBufferedInputStream(null);
        assertNotNull(testBufferedInputStream.buf);
        testBufferedInputStream.buf = null;
        new testBufferedInputStream(null, 100);
        assertNotNull(testBufferedInputStream.buf);
    }

    static class testBufferedInputStream extends BufferedInputStream {
        static byte[] buf;
        testBufferedInputStream(InputStream is) throws IOException {
            super(is);
            buf = super.buf;
        }

        testBufferedInputStream(InputStream is, int size) throws IOException {
            super(is, size);
            buf = super.buf;
        }
    }

    public void test_available() {
        // Test for method int java.io.BufferedInputStream.available()
        try {
            assertTrue("Returned incorrect number of available bytes", is
                    .available() == fileString.length());
        } catch (IOException e) {
            fail("Exception during available test");
        }

        // Test that a closed stream throws an IOE for available()
        BufferedInputStream bis = new BufferedInputStream(
                new ByteArrayInputStream(new byte[] { 'h', 'e', 'l', 'l', 'o',
                        ' ', 't', 'i', 'm' }));
        int available = 0;
        try {
            available = bis.available();
            bis.close();
        } catch (IOException ex) {
            fail();
        }
        assertTrue(available != 0);

        try {
            bis.available();
            fail("Expected test to throw IOE.");
        } catch (IOException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Expected test to throw IOE not "
                    + ex.getClass().getName());
        }
    }

    public void test_close() throws IOException {
        is.close();

        try {
            is.read();
            fail("Test 1: IOException expected when reading after closing " +
                 "the stream.");
        } catch (IOException e) {
            // Expected.
        }

        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(true);
        is = new BufferedInputStream(sis);
        try {
            is.close();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sis.throwExceptionOnNextUse = false;
    }

    public void test_markI_reset() throws IOException {
        byte[] buf1 = new byte[100];
        byte[] buf2 = new byte[100];

        // Test 1: Check that reset fails if no mark has been set.
        try {
            is.reset();
            fail("Test 1: IOException expected if no mark has been set.");
        } catch (IOException e) {
            // Expected.
        }

        // Test 2: Check that mark / reset works when the mark is not invalidated.
        is.skip(10);
        is.mark(100);
        is.read(buf1, 0, buf1.length);
        is.reset();
        is.read(buf2, 0, buf2.length);
        is.reset();
        assertTrue("Test 2: Failed to mark correct position or reset failed.",
                new String(buf1, 0, buf1.length).equals(new String(buf2, 0, buf2.length)));

        // Tests 3 and 4: Check that skipping less than readlimit bytes does
        // not invalidate the mark.
        is.skip(10);
        try {
            is.reset();
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException " + e.getMessage());
        }
        is.read(buf2, 0, buf2.length);
        is.reset();
        assertTrue("Test 4: Failed to mark correct position, or reset failed.",
                new String(buf1, 0, buf1.length).equals(new String(buf2, 0, buf2.length)));

        // Test 8: Check that reset fails for a closed input stream.
        is.close();
        try {
            is.reset();
            fail("Test 8: IOException expected because the input stream is closed.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_read() throws IOException {
        int c = is.read();
        assertTrue("Test 1: Incorrect character read.",
                c == fileString.charAt(0));

        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }

        BufferedInputStream in = new BufferedInputStream(
                new ByteArrayInputStream(bytes), 5);

        // Read more bytes than are buffered.
        for (int i = 0; i < 10; i++) {
            assertEquals("Test 2: Incorrect byte read;", bytes[i], in.read());
        }

        in.close();
        try {
            in.read();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    @Override
    protected void setUp() throws IOException {
        fileName = System.getProperty("user.dir");
            String separator = System.getProperty("file.separator");
            if (fileName.charAt(fileName.length() - 1) == separator.charAt(0)) {
                fileName = Support_PlatformFile.getNewPlatformFile(fileName,
                        "input.tst");
            } else {
                fileName = Support_PlatformFile.getNewPlatformFile(fileName
                        + separator, "input.tst");
            }
            OutputStream fos = new FileOutputStream(fileName);
            fos.write(fileString.getBytes());
            fos.close();
            isFile = new FileInputStream(fileName);
        is = new BufferedInputStream(isFile);
    }

    @Override
    protected void tearDown() {
        try {
            is.close();
        } catch (Exception e) {
        }
        try {
            File f = new File(fileName);
            f.delete();
        } catch (Exception e) {
        }
    }
}
