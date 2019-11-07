/*
 * Selectors.java
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

package com.strobel.core;

import com.strobel.util.ContractUtils;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("unchecked")
public final class Selectors {
    private final static Selector<?, ?> IDENTITY_SELECTOR = new Selector<Object, Object>() {
        @Override
        public Object select(final Object source) {
            return source;
        }
    };

    private final static Selector<String, String> TO_UPPERCASE = new Selector<String, String>() {
        @Override
        public String select(final String source) {
            if (source == null) {
                return null;
            }
            return source.toUpperCase();
        }
    };

    private final static Selector<String, String> TO_LOWERCASE = new Selector<String, String>() {
        @Override
        public String select(final String source) {
            if (source == null) {
                return null;
            }
            return source.toUpperCase();
        }
    };

    private final static Selector<?, String> TO_STRING = new Selector<Object, String>() {
        @Override
        public String select(final Object source) {
            if (source == null) {
                return null;
            }
            return source.toString();
        }
    };

    private Selectors() {
        throw ContractUtils.unreachable();
    }

    public static <T> Selector<T, T> identity() {
        return (Selector<T, T>)IDENTITY_SELECTOR;
    }

    public static Selector<String, String> toUpperCase() {
        return TO_UPPERCASE;
    }

    public static Selector<String, String> toLowerCase() {
        return TO_LOWERCASE;
    }

    public static <T> Selector<T, String> asString() {
        return (Selector<T, String>)TO_STRING;
    }

    public static <T, R> Selector<T, R> cast(final Class<R> destinationType) {
        return new Selector<T, R>() {
            @Override
            public R select(final T source) {
                return destinationType.cast(source);
            }
        };
    }

    public static <T, U, R> Selector<T, R> combine(
        final Selector<? super T, ? extends U> first,
        final Selector<? super U, ? extends R> second) {

        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        return new Selector<T, R>() {
            @Override
            public R select(final T source) {
                return second.select(first.select(source));
            }
        };
    }
}
