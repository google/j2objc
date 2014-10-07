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
import java.io.FilterOutputStream;
import java.io.IOException;
import tests.support.Support_OutputStream;

public class OldFilterOutputStreamTest extends junit.framework.TestCase {

    private java.io.FilterOutputStream os;

    java.io.ByteArrayOutputStream bos;

    java.io.ByteArrayInputStream bis;

    byte[] ibuf = new byte[4096];

    private final String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    private final int testLength = fileString.length();


    public void test_ConstructorLjava_io_OutputStream() {
        // Test for method java.io.FilterOutputStream(java.io.OutputStream)
        try {
            bos = new ByteArrayOutputStream();
            os = new FilterOutputStream(bos);
            os.write('t');
        } catch (java.io.IOException e) {
            fail("Constructor test failed : " + e.getMessage());
        }
    }

    public void test_close() throws IOException {
        Support_OutputStream sos = new Support_OutputStream();
        os = new FilterOutputStream(sos);
        os.close();

        try {
            os.write(42);
        } catch (java.io.IOException e) {
            fail("Test 1: Unexpected IOException.");
        }

        sos.setThrowsException(true);
        try {
            os.write(42);
            fail("Test 2: IOException expected.");
        } catch (java.io.IOException e) {
            // Expected.
        }

        os = new FilterOutputStream(sos);
        try {
            os.close();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_flush() throws IOException {
        Support_OutputStream sos = new Support_OutputStream(550);
        os = new FilterOutputStream(sos);
        os.write(fileString.getBytes(), 0, 500);
        os.flush();
        assertEquals("Test 1: Bytes not written after flush;",
                500, sos.size());

        sos.setThrowsException(true);
        try {
            os.flush();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sos.setThrowsException(false);
    }

    public void test_write$B() throws IOException {
        Support_OutputStream sos = new Support_OutputStream(testLength);
        os = new FilterOutputStream(sos);
        os.write(fileString.getBytes());

        bis = new ByteArrayInputStream(sos.toByteArray());
        assertTrue("Test 1: Bytes have not been written.",
                bis.available() == testLength);
        byte[] wbytes = new byte[testLength];
        bis.read(wbytes, 0, testLength);
        assertTrue("Test 2: Incorrect bytes written or read.",
                fileString.equals(new String(wbytes)));

        try {
            // Support_OutputStream throws an IOException if the internal
            // buffer is full, which it should be now.
            os.write(42);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_write$BII() throws IOException {
        Support_OutputStream sos = new Support_OutputStream(testLength);
        os = new FilterOutputStream(sos);
        os.write(fileString.getBytes(), 10, testLength - 10);

        bis = new ByteArrayInputStream(sos.toByteArray());
        assertTrue("Test 1: Bytes have not been written.",
                bis.available() == testLength - 10);
        byte[] wbytes = new byte[testLength - 10];
        bis.read(wbytes);
        assertTrue("Test 2: Incorrect bytes written or read.",
                fileString.substring(10).equals(new String(wbytes)));

        try {
            // Support_OutputStream throws an IOException if the internal
            // buffer is full, which it should be eventually.
            os.write(fileString.getBytes());
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_write$BII_Exception() throws IOException {
        Support_OutputStream sos = new Support_OutputStream(testLength);
        os = new FilterOutputStream(sos);
        byte[] buf = new byte[10];

        try {
            os.write(buf, -1, 1);
            fail("IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            os.write(buf, 0, -1);
            fail("IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            os.write(buf, 10, 1);
            fail("IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
    }

    public void test_writeI() throws IOException {
        Support_OutputStream sos = new Support_OutputStream(1);
        os = new FilterOutputStream(sos);
        os.write(42);

        bis = new ByteArrayInputStream(sos.toByteArray());
        assertTrue("Test 1: Byte has not been written.",
                bis.available() == 1);
        assertEquals("Test 2: Incorrect byte written or read;",
                42, bis.read());

        try {
            // Support_OutputStream throws an IOException if the internal
            // buffer is full, which it should be now.
            os.write(42);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    protected void tearDown() {
        try {
            if (bos != null)
                bos.close();
            if (bis != null)
                bis.close();
            if (os != null)
                os.close();
        } catch (Exception e) {
        }
    }
}
