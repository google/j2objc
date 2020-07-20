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
import java.lang.IllegalArgumentException;
import java.lang.ArrayIndexOutOfBoundsException;

import javax.crypto.spec.IvParameterSpec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class IvParameterSpecTest extends TestCase {

    /**
     * IvParameterSpec(byte[] iv) constructor testing. Checks that
     * NullPointerException is thrown in the case of null input
     * array and that input array is copied during initialization.
     */
    public void testIvParameterSpec1() {
        try {
            new IvParameterSpec(null);
            fail("Should raise an NullPointerException "
                    + "in the case of null byte array.");
        } catch(NullPointerException e) {
        }

        byte[] iv = new byte[] {1, 2, 3, 4, 5};
        IvParameterSpec ivps = new IvParameterSpec(iv);
        iv[0] ++;
        assertFalse("The change of input array's content should not cause "
                    + "the change of internal array", iv[0] == ivps.getIV()[0]);
    }

    /**
     * IvParameterSpec(byte[] iv) constructor testing. Checks that
     * NullPointerException is thrown in the case of null input
     * array and that input array is copied during initialization.
     */
    public void testIvParameterSpec2() {
        try {
            new IvParameterSpec(null, 1, 1);
            fail("Should raise an IllegalArgumentException "
                    + "in the case of null byte array.");
        } catch(ArrayIndexOutOfBoundsException e) {
            fail("Unexpected ArrayIndexOutOfBoundsException was thrown");
        } catch(IllegalArgumentException e) {
        } catch(NullPointerException e) {
            fail("Unexpected NullPointerException was thrown");
        }

        try {
            new IvParameterSpec(new byte[] {1, 2, 3}, 2, 2);
            fail("Should raise an IllegalArgumentException "
                    + "if (iv.length - offset < len).");
        } catch(ArrayIndexOutOfBoundsException e) {
            fail("Unexpected ArrayIndexOutOfBoundsException was thrown");
        } catch(IllegalArgumentException e) {
        } catch(NullPointerException e) {
            fail("Unexpected NullPointerException was thrown");
        }

        try {
            new IvParameterSpec(new byte[] {1, 2, 3}, -1, 1);
            fail("Should raise an ArrayIndexOutOfBoundsException "
                    + "if offset index bytes outside the iv.");
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException was thrown");
        } catch(NullPointerException e) {
            fail("Unexpected NullPointerException was thrown");
        }

        /* TODO: DRL fail with java.lang.NegativeArraySizeException
        try {
            new IvParameterSpec(new byte[] {1, 2, 3}, 1, -2);
            fail("Should raise an ArrayIndexOutOfBoundsException "
                    + "if len index bytes outside the iv.");
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException was thrown");
        } catch(NullPointerException e) {
            fail("Unexpected NullPointerException was thrown");
        }
        */

        byte[] iv = new byte[] {1, 2, 3, 4, 5};
        IvParameterSpec ivps = new IvParameterSpec(iv, 0, iv.length);
        iv[0] ++;
        assertFalse("The change of input array's content should not cause "
                    + "the change of internal array", iv[0] == ivps.getIV()[0]);

        //Regression for HARMONY-1089
        try {
            new IvParameterSpec(new byte[2], 2,  -1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    public void testGetIV() {
        byte[] iv = new byte[] {1, 2, 3, 4, 5};
        IvParameterSpec ivps = new IvParameterSpec(iv);
        iv = ivps.getIV();
        iv[0] ++;
        assertFalse("The change of returned array should not cause "
                    + "the change of internal array", iv[0] == ivps.getIV()[0]);
    }

    public static Test suite() {
        return new TestSuite(IvParameterSpecTest.class);
    }
}

