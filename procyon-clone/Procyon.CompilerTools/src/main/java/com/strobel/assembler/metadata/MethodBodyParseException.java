/*
 * MethodBodyParseException.java
 *
 * Copyright (c) 2014 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.metadata;

public class MethodBodyParseException extends IllegalStateException {
    private final static String DEFAULT_MESSAGE = "An error occurred while parsing a method body.";

    public MethodBodyParseException() {
        this(DEFAULT_MESSAGE);
    }

    public MethodBodyParseException(final Throwable cause) {
        this(DEFAULT_MESSAGE, cause);
    }

    public MethodBodyParseException(final String message) {
        super(message);
    }

    public MethodBodyParseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
