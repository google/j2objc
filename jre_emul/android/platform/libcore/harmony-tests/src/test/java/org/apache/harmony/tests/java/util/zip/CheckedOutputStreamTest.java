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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

public class CheckedOutputStreamTest extends junit.framework.TestCase {

    /**
     * java.util.zip.CheckedOutputStream#CheckedOutputStream(java.io.OutputStream,
     *java.util.zip.Checksum)
     */
    public void test_ConstructorLjava_io_OutputStreamLjava_util_zip_Checksum() {
        // test method java.util.zip.checkedOutputStream.constructor
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("CheckedOutputStreamTest", ".txt"));
            CheckedOutputStream chkOut = new CheckedOutputStream(outFile,
                    new CRC32());
            assertEquals("the checkSum value of the constructor is not 0", 0, chkOut
                    .getChecksum().getValue());
            outFile.close();
        } catch (IOException e) {
            fail("Unable to find file");
        } catch (SecurityException e) {
            fail(
                    "file cannot be opened for writing due to security reasons");
        }
    }

    /**
     * java.util.zip.CheckedOutputStream#getChecksum()
     */
    public void test_getChecksum() {
        // test method java.util.zip.checkedOutputStream.getChecksum()
        byte byteArray[] = { 1, 2, 3, 'e', 'r', 't', 'g', 3, 6 };
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("CheckedOutputStreamTest", ".txt"));
            CheckedOutputStream chkOut = new CheckedOutputStream(outFile,
                    new Adler32());
            chkOut.write(byteArray[4]);
            // ran JDK and found that checkSum value is 7536755
            // System.out.print(chkOut.getChecksum().getValue());

            assertEquals("the checkSum value for writeI is incorrect", 7536755, chkOut
                    .getChecksum().getValue());
            chkOut.getChecksum().reset();
            chkOut.write(byteArray, 5, 4);
            // ran JDK and found that checkSum value is 51708133
            // System.out.print(" " +chkOut.getChecksum().getValue());

            assertEquals("the checkSum value for writeBII is incorrect ", 51708133, chkOut
                    .getChecksum().getValue());
            outFile.close();
        } catch (IOException e) {
            fail("Unable to find file");
        } catch (SecurityException e) {
            fail(
                    "file cannot be opened for writing due to security reasons");
        }
    }

    /**
     * java.util.zip.CheckedOutputStream#write(int)
     */
    public void test_writeI() {
        // test method java.util.zip.checkedOutputStream.writeI()
        byte byteArray[] = { 1, 2, 3, 'e', 'r', 't', 'g', 3, 6 };
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("CheckedOutputStreamTest", ".txt"));
            CheckedOutputStream chkOut = new CheckedOutputStream(outFile,
                    new CRC32());
            for (byte element : byteArray) {
                chkOut.write(element);
            }
            assertTrue(
                    "the checkSum value is zero, no bytes are written to the output file",
                    chkOut.getChecksum().getValue() != 0);
            outFile.close();
        } catch (IOException e) {
            fail("Unable to find file");
        } catch (SecurityException e) {
            fail("File cannot be opened for writing due to security reasons");
        }
    }

    /**
     * java.util.zip.CheckedOutputStream#write(byte[], int, int)
     */
    public void test_write$BII() {
        // test method java.util.zip.checkOutputStream.writeBII()
        byte byteArray[] = { 1, 2, 3, 'e', 'r', 't', 'g', 3, 6 };
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("CheckedOutputStreamTest", ".txt"));
            CheckedOutputStream chkOut = new CheckedOutputStream(outFile,
                    new CRC32());
            chkOut.write(byteArray, 4, 5);
            assertTrue(
                    "the checkSum value is zero, no bytes are written to the output file",
                    chkOut.getChecksum().getValue() != 0);
            int r = 0;
            try {
                chkOut.write(byteArray, 4, 6);
            } catch (IndexOutOfBoundsException e) {
                r = 1;
            }
            assertEquals("boundary check is not performed", 1, r);
            outFile.close();
        } catch (IOException e) {
            fail("Unable to find file");
        } catch (SecurityException e) {
            fail(
                    "file cannot be opened for writing due to security reasons");
        } catch (IndexOutOfBoundsException e) {
            fail("Index for write is out of bounds");
        }
    }
}
