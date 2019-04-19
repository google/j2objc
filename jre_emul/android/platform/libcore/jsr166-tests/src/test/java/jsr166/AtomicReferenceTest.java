/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package jsr166;

import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AtomicReferenceTest extends JSR166TestCase {
    // android-note: Removed because the CTS runner does a bad job of
    // retrying tests that have suite() declarations.
    //
    // public static void main(String[] args) {
    //     main(suite(), args);
    // }
    // public static Test suite() {
    //     return new TestSuite(AtomicReferenceTest.class);
    // }

    /**
     * constructor initializes to given value
     */
    public void testConstructor() {
        AtomicReference ai = new AtomicReference(one);
        assertSame(one, ai.get());
    }

    /**
     * default constructed initializes to null
     */
    public void testConstructor2() {
        AtomicReference ai = new AtomicReference();
        assertNull(ai.get());
    }

    /**
     * get returns the last value set
     */
    public void testGetSet() {
        AtomicReference ai = new AtomicReference(one);
        assertSame(one, ai.get());
        ai.set(two);
        assertSame(two, ai.get());
        ai.set(m3);
        assertSame(m3, ai.get());
    }

    /**
     * get returns the last value lazySet in same thread
     */
    public void testGetLazySet() {
        AtomicReference ai = new AtomicReference(one);
        assertSame(one, ai.get());
        ai.lazySet(two);
        assertSame(two, ai.get());
        ai.lazySet(m3);
        assertSame(m3, ai.get());
    }

    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet() {
        AtomicReference ai = new AtomicReference(one);
        assertTrue(ai.compareAndSet(one, two));
        assertTrue(ai.compareAndSet(two, m4));
        assertSame(m4, ai.get());
        assertFalse(ai.compareAndSet(m5, seven));
        assertSame(m4, ai.get());
        assertTrue(ai.compareAndSet(m4, seven));
        assertSame(seven, ai.get());
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() throws Exception {
        final AtomicReference ai = new AtomicReference(one);
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!ai.compareAndSet(two, three))
                    Thread.yield();
            }});

        t.start();
        assertTrue(ai.compareAndSet(one, two));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertSame(three, ai.get());
    }

    /**
     * repeated weakCompareAndSet succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSet() {
        AtomicReference ai = new AtomicReference(one);
        do {} while (!ai.weakCompareAndSet(one, two));
        do {} while (!ai.weakCompareAndSet(two, m4));
        assertSame(m4, ai.get());
        do {} while (!ai.weakCompareAndSet(m4, seven));
        assertSame(seven, ai.get());
    }

    /**
     * getAndSet returns previous value and sets to given value
     */
    public void testGetAndSet() {
        AtomicReference ai = new AtomicReference(one);
        assertSame(one, ai.getAndSet(zero));
        assertSame(zero, ai.getAndSet(m10));
        assertSame(m10, ai.getAndSet(one));
    }

    /**
     * a deserialized serialized atomic holds same value
     */
    public void testSerialization() throws Exception {
        AtomicReference x = new AtomicReference();
        AtomicReference y = serialClone(x);
        assertNotSame(x, y);
        x.set(one);
        AtomicReference z = serialClone(x);
        assertNotSame(y, z);
        assertEquals(one, x.get());
        assertEquals(null, y.get());
        assertEquals(one, z.get());
    }

    /**
     * toString returns current value.
     */
    public void testToString() {
        AtomicReference<Integer> ai = new AtomicReference<Integer>(one);
        assertEquals(one.toString(), ai.toString());
        ai.set(two);
        assertEquals(two.toString(), ai.toString());
    }

}
