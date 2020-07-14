/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.lang;

import java.math.BigInteger;
import java.util.Properties;

public class LongTest extends junit.framework.TestCase {

    public void testSystemProperties() {
        Properties originalProperties = System.getProperties();
        try {
            Properties testProperties = new Properties();
            testProperties.put("testIncLong", "string");
            System.setProperties(testProperties);
            assertNull(Long.getLong("testIncLong"));
            assertEquals(new Long(4), Long.getLong("testIncLong", 4L));
            assertEquals(new Long(4), Long.getLong("testIncLong", new Long(4)));
        } finally {
            System.setProperties(originalProperties);
        }
    }

    public void testCompare() throws Exception {
        final long min = Long.MIN_VALUE;
        final long zero = 0L;
        final long max = Long.MAX_VALUE;
        assertTrue(Long.compare(max,  max)  == 0);
        assertTrue(Long.compare(min,  min)  == 0);
        assertTrue(Long.compare(zero, zero) == 0);
        assertTrue(Long.compare(max,  zero) > 0);
        assertTrue(Long.compare(max,  min)  > 0);
        assertTrue(Long.compare(zero, max)  < 0);
        assertTrue(Long.compare(zero, min)  > 0);
        assertTrue(Long.compare(min,  zero) < 0);
        assertTrue(Long.compare(min,  max)  < 0);
    }

    public void testSignum() throws Exception {
        assertEquals(0, Long.signum(0));
        assertEquals(1, Long.signum(1));
        assertEquals(-1, Long.signum(-1));
        assertEquals(1, Long.signum(Long.MAX_VALUE));
        assertEquals(-1, Long.signum(Long.MIN_VALUE));
    }

    /*
    public void testParsePositiveLong() throws Exception {
        assertEquals(0, Long.parsePositiveLong("0", 10));
        assertEquals(473, Long.parsePositiveLong("473", 10));
        assertEquals(255, Long.parsePositiveLong("FF", 16));

        try {
            Long.parsePositiveLong("-1", 10);
            fail();
        } catch (NumberFormatException e) {}

        try {
            Long.parsePositiveLong("+1", 10);
            fail();
        } catch (NumberFormatException e) {}

        try {
            Long.parsePositiveLong("+0", 16);
            fail();
        } catch (NumberFormatException e) {}
    }
    */

    public void testParseLong() throws Exception {
        assertEquals(0, Long.parseLong("+0", 10));
        assertEquals(473, Long.parseLong("+473", 10));
        assertEquals(255, Long.parseLong("+FF", 16));
        assertEquals(102, Long.parseLong("+1100110", 2));
        assertEquals(Long.MAX_VALUE, Long.parseLong("+" + Long.MAX_VALUE, 10));
        assertEquals(411787, Long.parseLong("Kona", 27));
        assertEquals(411787, Long.parseLong("+Kona", 27));
        assertEquals(-145, Long.parseLong("-145", 10));

        try {
            Long.parseLong("--1", 10); // multiple sign chars
            fail();
        } catch (NumberFormatException expected) {}

        try {
            Long.parseLong("++1", 10); // multiple sign chars
            fail();
        } catch (NumberFormatException expected) {}

        try {
            Long.parseLong("Kona", 10); // base to small
            fail();
        } catch (NumberFormatException expected) {}
    }

    public void testDecodeLong() throws Exception {
        assertEquals(0, Long.decode("+0").longValue());
        assertEquals(473, Long.decode("+473").longValue());
        assertEquals(255, Long.decode("+0xFF").longValue());
        assertEquals(16, Long.decode("+020").longValue());
        assertEquals(Long.MAX_VALUE, Long.decode("+" + Long.MAX_VALUE).longValue());
        assertEquals(-73, Long.decode("-73").longValue());
        assertEquals(-255, Long.decode("-0xFF").longValue());
        assertEquals(255, Long.decode("+#FF").longValue());
        assertEquals(-255, Long.decode("-#FF").longValue());

        try {
            Long.decode("--1"); // multiple sign chars
            fail();
        } catch (NumberFormatException expected) {}

        try {
            Long.decode("++1"); // multiple sign chars
            fail();
        } catch (NumberFormatException expected) {}

        try {
            Long.decode("+-1"); // multiple sign chars
            fail();
        } catch (NumberFormatException expected) {}

        try {
            Long.decode("Kona"); // invalid number
            fail();
        } catch (NumberFormatException expected) {}
    }

    public void testStaticHashCode() {
        assertEquals(Long.valueOf(567L).hashCode(), Long.hashCode(567L));
    }

    public void testMax() {
        long a = 567L;
        long b = 578L;
        assertEquals(Math.max(a, b), Long.max(a, b));
    }

    public void testMin() {
        long a = 567L;
        long b = 578L;
        assertEquals(Math.min(a, b), Long.min(a, b));
    }

