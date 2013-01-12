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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class PatternTest extends TestCase {
    private static final String[] testPatterns = {
            "(a|b)*abb",
            "(1*2*3*4*)*567",
            "(a|b|c|d)*aab",
            "(1|2|3|4|5|6|7|8|9|0)(1|2|3|4|5|6|7|8|9|0)*",
            "(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ)*",
            "(a|b)*(a|b)*A(a|b)*lice.*",
            "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|"
                    + "i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*(1|2|3|4|5|6|7|8|9|0)*|while|for|struct|if|do"

    };
    
    public PatternTest(String name) {
        super(name);
    }

    public void testCommentsInPattern() {
        Pattern p = Pattern.compile("ab# this is a comment\ncd", Pattern.COMMENTS);
        assertTrue(p.matcher("abcd").matches());
    }

    /*
     * Class under test for String[] split(CharSequence, int)
     */
    public void testSplitCharSequenceint() {
        // splitting CharSequence which ends with pattern
        // bug6193
        assertEquals(",,".split(",", 3).length, 3);
        assertEquals(",,".split(",", 4).length, 3);
        // bug6193
        // bug5391
        assertEquals(Pattern.compile("o").split("boo:and:foo", 5).length, 5);
        assertEquals(Pattern.compile("b").split("ab", -1).length, 2);
        // bug5391
        String s[];
        Pattern pat = Pattern.compile("x");
        s = pat.split("zxx:zzz:zxx", 10);
        assertEquals(s.length, 5);
        s = pat.split("zxx:zzz:zxx", 3);
        assertEquals(s.length, 3);
        s = pat.split("zxx:zzz:zxx", -1);
        assertEquals(s.length, 5);
        s = pat.split("zxx:zzz:zxx", 0);
        assertEquals(s.length, 3);
        // other splitting
        // negative limit
        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", -1);
        assertEquals(s.length, 5);
        s = pat.split("", -1);
        assertEquals(s.length, 1);
        // zero limit
        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", 0);
        assertEquals(s.length, 3);
        s = pat.split("", 0);
        assertEquals(s.length, 1);
        // positive limit
        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", 12);
        assertEquals(s.length, 5);
        s = pat.split("", 6);
        assertEquals(s.length, 1);

        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", 5);
        assertEquals(s.length, 5);
        s = pat.split("", 1);
        assertEquals(s.length, 1);

        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", 3);
        assertEquals(s.length, 3);
    }

    /*
     * Class under test for String[] split(CharSequence)
     */
    public void testSplitCharSequence() {
        String s[];
        Pattern pat = Pattern.compile("b");
        s = pat.split("abccbadfebb");
        assertEquals(s.length, 3);
        s = pat.split("");
        assertEquals(s.length, 1);
        // bug6544
        String s1 = "";
        String[] arr = s1.split(":");
        assertEquals(arr.length, 1);
        // bug6544
    }

    public void testPattern() {
    }

    public void testFlags() {
        String baseString;
        String testString;
        Pattern pat;
        Matcher mat;

        baseString = "((?i)|b)a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());

        baseString = "(?i)a|b";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?i)a|b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "c|(?i)a|b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?i)a|(?s)b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?i)a|(?-i)b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());

        baseString = "(?i)a|(?-i)c|b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());

        baseString = "(?i)a|(?-i)c|(?i)b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?i)a|(?-i)b";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "((?i))a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());

        baseString = "(?i)((?s)a.)";
        testString = "A\n";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?i)((?-i)a)";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());

        baseString = "(?i)(?s:a.)";
        testString = "A\n";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?i)fgh(?s:aa)";
        testString = "fghAA";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?i)((?-i))a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "abc(?i)d";
        testString = "ABCD";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());

        testString = "abcD";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "a(?i)a(?-i)a(?i)a(?-i)a";
        testString = "aAaAa";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        testString = "aAAAa";
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
    }

    /*
     * Class under test for Pattern compile(String, int)
     */
    public void testCompileStringint() {
        /*
         * this tests are needed to verify that appropriate exceptions are
         * thrown
         */
        String pattern = "b)a";
        try {
            Pattern.compile(pattern);
            fail("Expected a PatternSyntaxException when compiling pattern: "
                    + pattern);
        } catch (PatternSyntaxException e) {
            // pass
        }
        pattern = "bcde)a";
        try {
            Pattern.compile(pattern);
            fail("Expected a PatternSyntaxException when compiling pattern: "
                    + pattern);
        } catch (PatternSyntaxException e) {
            // pass
        }
        pattern = "bbg())a";
        try {
            Pattern.compile(pattern);
            fail("Expected a PatternSyntaxException when compiling pattern: "
                    + pattern);
        } catch (PatternSyntaxException e) {
            // pass
        }

        pattern = "cdb(?i))a";
        try {
            Pattern.compile(pattern);
            fail("Expected a PatternSyntaxException when compiling pattern: "
                    + pattern);
        } catch (PatternSyntaxException e) {
            // pass
        }

        /*
         * this pattern doesn't match any string, but should be compiled anyway
         */
        pattern = "(b\\1)a";
        Pattern.compile(pattern);
    }

    /*
     * Class under test for Pattern compile(String)
     */
    public void testQuantCompileNeg() {
        String[] patterns = { "5{,2}", "{5asd", "{hgdhg", "{5,hjkh", "{,5hdsh",
                "{5,3shdfkjh}" };
        for (String element : patterns) {
            try {
                Pattern.compile(element);
                fail("PatternSyntaxException was expected, but compilation succeeds");
            } catch (PatternSyntaxException pse) {
                continue;
            }
        }
    }

    public void testQuantCompilePos() {
        String[] patterns = {/* "(abc){1,3}", */"abc{2,}", "abc{5}" };
        for (String element : patterns) {
            Pattern.compile(element);
        }
    }

    public void testQuantComposition() {
        String pattern = "(a{1,3})aab";
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher mat = pat.matcher("aaab");
        mat.matches();
        mat.start(1);
        mat.group(1);
    }

    public void testMatches() {
        String[][] posSeq = {
                { "abb", "ababb", "abababbababb", "abababbababbabababbbbbabb" },
                { "213567", "12324567", "1234567", "213213567",
                        "21312312312567", "444444567" },
                { "abcdaab", "aab", "abaab", "cdaab", "acbdadcbaab" },
                { "213234567", "3458", "0987654", "7689546432", "0398576",
                        "98432", "5" },
                {
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" },
                { "ababbaAabababblice", "ababbaAliceababab", "ababbAabliceaaa",
                        "abbbAbbbliceaaa", "Alice" },
                { "a123", "bnxnvgds156", "for", "while", "if", "struct" },
                { "xy" }, { "xy" }, { "xcy" }

        };

        for (int i = 0; i < testPatterns.length; i++) {
            for (int j = 0; j < posSeq[i].length; j++) {
                assertTrue("Incorrect match: " + testPatterns[i] + " vs "
                        + posSeq[i][j], Pattern.matches(testPatterns[i],
                        posSeq[i][j]));
            }
        }
    }

    public void testTimeZoneIssue() {
        Pattern p = Pattern.compile("GMT(\\+|\\-)(\\d+)(:(\\d+))?");
        Matcher m = p.matcher("GMT-9:45");
        assertTrue(m.matches());
        assertEquals("-", m.group(1));
        assertEquals("9", m.group(2));
        assertEquals(":45", m.group(3));
        assertEquals("45", m.group(4));
    }

    public void testCompileRanges() {
        String[] correctTestPatterns = { "[^]*abb]*", "[a-d[m-p]]*abb",
                "[a-d\\d]*abb", "[abc]*abb", "[a-e&&[de]]*abb", "[^abc]*abb",
                "[a-e&&[^de]]*abb", "[a-z&&[^m-p]]*abb", "[a-d[m-p]]*abb",
                "[a-zA-Z]*abb", "[+*?]*abb", "[^+*?]*abb" };

        String[] inputSecuence = { "kkkk", "admpabb", "abcabcd124654abb",
                "abcabccbacababb", "dededededededeedabb", "gfdhfghgdfghabb",
                "accabacbcbaabb", "acbvfgtyabb", "adbcacdbmopabcoabb",
                "jhfkjhaSDFGHJkdfhHNJMjkhfabb", "+*??+*abb", "sdfghjkabb" };

        for (int i = 0; i < correctTestPatterns.length; i++) {
            assertTrue("pattern: " + correctTestPatterns[i] + " input: "
                    + inputSecuence[i], Pattern.matches(correctTestPatterns[i],
                    inputSecuence[i]));

        }

        String[] wrongInputSecuence = { "]", "admpkk", "abcabcd124k654abb",
                "abwcabccbacababb", "abababdeababdeabb", "abcabcacbacbabb",
                "acdcbecbaabb", "acbotyabb", "adbcaecdbmopabcoabb",
                "jhfkjhaSDFGHJk;dfhHNJMjkhfabb", "+*?a?+*abb", "sdf+ghjkabb" };

        for (int i = 0; i < correctTestPatterns.length; i++) {
            assertFalse("pattern: " + correctTestPatterns[i] + " input: "
                    + wrongInputSecuence[i], Pattern.matches(
                    correctTestPatterns[i], wrongInputSecuence[i]));

        }
    }

    public void testRangesSpecialCases() {
        String neg_patterns[] = { "[a-&&[b-c]]", "[a-\\w]", "[b-a]", "[]" };

        for (String element : neg_patterns) {
            try {
                Pattern.compile(element);
                fail("PatternSyntaxException was expected: " + element);
            } catch (PatternSyntaxException pse) {
            }
        }

        String pos_patterns[] = { "[-]+", "----", "[a-]+", "a-a-a-a-aa--",
                "[\\w-a]+", "123-2312--aaa-213", "[a-]]+", "-]]]]]]]]]]]]]]]" };

        for (int i = 0; i < pos_patterns.length; i++) {
            String pat = pos_patterns[i++];
            String inp = pos_patterns[i];
            assertTrue("pattern: " + pat + " input: " + inp, Pattern.matches(
                    pat, inp));
        }
    }

    public void testZeroSymbols() {
        assertTrue(Pattern.matches("[\0]*abb", "\0\0\0\0\0\0abb"));
    }

    public void testEscapes() {
        Pattern pat = Pattern.compile("\\Q{]()*?");
        Matcher mat = pat.matcher("{]()*?");

        assertTrue(mat.matches());
    }

    public void testRegressions() {
        // Bug 181
        Pattern.compile("[\\t-\\r]");

        // HARMONY-4472
        Pattern.compile("a*.+");

        // HARMONY-5858
        Pattern.compile("\\u6211", Pattern.LITERAL); 
    }

    public void testOrphanQuantifiers() {
        try {
            Pattern.compile("+++++");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException pse) {
        }
    }

    public void testOrphanQuantifiers2() {
        try {
            Pattern.compile("\\d+*");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException pse) {
        }
    }

    public void testURIPatterns() {
        String URI_REGEXP_STR = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
        String SCHEME_REGEXP_STR = "^[a-zA-Z]{1}[\\w+-.]+$";
        String REL_URI_REGEXP_STR = "^(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
        String IPV6_REGEXP_STR = "^[0-9a-fA-F\\:\\.]+(\\%\\w+)?$";
        String IPV6_REGEXP_STR2 = "^\\[[0-9a-fA-F\\:\\.]+(\\%\\w+)?\\]$";
        String IPV4_REGEXP_STR = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";
        String HOSTNAME_REGEXP_STR = "\\w+[\\w\\-\\.]*";

        Pattern.compile(URI_REGEXP_STR);
        Pattern.compile(REL_URI_REGEXP_STR);
        Pattern.compile(SCHEME_REGEXP_STR);
        Pattern.compile(IPV4_REGEXP_STR);
        Pattern.compile(IPV6_REGEXP_STR);
        Pattern.compile(IPV6_REGEXP_STR2);
        Pattern.compile(HOSTNAME_REGEXP_STR);
    }

    public void testFindBoundaryCases1() {
        Pattern pat = Pattern.compile(".*\n");
        Matcher mat = pat.matcher("a\n");

        mat.find();
        assertEquals("a\n", mat.group());

    }

    public void testFindBoundaryCases2() {
        Pattern pat = Pattern.compile(".*A");
        Matcher mat = pat.matcher("aAa");

        mat.find();
        assertEquals("aA", mat.group());

    }

    public void testFindBoundaryCases3() {
        Pattern pat = Pattern.compile(".*A", Pattern.MULTILINE);
        Matcher mat = pat.matcher("a\naA\n");

        mat.find();
        assertEquals("aA", mat.group());

    }

    public void testFindBoundaryCases4() {
        Pattern pat = Pattern.compile("A.*");
        Matcher mat = pat.matcher("A\n");

        mat.find();
        assertEquals("A", mat.group());

    }

    public void testFindBoundaryCases5() {
        Pattern pat = Pattern.compile(".*A.*");
        Matcher mat = pat.matcher("\nA\naaa\nA\naaAaa\naaaA\n");
        // Matcher mat = pat.matcher("\nA\n");
        String[] res = { "A", "A", "aaAaa", "aaaA" };
        int k = 0;
        for (; mat.find(); k++) {
            assertEquals(res[k], mat.group());
        }
    }

    public void testFindBoundaryCases6() {
        String[] res = { "", "a", "", "" };
        Pattern pat = Pattern.compile(".*");
        Matcher mat = pat.matcher("\na\n");
        int k = 0;

        for (; mat.find(); k++) {
            assertEquals(res[k], mat.group());
        }
    }

    public void _testFindBoundaryCases7() {
        Pattern pat = Pattern.compile(".*");
        Matcher mat = pat.matcher("\na\n");

        while (mat.find()) {
            System.out.println(mat.group());
            System.out.flush();
        }
    }

    public void testBackReferences() {
        Pattern pat = Pattern.compile("(\\((\\w*):(.*):(\\2)\\))");
        Matcher mat = pat
                .matcher("(start1: word :start1)(start2: word :start2)");
        int k = 1;
        for (; mat.find(); k++) {
            assertEquals("start" + k, mat.group(2));
            assertEquals(" word ", mat.group(3));
            assertEquals("start" + k, mat.group(4));

        }

        assertEquals(3, k);
        pat = Pattern.compile(".*(.)\\1");
        mat = pat.matcher("saa");
        assertTrue(mat.matches());
    }

    public void _testBackReferences1() {
        Pattern pat = Pattern.compile("(\\((\\w*):(.*):(\\2)\\))");
        Matcher mat = pat
                .matcher("(start1: word :start1)(start2: word :start2)");
        int k = 1;
        for (; mat.find(); k++) {
            System.out.println(mat.group(2));
            System.out.println(mat.group(3));
            System.out.println(mat.group(4));

        }

        assertEquals(3, k);
    }

    public void testNewLine() {
        Pattern pat = Pattern.compile("(^$)*\n", Pattern.MULTILINE);
        Matcher mat = pat.matcher("\r\n\n");
        int counter = 0;
        while (mat.find()) {
            counter++;
        }
        assertEquals(2, counter);
    }

    public void testFindGreedy() {
        Pattern pat = Pattern.compile(".*aaa", Pattern.DOTALL);
        Matcher mat = pat.matcher("aaaa\naaa\naaaaaa");
        mat.matches();
        assertEquals(15, mat.end());
    }

    public void testEmptyFamily() {
        Pattern.compile("\\p{Lower}");
    }

    public void testNonCaptConstr() {
        // Flags
        Pattern pat = Pattern.compile("(?i)b*(?-i)a*");
        assertTrue(pat.matcher("bBbBaaaa").matches());
        assertFalse(pat.matcher("bBbBAaAa").matches());

        // Non-capturing groups
        pat = Pattern.compile("(?i:b*)a*");
        assertTrue(pat.matcher("bBbBaaaa").matches());
        assertFalse(pat.matcher("bBbBAaAa").matches());

        // positive lookahead
        pat = Pattern.compile(".*\\.(?=log$).*$");
        assertTrue(pat.matcher("a.b.c.log").matches());
        assertFalse(pat.matcher("a.b.c.log.").matches());

        // negative lookahead
        pat = Pattern.compile(".*\\.(?!log$).*$");
        assertFalse(pat.matcher("abc.log").matches());
        assertTrue(pat.matcher("abc.logg").matches());

        // positive lookbehind
        pat = Pattern.compile(".*(?<=abc)\\.log$");
        assertFalse(pat.matcher("cde.log").matches());
        assertTrue(pat.matcher("abc.log").matches());

        // negative lookbehind
        pat = Pattern.compile(".*(?<!abc)\\.log$");
        assertTrue(pat.matcher("cde.log").matches());
        assertFalse(pat.matcher("abc.log").matches());

        // atomic group
        pat = Pattern.compile("(?>a*)abb");
        assertFalse(pat.matcher("aaabb").matches());
        pat = Pattern.compile("(?>a*)bb");
        assertTrue(pat.matcher("aaabb").matches());

        pat = Pattern.compile("(?>a|aa)aabb");
        assertTrue(pat.matcher("aaabb").matches());
        pat = Pattern.compile("(?>aa|a)aabb");
        assertFalse(pat.matcher("aaabb").matches());
    }

    public void _testCorrectReplacementBackreferencedJointSet() {
        Pattern.compile("ab(a)*\\1");
        Pattern.compile("abc(cd)fg");
        Pattern.compile("aba*cd");
        Pattern.compile("ab(a)*+cd");
        Pattern.compile("ab(a)*?cd");
        Pattern.compile("ab(a)+cd");
        Pattern.compile(".*(.)\\1");
        Pattern.compile("ab((a)|c|d)e");
        Pattern.compile("abc((a(b))cd)");
        Pattern.compile("ab(a)++cd");
        Pattern.compile("ab(a)?(c)d");
        Pattern.compile("ab(a)?+cd");
        Pattern.compile("ab(a)??cd");
        Pattern.compile("ab(a)??cd");
        Pattern.compile("ab(a){1,3}?(c)d");
    }

    public void testAlternations() {
        String baseString = "|a|bc";
        Pattern pat = Pattern.compile(baseString);
        Matcher mat = pat.matcher("");

        assertTrue(mat.matches());

        baseString = "a||bc";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());

        baseString = "a|bc|";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());

        baseString = "a|b|";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());

        baseString = "a(|b|cd)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());

        baseString = "a(b||cd)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());

        baseString = "a(b|cd|)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());

        baseString = "a(b|c|)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());

        baseString = "a(|)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());

        baseString = "|";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());

        baseString = "a(?:|)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());

        baseString = "a||||bc";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());
    }

    public void testMatchWithGroups() {
        String baseString = "jwkerhjwehrkwjehrkwjhrwkjehrjwkehrjkwhrkwehrkwhrkwrhwkhrwkjehr";
        String pattern = ".*(..).*\\1.*";
        assertTrue(Pattern.compile(pattern).matcher(baseString).matches());

        baseString = "saa";
        pattern = ".*(.)\\1";
        assertTrue(Pattern.compile(pattern).matcher(baseString).matches());
        assertTrue(Pattern.compile(pattern).matcher(baseString).find());
    }

    public void testSplitEmptyCharSequence() {
        String s1 = "";
        String[] arr = s1.split(":");
        assertEquals(arr.length, 1);
    }

    public void testSplitEndsWithPattern() {
        assertEquals(",,".split(",", 3).length, 3);
        assertEquals(",,".split(",", 4).length, 3);

        assertEquals(Pattern.compile("o").split("boo:and:foo", 5).length, 5);
        assertEquals(Pattern.compile("b").split("ab", -1).length, 2);
    }

    public void testCaseInsensitiveFlag() {
        assertTrue(Pattern.matches("(?i-:AbC)", "ABC"));
    }

    public void testEmptyGroups() {
        Pattern pat = Pattern.compile("ab(?>)cda");
        Matcher mat = pat.matcher("abcda");
        assertTrue(mat.matches());

        pat = Pattern.compile("ab()");
        mat = pat.matcher("ab");
        assertTrue(mat.matches());

        pat = Pattern.compile("abc(?:)(..)");
        mat = pat.matcher("abcgf");
        assertTrue(mat.matches());
    }

    public void testEmbeddedFlags() {
        String baseString = "(?i)((?s)a)";
        String testString = "A";
        Pattern pat = Pattern.compile(baseString);
        Matcher mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?x)(?i)(?s)(?d)a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "(?x)(?i)(?s)(?d)a.";
        testString = "a\n";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "abc(?x:(?i)(?s)(?d)a.)";
        testString = "abcA\n";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());

        baseString = "abc((?x)d)(?i)(?s)a";
        testString = "abcdA";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
    }

    public void testAltWithFlags() {
        Pattern.compile("|(?i-xi)|()");
    }

    public void testRestoreFlagsAfterGroup() {
        String baseString = "abc((?x)d)   a";
        String testString = "abcd   a";
        Pattern pat = Pattern.compile(baseString);
        Matcher mat = pat.matcher(testString);

        assertTrue(mat.matches());
    }

    /*
     * Verify if the Pattern support the following character classes:
     * \p{javaLowerCase} \p{javaUpperCase} \p{javaWhitespace} \p{javaMirrored}
     */
    public void testCompileCharacterClass() {
        // Regression for HARMONY-606, 696
        Pattern pattern = Pattern.compile("\\p{javaLowerCase}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaUpperCase}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaWhitespace}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaMirrored}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaDefined}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaDigit}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaIdentifierIgnorable}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaISOControl}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaJavaIdentifierPart}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaJavaIdentifierStart}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaLetter}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaLetterOrDigit}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaSpaceChar}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaTitleCase}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaUnicodeIdentifierPart}");
        assertNotNull(pattern);

        pattern = Pattern.compile("\\p{javaUnicodeIdentifierStart}");
        assertNotNull(pattern);
    }
}
