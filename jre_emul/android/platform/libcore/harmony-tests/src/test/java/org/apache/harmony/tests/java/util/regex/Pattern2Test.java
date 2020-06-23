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

package org.apache.harmony.tests.java.util.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;

/**
 * Tests simple Pattern compilation and Matcher methods
 */
@SuppressWarnings("nls")
public class Pattern2Test extends TestCase {
    public void testSimpleMatch() throws PatternSyntaxException {
        Pattern p = Pattern.compile("foo.*");

        Matcher m1 = p.matcher("foo123");
        assertTrue(m1.matches());
        assertTrue(m1.find(0));
        assertTrue(m1.lookingAt());

        Matcher m2 = p.matcher("fox");
        assertFalse(m2.matches());
        assertFalse(m2.find(0));
        assertFalse(m2.lookingAt());

        assertTrue(Pattern.matches("foo.*", "foo123"));
        assertFalse(Pattern.matches("foo.*", "fox"));

        assertFalse(Pattern.matches("bar", "foobar"));

        assertTrue(Pattern.matches("", ""));
    }

    public void testCursors() {
        Pattern p;
        Matcher m;

        try {
            p = Pattern.compile("foo");

            m = p.matcher("foobar");
            assertTrue(m.find());
            assertEquals(0, m.start());
            assertEquals(3, m.end());
            assertFalse(m.find());

            // Note: also testing reset here
            m.reset();
            assertTrue(m.find());
            assertEquals(0, m.start());
            assertEquals(3, m.end());
            assertFalse(m.find());

            m.reset("barfoobar");
            assertTrue(m.find());
            assertEquals(3, m.start());
            assertEquals(6, m.end());
            assertFalse(m.find());

            m.reset("barfoo");
            assertTrue(m.find());
            assertEquals(3, m.start());
            assertEquals(6, m.end());
            assertFalse(m.find());

            m.reset("foobarfoobarfoo");
            assertTrue(m.find());
            assertEquals(0, m.start());
            assertEquals(3, m.end());
            assertTrue(m.find());
            assertEquals(6, m.start());
            assertEquals(9, m.end());
            assertTrue(m.find());
            assertEquals(12, m.start());
            assertEquals(15, m.end());
            assertFalse(m.find());
            assertTrue(m.find(0));
            assertEquals(0, m.start());
            assertEquals(3, m.end());
            assertTrue(m.find(4));
            assertEquals(6, m.start());
            assertEquals(9, m.end());
        } catch (PatternSyntaxException e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    public void testGroups() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("(p[0-9]*)#?(q[0-9]*)");

        m = p.matcher("p1#q3p2q42p5p71p63#q888");
        assertTrue(m.find());
        assertEquals(0, m.start());
        assertEquals(5, m.end());
        assertEquals(2, m.groupCount());
        assertEquals(0, m.start(0));
        assertEquals(5, m.end(0));
        assertEquals(0, m.start(1));
        assertEquals(2, m.end(1));
        assertEquals(3, m.start(2));
        assertEquals(5, m.end(2));
        assertEquals("p1#q3", m.group());
        assertEquals("p1#q3", m.group(0));
        assertEquals("p1", m.group(1));
        assertEquals("q3", m.group(2));

        assertTrue(m.find());
        assertEquals(5, m.start());
        assertEquals(10, m.end());
        assertEquals(2, m.groupCount());
        assertEquals(10, m.end(0));
        assertEquals(5, m.start(1));
        assertEquals(7, m.end(1));
        assertEquals(7, m.start(2));
        assertEquals(10, m.end(2));
        assertEquals("p2q42", m.group());
        assertEquals("p2q42", m.group(0));
        assertEquals("p2", m.group(1));
        assertEquals("q42", m.group(2));

        assertTrue(m.find());
        assertEquals(15, m.start());
        assertEquals(23, m.end());
        assertEquals(2, m.groupCount());
        assertEquals(15, m.start(0));
        assertEquals(23, m.end(0));
        assertEquals(15, m.start(1));
        assertEquals(18, m.end(1));
        assertEquals(19, m.start(2));
        assertEquals(23, m.end(2));
        assertEquals("p63#q888", m.group());
        assertEquals("p63#q888", m.group(0));
        assertEquals("p63", m.group(1));
        assertEquals("q888", m.group(2));
        assertFalse(m.find());
    }

    public void testReplace() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        // Note: examples from book,
        // Hitchens, Ron, 2002, "Java NIO", O'Reilly, page 171
        p = Pattern.compile("a*b");

        m = p.matcher("aabfooaabfooabfoob");
        assertTrue(m.replaceAll("-").equals("-foo-foo-foo-"));
        assertTrue(m.replaceFirst("-").equals("-fooaabfooabfoob"));

        /*
         * p = Pattern.compile ("\\p{Blank}");
         * 
         * m = p.matcher ("fee fie foe fum"); assertTrue
         * (m.replaceFirst("-").equals ("fee-fie foe fum")); assertTrue
         * (m.replaceAll("-").equals ("fee-fie-foe-fum"));
         */

        p = Pattern.compile("([bB])yte");

        m = p.matcher("Byte for byte");
        assertTrue(m.replaceFirst("$1ite").equals("Bite for byte"));
        assertTrue(m.replaceAll("$1ite").equals("Bite for bite"));

        p = Pattern.compile("\\d\\d\\d\\d([- ])");

        m = p.matcher("card #1234-5678-1234");
        assertTrue(m.replaceFirst("xxxx$1").equals("card #xxxx-5678-1234"));
        assertTrue(m.replaceAll("xxxx$1").equals("card #xxxx-xxxx-1234"));

        p = Pattern.compile("(up|left)( *)(right|down)");

        m = p.matcher("left right, up down");
        assertTrue(m.replaceFirst("$3$2$1").equals("right left, up down"));
        assertTrue(m.replaceAll("$3$2$1").equals("right left, down up"));

        p = Pattern.compile("([CcPp][hl]e[ea]se)");

        m = p.matcher("I want cheese. Please.");
        assertTrue(m.replaceFirst("<b> $1 </b>").equals(
                "I want <b> cheese </b>. Please."));
        assertTrue(m.replaceAll("<b> $1 </b>").equals(
                "I want <b> cheese </b>. <b> Please </b>."));
    }

