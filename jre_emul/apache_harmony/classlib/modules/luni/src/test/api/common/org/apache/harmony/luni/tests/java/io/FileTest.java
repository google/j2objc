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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.TestCase;

public class FileTest extends TestCase {

    private static String platformId = "JDK";

    private static void deleteTempFolder(File dir) {
        String files[] = dir.list();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                if (f.isDirectory()) {
                    deleteTempFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }

    private static String addTrailingSlash(String path) {
        if (File.separatorChar == path.charAt(path.length() - 1)) {
            return path;
        }
        return path + File.separator;
    }

    /** Location to store tests in */
    private File tempDirectory;

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_File\nTest_FileDescriptor\nTest_FileInputStream\nTest_FileNotFoundException\nTest_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    protected void setUp() throws IOException {
        /** Setup the temporary directory */
        tempDirectory = new File(addTrailingSlash(System.getProperty("java.io.tmpdir")) + "harmony-test-" + getClass().getSimpleName() + File.separator);
        tempDirectory.mkdirs();
    }

    protected void tearDown() {
        if (tempDirectory != null) {
            deleteTempFolder(tempDirectory);
            tempDirectory = null;
        }
    }

    /**
     * @tests java.io.File#File(java.io.File, java.lang.String)
     */
    public void test_ConstructorLjava_io_FileLjava_lang_String0() {
        File f = new File(tempDirectory.getPath(), "input.tst");
        assertEquals("Created Incorrect File ", addTrailingSlash(tempDirectory.getPath()) + "input.tst", f.getPath());
    }

    public void test_ConstructorLjava_io_FileLjava_lang_String1() {
        try {
            new File(tempDirectory, null);
            fail("NullPointerException Not Thrown.");
        } catch (NullPointerException e) {
        }
    }

    public void test_ConstructorLjava_io_FileLjava_lang_String2() throws IOException {
        File f = new File((File)null, "input.tst");
        assertEquals("Created Incorrect File",
                new File("input.tst").getAbsolutePath(),
                f.getAbsolutePath());
    }

    public void test_ConstructorLjava_io_FileLjava_lang_String3() {
        // Regression test for HARMONY-382
        File f = new File("/abc");
        File d = new File((File)null, "/abc");
        assertEquals("Test3: Created Incorrect File",
                     d.getAbsolutePath(), f.getAbsolutePath());
    }

    public void test_ConstructorLjava_io_FileLjava_lang_String4() {
        // Regression test for HARMONY-21
        File path = new File("/dir/file");
        File root = new File("/");
        File file = new File(root, "/dir/file");
        assertEquals("Assert 1: wrong path result ", path.getPath(), file
                .getPath());
        assertFalse("Assert 1.1: path absolute ", new File("\\\\\\a\b").isAbsolute());
    }

    public void test_ConstructorLjava_io_FileLjava_lang_String5() {
        // Test data used in a few places below
        String dirName = tempDirectory.getPath();
        String fileName = "input.tst";

        // Check filename is preserved correctly
        File d = new File(dirName);
        File f = new File(d, fileName);
        dirName = addTrailingSlash(dirName);
        dirName += fileName;
        assertEquals("Assert 1: Created incorrect file ",
                     dirName, f.getPath());

        // Check null argument is handled
        try {
            f = new File(d, null);
            fail("Assert 2: NullPointerException not thrown.");
        } catch (NullPointerException e) {
            // Expected.
        }
    }

    public void test_ConstructorLjava_io_FileLjava_lang_String6() {
        // Regression for HARMONY-46
        File f1 = new File("a");
        File f2 = new File("a/");
        assertEquals("Trailing slash file name is incorrect", f1, f2);
    }

    /**
     * @tests java.io.File#File(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        String fileName = null;
        try {
            new File(fileName);
            fail("NullPointerException Not Thrown.");
        } catch (NullPointerException e) {
            // Expected
        }

        fileName = addTrailingSlash(tempDirectory.getPath());
        fileName += "input.tst";

        File f = new File(fileName);
        assertEquals("Created incorrect File", fileName, f.getPath());
    }

    /**
     * @tests java.io.File#File(java.lang.String, java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_String() throws IOException {
        String dirName = null;
        String fileName = "input.tst";
        File f = new File(dirName, fileName);
        assertEquals("Test 1: Created Incorrect File",
                new File("input.tst").getAbsolutePath(),
                f.getAbsolutePath());

        dirName = tempDirectory.getPath();
        fileName = null;
        try {
            f = new File(dirName, fileName);
            fail("NullPointerException Not Thrown.");
        } catch (NullPointerException e) {
            // Expected
        }

        fileName = "input.tst";
        f = new File(dirName, fileName);
        assertEquals("Test 2: Created Incorrect File",
                addTrailingSlash(tempDirectory.getPath()) + "input.tst",
                f.getPath());

        // Regression test for HARMONY-382
        String s = null;
        f = new File("/abc");
        File d = new File(s, "/abc");
        assertEquals("Test3: Created Incorrect File", d.getAbsolutePath(), f
                .getAbsolutePath());
    }

    /**
     * @tests java.io.File#File(java.lang.String, java.lang.String)
     */
    public void test_Constructor_String_String_112270() {
        File ref1 = new File("/dir1/file1");

        File file1 = new File("/", "/dir1/file1");
        assertEquals("wrong result 1", ref1.getPath(), file1.getPath());
        File file2 = new File("/", "//dir1/file1");
        assertEquals("wrong result 2", ref1.getPath(), file2.getPath());

        File ref2 = new File("/lib/content-types.properties");
        File file5 = new File("/", "lib/content-types.properties");
        assertEquals("wrong result 5", ref2.getPath(), file5.getPath());
    }

    /**
     * @tests java.io.File#File(java.io.File, java.lang.String)
     */
    public void test_Constructor_File_String_112270() {
        File ref1 = new File("/dir1/file1");

        File root = new File("/");
        File file1 = new File(root, "/dir1/file1");
        assertEquals("wrong result 1", ref1.getPath(), file1.getPath());
        File file2 = new File(root, "//dir1/file1");
        assertEquals("wrong result 2", ref1.getPath(), file2.getPath());

        File ref2 = new File("/lib/content-types.properties");
        File file5 = new File(root, "lib/content-types.properties");
        assertEquals("wrong result 5", ref2.getPath(), file5.getPath());
    }

    /**
     * @tests java.io.File#File(java.net.URI)
     *
    TODO(user): enable when java.net support is implemented.
    public void test_ConstructorLjava_net_URI() throws URISyntaxException {
        URI uri = null;
        try {
            new File(uri);
            fail("NullPointerException Not Thrown.");
        } catch (NullPointerException e) {
            // Expected
        }

        // invalid file URIs
        String[] uris = new String[] { "mailto:user@domain.com", // not
                // hierarchical
                "ftp:///path", // not file scheme
                "//host/path/", // not absolute
                "file://host/path", // non empty authority
                "file:///path?query", // non empty query
                "file:///path#fragment", // non empty fragment
                "file:///path?", "file:///path#" };

        for (int i = 0; i < uris.length; i++) {
            uri = new URI(uris[i]);
            try {
                new File(uri);
                fail("Expected IllegalArgumentException for new File(" + uri
                        + ")");
            } catch (IllegalArgumentException e) {
                // Expected
            }
        }

        // a valid File URI
        File f = new File(new URI("file:///pa%20th/another\u20ac/pa%25th"));
        assertTrue("Created incorrect File " + f.getPath(), f.getPath().equals(
                File.separator + "pa th" + File.separator + "another\u20ac" + File.separator + "pa%th"));
    }
    */

    /**
     * @tests java.io.File#canRead()
     */
    public void test_canRead() throws IOException {
        // canRead only returns if the file exists so cannot be fully tested.
        File f = new File(tempDirectory, platformId + "canRead.tst");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.close();
            assertTrue("canRead returned false", f.canRead());
        } finally {
            f.delete();
        }
    }

