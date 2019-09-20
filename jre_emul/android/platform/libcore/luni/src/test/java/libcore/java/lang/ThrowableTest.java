/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.lang;

import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class ThrowableTest extends TestCase {
    private static class NoStackTraceException extends Exception {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return null;
        }
    }
    public void testNullStackTrace() {
        try {
            throw new NoStackTraceException();
        } catch (NoStackTraceException ex) {
            // We used to throw NullPointerException when printing an exception with no stack trace.
            ex.printStackTrace(new PrintWriter(new StringWriter()));
        }
    }

    private static class SuppressionsThrowable extends Throwable {
        private static final long serialVersionUID = 202649043897209143L;

        public SuppressionsThrowable(String detailMessage, Throwable throwable,
                boolean enableSuppression, boolean writableStackTrace) {
            super(detailMessage, throwable, enableSuppression, writableStackTrace);
        }
    }

    public void testAddSuppressed() {
        Throwable throwable = new Throwable();
        assertSuppressed(throwable);
        Throwable suppressedA = new Throwable();
        throwable.addSuppressed(suppressedA);
        assertSuppressed(throwable, suppressedA);
        Throwable suppressedB = new Throwable();
        throwable.addSuppressed(suppressedB);
        assertSuppressed(throwable, suppressedA, suppressedB);
    }

    public void testAddDuplicateSuppressed() {
        Throwable throwable = new Throwable();
        Throwable suppressedA = new Throwable();
        throwable.addSuppressed(suppressedA);
        throwable.addSuppressed(suppressedA);
        throwable.addSuppressed(suppressedA);
        assertSuppressed(throwable, suppressedA, suppressedA, suppressedA);
    }

    public void testGetSuppressedReturnsCopy() {
        Throwable throwable = new Throwable();
        Throwable suppressedA = new Throwable();
        Throwable suppressedB = new Throwable();
        throwable.addSuppressed(suppressedA);
        throwable.addSuppressed(suppressedB);
        Throwable[] mutable = throwable.getSuppressed();
        mutable[0] = null;
        mutable[1] = null;
        assertSuppressed(throwable, suppressedA, suppressedB);
    }

    public void testAddSuppressedSelf() {
        Throwable throwable = new Throwable();
        try {
            throwable.addSuppressed(throwable);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testAddSuppressedNull() {
        Throwable throwable = new Throwable();
        try {
            throwable.addSuppressed(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testSetStackTraceWithNullElement() {
        Throwable throwable = new Throwable();
        try {
            throwable.setStackTrace(new StackTraceElement[]{ null });
            fail();
        } catch (NullPointerException expected) {
        }
    }

    private Throwable newThrowable(String message, String... stackTraceElements) {
        StackTraceElement[] array = new StackTraceElement[stackTraceElements.length];
        for (int i = 0; i < stackTraceElements.length; i++) {
            String s = stackTraceElements[i];
            array[stackTraceElements.length - 1 - i]
                    = new StackTraceElement("Class" + s, "do" + s, "Class" + s + ".java", i);
        }
        Throwable result = new Throwable(message);
        result.setStackTrace(array);
        return result;
    }

    private String printStackTraceToString(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private void assertSuppressed(Throwable throwable, Throwable... expectedSuppressed) {
        assertEquals(Arrays.asList(throwable.getSuppressed()), Arrays.asList(expectedSuppressed));
    }
}
