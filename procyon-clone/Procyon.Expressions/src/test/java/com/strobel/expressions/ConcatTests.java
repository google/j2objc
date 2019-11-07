/*
 * ConcatTests.java
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

package com.strobel.expressions;

import com.strobel.reflection.Types;
import org.junit.Test;

import static com.strobel.expressions.Expression.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Mike Strobel
 */
public class ConcatTests extends AbstractExpressionTest {
    @Test
    public void testConcat() throws Throwable {
        final LambdaExpression<Runnable> test = lambda(
            Types.Runnable,
            call(
                outExpression(),
                "println",
                concat(constant("a"), constant('b'), constant("c"))
            )
        );

        final Runnable runnable = test.compile();

        runnable.run();

        assertEquals("abc", dequeue());
    }

    @Test
    public void testNullConcat() throws Throwable {
        final LambdaExpression<Runnable> test = lambda(
            Types.Runnable,
            call(
                outExpression(),
                "println",
                concat(constant(1), defaultValue(Types.Object))
            )
        );

        final Runnable runnable = test.compile();

        runnable.run();

        assertEquals("1null", dequeue());
    }
}
