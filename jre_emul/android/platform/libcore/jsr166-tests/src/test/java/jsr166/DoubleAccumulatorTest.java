/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package jsr166;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.DoubleAccumulator;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DoubleAccumulatorTest extends JSR166TestCase {
    // android-note: Removed because the CTS runner does a bad job of
    // retrying tests that have suite() declarations.
    //
    // public static void main(String[] args) {
    //     main(suite(), args);
    // }
    // public static Test suite() {
    //     return new TestSuite(DoubleAccumulatorTest.class);
    // }

    /**
     * default constructed initializes to zero
     */
    public void testConstructor() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        assertEquals(0.0, ai.get());
    }

    /**
     * accumulate accumulates given value to current, and get returns current value
     */
    public void testAccumulateAndGet() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        ai.accumulate(2.0);
        assertEquals(2.0, ai.get());
        ai.accumulate(-4.0);
        assertEquals(2.0, ai.get());
        ai.accumulate(4.0);
        assertEquals(4.0, ai.get());
    }

    /**
     * reset() causes subsequent get() to return zero
     */
    public void testReset() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        ai.accumulate(2.0);
        assertEquals(2.0, ai.get());
        ai.reset();
        assertEquals(0.0, ai.get());
    }

    /**
     * getThenReset() returns current value; subsequent get() returns zero
     */
    public void testGetThenReset() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        ai.accumulate(2.0);
        assertEquals(2.0, ai.get());
        assertEquals(2.0, ai.getThenReset());
        assertEquals(0.0, ai.get());
    }

    /**
     * toString returns current value.
     */
    public void testToString() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        assertEquals("0.0", ai.toString());
        ai.accumulate(1.0);
        assertEquals(Double.toString(1.0), ai.toString());
    }

    /**
     * intValue returns current value.
     */
    public void testIntValue() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        assertEquals(0, ai.intValue());
        ai.accumulate(1.0);
        assertEquals(1, ai.intValue());
    }

    /**
     * longValue returns current value.
     */
    public void testLongValue() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        assertEquals(0, ai.longValue());
        ai.accumulate(1.0);
        assertEquals(1, ai.longValue());
    }

    /**
     * floatValue returns current value.
     */
    public void testFloatValue() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        assertEquals(0.0f, ai.floatValue());
        ai.accumulate(1.0);
        assertEquals(1.0f, ai.floatValue());
    }

    /**
     * doubleValue returns current value.
     */
    public void testDoubleValue() {
        DoubleAccumulator ai = new DoubleAccumulator(Double::max, 0.0);
        assertEquals(0.0, ai.doubleValue());
        ai.accumulate(1.0);
        assertEquals(1.0, ai.doubleValue());
    }

    /**
     * accumulates by multiple threads produce correct result
     */
    public void testAccumulateAndGetMT() {
        final int incs = 1000000;
        final int nthreads = 4;
        final ExecutorService pool = Executors.newCachedThreadPool();
        DoubleAccumulator a = new DoubleAccumulator(Double::max, 0.0);
        Phaser phaser = new Phaser(nthreads + 1);
        for (int i = 0; i < nthreads; ++i)
            pool.execute(new AccTask(a, phaser, incs));
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        double expected = incs - 1;
        double result = a.get();
        assertEquals(expected, result);
        pool.shutdown();
    }

    static final class AccTask implements Runnable {
        final DoubleAccumulator acc;
        final Phaser phaser;
        final int incs;
        volatile double result;
        AccTask(DoubleAccumulator acc, Phaser phaser, int incs) {
            this.acc = acc;
            this.phaser = phaser;
            this.incs = incs;
        }

        public void run() {
            phaser.arriveAndAwaitAdvance();
            DoubleAccumulator a = acc;
            for (int i = 0; i < incs; ++i)
                a.accumulate(i);
            result = a.get();
            phaser.arrive();
        }
    }

}
