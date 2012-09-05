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

package org.apache.harmony.luni.tests.java.lang;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;

public class StringTest extends TestCase {
    
    /**
     * @tests java.lang.String#String()
     */
    public void test_Constructor() {
        assertEquals("Created incorrect string", "", new String());
    }

    /**
     * @tests java.lang.String#String(byte[])
     */
    public void test_Constructor$B() {
        assertEquals("Failed to create string", "HelloWorld", new String(
                "HelloWorld".getBytes()));
    }

    /**
     * @tests java.lang.String#String(byte[], int, int)
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
     * @tests java.lang.String#String(byte[], int, int, java.lang.String)
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

    /**
     * @tests java.lang.String#String(byte[], java.lang.String)
     */
    public void test_Constructor$BLjava_lang_String() throws Exception {
        String s = new String(new byte[] { 65, 66, 67, 68, 69 }, "8859_1");
        assertEquals("Incorrect string returned: " + s, "ABCDE", s);
    }

    /**
     * @tests java.lang.String#String(char[])
     */
    public void test_Constructor$C() {
        assertEquals("Failed Constructor test", "World", new String(new char[] {
                'W', 'o', 'r', 'l', 'd' }));
    }

    /**
     * @tests java.lang.String#String(char[], int, int)
     */
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

    /**
     * @tests java.lang.String#String(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        String s = new String("Hello World");
        assertEquals("Failed to construct correct string", "Hello World", s);
    }

    /**
     * @tests java.lang.String#String(java.lang.StringBuffer)
     */
    public void test_ConstructorLjava_lang_StringBuffer() {
        StringBuffer sb = new StringBuffer();
        sb.append("HelloWorld");
        assertEquals("Created incorrect string", "HelloWorld", new String(sb));
    }

    /**
     * @tests java.lang.String#String(java.lang.StringBuilder)
     */
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
}
