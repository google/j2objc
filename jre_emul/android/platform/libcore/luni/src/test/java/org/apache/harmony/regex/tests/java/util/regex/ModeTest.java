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

import junit.framework.TestCase;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Tests Pattern compilation modes and modes triggered in pattern strings
 *
 */
public class ModeTest extends TestCase {

    public void testCase() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("([a-z]+)[0-9]+");
        m = p.matcher("cAT123#dog345");
        assertTrue(m.find());
        assertEquals("dog", m.group(1));
        assertFalse(m.find());


        p = Pattern.compile("([a-z]+)[0-9]+", Pattern.CASE_INSENSITIVE);
        m = p.matcher("cAt123#doG345");
        assertTrue(m.find());
        assertEquals("cAt", m.group(1));
        assertTrue(m.find());
        assertEquals("doG", m.group(1));
        assertFalse(m.find());


        p = Pattern.compile("(?i)([a-z]+)[0-9]+");
        m = p.matcher("cAt123#doG345");
        assertTrue(m.find());
        assertEquals("cAt", m.group(1));
        assertTrue(m.find());
        assertEquals("doG", m.group(1));
        assertFalse(m.find());
    }
    public void testMultiline() throws PatternSyntaxException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("^foo");
        m = p.matcher("foobar");
        assertTrue(m.find());
        assertTrue(m.start() == 0 && m.end() == 3);
        assertFalse(m.find());

        m = p.matcher("barfoo");
        assertFalse(m.find());


        p = Pattern.compile("foo$");
        m = p.matcher("foobar");
        assertFalse(m.find());

        m = p.matcher("barfoo");
        assertTrue(m.find());
        assertTrue(m.start() == 3 && m.end() == 6);
        assertFalse(m.find());


        p = Pattern.compile("^foo([0-9]*)", Pattern.MULTILINE);
        m = p.matcher("foo1bar\nfoo2foo3\nbarfoo4");
        assertTrue(m.find());
        assertEquals("1", m.group(1));
        assertTrue(m.find());
        assertEquals("2", m.group(1));
        assertFalse(m.find());


        p = Pattern.compile("foo([0-9]*)$", Pattern.MULTILINE);
        m = p.matcher("foo1bar\nfoo2foo3\nbarfoo4");
        assertTrue(m.find());
        assertEquals("3", m.group(1));
        assertTrue(m.find());
        assertEquals("4", m.group(1));
        assertFalse(m.find());


        p = Pattern.compile("(?m)^foo([0-9]*)");
        m = p.matcher("foo1bar\nfoo2foo3\nbarfoo4");
        assertTrue(m.find());
        assertEquals("1", m.group(1));
        assertTrue(m.find());
        assertEquals("2", m.group(1));
        assertFalse(m.find());

        p = Pattern.compile("(?m)foo([0-9]*)$");
        m = p.matcher("foo1bar\nfoo2foo3\nbarfoo4");
        assertTrue(m.find());
        assertEquals("3", m.group(1));
        assertTrue(m.find());
        assertEquals("4", m.group(1));
        assertFalse(m.find());
    }
}
