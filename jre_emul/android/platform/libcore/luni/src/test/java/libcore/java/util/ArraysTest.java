/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package libcore.java.util;

import java.util.Arrays;
import java.util.Random;

public class ArraysTest extends junit.framework.TestCase {

    /**
     * java.util.Arrays#setAll(int[], java.util.function.IntUnaryOperator)
     */
    public void test_setAll$I() {
        int[] list = new int[3];
        list[0] = 0;
        list[1] = 1;
        list[2] = 2;

        Arrays.setAll(list, x -> x + 1);
        assertEquals(1, list[0]);
        assertEquals(2, list[1]);
        assertEquals(3, list[2]);

        try {
            Arrays.setAll(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.setAll((int[]) null, (x -> x + 1));
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Arrays#parallelSetAll(int[], java.util.function.IntUnaryOperator)
     */
    public void test_parallelSetAll$I() {
        int[] list = new int[3];
        list[0] = 0;
        list[1] = 1;
        list[2] = 2;

        Arrays.parallelSetAll(list, x -> x + 1);
        assertEquals(1, list[0]);
        assertEquals(2, list[1]);
        assertEquals(3, list[2]);

        try {
            Arrays.parallelSetAll(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelSetAll((int[]) null, (x -> x + 1));
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Arrays#setAll(long[], java.util.function.IntToLongFunction)
     */
    public void test_setAll$L() {
        long[] list = new long[3];
        list[0] = 0;
        list[1] = 1;
        list[2] = 2;

        Arrays.setAll(list, x -> x + 1);
        assertEquals(1, list[0]);
        assertEquals(2, list[1]);
        assertEquals(3, list[2]);

        try {
            Arrays.setAll(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.setAll((long[]) null, (x -> x + 1));
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Arrays#parallelSetAll(long[], java.util.function.IntToLongFunction)
     */
    public void test_parallelSetAll$L() {
        long[] list = new long[3];
        list[0] = 0;
        list[1] = 1;
        list[2] = 2;

        Arrays.parallelSetAll(list, x -> x + 1);
        assertEquals(1, list[0]);
        assertEquals(2, list[1]);
        assertEquals(3, list[2]);

        try {
            Arrays.parallelSetAll(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelSetAll((long[]) null, (x -> x + 1));
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Arrays#setAll(double[], java.util.function.IntToDoubleFunction)
     */
    public void test_setAll$D() {
        double[] list = new double[3];
        list[0] = 0.0d;
        list[1] = 1.0d;
        list[2] = 2.0d;

        Arrays.setAll(list, x -> x + 0.5);
        assertEquals(0.5d, list[0]);
        assertEquals(1.5d, list[1]);
        assertEquals(2.5d, list[2]);

        try {
            Arrays.setAll(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.setAll((double[]) null, x -> x + 0.5);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Arrays#parallelSetAll(double[], java.util.function.IntToDoubleFunction)
     */
    public void test_parallelSetAll$D() {
        double[] list = new double[3];
        list[0] = 0.0d;
        list[1] = 1.0d;
        list[2] = 2.0d;

        Arrays.parallelSetAll(list, x -> x + 0.5);
        assertEquals(0.5d, list[0]);
        assertEquals(1.5d, list[1]);
        assertEquals(2.5d, list[2]);

        try {
            Arrays.parallelSetAll(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelSetAll((double[]) null, x -> x + 0.5);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Array#setAll(T[], java.util.function.IntFunction<\? extends T>)
     */
    public void test_setAll$T() {
        String[] strings = new String[3];
        strings[0] = "a";
        strings[0] = "b";
        strings[0] = "c";

        Arrays.setAll(strings, x -> "a" + x);
        assertEquals("a0", strings[0]);
        assertEquals("a1", strings[1]);
        assertEquals("a2", strings[2]);

        try {
            Arrays.setAll(strings, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.setAll((String[]) null, x -> "a" + x);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Array#parallelSetAll(T[], java.util.function.IntFunction<\? extends T>)
     */
    public void test_parallelSetAll$T() {
        String[] strings = new String[3];
        strings[0] = "a";
        strings[0] = "b";
        strings[0] = "c";

        Arrays.parallelSetAll(strings, x -> "a" + x);
        assertEquals("a0", strings[0]);
        assertEquals("a1", strings[1]);
        assertEquals("a2", strings[2]);

        try {
            Arrays.parallelSetAll(strings, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelSetAll((String[]) null, x -> "a" + x);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Array#parallelPrefix(int[], java.util.function.IntBinaryOperator)
     */
    public void test_parallelPrefix$I() {
        // Get an arbitrary array of ints.
        Random rand = new Random(0);
        int[] list = new int[1000];
        for(int i = 0; i < list.length; ++i) {
            list[i] = rand.nextInt() % 1000; // Prevent overflow
        }

        int[] seqResult = list.clone();

        // Sequential solution
        for(int i = 0; i < seqResult.length - 1; ++i) {
            seqResult[i + 1] += seqResult[i];
        }

        Arrays.parallelPrefix(list, (x, y) -> x + y);
        assertTrue(Arrays.equals(seqResult, list));

        try {
            Arrays.parallelPrefix(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix((int[]) null, (x, y) -> x + y);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Array#parallelPrefix(int[], int, int, java.util.function.IntBinaryOperator)
     */
    public void test_parallelPrefix$III() {
        // Get an arbitrary array of ints.
        Random rand = new Random(0);
        int[] list = new int[1000];
        for(int i = 0; i < list.length; ++i) {
            list[i] = rand.nextInt() % 1000; // Prevent overflow
        }

        int begin = 100, end = 500;
        int[] seqResult = list.clone();

        // Sequential solution
        for(int i = begin; i < end - 1; ++i) {
            seqResult[i + 1] += seqResult[i];
        }

        Arrays.parallelPrefix(list, begin, end, (x, y) -> x + y);
        assertTrue(Arrays.equals(seqResult, list));

        try {
            Arrays.parallelPrefix(list, begin, end, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix((int[]) null, begin, end, (x, y) -> x + y);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix(list, end, begin, (x, y) -> x + y);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * java.util.Array#parallelPrefix(long[], java.util.function.LongBinaryOperator)
     */
    public void test_parallelPrefix$L() {
        // Get an arbitrary array of ints.
        Random rand = new Random(0);
        long[] list = new long[1000];
        for(int i = 0; i < list.length; ++i) {
            list[i] = rand.nextLong() % 1000000; // Prevent overflow
        }

        long[] seqResult = list.clone();

        // Sequential solution
        for(int i = 0; i < seqResult.length - 1; ++i) {
            seqResult[i + 1] += seqResult[i];
        }

        Arrays.parallelPrefix(list, (x, y) -> x + y);
        assertTrue(Arrays.equals(seqResult, list));

        try {
            Arrays.parallelPrefix(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix((long[]) null, (x, y) -> x + y);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Array#parallelPrefix(long[], int, int, java.util.function.LongBinaryOperator)
     */
    public void test_parallelPrefix$LII() {
        // Get an arbitrary array of ints.
        Random rand = new Random(0);
        long[] list = new long[1000];
        for(int i = 0; i < list.length; ++i) {
            list[i] = rand.nextLong() % 1000000; // Prevent overflow
        }

        int begin = 100, end = 500;
        long[] seqResult = list.clone();

        // Sequential solution
        for(int i = begin; i < end - 1; ++i) {
            seqResult[i + 1] += seqResult[i];
        }

        Arrays.parallelPrefix(list, begin, end, (x, y) -> x + y);
        assertTrue(Arrays.equals(seqResult, list));

        try {
            Arrays.parallelPrefix(list, begin, end, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix((long[]) null, begin, end, (x, y) -> x + y);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix(list, end, begin, (x, y) -> x + y);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * java.util.Array#parallelPrefix(double[], java.util.function.DoubleBinaryOperator)
     */
    public void test_parallelPrefix$D() {
        // Get an arbitrary array of ints.
        Random rand = new Random(0);
        double[] list = new double[1000];
        for(int i = 0; i < list.length; ++i) {
            list[i] = rand.nextDouble() * 1000;
        }

        double[] seqResult = list.clone();

        // Sequential solution
        for(int i = 0; i < seqResult.length - 1; ++i) {
            seqResult[i + 1] += seqResult[i];
        }

        Arrays.parallelPrefix(list, (x, y) -> x + y);

        // Parallel double arithmetic contains error, reduce to integer for comparison.
        int[] listInInt = Arrays.stream(list).mapToInt(x -> (int) x).toArray();
        int[] seqResultInInt = Arrays.stream(seqResult).mapToInt(x -> (int) x).toArray();
        assertTrue(Arrays.equals(seqResultInInt, listInInt));

        try {
            Arrays.parallelPrefix(list, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix((double[]) null, (x, y) -> x + y);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Array#parallelPrefix(double[], int, int, java.util.function.DoubleBinaryOperator)
     */
    public void test_parallelPrefix$DII() {
        // Get an arbitrary array of ints.
        Random rand = new Random(0);
        double[] list = new double[1000];
        for(int i = 0; i < list.length; ++i) {
            list[i] = rand.nextDouble() * 1000;
        }

        int begin = 100, end = 500;
        double[] seqResult = list.clone();

        // Sequential solution
        for(int i = begin; i < end - 1; ++i) {
            seqResult[i + 1] += seqResult[i];
        }

        Arrays.parallelPrefix(list, begin, end, (x, y) -> x + y);

        // Parallel double arithmetic contains error, reduce to integer for comparison.
        int[] listInInt = Arrays.stream(list).mapToInt(x -> (int) x).toArray();
        int[] seqResultInInt = Arrays.stream(seqResult).mapToInt(x -> (int) x).toArray();
        assertTrue(Arrays.equals(seqResultInInt, listInInt));

        try {
            Arrays.parallelPrefix(list, begin, end, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix((double[]) null, begin, end, (x, y) -> x + y);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix(list, end, begin, (x, y) -> x + y);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * java.util.Array#parallelPrefix(T[], java.util.function.BinaryOperator<T>)
     */
    public void test_parallelPrefix$T() {
        String[] strings = new String[3];
        strings[0] = "a";
        strings[1] = "b";
        strings[2] = "c";

        Arrays.parallelPrefix(strings, (x, y) -> x + y);
        assertEquals("a", strings[0]);
        assertEquals("ab", strings[1]);
        assertEquals("abc", strings[2]);

        try {
            Arrays.parallelPrefix(strings, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix((String[]) null, (x, y) -> x + y);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Array#parallelPrefix(T[], int, int, java.util.function.BinaryOperator<T>)
     */
    public void test_parallelPrefix$TII() {
        String[] strings = new String[5];
        strings[0] = "a";
        strings[1] = "b";
        strings[2] = "c";
        strings[3] = "d";
        strings[4] = "e";
        int begin = 1, end = 4;

        Arrays.parallelPrefix(strings, begin, end, (x, y) -> x + y);
        assertEquals("a", strings[0]);
        assertEquals("b", strings[1]);
        assertEquals("bc", strings[2]);
        assertEquals("bcd", strings[3]);
        assertEquals("e", strings[4]);

        try {
            Arrays.parallelPrefix(strings, begin, end, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix((String[]) null, begin, end, (x, y) -> x + y);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            Arrays.parallelPrefix(strings, end, begin, (x, y) -> x + y);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }
}
