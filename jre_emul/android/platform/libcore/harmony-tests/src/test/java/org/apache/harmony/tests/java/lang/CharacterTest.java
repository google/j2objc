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

import java.util.Arrays;

import junit.framework.TestCase;

public class CharacterTest extends TestCase {

    // TODO(tball): uncomment tests of greater than 64k code points when iOS supports them.

    public void test_isValidCodePointI() {
        assertFalse(Character.isValidCodePoint(-1));
        assertTrue(Character.isValidCodePoint(0));
        assertTrue(Character.isValidCodePoint(1));
        assertFalse(Character.isValidCodePoint(Integer.MAX_VALUE));

        for (int c = '\u0000'; c <= 0x10FFFF; c++) {
            assertTrue(Character.isValidCodePoint(c));
        }

        assertFalse(Character.isValidCodePoint(0x10FFFF + 1));
    }

    public void test_isSupplementaryCodePointI() {
        assertFalse(Character.isSupplementaryCodePoint(-1));

        for (int c = '\u0000'; c <= '\uFFFF'; c++) {
            assertFalse(Character.isSupplementaryCodePoint(c));
        }

        for (int c = 0xFFFF + 1; c <= 0x10FFFF; c++) {
            assertTrue(Character.isSupplementaryCodePoint(c));
        }

        assertFalse(Character.isSupplementaryCodePoint(0x10FFFF + 1));
    }

    public void test_isHighSurrogateC() {
        // (\uD800-\uDBFF)
        assertFalse(Character.isHighSurrogate((char) ('\uD800' - 1)));
        for (int c = '\uD800'; c <= '\uDBFF'; c++) {
            assertTrue(Character.isHighSurrogate((char) c));
        }
        assertFalse(Character.isHighSurrogate((char) ('\uDBFF' + 1)));
        assertFalse(Character.isHighSurrogate('\uFFFF'));
    }

    public void test_isLowSurrogateC() {
        // (\uDC00-\uDFFF)
        assertFalse(Character.isLowSurrogate((char) ('\uDC00' - 1)));
        for (int c = '\uDC00'; c <= '\uDFFF'; c++) {
            assertTrue(Character.isLowSurrogate((char) c));
        }
        assertFalse(Character.isLowSurrogate((char) ('\uDFFF' + 1)));
    }

    public void test_isSurrogatePairCC() {
        assertFalse(Character.isSurrogatePair('\u0000', '\u0000'));
        assertFalse(Character.isSurrogatePair('\u0000', '\uDC00'));

        assertTrue(Character.isSurrogatePair('\uD800', '\uDC00'));
        assertTrue(Character.isSurrogatePair('\uD800', '\uDFFF'));
        assertTrue(Character.isSurrogatePair('\uDBFF', '\uDFFF'));

        assertFalse(Character.isSurrogatePair('\uDBFF', '\uF000'));
    }

    public void test_charCountI() {
        for (int c = '\u0000'; c <= '\uFFFF'; c++) {
            assertEquals(1, Character.charCount(c));
        }

        for (int c = 0xFFFF + 1; c <= 0x10FFFF; c++) {
            assertEquals(2, Character.charCount(c));
        }

        // invalid code points work in this method
        assertEquals(2, Character.charCount(Integer.MAX_VALUE));
    }

    public void test_toCodePointCC() {
        int result = Character.toCodePoint('\uD800', '\uDC00');
        assertEquals(0x00010000, result);

        result = Character.toCodePoint('\uD800', '\uDC01');
        assertEquals(0x00010001, result);

        result = Character.toCodePoint('\uD801', '\uDC01');
        assertEquals(0x00010401, result);

        result = Character.toCodePoint('\uDBFF', '\uDFFF');
        assertEquals(0x00010FFFF, result);
    }

