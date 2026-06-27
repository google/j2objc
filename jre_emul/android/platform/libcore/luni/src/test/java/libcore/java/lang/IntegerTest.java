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

import java.util.Properties;

public class IntegerTest extends junit.framework.TestCase {

  public void testSystemProperties() {
    Properties originalProperties = System.getProperties();
    try {
      Properties testProperties = new Properties();
      testProperties.put("testIncInt", "notInt");
      System.setProperties(testProperties);
      assertNull("returned incorrect default Integer",
        Integer.getInteger("testIncInt"));
      assertEquals(new Integer(4), Integer.getInteger("testIncInt", 4));
      assertEquals(new Integer(4),
        Integer.getInteger("testIncInt", new Integer(4)));
    } finally {
      System.setProperties(originalProperties);
    }
  }

  public void testCompare() throws Exception {
    final int min = Integer.MIN_VALUE;
    final int zero = 0;
    final int max = Integer.MAX_VALUE;
    assertTrue(Integer.compare(max,  max)  == 0);
    assertTrue(Integer.compare(min,  min)  == 0);
    assertTrue(Integer.compare(zero, zero) == 0);
    assertTrue(Integer.compare(max,  zero) > 0);
    assertTrue(Integer.compare(max,  min)  > 0);
    assertTrue(Integer.compare(zero, max)  < 0);
    assertTrue(Integer.compare(zero, min)  > 0);
    assertTrue(Integer.compare(min,  zero) < 0);
    assertTrue(Integer.compare(min,  max)  < 0);
  }

  public void testParseInt() throws Exception {
    assertEquals(0, Integer.parseInt("+0", 10));
    assertEquals(473, Integer.parseInt("+473", 10));
    assertEquals(255, Integer.parseInt("+FF", 16));
    assertEquals(102, Integer.parseInt("+1100110", 2));
    assertEquals(2147483647, Integer.parseInt("+2147483647", 10));
    assertEquals(411787, Integer.parseInt("Kona", 27));
    assertEquals(411787, Integer.parseInt("+Kona", 27));
    assertEquals(-145, Integer.parseInt("-145", 10));

    try {
      Integer.parseInt("--1", 10); // multiple sign chars
      fail();
    } catch (NumberFormatException expected) {}

    try {
      Integer.parseInt("++1", 10); // multiple sign chars
      fail();
    } catch (NumberFormatException expected) {}

    try {
      Integer.parseInt("Kona", 10); // base too small
      fail();
    } catch (NumberFormatException expected) {}
  }

  public void testDecodeInt() throws Exception {
    assertEquals(0, Integer.decode("+0").intValue());
    assertEquals(473, Integer.decode("+473").intValue());
    assertEquals(255, Integer.decode("+0xFF").intValue());
    assertEquals(16, Integer.decode("+020").intValue());
    assertEquals(2147483647, Integer.decode("+2147483647").intValue());
    assertEquals(-73, Integer.decode("-73").intValue());
    assertEquals(-255, Integer.decode("-0xFF").intValue());
    assertEquals(255, Integer.decode("+#FF").intValue());
    assertEquals(-255, Integer.decode("-#FF").intValue());

    try {
      Integer.decode("--1"); // multiple sign chars
      fail();
    } catch (NumberFormatException expected) {}

    try {
      Integer.decode("++1"); // multiple sign chars
      fail();
    } catch (NumberFormatException expected) {}

    try {
      Integer.decode("-+1"); // multiple sign chars
      fail();
    } catch (NumberFormatException expected) {}

    try {
      Integer.decode("Kona"); // invalid number
      fail();
    } catch (NumberFormatException expected) {}
  }

  /*
  public void testParsePositiveInt() throws Exception {
    assertEquals(0, Integer.parsePositiveInt("0", 10));
    assertEquals(473, Integer.parsePositiveInt("473", 10));
    assertEquals(255, Integer.parsePositiveInt("FF", 16));

    try {
      Integer.parsePositiveInt("-1", 10);
      fail();
    } catch (NumberFormatException e) {}

    try {
      Integer.parsePositiveInt("+1", 10);
      fail();
    } catch (NumberFormatException e) {}

    try {
      Integer.parsePositiveInt("+0", 16);
      fail();
    } catch (NumberFormatException e) {}
  }
  */

    public void testStaticHashCode() {
        assertEquals(Integer.valueOf(567).hashCode(), Integer.hashCode(567));
    }

    public void testMax() {
        int a = 567;
        int b = 578;
        assertEquals(Math.max(a, b), Integer.max(a, b));
    }

