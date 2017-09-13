/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.lang.reflect;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InvocationTargetExceptionTest extends junit.framework.TestCase {

    static class TestMethod {
        public TestMethod() {
        }

        public void voidMethod() throws IllegalArgumentException {
        }

        public void parmTest(int x, short y, String s, boolean bool, Object o,
                long l, byte b, char c, double d, float f) {
        }

        public int intMethod() {
            return 1;
        }

        public static final void printTest(int x, short y, String s,
                boolean bool, Object o, long l, byte b, char c, double d,
                float f) {
        }

        public double doubleMethod() {
            return 1.0;
        }

        public short shortMethod() {
            return (short) 1;
        }

        public byte byteMethod() {
            return (byte) 1;
        }

        public float floatMethod() {
            return 1.0f;
        }

        public long longMethod() {
            return 1l;
        }

        public char charMethod() {
            return 'T';
        }

        public Object objectMethod() {
            return new Object();
        }

        private static void prstatic() {
        }

        public static void pustatic() {
        }

        public static synchronized void pustatsynch() {
        }

        public static int invokeStaticTest() {
            return 1;
        }

        public int invokeInstanceTest() {
            return 1;
        }

        private int privateInvokeTest() {
            return 1;
        }

        public int invokeExceptionTest() throws NullPointerException {
            throw new NullPointerException();
        }

        public static synchronized native void pustatsynchnat() /*-[
            // Empty method body to satisfy link error.
        ]-*/;

    }

    abstract class AbstractTestMethod {
        public abstract void puabs();
    }

    class SubInvocationTargetException extends InvocationTargetException {}

    /**
     * java.lang.reflect.InvocationTargetException#InvocationTargetException()
     */
    public void test_Constructor() throws Exception {
        Constructor<InvocationTargetException> ctor = InvocationTargetException.class
                .getDeclaredConstructor();
        assertNotNull("Parameterless constructor does not exist.", ctor);
        assertTrue("Constructor is not protected", Modifier.isProtected(ctor
                .getModifiers()));
        //create an instance of a subtype using this constructor
        SubInvocationTargetException subException = new SubInvocationTargetException();
    }

    /**
     * java.lang.reflect.InvocationTargetException#InvocationTargetException(java.lang.Throwable)
     */
    public void test_ConstructorLjava_lang_Throwable() {
        // Test for method
        // java.lang.reflect.InvocationTargetException(java.lang.Throwable)
        try {
            Method mth = TestMethod.class.getDeclaredMethod(
                    "invokeExceptionTest", new Class[0]);
            Object[] args = { Object.class };
            Object ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            // Correct behaviour
            return;
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        }
        fail("Failed to throw exception");
    }

    /**
     * java.lang.reflect.InvocationTargetException#InvocationTargetException(java.lang.Throwable,
     *        java.lang.String)
     */
    public void test_ConstructorLjava_lang_ThrowableLjava_lang_String() {
        // Test for method
        // java.lang.reflect.InvocationTargetException(java.lang.Throwable,
        // java.lang.String)
        try {
            Method mth = TestMethod.class.getDeclaredMethod(
                    "invokeExceptionTest", new Class[0]);
            Object[] args = { Object.class };
            Object ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            // Correct behaviour
            return;
        } catch (NoSuchMethodException e) {
            ;
        } catch (IllegalAccessException e) {
        }
        fail("Failed to throw exception");
    }

    /**
     * java.lang.reflect.InvocationTargetException#getTargetException()
     */
    public void test_getTargetException() {
        // Test for method java.lang.Throwable
        // java.lang.reflect.InvocationTargetException.getTargetException()
        try {
            Method mth = TestMethod.class.getDeclaredMethod(
                    "invokeExceptionTest", new Class[0]);
            Object[] args = { Object.class };
            Object ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            // Correct behaviour
            assertTrue("Returned incorrect target exception", e
                    .getTargetException() instanceof NullPointerException);
            return;
        } catch (Exception e) {
            fail("Exception during constructor test : " + e.getMessage());
        }
        fail("Failed to throw exception");
    }

    /**
     * java.lang.reflect.InvocationTargetException#getCause()
     */
    public void test_getCause() {
        // java.lang.reflect.InvocationTargetException.getCause()
        try {
            Method mth = TestMethod.class.getDeclaredMethod(
                    "invokeExceptionTest", new Class[0]);
            Object[] args = {Object.class};
            Object ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            // Correct behaviour
            assertTrue("Returned incorrect cause",
                    e.getCause() instanceof NullPointerException);
            return;
        } catch (Exception e) {
            fail("Exception during InvocationTargetException test : "
                    + e.getMessage());
        }
        fail("Failed to throw exception");
    }

    /**
     * java.lang.reflect.InvocationTargetException#printStackTrace()
     */
    public void test_printStackTrace() {
        // Test for method void
        // java.lang.reflect.InvocationTargetException.printStackTrace()
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bao);
            PrintStream oldErr = System.err;
            System.setErr(ps);
            InvocationTargetException ite = new InvocationTargetException(null);
            ite.printStackTrace();
            System.setErr(oldErr);

            String s = new String(bao.toByteArray());

            assertTrue("Incorrect Stack trace: " + s, s != null
                    && s.length() > 300);
        } catch (Exception e) {
            fail("printStackTrace() caused exception : " + e.getMessage());
        }
    }

    /**
     * java.lang.reflect.InvocationTargetException#printStackTrace(java.io.PrintStream)
     */
    public void test_printStackTraceLjava_io_PrintStream() {
        // Test for method void
        // java.lang.reflect.InvocationTargetException.printStackTrace(java.io.PrintStream)
        assertTrue("Tested via test_printStackTrace().", true);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bao);
        InvocationTargetException ite = new InvocationTargetException(
                new InvocationTargetException(null));
        ite.printStackTrace(ps);
        String s = bao.toString();
        assertTrue("printStackTrace failed." + s.length(), s != null
                && s.length() > 400);
    }

    /**
     * java.lang.reflect.InvocationTargetException#printStackTrace(java.io.PrintWriter)
     */
    public void test_printStackTraceLjava_io_PrintWriter() {
        // Test for method void
        // java.lang.reflect.InvocationTargetException.printStackTrace(java.io.PrintWriter)
        try {
            PrintWriter pw;
            InvocationTargetException ite;
            String s;
            CharArrayWriter caw = new CharArrayWriter();
            pw = new PrintWriter(caw);
            ite = new InvocationTargetException(new InvocationTargetException(
                    null));
            ite.printStackTrace(pw);

            s = caw.toString();
            assertTrue("printStackTrace failed." + s.length(), s != null
                    && s.length() > 400);
            pw.close();

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            pw = new PrintWriter(bao);
            ite = new InvocationTargetException(new InvocationTargetException(
                    null));
            ite.printStackTrace(pw);

            pw.flush(); // Test will fail if this line removed.
            s = bao.toString();
            assertTrue("printStackTrace failed." + s.length(), s != null
                    && s.length() > 400);

        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}
