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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import junit.framework.TestCase;

public class DeflaterOutputStreamTest extends TestCase {

    private class MyDeflaterOutputStream extends DeflaterOutputStream {
        boolean deflateFlag = false;

        MyDeflaterOutputStream(OutputStream out) {
            super(out);
        }

        MyDeflaterOutputStream(OutputStream out, Deflater defl) {
            super(out, defl);
        }

        MyDeflaterOutputStream(OutputStream out, Deflater defl, int size) {
            super(out, defl, size);
        }

        byte[] getProtectedBuf() {
            return buf;
        }

        protected void deflate() throws IOException {
            deflateFlag = true;
            super.deflate();
        }

        boolean getDaflateFlag() {
            return deflateFlag;
        }
    }

    private final byte outputBuf[] = new byte[500];

    @Override
    protected void setUp() {
        // setting up a deflater to be used
        byte byteArray[] = { 1, 3, 4, 7, 8 };
        int x = 0;
        Deflater deflate = new Deflater(1);
        deflate.setInput(byteArray);
        while (!(deflate.needsInput())) {
            x += deflate.deflate(outputBuf, x, outputBuf.length - x);
        }
        deflate.finish();
        while (!(deflate.finished())) {
            x = x + deflate.deflate(outputBuf, x, outputBuf.length - x);
        }
        deflate.end();
    }

    /**
     * java.util.zip.DeflaterOutputStream#DeflaterOutputStream(java.io.OutputStream,
     *java.util.zip.Deflater)
     */
    public void test_ConstructorLjava_io_OutputStreamLjava_util_zip_Deflater() throws Exception {
        byte byteArray[] = { 1, 3, 4, 7, 8 };
        File f1 = File.createTempFile("hyts_ConstruOD", ".tst");
        FileOutputStream fos = new FileOutputStream(f1);
        Deflater defl = null;
        MyDeflaterOutputStream dos;
        // Test for a null Deflater.
        try {
            dos = new MyDeflaterOutputStream(fos, defl);
            fail("NullPointerException Not Thrown");
        } catch (NullPointerException e) {
        }
        defl = new Deflater();
        dos = new MyDeflaterOutputStream(fos, defl);

        // Test to see if DeflaterOutputStream was created with the correct
        // buffer.
        assertEquals("Incorrect Buffer Size", 512, dos.getProtectedBuf().length);

        dos.write(byteArray);
        dos.close();
        f1.delete();
    }

    /**
     * java.util.zip.DeflaterOutputStream#DeflaterOutputStream(java.io.OutputStream)
     */
    public void test_ConstructorLjava_io_OutputStream() throws Exception {
        File f1 = File.createTempFile("hyts_ConstruO", ".tst");
        FileOutputStream fos = new FileOutputStream(f1);
        MyDeflaterOutputStream dos = new MyDeflaterOutputStream(fos);

        // Test to see if DeflaterOutputStream was created with the correct
        // buffer.
        assertEquals("Incorrect Buffer Size", 512, dos.getProtectedBuf().length);

        dos.write(outputBuf);
        dos.close();
        f1.delete();
    }

    /**
     * java.util.zip.DeflaterOutputStream#DeflaterOutputStream(java.io.OutputStream,
     *java.util.zip.Deflater, int)
     */
    public void test_ConstructorLjava_io_OutputStreamLjava_util_zip_DeflaterI()
            throws Exception {
        int buf = 5;
        int negBuf = -5;
        int zeroBuf = 0;
        byte byteArray[] = { 1, 3, 4, 7, 8, 3, 6 };
        File f1 = File.createTempFile("hyts_ConstruODI", ".tst");
        FileOutputStream fos = new FileOutputStream(f1);
        Deflater defl = null;
        MyDeflaterOutputStream dos;

        // Test for a null Deflater.
        try {
            dos = new MyDeflaterOutputStream(fos, defl, buf);
            fail("NullPointerException Not Thrown");
        } catch (NullPointerException e) {
        }
        defl = new Deflater();

        // Test for a negative buf.
        try {
            dos = new MyDeflaterOutputStream(fos, defl, negBuf);
            fail("IllegalArgumentException Not Thrown");
        } catch (IllegalArgumentException e) {
        }

        // Test for a zero buf.
        try {
            dos = new MyDeflaterOutputStream(fos, defl, zeroBuf);
            fail("IllegalArgumentException Not Thrown");
        } catch (IllegalArgumentException e) {
        }

        // Test to see if DeflaterOutputStream was created with the correct
        // buffer.
        dos = new MyDeflaterOutputStream(fos, defl, buf);
        assertEquals("Incorrect Buffer Size", 5, dos.getProtectedBuf().length);

        dos.write(byteArray);
        dos.close();
        f1.delete();
    }

