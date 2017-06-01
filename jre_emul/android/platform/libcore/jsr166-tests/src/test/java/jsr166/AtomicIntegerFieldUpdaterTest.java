/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package jsr166;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AtomicIntegerFieldUpdaterTest extends JSR166TestCase {
    volatile int x = 0;
    protected volatile int protectedField;
    private volatile int privateField;
    int w;
    float z;
    // android-note: Removed because the CTS runner does a bad job of
    // retrying tests that have suite() declarations.
    //
    // public static void main(String[] args) {
    //     main(suite(), args);
    // }
    // public static Test suite() {
    //     return new TestSuite(AtomicIntegerFieldUpdaterTest.class);
    // }

    // for testing subclass access
    // android-note: Removed because android doesn't restrict reflection access
    // static class AtomicIntegerFieldUpdaterTestSubclass extends AtomicIntegerFieldUpdaterTest {
    //     public void checkPrivateAccess() {
    //         try {
    //             AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a =
    //                 AtomicIntegerFieldUpdater.newUpdater
    //                 (AtomicIntegerFieldUpdaterTest.class, "privateField");
    //             shouldThrow();
    //         } catch (RuntimeException success) {
    //             assertNotNull(success.getCause());
    //         }
    //     }

    //     public void checkCompareAndSetProtectedSub() {
    //         AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a =
    //             AtomicIntegerFieldUpdater.newUpdater
    //             (AtomicIntegerFieldUpdaterTest.class, "protectedField");
    //         this.protectedField = 1;
    //         assertTrue(a.compareAndSet(this, 1, 2));
    //         assertTrue(a.compareAndSet(this, 2, -4));
    //         assertEquals(-4, a.get(this));
    //         assertFalse(a.compareAndSet(this, -5, 7));
    //         assertEquals(-4, a.get(this));
    //         assertTrue(a.compareAndSet(this, -4, 7));
    //         assertEquals(7, a.get(this));
    //     }
    // }

    // static class UnrelatedClass {
    //     public void checkPackageAccess(AtomicIntegerFieldUpdaterTest obj) {
    //         obj.x = 72;
    //         AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a =
    //             AtomicIntegerFieldUpdater.newUpdater
    //             (AtomicIntegerFieldUpdaterTest.class, "x");
    //         assertEquals(72, a.get(obj));
    //         assertTrue(a.compareAndSet(obj, 72, 73));
    //         assertEquals(73, a.get(obj));
    //     }

    //     public void checkPrivateAccess(AtomicIntegerFieldUpdaterTest obj) {
    //         try {
    //             AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a =
    //                 AtomicIntegerFieldUpdater.newUpdater
    //                 (AtomicIntegerFieldUpdaterTest.class, "privateField");
    //             throw new AssertionError("should throw");
    //         } catch (RuntimeException success) {
    //             assertNotNull(success.getCause());
    //         }
    //     }
    // }

    AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> updaterFor(String fieldName) {
        return AtomicIntegerFieldUpdater.newUpdater
            (AtomicIntegerFieldUpdaterTest.class, fieldName);
    }

    /**
     * Construction with non-existent field throws RuntimeException
     */
    public void testConstructor() {
        try {
            updaterFor("y");
            shouldThrow();
        } catch (RuntimeException success) {
            assertNotNull(success.getCause());
        }
    }

    /**
     * construction with field not of given type throws IllegalArgumentException
     */
    public void testConstructor2() {
        try {
            updaterFor("z");
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * construction with non-volatile field throws IllegalArgumentException
     */
    public void testConstructor3() {
        try {
            updaterFor("w");
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * construction using private field from subclass throws RuntimeException
     */
    // android-note: Removed because android doesn't restrict reflection access
    // public void testPrivateFieldInSubclass() {
    //     AtomicIntegerFieldUpdaterTestSubclass s =
    //         new AtomicIntegerFieldUpdaterTestSubclass();
    //     s.checkPrivateAccess();
    // }

    /**
     * construction from unrelated class; package access is allowed,
     * private access is not
     */
    // android-note: Removed because android doesn't restrict reflection access
    // public void testUnrelatedClassAccess() {
    //     new UnrelatedClass().checkPackageAccess(this);
    //     new UnrelatedClass().checkPrivateAccess(this);
    // }

    /**
     * get returns the last value set or assigned
     */
    public void testGetSet() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.get(this));
        a.set(this, 2);
        assertEquals(2, a.get(this));
        a.set(this, -3);
        assertEquals(-3, a.get(this));
    }

    /**
     * get returns the last value lazySet by same thread
     */
    public void testGetLazySet() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.get(this));
        a.lazySet(this, 2);
        assertEquals(2, a.get(this));
        a.lazySet(this, -3);
        assertEquals(-3, a.get(this));
    }

    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertTrue(a.compareAndSet(this, 1, 2));
        assertTrue(a.compareAndSet(this, 2, -4));
        assertEquals(-4, a.get(this));
        assertFalse(a.compareAndSet(this, -5, 7));
        assertEquals(-4, a.get(this));
        assertTrue(a.compareAndSet(this, -4, 7));
        assertEquals(7, a.get(this));
    }

    /**
     * compareAndSet succeeds in changing protected field value if
     * equal to expected else fails
     */
    public void testCompareAndSetProtected() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("protectedField");
        protectedField = 1;
        assertTrue(a.compareAndSet(this, 1, 2));
        assertTrue(a.compareAndSet(this, 2, -4));
        assertEquals(-4, a.get(this));
        assertFalse(a.compareAndSet(this, -5, 7));
        assertEquals(-4, a.get(this));
        assertTrue(a.compareAndSet(this, -4, 7));
        assertEquals(7, a.get(this));
    }

    /**
     * compareAndSet succeeds in changing protected field value if
     * equal to expected else fails
     */
    // android-note: Removed because android doesn't restrict reflection access
    // public void testCompareAndSetProtectedInSubclass() {
    //     AtomicIntegerFieldUpdaterTestSubclass s =
    //         new AtomicIntegerFieldUpdaterTestSubclass();
    //     s.checkCompareAndSetProtectedSub();
    // }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() throws Exception {
        x = 1;
        final AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");

        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!a.compareAndSet(AtomicIntegerFieldUpdaterTest.this, 2, 3))
                    Thread.yield();
            }});

        t.start();
        assertTrue(a.compareAndSet(this, 1, 2));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertEquals(3, a.get(this));
    }

    /**
     * repeated weakCompareAndSet succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSet() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        do {} while (!a.weakCompareAndSet(this, 1, 2));
        do {} while (!a.weakCompareAndSet(this, 2, -4));
        assertEquals(-4, a.get(this));
        do {} while (!a.weakCompareAndSet(this, -4, 7));
        assertEquals(7, a.get(this));
    }

    /**
     * getAndSet returns previous value and sets to given value
     */
    public void testGetAndSet() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.getAndSet(this, 0));
        assertEquals(0, a.getAndSet(this, -10));
        assertEquals(-10, a.getAndSet(this, 1));
    }

    /**
     * getAndAdd returns previous value and adds given value
     */
    public void testGetAndAdd() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.getAndAdd(this, 2));
        assertEquals(3, a.get(this));
        assertEquals(3, a.getAndAdd(this, -4));
        assertEquals(-1, a.get(this));
    }

    /**
     * getAndDecrement returns previous value and decrements
     */
    public void testGetAndDecrement() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.getAndDecrement(this));
        assertEquals(0, a.getAndDecrement(this));
        assertEquals(-1, a.getAndDecrement(this));
    }

    /**
     * getAndIncrement returns previous value and increments
     */
    public void testGetAndIncrement() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.getAndIncrement(this));
        assertEquals(2, a.get(this));
        a.set(this, -2);
        assertEquals(-2, a.getAndIncrement(this));
        assertEquals(-1, a.getAndIncrement(this));
        assertEquals(0, a.getAndIncrement(this));
        assertEquals(1, a.get(this));
    }

    /**
     * addAndGet adds given value to current, and returns current value
     */
    public void testAddAndGet() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(3, a.addAndGet(this, 2));
        assertEquals(3, a.get(this));
        assertEquals(-1, a.addAndGet(this, -4));
        assertEquals(-1, a.get(this));
    }

    /**
     * decrementAndGet decrements and returns current value
     */
    public void testDecrementAndGet() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(0, a.decrementAndGet(this));
        assertEquals(-1, a.decrementAndGet(this));
        assertEquals(-2, a.decrementAndGet(this));
        assertEquals(-2, a.get(this));
    }

    /**
     * incrementAndGet increments and returns current value
     */
    public void testIncrementAndGet() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(2, a.incrementAndGet(this));
        assertEquals(2, a.get(this));
        a.set(this, -2);
        assertEquals(-1, a.incrementAndGet(this));
        assertEquals(0, a.incrementAndGet(this));
        assertEquals(1, a.incrementAndGet(this));
        assertEquals(1, a.get(this));
    }

}
