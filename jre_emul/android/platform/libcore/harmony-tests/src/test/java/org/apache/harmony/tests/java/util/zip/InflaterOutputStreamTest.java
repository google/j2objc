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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;
import java.util.zip.ZipException;

import junit.framework.TestCase;

public class InflaterOutputStreamTest extends TestCase {

    private ByteArrayOutputStream os = new ByteArrayOutputStream();

    private byte[] compressedBytes = new byte[100];

    private String testString = "Hello world";

    /**
     * java.util.zip.InflaterOutputStream#InflaterOutputStream(java.io.OutputStream)
     */
    public void test_ConstructorLjava_io_OutputStream() throws IOException {
        new InflaterOutputStream(os);

        try {
            new InflaterOutputStream(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.zip.InflaterOutputStream#InflaterOutputStream(java.io.OutputStream, Inflater)
     */
    public void test_ConstructorLjava_io_OutputStreamLjava_util_zip_Inflater() {
        new InflaterOutputStream(os, new Inflater());

        try {
            new InflaterOutputStream(null, new Inflater());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new InflaterOutputStream(os, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.zip.InflaterOutputStream#InflaterOutputStream(java.io.OutputStream, Inflater, int)
     */
    public void test_ConstructorLjava_io_OutputStreamLjava_util_zip_InflaterI() {
        new InflaterOutputStream(os, new Inflater(), 20);

        try {
            new InflaterOutputStream(null, null, 10);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new InflaterOutputStream(null, new Inflater(), -1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new InflaterOutputStream(os, null, -1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new InflaterOutputStream(null, null, -1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new InflaterOutputStream(os, new Inflater(), 0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new InflaterOutputStream(os, new Inflater(), -10000);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * java.util.zip.InflaterOutputStream#close()
     */
    public void test_close() throws IOException {
        InflaterOutputStream ios = new InflaterOutputStream(os);
        ios.close();
        // multiple close
        ios.close();
    }

    /**
     * java.util.zip.InflaterOutputStream#flush()
     */
    public void test_flush() throws IOException {
        InflaterOutputStream ios = new InflaterOutputStream(os);
        ios.close();
        try {
            ios.flush();
            fail("Should throw IOException");
        } catch (IOException e) {
            // expected
        }

        ios = new InflaterOutputStream(os);
        ios.flush();
        ios.flush();
    }

    /**
     * java.util.zip.InflaterOutputStream#finish()
     */
    public void test_finish() throws IOException {
        InflaterOutputStream ios = new InflaterOutputStream(os);
        ios.close();
        try {
            ios.finish();
            fail("Should throw IOException");
        } catch (IOException e) {
            // expected
        }

        ios = new InflaterOutputStream(os);
        ios.finish();
        ios.finish();
        ios.flush();
        ios.flush();
        ios.finish();

        byte[] bytes1 = { 10, 20, 30, 40, 50 };
        Deflater defaultDeflater = new Deflater(Deflater.BEST_SPEED);
        defaultDeflater.setInput(bytes1);
        defaultDeflater.finish();
        int length1 = defaultDeflater.deflate(compressedBytes);

        byte[] bytes2 = { 100, 90, 80, 70, 60 };
        Deflater bestDeflater = new Deflater(Deflater.BEST_COMPRESSION);
        bestDeflater.setInput(bytes2);
        bestDeflater.finish();
        int length2 = bestDeflater.deflate(compressedBytes, length1, compressedBytes.length - length1);

        ios = new InflaterOutputStream(os);
        for (int i = 0; i < length1; i++) {
            ios.write(compressedBytes[i]);
        }
        ios.finish();
        ios.close();

        byte[] result = os.toByteArray();
        for (int i = 0; i < bytes1.length; i++) {
            assertEquals(bytes1[i], result[i]);
        }

        ios = new InflaterOutputStream(os);
        for (int i = length1; i < length2 * 2; i++) {
            ios.write(compressedBytes[i]);
        }
        ios.finish();
        ios.close();

        result = os.toByteArray();
        for (int i = 0; i < bytes2.length; i++) {
            assertEquals(bytes2[i], result[bytes1.length + i]);
        }

    }

    /**
     * java.util.zip.InflaterOutputStream#write(int)
     */
    public void test_write_I() throws IOException {
        int length = compressToBytes(testString);

        // uncompress the data stored in the compressedBytes
        InflaterOutputStream ios = new InflaterOutputStream(os);
        for (int i = 0; i < length; i++) {
            ios.write(compressedBytes[i]);
        }

        String result = new String(os.toByteArray());
        assertEquals(testString, result);
    }

    /**
     * java.util.zip.InflaterOutputStream#write(int)
     */
    public void test_write_I_Illegal() throws IOException {

        // write after close
        InflaterOutputStream ios = new InflaterOutputStream(os);
        ios.close();
        try {
            ios.write(-1);
            fail("Should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * java.util.zip.InflaterOutputStream#write(byte[], int, int)
     */
    public void test_write_$BII() throws IOException {
        int length = compressToBytes(testString);

        // uncompress the data stored in the compressedBytes
        InflaterOutputStream ios = new InflaterOutputStream(os);
        ios.write(compressedBytes, 0, length);

        String result = new String(os.toByteArray());
        assertEquals(testString, result);
    }

    /**
     * java.util.zip.InflaterOutputStream#write(byte[], int, int)
     */
    public void test_write_$BII_Illegal() throws IOException {
        // write error compression (ZIP) format
        InflaterOutputStream ios = new InflaterOutputStream(os);
        byte[] bytes = { 0, 1, 2, 3 };
        try {
            ios.write(bytes, 0, 4);
            fail("Should throw ZipException");
        } catch (ZipException e) {
            // expected
        }
        try {
            ios.flush();
            fail("Should throw ZipException");
        } catch (ZipException e) {
            // expected
        }

        // write after close
        ios = new InflaterOutputStream(os);
        ios.close();
        try {
            ios.write(bytes, 0, 4);
            fail("Should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            ios.write(bytes, -1, 4);
            fail("Should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            ios.write(bytes, -1, -4);
            fail("Should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            ios.write(bytes, 0, 400);
            fail("Should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            ios.write(null, -1, 4);
            fail("Should throw IOException");
        } catch (IOException e) {
            // expected
        }

        ios = new InflaterOutputStream(os);
        try {
            ios.write(null, 0, 4);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            ios.write(null, -1, 4);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            ios.write(null, 0, -4);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            ios.write(null, 0, 1000);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            ios.write(bytes, -1, 4);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            ios.write(bytes, 0, -4);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            ios.write(bytes, 0, 100);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            ios.write(bytes, -100, 100);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        ios = new InflaterOutputStream(os);
        ios.finish();

        try {
            ios.write(bytes, -1, -100);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            ios.write(null, -1, -100);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        ios = new InflaterOutputStream(os);
        ios.flush();
        try {
            ios.write(bytes, 0, 4);
            fail("Should throw ZipException");
        } catch (ZipException e) {
            // expected
        }
    }

    // Compress the test string into compressedBytes
    private int compressToBytes(String string) {
        byte[] input = string.getBytes();
        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();
        return deflater.deflate(compressedBytes);
    }

}