    public void testMin() {
        int a = 567;
        int b = 578;
        assertEquals(Math.min(a, b), Integer.min(a, b));
    }

    public void testSum() {
        int a = 567;
        int b = 578;
        assertEquals(a + b, Integer.sum(a, b));
    }

    public void testBYTES() {
      assertEquals(4, Integer.BYTES);
    }

    public void testCompareUnsigned() {
        int[] ordVals = {0, 1, 23, 456, 0x7fff_ffff, 0x8000_0000, 0xffff_ffff};
        for(int i = 0; i < ordVals.length; ++i) {
            for(int j = 0; j < ordVals.length; ++j) {
                assertEquals(Integer.compare(i, j),
                             Integer.compareUnsigned(ordVals[i], ordVals[j]));
            }
        }
    }

    public void testDivideAndRemainderUnsigned() {
        long[] vals = {1L, 23L, 456L, 0x7fff_ffffL, 0x8000_0000L, 0xffff_ffffL};

        for(long dividend : vals) {
            for(long divisor : vals) {
                int uq = Integer.divideUnsigned((int) dividend, (int) divisor);
                int ur = Integer.remainderUnsigned((int) dividend, (int) divisor);
                assertEquals((int) (dividend / divisor), uq);
                assertEquals((int) (dividend % divisor), ur);
                assertEquals((int) dividend, uq * (int) divisor + ur);
            }
        }

        for(long dividend : vals) {
            try {
                Integer.divideUnsigned((int) dividend, 0);
                fail();
            } catch (ArithmeticException expected) { }
            try {
                Integer.remainderUnsigned((int) dividend, 0);
                fail();
            } catch (ArithmeticException expected) { }
        }
    }

    public void testParseUnsignedInt() {
        int[] vals = {0, 1, 23, 456, 0x7fff_ffff, 0x8000_0000, 0xffff_ffff};

        for(int val : vals) {
            // Special radices
            assertEquals(val, Integer.parseUnsignedInt(Integer.toBinaryString(val), 2));
            assertEquals(val, Integer.parseUnsignedInt(Integer.toOctalString(val), 8));
            assertEquals(val, Integer.parseUnsignedInt(Integer.toUnsignedString(val)));
            assertEquals(val, Integer.parseUnsignedInt(Integer.toHexString(val), 16));

            for(int radix = Character.MIN_RADIX; radix <= Character.MAX_RADIX; ++radix) {
                assertEquals(val,
                        Integer.parseUnsignedInt(Integer.toUnsignedString(val, radix), radix));
            }
        }

        try {
            Integer.parseUnsignedInt("-1");
            fail();
        } catch (NumberFormatException expected) { }
        try {
            Integer.parseUnsignedInt("123", 2);
            fail();
        } catch (NumberFormatException expected) { }
        try {
            Integer.parseUnsignedInt(null);
            fail();
        } catch (NumberFormatException expected) { }
        try {
            Integer.parseUnsignedInt("0", Character.MAX_RADIX + 1);
            fail();
        } catch (NumberFormatException expected) { }
        try {
            Integer.parseUnsignedInt("0", Character.MIN_RADIX - 1);
            fail();
        } catch (NumberFormatException expected) { }
    }

    public void testToUnsignedLong() {
        int[] vals = {0, 1, 23, 456, 0x7fff_ffff, 0x8000_0000, 0xffff_ffff};

        for(int val : vals) {
            long ul = Integer.toUnsignedLong(val);
            assertEquals(0, ul >>> Integer.BYTES * 8);
            assertEquals(val, (int) ul);
        }
    }

    public void testToUnsignedString() {
        int[] vals = {0, 1, 23, 456, 0x7fff_ffff, 0x8000_0000, 0xffff_ffff};

        for(int val : vals) {
            // Special radices
            assertTrue(Integer.toUnsignedString(val, 2).equals(Integer.toBinaryString(val)));
            assertTrue(Integer.toUnsignedString(val, 8).equals(Integer.toOctalString(val)));
            assertTrue(Integer.toUnsignedString(val, 10).equals(Integer.toUnsignedString(val)));
            assertTrue(Integer.toUnsignedString(val, 16).equals(Integer.toHexString(val)));

            for(int radix = Character.MIN_RADIX; radix <= Character.MAX_RADIX; ++radix) {
                assertTrue(Integer.toUnsignedString(val, radix)
                        .equals(Long.toString(Integer.toUnsignedLong(val), radix)));
            }

            // Behavior is not defined by Java API specification if the radix falls outside of valid
            // range, thus we don't test for such cases.
        }
    }

