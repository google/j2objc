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

package org.apache.harmony.regex.tests.java.util.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;

/**
 * Tests simple Pattern compilation and Matcher methods
 *
 */
public class Pattern2Test extends TestCase {

    public void testUnicodeCategories() throws PatternSyntaxException {
        // Test Unicode categories using \p and \P
        // One letter codes: L, M, N, P, S, Z, C
        // Two letter codes: Lu, Nd, Sc, Sm, ...
        // See java.lang.Character and Unicode standard for complete list
        // TODO
        // Test \p{L}
        // TODO

        // Test \p{N}
        // TODO

        // Test two letter codes:
        // From unicode.org:
        // Lu
        // Ll
        // Lt
        // Lm
        // Lo
        // Mn
        // Mc
        // Me
        // Nd
        // Nl
        // No
        // Pc
        // Pd
        // Ps
        // Pe
        // Pi
        // Pf
        // Po
        // Sm
        // Sc
        // Sk
        // So
        // Zs
        // Zl
        // Zp
        // Cc
        // Cf
        // Cs
        // Co
        // Cn

        // TODO add more tests per category
        //{"Cc", "\u0000", "-\u0041"},
        testCategory("Cf", "\u202B");
        testCategory("Co", "\uE000");
        testCategory("Cs", "\uD800");
        testCategory("Ll", "a", "b", "x", "y", "z", "-A", "-Z");
        testCategory("Lm", "\u02B9");
        testCategory("Lu", "B", "C", "-c");
        testCategory("Lo", "\u05E2");
        testCategory("Lt", "\u01C5");
        testCategory("Mc", "\u0903");
        testCategory("Me", "\u0488");
        testCategory("Mn", "\u0300");
        testCategory("Nd", "\u0030");
        testCategory("Nl", "\u2164");
        testCategory("No", "\u0BF0");
        // testCategory("Pc", "\u30FB");
        testCategory("Pd", "\u2015");
        testCategory("Pe", "\u207E");
        testCategory("Po", "\u00B7");
        testCategory("Ps", "\u0F3C");
        testCategory("Sc", "\u20A0");
        testCategory("Sk", "\u00B8");
        testCategory("Sm", "\u002B");
        testCategory("So", "\u0B70");
        testCategory("Zl", "\u2028");
        // testCategory("Pi", "\u200C");
        testCategory("Zp", "\u2029");
    }

    private void testCategory(String cat, String... matches) {
        String pa = "{"+cat+"}";
        String pat = "\\p"+pa;
        String npat = "\\P"+pa;
        Pattern p = Pattern.compile(pat);
        Pattern pn = Pattern.compile(npat);
        for (int j = 0; j < matches.length; j++) {
            String t = matches[j];
            boolean invert = t.startsWith("-");
            if (invert) {
                // test negative case, expected to fail
                t = t.substring(1);
                assertFalse("expected '"+t+"' to not be matched " +
                        "by pattern '"+pat, p.matcher(t).matches());
                assertTrue("expected '"+t+"' to  " +
                        "be matched by pattern '"+npat, pn.matcher(t).matches());
            } else {
                assertTrue("expected '"+t+"' to be matched " +
                        "by pattern '"+pat, p.matcher(t).matches());
                assertFalse("expected '"+t+"' to  " +
                        "not be matched by pattern '"+npat, pn.matcher(t).matches());
            }
        }
    }

    public void testCapturingGroups() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        // Test simple capturing groups
        p = Pattern.compile("(a+)b");
        m = p.matcher("aaaaaaaab");
        assertTrue(m.matches());
        assertEquals(1, m.groupCount());
        assertEquals("aaaaaaaa", m.group(1));

        p = Pattern.compile("((an)+)((as)+)");
        m = p.matcher("ananas");
        assertTrue(m.matches());
        assertEquals(4, m.groupCount());
        assertEquals("ananas", m.group(0));
        assertEquals("anan", m.group(1));
        assertEquals("an", m.group(2));
        assertEquals("as", m.group(3));
        assertEquals("as", m.group(4));

        // Test grouping without capture (?:...)
        p = Pattern.compile("(?:(?:an)+)(as)");
        m = p.matcher("ananas");
        assertTrue(m.matches());
        assertEquals(1, m.groupCount());
        assertEquals("as", m.group(1));
        try {
            m.group(2);
            fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ioobe) {
            // expected
        }

        // Test combination of grouping and capture
        // TODO

        // Test \<num> sequence with capturing and non-capturing groups
        // TODO

        // Test \<num> with <num> out of range
        p = Pattern.compile("((an)+)as\\1");
        m = p.matcher("ananasanan");
        assertTrue(m.matches());

        try {
            p = Pattern.compile("((an)+)as\\4");
            fail("expected PatternSyntaxException");
        } catch (PatternSyntaxException pse) {
            // expected
        }

    }
    public void testRepeats() {
        Pattern p;
        Matcher m;

        // Test ?
        p = Pattern.compile("(abc)?c");
        m = p.matcher("abcc");
        assertTrue(m.matches());
        m = p.matcher("c");
        assertTrue(m.matches());
        m = p.matcher("cc");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertFalse(m.matches());

        // Test *
        p = Pattern.compile("(abc)*c");
        m = p.matcher("abcc");
        assertTrue(m.matches());
        m = p.matcher("c");
        assertTrue(m.matches());
        m = p.matcher("cc");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertTrue(m.matches());

        // Test +
        p = Pattern.compile("(abc)+c");
        m = p.matcher("abcc");
        assertTrue(m.matches());
        m = p.matcher("c");
        assertFalse(m.matches());
        m = p.matcher("cc");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertTrue(m.matches());

        // Test {<num>}, including 0, 1 and more
        p = Pattern.compile("(abc){0}c");
        m = p.matcher("abcc");
        assertFalse(m.matches());
        m = p.matcher("c");
        assertTrue(m.matches());

        p = Pattern.compile("(abc){1}c");
        m = p.matcher("abcc");
        assertTrue(m.matches());
        m = p.matcher("c");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertFalse(m.matches());

        p = Pattern.compile("(abc){2}c");
        m = p.matcher("abcc");
        assertFalse(m.matches());
        m = p.matcher("c");
        assertFalse(m.matches());
        m = p.matcher("cc");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertTrue(m.matches());

        // Test {<num>,}, including 0, 1 and more
        // TODO

        // Test {<n1>,<n2>}, with n1 < n2, n1 = n2 and n1 > n2 (illegal?)
        // TODO
    }
    public void testAnchors() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        // Test ^, default and MULTILINE
        p = Pattern.compile("^abc\\n^abc", Pattern.MULTILINE);
        m = p.matcher("abc\nabc");
        assertTrue(m.matches());

        p = Pattern.compile("^abc\\n^abc");
        m = p.matcher("abc\nabc");
        assertFalse(m.matches());

        // Test $, default and MULTILINE
        // TODO

        // Test \b (word boundary)
        // TODO

        // Test \B (not a word boundary)
        // TODO

        // Test \A (beginning of string)
        // TODO

        // Test \Z (end of string)
        // TODO

        // Test \z (end of string)
        // TODO

        // Test \G
        // TODO

        // Test positive lookahead using (?=...)
        // TODO

        // Test negative lookahead using (?!...)
        // TODO

        // Test positive lookbehind using (?<=...)
        // TODO

        // Test negative lookbehind using (?<!...)
        // TODO
    }
}
