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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.crypto.tests.javax.crypto.spec;

import java.lang.NullPointerException;
import java.security.InvalidKeyException;
import java.util.Arrays;

import javax.crypto.spec.DESKeySpec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class DESKeySpecTest extends TestCase {

    // DES weak and semi-weak keys
    // Got from:
    // FIP PUB 74
    // FEDERAL INFORMATION PROCESSING STANDARDS PUBLICATION 1981
    // GUIDELINES FOR IMPLEMENTING AND USING THE NBS DATA ENCRYPTION STANDARD
    // http://www.dice.ucl.ac.be/crypto/standards/fips/fip74/fip74-1.pdf
    private static final byte[][] semiweaks = {
                {(byte) 0xE0, (byte) 0x01, (byte) 0xE0, (byte) 0x01,
                 (byte) 0xF1, (byte) 0x01, (byte) 0xF1, (byte) 0x01},

                {(byte) 0x01, (byte) 0xE0, (byte) 0x01, (byte) 0xE0,
                 (byte) 0x01, (byte) 0xF1, (byte) 0x01, (byte) 0xF1},

                {(byte) 0xFE, (byte) 0x1F, (byte) 0xFE, (byte) 0x1F,
                 (byte) 0xFE, (byte) 0x0E, (byte) 0xFE, (byte) 0x0E},

                {(byte) 0x1F, (byte) 0xFE, (byte) 0x1F, (byte) 0xFE,
                 (byte) 0x0E, (byte) 0xFE, (byte) 0x0E, (byte) 0xFE},

                {(byte) 0xE0, (byte) 0x1F, (byte) 0xE0, (byte) 0x1F,
                 (byte) 0xF1, (byte) 0x0E, (byte) 0xF1, (byte) 0x0E},

                {(byte) 0x1F, (byte) 0xE0, (byte) 0x1F, (byte) 0xE0,
                 (byte) 0x0E, (byte) 0xF1, (byte) 0x0E, (byte) 0xF1},

                {(byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE,
                 (byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE},

                {(byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01,
                 (byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01},

                {(byte) 0x01, (byte) 0x1F, (byte) 0x01, (byte) 0x1F,
                 (byte) 0x01, (byte) 0x0E, (byte) 0x01, (byte) 0x0E},

                {(byte) 0x1F, (byte) 0x01, (byte) 0x1F, (byte) 0x01,
                 (byte) 0x0E, (byte) 0x01, (byte) 0x0E, (byte) 0x01},

                {(byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE,
                 (byte) 0xF1, (byte) 0xFE, (byte) 0xF1, (byte) 0xFE},

                {(byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0,
                 (byte) 0xFE, (byte) 0xF1, (byte) 0xFE, (byte) 0xF1},

                {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                 (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01},

                {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                 (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE},

                {(byte) 0xE0, (byte) 0xE0, (byte) 0xE0, (byte) 0xE0,
                 (byte) 0xF1, (byte) 0xF1, (byte) 0xF1, (byte) 0xF1},

                {(byte) 0x1F, (byte) 0x1F, (byte) 0x1F, (byte) 0x1F,
                 (byte) 0x0E, (byte) 0x0E, (byte) 0x0E, (byte) 0x0E},
            };

    /* DES not weak or semi-weak keys */
    private static final byte[][] notsemiweaks = {
                {(byte) 0x1f, (byte) 0x1f, (byte) 0x1f, (byte) 0x1f,
                 (byte) 0x1f, (byte) 0x1f, (byte) 0x1f, (byte) 0x1f},

                {(byte) 0xe0, (byte) 0xe0, (byte) 0xe0, (byte) 0xe0,
                 (byte) 0xe0, (byte) 0xe0, (byte) 0xe0, (byte) 0xe0}
            };
    /**
     * Constructors testing. Tests behavior of each of two constructors
     * in the cases of: null array, short array, normal array.
     */
    public void testDESKeySpec() {
        try {
            new DESKeySpec((byte []) null);
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
        } catch (InvalidKeyException e) {
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        }
        try {
            new DESKeySpec(new byte [] {1, 2, 3});
            fail("Should raise an InvalidKeyException on a short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        try {
            new DESKeySpec(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
        try {
            new DESKeySpec((byte []) null, 1);
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
        } catch (InvalidKeyException e) {
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        }
        try {
            new DESKeySpec(new byte []  {1, 2, 3, 4, 5, 6, 7, 8}, 1);
            fail("Should raise an InvalidKeyException on a short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        try {
            new DESKeySpec(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, 1);
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
    }

    /**
     * getKey() method testing. Checks that modification of returned key
     * does not affect the internal key. Also test check an equality of
     * the key with the key specified in the constructor. The object under
     * the test is created by different constructors.
     */
    public void testGetKey() {
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        DESKeySpec ks;
        try {
            ks = new DESKeySpec(key);
        } catch (InvalidKeyException e) {
            fail("InvalidKeyException should not be thrown.");
            return;
        }
        byte[] res = ks.getKey();
        assertTrue("The returned array should be equal to the specified "
                    + "in constructor.", Arrays.equals(key, res));
        res[0] += 1;
        assertFalse("The modification of returned key should not affect"
                    + "the underlying key.", key[0] == res[0]);

        byte[] key1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        try {
            ks = new DESKeySpec(key1, 2);
        } catch (InvalidKeyException e) {
            fail("InvalidKeyException should not be thrown.");
            return;
        }
        res = ks.getKey();
        assertNotSame("The returned array should not be the same object "
                    + "as specified in a constructor.", key1, res);
        byte[] exp = new byte[8];
        System.arraycopy(key1, 2, exp, 0, 8);
        assertTrue("The returned array should be equal to the specified "
                    + "in constructor.", Arrays.equals(exp, res));
    }

    /**
     * isParityAdjusted(byte[] key, offset) method testing. Tests if the
     * method throws appropriate exceptions on incorrect byte array, if
     * it returns false on the key which is not parity adjusted, and if
     * it returns true on parity adjusted key.
     */
    public void testIsParityAdjusted() {
        try {
            DESKeySpec.isParityAdjusted(null, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }

        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        try {
            DESKeySpec.isParityAdjusted(key, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }

        byte[] key_not_pa = {1, 2, 3, 4, 5, 6, 7, 8};
        try {
            assertFalse("Method returns true when false is expected.",
                        DESKeySpec.isParityAdjusted(key_not_pa, 0));
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }

        byte[] key_pa = {(byte) 128, (byte) 131, (byte) 133, (byte) 134,
                         (byte) 137, (byte) 138, (byte) 140, (byte) 143};
        try {
            assertTrue("Method returns false when true is expected.",
                        DESKeySpec.isParityAdjusted(key_pa, 0));
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
    }

    /**
     * isWeak(byte[] key, int offset) method testing. Tests if the
     * method throws appropriate exceptions on incorrect byte array, if
     * it returns true on weak or semi-weak keys, and if it returns
     * false on other keys.
     */
    public void testIsWeak() {
        try {
            DESKeySpec.isWeak(null, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }

        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        try {
            DESKeySpec.isWeak(key, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }

        for (int i=0; i<semiweaks.length; i++) {
            try {
                assertTrue("Method returns false when true is expected",
                        DESKeySpec.isWeak(semiweaks[i], 0));
            } catch (InvalidKeyException e) {
                fail("Unexpected InvalidKeyException was thrown.");
            }
        }
        for (int i=0; i<notsemiweaks.length; i++) {
            try {
                assertFalse("Method returns true when false is expected",
                        DESKeySpec.isWeak(notsemiweaks[i], 0));
            } catch (InvalidKeyException e) {
                fail("Unexpected InvalidKeyException was thrown.");
            }
        }
    }

    public static Test suite() {
        return new TestSuite(DESKeySpecTest.class);
    }
}