    public void testIntegerOverflowWraparound() {
        int maxPlusOne = Integer.MAX_VALUE + 1;
        assertEquals(Integer.MIN_VALUE, maxPlusOne);

        int minMinusOne = Integer.MIN_VALUE - 1;
        assertEquals(Integer.MAX_VALUE, minMinusOne);

        int doubleMin = Integer.MIN_VALUE + Integer.MIN_VALUE;
        assertEquals(0, doubleMin);

        int subMin = Integer.MIN_VALUE - Integer.MIN_VALUE;
        assertEquals(0, subMin);
    }

    public void testIntegerMultiplicationOverflowWraparound() {
        int maxTimesTwo = Integer.MAX_VALUE * 2;
        assertEquals(-2, maxTimesTwo);

        int maxTimesMax = Integer.MAX_VALUE * Integer.MAX_VALUE;
        assertEquals(1, maxTimesMax);

        int minTimesTwo = Integer.MIN_VALUE * 2;
        assertEquals(0, minTimesTwo);

        int minTimesMinusOne = Integer.MIN_VALUE * -1;
        assertEquals(Integer.MIN_VALUE, minTimesMinusOne);

        int minTimesMin = Integer.MIN_VALUE * Integer.MIN_VALUE;
        assertEquals(0, minTimesMin);
    }

    public void testCompoundAssignmentOverflowWraparound() {
        int maxPlusOne = Integer.MAX_VALUE;
        maxPlusOne += 1;
        assertEquals(Integer.MIN_VALUE, maxPlusOne);

        int minMinusOne = Integer.MIN_VALUE;
        minMinusOne -= 1;
        assertEquals(Integer.MAX_VALUE, minMinusOne);

        int maxTimesTwo = Integer.MAX_VALUE;
        maxTimesTwo *= 2;
        assertEquals(-2, maxTimesTwo);

        int maxTimesMax = Integer.MAX_VALUE;
        maxTimesMax *= Integer.MAX_VALUE;
        assertEquals(1, maxTimesMax);

        int minTimesTwo = Integer.MIN_VALUE;
        minTimesTwo *= 2;
        assertEquals(0, minTimesTwo);

        int minTimesMinusOne = Integer.MIN_VALUE;
        minTimesMinusOne *= -1;
        assertEquals(Integer.MIN_VALUE, minTimesMinusOne);

        int minTimesMin = Integer.MIN_VALUE;
        minTimesMin *= Integer.MIN_VALUE;
        assertEquals(0, minTimesMin);

        long lMaxPlusOne = Long.MAX_VALUE;
        lMaxPlusOne += 1L;
        assertEquals(Long.MIN_VALUE, lMaxPlusOne);

        long lMinMinusOne = Long.MIN_VALUE;
        lMinMinusOne -= 1L;
        assertEquals(Long.MAX_VALUE, lMinMinusOne);

        long lMaxTimesTwo = Long.MAX_VALUE;
        lMaxTimesTwo *= 2L;
        assertEquals(-2L, lMaxTimesTwo);

        short sMaxPlusOne = Short.MAX_VALUE;
        sMaxPlusOne += (short) 1;
        assertEquals(Short.MIN_VALUE, sMaxPlusOne);

        short sMinMinusOne = Short.MIN_VALUE;
        sMinMinusOne -= (short) 1;
        assertEquals(Short.MAX_VALUE, sMinMinusOne);

        short sMaxTimesTwo = Short.MAX_VALUE;
        sMaxTimesTwo *= (short) 2;
        assertEquals((short) -2, sMaxTimesTwo);

        byte bMaxPlusOne = Byte.MAX_VALUE;
        bMaxPlusOne += (byte) 1;
        assertEquals(Byte.MIN_VALUE, bMaxPlusOne);

        byte bMinMinusOne = Byte.MIN_VALUE;
        bMinMinusOne -= (byte) 1;
        assertEquals(Byte.MAX_VALUE, bMinMinusOne);

        byte bMaxTimesTwo = Byte.MAX_VALUE;
        bMaxTimesTwo *= (byte) 2;
        assertEquals((byte) -2, bMaxTimesTwo);

        char cMaxPlusOne = Character.MAX_VALUE;
        cMaxPlusOne += (char) 1;
        assertEquals(Character.MIN_VALUE, cMaxPlusOne);

        char cMinMinusOne = Character.MIN_VALUE;
        cMinMinusOne -= (char) 1;
        assertEquals(Character.MAX_VALUE, cMinMinusOne);

        char cMaxTimesTwo = Character.MAX_VALUE;
        cMaxTimesTwo *= (char) 2;
        assertEquals((char) -2, cMaxTimesTwo);
    }

