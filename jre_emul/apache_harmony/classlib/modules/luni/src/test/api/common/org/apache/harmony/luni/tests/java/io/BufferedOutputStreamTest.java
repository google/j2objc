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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BufferedOutputStreamTest extends junit.framework.TestCase {

    private java.io.OutputStream os;

    java.io.ByteArrayOutputStream baos;

    java.io.ByteArrayInputStream bais;

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    /**
     * @tests java.io.BufferedOutputStream#BufferedOutputStream(java.io.OutputStream)
     */
    public void test_ConstructorLjava_io_OutputStream() throws IOException {
        baos = new java.io.ByteArrayOutputStream();
        os = new java.io.BufferedOutputStream(baos);
        os.write(fileString.getBytes(), 0, 500);
    }

    /**
     * @tests java.io.BufferedOutputStream#BufferedOutputStream(java.io.OutputStream,
     *        int)
     */
    public void test_ConstructorLjava_io_OutputStreamI() throws IOException {
        baos = new java.io.ByteArrayOutputStream();
        os = new java.io.BufferedOutputStream(baos, 1024);
        os.write(fileString.getBytes(), 0, 500);
    }

    public void test_flush_Constructor_NullStream() throws IOException {
        BufferedOutputStream buffos = new java.io.BufferedOutputStream(null);
        try {
            buffos.flush();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            buffos.close();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        buffos = new java.io.BufferedOutputStream(null, 10);
        try {
            buffos.flush();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            buffos.close();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            new java.io.BufferedOutputStream(null, 0);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            new java.io.BufferedOutputStream(null, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /**
     * @tests java.io.BufferedOutputStream#flush()
     */
    public void test_flush() throws IOException {
        baos = new ByteArrayOutputStream();
        os = new java.io.BufferedOutputStream(baos, 600);
        os.write(fileString.getBytes(), 0, 500);
        os.flush();
        assertEquals("Bytes not written after flush", 500,
                ((ByteArrayOutputStream) baos).size());
    }

    private static class MockOutputStream extends OutputStream {
        byte[] written;
        int count;

        public MockOutputStream(int size) {
            written = new byte[size];
            count = 0;
        }

        public void write(int b) {
            written[count++] = (byte) b;
        }

        public String getWritten() {
            return new String(written, 0, count);
        }
    }

    /**
     * @tests java.io.BufferedOutputStream#write(byte[], int, int)
     */
    public void test_write$BII() throws IOException {
        os = new BufferedOutputStream(baos = new ByteArrayOutputStream(), 512);
        os.write(fileString.getBytes(), 0, 500);
        bais = new ByteArrayInputStream(baos.toByteArray());
        assertEquals("Bytes written, not buffered", 0, bais.available());
        os.flush();
        bais = new ByteArrayInputStream(baos.toByteArray());
        assertEquals("Bytes not written after flush", 500, bais.available());
        os.write(fileString.getBytes(), 500, 513);
        bais = new ByteArrayInputStream(baos.toByteArray());
        assertTrue("Bytes not written when buffer full",
                bais.available() >= 1000);
        byte[] wbytes = new byte[1013];
        bais.read(wbytes, 0, 1013);
        assertEquals("Incorrect bytes written", new String(wbytes, 0,
                wbytes.length), fileString.substring(0, 1013));

        // regression test for HARMONY-4177
        MockOutputStream mos = new MockOutputStream(5);
        BufferedOutputStream bos = new BufferedOutputStream(mos, 3);
        bos.write("a".getBytes());
        bos.write("bcde".getBytes());
        assertEquals("Large data should be written directly", "abcde", mos
                .getWritten());
        mos = new MockOutputStream(4);
        bos = new BufferedOutputStream(mos, 3);
        bos.write("ab".getBytes());
        bos.write("cd".getBytes());
        assertEquals("Should flush before write", "ab", mos.getWritten());
    }

    /**
     * @tests java.io.BufferedOutputStream#write(byte[], int, int)
     */
    public void test_write_$BII_Exception() throws IOException {
        OutputStream bos = new BufferedOutputStream(new ByteArrayOutputStream());
        byte[] nullByteArray = null;
        byte[] byteArray = new byte[10];

        try {
            bos.write(nullByteArray, -1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, -1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(byteArray, -1, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, -1, 0);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, -1, 1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, 0, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, 0, byteArray.length + 1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, 1, byteArray.length);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, -1, byteArray.length);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, byteArray.length, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        bos.write(byteArray, byteArray.length, 0);
        try {
            bos.write(byteArray, byteArray.length, 1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        bos.write(byteArray, 0, 0);
        bos.write(byteArray, 0, 1);
        bos.write(byteArray, 1, byteArray.length - 1);
        bos.write(byteArray, 0, byteArray.length);

        try {
            bos.write(byteArray, 1, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        bos.write(byteArray, 1, 0);
        bos.write(byteArray, 1, 1);

        bos.write(byteArray, byteArray.length, 0);

        try {
            bos.write(byteArray, byteArray.length + 1, 0);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, byteArray.length + 1, 1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        bos.close();

        try {
            bos.write(byteArray, -1, -1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // expected IndexOutOfBoundsException
        }

        try {
            bos.write(null, -1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected NullPointerException.
        }

        try {
            bos.write(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected NullPointerException.
        }
    }

    /**
     * @tests java.io.BufferedOutputStream#write(byte[], int, int)
     */
    public void test_write_$BII_NullStream_NullArray() throws IOException {
        OutputStream bos = new BufferedOutputStream(null);
        byte[] nullByteArray = null;

        try {
            bos.write(nullByteArray, -1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, -1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.io.BufferedOutputStream#write(byte[], int, int)
     */
    public void test_write_$BII_NullStream_NullArray_Size() throws IOException {
        OutputStream bos = new BufferedOutputStream(null, 1);
        byte[] nullByteArray = null;

        try {
            bos.write(nullByteArray, -1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, -1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(nullByteArray, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.io.BufferedOutputStream#write(byte[], int, int)
     */
    public void test_write_$BII_NullStream() throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(null);
        byte[] byteArray = new byte[10];

        try {
            bos.write(byteArray, -1, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, 0, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, 1, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, -1, 0);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        bos.write(byteArray, 0, 0);

        bos.write(byteArray, 1, 0);

        bos.write(byteArray, byteArray.length, 0);

        try {
            bos.write(byteArray, byteArray.length + 1, 0);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, -1, 1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        bos.write(byteArray, 0, 1);
        bos.write(byteArray, 1, 1);

        bos.write(byteArray, 0, byteArray.length);

        try {
            bos.write(byteArray, byteArray.length + 1, 1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * @tests java.io.BufferedOutputStream#write(byte[], int, int)
     */
    public void test_write_$BII_NullStream_Size() throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(null, 1);
        byte[] byteArray = new byte[10];

        try {
            bos.write(byteArray, -1, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, 0, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, 1, -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, -1, 0);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        bos.write(byteArray, 0, 0);

        bos.write(byteArray, 1, 0);

        bos.write(byteArray, byteArray.length, 0);

        try {
            bos.write(byteArray, byteArray.length + 1, 0);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            bos.write(byteArray, -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(byteArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(byteArray, 0, byteArray.length);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(byteArray, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            bos.write(byteArray, byteArray.length + 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.io.BufferedOutputStream#write(int)
     */
    public void test_writeI() throws IOException {
        baos = new java.io.ByteArrayOutputStream();
        os = new java.io.BufferedOutputStream(baos);
        os.write('t');
        bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        assertEquals("Byte written, not buffered", 0, bais.available());
        os.flush();
        bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        assertEquals("Byte not written after flush", 1, bais.available());
        byte[] wbytes = new byte[1];
        bais.read(wbytes, 0, 1);
        assertEquals("Incorrect byte written", 't', wbytes[0]);
    }

    public void test_write_Close() throws IOException {
        BufferedOutputStream buffos = new BufferedOutputStream(
                new ByteArrayOutputStream());
        buffos.write(new byte[0]);
        try {
            buffos.write(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        byte[] buffer = "1234567890".getBytes();

        buffos.write(Integer.MIN_VALUE);
        buffos.write(Integer.MAX_VALUE);
        buffos.write(buffer, 0, 10);
        buffos.flush();

        buffos.close();
    }

    public void test_write_Scenario1() throws IOException {
        ByteArrayOutputStream byteArrayos = new ByteArrayOutputStream();
        ByteArrayInputStream byteArrayis = null;
        byte[] buffer = "1234567890".getBytes("UTF-8");

        BufferedOutputStream buffos = new BufferedOutputStream(byteArrayos, 10);
        buffos.write(buffer, 0, 10);
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 10, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 10, byteArrayis
                .available());
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }

        buffos.write(buffer, 0, 10);
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 20, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 20, byteArrayis
                .available());
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }

        buffos.write(buffer, 0, 10);
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 30, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 30, byteArrayis
                .available());
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }
    }

    public void test_write_Scenario2() throws IOException {
        ByteArrayOutputStream byteArrayos = new ByteArrayOutputStream();
        ByteArrayInputStream byteArrayis = null;
        byte[] buffer = "1234567890".getBytes("UTF-8");

        BufferedOutputStream buffos = new BufferedOutputStream(byteArrayos, 20);
        buffos.write(buffer, 0, 10);
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 0, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 10, byteArrayis
                .available());
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }

        byte[] buffer2 = new byte[] { 'a', 'b', 'c', 'd' };
        buffos.write(buffer2, 0, 4);
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 10, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 14, byteArrayis
                .available());
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }
        for (int i = 0; i < 4; i++) {
            assertEquals(buffer2[i], byteArrayis.read());
        }

        byte[] buffer3 = new byte[] { 'e', 'f', 'g', 'h', 'i' };
        buffos.write(buffer3, 0, 5);
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 14, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 19, byteArrayis
                .available());
        for (int i = 0; i < 10; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }
        for (int i = 0; i < 4; i++) {
            assertEquals(buffer2[i], byteArrayis.read());
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(buffer3[i], byteArrayis.read());
        }

        buffos.write(new byte[] { 'j', 'k' });
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 19, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 21, byteArrayis
                .available());

        buffos.close();
    }

    public void test_write_Scenario3() throws IOException {
        ByteArrayOutputStream byteArrayos = new ByteArrayOutputStream();
        ByteArrayInputStream byteArrayis = null;
        byte[] buffer = "1234567890".getBytes("UTF-8");

        BufferedOutputStream buffos = new BufferedOutputStream(byteArrayos, 5);
        buffos.write(buffer, 0, 4);
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 0, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 4, byteArrayis
                .available());
        for (int i = 0; i < 4; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }

        buffos.write(buffer, 0, 5);
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes written, not buffered", 9, byteArrayis.available());
        buffos.flush();
        byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray());
        assertEquals("Bytes not written after flush", 9, byteArrayis
                .available());
        for (int i = 0; i < 4; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(buffer[i], byteArrayis.read());
        }
    }

    // Regression test for flush on closed stream
    public void test_flush_on_closed_stream() throws IOException {
        BufferedOutputStream bos =
            new BufferedOutputStream(
                new ByteArrayOutputStream());
        bos.close();
        bos.flush(); // RI does not throw exception
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() throws IOException {
        if (bais != null) {
            bais.close();
        }
        if (os != null) {
            os.close();
        }
        if (baos != null) {
            baos.close();
        }
    }
}
