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

package org.apache.harmony.security.tests.java.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import tests.support.Support_ASimpleInputStream;

public class DigestInputStream2Test extends junit.framework.TestCase {

    ByteArrayInputStream inStream;

    ByteArrayInputStream inStream1;

    MessageDigest digest;

    /**
     * java.security.DigestInputStream#DigestInputStream(java.io.InputStream,
     *        java.security.MessageDigest)
     */
    public void test_ConstructorLjava_io_InputStreamLjava_security_MessageDigest() {
        // Test for method java.security.DigestInputStream(java.io.InputStream,
        // java.security.MessageDigest)
        DigestInputStream dis = new DigestInputStream(inStream, digest);
        assertNotNull("Constructor returned null instance", dis);
    }

    /**
     * java.security.DigestInputStream#getMessageDigest()
     */
    public void test_getMessageDigest() {
        // Test for method java.security.MessageDigest
        // java.security.DigestInputStream.getMessageDigest()
        DigestInputStream dis = new DigestInputStream(inStream, digest);
        assertEquals("getMessageDigest returned a bogus result", digest, dis
                .getMessageDigest());
    }

    /**
     * java.security.DigestInputStream#on(boolean)
     */
    public void test_onZ() throws Exception {
        // Test for method void java.security.DigestInputStream.on(boolean)
        MessageDigest originalDigest = (MessageDigest) (digest.clone());
        MessageDigest noChangeDigest = (MessageDigest) (digest.clone());
        DigestInputStream dis = new DigestInputStream(inStream, noChangeDigest);
        // turn off processing
        dis.on(false);
        // read some data
        int c = dis.read();
        assertEquals('T', c);

        // make sure the digest for the part where it was off has not
        // changed
        assertTrue("MessageDigest changed even though processing was off",
                MessageDigest.isEqual(noChangeDigest.digest(), originalDigest
                        .digest()));
        MessageDigest changeDigest = (MessageDigest) (digest.clone());
        dis = new DigestInputStream(inStream, digest);

        // turn on processing
        dis.on(true);
        c = dis.read();
        assertEquals('h', c);

        // make sure the digest has changed
        assertTrue("MessageDigest did not change with processing on",
                !MessageDigest.isEqual(digest.digest(), changeDigest.digest()));
    }

    /**
     * java.security.DigestInputStream#read()
     */
    public void test_read() throws IOException {
        // Test for method int java.security.DigestInputStream.read()
        DigestInputStream dis = new DigestInputStream(inStream, digest);

        // read and compare the data that the inStream has
        int c;
        while ((c = dis.read()) > -1) {
            int d = inStream1.read();
            assertEquals(d, c);
        }// end while
    }

    /**
     * java.security.DigestInputStream#read(byte[], int, int)
     */
    public void test_read$BII() throws IOException {
        // Test for method int java.security.DigestInputStream.read(byte [],
        // int, int)
        DigestInputStream dis = new DigestInputStream(inStream, digest);
        int bytesToRead = inStream.available();
        byte buf1[] = new byte[bytesToRead + 5];
        byte buf2[] = new byte[bytesToRead + 5];
        // make sure we're actually reading some data
        assertTrue("No data to read for this test", bytesToRead>0);

        // read and compare the data that the inStream has
        int bytesRead1 = dis.read(buf1, 5, bytesToRead);
        int bytesRead2 = inStream1.read(buf2, 5, bytesToRead);
        assertEquals("Didn't read the same from each stream", bytesRead1,
                bytesRead2);
        assertEquals("Didn't read the entire", bytesRead1, bytesToRead);
        // compare the arrays
        boolean same = true;
        for (int i = 0; i < bytesToRead + 5; i++) {
            if (buf1[i] != buf2[i]) {
                same = false;
            }
        }// end for
        assertTrue("Didn't get the same data", same);
    }

    /**
     * java.security.DigestInputStream#read(byte[], int, int)
     */
    public void test_read$BII_Exception() throws IOException {
        DigestInputStream is = new DigestInputStream(inStream, digest);
        byte[] buf = null;
        try {
            is.read(buf, -1, 0);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        buf = new byte[1000];
        try {
            is.read(buf, -1, 0);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            is.read(buf, 0, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            is.read(buf, -1, -1);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            is.read(buf, 0, 1001);
            fail("Test 5: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            is.read(buf, 1001, 0);
            fail("Test 6: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            is.read(buf, 500, 501);
            fail("Test 7: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        is.close();

        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(true);
        is = new DigestInputStream(sis, digest);
        try {
            is.read(buf, 0, 100);
            fail("Test 9: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sis.throwExceptionOnNextUse = false;
        is.close();
    }

    /**
     * java.security.DigestInputStream#setMessageDigest(java.security.MessageDigest)
     */
    public void test_setMessageDigestLjava_security_MessageDigest() {
        // Test for method void
        // java.security.DigestInputStream.setMessageDigest(java.security.MessageDigest)
        DigestInputStream dis = new DigestInputStream(inStream, null);

        // make sure the digest is null when it's not been set
        assertNull(
                "Uninitialised MessageDigest should have been returned as null",
                dis.getMessageDigest());
        dis.setMessageDigest(digest);
        assertEquals("Wrong MessageDigest was returned.", digest, dis
                .getMessageDigest());
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        // create a ByteArrayInputStream to perform digesting on
        inStream = new ByteArrayInputStream(
                "This is a test string for digesting".getBytes());
        inStream1 = new ByteArrayInputStream(
                "This is a test string for digesting".getBytes());
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            fail("Unable to find SHA-1 algorithm");
        }
    }
}
