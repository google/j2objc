/*
 * ConstantTests.java
 *
 * Copyright (c) 2015 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.reflection.PrimitiveTypes;
import com.strobel.reflection.Types;
import org.junit.Test;

import static com.strobel.expressions.Expression.constant;
import static org.junit.Assert.*;

public class ConstantTests extends AbstractExpressionTest {
    @Test
    public void testImplicitlyTypedPrimitiveConstant() {
        final ConstantExpression c = constant(42);

        assertEquals(42, c.getValue());
        assertEquals(PrimitiveTypes.Integer, c.getType());
    }

    @Test
    public void testExplicitlyTypedPrimitiveConstant() {
        final ConstantExpression c = constant(42, PrimitiveTypes.Integer);

        assertEquals(42, c.getValue());
        assertEquals(PrimitiveTypes.Integer, c.getType());
    }

    @Test
    public void testExplicitlyTypedBoxedConstant() {
        final ConstantExpression c = constant(42, Types.Integer);

        assertEquals(42, c.getValue());
        assertEquals(Types.Integer, c.getType());
    }
}
