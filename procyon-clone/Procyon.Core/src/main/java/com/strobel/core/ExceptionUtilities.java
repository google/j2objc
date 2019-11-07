/*
 * ExceptionUtilities.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.core;

import com.strobel.reflection.TargetInvocationException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public final class ExceptionUtilities {
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static RuntimeException asRuntimeException(final Throwable t) {
        VerifyArgument.notNull(t, "t");

        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }

        return new UndeclaredThrowableException(t, "An unhandled checked exception occurred.");
    }

    public static Throwable unwrap(final Throwable t) {
        final Throwable cause = t.getCause();

        if (cause == null || cause == t) {
            return t;
        }

        if (t instanceof InvocationTargetException ||
            t instanceof TargetInvocationException ||
            t instanceof UndeclaredThrowableException) {

            return unwrap(cause);
        }

        return t;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static String getMessage(final Throwable t) {
        final String message = VerifyArgument.notNull(t, "t").getMessage();

        if (StringUtilities.isNullOrWhitespace(message)) {
            return t.getClass().getSimpleName() + " was thrown.";
        }

        return message;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static String getStackTraceString(final Throwable t) {
        VerifyArgument.notNull(t, "t");

        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
             final PrintWriter writer = new PrintWriter(stream)) {

            t.printStackTrace(writer);

            writer.flush();
            stream.flush();

            return StringUtilities.trimRight(stream.toString());
        }
        catch (final Throwable ignored) {
            return t.toString();
        }
    }

    /**
     * Rethrows the specified exception only if it is within a narrow subset of 'critical'
     * exceptions, e.g., {@link ThreadDeath} or {@link VirtualMachineError}.
     */
    public static void rethrowCritical(final Throwable t) {
        if (t instanceof ThreadDeath ||
            t instanceof VirtualMachineError) {

            throw ExceptionUtilities.<java.lang.Error>rethrow(t);
        }
    }

    /**
     * <p>
     * Sneakily rethrows any exception without the compiler complaining if the exception
     * is checked but unhandled.  The signature declares a return type of {@link RuntimeException},
     * for cases where the caller method must exit to satisfy control flow requirements
     * (e.g., final variable assignment), but this method never actually returns a value.
     * </p>
     *
     * <p>Trivial example:</p>
     * <pre>
     * void doSomething() {
     *     try {
     *         mightThrowCheckedException();
     *     }
     *     catch (final Throwable t) {
     *         throw ExceptionUtilities.rethrow(t);
     *     }
     * }</pre>
     *
     * <p>Example requiring a return value:</p>
     * <pre>
     * T returnSomething() {
     *     try {
     *         return mightThrowCheckedException();
     *     }
     *     catch (final Throwable t) {
     *         // The call below always throws, but the compiler doesn't know that and demands
     *         // we either return a value or throw.  The return value, while never used, allows
     *         // us to satisfying the compiler by exiting the current method exceptionally.
     *         throw ExceptionUtilities.rethrow(t);
     *     }
     * }</pre>
     *
     * <p>Example with constructor and final fields:</p>
     * <pre>
     * class U {
     *     final T mustBeAssigned;
     *
     *     U() {
     *         try {
     *             mustBeAssigned = mightThrowCheckedException();
     *         }
     *         catch (final Throwable t) {
     *             // The compiler requires us to definitively assign all final fields before
     *             // returning or throw.  Since the compiler doesn't know that the call below
     *             // always throws, we can throw the dummy result to satisfy the compiler.
     *             throw ExceptionUtilities.rethrow(t);
     *         }
     *     }
     * }</pre>
     *
     * @return This method will never return a value; it always throws.
     *
     * @throws T
     *     This method rethrows the original exception {@code t}, or a
     *     {@link NullPointerException} if {@code t} is {@code null}.
     */
    @SuppressWarnings({ "unchecked", "JavaDoc" })
    public static <T extends Throwable> RuntimeException rethrow(final Throwable t) throws T {
        throw (T)t; // rely on vacuous cast
    }

    /**
     * <p>
     * Equivalent to {@link #rethrow(Throwable) rethrow}, but with an open-ended
     * return type, allowing calls to this method to be used as the body of lambda
     * expressions that must return a specific type.
     * </p>
     *
     * <p>Example:</p>
     * <pre>
     * public static &lt;T, R&gt; Function&lt;T, R&gt; throwing(final Throwable t) {
     *     return _ -&gt; ExceptionUtilities.rethrowAs(t);
     * }</pre>
     *
     * @return This method will never return a value; it always throws.
     *
     * @throws T
     *     This method rethrows the original exception {@code t}, or a
     *     {@link NullPointerException} if {@code t} is {@code null}.
     */
    @SuppressWarnings({ "unchecked", "JavaDoc" })
    public static <T extends Throwable, R> R rethrowAs(final Throwable t) throws T {
        throw (T)t; // rely on vacuous cast
    }
}
