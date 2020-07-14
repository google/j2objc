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
import javax.crypto.spec.PSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class PSourceTest extends TestCase {

    /**
     * PSpecified(byte[] p) method testing. Tests that NullPointerException
     * is thrown in the case of null p array. Also it checks the value of
     * DEFAULT field, and that input p array is copied to protect against
     * subsequent modification.
     */
    public void testPSpecified() {
        try {
            new PSource.PSpecified(null);
            fail("NullPointerException should be thrown in the case of "
                    + "null p array.");
        } catch (NullPointerException e) {
        }

        assertEquals("The PSource.PSpecified DEFAULT value should be byte[0]",
                0, PSource.PSpecified.DEFAULT.getValue().length);

        byte[] p = new byte[] {1, 2, 3, 4, 5};
        PSource.PSpecified ps = new PSource.PSpecified(p);
        p[0]++;
        assertFalse("The change of p specified in the constructor "
                + "should not cause the change of internal array.", p[0] == ps
                .getValue()[0]);
    }

    /**
     * getValue() method testing. Tests that returned array is equal to the
     * array specified in the constructor. Checks that modification
     * of returned array does not affect the internal array.
     */
    public void testGetValue() {
        byte[] p = new byte[] {1, 2, 3, 4, 5};

        PSource.PSpecified ps = new PSource.PSpecified(p);
        byte[] result = ps.getValue();
        if (!Arrays.equals(p, result)) {
            fail("The returned array does not equal to the specified "
                    + "in the constructor.");
        }
        result[0]++;
        assertFalse("The change of returned by getValue() array "
                + "should not cause the change of internal array.",
                result[0] == ps.getValue()[0]);
    }


    /**
     * PSource(String pSrcName) method testing. Tests that returned value is
     * equal to the value specified in the constructor.
     */
    public void testPSource() {
        try {
            new PSource(null) {};
            fail("NullPointerException should be thrown in the case of "
                    + "null pSrcName.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * getAlgorithm() method testing. Tests that returned value is
     * equal to the value specified in the constructor.
     */
    public void testGetAlgorithm() {
        String pSrcName = "pSrcName";
        PSource ps = new PSource(pSrcName) {};
        assertTrue("The returned value is not equal to the value specified "
                + "in constructor", pSrcName.equals(ps.getAlgorithm()));
    }

    public static Test suite() {
        return new TestSuite(PSourceTest.class);
    }
}