    public void testSum() {
        long a = 567L;
        long b = 578L;
        assertEquals(a + b, Long.sum(a, b));
    }

    public void testBYTES() {
        assertEquals(8, Long.BYTES);
    }

    public void testCompareUnsigned() {
        long[] ordVals = {0L, 1L, 23L, 456L, 0x7fff_ffff_ffff_ffffL, 0x8000_0000_0000_0000L,
                0xffff_ffff_ffff_ffffL};
        for(int i = 0; i < ordVals.length; ++i) {
            for(int j = 0; j < ordVals.length; ++j) {
                assertEquals(Integer.compare(i, j),
                        Long.compareUnsigned(ordVals[i], ordVals[j]));
            }
        }
    }

    public void testDivideAndRemainderUnsigned() {
        BigInteger[] vals = {
                BigInteger.ONE,
                BigInteger.valueOf(23L),
                BigInteger.valueOf(456L),
                BigInteger.valueOf(0x7fff_ffff_ffff_ffffL),
                BigInteger.valueOf(0x7fff_ffff_ffff_ffffL).add(BigInteger.ONE),
                BigInteger.valueOf(2).shiftLeft(63).subtract(BigInteger.ONE)
        };

        for(BigInteger dividend : vals) {
            for(BigInteger divisor : vals) {
                long uq = Long.divideUnsigned(dividend.longValue(), divisor.longValue());
                long ur = Long.remainderUnsigned(dividend.longValue(), divisor.longValue());
                assertEquals(dividend.divide(divisor).longValue(), uq);
                assertEquals(dividend.remainder(divisor).longValue(), ur);
                assertEquals(dividend.longValue(), uq * divisor.longValue() + ur);
            }
        }

        for(BigInteger dividend : vals) {
            try {
                Long.divideUnsigned(dividend.longValue(), 0);
                fail();
            } catch (ArithmeticException expected) { }
            try {
                Long.remainderUnsigned(dividend.longValue(), 0);
                fail();
            } catch (ArithmeticException expected) { }
        }
    }

    public void testParseUnsignedLong() {
        long[] vals = {0L, 1L, 23L, 456L, 0x7fff_ffff_ffff_ffffL, 0x8000_0000_0000_0000L,
                0xffff_ffff_ffff_ffffL};

        for(long val : vals) {
            // Special radices
            assertEquals(val, Long.parseUnsignedLong(Long.toBinaryString(val), 2));
            assertEquals(val, Long.parseUnsignedLong(Long.toOctalString(val), 8));
            assertEquals(val, Long.parseUnsignedLong(Long.toUnsignedString(val)));
            assertEquals(val, Long.parseUnsignedLong(Long.toHexString(val), 16));

            for(int radix = Character.MIN_RADIX; radix <= Character.MAX_RADIX; ++radix) {
                assertEquals(val,
                        Long.parseUnsignedLong(Long.toUnsignedString(val, radix), radix));
            }
        }

        try {
            Long.parseUnsignedLong("-1");
            fail();
        } catch (NumberFormatException expected) { }
        try {
            Long.parseUnsignedLong("123", 2);
            fail();
        } catch (NumberFormatException expected) { }
        try {
            Long.parseUnsignedLong(null, 2);
            fail();
        } catch (NumberFormatException expected) { }
        try {
            Long.parseUnsignedLong("0", Character.MAX_RADIX + 1);
            fail();
        } catch (NumberFormatException expected) { }
        try {
            Long.parseUnsignedLong("0", Character.MIN_RADIX - 1);
            fail();
        } catch (NumberFormatException expected) { }
    }

    public void testToUnsignedString() {
        long[] vals = {0L, 1L, 23L, 456L, 0x7fff_ffff_ffff_ffffL, 0x8000_0000_0000_0000L,
                0xffff_ffff_ffff_ffffL};

        for(long val : vals) {
            // Special radices
            assertTrue(Long.toUnsignedString(val, 2).equals(Long.toBinaryString(val)));
            assertTrue(Long.toUnsignedString(val, 8).equals(Long.toOctalString(val)));
            assertTrue(Long.toUnsignedString(val, 10).equals(Long.toUnsignedString(val)));
            assertTrue(Long.toUnsignedString(val, 16).equals(Long.toHexString(val)));

            for(int radix = Character.MIN_RADIX; radix <= Character.MAX_RADIX; ++radix) {
                int upper = (int) (val >>> 32), lower = (int) val;
                BigInteger b = (BigInteger.valueOf(Integer.toUnsignedLong(upper))).shiftLeft(32).
                        add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));

                assertTrue(Long.toUnsignedString(val, radix).equals(b.toString(radix)));
            }

            // Behavior is not defined by Java API specification if the radix falls outside of valid
            // range, thus we don't test for such cases.
        }
    }
}
