/*
 * LogicalBranchTests.java
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

import com.strobel.core.delegates.Func1;
import com.strobel.reflection.Type;
import com.strobel.reflection.Types;
import com.strobel.util.TypeUtils;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.strobel.expressions.Expression.*;
import static org.junit.Assert.*;

/**
 * @author Mike Strobel
 */
public class LogicalBranchTests extends AbstractExpressionTest {
    final static class OrCountdown {
        private final int _initialValue;
        private int _value;

        OrCountdown(final int value) {
            _initialValue = value;
            _value = value;
        }

        @SuppressWarnings("UnusedParameters")
        public boolean test(final int ignored) {
            return --_value == 0;
        }

        public boolean test() {
            return --_value == 0;
        }

        public int timesCalled() {
            return _initialValue - _value;
        }

        public void reset() {
            _value = _initialValue;
        }
    }

    interface BooleanCallback {
        boolean call();
    }

    @Test
    public void testThreeOrs() throws Throwable {
        final OrCountdown counter = new OrCountdown(3);
        final Expression counterConstant = constant(counter);

        final LambdaExpression<BooleanCallback> callback = lambda(
            Type.of(BooleanCallback.class),
            orElse(
                call(counterConstant, "test"),
                call(counterConstant, "test"),
                call(counterConstant, "test")
            )
        );

        final BooleanCallback delegate = callback.compile();

        assertTrue(delegate.call());
        assertEquals(3, counter.timesCalled());
    }

    @Test
    public void testAndOr() throws Throwable {
        final OrCountdown counter = new OrCountdown(2);
        final Expression counterConstant = constant(counter);

        final LambdaExpression<Callable<String>> callback = lambda(
            Types.Callable.makeGenericType(Types.String),
            condition(
                orElse(
                    andAlso(
                        call(counterConstant, "test", constant(1)),
                        call(counterConstant, "test", constant(2))
                    ),
                    call(counterConstant, "test", constant(3)),
                    equal(constant(2), constant(3))
                ),
                constant("true"),
                constant("false")
            )
        );

        final Callable<String> delegate = callback.compile();

        assertEquals("true", delegate.call());
        assertEquals(2, counter.timesCalled());
    }

    @Test
    public void testConditionalNullCheck() throws Throwable {
        final ParameterExpression p = parameter(Types.String);

        final LambdaExpression<Func1<String, String>> isNullLambda = lambda(
            Type.of(Func1.class).makeGenericType(Types.String, Types.String),
            condition(isNull(p), constant("true"), constant("false")),
            p
        );

        final LambdaExpression<Func1<String, String>> isNotNullLambda = lambda(
            Type.of(Func1.class).makeGenericType(Types.String, Types.String),
            condition(isNotNull(p), constant("true"), constant("false")),
            p
        );

        final Func1<String, String> isNullCheck = isNullLambda.compile();
        final Func1<String, String> isNullNotCheck = isNotNullLambda.compile();

        assertEquals("false", isNullCheck.apply("notNull"));
        assertEquals("true", isNullCheck.apply(null));
        assertEquals("true", isNullNotCheck.apply("notNull"));
        assertEquals("false", isNullNotCheck.apply(null));
    }

    @Test
    public void testRelationalAndAlso() throws Throwable {
        runRelationalBranchTest(true, constant(false), constant(true));
        runRelationalBranchTest(true, constant((byte)2), constant((byte)3));
        runRelationalBranchTest(true, constant('a'), constant('b'));
        runRelationalBranchTest(true, constant((short)2), constant((short)3));
        runRelationalBranchTest(true, constant(2), constant(3));
        runRelationalBranchTest(true, constant(2L), constant(3L));
        runRelationalBranchTest(true, constant(2f), constant(3f));
        runRelationalBranchTest(true, constant(2d), constant(3d));

        final Expression leftString = constant("two");
        final Expression rightString = constant("three");
        final Expression nullString = constant(null, Types.String);

        runRelationalBranchTest(true, leftString, rightString);
        runRelationalBranchTest(true, leftString, nullString);
    }

