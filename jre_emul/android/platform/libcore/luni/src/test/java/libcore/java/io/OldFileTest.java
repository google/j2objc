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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import junit.framework.TestCase;

public class OldFileTest extends TestCase {

    /** Location to store tests in */
    private File tempDirectory;

    /** Temp file that does exist */
    private File tempFile;

    /** File separator */
    private String slash = File.separator;

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_File\nTest_FileDescriptor\nTest_FileInputStream\nTest_FileNotFoundException\nTest_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    private static String platformId = "iOS";

    {
        // Delete all old temporary files
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        String[] files = tempDir.list();
        for (int i = 0; i < files.length; i++) {
            File f = new File(tempDir, files[i]);
            if (f.isDirectory()) {
                if (files[i].startsWith("hyts_resources"))
                    deleteTempFolder(f);
            }
            if (files[i].startsWith("hyts_") || files[i].startsWith("hyjar_"))
                new File(tempDir, files[i]).delete();
        }
    }

    private void deleteTempFolder(File dir) {
        String files[] = dir.list();
        for (int i = 0; i < files.length; i++) {
            File f = new File(dir, files[i]);
            if (f.isDirectory())
                deleteTempFolder(f);
            else {
                f.delete();
            }
        }
        dir.delete();

    }

    public void test_ConstructorLjava_io_FileLjava_lang_String() throws Exception {
        String error;
        String dirName = System.getProperty("java.io.tmpdir");
        System.setProperty("user.dir", dirName);

        File d = new File(dirName);
        File f = new File(d, "input.tst");
        if (!dirName.regionMatches((dirName.length() - 1), slash, 0, 1))
            dirName += slash;
        dirName += "input.tst";
        error = String.format("Test 1: Incorrect file created: %s; %s expected.", f.getPath(), dirName);
        assertTrue(error, f.getPath().equals(dirName));

        String fileName = null;
        try {
            f = new File(d, fileName);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
        }

        d = null;
        f = new File(d, "input.tst");
        error = String.format("Test 3: Incorrect file created: %s; %s expected.",
                f.getAbsolutePath(), dirName);
        assertTrue(error, f.getAbsolutePath().equals(dirName));

        // Regression test for Harmony-382
        File s = null;
        f = new File("/abc");
        d = new File(s, "/abc");
        assertEquals("Test 4: Incorrect file created;",
                f.getAbsolutePath(), d.getAbsolutePath());
    }

    public void test_ConstructorLjava_lang_StringLjava_lang_String() throws IOException {
        String dirName = null;
        String fileName = "input.tst";

        String userDir = System.getProperty("java.io.tmpdir");
        System.setProperty("user.dir", userDir);

        File f = new File(dirName, fileName);
        if (!userDir.regionMatches((userDir.length() - 1), slash, 0, 1))
            userDir += slash;
        userDir += "input.tst";
        String error = String.format("Test 1: Incorrect file created: %s; %s expected.",
                f.getAbsolutePath(), userDir);
        assertTrue(error, f.getAbsolutePath().equals(userDir));

        dirName = System.getProperty("java.io.tmpdir");
        fileName = null;
        try {
            f = new File(dirName, fileName);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        fileName = "input.tst";
        f = new File(dirName, fileName);
        assertTrue("Test 3: Incorrect file created.", f.getPath()
                .equals(userDir));

        // Regression test for Harmony-382
        String s = null;
        f = new File("/abc");
        File d = new File(s, "/abc");
        assertEquals("Test 4: Incorrect file created;", d.getAbsolutePath(), f
                .getAbsolutePath());
        assertEquals("Test3: Created Incorrect File", "/abc", f
                .getAbsolutePath());
    }

    public void test_createTempFileLjava_lang_StringLjava_lang_String() {
        try {
            // Create an illegal file prefix.
            char[] prefix = new char[255];
            Arrays.fill(prefix, 'a');
            File f3 = File.createTempFile(new String(prefix), null);
            f3.delete();
            fail("IOException not thrown");
        } catch (IOException e) {
            // java.io.IOException: open failed: ENAMETOOLONG (File name too long)
        }
    }

    public void test_renameToLjava_io_File() {
        String base = System.getProperty("java.io.tmpdir");
        File dir = new File(base, platformId);
        dir.mkdir();
        File f = new File(dir, "xxx.xxx");
        try {
            f.renameTo(null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
    }

    public void test_toURL3() throws MalformedURLException {
        File dir = new File(""); // current directory
        String newDirURL = dir.toURL().toString();
        assertTrue("Test 1: URL does not end with slash.",
                newDirURL.endsWith("/"));
    }

    /* Needs ProcessBuilder.
    public void test_deleteOnExit() throws IOException, InterruptedException {
        String cts = System.getProperty("java.io.tmpdir");
        File dir = new File(cts + "/hello");
        dir.mkdir();
        assertTrue(dir.exists());
        File subDir = new File(cts + "/hello/world");
        subDir.mkdir();
        assertTrue(subDir.exists());

        URL url = getClass().getResource("/HelloWorld.txt");
        String classPath = url.toString();
        int idx = classPath.indexOf("!");
        assertTrue("could not find the path of the test jar/apk", idx > 0);
        classPath = classPath.substring(9, idx); // cutting off jar:file:

        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("-cp");
        builder.command().add(System.getProperty("java.class.path"));
        builder.command().add("tests.support.Support_DeleteOnExitTest");
        builder.command().add(dir.getAbsolutePath());
        builder.command().add(subDir.getAbsolutePath());
        execAndGetOutput(builder);

        assertFalse(dir.exists());
        assertFalse(subDir.exists());
    }
    */

    protected void setUp() throws Exception {
        super.setUp();

        // Make sure that system properties are set correctly
        String userDir = System.getProperty("java.io.tmpdir");
        if (userDir == null)
            throw new Exception("System property java.io.tmpdir not defined.");
        System.setProperty("java.io.tmpdir", userDir);

        /** Setup the temporary directory */
        if (!userDir.regionMatches((userDir.length() - 1), slash, 0, 1))
            userDir += slash;
        tempDirectory = new File(userDir + "tempDir"
                + String.valueOf(System.currentTimeMillis()));
        if (!tempDirectory.mkdir())
            System.out.println("Setup for OldFileTest failed (1).");

        /** Setup the temporary file */
        tempFile = new File(tempDirectory, "tempfile");
        FileOutputStream tempStream;
        try {
            tempStream = new FileOutputStream(tempFile.getPath(), false);
            tempStream.close();
        } catch (IOException e) {
            System.out.println("Setup for OldFileTest failed (2).");
            return;
        }
    }

    protected void tearDown() {
        if (tempFile.exists() && !tempFile.delete())
            System.out
                    .println("OldFileTest.tearDown() failed, could not delete file!");
        if (!tempDirectory.delete())
            System.out
                    .println("OldFileTest.tearDown() failed, could not delete directory!");
    }
}
