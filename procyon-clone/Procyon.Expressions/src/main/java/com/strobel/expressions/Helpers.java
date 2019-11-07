/*
 * Helpers.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.core.Comparer;
import com.strobel.core.MutableInteger;
import com.strobel.core.delegates.Func1;

import java.util.HashSet;
import java.util.Map;

/**
 * @author Mike Strobel
 */
final class Helpers {
    static <T> T commonNode(final T first, final T second, final Func1<T, T> parent) {
        if (Comparer.equals(first, second)) {
            return first;
        }
        final HashSet<T> set = new HashSet<>();

        for (T t = first; t != null; t = parent.apply(t)) {
            set.add(t);
        }

        for (T t = second; t != null; t = parent.apply(t)) {
            if (set.contains(t)) {
                return t;
            }
        }

        return null;
    }

    static <T> void incrementCount(final T key, final Map<T, MutableInteger> dict) {
        MutableInteger count = dict.get(key);

        if (count == null) {
            dict.put(key, count = new MutableInteger());
        }

        count.increment();
    }
}
