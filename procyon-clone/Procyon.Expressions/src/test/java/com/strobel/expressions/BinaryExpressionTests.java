/*
 * BinaryExpressionTests.java
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

import com.strobel.reflection.PrimitiveTypes;
import com.strobel.reflection.Types;
import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.math.BigInteger;
import java.util.concurrent.Callable;

import static com.strobel.expressions.Expression.*;
import static org.junit.Assert.*;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public class BinaryExpressionTests extends AbstractExpressionTest {
    @Test
    public void testMethodBasedBinaryOperators() throws Throwable {
        final BigInteger big = new BigInteger("1234567890123456789012345678901234567890");
        final BigInteger big1 = new BigInteger("1");
        final BigInteger big2 = new BigInteger("2");
        final BigInteger notSoBig = new BigInteger("1234567890");
        final BigInteger big1337 = new BigInteger("1337");
        final BigInteger expectedAddResult = new BigInteger("2469135780246913578024691357802469135780");
        final BigInteger expectedMulResult = expectedAddResult;
        final BigInteger expectedDivResult = new BigInteger("617283945061728394506172839450617283945");
        final BigInteger expectedSubResult = new BigInteger("0");
        final BigInteger expectedModResult = new BigInteger("808");
        final BigInteger expectedShLResult = new BigInteger("633825300114114700748351602688");
        final BigInteger expectedShRResult = big1;

        final LambdaExpression<Callable<BigInteger[]>> lambda = lambda(
            Types.Callable.makeGenericType(Types.BigInteger.makeArrayType()),
            newArrayInit(
                Types.BigInteger,
                add(constant(big), constant(big)),
                subtract(constant(big), constant(big)),
                multiply(constant(big), constant(big2)),
                divide(constant(big), convert(constant(2), Types.BigInteger)),
                modulo(constant(notSoBig), constant(big1337)),
                leftShift(constant(big1), constant(99)),
                rightShift(constant(expectedShLResult), constant(99))
            )
        );

        final BigInteger[] result = lambda.compile().call();

        assertEquals(expectedAddResult, result[0]);
        assertEquals(expectedSubResult, result[1]);
        assertEquals(expectedMulResult, result[2]);
        assertEquals(expectedDivResult, result[3]);
        assertEquals(expectedModResult, result[4]);
        assertEquals(expectedShLResult, result[5]);
        assertEquals(expectedShRResult, result[6]);
    }

    @Test
    public void testMethodBasedComparisonOperators() throws Throwable {
        Expression zero = constant(new BigInteger("0"));
        Expression one = constant(new BigInteger("1"));

        assertResultTrue(lessThanOrEqual(zero, one));
        assertResultTrue(lessThanOrEqual(one, one));
        assertResultFalse(lessThanOrEqual(one, zero));

        assertResultTrue(lessThan(zero, one));
        assertResultFalse(lessThan(one, one));
        assertResultFalse(lessThan(one, zero));

        assertResultFalse(greaterThanOrEqual(zero, one));
        assertResultTrue(greaterThanOrEqual(one, one));
        assertResultTrue(greaterThanOrEqual(one, zero));

        assertResultFalse(greaterThan(zero, one));
        assertResultFalse(greaterThan(one, one));
        assertResultTrue(greaterThan(one, zero));

        assertResultFalse(equal(zero, one));
        assertResultTrue(equal(one, one));
        assertResultFalse(equal(one, zero));

        assertResultTrue(notEqual(zero, one));
        assertResultFalse(notEqual(one, one));
        assertResultTrue(notEqual(one, zero));

        zero = constant(new BigInteger("0"), Types.Object);
        one = constant(new BigInteger("1"), Types.Object);

        assertResultTrue(lessThanOrEqual(zero, one));
        assertResultTrue(lessThanOrEqual(one, one));
        assertResultFalse(lessThanOrEqual(one, zero));

        assertResultTrue(lessThan(zero, one));
        assertResultFalse(lessThan(one, one));
        assertResultFalse(lessThan(one, zero));

        assertResultFalse(greaterThanOrEqual(zero, one));
        assertResultTrue(greaterThanOrEqual(one, one));
        assertResultTrue(greaterThanOrEqual(one, zero));

        assertResultFalse(greaterThan(zero, one));
        assertResultFalse(greaterThan(one, one));
        assertResultTrue(greaterThan(one, zero));

        assertResultFalse(equal(zero, one));
        assertResultTrue(equal(one, one));
        assertResultFalse(equal(one, zero));

        assertResultTrue(notEqual(zero, one));
        assertResultFalse(notEqual(one, one));
        assertResultTrue(notEqual(one, zero));
    }
    
    @Test
    public void testLongArithmeticOperators() throws Throwable {
        final ParameterExpression boxed = parameter(Types.Long, "boxed");
        final ParameterExpression unboxed = parameter(PrimitiveTypes.Long, "unboxed");

        final long left = 13L;
        final long right = 7L;

        final long[][] resultsByOperation = {
            { left + right, right + left },
            { left - right, right - left },
            { left * right, right * left },
            { left / right, right / left },
            { left % right, right % left },
        };

        final ExpressionType[] types = new ExpressionType[] {
            ExpressionType.Add,
            ExpressionType.Subtract,
            ExpressionType.Multiply,
            ExpressionType.Divide,
            ExpressionType.Modulo
        };

        for (int i = 0; i < types.length; i++) {
            final ExpressionType type = types[i];
            final long[] results = resultsByOperation[i];

            final LambdaExpression lambda = lambda(makeBinary(type, boxed, unboxed), boxed, unboxed);
            final MethodHandle handle = lambda.compileHandle();

            assertEquals(results[0], handle.invoke(left, right));
            assertEquals(results[1], handle.invoke(right, left));

            try {
                handle.invoke(null, right);
                fail("NPE expected.");
            }
            catch (final NullPointerException ignored) {
            }

            final LambdaExpression lambda2 = lambda(makeBinary(type, constant(left, Types.Long), constant(right, PrimitiveTypes.Long)));
            final MethodHandle handle2 = lambda2.compileHandle();

            final LambdaExpression lambda3 = lambda(makeBinary(type, constant(right, PrimitiveTypes.Long), constant(left, Types.Long)));
            final MethodHandle handle3 = lambda3.compileHandle();

            assertEquals(results[0], handle2.invoke());
            assertEquals(results[1], handle3.invoke());
        }
    }

    @Test
    public void testComparisonOperators() throws Throwable {
        assertResultTrue(lessThanOrEqual(constant(1), constant(1)));
        assertResultTrue(lessThan(constant(2), constant(3L)));
        assertResultFalse(greaterThan(constant(2d), constant(3d)));
        assertResultTrue(lessThanOrEqual(constant(2f), constant((byte)2)));
    }
    
    @Test
    public void testComparisonOperatorsByteByte() throws Throwable {
        assertResultTrue(lessThanOrEqual(constant((byte)0), constant((byte)1)));
        assertResultTrue(lessThanOrEqual(constant((byte)1), constant((byte)1)));
        assertResultFalse(lessThanOrEqual(constant((byte)1), constant((byte)0)));

        assertResultTrue(lessThan(constant((byte)0), constant((byte)1)));
        assertResultFalse(lessThan(constant((byte)1), constant((byte)1)));
        assertResultFalse(lessThan(constant((byte)1), constant((byte)0)));

        assertResultFalse(greaterThanOrEqual(constant((byte)0), constant((byte)1)));
        assertResultTrue(greaterThanOrEqual(constant((byte)1), constant((byte)1)));
        assertResultTrue(greaterThanOrEqual(constant((byte)1), constant((byte)0)));

        assertResultFalse(greaterThan(constant((byte)0), constant((byte)1)));
        assertResultFalse(greaterThan(constant((byte)1), constant((byte)1)));
        assertResultTrue(greaterThan(constant((byte)1), constant((byte)0)));

        assertResultFalse(equal(constant((byte)0), constant((byte)1)));
        assertResultTrue(equal(constant((byte)1), constant((byte)1)));
        assertResultFalse(equal(constant((byte)1), constant((byte)0)));

        assertResultTrue(notEqual(constant((byte)0), constant((byte)1)));
        assertResultFalse(notEqual(constant((byte)1), constant((byte)1)));
        assertResultTrue(notEqual(constant((byte)1), constant((byte)0)));
    }

    @Test
    public void testComparisonOperatorsByteChar() throws Throwable {
        assertResultTrue(lessThanOrEqual(constant((byte)0), constant((char)1)));
        assertResultTrue(lessThanOrEqual(constant((byte)1), constant((char)1)));
        assertResultFalse(lessThanOrEqual(constant((byte)1), constant((char)0)));

        assertResultTrue(lessThan(constant((byte)0), constant((char)1)));
        assertResultFalse(lessThan(constant((byte)1), constant((char)1)));
        assertResultFalse(lessThan(constant((byte)1), constant((char)0)));

        assertResultFalse(greaterThanOrEqual(constant((byte)0), constant((char)1)));
        assertResultTrue(greaterThanOrEqual(constant((byte)1), constant((char)1)));
        assertResultTrue(greaterThanOrEqual(constant((byte)1), constant((byte)0)));

        assertResultFalse(greaterThan(constant((byte)0), constant((char)1)));
        assertResultFalse(greaterThan(constant((byte)1), constant((char)1)));
        assertResultTrue(greaterThan(constant((byte)1), constant((char)0)));

        assertResultFalse(equal(constant((byte)0), constant((char)1)));
        assertResultTrue(equal(constant((byte)1), constant((char)1)));
        assertResultFalse(equal(constant((byte)1), constant((char)0)));

        assertResultTrue(notEqual(constant((byte)0), constant((char)1)));
        assertResultFalse(notEqual(constant((byte)1), constant((char)1)));
        assertResultTrue(notEqual(constant((byte)1), constant((char)0)));
    }

/*
    private static void generateTests() throws Throwable {
        final String s = "        assertResultTrue(lessThanOrEqual(constant((%1$s)0), constant((%2$s)1)));\n" +
                   "        assertResultTrue(lessThanOrEqual(constant((%1$s)1), constant((%2$s)1)));\n" +
                   "        assertResultFalse(lessThanOrEqual(constant((%1$s)1), constant((%2$s)0)));\n" +
                   "\n" +
                   "        assertResultTrue(lessThan(constant((%1$s)0), constant((%2$s)1)));\n" +
                   "        assertResultFalse(lessThan(constant((%1$s)1), constant((%2$s)1)));\n" +
                   "        assertResultFalse(lessThan(constant((%1$s)1), constant((%2$s)0)));\n" +
                   "\n" +
                   "        assertResultFalse(greaterThanOrEqual(constant((%1$s)0), constant((%2$s)1)));\n" +
                   "        assertResultTrue(greaterThanOrEqual(constant((%1$s)1), constant((%2$s)1)));\n" +
                   "        assertResultTrue(greaterThanOrEqual(constant((%1$s)1), constant((%2$s)0)));\n" +
                   "\n" +
                   "        assertResultFalse(greaterThan(constant((%1$s)0), constant((%2$s)1)));\n" +
                   "        assertResultFalse(greaterThan(constant((%1$s)1), constant((%2$s)1)));\n" +
                   "        assertResultTrue(greaterThan(constant((%1$s)1), constant((%2$s)0)));\n" +
                   "\n" +
                   "        assertResultFalse(equal(constant((%1$s)0), constant((%2$s)1)));\n" +
                   "        assertResultTrue(equal(constant((%1$s)1), constant((%2$s)1)));\n" +
                   "        assertResultFalse(equal(constant((%1$s)1), constant((%2$s)0)));\n" +
                   "\n" +
                   "        assertResultTrue(notEqual(constant((%1$s)0), constant((%2$s)1)));\n" +
                   "        assertResultFalse(notEqual(constant((%1$s)1), constant((%2$s)1)));\n" +
                   "        assertResultTrue(notEqual(constant((%1$s)1), constant((%2$s)0)));\n";

        for (final TypeKind tk1 : EnumSet.allOf(TypeKind.class)) {
            for (final TypeKind tk2 : EnumSet.allOf(TypeKind.class)) {

            }
        }
    }
*/
}
