/*
 * UnaryExpressionTests.java
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

import com.strobel.reflection.Type;
import com.strobel.reflection.Types;
import com.strobel.util.TypeUtils;
import org.junit.Test;

import static com.strobel.expressions.Expression.*;
import static org.junit.Assert.*;

public class UnaryExpressionTests extends AbstractExpressionTest {
    @Test
    public void testIsTrue() throws Throwable {
        assertResultEquals(isTrue(constant(false)), false);
        assertResultEquals(isTrue(constant(true)), true);

        final ConstantExpression boxedNull = constant(null, Types.Boolean);
        final ConstantExpression boxedTrue = constant(Boolean.TRUE, Types.Boolean);
        final ConstantExpression boxedFalse = constant(Boolean.FALSE, Types.Boolean);

        assertResultEquals(condition(isNull(boxedNull), defaultValue(Types.Boolean), isTrue(unbox(boxedNull))), null);
        assertResultEquals(condition(isNull(boxedTrue), defaultValue(Types.Boolean), isTrue(unbox(boxedTrue))), true);
        assertResultEquals(condition(isNull(boxedFalse), defaultValue(Types.Boolean), isTrue(unbox(boxedFalse))), false);
    }

    @Test
    public void testIsFalse() throws Throwable {
        assertResultEquals(isFalse(constant(false)), true);
        assertResultEquals(isFalse(constant(true)), false);

        final ConstantExpression boxedNull = constant(null, Types.Boolean);
        final ConstantExpression boxedTrue = constant(Boolean.TRUE, Types.Boolean);
        final ConstantExpression boxedFalse = constant(Boolean.FALSE, Types.Boolean);

        assertResultEquals(condition(isNull(boxedNull), defaultValue(Types.Boolean), isFalse(unbox(boxedNull))), null);
        assertResultEquals(condition(isNull(boxedTrue), defaultValue(Types.Boolean), isFalse(unbox(boxedTrue))), false);
        assertResultEquals(condition(isNull(boxedFalse), defaultValue(Types.Boolean), isFalse(unbox(boxedFalse))), true);
    }

    @Test
    public void testUnaryPlus() throws Throwable {
        assertResultEquals(unaryPlus(constant((byte) -5)), (byte) -5);
        assertResultEquals(unaryPlus(constant((short) -5)), (short) -5);
        assertResultEquals(unaryPlus(constant(-5)), -5);
        assertResultEquals(unaryPlus(constant(-5L)), -5L);
        assertResultEquals(unaryPlus(constant(-5f)), -5f);
        assertResultEquals(unaryPlus(constant(-5d)), -5d);

        try {
            lambda(unaryPlus(constant(new TestNumber(5)))).compileHandle().invoke();
            fail("Exception expected!");
        }
        catch (final IllegalStateException e) {
            //noinspection ThrowableResultOfMethodCallIgnored
            assertEquals(
                Error.unaryOperatorNotDefined(ExpressionType.UnaryPlus, TestNumber.TYPE).getMessage(),
                e.getMessage()
            );
        }
    }

    @Test
    public void testUnaryPlusWithMethod() throws Throwable {
        assertResultEquals(
            unaryPlus(constant(new TestNumber(-5)), TestNumber.TYPE.getMethod("abs")),
            new TestNumber(5)
        );
    }

    @Test
    public void testNegate() throws Throwable {
        assertResultEquals(negate(constant((byte) 5)), (byte) -5);
        assertResultEquals(negate(constant((short) 5)), (short) -5);
        assertResultEquals(negate(constant(5)), -5);
        assertResultEquals(negate(constant(5L)), -5L);
        assertResultEquals(negate(constant(5f)), -5f);
        assertResultEquals(negate(constant(5d)), -5d);
        assertResultEquals(negate(constant(new TestNumber(5))), new TestNumber(-5));

        assertResultEquals(negate(constant((byte) -5)), (byte) 5);
        assertResultEquals(negate(constant((short) -5)), (short) 5);
        assertResultEquals(negate(constant(-5)), 5);
        assertResultEquals(negate(constant(-5L)), 5L);
        assertResultEquals(negate(constant(-5f)), 5f);
        assertResultEquals(negate(constant(-5d)), 5d);
        assertResultEquals(negate(constant(new TestNumber(-5))), new TestNumber(5));
    }

    @Test
    public void testNegateWithMethod() throws Throwable {
        assertResultEquals(
            unaryPlus(constant(new TestNumber(5)), TestNumber.TYPE.getMethod("negate")),
            new TestNumber(-5)
        );
    }

    @Test
    public void testUnbox() throws Throwable {
        final Object[] expectedResults = new Object[] { (byte) -5, (short) -5, -5, -5L, -5f, -5d };

        for (final Object expectedResult : expectedResults) {
            for (final Object sourceValue : expectedResults) {
                final Type<?> sourceType = TypeUtils.getBoxedTypeOrSelf(Type.getType(sourceValue));

                testUnbox(sourceValue, Types.Object, expectedResult);
                testUnbox(sourceValue, Types.Number, expectedResult);
                testUnbox(sourceValue, sourceType, expectedResult);
                testUnbox(new TestNumber(-5), null, expectedResult);
            }
        }
    }

    private <T, R> void testUnbox(final T value, final Type<?> inputType, final R result) throws Throwable {
        final Type<?> sourceType = inputType != null ? TypeUtils.getBoxedTypeOrSelf(inputType) : Type.getType(value);
        final Type<?> targetType = TypeUtils.getUnderlyingPrimitive(Type.getType(result));

        final ParameterExpression p = parameter(sourceType, "p");
        final LambdaExpression<?> lambda = lambda(unbox(p, targetType), p);

        assertEquals(result, lambda.compileHandle().invoke(value));
    }

    // <editor-fold defaultstate="collapsed" desc="TestNumber Class">

    private static final class TestNumber {
        private final static Type<TestNumber> TYPE = Type.of(TestNumber.class);
        private final int _value;

        public TestNumber(final int value) {
            _value = value;
        }

        public int value() {
            return _value;
        }

        public byte byteValue() {
            return (byte)_value;
        }

        public short shortValue() {
            return (short)_value;
        }

        public int intValue() {
            return _value;
        }

        public long longValue() {
            return _value;
        }

        public float floatValue() {
            return _value;
        }

        public double doubleValue() {
            return _value;
        }

        public final TestNumber abs() {
            return new TestNumber(Math.abs(_value));
        }

        public final TestNumber negate() {
            return new TestNumber(-_value);
        }

        @Override
        public boolean equals(final Object o) {
            return this == o ||
                   (o instanceof TestNumber) && ((TestNumber) o)._value == _value;
        }

        @Override
        public int hashCode() {
            return _value;
        }
    }

    // </editor-fold>
}
