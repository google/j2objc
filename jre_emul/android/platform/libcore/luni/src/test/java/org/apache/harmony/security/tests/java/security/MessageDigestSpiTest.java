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
 * @author Boris V. Kuznetsov
 * @version $Revision$
 */

package org.apache.harmony.security.tests.java.security;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;

import junit.framework.TestCase;

/**
 * Tests for <code>MessageDigestSpi</code> constructor and methods
 */
public class MessageDigestSpiTest extends TestCase {

    /**
    * java.security.MessageDigestSpi#MessageDigestSpi()
    */
    @SuppressWarnings("cast")
   public void test_constructor() {
        MyMessageDigest mds;
        try {
            mds = new MyMessageDigest();
            assertTrue(mds instanceof MessageDigestSpi);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }

        try {
            mds = new MyMessageDigest();
            byte[] ba = {0, 1, 2, 3, 4, 5};

            mds.engineDigest();
            mds.engineReset();
            mds.engineUpdate(ba[0]);
            mds.engineUpdate(ba, 0, ba.length);
        } catch (Exception e) {
            fail("Unexpected exception for abstract methods");
        }
    }

    /**
     * java.security.MessageDigestSpi#engineDigest(byte[], int, int)
     */
    public void test_engineDigestLB$LILI() throws Exception {

        final int DIGEST_LENGTH = 2;

        MyMessageDigest md = new MyMessageDigest() {

            public int engineGetDigestLength() {
                return DIGEST_LENGTH;
            }

            public byte[] engineDigest() {
                return new byte[DIGEST_LENGTH]; // return non-null value
            }
        };

        byte[] b = new byte[5];
        try {
            // test: null output buffer
            md.engineDigest(null, 1, DIGEST_LENGTH);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            //test: len param < digest length
            md.engineDigest(b, 1, DIGEST_LENGTH - 1);
            fail("No expected DigestException");
        } catch (DigestException e) {
        }

        try {
            //test: offset param > digest length
            md.engineDigest(b, b.length + 1, b.length);
            fail("No expected DigestException - 1");
        } catch (DigestException e) {
        }

        try {
            //test: negative param
            md.engineDigest(b, -1, b.length);
            fail("No expected DigestException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // on RI
        } catch (DigestException e) {
            // ok
        }

        assertEquals("incorrect result", DIGEST_LENGTH, md
                .engineDigest(b, 1, 3));

        // Regression for HARMONY-3045
        md = new MyMessageDigest();
        try {
            md.engineDigest(b, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.security.MessageDigestSpi#engineGetDigestLength()
     */
    public void test_engineGetDigestLength() {
        MyMessageDigest md = new MyMessageDigest();
        assertEquals(0, md.engineGetDigestLength());

        MessageDigest md5Digest = null;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected Exception: " + e);
        }
        assertEquals(16, md5Digest.getDigestLength());
    }

    /**
     * java.security.MessageDigestSpi#engineUpdate(ByteBuffer)
     */
    public void test_engineUpdateLjava_nio_ByteBuffer() {
        MyMessageDigest md = new MyMessageDigest();
        byte[] b = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        ByteBuffer buf = ByteBuffer.wrap(b, 0, b.length);
        buf.get(b);
        int limit = buf.limit();
        md.engineUpdate(buf);
        assertEquals(limit, buf.limit());
        assertEquals(limit, buf.position());

        buf = ByteBuffer.wrap(b, 0, b.length);
        buf.get();
        buf.get();
        buf.get();
        md.engineUpdate(buf);
        assertEquals(limit, buf.limit());
        assertEquals(limit, buf.position());
    }

    /**
     * java.security.MessageDigestSpi#clone()
     */
    public void test_clone() throws CloneNotSupportedException {
        MyMessageDigest md = new MyMessageDigest();
        try {
            md.clone();
            fail("No expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
        }

        MyMessageDigestCloneable mdc = new MyMessageDigestCloneable();
        assertNotSame(mdc, mdc.clone());
    }

    private class MyMessageDigest extends MessageDigestSpi {

        @Override
        public void engineReset() {
        }

        @Override
        public byte[] engineDigest() {
            return null;
        }

        @Override
        public void engineUpdate(byte arg0) {
        }

        @Override
        public void engineUpdate(byte[] arg0, int arg1, int arg2) {
        }

        @Override
        protected int engineDigest(byte[] buf, int offset, int len)
                throws DigestException {
            return super.engineDigest(buf, offset, len);
        }

        @Override
        protected int engineGetDigestLength() {
            return super.engineGetDigestLength();
        }

        @Override
        protected void engineUpdate(ByteBuffer input) {
            super.engineUpdate(input);
        }
    }

    private class MyMessageDigestCloneable extends MyMessageDigest implements
            Cloneable {
    }
}
