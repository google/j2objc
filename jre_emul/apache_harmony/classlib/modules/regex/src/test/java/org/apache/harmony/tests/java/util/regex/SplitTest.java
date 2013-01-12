/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.util.regex;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;

/**
 * Tests for the split methods in java.util.regex.Matcher. Based on
 * Apache Harmony's SplitTest, and augmented with a test where the
 * pattern is a simple whitespace regex.
 */
@SuppressWarnings("nls")
public class SplitTest extends TestCase {

    public void testSimple() {
        Pattern p = Pattern.compile("/");
        String[] results = p.split("have/you/done/it/right");
        String[] expected = new String[] { "have", "you", "done", "it", "right" };
        assertEquals(expected.length, results.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(results[i], expected[i]);
        }
    }

    public void testSplit1() throws PatternSyntaxException {
        Pattern p = Pattern.compile(" ");

        String input = "poodle zoo";
        String tokens[];

        tokens = p.split(input, 1);
        assertEquals(1, tokens.length);
        assertTrue(tokens[0].equals(input));
        tokens = p.split(input, 2);
        assertEquals(2, tokens.length);
        assertEquals("poodle", tokens[0]);
        assertEquals("zoo", tokens[1]);
        tokens = p.split(input, 5);
        assertEquals(2, tokens.length);
        assertEquals("poodle", tokens[0]);
        assertEquals("zoo", tokens[1]);
        tokens = p.split(input, -2);
        assertEquals(2, tokens.length);
        assertEquals("poodle", tokens[0]);
        assertEquals("zoo", tokens[1]);
        tokens = p.split(input, 0);
        assertEquals(2, tokens.length);
        assertEquals("poodle", tokens[0]);
        assertEquals("zoo", tokens[1]);
        tokens = p.split(input);
        assertEquals(2, tokens.length);
        assertEquals("poodle", tokens[0]);
        assertEquals("zoo", tokens[1]);

        p = Pattern.compile("d");

        tokens = p.split(input, 1);
        assertEquals(1, tokens.length);
        assertTrue(tokens[0].equals(input));
        tokens = p.split(input, 2);
        assertEquals(2, tokens.length);
        assertEquals("poo", tokens[0]);
        assertEquals("le zoo", tokens[1]);
        tokens = p.split(input, 5);
        assertEquals(2, tokens.length);
        assertEquals("poo", tokens[0]);
        assertEquals("le zoo", tokens[1]);
        tokens = p.split(input, -2);
        assertEquals(2, tokens.length);
        assertEquals("poo", tokens[0]);
        assertEquals("le zoo", tokens[1]);
        tokens = p.split(input, 0);
        assertEquals(2, tokens.length);
        assertEquals("poo", tokens[0]);
        assertEquals("le zoo", tokens[1]);
        tokens = p.split(input);
        assertEquals(2, tokens.length);
        assertEquals("poo", tokens[0]);
        assertEquals("le zoo", tokens[1]);

        p = Pattern.compile("o");

        tokens = p.split(input, 1);
        assertEquals(1, tokens.length);
        assertTrue(tokens[0].equals(input));
        tokens = p.split(input, 2);
        assertEquals(2, tokens.length);
        assertEquals("p", tokens[0]);
        assertEquals("odle zoo", tokens[1]);
        tokens = p.split(input, 5);
        assertEquals(5, tokens.length);
        assertEquals("p", tokens[0]);
        assertTrue(tokens[1].equals(""));
        assertEquals("dle z", tokens[2]);
        assertTrue(tokens[3].equals(""));
        assertTrue(tokens[4].equals(""));
        tokens = p.split(input, -2);
        assertEquals(5, tokens.length);
        assertEquals("p", tokens[0]);
        assertTrue(tokens[1].equals(""));
        assertEquals("dle z", tokens[2]);
        assertTrue(tokens[3].equals(""));
        assertTrue(tokens[4].equals(""));
        tokens = p.split(input, 0);
        assertEquals(3, tokens.length);
        assertEquals("p", tokens[0]);
        assertTrue(tokens[1].equals(""));
        assertEquals("dle z", tokens[2]);
        tokens = p.split(input);
        assertEquals(3, tokens.length);
        assertEquals("p", tokens[0]);
        assertTrue(tokens[1].equals(""));
        assertEquals("dle z", tokens[2]);
    }

    public void testSplitRegex() {
        String input1 = "HiThere";
        String input2 = "Hi there";
        String input3 = "Hi  there";

        String pattern = "[ ]+";

        assertEquals(1, input1.split(pattern).length);
        assertEquals(2, input2.split(pattern).length);
        assertEquals(2, input3.split(pattern).length);
    }
}