    @SuppressWarnings("cast")
    public void test_codePointAtLjava_lang_CharSequenceI() {
        assertEquals('a', Character.codePointAt("abc", 0));
        assertEquals('b', Character.codePointAt("abc", 1));
        assertEquals('c', Character.codePointAt("abc", 2));
        assertEquals(0x10000, Character.codePointAt("\uD800\uDC00", 0));
        assertEquals('\uDC00', Character.codePointAt("\uD800\uDC00", 1));

        try {
            Character.codePointAt((CharSequence) null, 0);
            fail("No NPE.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointAt("abc", -1);
            fail("No IOOBE, negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointAt("abc", 4);
            fail("No IOOBE, index too large.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_codePointAt$CI() {
        assertEquals('a', Character.codePointAt("abc".toCharArray(), 0));
        assertEquals('b', Character.codePointAt("abc".toCharArray(), 1));
        assertEquals('c', Character.codePointAt("abc".toCharArray(), 2));
        assertEquals(0x10000, Character.codePointAt("\uD800\uDC00".toCharArray(), 0));
        assertEquals('\uDC00', Character.codePointAt("\uD800\uDC00".toCharArray(), 1));

        try {
            Character.codePointAt((char[]) null, 0);
            fail("No NPE.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointAt("abc".toCharArray(), -1);
            fail("No IOOBE, negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointAt("abc".toCharArray(), 4);
            fail("No IOOBE, index too large.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_codePointAt$CII() {
        assertEquals('a', Character.codePointAt("abc".toCharArray(), 0, 3));
        assertEquals('b', Character.codePointAt("abc".toCharArray(), 1, 3));
        assertEquals('c', Character.codePointAt("abc".toCharArray(), 2, 3));
        assertEquals(0x10000, Character.codePointAt("\uD800\uDC00".toCharArray(), 0, 2));
        assertEquals('\uDC00', Character.codePointAt("\uD800\uDC00".toCharArray(), 1, 2));
        assertEquals('\uD800', Character.codePointAt("\uD800\uDC00".toCharArray(), 0, 1));

        try {
            Character.codePointAt(null, 0, 1);
            fail("No NPE.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointAt("abc".toCharArray(), -1, 3);
            fail("No IOOBE, negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointAt("abc".toCharArray(), 4, 3);
            fail("No IOOBE, index too large.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointAt("abc".toCharArray(), 2, 1);
            fail("No IOOBE, index larger than limit.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointAt("abc".toCharArray(), 2, -1);
            fail("No IOOBE, limit is negative.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @SuppressWarnings("cast")
    public void test_codePointBeforeLjava_lang_CharSequenceI() {
        assertEquals('a', Character.codePointBefore("abc", 1));
        assertEquals('b', Character.codePointBefore("abc", 2));
        assertEquals('c', Character.codePointBefore("abc", 3));
        assertEquals(0x10000, Character.codePointBefore("\uD800\uDC00", 2));
        assertEquals('\uD800', Character.codePointBefore("\uD800\uDC00", 1));

        try {
            Character.codePointBefore((CharSequence) null, 0);
            fail("No NPE.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointBefore("abc", 0);
            fail("No IOOBE, index below one.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointBefore("abc", 4);
            fail("No IOOBE, index too large.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_codePointBefore$CI() {
        assertEquals('a', Character.codePointBefore("abc".toCharArray(), 1));
        assertEquals('b', Character.codePointBefore("abc".toCharArray(), 2));
        assertEquals('c', Character.codePointBefore("abc".toCharArray(), 3));
        assertEquals(0x10000, Character.codePointBefore("\uD800\uDC00".toCharArray(), 2));
        assertEquals('\uD800', Character.codePointBefore("\uD800\uDC00".toCharArray(), 1));

        try {
            Character.codePointBefore((char[]) null, 0);
            fail("No NPE.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointBefore("abc".toCharArray(), -1);
            fail("No IOOBE, negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointBefore("abc".toCharArray(), 4);
            fail("No IOOBE, index too large.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_codePointBefore$CII() {
        assertEquals('a', Character.codePointBefore("abc".toCharArray(), 1, 0));
        assertEquals('b', Character.codePointBefore("abc".toCharArray(), 2, 0));
        assertEquals('c', Character.codePointBefore("abc".toCharArray(), 3, 0));
        assertEquals(0x10000, Character.codePointBefore("\uD800\uDC00".toCharArray(), 2, 0));
        assertEquals('\uDC00', Character.codePointBefore("\uD800\uDC00".toCharArray(), 2, 1));
        assertEquals('\uD800', Character.codePointBefore("\uD800\uDC00".toCharArray(), 1, 0));

        try {
            Character.codePointBefore(null, 1, 0);
            fail("No NPE.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointBefore("abc".toCharArray(), 0, 1);
            fail("No IOOBE, index less than start.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointBefore("abc".toCharArray(), 4, 0);
            fail("No IOOBE, index larger than length.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointBefore("abc".toCharArray(), 2, -1);
            fail("No IOOBE, start is negative.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointBefore("abc".toCharArray(), 2, 4);
            fail("No IOOBE, start larger than length.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_toCharsI$CI() {
        char[] dst = new char[2];
        int result = Character.toChars(0x10000, dst, 0);
        assertEquals(2, result);
        assertTrue(Arrays.equals(new char[] { '\uD800', '\uDC00' }, dst));

        result = Character.toChars(0x10001, dst, 0);
        assertEquals(2, result);
        assertTrue(Arrays.equals(new char[] { '\uD800', '\uDC01' }, dst));

        result = Character.toChars(0x10401, dst, 0);
        assertEquals(2, result);
        assertTrue(Arrays.equals(new char[] { '\uD801', '\uDC01' }, dst));

        result = Character.toChars(0x10FFFF, dst, 0);
        assertEquals(2, result);
        assertTrue(Arrays.equals(new char[] { '\uDBFF', '\uDFFF' }, dst));

        try {
            Character.toChars(Integer.MAX_VALUE, new char[2], 0);
            fail("No IAE, invalid code point.");
        } catch (IllegalArgumentException e) {
        }

        try {
            Character.toChars('a', null, 0);
            fail("No NPE, null char[].");
        } catch (NullPointerException e) {
        }

        try {
            Character.toChars('a', new char[1], -1);
            fail("No IOOBE, negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.toChars('a', new char[1], 1);
            fail("No IOOBE, index equal to length.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_toCharsI() {
        assertTrue(Arrays.equals(new char[] { '\uD800', '\uDC00' }, Character.toChars(0x10000)));
        assertTrue(Arrays.equals(new char[] { '\uD800', '\uDC01' }, Character.toChars(0x10001)));
        assertTrue(Arrays.equals(new char[] { '\uD801', '\uDC01' }, Character.toChars(0x10401)));
        assertTrue(Arrays.equals(new char[] { '\uDBFF', '\uDFFF' }, Character.toChars(0x10FFFF)));

        try {
            Character.toChars(Integer.MAX_VALUE);
            fail("No IAE, invalid code point.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void test_codePointCountLjava_lang_CharSequenceII() {
        assertEquals(1, Character.codePointCount("\uD800\uDC00", 0, 2));
        assertEquals(1, Character.codePointCount("\uD800\uDC01", 0, 2));
        assertEquals(1, Character.codePointCount("\uD801\uDC01", 0, 2));
        assertEquals(1, Character.codePointCount("\uDBFF\uDFFF", 0, 2));

        assertEquals(3, Character.codePointCount("a\uD800\uDC00b", 0, 4));
        assertEquals(4, Character.codePointCount("a\uD800\uDC00b\uD800", 0, 5));

        try {
            Character.codePointCount((CharSequence) null, 0, 1);
            fail("No NPE, null char sequence.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointCount("abc", -1, 1);
            fail("No IOOBE, negative start.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointCount("abc", 0, 4);
            fail("No IOOBE, end greater than length.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointCount("abc", 2, 1);
            fail("No IOOBE, end greater than start.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_offsetByCodePointsLjava_lang_CharSequenceII() {
        int result = Character.offsetByCodePoints("a\uD800\uDC00b", 0, 2);
        assertEquals(3, result);

        result = Character.offsetByCodePoints("abcd", 3, -1);
        assertEquals(2, result);

        result = Character.offsetByCodePoints("a\uD800\uDC00b", 0, 3);
        assertEquals(4, result);

        result = Character.offsetByCodePoints("a\uD800\uDC00b", 3, -1);
        assertEquals(1, result);

        result = Character.offsetByCodePoints("a\uD800\uDC00b", 3, 0);
        assertEquals(3, result);

        result = Character.offsetByCodePoints("\uD800\uDC00bc", 3, 0);
        assertEquals(3, result);

        result = Character.offsetByCodePoints("a\uDC00bc", 3, -1);
        assertEquals(2, result);

        result = Character.offsetByCodePoints("a\uD800bc", 3, -1);
        assertEquals(2, result);

        try {
            Character.offsetByCodePoints(null, 0, 1);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            Character.offsetByCodePoints("abc", -1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abc", 4, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abc", 1, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abc", 1, -2);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_offsetByCodePoints$CIIII() {
        int result = Character.offsetByCodePoints("a\uD800\uDC00b".toCharArray(), 0, 4, 0, 2);
        assertEquals(3, result);

        result = Character.offsetByCodePoints("a\uD800\uDC00b".toCharArray(), 0, 4, 0, 3);
        assertEquals(4, result);

        result = Character.offsetByCodePoints("a\uD800\uDC00b\uD800c".toCharArray(), 0, 5, 0, 3);
        assertEquals(4, result);

        result = Character.offsetByCodePoints("abcd".toCharArray(), 0, 4, 3, -1);
        assertEquals(2, result);

        result = Character.offsetByCodePoints("abcd".toCharArray(), 1, 2, 3, -2);
        assertEquals(1, result);

        result = Character.offsetByCodePoints("a\uD800\uDC00b".toCharArray(), 0, 4, 3, -1);
        assertEquals(1, result);

        result = Character.offsetByCodePoints("a\uD800\uDC00b".toCharArray(), 0, 2, 2, -1);
        assertEquals(1, result);

        result = Character.offsetByCodePoints("a\uD800\uDC00b".toCharArray(), 0, 4, 3, 0);
        assertEquals(3, result);

        result = Character.offsetByCodePoints("\uD800\uDC00bc".toCharArray(), 0, 4, 3, 0);
        assertEquals(3, result);

        result = Character.offsetByCodePoints("a\uDC00bc".toCharArray(), 0, 4, 3, -1);
        assertEquals(2, result);

        result = Character.offsetByCodePoints("a\uD800bc".toCharArray(), 0, 4, 3, -1);
        assertEquals(2, result);

        try {
            Character.offsetByCodePoints(null, 0, 4, 1, 1);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            Character.offsetByCodePoints("abcd".toCharArray(), -1, 4, 1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abcd".toCharArray(), 0, -1, 1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abcd".toCharArray(), 2, 4, 1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abcd".toCharArray(), 1, 3, 0, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abcd".toCharArray(), 1, 1, 3, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abc".toCharArray(), 0, 3, 1, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abc".toCharArray(), 0, 2, 1, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.offsetByCodePoints("abc".toCharArray(), 1, 3, 1, -2);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * java.lang.Character#compareTo(Character)
     */
    public void test_compareToLjava_lang_Byte() {
        final Character min = new Character(Character.MIN_VALUE);
        final Character mid = new Character((char) (Character.MAX_VALUE / 2));
        final Character max = new Character(Character.MAX_VALUE);

        assertTrue(max.compareTo(max) == 0);
        assertTrue(min.compareTo(min) == 0);
        assertTrue(mid.compareTo(mid) == 0);

        assertTrue(max.compareTo(mid) > 0);
        assertTrue(max.compareTo(min) > 0);

        assertTrue(mid.compareTo(max) < 0);
        assertTrue(mid.compareTo(min) > 0);

        assertTrue(min.compareTo(mid) < 0);
        assertTrue(min.compareTo(max) < 0);

        try {
            min.compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    public void test_codePointAt_Invalid() {
        try {
            Character.codePointAt(null, 6, 4);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            Character.codePointAt(null, 4, 6);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            Character.codePointAt(null, 0, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.lang.Character#Character(char)
     */
    public void test_ConstructorC() {
        assertEquals("Constructor failed", 'T', new Character('T').charValue());
    }

    /**
     * java.lang.Character#charValue()
     */
    public void test_charValue() {
        assertEquals("Incorrect char value returned", 'T', new Character('T').charValue());
    }

    /**
     * java.lang.Character#compareTo(java.lang.Character)
     */
    public void test_compareToLjava_lang_Character() {
        Character c = new Character('c');
        Character x = new Character('c');
        Character y = new Character('b');
        Character z = new Character('d');

        assertEquals("Returned false for same Character", 0, c.compareTo(c));
        assertEquals("Returned false for identical Character", 0, c.compareTo(x));
        assertTrue("Returned other than less than for lesser char", c.compareTo(y) > 0);
        assertTrue("Returned other than greater than for greater char", c.compareTo(z) < 0);
    }

    /**
     * java.lang.Character#digit(char, int)
     */
    public void test_digitCI() {
        assertEquals("Returned incorrect digit", 1, Character.digit('1', 10));
        assertEquals("Returned incorrect digit", 15, Character.digit('F', 16));
    }

    /**
     * java.lang.Character#digit(int, int)
     */
    public void test_digit_II() {
        assertEquals(1, Character.digit((int) '1', 10));
        assertEquals(15, Character.digit((int) 'F', 16));

        assertEquals(-1, Character.digit(0x0000, 37));
        assertEquals(-1, Character.digit(0x0045, 10));

        assertEquals(10, Character.digit(0x0041, 20));
        assertEquals(10, Character.digit(0x0061, 20));

        assertEquals(-1, Character.digit(0x110000, 20));
    }

    /**
     * java.lang.Character#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean java.lang.Character.equals(java.lang.Object)
        assertTrue("Equality test failed", new Character('A').equals(new Character('A')));
        assertFalse("Equality test failed", (new Character('A').equals(new Character('a'))));
    }

    /**
     * java.lang.Character#forDigit(int, int)
     */
    public void test_forDigitII() {
        char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        for (int i = 0; i < hexChars.length; i++) {
            assertTrue("Returned incorrect char for " + Integer.toString(i),
                    Character.forDigit(i, hexChars.length) == hexChars[i]);
        }

        char decimalChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        for (int i = 0; i < decimalChars.length; i++) {
            assertTrue(
                    "Returned incorrect char for " + Integer.toString(i),
                    Character.forDigit(i, decimalChars.length) == decimalChars[i]);
        }

    }

    /**
     * java.lang.Character#getNumericValue(char)
     */
    public void test_getNumericValueC() {
        assertEquals("Returned incorrect numeric value 1", 1, Character
                .getNumericValue('1'));
        assertEquals("Returned incorrect numeric value 2", 15, Character
                .getNumericValue('F'));
        assertEquals("Returned incorrect numeric value 3", -1, Character
                .getNumericValue('\u221e'));
//        assertEquals("Returned incorrect numeric value 4", -2, Character
//                .getNumericValue('\u00be'));
//        assertEquals("Returned incorrect numeric value 5", 10000, Character
//                .getNumericValue('\u2182'));
//        assertEquals("Returned incorrect numeric value 6", 2, Character
//                .getNumericValue('\uff12'));
    }

    /**
     * java.lang.Character#getNumericValue(int)
     */
    public void test_getNumericValue_I() {
        assertEquals(1, Character.getNumericValue((int) '1'));
        assertEquals(15, Character.getNumericValue((int) 'F'));
        assertEquals(-1, Character.getNumericValue((int) '\u221e'));
//        assertEquals(-2, Character.getNumericValue((int) '\u00be'));
//        assertEquals(10000, Character.getNumericValue((int) '\u2182'));
//        assertEquals(2, Character.getNumericValue((int) '\uff12'));

        assertEquals(-1, Character.getNumericValue(0xFFFF));
//        assertEquals(0, Character.getNumericValue(0x1D7CE));
//        assertEquals(0, Character.getNumericValue(0x1D7D8));
//        assertEquals(-1, Character.getNumericValue(0x2F800));
//        assertEquals(-1, Character.getNumericValue(0x10FFFD));
//        assertEquals(-1, Character.getNumericValue(0x110000));

//        assertEquals(50, Character.getNumericValue(0x216C));

        assertEquals(10, Character.getNumericValue(0x0041));
        assertEquals(35, Character.getNumericValue(0x005A));
        assertEquals(10, Character.getNumericValue(0x0061));
        assertEquals(35, Character.getNumericValue(0x007A));
        assertEquals(10, Character.getNumericValue(0xFF21));

        //FIXME depends on ICU4J
        //assertEquals(35, Character.getNumericValue(0xFF3A));

        assertEquals(10, Character.getNumericValue(0xFF41));
        assertEquals(35, Character.getNumericValue(0xFF5A));
    }

    /**
     * java.lang.Character#getType(char)
     */
    public void test_getTypeC() {
        assertTrue("Returned incorrect type for: \n",
                Character.getType('\n') == Character.CONTROL);
        assertTrue("Returned incorrect type for: 1",
                Character.getType('1') == Character.DECIMAL_DIGIT_NUMBER);
        assertTrue("Returned incorrect type for: ' '",
                Character.getType(' ') == Character.SPACE_SEPARATOR);
        assertTrue("Returned incorrect type for: a",
                Character.getType('a') == Character.LOWERCASE_LETTER);
        assertTrue("Returned incorrect type for: A",
                Character.getType('A') == Character.UPPERCASE_LETTER);
        assertTrue("Returned incorrect type for: <",
                Character.getType('<') == Character.MATH_SYMBOL);
        assertTrue("Returned incorrect type for: ;",
                Character.getType(';') == Character.OTHER_PUNCTUATION);
        assertTrue("Returned incorrect type for: _",
                Character.getType('_') == Character.CONNECTOR_PUNCTUATION);
        assertTrue("Returned incorrect type for: $",
                Character.getType('$') == Character.CURRENCY_SYMBOL);
        assertTrue("Returned incorrect type for: \u2029",
                Character.getType('\u2029') == Character.PARAGRAPH_SEPARATOR);

        assertEquals("Wrong constant for FORMAT", 16, Character.FORMAT);
        assertEquals("Wrong constant for PRIVATE_USE", 18, Character.PRIVATE_USE);
    }

    /**
     * java.lang.Character#getType(int)
     */
    public void test_getType_I() {
        assertTrue(Character.getType((int) '\n') == Character.CONTROL);
        assertTrue(Character.getType((int) '1') == Character.DECIMAL_DIGIT_NUMBER);
        assertTrue(Character.getType((int) ' ') == Character.SPACE_SEPARATOR);
        assertTrue(Character.getType((int) 'a') == Character.LOWERCASE_LETTER);
        assertTrue(Character.getType((int) 'A') == Character.UPPERCASE_LETTER);
        assertTrue(Character.getType((int) '<') == Character.MATH_SYMBOL);
        assertTrue(Character.getType((int) ';') == Character.OTHER_PUNCTUATION);
        assertTrue(Character.getType((int) '_') == Character.CONNECTOR_PUNCTUATION);
        assertTrue(Character.getType((int) '$') == Character.CURRENCY_SYMBOL);
        assertTrue(Character.getType((int) '\u2029') == Character.PARAGRAPH_SEPARATOR);

        assertTrue(Character.getType(0x9FFF) == Character.UNASSIGNED);
        assertTrue(Character.getType(0x30000) == Character.UNASSIGNED);
        assertTrue(Character.getType(0x110000) == Character.UNASSIGNED);

        assertTrue(Character.getType(0x0041) == Character.UPPERCASE_LETTER);
        assertTrue(Character.getType(0x10400) == Character.UPPERCASE_LETTER);

        assertTrue(Character.getType(0x0061) == Character.LOWERCASE_LETTER);
        assertTrue(Character.getType(0x10428) == Character.LOWERCASE_LETTER);

        assertTrue(Character.getType(0x01C5) == Character.TITLECASE_LETTER);
        assertTrue(Character.getType(0x1FFC) == Character.TITLECASE_LETTER);

        assertTrue(Character.getType(0x02B0) == Character.MODIFIER_LETTER);
        assertTrue(Character.getType(0xFF9F) == Character.MODIFIER_LETTER);

        assertTrue(Character.getType(0x01BB) == Character.OTHER_LETTER);
        assertTrue(Character.getType(0x2F888) == Character.OTHER_LETTER);

        assertTrue(Character.getType(0x0F82) == Character.NON_SPACING_MARK);
        assertTrue(Character.getType(0x1D180) == Character.NON_SPACING_MARK);

        assertTrue(Character.getType(0x0488) == Character.ENCLOSING_MARK);
        assertTrue(Character.getType(0x20DE) == Character.ENCLOSING_MARK);

        assertTrue(Character.getType(0x1938) == Character.COMBINING_SPACING_MARK);
        assertTrue(Character.getType(0x1D165) == Character.COMBINING_SPACING_MARK);

        assertTrue(Character.getType(0x194D) == Character.DECIMAL_DIGIT_NUMBER);
        assertTrue(Character.getType(0x1D7CE) == Character.DECIMAL_DIGIT_NUMBER);

        assertTrue(Character.getType(0x2160) == Character.LETTER_NUMBER);
        assertTrue(Character.getType(0x1034A) == Character.LETTER_NUMBER);

        assertTrue(Character.getType(0x00B2) == Character.OTHER_NUMBER);
        assertTrue(Character.getType(0x10120) == Character.OTHER_NUMBER);

        assertTrue(Character.getType(0x0020) == Character.SPACE_SEPARATOR);
        assertTrue(Character.getType(0x3000) == Character.SPACE_SEPARATOR);

        assertTrue(Character.getType(0x2028) == Character.LINE_SEPARATOR);

        assertTrue(Character.getType(0x2029) == Character.PARAGRAPH_SEPARATOR);

        assertTrue(Character.getType(0x0000) == Character.CONTROL);
        assertTrue(Character.getType(0x009F) == Character.CONTROL);

        assertTrue(Character.getType(0x00AD) == Character.FORMAT);
        assertTrue(Character.getType(0xE007F) == Character.FORMAT);

        assertTrue(Character.getType(0xE000) == Character.PRIVATE_USE);
        assertTrue(Character.getType(0x10FFFD) == Character.PRIVATE_USE);

        assertTrue(Character.getType(0xD800) == Character.SURROGATE);
        assertTrue(Character.getType(0xDFFF) == Character.SURROGATE);

        assertTrue(Character.getType(0xFE31) == Character.DASH_PUNCTUATION);
        assertTrue(Character.getType(0xFF0D) == Character.DASH_PUNCTUATION);

        assertTrue(Character.getType(0x0028) == Character.START_PUNCTUATION);
        assertTrue(Character.getType(0xFF62) == Character.START_PUNCTUATION);

        assertTrue(Character.getType(0x0029) == Character.END_PUNCTUATION);
        assertTrue(Character.getType(0xFF63) == Character.END_PUNCTUATION);

        assertTrue(Character.getType(0x005F) == Character.CONNECTOR_PUNCTUATION);
        assertTrue(Character.getType(0xFF3F) == Character.CONNECTOR_PUNCTUATION);

        assertTrue(Character.getType(0x2034) == Character.OTHER_PUNCTUATION);
        assertTrue(Character.getType(0x1039F) == Character.OTHER_PUNCTUATION);

        assertTrue(Character.getType(0x002B) == Character.MATH_SYMBOL);
        assertTrue(Character.getType(0x1D6C1) == Character.MATH_SYMBOL);

        assertTrue(Character.getType(0x0024) == Character.CURRENCY_SYMBOL);
        assertTrue(Character.getType(0xFFE6) == Character.CURRENCY_SYMBOL);

        assertTrue(Character.getType(0x005E) == Character.MODIFIER_SYMBOL);
        assertTrue(Character.getType(0xFFE3) == Character.MODIFIER_SYMBOL);

        assertTrue(Character.getType(0x00A6) == Character.OTHER_SYMBOL);
        assertTrue(Character.getType(0x1D356) == Character.OTHER_SYMBOL);

        assertTrue(Character.getType(0x00AB) == Character.INITIAL_QUOTE_PUNCTUATION);
        assertTrue(Character.getType(0x2039) == Character.INITIAL_QUOTE_PUNCTUATION);

        assertTrue(Character.getType(0x00BB) == Character.FINAL_QUOTE_PUNCTUATION);
        assertTrue(Character.getType(0x203A) == Character.FINAL_QUOTE_PUNCTUATION);
    }

    /**
     * java.lang.Character#hashCode()
     */
    public void test_hashCode() {
        assertEquals("Incorrect hash returned", 89, new Character('Y').hashCode());
    }

    /**
     * java.lang.Character#isDefined(char)
     */
    public void test_isDefinedC() {
        assertTrue("Defined character returned false", Character.isDefined('v'));
        assertTrue("Defined character returned false", Character.isDefined('\u6039'));
    }

    /**
     * java.lang.Character#isDefined(int)
     */
    public void test_isDefined_I() {
        assertTrue(Character.isDefined((int) 'v'));
        assertTrue(Character.isDefined((int) '\u6039'));
        assertTrue(Character.isDefined(0x10300));

        assertFalse(Character.isDefined(0x30000));
        assertFalse(Character.isDefined(0x3FFFF));
        assertFalse(Character.isDefined(0x110000));
    }

    /**
     * java.lang.Character#isDigit(char)
     */
    public void test_isDigitC() {
        assertTrue("Digit returned false", Character.isDigit('1'));
        assertFalse("Non-Digit returned false", Character.isDigit('A'));
    }

    /**
     * java.lang.Character#isDigit(int)
     */
    public void test_isDigit_I() {
        assertTrue(Character.isDigit((int) '1'));
        assertFalse(Character.isDigit((int) 'A'));

        assertTrue(Character.isDigit(0x0030));
        assertTrue(Character.isDigit(0x0035));
        assertTrue(Character.isDigit(0x0039));

        assertTrue(Character.isDigit(0x0660));
        assertTrue(Character.isDigit(0x0665));
        assertTrue(Character.isDigit(0x0669));

        assertTrue(Character.isDigit(0x06F0));
        assertTrue(Character.isDigit(0x06F5));
        assertTrue(Character.isDigit(0x06F9));

        assertTrue(Character.isDigit(0x0966));
        assertTrue(Character.isDigit(0x096A));
        assertTrue(Character.isDigit(0x096F));

        assertTrue(Character.isDigit(0xFF10));
        assertTrue(Character.isDigit(0xFF15));
        assertTrue(Character.isDigit(0xFF19));

        assertTrue(Character.isDigit(0x1D7CE));
        assertTrue(Character.isDigit(0x1D7D8));

        assertFalse(Character.isDigit(0x2F800));
        assertFalse(Character.isDigit(0x10FFFD));
        assertFalse(Character.isDigit(0x110000));
    }

    /**
     * java.lang.Character#isIdentifierIgnorable(char)
     */
    public void test_isIdentifierIgnorableC() {
        assertTrue("Ignorable whitespace returned false",
                Character.isIdentifierIgnorable('\u0007'));
        assertTrue("Ignorable non - whitespace  control returned false",
                Character.isIdentifierIgnorable('\u000f'));
        assertTrue("Ignorable join control returned false",
                Character.isIdentifierIgnorable('\u200e'));

        // the spec is wrong, and our implementation is correct
        assertTrue("Ignorable bidi control returned false",
                Character.isIdentifierIgnorable('\u202b'));

        assertTrue("Ignorable format control returned false",
                Character.isIdentifierIgnorable('\u206c'));
        assertTrue("Ignorable zero-width no-break returned false",
                Character.isIdentifierIgnorable('\ufeff'));

        assertFalse("Non-Ignorable returned true", Character.isIdentifierIgnorable('\u0065'));
    }

    /**
     * java.lang.Character#isIdentifierIgnorable(int)
     */
    public void test_isIdentifierIgnorable_I() {
        assertTrue(Character.isIdentifierIgnorable(0x0000));
        assertTrue(Character.isIdentifierIgnorable(0x0004));
        assertTrue(Character.isIdentifierIgnorable(0x0008));

        assertTrue(Character.isIdentifierIgnorable(0x000E));
        assertTrue(Character.isIdentifierIgnorable(0x0013));
        assertTrue(Character.isIdentifierIgnorable(0x001B));

        assertTrue(Character.isIdentifierIgnorable(0x007F));
        assertTrue(Character.isIdentifierIgnorable(0x008F));
        assertTrue(Character.isIdentifierIgnorable(0x009F));

        assertTrue(Character.isIdentifierIgnorable(0x202b));
        assertTrue(Character.isIdentifierIgnorable(0x206c));
        assertTrue(Character.isIdentifierIgnorable(0xfeff));
        assertFalse(Character.isIdentifierIgnorable(0x0065));

        assertTrue(Character.isIdentifierIgnorable(0x1D173));

        assertFalse(Character.isIdentifierIgnorable(0x10FFFD));
        assertFalse(Character.isIdentifierIgnorable(0x110000));
    }

    /**
     * java.lang.Character#isMirrored(char)
     */
    public void test_isMirrored_C() {
        assertTrue(Character.isMirrored('\u0028'));
        assertFalse(Character.isMirrored('\uFFFF'));
    }

    /**
     * java.lang.Character#isMirrored(int)
     */
    public void test_isMirrored_I() {
        assertTrue(Character.isMirrored(0x0028));
        assertFalse(Character.isMirrored(0xFFFF));
        assertFalse(Character.isMirrored(0x110000));
    }

    /**
     * java.lang.Character#isISOControl(char)
     */
    public void test_isISOControlC() {
        // Test for method boolean java.lang.Character.isISOControl(char)
        for (int i = 0; i < 32; i++) {
            assertTrue("ISOConstrol char returned false", Character.isISOControl((char) i));
        }

        for (int i = 127; i < 160; i++) {
            assertTrue("ISOConstrol char returned false", Character.isISOControl((char) i));
        }
    }

    /**
     * java.lang.Character#isISOControl(int)
     */
    public void test_isISOControlI() {
        // Test for method boolean java.lang.Character.isISOControl(char)
        for (int i = 0; i < 32; i++) {
            assertTrue("ISOConstrol char returned false", Character.isISOControl(i));
        }

        for (int i = 127; i < 160; i++) {
            assertTrue("ISOConstrol char returned false", Character.isISOControl(i));
        }

        for (int i = 160; i < 260; i++) {
            assertFalse("Not ISOConstrol char returned true", Character.isISOControl(i));
        }
    }


    /**
     * java.lang.Character#isJavaIdentifierPart(char)
     */
    public void test_isJavaIdentifierPartC() {
        assertTrue("letter returned false", Character.isJavaIdentifierPart('l'));
        assertTrue("currency returned false", Character.isJavaIdentifierPart('$'));
        assertTrue("digit returned false", Character.isJavaIdentifierPart('9'));
        assertTrue("connecting char returned false", Character.isJavaIdentifierPart('_'));
        assertTrue("ignorable control returned false", Character.isJavaIdentifierPart('\u200b'));
        assertFalse("semi returned true", Character.isJavaIdentifierPart(';'));
    }

    /**
     * java.lang.Character#isJavaIdentifierPart(int)
     */
    public void test_isJavaIdentifierPart_I() {
        assertTrue(Character.isJavaIdentifierPart((int) 'l'));
        assertTrue(Character.isJavaIdentifierPart((int) '$'));
        assertTrue(Character.isJavaIdentifierPart((int) '9'));
        assertTrue(Character.isJavaIdentifierPart((int) '_'));
        assertFalse(Character.isJavaIdentifierPart((int) ';'));

        assertTrue(Character.isJavaIdentifierPart(0x0041));
        assertTrue(Character.isJavaIdentifierPart(0x10400));
        assertTrue(Character.isJavaIdentifierPart(0x0061));
        assertTrue(Character.isJavaIdentifierPart(0x10428));
        assertTrue(Character.isJavaIdentifierPart(0x01C5));
        assertTrue(Character.isJavaIdentifierPart(0x1FFC));
        assertTrue(Character.isJavaIdentifierPart(0x02B0));
        assertTrue(Character.isJavaIdentifierPart(0xFF9F));
        assertTrue(Character.isJavaIdentifierPart(0x01BB));
        assertTrue(Character.isJavaIdentifierPart(0x2F888));

        assertTrue(Character.isJavaIdentifierPart(0x0024));
        assertTrue(Character.isJavaIdentifierPart(0xFFE6));

        assertTrue(Character.isJavaIdentifierPart(0x005F));
        assertTrue(Character.isJavaIdentifierPart(0xFF3F));

        assertTrue(Character.isJavaIdentifierPart(0x194D));
        assertTrue(Character.isJavaIdentifierPart(0x1D7CE));
        assertTrue(Character.isJavaIdentifierPart(0x2160));
        assertTrue(Character.isJavaIdentifierPart(0x1034A));

        assertTrue(Character.isJavaIdentifierPart(0x0F82));
        assertTrue(Character.isJavaIdentifierPart(0x1D180));

        assertTrue(Character.isJavaIdentifierPart(0x0000));
        assertTrue(Character.isJavaIdentifierPart(0x0008));
        assertTrue(Character.isJavaIdentifierPart(0x000E));
        assertTrue(Character.isJavaIdentifierPart(0x001B));
        assertTrue(Character.isJavaIdentifierPart(0x007F));
        assertTrue(Character.isJavaIdentifierPart(0x009F));
        assertTrue(Character.isJavaIdentifierPart(0x00AD));
        assertTrue(Character.isJavaIdentifierPart(0xE007F));

        assertTrue(Character.isJavaIdentifierPart(0x200B));
    }

    /**
     * java.lang.Character#isJavaIdentifierStart(char)
     */
    public void test_isJavaIdentifierStartC() {
        assertTrue("letter returned false", Character.isJavaIdentifierStart('l'));
        assertTrue("currency returned false", Character.isJavaIdentifierStart('$'));
        assertTrue("connecting char returned false", Character.isJavaIdentifierStart('_'));
        assertFalse("digit returned true", Character.isJavaIdentifierStart('9'));
        assertFalse("ignorable control returned true", Character.isJavaIdentifierStart('\u200b'));
        assertFalse("semi returned true", Character.isJavaIdentifierStart(';'));
    }

    /**
     * java.lang.Character#isJavaIdentifierStart(int)
     */
    public void test_isJavaIdentifierStart_I() {
        assertTrue(Character.isJavaIdentifierStart((int) 'l'));
        assertTrue(Character.isJavaIdentifierStart((int) '$'));
        assertTrue(Character.isJavaIdentifierStart((int) '_'));
        assertFalse(Character.isJavaIdentifierStart((int) '9'));
        assertFalse(Character.isJavaIdentifierStart((int) '\u200b'));
        assertFalse(Character.isJavaIdentifierStart((int) ';'));

        assertTrue(Character.isJavaIdentifierStart(0x0041));
        assertTrue(Character.isJavaIdentifierStart(0x10400));
        assertTrue(Character.isJavaIdentifierStart(0x0061));
        assertTrue(Character.isJavaIdentifierStart(0x10428));
        assertTrue(Character.isJavaIdentifierStart(0x01C5));
        assertTrue(Character.isJavaIdentifierStart(0x1FFC));
        assertTrue(Character.isJavaIdentifierStart(0x02B0));
        assertTrue(Character.isJavaIdentifierStart(0xFF9F));
        assertTrue(Character.isJavaIdentifierStart(0x01BB));
        assertTrue(Character.isJavaIdentifierStart(0x2F888));

        assertTrue(Character.isJavaIdentifierPart(0x0024));
        assertTrue(Character.isJavaIdentifierPart(0xFFE6));

        assertTrue(Character.isJavaIdentifierPart(0x005F));
        assertTrue(Character.isJavaIdentifierPart(0xFF3F));

        assertTrue(Character.isJavaIdentifierPart(0x2160));
        assertTrue(Character.isJavaIdentifierPart(0x1034A));

        assertFalse(Character.isJavaIdentifierPart(0x110000));
    }

    /**
     * java.lang.Character#isJavaLetter(char)
     */
    @SuppressWarnings("deprecation")
    public void test_isJavaLetterC() {
        assertTrue("letter returned false", Character.isJavaLetter('l'));
        assertTrue("currency returned false", Character.isJavaLetter('$'));
        assertTrue("connecting char returned false", Character.isJavaLetter('_'));

        assertFalse("digit returned true", Character.isJavaLetter('9'));
        assertFalse("ignored control returned true", Character.isJavaLetter('\u200b'));
        assertFalse("semi returned true", Character.isJavaLetter(';'));
    }

    /**
     * java.lang.Character#isJavaLetterOrDigit(char)
     */
    @SuppressWarnings("deprecation")
    public void test_isJavaLetterOrDigitC() {
        assertTrue("letter returned false", Character.isJavaLetterOrDigit('l'));
        assertTrue("currency returned false", Character.isJavaLetterOrDigit('$'));
        assertTrue("digit returned false", Character.isJavaLetterOrDigit('9'));
        assertTrue("connecting char returned false", Character.isJavaLetterOrDigit('_'));
        assertFalse("semi returned true", Character.isJavaLetterOrDigit(';'));
    }

    /**
     * java.lang.Character#isLetter(char)
     */
    public void test_isLetterC() {
        assertTrue("Letter returned false", Character.isLetter('L'));
        assertFalse("Non-Letter returned true", Character.isLetter('9'));
    }

    /**
     * java.lang.Character#isLetter(int)
     */
    public void test_isLetter_I() {
        assertTrue(Character.isLetter((int) 'L'));
        assertFalse(Character.isLetter((int) '9'));

        assertTrue(Character.isLetter(0x1FA9));
        assertTrue(Character.isLetter(0x1D400));
        assertTrue(Character.isLetter(0x1D622));
        assertTrue(Character.isLetter(0x10000));

        assertFalse(Character.isLetter(0x1012C));
        assertFalse(Character.isLetter(0x110000));
    }

    /**
     * java.lang.Character#isLetterOrDigit(char)
     */
    public void test_isLetterOrDigitC() {
        assertTrue("Digit returned false", Character.isLetterOrDigit('9'));
        assertTrue("Letter returned false", Character.isLetterOrDigit('K'));
        assertFalse("Control returned true", Character.isLetterOrDigit('\n'));
        assertFalse("Punctuation returned true", Character.isLetterOrDigit('?'));
    }

    /**
     * java.lang.Character#isLetterOrDigit(int)
     */
    public void test_isLetterOrDigit_I() {
        assertTrue(Character.isLetterOrDigit((int) '9'));
        assertTrue(Character.isLetterOrDigit((int) 'K'));
        assertFalse(Character.isLetterOrDigit((int) '\n'));
        assertFalse(Character.isLetterOrDigit((int) '?'));

        assertTrue(Character.isLetterOrDigit(0x1FA9));
        assertTrue(Character.isLetterOrDigit(0x1D400));
        assertTrue(Character.isLetterOrDigit(0x1D622));
        assertTrue(Character.isLetterOrDigit(0x10000));

        assertTrue(Character.isLetterOrDigit(0x1D7CE));
        assertTrue(Character.isLetterOrDigit(0x1D7D8));

        assertFalse(Character.isLetterOrDigit(0x10FFFD));
        assertFalse(Character.isLetterOrDigit(0x1012C));
        assertFalse(Character.isLetterOrDigit(0x110000));
    }

    /**
     * java.lang.Character#isLowerCase(char)
     */
    public void test_isLowerCaseC() {
        assertTrue("lower returned false", Character.isLowerCase('a'));
        assertFalse("upper returned true", Character.isLowerCase('T'));
    }

    /**
     * java.lang.Character#isLowerCase(int)
     */
    public void test_isLowerCase_I() {
        assertTrue(Character.isLowerCase((int) 'a'));
        assertFalse(Character.isLowerCase((int) 'T'));

//        assertTrue(Character.isLowerCase(0x10428));
//        assertTrue(Character.isLowerCase(0x1D4EA));

        assertFalse(Character.isLowerCase(0x1D504));
        assertFalse(Character.isLowerCase(0x30000));
        assertFalse(Character.isLowerCase(0x110000));
    }

    /**
     * java.lang.Character#isSpace(char)
     */
    @SuppressWarnings("deprecation")
    public void test_isSpaceC() {
        // Test for method boolean java.lang.Character.isSpace(char)
        assertTrue("space returned false", Character.isSpace('\n'));
        assertFalse("non-space returned true", Character.isSpace('T'));
    }

    /**
     * java.lang.Character#isSpaceChar(char)
     */
    public void test_isSpaceCharC() {
        assertTrue("space returned false", Character.isSpaceChar('\u0020'));
        assertFalse("non-space returned true", Character.isSpaceChar('\n'));
    }

    /**
     * java.lang.Character#isSpaceChar(int)
     */
    public void test_isSpaceChar_I() {
        assertTrue(Character.isSpaceChar((int) '\u0020'));
        assertFalse(Character.isSpaceChar((int) '\n'));

        assertTrue(Character.isSpaceChar(0x2000));
        assertTrue(Character.isSpaceChar(0x200A));

        assertTrue(Character.isSpaceChar(0x2028));
        assertTrue(Character.isSpaceChar(0x2029));

        assertFalse(Character.isSpaceChar(0x110000));
    }

    /**
     * java.lang.Character#isTitleCase(char)
     */
    public void test_isTitleCaseC() {
        char[] tChars = { (char) 0x01c5, (char) 0x01c8, (char) 0x01cb,
                (char) 0x01f2, (char) 0x1f88, (char) 0x1f89, (char) 0x1f8a,
                (char) 0x1f8b, (char) 0x1f8c, (char) 0x1f8d, (char) 0x1f8e,
                (char) 0x1f8f, (char) 0x1f98, (char) 0x1f99, (char) 0x1f9a,
                (char) 0x1f9b, (char) 0x1f9c, (char) 0x1f9d, (char) 0x1f9e,
                (char) 0x1f9f, (char) 0x1fa8, (char) 0x1fa9, (char) 0x1faa,
                (char) 0x1fab, (char) 0x1fac, (char) 0x1fad, (char) 0x1fae,
                (char) 0x1faf, (char) 0x1fbc, (char) 0x1fcc, (char) 0x1ffc };
        byte tnum = 0;
        for (char c = 0; c < 65535; c++) {
            if (Character.isTitleCase(c)) {
                tnum++;
                int i;
                for (i = 0; i < tChars.length; i++) {
                    if (tChars[i] == c) {
                        i = tChars.length + 1;
                    }
                }
                if (i < tChars.length) {
                    fail("Non Title Case char returned true");
                }
            }
        }
        assertTrue("Failed to find all Title Case chars", tnum == tChars.length);
    }

    /**
     * java.lang.Character#isTitleCase(int)
     */
    public void test_isTitleCase_I() {
        //all the titlecase characters
        int[] titleCaseCharacters = { 0x01c5, 0x01c8, 0x01cb, 0x01f2, 0x1f88,
                0x1f89, 0x1f8a, 0x1f8b, 0x1f8c, 0x1f8d, 0x1f8e, 0x1f8f, 0x1f98,
                0x1f99, 0x1f9a, 0x1f9b, 0x1f9c, 0x1f9d, 0x1f9e, 0x1f9f, 0x1fa8,
                0x1fa9, 0x1faa, 0x1fab, 0x1fac, 0x1fad, 0x1fae, 0x1faf, 0x1fbc,
                0x1fcc, 0x1ffc };

        for (int titleCaseCharacter : titleCaseCharacters) {
            assertTrue(Character.isTitleCase(titleCaseCharacter));
        }

        assertFalse(Character.isTitleCase(0x110000));
    }

    /**
     * java.lang.Character#isUnicodeIdentifierPart(char)
     */
    public void test_isUnicodeIdentifierPartC() {
        assertTrue("'a' returned false", Character.isUnicodeIdentifierPart('a'));
        assertTrue("'2' returned false", Character.isUnicodeIdentifierPart('2'));
        assertFalse("'+' returned true", Character.isUnicodeIdentifierPart('+'));
    }

    /**
     * java.lang.Character#isUnicodeIdentifierPart(int)
     */
    public void test_isUnicodeIdentifierPart_I() {
        assertTrue(Character.isUnicodeIdentifierPart((int) 'a'));
        assertTrue(Character.isUnicodeIdentifierPart((int) '2'));
        assertFalse(Character.isUnicodeIdentifierPart((int) '+'));

        assertTrue(Character.isUnicodeIdentifierPart(0x1FA9));
        assertTrue(Character.isUnicodeIdentifierPart(0x1D400));
        assertTrue(Character.isUnicodeIdentifierPart(0x1D622));
        assertTrue(Character.isUnicodeIdentifierPart(0x10000));

        assertTrue(Character.isUnicodeIdentifierPart(0x0030));
        assertTrue(Character.isUnicodeIdentifierPart(0x0035));
        assertTrue(Character.isUnicodeIdentifierPart(0x0039));

        assertTrue(Character.isUnicodeIdentifierPart(0x0660));
        assertTrue(Character.isUnicodeIdentifierPart(0x0665));
        assertTrue(Character.isUnicodeIdentifierPart(0x0669));

        assertTrue(Character.isUnicodeIdentifierPart(0x06F0));
        assertTrue(Character.isUnicodeIdentifierPart(0x06F5));
        assertTrue(Character.isUnicodeIdentifierPart(0x06F9));

        assertTrue(Character.isUnicodeIdentifierPart(0x0966));
        assertTrue(Character.isUnicodeIdentifierPart(0x096A));
        assertTrue(Character.isUnicodeIdentifierPart(0x096F));

        assertTrue(Character.isUnicodeIdentifierPart(0xFF10));
        assertTrue(Character.isUnicodeIdentifierPart(0xFF15));
        assertTrue(Character.isUnicodeIdentifierPart(0xFF19));

        assertTrue(Character.isUnicodeIdentifierPart(0x1D7CE));
        assertTrue(Character.isUnicodeIdentifierPart(0x1D7D8));

        assertTrue(Character.isUnicodeIdentifierPart(0x16EE));
        assertTrue(Character.isUnicodeIdentifierPart(0xFE33));
        assertTrue(Character.isUnicodeIdentifierPart(0xFF10));
        assertTrue(Character.isUnicodeIdentifierPart(0x1D165));
        assertTrue(Character.isUnicodeIdentifierPart(0x1D167));
        assertTrue(Character.isUnicodeIdentifierPart(0x1D173));

        assertFalse(Character.isUnicodeIdentifierPart(0x10FFFF));
        assertFalse(Character.isUnicodeIdentifierPart(0x110000));
    }

    /**
     * java.lang.Character#isUnicodeIdentifierStart(char)
     */
    public void test_isUnicodeIdentifierStartC() {
        assertTrue("'a' returned false", Character.isUnicodeIdentifierStart('a'));
        assertFalse("'2' returned true", Character.isUnicodeIdentifierStart('2'));
        assertFalse("'+' returned true", Character.isUnicodeIdentifierStart('+'));
    }

    /**
     * java.lang.Character#isUnicodeIdentifierStart(int)
     */
    public void test_isUnicodeIdentifierStart_I() {
        assertTrue(Character.isUnicodeIdentifierStart((int) 'a'));
        assertFalse(Character.isUnicodeIdentifierStart((int) '2'));
        assertFalse(Character.isUnicodeIdentifierStart((int) '+'));

        assertTrue(Character.isUnicodeIdentifierStart(0x1FA9));
        assertTrue(Character.isUnicodeIdentifierStart(0x1D400));
        assertTrue(Character.isUnicodeIdentifierStart(0x1D622));
        assertTrue(Character.isUnicodeIdentifierStart(0x10000));

        assertTrue(Character.isUnicodeIdentifierStart(0x16EE));

        // number is not a valid start of a Unicode identifier
        assertFalse(Character.isUnicodeIdentifierStart(0x0030));
        assertFalse(Character.isUnicodeIdentifierStart(0x0039));
        assertFalse(Character.isUnicodeIdentifierStart(0x0660));
        assertFalse(Character.isUnicodeIdentifierStart(0x0669));
        assertFalse(Character.isUnicodeIdentifierStart(0x06F0));
        assertFalse(Character.isUnicodeIdentifierStart(0x06F9));

        assertFalse(Character.isUnicodeIdentifierPart(0x10FFFF));
        assertFalse(Character.isUnicodeIdentifierPart(0x110000));
    }

    /**
     * java.lang.Character#isUpperCase(char)
     */
    public void test_isUpperCaseC() {
        assertFalse("Incorrect case value", Character.isUpperCase('t'));
        assertTrue("Incorrect case value", Character.isUpperCase('T'));
    }

    /**
     * java.lang.Character#isUpperCase(int)
     */
    public void test_isUpperCase_I() {
        assertFalse(Character.isUpperCase((int) 't'));
        assertTrue(Character.isUpperCase((int) 'T'));

        assertTrue(Character.isUpperCase(0x1D504));
        assertTrue(Character.isUpperCase(0x1D608));

        assertFalse(Character.isUpperCase(0x1D656));
        assertFalse(Character.isUpperCase(0x10FFFD));
        assertFalse(Character.isUpperCase(0x110000));
    }

    /**
     * java.lang.Character#isWhitespace(char)
     */
    public void test_isWhitespaceC() {
        assertTrue("space returned false", Character.isWhitespace('\n'));
        assertFalse("non-space returned true", Character.isWhitespace('T'));
    }

    /**
     * java.lang.Character#isWhitespace(int)
     */
    public void test_isWhitespace_I() {
        assertTrue(Character.isWhitespace((int) '\n'));
        assertFalse(Character.isWhitespace((int) 'T'));

        assertTrue(Character.isWhitespace(0x0009));
        assertTrue(Character.isWhitespace(0x000A));
        assertTrue(Character.isWhitespace(0x000B));
        assertTrue(Character.isWhitespace(0x000C));
        assertTrue(Character.isWhitespace(0x000D));
        assertTrue(Character.isWhitespace(0x001C));
        assertTrue(Character.isWhitespace(0x001D));
        assertTrue(Character.isWhitespace(0x001F));
        assertTrue(Character.isWhitespace(0x001E));

        assertTrue(Character.isWhitespace(0x2000));
        assertTrue(Character.isWhitespace(0x200A));

        assertTrue(Character.isWhitespace(0x2028));
        assertTrue(Character.isWhitespace(0x2029));

        assertFalse(Character.isWhitespace(0x00A0));
        assertFalse(Character.isWhitespace(0x202F));
        assertFalse(Character.isWhitespace(0x110000));

        assertFalse(Character.isWhitespace(0xFEFF));

        //FIXME depend on ICU4J
        //assertFalse(Character.isWhitespace(0x2007));

    }

    /**
     * java.lang.Character#reverseBytes(char)
     */
    public void test_reverseBytesC() {
        char original[] = new char[] { 0x0000, 0x0010, 0x00AA, 0xB000, 0xCC00, 0xABCD, 0xFFAA };
        char reversed[] = new char[] { 0x0000, 0x1000, 0xAA00, 0x00B0, 0x00CC, 0xCDAB, 0xAAFF };
        assertTrue("Test self check", original.length == reversed.length);

        for (int i = 0; i < original.length; i++) {
            char origChar = original[i];
            char reversedChar = reversed[i];
            char origReversed = Character.reverseBytes(origChar);

            assertTrue("java.lang.Character.reverseBytes failed: orig char="
                    + Integer.toHexString(origChar) + ", reversed char="
                    + Integer.toHexString(origReversed), reversedChar == origReversed);
        }
    }

    /**
     * java.lang.Character#toLowerCase(char)
     */
    public void test_toLowerCaseC() {
        assertEquals("Failed to change case", 't', Character.toLowerCase('T'));
    }

    /**
     * java.lang.Character#toLowerCase(int)
     */
    public void test_toLowerCase_I() {
        assertEquals('t', Character.toLowerCase((int) 'T'));

        assertEquals(0x10428, Character.toLowerCase(0x10400));
        assertEquals(0x10428, Character.toLowerCase(0x10428));

        assertEquals(0x1D504, Character.toLowerCase(0x1D504));
        assertEquals(0x10FFFD, Character.toLowerCase(0x10FFFD));
        assertEquals(0x110000, Character.toLowerCase(0x110000));
    }

    /**
     * java.lang.Character#toString()
     */
    public void test_toString() {
        assertEquals("Incorrect String returned", "T", new Character('T').toString());
    }

    /**
     * java.lang.Character#toTitleCase(char)
     */
    public void test_toTitleCaseC() {
        assertEquals("Incorrect title case for a", 'A', Character.toTitleCase('a'));
        assertEquals("Incorrect title case for A", 'A', Character.toTitleCase('A'));
        assertEquals("Incorrect title case for 1", '1', Character.toTitleCase('1'));
    }

    /**
     * java.lang.Character#toTitleCase(int)
     */
    public void test_toTitleCase_I() {
        assertEquals('A', Character.toTitleCase((int) 'a'));
        assertEquals('A', Character.toTitleCase((int) 'A'));
        assertEquals('1', Character.toTitleCase((int) '1'));

        assertEquals(0x10400, Character.toTitleCase(0x10428));
        assertEquals(0x10400, Character.toTitleCase(0x10400));

        assertEquals(0x10FFFF, Character.toTitleCase(0x10FFFF));
        assertEquals(0x110000, Character.toTitleCase(0x110000));
    }

    /**
     * java.lang.Character#toUpperCase(char)
     */
    public void test_toUpperCaseC() {
        // Test for method char java.lang.Character.toUpperCase(char)
        assertEquals("Incorrect upper case for a", 'A', Character.toUpperCase('a'));
        assertEquals("Incorrect upper case for A", 'A', Character.toUpperCase('A'));
        assertEquals("Incorrect upper case for 1", '1', Character.toUpperCase('1'));
    }

    /**
     * java.lang.Character#toUpperCase(int)
     */
    public void test_toUpperCase_I() {
        assertEquals('A', Character.toUpperCase((int) 'a'));
        assertEquals('A', Character.toUpperCase((int) 'A'));
        assertEquals('1', Character.toUpperCase((int) '1'));

        assertEquals(0x10400, Character.toUpperCase(0x10428));
        assertEquals(0x10400, Character.toUpperCase(0x10400));

        assertEquals(0x10FFFF, Character.toUpperCase(0x10FFFF));
        assertEquals(0x110000, Character.toUpperCase(0x110000));
    }

    /**
     * java.lang.Character#getDirectionality(int)
     */
    public void test_isDirectionaliy_I() {
        assertEquals(Character.DIRECTIONALITY_UNDEFINED, Character.getDirectionality(0xFFFE));
        assertEquals(Character.DIRECTIONALITY_UNDEFINED, Character.getDirectionality(0x30000));
        assertEquals(Character.DIRECTIONALITY_UNDEFINED, Character.getDirectionality(0x110000));
        assertEquals(Character.DIRECTIONALITY_UNDEFINED, Character.getDirectionality(-1));

        assertEquals(Character.DIRECTIONALITY_LEFT_TO_RIGHT, Character.getDirectionality(0x0041));
        assertEquals(Character.DIRECTIONALITY_LEFT_TO_RIGHT, Character.getDirectionality(0x10000));
        assertEquals(Character.DIRECTIONALITY_LEFT_TO_RIGHT, Character.getDirectionality(0x104A9));

        assertEquals(Character.DIRECTIONALITY_RIGHT_TO_LEFT, Character.getDirectionality(0xFB4F));
        assertEquals(Character.DIRECTIONALITY_RIGHT_TO_LEFT, Character.getDirectionality(0x10838));
        // Unicode standard 5.1 changed category of unicode point 0x0600 from AL to AN
        assertEquals(Character.DIRECTIONALITY_ARABIC_NUMBER, Character.getDirectionality(0x0600));
        assertEquals(Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
                Character.getDirectionality(0xFEFC));

        assertEquals(Character.DIRECTIONALITY_EUROPEAN_NUMBER, Character.getDirectionality(0x2070));
        assertEquals(Character.DIRECTIONALITY_EUROPEAN_NUMBER,
                Character.getDirectionality(0x1D7FF));

        assertEquals(Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR,
                Character.getDirectionality(0x002B));
        assertEquals(Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR,
                Character.getDirectionality(0xFF0B));

        assertEquals(Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR,
                Character.getDirectionality(0x0023));
        assertEquals(Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR,
                Character.getDirectionality(0x17DB));

        assertEquals(Character.DIRECTIONALITY_ARABIC_NUMBER, Character.getDirectionality(0x0660));
        assertEquals(Character.DIRECTIONALITY_ARABIC_NUMBER, Character.getDirectionality(0x066C));

        assertEquals(Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR,
                Character.getDirectionality(0x002C));
        assertEquals(Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR,
                Character.getDirectionality(0xFF1A));

        assertEquals(Character.DIRECTIONALITY_NONSPACING_MARK, Character.getDirectionality(0x17CE));
        assertEquals(Character.DIRECTIONALITY_NONSPACING_MARK,
                Character.getDirectionality(0xE01DB));

        assertEquals(Character.DIRECTIONALITY_BOUNDARY_NEUTRAL,
                Character.getDirectionality(0x0000));
        assertEquals(Character.DIRECTIONALITY_BOUNDARY_NEUTRAL,
                Character.getDirectionality(0xE007F));

        assertEquals(Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR,
                Character.getDirectionality(0x000A));
        assertEquals(Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR,
                Character.getDirectionality(0x2029));

        assertEquals(Character.DIRECTIONALITY_SEGMENT_SEPARATOR,
                Character.getDirectionality(0x0009));
        assertEquals(Character.DIRECTIONALITY_SEGMENT_SEPARATOR,
                Character.getDirectionality(0x001F));

        assertEquals(Character.DIRECTIONALITY_WHITESPACE, Character.getDirectionality(0x0020));
        assertEquals(Character.DIRECTIONALITY_WHITESPACE, Character.getDirectionality(0x3000));

        assertEquals(Character.DIRECTIONALITY_OTHER_NEUTRALS, Character.getDirectionality(0x2FF0));
        assertEquals(Character.DIRECTIONALITY_OTHER_NEUTRALS, Character.getDirectionality(0x1D356));

        assertEquals(Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
                Character.getDirectionality(0x202A));

        assertEquals(Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE,
                Character.getDirectionality(0x202D));

        assertEquals(Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
                Character.getDirectionality(0x202B));

        assertEquals(Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE,
                Character.getDirectionality(0x202E));

        assertEquals(Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT,
                Character.getDirectionality(0x202C));
    }
}
