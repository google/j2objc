/*
 * DefaultMap.java
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

package com.strobel.decompiler.ast;

import com.strobel.core.VerifyArgument;
import com.strobel.core.delegates.Func;
import com.strobel.functions.Supplier;

import java.util.IdentityHashMap;

/**
 * @author mstrobel
 */
public final class DefaultMap<K, V> extends IdentityHashMap<K, V> {
    private final Supplier<V> _defaultValueFactory;

    public DefaultMap(final Supplier<V> defaultValueFactory) {
        _defaultValueFactory = VerifyArgument.notNull(defaultValueFactory, "defaultValueFactory");
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final Object key) {
        V value = super.get(key);

        if (value == null) {
            put((K) key, value = _defaultValueFactory.get());
        }

        return value;
    }
}