    volatile int vVal;
    volatile long vlVal;
    volatile short vsVal;
    volatile byte vbVal;
    volatile char vcVal;

    public void testVolatileCompoundAssignmentOverflowWraparound() {
        vVal = Integer.MAX_VALUE;
        vVal += 1;
        assertEquals(Integer.MIN_VALUE, vVal);

        vVal = Integer.MIN_VALUE;
        vVal -= 1;
        assertEquals(Integer.MAX_VALUE, vVal);

        vVal = Integer.MAX_VALUE;
        vVal *= 2;
        assertEquals(-2, vVal);

        vVal = Integer.MAX_VALUE;
        vVal *= Integer.MAX_VALUE;
        assertEquals(1, vVal);

        vVal = Integer.MIN_VALUE;
        vVal *= 2;
        assertEquals(0, vVal);

        vVal = Integer.MIN_VALUE;
        vVal *= -1;
        assertEquals(Integer.MIN_VALUE, vVal);

        vVal = Integer.MIN_VALUE;
        vVal *= Integer.MIN_VALUE;
        assertEquals(0, vVal);

        vlVal = Long.MAX_VALUE;
        vlVal += 1L;
        assertEquals(Long.MIN_VALUE, vlVal);

        vlVal = Long.MIN_VALUE;
        vlVal -= 1L;
        assertEquals(Long.MAX_VALUE, vlVal);

        vlVal = Long.MAX_VALUE;
        vlVal *= 2L;
        assertEquals(-2L, vlVal);

        vsVal = Short.MAX_VALUE;
        vsVal += (short) 1;
        assertEquals(Short.MIN_VALUE, vsVal);

        vsVal = Short.MIN_VALUE;
        vsVal -= (short) 1;
        assertEquals(Short.MAX_VALUE, vsVal);

        vsVal = Short.MAX_VALUE;
        vsVal *= (short) 2;
        assertEquals((short) -2, vsVal);

        vbVal = Byte.MAX_VALUE;
        vbVal += (byte) 1;
        assertEquals(Byte.MIN_VALUE, vbVal);

        vbVal = Byte.MIN_VALUE;
        vbVal -= (byte) 1;
        assertEquals(Byte.MAX_VALUE, vbVal);

        vbVal = Byte.MAX_VALUE;
        vbVal *= (byte) 2;
        assertEquals((byte) -2, vbVal);

        vcVal = Character.MAX_VALUE;
        vcVal += (char) 1;
        assertEquals(Character.MIN_VALUE, vcVal);

        vcVal = Character.MIN_VALUE;
        vcVal -= (char) 1;
        assertEquals(Character.MAX_VALUE, vcVal);

        vcVal = Character.MAX_VALUE;
        vcVal *= (char) 2;
        assertEquals((char) -2, vcVal);
    }

