package org.apache.harmony.security.tests.java.security;

import junit.framework.TestCase;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class PrivilegedActionTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    private class MyPrivilegedAction implements PrivilegedAction<String> {

        private boolean called=false;
        public String run() {
            called = true;
            return "ok";
        }
    }

    private class MyPrivilegedAction2 implements PrivilegedAction<String> {

        private boolean called=false;
        public String run() {
            called = true;
            throw new RuntimeException("fail");
        }

    }

    public void testRun() {
        MyPrivilegedAction action = new MyPrivilegedAction();
        String result = AccessController.doPrivileged(action);
        assertEquals("return value not correct", "ok", result);
        assertTrue("run method was not called", action.called);

        MyPrivilegedAction2 action2 = new MyPrivilegedAction2();


        try {
            result = AccessController.doPrivileged(action2);
            fail("exception expected");
        } catch (RuntimeException e) {
            // expected exception
        }
    }
}
