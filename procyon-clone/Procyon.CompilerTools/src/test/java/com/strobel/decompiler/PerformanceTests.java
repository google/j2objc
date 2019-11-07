/*
 * PerformanceTests.java
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

package com.strobel.decompiler;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class PerformanceTests extends DecompilerTest {
    private static class A {
        private static long f() {
            return 1000L;
        }

        public static long test() {
            return f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f() +
                   f();
        }
    }

    @Test
    public void testComplexBinaryExpressionTypeInferencePerformance() {
        //
        // Procyon had an obscene complexity issue with type inference on binary expressions,
        // causing long, multi-operand expressions to take a very long time to evaluate.
        // Prior to the fix, this test would take a very long time to run, so as long as it
        // finishes within several seconds, this test will pass.
        //

        final long startTime = System.nanoTime();

        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {" +
            "    private static long f() {" +
            "        return 1000L;" +
            "    }" +
            "    public static long test() {" +
            "        return f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f() +" +
            "               f();" +
            "    }\n" +
            "}"
        );

        final long endTime = System.nanoTime();
        final long totalSeconds = TimeUnit.NANOSECONDS.toSeconds(endTime - startTime);

        Assert.assertTrue(
            "Class took more than 5 seconds to decompile!  " +
            "Check type inference of binary expressions.",
            totalSeconds < 5L
        );
    }
}
