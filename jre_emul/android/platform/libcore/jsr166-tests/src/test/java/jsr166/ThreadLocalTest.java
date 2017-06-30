/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package jsr166;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ThreadLocalTest extends JSR166TestCase {
    // android-note: Removed because the CTS runner does a bad job of
    // retrying tests that have suite() declarations.
    //
    // public static void main(String[] args) {
    //     main(suite(), args);
    // }
    // public static Test suite() {
    //     return new TestSuite(ThreadLocalTest.class);
    // }

    static ThreadLocal<Integer> tl = new ThreadLocal<Integer>() {
            public Integer initialValue() {
                return one;
            }
        };

    static InheritableThreadLocal<Integer> itl =
        new InheritableThreadLocal<Integer>() {
            protected Integer initialValue() {
                return zero;
            }

            protected Integer childValue(Integer parentValue) {
                return new Integer(parentValue.intValue() + 1);
            }
        };

    /**
     * remove causes next access to return initial value
     */
    public void testRemove() {
        assertSame(tl.get(), one);
        tl.set(two);
        assertSame(tl.get(), two);
        tl.remove();
        assertSame(tl.get(), one);
    }

    /**
     * remove in InheritableThreadLocal causes next access to return
     * initial value
     */
    public void testRemoveITL() {
        assertSame(itl.get(), zero);
        itl.set(two);
        assertSame(itl.get(), two);
        itl.remove();
        assertSame(itl.get(), zero);
    }

    private class ITLThread extends Thread {
        final int[] x;
        ITLThread(int[] array) { x = array; }
        public void run() {
            Thread child = null;
            if (itl.get().intValue() < x.length - 1) {
                child = new ITLThread(x);
                child.start();
            }
            Thread.yield();

            int threadId = itl.get().intValue();
            for (int j = 0; j < threadId; j++) {
                x[threadId]++;
                Thread.yield();
            }

            if (child != null) { // Wait for child (if any)
                try {
                    child.join();
                } catch (InterruptedException e) {
                    threadUnexpectedException(e);
                }
            }
        }
    }

    /**
     * InheritableThreadLocal propagates generic values.
     */
    public void testGenericITL() throws InterruptedException {
        final int threadCount = 10;
        final int[] x = new int[threadCount];
        Thread progenitor = new ITLThread(x);
        progenitor.start();
        progenitor.join();
        for (int i = 0; i < threadCount; i++) {
            assertEquals(i, x[i]);
        }
    }
}
