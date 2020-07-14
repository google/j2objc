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

import java.util.Arrays;

import javax.crypto.spec.RC5ParameterSpec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class RC5ParameterSpecTest extends TestCase {

    /**
     * RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv) method
     * testing. Tests that IllegalArgumentException is thrown in the case of
     * inappropriate constructor parameters and that input iv array is
     * copied to protect against subsequent modification.
     */
    public void testRC5ParameterSpec1() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4};

        try {
            new RC5ParameterSpec(version, rounds, wordSize, null);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null iv.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new RC5ParameterSpec(version, rounds, wordSize+8, iv);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new RC5ParameterSpec(version, rounds, wordSize, new byte[] {1, 2, 3});
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }

        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        iv[0] ++;
        assertFalse("The change of iv specified in the constructor "
                    + "should not cause the change of internal array.",
                    iv[0] == ps.getIV()[0]);
    }

    /**
     * RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv, int
     * offset) method testing. Tests that IllegalArgumentException is thrown in
     * the case of inappropriate constructor parameters and that input iv array
     * is copied to protect against subsequent modification.
     */
    public void testRC5ParameterSpec2() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4, 5, 6};
        int offset = 2;

        try {
            new RC5ParameterSpec(version, rounds, wordSize, null, offset);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null iv.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new RC5ParameterSpec(version, rounds, wordSize+8, iv, offset);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new RC5ParameterSpec(version, rounds, wordSize, iv, offset+1);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new RC5ParameterSpec(version, rounds, wordSize, new byte[] { 1, 2,
                    3, 4 }, offset);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }

        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds, wordSize,
                                                                    iv, offset);
        iv[offset] ++;
        assertFalse("The change of iv specified in the constructor "
                    + "should not cause the change of internal array.",
                    iv[offset] == ps.getIV()[0]);

        // Regression test for HARMONY-1077
        try {
            new RC5ParameterSpec(0, 9, 77, new byte[] { 2 }, -100);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * getVersion() method testing. Tests that returned value is
     * equal to the value specified in the constructor.
     */
    public void testGetVersion() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;

        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds, wordSize);
        assertTrue("The returned version value should be equal to the "
                + "value specified in the constructor.",
                ps.getVersion() == version);
    }

    /**
     * getRounds() method testing. Tests that returned value is
     * equal to the value specified in the constructor.
     */
    public void testGetRounds() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;

        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds, wordSize);
        assertTrue("The returned rounds value should be equal to the "
                + "value specified in the constructor.",
                ps.getRounds() == rounds);
    }

    /**
     * getWordSize() method testing. Tests that returned value is
     * equal to the value specified in the constructor.
     */
    public void testGetWordSize() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;

        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds, wordSize);
        assertTrue("The returned wordSize value should be equal to the "
                + "value specified in the constructor.",
                ps.getWordSize() == wordSize);
    }

    /**
     * getIV() method testing. Tests that returned array is equal to the
     * array specified in the constructor. Checks that modification
     * of returned array does not affect the internal array. Also it checks
     * that getIV() method returns null if iv is not specified.
     */
    public void testGetIV() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4};

        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds,
                                                            wordSize, iv);
        byte[] result = ps.getIV();
        if (! Arrays.equals(iv, result)) {
            fail("The returned iv is not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getIV() method iv "
                    + "should not cause the change of internal array.",
                    result[0] == ps.getIV()[0]);
        ps = new RC5ParameterSpec(version, rounds, wordSize);
        assertNull("The getIV() method should return null if the parameter "
                    + "set does not contain IV.", ps.getIV());
    }

    /**
     * equals(Object obj) method testing. Tests the correctness of equal
     * operation: it should be reflexive, symmetric, transitive, consistent
     * and should be false on null object.
     */
    public void testEquals() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4, 5, 6};

        RC5ParameterSpec ps1 = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        RC5ParameterSpec ps2 = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        RC5ParameterSpec ps3 = new RC5ParameterSpec(version, rounds, wordSize,
                                                    new byte[] {1, 2, 3, 4});
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

        ps2 = new RC5ParameterSpec(version+1, rounds, wordSize, iv);
        assertFalse("Objects should not be equal.", ps1.equals(ps2));

        ps2 = new RC5ParameterSpec(version, rounds+1, wordSize, iv);
        assertFalse("Objects should not be equal.", ps1.equals(ps2));

        ps2 = new RC5ParameterSpec(version, rounds, wordSize/2, iv);
        assertFalse("Objects should not be equal.", ps1.equals(ps2));

        ps2 = new RC5ParameterSpec(version, rounds, wordSize,
                                                    new byte[] {4, 3, 2, 1});
        assertFalse("Objects should not be equal.", ps1.equals(ps2));
    }

    /**
     * hashCode() method testing. Tests that for equal objects hash codes
     * are equal.
     */
    public void testHashCode() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4, 5, 6};

        RC5ParameterSpec ps1 = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        RC5ParameterSpec ps2 = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        assertTrue("Equal objects should have the same hash codes.",
                                            ps1.hashCode() == ps2.hashCode());
    }

    public void test_constructorIII() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        RC5ParameterSpec ps1 = new RC5ParameterSpec(version, rounds, wordSize);
        RC5ParameterSpec ps2 = new RC5ParameterSpec(version, rounds, wordSize);
        RC5ParameterSpec ps3 = new RC5ParameterSpec(version, rounds, wordSize + 1);

        assertTrue(ps1.equals(ps2));
        assertFalse(ps1.equals(ps3));
    }

    public static Test suite() {
        return new TestSuite(RC5ParameterSpecTest.class);
    }
}

