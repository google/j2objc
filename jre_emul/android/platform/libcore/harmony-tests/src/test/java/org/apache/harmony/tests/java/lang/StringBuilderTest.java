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

import java.io.Serializable;
import java.util.Arrays;

import junit.framework.TestCase;

// import org.apache.harmony.testframework.serialization.SerializationTest;
// import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class StringBuilderTest extends TestCase {

    /**
     * java.lang.StringBuilder.StringBuilder()
     */
    public void test_Constructor() {
        StringBuilder sb = new StringBuilder();
        assertNotNull(sb);
        assertEquals(16, sb.capacity());
    }

    /**
     * java.lang.StringBuilder.StringBuilder(int)
     */
    public void test_ConstructorI() {
        StringBuilder sb = new StringBuilder(24);
        assertNotNull(sb);
        assertEquals(24, sb.capacity());

        try {
            new StringBuilder(-1);
            fail("no exception");
        } catch (NegativeArraySizeException e) {
            // Expected
        }

        assertNotNull(new StringBuilder(0));
    }

    /**
     * java.lang.StringBuilder.StringBuilder(CharSequence)
     */
    @SuppressWarnings("cast")
    public void test_ConstructorLjava_lang_CharSequence() {
        StringBuilder sb = new StringBuilder((CharSequence) "fixture");
        assertEquals("fixture", sb.toString());
        assertEquals("fixture".length() + 16, sb.capacity());

        sb = new StringBuilder((CharSequence) new StringBuffer("fixture"));
        assertEquals("fixture", sb.toString());
        assertEquals("fixture".length() + 16, sb.capacity());

        try {
            new StringBuilder((CharSequence) null);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.StringBuilder(String)
     */
    public void test_ConstructorLjava_lang_String() {
        StringBuilder sb = new StringBuilder("fixture");
        assertEquals("fixture", sb.toString());
        assertEquals("fixture".length() + 16, sb.capacity());

        try {
            new StringBuilder((String) null);
            fail("no NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * java.lang.StringBuilder.append(boolean)
     */
    public void test_appendZ() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(true));
        assertEquals("true", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(false));
        assertEquals("false", sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(char)
     */
    public void test_appendC() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append('a'));
        assertEquals("a", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append('b'));
        assertEquals("b", sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(char[])
     */
    public void test_append$C() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(new char[] { 'a', 'b' }));
        assertEquals("ab", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(new char[] { 'c', 'd' }));
        assertEquals("cd", sb.toString());
        try {
            sb.append((char[]) null);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.append(char[], int, int)
     */
    public void test_append$CII() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(new char[] { 'a', 'b' }, 0, 2));
        assertEquals("ab", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(new char[] { 'c', 'd' }, 0, 2));
        assertEquals("cd", sb.toString());

        sb.setLength(0);
        assertSame(sb, sb.append(new char[] { 'a', 'b', 'c', 'd' }, 0, 2));
        assertEquals("ab", sb.toString());

        sb.setLength(0);
        assertSame(sb, sb.append(new char[] { 'a', 'b', 'c', 'd' }, 2, 2));
        assertEquals("cd", sb.toString());

        sb.setLength(0);
        assertSame(sb, sb.append(new char[] { 'a', 'b', 'c', 'd' }, 2, 0));
        assertEquals("", sb.toString());

        try {
            sb.append((char[]) null, 0, 2);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            sb.append(new char[] { 'a', 'b', 'c', 'd' }, -1, 2);
            fail("no IOOBE, negative offset");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.append(new char[] { 'a', 'b', 'c', 'd' }, 0, -1);
            fail("no IOOBE, negative length");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.append(new char[] { 'a', 'b', 'c', 'd' }, 2, 3);
            fail("no IOOBE, offset and length overflow");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.append(CharSequence)
     */
    public void test_appendLjava_lang_CharSequence() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append((CharSequence) "ab"));
        assertEquals("ab", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append((CharSequence) "cd"));
        assertEquals("cd", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append((CharSequence) null));
        assertEquals("null", sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(CharSequence, int, int)
     */
    @SuppressWarnings("cast")
    public void test_appendLjava_lang_CharSequenceII() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append((CharSequence) "ab", 0, 2));
        assertEquals("ab", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append((CharSequence) "cd", 0, 2));
        assertEquals("cd", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append((CharSequence) "abcd", 0, 2));
        assertEquals("ab", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append((CharSequence) "abcd", 2, 4));
        assertEquals("cd", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append((CharSequence) null, 0, 2));
        assertEquals("nu", sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(double)
     */
    public void test_appendD() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(1D));
        assertEquals(String.valueOf(1D), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(0D));
        assertEquals(String.valueOf(0D), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(-1D));
        assertEquals(String.valueOf(-1D), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Double.NaN));
        assertEquals(String.valueOf(Double.NaN), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Double.NEGATIVE_INFINITY));
        assertEquals(String.valueOf(Double.NEGATIVE_INFINITY), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Double.POSITIVE_INFINITY));
        assertEquals(String.valueOf(Double.POSITIVE_INFINITY), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Double.MIN_VALUE));
        assertEquals(String.valueOf(Double.MIN_VALUE), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Double.MAX_VALUE));
        assertEquals(String.valueOf(Double.MAX_VALUE), sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(float)
     */
    public void test_appendF() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(1F));
        assertEquals(String.valueOf(1F), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(0F));
        assertEquals(String.valueOf(0F), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(-1F));
        assertEquals(String.valueOf(-1F), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Float.NaN));
        assertEquals(String.valueOf(Float.NaN), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Float.NEGATIVE_INFINITY));
        assertEquals(String.valueOf(Float.NEGATIVE_INFINITY), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Float.POSITIVE_INFINITY));
        assertEquals(String.valueOf(Float.POSITIVE_INFINITY), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Float.MIN_VALUE));
        assertEquals(String.valueOf(Float.MIN_VALUE), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Float.MAX_VALUE));
        assertEquals(String.valueOf(Float.MAX_VALUE), sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(int)
     */
    public void test_appendI() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(1));
        assertEquals(String.valueOf(1), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(0));
        assertEquals(String.valueOf(0), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(-1));
        assertEquals(String.valueOf(-1), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Integer.MIN_VALUE));
        assertEquals(String.valueOf(Integer.MIN_VALUE), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Integer.MAX_VALUE));
        assertEquals(String.valueOf(Integer.MAX_VALUE), sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(long)
     */
    public void test_appendL() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(1L));
        assertEquals(String.valueOf(1L), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(0L));
        assertEquals(String.valueOf(0L), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(-1L));
        assertEquals(String.valueOf(-1L), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Integer.MIN_VALUE));
        assertEquals(String.valueOf(Integer.MIN_VALUE), sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(Integer.MAX_VALUE));
        assertEquals(String.valueOf(Integer.MAX_VALUE), sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(Object)'
     */
    public void test_appendLjava_lang_Object() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(Fixture.INSTANCE));
        assertEquals(Fixture.INSTANCE.toString(), sb.toString());

        sb.setLength(0);
        assertSame(sb, sb.append((Object) null));
        assertEquals("null", sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(String)
     */
    public void test_appendLjava_lang_String() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append("ab"));
        assertEquals("ab", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append("cd"));
        assertEquals("cd", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append((String) null));
        assertEquals("null", sb.toString());
    }

    /**
     * java.lang.StringBuilder.append(StringBuffer)
     */
    public void test_appendLjava_lang_StringBuffer() {
        StringBuilder sb = new StringBuilder();
        assertSame(sb, sb.append(new StringBuffer("ab")));
        assertEquals("ab", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append(new StringBuffer("cd")));
        assertEquals("cd", sb.toString());
        sb.setLength(0);
        assertSame(sb, sb.append((StringBuffer) null));
        assertEquals("null", sb.toString());
    }

    /**
     * java.lang.StringBuilder.appendCodePoint(int)'
     */
    public void test_appendCodePointI() {
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(0x10000);
        assertEquals("\uD800\uDC00", sb.toString());
        sb.append("fixture");
        assertEquals("\uD800\uDC00fixture", sb.toString());
        sb.appendCodePoint(0x00010FFFF);
        assertEquals("\uD800\uDC00fixture\uDBFF\uDFFF", sb.toString());
    }

    /**
     * java.lang.StringBuilder.capacity()'
     */
    public void test_capacity() {
        StringBuilder sb = new StringBuilder();
        assertEquals(16, sb.capacity());
        sb.append("0123456789ABCDEF0123456789ABCDEF");
        assertTrue(sb.capacity() > 16);
    }

    /**
     * java.lang.StringBuilder.charAt(int)'
     */
    public void test_charAtI() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        for (int i = 0; i < fixture.length(); i++) {
            assertEquals((char) ('0' + i), sb.charAt(i));
        }

        try {
            sb.charAt(-1);
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.charAt(fixture.length());
            fail("no IOOBE, equal to length");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            sb.charAt(fixture.length() + 1);
            fail("no IOOBE, greater than length");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * java.lang.StringBuilder.codePointAt(int)
     */
    public void test_codePointAtI() {
        StringBuilder sb = new StringBuilder("abc");
        assertEquals('a', sb.codePointAt(0));
        assertEquals('b', sb.codePointAt(1));
        assertEquals('c', sb.codePointAt(2));

        sb = new StringBuilder("\uD800\uDC00");
        assertEquals(0x10000, sb.codePointAt(0));
        assertEquals('\uDC00', sb.codePointAt(1));

        sb = new StringBuilder();
        sb.append("abc");
        try {
            sb.codePointAt(-1);
            fail("No IOOBE on negative index.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.codePointAt(sb.length());
            fail("No IOOBE on index equal to length.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.codePointAt(sb.length() + 1);
            fail("No IOOBE on index greater than length.");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    /**
     * java.lang.StringBuilder.codePointBefore(int)
     */
    public void test_codePointBeforeI() {
        StringBuilder sb = new StringBuilder("abc");
        assertEquals('a', sb.codePointBefore(1));
        assertEquals('b', sb.codePointBefore(2));
        assertEquals('c', sb.codePointBefore(3));

        sb = new StringBuilder("\uD800\uDC00");
        assertEquals(0x10000, sb.codePointBefore(2));
        assertEquals('\uD800', sb.codePointBefore(1));

        sb = new StringBuilder();
        sb.append("abc");

        try {
            sb.codePointBefore(0);
            fail("No IOOBE on zero index.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.codePointBefore(-1);
            fail("No IOOBE on negative index.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.codePointBefore(sb.length() + 1);
            fail("No IOOBE on index greater than length.");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    /**
     * java.lang.StringBuilder.codePointCount(int, int)
     */
    public void test_codePointCountII() {
        assertEquals(1, new StringBuilder("\uD800\uDC00").codePointCount(0, 2));
        assertEquals(1, new StringBuilder("\uD800\uDC01").codePointCount(0, 2));
        assertEquals(1, new StringBuilder("\uD801\uDC01").codePointCount(0, 2));
        assertEquals(1, new StringBuilder("\uDBFF\uDFFF").codePointCount(0, 2));

        assertEquals(3, new StringBuilder("a\uD800\uDC00b").codePointCount(0, 4));
        assertEquals(4, new StringBuilder("a\uD800\uDC00b\uD800").codePointCount(0, 5));

        StringBuilder sb = new StringBuilder();
        sb.append("abc");
        try {
            sb.codePointCount(-1, 2);
            fail("No IOOBE for negative begin index.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.codePointCount(0, 4);
            fail("No IOOBE for end index that's too large.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.codePointCount(3, 2);
            fail("No IOOBE for begin index larger than end index.");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    /**
     * java.lang.StringBuilder.delete(int, int)
     */
    public void test_deleteII() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.delete(0, 0));
        assertEquals(fixture, sb.toString());
        assertSame(sb, sb.delete(5, 5));
        assertEquals(fixture, sb.toString());
        assertSame(sb, sb.delete(0, 1));
        assertEquals("123456789", sb.toString());
        assertEquals(9, sb.length());
        assertSame(sb, sb.delete(0, sb.length()));
        assertEquals("", sb.toString());
        assertEquals(0, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.delete(0, 11));
        assertEquals("", sb.toString());
        assertEquals(0, sb.length());

        try {
            new StringBuilder(fixture).delete(-1, 2);
            fail("no SIOOBE, negative start");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            new StringBuilder(fixture).delete(11, 12);
            fail("no SIOOBE, start too far");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            new StringBuilder(fixture).delete(13, 12);
            fail("no SIOOBE, start larger than end");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        // HARMONY 6212
        sb = new StringBuilder();
        sb.append("abcde");
        String str = sb.toString();
        sb.delete(0, sb.length());
        sb.append("YY");
        assertEquals("abcde", str);
        assertEquals("YY", sb.toString());
    }

    /**
     * java.lang.StringBuilder.deleteCharAt(int)
     */
    public void test_deleteCharAtI() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.deleteCharAt(0));
        assertEquals("123456789", sb.toString());
        assertEquals(9, sb.length());
        sb = new StringBuilder(fixture);
        assertSame(sb, sb.deleteCharAt(5));
        assertEquals("012346789", sb.toString());
        assertEquals(9, sb.length());
        sb = new StringBuilder(fixture);
        assertSame(sb, sb.deleteCharAt(9));
        assertEquals("012345678", sb.toString());
        assertEquals(9, sb.length());

        try {
            new StringBuilder(fixture).deleteCharAt(-1);
            fail("no SIOOBE, negative index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            new StringBuilder(fixture).deleteCharAt(fixture.length());
            fail("no SIOOBE, index equals length");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            new StringBuilder(fixture).deleteCharAt(fixture.length() + 1);
            fail("no SIOOBE, index exceeds length");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.ensureCapacity(int)'
     */
    public void test_ensureCapacityI() {
        StringBuilder sb = new StringBuilder(5);
        assertEquals(5, sb.capacity());
        sb.ensureCapacity(10);
        assertEquals(12, sb.capacity());
        sb.ensureCapacity(26);
        assertEquals(26, sb.capacity());
        sb.ensureCapacity(55);
        assertEquals(55, sb.capacity());
    }

    /**
     * java.lang.StringBuilder.getChars(int, int, char[], int)'
     */
    public void test_getCharsII$CI() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        char[] dst = new char[10];
        sb.getChars(0, 10, dst, 0);
        assertTrue(Arrays.equals(fixture.toCharArray(), dst));

        Arrays.fill(dst, '\0');
        sb.getChars(0, 5, dst, 0);
        char[] fixtureChars = new char[10];
        fixture.getChars(0, 5, fixtureChars, 0);
        assertTrue(Arrays.equals(fixtureChars, dst));

        Arrays.fill(dst, '\0');
        Arrays.fill(fixtureChars, '\0');
        sb.getChars(0, 5, dst, 5);
        fixture.getChars(0, 5, fixtureChars, 5);
        assertTrue(Arrays.equals(fixtureChars, dst));

        Arrays.fill(dst, '\0');
        Arrays.fill(fixtureChars, '\0');
        sb.getChars(5, 10, dst, 1);
        fixture.getChars(5, 10, fixtureChars, 1);
        assertTrue(Arrays.equals(fixtureChars, dst));

        try {
            sb.getChars(0, 10, null, 0);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            sb.getChars(-1, 10, dst, 0);
            fail("no IOOBE, srcBegin negative");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.getChars(0, 10, dst, -1);
            fail("no IOOBE, dstBegin negative");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.getChars(5, 4, dst, 0);
            fail("no IOOBE, srcBegin > srcEnd");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.getChars(0, 11, dst, 0);
            fail("no IOOBE, srcEnd > length");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.getChars(0, 10, dst, 5);
            fail("no IOOBE, dstBegin and src size too large for what's left in dst");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.indexOf(String)
     */
    public void test_indexOfLjava_lang_String() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertEquals(0, sb.indexOf("0"));
        assertEquals(0, sb.indexOf("012"));
        assertEquals(-1, sb.indexOf("02"));
        assertEquals(8, sb.indexOf("89"));

        try {
            sb.indexOf(null);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.indexOf(String, int)
     */
    public void test_IndexOfStringInt() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertEquals(0, sb.indexOf("0"));
        assertEquals(0, sb.indexOf("012"));
        assertEquals(-1, sb.indexOf("02"));
        assertEquals(8, sb.indexOf("89"));

        assertEquals(0, sb.indexOf("0"), 0);
        assertEquals(0, sb.indexOf("012"), 0);
        assertEquals(-1, sb.indexOf("02"), 0);
        assertEquals(8, sb.indexOf("89"), 0);

        assertEquals(-1, sb.indexOf("0"), 5);
        assertEquals(-1, sb.indexOf("012"), 5);
        assertEquals(-1, sb.indexOf("02"), 0);
        assertEquals(8, sb.indexOf("89"), 5);

        try {
            sb.indexOf(null, 0);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, boolean)
     */
    public void test_insertIZ() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, true));
        assertEquals("true0000", sb.toString());
        assertEquals(8, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, false));
        assertEquals("false0000", sb.toString());
        assertEquals(9, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, false));
        assertEquals("00false00", sb.toString());
        assertEquals(9, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, false));
        assertEquals("0000false", sb.toString());
        assertEquals(9, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, false);
            fail("no SIOOBE, negative index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, false);
            fail("no SIOOBE, index too large index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, char)
     */
    public void test_insertIC() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, 'a'));
        assertEquals("a0000", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, 'b'));
        assertEquals("b0000", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, 'b'));
        assertEquals("00b00", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, 'b'));
        assertEquals("0000b", sb.toString());
        assertEquals(5, sb.length());

        // FIXME this fails on Sun JRE 5.0_5
//		try {
//			sb = new StringBuilder(fixture);
//			sb.insert(-1, 'a');
//			fail("no SIOOBE, negative index");
//		} catch (StringIndexOutOfBoundsException e) {
//			// Expected
//		}

        /*
           * FIXME This fails on Sun JRE 5.0_5, but that seems like a bug, since
           * the 'insert(int, char[]) behaves this way.
           */
//		try {
//			sb = new StringBuilder(fixture);
//			sb.insert(5, 'a');
//			fail("no SIOOBE, index too large index");
//		} catch (StringIndexOutOfBoundsException e) {
//			// Expected
//		}
    }

    /**
     * java.lang.StringBuilder.insert(int, char)
     */
    public void test_insertIC_2() {
        StringBuilder obj = new StringBuilder();
        try {
            obj.insert(-1, '?');
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, char[])'
     */
    public void test_insertI$C() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, new char[] { 'a', 'b' }));
        assertEquals("ab0000", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, new char[] { 'a', 'b' }));
        assertEquals("00ab00", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, new char[] { 'a', 'b' }));
        assertEquals("0000ab", sb.toString());
        assertEquals(6, sb.length());

        /*
           * TODO This NPE is the behavior on Sun's JRE 5.0_5, but it's
           * undocumented. The assumption is that this method behaves like
           * String.valueOf(char[]), which does throw a NPE too, but that is also
           * undocumented.
           */

        try {
            sb.insert(0, (char[]) null);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, new char[] { 'a', 'b' });
            fail("no SIOOBE, negative index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, new char[] { 'a', 'b' });
            fail("no SIOOBE, index too large index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, char[], int, int)
     */
    public void test_insertI$CII() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, new char[] { 'a', 'b' }, 0, 2));
        assertEquals("ab0000", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, new char[] { 'a', 'b' }, 0, 1));
        assertEquals("a0000", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, new char[] { 'a', 'b' }, 0, 2));
        assertEquals("00ab00", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, new char[] { 'a', 'b' }, 0, 1));
        assertEquals("00a00", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, new char[] { 'a', 'b' }, 0, 2));
        assertEquals("0000ab", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, new char[] { 'a', 'b' }, 0, 1));
        assertEquals("0000a", sb.toString());
        assertEquals(5, sb.length());

        /*
           * TODO This NPE is the behavior on Sun's JRE 5.0_5, but it's
           * undocumented. The assumption is that this method behaves like
           * String.valueOf(char[]), which does throw a NPE too, but that is also
           * undocumented.
           */

        try {
            sb.insert(0, (char[]) null, 0, 2);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, new char[] { 'a', 'b' }, 0, 2);
            fail("no SIOOBE, negative index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, new char[] { 'a', 'b' }, 0, 2);
            fail("no SIOOBE, index too large index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, new char[] { 'a', 'b' }, -1, 2);
            fail("no SIOOBE, negative offset");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, new char[] { 'a', 'b' }, 0, -1);
            fail("no SIOOBE, negative length");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, new char[] { 'a', 'b' }, 0, 3);
            fail("no SIOOBE, too long");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, CharSequence)
     */
    public void test_insertILjava_lang_CharSequence() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, (CharSequence) "ab"));
        assertEquals("ab0000", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, (CharSequence) "ab"));
        assertEquals("00ab00", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) "ab"));
        assertEquals("0000ab", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) null));
        assertEquals("0000null", sb.toString());
        assertEquals(8, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, (CharSequence) "ab");
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, (CharSequence) "ab");
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, CharSequence, int, int)
     */
    @SuppressWarnings("cast")
    public void test_insertILjava_lang_CharSequenceII() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, (CharSequence) "ab", 0, 2));
        assertEquals("ab0000", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, (CharSequence) "ab", 0, 1));
        assertEquals("a0000", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, (CharSequence) "ab", 0, 2));
        assertEquals("00ab00", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, (CharSequence) "ab", 0, 1));
        assertEquals("00a00", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) "ab", 0, 2));
        assertEquals("0000ab", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) "ab", 0, 1));
        assertEquals("0000a", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) null, 0, 2));
        assertEquals("0000nu", sb.toString());
        assertEquals(6, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, (CharSequence) "ab", 0, 2);
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, (CharSequence) "ab", 0, 2);
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, (CharSequence) "ab", -1, 2);
            fail("no IOOBE, negative offset");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, new char[] { 'a', 'b' }, 0, -1);
            fail("no IOOBE, negative length");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, new char[] { 'a', 'b' }, 0, 3);
            fail("no IOOBE, too long");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, double)
     */
    public void test_insertID() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, -1D));
        assertEquals("-1.00000", sb.toString());
        assertEquals(8, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, 0D));
        assertEquals("0.00000", sb.toString());
        assertEquals(7, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, 1D));
        assertEquals("001.000", sb.toString());
        assertEquals(7, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, 2D));
        assertEquals("00002.0", sb.toString());
        assertEquals(7, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, 1D);
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, 1D);
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, float)
     */
    public void test_insertIF() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, -1F));
        assertEquals("-1.00000", sb.toString());
        assertEquals(8, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, 0F));
        assertEquals("0.00000", sb.toString());
        assertEquals(7, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, 1F));
        assertEquals("001.000", sb.toString());
        assertEquals(7, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, 2F));
        assertEquals("00002.0", sb.toString());
        assertEquals(7, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, 1F);
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, 1F);
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, int)
     */
    public void test_insertII() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, -1));
        assertEquals("-10000", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, 0));
        assertEquals("00000", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, 1));
        assertEquals("00100", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, 2));
        assertEquals("00002", sb.toString());
        assertEquals(5, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, 1);
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, 1);
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, long)
     */
    public void test_insertIJ() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, -1L));
        assertEquals("-10000", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, 0L));
        assertEquals("00000", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, 1L));
        assertEquals("00100", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, 2L));
        assertEquals("00002", sb.toString());
        assertEquals(5, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, 1L);
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, 1L);
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, Object)
     */
    public void test_insertILjava_lang_Object() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, Fixture.INSTANCE));
        assertEquals("fixture0000", sb.toString());
        assertEquals(11, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, Fixture.INSTANCE));
        assertEquals("00fixture00", sb.toString());
        assertEquals(11, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, Fixture.INSTANCE));
        assertEquals("0000fixture", sb.toString());
        assertEquals(11, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, (Object) null));
        assertEquals("0000null", sb.toString());
        assertEquals(8, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, Fixture.INSTANCE);
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, Fixture.INSTANCE);
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.insert(int, String)
     */
    public void test_insertILjava_lang_String() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(0, "fixture"));
        assertEquals("fixture0000", sb.toString());
        assertEquals(11, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(2, "fixture"));
        assertEquals("00fixture00", sb.toString());
        assertEquals(11, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, "fixture"));
        assertEquals("0000fixture", sb.toString());
        assertEquals(11, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.insert(4, (Object) null));
        assertEquals("0000null", sb.toString());
        assertEquals(8, sb.length());

        try {
            sb = new StringBuilder(fixture);
            sb.insert(-1, "fixture");
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.insert(5, "fixture");
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.lastIndexOf(String)
     */
    public void test_lastIndexOfLjava_lang_String() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertEquals(0, sb.lastIndexOf("0"));
        assertEquals(0, sb.lastIndexOf("012"));
        assertEquals(-1, sb.lastIndexOf("02"));
        assertEquals(8, sb.lastIndexOf("89"));

        try {
            sb.lastIndexOf(null);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.lastIndexOf(String, int)
     */
    public void test_lastIndexOfLjava_lang_StringI() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertEquals(0, sb.lastIndexOf("0"));
        assertEquals(0, sb.lastIndexOf("012"));
        assertEquals(-1, sb.lastIndexOf("02"));
        assertEquals(8, sb.lastIndexOf("89"));

        assertEquals(0, sb.lastIndexOf("0"), 0);
        assertEquals(0, sb.lastIndexOf("012"), 0);
        assertEquals(-1, sb.lastIndexOf("02"), 0);
        assertEquals(8, sb.lastIndexOf("89"), 0);

        assertEquals(-1, sb.lastIndexOf("0"), 5);
        assertEquals(-1, sb.lastIndexOf("012"), 5);
        assertEquals(-1, sb.lastIndexOf("02"), 0);
        assertEquals(8, sb.lastIndexOf("89"), 5);

        try {
            sb.lastIndexOf(null, 0);
            fail("no NPE");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.length()
     */
    public void test_length() {
        StringBuilder sb = new StringBuilder();
        assertEquals(0, sb.length());
        sb.append("0000");
        assertEquals(4, sb.length());
    }

    /**
     * java.lang.StringBuilder.offsetByCodePoints(int, int)'
     */
    public void test_offsetByCodePointsII() {
        int result = new StringBuilder("a\uD800\uDC00b").offsetByCodePoints(0, 2);
        assertEquals(3, result);

        result = new StringBuilder("abcd").offsetByCodePoints(3, -1);
        assertEquals(2, result);

        result = new StringBuilder("a\uD800\uDC00b").offsetByCodePoints(0, 3);
        assertEquals(4, result);

        result = new StringBuilder("a\uD800\uDC00b").offsetByCodePoints(3, -1);
        assertEquals(1, result);

        result = new StringBuilder("a\uD800\uDC00b").offsetByCodePoints(3, 0);
        assertEquals(3, result);

        result = new StringBuilder("\uD800\uDC00bc").offsetByCodePoints(3, 0);
        assertEquals(3, result);

        result = new StringBuilder("a\uDC00bc").offsetByCodePoints(3, -1);
        assertEquals(2, result);

        result = new StringBuilder("a\uD800bc").offsetByCodePoints(3, -1);
        assertEquals(2, result);

        StringBuilder sb = new StringBuilder();
        sb.append("abc");
        try {
            sb.offsetByCodePoints(-1, 1);
            fail("No IOOBE for negative index.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.offsetByCodePoints(0, 4);
            fail("No IOOBE for offset that's too large.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.offsetByCodePoints(3, -4);
            fail("No IOOBE for offset that's too small.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.offsetByCodePoints(3, 1);
            fail("No IOOBE for index that's too large.");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            sb.offsetByCodePoints(4, -1);
            fail("No IOOBE for index that's too large.");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    /**
     * java.lang.StringBuilder.replace(int, int, String)'
     */
    public void test_replaceIILjava_lang_String() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.replace(1, 3, "11"));
        assertEquals("0110", sb.toString());
        assertEquals(4, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.replace(1, 2, "11"));
        assertEquals("01100", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.replace(4, 5, "11"));
        assertEquals("000011", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuilder(fixture);
        assertSame(sb, sb.replace(4, 6, "11"));
        assertEquals("000011", sb.toString());
        assertEquals(6, sb.length());

        // FIXME Undocumented NPE in Sun's JRE 5.0_5
        try {
            sb.replace(1, 2, null);
            fail("No NPE");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.replace(-1, 2, "11");
            fail("No SIOOBE, negative start");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.replace(5, 2, "11");
            fail("No SIOOBE, start > length");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuilder(fixture);
            sb.replace(3, 2, "11");
            fail("No SIOOBE, start > end");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        // Regression for HARMONY-348
        StringBuilder buffer = new StringBuilder("1234567");
        buffer.replace(2, 6, "XXX");
        assertEquals("12XXX7", buffer.toString());
    }

    private void reverseTest(String org, String rev, String back) {
        // create non-shared StringBuilder
        StringBuilder sb = new StringBuilder(org);
        sb.reverse();
        String reversed = sb.toString();
        assertEquals(rev, reversed);
        // create non-shared StringBuilder
        sb = new StringBuilder(reversed);
        sb.reverse();
        reversed = sb.toString();
        assertEquals(back, reversed);

        // test algorithm when StringBuilder is shared
        sb = new StringBuilder(org);
        String copy = sb.toString();
        assertEquals(org, copy);
        sb.reverse();
        reversed = sb.toString();
        assertEquals(rev, reversed);
        sb = new StringBuilder(reversed);
        copy = sb.toString();
        assertEquals(rev, copy);
        sb.reverse();
        reversed = sb.toString();
        assertEquals(back, reversed);
    }

    /**
     * java.lang.StringBuilder.reverse()
     */
    public void test_reverse() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertSame(sb, sb.reverse());
        assertEquals("9876543210", sb.toString());

        sb = new StringBuilder("012345678");
        assertSame(sb, sb.reverse());
        assertEquals("876543210", sb.toString());

        sb.setLength(1);
        assertSame(sb, sb.reverse());
        assertEquals("8", sb.toString());

        sb.setLength(0);
        assertSame(sb, sb.reverse());
        assertEquals("", sb.toString());

        String str;
        str = "a";
        reverseTest(str, str, str);

        str = "ab";
        reverseTest(str, "ba", str);

        str = "abcdef";
        reverseTest(str, "fedcba", str);

        str = "abcdefg";
        reverseTest(str, "gfedcba", str);

        str = "\ud800\udc00";
        reverseTest(str, str, str);

        str = "\udc00\ud800";
        reverseTest(str, "\ud800\udc00", "\ud800\udc00");

        str = "a\ud800\udc00";
        reverseTest(str, "\ud800\udc00a", str);

        str = "ab\ud800\udc00";
        reverseTest(str, "\ud800\udc00ba", str);

        str = "abc\ud800\udc00";
        reverseTest(str, "\ud800\udc00cba", str);

        str = "\ud800\udc00\udc01\ud801\ud802\udc02";
        reverseTest(str, "\ud802\udc02\ud801\udc01\ud800\udc00",
                "\ud800\udc00\ud801\udc01\ud802\udc02");

        str = "\ud800\udc00\ud801\udc01\ud802\udc02";
        reverseTest(str, "\ud802\udc02\ud801\udc01\ud800\udc00", str);

        str = "\ud800\udc00\udc01\ud801a";
        reverseTest(str, "a\ud801\udc01\ud800\udc00",
                "\ud800\udc00\ud801\udc01a");

        str = "a\ud800\udc00\ud801\udc01";
        reverseTest(str, "\ud801\udc01\ud800\udc00a", str);

        str = "\ud800\udc00\udc01\ud801ab";
        reverseTest(str, "ba\ud801\udc01\ud800\udc00",
                "\ud800\udc00\ud801\udc01ab");

        str = "ab\ud800\udc00\ud801\udc01";
        reverseTest(str, "\ud801\udc01\ud800\udc00ba", str);

        str = "\ud800\udc00\ud801\udc01";
        reverseTest(str, "\ud801\udc01\ud800\udc00", str);

        str = "a\ud800\udc00z\ud801\udc01";
        reverseTest(str, "\ud801\udc01z\ud800\udc00a", str);

        str = "a\ud800\udc00bz\ud801\udc01";
        reverseTest(str, "\ud801\udc01zb\ud800\udc00a", str);

        str = "abc\ud802\udc02\ud801\udc01\ud800\udc00";
        reverseTest(str, "\ud800\udc00\ud801\udc01\ud802\udc02cba", str);

        str = "abcd\ud802\udc02\ud801\udc01\ud800\udc00";
        reverseTest(str, "\ud800\udc00\ud801\udc01\ud802\udc02dcba", str);
    }

    /**
     * java.lang.StringBuilder.setCharAt(int, char)
     */
    public void test_setCharAtIC() {
        final String fixture = "0000";
        StringBuilder sb = new StringBuilder(fixture);
        sb.setCharAt(0, 'A');
        assertEquals("A000", sb.toString());
        sb.setCharAt(1, 'B');
        assertEquals("AB00", sb.toString());
        sb.setCharAt(2, 'C');
        assertEquals("ABC0", sb.toString());
        sb.setCharAt(3, 'D');
        assertEquals("ABCD", sb.toString());

        try {
            sb.setCharAt(-1, 'A');
            fail("No IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.setCharAt(4, 'A');
            fail("No IOOBE, index == length");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.setCharAt(5, 'A');
            fail("No IOOBE, index > length");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.setLength(int)'
     */
    public void test_setLengthI() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals("01234", sb.toString());
        sb.setLength(6);
        assertEquals(6, sb.length());
        assertEquals("01234\0", sb.toString());
        sb.setLength(0);
        assertEquals(0, sb.length());
        assertEquals("", sb.toString());

        try {
            sb.setLength(-1);
            fail("No IOOBE, negative length.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        sb = new StringBuilder("abcde");
        assertEquals("abcde", sb.toString());
        sb.setLength(1);
        sb.append('g');
        assertEquals("ag", sb.toString());

        sb = new StringBuilder("abcde");
        sb.setLength(3);
        sb.append('g');
        assertEquals("abcg", sb.toString());

        sb = new StringBuilder("abcde");
        sb.setLength(2);
        try {
            sb.charAt(3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        sb = new StringBuilder();
        sb.append("abcdefg");
        sb.setLength(2);
        sb.setLength(5);
        for (int i = 2; i < 5; i++) {
            assertEquals(0, sb.charAt(i));
        }

        sb = new StringBuilder();
        sb.append("abcdefg");
        sb.delete(2, 4);
        sb.setLength(7);
        assertEquals('a', sb.charAt(0));
        assertEquals('b', sb.charAt(1));
        assertEquals('e', sb.charAt(2));
        assertEquals('f', sb.charAt(3));
        assertEquals('g', sb.charAt(4));
        for (int i = 5; i < 7; i++) {
            assertEquals(0, sb.charAt(i));
        }

        sb = new StringBuilder();
        sb.append("abcdefg");
        sb.replace(2, 5, "z");
        sb.setLength(7);
        for (int i = 5; i < 7; i++) {
            assertEquals(0, sb.charAt(i));
        }
    }

    /**
     * java.lang.StringBuilder.subSequence(int, int)
     */
    public void test_subSequenceII() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        CharSequence ss = sb.subSequence(0, 5);
        assertEquals("01234", ss.toString());

        ss = sb.subSequence(0, 0);
        assertEquals("", ss.toString());

        try {
            sb.subSequence(-1, 1);
            fail("No IOOBE, negative start.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.subSequence(0, -1);
            fail("No IOOBE, negative end.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.subSequence(0, fixture.length() + 1);
            fail("No IOOBE, end > length.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.subSequence(3, 2);
            fail("No IOOBE, start > end.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.substring(int)
     */
    public void test_substringI() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        String ss = sb.substring(0);
        assertEquals(fixture, ss);

        ss = sb.substring(10);
        assertEquals("", ss);

        try {
            sb.substring(-1);
            fail("No SIOOBE, negative start.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.substring(0, -1);
            fail("No SIOOBE, negative end.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.substring(fixture.length() + 1);
            fail("No SIOOBE, start > length.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.substring(int, int)
     */
    public void test_substringII() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        String ss = sb.substring(0, 5);
        assertEquals("01234", ss);

        ss = sb.substring(0, 0);
        assertEquals("", ss);

        try {
            sb.substring(-1, 1);
            fail("No SIOOBE, negative start.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.substring(0, -1);
            fail("No SIOOBE, negative end.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.substring(0, fixture.length() + 1);
            fail("No SIOOBE, end > length.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb.substring(3, 2);
            fail("No SIOOBE, start > end.");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * java.lang.StringBuilder.toString()'
     */
    public void test_toString() throws Exception {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertEquals(fixture, sb.toString());

        sb.setLength(0);
        sb.append("abcde");
        assertEquals("abcde", sb.toString());
        sb.setLength(1000);
        byte[] bytes = sb.toString().getBytes("GB18030");
        for (int i = 5; i < bytes.length; i++) {
            assertEquals(0, bytes[i]);
        }

        sb.setLength(5);
        sb.append("fghij");
        assertEquals("abcdefghij", sb.toString());
    }

    /**
     * java.lang.StringBuilder.trimToSize()'
     */
    public void test_trimToSize() {
        final String fixture = "0123456789";
        StringBuilder sb = new StringBuilder(fixture);
        assertTrue(sb.capacity() > fixture.length());
        assertEquals(fixture.length(), sb.length());
        assertEquals(fixture, sb.toString());
        int prevCapacity = sb.capacity();
        sb.trimToSize();
        assertTrue(prevCapacity > sb.capacity());
        assertEquals(fixture.length(), sb.length());
        assertEquals(fixture, sb.toString());
    }

    // comparator for StringBuilder objects
    /*
    private static final SerializableAssert STRING_BILDER_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            StringBuilder init = (StringBuilder) initial;
            StringBuilder desr = (StringBuilder) deserialized;

            assertEquals("toString", init.toString(), desr.toString());
        }
    };
    */

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        // SerializationTest.verifySelf(new StringBuilder("0123456789"),
        //        STRING_BILDER_COMPARATOR);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        //SerializationTest.verifyGolden(this, new StringBuilder("0123456789"),
        //        STRING_BILDER_COMPARATOR);
    }

    private static final class Fixture {
        static final Fixture INSTANCE = new Fixture();

        private Fixture() {
            super();
        }

        @Override
        public String toString() {
            return "fixture";
        }
    }
}