    @Test
    public void testRelationalOrElse() throws Throwable {
        runRelationalBranchTest(false, constant(false), constant(true));
        runRelationalBranchTest(false, constant((byte)2), constant((byte)3));
        runRelationalBranchTest(false, constant('a'), constant('b'));
        runRelationalBranchTest(false, constant((short)2), constant((short)3));
        runRelationalBranchTest(false, constant(2), constant(3));
        runRelationalBranchTest(false, constant(2L), constant(3L));
        runRelationalBranchTest(false, constant(2f), constant(3f));
        runRelationalBranchTest(false, constant(2d), constant(3d));

        final Expression leftString = constant("two");
        final Expression rightString = constant("three");
        final Expression nullString = constant(null, Types.String);

        runRelationalBranchTest(false, leftString, rightString);
        runRelationalBranchTest(false, leftString, nullString);
    }

    private static void runRelationalBranchTest(final boolean isAnd, final Expression smaller, final Expression larger) throws Throwable {
        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.Equal, smaller, smaller));
        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.Equal, larger, larger));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.Equal, smaller, larger));

        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.NotEqual, smaller, larger));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.NotEqual, larger, larger));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.NotEqual, smaller, smaller));

        final Type operandType = smaller.getType();
        if (!TypeUtils.isArithmetic(operandType)) {
            if (TypeUtils.isBoolean(operandType)) {
                return;
            }

            assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.ReferenceEqual, smaller, smaller));
            assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.ReferenceEqual, larger, larger));
            assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.ReferenceEqual, smaller, larger));

            assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.ReferenceNotEqual, smaller, larger));
            assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.ReferenceNotEqual, larger, larger));
            assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.ReferenceNotEqual, smaller, smaller));

            return;
        }

        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.GreaterThan, larger, smaller));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.GreaterThan, larger, larger));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.GreaterThan, smaller, larger));

        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.GreaterThanOrEqual, smaller, smaller));
        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.GreaterThanOrEqual, larger, larger));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.GreaterThanOrEqual, smaller, larger));

        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.LessThan, smaller, larger));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.LessThan, larger, larger));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.LessThan, larger, smaller));

        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.LessThanOrEqual, smaller, smaller));
        assertTrue(runRelationalBranchTestForOp(isAnd, ExpressionType.LessThanOrEqual, smaller, larger));
        assertFalse(runRelationalBranchTestForOp(isAnd, ExpressionType.LessThanOrEqual, larger, smaller));
    }

    private static boolean runRelationalBranchTestForOp(
        final boolean isAnd,
        final ExpressionType op,
        final Expression left,
        final Expression right) throws Throwable {

        final OrCountdown counter1 = new OrCountdown(isAnd ? 1 : 2);
        final OrCountdown counter2 = new OrCountdown(isAnd ? 1 : 2);
        final Expression counterConstant1 = constant(counter1);
        final Expression counterConstant2 = constant(counter2);

        final LambdaExpression<Callable<String>> callback = lambda(
            Types.Callable.makeGenericType(Types.String),
            condition(
                makeBinary(
                    isAnd ? ExpressionType.AndAlso : ExpressionType.OrElse,
                    call(counterConstant1, "test", constant(1)),
                    makeBinary(
                        isAnd ? ExpressionType.AndAlso : ExpressionType.OrElse,
                        makeBinary(op, left, right),
                        call(counterConstant2, "test", constant(2))
                    )
                ),
                constant("true"),
                constant("false")
            )
        );

        final Callable<String> delegate = callback.compile();
        final String stringResult = delegate.call();
        final boolean result = "true".equals(stringResult);

        assertEquals(1, counter1.timesCalled());
        assertEquals(isAnd ? (result ? 1 : 0) : (result ? 0 : 1), counter2.timesCalled());

        return result;
    }
}
