/*
 * TypeVisitor.java
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
 * @author Mike Strobel
 */
@SuppressWarnings("ALL")
public class TypeVisitor<P, R> {
    public R visit(final Type<?> type) {
        return visit(type, null);
    }

    public final R visit(final Type<?> type, final P parameter) {
        return type.accept(this, parameter);
    }

    public R visitClassType(final Type<?> type, final P parameter) {
       return null;
    }

    public R visitPrimitiveType(final Type<?> type, final P parameter) {
        return null;
    }

    public R visitTypeParameter(final Type<?> type, final P parameter) {
        return null;
    }

    public R visitWildcardType(final Type<?> type, final P parameter) {
        return null;
    }

    public R visitArrayType(final Type<?> type, final P parameter) {
        return null;
    }

    public R visitType(final Type<?> type, final P parameter) {
        return null;
    }

    public R visitCapturedType(final Type<?> type, final P parameter) {
        return null;
    }
}

