/*
 * UnaryTypeVisitor.java
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
public abstract class UnaryTypeVisitor<R> extends SimpleVisitor<Void, R> {
    public final R visit(final Type<?> t) {
        return t.accept(this, null);
    }
}
