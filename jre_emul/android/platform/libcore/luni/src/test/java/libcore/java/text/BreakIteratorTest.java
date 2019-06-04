/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.java.text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

public class BreakIteratorTest extends junit.framework.TestCase {
    BreakIterator iterator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        iterator = BreakIterator.getCharacterInstance(Locale.US);
    }

    public void testGetAvailableLocales() {
        Locale[] locales = BreakIterator.getAvailableLocales();
        assertTrue("Array available locales is null", locales != null);
        assertTrue("Array available locales is 0-length",
                (locales != null && locales.length != 0));
        boolean found = false;
        for (Locale l : locales) {
            if (l.equals(Locale.US)) {
                // expected
                found = true;
            }
        }
        assertTrue("At least locale " + Locale.US + " must be presented", found);
    }

    public void testGetWordInstanceLocale() {
        BreakIterator it1 = BreakIterator.getWordInstance(Locale.CANADA_FRENCH);
        assertTrue("Incorrect BreakIterator", it1 != BreakIterator.getWordInstance());
        BreakIterator it2 = BreakIterator.getWordInstance(new Locale("bad locale"));
        assertTrue("Incorrect BreakIterator", it2 != BreakIterator.getWordInstance());
    }

    // http://b/7307154 - we used to pin an unbounded number of char[]s, relying on finalization.
    public void testStress() throws Exception {
        char[] cs = { 'a' };
        for (int i = 0; i < 4096; ++i) {
            BreakIterator it = BreakIterator.getWordInstance(Locale.US);
            it.setText(new String(cs));
        }
    }

    public void testWordBoundaries() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024; ++i) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append("12345");
        }
        String s = sb.toString();

        BreakIterator it = BreakIterator.getWordInstance(Locale.US);
        it.setText(s);

        // Check we're not leaking global references. 2048 would bust the VM's hard-coded limit.
        for (int i = 0; i < 2048; ++i) {
            it.setText(s);
        }

        BreakIterator clone = (BreakIterator) it.clone();

        assertExpectedWordBoundaries(it, s);
        assertExpectedWordBoundaries(clone, s);
    }

    private void assertExpectedWordBoundaries(BreakIterator it, String s) {
        int expectedPos = 0;
        int pos = it.first();
        assertEquals(expectedPos, pos);
        while (pos != BreakIterator.DONE) {
            expectedPos += 5; // The five characters until the end of this word.
            pos = it.next();
            assertEquals(expectedPos, pos);

            expectedPos += 1; // The space before the start of the next word...
            if (expectedPos > s.length()) {
                expectedPos = BreakIterator.DONE; // ...unless we're done.
            }
            pos = it.next();
            assertEquals(expectedPos, pos);
        }
    }

    public void testIsBoundary() {
        BreakIterator it = BreakIterator.getCharacterInstance(Locale.US);
        it.setText("hello");

        try {
            it.isBoundary(-1);
            fail();
        } catch (IllegalArgumentException expected) {
            // Note that this exception is not listed in the Java API documentation
        }

        assertTrue(it.isBoundary(0));
        assertTrue(it.isBoundary(1));
        assertTrue(it.isBoundary(4));
        assertTrue(it.isBoundary(5));

        try {
            it.isBoundary(6);
            fail();
        } catch (IllegalArgumentException expected) {
            // Note that this exception is not listed in the Java API documentation
        }
    }

    public void testFollowing() {
        BreakIterator it = BreakIterator.getCharacterInstance(Locale.US);
        it.setText("hello");

        try {
            it.following(-1);
            fail();
        } catch (IllegalArgumentException expected) {
            // Expected exception
        }

        assertEquals(1, it.following(0));
        assertEquals(2, it.following(1));
        assertEquals(5, it.following(4));
        assertEquals(BreakIterator.DONE, it.following(5));

        try {
            it.following(6);
            fail();
        } catch (IllegalArgumentException expected) {
            // Expected exception
        }
    }

    public void testPreceding() {
        BreakIterator it = BreakIterator.getCharacterInstance(Locale.US);
        it.setText("hello");

        try {
            it.preceding(-1);
            fail();
        } catch (IllegalArgumentException expected) {
            // Expected exception
        }

        assertEquals(BreakIterator.DONE, it.preceding(0));
        assertEquals(0, it.preceding(1));
        assertEquals(4, it.preceding(5));

        try {
            it.preceding(6);
            fail();
        } catch (IllegalArgumentException expected) {
            // Expected exception
        }
    }
}
