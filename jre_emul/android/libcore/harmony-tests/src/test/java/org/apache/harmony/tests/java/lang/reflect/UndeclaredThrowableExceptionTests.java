package org.apache.harmony.tests.java.lang.reflect;

import junit.framework.TestCase;

import java.io.EOFException;
import java.lang.reflect.UndeclaredThrowableException;

public class UndeclaredThrowableExceptionTests extends TestCase {

    private static EOFException throwable = new EOFException();
    private static String msg = "TEST_MSG";
    /**
     * java.lang.reflect.UndeclaredThrowableException#getCause()
     */
    public void test_getCause() throws Exception {
        UndeclaredThrowableException ute = new UndeclaredThrowableException(
                throwable);
        assertSame("Wrong cause returned", throwable, ute.getCause());
    }

    /**
     * java.lang.reflect.UndeclaredThrowableException#getUndeclaredThrowable()
     */
    public void test_getUndeclaredThrowable() throws Exception {
        UndeclaredThrowableException ute = new UndeclaredThrowableException(
                throwable);
        assertSame("Wrong undeclared throwable returned", throwable, ute
                .getUndeclaredThrowable());
    }

    /**
     * java.lang.reflect.UndeclaredThrowableException#UndeclaredThrowableException(java.lang.Throwable)
     */
    public void test_Constructor_Throwable() throws Exception {
        UndeclaredThrowableException e = new UndeclaredThrowableException(
                throwable);
        assertEquals("Wrong cause returned", throwable, e.getCause());
        assertEquals("Wrong throwable returned", throwable, e
                .getUndeclaredThrowable());
    }

    /**
     * java.lang.reflect.UndeclaredThrowableException#UndeclaredThrowableException(java.lang.Throwable, java.lang.String)
     */
    public void test_Constructor_Throwable_String() throws Exception {
       UndeclaredThrowableException e = new UndeclaredThrowableException(
                throwable, msg);
        assertEquals("Wrong cause returned", throwable, e.getCause());
        assertEquals("Wrong throwable returned", throwable, e
                .getUndeclaredThrowable());
        assertEquals("Wrong message returned", msg, e.getMessage());
    }
}
