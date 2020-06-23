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

package org.apache.harmony.regex.tests.java.util.regex;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;
import java.util.regex.*;

/**
 * TODO Type description
 *
 */
@SuppressWarnings("nls")
public class SplitTest extends TestCase {

    public void testSimple() {
        Pattern p = Pattern.compile("/");
        String[] results = p.split("have/you/done/it/right");
        String[] expected = new String[] { "have", "you", "done", "it", "right" };
        assertArraysEqual(expected, results);
    }

    @SuppressWarnings("InvalidPatternSyntax")
    public void testEmptySplits() {
        // Trailing empty matches are removed.
        assertArraysEqual(new String[0], "hello".split("."));
        assertArraysEqual(new String[] { "1", "2" }, "1:2:".split(":"));
        // ...including when that results in an empty result.
        assertArraysEqual(new String[0], ":".split(":"));
        // ...but not when limit < 0.
        assertArraysEqual(new String[] { "1", "2", "" }, "1:2:".split(":", -1));

        // Leading empty matches are retained.
        assertArraysEqual(new String[] { "", "", "o" }, "hello".split(".."));

        // A separator that doesn't occur in the input gets you the input.
        assertArraysEqual(new String[] { "hello" }, "hello".split("not-present-in-test"));
        // ...including when the input is the empty string.
        // (Perl returns an empty list instead.)
        assertArraysEqual(new String[] { "" }, "".split("not-present-in-test"));
        assertArraysEqual(new String[] { "" }, "".split("A?"));

        // The limit argument controls the size of the result.
        // If l == 0, the result is as long as needed, except trailing empty matches are dropped.
        // If l < 0, the result is as long as needed, and trailing empty matches are retained.
        // If l > 0, the result contains the first l matches, plus one string containing the remaining input.
        // Examples without a trailing separator (and hence without a trailing empty match):
        assertArraysEqual(new String[] { "a", "b", "c" }, "a,b,c".split(",", 0));
        assertArraysEqual(new String[] { "a,b,c" }, "a,b,c".split(",", 1));
        assertArraysEqual(new String[] { "a", "b,c" }, "a,b,c".split(",", 2));
        assertArraysEqual(new String[] { "a", "b", "c" }, "a,b,c".split(",", 3));
        assertArraysEqual(new String[] { "a", "b", "c" }, "a,b,c".split(",", Integer.MAX_VALUE));
        // Examples with a trailing separator (and hence possibly with a trailing empty match):
        assertArraysEqual(new String[] { "a", "b", "c" }, "a,b,c,".split(",", 0));
        assertArraysEqual(new String[] { "a,b,c," }, "a,b,c,".split(",", 1));
        assertArraysEqual(new String[] { "a", "b,c," }, "a,b,c,".split(",", 2));
        assertArraysEqual(new String[] { "a", "b", "c," }, "a,b,c,".split(",", 3));
        assertArraysEqual(new String[] { "a", "b", "c", "" }, "a,b,c,".split(",", Integer.MAX_VALUE));
        assertArraysEqual(new String[] { "a", "b", "c", "" }, "a,b,c,".split(",", -1));
    }

    private void assertArraysEqual(String[] expected, String[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(Integer.toString(i), expected[i], actual[i]);
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

    /* J2ObjC removed: empty pattern is invalid in ICU 51. (b/12096792)
    public void testSplit2() {
        Pattern p = Pattern.compile("");
        assertEquals(Arrays.asList("a", ""), Arrays.asList(p.split("a", -1)));
        assertEquals(Arrays.asList(""), Arrays.asList(p.split("", -1)));
        assertEquals(Arrays.asList("a", "b", "c", "d", ""), Arrays.asList(p.split("abcd", -1)));

        // Regression test for Android
        assertEquals("GOOG,23,500".split("|").length, 11);
    }
    */

    /* J2ObjC removed: empty pattern is invalid in ICU 51. (b/12096792)
    public void testSplitSupplementaryWithEmptyString() {
        // See http://www.unicode.org/reports/tr18/#Supplementary_Characters
        // We have to treat text as code points not code units.
        Pattern p = Pattern.compile("");
        String[] s = p.split("a\ud869\uded6b", -1);
        assertEquals(Arrays.asList("a", "\ud869\uded6", "b", ""), Arrays.asList(s));
    }
    */

}
