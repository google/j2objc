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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import junit.framework.TestCase;

public class OldFileWriterTest extends TestCase {

    FileWriter fw;

    FileInputStream fis;

    BufferedWriter bw;

    File f;

    public void test_ConstructorLjava_io_File_IOException() {
        File dir = new File(System.getProperty("java.io.tmpdir"));

        try {
            fw = new FileWriter(dir);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_ConstructorLjava_io_FileZ_IOException() {
        File dir = new File(System.getProperty("java.io.tmpdir"));

        try {
            fw = new FileWriter(dir, true);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_ConstructorLjava_lang_String_IOException() {
        try {
            fw = new FileWriter(System.getProperty("java.io.tmpdir"));
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }


    public void test_ConstructorLjava_lang_StringZ_IOException() {
        try {
            fw = new FileWriter(System.getProperty("java.io.tmpdir"), false);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_handleEarlyEOFChar_1() {
        String str = "All work and no play makes Jack a dull boy\n";
        int NUMBER = 2048;
        int j = 0;
        int len = str.length() * NUMBER;
        /* == 88064 *//* NUMBER compulsively written copies of the same string */
        char[] strChars = new char[len];
        for (int i = 0; i < NUMBER; ++i) {
            for (int k = 0; k < str.length(); ++k) {
                strChars[j++] = str.charAt(k);
            }
        }
        File f = null;
        FileWriter fw = null;
        try {
            f = File.createTempFile("ony", "by_one");
            fw = new FileWriter(f);
            fw.write(strChars);
            fw.close();
            InputStreamReader in = null;
            FileInputStream fis = new FileInputStream(f);
            in = new InputStreamReader(fis);
            int b;
            int errors = 0;
            for (int offset = 0; offset < strChars.length; ++offset) {
                b = in.read();
                if (b == -1) {
                    fail("Early EOF at offset " + offset + "\n");
                    return;
                }
            }
            assertEquals(0, errors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test_handleEarlyEOFChar_2() throws IOException {
        int capacity = 65536;
        byte[] bytes = new byte[capacity];
        byte[] bs = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'
        };
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bs[i / 8192];
        }
        String inputStr = new String(bytes);
        int len = inputStr.length();
        File f = File.createTempFile("FileWriterBugTest ", null);
        FileWriter writer = new FileWriter(f);
        writer.write(inputStr);
        writer.close();
        long flen = f.length();

        FileReader reader = new FileReader(f);
        char[] outChars = new char[capacity];
        int outCount = reader.read(outChars);
        String outStr = new String(outChars, 0, outCount);

        f.deleteOnExit();
        assertEquals(len, flen);
        assertEquals(inputStr, outStr);
    }

    protected void setUp() throws Exception {
        f = File.createTempFile("writer", ".tst");

        if (f.exists())
            if (!f.delete()) {
                fail("Unable to delete test file");
            }
    }

    protected void tearDown() {
        try {
            bw.close();
        } catch (Exception e) {
        }
        try {
            fis.close();
        } catch (Exception e) {
        }
        f.delete();
    }
}
