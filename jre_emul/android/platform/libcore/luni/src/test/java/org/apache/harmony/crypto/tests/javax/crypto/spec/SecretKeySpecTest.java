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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

/**
 */
public class SecretKeySpecTest extends TestCase {

    /**
     * SecretKeySpec(byte[] key, String algorithm) method testing. Tests that
     * IllegalArgumentException is thrown in the case of inappropriate
     * constructor parameters and that input iv array is
     * copied to protect against subsequent modification.
     */
    public void testSecretKeySpec1() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";

        try {
            new SecretKeySpec(new byte[] {}, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of empty key.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new SecretKeySpec(null, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null key.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new SecretKeySpec(key, null);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null algorithm.");
        } catch (IllegalArgumentException e) {
        }

        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        key[0] ++;
        assertFalse("The change of key specified in the constructor "
                    + "should not cause the change of internal array.",
                    key[0] == ks.getEncoded()[0]);
    }

    /**
     * SecretKeySpec(byte[] key, int offset, int len, String algorithm) method
     * testing. Tests that IllegalArgumentException is thrown in
     * the case of inappropriate constructor parameters and that input iv array
     * is copied to protect against subsequent modification.
     */
    public void testSecretKeySpec2() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        int offset = 1;
        int len = 4;
        String algorithm = "Algorithm";

        try {
            new SecretKeySpec(new byte[] {}, 0, 0, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of empty key.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new SecretKeySpec(null, 0, 0, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null key.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new SecretKeySpec(key, offset, len, null);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short key algorithm.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new SecretKeySpec(key, offset, key.length, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null key.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new SecretKeySpec(key, 0, -1, algorithm);
            fail("An ArrayIndexOutOfBoundsException should be thrown "
                    + "in the case of illegal length.");
        } catch (IllegalArgumentException e) {
            fail("Not expected IllegalArgumentException was thrown.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        key[offset] ++;
        assertFalse("The change of key specified in the constructor "
                    + "should not cause the change of internal array.",
                    key[offset] == ks.getEncoded()[0]);

        // Regression test for HARMONY-1077
        try {
            new SecretKeySpec(new byte[] { 2 }, 4, -100, "CCC");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    public void testSecretKeySpec3() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        int offset = 1;
        int len = 4;
        String algorithm = "Algorithm";

        try {
            new SecretKeySpec(key, -1, key.length, algorithm);
            fail("An ArrayIndexOutOfBoundsException should be thrown "
                    + "in the case of illegal offset.");
        } catch (IllegalArgumentException e) {
            fail("Not expected IllegalArgumentException was thrown.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    /**
     * getAlgorithm() method testing. Tests that returned value is
     * equal to the value specified in the constructor.
     */
    public void testGetAlgorithm() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";

        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        assertEquals("The returned value does not equal to the "
                + "value specified in the constructor.",
                algorithm, ks.getAlgorithm());
    }

    /**
     * getFormat() method testing. Tests that returned value is "RAW".
     */
    public void testGetFormat() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";

        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        assertTrue("The returned value is not \"RAW\".",
                ks.getFormat() == "RAW");
    }

    /**
     * getEncoded() method testing. Tests that returned array is equal to the
     * array specified in the constructor. Checks that modification
     * of returned array does not affect the internal array.
     */
    public void testGetEncoded() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";

        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        byte[] result = ks.getEncoded();
        if (! Arrays.equals(key, result)) {
            fail("The returned key does not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getEncoded() method key "
                    + "should not cause the change of internal array.",
                    result[0] == ks.getEncoded()[0]);

        // Regression for HARMONY-78
        int offset = 1;
        int len = 4;
        SecretKeySpec sks = new SecretKeySpec(key, offset, len, algorithm);
        assertEquals("Key length is incorrect", len, sks.getEncoded().length);
    }

    /**
     * hashCode() method testing. Tests that for equal objects hash codes
     * are equal.
     */
    public void testHashCode() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";

        SecretKeySpec ks1 = new SecretKeySpec(key, algorithm);
        SecretKeySpec ks2 = new SecretKeySpec(key, algorithm);
        assertTrue("Equal objects should have the same hash codes.",
                                            ks1.hashCode() == ks2.hashCode());
    }

    /**
     * equals(Object obj) method testing. Tests the correctness of equal
     * operation: it should be reflexive, symmetric, transitive, consistent
     * and should be false on null object.
     */
    public void testEquals() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";

        SecretKeySpec ks1 = new SecretKeySpec(key, algorithm);
        SecretKeySpec ks2 = new SecretKeySpec(key, algorithm);
        SecretKeySpec ks3 = new SecretKeySpec(key, algorithm);

        // checking for reflexive law:
        assertTrue("The equivalence relation should be reflexive.",
                                                        ks1.equals(ks1));

        assertTrue("Objects built on the same parameters should be equal.",
                                                        ks1.equals(ks2));
        // checking for symmetric law:
        assertTrue("The equivalence relation should be symmetric.",
                                                        ks2.equals(ks1));

        assertTrue("Objects built on the equal parameters should be equal.",
                                                        ks2.equals(ks3));
        // checking for transitive law:
        assertTrue("The equivalence relation should be transitive.",
                                                        ks1.equals(ks3));

        assertFalse("Should not be equal to null object.",
                                                        ks1.equals(null));

        ks2 = new SecretKeySpec(new byte[] {1}, algorithm);
        assertFalse("Objects should not be equal.", ks1.equals(ks2));

        ks2 = new SecretKeySpec(key, "Another Algorithm");
        assertFalse("Objects should not be equal.", ks1.equals(ks2));
    }

    public static Test suite() {
        return new TestSuite(SecretKeySpecTest.class);
    }
}