    /**
     * java.util.zip.DeflaterOutputStream#close()
     */
    public void test_close() throws Exception {
        File f1 = File.createTempFile("close", ".tst");

        InflaterInputStream iis = new InflaterInputStream(new FileInputStream(f1));
        try {
            iis.read();
            fail("EOFException Not Thrown");
        } catch (EOFException e) {
        }

        FileOutputStream fos = new FileOutputStream(f1);
        DeflaterOutputStream dos = new DeflaterOutputStream(fos);
        byte byteArray[] = { 1, 3, 4, 6 };
        dos.write(byteArray);
        dos.close();

        iis = new InflaterInputStream(new FileInputStream(f1));

        // Test to see if the finish method wrote the bytes to the file.
        assertEquals("Incorrect Byte Returned.", 1, iis.read());
        assertEquals("Incorrect Byte Returned.", 3, iis.read());
        assertEquals("Incorrect Byte Returned.", 4, iis.read());
        assertEquals("Incorrect Byte Returned.", 6, iis.read());
        assertEquals("Incorrect Byte Returned.", -1, iis.read());
        assertEquals("Incorrect Byte Returned.", -1, iis.read());
        iis.close();

        // Not sure if this test will stay.
        FileOutputStream fos2 = new FileOutputStream(f1);
        DeflaterOutputStream dos2 = new DeflaterOutputStream(fos2);
        fos2.close();
        try {
            dos2.close();
            fail("IOException not thrown");
        } catch (IOException e) {
        }

        // Test to write to a closed DeflaterOutputStream
        try {
            dos.write(5);
            fail("DeflaterOutputStream Able To Write After Being Closed.");
        } catch (IOException e) {
        }

        // Test to write to a FileOutputStream that should have been closed
        // by
        // the DeflaterOutputStream.
        try {
            fos.write(("testing").getBytes());
            fail("FileOutputStream Able To Write After Being Closed.");
        } catch (IOException e) {
        }

        f1.delete();
    }

    /**
     * java.util.zip.DeflaterOutputStream#finish()
     */
    public void test_finish() throws Exception {
        // Need test to see if method finish() actually finishes
        // Only testing possible errors, not if it actually works

        File f1 = File.createTempFile("finish", ".tst");
        FileOutputStream fos1 = new FileOutputStream(f1);
        DeflaterOutputStream dos = new DeflaterOutputStream(fos1);
        byte byteArray[] = { 1, 3, 4, 6 };
        dos.write(byteArray);
        dos.finish();

        // Test to see if the same FileOutputStream can be used with the
        // DeflaterOutputStream after finish is called.
        try {
            dos.write(1);
            fail("IOException not thrown");
        } catch (IOException e) {
        }

        // Test for writing with a new FileOutputStream using the same
        // DeflaterOutputStream.
        FileOutputStream fos2 = new FileOutputStream(f1);
        dos = new DeflaterOutputStream(fos2);
        dos.write(1);

        // Test for writing to FileOutputStream fos1, which should be open.
        fos1.write(("testing").getBytes());

        // Test for writing to FileOutputStream fos2, which should be open.
        fos2.write(("testing").getBytes());

        // Not sure if this test will stay.
        FileOutputStream fos3 = new FileOutputStream(f1);
        DeflaterOutputStream dos3 = new DeflaterOutputStream(fos3);
        fos3.close();
        try {
            dos3.finish();
            fail("IOException not thrown");
        } catch (IOException e) {
        }

        // dos.close() won't close fos1 because it has been re-assigned to
        // fos2
        fos1.close();
        dos.close();
        f1.delete();
    }

