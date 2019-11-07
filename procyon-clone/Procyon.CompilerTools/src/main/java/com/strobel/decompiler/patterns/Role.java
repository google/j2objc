/*
 * Role.java
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

package com.strobel.decompiler.patterns;

import com.strobel.core.VerifyArgument;

import java.util.concurrent.atomic.AtomicInteger;

public class Role<T> {
    public final static int ROLE_INDEX_BITS = 9;

    final static Role[] ROLES = new Role[1 << ROLE_INDEX_BITS];
    final static AtomicInteger NEXT_ROLE_INDEX = new AtomicInteger();

    final int index;
    final String name;
    final Class<T> nodeType;
    final T nullObject;

    public Role(final String name, final Class<T> nodeType) {
        this(name, nodeType, null);
    }

    public Role(final String name, final Class<T> nodeType, final T nullObject) {
        VerifyArgument.notNull(nodeType, "nodeType");

        this.index = NEXT_ROLE_INDEX.getAndIncrement();

        if (this.index >= ROLES.length) {
            throw new IllegalStateException("Too many roles created!");
        }

        this.name = name;
        this.nodeType = nodeType;
        this.nullObject = nullObject;

        ROLES[this.index] = this;
    }

    public final T getNullObject() {
        return nullObject;
    }

    public final Class<T> getNodeType() {
        return nodeType;
    }

    public final int getIndex() {
        return index;
    }

    public boolean isValid(final Object node) {
        return nodeType.isInstance(node);
    }

    @SuppressWarnings("unchecked")
    public static <U> Role<U> get(final int index) {
        return ROLES[index];
    }

    @Override
    public String toString() {
        return name;
    }
}
