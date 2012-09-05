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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

public class FileWriterTest extends TestCase {

    FileWriter fw;

    FileInputStream fis;

    BufferedWriter bw;

    File f;

    FileOutputStream fos;

    BufferedReader br;

    /**
     * @tests java.io.FileWriter#FileWriter(java.io.File)
     */
    public void test_ConstructorLjava_io_File() throws IOException {
        fos = new FileOutputStream(f.getPath());
        fos.write("Test String".getBytes());
        fos.close();
        bw = new BufferedWriter(new FileWriter(f));
        bw.write(" After test string", 0, 18);
        bw.close();
        br = new BufferedReader(new FileReader(f.getPath()));
        char[] buf = new char[100];
        int r = br.read(buf);
        br.close();
        assertEquals("Failed to write correct chars", " After test string",
                new String(buf, 0, r));
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.io.File, boolean)
     */
    public void test_ConstructorLjava_io_FileZ() throws IOException {
        FileWriter fileWriter = new FileWriter(f);

        String first = "The first string for testing. ";
        fileWriter.write(first);
        fileWriter.close();

        fileWriter = new FileWriter(f, true);
        String second = "The second String for testing.";
        fileWriter.write(second);
        fileWriter.close();

        FileReader fileReader = new FileReader(f);
        char[] out = new char[first.length() + second.length() + 10];
        int length = fileReader.read(out);
        fileReader.close();
        assertEquals(first + second, new String(out, 0, length));

        fileWriter = new FileWriter(f);
        first = "The first string for testing. ";
        fileWriter.write(first);
        fileWriter.close();

        fileWriter = new FileWriter(f, false);
        second = "The second String for testing.";
        fileWriter.write(second);
        fileWriter.close();

        fileReader = new FileReader(f);
        out = new char[first.length() + second.length() + 10];
        length = fileReader.read(out);
        fileReader.close();
        assertEquals(second, new String(out, 0, length));
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.io.FileDescriptor)
     */
    public void test_ConstructorLjava_io_FileDescriptor() throws IOException {
        fos = new FileOutputStream(f.getPath());
        fos.write("Test String".getBytes());
        fos.close();
        fis = new FileInputStream(f.getPath());
        br = new BufferedReader(new FileReader(fis.getFD()));
        char[] buf = new char[100];
        int r = br.read(buf);
        br.close();
        fis.close();
        assertTrue("Failed to write correct chars: " + new String(buf, 0, r),
                new String(buf, 0, r).equals("Test String"));
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() throws IOException {
        fos = new FileOutputStream(f.getPath());
        fos.write("Test String".getBytes());
        fos.close();
        bw = new BufferedWriter(new FileWriter(f.getPath()));
        bw.write(" After test string", 0, 18);
        bw.close();
        br = new BufferedReader(new FileReader(f.getPath()));
        char[] buf = new char[100];
        int r = br.read(buf);
        br.close();
        assertEquals("Failed to write correct chars", " After test string",
                new String(buf, 0, r));
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.lang.String, boolean)
     */
    public void test_ConstructorLjava_lang_StringZ() throws IOException {
        fos = new FileOutputStream(f.getPath());
        fos.write("Test String".getBytes());
        fos.close();
        bw = new BufferedWriter(new FileWriter(f.getPath(), true));
        bw.write(" After test string", 0, 18);
        bw.close();
        br = new BufferedReader(new FileReader(f.getPath()));
        char[] buf = new char[100];
        int r = br.read(buf);
        br.close();
        assertEquals("Failed to append to file",
                "Test String After test string", new String(buf, 0, r));

        fos = new FileOutputStream(f.getPath());
        fos.write("Test String".getBytes());
        fos.close();
        bw = new BufferedWriter(new FileWriter(f.getPath(), false));
        bw.write(" After test string", 0, 18);
        bw.close();
        br = new BufferedReader(new FileReader(f.getPath()));
        buf = new char[100];
        r = br.read(buf);
        br.close();
        assertEquals("Failed to overwrite file", " After test string",
                new String(buf, 0, r));
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    @Override
    protected void setUp() {
        f = new File(System.getProperty("user.home"), "writer.tst");
        if (f.exists()) {
            if (!f.delete()) {
                fail("Unable to delete test file");
            }
        }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    @Override
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