    public void testIncrementDecrementOverflowWraparound() {
        int iMaxPlusOne = Integer.MAX_VALUE;
        iMaxPlusOne++;
        assertEquals(Integer.MIN_VALUE, iMaxPlusOne);

        int iMaxPlusOnePre = Integer.MAX_VALUE;
        ++iMaxPlusOnePre;
        assertEquals(Integer.MIN_VALUE, iMaxPlusOnePre);

        int iMinMinusOne = Integer.MIN_VALUE;
        iMinMinusOne--;
        assertEquals(Integer.MAX_VALUE, iMinMinusOne);

        int iMinMinusOnePre = Integer.MIN_VALUE;
        --iMinMinusOnePre;
        assertEquals(Integer.MAX_VALUE, iMinMinusOnePre);

        long lMaxPlusOne = Long.MAX_VALUE;
        lMaxPlusOne++;
        assertEquals(Long.MIN_VALUE, lMaxPlusOne);

        long lMaxPlusOnePre = Long.MAX_VALUE;
        ++lMaxPlusOnePre;
        assertEquals(Long.MIN_VALUE, lMaxPlusOnePre);

        long lMinMinusOne = Long.MIN_VALUE;
        lMinMinusOne--;
        assertEquals(Long.MAX_VALUE, lMinMinusOne);

        long lMinMinusOnePre = Long.MIN_VALUE;
        --lMinMinusOnePre;
        assertEquals(Long.MAX_VALUE, lMinMinusOnePre);

        short sMaxPlusOne = Short.MAX_VALUE;
        sMaxPlusOne++;
        assertEquals(Short.MIN_VALUE, sMaxPlusOne);

        short sMaxPlusOnePre = Short.MAX_VALUE;
        ++sMaxPlusOnePre;
        assertEquals(Short.MIN_VALUE, sMaxPlusOnePre);

        short sMinMinusOne = Short.MIN_VALUE;
        sMinMinusOne--;
        assertEquals(Short.MAX_VALUE, sMinMinusOne);

        short sMinMinusOnePre = Short.MIN_VALUE;
        --sMinMinusOnePre;
        assertEquals(Short.MAX_VALUE, sMinMinusOnePre);

        byte bMaxPlusOne = Byte.MAX_VALUE;
        bMaxPlusOne++;
        assertEquals(Byte.MIN_VALUE, bMaxPlusOne);

        byte bMaxPlusOnePre = Byte.MAX_VALUE;
        ++bMaxPlusOnePre;
        assertEquals(Byte.MIN_VALUE, bMaxPlusOnePre);

        byte bMinMinusOne = Byte.MIN_VALUE;
        bMinMinusOne--;
        assertEquals(Byte.MAX_VALUE, bMinMinusOne);

        byte bMinMinusOnePre = Byte.MIN_VALUE;
        --bMinMinusOnePre;
        assertEquals(Byte.MAX_VALUE, bMinMinusOnePre);

        char cMaxPlusOne = Character.MAX_VALUE;
        cMaxPlusOne++;
        assertEquals(Character.MIN_VALUE, cMaxPlusOne);

        char cMaxPlusOnePre = Character.MAX_VALUE;
        ++cMaxPlusOnePre;
        assertEquals(Character.MIN_VALUE, cMaxPlusOnePre);

        char cMinMinusOne = Character.MIN_VALUE;
        cMinMinusOne--;
        assertEquals(Character.MAX_VALUE, cMinMinusOne);

        char cMinMinusOnePre = Character.MIN_VALUE;
        --cMinMinusOnePre;
        assertEquals(Character.MAX_VALUE, cMinMinusOnePre);
    }

    public void testVolatileIncrementDecrementOverflowWraparound() {
        vVal = Integer.MAX_VALUE;
        vVal++;
        assertEquals(Integer.MIN_VALUE, vVal);

        vVal = Integer.MAX_VALUE;
        ++vVal;
        assertEquals(Integer.MIN_VALUE, vVal);

        vVal = Integer.MIN_VALUE;
        vVal--;
        assertEquals(Integer.MAX_VALUE, vVal);

        vVal = Integer.MIN_VALUE;
        --vVal;
        assertEquals(Integer.MAX_VALUE, vVal);

        vlVal = Long.MAX_VALUE;
        vlVal++;
        assertEquals(Long.MIN_VALUE, vlVal);

        vlVal = Long.MAX_VALUE;
        ++vlVal;
        assertEquals(Long.MIN_VALUE, vlVal);

        vlVal = Long.MIN_VALUE;
        vlVal--;
        assertEquals(Long.MAX_VALUE, vlVal);

        vlVal = Long.MIN_VALUE;
        --vlVal;
        assertEquals(Long.MAX_VALUE, vlVal);

        vsVal = Short.MAX_VALUE;
        vsVal++;
        assertEquals(Short.MIN_VALUE, vsVal);

        vsVal = Short.MAX_VALUE;
        ++vsVal;
        assertEquals(Short.MIN_VALUE, vsVal);

        vsVal = Short.MIN_VALUE;
        vsVal--;
        assertEquals(Short.MAX_VALUE, vsVal);

        vsVal = Short.MIN_VALUE;
        --vsVal;
        assertEquals(Short.MAX_VALUE, vsVal);

        vbVal = Byte.MAX_VALUE;
        vbVal++;
        assertEquals(Byte.MIN_VALUE, vbVal);

        vbVal = Byte.MAX_VALUE;
        ++vbVal;
        assertEquals(Byte.MIN_VALUE, vbVal);

        vbVal = Byte.MIN_VALUE;
        vbVal--;
        assertEquals(Byte.MAX_VALUE, vbVal);

        vbVal = Byte.MIN_VALUE;
        --vbVal;
        assertEquals(Byte.MAX_VALUE, vbVal);

        vcVal = Character.MAX_VALUE;
        vcVal++;
        assertEquals(Character.MIN_VALUE, vcVal);

        vcVal = Character.MAX_VALUE;
        ++vcVal;
        assertEquals(Character.MIN_VALUE, vcVal);

        vcVal = Character.MIN_VALUE;
        vcVal--;
        assertEquals(Character.MAX_VALUE, vcVal);

        vcVal = Character.MIN_VALUE;
        --vcVal;
        assertEquals(Character.MAX_VALUE, vcVal);
    }
  }
