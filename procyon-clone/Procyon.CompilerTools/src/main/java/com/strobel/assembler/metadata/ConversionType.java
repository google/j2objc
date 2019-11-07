/*
 * ConversionType.java
 *
 * Copyright (c) 2013 Mike Strobel
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

public enum ConversionType {
    IDENTITY,
    IMPLICIT,
    IMPLICIT_LOSSY,
    EXPLICIT,
    EXPLICIT_TO_UNBOXED,
    NONE;

    public final boolean isImplicit() {
        switch (this) {
            case IDENTITY:
            case IMPLICIT:
            case IMPLICIT_LOSSY:
                return true;

            default:
                return false;
        }
    }

    public final boolean isDirect() {
        switch (this) {
            case IDENTITY:
            case IMPLICIT:
            case IMPLICIT_LOSSY:
            case EXPLICIT:
                return true;

            default:
                return false;
        }
    }

    public final boolean isLossless() {
        switch (this) {
            case IDENTITY:
            case IMPLICIT:
                return true;

            default:
                return false;
        }
    }
}
