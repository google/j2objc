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
        m = p.matcher("aa\nbb;cc\u0009\rdd;ee\u000C\fff;gg\n\u0007hh");
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
    }

    public void testMisc() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

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
        p = Pattern.compile("a$", Pattern.MULTILINE);
        m = p.matcher("a\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertFalse(m.find());

        p = Pattern.compile("(a$)", Pattern.MULTILINE);
        m = p.matcher("a\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertEquals("a", m.group(1));
        assertFalse(m.find());

        p = Pattern.compile("^.*$", Pattern.MULTILINE);

        m = p.matcher("a\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertFalse(m.find());
    }

    public void testCompile4() throws PatternSyntaxException {
        String findString = "\\Qpublic\\E";
        StringBuffer text = new StringBuffer("    public class Class {\n"
                + "    public class Class {");

        Pattern pattern = Pattern.compile(findString, Pattern.MULTILINE);
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
}
