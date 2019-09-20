/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tests.java.text;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

import junit.framework.TestCase;

public class BreakIteratorTest extends TestCase {

    private static final String TEXT = "a\u0308abc def, gh-12i?jkl.mno?";

    BreakIterator iterator;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        iterator = BreakIterator.getCharacterInstance(Locale.US);
    }

    public void testConsts() {
        assertEquals(-1, BreakIterator.DONE);
    }

    public void testCache() {
        BreakIterator newOne = BreakIterator.getCharacterInstance(Locale.US);
        assertNotSame(newOne, iterator);
        assertEquals(newOne, iterator);

        newOne = BreakIterator.getCharacterInstance();
        assertEquals(newOne, iterator);

        newOne = BreakIterator.getCharacterInstance(Locale.CHINA);
        assertEquals(newOne, iterator);

        BreakIterator wordIterator = BreakIterator.getWordInstance();
        assertFalse(wordIterator.equals(iterator));

        BreakIterator lineIterator = BreakIterator.getLineInstance();
        assertFalse(lineIterator.equals(iterator));

        BreakIterator senteIterator = BreakIterator.getSentenceInstance();
        assertFalse(senteIterator.equals(iterator));
    }

    public void testClone() {
        BreakIterator cloned = (BreakIterator) iterator.clone();
        assertNotSame(cloned, iterator);
        assertEquals(cloned, iterator);
    }

    public void testCurrent() {
        assertEquals(0, iterator.current());
        iterator.setText(TEXT);
        assertEquals(iterator.first(), iterator.current());
    }

    public void testFirst() {
        assertEquals(0, iterator.first());
        iterator.setText(TEXT);
        assertEquals(0, iterator.first());
    }

    public void testFollowing() {
        try {
            iterator.following(1);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        iterator.setText(TEXT);
        assertEquals(2, iterator.following(1));
        try {
            assertEquals(0, iterator.following(-1));
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(BreakIterator.DONE, iterator.following(TEXT.length()));
    }

    public void testIsBoundary() {
        try {
            iterator.isBoundary(2);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        iterator.setText(TEXT);
        assertTrue(iterator.isBoundary(2));
        assertFalse(iterator.isBoundary(1));
        assertTrue(iterator.isBoundary(0));
        try {
            iterator.isBoundary(-1);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        assertTrue(iterator.isBoundary(TEXT.length()));
    }

    public void testLast() {
        assertEquals(0, iterator.last());
        iterator.setText(TEXT);
        assertEquals(TEXT.length(), iterator.last());
    }

    /*
     * Class under test for int next(int)
     */
    public void testNextint() {
        assertEquals(BreakIterator.DONE, iterator.next(3));
        iterator.setText(TEXT);
        assertEquals(4, iterator.next(3));
        assertEquals(24, iterator.next(20));
        assertEquals(23, iterator.next(-1));
        assertEquals(-1, iterator.next(TEXT.length()));
    }

    public void testPreceding() {
        try {
            iterator.preceding(2);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        iterator.setText(TEXT);
        assertEquals(0, iterator.preceding(2));
        assertEquals(2, iterator.preceding(3));
        assertEquals(16, iterator.preceding(17));
        assertEquals(17, iterator.preceding(18));
        assertEquals(18, iterator.preceding(19));
        try {
            iterator.preceding(-1);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(TEXT.length() - 1, iterator.preceding(TEXT.length()));
        assertEquals(BreakIterator.DONE, iterator.preceding(0));
    }

    public void testPrevious() {
        assertEquals(-1, iterator.previous());
        iterator.setText(TEXT);
        assertEquals(-1, iterator.previous());
        iterator.last();
        assertEquals(TEXT.length() - 1, iterator.previous());
    }

    public void testGetAvailableLocales() {
        Locale[] locales = BreakIterator.getAvailableLocales();
        assertTrue(locales.length > 0);
    }

    /*
     * Class under test for BreakIterator getCharacterInstance()
     */
    public void testGetCharacterInstance() {
        BreakIterator.getCharacterInstance();
    }

    /*
     * Class under test for BreakIterator getCharacterInstance(Locale)
     */
    public void testGetCharacterInstanceLocale() {
        BreakIterator it = BreakIterator.getCharacterInstance(Locale.US);
        BreakIterator it2 = BreakIterator.getCharacterInstance(Locale.CHINA);
        assertEquals(it, it2);
    }

    /*
     * Class under test for BreakIterator getLineInstance()
     */
    public void testGetLineInstance() {
        BreakIterator it = BreakIterator.getLineInstance();
        assertNotNull(it);
    }

    /*
     * Class under test for BreakIterator getLineInstance(Locale)
     */
    public void testGetLineInstanceLocale() {
        BreakIterator it = BreakIterator.getLineInstance(Locale.US);
        assertNotNull(it);
        BreakIterator.getLineInstance(new Locale("bad locale"));
    }

    /*
     * Class under test for BreakIterator getSentenceInstance()
     */
    public void testGetSentenceInstance() {
        BreakIterator it = BreakIterator.getSentenceInstance();
        assertNotNull(it);
    }

    /*
     * Class under test for BreakIterator getSentenceInstance(Locale)
     */
    public void testGetSentenceInstanceLocale() {
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.US);
        assertNotNull(it);
    }

    public void testGetText() {
        assertEquals(new StringCharacterIterator(""), iterator.getText());
        iterator.setText(TEXT);
        assertEquals(new StringCharacterIterator(TEXT), iterator.getText());
    }

    /*
     * Class under test for BreakIterator getWordInstance()
     */
    public void testGetWordInstance() {
        BreakIterator it = BreakIterator.getWordInstance();
        assertNotNull(it);
    }

    /*
     * Class under test for BreakIterator getWordInstance(Locale)
     */
    public void testGetWordInstanceLocale() {
        BreakIterator it = BreakIterator.getWordInstance(Locale.US);
        assertNotNull(it);
    }

    /*
     * Class under test for void setText(CharacterIterator)
     */
    public void testSetTextCharacterIterator() {
        try {
            iterator.setText((CharacterIterator) null);
            fail();
        } catch (NullPointerException e) {
        }
        CharacterIterator it = new StringCharacterIterator("abc");
        iterator.setText(it);
        assertSame(it, iterator.getText());
    }

    /*
     * Class under test for void setText(String)
     */
    public void testSetTextString() {
        try {
            iterator.setText((String) null);
            fail();
        } catch (NullPointerException e) {
        }
        iterator.setText("abc");
        CharacterIterator it = new StringCharacterIterator("abc");
        assertEquals(it, iterator.getText());
    }
    
	public void test_next() {
		// Regression test for HARMONY-30
		BreakIterator bi = BreakIterator.getWordInstance(Locale.US);
		bi.setText("This is the test, WordInstance");
		int n = bi.first();
		n = bi.next();
		assertEquals("Assert 0: next() returns incorrect value ", 4, n);
        
        assertEquals(BreakIterator.DONE, iterator.next());
        iterator.setText(TEXT);
        assertEquals(2, iterator.next());
	}

	/**
	 * @tests java.text.BreakIterator#getCharacterInstance(Locale)
	 */
    public void testGetCharacterInstanceLocale_NPE() {
        // Regression for HARMONY-265
        try {
            BreakIterator.getCharacterInstance(null);
            fail("BreakIterator.getCharacterInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testGetLineInstanceLocale_NPE() {
        try {
            BreakIterator.getLineInstance(null);
            fail("BreakIterator.getLineInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testGetSentenceInstanceLocale_NPE() {
        try {
            BreakIterator.getSentenceInstance(null);
            fail("BreakIterator.getSentenceInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testGetWordInstanceLocale_NPE() {
        try {
            BreakIterator.getWordInstance(null);
            fail("BreakIterator.getWordInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
}
