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

import java.io.BufferedWriter;
import java.io.IOException;
import tests.support.Support_ASimpleWriter;
import tests.support.Support_StringWriter;

public class OldBufferedWriterTest extends junit.framework.TestCase {

    BufferedWriter bw;

    Support_StringWriter sw;

    Support_ASimpleWriter ssw;

    public String testString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    public void test_ConstructorLjava_io_Writer() {
        bw = new BufferedWriter(sw);
        try {
            bw.write("Hi", 0, 2);
            assertTrue("Test 1: Buffering failed.", sw.toString().equals(""));
            bw.flush();
            assertEquals("Test 2: Incorrect value;", "Hi", sw.toString());
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException.");
        }
    }

    public void test_ConstructorLjava_io_WriterI() {
        try {
            bw = new BufferedWriter(sw, 0);
            fail("Test 1: IllegalArgumentException expected.");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }

        bw = new BufferedWriter(sw, 10);
        try {
            bw.write("Hi", 0, 2);
            assertTrue("Test 2: Buffering failed.", sw.toString().equals(""));
            bw.flush();
            assertEquals("Test 3: Incorrect value;", "Hi", sw.toString());
        } catch (IOException e) {
            fail("Test 4: Unexpected IOException.");
        }
    }

    public void test_close() {
        // Test for method void java.io.BufferedWriter.close()
        try {
            bw.close();
            bw.write(testString);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        assertFalse("Test 2: Write after close.", sw.toString().equals(testString));

        bw = new BufferedWriter(ssw);
        try {
            bw.close();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_flush() throws IOException {
        bw.write("This should not cause a flush");
        assertTrue("Test 1: Bytes written without flush.",
                sw.toString().equals(""));
        bw.flush();
        assertEquals("Test 2: Bytes not flushed.",
                "This should not cause a flush", sw.toString());

        bw.close();
        bw = new BufferedWriter(ssw);
        try {
            bw.flush();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_newLine() throws IOException {
        String separator = System.getProperty("line.separator");
        bw.write("Hello");
        bw.newLine();
        bw.write("World");
        bw.flush();
        assertTrue("Test 1: Incorrect string written: " + sw.toString(),
                sw.toString().equals("Hello" + separator + "World"));

        bw.close();
        bw = new BufferedWriter(ssw, 1);
        try {
            bw.newLine();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
   }

    public void test_write$CII() {
        // Test for method void java.io.BufferedWriter.write(char [], int, int)
        try {
            char[] testCharArray = testString.toCharArray();
            bw.write(testCharArray, 500, 1000);
            bw.flush();
            assertTrue("Incorrect string written", sw.toString().equals(
                    testString.substring(500, 1500)));

            int idx = sw.toString().length();
            bw.write(testCharArray, 0, testCharArray.length);
            assertEquals(idx + testCharArray.length, sw.toString().length());
            bw.write(testCharArray, 0, 0);
            assertEquals(idx + testCharArray.length, sw.toString().length());
            bw.write(testCharArray, testCharArray.length, 0);
            assertEquals(idx + testCharArray.length, sw.toString().length());
        } catch (Exception e) {
            fail("Exception during write test");
        }

    }

    public void test_write$CII_Exception() throws IOException {
        char[] nullCharArray = null;
        char[] charArray = testString.toCharArray();

        try {
            bw.write(nullCharArray, 0, 1);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        try {
            bw.write(charArray, -1, 0);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            bw.write(charArray, 0, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            bw.write(charArray, charArray.length + 1, 0);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            bw.write(charArray, charArray.length, 1);
            fail("Test 5: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            bw.write(charArray, 0, charArray.length + 1);
            fail("Test 6: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            bw.write(charArray, 1, charArray.length);
            fail("Test 7: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        bw.close();

        try {
            bw.write(charArray, 0, 1);
            fail("Test 7: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }

        bw = new BufferedWriter(ssw, charArray.length / 2);
        try {
            bw.write(charArray, 0, charArray.length);
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_writeI() throws IOException {
        bw.write('T');
        assertTrue("Test 1: Char written without flush.",
                sw.toString().equals(""));
        bw.flush();
        assertEquals("Test 2: Incorrect char written;",
                "T", sw.toString());

        bw.close();
        try {
            bw.write('E');
            fail("Test 3: IOException expected since the target writer is closed.");
        } catch (IOException e) {
            // Expected.
        }

        // IOException should be thrown when the buffer is full and data is
        // written out to the target writer.
        bw = new BufferedWriter(ssw, 1);
        bw.write('S');
        try {
            bw.write('T');
            fail("Test 4: IOException expected since the target writer throws it.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_writeLjava_lang_StringII() {
        // Test for method void java.io.BufferedWriter.write(java.lang.String,
        // int, int)
        try {
            bw.write(testString);
            bw.flush();
            assertTrue("Incorrect string written", sw.toString().equals(
                    testString));
        } catch (Exception e) {
            fail("Exception during write test");
        }
    }

    public void test_writeLjava_lang_StringII_Exception() throws IOException {

        bw.write((String) null , -1, -1);
        bw.write((String) null , -1, 0);
        bw.write((String) null , 0 , -1);
        bw.write((String) null , 0 , 0);

        try {
            bw.write((String) null, 0, 1);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        try {
            bw.write(testString, -1, 1);
            fail("Test 2: StringIndexOutOfBoundsException expected.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            bw.write(testString, 1, testString.length());
            fail("Test 3: StringIndexOutOfBoundsException expected.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected.
        }

        bw.close();

        try {
            bw.write(testString, 0, 1);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }

        bw = new BufferedWriter(ssw, testString.length() / 2);
        try {
            bw.write(testString, 0, testString.length());
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    protected void setUp() {
        sw = new Support_StringWriter();
        ssw = new Support_ASimpleWriter(true);
        bw = new BufferedWriter(sw, 500);
    }

    protected void tearDown() {
        ssw.throwExceptionOnNextUse = false;
        try {
            bw.close();
        } catch (Exception e) {
        }
    }
}
