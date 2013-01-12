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

@SuppressWarnings("nls")
public class ReplaceTest extends TestCase {

    public void testSimpleReplace() throws PatternSyntaxException {
        String target, pattern, repl;

        target = "foobarfobarfoofo1";
        pattern = "fo[^o]";
        repl = "xxx";

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(target);

        assertEquals("foobarxxxarfoofo1", m.replaceFirst(repl));
        assertEquals("foobarxxxarfooxxx", m.replaceAll(repl));
    }

    public void testCaptureReplace() {
        String target, pattern, repl, s;
        Pattern p = null;
        Matcher m;

        target = "[31]foo;bar[42];[99]xyz";
        pattern = "\\[([0-9]+)\\]([a-z]+)";
        repl = "$2[$1]";

        p = Pattern.compile(pattern);
        m = p.matcher(target);
        s = m.replaceFirst(repl);
        assertEquals("foo[31];bar[42];[99]xyz", s);
        s = m.replaceAll(repl);
        assertEquals("foo[31];bar[42];xyz[99]", s);

        target = "[31]foo(42)bar{63}zoo;[12]abc(34)def{56}ghi;{99}xyz[88]xyz(77)xyz;";
        pattern = "\\[([0-9]+)\\]([a-z]+)\\(([0-9]+)\\)([a-z]+)\\{([0-9]+)\\}([a-z]+)";
        repl = "[$5]$6($3)$4{$1}$2";
        p = Pattern.compile(pattern);
        m = p.matcher(target);
        s = m.replaceFirst(repl);
        // System.out.println(s);
        assertEquals(
                "[63]zoo(42)bar{31}foo;[12]abc(34)def{56}ghi;{99}xyz[88]xyz(77)xyz;",
                s);
        s = m.replaceAll(repl);
        // System.out.println(s);
        assertEquals(
                "[63]zoo(42)bar{31}foo;[56]ghi(34)def{12}abc;{99}xyz[88]xyz(77)xyz;",
                s);
    }

    public void testEscapeReplace() {
        String target, pattern, repl, s;

        target = "foo'bar''foo";
        pattern = "'";
        repl = "\\'";
        s = target.replaceAll(pattern, repl);
        assertEquals("foo'bar''foo", s);
        repl = "\\\\'";
        s = target.replaceAll(pattern, repl);
        assertEquals("foo\\'bar\\'\\'foo", s);
        repl = "\\$3";
        s = target.replaceAll(pattern, repl);
        assertEquals("foo$3bar$3$3foo", s);
    }
}
