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

package tests.security.spec;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.EllipticCurve;
import java.util.Arrays;

/**
 * Tests for <code>EllipticCurve</code> class fields and methods.
 *
 */
public class EllipticCurveTest extends TestCase {

    /**
     * Test #1 for <code>EllipticCurve(ECField, BigInteger, BigInteger, byte[])</code>
     * constructor<br>
     * Assertion: creates instance of EllipticCurve<br>
     * Test preconditions: valid parameters passed<br>
     * Expected: must pass without any exceptions
     */
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray01() {
        // test case 1 parameters set
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        // perform test case 1
        new EllipticCurve(f, a, b, seed);

        // test case 2 parameters set
        ECFieldF2m f1 = new ECFieldF2m(5);
        a = BigInteger.ZERO;
        b = BigInteger.valueOf(23L);
        // perform test case 2
        new EllipticCurve(f1, a, b, seed);

        // test case 3 parameters set,
        // the seed parameter may be null
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.ONE;
        b = BigInteger.valueOf(19L);
        seed = null;
        // perform test case 3
        new EllipticCurve(f, a, b, seed);
    }

    /**
     * Test #2 for <code>EllipticCurve(ECField, BigInteger, BigInteger, byte[])</code>
     * constructor<br>
     * Assertion: throws <code>NullPointerException</code> if <code>field</code>,
     * <code>a</code> or <code>b</code> is <code>null</code><br>
     * Test preconditions: pass <code>null</code> as mentioned parameters<br>
     * Expected: must throw <code>NullPointerException</code>
     */
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray02() {
        // test case 1 parameters set
        ECFieldFp f = null;
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];

