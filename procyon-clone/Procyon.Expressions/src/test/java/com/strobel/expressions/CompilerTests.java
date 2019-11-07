/*
 * CompilerTests.java
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

import com.strobel.core.MutableInteger;
import com.strobel.core.delegates.Action1;
import com.strobel.core.delegates.Func1;
import com.strobel.reflection.*;
import com.strobel.reflection.emit.FieldBuilder;
import com.strobel.reflection.emit.MethodBuilder;
import com.strobel.reflection.emit.SwitchOptions;
import com.strobel.reflection.emit.TypeBuilder;
import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import static com.strobel.expressions.Expression.*;
import static org.junit.Assert.*;

/**
 * @author Mike Strobel
 */
public final class CompilerTests extends AbstractExpressionTest {
    private static final RuntimeException TestRuntimeException = new RuntimeException("More bad shit happened, yo.");

    interface IListRetriever<T> {
        List<T> getList();
    }

    interface INeedsBridgeMethod<T extends Comparable<String>> {
        T invoke(final T t);
    }

    @Test
    public void testStringEquals() throws Exception {
        final ParameterExpression p = parameter(Types.String, "s");
        final MemberExpression out = field(null, Type.of(System.class).getField("out"));

        final LambdaExpression<Action1<String>> lambda = lambda(
            Type.of(Action1.class).makeGenericType(Types.String),
            call(
                out,
                "println",
                equal(constant("one"), p)
            ),
            p
        );

        System.out.println();
        System.out.println(lambda);

        final Delegate delegate = lambda.compileDelegate();

        System.out.println();
        System.out.printf("\n[%s]\n", delegate.getInstance().getClass().getSimpleName());

        delegate.invokeDynamic("one");
        delegate.invokeDynamic("two");
    }

    @Test
    public void testCoalesce() throws Exception {
        final ParameterExpression p = parameter(Types.String, "s");

        final LambdaExpression<Func1<String, String>> lambda = lambda(
            Type.of(Func1.class).makeGenericType(Types.String, Types.String),
            coalesce(p, constant("null")),
            p
        );

        System.out.println();
        System.out.println(lambda);

        final Func1<String, String> delegate = lambda.compile();

        System.out.println();
        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        assertEquals("notnull", delegate.apply("notnull"));
        assertEquals("null", delegate.apply(null));
    }