    /**
     * @tests java.io.File#canWrite()
     */
    public void test_canWrite() throws IOException {
        // canWrite only returns if the file exists so cannot be fully tested.
        File f = new File(tempDirectory, platformId + "canWrite.tst");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.close();
            assertTrue("canWrite returned false", f.canWrite());
        } finally {
            f.delete();
        }
    }

    /**
     * @tests java.io.File#compareTo(java.io.File)
     */
    public void test_compareToLjava_io_File() {
        File f1 = new File("thisFile.file");
        File f2 = new File("thisFile.file");
        File f3 = new File("thatFile.file");
        assertEquals("Equal files did not answer zero for compareTo", 0, f1
                .compareTo(f2));
        assertTrue("f3.compareTo(f1) did not result in value < 0", f3
                .compareTo(f1) < 0);
        assertTrue("f1.compareTo(f3) did not result in value > 0", f1
                .compareTo(f3) > 0);
    }

    /**
     * @tests java.io.File#createNewFile()
     */
    public void test_createNewFile_EmptyString() {
        File f = new File("");
        try {
            f.createNewFile();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests java.io.File#createNewFile()
     */
    public void test_createNewFile() throws IOException {
        String base = tempDirectory.getPath();
        boolean dirExists = true;
        int numDir = 1;
        File dir = new File(base, String.valueOf(numDir));
        // Making sure that the directory does not exist.
        while (dirExists) {
            // If the directory exists, add one to the directory number
            // (making it a new directory name.)
            if (dir.exists()) {
                numDir++;
                dir = new File(base, String.valueOf(numDir));
            } else {
                dirExists = false;
            }
        }

        // Test for trying to create a file in a directory that does not
        // exist.
        try {
            // Try to create a file in a directory that does not exist
            File f1 = new File(dir, "tempfile.tst");
            f1.createNewFile();
            fail("IOException not thrown");
        } catch (IOException e) {
            // Expected
        }

        dir.mkdir();

        File f1 = new File(dir, "tempfile.tst");
        File f2 = new File(dir, "tempfile.tst");
        f1.delete();
        f2.delete();
        dir.delete();
        assertFalse("File Should Not Exist", f1.isFile());
        f1.createNewFile();
        assertTrue("File Should Exist.", f1.isFile());
        assertTrue("File Should Exist.", f2.isFile());
        String dirName = f1.getParent();
        if (!dirName.endsWith(File.separator)) {
            dirName += File.separator;
        }
        assertEquals("File Saved To Wrong Directory.",
                     dir.getPath() + File.separator, dirName);
        assertEquals("File Saved With Incorrect Name.", "tempfile.tst",
                     f1.getName());

        // Test for creating a file that already exists.
        assertFalse("File Already Exists, createNewFile Should Return False.",
                f2.createNewFile());

        // Test create an illegal file
        String sep = File.separator;
        f1 = new File(sep + "..");
        try {
            f1.createNewFile();
            fail("should throw IOE");
        } catch (IOException e) {
            // expected;
        }
        f1 = new File(sep + "a" + sep + ".." + sep + ".." + sep);
        try {
            f1.createNewFile();
            fail("should throw IOE");
        } catch (IOException e) {
            // expected;
        }

        // This test is invalid. createNewFile should return false
        // not IOE when the file exists (in this case it exists and is
        // a directory). TODO: We should probably replace this test
        // with some that cover this behaviour. It might even be
        // different on unix and windows since it directly reflects
        // the open syscall behaviour.
        //
        // // Test create an exist path
        // f1 = new File(base);
        // try {
        // assertFalse(f1.createNewFile());
        // fail("should throw IOE");
        // } catch (IOException e) {
        // // expected;
        // }
    }

    /**
     * @tests java.io.File#createTempFile(java.lang.String, java.lang.String)
     */
    public void test_createTempFileLjava_lang_StringLjava_lang_String()
            throws IOException {
        // Error protection against using a suffix without a "."?
        File f1 = null;
        File f2 = null;
        try {
            f1 = File.createTempFile("harmony-test-FileTest_tempFile_abc", ".tmp");
            f2 = File.createTempFile("harmony-test-FileTest_tempFile_tf", null);

            String fileLocation = addTrailingSlash(f1.getParent());

            String tempDir = addTrailingSlash(System.getProperty("java.io.tmpdir"));

            assertEquals(
                    "File did not save to the default temporary-file location.",
                    tempDir, fileLocation);

            // Test to see if correct suffix was used to create the tempfile.
            File currentFile;
            String fileName;
            // Testing two files, one with suffix ".tmp" and one with null
            for (int i = 0; i < 2; i++) {
                currentFile = i == 0 ? f1 : f2;
                fileName = currentFile.getPath();
                assertTrue("File Created With Incorrect Suffix.", fileName
                        .endsWith(".tmp"));
            }

            // Tests to see if the correct prefix was used to create the
            // tempfiles.
            fileName = f1.getName();
            assertTrue("Test 1: File Created With Incorrect Prefix.", fileName
                    .startsWith("harmony-test-FileTest_tempFile_abc"));
            fileName = f2.getName();
            assertTrue("Test 2: File Created With Incorrect Prefix.", fileName
                    .startsWith("harmony-test-FileTest_tempFile_tf"));

            // Tests for creating a tempfile with a filename shorter than 3
            // characters.
            try {
                File f3 = File.createTempFile("ab", ".tst");
                f3.delete();
                fail("IllegalArgumentException Not Thrown.");
            } catch (IllegalArgumentException e) {
                // Expected
            }
            try {
                File f3 = File.createTempFile("a", ".tst");
                f3.delete();
                fail("IllegalArgumentException Not Thrown.");
            } catch (IllegalArgumentException e) {
                // Expected
            }
            try {
                File f3 = File.createTempFile("", ".tst");
                f3.delete();
                fail("IllegalArgumentException Not Thrown.");
            } catch (IllegalArgumentException e) {
                // Expected
            }
        } finally {
            if (f1 != null) {
                f1.delete();
            }
            if (f2 != null) {
                f2.delete();
            }
        }
    }

    /**
     * @tests java.io.File#createTempFile(java.lang.String, java.lang.String,
     *        java.io.File)
     */
    public void test_createTempFileLjava_lang_StringLjava_lang_StringLjava_io_File()
            throws IOException {
        File f1 = null;
        File f2 = null;
        String base = System.getProperty("java.io.tmpdir");
        try {
            // Test to make sure that the tempfile was saved in the correct
            // location and with the correct prefix/suffix.
            f1 = File.createTempFile("harmony-test-FileTest_tempFile2_tf", null, null);
            File dir = new File(base);
            f2 = File.createTempFile("harmony-test-FileTest_tempFile2_tf", ".tmp", dir);
            File currentFile;
            String fileLocation;
            String fileName;
            for (int i = 0; i < 2; i++) {
                currentFile = i == 0 ? f1 : f2;
                fileLocation = addTrailingSlash(currentFile.getParent());
                base = addTrailingSlash(base);
                assertEquals(
                        "File not created in the default temporary-file location.",
                        base, fileLocation);
                fileName = currentFile.getName();
                assertTrue("File created with incorrect suffix.", fileName
                        .endsWith(".tmp"));
                assertTrue("File created with incorrect prefix.", fileName
                        .startsWith("harmony-test-FileTest_tempFile2_tf"));
                currentFile.delete();
            }

            // Test for creating a tempfile in a directory that does not exist.
            int dirNumber = 1;
            boolean dirExists = true;
            // Set dir to a non-existent directory inside the temporary
            // directory
            dir = new File(base, String.valueOf(dirNumber));
            // Making sure that the directory does not exist.
            while (dirExists) {
                // If the directory exists, add one to the directory number
                // (making it
                // a new directory name.)
                if (dir.exists()) {
                    dirNumber++;
                    dir = new File(base, String.valueOf(dirNumber));
                } else {
                    dirExists = false;
                }
            }
            try {
                // Try to create a file in a directory that does not exist
                File f3 = File.createTempFile("harmony-test-FileTest_tempFile2_tf", null, dir);
                f3.delete();
                fail("IOException not thrown");
            } catch (IOException e) {
                // Expected
            }
            dir.delete();

            // Tests for creating a tempfile with a filename shorter than 3
            // characters.
            try {
                File f4 = File.createTempFile("ab", null, null);
                f4.delete();
                fail("IllegalArgumentException not thrown.");
            } catch (IllegalArgumentException e) {
                // Expected
            }
            try {
                File f4 = File.createTempFile("a", null, null);
                f4.delete();
                fail("IllegalArgumentException not thrown.");
            } catch (IllegalArgumentException e) {
                // Expected
            }
            try {
                File f4 = File.createTempFile("", null, null);
                f4.delete();
                fail("IllegalArgumentException not thrown.");
            } catch (IllegalArgumentException e) {
                // Expected
            }
        } finally {
            if (f1 != null) {
                f1.delete();
            }
            if (f2 != null) {
                f1.delete();
            }
        }
    }

    /**
     * @tests java.io.File#delete()
     */
    public void test_delete() throws IOException {
        File dir = new File(tempDirectory, platformId
                + "filechk");
        dir.mkdir();
        assertTrue("Directory does not exist", dir.exists());
        assertTrue("Directory is not directory", dir.isDirectory());
        File f = new File(dir, "filechk.tst");
        FileOutputStream fos = new FileOutputStream(f);
        fos.close();
        assertTrue("Error Creating File For Delete Test", f.exists());
        dir.delete();
        assertTrue("Directory Should Not Have Been Deleted.", dir.exists());
        f.delete();
        assertTrue("File Was Not Deleted", !f.exists());
        dir.delete();
        assertTrue("Directory Was Not Deleted", !dir.exists());
    }

    // GCH
    // TODO : This test passes on Windows but fails on Linux with a
    // java.lang.NoClassDefFoundError. Temporarily removing from the test
    // suite while I investigate the cause.
    // /**
    // * @tests java.io.File#delete()
    // */
    // public void test_delete() {
    // File f1 = new File(System.getProperty("java.io.tmpdir"), platformId
    // + "delete.tst");
    // try {
    // FileOutputStream fos = new FileOutputStream(f1);
    // fos.close();
    // } catch (IOException e) {
    // fail("Unexpected IOException During Test : " + e.getMessage());
    // }
    // assertTrue("File Should Exist.", f1.exists());
    //
    // try {
    // Support_Exec.execJava(new String[] {
    // "tests.support.Support_deleteTest", f1.getPath() },
    // null, true);
    // } catch (IOException e) {
    // fail("Unexpected IOException During Test + " + e.getMessage());
    // } catch (InterruptedException e) {
    // fail("Unexpected InterruptedException During Test: " + e);
    // }
    //
    // boolean gone = !f1.exists();
    // f1.delete();
    // assertTrue("File Should Already Be Deleted.", gone);
    // }

    /**
     * @tests java.io.File#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() throws IOException {
        File f1 = new File("filechk.tst");
        File f2 = new File("filechk.tst");
        File f3 = new File("xxxx");

        assertTrue("Equality test failed", f1.equals(f2));
        assertTrue("Files Should Not Return Equal.", !f1.equals(f3));

        f3 = new File("FiLeChK.tst");
        assertTrue("Files Should NOT Return Equal.", !f1.equals(f3));

        f1 = new File(tempDirectory, "casetest.tmp");
        f2 = new File(tempDirectory, "CaseTest.tmp");
        new FileOutputStream(f1).close(); // create the file
        if (f1.equals(f2)) {
            try {
                FileInputStream fis = new FileInputStream(f2);
                fis.close();
            } catch (IOException e) {
                fail("File system is case sensitive");
            }
        } else {
            boolean exception = false;
            try {
                FileInputStream fis = new FileInputStream(f2);
                fis.close();
            } catch (IOException e) {
                exception = true;
            }
            assertTrue("File system is case insensitive", exception);
        }
        f1.delete();
    }

    /**
     * @tests java.io.File#exists()
     */
    public void test_exists() throws IOException {
        File f = new File(tempDirectory, platformId
                + "exists.tst");
        assertTrue("Exists returned true for non-existent file", !f.exists());
        FileOutputStream fos = new FileOutputStream(f);
        fos.close();
        assertTrue("Exists returned false file", f.exists());
        f.delete();
    }

    /**
     * @tests java.io.File#getAbsoluteFile()
     */
    public void test_getAbsoluteFile() {
        String base = addTrailingSlash(tempDirectory.getPath());
        File f = new File(base, "temp.tst");
        File f2 = f.getAbsoluteFile();
        assertEquals("Test 1: Incorrect File Returned.", 0, f2.compareTo(f
                .getAbsoluteFile()));
        f = new File(base + "Temp" + File.separator + File.separator + "temp.tst");
        f2 = f.getAbsoluteFile();
        assertEquals("Test 2: Incorrect File Returned.", 0, f2.compareTo(f
                .getAbsoluteFile()));
        f = new File(base + File.separator + ".." + File.separator + "temp.tst");
        f2 = f.getAbsoluteFile();
        assertEquals("Test 3: Incorrect File Returned.", 0, f2.compareTo(f
                .getAbsoluteFile()));
        f.delete();
        f2.delete();
    }

    /**
     * @tests java.io.File#getAbsolutePath()
     */
    public void test_getAbsolutePath() {
        String base = addTrailingSlash(tempDirectory.getPath());
        File f = new File(base, "temp.tst");
        assertEquals("Test 1: Incorrect Path Returned.",
                     base + "temp.tst", f.getAbsolutePath());

        f = new File(base + "Temp" + File.separator + File.separator + File.separator + "Testing" + File.separator
                + "temp.tst");
        assertEquals("Test 2: Incorrect Path Returned.",
		     base + "Temp" + File.separator + "Testing" + File.separator + "temp.tst",
                     f.getAbsolutePath());

        f = new File(base + "a" + File.separator + File.separator + ".." + File.separator + "temp.tst");
        assertEquals("Test 3: Incorrect Path Returned.",
                     base + "a" + File.separator + ".." + File.separator + "temp.tst",
                     f.getAbsolutePath());
        f.delete();
    }

    /**
     * @tests java.io.File#getCanonicalFile()
     */
    public void test_getCanonicalFile() throws IOException {
        String base = addTrailingSlash(tempDirectory.getPath());
        File f = new File(base, "temp.tst");
        File f2 = f.getCanonicalFile();
        assertEquals("Test 1: Incorrect File Returned.", 0, f2
                .getCanonicalFile().compareTo(f.getCanonicalFile()));
        f = new File(base + "Temp" + File.separator + File.separator + "temp.tst");
        f2 = f.getCanonicalFile();
        assertEquals("Test 2: Incorrect File Returned.", 0, f2
                .getCanonicalFile().compareTo(f.getCanonicalFile()));
        f = new File(base + "Temp" + File.separator + File.separator + ".." + File.separator + "temp.tst");
        f2 = f.getCanonicalFile();
        assertEquals("Test 3: Incorrect File Returned.", 0, f2
                .getCanonicalFile().compareTo(f.getCanonicalFile()));

        // Test for when long directory/file names in Windows
        boolean onWindows = File.separatorChar == '\\';
        if (onWindows) {
            File testdir = new File(base, "long-" + platformId);
            testdir.mkdir();
            File dir = new File(testdir, "longdirectory" + platformId);
            try {
                dir.mkdir();
                f = new File(dir, "longfilename.tst");
                f2 = f.getCanonicalFile();
                assertEquals("Test 4: Incorrect File Returned.", 0, f2
                        .getCanonicalFile().compareTo(f.getCanonicalFile()));
                FileOutputStream fos = new FileOutputStream(f);
                fos.close();
                f2 = new File(testdir + File.separator + "longdi~1" + File.separator
                        + "longfi~1.tst");
                File canonicalf2 = f2.getCanonicalFile();
                /*
                 * If the "short file name" doesn't exist, then assume that the
                 * 8.3 file name compatibility is disabled.
                 */
                if (canonicalf2.exists()) {
                    assertTrue("Test 5: Incorrect File Returned: "
                            + canonicalf2, canonicalf2.compareTo(f
                            .getCanonicalFile()) == 0);
                }
            } finally {
                f.delete();
                f2.delete();
                dir.delete();
                testdir.delete();
            }
        }
    }

    /**
     * @tests java.io.File#getCanonicalPath()
     */
    public void test_getCanonicalPath() throws IOException {
        // Should work for Unix/Windows.
        String dots = "..";
        String base = tempDirectory.getCanonicalPath();
        base = addTrailingSlash(base);
        File f = new File(base, "temp.tst");
        assertEquals("Test 1: Incorrect Path Returned.", base + "temp.tst", f
                .getCanonicalPath());
        f = new File(base + "Temp" + File.separator + dots + File.separator + "temp.tst");
        assertEquals("Test 2: Incorrect Path Returned.", base + "temp.tst", f
                .getCanonicalPath());

        // Finding a non-existent directory for tests 3 and 4
        // This is necessary because getCanonicalPath is case sensitive and
        // could cause a failure in the test if the directory exists but with
        // different case letters (e.g "Temp" and "temp")
        int dirNumber = 1;
        boolean dirExists = true;
        File dir1 = new File(base, String.valueOf(dirNumber));
        while (dirExists) {
            if (dir1.exists()) {
                dirNumber++;
                dir1 = new File(base, String.valueOf(dirNumber));
            } else {
                dirExists = false;
            }
        }
        f = new File(base + dirNumber + File.separator + dots + File.separator + dirNumber
                + File.separator + "temp.tst");
        assertEquals("Test 3: Incorrect Path Returned.", base + dirNumber
                + File.separator + "temp.tst", f.getCanonicalPath());
        f = new File(base + dirNumber + File.separator + "Temp" + File.separator + dots + File.separator
                + "Test" + File.separator + "temp.tst");
        assertEquals("Test 4: Incorrect Path Returned.", base + dirNumber
                + File.separator + "Test" + File.separator + "temp.tst", f.getCanonicalPath());

        f = new File(base + "1234.567");
        assertEquals("Test 5: Incorrect Path Returned.", base + "1234.567", f
                .getCanonicalPath());

        // Test for long file names on Windows
        boolean onWindows = (File.separatorChar == '\\');
        if (onWindows) {
            File testdir = new File(base, "long-" + platformId);
            testdir.mkdir();
            File f1 = new File(testdir, "longfilename" + platformId + ".tst");
            FileOutputStream fos = new FileOutputStream(f1);
            File f2 = null, f3 = null, dir2 = null;
            try {
                fos.close();
                String dirName1 = f1.getCanonicalPath();
                File f4 = new File(testdir, "longfi~1.tst");
                /*
                 * If the "short file name" doesn't exist, then assume that the
                 * 8.3 file name compatibility is disabled.
                 */
                if (f4.exists()) {
                    String dirName2 = f4.getCanonicalPath();
                    assertEquals("Test 6: Incorrect Path Returned.", dirName1,
                            dirName2);
                    dir2 = new File(testdir, "longdirectory" + platformId);
                    if (!dir2.exists()) {
                        assertTrue("Could not create dir: " + dir2, dir2
                                .mkdir());
                    }
                    f2 = new File(testdir.getPath() + File.separator + "longdirectory"
                            + platformId + File.separator + "Test" + File.separator + dots
                            + File.separator + "longfilename.tst");
                    FileOutputStream fos2 = new FileOutputStream(f2);
                    fos2.close();
                    dirName1 = f2.getCanonicalPath();
                    f3 = new File(testdir.getPath() + File.separator + "longdi~1"
                            + File.separator + "Test" + File.separator + dots + File.separator
                            + "longfi~1.tst");
                    dirName2 = f3.getCanonicalPath();
                    assertEquals("Test 7: Incorrect Path Returned.", dirName1,
                            dirName2);
                }
            } finally {
                f1.delete();
                if (f2 != null) {
                    f2.delete();
                }
                if (dir2 != null) {
                    dir2.delete();
                }
                testdir.delete();
            }
        }
    }

    /**
     * @tests java.io.File#getName()
     */
    public void test_getName() {
        File f = new File("name.tst");
        assertEquals("Test 1: Returned incorrect name", "name.tst", f.getName());

        f = new File("");
        assertEquals("Test 2: Returned incorrect name", "", f.getName());

        f.delete();
    }

    /**
     * @tests java.io.File#getParent()
     */
    public void test_getParent() {
        File f = new File("p.tst");
        assertNull("Incorrect path returned", f.getParent());
        f = new File(System.getProperty("java.io.tmpdir"), "p.tst");
        assertEquals("Incorrect path returned",
                     System.getProperty("java.io.tmpdir"), f.getParent());
        f.delete();

        File f1 = new File("/directory");
        assertEquals("Wrong parent test 1", File.separator, f1.getParent());
        f1 = new File("/directory/file");
        assertEquals("Wrong parent test 2",
                     File.separator + "directory", f1.getParent());
        f1 = new File("directory/file");
        assertEquals("Wrong parent test 3", "directory", f1.getParent());
        f1 = new File("/");
        assertNull("Wrong parent test 4", f1.getParent());
        f1 = new File("directory");
        assertNull("Wrong parent test 5", f1.getParent());
    }

    /**
     * @tests java.io.File#getParentFile()
     */
    public void test_getParentFile() {
        File f = new File("tempfile.tst");
        assertNull("Incorrect path returned", f.getParentFile());
        f = new File(tempDirectory, "tempfile1.tmp");
        File f2 = new File(tempDirectory, "tempfile2.tmp");
        File f3 = new File(tempDirectory, "/a/tempfile.tmp");
        assertEquals("Incorrect File Returned", 0, f.getParentFile().compareTo(
                f2.getParentFile()));
        assertTrue("Incorrect File Returned", f.getParentFile().compareTo(
                f3.getParentFile()) != 0);
        f.delete();
        f2.delete();
        f3.delete();
    }

    /**
     * @tests java.io.File#getPath()
     */
    public void test_getPath() {
        String base = System.getProperty("java.io.tmpdir");
        String fname;
        File f1;
        if (!base.regionMatches((base.length() - 1), File.separator, 0, 1)) {
            base += File.separator;
        }
        fname = base + "filechk.tst";
        f1 = new File(base, "filechk.tst");
        File f2 = new File("filechk.tst");
        File f3 = new File("c:");
        File f4 = new File(base + "a" + File.separator + File.separator + ".." + File.separator
                + "filechk.tst");
        assertEquals("getPath returned incorrect path(f1)",
                     fname, f1.getPath());
        assertEquals("getPath returned incorrect path(f2)",
                     "filechk.tst", f2.getPath());
        assertEquals("getPath returned incorrect path(f3)","c:", f3.getPath());
        assertEquals("getPath returned incorrect path(f4)",
                     base + "a" + File.separator + ".." + File.separator + "filechk.tst",
                     f4.getPath());
        f1.delete();
        f2.delete();
        f3.delete();
        f4.delete();

        // Regression for HARMONY-444
        File file;
        String separator = File.separator;

        file = new File((File) null, "x/y/z");
        assertEquals("x" + separator + "y" + separator + "z", file.getPath());

        file = new File((String) null, "x/y/z");
        assertEquals("x" + separator + "y" + separator + "z", file.getPath());

        // Regression for HARMONY-829
        String f1ParentName = "01";
        f1 = new File(f1ParentName, "");
        assertEquals(f1ParentName, f1.getPath());

        String f2ParentName = "0";
        f2 = new File(f2ParentName, "");

        assertEquals(-1, f2.compareTo(f1));
        assertEquals(1, f1.compareTo(f2));

        File parent = tempDirectory;
        f3 = new File(parent, "");

        assertEquals(parent.getPath(), f3.getPath());

        // Regression for HARMONY-3869
        File file1 = new File("", "");
        assertEquals(File.separator, file1.getPath());

        File file2 = new File(new File(""), "");
        assertEquals(File.separator, file2.getPath());
    }

    /**
     * @tests java.io.File#hashCode()
     */
    public void test_hashCode() {
        // Regression for HARMONY-53
        File mfile = new File("SoMe FiLeNaMe"); // Mixed case
        File lfile = new File("some filename"); // Lower case

        if (mfile.equals(lfile)) {
            assertTrue("Assert 0: wrong hashcode", mfile.hashCode() == lfile
                    .hashCode());
        } else {
            assertFalse("Assert 1: wrong hashcode", mfile.hashCode() == lfile
                    .hashCode());
        }
    }

    /**
     * @tests java.io.File#isAbsolute()
     */
    public void test_isAbsolute() {
        File f = new File("/test");
        File f1 = new File("\\test");
        assertTrue("Absolute returned false", f.isAbsolute());
        assertFalse("Absolute returned true", f1.isAbsolute());
        assertTrue(new File("//test").isAbsolute());
        assertFalse(new File("test").isAbsolute());
        assertFalse(new File("c:/").isAbsolute());
        assertFalse(new File("c:\\").isAbsolute());
        assertFalse(new File("c:").isAbsolute());
        assertFalse(new File("\\").isAbsolute());
        assertFalse(new File("\\\\").isAbsolute());
        assertTrue("Non-Absolute returned true", !new File("../test")
                .isAbsolute());
    }

    /**
     * @tests java.io.File#isDirectory()
     */
    public void test_isDirectory() {
        String base = addTrailingSlash(tempDirectory.getPath());
        File f = new File(base);
        assertTrue("Test 1: Directory Returned False", f.isDirectory());
        f = new File(base + "zxzxzxz" + platformId);
        assertTrue("Test 2: (Not Created) Directory Returned True.", !f
                .isDirectory());
        f.mkdir();
        try {
            assertTrue("Test 3: Directory Returned False.", f.isDirectory());
        } finally {
            f.delete();
        }
    }

    /**
     * @tests java.io.File#isFile()
     */
    public void test_isFile() throws IOException {
        String base = tempDirectory.getPath();
        File f = new File(base);
        assertFalse("Directory Returned True As Being A File.", f.isFile());

        base = addTrailingSlash(base);
        f = new File(base, platformId + "amiafile");
        assertTrue("Non-existent File Returned True", !f.isFile());
        FileOutputStream fos = new FileOutputStream(f);
        fos.close();
        assertTrue("File returned false", f.isFile());
        f.delete();
    }

    /**
     * @tests java.io.File#isHidden()
     */
    public void test_isHidden() throws IOException, InterruptedException {
        File f = File.createTempFile("harmony-test-FileTest_isHidden_", ".tmp");
        // On Unix hidden files are marked with a "." at the beginning
        // of the file name.
        File f2 = new File(".test.tst" + platformId);
        FileOutputStream fos2 = new FileOutputStream(f2);
        fos2.close();
        assertTrue("File returned hidden on Unix", !f.isHidden());
        assertTrue("File returned visible on Unix", f2.isHidden());
        assertTrue("File did not delete.", f2.delete());
        f.delete();
    }

    /**
     * @tests java.io.File#lastModified()
     */
    public void test_lastModified() throws IOException {
        File f = new File(System.getProperty("java.io.tmpdir"), platformId
                + "lModTest.tst");
        f.delete();
        long lastModifiedTime = f.lastModified();
        assertEquals("LastModified Time Should Have Returned 0.", 0,
                lastModifiedTime);
        FileOutputStream fos = new FileOutputStream(f);
        fos.close();
        f.setLastModified(315550800000L);
        lastModifiedTime = f.lastModified();
        assertEquals("LastModified Time Incorrect",
                     315550800000L, lastModifiedTime);
        f.delete();

        // Regression for HARMONY-2146
        f = new File("/../");
        assertTrue(f.lastModified() > 0);
    }

    /**
     * @tests java.io.File#length()
     */
    public void test_length() throws IOException {
        File f = new File(tempDirectory, platformId
                + "input.tst");
        assertEquals("File Length Should Have Returned 0.", 0, f.length());
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(fileString.getBytes());
        fos.close();
        assertEquals("Incorrect file length returned",
		     fileString.length(), f.length());
        f.delete();
    }

    /**
     * @tests java.io.File#list()
     */
    public void test_list() throws IOException {
        String base = tempDirectory.getPath();
        // Old test left behind "garbage files" so this time it creates a
        // directory that is guaranteed not to already exist (and deletes it
        // afterward.)
        int dirNumber = 1;
        boolean dirExists = true;
        File dir = null;
        dir = new File(base, platformId + String.valueOf(dirNumber));
        while (dirExists) {
            if (dir.exists()) {
                dirNumber++;
                dir = new File(base, String.valueOf(dirNumber));
            } else {
                dirExists = false;
            }
        }

        String[] flist = dir.list();

        assertNull("Method list() Should Have Returned null.", flist);

        assertTrue("Could not create parent directory for list test", dir
                .mkdir());

        String[] files = { "mtzz1.xx", "mtzz2.xx", "mtzz3.yy", "mtzz4.yy" };
        try {
            assertEquals(
                    "Method list() Should Have Returned An Array Of Length 0.",
                    0, dir.list().length);

            File file = new File(dir, "notADir.tst");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.close();
                assertNull(
                        "listFiles Should Have Returned Null When Used On A File Instead Of A Directory.",
                        file.list());
            } finally {
                file.delete();
            }

            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                FileOutputStream fos = new FileOutputStream(f);
                fos.close();
            }

            flist = dir.list();
            if (flist.length != files.length) {
                fail("Incorrect list returned");
            }

            // Checking to make sure the correct files were are listed in the
            // array.
            boolean[] check = new boolean[flist.length];
            for (int i = 0; i < check.length; i++) {
                check[i] = false;
            }
            for (int i = 0; i < files.length; i++) {
                for (int j = 0; j < flist.length; j++) {
                    if (flist[j].equals(files[i])) {
                        check[i] = true;
                        break;
                    }
                }
            }
            int checkCount = 0;
            for (int i = 0; i < check.length; i++) {
                if (check[i] == false) {
                    checkCount++;
                }
            }
            assertEquals("Invalid file returned in listing", 0, checkCount);

            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                f.delete();
            }

            assertTrue("Could not delete parent directory for list test.", dir
                    .delete());
        } finally {
            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                f.delete();
            }
            dir.delete();
        }
    }

    /**
     * @tests java.io.File#listFiles()
     */
    public void test_listFiles() throws IOException, InterruptedException {
        String base = tempDirectory.getPath();
        // Finding a non-existent directory to create.
        int dirNumber = 1;
        boolean dirExists = true;
        File dir = new File(base, platformId + String.valueOf(dirNumber));
        // Making sure that the directory does not exist.
        while (dirExists) {
            // If the directory exists, add one to the directory number
            // (making it a new directory name.)
            if (dir.exists()) {
                dirNumber++;
                dir = new File(base, String.valueOf(dirNumber));
            } else {
                dirExists = false;
            }
        }
        // Test for attempting to call listFiles on a non-existent directory.
        assertNull("listFiles Should Return Null.", dir.listFiles());

        assertTrue("Failed To Create Parent Directory.", dir.mkdir());

        String[] files = { "1.tst", "2.tst", "3.tst", "" };
        try {
            assertEquals("listFiles Should Return An Array Of Length 0.", 0,
                    dir.listFiles().length);

            File file = new File(dir, "notADir.tst");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.close();
                assertNull(
                        "listFiles Should Have Returned Null When Used On A File Instead Of A Directory.",
                        file.listFiles());
            } finally {
                file.delete();
            }

            for (int i = 0; i < (files.length - 1); i++) {
                File f = new File(dir, files[i]);
                FileOutputStream fos = new FileOutputStream(f);
                fos.close();
            }

            new File(dir, "doesNotExist.tst");
            File[] flist = dir.listFiles();

            // Test to make sure that only the 3 files that were created are
            // listed.
            assertEquals("Incorrect Number Of Files Returned.", 3, flist.length);

            // Test to make sure that listFiles can read hidden files.
            files[3] = ".4.tst";
            File f = new File(dir, ".4.tst");
            FileOutputStream fos = new FileOutputStream(f);
            fos.close();
            flist = dir.listFiles();
            assertEquals("Incorrect Number Of Files Returned.", 4, flist.length);

            // Checking to make sure the correct files were are listed in
            // the array.
            boolean[] check = new boolean[flist.length];
            for (int i = 0; i < check.length; i++) {
                check[i] = false;
            }
            for (int i = 0; i < files.length; i++) {
                for (int j = 0; j < flist.length; j++) {
                    if (flist[j].getName().equals(files[i])) {
                        check[i] = true;
                        break;
                    }
                }
            }
            int checkCount = 0;
            for (int i = 0; i < check.length; i++) {
                if (check[i] == false) {
                    checkCount++;
                }
            }
            assertEquals("Invalid file returned in listing", 0, checkCount);

            for (int i = 0; i < files.length; i++) {
                f = new File(dir, files[i]);
                f.delete();
            }
            assertTrue("Parent Directory Not Deleted.", dir.delete());
        } finally {
            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                f.delete();
            }
            dir.delete();
        }
    }

    /**
     * @tests java.io.File#listFiles(java.io.FileFilter)
     */
    public void test_listFilesLjava_io_FileFilter() throws IOException {
        String base = System.getProperty("java.io.tmpdir");
        // Finding a non-existent directory to create.
        int dirNumber = 1;
        boolean dirExists = true;
        File baseDir = new File(base, platformId + String.valueOf(dirNumber));
        // Making sure that the directory does not exist.
        while (dirExists) {
            // If the directory exists, add one to the directory number (making
            // it a new directory name.)
            if (baseDir.exists()) {
                dirNumber++;
                baseDir = new File(base, String.valueOf(dirNumber));
            } else {
                dirExists = false;
            }
        }

        // Creating a filter that catches directories.
        FileFilter dirFilter = new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        };

        assertNull("listFiles Should Return Null.", baseDir
                .listFiles(dirFilter));

        assertTrue("Failed To Create Parent Directory.", baseDir.mkdir());

        File dir1 = null;
        String[] files = { "1.tst", "2.tst", "3.tst" };
        try {
            assertEquals("listFiles Should Return An Array Of Length 0.", 0,
                    baseDir.listFiles(dirFilter).length);

            File file = new File(baseDir, "notADir.tst");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.close();
                assertNull(
                        "listFiles Should Have Returned Null When Used On A File Instead Of A Directory.",
                        file.listFiles(dirFilter));
            } finally {
                file.delete();
            }

            for (int i = 0; i < files.length; i++) {
                File f = new File(baseDir, files[i]);
                FileOutputStream fos = new FileOutputStream(f);
                fos.close();
            }
            dir1 = new File(baseDir, "Temp1");
            dir1.mkdir();

            // Creating a filter that catches files.
            FileFilter fileFilter = new FileFilter() {
                public boolean accept(File f) {
                    return f.isFile();
                }
            };

            // Test to see if the correct number of directories are returned.
            File[] directories = baseDir.listFiles(dirFilter);
            assertEquals("Incorrect Number Of Directories Returned.", 1,
                    directories.length);

            // Test to see if the directory was saved with the correct name.
            assertEquals("Incorrect Directory Returned.", 0, directories[0]
                    .compareTo(dir1));

            // Test to see if the correct number of files are returned.
            File[] flist = baseDir.listFiles(fileFilter);
            assertEquals("Incorrect Number Of Files Returned.",
                         files.length, flist.length);

            // Checking to make sure the correct files were are listed in the
            // array.
            boolean[] check = new boolean[flist.length];
            for (int i = 0; i < check.length; i++) {
                check[i] = false;
            }
            for (int i = 0; i < files.length; i++) {
                for (int j = 0; j < flist.length; j++) {
                    if (flist[j].getName().equals(files[i])) {
                        check[i] = true;
                        break;
                    }
                }
            }
            int checkCount = 0;
            for (int i = 0; i < check.length; i++) {
                if (check[i] == false) {
                    checkCount++;
                }
            }
            assertEquals("Invalid file returned in listing", 0, checkCount);

            for (int i = 0; i < files.length; i++) {
                File f = new File(baseDir, files[i]);
                f.delete();
            }
            dir1.delete();
            assertTrue("Parent Directory Not Deleted.", baseDir.delete());
        } finally {
            for (int i = 0; i < files.length; i++) {
                File f = new File(baseDir, files[i]);
                f.delete();
            }
            if (dir1 != null) {
                dir1.delete();
            }
            baseDir.delete();
        }
    }

    /**
     * @tests java.io.File#listFiles(java.io.FilenameFilter)
     */
    public void test_listFilesLjava_io_FilenameFilter() throws IOException {
        String base = System.getProperty("java.io.tmpdir");
        // Finding a non-existent directory to create.
        int dirNumber = 1;
        boolean dirExists = true;
        File dir = new File(base, platformId + String.valueOf(dirNumber));
        // Making sure that the directory does not exist.
        while (dirExists) {
            // If the directory exists, add one to the directory number (making
            // it a new directory name.)
            if (dir.exists()) {
                dirNumber++;
                dir = new File(base, platformId + String.valueOf(dirNumber));
            } else {
                dirExists = false;
            }
        }

        // Creating a filter that catches "*.tst" files.
        FilenameFilter tstFilter = new FilenameFilter() {
            public boolean accept(File f, String fileName) {
                return fileName.endsWith(".tst");
            }
        };

        assertNull("listFiles Should Return Null.", dir.listFiles(tstFilter));

        assertTrue("Failed To Create Parent Directory.", dir.mkdir());

        String[] files = { "1.tst", "2.tst", "3.tmp" };
        try {
            assertEquals("listFiles Should Return An Array Of Length 0.", 0,
                    dir.listFiles(tstFilter).length);

            File file = new File(dir, "notADir.tst");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.close();
                assertNull(
                        "listFiles Should Have Returned Null When Used On A File Instead Of A Directory.",
                        file.listFiles(tstFilter));
            } finally {
                file.delete();
            }

            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                FileOutputStream fos = new FileOutputStream(f);
                fos.close();
            }

            // Creating a filter that catches "*.tmp" files.
            FilenameFilter tmpFilter = new FilenameFilter() {
                public boolean accept(File f, String fileName) {
                    // If the suffix is ".tmp" then send it to the array
                    if (fileName.endsWith(".tmp")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };

            // Tests to see if the correct number of files were returned.
            File[] flist = dir.listFiles(tstFilter);
            assertEquals("Incorrect Number Of Files Passed Through tstFilter.",
                    2, flist.length);
            for (int i = 0; i < flist.length; i++) {
                assertTrue("File Should Not Have Passed The tstFilter.",
                        flist[i].getPath().endsWith(".tst"));
            }

            flist = dir.listFiles(tmpFilter);
            assertEquals("Incorrect Number Of Files Passed Through tmpFilter.",
                    1, flist.length);
            assertTrue("File Should Not Have Passed The tmpFilter.", flist[0]
                    .getPath().endsWith(".tmp"));

            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                f.delete();
            }
            assertTrue("Parent Directory Not Deleted.", dir.delete());
        } finally {
            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                f.delete();
            }
            dir.delete();
        }
    }

    /**
     * @tests java.io.File#list(java.io.FilenameFilter)
     */
    public void test_listLjava_io_FilenameFilter() throws IOException {
        String base = tempDirectory.getPath();
        // Old test left behind "garbage files" so this time it creates a
        // directory that is guaranteed not to already exist (and deletes it
        // afterward.)
        int dirNumber = 1;
        boolean dirExists = true;
        File dir = new File(base, platformId + String.valueOf(dirNumber));
        while (dirExists) {
            if (dir.exists()) {
                dirNumber++;
                dir = new File(base, String.valueOf(dirNumber));
            } else {
                dirExists = false;
            }
        }

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.equals("mtzz1.xx");
            }
        };

        String[] flist = dir.list(filter);
        assertNull("Method list(FilenameFilter) Should Have Returned Null.",
                flist);

        assertTrue("Could not create parent directory for test", dir.mkdir());

        String[] files = { "mtzz1.xx", "mtzz2.xx", "mtzz3.yy", "mtzz4.yy" };
        try {
            /*
             * Do not return null when trying to use list(Filename Filter) on a
             * file rather than a directory. All other "list" methods return
             * null for this test case.
             */
            /*
             * File file = new File(dir, "notADir.tst"); try { FileOutputStream
             * fos = new FileOutputStream(file); fos.close(); } catch
             * (IOException e) { fail("Unexpected IOException During Test."); }
             * flist = dir.list(filter); assertNull("listFiles Should Have
             * Returned Null When Used On A File Instead Of A Directory.",
             * flist); file.delete();
             */

            flist = dir.list(filter);
            assertEquals("Array Of Length 0 Should Have Returned.", 0,
                    flist.length);

            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                FileOutputStream fos = new FileOutputStream(f);
                fos.close();
            }

            flist = dir.list(filter);

            assertEquals("Incorrect list returned", flist.length,
                    files.length - 1);

            // Checking to make sure the correct files were are listed in the
            // array.
            boolean[] check = new boolean[flist.length];
            for (int i = 0; i < check.length; i++) {
                check[i] = false;
            }
            String[] wantedFiles = { "mtzz2.xx", "mtzz3.yy", "mtzz4.yy" };
            for (int i = 0; i < wantedFiles.length; i++) {
                for (int j = 0; j < flist.length; j++) {
                    if (flist[j].equals(wantedFiles[i])) {
                        check[i] = true;
                        break;
                    }
                }
            }
            int checkCount = 0;
            for (int i = 0; i < check.length; i++) {
                if (check[i] == false) {
                    checkCount++;
                }
            }
            assertEquals("Invalid file returned in listing", 0, checkCount);

            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                f.delete();
            }
            assertTrue("Could not delete parent directory for test.", dir
                    .delete());
        } finally {
            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                f.delete();
            }
            dir.delete();
        }
    }

    /**
     * @tests java.io.File#listRoots()
     */
    public void test_listRoots() {
        File[] roots = File.listRoots();
        assertEquals("Incorrect Number Of Root Directories.", 1,
                roots.length);
        String fileLoc = roots[0].getPath();
        assertTrue("Incorrect Root Directory Returned.", fileLoc
                .startsWith(File.separator));
    }

    /**
     * @tests java.io.File#mkdir()
     */
    public void test_mkdir() throws IOException {
        String base = tempDirectory.getPath();
        // Old test left behind "garbage files" so this time it creates a
        // directory that is guaranteed not to already exist (and deletes it
        // afterward.)
        int dirNumber = 1;
        boolean dirExists = true;
        File dir = new File(base, String.valueOf(dirNumber));
        while (dirExists) {
            if (dir.exists()) {
                dirNumber++;
                dir = new File(base, String.valueOf(dirNumber));
            } else {
                dirExists = false;
            }
        }

        assertTrue("mkdir failed", dir.mkdir());
	assertTrue("mkdir worked but exists check failed", dir.exists());
        dir.delete();

        String longDirName = "abcdefghijklmnopqrstuvwx";// 24 chars
        String newbase = new String(dir + File.separator);
        StringBuilder sb = new StringBuilder(dir + File.separator);
        StringBuilder sb2 = new StringBuilder(dir + File.separator);

        // Test make a long path
        while (dir.getCanonicalPath().length() < 256 - longDirName.length()) {
            sb.append(longDirName + File.separator);
            dir = new File(sb.toString());
            assertTrue("mkdir failed", dir.mkdir());
	    assertTrue("mkdir worked but exists check failed", dir.exists());
            dir.delete();
        }

        while (dir.getCanonicalPath().length() < 256) {
            sb.append(0);
            dir = new File(sb.toString());
            assertTrue("mkdir " + dir.getCanonicalPath().length() + " failed",
                    dir.mkdir());
            assertTrue("mkdir " + dir.getCanonicalPath().length()
                       + " worked but exists check failed", dir.exists());
            dir.delete();
        }
        dir = new File(sb2.toString());
        // Test make many paths
        while (dir.getCanonicalPath().length() < 256) {
            sb2.append(0);
            dir = new File(sb2.toString());
            assertTrue("mkdir " + dir.getCanonicalPath().length() + " failed",
                    dir.mkdir());
            assertTrue("mkdir " + dir.getCanonicalPath().length()
                       + " worked but exists check failed", dir.exists());
            dir.delete();
        }

        // Regression test for HARMONY-3656
        String[] ss = { "dir\u3400", "abc", "abc@123", "!@#$%^&",
                "~\u4E00!\u4E8C@\u4E09$", "\u56DB\u4E94\u516D",
                "\u4E03\u516B\u4E5D" };
        for (int i = 0; i < ss.length; i++) {
            dir = new File(newbase, ss[i]);
            assertTrue("mkdir " + dir.getCanonicalPath() + " failed",
                       dir.mkdir());
            assertTrue("mkdir " + dir.getCanonicalPath()
                       + " worked but exists check failed",
                       dir.exists());
            dir.delete();
        }
    }

    /**
     * @tests java.io.File#mkdir()
     *
     * HARMONY-6041
     */
    public void test_mkdir_special_unicode() throws IOException {
        File specialDir = new File(this.tempDirectory,"\u5C73");
        int i = 0;
        while (specialDir.exists()) {
            specialDir = new File("\u5C73" + i);
            ++i;
        }
        try {
            assertFalse(specialDir.exists());
            assertTrue(specialDir.mkdir());
            assertTrue(specialDir.exists());
        } finally {
            specialDir.delete();
        }
    }

    /**
     * @tests java.io.File#mkdirs()
     */
    public void test_mkdirs() {
        String userHome = addTrailingSlash(tempDirectory.getPath());
        File f = new File(userHome + "mdtest" + platformId + File.separator + "mdtest2",
                "p.tst");
        File g = new File(userHome + "mdtest" + platformId + File.separator + "mdtest2");
        File h = new File(userHome + "mdtest" + platformId);
        f.mkdirs();
        try {
            assertTrue("Base Directory not created", h.exists());
            assertTrue("Directories not created", g.exists());
            assertTrue("File not created", f.exists());
        } finally {
            f.delete();
            g.delete();
            h.delete();
        }
    }

    /**
     * @tests java.io.File#renameTo(java.io.File)
     */
    public void test_renameToLjava_io_File() throws IOException {
        String base = tempDirectory.getPath();
        File dir = new File(base, platformId);
        dir.mkdir();
        File f = new File(dir, "xxx.xxx");
        File rfile = new File(dir, "yyy.yyy");
        File f2 = new File(dir, "zzz.zzz");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(fileString.getBytes());
            fos.close();
            long lengthOfFile = f.length();

            rfile.delete(); // in case it already exists

            assertTrue("Test 1: File Rename Failed", f.renameTo(rfile));
            assertTrue("Test 2: File Rename Failed.", rfile.exists());
            assertEquals("Test 3: Size Of File Changed.",
                         lengthOfFile, rfile.length());

            fos = new FileOutputStream(rfile);
            fos.close();

            f2.delete(); // in case it already exists
            assertTrue("Test 4: File Rename Failed", rfile.renameTo(f2));
            assertTrue("Test 5: File Rename Failed.", f2.exists());
        } finally {
            f.delete();
            rfile.delete();
            f2.delete();
            dir.delete();
        }
    }

    /**
     * @tests java.io.File#setLastModified(long)
     */
    public void test_setLastModifiedJ() throws IOException {
        File f1 = null;
        try {
            f1 = new File("harmony-test-FileTest_setLastModified.tmp");
            f1.createNewFile();
            long orgTime = f1.lastModified();
            // Subtracting 100 000 milliseconds from the orgTime of File f1
            f1.setLastModified(orgTime - 100000);
            long lastModified = f1.lastModified();
            assertEquals("Test 1: LastModifed time incorrect",
                         orgTime - 100000, lastModified);
            // Subtracting 10 000 000 milliseconds from the orgTime of File f1
            f1.setLastModified(orgTime - 10000000);
            lastModified = f1.lastModified();
            assertEquals("Test 2: LastModifed time incorrect",
                         orgTime - 10000000, lastModified);
            // Adding 100 000 milliseconds to the orgTime of File f1
            f1.setLastModified(orgTime + 100000);
            lastModified = f1.lastModified();
            assertEquals("Test 3: LastModifed time incorrect",
                         orgTime + 100000, lastModified);
            // Adding 10 000 000 milliseconds from the orgTime of File f1
            f1.setLastModified(orgTime + 10000000);
            lastModified = f1.lastModified();
            assertEquals("Test 4: LastModifed time incorrect",
                         orgTime + 10000000, lastModified);
            // Trying to set time to an exact number
            f1.setLastModified(315550800000L);
            lastModified = f1.lastModified();
            assertEquals("Test 5: LastModified time incorrect",
                         315550800000L, lastModified);
            // Trying to set time to a negative number
            try {
                f1.setLastModified(-25);
                fail("IllegalArgumentException Not Thrown.");
            } catch (IllegalArgumentException e) {
            }
        } finally {
            if (f1 != null) {
                f1.delete();
            }
        }
    }

    /**
     * @tests java.io.File#setReadOnly()
     *
    TODO(user): enable when java.lang.Runtime/Process are implemented.
    public void test_setReadOnly() throws IOException, InterruptedException {
        File f1 = null;
        File f2 = null;
        try {
            f1 = File.createTempFile("harmony-test-FileTest_setReadOnly", ".tmp");
            f2 = File.createTempFile("harmony-test-FileTest_setReadOnly", ".tmp");
            // Assert is flawed because canWrite does not work.
            // assertTrue("File f1 Is Set To ReadOnly." , f1.canWrite());
            f1.setReadOnly();
            // Assert is flawed because canWrite does not work.
            // assertTrue("File f1 Is Not Set To ReadOnly." , !f1.canWrite());
            try {
                // Attempt to write to a file that is setReadOnly.
                new FileOutputStream(f1);
                fail("IOException not thrown.");
            } catch (IOException e) {
                // Expected
            }
            Runtime r = Runtime.getRuntime();
            Process p;
            p = r.exec("chmod +w " + f1.getAbsolutePath());
            p.waitFor();
            // Assert is flawed because canWrite does not work.
            // assertTrue("File f1 Is Set To ReadOnly." , f1.canWrite());
            FileOutputStream fos = new FileOutputStream(f1);
            fos.write(fileString.getBytes());
            fos.close();
            assertTrue("File Was Not Able To Be Written To.",
                    f1.length() == fileString.length());
            assertTrue("File f1 Did Not Delete", f1.delete());

            // Assert is flawed because canWrite does not work.
            // assertTrue("File f2 Is Set To ReadOnly." , f2.canWrite());
            fos = new FileOutputStream(f2);
            // Write to a file.
            fos.write(fileString.getBytes());
            fos.close();
            f2.setReadOnly();
            // Assert is flawed because canWrite does not work.
            // assertTrue("File f2 Is Not Set To ReadOnly." , !f2.canWrite());
            try {
                // Attempt to write to a file that has previously been written
                // to.
                // and is now set to read only.
                fos = new FileOutputStream(f2);
                fail("IOException not thrown.");
            } catch (IOException e) {
                // Expected
            }
            r = Runtime.getRuntime();
            p = r.exec("chmod +w " + f2.getAbsolutePath());
            p.waitFor();
            assertTrue("File f2 Is Set To ReadOnly.", f2.canWrite());
            fos = new FileOutputStream(f2);
            fos.write(fileString.getBytes());
            fos.close();
            f2.setReadOnly();
            assertTrue("File f2 Did Not Delete", f2.delete());
            // Similarly, trying to delete a read-only directory should succeed
            f2 = new File(tempDirectory, "deltestdir");
            f2.mkdir();
            f2.setReadOnly();
            assertTrue("Directory f2 Did Not Delete", f2.delete());
            assertTrue("Directory f2 Did Not Delete", !f2.exists());
        } finally {
            if (f1 != null) {
                f1.delete();
            }
            if (f2 != null) {
                f2.delete();
            }
        }
    }
    */

    /**
     * @tests java.io.File#toString()
     */
    public void test_toString() {
        String fileName = System.getProperty("java.io.tmpdir") + File.separator + "input.tst";
        File f = new File(fileName);
        assertEquals("Incorrect string returned", fileName, f.toString());
    }

    /**
     * @tests java.io.File#toURI()
     *
     TODO(user): enable when java.net support is implemented.
    public void test_toURI() throws URISyntaxException {
        // Need a directory that exists
        File dir = tempDirectory;

        // Test for toURI when the file is a directory.
        String newURIPath = dir.getAbsolutePath();
        newURIPath = newURIPath.replace(File.separatorChar, '/');
        if (!newURIPath.startsWith("/")) {
            newURIPath = "/" + newURIPath;
        }
        if (!newURIPath.endsWith("/")) {
            newURIPath += '/';
        }

        URI uri = dir.toURI();
        assertEquals("Test 1A: Incorrect URI Returned.", dir.getAbsoluteFile(), new File(uri));
        assertEquals("Test 1B: Incorrect URI Returned.",
                     new URI("file", null, newURIPath, null, null), uri);

        // Test for toURI with a file name with illegal chars.
        File f = new File(dir, "te% \u20ac st.tst");
        newURIPath = f.getAbsolutePath();
        newURIPath = newURIPath.replace(File.separatorChar, '/');
        if (!newURIPath.startsWith("/")) {
            newURIPath = "/" + newURIPath;
        }

        uri = f.toURI();
        assertEquals("Test 2A: Incorrect URI Returned.",
                     f.getAbsoluteFile(), new File(uri));
        assertEquals("Test 2B: Incorrect URI Returned.",
                     new URI("file", null, newURIPath, null, null), uri);

        // Regression test for HARMONY-3207
        dir = new File(""); // current directory
        uri = dir.toURI();
        assertTrue("Test current dir: URI does not end with slash.", uri
                .toString().endsWith("/"));
    }
    */

    /**
     * @tests java.io.File#toURL()
     *
     TODO(user): enable when java.net support is implemented.
    public void test_toURL() throws MalformedURLException {
        // Need a directory that exists
        File dir = tempDirectory;

        // Test for toURL when the file is a directory.
        String newDirURL = dir.getAbsolutePath();
        newDirURL = newDirURL.replace(File.separatorChar, '/');
        if (newDirURL.startsWith("/")) {
            newDirURL = "file:" + newDirURL;
        } else {
            newDirURL = "file:/" + newDirURL;
        }
        if (!newDirURL.endsWith("/")) {
            newDirURL += '/';
        }
        assertEquals("Test 1: Incorrect URL Returned.",
                     dir.toURL().toString(), newDirURL);

        // Test for toURL with a file.
        File f = new File(dir, "test.tst");
        String newURL = f.getAbsolutePath();
        newURL = newURL.replace(File.separatorChar, '/');
        if (newURL.startsWith("/")) {
            newURL = "file:" + newURL;
        } else {
            newURL = "file:/" + newURL;
        }
        assertEquals("Test 2: Incorrect URL Returned.",
                     f.toURL().toString(), newURL);

        // Regression test for HARMONY-3207
        dir = new File(""); // current directory
        newDirURL = dir.toURL().toString();
        assertTrue("Test current dir: URL does not end with slash.", newDirURL
                .endsWith("/"));
    }
    */

    /**
     * @tests java.io.File#toURI()
     *
     TODO(user): enable when java.net support is implemented.
    public void test_toURI2() throws URISyntaxException {
        File f = new File(tempDirectory, "a/b/c/../d/e/./f");

        String path = f.getAbsolutePath();
        path = path.replace(File.separatorChar, '/');
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        URI uri1 = new URI("file", null, path, null);
        URI uri2 = f.toURI();
        assertEquals("uris not equal", uri1, uri2);
    }
    */

    /**
     * @tests java.io.File#toURL()
     *
     TODO(user): enable when java.net support is implemented.
    public void test_toURL2() throws MalformedURLException {
        File f = new File(tempDirectory, "a/b/c/../d/e/./f");

        String path = f.getAbsolutePath();
        path = path.replace(File.separatorChar, '/');
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        URL url1 = new URL("file", "", path);
        URL url2 = f.toURL();
        assertEquals("urls not equal", url1, url2);
    }
    */
}
