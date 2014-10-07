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

package org.apache.harmony.luni.tests.java.lang;

import junit.framework.TestCase;

public class ShortTest extends TestCase {
    private Short sp = new Short((short) 18000);
    private Short sn = new Short((short) -19000);

    /**
     * @tests java.lang.Short#byteValue()
     */
    public void test_byteValue() {
        // Test for method byte java.lang.Short.byteValue()
        assertEquals("Returned incorrect byte value", 0, new Short(Short.MIN_VALUE)
                .byteValue());
        assertEquals("Returned incorrect byte value", -1, new Short(Short.MAX_VALUE)
                .byteValue());
    }

    /**
     * @tests java.lang.Short#compareTo(java.lang.Short)
     */
    public void test_compareToLjava_lang_Short() {
        // Test for method int java.lang.Short.compareTo(java.lang.Short)
        Short s = new Short((short) 1);
        Short x = new Short((short) 3);
        assertTrue(
                "Should have returned negative value when compared to greater short",
                s.compareTo(x) < 0);
        x = new Short((short) -1);
        assertTrue(
                "Should have returned positive value when compared to lesser short",
                s.compareTo(x) > 0);
        x = new Short((short) 1);
        assertEquals("Should have returned zero when compared to equal short",
                             0, s.compareTo(x));
        
        try {
            new Short((short)0).compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.lang.Short#decode(java.lang.String)
     */
    public void test_decodeLjava_lang_String2() {
        // Test for method java.lang.Short
        // java.lang.Short.decode(java.lang.String)
        assertTrue("Did not decode -1 correctly", Short.decode("-1")
                .shortValue() == (short) -1);
        assertTrue("Did not decode -100 correctly", Short.decode("-100")
                .shortValue() == (short) -100);
        assertTrue("Did not decode 23 correctly", Short.decode("23")
                .shortValue() == (short) 23);
        assertTrue("Did not decode 0x10 correctly", Short.decode("0x10")
                .shortValue() == (short) 16);
        assertTrue("Did not decode 32767 correctly", Short.decode("32767")
                .shortValue() == (short) 32767);
        assertTrue("Did not decode -32767 correctly", Short.decode("-32767")
                .shortValue() == (short) -32767);
        assertTrue("Did not decode -32768 correctly", Short.decode("-32768")
                .shortValue() == (short) -32768);

        boolean exception = false;
        try {
            Short.decode("123s");
        } catch (NumberFormatException e) {
            // correct
            exception = true;
        }
        assertTrue("Did not throw NumberFormatException decoding 123s",
                exception);

        exception = false;
        try {
            Short.decode("32768");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Short.decode("-32769");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

        exception = false;
        try {
            Short.decode("0x8000");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

        exception = false;
        try {
            Short.decode("-0x8001");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MIN_VALUE - 1", exception);
    }

    /**
     * @tests java.lang.Short#parseShort(java.lang.String)
     */
    public void test_parseShortLjava_lang_String2() {
        // Test for method short java.lang.Short.parseShort(java.lang.String)
        short sp = Short.parseShort("32746");
        short sn = Short.parseShort("-32746");

        assertTrue("Incorrect parse of short", sp == (short) 32746
                && (sn == (short) -32746));
        assertEquals("Returned incorrect value for 0", 0, Short.parseShort("0"));
        assertTrue("Returned incorrect value for most negative value", Short
                .parseShort("-32768") == (short) 0x8000);
        assertTrue("Returned incorrect value for most positive value", Short
                .parseShort("32767") == 0x7fff);

        boolean exception = false;
        try {
            Short.parseShort("32768");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Short.parseShort("-32769");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);
    }

    /**
     * @tests java.lang.Short#parseShort(java.lang.String, int)
     */
    public void test_parseShortLjava_lang_StringI2() {
        // Test for method short java.lang.Short.parseShort(java.lang.String,
        // int)
        boolean aThrow = true;
        assertEquals("Incorrectly parsed hex string",
                255, Short.parseShort("FF", 16));
        assertEquals("Incorrectly parsed oct string",
                16, Short.parseShort("20", 8));
        assertEquals("Incorrectly parsed dec string",
                20, Short.parseShort("20", 10));
        assertEquals("Incorrectly parsed bin string",
                4, Short.parseShort("100", 2));
        assertEquals("Incorrectly parsed -hex string", -255, Short
                .parseShort("-FF", 16));
        assertEquals("Incorrectly parsed -oct string",
                -16, Short.parseShort("-20", 8));
        assertEquals("Incorrectly parsed -bin string", -4, Short
                .parseShort("-100", 2));
        assertEquals("Returned incorrect value for 0 hex", 0, Short.parseShort("0",
                16));
        assertTrue("Returned incorrect value for most negative value hex",
                Short.parseShort("-8000", 16) == (short) 0x8000);
        assertTrue("Returned incorrect value for most positive value hex",
                Short.parseShort("7fff", 16) == 0x7fff);
        assertEquals("Returned incorrect value for 0 decimal", 0, Short.parseShort(
                "0", 10));
        assertTrue("Returned incorrect value for most negative value decimal",
                Short.parseShort("-32768", 10) == (short) 0x8000);
        assertTrue("Returned incorrect value for most positive value decimal",
                Short.parseShort("32767", 10) == 0x7fff);

        try {
            Short.parseShort("FF", 2);
        } catch (NumberFormatException e) {
            // Correct
            aThrow = false;
        }
        if (aThrow) {
            fail(
                    "Failed to throw exception when passed hex string and base 2 radix");
        }

        boolean exception = false;
        try {
            Short.parseShort("10000000000", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue(
                "Failed to throw exception when passed string larger than 16 bits",
                exception);

        exception = false;
        try {
            Short.parseShort("32768", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Short.parseShort("-32769", 10);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

        exception = false;
        try {
            Short.parseShort("8000", 16);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

        exception = false;
        try {
            Short.parseShort("-8001", 16);
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MIN_VALUE + 1", exception);
    }

    /**
     * @tests java.lang.Short#toString()
     */
    public void test_toString2() {
        // Test for method java.lang.String java.lang.Short.toString()
        assertTrue("Invalid string returned", sp.toString().equals("18000")
                && (sn.toString().equals("-19000")));
        assertEquals("Returned incorrect string", "32767", new Short((short) 32767)
                .toString());
        assertEquals("Returned incorrect string", "-32767", new Short((short) -32767)
                .toString());
        assertEquals("Returned incorrect string", "-32768", new Short((short) -32768)
                .toString());
    }

    /**
     * @tests java.lang.Short#toString(short)
     */
    public void test_toStringS2() {
        // Test for method java.lang.String java.lang.Short.toString(short)
        assertEquals("Returned incorrect string", "32767", Short.toString((short) 32767)
                );
        assertEquals("Returned incorrect string", "-32767", Short.toString((short) -32767)
                );
        assertEquals("Returned incorrect string", "-32768", Short.toString((short) -32768)
                );
    }

    /**
     * @tests java.lang.Short#valueOf(java.lang.String)
     */
    public void test_valueOfLjava_lang_String2() {
        // Test for method java.lang.Short
        // java.lang.Short.valueOf(java.lang.String)
        assertEquals("Returned incorrect short", -32768, Short.valueOf("-32768")
                .shortValue());
        assertEquals("Returned incorrect short", 32767, Short.valueOf("32767")
                .shortValue());
    }

    /**
     * @tests java.lang.Short#valueOf(java.lang.String, int)
     */
    public void test_valueOfLjava_lang_StringI2() {
        // Test for method java.lang.Short
        // java.lang.Short.valueOf(java.lang.String, int)
        boolean aThrow = true;
        assertEquals("Incorrectly parsed hex string", 255, Short.valueOf("FF", 16)
                .shortValue());
        assertEquals("Incorrectly parsed oct string", 16, Short.valueOf("20", 8)
                .shortValue());
        assertEquals("Incorrectly parsed dec string", 20, Short.valueOf("20", 10)
                .shortValue());
        assertEquals("Incorrectly parsed bin string", 4, Short.valueOf("100", 2)
                .shortValue());
        assertEquals("Incorrectly parsed -hex string", -255, Short.valueOf("-FF", 16)
                .shortValue());
        assertEquals("Incorrectly parsed -oct string", -16, Short.valueOf("-20", 8)
                .shortValue());
        assertEquals("Incorrectly parsed -bin string", -4, Short.valueOf("-100", 2)
                .shortValue());
        assertTrue("Did not decode 32767 correctly", Short.valueOf("32767", 10)
                .shortValue() == (short) 32767);
        assertTrue("Did not decode -32767 correctly", Short.valueOf("-32767",
                10).shortValue() == (short) -32767);
        assertTrue("Did not decode -32768 correctly", Short.valueOf("-32768",
                10).shortValue() == (short) -32768);
        try {
            Short.valueOf("FF", 2);
        } catch (NumberFormatException e) {
            // Correct
            aThrow = false;
        }
        if (aThrow) {
            fail(
                    "Failed to throw exception when passed hex string and base 2 radix");
        }
        try {
            Short.valueOf("10000000000", 10);
        } catch (NumberFormatException e) {
            // Correct
            return;
        }
        fail(
                "Failed to throw exception when passed string larger than 16 bits");
    }
	/**
	 * @tests java.lang.Short#valueOf(byte)
	 */
	public void test_valueOfS() {
		assertEquals(new Short(Short.MIN_VALUE), Short.valueOf(Short.MIN_VALUE));
		assertEquals(new Short(Short.MAX_VALUE), Short.valueOf(Short.MAX_VALUE));
		assertEquals(new Short((short) 0), Short.valueOf((short) 0));

		short s = -128;
		while (s < 128) {
			assertEquals(new Short(s), Short.valueOf(s));
			assertSame(Short.valueOf(s), Short.valueOf(s));
			s++;
		}
	}
    
    /**
     * @tests java.lang.Short#hashCode()
     */
    public void test_hashCode() {
        assertEquals(1, new Short((short)1).hashCode());
        assertEquals(2, new Short((short)2).hashCode());
        assertEquals(0, new Short((short)0).hashCode());
        assertEquals(-1, new Short((short)-1).hashCode());
    }

    /**
     * @tests java.lang.Short#Short(String)
     */
    public void test_ConstructorLjava_lang_String() {
        assertEquals(new Short((short)0), new Short("0"));
        assertEquals(new Short((short)1), new Short("1"));
        assertEquals(new Short((short)-1), new Short("-1"));
        
        try {
            new Short("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {}

        try {
            new Short("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {}

        try {
            new Short("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {}
        
        try {
            new Short(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {}
    }

    /**
     * @tests java.lang.Short#Short(short)
     */
    public void test_ConstructorS() {
        assertEquals(1, new Short((short)1).shortValue());
        assertEquals(2, new Short((short)2).shortValue());
        assertEquals(0, new Short((short)0).shortValue());
        assertEquals(-1, new Short((short)-1).shortValue());
    }

    /**
     * @tests java.lang.Short#byteValue()
     */
    public void test_booleanValue() {
        assertEquals(1, new Short((short)1).byteValue());    
        assertEquals(2, new Short((short)2).byteValue());
        assertEquals(0, new Short((short)0).byteValue());
        assertEquals(-1, new Short((short)-1).byteValue());
    }

    /**
     * @tests java.lang.Short#equals(Object)
     */
    public void test_equalsLjava_lang_Object() {
        assertEquals(new Short((short)0), Short.valueOf((short)0));
        assertEquals(new Short((short)1), Short.valueOf((short)1));
        assertEquals(new Short((short)-1), Short.valueOf((short)-1));
        
        Short fixture = new Short((short)25);
        assertEquals(fixture, fixture);
        assertFalse(fixture.equals(null));
        assertFalse(fixture.equals("Not a Short"));
    }

    /**
     * @tests java.lang.Short#toString()
     */
    public void test_toString() {
        assertEquals("-1", new Short((short)-1).toString());
        assertEquals("0", new Short((short)0).toString());
        assertEquals("1", new Short((short)1).toString());
        assertEquals("-1", new Short((short)0xFFFF).toString());
    }

    /**
     * @tests java.lang.Short#toString(short)
     */
    public void test_toStringS() {
        assertEquals("-1", Short.toString((short)-1));
        assertEquals("0", Short.toString((short)0));
        assertEquals("1", Short.toString((short)1));
        assertEquals("-1", Short.toString((short)0xFFFF));
    }

    /**
     * @tests java.lang.Short#valueOf(String)
     */
    public void test_valueOfLjava_lang_String() {
        assertEquals(new Short((short)0), Short.valueOf("0"));
        assertEquals(new Short((short)1), Short.valueOf("1"));
        assertEquals(new Short((short)-1), Short.valueOf("-1"));
        
        try {
            Short.valueOf("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {}

        try {
            Short.valueOf("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {}

        try {
            Short.valueOf("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {}
        
        try {
            Short.valueOf(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {}
    }
    
    /**
     * @tests java.lang.Short#valueOf(String,int)
     */
    public void test_valueOfLjava_lang_StringI() {
        assertEquals(new Short((short)0), Short.valueOf("0", 10));
        assertEquals(new Short((short)1), Short.valueOf("1", 10));
        assertEquals(new Short((short)-1), Short.valueOf("-1", 10));
        
        //must be consistent with Character.digit()
        assertEquals(Character.digit('1', 2), Short.valueOf("1", 2).byteValue());
        assertEquals(Character.digit('F', 16), Short.valueOf("F", 16).byteValue());
        
        try {
            Short.valueOf("0x1", 10);
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {}

        try {
            Short.valueOf("9.2", 10);
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {}

        try {
            Short.valueOf("", 10);
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {}
        
        try {
            Short.valueOf(null, 10);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {}
    }
    
    /**
     * @tests java.lang.Short#parseShort(String)
     */
    public void test_parseShortLjava_lang_String() {
        assertEquals(0, Short.parseShort("0"));
        assertEquals(1, Short.parseShort("1"));
        assertEquals(-1, Short.parseShort("-1"));
        
        try {
            Short.parseShort("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {}

        try {
            Short.parseShort("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {}

        try {
            Short.parseShort("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {}
        
        try {
            Short.parseShort(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {}
    }
    
    /**
     * @tests java.lang.Short#parseShort(String,int)
     */
    public void test_parseShortLjava_lang_StringI() {
        assertEquals(0, Short.parseShort("0", 10));
        assertEquals(1, Short.parseShort("1", 10));
        assertEquals(-1, Short.parseShort("-1", 10));
        
        //must be consistent with Character.digit()
        assertEquals(Character.digit('1', 2), Short.parseShort("1", 2));
        assertEquals(Character.digit('F', 16), Short.parseShort("F", 16));
        
        try {
            Short.parseShort("0x1", 10);
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {}

        try {
            Short.parseShort("9.2", 10);
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {}

        try {
            Short.parseShort("", 10);
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {}
        
        try {
            Short.parseShort(null, 10);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {}
    }
    
    /**
     * @tests java.lang.Short#decode(String)
     */
    public void test_decodeLjava_lang_String() {
        assertEquals(new Short((short)0), Short.decode("0"));
        assertEquals(new Short((short)1), Short.decode("1"));
        assertEquals(new Short((short)-1), Short.decode("-1"));
        assertEquals(new Short((short)0xF), Short.decode("0xF"));
        assertEquals(new Short((short)0xF), Short.decode("#F"));
        assertEquals(new Short((short)0xF), Short.decode("0XF"));
        assertEquals(new Short((short)07), Short.decode("07"));
        
        try {
            Short.decode("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {}

        try {
            Short.decode("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {}
        
        try {
            Short.decode(null);
            //undocumented NPE, but seems consistent across JREs
            fail("Expected NullPointerException with null string.");
        } catch (NullPointerException e) {}
    }
    
    /**
     * @tests java.lang.Short#doubleValue()
     */
    public void test_doubleValue() {
        assertEquals(-1D, new Short((short)-1).doubleValue(), 0D);
        assertEquals(0D, new Short((short)0).doubleValue(), 0D);
        assertEquals(1D, new Short((short)1).doubleValue(), 0D);
    }
    
    /**
     * @tests java.lang.Short#floatValue()
     */
    public void test_floatValue() {
        assertEquals(-1F, new Short((short)-1).floatValue(), 0F);
        assertEquals(0F, new Short((short)0).floatValue(), 0F);
        assertEquals(1F, new Short((short)1).floatValue(), 0F);
    }
    
    /**
     * @tests java.lang.Short#intValue()
     */
    public void test_intValue() {
        assertEquals(-1, new Short((short)-1).intValue());
        assertEquals(0, new Short((short)0).intValue());
        assertEquals(1, new Short((short)1).intValue());
    }
    
    /**
     * @tests java.lang.Short#longValue()
     */
    public void test_longValue() {
        assertEquals(-1L, new Short((short)-1).longValue());
        assertEquals(0L, new Short((short)0).longValue());
        assertEquals(1L, new Short((short)1).longValue());
    }
    
    /**
     * @tests java.lang.Short#shortValue()
     */
    public void test_shortValue() {
        assertEquals(-1, new Short((short)-1).shortValue());
        assertEquals(0, new Short((short)0).shortValue());
        assertEquals(1, new Short((short)1).shortValue());
    }
    
    /**
     * @tests java.lang.Short#reverseBytes(short)
     */
    public void test_reverseBytesS() {
        assertEquals((short)0xABCD, Short.reverseBytes((short)0xCDAB));
        assertEquals((short)0x1234, Short.reverseBytes((short)0x3412));
        assertEquals((short)0x0011, Short.reverseBytes((short)0x1100));
        assertEquals((short)0x2002, Short.reverseBytes((short)0x0220));
    }
    
}
