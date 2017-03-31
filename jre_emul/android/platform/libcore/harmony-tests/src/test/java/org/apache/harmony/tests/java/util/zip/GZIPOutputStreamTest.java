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
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamTest extends junit.framework.TestCase {

    class TestGZIPOutputStream extends GZIPOutputStream {
        TestGZIPOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        TestGZIPOutputStream(OutputStream out, int size) throws IOException {
            super(out, size);
        }

        Checksum getChecksum() {
            return crc;
        }
    }

    /**
     * java.util.zip.GZIPOutputStream#GZIPOutputStream(java.io.OutputStream)
     */
    public void test_ConstructorLjava_io_OutputStream() {
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPCon", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile);
            assertNotNull("the constructor for GZIPOutputStream is null",
                    outGZIP);
            assertEquals("the CRC value of the outputStream is not zero", 0, outGZIP
                    .getChecksum().getValue());
            outGZIP.close();
        } catch (IOException e) {
            fail(
                    "an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }

    /**
     * java.util.zip.GZIPOutputStream#GZIPOutputStream(java.io.OutputStream,
     *int)
     */
    public void test_ConstructorLjava_io_OutputStreamI() {
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile,
                    100);
            assertNotNull("the constructor for GZIPOutputStream is null",
                    outGZIP);
            assertEquals("the CRC value of the outputStream is not zero", 0, outGZIP
                    .getChecksum().getValue());
            outGZIP.close();
        } catch (IOException e) {
            fail(
                    "an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }

    /**
     * java.util.zip.GZIPOutputStream#finish()
     */
    public void test_finish() {
        // test method java.util.zip.GZIPOutputStream.finish()
        byte byteArray[] = { 3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w' };
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutFinish", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile);

            outGZIP.finish();
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 1);
            } catch (IOException e) {
                r = 1;
            }

            assertEquals("GZIP instance can still be used after finish is called",
                    1, r);
            outGZIP.close();
        } catch (IOException e) {
            fail(
                    "an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }

    /**
     * java.util.zip.GZIPOutputStream#close()
     */
    public void test_close() {
        // test method java.util.zip.GZIPOutputStream.close()
        byte byteArray[] = { 3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w' };
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutClose2", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile);
            outGZIP.close();
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 1);
            } catch (IOException e) {
                r = 1;
            }
            assertEquals("GZIP instance can still be used after close is called",
                    1, r);
        } catch (IOException e) {
            fail(
                    "an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }

    /**
     * java.util.zip.GZIPOutputStream#write(byte[], int, int)
     */
    public void test_write$BII() {
        // test method java.util.zip.GZIPOutputStream.writeBII
        byte byteArray[] = { 3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w' };
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutWrite", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile);
            outGZIP.write(byteArray, 0, 10);
            // ran JDK and found this CRC32 value is 3097700292
            // System.out.print(outGZIP.getChecksum().getValue());
            assertEquals("the checksum value was incorrect result of write from GZIP",
                    3097700292L, outGZIP.getChecksum().getValue());

            // test for boundary check
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 11);
            } catch (IndexOutOfBoundsException e) {
                r = 1;
            }
            assertEquals("out of bounds exception is not present", 1, r);
            outGZIP.close();
        } catch (IOException e) {
            fail(
                    "an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }

    public void testSyncFlush() throws IOException {
        PipedOutputStream pout = new PipedOutputStream();
        PipedInputStream pin = new PipedInputStream(pout);
        GZIPOutputStream out = new GZIPOutputStream(pout, true /* syncFlush */);
        GZIPInputStream in = new GZIPInputStream(pin);

        out.write(1);
        out.write(2);
        out.write(3);
        out.flush();
        // flush() is guaranteed to flush data only if syncFlush is true.
        // The default flush param is NO_FLUSH so it's up to the deflater to
        // decide how much input it wants to read before generating a compressed
        // block.
        assertEquals(1, in.read());
        assertEquals(2, in.read());
        assertEquals(3, in.read());
    }
}
