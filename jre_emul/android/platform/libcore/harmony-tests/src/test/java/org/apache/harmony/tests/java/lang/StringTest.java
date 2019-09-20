/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tests.java.lang;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.SortedMap;

import junit.framework.TestCase;

/**
 * Tests for the class {@link String}.
 */
public class StringTest extends TestCase {

    private static final Constructor<String> UNSAFE_CONSTRUCTOR;

    static {
        Constructor<String> uc;
        try {
            uc = String.class.getDeclaredConstructor(new Class[] { int.class,
                    int.class, char[].class });
            uc.setAccessible(true);
        } catch (Exception e) {
            uc = null;
        }
        UNSAFE_CONSTRUCTOR = uc;
    }

    private static String newString(int start, int len, char[] data) throws Exception {
        if (UNSAFE_CONSTRUCTOR == null) {
            return new String(data, start, len);
        }

        return UNSAFE_CONSTRUCTOR.newInstance(Integer.valueOf(start), Integer.valueOf(len),
                data);
    }

    public void test_contains() {
        assertTrue("aabc".contains("abc"));
        assertTrue("abcd".contains("abc"));
        assertFalse("abcd".contains("cba"));
    }

    public void test_charAt() {
        assertTrue("abcd".charAt(0) == 'a');
        assertTrue("abcd".charAt(3) == 'd');
    }

    public void test_StartsWith() {
        assertTrue("abcd".startsWith("abc"));
        assertFalse("abcd".startsWith("aabc"));
    }

    public void test_EndsWith() {
        assertTrue("abcd".endsWith("bcd"));
        assertFalse("abcd".endsWith("bcde"));
    }

    public void test_CASE_INSENSITIVE_ORDER() {
        String  s1 = "ABCDEFG";
        String  s2 = "abcdefg";

        assertTrue(String.CASE_INSENSITIVE_ORDER.compare(s1, s2) == 0);
    }

    public void test_Constructor() {
        assertEquals("Created incorrect string", "", new String());
    }

    public void test_Constructor$B() {
        assertEquals("Failed to create string", "HelloWorld", new String(
                "HelloWorld".getBytes()));
    }

    /**
     * @tests java.lang.String#String(byte[], int)
     *
    @SuppressWarnings("deprecation")
    public void test_Constructor$BI() {
        String s = new String(new byte[] { 65, 66, 67, 68, 69 }, 0);
        assertEquals("Incorrect string returned: " + s, "ABCDE", s);
        s = new String(new byte[] { 65, 66, 67, 68, 69 }, 1);
        assertFalse("Did not use nonzero hibyte", s.equals("ABCDE"));
    }
    */

