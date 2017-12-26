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

/**
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import junit.framework.TestCase;
import org.apache.harmony.security.tests.support.MDGoldenData;
import org.apache.harmony.security.tests.support.MyMessageDigest1;
import tests.support.Support_OutputStream;

/**
 * Tests for fields and methods of class <code>DigestInputStream</code>
 *
 */
public class DigestOutputStreamTest extends TestCase {

    /**
     * Message digest algorithm name used during testing
     */
    private static final String algorithmName[] = {
            "SHA-1",
            "SHA",
            "SHA1",
            "SHA-256",
            "SHA-384",
            "SHA-512",
            "MD5",
    };
    /**
     * Chunk size for read(byte, off, len) tests
     */
    private static final int CHUNK_SIZE = 32;
    /**
     * Test message for digest computations
     */
    private static final byte[] myMessage = MDGoldenData.getMessage();
    /**
     * The length of test message
     */
    private static final int MY_MESSAGE_LEN = myMessage.length;

    //
    // Tests
    //

    /**
     * java.security.DigestOutputStream#DigestOutputStream(java.io.OutputStream,
     *        java.security.MessageDigest)
     */
    public void test_CtorLjava_io_OutputStreamLjava_security_MessageDigest() {

        // non-null parameters
        MessageDigest md = new MyMessageDigest1();
        MyOutputStream out = new MyOutputStream();

        MyDigestOutputStream dos = new MyDigestOutputStream(out, md);
        assertSame(out, dos.myOutputStream());
        assertSame(md, dos.myMessageDigest());

        // null parameters
        dos = new MyDigestOutputStream(null, null);
        assertNull(dos.myOutputStream());
        assertNull(dos.myMessageDigest());

        dos = new MyDigestOutputStream(null, md);
        assertNull(dos.myOutputStream());
        assertNotNull(dos.myMessageDigest());

        dos = new MyDigestOutputStream(out, null);
        assertNotNull(dos.myOutputStream());
        assertNull(dos.myMessageDigest());
    }

    /**
     * java.security.DigestOutputStream#getMessageDigest()
     */
    public void test_getMessageDigest() {

        MessageDigest digest = new MyMessageDigest1();
        OutputStream out = new MyOutputStream();

        // non-null parameter
        DigestOutputStream dos = new DigestOutputStream(out, digest);
        assertSame(digest, dos.getMessageDigest());

        // null parameter
        dos = new DigestOutputStream(out, null);
        assertNull("getMessageDigest should have returned null", dos.getMessageDigest());
    }

    /**
     * java.security.DigestOutputStream#setMessageDigest(MessageDigest)
     */
    public void test_setMessageDigestLjava_security_MessageDigest() {

        MessageDigest digest = new MyMessageDigest1();
        OutputStream out = new MyOutputStream();

        DigestOutputStream dos = new DigestOutputStream(out, null);

        // non-null parameter
        dos.setMessageDigest(digest);
        assertSame(digest, dos.getMessageDigest());

        // null parameter
        dos.setMessageDigest(null);
        assertNull("getMessageDigest should have returned null", dos.getMessageDigest());
    }