    public void testEscapes() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        // Test \\ sequence
        p = Pattern.compile("([a-z]+)\\\\([a-z]+);");
        m = p.matcher("fred\\ginger;abbott\\costello;jekell\\hyde;");
        assertTrue(m.find());
        assertEquals("fred", m.group(1));
        assertEquals("ginger", m.group(2));
        assertTrue(m.find());
        assertEquals("abbott", m.group(1));
        assertEquals("costello", m.group(2));
        assertTrue(m.find());
        assertEquals("jekell", m.group(1));
        assertEquals("hyde", m.group(2));
        assertFalse(m.find());

        // Test \n, \t, \r, \f, \e, \a sequences
        p = Pattern.compile("([a-z]+)[\\n\\t\\r\\f\\e\\a]+([a-z]+)");
        m = p.matcher("aa\nbb;cc\u0009\rdd;ee\u000C\u001Bff;gg\n\u0007hh");
        assertTrue(m.find());
        assertEquals("aa", m.group(1));
        assertEquals("bb", m.group(2));
        assertTrue(m.find());
        assertEquals("cc", m.group(1));
        assertEquals("dd", m.group(2));
        assertTrue(m.find());
        assertEquals("ee", m.group(1));
        assertEquals("ff", m.group(2));
        assertTrue(m.find());
        assertEquals("gg", m.group(1));
        assertEquals("hh", m.group(2));
        assertFalse(m.find());

        // Test \\u and \\x sequences
p = Pattern.compile("([0-9]+)[\\u0020:\\x21];");
        m = p.matcher("11:;22 ;33-;44!;");
        assertTrue(m.find());
        assertEquals("11", m.group(1));
        assertTrue(m.find());
        assertEquals("22", m.group(1));
        assertTrue(m.find());
        assertEquals("44", m.group(1));
        assertFalse(m.find());

