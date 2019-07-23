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
 * @author Elena Semukhina
 */

package org.apache.harmony.tests.java.math;

import junit.framework.TestCase;
import java.math.BigInteger;
import java.util.Random;

/**
 * Class:   java.math.BigInteger
 * Constructors: BigInteger(byte[] a), BigInteger(int sign, byte[] a), 
 *               BigInteger(String val, int radix)
 */
public class BigIntegerConstructorsTest extends TestCase {
    /**
     * Create a number from an array of bytes.
     * Verify an exception thrown if an array is zero bytes long
     */
    public void testConstructorBytesException() {
        byte aBytes[] = {};
        try {
            new BigInteger(aBytes);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    
    /**
     * Create a positive number from an array of bytes.
     * The number fits in an array of integers.
     */
    public void testConstructorBytesPositive1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from an array of bytes.
     * The number fits in an integer.
     */
    public void testConstructorBytesPositive2() {
        byte aBytes[] = {12, 56, 100};
        byte rBytes[] = {12, 56, 100};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from an array of bytes.
     * The number of bytes is 4.
     */
    public void testConstructorBytesPositive3() {
        byte aBytes[] = {127, 56, 100, -1};
        byte rBytes[] = {127, 56, 100, -1};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from an array of bytes.
     * The number of bytes is multiple of 4.
     */
    public void testConstructorBytesPositive() {
        byte aBytes[] = {127, 56, 100, -1, 14, 75, -24, -100};
        byte rBytes[] = {127, 56, 100, -1, 14, 75, -24, -100};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a negative number from an array of bytes.
     * The number fits in an array of integers.
     */
    public void testConstructorBytesNegative1() {
        byte aBytes[] = {-12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte rBytes[] = {-12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a negative number from an array of bytes.
     * The number fits in an integer.
     */
    public void testConstructorBytesNegative2() {
        byte aBytes[] = {-12, 56, 100};
        byte rBytes[] = {-12, 56, 100};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a negative number from an array of bytes.
     * The number of bytes is 4.
     */
    public void testConstructorBytesNegative3() {
        byte aBytes[] = {-128, -12, 56, 100};
        byte rBytes[] = {-128, -12, 56, 100};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }
    
    /**
     * Create a negative number from an array of bytes.
     * The number of bytes is multiple of 4.
     */
    public void testConstructorBytesNegative4() {
        byte aBytes[] = {-128, -12, 56, 100, -13, 56, 93, -78};
        byte rBytes[] = {-128, -12, 56, 100, -13, 56, 93, -78};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a zero number from an array of zero bytes.
     */
    public void testConstructorBytesZero() {
        byte aBytes[] = {0, 0, 0, -0, +0, 0, -0};
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }

    /**
     * Create a number from a sign and an array of bytes.
     * Verify an exception thrown if a sign has improper value.
     */
    public void testConstructorSignBytesException1() {
        byte aBytes[] = {123, 45, -3, -76};
        int aSign = 3;
        try {
            new BigInteger(aSign, aBytes);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    
    /**
     * Create a number from a sign and an array of bytes.
     * Verify an exception thrown if the array contains non-zero bytes while the sign is 0. 
     */
    public void testConstructorSignBytesException2() {
        byte aBytes[] = {123, 45, -3, -76};
        int aSign = 0;
        try {
            new BigInteger(aSign, aBytes);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
            assertEquals("Improper exception message", "signum-magnitude mismatch", e.getMessage());
        }
    }

    /**
     * Create a positive number from a sign and an array of bytes.
     * The number fits in an array of integers.
     * The most significant byte is positive.
     */
    public void testConstructorSignBytesPositive1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15};
        int aSign = 1;
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    
    /**
     * Create a positive number from a sign and an array of bytes.
     * The number fits in an array of integers.
     * The most significant byte is negative.
     */
    public void testConstructorSignBytesPositive2() {
        byte aBytes[] = {-12, 56, 100, -2, -76, 89, 45, 91, 3, -15};
        int aSign = 1;
        byte rBytes[] = {0, -12, 56, 100, -2, -76, 89, 45, 91, 3, -15};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from a sign and an array of bytes.
     * The number fits in an integer.
     */
    public void testConstructorSignBytesPositive3() {
        byte aBytes[] = {-12, 56, 100};
        int aSign = 1;
        byte rBytes[] = {0, -12, 56, 100};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from a sign and an array of bytes.
     * The number of bytes is 4.
     * The most significant byte is positive.
     */
    public void testConstructorSignBytesPositive4() {
        byte aBytes[] = {127, 56, 100, -2};
        int aSign = 1;
        byte rBytes[] = {127, 56, 100, -2};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from a sign and an array of bytes.
     * The number of bytes is 4.
     * The most significant byte is negative.
     */
    public void testConstructorSignBytesPositive5() {
        byte aBytes[] = {-127, 56, 100, -2};
        int aSign = 1;
        byte rBytes[] = {0, -127, 56, 100, -2};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    
    /**
     * Create a positive number from a sign and an array of bytes.
     * The number of bytes is multiple of 4.
     * The most significant byte is positive.
     */
    public void testConstructorSignBytesPositive6() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 23, -101};
        int aSign = 1;
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 23, -101};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from a sign and an array of bytes.
     * The number of bytes is multiple of 4.
     * The most significant byte is negative.
     */
    public void testConstructorSignBytesPositive7() {
        byte aBytes[] = {-12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 23, -101};
        int aSign = 1;
        byte rBytes[] = {0, -12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 23, -101};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    
    /**
     * Create a negative number from a sign and an array of bytes.
     * The number fits in an array of integers.
     * The most significant byte is positive.
     */
    public void testConstructorSignBytesNegative1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15};
        int aSign = -1;
        byte rBytes[] = {-13, -57, -101, 1, 75, -90, -46, -92, -4, 15};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }
    
    /**
     * Create a negative number from a sign and an array of bytes.
     * The number fits in an array of integers.
     * The most significant byte is negative.
     */
    public void testConstructorSignBytesNegative2() {
        byte aBytes[] = {-12, 56, 100, -2, -76, 89, 45, 91, 3, -15};
        int aSign = -1;
        byte rBytes[] = {-1, 11, -57, -101, 1, 75, -90, -46, -92, -4, 15};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a negative number from a sign and an array of bytes.
     * The number fits in an integer.
     */
    public void testConstructorSignBytesNegative3() {
        byte aBytes[] = {-12, 56, 100};
        int aSign = -1;
        byte rBytes[] = {-1, 11, -57, -100};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a negative number from a sign and an array of bytes.
     * The number of bytes is 4.
     * The most significant byte is positive.
     */
    public void testConstructorSignBytesNegative4() {
        byte aBytes[] = {127, 56, 100, -2};
        int aSign = -1;
        byte rBytes[] = {-128, -57, -101, 2};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a negative number from a sign and an array of bytes.
     * The number of bytes is 4.
     * The most significant byte is negative.
     */
    public void testConstructorSignBytesNegative5() {
        byte aBytes[] = {-127, 56, 100, -2};
        int aSign = -1;
        byte rBytes[] = {-1, 126, -57, -101, 2};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }
    
    /**
     * Create a negative number from a sign and an array of bytes.
     * The number of bytes is multiple of 4.
     * The most significant byte is positive.
     */
    public void testConstructorSignBytesNegative6() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 23, -101};
        int aSign = -1;
        byte rBytes[] = {-13, -57, -101, 1, 75, -90, -46, -92, -4, 14, -24, 101};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a negative number from a sign and an array of bytes.
     * The number of bytes is multiple of 4.
     * The most significant byte is negative.
     */
    public void testConstructorSignBytesNegative7() {
        byte aBytes[] = {-12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 23, -101};
        int aSign = -1;
        byte rBytes[] = {-1, 11, -57, -101, 1, 75, -90, -46, -92, -4, 14, -24, 101};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a zero number from a sign and an array of zero bytes.
     * The sign is -1.
     */
    public void testConstructorSignBytesZero1() {
        byte aBytes[] = {-0, 0, +0, 0, 0, 00, 000};
        int aSign = -1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }

    /**
     * Create a zero number from a sign and an array of zero bytes.
     * The sign is 0.
     */
    public void testConstructorSignBytesZero2() {
        byte aBytes[] = {-0, 0, +0, 0, 0, 00, 000};
        int aSign = 0;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }

    /**
     * Create a zero number from a sign and an array of zero bytes.
     * The sign is 1.
     */
    public void testConstructorSignBytesZero3() {
        byte aBytes[] = {-0, 0, +0, 0, 0, 00, 000};
        int aSign = 1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }

    /**
     * Create a zero number from a sign and an array of zero length.
     * The sign is -1.
     */
    public void testConstructorSignBytesZeroNull1() {
        byte aBytes[] = {};
        int aSign = -1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }

    /**
     * Create a zero number from a sign and an array of zero length.
     * The sign is 0.
     */
    public void testConstructorSignBytesZeroNull2() {
        byte aBytes[] = {};
        int aSign = 0;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }

    /**
     * Create a zero number from a sign and an array of zero length.
     * The sign is 1.
     */
    public void testConstructorSignBytesZeroNull3() {
        byte aBytes[] = {};
        int aSign = 1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }

    /**
     * Create a number from a string value and radix.
     * Verify an exception thrown if a radix is out of range
     */
    public void testConstructorStringException1() {
        String value = "9234853876401";
        int radix = 45;
        try {
            new BigInteger(value, radix);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    
    /**
     * Create a number from a string value and radix.
     * Verify an exception thrown if the string starts with a space.
     */
    public void testConstructorStringException2() {
        String value = "   9234853876401";
        int radix = 10;
        try {
            new BigInteger(value, radix);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    
    /**
     * Create a number from a string value and radix.
     * Verify an exception thrown if the string contains improper characters.
     */
    public void testConstructorStringException3() {
        String value = "92348$*#78987";
        int radix = 34;
        try {
            new BigInteger(value, radix);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }
    
    /**
     * Create a number from a string value and radix.
     * Verify an exception thrown if some digits are greater than radix.
     */
    public void testConstructorStringException4() {
        String value = "98zv765hdsaiy";
        int radix = 20;
        try {
            new BigInteger(value, radix);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * Create a positive number from a string value and radix 2.
     */
    public void testConstructorStringRadix2() {
        String value = "10101010101010101";
        int radix = 2;
        byte rBytes[] = {1, 85, 85};
        BigInteger aNumber = new BigInteger(value, radix);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    
    /**
     * Create a positive number from a string value and radix 8.
     */
    public void testConstructorStringRadix8() {
        String value = "76356237071623450";
        int radix = 8;
        byte rBytes[] = {7, -50, -28, -8, -25, 39, 40};
        BigInteger aNumber = new BigInteger(value, radix);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from a string value and radix 10.
     */
    public void testConstructorStringRadix10() {
        String value = "987328901348934898";
        int radix = 10;
        byte rBytes[] = {13, -77, -78, 103, -103, 97, 68, -14};
        BigInteger aNumber = new BigInteger(value, radix);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from a string value and radix 16.
     */
    public void testConstructorStringRadix16() {
        String value = "fe2340a8b5ce790";
        int radix = 16;
        byte rBytes[] = {15, -30, 52, 10, -117, 92, -25, -112};
        BigInteger aNumber = new BigInteger(value, radix);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a positive number from a string value and radix 36.
     */
    public void testConstructorStringRadix36() {
        String value = "skdjgocvhdjfkl20jndjkf347ejg457";
        int radix = 36;
        byte rBytes[] = {0, -12, -116, 112, -105, 12, -36, 66, 108, 66, -20, -37, -15, 108, -7, 52, -99, -109, -8, -45, -5};
        BigInteger aNumber = new BigInteger(value, radix);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }

    /**
     * Create a negative number from a string value and radix 10.
     */
    public void testConstructorStringRadix10Negative() {
        String value = "-234871376037";
        int radix = 36;
        byte rBytes[] = {-4, 48, 71, 62, -76, 93, -105, 13};
        BigInteger aNumber = new BigInteger(value, radix);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }

    /**
     * Create a zero number from a string value and radix 36.
     */
    public void testConstructorStringRadix10Zero() {
        String value = "-00000000000000";
        int radix = 10;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(value, radix);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }

    /**
     * Create a random number of 75 bits length.
     */
    public void testConstructorRandom() {
        int bitLen = 75;
        Random rnd = new Random();
        BigInteger aNumber = new BigInteger(bitLen, rnd);
        assertTrue("incorrect bitLength", aNumber.bitLength() <= bitLen);
    }

  public void testConstructorPrime() {
    for (int rep = 0; rep < 2048; ++rep) {
      Random rnd = new Random();
      BigInteger b;
      int bits;

      // Create a 128-bit prime number.
      bits = 128;
      b = new BigInteger(bits, 10, rnd);
      assertEquals(b.toString(), bits, b.bitLength());

      // Create a prime number of 25 bits length.
      bits = 25;
      b = new BigInteger(bits, 10, rnd);
      assertEquals(b.toString(), bits, b.bitLength());

      // Create a prime number of 18 bits length.
      bits = 18;
      b = new BigInteger(bits, 10, rnd);
      assertEquals(b.toString(), bits, b.bitLength());

      // On Android, anything less than 16 bits used to be at least 16 bits
      // because that's how OpenSSL behaves, but we recently fixed this...
      bits = 2;
      b = new BigInteger(bits, 10, rnd);
      assertEquals(b.toString(), bits, b.bitLength());

      // The 2-arg constructor has never used OpenSSL.
      bits = 2;
      b = new BigInteger(bits, rnd);
      assertTrue(b.toString(), b.bitLength() <= bits);
      assertTrue(b.toString(), b.intValue() <= 3);

      bits = 16;
      b = new BigInteger(bits, rnd);
      assertTrue(b.toString(), b.bitLength() <= bits);
    }
  }
}