        // perform test case 1
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#1: Expected NPE not thrown");
        } catch (NullPointerException ok) {}

        // test case 2 parameters set,
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = null;
        b = BigInteger.valueOf(19L);
        seed = new byte[24];
        // perform test case 2
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#2: Expected NPE not thrown");
        } catch (NullPointerException ok) {}

        // test case 3 parameters set,
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.ONE;
        b = null;
        seed = new byte[24];
        // perform test case 2
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#3: Expected NPE not thrown");
        } catch (NullPointerException ok) {}
    }

    /**
     * Test #3 for <code>EllipticCurve(ECField, BigInteger, BigInteger, byte[])</code>
     * constructor<br>
     * Assertion: throws <code>IllegalArgumentException</code> if
     * <code>a</code> or <code>b</code> is not <code>null</code> and not in
     * the <code>field</code><br>
     * Test preconditions: pass <code>a</code>, <code>b</code> which are
     * not in the <code>field</code> of type <code>ECFieldFp</code><br>
     * Expected: must throw <code>IllegalArgumentException</code>
     */
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray03() {
        // test case 1 parameters set,
        // a is not in field
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.valueOf(24L);
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];

        // perform test case 1
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 1.1 parameters set,
        // b is not in field
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(1L);
        b = BigInteger.valueOf(23L);
        seed = new byte[24];
        // perform test case 1.1
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#1.1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 2 parameters set,
        // b is not in field
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(19L);
        b = BigInteger.valueOf(24L);
        seed = new byte[24];
        // perform test case 2
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#2: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 3 parameters set,
        // both a and b are not in field
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(25L);
        b = BigInteger.valueOf(240L);
        seed = new byte[24];
        // perform test case 3
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#3: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
    }

    /**
     * Test #4 for <code>EllipticCurve(ECField, BigInteger, BigInteger, byte[])</code>
     * constructor<br>
     * Assertion: throws <code>IllegalArgumentException</code> if
     * <code>a</code> or <code>b</code> is not <code>null</code> and not in
     * the <code>field</code><br>
     * Test preconditions: pass <code>a</code>, <code>b</code> which are
     * not in the <code>field</code> of type <code>ECFieldF2m</code><br>
     * Expected: must throw <code>IllegalArgumentException</code>
     */
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray04() {
        // test case 1 parameters set,
        // a is not in field
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(32L);
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];

        // perform test case 1
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 2 parameters set,
        // b is not in field
        f = new ECFieldF2m(5);
        a = BigInteger.valueOf(19L);
        b = BigInteger.valueOf(32L);
        seed = new byte[24];
        // perform test case 2
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#2: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 3 parameters set,
        // both a and b are not in field
        f = new ECFieldF2m(5);
        a = BigInteger.valueOf(32L);
        b = BigInteger.valueOf(43L);
        seed = new byte[24];
        // perform test case 3
        try {
            new EllipticCurve(f, a, b, seed);
            fail("#3: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
    }

    /**
     * Test #5 for <code>EllipticCurve(ECField, BigInteger, BigInteger, byte[])</code>
     * constructor<br>
     * Assertion: array <code>seed</code> is copied to prevent subsequent modification<br>
     * Test preconditions: pass <code>seed</code> to the ctor then modify it<br>
     * Expected: getSeed() must return unmodified array
     */
    public final void testEllipticCurveECFieldBigIntegerBigIntegerbyteArray05() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(0L);
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        byte[] seedCopy = seed.clone();
        EllipticCurve c = new EllipticCurve(f, a, b, seedCopy);
        // modify array passed
        seedCopy[0] = (byte) 1;
        // check that above modification did not changed
        // internal state of test object
        assertTrue(Arrays.equals(seed, c.getSeed()));
    }

    /**
     * Test #1 for <code>EllipticCurve(ECField, BigInteger, BigInteger)</code>
     * constructor<br>
     * Assertion: creates instance of EllipticCurve<br>
     * Test preconditions: valid parameters passed, field type is ECFieldFp<br>
     * Expected: must pass without any exceptions
     */
    public final void testEllipticCurveECFieldBigIntegerBigInteger01() {
        // test case 1 parameters set
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        // perform test case 1
        new EllipticCurve(f, a, b);

        // test case 2 parameters set
        ECFieldF2m f1 = new ECFieldF2m(5);
        a = BigInteger.ZERO;
        b = BigInteger.valueOf(23L);
        // perform test case 2
        new EllipticCurve(f1, a, b);

        // test case 3 parameters set,
        // the seed parameter may be null
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.ONE;
        b = BigInteger.valueOf(19L);
        // perform test case 3
        new EllipticCurve(f, a, b);
    }

    /**
     * Test #2 for <code>EllipticCurve(ECField, BigInteger, BigInteger)</code>
     * constructor<br>
     * Assertion: throws <code>NullPointerException</code> if <code>field</code>,
     * <code>a</code> or <code>b</code> is <code>null</code><br>
     * Test preconditions: pass <code>null</code> as mentioned parameters<br>
     * Expected: must throw <code>NullPointerException</code>
     */
    public final void testEllipticCurveECFieldBigIntegerBigInteger02() {
        // test case 1 parameters set
        ECFieldFp f = null;
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);

        // perform test case 1
        try {
            new EllipticCurve(f, a, b);
            fail("#1: Expected NPE not thrown");
        } catch (NullPointerException ok) {}

        // test case 2 parameters set,
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = null;
        b = BigInteger.valueOf(19L);
        // perform test case 2
        try {
            new EllipticCurve(f, a, b);
            fail("#2: Expected NPE not thrown");
        } catch (NullPointerException ok) {}

        // test case 3 parameters set,
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.ONE;
        b = null;
        // perform test case 3
        try {
            new EllipticCurve(f, a, b);
            fail("#3: Expected NPE not thrown");
        } catch (NullPointerException ok) {}
    }

    /**
     * Test #3 for <code>EllipticCurve(ECField, BigInteger, BigInteger)</code>
     * constructor<br>
     * Assertion: throws <code>IllegalArgumentException</code> if
     * <code>a</code> or <code>b</code> is not <code>null</code> and not in
     * the <code>field</code><br>
     * Test preconditions: pass <code>a</code>, <code>b</code> which are
     * not in the <code>field</code> of type <code>ECFieldFp</code><br>
     * Expected: must throw <code>IllegalArgumentException</code>
     */
    public final void testEllipticCurveECFieldBigIntegerBigInteger03() {
        // test case 1 parameters set,
        // a is not in field
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.valueOf(24L);
        BigInteger b = BigInteger.valueOf(19L);

        // perform test case 1
        try {
            new EllipticCurve(f, a, b);
            fail("#1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 1.1 parameters set,
        // a is not in field
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(23L);
        b = BigInteger.valueOf(19L);
        // perform test case 1.1
        try {
            new EllipticCurve(f, a, b);
            fail("#1.1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 2 parameters set,
        // b is not in field
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(19L);
        b = BigInteger.valueOf(24L);
        // perform test case 2
        try {
            new EllipticCurve(f, a, b);
            fail("#2: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 3 parameters set,
        // both a and b are not in field
        f = new ECFieldFp(BigInteger.valueOf(23L));
        a = BigInteger.valueOf(25L);
        b = BigInteger.valueOf(240L);
        // perform test case 3
        try {
            new EllipticCurve(f, a, b);
            fail("#3: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
    }

    /**
     * Test #4 for <code>EllipticCurve(ECField, BigInteger, BigInteger, byte[])</code>
     * constructor<br>
     * Assertion: throws <code>IllegalArgumentException</code> if
     * <code>a</code> or <code>b</code> is not <code>null</code> and not in
     * the <code>field</code><br>
     * Test preconditions: pass <code>a</code>, <code>b</code> which are
     * not in the <code>field</code> of type <code>ECFieldF2m</code><br>
     * Expected: must throw <code>IllegalArgumentException</code>
     */
    public final void testEllipticCurveECFieldBigIntegerBigInteger04() {
        // test case 1 parameters set,
        // a is not in field
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(32L);
        BigInteger b = BigInteger.valueOf(19L);
        // perform test case 1
        try {
            new EllipticCurve(f, a, b);
            fail("#1: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 2 parameters set,
        // b is not in field
        f = new ECFieldF2m(5);
        a = BigInteger.valueOf(19L);
        b = BigInteger.valueOf(32L);
        // perform test case 2
        try {
            new EllipticCurve(f, a, b);
            fail("#2: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}

        // test case 3 parameters set,
        // both a and b are not in field
        f = new ECFieldF2m(5);
        a = BigInteger.valueOf(32L);
        b = BigInteger.valueOf(43L);
        // perform test case 3
        try {
            new EllipticCurve(f, a, b);
            fail("#3: Expected IAE not thrown");
        } catch (IllegalArgumentException ok) {}
    }

    /**
     * Test for <code>getA()</code> method<br>
     * Assertion: returns coefficient <code>a</code><br>
     * Test preconditions: <code>ECFieldF2m</code> instance
     * created using valid parameters<br>
     * Expected: must return coefficient <code>a</code> which is equal
     * to the one passed to the constructor; (both must refer
     * the same object)
     */
    public final void testGetA() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(5L);
        BigInteger b = BigInteger.valueOf(19L);
        EllipticCurve c = new EllipticCurve(f, a, b);
        assertEquals(a, c.getA());
        assertSame(a, c.getA());
    }

    /**
     * java/security/spec/EllipticCurve#EllipticCurve(EcField,BigInteger,BigInteger)
     */
    public final void testEllipticCurveECFieldBigIntegerBigInteger05() {
        // Regression for Harmony-731
        EllipticCurve ec = new EllipticCurve(new testECField(), BigInteger
                .valueOf(4L), BigInteger.ONE);
        assertEquals("incorrect a", ec.getA(), BigInteger.valueOf(4L));
        assertEquals("incorrect b", ec.getB(), BigInteger.ONE);
        assertEquals("incorrect size", ec.getField().getFieldSize(), 2);
    }

    /**
     * Test for <code>getB()</code> method<br>
     * Assertion: returns coefficient <code>b</code><br>
     * Test preconditions: <code>ECFieldF2m</code> instance
     * created using valid parameters<br>
     * Expected: must return coefficient <code>b</code> which is equal
     * to the one passed to the constructor; (both must refer
     * the same object)
     */
    public final void testGetB() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(5L);
        BigInteger b = BigInteger.valueOf(19L);
        EllipticCurve c = new EllipticCurve(f, a, b);
        assertEquals(b, c.getB());
        assertSame(b, c.getB());
    }

    /**
     * Test for <code>getField()</code> method<br>
     * Assertion: returns <code>field</code><br>
     * Test preconditions: <code>ECFieldF2m</code> instance
     * created using valid parameters<br>
     * Expected: must return <code>field</code> which is equal
     * to the one passed to the constructor; (both must refer
     * the same object)
     */
    public final void testGetField() {
        ECFieldF2m f = new ECFieldF2m(5);
        BigInteger a = BigInteger.valueOf(5L);
        BigInteger b = BigInteger.valueOf(19L);
        EllipticCurve c = new EllipticCurve(f, a, b);
        assertEquals(f, c.getField());
        assertSame(f, c.getField());
    }

    /**
     * Test #1 for <code>getSeed()</code> method<br>
     * Assertion: returns <code>seed</code><br>
     * Test preconditions: <code>ECFieldF2m</code> instance
     * created using valid parameters<br>
     * Expected: must return <code>seed</code> which is equal
     * to the one passed to the constructor
     */
    public final void testGetSeed01() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        EllipticCurve c = new EllipticCurve(f, a, b, seed);
        byte[] seedRet = c.getSeed();
        assertNotNull(seedRet);
        assertTrue(Arrays.equals(seed, seedRet));
    }

    /**
     * Test #2 for <code>getSeed()</code> method<br>
     * Assertion: returned array is copied to prevent subsequent modification<br>
     * Test preconditions: <code>ECFieldF2m</code> instance
     * created using valid parameters; <code>getSeed()</code>
     * called and then returned array modified<br>
     * Expected: internal state must not be affected by the modification
     */
    public final void testGetSeed02() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        EllipticCurve c = new EllipticCurve(f, a, b, seed.clone());
        byte[] seedRet = c.getSeed();
        // modify returned array
        seedRet[0] = (byte) 1;
        // check that above modification did not changed
        // internal state of test object
        assertTrue(Arrays.equals(seed, c.getSeed()));
    }

    /**
     * Test #3 for <code>getSeed()</code> method<br>
     * Assertion: returned array is copied to prevent subsequent modification<br>
     * Test preconditions: <code>ECFieldF2m</code> instance
     * created using valid parameters<br>
     * Expected: repeated method calls must return different refs
     */
    public final void testGetSeed03() {
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.valueOf(19L);
        byte[] seed = new byte[24];
        EllipticCurve c = new EllipticCurve(f, a, b, seed);
        c.getSeed();
        assertNotSame(c.getSeed(), c.getSeed());
    }

    /**
     * java.security.spec.EllipticCurve#getSeed()
     * Assertion: null if not specified
     */
    public final void testGetSeed04() {
        //Regression for HARMONY-732
        ECFieldFp f = new ECFieldFp(BigInteger.valueOf(23L));
        BigInteger a = BigInteger.ONE;
        assertNull(new EllipticCurve(f, a, a).getSeed());
    }

    /**
     * Test #1 for <code>equals(Object other)</code> method<br>
     * Assertion: return true if this and other objects are equal<br>
     * Test preconditions: see test comments<br>
     * Expected: all objects in this test must be equal
     */
    public final void testEqualsObject01() {
        // test case 1: must be equal to itself
        EllipticCurve c2 = null, c1 = new EllipticCurve(new ECFieldFp(
                BigInteger.valueOf(23L)), BigInteger.ONE, BigInteger
                .valueOf(19L));
        assertTrue(c1.equals(c1));

        // test case 2: equal objects
        c1 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L));
        c2 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.valueOf(1L), BigInteger.valueOf(19L));
        assertTrue(c1.equals(c2) && c2.equals(c1));

        // test case 3: equal objects with seed not null
        c1 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L), new byte[24]);
        c2 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.valueOf(1L), BigInteger.valueOf(19L), new byte[24]);
        assertTrue(c1.equals(c2) && c2.equals(c1));

        // test case 4: equal object and subclass object
        c1 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L), new byte[24]);
        MyEllipticCurve c3 = new MyEllipticCurve(new ECFieldFp(BigInteger
                .valueOf(23L)), BigInteger.ONE, BigInteger.valueOf(19L),
                new byte[24]);
        assertTrue(c1.equals(c3) && c3.equals(c1));

        // test case 5: equal objects
        c1 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L));
        c2 = new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.valueOf(1L), BigInteger.valueOf(19L), null);
        assertTrue(c1.equals(c2) && c2.equals(c1));
    }

    /**
     * Test #1 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * repeatedly on the same object.
     */
    public final void testHashCode01() {
        int hc = 0;
        EllipticCurve f = new EllipticCurve(new ECFieldFp(BigInteger
                .valueOf(23L)), BigInteger.ONE, BigInteger.valueOf(19L),
                new byte[24]);
        hc = f.hashCode();
        assertTrue(hc == f.hashCode() && hc == f.hashCode()
                && hc == f.hashCode() && hc == f.hashCode()
                && hc == f.hashCode() && hc == f.hashCode()
                && hc == f.hashCode() && hc == f.hashCode());
    }

    /**
     * Test #2 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * on equal (according to the <code>equals(Object)</code> method) objects.
     */
    public final void testHashCode02() {
        assertEquals(new EllipticCurve(new ECFieldFp(BigInteger.valueOf(23L)),
                BigInteger.ONE, BigInteger.valueOf(19L), new byte[24])
                .hashCode(), new EllipticCurve(new ECFieldFp(BigInteger
                .valueOf(23L)), BigInteger.ONE, BigInteger.valueOf(19L),
                new byte[24]).hashCode());
    }

    //
    // Private stuff
    //

    class testECField implements ECField {

        public int getFieldSize() {
            return 2;
        }
    }

    /**
     * EllipticCurve subclass for testing purposes
     *
     */
    private static class MyEllipticCurve extends EllipticCurve {

        MyEllipticCurve(ECField f, BigInteger a, BigInteger b, byte[] seed) {
            super(f, a, b, seed);
        }
    }
}
