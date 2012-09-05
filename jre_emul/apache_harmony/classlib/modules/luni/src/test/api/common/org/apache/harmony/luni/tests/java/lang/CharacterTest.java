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

import java.util.Arrays;

import junit.framework.TestCase;

public class CharacterTest extends TestCase {

    public void test_valueOfC() {
        // test the cache range
        for (char c = '\u0000'; c < 512; c++) {
            Character e = new Character(c);
            Character a = Character.valueOf(c);
            assertEquals(e, a);
        }
        // test the rest of the chars
        for (int c = 512; c <= Character.MAX_VALUE; c++) {
            assertEquals(new Character((char) c), Character.valueOf((char) c));
        }
    }

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

        assertEquals('a', Character.codePointAt((CharSequence) "abc", 0));
        assertEquals('b', Character.codePointAt((CharSequence) "abc", 1));
        assertEquals('c', Character.codePointAt((CharSequence) "abc", 2));
        assertEquals(0x10000, Character.codePointAt(
                (CharSequence) "\uD800\uDC00", 0));
        assertEquals('\uDC00', Character.codePointAt(
                (CharSequence) "\uD800\uDC00", 1));

        try {
            Character.codePointAt((CharSequence) null, 0);
            fail("No NPE.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointAt((CharSequence) "abc", -1);
            fail("No IOOBE, negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointAt((CharSequence) "abc", 4);
            fail("No IOOBE, index too large.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_codePointAt$CI() {

        assertEquals('a', Character.codePointAt("abc".toCharArray(), 0));
        assertEquals('b', Character.codePointAt("abc".toCharArray(), 1));
        assertEquals('c', Character.codePointAt("abc".toCharArray(), 2));
        assertEquals(0x10000, Character.codePointAt("\uD800\uDC00"
                .toCharArray(), 0));
        assertEquals('\uDC00', Character.codePointAt("\uD800\uDC00"
                .toCharArray(), 1));

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
        assertEquals(0x10000, Character.codePointAt("\uD800\uDC00"
                .toCharArray(), 0, 2));
        assertEquals('\uDC00', Character.codePointAt("\uD800\uDC00"
                .toCharArray(), 1, 2));
        assertEquals('\uD800', Character.codePointAt("\uD800\uDC00"
                .toCharArray(), 0, 1));

        try {
            Character.codePointAt((char[]) null, 0, 1);
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

        assertEquals('a', Character.codePointBefore((CharSequence) "abc", 1));
        assertEquals('b', Character.codePointBefore((CharSequence) "abc", 2));
        assertEquals('c', Character.codePointBefore((CharSequence) "abc", 3));
        assertEquals(0x10000, Character.codePointBefore(
                (CharSequence) "\uD800\uDC00", 2));
        assertEquals('\uD800', Character.codePointBefore(
                (CharSequence) "\uD800\uDC00", 1));

        try {
            Character.codePointBefore((CharSequence) null, 0);
            fail("No NPE.");
        } catch (NullPointerException e) {
        }

        try {
            Character.codePointBefore((CharSequence) "abc", 0);
            fail("No IOOBE, index below one.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Character.codePointBefore((CharSequence) "abc", 4);
            fail("No IOOBE, index too large.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_codePointBefore$CI() {

        assertEquals('a', Character.codePointBefore("abc".toCharArray(), 1));
        assertEquals('b', Character.codePointBefore("abc".toCharArray(), 2));
        assertEquals('c', Character.codePointBefore("abc".toCharArray(), 3));
        assertEquals(0x10000, Character.codePointBefore("\uD800\uDC00"
                .toCharArray(), 2));
        assertEquals('\uD800', Character.codePointBefore("\uD800\uDC00"
                .toCharArray(), 1));

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
        assertEquals(0x10000, Character.codePointBefore("\uD800\uDC00"
                .toCharArray(), 2, 0));
        assertEquals('\uDC00', Character.codePointBefore("\uD800\uDC00"
                .toCharArray(), 2, 1));
        assertEquals('\uD800', Character.codePointBefore("\uD800\uDC00"
                .toCharArray(), 1, 0));

        try {
            Character.codePointBefore((char[]) null, 1, 0);
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
        assertTrue(Arrays.equals(new char[] { '\uD800', '\uDC00' }, Character
                .toChars(0x10000)));
        assertTrue(Arrays.equals(new char[] { '\uD800', '\uDC01' }, Character
                .toChars(0x10001)));
        assertTrue(Arrays.equals(new char[] { '\uD801', '\uDC01' }, Character
                .toChars(0x10401)));
        assertTrue(Arrays.equals(new char[] { '\uDBFF', '\uDFFF' }, Character
                .toChars(0x10FFFF)));

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
            Character.offsetByCodePoints((CharSequence) null, 0, 1);
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

    /**
     * @tests java.lang.Character#compareTo(Character)
     */
    public void test_compareToLjava_lang_Byte() {
        final Character min = new Character(Character.MIN_VALUE);
        final Character mid = new Character((char)(Character.MAX_VALUE/2));
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
     * @tests java.lang.Character#Character(char)
     */
    public void test_ConstructorC() {
        assertEquals("Constructor failed", 'T', new Character('T').charValue());
    }

    /**
     * @tests java.lang.Character#charValue()
     */
    public void test_charValue() {
        assertEquals("Incorrect char value returned", 'T', new Character('T')
                .charValue());
    }

    /**
     * @tests java.lang.Character#compareTo(java.lang.Character)
     */
    public void test_compareToLjava_lang_Character() {
        Character c = new Character('c');
        Character x = new Character('c');
        Character y = new Character('b');
        Character z = new Character('d');

        assertEquals("Returned false for same Character", 0, c.compareTo(c));
        assertEquals("Returned false for identical Character",
                0, c.compareTo(x));
        assertTrue("Returned other than less than for lesser char", c
                .compareTo(y) > 0);
        assertTrue("Returned other than greater than for greater char", c
                .compareTo(z) < 0);
    }

    /**
     * @tests java.lang.Character#digit(char, int)
     */
    public void test_digitCI() {
        assertEquals("Returned incorrect digit", 1, Character.digit('1', 10));
        assertEquals("Returned incorrect digit", 15, Character.digit('F', 16));
    }

    /**
     * @tests java.lang.Character#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean java.lang.Character.equals(java.lang.Object)
        assertTrue("Equality test failed", new Character('A')
                .equals(new Character('A')));
        assertTrue("Equality test failed", !(new Character('A')
                .equals(new Character('a'))));
    }

    /**
     * @tests java.lang.Character#forDigit(int, int)
     */
    public void test_forDigitII() {
        char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        for (int i = 0; i < hexChars.length; i++) {
            assertTrue("Returned incorrect char for " + Integer.toString(i),
                    Character.forDigit(i, hexChars.length) == hexChars[i]);
        }

        char decimalChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9' };
        for (int i = 0; i < decimalChars.length; i++) {
            assertTrue(
                    "Returned incorrect char for " + Integer.toString(i),
                    Character.forDigit(i, decimalChars.length) == decimalChars[i]);
        }

    }

    /**
     * @tests java.lang.Character#getNumericValue(char)
     */
    public void test_getNumericValueC() {
        assertEquals("Returned incorrect numeric value 1", 1, Character
                .getNumericValue('1'));
        assertEquals("Returned incorrect numeric value 2", 15, Character
                .getNumericValue('F'));
        assertEquals("Returned incorrect numeric value 3", -1, Character
                .getNumericValue('\u221e'));
        assertEquals("Returned incorrect numeric value 4", -2, Character
                .getNumericValue('\u00be'));
        assertEquals("Returned incorrect numeric value 5", 10000, Character
                .getNumericValue('\u2182'));
        assertEquals("Returned incorrect numeric value 6", 2, Character
                .getNumericValue('\uff12'));
    }

    /**
     * @tests java.lang.Character#getType(char)
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
        assertTrue("Returned incorrect type for: \u2029", Character
                .getType('\u2029') == Character.PARAGRAPH_SEPARATOR);

        assertEquals("Wrong constant for FORMAT", 16, Character.FORMAT);
        assertEquals("Wrong constant for PRIVATE_USE",
                18, Character.PRIVATE_USE);
    }

    /**
     * @tests java.lang.Character#hashCode()
     */
    public void test_hashCode() {
        assertEquals("Incorrect hash returned",
                89, new Character('Y').hashCode());
    }

    /**
     * @tests java.lang.Character#isDefined(char)
     */
    public void test_isDefinedC() {
        assertTrue("Defined character returned false", Character.isDefined('v'));
        assertTrue("Defined character returned false", Character
                .isDefined('\u6039'));
    }
    
    /**
     * @tests java.lang.Character#isDigit(char)
     */
    public void test_isDigitC() {
        assertTrue("Digit returned false", Character.isDigit('1'));
        assertTrue("Non-Digit returned false", !Character.isDigit('A'));
    }

    /**
     * @tests java.lang.Character#isIdentifierIgnorable(char)
     */
    public void test_isIdentifierIgnorableC() {
        assertTrue("Ignorable whitespace returned false", Character
                .isIdentifierIgnorable('\u0007'));
        assertTrue("Ignorable non - whitespace  control returned false",
                Character.isIdentifierIgnorable('\u000f'));
        assertTrue("Ignorable join control returned false", Character
                .isIdentifierIgnorable('\u200e'));

        // the spec is wrong, and our implementation is correct
        assertTrue("Ignorable bidi control returned false", Character
                .isIdentifierIgnorable('\u202b'));

        assertTrue("Ignorable format control returned false", Character
                .isIdentifierIgnorable('\u206c'));
        assertTrue("Ignorable zero-width no-break returned false", Character
                .isIdentifierIgnorable('\ufeff'));

        assertTrue("Non-Ignorable returned true", !Character
                .isIdentifierIgnorable('\u0065'));
    }
    
    /**
     * @tests java.lang.Character#isMirrored(char)
     */
    public void test_isMirrored_C() {
        assertTrue(Character.isMirrored('\u0028'));
        assertFalse(Character.isMirrored('\uFFFF'));
    }

    /**
     * @tests java.lang.Character#isISOControl(char)
     */
    public void test_isISOControlC() {
        // Test for method boolean java.lang.Character.isISOControl(char)
        for (int i = 0; i < 32; i++)
            assertTrue("ISOConstrol char returned false", Character
                    .isISOControl((char) i));

        for (int i = 127; i < 160; i++)
            assertTrue("ISOConstrol char returned false", Character
                    .isISOControl((char) i));
    }

    /**
     * @tests java.lang.Character#isISOControl(int)
     */
    public void test_isISOControlI() {
        // Test for method boolean java.lang.Character.isISOControl(char)
        for (int i = 0; i < 32; i++)
            assertTrue("ISOConstrol char returned false", Character
                .isISOControl(i));

        for (int i = 127; i < 160; i++)
            assertTrue("ISOConstrol char returned false", Character
                .isISOControl(i));

        for (int i = 160; i < 260; i++)
            assertFalse("Not ISOConstrol char returned true", Character
                .isISOControl(i));

    }


    /**
     * @tests java.lang.Character#isJavaIdentifierPart(char)
     */
    public void test_isJavaIdentifierPartC() {
        assertTrue("letter returned false", Character.isJavaIdentifierPart('l'));
        assertTrue("currency returned false", Character
                .isJavaIdentifierPart('$'));
        assertTrue("digit returned false", Character.isJavaIdentifierPart('9'));
        assertTrue("connecting char returned false", Character
                .isJavaIdentifierPart('_'));
        assertTrue("ignorable control returned true", !Character
                .isJavaIdentifierPart('\u200b'));
        assertTrue("semi returned true", !Character.isJavaIdentifierPart(';'));
    }

    /**
     * @tests java.lang.Character#isJavaIdentifierStart(char)
     */
    public void test_isJavaIdentifierStartC() {
        assertTrue("letter returned false", Character
                .isJavaIdentifierStart('l'));
        assertTrue("currency returned false", Character
                .isJavaIdentifierStart('$'));
        assertTrue("connecting char returned false", Character
                .isJavaIdentifierStart('_'));
        assertTrue("digit returned true", !Character.isJavaIdentifierStart('9'));
        assertTrue("ignorable control returned true", !Character
                .isJavaIdentifierStart('\u200b'));
        assertTrue("semi returned true", !Character.isJavaIdentifierStart(';'));
    }

    /**
     * @tests java.lang.Character#isJavaLetter(char)
     */
    @SuppressWarnings("deprecation")
    public void test_isJavaLetterC() {
        assertTrue("letter returned false", Character.isJavaLetter('l'));
        assertTrue("currency returned false", Character.isJavaLetter('$'));
        assertTrue("connecting char returned false", Character
                .isJavaLetter('_'));

        assertTrue("digit returned true", !Character.isJavaLetter('9'));
        assertTrue("ignored control returned true", !Character
                .isJavaLetter('\u200b'));
        assertTrue("semi returned true", !Character.isJavaLetter(';'));
    }

    /**
     * @tests java.lang.Character#isJavaLetterOrDigit(char)
     */
    @SuppressWarnings("deprecation")
    public void test_isJavaLetterOrDigitC() {
        assertTrue("letter returned false", Character.isJavaLetterOrDigit('l'));
        assertTrue("currency returned false", Character
                .isJavaLetterOrDigit('$'));
        assertTrue("digit returned false", Character.isJavaLetterOrDigit('9'));
        assertTrue("connecting char returned false", Character
                .isJavaLetterOrDigit('_'));
        assertTrue("semi returned true", !Character.isJavaLetterOrDigit(';'));
    }

    /**
     * @tests java.lang.Character#isLetter(char)
     */
    public void test_isLetterC() {
        assertTrue("Letter returned false", Character.isLetter('L'));
        assertTrue("Non-Letter returned true", !Character.isLetter('9'));
    }

    /**
     * @tests java.lang.Character#isLetterOrDigit(char)
     */
    public void test_isLetterOrDigitC() {
        assertTrue("Digit returned false", Character.isLetterOrDigit('9'));
        assertTrue("Letter returned false", Character.isLetterOrDigit('K'));
        assertTrue("Control returned true", !Character.isLetterOrDigit('\n'));
        assertTrue("Punctuation returned true", !Character.isLetterOrDigit('?'));
    }

    /**
     * @tests java.lang.Character#isLowerCase(char)
     */
    public void test_isLowerCaseC() {
        assertTrue("lower returned false", Character.isLowerCase('a'));
        assertTrue("upper returned true", !Character.isLowerCase('T'));
    }

    /**
     * @tests java.lang.Character#isSpace(char)
     */
    @SuppressWarnings("deprecation")
    public void test_isSpaceC() {
        // Test for method boolean java.lang.Character.isSpace(char)
        assertTrue("space returned false", Character.isSpace('\n'));
        assertTrue("non-space returned true", !Character.isSpace('T'));
    }

    /**
     * @tests java.lang.Character#isSpaceChar(char)
     */
    public void test_isSpaceCharC() {
        assertTrue("space returned false", Character.isSpaceChar('\u0020'));
        assertTrue("non-space returned true", !Character.isSpaceChar('\n'));
    }

    /**
     * @tests java.lang.Character#isTitleCase(char)
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
                for (i = 0; i < tChars.length; i++)
                    if (tChars[i] == c)
                        i = tChars.length + 1;
                if (i < tChars.length) {
                    fail("Non Title Case char returned true");
                }
            }
        }
        assertTrue("Failed to find all Title Case chars", tnum == tChars.length);
    }

    /**
     * @tests java.lang.Character#isUnicodeIdentifierPart(char)
     */
    public void test_isUnicodeIdentifierPartC() {
        assertTrue("'a' returned false", Character.isUnicodeIdentifierPart('a'));
        assertTrue("'2' returned false", Character.isUnicodeIdentifierPart('2'));
        assertTrue("'+' returned true", !Character.isUnicodeIdentifierPart('+'));
    }

    /**
     * @tests java.lang.Character#isUnicodeIdentifierStart(char)
     */
    public void test_isUnicodeIdentifierStartC() {
        assertTrue("'a' returned false", Character
                .isUnicodeIdentifierStart('a'));
        assertTrue("'2' returned true", !Character
                .isUnicodeIdentifierStart('2'));
        assertTrue("'+' returned true", !Character
                .isUnicodeIdentifierStart('+'));
    }

    /**
     * @tests java.lang.Character#isUpperCase(char)
     */
    public void test_isUpperCaseC() {
        assertTrue("Incorrect case value", !Character.isUpperCase('t'));
        assertTrue("Incorrect case value", Character.isUpperCase('T'));
    }

    /**
     * @tests java.lang.Character#isWhitespace(char)
     */
    public void test_isWhitespaceC() {
        assertTrue("space returned false", Character.isWhitespace('\n'));
        assertTrue("non-space returned true", !Character.isWhitespace('T'));
    }

    /**
     * @tests java.lang.Character#reverseBytes(char)
     */
    public void test_reverseBytesC() {
        char original[] = new char[]{0x0000, 0x0010, 0x00AA, 0xB000, 0xCC00, 0xABCD, 0xFFAA};
        char reversed[] = new char[]{0x0000, 0x1000, 0xAA00, 0x00B0, 0x00CC, 0xCDAB, 0xAAFF};
        assertTrue("Test self check", original.length==reversed.length);

        for (int i=0; i<original.length; i++) {
            char origChar = original[i];
            char reversedChar = reversed[i];
            char origReversed= Character.reverseBytes(origChar);

            assertTrue("java.lang.Character.reverseBytes failed: orig char="
                +Integer.toHexString(origChar)+", reversed char="
                +Integer.toHexString(origReversed), reversedChar==origReversed);
        }
    }

    /**
     * @tests java.lang.Character#toLowerCase(char)
     */
    public void test_toLowerCaseC() {
        assertEquals("Failed to change case", 't', Character.toLowerCase('T'));
    }

    /**
     * @tests java.lang.Character#toString()
     */
    public void test_toString() {
        assertEquals("Incorrect String returned", "T", new Character('T').toString());
    }

    /**
     * @tests java.lang.Character#toTitleCase(char)
     */
    public void test_toTitleCaseC() {
        assertEquals("Incorrect title case for a",
                'A', Character.toTitleCase('a'));
        assertEquals("Incorrect title case for A",
                'A', Character.toTitleCase('A'));
        assertEquals("Incorrect title case for 1",
                '1', Character.toTitleCase('1'));
    }

    /**
     * @tests java.lang.Character#toUpperCase(char)
     */
    public void test_toUpperCaseC() {
        // Test for method char java.lang.Character.toUpperCase(char)
        assertEquals("Incorrect upper case for a",
                'A', Character.toUpperCase('a'));
        assertEquals("Incorrect upper case for A",
                'A', Character.toUpperCase('A'));
        assertEquals("Incorrect upper case for 1",
                '1', Character.toUpperCase('1'));
    }
}
