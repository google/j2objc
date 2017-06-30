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

public class ThreadTest extends JSR166TestCase {
    // android-note: Removed because the CTS runner does a bad job of
    // retrying tests that have suite() declarations.
    //
    // public static void main(String[] args) {
    //     main(suite(), args);
    // }
    // public static Test suite() {
    //     return new TestSuite(ThreadTest.class);
    // }

    static class MyHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * getUncaughtExceptionHandler returns ThreadGroup unless set,
     * otherwise returning value of last setUncaughtExceptionHandler.
     */
    public void testGetAndSetUncaughtExceptionHandler() {
        // these must be done all at once to avoid state
        // dependencies across tests
        Thread current = Thread.currentThread();
        ThreadGroup tg = current.getThreadGroup();
        MyHandler eh = new MyHandler();
        assertSame(tg, current.getUncaughtExceptionHandler());
        current.setUncaughtExceptionHandler(eh);
        try {
            assertSame(eh, current.getUncaughtExceptionHandler());
        } finally {
            current.setUncaughtExceptionHandler(null);
        }
        assertSame(tg, current.getUncaughtExceptionHandler());
    }

    /**
     * getDefaultUncaughtExceptionHandler returns value of last
     * setDefaultUncaughtExceptionHandler.
     */
    public void testGetAndSetDefaultUncaughtExceptionHandler() {
        // android-note: Removed assertion; all "normal" android apps (including CTS tests) have a
        // default uncaught exception handler installed by the framework.
        //
        // assertEquals(null, Thread.getDefaultUncaughtExceptionHandler());
        // failure due to SecurityException is OK.
        // Would be nice to explicitly test both ways, but cannot yet.
        Thread.UncaughtExceptionHandler defaultHandler
            = Thread.getDefaultUncaughtExceptionHandler();
        MyHandler eh = new MyHandler();
        try {
            Thread.setDefaultUncaughtExceptionHandler(eh);
            try {
                assertSame(eh, Thread.getDefaultUncaughtExceptionHandler());
            } finally {
                Thread.setDefaultUncaughtExceptionHandler(defaultHandler);
            }
        } catch (SecurityException ok) {
            assertNotNull(System.getSecurityManager());
        }
        assertSame(defaultHandler, Thread.getDefaultUncaughtExceptionHandler());
    }

    // How to test actually using UEH within junit?

}
