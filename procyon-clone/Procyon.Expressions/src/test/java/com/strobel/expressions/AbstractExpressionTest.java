/*
 * AbstractExpressionTest.java
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

import com.strobel.reflection.Type;
import org.junit.After;
import org.junit.Before;

import java.io.PrintStream;
import java.security.cert.PolicyQualifierInfo;
import java.util.ArrayDeque;
import java.util.Queue;

import static com.strobel.expressions.Expression.lambda;
import static org.junit.Assert.*;

/**
 * @author Mike Strobel
 */
public abstract class AbstractExpressionTest {
    private final static ThreadLocal<Queue<Object>> OUTPUT_QUEUE = new ThreadLocal<Queue<Object>>() {
        @Override
        protected Queue<Object> initialValue() {
            return new ArrayDeque<>();
        }
    };

    private static final ThreadLocal<OutputInfo> THREAD_OUT = new ThreadLocal<OutputInfo>() {
        @Override
        protected OutputInfo initialValue() {
            return new OutputInfo(queue());
        }
    };

    public static void push(final Object value) {
        OUTPUT_QUEUE.get().add(value);
    }

    public static Object dequeue() {
        return OUTPUT_QUEUE.get().poll();
    }

    public static Object peek() {
        return OUTPUT_QUEUE.get().peek();
    }

    public static Queue<Object> queue() {
        return OUTPUT_QUEUE.get();
    }

    public static void clearQueue() {
        OUTPUT_QUEUE.get().clear();
    }

    public static Expression makePush(final Expression value) {
        return Expression.call(
            Expression.call(
                Type.of(AbstractExpressionTest.class),
                "queue"
            ),
            "add",
            value
        );
    }

    public static Expression outExpression() {
        return Expression.field(
            null,
            Type.of(System.class).getField("out")
        );
    }
    public static OutputRecorder outRecorder() {
        return THREAD_OUT.get().recorder;
    }

    @Before
    public void setUp() throws Throwable {
        final OutputInfo outputInfo = THREAD_OUT.get();
        outputInfo.systemStream = System.out;
        System.setOut(outputInfo.recorderStream);
        System.setProperty("com.strobel.reflection.emit.TypeBuilder.DumpGeneratedClasses", "true");
    }

    @After
    public void tearDown() throws Throwable {
        final OutputInfo outputInfo = THREAD_OUT.get();
        outputInfo.recorder.reset();
        System.setOut(outputInfo.systemStream);
        outputInfo.systemStream = null;
        final Queue<Object> queue = OUTPUT_QUEUE.get();
        queue.clear();
    }

    @SuppressWarnings("PackageVisibleField")
    final static class OutputInfo {
        final Queue<Object> outputQueue;
        final OutputRecorder recorder;
        final PrintStream recorderStream;
        PrintStream systemStream;

        OutputInfo(final Queue<Object> outputQueue) {
            this.outputQueue = outputQueue;
            this.recorder = new OutputRecorder(System.out, outputQueue);
            this.recorderStream = new PrintStream(recorder);
        }
    }

    protected static void assertResultTrue(final Expression e) throws Throwable {
        assertTrue((boolean)lambda(e).compileHandle().invokeExact());
    }

    protected static void assertResultFalse(final Expression e) throws Throwable {
        assertFalse((boolean)lambda(e).compileHandle().invokeExact());
    }

    protected static <T> void assertResultEquals(final Expression e, final T result) throws Throwable {
        assertEquals(result, lambda(e).compileHandle().invoke());
    }

    protected static void assertResultEquals(final Expression e, final byte result) throws Throwable {
        assertEquals(result, (byte)lambda(e).compileHandle().invokeExact());
    }

    protected static void assertResultEquals(final Expression e, final char result) throws Throwable {
        assertEquals(result, (char)lambda(e).compileHandle().invokeExact());
    }

    protected static void assertResultEquals(final Expression e, final short result) throws Throwable {
        assertEquals(result, (short)lambda(e).compileHandle().invokeExact());
    }

    protected static void assertResultEquals(final Expression e, final int result) throws Throwable {
        assertEquals(result, (int)lambda(e).compileHandle().invokeExact());
    }

    protected static void assertResultEquals(final Expression e, final long result) throws Throwable {
        assertEquals(result, (long)lambda(e).compileHandle().invokeExact());
    }

    protected static void assertResultEquals(final Expression e, final float result) throws Throwable {
        assertEquals(result, (float)lambda(e).compileHandle().invokeExact(), Double.MIN_NORMAL);
    }

    protected static void assertResultEquals(final Expression e, final double result) throws Throwable {
        assertEquals(result, (double)lambda(e).compileHandle().invokeExact(), Double.MIN_NORMAL);
    }
}
