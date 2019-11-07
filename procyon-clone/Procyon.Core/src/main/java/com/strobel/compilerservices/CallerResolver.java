/*
 * CallerResolver.java
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

package com.strobel.compilerservices;

/**
 * @author Mike Strobel
 */
public final class CallerResolver extends SecurityManager {
    private static final CallerResolver CALLER_RESOLVER = new CallerResolver();
    private static final int CALL_CONTEXT_OFFSET = 3; // may need to change if this class is redesigned

    protected Class[] getClassContext() {
        return super.getClassContext();
    }

    /**
     * Indexes into the current method call context with a given offset.
     */
    public static Class getCallerClass(final int callerOffset) {
        return CALLER_RESOLVER.getClassContext()[CALL_CONTEXT_OFFSET + callerOffset];
    }

    public static int getContextSize(final int callerOffset) {
        return CALLER_RESOLVER.getClassContext().length - callerOffset;
    }

    public static int getContextSize() {
        return getContextSize(CALL_CONTEXT_OFFSET);
    }
}
