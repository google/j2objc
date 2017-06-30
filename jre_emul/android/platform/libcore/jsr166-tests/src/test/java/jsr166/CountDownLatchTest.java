/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package jsr166;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.CountDownLatch;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CountDownLatchTest extends JSR166TestCase {
    // android-note: Removed because the CTS runner does a bad job of
    // retrying tests that have suite() declarations.
    //
    // public static void main(String[] args) {
    //     main(suite(), args);
    // }
    // public static Test suite() {
    //     return new TestSuite(CountDownLatchTest.class);
    // }

    /**
     * negative constructor argument throws IAE
     */
    public void testConstructor() {
        try {
            new CountDownLatch(-1);
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * getCount returns initial count and decreases after countDown
     */
    public void testGetCount() {
        final CountDownLatch l = new CountDownLatch(2);
        assertEquals(2, l.getCount());
        l.countDown();
        assertEquals(1, l.getCount());
    }

    /**
     * countDown decrements count when positive and has no effect when zero
     */
    public void testCountDown() {
        final CountDownLatch l = new CountDownLatch(1);
        assertEquals(1, l.getCount());
        l.countDown();
        assertEquals(0, l.getCount());
        l.countDown();
        assertEquals(0, l.getCount());
    }

    /**
     * await returns after countDown to zero, but not before
     */
    public void testAwait() {
        final CountDownLatch l = new CountDownLatch(2);
        final CountDownLatch pleaseCountDown = new CountDownLatch(1);

        Thread t = newStartedThread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                assertEquals(2, l.getCount());
                pleaseCountDown.countDown();
                l.await();
                assertEquals(0, l.getCount());
            }});

        await(pleaseCountDown);
        assertEquals(2, l.getCount());
        l.countDown();
        assertEquals(1, l.getCount());
        assertThreadStaysAlive(t);
        l.countDown();
        assertEquals(0, l.getCount());
        awaitTermination(t);
    }

    /**
     * timed await returns after countDown to zero
     */
    public void testTimedAwait() {
        final CountDownLatch l = new CountDownLatch(2);
        final CountDownLatch pleaseCountDown = new CountDownLatch(1);

        Thread t = newStartedThread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                assertEquals(2, l.getCount());
                pleaseCountDown.countDown();
                assertTrue(l.await(LONG_DELAY_MS, MILLISECONDS));
                assertEquals(0, l.getCount());
            }});

        await(pleaseCountDown);
        assertEquals(2, l.getCount());
        l.countDown();
        assertEquals(1, l.getCount());
        assertThreadStaysAlive(t);
        l.countDown();
        assertEquals(0, l.getCount());
        awaitTermination(t);
    }

    /**
     * await throws IE if interrupted before counted down
     */
    public void testAwait_Interruptible() {
        final CountDownLatch l = new CountDownLatch(1);
        final CountDownLatch pleaseInterrupt = new CountDownLatch(1);
        Thread t = newStartedThread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                Thread.currentThread().interrupt();
                try {
                    l.await();
                    shouldThrow();
                } catch (InterruptedException success) {}
                assertFalse(Thread.interrupted());

                pleaseInterrupt.countDown();
                try {
                    l.await();
                    shouldThrow();
                } catch (InterruptedException success) {}
                assertFalse(Thread.interrupted());

                assertEquals(1, l.getCount());
            }});

        await(pleaseInterrupt);
        assertThreadStaysAlive(t);
        t.interrupt();
        awaitTermination(t);
    }

    /**
     * timed await throws IE if interrupted before counted down
     */
    public void testTimedAwait_Interruptible() {
        final CountDownLatch l = new CountDownLatch(1);
        final CountDownLatch pleaseInterrupt = new CountDownLatch(1);
        Thread t = newStartedThread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                Thread.currentThread().interrupt();
                try {
                    l.await(LONG_DELAY_MS, MILLISECONDS);
                    shouldThrow();
                } catch (InterruptedException success) {}
                assertFalse(Thread.interrupted());

                pleaseInterrupt.countDown();
                try {
                    l.await(LONG_DELAY_MS, MILLISECONDS);
                    shouldThrow();
                } catch (InterruptedException success) {}
                assertFalse(Thread.interrupted());

                assertEquals(1, l.getCount());
            }});

        await(pleaseInterrupt);
        assertThreadStaysAlive(t);
        t.interrupt();
        awaitTermination(t);
    }

    /**
     * timed await times out if not counted down before timeout
     */
    public void testAwaitTimeout() throws InterruptedException {
        final CountDownLatch l = new CountDownLatch(1);
        Thread t = newStartedThread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                assertEquals(1, l.getCount());
                assertFalse(l.await(timeoutMillis(), MILLISECONDS));
                assertEquals(1, l.getCount());
            }});

        awaitTermination(t);
        assertEquals(1, l.getCount());
    }

    /**
     * toString indicates current count
     */
    public void testToString() {
        CountDownLatch s = new CountDownLatch(2);
        assertTrue(s.toString().contains("Count = 2"));
        s.countDown();
        assertTrue(s.toString().contains("Count = 1"));
        s.countDown();
        assertTrue(s.toString().contains("Count = 0"));
    }

}
