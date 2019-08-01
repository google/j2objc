/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util;

import junit.framework.TestCase;

import java.util.StringJoiner;

public class StringJoinerTest extends TestCase {

    private static final String[] WEEKDAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final String EXPECTED = "[Mon,Tue,Wed,Thu,Fri,Sat,Sun]";

    public void testConstructorNull() {
        try {
            new StringJoiner(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            new StringJoiner(null, "[", "]");
            fail();
        } catch (NullPointerException expected) {}

        try {
            new StringJoiner(",", null, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void testAddString() {
        StringJoiner sj = new StringJoiner(",", "[", "]");

        for (int i = 0; i < WEEKDAYS.length; ++i) {
            sj.add(WEEKDAYS[i]);
            int expectedLength = 2 /* prefix and postfix */
                    + 3 * (i + 1) /* length of elements */
                    + i /* length of separators, one less than number of elements */;
            assertEquals(expectedLength, sj.length());
        }
        assertEquals(EXPECTED, sj.toString());
    }

    public void testAddStringBuilder() {
        StringJoiner sj = new StringJoiner(",", "[", "]");

        for (int i = 0; i < WEEKDAYS.length; ++i) {
            sj.add(new StringBuilder(WEEKDAYS[i]));
            int expectedLength = 2 /* prefix and postfix */
                    + 3 * (i + 1) /* length of elements */
                    + i /* length of separators, one less than number of elements */;
            assertEquals(expectedLength, sj.length());
        }
        assertEquals(EXPECTED, sj.toString());
    }

    public void testAddNone() {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        assertEquals("[]", sj.toString());
        assertEquals(2, sj.length()); // len("[]")
    }

    public void testAddNull() {
        StringJoiner sj = new StringJoiner("|");
        sj.add(null).add(null);
        assertEquals("null|null", sj.toString());
    }

    public void testMerge() {
        StringJoiner sj1 = new StringJoiner(" ", "[", "]").add("Hello").add("world");
        StringJoiner sj2 = new StringJoiner("", "{", "}").add("Foo").add("Bar");
        StringJoiner sj3 = new StringJoiner("!", "<", ">").add("a").add("b");
        assertEquals("[Hello world FooBar a!b]", sj1.merge(sj2).merge(sj3).toString());
    }

    public void testMergeEmpty() {
        StringJoiner sj1 = new StringJoiner(",").add("");
        StringJoiner sj2 = new StringJoiner(".");
        assertEquals("", sj1.merge(sj2).toString());
    }

    public void testMergeSelf() {
        StringJoiner sj = new StringJoiner(" ", "|", "|").add("Hello").add("world");
        assertEquals("|Hello world Hello world|", sj.merge(sj).toString());
    }

    public void testMergeNull() {
        try {
            new StringJoiner(" ").merge(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void testSetEmptyValue() {
        StringJoiner sj = new StringJoiner(",", "[", "]").setEmptyValue("EMPTY");
        assertEquals("EMPTY", sj.toString());
    }

    public void testSetEmptyValuePopulated() {
        StringJoiner sj = new StringJoiner(",", "[", "]").add("FOOBAR").setEmptyValue("");
        assertEquals("[FOOBAR]", sj.toString());
    }

    public void testSetEmptyValuePopulated2() {
        StringJoiner sj = new StringJoiner(",").add("").setEmptyValue("FOOBAR");
        assertEquals("", sj.toString());
    }

    public void testSetEmptyValueEmpty() {
        StringJoiner sj = new StringJoiner(",", "[", "]").setEmptyValue("");
        assertEquals("", sj.toString());
    }

    public void testSetEmptyValueNull() {
        try {
            StringJoiner sj = new StringJoiner(",", "[", "]").setEmptyValue(null);
            fail();
        } catch (NullPointerException expected) {}
    }
}
