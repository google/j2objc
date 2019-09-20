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

package tests.security.spec;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.spec.ECPoint;

/**
 * Tests for <code>ECPoint</code> class fields and methods.
 *
 */
public class ECPointTest extends TestCase {

    //
    // Tests
    //

    /**
     * Test #1 for <code>ECPoint(BigInteger, BigInteger)</code> constructor<br>
     * Assertion: creates <code>ECPoint</code> instance<br>
     * Test preconditions: valid parameters passed<br>
     * Expected: must pass without any exceptions
     */
    public final void testECPoint01() {
        new ECPoint(BigInteger.ZERO, BigInteger.ZERO);
        new ECPoint(BigInteger.valueOf(-23456L), BigInteger.valueOf(-23456L));
        new ECPoint(BigInteger.valueOf(123456L), BigInteger.valueOf(123456L));
        new ECPoint(BigInteger.valueOf(-56L), BigInteger.valueOf(234L));
        new ECPoint(BigInteger.valueOf(3456L), BigInteger.valueOf(-2344L));
    }

    /**
     * Test #2 for <code>ECPoint(BigInteger x, BigInteger y)</code> constructor<br>
     * Assertion: throws <code>NullPointerException</code> if <code>x</code>or
     * <code>y</code> is <code>null</code><br>
     * Test preconditions: pass <code>null</code> as mentioned parameters<br>
     * Expected: must throw <code>NullPointerException</code>
     */
    public final void testECPoint02() {
        // test case 1: x is null
        try {
            new ECPoint(null, BigInteger.ZERO);
            fail("#1: Expected NPE not thrown");
        } catch (NullPointerException ok) {
        }


        // test case 2: y is null
        try {
            new ECPoint(BigInteger.ZERO, null);
            fail("#2: Expected NPE not thrown");
        } catch (NullPointerException ok) {
        }


        // test case 3: both : x and y are null
        try {
            new ECPoint(null, null);
            fail("#3: Expected NPE not thrown");
        } catch (NullPointerException ok) {
        }
    }

    /**
     * Test #1 for <code>getAffineX()</code> method<br>
     * Assertion: returns affine <code>x</code> coordinate<br>
     * Test preconditions: <code>ECPoint</code> instance
     * created using valid parameters<br>
     * Expected: must return affine <code>x</code> coordinate
     * which is equal to the one passed to the constructor;
     * (both must refer the same object)
     */
    public final void testGetAffineX01() {
        BigInteger x = BigInteger.valueOf(-23456L);
        ECPoint p = new ECPoint(x, BigInteger.valueOf(23456L));
        BigInteger xRet = p.getAffineX();
        assertEquals(x, xRet);
        assertSame(x, xRet);
    }

    /**
     * Test #2 for <code>getAffineX()</code> method<br>
     * Assertion: returns <code>null</code> for <code>ECPoint.POINT_INFINITY</code><br>
     * Test preconditions: none<br>
     * Expected: must return <code>null</code> for
     * <code>ECPoint.POINT_INFINITY</code>
     */
    public final void testGetAffineX02() {
        assertNull(ECPoint.POINT_INFINITY.getAffineX());
    }

    /**
     * Test #1 for <code>getAffineY()</code> method<br>
     * Assertion: returns affine <code>y</code> coordinate<br>
     * Test preconditions: <code>ECPoint</code> instance
     * created using valid parameters<br>
     * Expected: must return affine <code>y</code> coordinate
     * which is equal to the one passed to the constructor;
     * (both must refer the same object)
     */
    public final void testGetAffineY01() {
        BigInteger y =  BigInteger.valueOf(23456L);
        ECPoint p = new ECPoint(BigInteger.valueOf(-23456L), y);
        BigInteger yRet = p.getAffineY();
        assertEquals(y, yRet);
        assertSame(y, yRet);
    }

    /**
     * Test #2 for <code>getAffineX()</code> method<br>
     * Assertion: returns <code>null</code> for <code>ECPoint.POINT_INFINITY</code><br>
     * Test preconditions: none<br>
     * Expected: must return <code>null</code> for
     * <code>ECPoint.POINT_INFINITY</code>
     */
    public final void testGetAffineY02() {
        assertNull(ECPoint.POINT_INFINITY.getAffineY());
    }

    /**
     * Test #1 for <code>equals(Object other)</code> method<br>
     * Assertion: return true if this and other objects are equal<br>
     * Test preconditions: see test comments<br>
     * Expected: all objects in this test must be equal
     */
    public final void testEqualsObject01() {
        // test case 1: must be equal to itself
        ECPoint p2=null, p1 =
            new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ONE);
        assertTrue(p1.equals(p1));

        // test case 2: equal objects
        p1 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ONE);
        p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.valueOf(1L));
        assertTrue(p1.equals(p2) && p2.equals(p1));

        // test case 3: equal POINT_INFINITY object(s)
        p1 = ECPoint.POINT_INFINITY;
        p2 = ECPoint.POINT_INFINITY;
        assertTrue(p1.equals(p2) && p2.equals(p1));
    }

    /**
     * Test #2 for <code>equals(Object other)</code> method<br>
     * Assertion: return false if this and other objects are not equal<br>
     * Test preconditions: see test comments<br>
     * Expected: all objects in this test must be not equal
     */
    public final void testEqualsObject02() {
        // test case 1: must be not equal to null
        ECPoint p2=null, p1 =
            new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ONE);
        assertFalse(p1.equals(p2));

        // test case 2: not equal objects - x
        p1 = new ECPoint(BigInteger.valueOf(-23457L), BigInteger.ONE);
        p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.valueOf(1L));
        assertFalse(p1.equals(p2) || p2.equals(p1));

        // test case 3: not equal objects - y
        p1 = new ECPoint(BigInteger.valueOf(-23457L), BigInteger.ONE);
        p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ZERO);
        assertFalse(p1.equals(p2) || p2.equals(p1));

        // test case 4: not equal - some point and POINT_INFINITY
        p1 = ECPoint.POINT_INFINITY;
        p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ZERO);
        assertFalse(p1.equals(p2) || p2.equals(p1));
    }

    /**
     * Test #1 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * repeatedly on the same object.
     */
    public final void testHashCode01() {
        ECPoint f = new ECPoint(BigInteger.valueOf(-23457L), BigInteger.ONE);
        int hc = f.hashCode();
        assertTrue(hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode());


        // the same for POINT_INFINITY
        hc = ECPoint.POINT_INFINITY.hashCode();
        assertTrue(hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode() &&
                   hc == ECPoint.POINT_INFINITY.hashCode());
    }

    /**
     * Test #2 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * on equal (according to the <code>equals(Object)</code> method) objects.
     */
    public final void testHashCode02() {
        ECPoint p1 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.ONE);
        ECPoint p2 = new ECPoint(BigInteger.valueOf(-23456L), BigInteger.valueOf(1L));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

}
