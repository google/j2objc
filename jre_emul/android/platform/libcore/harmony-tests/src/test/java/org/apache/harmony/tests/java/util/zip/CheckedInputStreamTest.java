/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.tests.java.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import junit.framework.TestCase;
import tests.support.resource.Support_Resources;

public class CheckedInputStreamTest extends TestCase {

    /**
     * java.util.zip.CheckedInputStream#CheckedInputStream(java.io.InputStream,
     *java.util.zip.Checksum)
     */
    public void test_ConstructorLjava_io_InputStreamLjava_util_zip_Checksum() throws Exception {
        InputStream checkInput = Support_Resources.getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput, new CRC32());
        assertEquals("constructor of checkedInputStream has failed", 0, checkIn.getChecksum()
                .getValue());
        checkInput.close();
    }

    /**
     * java.util.zip.CheckedInputStream#getChecksum()
     */
    public void test_getChecksum() throws Exception {
        byte outBuf[] = new byte[100];
        File f = File.createTempFile("CheckedInputStreamTest", ".txt");
        InputStream inEmp = new FileInputStream(f);
        CheckedInputStream checkEmpty = new CheckedInputStream(inEmp, new CRC32());
        while (checkEmpty.read() >= 0) {
        }
        assertEquals("the checkSum value of an empty file is not zero", 0, checkEmpty
                .getChecksum().getValue());
        inEmp.close();

        // testing getChecksum for the file checkInput
        InputStream checkInput = Support_Resources.getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput, new CRC32());
        while (checkIn.read() >= 0) {
        }
        // ran JDK and found that the checkSum value of this is 2036203193
        // System.out.print(" " + checkIn.getChecksum().getValue());
        assertEquals("the checksum value is incorrect", 2036203193, checkIn.getChecksum()
                .getValue());
        checkInput.close();
        // testing getChecksum for file checkInput
        checkInput = Support_Resources.getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn2 = new CheckedInputStream(checkInput, new CRC32());
        checkIn2.read(outBuf, 0, 10);
        // ran JDK and found that the checkSum value of this is 2235765342
        // System.out.print(" " + checkIn2.getChecksum().getValue());
        assertEquals("the checksum value is incorrect", 2235765342L, checkIn2.getChecksum()
                .getValue());
        checkInput.close();
    }

    /**
     * java.util.zip.CheckedInputStream#skip(long)
     */
    public void test_skipJ() throws Exception {
        // testing that the return by skip is valid
        InputStream checkInput = Support_Resources.getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput, new CRC32());
        long skipValue = 5;
        assertEquals("the value returned by skip(n) is not the same as its parameter",
                skipValue, checkIn.skip(skipValue));
        checkIn.skip(skipValue);
        // ran JDK and found the checkSum value is 2235765342
        // System.out.print(checkIn.getChecksum().getValue());
        assertEquals("checkSum value is not correct", 2235765342L, checkIn.getChecksum()
                .getValue());
        assertEquals(0, checkIn.skip(0));
        checkInput.close();
    }

    public void test_read() throws Exception {
        // testing that the return by skip is valid
        InputStream checkInput = Support_Resources
                .getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput,
                new CRC32());
        checkIn.read();
        checkIn.close();
        try {
            checkIn.read();
            fail("IOException expected.");
        } catch (IOException ee) {
            // expected
        }
        checkInput.close();
    }

    public void test_read$byteII() throws Exception {
        // testing that the return by skip is valid
        InputStream checkInput = Support_Resources
                .getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput,
                new CRC32());
        byte buff[] = new byte[50];
        checkIn.read(buff, 10, 5);
        checkIn.close();
        try {
            checkIn.read(buff, 10, 5);
            fail("IOException expected.");
        } catch (IOException ee) {
            // expected
        }
        checkInput.close();
    }
}
