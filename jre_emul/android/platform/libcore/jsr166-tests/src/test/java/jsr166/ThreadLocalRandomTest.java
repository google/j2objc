/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package jsr166;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ThreadLocalRandomTest extends JSR166TestCase {

    // android-note: Removed because the CTS runner does a bad job of
    // retrying tests that have suite() declarations.
    //
    // public static void main(String[] args) {
    //     main(suite(), args);
    // }
    // public static Test suite() {
    //     return new TestSuite(ThreadLocalRandomTest.class);
    // }

    /*
     * Testing coverage notes:
     *
     * We don't test randomness properties, but only that repeated
     * calls, up to NCALLS tries, produce at least one different
     * result.  For bounded versions, we sample various intervals
     * across multiples of primes.
     */

    // max numbers of calls to detect getting stuck on one value
    static final int NCALLS = 10000;

    // max sampled int bound
    static final int MAX_INT_BOUND = (1 << 28);

    // max sampled long bound
    static final long MAX_LONG_BOUND = (1L << 42);

    // Number of replications for other checks
    static final int REPS = 20;

    /**
     * setSeed throws UnsupportedOperationException
     */
    public void testSetSeed() {
        try {
            ThreadLocalRandom.current().setSeed(17);
            shouldThrow();
        } catch (UnsupportedOperationException success) {}
    }

    /**
     * Repeated calls to nextInt produce at least two distinct results
     */
    public void testNextInt() {
        int f = ThreadLocalRandom.current().nextInt();
        int i = 0;
        while (i < NCALLS && ThreadLocalRandom.current().nextInt() == f)
            ++i;
        assertTrue(i < NCALLS);
    }

    /**
     * Repeated calls to nextLong produce at least two distinct results
     */
    public void testNextLong() {
        long f = ThreadLocalRandom.current().nextLong();
        int i = 0;
        while (i < NCALLS && ThreadLocalRandom.current().nextLong() == f)
            ++i;
        assertTrue(i < NCALLS);
    }

    /**
     * Repeated calls to nextBoolean produce at least two distinct results
     */
    public void testNextBoolean() {
        boolean f = ThreadLocalRandom.current().nextBoolean();
        int i = 0;
        while (i < NCALLS && ThreadLocalRandom.current().nextBoolean() == f)
            ++i;
        assertTrue(i < NCALLS);
    }

    /**
     * Repeated calls to nextFloat produce at least two distinct results
     */
    public void testNextFloat() {
        float f = ThreadLocalRandom.current().nextFloat();
        int i = 0;
        while (i < NCALLS && ThreadLocalRandom.current().nextFloat() == f)
            ++i;
        assertTrue(i < NCALLS);
    }

    /**
     * Repeated calls to nextDouble produce at least two distinct results
     */
    public void testNextDouble() {
        double f = ThreadLocalRandom.current().nextDouble();
        int i = 0;
        while (i < NCALLS && ThreadLocalRandom.current().nextDouble() == f)
            ++i;
        assertTrue(i < NCALLS);
    }

    /**
     * Repeated calls to nextGaussian produce at least two distinct results
     */
    public void testNextGaussian() {
        double f = ThreadLocalRandom.current().nextGaussian();
        int i = 0;
        while (i < NCALLS && ThreadLocalRandom.current().nextGaussian() == f)
            ++i;
        assertTrue(i < NCALLS);
    }

    /**
     * nextInt(non-positive) throws IllegalArgumentException
     */
    public void testNextIntBoundNonPositive() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int bound : new int[] { 0, -17, Integer.MIN_VALUE }) {
            try {
                rnd.nextInt(bound);
                shouldThrow();
            } catch (IllegalArgumentException success) {}
        }
    }

    /**
     * nextInt(least >= bound) throws IllegalArgumentException
     */
    public void testNextIntBadBounds() {
        int[][] badBoundss = {
            { 17, 2 },
            { -42, -42 },
            { Integer.MAX_VALUE, Integer.MIN_VALUE },
        };
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int[] badBounds : badBoundss) {
            try {
                rnd.nextInt(badBounds[0], badBounds[1]);
                shouldThrow();
            } catch (IllegalArgumentException success) {}
        }
    }

    /**
     * nextInt(bound) returns 0 <= value < bound;
     * repeated calls produce at least two distinct results
     */
    public void testNextIntBounded() {
        // sample bound space across prime number increments
        for (int bound = 2; bound < MAX_INT_BOUND; bound += 524959) {
            int f = ThreadLocalRandom.current().nextInt(bound);
            assertTrue(0 <= f && f < bound);
            int i = 0;
            int j;
            while (i < NCALLS &&
                   (j = ThreadLocalRandom.current().nextInt(bound)) == f) {
                assertTrue(0 <= j && j < bound);
                ++i;
            }
            assertTrue(i < NCALLS);
        }
    }

    /**
     * nextInt(least, bound) returns least <= value < bound;
     * repeated calls produce at least two distinct results
     */
    public void testNextIntBounded2() {
        for (int least = -15485863; least < MAX_INT_BOUND; least += 524959) {
            for (int bound = least + 2; bound > least && bound < MAX_INT_BOUND; bound += 49979687) {
                int f = ThreadLocalRandom.current().nextInt(least, bound);
                assertTrue(least <= f && f < bound);
                int i = 0;
                int j;
                while (i < NCALLS &&
                       (j = ThreadLocalRandom.current().nextInt(least, bound)) == f) {
                    assertTrue(least <= j && j < bound);
                    ++i;
                }
                assertTrue(i < NCALLS);
            }
        }
    }

    /**
     * nextLong(non-positive) throws IllegalArgumentException
     */
    public void testNextLongBoundNonPositive() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (long bound : new long[] { 0L, -17L, Long.MIN_VALUE }) {
            try {
                rnd.nextLong(bound);
                shouldThrow();
            } catch (IllegalArgumentException success) {}
        }
    }

    /**
     * nextLong(least >= bound) throws IllegalArgumentException
     */
    public void testNextLongBadBounds() {
        long[][] badBoundss = {
            { 17L, 2L },
            { -42L, -42L },
            { Long.MAX_VALUE, Long.MIN_VALUE },
        };
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (long[] badBounds : badBoundss) {
            try {
                rnd.nextLong(badBounds[0], badBounds[1]);
                shouldThrow();
            } catch (IllegalArgumentException success) {}
        }
    }

    /**
     * nextLong(bound) returns 0 <= value < bound;
     * repeated calls produce at least two distinct results
     */
    public void testNextLongBounded() {
        for (long bound = 2; bound < MAX_LONG_BOUND; bound += 15485863) {
            long f = ThreadLocalRandom.current().nextLong(bound);
            assertTrue(0 <= f && f < bound);
            int i = 0;
            long j;
            while (i < NCALLS &&
                   (j = ThreadLocalRandom.current().nextLong(bound)) == f) {
                assertTrue(0 <= j && j < bound);
                ++i;
            }
            assertTrue(i < NCALLS);
        }
    }

    /**
     * nextLong(least, bound) returns least <= value < bound;
     * repeated calls produce at least two distinct results
     */
    public void testNextLongBounded2() {
        for (long least = -86028121; least < MAX_LONG_BOUND; least += 982451653L) {
            for (long bound = least + 2; bound > least && bound < MAX_LONG_BOUND; bound += Math.abs(bound * 7919)) {
                long f = ThreadLocalRandom.current().nextLong(least, bound);
                assertTrue(least <= f && f < bound);
                int i = 0;
                long j;
                while (i < NCALLS &&
                       (j = ThreadLocalRandom.current().nextLong(least, bound)) == f) {
                    assertTrue(least <= j && j < bound);
                    ++i;
                }
                assertTrue(i < NCALLS);
            }
        }
    }

    /**
     * nextDouble(non-positive) throws IllegalArgumentException
     */
    public void testNextDoubleBoundNonPositive() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double[] badBounds = {
            0.0d,
            -17.0d,
            -Double.MIN_VALUE,
            Double.NEGATIVE_INFINITY,
            Double.NaN,
        };
        for (double bound : badBounds) {
            try {
                rnd.nextDouble(bound);
                shouldThrow();
            } catch (IllegalArgumentException success) {}
        }
    }

    /**
     * nextDouble(least, bound) returns least <= value < bound;
     * repeated calls produce at least two distinct results
     */
    public void testNextDoubleBounded2() {
        for (double least = 0.0001; least < 1.0e20; least *= 8) {
            for (double bound = least * 1.001; bound < 1.0e20; bound *= 16) {
                double f = ThreadLocalRandom.current().nextDouble(least, bound);
                assertTrue(least <= f && f < bound);
                int i = 0;
                double j;
                while (i < NCALLS &&
                       (j = ThreadLocalRandom.current().nextDouble(least, bound)) == f) {
                    assertTrue(least <= j && j < bound);
                    ++i;
                }
                assertTrue(i < NCALLS);
            }
        }
    }

    /**
     * Different threads produce different pseudo-random sequences
     */
    public void testDifferentSequences() {
        // Don't use main thread's ThreadLocalRandom - it is likely to
        // be polluted by previous tests.
        final AtomicReference<ThreadLocalRandom> threadLocalRandom =
            new AtomicReference<ThreadLocalRandom>();
        final AtomicLong rand = new AtomicLong();

        long firstRand = 0;
        ThreadLocalRandom firstThreadLocalRandom = null;

        Runnable getRandomState = new CheckedRunnable() {
            public void realRun() {
                ThreadLocalRandom current = ThreadLocalRandom.current();
                assertSame(current, ThreadLocalRandom.current());
                // test bug: the following is not guaranteed and not true in JDK8
                //                assertNotSame(current, threadLocalRandom.get());
                rand.set(current.nextLong());
                threadLocalRandom.set(current);
            }};

        Thread first = newStartedThread(getRandomState);
        awaitTermination(first);
        firstRand = rand.get();
        firstThreadLocalRandom = threadLocalRandom.get();

        for (int i = 0; i < NCALLS; i++) {
            Thread t = newStartedThread(getRandomState);
            awaitTermination(t);
            if (firstRand != rand.get())
                return;
        }
        fail("all threads generate the same pseudo-random sequence");
    }

}