    @Test
    public void testGenericMethodCall() throws Exception {
        final LambdaExpression<?> listRetriever = lambda(
            Type.of(IListRetriever.class).makeGenericType(Types.String),
            call(
                Type.of(Collections.class),
                "emptyList",
                Type.list(Types.String)
            )
        );

        System.out.println();
        System.out.println(listRetriever);

        final Delegate delegate = listRetriever.compileDelegate();
        final Object result = delegate.invokeDynamic();

        System.out.println();
        System.out.printf("\n[%s]\n", delegate.getInstance().getClass().getSimpleName());
        System.out.println(result);

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testBridgeMethodGeneration() throws Exception {
        final ParameterExpression arg = parameter(Types.String);

        final LambdaExpression<?> listRetriever = lambda(
            Type.of(INeedsBridgeMethod.class).makeGenericType(Types.String),
            arg,
            arg
        );

        final String input = "zomg";
        final Delegate delegate = listRetriever.compileDelegate();
        final Object result = delegate.invokeDynamic(input);

        assertSame(input, result);
    }

    @Test
    public void testSimpleLoop() throws Exception {
        final ParameterExpression lcv = variable(PrimitiveTypes.Integer, "i");
        final LabelTarget breakLabel = label();
        final LabelTarget continueLabel = label();
        final MemberExpression out = field(null, Type.of(System.class).getField("out"));

        final LambdaExpression<Runnable> runnable = lambda(
            Type.of(Runnable.class),
            block(
                new ParameterExpressionList(lcv),
                assign(lcv, constant(0)),
                call(out, "println", constant("Starting the loop...")),
                loop(
                    block(
                        PrimitiveTypes.Void,
                        ifThen(
                            greaterThanOrEqual(lcv, constant(5)),
                            makeBreak(breakLabel)
                        ),
                        call(out, "printf", constant("Loop iteration #%d\n"), lcv),
                        preIncrementAssign(lcv)
                    ),
                    breakLabel,
                    continueLabel
                ),
                call(out, "println", constant("Finished the loop!"))
            )
        );

        System.out.println();
        System.out.println(runnable);

        final Runnable delegate = runnable.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        delegate.run();
    }

    @Test
    public void testForEachWithArray() throws Exception {
        final Expression out = field(null, Type.of(System.class).getField("out"));
        final ParameterExpression item = variable(Types.String, "item");

        final ConstantExpression items = constant(
            new String[] { "one", "two", "three", "four", "five" }
        );

        final LambdaExpression<Runnable> runnable = lambda(
            Type.of(Runnable.class),
            block(
                call(out, "println", constant("Starting the 'for each' loop...")),
                forEach(
                    item,
                    items,
                    call(out, "printf", constant("Got item: %s\n"), item)
                ),
                call(out, "println", constant("Finished the loop!"))
            )
        );

        System.out.println();
        System.out.println(runnable);

        final Runnable delegate = runnable.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        delegate.run();
    }

    @Test
    public void testForEachWithIterable() throws Exception {
        final Expression out = field(null, Type.of(System.class).getField("out"));
        final ParameterExpression item = variable(Types.String, "item");

        final ConstantExpression items = constant(
            Arrays.asList("one", "two", "three", "four", "five"),
            Types.Iterable.makeGenericType(Types.String)
        );

        final LambdaExpression<Runnable> runnable = lambda(
            Type.of(Runnable.class),
            block(
                call(out, "println", constant("Starting the 'for each' loop...")),
                forEach(
                    item,
                    items,
                    call(out, "printf", constant("Got item: %s\n"), item)
                ),
                call(out, "println", constant("Finished the loop!"))
            )
        );

        System.out.println();
        System.out.println(runnable);

        final Runnable delegate = runnable.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        delegate.run();
    }

    @Test
    public void testForLoop() throws Exception {
        final Expression out = field(null, Type.of(System.class).getField("out"));
        final ParameterExpression base = variable(PrimitiveTypes.Integer, "base");
        final ParameterExpression power = variable(PrimitiveTypes.Integer, "power");
        final ParameterExpression accumulator = variable(PrimitiveTypes.Integer, "accumulator");
        final ParameterExpression variable = variable(PrimitiveTypes.Integer, "i");

        final LambdaExpression<IntegerPowerDelegate> runnable = lambda(
            Type.of(IntegerPowerDelegate.class),
            block(
                new ParameterExpressionList(accumulator),
                assign(accumulator, base),
                makeFor(
                    variable,
                    constant(1),
                    lessThan(variable, power),
                    preIncrementAssign(variable),
                    multiplyAssign(accumulator, base)
                ),
                call(out, "printf", constant("%d^%d=%d\n"), base, power, accumulator),
                accumulator
            ),
            base,
            power
        );

        System.out.println();
        System.out.println(runnable);

        final IntegerPowerDelegate delegate = runnable.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        assertEquals(-1, delegate.transform(-1, 0));
        assertEquals(-1, delegate.transform(-1, 1));
        assertEquals(2, delegate.transform(2, 1));
        assertEquals(4, delegate.transform(2, 2));
        assertEquals(16, delegate.transform(2, 4));
    }

    @Test
    public void simpleLambdaTest() throws Exception {
        final ParameterExpression number = parameter(PrimitiveTypes.Integer, "number");

        final LambdaExpression<ITest> lambda = lambda(
            Type.of(ITest.class),
            call(
                condition(
                    equal(
                        number,
                        call(
                            Types.Integer,
                            "parseInt",
                            TypeList.empty(),
                            constant("0")
                        )
                    ),
                    constant("zero"),
                    condition(
                        lessThan(number, constant(0)),
                        constant("negative"),
                        constant("positive")
                    )
                ),
                "toUpperCase",
                TypeList.empty(),
                constant(Locale.getDefault())
            ),
            number
        );

        System.out.println();
        System.out.println(lambda);

        final ITest delegate = lambda.compile();

        assertEquals("NEGATIVE", delegate.testNumber(-15));
        assertEquals("ZERO", delegate.testNumber(0));
        assertEquals("POSITIVE", delegate.testNumber(99));

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        System.out.println(delegate.testNumber(-15));
        System.out.println(delegate.testNumber(0));
        System.out.println(delegate.testNumber(99));
    }

    @Test
    public void returnLabelTest() throws Exception {
        final ParameterExpression number = parameter(PrimitiveTypes.Integer, "number");
        final LabelTarget returnLabel = label(Types.String);

        final LambdaExpression<ITest> lambda = lambda(
            Type.of(ITest.class),
            block(
                ifThenElse(
                    equal(number, constant(0)),
                    makeReturn(returnLabel, constant("zero")),
                    ifThenElse(
                        lessThan(number, constant(0)),
                        makeReturn(returnLabel, constant("negative")),
                        makeReturn(returnLabel, constant("positive"))
                    )
                ),
                label(returnLabel)
            ),
            number
        );

        System.out.println();

        System.out.println(lambda);

        final ITest delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        assertEquals("negative", delegate.testNumber(-15));
        assertEquals("zero", delegate.testNumber(0));
        assertEquals("positive", delegate.testNumber(99));

        System.out.println(delegate.testNumber(-15));
        System.out.println(delegate.testNumber(0));
        System.out.println(delegate.testNumber(99));
    }

    @Test
    public void testIntegerLookupSwitch() throws Exception {
        final ParameterExpression number = parameter(Types.Integer, "number");

        final LambdaExpression<Func1<Integer, String>> lambda = lambda(
            Type.of(Func1.class).makeGenericType(Types.Integer, Types.String),
            makeSwitch(
                unbox(number),
                SwitchOptions.PreferLookup,
                constant("something else"),
                switchCase(
                    constant("one or two"),
                    constant(1),
                    constant(2)
                ),
                switchCase(
                    constant("three"),
                    constant(3)
                ),
                switchCase(
                    constant("five"),
                    constant(5)
                )
            ),
            number
        );

        System.out.println();

        System.out.println(lambda);

        final Func1<Integer, String> delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        System.out.println(delegate.apply(0));
        System.out.println(delegate.apply(1));
        System.out.println(delegate.apply(2));
        System.out.println(delegate.apply(3));
        System.out.println(delegate.apply(4));
        System.out.println(delegate.apply(5));
        System.out.println(delegate.apply(6));

        assertEquals("something else", delegate.apply(0));
        assertEquals("one or two", delegate.apply(1));
        assertEquals("one or two", delegate.apply(2));
        assertEquals("three", delegate.apply(3));
        assertEquals("something else", delegate.apply(4));
        assertEquals("five", delegate.apply(5));
        assertEquals("something else", delegate.apply(6));
    }

    @Test
    public void testIntegerTableSwitch() throws Exception {
        final ParameterExpression number = parameter(Types.Integer, "number");

        final LambdaExpression<Func1<Integer, String>> lambda = lambda(
            Type.of(Func1.class).makeGenericType(Types.Integer, Types.String),
            makeSwitch(
                unbox(number),
                SwitchOptions.PreferTable,
                constant("something else"),
                switchCase(
                    constant("one or two"),
                    constant(1),
                    constant(2)
                ),
                switchCase(
                    constant("three"),
                    constant(3)
                ),
                switchCase(
                    constant("five"),
                    constant(5)
                )
            ),
            number
        );

        System.out.println();

        System.out.println(lambda);

        final Func1<Integer, String> delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        System.out.println(delegate.apply(0));
        System.out.println(delegate.apply(1));
        System.out.println(delegate.apply(2));
        System.out.println(delegate.apply(3));
        System.out.println(delegate.apply(4));
        System.out.println(delegate.apply(5));
        System.out.println(delegate.apply(6));

        assertEquals("something else", delegate.apply(0));
        assertEquals("one or two", delegate.apply(1));
        assertEquals("one or two", delegate.apply(2));
        assertEquals("three", delegate.apply(3));
        assertEquals("something else", delegate.apply(4));
        assertEquals("five", delegate.apply(5));
        assertEquals("something else", delegate.apply(6));
    }

    enum TestEnum {
        ZERO,
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX
    }

    @Test
    public void testEnumLookupSwitch() throws Exception {
        final Type<TestEnum> enumType = Type.of(TestEnum.class);
        final ParameterExpression enumValue = parameter(enumType, "e");

        final LambdaExpression<Func1<TestEnum, String>> lambda = lambda(
            Type.of(Func1.class).makeGenericType(enumType, Types.String),
            "testEnumLookupSwitch",
            makeSwitch(
                enumValue,
                constant("something else"),
                switchCase(
                    constant("one or two"),
                    constant(TestEnum.ONE),
                    constant(TestEnum.TWO)
                ),
                switchCase(
                    constant("three"),
                    constant(TestEnum.THREE)
                ),
                switchCase(
                    constant("five"),
                    constant(TestEnum.FIVE)
                )
            ),
            enumValue
        );

        System.out.println();

        System.out.println(lambda);

        final Func1<TestEnum, String> delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        System.out.println(delegate.apply(TestEnum.ONE));
        System.out.println(delegate.apply(TestEnum.TWO));
        System.out.println(delegate.apply(TestEnum.THREE));
        System.out.println(delegate.apply(TestEnum.FOUR));
        System.out.println(delegate.apply(TestEnum.FIVE));
        System.out.println(delegate.apply(TestEnum.SIX));

        assertEquals("something else", delegate.apply(TestEnum.ZERO));
        assertEquals("one or two", delegate.apply(TestEnum.ONE));
        assertEquals("one or two", delegate.apply(TestEnum.TWO));
        assertEquals("three", delegate.apply(TestEnum.THREE));
        assertEquals("something else", delegate.apply(TestEnum.FOUR));
        assertEquals("five", delegate.apply(TestEnum.FIVE));
        assertEquals("something else", delegate.apply(TestEnum.SIX));
    }

    @Test
    public void testStringTrieSwitch() throws Exception {
        final ParameterExpression stringValue = parameter(Types.String, "s");

        final LambdaExpression<Func1<String, String>> lambda = lambda(
            Type.of(Func1.class).makeGenericType(Types.String, Types.String),
            "testStringTrieSwitch",
            makeSwitch(
                stringValue,
                SwitchOptions.PreferTrie,
                constant("something else"),
                switchCase(
                    constant("one or two"),
                    constant("1"),
                    constant("2")
                ),
                switchCase(
                    constant("three"),
                    constant("3")
                ),
                switchCase(
                    constant("five"),
                    constant("5")
                )
            ),
            stringValue
        );

        System.out.println();

        System.out.println(lambda);

        final Func1<String, String> delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        System.out.println(delegate.apply("0"));
        System.out.println(delegate.apply("1"));
        System.out.println(delegate.apply("2"));
        System.out.println(delegate.apply("3"));
        System.out.println(delegate.apply("4"));
        System.out.println(delegate.apply("5"));
        System.out.println(delegate.apply("6"));

        assertEquals("something else", delegate.apply("0"));
        assertEquals("one or two", delegate.apply("1"));
        assertEquals("one or two", delegate.apply("2"));
        assertEquals("three", delegate.apply("3"));
        assertEquals("something else", delegate.apply("4"));
        assertEquals("five", delegate.apply("5"));
        assertEquals("something else", delegate.apply("6"));
    }

    @Test
    public void testStringHashTableSwitch() throws Exception {
        final ParameterExpression stringValue = parameter(Types.String, "s");

        final LambdaExpression<Func1<String, String>> lambda = lambda(
            Type.of(Func1.class).makeGenericType(Types.String, Types.String),
            "testStringHashTableSwitch",
            makeSwitch(
                stringValue,
                SwitchOptions.PreferTable,
                constant("something else"),
                switchCase(
                    constant("one or two"),
                    constant("1"),
                    constant("2")
                ),
                switchCase(
                    constant("three"),
                    constant("3")
                ),
                switchCase(
                    constant("five"),
                    constant("5")
                )
            ),
            stringValue
        );

        System.out.println();
        System.out.println(lambda);

        final Func1<String, String> delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        System.out.println(delegate.apply("0"));
        System.out.println(delegate.apply("1"));
        System.out.println(delegate.apply("2"));
        System.out.println(delegate.apply("3"));
        System.out.println(delegate.apply("4"));
        System.out.println(delegate.apply("5"));
        System.out.println(delegate.apply("6"));

        assertEquals("something else", delegate.apply("0"));
        assertEquals("one or two", delegate.apply("1"));
        assertEquals("one or two", delegate.apply("2"));
        assertEquals("three", delegate.apply("3"));
        assertEquals("something else", delegate.apply("4"));
        assertEquals("five", delegate.apply("5"));
        assertEquals("something else", delegate.apply("6"));
    }

    @Test
    public void testStringHashLookupSwitch() throws Exception {
        final ParameterExpression stringValue = parameter(Types.String, "s");

        final LambdaExpression<Func1<String, String>> lambda = lambda(
            Type.of(Func1.class).makeGenericType(Types.String, Types.String),
            "testStringHashLookupSwitch",
            makeSwitch(
                stringValue,
                SwitchOptions.PreferLookup,
                constant("something else"),
                switchCase(
                    constant("one or two"),
                    constant("1"),
                    constant("2")
                ),
                switchCase(
                    constant("three"),
                    constant("3")
                ),
                switchCase(
                    constant("five"),
                    constant("5")
                )
            ),
            stringValue
        );

        System.out.println();
        System.out.println(lambda);

        final Func1<String, String> delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        System.out.println(delegate.apply("0"));
        System.out.println(delegate.apply("1"));
        System.out.println(delegate.apply("2"));
        System.out.println(delegate.apply("3"));
        System.out.println(delegate.apply("4"));
        System.out.println(delegate.apply("5"));
        System.out.println(delegate.apply("6"));

        assertEquals("something else", delegate.apply("0"));
        assertEquals("one or two", delegate.apply("1"));
        assertEquals("one or two", delegate.apply("2"));
        assertEquals("three", delegate.apply("3"));
        assertEquals("something else", delegate.apply("4"));
        assertEquals("five", delegate.apply("5"));
        assertEquals("something else", delegate.apply("6"));
    }

    @Test
    public void testTryCatchFinally() throws Exception {
        final Expression out = field(null, Type.of(System.class).getField("out"));
        final ParameterExpression tempException = variable(Types.RuntimeException, "$exception");

        final LambdaExpression<Runnable> lambda = lambda(
            Type.of(Runnable.class),
            block(
                new ParameterExpressionList(tempException),
                tryCatchFinally(
                    call(Type.of(CompilerTests.class), "throwAssertionError"),
                    call(out, "println", constant("In the finally block.")),
                    makeCatch(
                        Type.of(AssertionError.class),
                        call(out, "println", constant("In the AssertionError catch block."))
                    )
                )
            )
        );

        System.out.println();
        System.out.println(lambda);

        final Runnable delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        try {
            delegate.run();
        }
        catch (Throwable t) {
            fail("AssertionError should have been caught.");
        }
    }

    @Test
    public void testTryFinally() throws Exception {
        final MutableInteger counter = new MutableInteger(0);
        final Expression counterConstant = constant(counter);
        final ParameterExpression shouldThrow = parameter(PrimitiveTypes.Boolean);

        final LambdaExpression<ShouldThrowDelegate> lambda = lambda(
            Type.of(ShouldThrowDelegate.class),
            tryFinally(
                call(Type.of(CompilerTests.class), "maybeThrow", shouldThrow),
                call(counterConstant, "increment")
            ),
            shouldThrow
        );

        System.out.println();
        System.out.println(lambda);

        final ShouldThrowDelegate delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        try {
            delegate.maybeThrow(false);
        }
        catch (Throwable t) {
            fail("Exception should not have been thrown.");
        }

        assertEquals(1, counter.getValue());
        counter.setValue(0);

        try {
            delegate.maybeThrow(true);
            fail("Exception should have been thrown.");
        }
        catch (Throwable ignored) {
        }

        assertEquals(1, counter.getValue());
    }

    @Test
    public void testTryNestedCatchFinally() throws Exception {
        final Expression out = field(null, Type.of(System.class).getField("out"));
        final ParameterExpression tempException = variable(Types.RuntimeException, "$exception");

        final LambdaExpression<Runnable> lambda = lambda(
            Type.of(Runnable.class),
            block(
                new ParameterExpressionList(tempException),
                tryCatchFinally(
                    call(Type.of(CompilerTests.class), "throwAssertionError"),
                    call(out, "println", constant("In the finally block.")),
                    makeCatch(
                        Type.of(AssertionError.class),
                        tryCatch(
                            block(
                                call(out, "println", constant("In the AssertionError catch block.")),
                                call(Type.of(CompilerTests.class), "throwRuntimeException")
                            ),
                            makeCatch(
                                Types.RuntimeException,
                                tempException,
                                block(
                                    call(out, "println", constant("In the RuntimeException catch block.")),
                                    makeThrow(tempException)
                                )
                            )
                        )
                    )
                )
            )
        );

        System.out.println();
        System.out.println(lambda);

        final Runnable delegate = lambda.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        try {
            delegate.run();
            fail("RuntimeException should have been rethrown.");
        }
        catch (AssertionError e) {
            fail("AssertionError should have been caught.");
        }
        catch (Throwable e) {
            assertEquals(TestRuntimeException, e);
        }
    }

    @Test
    public void testBinaryNumericPromotion() throws Exception {
        final LambdaExpression<ISimpleTest> lambda = lambda(
            Type.of(ISimpleTest.class),
            andAlso(
                typeEqual(convert(multiply(constant(3), constant(2L)), Types.Object), Types.Long),
                typeEqual(convert(multiply(constant(3L), constant(2)), Types.Object), Types.Long),
                typeEqual(convert(multiply(constant(3f), constant(2L)), Types.Object), Types.Float),
                typeEqual(convert(multiply(constant((short) 3), constant(2f)), Types.Object), Types.Float),
                typeEqual(convert(multiply(constant(3d), constant((char) 2)), Types.Object), Types.Double),
                typeEqual(convert(multiply(constant((byte) 3), constant(2d)), Types.Object), Types.Double)
            )
        );

        System.out.println();
        System.out.println(lambda);

        final ISimpleTest delegate = lambda.compile();

        assertTrue(delegate.test());
    }

    @Test
    public void testCompileToMethod() throws Exception {
        final Expression out = field(null, Type.of(System.class).getField("out"));

        final TypeBuilder<Runnable> typeBuilder = new TypeBuilder<>(
            "TestCompileToMethod",
            Modifier.PUBLIC | Modifier.FINAL,
            Types.Object,
            Type.list(Type.of(Runnable.class))
        );

        final MethodBuilder powerMethod = typeBuilder.defineMethod(
            "power",
            Modifier.PUBLIC | Modifier.FINAL,
            PrimitiveTypes.Integer,
            Type.list(PrimitiveTypes.Integer, PrimitiveTypes.Integer)
        );

        final MethodBuilder runMethod = typeBuilder.defineMethod(
            "run",
            Modifier.PUBLIC | Modifier.FINAL,
            PrimitiveTypes.Void
        );

        final Expression self = self(typeBuilder);

        final ParameterExpression base = variable(PrimitiveTypes.Integer, "base");
        final ParameterExpression power = variable(PrimitiveTypes.Integer, "power");
        final ParameterExpression accumulator = variable(PrimitiveTypes.Integer, "accumulator");
        final ParameterExpression variable = variable(PrimitiveTypes.Integer, "i");

        final LambdaExpression<IntegerPowerDelegate> powerLambda = lambda(
            Type.of(IntegerPowerDelegate.class),
            block(
                new ParameterExpressionList(accumulator),
                assign(accumulator, base),
                makeFor(
                    variable,
                    constant(1),
                    lessThan(variable, power),
                    preIncrementAssign(variable),
                    multiplyAssign(accumulator, base)
                ),
                call(out, "printf", constant("%d^%d=%d\n"), base, power, accumulator),
                accumulator
            ),
            base,
            power
        );

        powerLambda.compileToMethod(powerMethod);

        final LambdaExpression<Runnable> runLambda = lambda(
            Type.of(Runnable.class),
            call(
                out,
                "println",
                call(self, powerMethod, constant(2), constant(4))
            )
        );

        runLambda.compileToMethod(runMethod);

        final Type<Runnable> type = typeBuilder.createType();
        final Runnable instance = type.newInstance();

        instance.run();
    }

    @Test
    public void testCompileToGeneratedMethod() throws Exception {
        final Expression out = field(null, Type.of(System.class).getField("out"));

        final TypeBuilder<Runnable> typeBuilder = new TypeBuilder<>(
            "TestCompileToGeneratedMethod",
            Modifier.PUBLIC | Modifier.FINAL,
            Types.Object,
            Type.list(Type.of(Runnable.class))
        );

        final Expression self = self(typeBuilder);

        final ParameterExpression base = variable(PrimitiveTypes.Integer, "base");
        final ParameterExpression power = variable(PrimitiveTypes.Integer, "power");
        final ParameterExpression accumulator = variable(PrimitiveTypes.Integer, "accumulator");
        final ParameterExpression variable = variable(PrimitiveTypes.Integer, "i");

        final LambdaExpression<IntegerPowerDelegate> powerLambda = lambda(
            Type.of(IntegerPowerDelegate.class),
            "power",
            block(
                new ParameterExpressionList(accumulator),
                assign(accumulator, base),
                makeFor(
                    variable,
                    constant(1),
                    lessThan(variable, power),
                    preIncrementAssign(variable),
                    multiplyAssign(accumulator, base)
                ),
                call(out, "printf", constant("%d^%d=%d\n"), base, power, accumulator),
                accumulator
            ),
            base,
            power
        );

        final MethodInfo powerMethod = powerLambda.compileToMethod(typeBuilder);

        final LambdaExpression<Runnable> runLambda = lambda(
            Type.of(Runnable.class),
            "run",
            call(
                out,
                "println",
                call(self, powerMethod, constant(2), constant(4))
            )
        );

        runLambda.compileToMethod(typeBuilder);

        final Type<Runnable> type = typeBuilder.createType();
        final Runnable instance = type.newInstance();

        instance.run();
    }

    @Test
    public void testNestedLambdaInvocation() throws Exception {
        final Type<?> callable = Type.of(Callable.class).makeGenericType(Types.Object);

        final Object expectedResult = this;

        final LambdaExpression<Callable<Object>> outer = lambda(
            callable,
            call(
                Type.of(CompilerTests.class),
                "invoke",
                Type.list(Types.Object),
                lambda(callable, constant(expectedResult))
            )
        );

        System.out.println();
        System.out.println(outer);

        final Callable<Object> delegate = outer.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        final Object result = delegate.call();

        System.out.println(result);

        assertSame(expectedResult, result);
    }

    @Test
    public void testNestedLambdaInvocationWithWildcardCapture() throws Exception {
        final String expectedResult = "EXPECTED_RESULT";
        final Type<? extends CharSequence> wildcard = Type.makeExtendsWildcard(Type.of(CharSequence.class));
        final Type<Callable<? extends CharSequence>> callable = Type.of(Callable.class).makeGenericType(wildcard);

        final LambdaExpression<Callable<Object>> outer = lambda(
            callable,
            call(
                Type.of(CompilerTests.class),
                "invoke",
                Type.list(wildcard),
                lambda(callable, constant(expectedResult))
            )
        );

        System.out.println();
        System.out.println(outer);

        final Callable<Object> delegate = outer.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        final Object result = delegate.call();

        System.out.println(result);

        assertSame(expectedResult, result);
    }

    @Test
    public void testNestedLambdaClosureAccess() throws Throwable {
        final Type<?> callable = Type.of(Callable.class).makeGenericType(Types.Integer);

        final int expectedResult = 84;
        final ParameterExpression temp = variable(PrimitiveTypes.Integer);
        final ParameterExpression innerTemp = variable(PrimitiveTypes.Integer);

        final LambdaExpression<?> outer = lambda(
            block(
                new ParameterExpression[] { temp },
                assign(temp, constant(42)),
                call(
                    Type.of(CompilerTests.class),
                    "invoke",
                    Type.list(Types.Integer),
                    lambda(
                        callable,
                        block(
                            new ParameterExpression[] { innerTemp },
                            assign(innerTemp, temp),
                            assign(temp, multiply(temp, constant(2))),
                            innerTemp
                        )
                    )
                ),
                temp
            )
        );

        System.out.println();
        System.out.println(outer);

        final Delegate delegate = outer.compileDelegate();
        final MethodHandle handle = delegate.getMethodHandle();

        System.out.printf("\n[%s]\n", handle.getClass().getSimpleName());

        final int result = (int) handle.invokeExact();

        System.out.println(result);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testNew() throws Exception {
        final Type<NeedsTwoCtorArgs> resultType = Type.of(NeedsTwoCtorArgs.class);
        final ConstructorInfo constructor = resultType.getConstructors(BindingFlags.All).get(0);
        final Type<?> callable = Type.of(Callable.class).makeGenericType(resultType);

        final LambdaExpression<Callable<NeedsTwoCtorArgs>> outer = lambda(
            callable,
            makeNew(
                constructor,
                constant(2),
                constant(3d)
            )
        );

        System.out.println();
        System.out.println(outer);

        final Callable<?> delegate = outer.compile();

        System.out.printf("\n[%s]\n", delegate.getClass().getSimpleName());

        final Object result = delegate.call();

        System.out.println(result);

        assertTrue(result instanceof NeedsTwoCtorArgs);
    }

    @Test
    public void testTypeInitializerGeneration() throws Throwable {
        final TypeBuilder<Runnable> typeBuilder = new TypeBuilder<>(
            "TestTypeInitializerGeneration",
            Modifier.PUBLIC | Modifier.FINAL,
            Types.Object,
            Type.list(Type.of(Runnable.class))
        );

        final FieldBuilder staticField = typeBuilder.defineField(
            "Numbers",
            Type.of(int[].class),
            Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC
        );

        final MethodBuilder typeInitializer = typeBuilder.defineTypeInitializer();

        lambda(
            Types.Runnable,
            assign(
                field(null, staticField),
                newArrayInit(
                    PrimitiveTypes.Integer,
                    constant(1),
                    constant(2),
                    constant(3),
                    constant(4),
                    constant(5)
                )
            )
        ).compileToMethod(typeInitializer);

        final Type<Runnable> generatedType = typeBuilder.createType();

        final MethodHandle getter = MethodHandles.lookup().findStaticGetter(
            generatedType.getErasedClass(),
            "Numbers",
            int[].class
        );

        final int[] numbers = (int[]) getter.invokeExact();

        assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, numbers);
    }

    @Test
    public void testUntypedLambdaCreation() throws Throwable {
        final ParameterExpression p1 = parameter(PrimitiveTypes.Integer);
        final ParameterExpression p2 = parameter(PrimitiveTypes.Long);
        final ParameterExpression p3 = parameter(Types.String);

        final LambdaExpression<?> e = lambda(
            andAlso(
                lessThan(convert(p1, PrimitiveTypes.Long), p2),
                isNotNull(p3)
            ),
            p1,
            p2,
            p3
        );

        final MethodInfo invokeMethod = Expression.getInvokeMethod(e);
        final Type<?> returnType = invokeMethod.getReturnType();
        final TypeList parameterTypes = invokeMethod.getParameters().getParameterTypes();
        final Type delegateType = invokeMethod.getDeclaringType();

        assertTrue(delegateType.isGenericType());
        assertEquals(Types.String, delegateType.getTypeArguments().get(0));

        assertEquals(PrimitiveTypes.Boolean, returnType);
        assertEquals(PrimitiveTypes.Integer, parameterTypes.get(0));
        assertEquals(PrimitiveTypes.Long, parameterTypes.get(1));
        assertEquals(Types.String, parameterTypes.get(2));

        final Delegate<?> delegate = e.compileDelegate();
        final boolean result = (boolean) delegate.getMethodHandle().invokeExact(2, 3L, "moo");

        assertTrue(result);
    }

    @Test
    public void testFinallyWithReturnFromTry1() throws Throwable {
        final ParameterExpression p1 = parameter(PrimitiveTypes.Integer);
        final MemberExpression out = field(null, Type.of(System.class).getField("out"));
        final LabelTarget returnLabel = label(PrimitiveTypes.Integer);

        final LambdaExpression<?> e = lambda(
            block(
                tryFinally(
                    block(
                        ifThen(
                            lessThan(p1, constant(0)),
                            block(
                                call(out, "println", constant("negative")),
                                makeReturn(returnLabel, constant(-1))
                            )
                        ),
                        call(out, "println", condition(greaterThan(p1, constant(0)), constant("positive"), constant("zero")))
                    ),
                    block(
                        call(out, "println", constant("finally"))
                    )
                ),
                label(returnLabel, condition(greaterThan(p1, constant(0)), constant(1), constant(0)))
            ),
            p1
        );

        System.out.println(e.getDebugView());
        queue().clear();

        final Delegate<?> delegate = e.compileDelegate();

        assertEquals(-1, delegate.invokeDynamic(-25));
        assertEquals(0, delegate.invokeDynamic(0));
        assertEquals(1, delegate.invokeDynamic(25));

        assertEquals("negative", dequeue());
        assertEquals("finally", dequeue());
        assertEquals("zero", dequeue());
        assertEquals("finally", dequeue());
        assertEquals("positive", dequeue());
        assertEquals("finally", dequeue());
    }
    @Test
    public void testFinallyWithReturnFromTry2() throws Throwable {
        final ParameterExpression p1 = parameter(PrimitiveTypes.Integer);
        final MemberExpression out = field(null, Type.of(System.class).getField("out"));
        final LabelTarget returnLabel = label(PrimitiveTypes.Integer);

        final LambdaExpression<?> e = lambda(
            block(
                tryFinally(
                    block(
                        ifThen(
                            lessThan(p1, constant(0)),
                            block(
                                call(out, "println", constant("negative")),
                                makeReturn(returnLabel, constant(-1))
                            )
                        )
                    ),
                    block(
                        call(out, "println", constant("finally"))
                    )
                ),
                call(out, "println", condition(greaterThan(p1, constant(0)), constant("positive"), constant("zero"))),
                label(returnLabel, condition(greaterThan(p1, constant(0)), constant(1), constant(0)))
            ),
            p1
        );

        System.out.println(e.getDebugView());
        queue().clear();

        final Delegate<?> delegate = e.compileDelegate();

        assertEquals(-1, delegate.invokeDynamic(-25));
        assertEquals(0, delegate.invokeDynamic(0));
        assertEquals(1, delegate.invokeDynamic(25));

        assertEquals("negative", dequeue());
        assertEquals("finally", dequeue());
        assertEquals("finally", dequeue());
        assertEquals("zero", dequeue());
        assertEquals("finally", dequeue());
        assertEquals("positive", dequeue());
    }

    @Test
    public void testFinallyWithGotoEscapeFromTry() throws Throwable {
        final ParameterExpression p1 = parameter(PrimitiveTypes.Integer);
        final MemberExpression out = field(null, Type.of(System.class).getField("out"));
        final LabelTarget exitLabel = label();

        final LambdaExpression<?> e = lambda(
            block(
                tryFinally(
                    block(
                        ifThen(
                            lessThan(p1, constant(0)),
                            block(
                                call(out, "println", constant("negative")),
                                makeGoto(exitLabel)
                            )
                        ),
                        call(out, "println", condition(greaterThan(p1, constant(0)), constant("positive"), constant("zero")))
                    ),
                    block(
                        call(out, "println", constant("finally"))
                    )
                ),
                label(exitLabel),
                call(out, "println", constant("return"))
            ),
            p1
        );

        System.out.println(e.getDebugView());
        queue().clear();

        final Delegate<?> delegate = e.compileDelegate();

        delegate.invokeDynamic(-1);
        delegate.invokeDynamic(0);
        delegate.invokeDynamic(1);

        assertEquals("negative", dequeue());
        assertEquals("return", dequeue());
        assertEquals("zero", dequeue());
        assertEquals("finally", dequeue());
        assertEquals("return", dequeue());
        assertEquals("positive", dequeue());
        assertEquals("finally", dequeue());
        assertEquals("return", dequeue());
    }

    @Test
    public void testHoistedLocals() throws Throwable {
        final ParameterExpression v1 = variable(PrimitiveTypes.Integer, "i");
        final MemberExpression out = field(null, Type.of(System.class).getField("out"));

        final LabelTarget breakLabel = label();
        final LabelTarget continueLabel = label();

        final LambdaExpression<?> e = lambda(
            block(
                new ParameterExpressionList(v1),
                assign(v1, constant(0)),
                loop(
                    block(
                        PrimitiveTypes.Void,
                        ifThen(
                            greaterThanOrEqual(v1, constant(5)),
                            makeBreak(breakLabel)
                        ),
                    call(
                        Type.of(CompilerTests.class),
                        "run",
                        TypeList.empty(),
                        lambda(
                            Types.Runnable,
                            Expression.invoke(
                                lambda(call(out, "printf", constant("i=%d\n"), preIncrementAssign(v1)))
                            )
                        )
                    )

//                    Expression.invoke(
//                        lambda(
//                            Types.Runnable,
//                            Expression.invoke(
//                                lambda(call(out, "printf", constant("i=%d\n"), preIncrementAssign(v1)))
//                            ))
//                    )

//                        call(
//                            Type.of(CompilerTests.class),
//                            "run",
//                            TypeList.empty(),
//                            lambda(
//                                Types.Runnable,
//                                call(out, "printf", constant("i=%d\n"), preIncrementAssign(v1))
//                            )
//                        )
                    ),
                    breakLabel,
                    continueLabel
                )
            )
        );

        System.out.println(e.getDebugView());
        queue().clear();

        final Delegate<?> delegate = e.compileDelegate();

        delegate.invokeDynamic();

        assertEquals("i=1", dequeue());
        assertEquals("i=2", dequeue());
        assertEquals("i=3", dequeue());
        assertEquals("i=4", dequeue());
        assertEquals("i=5", dequeue());
    }

    static <T> T invoke(final Callable<T> callback) {
        try {
            return callback.call();
        }
        catch (Exception e) {
            throw new TargetInvocationException(e);
        }
    }

    static void run(final Runnable callback) {
        callback.run();
    }

    static void maybeThrow(final boolean throwException) {
        if (throwException) {
            throw new RuntimeException();
        }
    }

    static final class NeedsTwoCtorArgs {
        @SuppressWarnings("UnusedParameters")
        NeedsTwoCtorArgs(final int x, final double y) {
        }
    }

    static void throwAssertionError()
        throws AssertionError {
        throw new AssertionError("Bad shit happened, yo.");
    }

    static void throwRuntimeException() {
        throw TestRuntimeException;
    }

    interface ISimpleTest {
        boolean test();
    }

    interface ShouldThrowDelegate {
        void maybeThrow(final boolean throwException);
    }

    interface IntegerPowerDelegate {
        int transform(final int base, final int power);
    }

    interface ITest {
        String testNumber(int number);
    }
}
