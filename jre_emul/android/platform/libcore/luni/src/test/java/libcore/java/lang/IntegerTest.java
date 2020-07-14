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
}