        // Test invalid unicode sequences
        try {
            p = Pattern.compile("\\u");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\u;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\u002");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\u002;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        // Test invalid hex sequences
        try {
            p = Pattern.compile("\\x");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\x;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        // icu4c allows 1 to 6 hex digits in \x escapes.
        p = Pattern.compile("\\xa");
        p = Pattern.compile("\\xab");
        p = Pattern.compile("\\xabc");
        p = Pattern.compile("\\xabcd");
        p = Pattern.compile("\\xabcde");
        p = Pattern.compile("\\xabcdef");
        // (Further digits would just be treated as characters after the escape.)
        try {
            p = Pattern.compile("\\xg");
            fail();
        } catch (PatternSyntaxException expected) {
        }

        // Test \0 (octal) sequences (1, 2 and 3 digit)
        p = Pattern.compile("([0-9]+)[\\07\\040\\0160];");
        m = p.matcher("11\u0007;22:;33 ;44p;");
        assertTrue(m.find());
        assertEquals("11", m.group(1));
        assertTrue(m.find());
        assertEquals("33", m.group(1));
        assertTrue(m.find());
        assertEquals("44", m.group(1));
        assertFalse(m.find());

        // Test invalid octal sequences
        try {
            p = Pattern.compile("\\08");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        // originally contributed test did not check the result
        // TODO: check what RI does here
        // try {
        // p = Pattern.compile("\\0477");
        // fail("PatternSyntaxException expected");
        // } catch (PatternSyntaxException e) {
        // }

        try {
            p = Pattern.compile("\\0");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\0;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        // Test \c (control character) sequence
        p = Pattern.compile("([0-9]+)[\\cA\\cB\\cC\\cD];");
        m = p.matcher("11\u0001;22:;33\u0002;44p;55\u0003;66\u0004;");
        assertTrue(m.find());
        assertEquals("11", m.group(1));
        assertTrue(m.find());
        assertEquals("33", m.group(1));
        assertTrue(m.find());
        assertEquals("55", m.group(1));
        assertTrue(m.find());
        assertEquals("66", m.group(1));
        assertFalse(m.find());

        // More thorough control escape test
        // Ensure that each escape matches exactly the corresponding
        // character
        // code and no others (well, from 0-255 at least)
        int i, j;
        for (i = 0; i < 26; i++) {
            p = Pattern.compile("\\c" + Character.toString((char) ('A' + i)));
            int match_char = -1;
            for (j = 0; j < 255; j++) {
                m = p.matcher(Character.toString((char) j));
                if (m.matches()) {
                    assertEquals(-1, match_char);
                    match_char = j;
                }
            }
            assertTrue(match_char == i + 1);
        }

        // Test invalid control escapes
        // icu4c 50 accepts this pattern, and treats it as a literal.
        //try {
            p = Pattern.compile("\\c");
            assertTrue(p.matcher("x\\cy").find());
        //    fail(p.matcher("").toString());
        //} catch (PatternSyntaxException e) {
        //}

        // But \cH works.
        p = Pattern.compile("\\cH");
        assertTrue(p.matcher("x\u0008y").find());
        assertFalse(p.matcher("x\\cHy").find());

        // originally contributed test did not check the result
        // TODO: check what RI does here
        // try {
        // p = Pattern.compile("\\c;");
        // fail("PatternSyntaxException expected");
        // } catch (PatternSyntaxException e) {
        // }
        //
        // try {
        // p = Pattern.compile("\\ca;");
        // fail("PatternSyntaxException expected");
        // } catch (PatternSyntaxException e) {
        // }
        //
        // try {
        // p = Pattern.compile("\\c4;");
        // fail("PatternSyntaxException expected");
        // } catch (PatternSyntaxException e) {
        // }
    }

    public void testCharacterClasses() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        // Test one character range
        p = Pattern.compile("[p].*[l]");
        m = p.matcher("paul");
        assertTrue(m.matches());
        m = p.matcher("pool");
        assertTrue(m.matches());
        m = p.matcher("pong");
        assertFalse(m.matches());
        m = p.matcher("pl");
        assertTrue(m.matches());

        // Test two character range
        p = Pattern.compile("[pm].*[lp]");
        m = p.matcher("prop");
        assertTrue(m.matches());
        m = p.matcher("mall");
        assertTrue(m.matches());
        m = p.matcher("pong");
        assertFalse(m.matches());
        m = p.matcher("pill");
        assertTrue(m.matches());

        // Test range including [ and ]
        p = Pattern.compile("[<\\[].*[\\]>]");
        m = p.matcher("<foo>");
        assertTrue(m.matches());
        m = p.matcher("[bar]");
        assertTrue(m.matches());
        m = p.matcher("{foobar]");
        assertFalse(m.matches());
        m = p.matcher("<pill]");
        assertTrue(m.matches());

        // Test range using ^
        p = Pattern.compile("[^bc][a-z]+[tr]");
        m = p.matcher("pat");
        assertTrue(m.matches());
        m = p.matcher("liar");
        assertTrue(m.matches());
        m = p.matcher("car");
        assertFalse(m.matches());
        m = p.matcher("gnat");
        assertTrue(m.matches());

        // Test character range using -
        p = Pattern.compile("[a-z]_+[a-zA-Z]-+[0-9p-z]");
        m = p.matcher("d__F-8");
        assertTrue(m.matches());
        m = p.matcher("c_a-q");
        assertTrue(m.matches());
        m = p.matcher("a__R-a");
        assertFalse(m.matches());
        m = p.matcher("r_____d-----5");
        assertTrue(m.matches());

        // Test range using unicode characters and unicode and hex escapes
        p = Pattern.compile("[\\u1234-\\u2345]_+[a-z]-+[\u0001-\\x11]");
        m = p.matcher("\u2000_q-\u0007");
        assertTrue(m.matches());
        m = p.matcher("\u1234_z-\u0001");
        assertTrue(m.matches());
        m = p.matcher("r_p-q");
        assertFalse(m.matches());
        m = p.matcher("\u2345_____d-----\n");
        assertTrue(m.matches());

        // Test ranges including the "-" character
        // "---" collides with icu4c's "--" operator, and likely to be user error anyway.
        if (false) {
            p = Pattern.compile("[\\*-/]_+[---]!+[--AP]");
            m = p.matcher("-_-!!A");
            assertTrue(m.matches());
            m = p.matcher("\u002b_-!!!-");
            assertTrue(m.matches());
            m = p.matcher("!_-!@");
            assertFalse(m.matches());
            m = p.matcher(",______-!!!!!!!P");
            assertTrue(m.matches());
        }

        // Test nested ranges
        p = Pattern.compile("[pm[t]][a-z]+[[r]lp]");
        m = p.matcher("prop");
        assertTrue(m.matches());
        m = p.matcher("tsar");
        assertTrue(m.matches());
        m = p.matcher("pong");
        assertFalse(m.matches());
        m = p.matcher("moor");
        assertTrue(m.matches());

        // Test character class intersection with &&
        // TODO: figure out what x&&y or any class with a null intersection
        // set (like [[a-c]&&[d-f]]) might mean. It doesn't mean "match
        // nothing" and doesn't mean "match anything" so I'm stumped.
        p = Pattern.compile("[[a-p]&&[g-z]]+-+[[a-z]&&q]-+[x&&[a-z]]-+");
        m = p.matcher("h--q--x--");
        assertTrue(m.matches());
        m = p.matcher("hog--q-x-");
        assertTrue(m.matches());
        m = p.matcher("ape--q-x-");
        assertFalse(m.matches());
        m = p.matcher("mop--q-x----");
        assertTrue(m.matches());

        // Test error cases with &&
        // This is an RI bug that icu4c doesn't have.
        if (false) {
            p = Pattern.compile("[&&[xyz]]");
            m = p.matcher("&");
            // System.out.println(m.matches());
            m = p.matcher("x");
            // System.out.println(m.matches());
            m = p.matcher("y");
            // System.out.println(m.matches());
        }
        p = Pattern.compile("[[xyz]&[axy]]");
        m = p.matcher("x");
        // System.out.println(m.matches());
        m = p.matcher("z");
        // System.out.println(m.matches());
        m = p.matcher("&");
        // System.out.println(m.matches());
        p = Pattern.compile("[abc[123]&&[345]def]");
        m = p.matcher("a");
        // System.out.println(m.matches());

        // icu4c rightly considers a missing rhs to && a syntax error.
        if (false) {
            p = Pattern.compile("[[xyz]&&]");
        }

        p = Pattern.compile("[[abc]&]");

        try {
            p = Pattern.compile("[[abc]&&");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        p = Pattern.compile("[[abc]\\&&[xyz]]");

        p = Pattern.compile("[[abc]&\\&[xyz]]");

        // Test 3-way intersection
        p = Pattern.compile("[[a-p]&&[g-z]&&[d-k]]");
        m = p.matcher("g");
        assertTrue(m.matches());
        m = p.matcher("m");
        assertFalse(m.matches());

        // Test nested intersection
        p = Pattern.compile("[[[a-p]&&[g-z]]&&[d-k]]");
        m = p.matcher("g");
        assertTrue(m.matches());
        m = p.matcher("m");
        assertFalse(m.matches());

        // Test character class subtraction with && and ^
        p = Pattern.compile("[[a-z]&&[^aeiou]][aeiou][[^xyz]&&[a-z]]");
        m = p.matcher("pop");
        assertTrue(m.matches());
        m = p.matcher("tag");
        assertTrue(m.matches());
        m = p.matcher("eat");
        assertFalse(m.matches());
        m = p.matcher("tax");
        assertFalse(m.matches());
        m = p.matcher("zip");
        assertTrue(m.matches());

        // Test . (DOT), with and without DOTALL
        // Note: DOT not allowed in character classes
        p = Pattern.compile(".+/x.z");
        m = p.matcher("!$/xyz");
        assertTrue(m.matches());
        m = p.matcher("%\n\r/x\nz");
        assertFalse(m.matches());
        p = Pattern.compile(".+/x.z", Pattern.DOTALL);
        m = p.matcher("%\n\r/x\nz");
        assertTrue(m.matches());

        // Test \d (digit)
        p = Pattern.compile("\\d+[a-z][\\dx]");
        m = p.matcher("42a6");
        assertTrue(m.matches());
        m = p.matcher("21zx");
        assertTrue(m.matches());
        m = p.matcher("ab6");
        assertFalse(m.matches());
        m = p.matcher("56912f9");
        assertTrue(m.matches());

        // Test \D (not a digit)
        p = Pattern.compile("\\D+[a-z]-[\\D3]");
        m = p.matcher("za-p");
        assertTrue(m.matches());
        m = p.matcher("%!e-3");
        assertTrue(m.matches());
        m = p.matcher("9a-x");
        assertFalse(m.matches());
        m = p.matcher("\u1234pp\ny-3");
        assertTrue(m.matches());

        // Test \s (whitespace)
        p = Pattern.compile("<[a-zA-Z]+\\s+[0-9]+[\\sx][^\\s]>");
        m = p.matcher("<cat \t1\fx>");
        assertTrue(m.matches());
        m = p.matcher("<cat \t1\f >");
        assertFalse(m.matches());
        m = p
                .matcher("xyz <foo\n\r22 5> <pp \t\n\f\r \u000b41x\u1234><pp \nx7\rc> zzz");
        assertTrue(m.find());
        assertTrue(m.find());
        assertFalse(m.find());

        // Test \S (not whitespace)
        p = Pattern.compile("<[a-z] \\S[0-9][\\S\n]+[^\\S]221>");
        m = p.matcher("<f $0**\n** 221>");
        assertTrue(m.matches());
        m = p.matcher("<x 441\t221>");
        assertTrue(m.matches());
        m = p.matcher("<z \t9\ng 221>");
        assertFalse(m.matches());
        m = p.matcher("<z 60\ngg\u1234\f221>");
        assertTrue(m.matches());
        p = Pattern.compile("<[a-z] \\S[0-9][\\S\n]+[^\\S]221[\\S&&[^abc]]>");
        m = p.matcher("<f $0**\n** 221x>");
        assertTrue(m.matches());
        m = p.matcher("<x 441\t221z>");
        assertTrue(m.matches());
        m = p.matcher("<x 441\t221 >");
        assertFalse(m.matches());
        m = p.matcher("<x 441\t221c>");
        assertFalse(m.matches());
        m = p.matcher("<z \t9\ng 221x>");
        assertFalse(m.matches());
        m = p.matcher("<z 60\ngg\u1234\f221\u0001>");
        assertTrue(m.matches());

        // Test \w (ascii word)
        p = Pattern.compile("<\\w+\\s[0-9]+;[^\\w]\\w+/[\\w$]+;");
        m = p.matcher("<f1 99;!foo5/a$7;");
        assertTrue(m.matches());
        m = p.matcher("<f$ 99;!foo5/a$7;");
        assertFalse(m.matches());
        m = p
                .matcher("<abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789 99;!foo5/a$7;");
        assertTrue(m.matches());

        // Test \W (not an ascii word)
        p = Pattern.compile("<\\W\\w+\\s[0-9]+;[\\W_][^\\W]+\\s[0-9]+;");
        m = p.matcher("<$foo3\n99;_bar\t0;");
        assertTrue(m.matches());
        m = p.matcher("<hh 99;_g 0;");
        assertFalse(m.matches());
        m = p.matcher("<*xx\t00;^zz\f11;");
        assertTrue(m.matches());

        // Test x|y pattern
        // TODO
    }

    public void testPOSIXGroups() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        // Test POSIX groups using \p and \P (in the group and not in the group)
        // Groups are Lower, Upper, ASCII, Alpha, Digit, XDigit, Alnum, Punct,
        // Graph, Print, Blank, Space, Cntrl
        // Test \p{Lower}
        /*
         * FIXME: Requires complex range processing p = Pattern.compile("<\\p{Lower}\\d\\P{Lower}:[\\p{Lower}Z]\\s[^\\P{Lower}]>");
         * m = p.matcher("<a4P:g x>"); assertTrue(m.matches()); m = p.matcher("<p4%:Z\tq>");
         * assertTrue(m.matches()); m = p.matcher("<A6#:e e>");
         * assertFalse(m.matches());
         */
        p = Pattern.compile("\\p{Lower}+");
        m = p.matcher("abcdefghijklmnopqrstuvwxyz");
        assertTrue(m.matches());

        // Invalid uses of \p{Lower}
        try {
            p = Pattern.compile("\\p");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\p;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\p{");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\p{;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\p{Lower");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\p{Lower;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        // Test \p{Upper}
        /*
         * FIXME: Requires complex range processing p = Pattern.compile("<\\p{Upper}\\d\\P{Upper}:[\\p{Upper}z]\\s[^\\P{Upper}]>");
         * m = p.matcher("<A4p:G X>"); assertTrue(m.matches()); m = p.matcher("<P4%:z\tQ>");
         * assertTrue(m.matches()); m = p.matcher("<a6#:E E>");
         * assertFalse(m.matches());
         */
        p = Pattern.compile("\\p{Upper}+");
        m = p.matcher("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        assertTrue(m.matches());

        // Invalid uses of \p{Upper}
        try {
            p = Pattern.compile("\\p{Upper");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\p{Upper;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        // Test \p{ASCII}
        /*
         * FIXME: Requires complex range processing p = Pattern.compile("<\\p{ASCII}\\d\\P{ASCII}:[\\p{ASCII}\u1234]\\s[^\\P{ASCII}]>");
         * m = p.matcher("<A4\u0080:G X>"); assertTrue(m.matches()); m =
         * p.matcher("<P4\u00ff:\u1234\t\n>"); assertTrue(m.matches()); m =
         * p.matcher("<\u00846#:E E>"); assertFalse(m.matches())
         */
        int i;
        p = Pattern.compile("\\p{ASCII}");
        for (i = 0; i < 0x80; i++) {
            m = p.matcher(Character.toString((char) i));
            assertTrue(m.matches());
        }
        for (; i < 0xff; i++) {
            m = p.matcher(Character.toString((char) i));
            assertFalse(m.matches());
        }

        // Invalid uses of \p{ASCII}
        try {
            p = Pattern.compile("\\p{ASCII");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        try {
            p = Pattern.compile("\\p{ASCII;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }

        // Test \p{Alpha}
        // TODO

        // Test \p{Digit}
        // TODO

        // Test \p{XDigit}
        // TODO

        // Test \p{Alnum}
        // TODO

        // Test \p{Punct}
        // TODO

        // Test \p{Graph}
        // TODO

        // Test \p{Print}
        // TODO

        // Test \p{Blank}
        // TODO

        // Test \p{Space}
        // TODO

        // Test \p{Cntrl}
        // TODO
    }

    public void testUnicodeBlocks() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        int i, j;

        // Test Unicode blocks using \p and \P
        // FIXME:
        // Note that LatinExtended-B and ArabicPresentations-B are unrecognized
        // by the reference JDK.
        for (i = 0; i < UBlocks.length; i++) {
            /*
             * p = Pattern.compile("\\p{"+UBlocks[i].name+"}");
             * 
             * if (UBlocks[i].low > 0) { m =
             * p.matcher(Character.toString((char)(UBlocks[i].low-1)));
             * assertFalse(m.matches()); } for (j=UBlocks[i].low; j <=
             * UBlocks[i].high; j++) { m =
             * p.matcher(Character.toString((char)j)); assertTrue(m.matches()); }
             * if (UBlocks[i].high < 0xFFFF) { m =
             * p.matcher(Character.toString((char)(UBlocks[i].high+1)));
             * assertFalse(m.matches()); }
             * 
             * p = Pattern.compile("\\P{"+UBlocks[i].name+"}");
             * 
             * if (UBlocks[i].low > 0) { m =
             * p.matcher(Character.toString((char)(UBlocks[i].low-1)));
             * assertTrue(m.matches()); } for (j=UBlocks[i].low; j <
             * UBlocks[i].high; j++) { m =
             * p.matcher(Character.toString((char)j)); assertFalse(m.matches()); }
             * if (UBlocks[i].high < 0xFFFF) { m =
             * p.matcher(Character.toString((char)(UBlocks[i].high+1)));
             * assertTrue(m.matches()); }
             */

            p = Pattern.compile("\\p{In" + UBlocks[i].name + "}");

            if (UBlocks[i].low > 0) {
                m = p.matcher(Character.toString((char) (UBlocks[i].low - 1)));
                assertFalse(UBlocks[i].name, m.matches());
            }
            for (j = UBlocks[i].low; j <= UBlocks[i].high; j++) {
                m = p.matcher(Character.toString((char) j));
                assertTrue(UBlocks[i].name, m.matches());
            }
            if (UBlocks[i].high < 0xFFFF) {
                m = p.matcher(Character.toString((char) (UBlocks[i].high + 1)));
                assertFalse(UBlocks[i].name, m.matches());
            }

            p = Pattern.compile("\\P{In" + UBlocks[i].name + "}");

            if (UBlocks[i].low > 0) {
                m = p.matcher(Character.toString((char) (UBlocks[i].low - 1)));
                assertTrue(UBlocks[i].name, m.matches());
            }
            for (j = UBlocks[i].low; j < UBlocks[i].high; j++) {
                m = p.matcher(Character.toString((char) j));
                assertFalse(UBlocks[i].name, m.matches());
            }
            if (UBlocks[i].high < 0xFFFF) {
                m = p.matcher(Character.toString((char) (UBlocks[i].high + 1)));
                assertTrue(UBlocks[i].name, m.matches());
            }
        }
    }

    public void testMisc() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        // Test (?>...)
        // TODO

        // Test (?onflags-offflags)
        // Valid flags are i,m,d,s,u,x
        // TODO

        // Test (?onflags-offflags:...)
        // TODO

        // Test \Q, \E
        p = Pattern.compile("[a-z]+;\\Q[a-z]+;\\Q(foo.*);\\E[0-9]+");
        m = p.matcher("abc;[a-z]+;\\Q(foo.*);411");
        assertTrue(m.matches());
        m = p.matcher("abc;def;foo42;555");
        assertFalse(m.matches());
        m = p.matcher("abc;\\Qdef;\\Qfoo99;\\E123");
        assertFalse(m.matches());

        p = Pattern.compile("[a-z]+;(foo[0-9]-\\Q(...)\\E);[0-9]+");
        m = p.matcher("abc;foo5-(...);123");
        assertTrue(m.matches());
        assertEquals("foo5-(...)", m.group(1));
        m = p.matcher("abc;foo9-(xxx);789");
        assertFalse(m.matches());

        p = Pattern.compile("[a-z]+;(bar[0-9]-[a-z\\Q$-\\E]+);[0-9]+");
        m = p.matcher("abc;bar0-def$-;123");
        assertTrue(m.matches());

        // FIXME:
        // This should work the same as the pattern above but fails with the
        // the reference JDK
        p = Pattern.compile("[a-z]+;(bar[0-9]-[a-z\\Q-$\\E]+);[0-9]+");
        m = p.matcher("abc;bar0-def$-;123");
        // assertTrue(m.matches());

        // FIXME:
        // This should work too .. it looks as if just about anything that
        // has more
        // than one character between \Q and \E is broken in the the reference
        // JDK
        p = Pattern.compile("[a-z]+;(bar[0-9]-[a-z\\Q[0-9]\\E]+);[0-9]+");
        m = p.matcher("abc;bar0-def[99]-]0x[;123");
        // assertTrue(m.matches());

        // This is the same as above but with explicit escapes .. and this
        // does work
        // on the the reference JDK
        p = Pattern.compile("[a-z]+;(bar[0-9]-[a-z\\[0\\-9\\]]+);[0-9]+");
        m = p.matcher("abc;bar0-def[99]-]0x[;123");
        assertTrue(m.matches());

        // Test #<comment text>
        // TODO
    }

    public void testCompile1() throws PatternSyntaxException {
        Pattern pattern = Pattern
                .compile("[0-9A-Za-z][0-9A-Za-z\\x2e\\x3a\\x2d\\x5f]*");
        String name = "iso-8859-1";
        assertTrue(pattern.matcher(name).matches());
    }

    public void testCompile2() throws PatternSyntaxException {
        String findString = "\\Qimport\\E";

        Pattern pattern = Pattern.compile(findString, 0);
        Matcher matcher = pattern.matcher(new String(
                "import a.A;\n\n import b.B;\nclass C {}"));

        assertTrue(matcher.find(0));
    }

    public void testCompile3() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("a$");
        m = p.matcher("a\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertFalse(m.find());

        p = Pattern.compile("(a$)");
        m = p.matcher("a\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertEquals("a", m.group(1));
        assertFalse(m.find());

        p = Pattern.compile("^.*$", Pattern.MULTILINE);

        m = p.matcher("a\n");
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertEquals("a", m.group());
        assertFalse(m.find());

        m = p.matcher("a\nb\n");
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertEquals("a", m.group());
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertEquals("b", m.group());
        assertFalse(m.find());

        m = p.matcher("a\nb");
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertEquals("a", m.group());
        assertTrue(m.find());
        assertEquals("b", m.group());
        assertFalse(m.find());

        m = p.matcher("\naa\r\nbb\rcc\n\n");
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertTrue(m.group().equals(""));
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertEquals("aa", m.group());
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertEquals("bb", m.group());
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertEquals("cc", m.group());
        assertTrue(m.find());
        // System.out.println("["+m.group()+"]");
        assertTrue(m.group().equals(""));
        assertFalse(m.find());

        m = p.matcher("a");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertFalse(m.find());

        m = p.matcher("");
        // This differs from the RI behaviour but seems more correct.
        assertTrue(m.find());
        assertTrue(m.group().equals(""));
        assertFalse(m.find());

        p = Pattern.compile("^.*$");
        m = p.matcher("");
        assertTrue(m.find());
        assertTrue(m.group().equals(""));
        assertFalse(m.find());
    }

    public void testCompile4() throws PatternSyntaxException {
        String findString = "\\Qpublic\\E";
        StringBuffer text = new StringBuffer("    public class Class {\n"
                + "    public class Class {");

        Pattern pattern = Pattern.compile(findString, 0);
        Matcher matcher = pattern.matcher(text);

        boolean found = matcher.find();
        assertTrue(found);
        assertEquals(4, matcher.start());
        if (found) {
            // modify text
            text.delete(0, text.length());
            text.append("Text have been changed.");
            matcher.reset(text);
        }

        found = matcher.find();
        assertFalse(found);
    }

    public void testCompile5() throws PatternSyntaxException {
        Pattern p = Pattern.compile("^[0-9]");
        String s[] = p.split("12", -1);
        assertEquals("", s[0]);
        assertEquals("2", s[1]);
        assertEquals(2, s.length);
    }

    // public void testCompile6() {
    // String regex = "[\\p{L}[\\p{Mn}[\\p{Pc}[\\p{Nd}[\\p{Nl}[\\p{Sc}]]]]]]+";
    // String regex = "[\\p{L}\\p{Mn}\\p{Pc}\\p{Nd}\\p{Nl}\\p{Sc}]+";
    // try {
    // Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    // assertTrue(true);
    // } catch (PatternSyntaxException e) {
    // System.out.println(e.getMessage());
    // assertTrue(false);
    // }
    // }

    private static class UBInfo {
        public UBInfo(int low, int high, String name) {
            this.name = name;
            this.low = low;
            this.high = high;
        }

        public String name;

        public int low, high;
    }

    // A table representing the unicode categories
    // private static UBInfo[] UCategories = {
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
    // };

    // A table representing the unicode character blocks
    private static UBInfo[] UBlocks = {
    /* 0000; 007F; Basic Latin */
    new UBInfo(0x0000, 0x007F, "BasicLatin"), // Character.UnicodeBlock.BASIC_LATIN
            /* 0080; 00FF; Latin-1 Supplement */
            new UBInfo(0x0080, 0x00FF, "Latin-1Supplement"), // Character.UnicodeBlock.LATIN_1_SUPPLEMENT
            /* 0100; 017F; Latin Extended-A */
            new UBInfo(0x0100, 0x017F, "LatinExtended-A"), // Character.UnicodeBlock.LATIN_EXTENDED_A
            /* 0180; 024F; Latin Extended-B */
            // new UBInfo (0x0180,0x024F,"InLatinExtended-B"), //
            // Character.UnicodeBlock.LATIN_EXTENDED_B
            /* 0250; 02AF; IPA Extensions */
            new UBInfo(0x0250, 0x02AF, "IPAExtensions"), // Character.UnicodeBlock.IPA_EXTENSIONS
            /* 02B0; 02FF; Spacing Modifier Letters */
            new UBInfo(0x02B0, 0x02FF, "SpacingModifierLetters"), // Character.UnicodeBlock.SPACING_MODIFIER_LETTERS
            /* 0300; 036F; Combining Diacritical Marks */
            new UBInfo(0x0300, 0x036F, "CombiningDiacriticalMarks"), // Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS
            /* 0370; 03FF; Greek */
            new UBInfo(0x0370, 0x03FF, "Greek"), // Character.UnicodeBlock.GREEK
            /* 0400; 04FF; Cyrillic */
            new UBInfo(0x0400, 0x04FF, "Cyrillic"), // Character.UnicodeBlock.CYRILLIC
            /* 0530; 058F; Armenian */
            new UBInfo(0x0530, 0x058F, "Armenian"), // Character.UnicodeBlock.ARMENIAN
            /* 0590; 05FF; Hebrew */
            new UBInfo(0x0590, 0x05FF, "Hebrew"), // Character.UnicodeBlock.HEBREW
            /* 0600; 06FF; Arabic */
            new UBInfo(0x0600, 0x06FF, "Arabic"), // Character.UnicodeBlock.ARABIC
            /* 0700; 074F; Syriac */
            new UBInfo(0x0700, 0x074F, "Syriac"), // Character.UnicodeBlock.SYRIAC
            /* 0780; 07BF; Thaana */
            new UBInfo(0x0780, 0x07BF, "Thaana"), // Character.UnicodeBlock.THAANA
            /* 0900; 097F; Devanagari */
            new UBInfo(0x0900, 0x097F, "Devanagari"), // Character.UnicodeBlock.DEVANAGARI
            /* 0980; 09FF; Bengali */
            new UBInfo(0x0980, 0x09FF, "Bengali"), // Character.UnicodeBlock.BENGALI
            /* 0A00; 0A7F; Gurmukhi */
            new UBInfo(0x0A00, 0x0A7F, "Gurmukhi"), // Character.UnicodeBlock.GURMUKHI
            /* 0A80; 0AFF; Gujarati */
            new UBInfo(0x0A80, 0x0AFF, "Gujarati"), // Character.UnicodeBlock.GUJARATI
            /* 0B00; 0B7F; Oriya */
            new UBInfo(0x0B00, 0x0B7F, "Oriya"), // Character.UnicodeBlock.ORIYA
            /* 0B80; 0BFF; Tamil */
            new UBInfo(0x0B80, 0x0BFF, "Tamil"), // Character.UnicodeBlock.TAMIL
            /* 0C00; 0C7F; Telugu */
            new UBInfo(0x0C00, 0x0C7F, "Telugu"), // Character.UnicodeBlock.TELUGU
            /* 0C80; 0CFF; Kannada */
            new UBInfo(0x0C80, 0x0CFF, "Kannada"), // Character.UnicodeBlock.KANNADA
            /* 0D00; 0D7F; Malayalam */
            new UBInfo(0x0D00, 0x0D7F, "Malayalam"), // Character.UnicodeBlock.MALAYALAM
            /* 0D80; 0DFF; Sinhala */
            new UBInfo(0x0D80, 0x0DFF, "Sinhala"), // Character.UnicodeBlock.SINHALA
            /* 0E00; 0E7F; Thai */
            new UBInfo(0x0E00, 0x0E7F, "Thai"), // Character.UnicodeBlock.THAI
            /* 0E80; 0EFF; Lao */
            new UBInfo(0x0E80, 0x0EFF, "Lao"), // Character.UnicodeBlock.LAO
            /* 0F00; 0FFF; Tibetan */
            new UBInfo(0x0F00, 0x0FFF, "Tibetan"), // Character.UnicodeBlock.TIBETAN
            /* 1000; 109F; Myanmar */
            new UBInfo(0x1000, 0x109F, "Myanmar"), // Character.UnicodeBlock.MYANMAR
            /* 10A0; 10FF; Georgian */
            new UBInfo(0x10A0, 0x10FF, "Georgian"), // Character.UnicodeBlock.GEORGIAN
            /* 1100; 11FF; Hangul Jamo */
            new UBInfo(0x1100, 0x11FF, "HangulJamo"), // Character.UnicodeBlock.HANGUL_JAMO
            /* 1200; 137F; Ethiopic */
            new UBInfo(0x1200, 0x137F, "Ethiopic"), // Character.UnicodeBlock.ETHIOPIC
            /* 13A0; 13FF; Cherokee */
            new UBInfo(0x13A0, 0x13FF, "Cherokee"), // Character.UnicodeBlock.CHEROKEE
            /* 1400; 167F; Unified Canadian Aboriginal Syllabics */
            new UBInfo(0x1400, 0x167F, "UnifiedCanadianAboriginalSyllabics"), // Character.UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS
            /* 1680; 169F; Ogham */
            new UBInfo(0x1680, 0x169F, "Ogham"), // Character.UnicodeBlock.OGHAM
            /* 16A0; 16FF; Runic */
            new UBInfo(0x16A0, 0x16FF, "Runic"), // Character.UnicodeBlock.RUNIC
            /* 1780; 17FF; Khmer */
            new UBInfo(0x1780, 0x17FF, "Khmer"), // Character.UnicodeBlock.KHMER
            /* 1800; 18AF; Mongolian */
            new UBInfo(0x1800, 0x18AF, "Mongolian"), // Character.UnicodeBlock.MONGOLIAN
            /* 1E00; 1EFF; Latin Extended Additional */
            new UBInfo(0x1E00, 0x1EFF, "LatinExtendedAdditional"), // Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL
            /* 1F00; 1FFF; Greek Extended */
            new UBInfo(0x1F00, 0x1FFF, "GreekExtended"), // Character.UnicodeBlock.GREEK_EXTENDED
            /* 2000; 206F; General Punctuation */
            new UBInfo(0x2000, 0x206F, "GeneralPunctuation"), // Character.UnicodeBlock.GENERAL_PUNCTUATION
            /* 2070; 209F; Superscripts and Subscripts */
            new UBInfo(0x2070, 0x209F, "SuperscriptsandSubscripts"), // Character.UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS
            /* 20A0; 20CF; Currency Symbols */
            new UBInfo(0x20A0, 0x20CF, "CurrencySymbols"), // Character.UnicodeBlock.CURRENCY_SYMBOLS
            /* 20D0; 20FF; Combining Marks for Symbols */
            new UBInfo(0x20D0, 0x20FF, "CombiningMarksforSymbols"), // Character.UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS
            /* 2100; 214F; Letterlike Symbols */
            new UBInfo(0x2100, 0x214F, "LetterlikeSymbols"), // Character.UnicodeBlock.LETTERLIKE_SYMBOLS
            /* 2150; 218F; Number Forms */
            new UBInfo(0x2150, 0x218F, "NumberForms"), // Character.UnicodeBlock.NUMBER_FORMS
            /* 2190; 21FF; Arrows */
            new UBInfo(0x2190, 0x21FF, "Arrows"), // Character.UnicodeBlock.ARROWS
            /* 2200; 22FF; Mathematical Operators */
            new UBInfo(0x2200, 0x22FF, "MathematicalOperators"), // Character.UnicodeBlock.MATHEMATICAL_OPERATORS
            /* 2300; 23FF; Miscellaneous Technical */
            new UBInfo(0x2300, 0x23FF, "MiscellaneousTechnical"), // Character.UnicodeBlock.MISCELLANEOUS_TECHNICAL
            /* 2400; 243F; Control Pictures */
            new UBInfo(0x2400, 0x243F, "ControlPictures"), // Character.UnicodeBlock.CONTROL_PICTURES
            /* 2440; 245F; Optical Character Recognition */
            new UBInfo(0x2440, 0x245F, "OpticalCharacterRecognition"), // Character.UnicodeBlock.OPTICAL_CHARACTER_RECOGNITION
            /* 2460; 24FF; Enclosed Alphanumerics */
            new UBInfo(0x2460, 0x24FF, "EnclosedAlphanumerics"), // Character.UnicodeBlock.ENCLOSED_ALPHANUMERICS
            /* 2500; 257F; Box Drawing */
            new UBInfo(0x2500, 0x257F, "BoxDrawing"), // Character.UnicodeBlock.BOX_DRAWING
            /* 2580; 259F; Block Elements */
            new UBInfo(0x2580, 0x259F, "BlockElements"), // Character.UnicodeBlock.BLOCK_ELEMENTS
            /* 25A0; 25FF; Geometric Shapes */
            new UBInfo(0x25A0, 0x25FF, "GeometricShapes"), // Character.UnicodeBlock.GEOMETRIC_SHAPES
            /* 2600; 26FF; Miscellaneous Symbols */
            new UBInfo(0x2600, 0x26FF, "MiscellaneousSymbols"), // Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS
            /* 2700; 27BF; Dingbats */
            new UBInfo(0x2700, 0x27BF, "Dingbats"), // Character.UnicodeBlock.DINGBATS
            /* 2800; 28FF; Braille Patterns */
            new UBInfo(0x2800, 0x28FF, "BraillePatterns"), // Character.UnicodeBlock.BRAILLE_PATTERNS
            /* 2E80; 2EFF; CJK Radicals Supplement */
            new UBInfo(0x2E80, 0x2EFF, "CJKRadicalsSupplement"), // Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT
            /* 2F00; 2FDF; Kangxi Radicals */
            new UBInfo(0x2F00, 0x2FDF, "KangxiRadicals"), // Character.UnicodeBlock.KANGXI_RADICALS
            /* 2FF0; 2FFF; Ideographic Description Characters */
            new UBInfo(0x2FF0, 0x2FFF, "IdeographicDescriptionCharacters"), // Character.UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS
            /* 3000; 303F; CJK Symbols and Punctuation */
            new UBInfo(0x3000, 0x303F, "CJKSymbolsandPunctuation"), // Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            /* 3040; 309F; Hiragana */
            new UBInfo(0x3040, 0x309F, "Hiragana"), // Character.UnicodeBlock.HIRAGANA
            /* 30A0; 30FF; Katakana */
            new UBInfo(0x30A0, 0x30FF, "Katakana"), // Character.UnicodeBlock.KATAKANA
            /* 3100; 312F; Bopomofo */
            new UBInfo(0x3100, 0x312F, "Bopomofo"), // Character.UnicodeBlock.BOPOMOFO
            /* 3130; 318F; Hangul Compatibility Jamo */
            new UBInfo(0x3130, 0x318F, "HangulCompatibilityJamo"), // Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
            /* 3190; 319F; Kanbun */
            new UBInfo(0x3190, 0x319F, "Kanbun"), // Character.UnicodeBlock.KANBUN
            /* 31A0; 31BF; Bopomofo Extended */
            new UBInfo(0x31A0, 0x31BF, "BopomofoExtended"), // Character.UnicodeBlock.BOPOMOFO_EXTENDED
            /* 3200; 32FF; Enclosed CJK Letters and Months */
            new UBInfo(0x3200, 0x32FF, "EnclosedCJKLettersandMonths"), // Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS
            /* 3300; 33FF; CJK Compatibility */
            new UBInfo(0x3300, 0x33FF, "CJKCompatibility"), // Character.UnicodeBlock.CJK_COMPATIBILITY
            /* 3400; 4DB5; CJK Unified Ideographs Extension A */
            new UBInfo(0x3400, 0x4DBF, "CJKUnifiedIdeographsExtensionA"), // Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            /* 4E00; 9FFF; CJK Unified Ideographs */
            new UBInfo(0x4E00, 0x9FFF, "CJKUnifiedIdeographs"), // Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            /* A000; A48F; Yi Syllables */
            new UBInfo(0xA000, 0xA48F, "YiSyllables"), // Character.UnicodeBlock.YI_SYLLABLES
            /* A490; A4CF; Yi Radicals */
            new UBInfo(0xA490, 0xA4CF, "YiRadicals"), // Character.UnicodeBlock.YI_RADICALS
            /* AC00; D7A3; Hangul Syllables */
            new UBInfo(0xAC00, 0xD7AF, "HangulSyllables"), // Character.UnicodeBlock.HANGUL_SYLLABLES
            /* D800; DB7F; High Surrogates */
            /* DB80; DBFF; High Private Use Surrogates */
            /* DC00; DFFF; Low Surrogates */
            /* E000; F8FF; Private Use */
            /* F900; FAFF; CJK Compatibility Ideographs */
            new UBInfo(0xF900, 0xFAFF, "CJKCompatibilityIdeographs"), // Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            /* FB00; FB4F; Alphabetic Presentation Forms */
            new UBInfo(0xFB00, 0xFB4F, "AlphabeticPresentationForms"), // Character.UnicodeBlock.ALPHABETIC_PRESENTATION_FORMS
            /* FB50; FDFF; Arabic Presentation Forms-A */
            new UBInfo(0xFB50, 0xFDFF, "ArabicPresentationForms-A"), // Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A
            /* FE20; FE2F; Combining Half Marks */
            new UBInfo(0xFE20, 0xFE2F, "CombiningHalfMarks"), // Character.UnicodeBlock.COMBINING_HALF_MARKS
            /* FE30; FE4F; CJK Compatibility Forms */
            new UBInfo(0xFE30, 0xFE4F, "CJKCompatibilityForms"), // Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
            /* FE50; FE6F; Small Form Variants */
            new UBInfo(0xFE50, 0xFE6F, "SmallFormVariants"), // Character.UnicodeBlock.SMALL_FORM_VARIANTS
            /* FE70; FEFE; Arabic Presentation Forms-B */
            new UBInfo(0xFE70, 0xFEFF, "ArabicPresentationForms-B"), // Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B
            /* FF00; FFEF; Halfwidth and Fullwidth Forms */
            new UBInfo(0xFF00, 0xFFEF, "HalfwidthandFullwidthForms"), // Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
            /* FFF0; FFFD; Specials */
            new UBInfo(0xFFF0, 0xFFFF, "Specials") // Character.UnicodeBlock.SPECIALS
    };
}