    /**
     * java.util.zip.DeflaterOutputStream#write(int)
     */
    public void test_writeI() throws Exception {
        File f1 = File.createTempFile("writeIL", ".tst");
        FileOutputStream fos = new FileOutputStream(f1);
        DeflaterOutputStream dos = new DeflaterOutputStream(fos);
        for (int i = 0; i < 3; i++) {
            dos.write(i);
        }
        dos.close();
        FileInputStream fis = new FileInputStream(f1);
        InflaterInputStream iis = new InflaterInputStream(fis);
        for (int i = 0; i < 3; i++) {
            assertEquals("Incorrect Byte Returned.", i, iis.read());
        }
        assertEquals("Incorrect Byte Returned (EOF).", -1, iis.read());
        assertEquals("Incorrect Byte Returned (EOF).", -1, iis.read());
        iis.close();

        // Not sure if this test is that important.
        // Checks to see if you can write using the DeflaterOutputStream
        // after
        // the FileOutputStream has been closed.
        FileOutputStream fos2 = new FileOutputStream(f1);
        DeflaterOutputStream dos2 = new DeflaterOutputStream(fos2);
        fos2.close();
        try {
            dos2.write(2);
            fail("IOException not thrown");
        } catch (IOException e) {
        }

        f1.delete();
    }

    /**
     * java.util.zip.DeflaterOutputStream#write(byte[], int, int)
     */
    public void test_write$BII() throws Exception {
        byte byteArray[] = { 1, 3, 4, 7, 8, 3, 6 };

        // Test to see if the correct bytes are saved.
        File f1 = File.createTempFile("writeBII", ".tst");
        FileOutputStream fos1 = new FileOutputStream(f1);
        DeflaterOutputStream dos1 = new DeflaterOutputStream(fos1);
        dos1.write(byteArray, 2, 3);
        dos1.close();
        FileInputStream fis = new FileInputStream(f1);
        InflaterInputStream iis = new InflaterInputStream(fis);
        assertEquals("Incorrect Byte Returned.", 4, iis.read());
        assertEquals("Incorrect Byte Returned.", 7, iis.read());
        assertEquals("Incorrect Byte Returned.", 8, iis.read());
        assertEquals("Incorrect Byte Returned (EOF).", -1, iis.read());
        assertEquals("Incorrect Byte Returned (EOF).", -1, iis.read());
        iis.close();
        f1.delete();

        // Test for trying to write more bytes than available from the array
        File f2 = File.createTempFile("writeBII2", ".tst");
        FileOutputStream fos2 = new FileOutputStream(f2);
        DeflaterOutputStream dos2 = new DeflaterOutputStream(fos2);
        try {
            dos2.write(byteArray, 2, 10);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
        }

        // Test for trying to write a negative number of bytes.
        try {
            dos2.write(byteArray, 2, Integer.MIN_VALUE);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
        }

        // Test for trying to start writing from a byte < 0 from the array.
        try {
            dos2.write(byteArray, Integer.MIN_VALUE, 2);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
        }

        // Test for trying to start writing from a byte > than the array
        // size.
        try {
            dos2.write(byteArray, 10, 2);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        dos2.close();

        // Not sure if this test is that important.
        // Checks to see if you can write using the DeflaterOutputStream
        // after
        // the FileOutputStream has been closed.
        FileOutputStream fos3 = new FileOutputStream(f2);
        DeflaterOutputStream dos3 = new DeflaterOutputStream(fos3);
        fos3.close();
        try {
            dos3.write(byteArray, 2, 3);
            fail("IOException not thrown");
        } catch (IOException e) {
        }

        f2.delete();
    }

    public void test_deflate() throws Exception {
        File f1 = File.createTempFile("writeI1", ".tst");
        FileOutputStream fos = new FileOutputStream(f1);
        MyDeflaterOutputStream dos = new MyDeflaterOutputStream(fos);
        assertFalse(dos.getDaflateFlag());
        for (int i = 0; i < 3; i++) {
            dos.write(i);
        }
        assertTrue(dos.getDaflateFlag());
        dos.close();
    }
}
