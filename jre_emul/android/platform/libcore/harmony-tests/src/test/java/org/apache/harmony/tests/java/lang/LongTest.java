/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.lang;

import java.util.Properties;

import junit.framework.TestCase;

public class LongTest extends TestCase {
    private Properties orgProps;

    @Override
    protected void setUp() {
        orgProps = System.getProperties();
    }

    @Override
    protected void tearDown() {
        System.setProperties(orgProps);
    }

    /**
     * java.lang.Long#byteValue()
     */
    public void test_byteValue() {
        // Test for method byte java.lang.Long.byteValue()
        Long l = new Long(127);
        assertEquals("Returned incorrect byte value", 127, l.byteValue());
        assertEquals("Returned incorrect byte value", -1, new Long(Long.MAX_VALUE)
                .byteValue());
    }

    /**
     * java.lang.Long#compareTo(java.lang.Long)
     */
    public void test_compareToLjava_lang_Long() {
        // Test for method int java.lang.Long.compareTo(java.lang.Long)
        assertTrue("-2 compared to 1 gave non-negative answer", new Long(-2L)
                .compareTo(new Long(1L)) < 0);
        assertEquals("-2 compared to -2 gave non-zero answer", 0, new Long(-2L)
                .compareTo(new Long(-2L)));
        assertTrue("3 compared to 2 gave non-positive answer", new Long(3L)
                .compareTo(new Long(2L)) > 0);

        try {
            new Long(0).compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * java.lang.Long#decode(java.lang.String)
     */
    public void test_decodeLjava_lang_String2() {
        // Test for method java.lang.Long
        // java.lang.Long.decode(java.lang.String)
        assertEquals("Returned incorrect value for hex string", 255L, Long.decode(
                "0xFF").longValue());
        assertEquals("Returned incorrect value for dec string", -89000L, Long.decode(
                "-89000").longValue());
        assertEquals("Returned incorrect value for 0 decimal", 0, Long.decode("0")
                .longValue());
        assertEquals("Returned incorrect value for 0 hex", 0, Long.decode("0x0")
                .longValue());
        assertTrue(
                "Returned incorrect value for most negative value decimal",
                Long.decode("-9223372036854775808").longValue() == 0x8000000000000000L);
        assertTrue(
                "Returned incorrect value for most negative value hex",
                Long.decode("-0x8000000000000000").longValue() == 0x8000000000000000L);
        assertTrue(
                "Returned incorrect value for most positive value decimal",
                Long.decode("9223372036854775807").longValue() == 0x7fffffffffffffffL);
        assertTrue(
                "Returned incorrect value for most positive value hex",
                Long.decode("0x7fffffffffffffff").longValue() == 0x7fffffffffffffffL);
        assertTrue("Failed for 07654321765432", Long.decode("07654321765432")
                .longValue() == 07654321765432l);

        boolean exception = false;
        try {
            Long
                    .decode("999999999999999999999999999999999999999999999999999999");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for value > ilong", exception);

        exception = false;
        try {
            Long.decode("9223372036854775808");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Long.decode("-9223372036854775809");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

        exception = false;
        try {
            Long.decode("0x8000000000000000");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

        exception = false;
        try {
            Long.decode("-0x8000000000000001");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MIN_VALUE - 1", exception);

        exception = false;
        try {
            Long.decode("42325917317067571199");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for 42325917317067571199",
                exception);
    }

    /**
     * java.lang.Long#getLong(java.lang.String)
     */
    public void test_getLongLjava_lang_String() {
        // Test for method java.lang.Long
        // java.lang.Long.getLong(java.lang.String)
        Properties tProps = new Properties();
        tProps.put("testLong", "99");
        System.setProperties(tProps);
        assertTrue("returned incorrect Long", Long.getLong("testLong").equals(
                new Long(99)));
        assertNull("returned incorrect default Long",
                Long.getLong("ff"));
    }

    /**
     * java.lang.Long#getLong(java.lang.String, long)
     */
    public void test_getLongLjava_lang_StringJ() {
        // Test for method java.lang.Long
        // java.lang.Long.getLong(java.lang.String, long)
        Properties tProps = new Properties();
        tProps.put("testLong", "99");
        System.setProperties(tProps);
        assertTrue("returned incorrect Long", Long.getLong("testLong", 4L)
                .equals(new Long(99)));
        assertTrue("returned incorrect default Long", Long.getLong("ff", 4L)
                .equals(new Long(4)));
    }

    /**
     * java.lang.Long#getLong(java.lang.String, java.lang.Long)
     */
    public void test_getLongLjava_lang_StringLjava_lang_Long() {
        // Test for method java.lang.Long
        // java.lang.Long.getLong(java.lang.String, java.lang.Long)
        Properties tProps = new Properties();
        tProps.put("testLong", "99");
        System.setProperties(tProps);
        assertTrue("returned incorrect Long", Long.getLong("testLong",
                new Long(4)).equals(new Long(99)));
        assertTrue("returned incorrect default Long", Long.getLong("ff",
                new Long(4)).equals(new Long(4)));
    }

    /**
     * java.lang.Long#parseLong(java.lang.String)
     */
    public void test_parseLongLjava_lang_String2() {
        // Test for method long java.lang.Long.parseLong(java.lang.String)

        long l = Long.parseLong("89000000005");
        assertEquals("Parsed to incorrect long value", 89000000005L, l);
        assertEquals("Returned incorrect value for 0", 0, Long.parseLong("0"));
        assertTrue("Returned incorrect value for most negative value", Long
                .parseLong("-9223372036854775808") == 0x8000000000000000L);
        assertTrue("Returned incorrect value for most positive value", Long
                .parseLong("9223372036854775807") == 0x7fffffffffffffffL);

        boolean exception = false;
        try {
            Long.parseLong("9223372036854775808");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Long.parseLong("-9223372036854775809");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);
    }

    /**
     * java.lang.Long#parseLong(java.lang.String, int)
     */
    public void test_parseLongLjava_lang_StringI() {
        // Test for method long java.lang.Long.parseLong(java.lang.String, int)
        assertEquals("Returned incorrect value",
                100000000L, Long.parseLong("100000000", 10));
        assertEquals("Returned incorrect value from hex string", 68719476735L, Long.parseLong(
                "FFFFFFFFF", 16));
        assertTrue("Returned incorrect value from octal string: "
                + Long.parseLong("77777777777"), Long.parseLong("77777777777",
                8) == 8589934591L);
        assertEquals("Returned incorrect value for 0 hex", 0, Long
                .parseLong("0", 16));
        assertTrue("Returned incorrect value for most negative value hex", Long
                .parseLong("-8000000000000000", 16) == 0x8000000000000000L);
        assertTrue("Returned incorrect value for most positive value hex", Long
                .parseLong("7fffffffffffffff", 16) == 0x7fffffffffffffffL);
        assertEquals("Returned incorrect value for 0 decimal", 0, Long.parseLong(
                "0", 10));
        assertTrue(
                "Returned incorrect value for most negative value decimal",
                Long.parseLong("-9223372036854775808", 10) == 0x8000000000000000L);
        assertTrue(
                "Returned incorrect value for most positive value decimal",
                Long.parseLong("9223372036854775807", 10) == 0x7fffffffffffffffL);

        boolean exception = false;
        try {
            Long.parseLong("999999999999", 8);
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Failed to throw exception when passed invalid string",
                exception);

        exception = false;
        try {
            Long.parseLong("9223372036854775808", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Long.parseLong("-9223372036854775809", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

        exception = false;
        try {
            Long.parseLong("8000000000000000", 16);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

        exception = false;
        try {
            Long.parseLong("-8000000000000001", 16);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MIN_VALUE + 1", exception);

        exception = false;
        try {
            Long.parseLong("42325917317067571199", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for 42325917317067571199",
                exception);
    }

    /**
     * java.lang.Long#toBinaryString(long)
     */
    public void test_toBinaryStringJ() {
        // Test for method java.lang.String java.lang.Long.toBinaryString(long)
        assertEquals("Incorrect binary string returned", "11011001010010010000", Long.toBinaryString(
                890000L));
        assertEquals("Incorrect binary string returned",

                "1000000000000000000000000000000000000000000000000000000000000000", Long
                .toBinaryString(Long.MIN_VALUE)
        );
        assertEquals("Incorrect binary string returned",

                "111111111111111111111111111111111111111111111111111111111111111", Long
                .toBinaryString(Long.MAX_VALUE)
        );
    }

    /**
     * java.lang.Long#toHexString(long)
     */
    public void test_toHexStringJ() {
        // Test for method java.lang.String java.lang.Long.toHexString(long)
        assertEquals("Incorrect hex string returned", "54e0845", Long.toHexString(89000005L)
        );
        assertEquals("Incorrect hex string returned", "8000000000000000", Long.toHexString(
                Long.MIN_VALUE));
        assertEquals("Incorrect hex string returned", "7fffffffffffffff", Long.toHexString(
                Long.MAX_VALUE));
    }

    /**
     * java.lang.Long#toOctalString(long)
     */
    public void test_toOctalStringJ() {
        // Test for method java.lang.String java.lang.Long.toOctalString(long)
        assertEquals("Returned incorrect oct string", "77777777777", Long.toOctalString(
                8589934591L));
        assertEquals("Returned incorrect oct string", "1000000000000000000000", Long.toOctalString(
                Long.MIN_VALUE));
        assertEquals("Returned incorrect oct string", "777777777777777777777", Long.toOctalString(
                Long.MAX_VALUE));
    }

    /**
     * java.lang.Long#toString()
     */
    public void test_toString2() {
        // Test for method java.lang.String java.lang.Long.toString()
        Long l = new Long(89000000005L);
        assertEquals("Returned incorrect String",
                "89000000005", l.toString());
        assertEquals("Returned incorrect String", "-9223372036854775808", new Long(Long.MIN_VALUE)
                .toString());
        assertEquals("Returned incorrect String", "9223372036854775807", new Long(Long.MAX_VALUE)
                .toString());
    }

    /**
     * java.lang.Long#toString(long)
     */
    public void test_toStringJ2() {
        // Test for method java.lang.String java.lang.Long.toString(long)

        assertEquals("Returned incorrect String", "89000000005", Long.toString(89000000005L)
        );
        assertEquals("Returned incorrect String", "-9223372036854775808", Long.toString(Long.MIN_VALUE)
        );
        assertEquals("Returned incorrect String", "9223372036854775807", Long.toString(Long.MAX_VALUE)
        );
    }

    /**
     * java.lang.Long#toString(long, int)
     */
    public void test_toStringJI() {
        // Test for method java.lang.String java.lang.Long.toString(long, int)
        assertEquals("Returned incorrect dec string", "100000000", Long.toString(100000000L,
                10));
        assertEquals("Returned incorrect hex string", "fffffffff", Long.toString(68719476735L,
                16));
        assertEquals("Returned incorrect oct string", "77777777777", Long.toString(8589934591L,
                8));
        assertEquals("Returned incorrect bin string",
                "1111111111111111111111111111111111111111111", Long.toString(
                8796093022207L, 2));
        assertEquals("Returned incorrect min string", "-9223372036854775808", Long.toString(
                0x8000000000000000L, 10));
        assertEquals("Returned incorrect max string", "9223372036854775807", Long.toString(
                0x7fffffffffffffffL, 10));
        assertEquals("Returned incorrect min string", "-8000000000000000", Long.toString(
                0x8000000000000000L, 16));
        assertEquals("Returned incorrect max string", "7fffffffffffffff", Long.toString(
                0x7fffffffffffffffL, 16));
    }

    /**
     * java.lang.Long#valueOf(java.lang.String)
     */
    public void test_valueOfLjava_lang_String2() {
        // Test for method java.lang.Long
        // java.lang.Long.valueOf(java.lang.String)
        assertEquals("Returned incorrect value", 100000000L, Long.valueOf("100000000")
                .longValue());
        assertTrue("Returned incorrect value", Long.valueOf(
                "9223372036854775807").longValue() == Long.MAX_VALUE);
        assertTrue("Returned incorrect value", Long.valueOf(
                "-9223372036854775808").longValue() == Long.MIN_VALUE);

        boolean exception = false;
        try {
            Long
                    .valueOf("999999999999999999999999999999999999999999999999999999999999");
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Failed to throw exception when passed invalid string",
                exception);

        exception = false;
        try {
            Long.valueOf("9223372036854775808");
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Failed to throw exception when passed invalid string",
                exception);

        exception = false;
        try {
            Long.valueOf("-9223372036854775809");
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Failed to throw exception when passed invalid string",
                exception);
    }

    /**
     * java.lang.Long#valueOf(java.lang.String, int)
     */
    public void test_valueOfLjava_lang_StringI() {
        // Test for method java.lang.Long
        // java.lang.Long.valueOf(java.lang.String, int)
        assertEquals("Returned incorrect value", 100000000L, Long.valueOf("100000000", 10)
                .longValue());
        assertEquals("Returned incorrect value from hex string", 68719476735L, Long.valueOf(
                "FFFFFFFFF", 16).longValue());
        assertTrue("Returned incorrect value from octal string: "
                + Long.valueOf("77777777777", 8).toString(), Long.valueOf(
                "77777777777", 8).longValue() == 8589934591L);
        assertTrue("Returned incorrect value", Long.valueOf(
                "9223372036854775807", 10).longValue() == Long.MAX_VALUE);
        assertTrue("Returned incorrect value", Long.valueOf(
                "-9223372036854775808", 10).longValue() == Long.MIN_VALUE);
        assertTrue("Returned incorrect value", Long.valueOf("7fffffffffffffff",
                16).longValue() == Long.MAX_VALUE);
        assertTrue("Returned incorrect value", Long.valueOf(
                "-8000000000000000", 16).longValue() == Long.MIN_VALUE);

        boolean exception = false;
        try {
            Long.valueOf("999999999999", 8);
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Failed to throw exception when passed invalid string",
                exception);

        exception = false;
        try {
            Long.valueOf("9223372036854775808", 10);
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Failed to throw exception when passed invalid string",
                exception);

        exception = false;
        try {
            Long.valueOf("-9223372036854775809", 10);
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Failed to throw exception when passed invalid string",
                exception);
    }

    /**
     * java.lang.Long#valueOf(long)
     */
    public void test_valueOfJ() {
        assertEquals(new Long(Long.MIN_VALUE), Long.valueOf(Long.MIN_VALUE));
        assertEquals(new Long(Long.MAX_VALUE), Long.valueOf(Long.MAX_VALUE));
        assertEquals(new Long(0), Long.valueOf(0));

        long lng = -128;
        while (lng < 128) {
            assertEquals(new Long(lng), Long.valueOf(lng));
            assertSame(Long.valueOf(lng), Long.valueOf(lng));
            lng++;
        }
    }

    /**
     * java.lang.Long#hashCode()
     */
    public void test_hashCode() {
        assertEquals((int) (1L ^ (1L >>> 32)), new Long(1).hashCode());
        assertEquals((int) (2L ^ (2L >>> 32)), new Long(2).hashCode());
        assertEquals((int) (0L ^ (0L >>> 32)), new Long(0).hashCode());
        assertEquals((int) (-1L ^ (-1L >>> 32)), new Long(-1).hashCode());
    }

    /**
     * java.lang.Long#Long(String)
     */
    public void test_ConstructorLjava_lang_String() {
        assertEquals(new Long(0), new Long("0"));
        assertEquals(new Long(1), new Long("1"));
        assertEquals(new Long(-1), new Long("-1"));

        try {
            new Long("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Long("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Long("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Long(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Long#Long
     */
    public void test_ConstructorJ() {
        assertEquals(1, new Long(1).intValue());
        assertEquals(2, new Long(2).intValue());
        assertEquals(0, new Long(0).intValue());
        assertEquals(-1, new Long(-1).intValue());
    }

    /**
     * java.lang.Long#byteValue()
     */
    public void test_booleanValue() {
        assertEquals(1, new Long(1).byteValue());
        assertEquals(2, new Long(2).byteValue());
        assertEquals(0, new Long(0).byteValue());
        assertEquals(-1, new Long(-1).byteValue());
    }

    /**
     * java.lang.Long#equals(Object)
     */
    public void test_equalsLjava_lang_Object() {
        assertEquals(new Long(0), Long.valueOf(0));
        assertEquals(new Long(1), Long.valueOf(1));
        assertEquals(new Long(-1), Long.valueOf(-1));

        Long fixture = new Long(25);
        assertEquals(fixture, fixture);
        assertFalse(fixture.equals(null));
        assertFalse(fixture.equals("Not a Long"));
    }

    /**
     * java.lang.Long#toString()
     */
    public void test_toString() {
        assertEquals("-1", new Long(-1).toString());
        assertEquals("0", new Long(0).toString());
        assertEquals("1", new Long(1).toString());
        assertEquals("-1", new Long(0xFFFFFFFF).toString());
    }

    /**
     * java.lang.Long#toString
     */
    public void test_toStringJ() {
        assertEquals("-1", Long.toString(-1));
        assertEquals("0", Long.toString(0));
        assertEquals("1", Long.toString(1));
        assertEquals("-1", Long.toString(0xFFFFFFFF));
    }

    /**
     * java.lang.Long#valueOf(String)
     */
    public void test_valueOfLjava_lang_String() {
        assertEquals(new Long(0), Long.valueOf("0"));
        assertEquals(new Long(1), Long.valueOf("1"));
        assertEquals(new Long(-1), Long.valueOf("-1"));

        try {
            Long.valueOf("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.valueOf("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.valueOf("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.valueOf(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Long#valueOf(String, long)
     */
    public void test_valueOfLjava_lang_StringJ() {
        assertEquals(new Long(0), Long.valueOf("0", 10));
        assertEquals(new Long(1), Long.valueOf("1", 10));
        assertEquals(new Long(-1), Long.valueOf("-1", 10));

        //must be consistent with Character.digit()
        assertEquals(Character.digit('1', 2), Long.valueOf("1", 2).byteValue());
        assertEquals(Character.digit('F', 16), Long.valueOf("F", 16).byteValue());

        try {
            Long.valueOf("0x1", 10);
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.valueOf("9.2", 10);
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.valueOf("", 10);
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.valueOf(null, 10);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Long#parseLong(String)
     */
    public void test_parseLongLjava_lang_String() {
        assertEquals(0, Long.parseLong("0"));
        assertEquals(1, Long.parseLong("1"));
        assertEquals(-1, Long.parseLong("-1"));

        try {
            Long.parseLong("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.parseLong("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.parseLong("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.parseLong(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Long#parseLong(String, long)
     */
    public void test_parseLongLjava_lang_StringJ() {
        assertEquals(0, Long.parseLong("0", 10));
        assertEquals(1, Long.parseLong("1", 10));
        assertEquals(-1, Long.parseLong("-1", 10));

        //must be consistent with Character.digit()
        assertEquals(Character.digit('1', 2), Long.parseLong("1", 2));
        assertEquals(Character.digit('F', 16), Long.parseLong("F", 16));

        try {
            Long.parseLong("0x1", 10);
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.parseLong("9.2", 10);
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.parseLong("", 10);
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.parseLong(null, 10);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Long#decode(String)
     */
    public void test_decodeLjava_lang_String() {
        assertEquals(new Long(0), Long.decode("0"));
        assertEquals(new Long(1), Long.decode("1"));
        assertEquals(new Long(-1), Long.decode("-1"));
        assertEquals(new Long(0xF), Long.decode("0xF"));
        assertEquals(new Long(0xF), Long.decode("#F"));
        assertEquals(new Long(0xF), Long.decode("0XF"));
        assertEquals(new Long(07), Long.decode("07"));

        try {
            Long.decode("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.decode("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Long.decode(null);
            //undocumented NPE, but seems consistent across JREs
            fail("Expected NullPointerException with null string.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * java.lang.Long#doubleValue()
     */
    public void test_doubleValue() {
        assertEquals(-1D, new Long(-1).doubleValue(), 0D);
        assertEquals(0D, new Long(0).doubleValue(), 0D);
        assertEquals(1D, new Long(1).doubleValue(), 0D);
    }

    /**
     * java.lang.Long#floatValue()
     */
    public void test_floatValue() {
        assertEquals(-1F, new Long(-1).floatValue(), 0F);
        assertEquals(0F, new Long(0).floatValue(), 0F);
        assertEquals(1F, new Long(1).floatValue(), 0F);
    }

    /**
     * java.lang.Long#intValue()
     */
    public void test_intValue() {
        assertEquals(-1, new Long(-1).intValue());
        assertEquals(0, new Long(0).intValue());
        assertEquals(1, new Long(1).intValue());
    }

    /**
     * java.lang.Long#longValue()
     */
    public void test_longValue() {
        assertEquals(-1L, new Long(-1).longValue());
        assertEquals(0L, new Long(0).longValue());
        assertEquals(1L, new Long(1).longValue());
    }

    /**
     * java.lang.Long#shortValue()
     */
    public void test_shortValue() {
        assertEquals(-1, new Long(-1).shortValue());
        assertEquals(0, new Long(0).shortValue());
        assertEquals(1, new Long(1).shortValue());
    }

    /**
     * java.lang.Long#highestOneBit(long)
     */
    public void test_highestOneBitJ() {
        assertEquals(0x08, Long.highestOneBit(0x0A));
        assertEquals(0x08, Long.highestOneBit(0x0B));
        assertEquals(0x08, Long.highestOneBit(0x0C));
        assertEquals(0x08, Long.highestOneBit(0x0F));
        assertEquals(0x80, Long.highestOneBit(0xFF));

        assertEquals(0x080000, Long.highestOneBit(0x0F1234));
        assertEquals(0x800000, Long.highestOneBit(0xFF9977));

        assertEquals(0x8000000000000000L, Long.highestOneBit(0xFFFFFFFFFFFFFFFFL));

        assertEquals(0, Long.highestOneBit(0));
        assertEquals(1, Long.highestOneBit(1));
        assertEquals(0x8000000000000000L, Long.highestOneBit(-1));
    }

    /**
     * java.lang.Long#lowestOneBit(long)
     */
    public void test_lowestOneBitJ() {
        assertEquals(0x10, Long.lowestOneBit(0xF0));

        assertEquals(0x10, Long.lowestOneBit(0x90));
        assertEquals(0x10, Long.lowestOneBit(0xD0));

        assertEquals(0x10, Long.lowestOneBit(0x123490));
        assertEquals(0x10, Long.lowestOneBit(0x1234D0));

        assertEquals(0x100000, Long.lowestOneBit(0x900000));
        assertEquals(0x100000, Long.lowestOneBit(0xD00000));

        assertEquals(0x40, Long.lowestOneBit(0x40));
        assertEquals(0x40, Long.lowestOneBit(0xC0));

        assertEquals(0x4000, Long.lowestOneBit(0x4000));
        assertEquals(0x4000, Long.lowestOneBit(0xC000));

        assertEquals(0x4000, Long.lowestOneBit(0x99994000));
        assertEquals(0x4000, Long.lowestOneBit(0x9999C000));

        assertEquals(0, Long.lowestOneBit(0));
        assertEquals(1, Long.lowestOneBit(1));
        assertEquals(1, Long.lowestOneBit(-1));
    }

    /**
     * java.lang.Long#numberOfLeadingZeros(long)
     */
    public void test_numberOfLeadingZerosJ() {
        assertEquals(64, Long.numberOfLeadingZeros(0x0L));
        assertEquals(63, Long.numberOfLeadingZeros(0x1));
        assertEquals(62, Long.numberOfLeadingZeros(0x2));
        assertEquals(62, Long.numberOfLeadingZeros(0x3));
        assertEquals(61, Long.numberOfLeadingZeros(0x4));
        assertEquals(61, Long.numberOfLeadingZeros(0x5));
        assertEquals(61, Long.numberOfLeadingZeros(0x6));
        assertEquals(61, Long.numberOfLeadingZeros(0x7));
        assertEquals(60, Long.numberOfLeadingZeros(0x8));
        assertEquals(60, Long.numberOfLeadingZeros(0x9));
        assertEquals(60, Long.numberOfLeadingZeros(0xA));
        assertEquals(60, Long.numberOfLeadingZeros(0xB));
        assertEquals(60, Long.numberOfLeadingZeros(0xC));
        assertEquals(60, Long.numberOfLeadingZeros(0xD));
        assertEquals(60, Long.numberOfLeadingZeros(0xE));
        assertEquals(60, Long.numberOfLeadingZeros(0xF));
        assertEquals(59, Long.numberOfLeadingZeros(0x10));
        assertEquals(56, Long.numberOfLeadingZeros(0x80));
        assertEquals(56, Long.numberOfLeadingZeros(0xF0));
        assertEquals(55, Long.numberOfLeadingZeros(0x100));
        assertEquals(52, Long.numberOfLeadingZeros(0x800));
        assertEquals(52, Long.numberOfLeadingZeros(0xF00));
        assertEquals(51, Long.numberOfLeadingZeros(0x1000));
        assertEquals(48, Long.numberOfLeadingZeros(0x8000));
        assertEquals(48, Long.numberOfLeadingZeros(0xF000));
        assertEquals(47, Long.numberOfLeadingZeros(0x10000));
        assertEquals(44, Long.numberOfLeadingZeros(0x80000));
        assertEquals(44, Long.numberOfLeadingZeros(0xF0000));
        assertEquals(43, Long.numberOfLeadingZeros(0x100000));
        assertEquals(40, Long.numberOfLeadingZeros(0x800000));
        assertEquals(40, Long.numberOfLeadingZeros(0xF00000));
        assertEquals(39, Long.numberOfLeadingZeros(0x1000000));
        assertEquals(36, Long.numberOfLeadingZeros(0x8000000));
        assertEquals(36, Long.numberOfLeadingZeros(0xF000000));
        assertEquals(35, Long.numberOfLeadingZeros(0x10000000));
        assertEquals(0, Long.numberOfLeadingZeros(0x80000000));
        assertEquals(0, Long.numberOfLeadingZeros(0xF0000000));

        assertEquals(1, Long.numberOfLeadingZeros(Long.MAX_VALUE));
        assertEquals(0, Long.numberOfLeadingZeros(Long.MIN_VALUE));
    }

    /**
     * java.lang.Long#numberOfTrailingZeros(long)
     */
    public void test_numberOfTrailingZerosJ() {
        assertEquals(64, Long.numberOfTrailingZeros(0x0));
        assertEquals(63, Long.numberOfTrailingZeros(Long.MIN_VALUE));
        assertEquals(0, Long.numberOfTrailingZeros(Long.MAX_VALUE));

        assertEquals(0, Long.numberOfTrailingZeros(0x1));
        assertEquals(3, Long.numberOfTrailingZeros(0x8));
        assertEquals(0, Long.numberOfTrailingZeros(0xF));

        assertEquals(4, Long.numberOfTrailingZeros(0x10));
        assertEquals(7, Long.numberOfTrailingZeros(0x80));
        assertEquals(4, Long.numberOfTrailingZeros(0xF0));

        assertEquals(8, Long.numberOfTrailingZeros(0x100));
        assertEquals(11, Long.numberOfTrailingZeros(0x800));
        assertEquals(8, Long.numberOfTrailingZeros(0xF00));

        assertEquals(12, Long.numberOfTrailingZeros(0x1000));
        assertEquals(15, Long.numberOfTrailingZeros(0x8000));
        assertEquals(12, Long.numberOfTrailingZeros(0xF000));

        assertEquals(16, Long.numberOfTrailingZeros(0x10000));
        assertEquals(19, Long.numberOfTrailingZeros(0x80000));
        assertEquals(16, Long.numberOfTrailingZeros(0xF0000));

        assertEquals(20, Long.numberOfTrailingZeros(0x100000));
        assertEquals(23, Long.numberOfTrailingZeros(0x800000));
        assertEquals(20, Long.numberOfTrailingZeros(0xF00000));

        assertEquals(24, Long.numberOfTrailingZeros(0x1000000));
        assertEquals(27, Long.numberOfTrailingZeros(0x8000000));
        assertEquals(24, Long.numberOfTrailingZeros(0xF000000));

        assertEquals(28, Long.numberOfTrailingZeros(0x10000000));
        assertEquals(31, Long.numberOfTrailingZeros(0x80000000));
        assertEquals(28, Long.numberOfTrailingZeros(0xF0000000));
    }

    /**
     * java.lang.Long#bitCount(long)
     */
    public void test_bitCountJ() {
        assertEquals(0, Long.bitCount(0x0));
        assertEquals(1, Long.bitCount(0x1));
        assertEquals(1, Long.bitCount(0x2));
        assertEquals(2, Long.bitCount(0x3));
        assertEquals(1, Long.bitCount(0x4));
        assertEquals(2, Long.bitCount(0x5));
        assertEquals(2, Long.bitCount(0x6));
        assertEquals(3, Long.bitCount(0x7));
        assertEquals(1, Long.bitCount(0x8));
        assertEquals(2, Long.bitCount(0x9));
        assertEquals(2, Long.bitCount(0xA));
        assertEquals(3, Long.bitCount(0xB));
        assertEquals(2, Long.bitCount(0xC));
        assertEquals(3, Long.bitCount(0xD));
        assertEquals(3, Long.bitCount(0xE));
        assertEquals(4, Long.bitCount(0xF));

        assertEquals(8, Long.bitCount(0xFF));
        assertEquals(12, Long.bitCount(0xFFF));
        assertEquals(16, Long.bitCount(0xFFFF));
        assertEquals(20, Long.bitCount(0xFFFFF));
        assertEquals(24, Long.bitCount(0xFFFFFF));
        assertEquals(28, Long.bitCount(0xFFFFFFF));
        assertEquals(64, Long.bitCount(0xFFFFFFFFFFFFFFFFL));
    }

    /**
     * java.lang.Long#rotateLeft(long, long)
     */
    public void test_rotateLeftJI() {
        assertEquals(0xF, Long.rotateLeft(0xF, 0));
        assertEquals(0xF0, Long.rotateLeft(0xF, 4));
        assertEquals(0xF00, Long.rotateLeft(0xF, 8));
        assertEquals(0xF000, Long.rotateLeft(0xF, 12));
        assertEquals(0xF0000, Long.rotateLeft(0xF, 16));
        assertEquals(0xF00000, Long.rotateLeft(0xF, 20));
        assertEquals(0xF000000, Long.rotateLeft(0xF, 24));
        assertEquals(0xF0000000L, Long.rotateLeft(0xF, 28));
        assertEquals(0xF000000000000000L, Long.rotateLeft(0xF000000000000000L, 64));
    }

    /**
     * java.lang.Long#rotateRight(long, long)
     */
    public void test_rotateRightJI() {
        assertEquals(0xF, Long.rotateRight(0xF0, 4));
        assertEquals(0xF, Long.rotateRight(0xF00, 8));
        assertEquals(0xF, Long.rotateRight(0xF000, 12));
        assertEquals(0xF, Long.rotateRight(0xF0000, 16));
        assertEquals(0xF, Long.rotateRight(0xF00000, 20));
        assertEquals(0xF, Long.rotateRight(0xF000000, 24));
        assertEquals(0xF, Long.rotateRight(0xF0000000L, 28));
        assertEquals(0xF000000000000000L, Long.rotateRight(0xF000000000000000L, 64));
        assertEquals(0xF000000000000000L, Long.rotateRight(0xF000000000000000L, 0));

    }

    /**
     * java.lang.Long#reverseBytes(long)
     */
    public void test_reverseBytesJ() {
        assertEquals(0xAABBCCDD00112233L, Long.reverseBytes(0x33221100DDCCBBAAL));
        assertEquals(0x1122334455667788L, Long.reverseBytes(0x8877665544332211L));
        assertEquals(0x0011223344556677L, Long.reverseBytes(0x7766554433221100L));
        assertEquals(0x2000000000000002L, Long.reverseBytes(0x0200000000000020L));
    }

    /**
     * java.lang.Long#reverse(long)
     */
    public void test_reverseJ() {
        assertEquals(0, Long.reverse(0));
        assertEquals(-1, Long.reverse(-1));
        assertEquals(0x8000000000000000L, Long.reverse(1));
    }

    /**
     * java.lang.Long#signum(long)
     */
    public void test_signumJ() {
        for (int i = -128; i < 0; i++) {
            assertEquals(-1, Long.signum(i));
        }
        assertEquals(0, Long.signum(0));
        for (int i = 1; i <= 127; i++) {
            assertEquals(1, Long.signum(i));
        }
    }
}