    public void test_Constructor$BII() {
        byte[] hwba = "HelloWorld".getBytes();
        assertEquals("Failed to create string", "HelloWorld", new String(hwba,
                0, hwba.length));

        try {
            new String(new byte[0], 0, Integer.MAX_VALUE);
            fail("No IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * @tests java.lang.String#String(byte[], int, int, int)
     *
    @SuppressWarnings("deprecation")
    public void test_Constructor$BIII() {
        String s = new String(new byte[] { 65, 66, 67, 68, 69 }, 0, 1, 3);
        assertEquals("Incorrect string returned: " + s, "BCD", s);
        s = new String(new byte[] { 65, 66, 67, 68, 69 }, 1, 0, 5);
        assertFalse("Did not use nonzero hibyte", s.equals("ABCDE"));
    }
    */

    public void test_Constructor$BIILjava_lang_String() throws Exception {
        String s = new String(new byte[] { 65, 66, 67, 68, 69 }, 0, 5, "8859_1");
        assertEquals("Incorrect string returned: " + s, "ABCDE", s);

        try {
            new String(new byte[] { 65, 66, 67, 68, 69 }, 0, 5, "");
            fail("Should throw UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e) {
            //expected
        }
    }

    public void test_Constructor$BLjava_lang_String() throws Exception {
        String s = new String(new byte[] { 65, 66, 67, 68, 69 }, "8859_1");
        assertEquals("Incorrect string returned: " + s, "ABCDE", s);
    }

    public void test_Constructor$C() {
        assertEquals("Failed Constructor test", "World", new String(new char[] {
                'W', 'o', 'r', 'l', 'd' }));
    }

    public void test_Constructor$CII() throws Exception {
        char[] buf = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };
        String s = new String(buf, 0, buf.length);
        assertEquals("Incorrect string created", "HelloWorld", s);

        try {
            new String(new char[0], 0, Integer.MAX_VALUE);
            fail("No IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_ConstructorLjava_lang_String() {
        String s = new String("Hello World");
        assertEquals("Failed to construct correct string", "Hello World", s);
    }

    public void test_ConstructorLjava_lang_StringBuffer() {
        StringBuffer sb = new StringBuffer();
        sb.append("HelloWorld");
        assertEquals("Created incorrect string", "HelloWorld", new String(sb));
    }

    public void test_ConstructorLjava_lang_StringBuilder() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("HelloWorld");
        assertEquals("HelloWorld", new String(sb));

        try {
            new String((StringBuilder) null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.lang.String#String(int[],int,int)
     *
    public void test_Constructor$III() {
        assertEquals("HelloWorld", new String(new int[] { 'H', 'e', 'l', 'l',
                'o', 'W', 'o', 'r', 'l', 'd' }, 0, 10));
        assertEquals("Hello", new String(new int[] { 'H', 'e', 'l', 'l', 'o',
                'W', 'o', 'r', 'l', 'd' }, 0, 5));
        assertEquals("World", new String(new int[] { 'H', 'e', 'l', 'l', 'o',
                'W', 'o', 'r', 'l', 'd' }, 5, 5));
        assertEquals("", new String(new int[] { 'H', 'e', 'l', 'l', 'o', 'W',
                'o', 'r', 'l', 'd' }, 5, 0));

        assertEquals("\uD800\uDC00", new String(new int[] { 0x010000 }, 0, 1));
        assertEquals("\uD800\uDC00a\uDBFF\uDFFF", new String(new int[] {
                0x010000, 'a', 0x010FFFF }, 0, 3));

        try {
            new String((int[]) null, 0, 1);
            fail("No NPE");
        } catch (NullPointerException e) {
        }

        try {
            new String(new int[] { 'a', 'b' }, -1, 2);
            fail("No IOOBE, negative offset");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            new String(new int[] { 'a', 'b' }, 0, -1);
            fail("No IOOBE, negative count");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            new String(new int[] { 'a', 'b' }, 0, -1);
            fail("No IOOBE, negative count");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            new String(new int[] { 'a', 'b' }, 0, 3);
            fail("No IOOBE, too large");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    */

    /**
     * @tests java.lang.String#contentEquals(CharSequence)
     *
    public void test_contentEqualsLjava_lang_CharSequence() throws Exception {
        String s = "abc";
        assertTrue(s.contentEquals((CharSequence) new StringBuffer("abc")));
        assertFalse(s.contentEquals((CharSequence) new StringBuffer("def")));
        assertFalse(s.contentEquals((CharSequence) new StringBuffer("ghij")));

        s = newString(1, 3, "_abc_".toCharArray());
        assertTrue(s.contentEquals((CharSequence) new StringBuffer("abc")));
        assertFalse(s.contentEquals((CharSequence) new StringBuffer("def")));
        assertFalse(s.contentEquals((CharSequence) new StringBuffer("ghij")));

        try {
            s.contentEquals((CharSequence) null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }
    */
    
    /**
     * @tests java.lang.String#contentEquals(StringBuffer)
     *
    @SuppressWarnings("nls")
    public void test_boolean_contentEquals_StringBuffer() throws Exception {
        String s = "abc";
        assertTrue(s.contentEquals(new StringBuffer("abc")));
        assertFalse(s.contentEquals(new StringBuffer("def")));
        assertFalse(s.contentEquals(new StringBuffer("ghij")));

        s = newString(1, 3, "_abc_".toCharArray());
        assertTrue(s.contentEquals(new StringBuffer("abc")));
        assertFalse(s.contentEquals(new StringBuffer("def")));
        assertFalse(s.contentEquals(new StringBuffer("ghij")));

        try {
            s.contentEquals((StringBuffer) null);
            fail("Should throw a NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    */

    /**
     * @tests java.lang.String#contains(CharSequence)
     *
    @SuppressWarnings("cast")
    public void test_containsLjava_lang_CharSequence() throws Exception {
        String s = "abcdefghijklmnopqrstuvwxyz";
        assertTrue(s.contains((CharSequence) new StringBuffer("abc")));
        assertTrue(s.contains((CharSequence) new StringBuffer("def")));
        assertFalse(s.contains((CharSequence) new StringBuffer("ac")));

        s = newString(1, 26, "_abcdefghijklmnopqrstuvwxyz_".toCharArray());
        assertTrue(s.contains((CharSequence) new StringBuffer("abc")));
        assertTrue(s.contains((CharSequence) new StringBuffer("def")));
        assertFalse(s.contains((CharSequence) new StringBuffer("ac")));

        try {
            s.contentEquals((CharSequence) null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }
    */

    /**
     * @tests java.lang.String.offsetByCodePoints(int, int)'
     *
    public void test_offsetByCodePoints_II() throws Exception {
        int result = new String("a\uD800\uDC00b").offsetByCodePoints(0, 2);
        assertEquals(3, result);

        result = new String("abcd").offsetByCodePoints(3, -1);
        assertEquals(2, result);

        result = new String("a\uD800\uDC00b").offsetByCodePoints(0, 3);
        assertEquals(4, result);

        result = new String("a\uD800\uDC00b").offsetByCodePoints(3, -1);
        assertEquals(1, result);

        result = new String("a\uD800\uDC00b").offsetByCodePoints(3, 0);
        assertEquals(3, result);

        result = new String("\uD800\uDC00bc").offsetByCodePoints(3, 0);
        assertEquals(3, result);

        result = new String("a\uDC00bc").offsetByCodePoints(3, -1);
        assertEquals(2, result);

        result = new String("a\uD800bc").offsetByCodePoints(3, -1);
        assertEquals(2, result);

        result = newString(2, 4, "__a\uD800\uDC00b__".toCharArray())
                .offsetByCodePoints(0, 2);
        assertEquals(3, result);

        result = newString(2, 4, "__abcd__".toCharArray()).offsetByCodePoints(
                3, -1);
        assertEquals(2, result);

        result = newString(2, 4, "__a\uD800\uDC00b__".toCharArray())
                .offsetByCodePoints(0, 3);
        assertEquals(4, result);

        result = newString(2, 4, "__a\uD800\uDC00b__".toCharArray())
                .offsetByCodePoints(3, -1);
        assertEquals(1, result);

        result = newString(2, 4, "__a\uD800\uDC00b__".toCharArray())
                .offsetByCodePoints(3, 0);
        assertEquals(3, result);

        result = newString(2, 4, "__\uD800\uDC00bc__".toCharArray())
                .offsetByCodePoints(3, 0);
        assertEquals(3, result);

        result = newString(2, 4, "__a\uDC00bc__".toCharArray())
                .offsetByCodePoints(3, -1);
        assertEquals(2, result);

        result = newString(2, 4, "__a\uD800bc__".toCharArray())
                .offsetByCodePoints(3, -1);
        assertEquals(2, result);

        String s = "abc";
        try {
            s.offsetByCodePoints(-1, 1);
            fail("No IOOBE for negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.offsetByCodePoints(0, 4);
            fail("No IOOBE for offset that's too large.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.offsetByCodePoints(3, -4);
            fail("No IOOBE for offset that's too small.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.offsetByCodePoints(3, 1);
            fail("No IOOBE for index that's too large.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.offsetByCodePoints(4, -1);
            fail("No IOOBE for index that's too large.");
        } catch (IndexOutOfBoundsException e) {
        }
        
        s = newString(2,3,"__abc__".toCharArray());
        try {
            s.offsetByCodePoints(-1, 1);
            fail("No IOOBE for negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.offsetByCodePoints(0, 4);
            fail("No IOOBE for offset that's too large.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.offsetByCodePoints(3, -4);
            fail("No IOOBE for offset that's too small.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.offsetByCodePoints(3, 1);
            fail("No IOOBE for index that's too large.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.offsetByCodePoints(4, -1);
            fail("No IOOBE for index that's too large.");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    */

    public void test_codePointAtI() throws Exception {
        String s = "abc";
        assertEquals('a', s.codePointAt(0));
        assertEquals('b', s.codePointAt(1));
        assertEquals('c', s.codePointAt(2));

        s = newString(2, 3, "__abc__".toCharArray());
        assertEquals('a', s.codePointAt(0));
        assertEquals('b', s.codePointAt(1));
        assertEquals('c', s.codePointAt(2));

        s = "\uD800\uDC00";
        assertEquals(0x10000, s.codePointAt(0));
        assertEquals('\uDC00', s.codePointAt(1));

        s = newString(2, 2, "__\uD800\uDC00__".toCharArray());
        assertEquals(0x10000, s.codePointAt(0));
        assertEquals('\uDC00', s.codePointAt(1));

        s = "abc";
        try {
            s.codePointAt(-1);
            fail("No IOOBE on negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointAt(s.length());
            fail("No IOOBE on index equal to length.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointAt(s.length() + 1);
            fail("No IOOBE on index greater than length.");
        } catch (IndexOutOfBoundsException e) {
        }

        s = newString(2, 3, "__abc__".toCharArray());
        try {
            s.codePointAt(-1);
            fail("No IOOBE on negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointAt(s.length());
            fail("No IOOBE on index equal to length.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointAt(s.length() + 1);
            fail("No IOOBE on index greater than length.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_codePointBeforeI() throws Exception {
        String s = "abc";
        assertEquals('a', s.codePointBefore(1));
        assertEquals('b', s.codePointBefore(2));
        assertEquals('c', s.codePointBefore(3));

        s = newString(2, 3, "__abc__".toCharArray());
        assertEquals('a', s.codePointBefore(1));
        assertEquals('b', s.codePointBefore(2));
        assertEquals('c', s.codePointBefore(3));

        s = "\uD800\uDC00";
        assertEquals(0x10000, s.codePointBefore(2));
        assertEquals('\uD800', s.codePointBefore(1));

        s = newString(2, 2, "__\uD800\uDC00__".toCharArray());
        assertEquals(0x10000, s.codePointBefore(2));
        assertEquals('\uD800', s.codePointBefore(1));

        s = "abc";
        try {
            s.codePointBefore(0);
            fail("No IOOBE on zero index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointBefore(-1);
            fail("No IOOBE on negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointBefore(s.length() + 1);
            fail("No IOOBE on index greater than length.");
        } catch (IndexOutOfBoundsException e) {
        }

        s = newString(2, 3, "__abc__".toCharArray());
        try {
            s.codePointBefore(0);
            fail("No IOOBE on zero index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointBefore(-1);
            fail("No IOOBE on negative index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointBefore(s.length() + 1);
            fail("No IOOBE on index greater than length.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_codePointCountII() throws Exception {
        assertEquals(1, "\uD800\uDC00".codePointCount(0, 2));
        assertEquals(1, "\uD800\uDC01".codePointCount(0, 2));
        assertEquals(1, "\uD801\uDC01".codePointCount(0, 2));
        assertEquals(1, "\uDBFF\uDFFF".codePointCount(0, 2));

        assertEquals(3, "a\uD800\uDC00b".codePointCount(0, 4));
        assertEquals(4, "a\uD800\uDC00b\uD800".codePointCount(0, 5));

        assertEquals(1, newString(2, 2, "__\uD800\uDC00__".toCharArray()).codePointCount(0, 2));
        assertEquals(1, newString(2, 2, "__\uD800\uDC01__".toCharArray()).codePointCount(0, 2));
        assertEquals(1, newString(2, 2, "__\uD801\uDC01__".toCharArray()).codePointCount(0, 2));
        assertEquals(1, newString(2, 2, "__\uDBFF\uDFFF__".toCharArray()).codePointCount(0, 2));

        assertEquals(3, newString(2, 4, "__a\uD800\uDC00b__".toCharArray()).codePointCount(0, 4));
        assertEquals(4, newString(2, 5, "__a\uD800\uDC00b\uD800__".toCharArray()).codePointCount(0, 5));

        String s = "abc";
        try {
            s.codePointCount(-1, 2);
            fail("No IOOBE for negative begin index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointCount(0, 4);
            fail("No IOOBE for end index that's too large.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointCount(3, 2);
            fail("No IOOBE for begin index larger than end index.");
        } catch (IndexOutOfBoundsException e) {
        }

        s = newString(2, 3, "__abc__".toCharArray());
        try {
            s.codePointCount(-1, 2);
            fail("No IOOBE for negative begin index.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointCount(0, 4);
            fail("No IOOBE for end index that's too large.");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            s.codePointCount(3, 2);
            fail("No IOOBE for begin index larger than end index.");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void test_isEmpty() throws Exception {
        assertTrue(new String(new byte[0], Charset.defaultCharset()).isEmpty());
        assertTrue(new String(new byte[8], Charset.defaultCharset()).substring(0, 0).isEmpty());
    }

    public void test_getBytesLCharset() throws Exception {
        byte[] emptyBytes = new byte[0];
        byte[] someBytes = new byte[] { 'T', 'h', 'i', 's', ' ', ' ', 'i', 's', ' ', 't', 'e', 's', 't', ' ', 'b', 'y', 't', 'e', 's' };
        assertEquals(0, new String(emptyBytes, Charset.defaultCharset()).getBytes(Charset.defaultCharset()).length);
        try {
            new String(emptyBytes, Charset.defaultCharset()).getBytes((Charset) null);
            fail("should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        assertTrue(bytesEquals(someBytes, new String(someBytes, Charset.defaultCharset()).getBytes(Charset.defaultCharset())));
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
    }

    boolean bytesEquals(byte[] bytes1, byte[] bytes2) {
        return Arrays.toString(bytes1).equals(Arrays.toString(bytes2));
    }
}
