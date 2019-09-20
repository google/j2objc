/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.dev.test.rbbi;

import java.text.CharacterIterator;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.BreakIterator;

/**
 * @author sgill
 *
 */
public class AbstractBreakIteratorTests extends TestFmwk {

    private class AbstractBreakIterator extends BreakIterator {
        private int position = 0;
        private static final int LIMIT = 100;

        private int set(int n) {
            position = n;
            if (position > LIMIT) {
                position = LIMIT;
                return DONE;
            }
            if (position < 0) {
                position = 0;
                return DONE;
            }
            return position;
        }

        @Override
        public int first() {
            return set(0);
        }

        @Override
        public int last() {
            return set(LIMIT);
        }

        @Override
        public int next(int n) {
            return set(position + n);
        }

        @Override
        public int next() {
            return next(1);
        }

        @Override
        public int previous() {
            return next(-1);
        }

        @Override
        public int following(int offset) {
            return set(offset + 1);
        }

        @Override
        public int current() {
            return position;
        }

        @Override
        public CharacterIterator getText() {
            return null;
        }

        @Override
        public void setText(CharacterIterator newText) {
        }

    }

    private BreakIterator bi;

    @Before
    public void createBreakIterator() {
        bi = new AbstractBreakIterator();
    }

    @Test
    public void testPreceding() {
        int pos = bi.preceding(0);
        TestFmwk.assertEquals("BreakIterator preceding position not correct", BreakIterator.DONE, pos);

        pos = bi.preceding(5);
        TestFmwk.assertEquals("BreakIterator preceding position not correct", 4, pos);
    }

    @Test
    public void testIsBoundary() {
        boolean b = bi.isBoundary(0);
        TestFmwk.assertTrue("BreakIterator is boundary not correct", b);

        b = bi.isBoundary(5);
        TestFmwk.assertTrue("BreakIterator is boundary not correct", b);
    }

}