    /**
     * Test #1 for <code>write(int)</code> method<br>
     *
     * Assertion: writes the byte to the output stream<br>
     * Assertion: updates associated digest<br>
     */
    public final void testWriteint01()
        throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    dos.write(myMessage[i]);
                }
                // check that bytes have been written correctly
                assertTrue("write", Arrays.equals(MDGoldenData.getMessage(), bos.toByteArray()));
                // check that associated digest has been updated properly
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                                                   MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #2 for <code>write(int)</code> method<br>
     * Test #1 for <code>on(boolean)</code> method<br>
     *
     * Assertion: <code>write(int)</code> must not update digest if it is off<br>
     * Assertion: <code>on(boolean)</code> turns digest functionality on
     * if <code>true</code> passed as a parameter or off if <code>false</code>
     * passed
     */
    public final void testWriteint02()
        throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                DigestOutputStream dos = new DigestOutputStream(bos, md);

                // turn digest off
                dos.on(false);

                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    dos.write(myMessage[i]);
                }

                // check that bytes have been written correctly
                assertTrue("write", Arrays.equals(MDGoldenData.getMessage(), bos.toByteArray()));
                // check that digest value has not been updated by write()
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                                                   MDGoldenData.getDigest(algorithmName[k]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #3 for <code>write(int)</code> method<br>
     *
     * Assertion: broken <code>DigestOutputStream</code>instance:
     * <code>OutputStream</code> not set. <code>write(int)</code> must
     * not work
     */
    public final void testWriteint03() throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(null, md);
                // must result in an exception
                try {
                    for (int i=0; i<MY_MESSAGE_LEN; i++) {
                        dos.write(myMessage[i]);
                    }
                    fail("OutputStream not set. write(int) must not work");
                } catch (Exception e) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #4 for <code>write(int)</code> method<br>
     *
     * Assertion: broken <code>DigestOutputStream</code>instance:
     * associated <code>MessageDigest</code> not set.
     * <code>write(int)</code> must not work when digest
     * functionality is on
     */
    public final void testWriteint04() throws IOException {
        OutputStream os = new ByteArrayOutputStream(MY_MESSAGE_LEN);
        DigestOutputStream dos = new DigestOutputStream(os, null);

        // must result in an exception
        try {
            for (int i=0; i<MY_MESSAGE_LEN; i++) {
                dos.write(myMessage[i]);
            }
            fail("OutputStream not set. write(int) must not work");
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Test #5 for <code>write(int)</code> method<br>
     * Test #2 for <code>on(boolean)</code> method<br>
     *
     * Assertion: broken <code>DigestOutputStream</code>instance:
     * associated <code>MessageDigest</code> not set.
     * <code>write(int)</code> must work when digest
     * functionality is off
     */
    public final void testWriteint05() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
        DigestOutputStream dos = new DigestOutputStream(bos, null);
        // set digest functionality to off
        dos.on(false);
        // the following must pass without any exception
        for (int i=0; i<MY_MESSAGE_LEN; i++) {
            dos.write(myMessage[i]);
        }
        // check that bytes have been written correctly
        assertTrue(Arrays.equals(MDGoldenData.getMessage(), bos.toByteArray()));
    }

    /**
     * Test #1 for <code>write(byte[],int,int)</code> method<br>
     *
     * Assertion: put bytes into output stream<br>
     *
     * Assertion: updates associated digest<br>
     */
    public final void test_write$BII_1() throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);

                // write message at once
                dos.write(myMessage, 0, MY_MESSAGE_LEN);

                // check write
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                // check that associated digest has been updated properly
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                                                   MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #2 for <code>write(byte[],int,int)</code> method<br>
     *
     * Assertion: put bytes into output stream<br>
     *
     * Assertion: updates associated digest<br>
     */
    public final void test_write$BII_2() throws IOException {
        // check precondition
        assertEquals(0, MY_MESSAGE_LEN % CHUNK_SIZE);
        for (int k=0; k<algorithmName.length; k++) {
            try {

                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);

                // write message by chunks
                for (int i=0; i<MY_MESSAGE_LEN/CHUNK_SIZE; i++) {
                    dos.write(myMessage, i*CHUNK_SIZE, CHUNK_SIZE);
                }
                // check write
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                // check that associated digest has been updated properly
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }


    /**
     * Test #3 for <code>write(byte[],int,int)</code> method<br>
     *
     * Assertion: put bytes into output stream<br>
     *
     * Assertion: updates associated digest<br>
     */
    public final void test_write$BII_3()
        throws NoSuchAlgorithmException,
               IOException {
        // check precondition
        assertTrue(MY_MESSAGE_LEN % (CHUNK_SIZE+1) != 0);

        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);

                // write message by chunks
                for (int i=0; i<MY_MESSAGE_LEN/(CHUNK_SIZE+1); i++) {
                    dos.write(myMessage, i*(CHUNK_SIZE+1), CHUNK_SIZE+1);
                }
                // write remaining bytes
                dos.write(myMessage,
                        MY_MESSAGE_LEN/(CHUNK_SIZE+1)*(CHUNK_SIZE+1),
                        MY_MESSAGE_LEN % (CHUNK_SIZE+1));
                // check write
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                // check that associated digest has been updated properly
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #4 for <code>write(byte[],int,int)</code> method<br>
     *
     * Assertion: put bytes into output stream<br>
     *
     * Assertion: does not update associated digest if digest
     * functionality is off<br>
     */
    public final void test_write$BII_4()
        throws NoSuchAlgorithmException,
               IOException {
        // check precondition
        assertEquals(0, MY_MESSAGE_LEN % CHUNK_SIZE);

        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);

                // set digest functionality off
                dos.on(false);

                // write message by chunks
                for (int i=0; i<MY_MESSAGE_LEN/CHUNK_SIZE; i++) {
                    dos.write(myMessage, i*CHUNK_SIZE, CHUNK_SIZE);
                }

                // check write
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                // check that associated digest has not been updated
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * java.security.DigestOutputStream#write(byte[], int, int)
     */
    public void test_write$BII_6() throws Exception {

        // Regression form HARMONY-1091.
        MessageDigest md = new MyMessageDigest1();
        byte[] bytes = new byte[] { 1, 2 };
        DigestOutputStream dig = new DigestOutputStream(
                new ByteArrayOutputStream(), md);
        // buf == null
        try {
            dig.write(null, -1, 0);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        // offset + len > buf.length
        try {
            dig.write(bytes, 0, bytes.length + 1);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        // offset < 0
        try {
            dig.write(bytes, -1, 1);
            fail("No expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        // len < 0
        try {
            dig.write(bytes, 0, -1);
            fail("No expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * java.io.DigestOutputStream#write(byte[], int, int)
     */
    public void test_write$BII_7()
        throws IOException, NoSuchAlgorithmException {
        Support_OutputStream sos = new Support_OutputStream(MY_MESSAGE_LEN);
        MessageDigest md = MessageDigest.getInstance(algorithmName[0]);
        DigestOutputStream dos = new DigestOutputStream(sos, md);

        dos.write(myMessage, 0, MY_MESSAGE_LEN);

        try {
            // Support_OutputStream throws an IOException if the internal
            // buffer is full, which it should be now.
            dos.write(myMessage, 0, MY_MESSAGE_LEN);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * Test for <code>on()</code> method<br>
     * Assertion: turns digest functionality on or off
     */
    public final void testOn() throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);

                // turn digest off
                dos.on(false);

                for (int i=0; i<MY_MESSAGE_LEN-1; i++) {
                    dos.write(myMessage[i]);
                }

                // turn digest on
                dos.on(true);

                // read remaining byte
                dos.write(myMessage[MY_MESSAGE_LEN-1]);

                byte[] digest = dos.getMessageDigest().digest();

                // check that digest value has been
                // updated by the last write(int) call
                assertFalse(Arrays.equals(digest,MDGoldenData.getDigest(algorithmName[k])));
                assertFalse(Arrays.equals(digest,MDGoldenData.getDigest(algorithmName[k]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test for <code>toString()</code> method<br>
     * Assertion: returns <code>String</code> representation of this object
     */
    public final void testToString() throws NoSuchAlgorithmException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);

                assertNotNull(dos.toString());
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * java.security.DigestOutputStream#on(boolean)
     */
    public void test_onZ() throws Exception {
        // Test for method void java.security.DigestOutputStream.on(boolean)
        DigestOutputStream dos = new DigestOutputStream(
                new ByteArrayOutputStream(), MessageDigest.getInstance("SHA"));
        dos.on(false);
        byte digestArray[] = { 23, 43, 44 };
        dos.write(digestArray, 1, 1);
        byte digestResult[] = dos.getMessageDigest().digest();
        byte expected[] = { -38, 57, -93, -18, 94, 107, 75, 13, 50, 85,
                -65, -17, -107, 96, 24, -112, -81, -40, 7, 9 };
        assertTrue("Digest did not return expected result.",
                   Arrays.equals(digestResult, expected));
        // now turn on processing and re-run
        dos.on(true);
        dos.write(digestArray, 1, 1);
        digestResult = dos.getMessageDigest().digest();
        byte expected1[] = { -87, 121, -17, 16, -52, 111, 106, 54, -33,
                107, -118, 50, 51, 7, -18, 59, -78, -30, -37, -100 };

        assertTrue("Digest did not return expected result.",
                   Arrays.equals(digestResult, expected1));
    }

    /**
     * java.security.DigestOutputStream#write(byte[], int, int)
     */
    public void test_write$BII_5() throws Exception {
        // Test for method void java.security.DigestOutputStream.write(byte [],
        // int, int)
            DigestOutputStream dos = new DigestOutputStream(
                new ByteArrayOutputStream(), MessageDigest.getInstance("SHA"));
            byte digestArray[] = { 23, 43, 44 };
            dos.write(digestArray, 1, 1);
            byte digestResult[] = dos.getMessageDigest().digest();
            byte expected[] = { -87, 121, -17, 16, -52, 111, 106, 54, -33, 107,
                    -118, 50, 51, 7, -18, 59, -78, -30, -37, -100 };

            assertTrue("Digest did not return expected result.",
                       Arrays.equals(digestResult, expected));
    }

    /**
     * java.security.DigestOutputStream#write(int)
     */
    public void test_writeI() throws Exception {
        // Test for method void java.security.DigestOutputStream.write(int)
            DigestOutputStream dos = new DigestOutputStream(
                new ByteArrayOutputStream(), MessageDigest.getInstance("SHA"));
            dos.write((byte) 43);
            byte digestResult[] = dos.getMessageDigest().digest();
            byte expected[] = { -87, 121, -17, 16, -52, 111, 106, 54, -33, 107,
                    -118, 50, 51, 7, -18, 59, -78, -30, -37, -100 };

            assertTrue("Digest did not return expected result.",
                       Arrays.equals(digestResult, expected));
    }


    private class MyOutputStream extends OutputStream {
        @Override
        public void write(int arg0) throws IOException {
        }
    }

    private class MyDigestOutputStream extends DigestOutputStream {
        public MyDigestOutputStream(OutputStream out, MessageDigest digest) {
            super(out, digest);
        }

        public MessageDigest myMessageDigest() {
            return digest;
        }

        public OutputStream myOutputStream() {
            return out;
        }
    }
}
