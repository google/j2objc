/*
 * TargetInvocationException.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection;

/**
 * @author strobelm
 */
public class TargetInvocationException extends RuntimeException {
    private final static String DefaultMessage = "Exception has been thrown by the target of an invocation.";

    public TargetInvocationException() {
        super(DefaultMessage);
    }

    public TargetInvocationException(final String message) {
        super(message);
    }

    public TargetInvocationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TargetInvocationException(final Throwable cause) {
        super(DefaultMessage, cause);
    }

    public TargetInvocationException(final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(DefaultMessage, cause, enableSuppression, writableStackTrace);
    }

    public TargetInvocationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
