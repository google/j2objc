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

public class StringBufferTest extends TestCase {

    /**
     * @tests java.lang.StringBuffer#setLength(int)
     */
    public void test_setLengthI() {
        // Regression for HARMONY-90
        StringBuffer buffer = new StringBuffer("abcde");
        try {
            buffer.setLength(-1);
            fail("Assert 0: IndexOutOfBoundsException must be thrown");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        assertEquals("abcde", buffer.toString());
        buffer.setLength(1);
        buffer.append('f');
        assertEquals("af", buffer.toString());

        buffer = new StringBuffer("abcde");
        assertEquals("cde", buffer.substring(2));
        buffer.setLength(3);
        buffer.append('f');
        assertEquals("abcf", buffer.toString());

        buffer = new StringBuffer("abcde");
        buffer.setLength(2);
        try {
            buffer.charAt(3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        buffer = new StringBuffer();
        buffer.append("abcdefg");
        buffer.setLength(2);
        buffer.setLength(5);
        for (int i = 2; i < 5; i++) {
            assertEquals(0, buffer.charAt(i));
        }

        buffer = new StringBuffer();
        buffer.append("abcdefg");
        buffer.delete(2, 4);
        buffer.setLength(7);
        assertEquals('a', buffer.charAt(0));
        assertEquals('b', buffer.charAt(1));
        assertEquals('e', buffer.charAt(2));
        assertEquals('f', buffer.charAt(3));
        assertEquals('g', buffer.charAt(4));
        for (int i = 5; i < 7; i++) {
            assertEquals(0, buffer.charAt(i));
        }

        buffer = new StringBuffer();
        buffer.append("abcdefg");
        buffer.replace(2, 5, "z");
        buffer.setLength(7);
        for (int i = 5; i < 7; i++) {
            assertEquals(0, buffer.charAt(i));
        }
    }

    /**
     * @tests java.lang.StringBuffer#toString()
     */
    public void test_toString() throws Exception {
        StringBuffer buffer = new StringBuffer();
        assertEquals("", buffer.toString());

        buffer.append("abcde");
        assertEquals("abcde", buffer.toString());
        buffer.setLength(1000);
        byte[] bytes = buffer.toString().getBytes();
        for (int i = 5; i < bytes.length; i++) {
            assertEquals(0, bytes[i]);
        }

        buffer.setLength(5);
        buffer.append("fghij");
        assertEquals("abcdefghij", buffer.toString());
    }

    /**
     * @tests StringBuffer.StringBuffer(CharSequence);
     */
    public void test_constructorLjava_lang_CharSequence() {
        try {
            new StringBuffer((CharSequence) null);
            fail("Assert 0: NPE must be thrown.");
        } catch (NullPointerException e) {}
        
        assertEquals("Assert 1: must equal 'abc'.", "abc", new StringBuffer((CharSequence)"abc").toString());
    }
    
    public void test_trimToSize() {
        StringBuffer buffer = new StringBuffer(25);
        buffer.append("abc");
        int origCapacity = buffer.capacity();
        buffer.trimToSize();
        int trimCapacity = buffer.capacity();
        assertTrue("Assert 0: capacity must be smaller.", trimCapacity < origCapacity);
        assertEquals("Assert 1: length must still be 3", 3, buffer.length());
        assertEquals("Assert 2: value must still be 'abc'.", "abc", buffer.toString());
    }
    
    /**
     * @tests java.lang.StringBuffer.append(CharSequence)
     */
    public void test_appendLjava_lang_CharSequence() {
        StringBuffer sb = new StringBuffer();
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
     * @tests java.lang.StringBuffer.append(CharSequence, int, int)
     */
    @SuppressWarnings("cast")
    public void test_appendLjava_lang_CharSequenceII() {
        StringBuffer sb = new StringBuffer();
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
     * @tests java.lang.StringBuffer.append(char[], int, int)
     */
    public void test_append$CII_2() {
        StringBuffer obj = new StringBuffer();
        try {
            obj.append(new char[0], -1, -1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * @tests java.lang.StringBuffer.append(char[], int, int)
     */
    public void test_append$CII_3() throws Exception {
        StringBuffer obj = new StringBuffer();
        try {
            obj.append((char[]) null, -1, -1);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.lang.StringBuffer.insert(int, CharSequence)
     */
    public void test_insertILjava_lang_CharSequence() {
        final String fixture = "0000";
        StringBuffer sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(0, (CharSequence) "ab"));
        assertEquals("ab0000", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(2, (CharSequence) "ab"));
        assertEquals("00ab00", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) "ab"));
        assertEquals("0000ab", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) null));
        assertEquals("0000null", sb.toString());
        assertEquals(8, sb.length());

        try {
            sb = new StringBuffer(fixture);
            sb.insert(-1, (CharSequence) "ab");
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuffer(fixture);
            sb.insert(5, (CharSequence) "ab");
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    /**
     * @tests java.lang.StringBuffer.insert(int, CharSequence, int, int)
     */
    @SuppressWarnings("cast")
    public void test_insertILjava_lang_CharSequenceII() {
        final String fixture = "0000";
        StringBuffer sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(0, (CharSequence) "ab", 0, 2));
        assertEquals("ab0000", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(0, (CharSequence) "ab", 0, 1));
        assertEquals("a0000", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(2, (CharSequence) "ab", 0, 2));
        assertEquals("00ab00", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(2, (CharSequence) "ab", 0, 1));
        assertEquals("00a00", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) "ab", 0, 2));
        assertEquals("0000ab", sb.toString());
        assertEquals(6, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) "ab", 0, 1));
        assertEquals("0000a", sb.toString());
        assertEquals(5, sb.length());

        sb = new StringBuffer(fixture);
        assertSame(sb, sb.insert(4, (CharSequence) null, 0, 2));
        assertEquals("0000nu", sb.toString());
        assertEquals(6, sb.length());

        try {
            sb = new StringBuffer(fixture);
            sb.insert(-1, (CharSequence) "ab", 0, 2);
            fail("no IOOBE, negative index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuffer(fixture);
            sb.insert(5, (CharSequence) "ab", 0, 2);
            fail("no IOOBE, index too large index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuffer(fixture);
            sb.insert(5, (CharSequence) "ab", -1, 2);
            fail("no IOOBE, negative offset");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuffer(fixture);
            sb.insert(5, new char[] { 'a', 'b' }, 0, -1);
            fail("no IOOBE, negative length");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            sb = new StringBuffer(fixture);
            sb.insert(5, new char[] { 'a', 'b' }, 0, 3);
            fail("no IOOBE, too long");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }
    
    /**
     * @tests java.lang.StringBuffer.insert(int, char)
     */
    public void test_insertIC() {
        StringBuffer obj = new StringBuffer();
        try {
            obj.insert(-1, ' ');
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }
    
    /**
     * @tests java.lang.StringBuffer.getChars(int, int, char[], int)
     */
    public void test_getCharsII$CI() {
        StringBuffer obj = new StringBuffer();
        try {
            obj.getChars(0, 0,  new char[0], -1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }
    
    /**
     * @tests {@link java.lang.StringBuffer#indexOf(String, int)}
     */
    @SuppressWarnings("nls")
    public void test_IndexOfStringInt() {
        final String fixture = "0123456789";
        StringBuffer sb = new StringBuffer(fixture);
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
            fail("Should throw a NullPointerExceptionE");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * @tests {@link java.lang.StringBuffer#lastIndexOf(String, int)}
     */
    @SuppressWarnings("nls")
    public void test_lastIndexOfLjava_lang_StringI() {
        final String fixture = "0123456789";
        StringBuffer sb = new StringBuffer(fixture);
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
            fail("Should throw a NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }
}
