/*
 * Aggregate.java
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
public final class Aggregate {
    private Aggregate() {
        throw ContractUtils.unreachable();
    }

    public static <TSource, TAccumulate> TAccumulate aggregate(
        final Iterable<TSource> source,
        final Accumulator<TSource, TAccumulate> accumulator) {

        return aggregate(source, null, accumulator);
    }

    public static <TSource, TAccumulate> TAccumulate aggregate(
        final Iterable<TSource> source,
        final TAccumulate seed,
        final Accumulator<TSource, TAccumulate> accumulator) {

        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(accumulator, "accumulator");

        TAccumulate accumulate = seed;

        for (final TSource item : source) {
            accumulate = accumulator.accumulate(accumulate, item);
        }

        return accumulate;
    }

    public static <TSource, TAccumulate, TResult> TResult aggregate(
        final Iterable<TSource> source,
        final Accumulator<TSource, TAccumulate> accumulator,
        final Selector<TAccumulate, TResult> resultSelector) {

        return aggregate(source, null, accumulator, resultSelector);
    }

    public static <TSource, TAccumulate, TResult> TResult aggregate(
        final Iterable<TSource> source,
        final TAccumulate seed,
        final Accumulator<TSource, TAccumulate> accumulator,
        final Selector<TAccumulate, TResult> resultSelector) {

        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(accumulator, "accumulator");
        VerifyArgument.notNull(resultSelector, "resultSelector");

        TAccumulate accumulate = seed;

        for (final TSource item : source) {
            accumulate = accumulator.accumulate(accumulate, item);
        }

        return resultSelector.select(accumulate);
    }
}
