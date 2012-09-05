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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

public class FileReaderTest extends TestCase {

    FileReader br;

    BufferedWriter bw;

    FileInputStream fis;

    File f;

    /**
     * @tests java.io.FileReader#FileReader(java.io.File)
     */
    public void test_ConstructorLjava_io_File() throws IOException {
        bw = new BufferedWriter(new FileWriter(f.getPath()));
        bw.write(" After test string", 0, 18);
        bw.close();
        br = new FileReader(f);
        char[] buf = new char[100];
        int r = br.read(buf);
        br.close();
        assertEquals("Failed to read correct chars", " After test string",
                new String(buf, 0, r));
    }

    /**
     * @tests java.io.FileReader#FileReader(java.io.FileDescriptor)
     */
    public void test_ConstructorLjava_io_FileDescriptor() throws IOException {
        bw = new BufferedWriter(new FileWriter(f.getPath()));
        bw.write(" After test string", 0, 18);
        bw.close();
        FileInputStream fis = new FileInputStream(f.getPath());
        br = new FileReader(fis.getFD());
        char[] buf = new char[100];
        int r = br.read(buf);
        br.close();
        fis.close();
        assertEquals("Failed to read correct chars", " After test string",
                new String(buf, 0, r));
    }

    /**
     * @tests java.io.FileReader#FileReader(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() throws IOException {
        bw = new BufferedWriter(new FileWriter(f.getPath()));
        bw.write(" After test string", 0, 18);
        bw.close();
        br = new FileReader(f.getPath());
        char[] buf = new char[100];
        int r = br.read(buf);
        br.close();
        assertEquals("Failed to read correct chars", " After test string",
                new String(buf, 0, r));
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        f = new File(System.getProperty("user.home"), "reader.tst");
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
    protected void tearDown() {
        try {
            bw.close();
        } catch (Exception e) {
            // Ignore
        }
        try {
            br.close();
        } catch (Exception e) {
            // Ignore
        }
        try {
            if (fis != null) {
                fis.close();
            }
        } catch (Exception e) {
            // Ignore
        }
        f.delete();
    }
}
