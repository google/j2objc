/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package jsr166;

import java.util.concurrent.atomic.AtomicMarkableReference;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AtomicMarkableReferenceTest extends JSR166TestCase {
    // android-note: Removed because the CTS runner does a bad job of
    // retrying tests that have suite() declarations.
    //
    // public static void main(String[] args) {
    //     main(suite(), args);
    // }
    // public static Test suite() {
    //     return new TestSuite(AtomicMarkableReferenceTest.class);
    // }

    /**
     * constructor initializes to given reference and mark
     */
    public void testConstructor() {
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertSame(one, ai.getReference());
        assertFalse(ai.isMarked());
        AtomicMarkableReference a2 = new AtomicMarkableReference(null, true);
        assertNull(a2.getReference());
        assertTrue(a2.isMarked());
    }

    /**
     * get returns the last values of reference and mark set
     */
    public void testGetSet() {
        boolean[] mark = new boolean[1];
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertSame(one, ai.getReference());
        assertFalse(ai.isMarked());
        assertSame(one, ai.get(mark));
        assertFalse(mark[0]);
        ai.set(two, false);
        assertSame(two, ai.getReference());
        assertFalse(ai.isMarked());
        assertSame(two, ai.get(mark));
        assertFalse(mark[0]);
        ai.set(one, true);
        assertSame(one, ai.getReference());
        assertTrue(ai.isMarked());
        assertSame(one, ai.get(mark));
        assertTrue(mark[0]);
    }

    /**
     * attemptMark succeeds in single thread
     */
    public void testAttemptMark() {
        boolean[] mark = new boolean[1];
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertFalse(ai.isMarked());
        assertTrue(ai.attemptMark(one, true));
        assertTrue(ai.isMarked());
        assertSame(one, ai.get(mark));
        assertTrue(mark[0]);
    }

    /**
     * compareAndSet succeeds in changing values if equal to expected reference
     * and mark else fails
     */
    public void testCompareAndSet() {
        boolean[] mark = new boolean[1];
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertSame(one, ai.get(mark));
        assertFalse(ai.isMarked());
        assertFalse(mark[0]);

        assertTrue(ai.compareAndSet(one, two, false, false));
        assertSame(two, ai.get(mark));
        assertFalse(mark[0]);

        assertTrue(ai.compareAndSet(two, m3, false, true));
        assertSame(m3, ai.get(mark));
        assertTrue(mark[0]);

        assertFalse(ai.compareAndSet(two, m3, true, true));
        assertSame(m3, ai.get(mark));
        assertTrue(mark[0]);
    }

    /**
     * compareAndSet in one thread enables another waiting for reference value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() throws Exception {
        final AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!ai.compareAndSet(two, three, false, false))
                    Thread.yield();
            }});

        t.start();
        assertTrue(ai.compareAndSet(one, two, false, false));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertSame(three, ai.getReference());
        assertFalse(ai.isMarked());
    }

    /**
     * compareAndSet in one thread enables another waiting for mark value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads2() throws Exception {
        final AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!ai.compareAndSet(one, one, true, false))
                    Thread.yield();
            }});

        t.start();
        assertTrue(ai.compareAndSet(one, one, false, true));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertSame(one, ai.getReference());
        assertFalse(ai.isMarked());
    }

    /**
     * repeated weakCompareAndSet succeeds in changing values when equal
     * to expected
     */
    public void testWeakCompareAndSet() {
        boolean[] mark = new boolean[1];
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertSame(one, ai.get(mark));
        assertFalse(ai.isMarked());
        assertFalse(mark[0]);

        do {} while (!ai.weakCompareAndSet(one, two, false, false));
        assertSame(two, ai.get(mark));
        assertFalse(mark[0]);

        do {} while (!ai.weakCompareAndSet(two, m3, false, true));
        assertSame(m3, ai.get(mark));
        assertTrue(mark[0]);
    }

}
