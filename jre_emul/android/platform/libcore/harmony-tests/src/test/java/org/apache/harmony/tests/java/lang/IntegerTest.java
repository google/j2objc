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

public class IntegerTest extends TestCase {
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
     * java.lang.Integer#byteValue()
     */
    public void test_byteValue() {
        // Test for method byte java.lang.Integer.byteValue()
        assertEquals("Returned incorrect byte value", -1, new Integer(65535)
                .byteValue());
        assertEquals("Returned incorrect byte value", 127, new Integer(127)
                .byteValue());
    }

    /**
     * java.lang.Integer#compareTo(java.lang.Integer)
     */
    public void test_compareToLjava_lang_Integer() {
        // Test for method int java.lang.Integer.compareTo(java.lang.Integer)
        assertTrue("-2 compared to 1 gave non-negative answer", new Integer(-2)
                .compareTo(new Integer(1)) < 0);
        assertEquals("-2 compared to -2 gave non-zero answer", 0, new Integer(-2)
                .compareTo(new Integer(-2)));
        assertTrue("3 compared to 2 gave non-positive answer", new Integer(3)
                .compareTo(new Integer(2)) > 0);

        try {
            new Integer(0).compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * java.lang.Integer#decode(java.lang.String)
     */
    public void test_decodeLjava_lang_String2() {
        // Test for method java.lang.Integer
        // java.lang.Integer.decode(java.lang.String)
        assertEquals("Failed for 132233",
                132233, Integer.decode("132233").intValue());
        assertEquals("Failed for 07654321",
                07654321, Integer.decode("07654321").intValue());
        assertTrue("Failed for #1234567",
                Integer.decode("#1234567").intValue() == 0x1234567);
        assertTrue("Failed for 0xdAd",
                Integer.decode("0xdAd").intValue() == 0xdad);
        assertEquals("Failed for -23", -23, Integer.decode("-23").intValue());
        assertEquals("Returned incorrect value for 0 decimal", 0, Integer
                .decode("0").intValue());
        assertEquals("Returned incorrect value for 0 hex", 0, Integer.decode("0x0")
                .intValue());
        assertTrue("Returned incorrect value for most negative value decimal",
                Integer.decode("-2147483648").intValue() == 0x80000000);
        assertTrue("Returned incorrect value for most negative value hex",
                Integer.decode("-0x80000000").intValue() == 0x80000000);
        assertTrue("Returned incorrect value for most positive value decimal",
                Integer.decode("2147483647").intValue() == 0x7fffffff);
        assertTrue("Returned incorrect value for most positive value hex",
                Integer.decode("0x7fffffff").intValue() == 0x7fffffff);

        boolean exception = false;
        try {
            Integer.decode("0a");
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Failed to throw NumberFormatException for \"Oa\"",
                exception);

        exception = false;
        try {
            Integer.decode("2147483648");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Integer.decode("-2147483649");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

        exception = false;
        try {
            Integer.decode("0x80000000");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

        exception = false;
        try {
            Integer.decode("-0x80000001");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MIN_VALUE - 1", exception);

        exception = false;
        try {
            Integer.decode("9999999999");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for 9999999999", exception);

        try {
            Integer.decode("-");
            fail("Expected exception for -");
        } catch (NumberFormatException e) {
            // Expected
        }

        try {
            Integer.decode("0x");
            fail("Expected exception for 0x");
        } catch (NumberFormatException e) {
            // Expected
        }

        try {
            Integer.decode("#");
            fail("Expected exception for #");
        } catch (NumberFormatException e) {
            // Expected
        }

        try {
            Integer.decode("x123");
            fail("Expected exception for x123");
        } catch (NumberFormatException e) {
            // Expected
        }

        try {
            Integer.decode(null);
            fail("Expected exception for null");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            Integer.decode("");
            fail("Expected exception for empty string");
        } catch (NumberFormatException ex) {
            // Expected
        }

        try {
            Integer.decode(" ");
            fail("Expected exception for single space");
        } catch (NumberFormatException ex) {
            // Expected
        }

    }

    /**
     * java.lang.Integer#doubleValue()
     */
    public void test_doubleValue2() {
        // Test for method double java.lang.Integer.doubleValue()
        assertEquals("Returned incorrect double value", 2147483647.0, new Integer(2147483647)
                .doubleValue(), 0.0D);
        assertEquals("Returned incorrect double value", -2147483647.0, new Integer(-2147483647)
                .doubleValue(), 0.0D);
    }

    /**
     * java.lang.Integer#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object2() {
        // Test for method boolean java.lang.Integer.equals(java.lang.Object)
        Integer i1 = new Integer(1000);
        Integer i2 = new Integer(1000);
        Integer i3 = new Integer(-1000);
        assertTrue("Equality test failed", i1.equals(i2) && !(i1.equals(i3)));
    }

    /**
     * java.lang.Integer#floatValue()
     */
    public void test_floatValue2() {
        // Test for method float java.lang.Integer.floatValue()
        assertTrue("Returned incorrect float value", new Integer(65535)
                .floatValue() == 65535.0f);
        assertTrue("Returned incorrect float value", new Integer(-65535)
                .floatValue() == -65535.0f);
    }

    /**
     * java.lang.Integer#getInteger(java.lang.String)
     */
    public void test_getIntegerLjava_lang_String() {
        // Test for method java.lang.Integer
        // java.lang.Integer.getInteger(java.lang.String)
        Properties tProps = new Properties();
        tProps.put("testInt", "99");
        System.setProperties(tProps);
        assertTrue("returned incorrect Integer", Integer.getInteger("testInt")
                .equals(new Integer(99)));
        assertNull("returned incorrect default Integer", Integer
                .getInteger("ff"));
    }

    /**
     * java.lang.Integer#getInteger(java.lang.String, int)
     */
    public void test_getIntegerLjava_lang_StringI() {
        // Test for method java.lang.Integer
        // java.lang.Integer.getInteger(java.lang.String, int)
        Properties tProps = new Properties();
        tProps.put("testInt", "99");
        System.setProperties(tProps);
        assertTrue("returned incorrect Integer", Integer.getInteger("testInt",
                4).equals(new Integer(99)));
        assertTrue("returned incorrect default Integer", Integer.getInteger(
                "ff", 4).equals(new Integer(4)));
    }

    /**
     * java.lang.Integer#getInteger(java.lang.String, java.lang.Integer)
     */
    public void test_getIntegerLjava_lang_StringLjava_lang_Integer() {
        // Test for method java.lang.Integer
        // java.lang.Integer.getInteger(java.lang.String, java.lang.Integer)
        Properties tProps = new Properties();
        tProps.put("testInt", "99");
        System.setProperties(tProps);
        assertTrue("returned incorrect Integer", Integer.getInteger("testInt",
                new Integer(4)).equals(new Integer(99)));
        assertTrue("returned incorrect default Integer", Integer.getInteger(
                "ff", new Integer(4)).equals(new Integer(4)));
    }

    /**
     * java.lang.Integer#hashCode()
     */
    public void test_hashCode2() {
        // Test for method int java.lang.Integer.hashCode()

        Integer i1 = new Integer(1000);
        Integer i2 = new Integer(-1000);
        assertTrue("Returned incorrect hashcode", i1.hashCode() == 1000
                && (i2.hashCode() == -1000));
    }

    /**
     * java.lang.Integer#intValue()
     */
    public void test_intValue2() {
        // Test for method int java.lang.Integer.intValue()

        Integer i = new Integer(8900);
        assertEquals("Returned incorrect int value", 8900, i.intValue());
    }

    /**
     * java.lang.Integer#longValue()
     */
    public void test_longValue2() {
        // Test for method long java.lang.Integer.longValue()
        Integer i = new Integer(8900);
        assertEquals("Returned incorrect long value", 8900L, i.longValue());
    }

    /**
     * java.lang.Integer#parseInt(java.lang.String)
     */
    public void test_parseIntLjava_lang_String2() {
        // Test for method int java.lang.Integer.parseInt(java.lang.String)

        int i = Integer.parseInt("-8900");
        assertEquals("Returned incorrect int", -8900, i);
        assertEquals("Returned incorrect value for 0", 0, Integer.parseInt("0"));
        assertTrue("Returned incorrect value for most negative value", Integer
                .parseInt("-2147483648") == 0x80000000);
        assertTrue("Returned incorrect value for most positive value", Integer
                .parseInt("2147483647") == 0x7fffffff);

        boolean exception = false;
        try {
            Integer.parseInt("999999999999");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for value > int", exception);

        exception = false;
        try {
            Integer.parseInt("2147483648");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Integer.parseInt("-2147483649");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);
    }

    /**
     * java.lang.Integer#parseInt(java.lang.String, int)
     */
    public void test_parseIntLjava_lang_StringI2() {
        // Test for method int java.lang.Integer.parseInt(java.lang.String, int)
        assertEquals("Parsed dec val incorrectly",
                -8000, Integer.parseInt("-8000", 10));
        assertEquals("Parsed hex val incorrectly",
                255, Integer.parseInt("FF", 16));
        assertEquals("Parsed oct val incorrectly",
                16, Integer.parseInt("20", 8));
        assertEquals("Returned incorrect value for 0 hex", 0, Integer.parseInt("0",
                16));
        assertTrue("Returned incorrect value for most negative value hex",
                Integer.parseInt("-80000000", 16) == 0x80000000);
        assertTrue("Returned incorrect value for most positive value hex",
                Integer.parseInt("7fffffff", 16) == 0x7fffffff);
        assertEquals("Returned incorrect value for 0 decimal", 0, Integer.parseInt(
                "0", 10));
        assertTrue("Returned incorrect value for most negative value decimal",
                Integer.parseInt("-2147483648", 10) == 0x80000000);
        assertTrue("Returned incorrect value for most positive value decimal",
                Integer.parseInt("2147483647", 10) == 0x7fffffff);

        boolean exception = false;
        try {
            Integer.parseInt("FFFF", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue(
                "Failed to throw exception when passes hex string and dec parm",
                exception);

        exception = false;
        try {
            Integer.parseInt("2147483648", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Integer.parseInt("-2147483649", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

        exception = false;
        try {
            Integer.parseInt("80000000", 16);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

        exception = false;
        try {
            Integer.parseInt("-80000001", 16);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MIN_VALUE + 1", exception);

        exception = false;
        try {
            Integer.parseInt("9999999999", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for 9999999999", exception);
    }

    /**
     * java.lang.Integer#shortValue()
     */
    public void test_shortValue2() {
        // Test for method short java.lang.Integer.shortValue()
        Integer i = new Integer(2147450880);
        assertEquals("Returned incorrect long value", -32768, i.shortValue());
    }

    /**
     * java.lang.Integer#toBinaryString(int)
     */
    public void test_toBinaryStringI() {
        // Test for method java.lang.String
        // java.lang.Integer.toBinaryString(int)
        assertEquals("Incorrect string returned", "1111111111111111111111111111111", Integer.toBinaryString(
                Integer.MAX_VALUE));
        assertEquals("Incorrect string returned", "10000000000000000000000000000000", Integer.toBinaryString(
                Integer.MIN_VALUE));
    }

    /**
     * java.lang.Integer#toHexString(int)
     */
    public void test_toHexStringI() {
        // Test for method java.lang.String java.lang.Integer.toHexString(int)

        String[] hexvals = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e", "f" };

        for (int i = 0; i < 16; i++) {
            assertTrue("Incorrect string returned " + hexvals[i], Integer
                    .toHexString(i).equals(hexvals[i]));
        }

        assertTrue("Returned incorrect hex string: "
                + Integer.toHexString(Integer.MAX_VALUE), Integer.toHexString(
                Integer.MAX_VALUE).equals("7fffffff"));
        assertTrue("Returned incorrect hex string: "
                + Integer.toHexString(Integer.MIN_VALUE), Integer.toHexString(
                Integer.MIN_VALUE).equals("80000000"));
    }

    /**
     * java.lang.Integer#toOctalString(int)
     */
    public void test_toOctalStringI() {
        // Test for method java.lang.String java.lang.Integer.toOctalString(int)
        // Spec states that the int arg is treated as unsigned
        assertEquals("Returned incorrect octal string", "17777777777", Integer.toOctalString(
                Integer.MAX_VALUE));
        assertEquals("Returned incorrect octal string", "20000000000", Integer.toOctalString(
                Integer.MIN_VALUE));
    }

    /**
     * java.lang.Integer#toString()
     */
    public void test_toString2() {
        // Test for method java.lang.String java.lang.Integer.toString()

        Integer i = new Integer(-80001);

        assertEquals("Returned incorrect String", "-80001", i.toString());
    }

    /**
     * java.lang.Integer#toString(int)
     */
    public void test_toStringI2() {
        // Test for method java.lang.String java.lang.Integer.toString(int)

        assertEquals("Returned incorrect String", "-80765", Integer.toString(-80765)
        );
        assertEquals("Returned incorrect octal string", "2147483647", Integer.toString(
                Integer.MAX_VALUE));
        assertEquals("Returned incorrect octal string", "-2147483647", Integer.toString(
                -Integer.MAX_VALUE));
        assertEquals("Returned incorrect octal string", "-2147483648", Integer.toString(
                Integer.MIN_VALUE));

        // Test for HARMONY-6068
        assertEquals("Returned incorrect octal String", "-1000", Integer.toString(-1000));
        assertEquals("Returned incorrect octal String", "1000", Integer.toString(1000));
        assertEquals("Returned incorrect octal String", "0", Integer.toString(0));
        assertEquals("Returned incorrect octal String", "708", Integer.toString(708));
        assertEquals("Returned incorrect octal String", "-100", Integer.toString(-100));
        assertEquals("Returned incorrect octal String", "-1000000008", Integer.toString(-1000000008));
        assertEquals("Returned incorrect octal String", "2000000008", Integer.toString(2000000008));
    }

    /**
     * java.lang.Integer#toString(int, int)
     */
    public void test_toStringII() {
        // Test for method java.lang.String java.lang.Integer.toString(int, int)
        assertEquals("Returned incorrect octal string", "17777777777", Integer.toString(
                2147483647, 8));
        assertTrue("Returned incorrect hex string--wanted 7fffffff but got: "
                + Integer.toString(2147483647, 16), Integer.toString(
                2147483647, 16).equals("7fffffff"));
        assertEquals("Incorrect string returned", "1111111111111111111111111111111", Integer.toString(2147483647, 2)
        );
        assertEquals("Incorrect string returned", "2147483647", Integer
                .toString(2147483647, 10));

        assertEquals("Returned incorrect octal string", "-17777777777", Integer.toString(
                -2147483647, 8));
        assertTrue("Returned incorrect hex string--wanted -7fffffff but got: "
                + Integer.toString(-2147483647, 16), Integer.toString(
                -2147483647, 16).equals("-7fffffff"));
        assertEquals("Incorrect string returned",
                "-1111111111111111111111111111111", Integer
                .toString(-2147483647, 2));
        assertEquals("Incorrect string returned", "-2147483647", Integer.toString(-2147483647,
                10));

        assertEquals("Returned incorrect octal string", "-20000000000", Integer.toString(
                -2147483648, 8));
        assertTrue("Returned incorrect hex string--wanted -80000000 but got: "
                + Integer.toString(-2147483648, 16), Integer.toString(
                -2147483648, 16).equals("-80000000"));
        assertEquals("Incorrect string returned",
                "-10000000000000000000000000000000", Integer
                .toString(-2147483648, 2));
        assertEquals("Incorrect string returned", "-2147483648", Integer.toString(-2147483648,
                10));
    }

    /**
     * java.lang.Integer#valueOf(java.lang.String)
     */
    public void test_valueOfLjava_lang_String2() {
        // Test for method java.lang.Integer
        // java.lang.Integer.valueOf(java.lang.String)
        assertEquals("Returned incorrect int", 8888888, Integer.valueOf("8888888")
                .intValue());
        assertTrue("Returned incorrect int", Integer.valueOf("2147483647")
                .intValue() == Integer.MAX_VALUE);
        assertTrue("Returned incorrect int", Integer.valueOf("-2147483648")
                .intValue() == Integer.MIN_VALUE);

        boolean exception = false;
        try {
            Integer.valueOf("2147483648");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception with MAX_VALUE + 1", exception);

        exception = false;
        try {
            Integer.valueOf("-2147483649");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception with MIN_VALUE - 1", exception);
    }

    /**
     * java.lang.Integer#valueOf(java.lang.String, int)
     */
    public void test_valueOfLjava_lang_StringI2() {
        // Test for method java.lang.Integer
        // java.lang.Integer.valueOf(java.lang.String, int)
        assertEquals("Returned incorrect int for hex string", 255, Integer.valueOf(
                "FF", 16).intValue());
        assertEquals("Returned incorrect int for oct string", 16, Integer.valueOf(
                "20", 8).intValue());
        assertEquals("Returned incorrect int for bin string", 4, Integer.valueOf(
                "100", 2).intValue());

        assertEquals("Returned incorrect int for - hex string", -255, Integer.valueOf(
                "-FF", 16).intValue());
        assertEquals("Returned incorrect int for - oct string", -16, Integer.valueOf(
                "-20", 8).intValue());
        assertEquals("Returned incorrect int for - bin string", -4, Integer.valueOf(
                "-100", 2).intValue());
        assertTrue("Returned incorrect int", Integer.valueOf("2147483647", 10)
                .intValue() == Integer.MAX_VALUE);
        assertTrue("Returned incorrect int", Integer.valueOf("-2147483648", 10)
                .intValue() == Integer.MIN_VALUE);
        assertTrue("Returned incorrect int", Integer.valueOf("7fffffff", 16)
                .intValue() == Integer.MAX_VALUE);
        assertTrue("Returned incorrect int", Integer.valueOf("-80000000", 16)
                .intValue() == Integer.MIN_VALUE);

        boolean exception = false;
        try {
            Integer.valueOf("FF", 2);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue(
                "Failed to throw exception with hex string and base 2 radix",
                exception);

        exception = false;
        try {
            Integer.valueOf("2147483648", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception with MAX_VALUE + 1", exception);

        exception = false;
        try {
            Integer.valueOf("-2147483649", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception with MIN_VALUE - 1", exception);

        exception = false;
        try {
            Integer.valueOf("80000000", 16);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception with hex MAX_VALUE + 1",
                exception);

        exception = false;
        try {
            Integer.valueOf("-80000001", 16);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception with hex MIN_VALUE - 1",
                exception);
    }

    /**
     * java.lang.Integer#valueOf(byte)
     */
    public void test_valueOfI() {
        assertEquals(new Integer(Integer.MIN_VALUE), Integer.valueOf(Integer.MIN_VALUE));
        assertEquals(new Integer(Integer.MAX_VALUE), Integer.valueOf(Integer.MAX_VALUE));
        assertEquals(new Integer(0), Integer.valueOf(0));

        short s = -128;
        while (s < 128) {
            assertEquals(new Integer(s), Integer.valueOf(s));
            assertSame(Integer.valueOf(s), Integer.valueOf(s));
            s++;
        }
    }

    /**
     * java.lang.Integer#hashCode()
     */
    public void test_hashCode() {
        assertEquals(1, new Integer(1).hashCode());
        assertEquals(2, new Integer(2).hashCode());
        assertEquals(0, new Integer(0).hashCode());
        assertEquals(-1, new Integer(-1).hashCode());
    }

    /**
     * java.lang.Integer#Integer(String)
     */
    public void test_ConstructorLjava_lang_String() {
        assertEquals(new Integer(0), new Integer("0"));
        assertEquals(new Integer(1), new Integer("1"));
        assertEquals(new Integer(-1), new Integer("-1"));

        try {
            new Integer("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Integer("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Integer("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Integer(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Integer#Integer
     */
    public void test_ConstructorI() {
        assertEquals(1, new Integer(1).intValue());
        assertEquals(2, new Integer(2).intValue());
        assertEquals(0, new Integer(0).intValue());
        assertEquals(-1, new Integer(-1).intValue());

        Integer i = new Integer(-89000);
        assertEquals("Incorrect Integer created", -89000, i.intValue());
    }

    /**
     * java.lang.Integer#byteValue()
     */
    public void test_booleanValue() {
        assertEquals(1, new Integer(1).byteValue());
        assertEquals(2, new Integer(2).byteValue());
        assertEquals(0, new Integer(0).byteValue());
        assertEquals(-1, new Integer(-1).byteValue());
    }

    /**
     * java.lang.Integer#equals(Object)
     */
    public void test_equalsLjava_lang_Object() {
        assertEquals(new Integer(0), Integer.valueOf(0));
        assertEquals(new Integer(1), Integer.valueOf(1));
        assertEquals(new Integer(-1), Integer.valueOf(-1));

        Integer fixture = new Integer(25);
        assertEquals(fixture, fixture);
        assertFalse(fixture.equals(null));
        assertFalse(fixture.equals("Not a Integer"));
    }

    /**
     * java.lang.Integer#toString()
     */
    public void test_toString() {
        assertEquals("-1", new Integer(-1).toString());
        assertEquals("0", new Integer(0).toString());
        assertEquals("1", new Integer(1).toString());
        assertEquals("-1", new Integer(0xFFFFFFFF).toString());
    }

    /**
     * java.lang.Integer#toString
     */
    public void test_toStringI() {
        assertEquals("-1", Integer.toString(-1));
        assertEquals("0", Integer.toString(0));
        assertEquals("1", Integer.toString(1));
        assertEquals("-1", Integer.toString(0xFFFFFFFF));
    }

    /**
     * java.lang.Integer#valueOf(String)
     */
    public void test_valueOfLjava_lang_String() {
        assertEquals(new Integer(0), Integer.valueOf("0"));
        assertEquals(new Integer(1), Integer.valueOf("1"));
        assertEquals(new Integer(-1), Integer.valueOf("-1"));

        try {
            Integer.valueOf("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.valueOf("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.valueOf("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.valueOf(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Integer#valueOf(String, int)
     */
    public void test_valueOfLjava_lang_StringI() {
        assertEquals(new Integer(0), Integer.valueOf("0", 10));
        assertEquals(new Integer(1), Integer.valueOf("1", 10));
        assertEquals(new Integer(-1), Integer.valueOf("-1", 10));

        //must be consistent with Character.digit()
        assertEquals(Character.digit('1', 2), Integer.valueOf("1", 2).byteValue());
        assertEquals(Character.digit('F', 16), Integer.valueOf("F", 16).byteValue());

        try {
            Integer.valueOf("0x1", 10);
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.valueOf("9.2", 10);
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.valueOf("", 10);
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.valueOf(null, 10);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Integer#parseInt(String)
     */
    public void test_parseIntLjava_lang_String() {
        assertEquals(0, Integer.parseInt("0"));
        assertEquals(1, Integer.parseInt("1"));
        assertEquals(-1, Integer.parseInt("-1"));

        try {
            Integer.parseInt("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.parseInt("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.parseInt("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.parseInt(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Integer#parseInt(String, int)
     */
    public void test_parseIntLjava_lang_StringI() {
        assertEquals(0, Integer.parseInt("0", 10));
        assertEquals(1, Integer.parseInt("1", 10));
        assertEquals(-1, Integer.parseInt("-1", 10));

        //must be consistent with Character.digit()
        assertEquals(Character.digit('1', 2), Integer.parseInt("1", 2));
        assertEquals(Character.digit('F', 16), Integer.parseInt("F", 16));

        try {
            Integer.parseInt("0x1", 10);
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.parseInt("9.2", 10);
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.parseInt("", 10);
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.parseInt(null, 10);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * java.lang.Integer#decode(String)
     */
    public void test_decodeLjava_lang_String() {
        assertEquals(new Integer(0), Integer.decode("0"));
        assertEquals(new Integer(1), Integer.decode("1"));
        assertEquals(new Integer(-1), Integer.decode("-1"));
        assertEquals(new Integer(0xF), Integer.decode("0xF"));
        assertEquals(new Integer(0xF), Integer.decode("#F"));
        assertEquals(new Integer(0xF), Integer.decode("0XF"));
        assertEquals(new Integer(07), Integer.decode("07"));

        try {
            Integer.decode("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.decode("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Integer.decode(null);
            //undocumented NPE, but seems consistent across JREs
            fail("Expected NullPointerException with null string.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * java.lang.Integer#doubleValue()
     */
    public void test_doubleValue() {
        assertEquals(-1D, new Integer(-1).doubleValue(), 0D);
        assertEquals(0D, new Integer(0).doubleValue(), 0D);
        assertEquals(1D, new Integer(1).doubleValue(), 0D);
    }

    /**
     * java.lang.Integer#floatValue()
     */
    public void test_floatValue() {
        assertEquals(-1F, new Integer(-1).floatValue(), 0F);
        assertEquals(0F, new Integer(0).floatValue(), 0F);
        assertEquals(1F, new Integer(1).floatValue(), 0F);
    }

    /**
     * java.lang.Integer#intValue()
     */
    public void test_intValue() {
        assertEquals(-1, new Integer(-1).intValue());
        assertEquals(0, new Integer(0).intValue());
        assertEquals(1, new Integer(1).intValue());
    }

    /**
     * java.lang.Integer#longValue()
     */
    public void test_longValue() {
        assertEquals(-1L, new Integer(-1).longValue());
        assertEquals(0L, new Integer(0).longValue());
        assertEquals(1L, new Integer(1).longValue());
    }

    /**
     * java.lang.Integer#shortValue()
     */
    public void test_shortValue() {
        assertEquals(-1, new Integer(-1).shortValue());
        assertEquals(0, new Integer(0).shortValue());
        assertEquals(1, new Integer(1).shortValue());
    }

    /**
     * java.lang.Integer#highestOneBit(int)
     */
    public void test_highestOneBitI() {
        assertEquals(0x08, Integer.highestOneBit(0x0A));
        assertEquals(0x08, Integer.highestOneBit(0x0B));
        assertEquals(0x08, Integer.highestOneBit(0x0C));
        assertEquals(0x08, Integer.highestOneBit(0x0F));
        assertEquals(0x80, Integer.highestOneBit(0xFF));

        assertEquals(0x080000, Integer.highestOneBit(0x0F1234));
        assertEquals(0x800000, Integer.highestOneBit(0xFF9977));

        assertEquals(0x80000000, Integer.highestOneBit(0xFFFFFFFF));

        assertEquals(0, Integer.highestOneBit(0));
        assertEquals(1, Integer.highestOneBit(1));
        assertEquals(0x80000000, Integer.highestOneBit(-1));
    }

    /**
     * java.lang.Integer#lowestOneBit(int)
     */
    public void test_lowestOneBitI() {
        assertEquals(0x10, Integer.lowestOneBit(0xF0));

        assertEquals(0x10, Integer.lowestOneBit(0x90));
        assertEquals(0x10, Integer.lowestOneBit(0xD0));

        assertEquals(0x10, Integer.lowestOneBit(0x123490));
        assertEquals(0x10, Integer.lowestOneBit(0x1234D0));

        assertEquals(0x100000, Integer.lowestOneBit(0x900000));
        assertEquals(0x100000, Integer.lowestOneBit(0xD00000));

        assertEquals(0x40, Integer.lowestOneBit(0x40));
        assertEquals(0x40, Integer.lowestOneBit(0xC0));

        assertEquals(0x4000, Integer.lowestOneBit(0x4000));
        assertEquals(0x4000, Integer.lowestOneBit(0xC000));

        assertEquals(0x4000, Integer.lowestOneBit(0x99994000));
        assertEquals(0x4000, Integer.lowestOneBit(0x9999C000));

        assertEquals(0, Integer.lowestOneBit(0));
        assertEquals(1, Integer.lowestOneBit(1));
        assertEquals(1, Integer.lowestOneBit(-1));
    }

    /**
     * java.lang.Integer#numberOfLeadingZeros(int)
     */
    public void test_numberOfLeadingZerosI() {
        assertEquals(32, Integer.numberOfLeadingZeros(0x0));
        assertEquals(31, Integer.numberOfLeadingZeros(0x1));
        assertEquals(30, Integer.numberOfLeadingZeros(0x2));
        assertEquals(30, Integer.numberOfLeadingZeros(0x3));
        assertEquals(29, Integer.numberOfLeadingZeros(0x4));
        assertEquals(29, Integer.numberOfLeadingZeros(0x5));
        assertEquals(29, Integer.numberOfLeadingZeros(0x6));
        assertEquals(29, Integer.numberOfLeadingZeros(0x7));
        assertEquals(28, Integer.numberOfLeadingZeros(0x8));
        assertEquals(28, Integer.numberOfLeadingZeros(0x9));
        assertEquals(28, Integer.numberOfLeadingZeros(0xA));
        assertEquals(28, Integer.numberOfLeadingZeros(0xB));
        assertEquals(28, Integer.numberOfLeadingZeros(0xC));
        assertEquals(28, Integer.numberOfLeadingZeros(0xD));
        assertEquals(28, Integer.numberOfLeadingZeros(0xE));
        assertEquals(28, Integer.numberOfLeadingZeros(0xF));
        assertEquals(27, Integer.numberOfLeadingZeros(0x10));
        assertEquals(24, Integer.numberOfLeadingZeros(0x80));
        assertEquals(24, Integer.numberOfLeadingZeros(0xF0));
        assertEquals(23, Integer.numberOfLeadingZeros(0x100));
        assertEquals(20, Integer.numberOfLeadingZeros(0x800));
        assertEquals(20, Integer.numberOfLeadingZeros(0xF00));
        assertEquals(19, Integer.numberOfLeadingZeros(0x1000));
        assertEquals(16, Integer.numberOfLeadingZeros(0x8000));
        assertEquals(16, Integer.numberOfLeadingZeros(0xF000));
        assertEquals(15, Integer.numberOfLeadingZeros(0x10000));
        assertEquals(12, Integer.numberOfLeadingZeros(0x80000));
        assertEquals(12, Integer.numberOfLeadingZeros(0xF0000));
        assertEquals(11, Integer.numberOfLeadingZeros(0x100000));
        assertEquals(8, Integer.numberOfLeadingZeros(0x800000));
        assertEquals(8, Integer.numberOfLeadingZeros(0xF00000));
        assertEquals(7, Integer.numberOfLeadingZeros(0x1000000));
        assertEquals(4, Integer.numberOfLeadingZeros(0x8000000));
        assertEquals(4, Integer.numberOfLeadingZeros(0xF000000));
        assertEquals(3, Integer.numberOfLeadingZeros(0x10000000));
        assertEquals(0, Integer.numberOfLeadingZeros(0x80000000));
        assertEquals(0, Integer.numberOfLeadingZeros(0xF0000000));

        assertEquals(1, Integer.numberOfLeadingZeros(Integer.MAX_VALUE));
        assertEquals(0, Integer.numberOfLeadingZeros(Integer.MIN_VALUE));
    }

    /**
     * java.lang.Integer#numberOfTrailingZeros(int)
     */
    public void test_numberOfTrailingZerosI() {
        assertEquals(32, Integer.numberOfTrailingZeros(0x0));
        assertEquals(31, Integer.numberOfTrailingZeros(Integer.MIN_VALUE));
        assertEquals(0, Integer.numberOfTrailingZeros(Integer.MAX_VALUE));

        assertEquals(0, Integer.numberOfTrailingZeros(0x1));
        assertEquals(3, Integer.numberOfTrailingZeros(0x8));
        assertEquals(0, Integer.numberOfTrailingZeros(0xF));

        assertEquals(4, Integer.numberOfTrailingZeros(0x10));
        assertEquals(7, Integer.numberOfTrailingZeros(0x80));
        assertEquals(4, Integer.numberOfTrailingZeros(0xF0));

        assertEquals(8, Integer.numberOfTrailingZeros(0x100));
        assertEquals(11, Integer.numberOfTrailingZeros(0x800));
        assertEquals(8, Integer.numberOfTrailingZeros(0xF00));

        assertEquals(12, Integer.numberOfTrailingZeros(0x1000));
        assertEquals(15, Integer.numberOfTrailingZeros(0x8000));
        assertEquals(12, Integer.numberOfTrailingZeros(0xF000));

        assertEquals(16, Integer.numberOfTrailingZeros(0x10000));
        assertEquals(19, Integer.numberOfTrailingZeros(0x80000));
        assertEquals(16, Integer.numberOfTrailingZeros(0xF0000));

        assertEquals(20, Integer.numberOfTrailingZeros(0x100000));
        assertEquals(23, Integer.numberOfTrailingZeros(0x800000));
        assertEquals(20, Integer.numberOfTrailingZeros(0xF00000));

        assertEquals(24, Integer.numberOfTrailingZeros(0x1000000));
        assertEquals(27, Integer.numberOfTrailingZeros(0x8000000));
        assertEquals(24, Integer.numberOfTrailingZeros(0xF000000));

        assertEquals(28, Integer.numberOfTrailingZeros(0x10000000));
        assertEquals(31, Integer.numberOfTrailingZeros(0x80000000));
        assertEquals(28, Integer.numberOfTrailingZeros(0xF0000000));
    }

    /**
     * java.lang.Integer#bitCount(int)
     */
    public void test_bitCountI() {
        assertEquals(0, Integer.bitCount(0x0));
        assertEquals(1, Integer.bitCount(0x1));
        assertEquals(1, Integer.bitCount(0x2));
        assertEquals(2, Integer.bitCount(0x3));
        assertEquals(1, Integer.bitCount(0x4));
        assertEquals(2, Integer.bitCount(0x5));
        assertEquals(2, Integer.bitCount(0x6));
        assertEquals(3, Integer.bitCount(0x7));
        assertEquals(1, Integer.bitCount(0x8));
        assertEquals(2, Integer.bitCount(0x9));
        assertEquals(2, Integer.bitCount(0xA));
        assertEquals(3, Integer.bitCount(0xB));
        assertEquals(2, Integer.bitCount(0xC));
        assertEquals(3, Integer.bitCount(0xD));
        assertEquals(3, Integer.bitCount(0xE));
        assertEquals(4, Integer.bitCount(0xF));

        assertEquals(8, Integer.bitCount(0xFF));
        assertEquals(12, Integer.bitCount(0xFFF));
        assertEquals(16, Integer.bitCount(0xFFFF));
        assertEquals(20, Integer.bitCount(0xFFFFF));
        assertEquals(24, Integer.bitCount(0xFFFFFF));
        assertEquals(28, Integer.bitCount(0xFFFFFFF));
        assertEquals(32, Integer.bitCount(0xFFFFFFFF));
    }

    /**
     * java.lang.Integer#rotateLeft(int, int)
     */
    public void test_rotateLeftII() {
        assertEquals(0xF, Integer.rotateLeft(0xF, 0));
        assertEquals(0xF0, Integer.rotateLeft(0xF, 4));
        assertEquals(0xF00, Integer.rotateLeft(0xF, 8));
        assertEquals(0xF000, Integer.rotateLeft(0xF, 12));
        assertEquals(0xF0000, Integer.rotateLeft(0xF, 16));
        assertEquals(0xF00000, Integer.rotateLeft(0xF, 20));
        assertEquals(0xF000000, Integer.rotateLeft(0xF, 24));
        assertEquals(0xF0000000, Integer.rotateLeft(0xF, 28));
        assertEquals(0xF0000000, Integer.rotateLeft(0xF0000000, 32));
    }

    /**
     * java.lang.Integer#rotateRight(int, int)
     */
    public void test_rotateRightII() {
        assertEquals(0xF, Integer.rotateRight(0xF0, 4));
        assertEquals(0xF, Integer.rotateRight(0xF00, 8));
        assertEquals(0xF, Integer.rotateRight(0xF000, 12));
        assertEquals(0xF, Integer.rotateRight(0xF0000, 16));
        assertEquals(0xF, Integer.rotateRight(0xF00000, 20));
        assertEquals(0xF, Integer.rotateRight(0xF000000, 24));
        assertEquals(0xF, Integer.rotateRight(0xF0000000, 28));
        assertEquals(0xF0000000, Integer.rotateRight(0xF0000000, 32));
        assertEquals(0xF0000000, Integer.rotateRight(0xF0000000, 0));

    }

    /**
     * java.lang.Integer#reverseBytes(int)
     */
    public void test_reverseBytesI() {
        assertEquals(0xAABBCCDD, Integer.reverseBytes(0xDDCCBBAA));
        assertEquals(0x11223344, Integer.reverseBytes(0x44332211));
        assertEquals(0x00112233, Integer.reverseBytes(0x33221100));
        assertEquals(0x20000002, Integer.reverseBytes(0x02000020));
    }

    /**
     * java.lang.Integer#reverse(int)
     */
    public void test_reverseI() {
        assertEquals(-1, Integer.reverse(-1));
        assertEquals(0x80000000, Integer.reverse(1));
    }

    /**
     * java.lang.Integer#signum(int)
     */
    public void test_signumI() {
        for (int i = -128; i < 0; i++) {
            assertEquals(-1, Integer.signum(i));
        }
        assertEquals(0, Integer.signum(0));
        for (int i = 1; i <= 127; i++) {
            assertEquals(1, Integer.signum(i));
        }
    }
}
