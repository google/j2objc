/*
 * DefaultTypeVisitor.java
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

public abstract class DefaultTypeVisitor<P, R> extends TypeVisitor<P, R> {
    @Override
    public R visitClassType(final Type<?> type, final P parameter) {
        return visitType(type, parameter);
    }

    @Override
    public R visitPrimitiveType(final Type<?> type, final P parameter) {
        return visitType(type, parameter);
    }

    @Override
    public R visitTypeParameter(final Type<?> type, final P parameter) {
        return visitType(type, parameter);
    }

    @Override
    public R visitWildcardType(final Type<?> type, final P parameter) {
        return visitType(type, parameter);
    }

    @Override
    public R visitCapturedType(final Type<?> type, final P parameter) {
        return visitType(type, parameter);
    }

    @Override
    public R visitArrayType(final Type<?> type, final P parameter) {
        return visitType(type, parameter);
    }
}
