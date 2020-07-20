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

import java.lang.IllegalArgumentException;
import java.util.Arrays;

import javax.crypto.spec.RC2ParameterSpec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class RC2ParameterSpecTest extends TestCase {

    /**
     * RC2ParameterSpec(int effectiveKeyBits, byte[] iv) method testing.
     * Tests that IllegalArgumentException is thrown in the case of
     * inappropriate constructor parameters and that input iv array is
     * copied to protect against subsequent modification.
     */
    public void testRC2ParameterSpec1() {
        int effectiveKeyBits = 10;
        byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};

        try {
            new RC2ParameterSpec(effectiveKeyBits, null);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null iv.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new RC2ParameterSpec(effectiveKeyBits, new byte[] {1, 2, 3, 4, 5});
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }

        RC2ParameterSpec ps = new RC2ParameterSpec(effectiveKeyBits, iv);
        iv[0] ++;
        assertFalse("The change of iv specified in the constructor "
                    + "should not cause the change of internal array.",
                    iv[0] == ps.getIV()[0]);
    }

    /**
     * RC2ParameterSpec(int effectiveKeyBits, byte[] iv, int offset) method
     * testing. Tests that IllegalArgumentException is thrown in the case of
     * inappropriate constructor parameters and that input iv array is
     * copied to protect against subsequent modification.
     */
    public void testRC2ParameterSpec2() {
        int effectiveKeyBits = 10;
        byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        int offset = 2;

        try {
            new RC2ParameterSpec(effectiveKeyBits, null, offset);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null iv.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new RC2ParameterSpec(effectiveKeyBits, iv, 4);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }

        RC2ParameterSpec ps = new RC2ParameterSpec(effectiveKeyBits, iv, offset);
        iv[offset] ++;
        assertFalse("The change of iv specified in the constructor "
                    + "should not cause the change of internal array.",
                    iv[offset] == ps.getIV()[0]);
    }

    /**
     * getEffectiveKeyBits() method testing. Tests that returned value is
     * equal to the value specified in the constructor.
     */
    public void testGetEffectiveKeyBits() {
        int effectiveKeyBits = 10;
        byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};

        RC2ParameterSpec ps = new RC2ParameterSpec(effectiveKeyBits, iv);
        assertTrue("The returned effectiveKeyBits value is not equal to the "
                + "value specified in the constructor.",
                effectiveKeyBits == ps.getEffectiveKeyBits());
    }

    /**
     * getIV() method testing. Tests that returned array is equal to the
     * array specified in the constructor. Checks that modification
     * of returned array does not affect the internal array. Also it checks
     * that getIV() method returns null if iv is not specified.
     */
    public void testGetIV() {
        int effectiveKeyBits = 10;
        byte[] iv = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};

        RC2ParameterSpec ps = new RC2ParameterSpec(effectiveKeyBits, iv);
        byte[] result = ps.getIV();
        if (! Arrays.equals(iv, result)) {
            fail("The returned iv is not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getIV() method iv "
                    + "should not cause the change of internal array.",
                    result[0] == ps.getIV()[0]);
        ps = new RC2ParameterSpec(effectiveKeyBits);
        assertNull("The getIV() method should return null if the parameter "
                    + "set does not contain iv.", ps.getIV());
    }

    /**
     * equals(Object obj) method testing. Tests the correctness of equal
     * operation: it should be reflexive, symmetric, transitive, consistent
     * and should be false on null object.
     */
    public void testEquals() {
        int effectiveKeyBits = 10;
        byte[] iv = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};

        RC2ParameterSpec ps1 = new RC2ParameterSpec(effectiveKeyBits, iv);
        RC2ParameterSpec ps2 = new RC2ParameterSpec(effectiveKeyBits, iv);
        RC2ParameterSpec ps3 = new RC2ParameterSpec(10,
                                    new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9});

        // checking for reflexive law:
        assertTrue("The equivalence relation should be reflexive.",
                                                        ps1.equals(ps1));

        assertTrue("Objects built on the same parameters should be equal.",
                                                        ps1.equals(ps2));
        // checking for symmetric law:
        assertTrue("The equivalence relation should be symmetric.",
                                                        ps2.equals(ps1));

        assertTrue("Objects built on the equal parameters should be equal.",
                                                        ps2.equals(ps3));

        // checking for transitive law:
        assertTrue("The equivalence relation should be transitive.",
                                                        ps1.equals(ps3));

        assertFalse("Should return not be equal to null object.",
                                                        ps1.equals(null));

        ps2 = new RC2ParameterSpec(11, iv);
        assertFalse("Objects should not be equal.", ps1.equals(ps2));

        ps2 = new RC2ParameterSpec(11, new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1});
        assertFalse("Objects should not be equal.", ps1.equals(ps2));
    }

    /**
     * hashCode() method testing. Tests that for equal objects hash codes
     * are equal.
     */
    public void testHashCode() {
        int effectiveKeyBits = 0;
        byte[] iv = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};

        RC2ParameterSpec ps1 = new RC2ParameterSpec(effectiveKeyBits, iv);
        RC2ParameterSpec ps2 = new RC2ParameterSpec(effectiveKeyBits, iv);

        assertTrue("Equal objects should have the same hash codes.",
                                            ps1.hashCode() == ps2.hashCode());
    }

    public void test_constructorI() {
        int effectiveKeyBits = 0;

        RC2ParameterSpec ps1 = new RC2ParameterSpec(effectiveKeyBits);
        RC2ParameterSpec ps2 = new RC2ParameterSpec(effectiveKeyBits);

        assertTrue(ps1.equals(ps2));
    }

    public static Test suite() {
        return new TestSuite(RC2ParameterSpecTest.class);
    }
}

