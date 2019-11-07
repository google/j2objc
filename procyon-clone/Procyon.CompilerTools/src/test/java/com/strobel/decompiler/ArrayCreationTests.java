/*
 * ArrayCreationTests.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler;

import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;

public class ArrayCreationTests extends DecompilerTest {
    private static class A {
        public int[] test() {
            return new int[3];
        }
    }

    private static class B {
        public int[][] test() {
            return new int[][] { { 1, 2, 3 }, { 4, 5, 6 } };
        }
    }

    private static class C {
        public int[][] test() {
            return new int[3][2];
        }
    }

    private static class D {
        public int[][] test() {
            return new int[10][];
        }
    }

    private static class E {
        public int[][][][] test() {
            return new int[3][4][5][];
        }
    }

    private static class F {
        public void test() {
            final Object[] a = { null, null, new Object() };
            final int[] b = { 1, 2, 3 };
            final int[][] c = { { 1, 2, 3 }, { 4, 5, 6 } };
            final int[][][] d = { { { 1, 2, 3, 4 }, { 1, 2, 3, 4 } } };
            final byte[] e = { 100 };
            final double[][][] g = new double[3][4][5];
            final byte[][][][] h = new byte[3][4][5][6];
            final byte[][][][] i = new byte[3][4][5][];
        }
    }

    private static class G {
        public void test(final String[] args) {
            final Object[] x = new Cloneable[4][];
            final Serializable[][] y = new Serializable[4][2];

            y[3] = args;
            x[1] = y;
            y[2][1] = x;

            System.out.println(Arrays.deepToString(x));
            System.out.println(Arrays.deepToString(y));
            y[3][0] = x;

            final long[] z = new long[1];
            z[0] = (long) -(int) z[~+~0];
        }
    }

    private static class H {
        int[] arr;

        public void test(final boolean b) {
            this.arr = b ? new int[] { 1, 2 } : new int[] { 1 };
        }
    }

    private static class I {
        public void test() {
            final int[] a = { valueB() ? 1 : 0 };
            final int[] b = { valueB() ? 0 : 1, valueB() ? 1 : 0 };
            final int[][] c = { { valueB() ? 0 : 1, 15 }, { valueB() ? 1 : 0, valueB() ? 1 : 0 }, null };
            final int[][][] d = { { { valueB() ? 0 : 1, valueB() ? 1 : 0 }, { valueB() ? 0 : 1 } } };
        }

        private boolean valueB() {
            return true;
        }
    }

    @Test
    public void testSimpleArrayCreation() {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    public int[] test() {\n" +
            "        return new int[3];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testJaggedArrayInitialization() {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static class B {\n" +
            "    public int[][] test() {\n" +
            "        return new int[][] { { 1, 2, 3 }, { 4, 5, 6 } };\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testMultiDimensionalArrayCreation() {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static class C {\n" +
            "    public int[][] test() {\n" +
            "        return new int[3][2];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testJaggedArrayCreationWithSingleDimensionSize() {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static class D {\n" +
            "    public int[][] test() {\n" +
            "        return new int[10][];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testMultiDimensionalArrayCreationWithPartialDimensions() {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    public int[][][][] test() {\n" +
            "        return new int[3][4][5][];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testArrayVariableInitializers() {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static class F {\n" +
            "    public void test() {\n" +
            "        final Object[] a = { null, null, new Object() };\n" +
            "        final int[] b = { 1, 2, 3 };\n" +
            "        final int[][] c = { { 1, 2, 3 }, { 4, 5, 6 } };\n" +
            "        final int[][][] d = { { { 1, 2, 3, 4 }, { 1, 2, 3, 4 } } };\n" +
            "        final byte[] e = { 100 };\n" +
            "        final double[][][] g = new double[3][4][5];\n" +
            "        final byte[][][][] h = new byte[3][4][5][6];\n" +
            "        final byte[][][][] i = new byte[3][4][5][];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testVarianceAndSelfReferencingAssignments() {
        verifyOutput(
            G.class,
            defaultSettings(),
            "private static class G {\n" +
            "    public void test(final String[] args) {\n" +
            "        final Object[] x = new Cloneable[4][];\n" +
            "        final Serializable[][] y = new Serializable[4][2];\n" +
            "        y[3] = args;\n" +
            "        x[1] = y;\n" +
            "        y[2][1] = x;\n" +
            "        System.out.println(Arrays.deepToString(x));\n" +
            "        System.out.println(Arrays.deepToString(y));\n" +
            "        y[3][0] = x;\n" +
            "        final long[] z = { 0L };\n" +
            "        z[0] = -(int)z[0];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testArrayInitExpressionsWithinTernaryOperator() {
        verifyOutput(
            H.class,
            defaultSettings(),
            "private static class H {\n" +
            "    int[] arr;\n" +
            "    public void test(final boolean b) {\n" +
            "        this.arr = (b ? new int[] { 1, 2 } : new int[] { 1 });\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testIntArrayInitializationFromBoolean() {
        verifyOutput(
            I.class,
            defaultSettings(),
            "private static class I {\n" +
            "    public void test() {\n" +
            "        final int[] a = { this.valueB() ? 1 : 0 };\n" +
            "        final int[] b = { this.valueB() ? 0 : 1, this.valueB() ? 1 : 0 };\n" +
            "        final int[][] c = { { this.valueB() ? 0 : 1, 15 }, { this.valueB() ? 1 : 0, this.valueB() ? 1 : 0 }, null };\n" +
            "        final int[][][] d = { { { this.valueB() ? 0 : 1, this.valueB() ? 1 : 0 }, { this.valueB() ? 0 : 1 } } };\n" +
            "    }\n" +
            "    private boolean valueB() {\n" +
            "        return true;\n" +
            "    }\n" +
            "}\n"
        );
    }
}
