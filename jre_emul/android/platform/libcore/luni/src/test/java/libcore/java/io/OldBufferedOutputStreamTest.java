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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import tests.support.Support_OutputStream;

public class OldBufferedOutputStreamTest extends junit.framework.TestCase {

    private java.io.OutputStream os;

    java.io.ByteArrayOutputStream baos;

    java.io.ByteArrayInputStream bais;

    Support_OutputStream sos;

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    public void test_ConstructorLjava_io_OutputStream() {
        try {
            baos = new java.io.ByteArrayOutputStream();
            os = new java.io.BufferedOutputStream(baos);
            os.write(fileString.getBytes(), 0, 500);
        } catch (java.io.IOException e) {
            fail("Constructor test failed");
        }

    }

    public void test_ConstructorLjava_io_OutputStreamI() {
        baos = new java.io.ByteArrayOutputStream();

        try {
            os = new java.io.BufferedOutputStream(baos, -1);
            fail("Test 1: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            os = new java.io.BufferedOutputStream(baos, 1024);
            os.write(fileString.getBytes(), 0, 500);
        } catch (java.io.IOException e) {
            fail("Test 2: Unexpected IOException.");
        }
    }

    public void test_flush() throws IOException {
        baos = new ByteArrayOutputStream();
        os = new java.io.BufferedOutputStream(baos, 600);
        os.write(fileString.getBytes(), 0, 500);
        os.flush();
        assertEquals("Test 1: Bytes not written after flush;",
                500, ((ByteArrayOutputStream) baos).size());
        os.close();

        sos = new Support_OutputStream(true);
        os = new BufferedOutputStream(sos, 10);
        try {
            os.flush();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // To avoid exception during tearDown().
        sos.setThrowsException(false);
    }

    public void test_write$BII() throws IOException {
        os = new java.io.BufferedOutputStream(
                baos = new java.io.ByteArrayOutputStream(),512);
        os.write(fileString.getBytes(), 0, 500);
        bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        assertEquals("Test 1: Bytes written, not buffered;",
                0, bais.available());
        os.flush();
        bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        assertEquals("Test 2: Bytes not written after flush;",
                500, bais.available());
        os.write(fileString.getBytes(), 500, 513);
        bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        assertTrue("Test 3: Bytes not written when buffer full.",
                bais.available() >= 1000);
        byte[] wbytes = new byte[1013];
        bais.read(wbytes, 0, 1013);
        assertTrue("Test 4: Incorrect bytes written or read.",
                fileString.substring(0, 1013).equals(
                        new String(wbytes, 0, wbytes.length)));
        os.close();

        sos = new Support_OutputStream(true);
        os = new BufferedOutputStream(sos, 10);
        try {
            os.write(fileString.getBytes(), 0, 500);
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // To avoid exception during tearDown().
        sos.setThrowsException(false);
    }

    public void test_write$BII_Exception() throws IOException {
        OutputStream bos = new BufferedOutputStream(new ByteArrayOutputStream());
        byte[] nullByteArray = null;
        byte[] byteArray = new byte[10];

        try {
            bos.write(nullByteArray, 0, 1);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        try {
            bos.write(byteArray, -1, 1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            bos.write(byteArray, 0, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            bos.write(byteArray, 1, 10);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void test_writeI() throws IOException {
        baos = new java.io.ByteArrayOutputStream();
        os = new java.io.BufferedOutputStream(baos);
        os.write('t');
        bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        assertEquals("Test 1: Byte written, not buffered;",
                0, bais.available());
        os.flush();
        bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        assertEquals("Test 2: Byte not written after flush;",
                1, bais.available());
        byte[] wbytes = new byte[1];
        bais.read(wbytes, 0, 1);
        assertEquals("Test 3: Incorrect byte written or read;",
                't', wbytes[0]);
        os.close();

        sos = new Support_OutputStream(true);
        os = new BufferedOutputStream(sos, 1);
        os.write('t');
        try {
            // Exception is only thrown when the buffer is flushed.
            os.write('e');
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // To avoid exception during tearDown().
        sos.setThrowsException(false);
    }

    protected void tearDown() {
        try {
            if (bais != null)
                bais.close();
            if (os != null)
                os.close();
            if (baos != null)
                baos.close();
        } catch (Exception e) {
            System.out.println("Exception during tearDown" + e.toString());
        }
    }
}
