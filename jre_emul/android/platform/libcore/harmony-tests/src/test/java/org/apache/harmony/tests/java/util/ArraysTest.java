/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.tests.java.util;

import libcore.java.util.SpliteratorTester;
import tests.support.Support_UnmodifiableCollectionTest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class ArraysTest extends junit.framework.TestCase {

    public static class ReversedIntegerComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return -(((Integer) o1).compareTo((Integer) o2));
        }

        public boolean equals(Object o1, Object o2) {
            return ((Integer) o1).compareTo((Integer) o2) == 0;
        }
    }

    static class MockComparable implements Comparable {
        public int compareTo(Object o) {
            return 0;
        }
    }

    final static int arraySize = 100;

    Object[] objArray;

    boolean[] booleanArray;

    byte[] byteArray;

    char[] charArray;

    double[] doubleArray;

    float[] floatArray;

    int[] intArray;

    long[] longArray;

    Object[] objectArray;

    short[] shortArray;

    /**
     * java.util.Arrays#asList(java.lang.Object[])
     */
    public void test_asList$Ljava_lang_Object() {
        // Test for method java.util.List
        // java.util.Arrays.asList(java.lang.Object [])
        List convertedList = Arrays.asList(objectArray);
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Array and List converted from array do not contain identical elements",
                    convertedList.get(counter) == objectArray[counter]);
        }
        convertedList.set(50, new Integer(1000));
        assertTrue("set/get did not work on coverted list", convertedList.get(
                50).equals(new Integer(1000)));
        convertedList.set(50, new Integer(50));
        new Support_UnmodifiableCollectionTest("", convertedList).runTest();

        Object[] myArray = (Object[]) (objectArray.clone());
        myArray[30] = null;
        myArray[60] = null;
        convertedList = Arrays.asList(myArray);
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Array and List converted from array do not contain identical elements",
                    convertedList.get(counter) == myArray[counter]);
        }

        try {
            Arrays.asList((Object[]) null);
            fail("asList with null arg didn't throw NPE");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(byte[], byte)
     */
    public void test_binarySearch$BB() {
        // Test for method int java.util.Arrays.binarySearch(byte [], byte)
        for (byte counter = 0; counter < arraySize; counter++)
            assertTrue("Binary search on byte[] answered incorrect position",
                    Arrays.binarySearch(byteArray, counter) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(intArray, (byte) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(intArray, (byte) arraySize) == -(arraySize + 1));
        for (byte counter = 0; counter < arraySize; counter++)
            byteArray[counter] -= 50;
        for (byte counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on byte[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(byteArray, (byte) (counter - 50)) == counter);
    }

    /**
     * java.util.Arrays#binarySearch(char[], char)
     */
    public void test_binarySearch$CC() {
        // Test for method int java.util.Arrays.binarySearch(char [], char)
        for (char counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on char[] answered incorrect position",
                    Arrays.binarySearch(charArray, (char) (counter + 1)) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(charArray, '\u0000'));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(charArray, (char) (arraySize + 1)) == -(arraySize + 1));
    }

    /**
     * java.util.Arrays#binarySearch(double[], double)
     */
    public void test_binarySearch$DD() {
        // Test for method int java.util.Arrays.binarySearch(double [], double)
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on double[] answered incorrect position",
                    Arrays.binarySearch(doubleArray, (double) counter) == (double) counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(doubleArray, (double) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(doubleArray, (double) arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++)
            doubleArray[counter] -= (double) 50;
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on double[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(doubleArray, (double) (counter - 50)) == (double) counter);

        double[] specials = new double[] { Double.NEGATIVE_INFINITY,
                -Double.MAX_VALUE, -2d, -Double.MIN_VALUE, -0d, 0d,
                Double.MIN_VALUE, 2d, Double.MAX_VALUE,
                Double.POSITIVE_INFINITY, Double.NaN };
        for (int i = 0; i < specials.length; i++) {
            int result = Arrays.binarySearch(specials, specials[i]);
            assertTrue(specials[i] + " invalid: " + result, result == i);
        }
        assertEquals("-1d", -4, Arrays.binarySearch(specials, -1d));
        assertEquals("1d", -8, Arrays.binarySearch(specials, 1d));

    }

    /**
     * java.util.Arrays#binarySearch(float[], float)
     */
    public void test_binarySearch$FF() {
        // Test for method int java.util.Arrays.binarySearch(float [], float)
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on float[] answered incorrect position",
                    Arrays.binarySearch(floatArray, (float) counter) == (float) counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(floatArray, (float) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(floatArray, (float) arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++)
            floatArray[counter] -= (float) 50;
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on float[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(floatArray, (float) counter - 50) == (float) counter);

        float[] specials = new float[] { Float.NEGATIVE_INFINITY,
                -Float.MAX_VALUE, -2f, -Float.MIN_VALUE, -0f, 0f,
                Float.MIN_VALUE, 2f, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
                Float.NaN };
        for (int i = 0; i < specials.length; i++) {
            int result = Arrays.binarySearch(specials, specials[i]);
            assertTrue(specials[i] + " invalid: " + result, result == i);
        }
        assertEquals("-1f", -4, Arrays.binarySearch(specials, -1f));
        assertEquals("1f", -8, Arrays.binarySearch(specials, 1f));
    }

    /**
     * java.util.Arrays#binarySearch(int[], int)
     */
    public void test_binarySearch$II() {
        // Test for method int java.util.Arrays.binarySearch(int [], int)
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Binary search on int[] answered incorrect position",
                    Arrays.binarySearch(intArray, counter) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(intArray, -1));
        assertTrue("Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(intArray, arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++)
            intArray[counter] -= 50;
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on int[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(intArray, counter - 50) == counter);
    }

    /**
     * java.util.Arrays#binarySearch(long[], long)
     */
    public void test_binarySearch$JJ() {
        // Test for method int java.util.Arrays.binarySearch(long [], long)
        for (long counter = 0; counter < arraySize; counter++)
            assertTrue("Binary search on long[] answered incorrect position",
                    Arrays.binarySearch(longArray, counter) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(longArray, (long) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(longArray, (long) arraySize) == -(arraySize + 1));
        for (long counter = 0; counter < arraySize; counter++)
            longArray[(int) counter] -= (long) 50;
        for (long counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on long[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(longArray, counter - (long) 50) == counter);
    }

    /**
     * java.util.Arrays#binarySearch(java.lang.Object[],
     *        java.lang.Object)
     */
    public void test_binarySearch$Ljava_lang_ObjectLjava_lang_Object() {
        // Test for method int java.util.Arrays.binarySearch(java.lang.Object
        // [], java.lang.Object)
        assertEquals(
                "Binary search succeeded for non-comparable value in empty array",
                -1, Arrays.binarySearch(new Object[] {}, new Object()));
        assertEquals(
                "Binary search succeeded for comparable value in empty array",
                -1, Arrays.binarySearch(new Object[] {}, new Integer(-1)));
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on Object[] answered incorrect position",
                    Arrays.binarySearch(objectArray, objArray[counter]) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(objectArray, new Integer(-1)));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(objectArray, new Integer(arraySize)) == -(arraySize + 1));

        Object object = new Object();
        Object[] objects = new MockComparable[] { new MockComparable() };
        assertEquals("Should always return 0", 0, Arrays.binarySearch(objects, object));

        Object[] string_objects = new String[] { "one" };
        try {
            Arrays.binarySearch(string_objects, object);
            fail("No expected ClassCastException");
        } catch (ClassCastException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(java.lang.Object[],
     *        java.lang.Object, java.util.Comparator)
     */
    public void test_binarySearch$Ljava_lang_ObjectLjava_lang_ObjectLjava_util_Comparator() {
        // Test for method int java.util.Arrays.binarySearch(java.lang.Object
        // [], java.lang.Object, java.util.Comparator)
        Comparator comp = new ReversedIntegerComparator();
        for (int counter = 0; counter < arraySize; counter++)
            objectArray[counter] = objArray[arraySize - counter - 1];
        assertTrue(
                "Binary search succeeded for value not present in array 1",
                Arrays.binarySearch(objectArray, new Integer(-1), comp) == -(arraySize + 1));
        assertEquals("Binary search succeeded for value not present in array 2",
                -1, Arrays.binarySearch(objectArray, new Integer(arraySize), comp));
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on Object[] with custom comparator answered incorrect position",
                    Arrays.binarySearch(objectArray, objArray[counter], comp) == arraySize
                            - counter - 1);
    }

    /**
     * java.util.Arrays#binarySearch(short[], short)
     */
    public void test_binarySearch$SS() {
        // Test for method int java.util.Arrays.binarySearch(short [], short)
        for (short counter = 0; counter < arraySize; counter++)
            assertTrue("Binary search on short[] answered incorrect position",
                    Arrays.binarySearch(shortArray, counter) == counter);
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(intArray, (short) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(intArray, (short) arraySize) == -(arraySize + 1));
        for (short counter = 0; counter < arraySize; counter++)
            shortArray[counter] -= 50;
        for (short counter = 0; counter < arraySize; counter++)
            assertTrue(
                    "Binary search on short[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(shortArray, (short) (counter - 50)) == counter);
    }

    public void test_Arrays_binaraySearch_byte() {
        assertEquals(-1, Arrays.binarySearch(new byte[] { '0' }, 0, 0,
                (byte) '1'));
        assertEquals(-2, Arrays.binarySearch(new byte[] { '0' }, 1, 1,
                (byte) '1'));
        assertEquals(-2, Arrays.binarySearch(new byte[] { '0', '1' }, 1, 1,
                (byte) '2'));
        assertEquals(-3, Arrays.binarySearch(new byte[] { '0', '1' }, 2, 2,
                (byte) '2'));
    }

    public void test_Arrays_binaraySearch_char() {
        assertEquals(-1, Arrays.binarySearch(new char[] { '0' }, 0, 0, '1'));
        assertEquals(-2, Arrays.binarySearch(new char[] { '0' }, 1, 1, '1'));
        assertEquals(-2, Arrays
                .binarySearch(new char[] { '0', '1' }, 1, 1, '2'));
        assertEquals(-3, Arrays
                .binarySearch(new char[] { '0', '1' }, 2, 2, '2'));
    }

    public void test_Arrays_binaraySearch_float() {
        assertEquals(-1, Arrays.binarySearch(new float[] { -1.0f }, 0, 0, 0.0f));
        assertEquals(-2, Arrays.binarySearch(new float[] { -1.0f }, 1, 1, 0.0f));
        assertEquals(-2, Arrays.binarySearch(new float[] { -1.0f, 0f }, 1, 1,
                1f));
        assertEquals(-3, Arrays.binarySearch(new float[] { -1.0f, 0f }, 2, 2,
                1f));
    }

    public void test_Arrays_binaraySearch_double() {
        assertEquals(-1, Arrays.binarySearch(new double[] { -1.0 }, 0, 0, 0.0));
        assertEquals(-2, Arrays.binarySearch(new double[] { -1.0 }, 1, 1, 0.0));
        assertEquals(-2, Arrays.binarySearch(new double[] { -1.0, 0 }, 1, 1, 1));
        assertEquals(-3, Arrays.binarySearch(new double[] { -1.0, 0 }, 2, 2, 1));
    }

    public void test_Arrays_binaraySearch_int() {
        assertEquals(-1, Arrays.binarySearch(new int[] { -1 }, 0, 0, 0));
        assertEquals(-2, Arrays.binarySearch(new int[] { -1 }, 1, 1, 0));
        assertEquals(-2, Arrays.binarySearch(new int[] { -1, 0 }, 1, 1, 1));
        assertEquals(-3, Arrays.binarySearch(new int[] { -1, 0 }, 2, 2, 1));
    }

    public void test_Arrays_binaraySearch_long() {
        assertEquals(-1, Arrays.binarySearch(new long[] { -1l }, 0, 0, 0l));
        assertEquals(-2, Arrays.binarySearch(new long[] { -1l }, 1, 1, 0l));
        assertEquals(-2, Arrays.binarySearch(new long[] { -1l, 0l }, 1, 1, 1l));
        assertEquals(-3, Arrays.binarySearch(new long[] { -1l, 0l }, 2, 2, 1l));
    }

    public void test_Arrays_binaraySearch_short() {
        assertEquals(-1, Arrays.binarySearch(new short[] { (short) -1 }, 0, 0,
                (short) 0));
        assertEquals(-2, Arrays.binarySearch(new short[] { (short) -1 }, 1, 1,
                (short) 0));
        assertEquals(-2, Arrays.binarySearch(new short[] { (short) -1,
                (short) 0 }, 1, 1, (short) 1));
        assertEquals(-3, Arrays.binarySearch(new short[] { (short) -1,
                (short) 0 }, 2, 2, (short) 1));
    }

    public void test_Arrays_binaraySearch_Object() {
        assertEquals(-1, Arrays.binarySearch(new Object[] { new Integer(-1) },
                0, 0, new Integer(0)));
        assertEquals(-2, Arrays.binarySearch(new Object[] { new Integer(-1) },
                1, 1, new Integer(0)));
        assertEquals(-2, Arrays.binarySearch(new Object[] { new Integer(-1),
                new Integer(0) }, 1, 1, new Integer(1)));
        assertEquals(-3, Arrays.binarySearch(new Object[] { new Integer(-1),
                new Integer(0) }, 2, 2, new Integer(1)));
    }

    public void test_Arrays_binaraySearch_T() {
        ReversedIntegerComparator reversedComparator = new ReversedIntegerComparator();
        assertEquals(-1, Arrays.binarySearch(new Integer[] { new Integer(-1) },
                0, 0, new Integer(0), reversedComparator));
        assertEquals(-2, Arrays.binarySearch(new Integer[] { new Integer(-1) },
                1, 1, new Integer(0), reversedComparator));
        assertEquals(-2, Arrays.binarySearch(new Integer[] { new Integer(-1),
                new Integer(0) }, 1, 1, new Integer(1), reversedComparator));
        assertEquals(-3, Arrays.binarySearch(new Integer[] { new Integer(-1),
                new Integer(0) }, 2, 2, new Integer(1), reversedComparator));
    }

    /**
     * java.util.Arrays#fill(byte[], byte)
     */
    public void test_fill$BB() {
        // Test for method void java.util.Arrays.fill(byte [], byte)

        byte d[] = new byte[1000];
        Arrays.fill(d, Byte.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill byte array correctly",
                    d[i] == Byte.MAX_VALUE);
    }

    /**
     * java.util.Arrays#fill(byte[], int, int, byte)
     */
    public void test_fill$BIIB() {
        // Test for method void java.util.Arrays.fill(byte [], int, int, byte)
        byte val = Byte.MAX_VALUE;
        byte d[] = new byte[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill byte array correctly", d[i] == val);

        int result;
        try {
            Arrays.fill(new byte[2], 2, 1, (byte) 27);
            result = 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            result = 1;
        } catch (IllegalArgumentException e) {
            result = 2;
        }
        assertEquals("Wrong exception1", 2, result);
        try {
            Arrays.fill(new byte[2], -1, 1, (byte) 27);
            result = 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            result = 1;
        } catch (IllegalArgumentException e) {
            result = 2;
        }
        assertEquals("Wrong exception2", 1, result);
        try {
            Arrays.fill(new byte[2], 1, 4, (byte) 27);
            result = 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            result = 1;
        } catch (IllegalArgumentException e) {
            result = 2;
        }
        assertEquals("Wrong exception", 1, result);
    }

    /**
     * java.util.Arrays#fill(short[], short)
     */
    public void test_fill$SS() {
        // Test for method void java.util.Arrays.fill(short [], short)

        short d[] = new short[1000];
        Arrays.fill(d, Short.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill short array correctly",
                    d[i] == Short.MAX_VALUE);
    }

    /**
     * java.util.Arrays#fill(short[], int, int, short)
     */
    public void test_fill$SIIS() {
        // Test for method void java.util.Arrays.fill(short [], int, int, short)
        short val = Short.MAX_VALUE;
        short d[] = new short[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill short array correctly", d[i] == val);

        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#fill(char[], char)
     */
    public void test_fill$CC() {
        // Test for method void java.util.Arrays.fill(char [], char)

        char d[] = new char[1000];
        Arrays.fill(d, 'V');
        for (int i = 0; i < d.length; i++)
            assertEquals("Failed to fill char array correctly", 'V', d[i]);
    }

    /**
     * java.util.Arrays#fill(char[], int, int, char)
     */
    public void test_fill$CIIC() {
        // Test for method void java.util.Arrays.fill(char [], int, int, char)
        char val = 'T';
        char d[] = new char[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill char array correctly", d[i] == val);

        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#fill(int[], int)
     */
    public void test_fill$II() {
        // Test for method void java.util.Arrays.fill(int [], int)

        int d[] = new int[1000];
        Arrays.fill(d, Integer.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill int array correctly",
                    d[i] == Integer.MAX_VALUE);
    }

    /**
     * java.util.Arrays#fill(int[], int, int, int)
     */
    public void test_fill$IIII() {
        // Test for method void java.util.Arrays.fill(int [], int, int, int)
        int val = Integer.MAX_VALUE;
        int d[] = new int[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill int array correctly", d[i] == val);

        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#fill(long[], long)
     */
    public void test_fill$JJ() {
        // Test for method void java.util.Arrays.fill(long [], long)

        long d[] = new long[1000];
        Arrays.fill(d, Long.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill long array correctly",
                    d[i] == Long.MAX_VALUE);
    }

    /**
     * java.util.Arrays#fill(long[], int, int, long)
     */
    public void test_fill$JIIJ() {
        // Test for method void java.util.Arrays.fill(long [], int, int, long)
        long d[] = new long[1000];
        Arrays.fill(d, 400, d.length, Long.MAX_VALUE);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == Long.MAX_VALUE));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill long array correctly",
                    d[i] == Long.MAX_VALUE);

        try {
            Arrays.fill(d, 10, 0, Long.MIN_VALUE);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            Arrays.fill(d, -10, 0, Long.MAX_VALUE);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.fill(d, 10, d.length+1, Long.MAX_VALUE);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#fill(float[], float)
     */
    public void test_fill$FF() {
        // Test for method void java.util.Arrays.fill(float [], float)
        float d[] = new float[1000];
        Arrays.fill(d, Float.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill float array correctly",
                    d[i] == Float.MAX_VALUE);
    }

    /**
     * java.util.Arrays#fill(float[], int, int, float)
     */
    public void test_fill$FIIF() {
        // Test for method void java.util.Arrays.fill(float [], int, int, float)
        float val = Float.MAX_VALUE;
        float d[] = new float[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill float array correctly", d[i] == val);

        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#fill(double[], double)
     */
    public void test_fill$DD() {
        // Test for method void java.util.Arrays.fill(double [], double)

        double d[] = new double[1000];
        Arrays.fill(d, Double.MAX_VALUE);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill double array correctly",
                    d[i] == Double.MAX_VALUE);
    }

    /**
     * java.util.Arrays#fill(double[], int, int, double)
     */
    public void test_fill$DIID() {
        // Test for method void java.util.Arrays.fill(double [], int, int,
        // double)
        double val = Double.MAX_VALUE;
        double d[] = new double[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill double array correctly", d[i] == val);

        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#fill(boolean[], boolean)
     */
    public void test_fill$ZZ() {
        // Test for method void java.util.Arrays.fill(boolean [], boolean)

        boolean d[] = new boolean[1000];
        Arrays.fill(d, true);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill boolean array correctly", d[i]);
    }

    /**
     * java.util.Arrays#fill(boolean[], int, int, boolean)
     */
    public void test_fill$ZIIZ() {
        // Test for method void java.util.Arrays.fill(boolean [], int, int,
        // boolean)
        boolean val = true;
        boolean d[] = new boolean[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill boolean array correctly", d[i] == val);

        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#fill(java.lang.Object[], java.lang.Object)
     */
    public void test_fill$Ljava_lang_ObjectLjava_lang_Object() {
        // Test for method void java.util.Arrays.fill(java.lang.Object [],
        // java.lang.Object)
        Object val = new Object();
        Object d[] = new Object[1000];
        Arrays.fill(d, 0, d.length, val);
        for (int i = 0; i < d.length; i++)
            assertTrue("Failed to fill Object array correctly", d[i] == val);
    }

    /**
     * java.util.Arrays#fill(java.lang.Object[], int, int,
     *        java.lang.Object)
     */
    public void test_fill$Ljava_lang_ObjectIILjava_lang_Object() {
        // Test for method void java.util.Arrays.fill(java.lang.Object [], int,
        // int, java.lang.Object)
        Object val = new Object();
        Object d[] = new Object[1000];
        Arrays.fill(d, 400, d.length, val);
        for (int i = 0; i < 400; i++)
            assertTrue("Filled elements not in range", !(d[i] == val));
        for (int i = 400; i < d.length; i++)
            assertTrue("Failed to fill Object array correctly", d[i] == val);

        Arrays.fill(d, 400, d.length, null);
        for (int i = 400; i < d.length; i++)
            assertNull("Failed to fill Object array correctly with nulls",
                    d[i]);

        try {
            Arrays.fill(d, 10, 0, val);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            Arrays.fill(d, -10, 0, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.fill(d, 10, d.length+1, val);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#equals(byte[], byte[])
     */
    public void test_equals$B$B() {
        // Test for method boolean java.util.Arrays.equals(byte [], byte [])
        byte d[] = new byte[1000];
        byte x[] = new byte[1000];
        Arrays.fill(d, Byte.MAX_VALUE);
        Arrays.fill(x, Byte.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Byte.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }

    /**
     * java.util.Arrays#equals(short[], short[])
     */
    public void test_equals$S$S() {
        // Test for method boolean java.util.Arrays.equals(short [], short [])
        short d[] = new short[1000];
        short x[] = new short[1000];
        Arrays.fill(d, Short.MAX_VALUE);
        Arrays.fill(x, Short.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Short.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }

    /**
     * java.util.Arrays#equals(char[], char[])
     */
    public void test_equals$C$C() {
        // Test for method boolean java.util.Arrays.equals(char [], char [])
        char d[] = new char[1000];
        char x[] = new char[1000];
        char c = 'T';
        Arrays.fill(d, c);
        Arrays.fill(x, 'L');
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, c);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }

    /**
     * java.util.Arrays#equals(int[], int[])
     */
    public void test_equals$I$I() {
        // Test for method boolean java.util.Arrays.equals(int [], int [])
        int d[] = new int[1000];
        int x[] = new int[1000];
        Arrays.fill(d, Integer.MAX_VALUE);
        Arrays.fill(x, Integer.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Integer.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));

        assertTrue("wrong result for null array1", !Arrays.equals(new int[2],
                null));
        assertTrue("wrong result for null array2", !Arrays.equals(null,
                new int[2]));
    }

    /**
     * java.util.Arrays#equals(long[], long[])
     */
    public void test_equals$J$J() {
        // Test for method boolean java.util.Arrays.equals(long [], long [])
        long d[] = new long[1000];
        long x[] = new long[1000];
        Arrays.fill(d, Long.MAX_VALUE);
        Arrays.fill(x, Long.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Long.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));

        assertTrue("should be false", !Arrays.equals(
                new long[] { 0x100000000L }, new long[] { 0x200000000L }));

    }

    /**
     * java.util.Arrays#equals(float[], float[])
     */
    public void test_equals$F$F() {
        // Test for method boolean java.util.Arrays.equals(float [], float [])
        float d[] = new float[1000];
        float x[] = new float[1000];
        Arrays.fill(d, Float.MAX_VALUE);
        Arrays.fill(x, Float.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Float.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));

        assertTrue("NaN not equals", Arrays.equals(new float[] { Float.NaN },
                new float[] { Float.NaN }));
        assertTrue("0f equals -0f", !Arrays.equals(new float[] { 0f },
                new float[] { -0f }));
    }

    /**
     * java.util.Arrays#equals(double[], double[])
     */
    public void test_equals$D$D() {
        // Test for method boolean java.util.Arrays.equals(double [], double [])
        double d[] = new double[1000];
        double x[] = new double[1000];
        Arrays.fill(d, Double.MAX_VALUE);
        Arrays.fill(x, Double.MIN_VALUE);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, Double.MAX_VALUE);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));

        assertTrue("should be false", !Arrays.equals(new double[] { 1.0 },
                new double[] { 2.0 }));

        assertTrue("NaN not equals", Arrays.equals(new double[] { Double.NaN },
                new double[] { Double.NaN }));
        assertTrue("0d equals -0d", !Arrays.equals(new double[] { 0d },
                new double[] { -0d }));
    }

    /**
     * java.util.Arrays#equals(boolean[], boolean[])
     */
    public void test_equals$Z$Z() {
        // Test for method boolean java.util.Arrays.equals(boolean [], boolean
        // [])
        boolean d[] = new boolean[1000];
        boolean x[] = new boolean[1000];
        Arrays.fill(d, true);
        Arrays.fill(x, false);
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, true);
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }

    /**
     * java.util.Arrays#equals(java.lang.Object[], java.lang.Object[])
     */
    public void test_equals$Ljava_lang_Object$Ljava_lang_Object() {
        // Test for method boolean java.util.Arrays.equals(java.lang.Object [],
        // java.lang.Object [])
        Object d[] = new Object[1000];
        Object x[] = new Object[1000];
        Object o = new Object();
        Arrays.fill(d, o);
        Arrays.fill(x, new Object());
        assertTrue("Inequal arrays returned true", !Arrays.equals(d, x));
        Arrays.fill(x, o);
        d[50] = null;
        x[50] = null;
        assertTrue("equal arrays returned false", Arrays.equals(d, x));
    }

    /**
     * java.util.Arrays#sort(byte[])
     */
    public void test_sort$B() {
        // Test for method void java.util.Arrays.sort(byte [])
        byte[] reversedArray = new byte[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (byte) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (byte) counter);
    }

    /**
     * java.util.Arrays#sort(byte[], int, int)
     */
    public void test_sort$BII() {
        // Test for method void java.util.Arrays.sort(byte [], int, int)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        byte[] reversedArray = new byte[arraySize];
        byte[] originalReversedArray = new byte[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (byte) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#sort(char[])
     */
    public void test_sort$C() {
        // Test for method void java.util.Arrays.sort(char [])
        char[] reversedArray = new char[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (char) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (char) counter);

    }

    /**
     * java.util.Arrays#sort(char[], int, int)
     */
    public void test_sort$CII() {
        // Test for method void java.util.Arrays.sort(char [], int, int)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        char[] reversedArray = new char[arraySize];
        char[] originalReversedArray = new char[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (char) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#sort(double[])
     */
    public void test_sort$D() {
        // Test for method void java.util.Arrays.sort(double [])
        double[] reversedArray = new double[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (double) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (double) counter);

        double[] specials1 = new double[] { Double.NaN, Double.MAX_VALUE,
                Double.MIN_VALUE, 0d, -0d, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY };
        double[] specials2 = new double[] { 0d, Double.POSITIVE_INFINITY, -0d,
                Double.NEGATIVE_INFINITY, Double.MIN_VALUE, Double.NaN,
                Double.MAX_VALUE };
        double[] specials3 = new double[] { 0.0, Double.NaN, 1.0, 2.0, Double.NaN,
                Double.NaN, 1.0, 3.0, -0.0 };
        double[] answer = new double[] { Double.NEGATIVE_INFINITY, -0d, 0d,
                Double.MIN_VALUE, Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                Double.NaN };
        double[] answer3 = new double[] { -0.0, 0.0, 1.0, 1.0, 2.0, 3.0, Double.NaN,
                Double.NaN, Double.NaN };

        Arrays.sort(specials1);
        Object[] print1 = new Object[specials1.length];
        for (int i = 0; i < specials1.length; i++)
            print1[i] = new Double(specials1[i]);
        assertTrue("specials sort incorrectly 1: " + Arrays.asList(print1),
                Arrays.equals(specials1, answer));

        Arrays.sort(specials2);
        Object[] print2 = new Object[specials2.length];
        for (int i = 0; i < specials2.length; i++)
            print2[i] = new Double(specials2[i]);
        assertTrue("specials sort incorrectly 2: " + Arrays.asList(print2),
                Arrays.equals(specials2, answer));

        Arrays.sort(specials3);
        Object[] print3 = new Object[specials3.length];
        for (int i = 0; i < specials3.length; i++)
            print3[i] = new Double(specials3[i]);
        assertTrue("specials sort incorrectly 3: " + Arrays.asList(print3),
                Arrays.equals(specials3, answer3));
    }

    /**
     * java.util.Arrays#sort(double[], int, int)
     */
    public void test_sort$DII() {
        // Test for method void java.util.Arrays.sort(double [], int, int)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        double[] reversedArray = new double[arraySize];
        double[] originalReversedArray = new double[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (double) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#sort(float[])
     */
    public void test_sort$F() {
        // Test for method void java.util.Arrays.sort(float [])
        float[] reversedArray = new float[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (float) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (float) counter);

        float[] specials1 = new float[] { Float.NaN, Float.MAX_VALUE,
                Float.MIN_VALUE, 0f, -0f, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY };
        float[] specials2 = new float[] { 0f, Float.POSITIVE_INFINITY, -0f,
                Float.NEGATIVE_INFINITY, Float.MIN_VALUE, Float.NaN,
                Float.MAX_VALUE };
        float[] answer = new float[] { Float.NEGATIVE_INFINITY, -0f, 0f,
                Float.MIN_VALUE, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
                Float.NaN };

        Arrays.sort(specials1);
        Object[] print1 = new Object[specials1.length];
        for (int i = 0; i < specials1.length; i++)
            print1[i] = new Float(specials1[i]);
        assertTrue("specials sort incorrectly 1: " + Arrays.asList(print1),
                Arrays.equals(specials1, answer));

        Arrays.sort(specials2);
        Object[] print2 = new Object[specials2.length];
        for (int i = 0; i < specials2.length; i++)
            print2[i] = new Float(specials2[i]);
        assertTrue("specials sort incorrectly 2: " + Arrays.asList(print2),
                Arrays.equals(specials2, answer));
    }

    /**
     * java.util.Arrays#sort(float[], int, int)
     */
    public void test_sort$FII() {
        // Test for method void java.util.Arrays.sort(float [], int, int)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        float[] reversedArray = new float[arraySize];
        float[] originalReversedArray = new float[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (float) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#sort(int[])
     */
    public void test_sort$I() {
        // Test for method void java.util.Arrays.sort(int [])
        int[] reversedArray = new int[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = arraySize - counter - 1;
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == counter);
    }

    /**
     * java.util.Arrays#sort(int[], int, int)
     */
    public void test_sort$III() {
        // Test for method void java.util.Arrays.sort(int [], int, int)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        int[] reversedArray = new int[arraySize];
        int[] originalReversedArray = new int[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = arraySize - counter - 1;
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#sort(long[])
     */
    public void test_sort$J() {
        // Test for method void java.util.Arrays.sort(long [])
        long[] reversedArray = new long[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (long) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (long) counter);

    }

    /**
     * java.util.Arrays#sort(long[], int, int)
     */
    public void test_sort$JII() {
        // Test for method void java.util.Arrays.sort(long [], int, int)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        long[] reversedArray = new long[arraySize];
        long[] originalReversedArray = new long[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (long) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#sort(java.lang.Object[])
     */
    public void test_sort$Ljava_lang_Object() {
        // Test for method void java.util.Arrays.sort(java.lang.Object [])
        Object[] reversedArray = new Object[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = objectArray[arraySize - counter - 1];
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == objectArray[counter]);

        Arrays.fill(reversedArray, 0, reversedArray.length/2, "String");
        Arrays.fill(reversedArray, reversedArray.length/2, reversedArray.length, new Integer(1));

        try {
            Arrays.sort(reversedArray);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#sort(java.lang.Object[], int, int)
     */
    public void test_sort$Ljava_lang_ObjectII() {
        // Test for method void java.util.Arrays.sort(java.lang.Object [], int,
        // int)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        Object[] reversedArray = new Object[arraySize];
        Object[] originalReversedArray = new Object[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = objectArray[arraySize - counter - 1];
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    ((Comparable) reversedArray[counter])
                            .compareTo(reversedArray[counter + 1]) <= 0);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        Arrays.fill(reversedArray, 0, reversedArray.length/2, "String");
        Arrays.fill(reversedArray, reversedArray.length/2, reversedArray.length, new Integer(1));

        try {
            Arrays.sort(reversedArray, reversedArray.length/4, 3*reversedArray.length/4);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }

        Arrays.sort(reversedArray, 0, reversedArray.length/4);
        Arrays.sort(reversedArray, 3*reversedArray.length/4, reversedArray.length);
    }

    /**
     * java.util.Arrays#sort(java.lang.Object[], int, int,
     *        java.util.Comparator)
     */
    public void test_sort$Ljava_lang_ObjectIILjava_util_Comparator() {
        // Test for method void java.util.Arrays.sort(java.lang.Object [], int,
        // int, java.util.Comparator)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        ReversedIntegerComparator comp = new ReversedIntegerComparator();
        Object[] originalArray = new Object[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            originalArray[counter] = objectArray[counter];
        Arrays.sort(objectArray, startIndex, endIndex, comp);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    objectArray[counter] == originalArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds", comp.compare(
                    objectArray[counter], objectArray[counter + 1]) <= 0);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    objectArray[counter] == originalArray[counter]);

        Arrays.fill(originalArray, 0, originalArray.length/2, "String");
        Arrays.fill(originalArray, originalArray.length/2, originalArray.length, new Integer(1));

        try {
            Arrays.sort(originalArray, startIndex, endIndex, comp);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }

        Arrays.sort(originalArray, endIndex, originalArray.length, comp);

        try {
            Arrays.sort(originalArray, endIndex, originalArray.length + 1, comp);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.sort(originalArray, -1, startIndex, comp);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            Arrays.sort(originalArray, originalArray.length, endIndex, comp);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException e) {
            //expected
        }
    }

    /**
     * java.util.Arrays#sort(java.lang.Object[], java.util.Comparator)
     */
    public void test_sort$Ljava_lang_ObjectLjava_util_Comparator() {
        // Test for method void java.util.Arrays.sort(java.lang.Object [],
        // java.util.Comparator)
        ReversedIntegerComparator comp = new ReversedIntegerComparator();
        Arrays.sort(objectArray, comp);
        for (int counter = 0; counter < arraySize - 1; counter++)
            assertTrue("Array not sorted correctly with custom comparator",
                    comp
                            .compare(objectArray[counter],
                                    objectArray[counter + 1]) <= 0);

        Arrays.fill(objectArray, 0, objectArray.length/2, "String");
        Arrays.fill(objectArray, objectArray.length/2, objectArray.length, new Integer(1));

        try {
            Arrays.sort(objectArray, comp);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    // Regression HARMONY-6076
    public void test_sort$Ljava_lang_ObjectLjava_util_Comparator_stable() {
        Element[] array = new Element[11];
        array[0] = new Element(122);
        array[1] = new Element(146);
        array[2] = new Element(178);
        array[3] = new Element(208);
        array[4] = new Element(117);
        array[5] = new Element(146);
        array[6] = new Element(173);
        array[7] = new Element(203);
        array[8] = new Element(56);
        array[9] = new Element(208);
        array[10] = new Element(96);

        Comparator<Element> comparator = new Comparator<Element>() {
            public int compare(Element object1, Element object2) {
                return object1.value - object2.value;
            }
        };

        Arrays.sort(array, comparator);

        for (int i = 1; i < array.length; i++) {
            assertTrue(comparator.compare(array[i - 1], array[i]) <= 0);
            if (comparator.compare(array[i - 1], array[i]) == 0) {
                assertTrue(array[i - 1].index < array[i].index);
            }
        }
    }

    public static class Element {
        public int value;

        public int index;

        private static int count = 0;

        public Element(int value) {
            this.value = value;
            index = count++;
        }
    }

    /**
     * java.util.Arrays#sort(short[])
     */
    public void test_sort$S() {
        // Test for method void java.util.Arrays.sort(short [])
        short[] reversedArray = new short[arraySize];
        for (int counter = 0; counter < arraySize; counter++)
            reversedArray[counter] = (short) (arraySize - counter - 1);
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < arraySize; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (short) counter);
    }

    /**
     * java.util.Arrays#sort(short[], int, int)
     */
    public void test_sort$SII() {
        // Test for method void java.util.Arrays.sort(short [], int, int)
        int startIndex = arraySize / 4;
        int endIndex = 3 * arraySize / 4;
        short[] reversedArray = new short[arraySize];
        short[] originalReversedArray = new short[arraySize];
        for (int counter = 0; counter < arraySize; counter++) {
            reversedArray[counter] = (short) (arraySize - counter - 1);
            originalReversedArray[counter] = reversedArray[counter];
        }
        Arrays.sort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                    reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                    reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.sort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.sort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.sort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#sort(byte[], int, int)
     */
    public void test_java_util_Arrays_sort_byte_array_NPE() {
        byte[] byte_array_null = null;
        try {
            java.util.Arrays.sort(byte_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(byte_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#sort(char[], int, int)
     */
    public void test_java_util_Arrays_sort_char_array_NPE() {
        char[] char_array_null = null;
        try {
            java.util.Arrays.sort(char_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(char_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#sort(double[], int, int)
     */
    public void test_java_util_Arrays_sort_double_array_NPE() {
        double[] double_array_null = null;
        try {
            java.util.Arrays.sort(double_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(double_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#sort(float[], int, int)
     */
    public void test_java_util_Arrays_sort_float_array_NPE() {
        float[] float_array_null = null;
        try {
            java.util.Arrays.sort(float_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(float_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#sort(int[], int, int)
     */
    public void test_java_util_Arrays_sort_int_array_NPE() {
        int[] int_array_null = null;
        try {
            java.util.Arrays.sort(int_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(int_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#sort(Object[], int, int)
     */
    public void test_java_util_Arrays_sort_object_array_NPE() {
        Object[] object_array_null = null;
        try {
            java.util.Arrays.sort(object_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(object_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(object_array_null, (int) -1, (int) 1, null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#sort(long[], int, int)
     */
    public void test_java_util_Arrays_sort_long_array_NPE() {
        long[] long_array_null = null;
        try {
            java.util.Arrays.sort(long_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(long_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.util.Arrays#sort(short[], int, int)
     */
    public void test_java_util_Arrays_sort_short_array_NPE() {
        short[] short_array_null = null;
        try {
            java.util.Arrays.sort(short_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            // Regression for HARMONY-378
            java.util.Arrays.sort(short_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // Lenghts of arrays to test in test_sort;
    private static final int[] LENGTHS = { 0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 100, 1000, 10000 };

    /**
     * java.util.Arrays#sort()
     */
    public void test_sort() {
        for (int len : LENGTHS) {
            PrimitiveTypeArrayBuilder.reset();
            int[] golden = new int[len];
            for (int m = 1; m < 2 * len; m *= 2) {
                for (PrimitiveTypeArrayBuilder builder : PrimitiveTypeArrayBuilder.values()) {
                    builder.build(golden, m);
                    int[] test = golden.clone();

                    for (PrimitiveTypeConverter converter : PrimitiveTypeConverter.values()) {
                        Object convertedGolden = converter.convert(golden);
                        Object convertedTest = converter.convert(test);
                        sort(convertedTest);
                        checkSorted(convertedTest);
                        assertEquals(checkSum(convertedGolden), checkSum(convertedTest));
                    }
                }
            }
        }
    }

    private void sort(Object array) {
        if (array instanceof int[]) {
            Arrays.sort((int[]) array);
        }
        else if (array instanceof long[]) {
            Arrays.sort((long[]) array);
        } else if (array instanceof short[]) {
            Arrays.sort((short[]) array);
        } else if (array instanceof byte[]) {
            Arrays.sort((byte[]) array);
        } else if (array instanceof char[]) {
            Arrays.sort((char[]) array);
        } else if (array instanceof float[]) {
            Arrays.sort((float[]) array);
        } else if (array instanceof double[]) {
            Arrays.sort((double[]) array);
        } else {
            fail("Unknow type of array: " + array.getClass());
        }
    }

    private void checkSorted(Object array) {
        if (array instanceof int[]) {
            checkSorted((int[]) array);
        } else if (array instanceof long[]) {
            checkSorted((long[]) array);
        } else if (array instanceof short[]) {
            checkSorted((short[]) array);
        } else if (array instanceof byte[]) {
            checkSorted((byte[]) array);
        } else if (array instanceof char[]) {
            checkSorted((char[]) array);
        } else if (array instanceof float[]) {
            checkSorted((float[]) array);
        } else if (array instanceof double[]) {
            checkSorted((double[]) array);
        } else {
            fail("Unknow type of array: " + array.getClass());
        }
    }

    private void checkSorted(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }

    private void checkSorted(long[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }

    private void checkSorted(short[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }

    private void checkSorted(byte[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }

    private void checkSorted(char[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }

    private void checkSorted(float[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }

    private void checkSorted(double[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                orderFail(i, "" + a[i], "" + a[i + 1]);
            }
        }
    }


    private void orderFail(int index, String value1, String value2) {
        fail("Array is not sorted at " + index + "-th position: " + value1 + " and " + value2);
    }

    private int checkSum(Object array) {
        if (array instanceof int[]) {
            return checkSum((int[]) array);
        } else if (array instanceof long[]) {
            return checkSum((long[]) array);
        } else if (array instanceof short[]) {
            return checkSum((short[]) array);
        } else if (array instanceof byte[]) {
            return checkSum((byte[]) array);
        } else if (array instanceof char[]) {
            return checkSum((char[]) array);
        } else if (array instanceof float[]) {
            return checkSum((float[]) array);
        } else if (array instanceof double[]) {
            return checkSum((double[]) array);
        } else {
            fail("Unknow type of array: " + array.getClass());
        }
        throw new AssertionError(); // Needed to shut up compiler
    }

    private int checkSum(int[] a) {
        int checkSum = 0;

        for (int e : a) {
            checkSum ^= e; // xor
        }
        return checkSum;
    }

    private int checkSum(long[] a) {
        long checkSum = 0;

        for (long e : a) {
            checkSum ^= e; // xor
        }
        return (int) checkSum;
    }

    private int checkSum(short[] a) {
        short checkSum = 0;

        for (short e : a) {
            checkSum ^= e; // xor
        }
        return (int) checkSum;
    }

    private int checkSum(byte[] a) {
        byte checkSum = 0;

        for (byte e : a) {
            checkSum ^= e; // xor
        }
        return (int) checkSum;
    }

    private int checkSum(char[] a) {
        char checkSum = 0;

        for (char e : a) {
            checkSum ^= e; // xor
        }
        return (int) checkSum;
    }

    private int checkSum(float[] a) {
        int checkSum = 0;

        for (float e : a) {
            checkSum ^= (int) e; // xor
        }
        return checkSum;
    }

    private int checkSum(double[] a) {
        int checkSum = 0;

        for (double e : a) {
            checkSum ^= (int) e; // xor
        }
        return checkSum;
    }

    private enum PrimitiveTypeArrayBuilder {

        RANDOM {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = ourRandom.nextInt();
                }
            }
        },

        ASCENDING {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = m + i;
                }
            }
        },

        DESCENDING {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = a.length - m - i;
                }
            }
        },

        ALL_EQUAL {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = m;
                }
            }
        },

        SAW {
            void build(int[] a, int m) {
                int incCount = 1;
                int decCount = a.length;
                int i = 0;
                int period = m;
                m--;

                while (true) {
                    for (int k = 1; k <= period; k++) {
                        if (i >= a.length) {
                            return;
                        }
                        a[i++] = incCount++;
                    }
                    period += m;

                    for (int k = 1; k <= period; k++) {
                        if (i >= a.length) {
                            return;
                        }
                        a[i++] = decCount--;
                    }
                    period += m;
                }
            }
        },

        REPEATED {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = i % m;
                }
            }
        },

        DUPLICATED {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = ourRandom.nextInt(m);
                }
            }
        },

        ORGAN_PIPES {
            void build(int[] a, int m) {
                int middle = a.length / (m + 1);

                for (int i = 0; i < middle; i++) {
                    a[i] = i;
                }
                for (int i = middle; i < a.length ; i++) {
                    a[i] = a.length - i - 1;
                }
            }
        },

        STAGGER {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = (i * m + i) % a.length;
                }
            }
        },

        PLATEAU {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] =  Math.min(i, m);
                }
            }
        },

        SHUFFLE {
            void build(int[] a, int m) {
                for (int i = 0; i < a.length; i++) {
                    a[i] = ourRandom.nextBoolean() ? (ourFirst += 2) : (ourSecond += 2);
                }
            }
        };

        abstract void build(int[] a, int m);

        static void reset() {
            ourRandom = new Random(666);
            ourFirst = 0;
            ourSecond = 0;
        }

        @Override
        public String toString() {
            String name = name();

            for (int i = name.length(); i < 12; i++) {
                name += " " ;
            }
            return name;
        }

        private static int ourFirst;
        private static int ourSecond;
        private static Random ourRandom = new Random(666);
    }

    private enum PrimitiveTypeConverter {

        INT {
            Object convert(int[] a) {
                return a;
            }
        },

        LONG {
            Object convert(int[] a) {
                long[] b = new long[a.length];

                for (int i = 0; i < a.length; i++) {
                    b[i] = (long) a[i];
                }
                return b;
            }
        },

        BYTE {
            Object convert(int[] a) {
                byte[] b = new byte[a.length];

                for (int i = 0; i < a.length; i++) {
                    b[i] = (byte) a[i];
                }
                return b;
            }
        },

        SHORT {
            Object convert(int[] a) {
                short[] b = new short[a.length];

                for (int i = 0; i < a.length; i++) {
                    b[i] = (short) a[i];
                }
                return b;
            }
        },

        CHAR {
            Object convert(int[] a) {
                char[] b = new char[a.length];

                for (int i = 0; i < a.length; i++) {
                    b[i] = (char) a[i];
                }
                return b;
            }
        },

        FLOAT {
            Object convert(int[] a) {
                float[] b = new float[a.length];

                for (int i = 0; i < a.length; i++) {
                    b[i] = (float) a[i];
                }
                return b;
            }
        },

        DOUBLE {
            Object convert(int[] a) {
                double[] b = new double[a.length];

                for (int i = 0; i < a.length; i++) {
                    b[i] = (double) a[i];
                }
                return b;
            }
        };

        abstract Object convert(int[] a);

        public String toString() {
            String name = name();

            for (int i = name.length(); i < 9; i++) {
                name += " " ;
            }
            return name;
        }
    }


    /**
     * java.util.Arrays#deepEquals(Object[], Object[])
     */
    public void test_deepEquals$Ljava_lang_ObjectLjava_lang_Object() {
        int[] a1 = { 1, 2, 3 };
        short[] a2 = { 0, 1 };
        Object[] a3 = { new Integer(1), a2 };
        int[] a4 = { 6, 5, 4 };

        int[] b1 = { 1, 2, 3 };
        short[] b2 = { 0, 1 };
        Object[] b3 = { new Integer(1), b2 };

        Object a[] = { a1, a2, a3 };
        Object b[] = { b1, b2, b3 };

        assertFalse(Arrays.equals(a, b));
        assertTrue(Arrays.deepEquals(a, b));

        a[2] = a4;

        assertFalse(Arrays.deepEquals(a, b));
    }

    /**
     * java.util.Arrays#deepHashCode(Object[])
     */
    public void test_deepHashCode$Ljava_lang_Object() {
        int[] a1 = { 1, 2, 3 };
        short[] a2 = { 0, 1 };
        Object[] a3 = { new Integer(1), a2 };

        int[] b1 = { 1, 2, 3 };
        short[] b2 = { 0, 1 };
        Object[] b3 = { new Integer(1), b2 };

        Object a[] = { a1, a2, a3 };
        Object b[] = { b1, b2, b3 };

        int deep_hash_a = Arrays.deepHashCode(a);
        int deep_hash_b = Arrays.deepHashCode(b);

        assertEquals(deep_hash_a, deep_hash_b);
    }

    /**
     * java.util.Arrays#hashCode(boolean[] a)
     */
    public void test_hashCode$LZ() {
        int listHashCode;
        int arrayHashCode;

        boolean[] boolArr = { true, false, false, true, false };
        List listOfBoolean = new LinkedList();
        for (int i = 0; i < boolArr.length; i++) {
            listOfBoolean.add(new Boolean(boolArr[i]));
        }
        listHashCode = listOfBoolean.hashCode();
        arrayHashCode = Arrays.hashCode(boolArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * java.util.Arrays#hashCode(int[] a)
     */
    public void test_hashCode$LI() {
        int listHashCode;
        int arrayHashCode;

        int[] intArr = { 10, 5, 134, 7, 19 };
        List listOfInteger = new LinkedList();

        for (int i = 0; i < intArr.length; i++) {
            listOfInteger.add(new Integer(intArr[i]));
        }
        listHashCode = listOfInteger.hashCode();
        arrayHashCode = Arrays.hashCode(intArr);
        assertEquals(listHashCode, arrayHashCode);

        int[] intArr2 = { 10, 5, 134, 7, 19 };
        assertEquals(Arrays.hashCode(intArr2), Arrays.hashCode(intArr));
    }

    /**
     * java.util.Arrays#hashCode(char[] a)
     */
    public void test_hashCode$LC() {
        int listHashCode;
        int arrayHashCode;

        char[] charArr = { 'a', 'g', 'x', 'c', 'm' };
        List listOfCharacter = new LinkedList();
        for (int i = 0; i < charArr.length; i++) {
            listOfCharacter.add(new Character(charArr[i]));
        }
        listHashCode = listOfCharacter.hashCode();
        arrayHashCode = Arrays.hashCode(charArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * java.util.Arrays#hashCode(byte[] a)
     */
    public void test_hashCode$LB() {
        int listHashCode;
        int arrayHashCode;

        byte[] byteArr = { 5, 9, 7, 6, 17 };
        List listOfByte = new LinkedList();
        for (int i = 0; i < byteArr.length; i++) {
            listOfByte.add(new Byte(byteArr[i]));
        }
        listHashCode = listOfByte.hashCode();
        arrayHashCode = Arrays.hashCode(byteArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * java.util.Arrays#hashCode(long[] a)
     */
    public void test_hashCode$LJ() {
        int listHashCode;
        int arrayHashCode;

        long[] longArr = { 67890234512l, 97587236923425l, 257421912912l,
                6754268100l, 5 };
        List listOfLong = new LinkedList();
        for (int i = 0; i < longArr.length; i++) {
            listOfLong.add(new Long(longArr[i]));
        }
        listHashCode = listOfLong.hashCode();
        arrayHashCode = Arrays.hashCode(longArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * java.util.Arrays#hashCode(float[] a)
     */
    public void test_hashCode$LF() {
        int listHashCode;
        int arrayHashCode;

        float[] floatArr = { 0.13497f, 0.268934f, 12e-5f, -3e+2f, 10e-4f };
        List listOfFloat = new LinkedList();
        for (int i = 0; i < floatArr.length; i++) {
            listOfFloat.add(new Float(floatArr[i]));
        }
        listHashCode = listOfFloat.hashCode();
        arrayHashCode = Arrays.hashCode(floatArr);
        assertEquals(listHashCode, arrayHashCode);

        float[] floatArr2 = { 0.13497f, 0.268934f, 12e-5f, -3e+2f, 10e-4f };
        assertEquals(Arrays.hashCode(floatArr2), Arrays.hashCode(floatArr));
    }

    /**
     * java.util.Arrays#hashCode(double[] a)
     */
    public void test_hashCode$LD() {
        int listHashCode;
        int arrayHashCode;

        double[] doubleArr = { 0.134945657, 0.0038754, 11e-150, -30e-300, 10e-4 };
        List listOfDouble = new LinkedList();
        for (int i = 0; i < doubleArr.length; i++) {
            listOfDouble.add(new Double(doubleArr[i]));
        }
        listHashCode = listOfDouble.hashCode();
        arrayHashCode = Arrays.hashCode(doubleArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * java.util.Arrays#hashCode(short[] a)
     */
    public void test_hashCode$LS() {
        int listHashCode;
        int arrayHashCode;

        short[] shortArr = { 35, 13, 45, 2, 91 };
        List listOfShort = new LinkedList();
        for (int i = 0; i < shortArr.length; i++) {
            listOfShort.add(new Short(shortArr[i]));
        }
        listHashCode = listOfShort.hashCode();
        arrayHashCode = Arrays.hashCode(shortArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * java.util.Arrays#hashCode(Object[] a)
     */
    public void test_hashCode$Ljava_lang_Object() {
        int listHashCode;
        int arrayHashCode;

        Object[] objectArr = { new Integer(1), new Float(10e-12f), null };
        List listOfObject = new LinkedList();
        for (int i = 0; i < objectArr.length; i++) {
            listOfObject.add(objectArr[i]);
        }
        listHashCode = listOfObject.hashCode();
        arrayHashCode = Arrays.hashCode(objectArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        objArray = new Object[arraySize];
        for (int i = 0; i < objArray.length; i++)
            objArray[i] = new Integer(i);

        booleanArray = new boolean[arraySize];
        byteArray = new byte[arraySize];
        charArray = new char[arraySize];
        doubleArray = new double[arraySize];
        floatArray = new float[arraySize];
        intArray = new int[arraySize];
        longArray = new long[arraySize];
        objectArray = new Object[arraySize];
        shortArray = new short[arraySize];

        for (int counter = 0; counter < arraySize; counter++) {
            byteArray[counter] = (byte) counter;
            charArray[counter] = (char) (counter + 1);
            doubleArray[counter] = counter;
            floatArray[counter] = counter;
            intArray[counter] = counter;
            longArray[counter] = counter;
            objectArray[counter] = objArray[counter];
            shortArray[counter] = (short) counter;
        }
        for (int counter = 0; counter < arraySize; counter += 2) {
            booleanArray[counter] = false;
            booleanArray[counter + 1] = true;
        }
    }

    /**
     * java.util.Arrays#binarySearch(byte[], int, int, byte)
     */
    public void test_binarySearch$BIIB() {
        for (byte counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on byte[] answered incorrect position",
                    Arrays.binarySearch(byteArray, counter, arraySize, counter) == counter);
        }
        assertEquals(
                "Binary search succeeded for value not present in array 1", -1,
                Arrays.binarySearch(byteArray, 0, arraySize, (byte) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(byteArray, (byte) arraySize) == -(arraySize + 1));
        for (byte counter = 0; counter < arraySize; counter++) {
            byteArray[counter] -= 50;
        }
        for (byte counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on byte[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(byteArray, counter, arraySize,
                            (byte) (counter - 50)) == counter);
        }
        try {
            Arrays.binarySearch((byte[]) null, 2, 1, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((byte[]) null, -1, 0, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((byte[]) null, -1, -2, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch(byteArray, 2, 1, (byte) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(-1, Arrays.binarySearch(byteArray, 0, 0, (byte) arraySize));
        try {
            Arrays.binarySearch(byteArray, -1, -2, (byte) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(byteArray, arraySize + 2, arraySize + 1,
                    (byte) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(byteArray, -1, 0, (byte) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(byteArray, 0, arraySize + 1, (byte) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(char[], char)
     */
    public void test_binarySearch$CIIC() {
        for (char counter = 0; counter < arraySize; counter++) {
            assertTrue("Binary search on char[] answered incorrect position",
                    Arrays.binarySearch(charArray, counter, arraySize,
                            (char) (counter + 1)) == counter);
        }
        assertEquals(
                "Binary search succeeded for value not present in array 1", -1,
                Arrays.binarySearch(charArray, 0, arraySize, '\u0000'));
        assertTrue("Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(charArray, 0, arraySize,
                        (char) (arraySize + 1)) == -(arraySize + 1));
        try {
            Arrays.binarySearch(charArray, 2, 1, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch((char[]) null, 2, 1, (char) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((char[]) null, -1, 0, (char) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((char[]) null, -1, -2, (char) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        assertEquals(-1, Arrays.binarySearch(charArray, 0, 0, (char) arraySize));
        try {
            Arrays.binarySearch(charArray, -1, -2, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(charArray, arraySize + 2, arraySize + 1,
                    (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(charArray, -1, 0, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(charArray, 0, arraySize + 1, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(double[], double)
     */
    public void test_binarySearch$DIID() {
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue("Binary search on double[] answered incorrect position",
                    Arrays.binarySearch(doubleArray, counter, arraySize,
                            (double) counter) == (double) counter);
        }
        assertEquals(
                "Binary search succeeded for value not present in array 1", -1,
                Arrays.binarySearch(doubleArray, 0, arraySize, (double) -1));
        assertTrue("Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(doubleArray, 0, arraySize,
                        (double) arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++) {
            doubleArray[counter] -= (double) 50;
        }
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on double[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(doubleArray, counter, arraySize, (double) (counter - 50)) == (double) counter);
        }
        double[] specials = new double[] { Double.NEGATIVE_INFINITY,
                -Double.MAX_VALUE, -2d, -Double.MIN_VALUE, -0d, 0d,
                Double.MIN_VALUE, 2d, Double.MAX_VALUE,
                Double.POSITIVE_INFINITY, Double.NaN };
        for (int i = 0; i < specials.length; i++) {
            int result = Arrays.binarySearch(specials, i, specials.length, specials[i]);
            assertTrue(specials[i] + " invalid: " + result, result == i);
        }
        assertEquals("-1d", -4, Arrays.binarySearch(specials, 0, specials.length, -1d));
        assertEquals("1d", -8, Arrays.binarySearch(specials, 0, specials.length, 1d));
        try {
            Arrays.binarySearch((double[]) null, 2, 1, (double) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((double[]) null, -1, 0, (double) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((double[]) null, -1, -2, (double) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch(doubleArray, 2, 1, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(-1, Arrays.binarySearch(doubleArray, 0, 0, (char) arraySize));
        try {
            Arrays.binarySearch(doubleArray, -1, -2, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(doubleArray, arraySize + 2, arraySize + 1,
                    (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(doubleArray, -1, 0, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(doubleArray, 0, arraySize + 1, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(float[], float)
     */
    public void test_binarySearch$FIIF() {
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue("Binary search on float[] answered incorrect position",
                    Arrays.binarySearch(floatArray, counter, arraySize,
                            (float) counter) == (float) counter);
        }
        assertEquals(
                "Binary search succeeded for value not present in array 1", -1,
                Arrays.binarySearch(floatArray, 0, arraySize, (float) -1));
        assertTrue("Binary search succeeded for value not present in array 2",
                Arrays
                        .binarySearch(floatArray, 0, arraySize,
                                (float) arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++) {
            floatArray[counter] -= (float) 50;
        }
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on float[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(floatArray, 0, arraySize,
                            (float) counter - 50) == (float) counter);
        }
        float[] specials = new float[] { Float.NEGATIVE_INFINITY,
                -Float.MAX_VALUE, -2f, -Float.MIN_VALUE, -0f, 0f,
                Float.MIN_VALUE, 2f, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
                Float.NaN };
        for (int i = 0; i < specials.length; i++) {
            int result = Arrays.binarySearch(specials, i, specials.length, specials[i]);
            assertTrue(specials[i] + " invalid: " + result, result == i);
        }
        assertEquals("-1f", -4, Arrays.binarySearch(specials, 0, specials.length, -1f));
        assertEquals("1f", -8, Arrays.binarySearch(specials, 0, specials.length, 1f));
        try {
            Arrays.binarySearch((float[]) null, 2, 1, (float) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((float[]) null, -1, 0, (float) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((float[]) null, -1, -2, (float) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch(floatArray, 2, 1, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(-1, Arrays.binarySearch(floatArray, 0, 0,
                (char) arraySize));
        try {
            Arrays.binarySearch(floatArray, -1, -2, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(floatArray, arraySize + 2, arraySize + 1,
                    (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(floatArray, -1, 0, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays
                    .binarySearch(floatArray, 0, arraySize + 1,
                            (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(int[], int)
     */
    public void test_binarySearch$IIII() {
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on int[] answered incorrect position",
                    Arrays.binarySearch(intArray, counter, arraySize, counter) == counter);
        }
        assertEquals(
                "Binary search succeeded for value not present in array 1", -1,
                Arrays.binarySearch(intArray, 0, arraySize, -1));
        assertTrue("Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(intArray, 0, arraySize, arraySize) == -(arraySize + 1));
        for (int counter = 0; counter < arraySize; counter++) {
            intArray[counter] -= 50;
        }
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on int[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(intArray, 0, arraySize, counter - 50) == counter);
        }
        try {
            Arrays.binarySearch((int[]) null, 2, 1, (int) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((int[]) null, -1, 0, (int) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((int[]) null, -1, -2, (int) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch(intArray, 2, 1, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(-1, Arrays
                .binarySearch(intArray, 0, 0, (char) arraySize));
        try {
            Arrays.binarySearch(intArray, -1, -2, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(intArray, arraySize + 2, arraySize + 1,
                    (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(intArray, -1, 0, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(intArray, 0, arraySize + 1, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(long[], long)
     */
    public void test_binarySearch$JIIJ() {
        for (long counter = 0; counter < arraySize; counter++) {
            assertTrue("Binary search on long[] answered incorrect position",
                    Arrays.binarySearch(longArray, 0, arraySize, counter) == counter);
        }
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(longArray, 0, arraySize, (long) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(longArray, 0, arraySize, (long) arraySize) == -(arraySize + 1));
        for (long counter = 0; counter < arraySize; counter++) {
            longArray[(int) counter] -= (long) 50;
        }
        for (long counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on long[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(longArray, 0, arraySize, counter - (long) 50) == counter);
        }
        try {
            Arrays.binarySearch((long[]) null, 2, 1, (long) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((long[]) null, -1, 0, (long) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((long[]) null, -1, -2, (long) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch(longArray, 2, 1, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(-1, Arrays
                .binarySearch(longArray, 0, 0, (char) arraySize));
        try {
            Arrays.binarySearch(longArray, -1, -2, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(longArray, arraySize + 2, arraySize + 1,
                    (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(longArray, -1, 0, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(longArray, 0, arraySize + 1, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(java.lang.Object[],
     *java.lang.Object)
     */
    public void test_binarySearch$Ljava_lang_ObjectIILjava_lang_Object() {
        assertEquals(
                "Binary search succeeded for non-comparable value in empty array",
                -1, Arrays.binarySearch(new Object[] { }, 0, 0, new Object()));
        assertEquals(
                "Binary search succeeded for comparable value in empty array",
                -1, Arrays.binarySearch(new Object[] { }, 0, 0, new Integer(-1)));
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on Object[] answered incorrect position",
                    Arrays.binarySearch(objectArray, counter, arraySize, objArray[counter]) == counter);
        }
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(objectArray, 0, arraySize, new Integer(-1)));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(objectArray, 0, arraySize, new Integer(arraySize)) == -(arraySize + 1));
        try {
            Arrays.binarySearch((Object[]) null, 2, 1, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((Object[]) null, -1, 0, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((Object[]) null, -1, -2, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, 2, 1, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(-1, Arrays
                .binarySearch(objectArray, 0, 0, (char) arraySize));
        try {
            Arrays.binarySearch(objectArray, -1, -2, (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, arraySize + 2, arraySize + 1,
                    (char) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, -1, 0, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, 0, arraySize + 1, (char) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(java.lang.Object[],
     *java.lang.Object, java.util.Comparator)
     */
    public void test_binarySearch$Ljava_lang_ObjectIILjava_lang_ObjectLjava_util_Comparator() {
        Comparator comp = new ReversedIntegerComparator();
        for (int counter = 0; counter < arraySize; counter++) {
            objectArray[counter] = objArray[arraySize - counter - 1];
        }
        assertTrue("Binary search succeeded for value not present in array 1",
                Arrays.binarySearch(objectArray, 0, arraySize, new Integer(-1),
                        comp) == -(arraySize + 1));
        assertEquals(
                "Binary search succeeded for value not present in array 2", -1,
                Arrays.binarySearch(objectArray, 0, arraySize, new Integer(
                        arraySize), comp));
        for (int counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on Object[] with custom comparator answered incorrect position",
                    Arrays.binarySearch(objectArray, objArray[counter], comp) == arraySize
                            - counter - 1);
        }
        try {
            Arrays.binarySearch((Object[]) null, 2, 1, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((Object[]) null, -1, 0, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((Object[]) null, -1, -2, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, 2, 1, (char) arraySize, comp);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(-1, Arrays.binarySearch(objectArray, 0, 0,
                (char) arraySize, comp));
        try {
            Arrays.binarySearch(objectArray, -1, -2, (char) arraySize, comp);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, arraySize + 2, arraySize + 1,
                    (char) arraySize, comp);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, -1, 0, (char) arraySize, comp);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, 0, arraySize + 1,
                    (char) arraySize, comp);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(objectArray, 0, arraySize,
                    new LinkedList(), comp);
            fail("should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }
    }

    /**
     * java.util.Arrays#binarySearch(short[], short)
     */
    public void test_binarySearch$SIIS() {
        for (short counter = 0; counter < arraySize; counter++) {
            assertTrue("Binary search on short[] answered incorrect position",
                    Arrays.binarySearch(shortArray, counter, arraySize, counter) == counter);
        }
        assertEquals("Binary search succeeded for value not present in array 1",
                -1, Arrays.binarySearch(shortArray, 0, arraySize, (short) -1));
        assertTrue(
                "Binary search succeeded for value not present in array 2",
                Arrays.binarySearch(shortArray, 0, arraySize, (short) arraySize) == -(arraySize + 1));
        for (short counter = 0; counter < arraySize; counter++) {
            shortArray[counter] -= 50;
        }
        for (short counter = 0; counter < arraySize; counter++) {
            assertTrue(
                    "Binary search on short[] involving negative numbers answered incorrect position",
                    Arrays.binarySearch(shortArray, counter, arraySize, (short) (counter - 50)) == counter);
        }
        try {
            Arrays.binarySearch((String[]) null, 2, 1, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((String[]) null, -1, 0, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch((String[]) null, -1, -2, (byte) arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.binarySearch(shortArray, 2, 1, (short) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(-1, Arrays
                .binarySearch(shortArray, 0, 0, (short) arraySize));
        try {
            Arrays.binarySearch(shortArray, -1, -2, (short) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(shortArray, arraySize + 2, arraySize + 1,
                    (short) arraySize);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.binarySearch(shortArray, -1, 0, (short) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.binarySearch(shortArray, 0, arraySize + 1, (short) arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        String[] array = { "a", "b", "c" };
        assertEquals(-2, Arrays.binarySearch(array, 1, 2, "a", null));
    }

    /**
     * {@link java.util.Arrays#copyOf(byte[], int)
     */
    public void test_copyOf_$BI() throws Exception {
        byte[] result = Arrays.copyOf(byteArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOf(byteArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i, result[i]);
        }
        try {
            Arrays.copyOf((byte[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(byteArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((byte[]) null, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
    }

    /**
     * {@link java.util.Arrays#copyOf(short[], int)
     */
    public void test_copyOf_$SI() throws Exception {
        short[] result = Arrays.copyOf(shortArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOf(shortArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i, result[i]);
        }
        try {
            Arrays.copyOf((short[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(shortArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((short[]) null, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
    }

    /**
     * {@link java.util.Arrays#copyOf(int[], int)
     */
    public void test_copyOf_$II() throws Exception {
        int[] result = Arrays.copyOf(intArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOf(intArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i, result[i]);
        }
        try {
            Arrays.copyOf((int[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(intArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((int[]) null, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
    }

    /**
     * {@link java.util.Arrays#copyOf(boolean[], int)
     */
    public void test_copyOf_$ZI() throws Exception {
        boolean[] result = Arrays.copyOf(booleanArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(booleanArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(false, result[i]);
        }
        result = Arrays.copyOf(booleanArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(booleanArray[i], result[i]);
        }
        try {
            Arrays.copyOf((boolean[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(booleanArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((boolean[]) null, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
    }

    /**
     * {@link java.util.Arrays#copyOf(char[], int)
     */
    public void test_copyOf_$CI() throws Exception {
        char[] result = Arrays.copyOf(charArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i + 1, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOf(charArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i + 1, result[i]);
        }
        try {
            Arrays.copyOf((char[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(charArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((char[]) null, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
    }

    /**
     * {@link java.util.Arrays#copyOf(float[], int)
     */
    public void test_copyOf_$FI() throws Exception {
        float[] result = Arrays.copyOf(floatArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(floatArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0.0f, result[i]);
        }
        result = Arrays.copyOf(floatArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(floatArray[i], result[i]);
        }
        try {
            Arrays.copyOf((float[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(floatArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((float[]) null, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
    }

    /**
     * {@link java.util.Arrays#copyOf(double[], int)
     */
    public void test_copyOf_$DI() throws Exception {
        double[] result = Arrays.copyOf(doubleArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(doubleArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0.0, result[i]);
        }
        result = Arrays.copyOf(doubleArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(doubleArray[i], result[i]);
        }
        try {
            Arrays.copyOf((double[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(doubleArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((double[]) null, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
    }

    /**
     * {@link java.util.Arrays#copyOf(long[], int)
     */
    public void test_copyOf_$JI() throws Exception {
        long[] result = Arrays.copyOf(longArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(longArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOf(longArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(longArray[i], result[i]);
        }
        try {
            Arrays.copyOf((long[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(longArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((long[]) null, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
    }

    /**
     * {@link java.util.Arrays#copyOf(T[], int)
     */
    public void test_copyOf_$TI() throws Exception {
        Object[] result = Arrays.copyOf(objArray, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(objArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertNull(result[i]);
        }
        result = Arrays.copyOf(objArray, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(objArray[i], result[i]);
        }
        try {
            Arrays.copyOf((String[]) null, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(objArray, -1);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((String[]) null, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        Date[] component = new Date[0];
        Object object[] = new Date[0];

        object = Arrays.copyOf(component, 2);
        assertNotNull(object);
        component = Arrays.copyOf(component, 2);
        assertNotNull(component);
        assertEquals(2, component.length);
    }

    /**
     * {@link java.util.Arrays#copyOf(T[], int, Class<? extends Object[]>))
     */
    public void test_copyOf_$TILClass() throws Exception {
        Object[] result = Arrays.copyOf(objArray, arraySize * 2, Object[].class);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(objArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertNull(result[i]);
        }
        result = Arrays.copyOf(objArray, arraySize / 2, Object[].class);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(objArray[i], result[i]);
        }
        result = Arrays.copyOf(objArray, arraySize / 2, Integer[].class);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(objArray[i], result[i]);
        }
        try {
            Arrays.copyOf((Object[]) null, arraySize, LinkedList[].class);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(objArray, arraySize, LinkedList[].class);
            fail("should throw ArrayStoreException ");
        } catch (ArrayStoreException e) {
            // expected
        }
        try {
            Arrays.copyOf((Object[]) null, arraySize, Object[].class);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOf(objArray, -1, Object[].class);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((Object[]) null, -1, Object[].class);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((Object[]) null, -1, LinkedList[].class);
            fail("should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            // expected
        }
        try {
            Arrays.copyOf((Object[]) null, 0, LinkedList[].class);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        assertEquals(0, Arrays.copyOf(objArray, 0, LinkedList[].class).length);
    }

    /**
     * {@link java.util.Arrays#copyOfRange(byte[], int, int)
     */
    public void test_copyOfRange_$BII() throws Exception {
        byte[] result = Arrays.copyOfRange(byteArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOfRange(byteArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i, result[i]);
        }
        result = Arrays.copyOfRange(byteArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((byte[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((byte[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((byte[]) null, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(byteArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(byteArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(byteArray.length + 1, Arrays.copyOfRange(byteArray, 0,
                byteArray.length + 1).length);
    }

    /**
     * {@link java.util.Arrays#copyOfRange(short[], int, int)
     */
    public void test_copyOfRange_$SII() throws Exception {
        short[] result = Arrays.copyOfRange(shortArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOfRange(shortArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i, result[i]);
        }
        result = Arrays.copyOfRange(shortArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((short[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((short[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((short[]) null, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(shortArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(shortArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(shortArray.length + 1, Arrays.copyOfRange(shortArray, 0,
                shortArray.length + 1).length);
    }

    /**
     * {@link java.util.Arrays#copyOfRange(int[], int, int)
     */
    public void test_copyOfRange_$III() throws Exception {
        int[] result = Arrays.copyOfRange(intArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOfRange(intArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i, result[i]);
        }
        result = Arrays.copyOfRange(intArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((int[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((int[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((int[]) null, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(intArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(intArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(intArray.length + 1, Arrays.copyOfRange(intArray, 0,
                intArray.length + 1).length);
    }

    /**
     * {@link java.util.Arrays#copyOfRange(long[], int, int)
     */
    public void test_copyOfRange_$JII() throws Exception {
        long[] result = Arrays.copyOfRange(longArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOfRange(longArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i, result[i]);
        }
        result = Arrays.copyOfRange(longArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((long[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((long[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((long[]) null, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(longArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(longArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(longArray.length + 1, Arrays.copyOfRange(longArray, 0,
                longArray.length + 1).length);
    }

    /**
     * {@link java.util.Arrays#copyOfRange(char[], int, int)
     */
    public void test_copyOfRange_$CII() throws Exception {
        char[] result = Arrays.copyOfRange(charArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(i + 1, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0, result[i]);
        }
        result = Arrays.copyOfRange(charArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(i + 1, result[i]);
        }
        result = Arrays.copyOfRange(charArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((char[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((char[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((char[]) null, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(charArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(charArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(charArray.length + 1, Arrays.copyOfRange(charArray, 0,
                charArray.length + 1).length);
    }

    public void test_copyOfRange_$FII() throws Exception {
        float[] result = Arrays.copyOfRange(floatArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals((float) i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0.0f, result[i]);
        }
        result = Arrays.copyOfRange(floatArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals((float) i, result[i]);
        }
        result = Arrays.copyOfRange(floatArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((float[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((float[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((float[]) null, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(floatArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(floatArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(floatArray.length + 1, Arrays.copyOfRange(floatArray, 0,
                floatArray.length + 1).length);
    }

    /**
     * {@link java.util.Arrays#copyOfRange(double[], int, int)
     */
    public void test_copyOfRange_$DII() throws Exception {
        double[] result = Arrays.copyOfRange(doubleArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals((double) i, result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(0.0, result[i]);
        }
        result = Arrays.copyOfRange(doubleArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals((double) i, result[i]);
        }
        result = Arrays.copyOfRange(doubleArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((double[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((double[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((double[]) null, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(doubleArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(doubleArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(doubleArray.length + 1, Arrays.copyOfRange(doubleArray, 0,
                doubleArray.length + 1).length);
    }

    /**
     * {@link java.util.Arrays#copyOfRange(boolean[], int, int)
     */
    public void test_copyOfRange_$ZII() throws Exception {
        boolean[] result = Arrays.copyOfRange(booleanArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(booleanArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(false, result[i]);
        }
        result = Arrays.copyOfRange(booleanArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(booleanArray[i], result[i]);
        }
        result = Arrays.copyOfRange(booleanArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((boolean[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((boolean[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((boolean[]) null, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(booleanArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(booleanArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(booleanArray.length + 1, Arrays.copyOfRange(booleanArray, 0,
                booleanArray.length + 1).length);
    }

    /**
     * {@link java.util.Arrays#copyOfRange(Object[], int, int)
     */
    public void test_copyOfRange_$TII() throws Exception {
        Object[] result = Arrays.copyOfRange(objArray, 0, arraySize * 2);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(objArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(null, result[i]);
        }
        result = Arrays.copyOfRange(objArray, 0, arraySize / 2);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(objArray[i], result[i]);
        }
        result = Arrays.copyOfRange(objArray, 0, 0);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange((Object[]) null, 0, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((Object[]) null, -1, arraySize);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((Object[]) null, 0, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((Object[]) objArray, -1, arraySize);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange((Object[]) objArray, 0, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(objArray.length + 1, Arrays.copyOfRange(objArray, 0,
                objArray.length + 1).length);
    }

    public void test_copyOfRange_$TIILClass() throws Exception {
        Object[] result = Arrays.copyOfRange(objArray, 0, arraySize * 2, Integer[].class);
        int i = 0;
        for (; i < arraySize; i++) {
            assertEquals(objArray[i], result[i]);
        }
        for (; i < result.length; i++) {
            assertEquals(null, result[i]);
        }
        result = Arrays.copyOfRange(objArray, 0, arraySize / 2, Integer[].class);
        i = 0;
        for (; i < result.length; i++) {
            assertEquals(objArray[i], result[i]);
        }
        result = Arrays.copyOfRange(objArray, 0, 0, Integer[].class);
        assertEquals(0, result.length);
        try {
            Arrays.copyOfRange(null, 0, arraySize, Integer[].class);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(null, -1, arraySize, Integer[].class);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(null, 0, -1, Integer[].class);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(objArray, -1, arraySize, Integer[].class);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(objArray, 0, -1, Integer[].class);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(objArray, 0, -1, LinkedList[].class);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(objArray, 0, 1, LinkedList[].class);
            fail("should throw ArrayStoreException");
        } catch (ArrayStoreException e) {
            // expected
        }
        try {
            Arrays.copyOfRange(null, 0, 1, LinkedList[].class);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            assertEquals(objArray.length + 1, Arrays.copyOfRange(objArray, 0,
                    objArray.length + 1, LinkedList[].class).length);
            fail("should throw ArrayStoreException");
        } catch (ArrayStoreException e) {
            // expected
        }
        assertEquals(0,
                Arrays.copyOfRange(objArray, 0, 0, LinkedList[].class).length);
    }

    public void test_asList_spliterator() {
        List<String> list = Arrays.asList(
                "a", "b", "c", "d", "e", "f", "g", "h",
                "i", "j", "k", "l", "m", "n", "o", "p");
        ArrayList<String> expected = new ArrayList<>(list);

        SpliteratorTester.runBasicIterationTests(list.spliterator(), expected);
        SpliteratorTester.runBasicSplitTests(list, expected);
        SpliteratorTester.testSpliteratorNPE(list.spliterator());

        assertTrue(list.spliterator().hasCharacteristics(Spliterator.ORDERED));

        SpliteratorTester.runOrderedTests(list);
        SpliteratorTester.assertSupportsTrySplit(list);
    }

    public void test_spliterator_ref() {
        String[] elements = {
                "a", "b", "c", "d", "e", "f", "g", "h",
                "i", "j", "k", "l", "m", "n", "o", "p" };

        ArrayList<String> expected = new ArrayList<>(Arrays.asList(elements));

        SpliteratorTester.runBasicIterationTests(Arrays.spliterator(elements), expected);
        SpliteratorTester.testSpliteratorNPE(Arrays.spliterator(elements));
        assertNotNull(Arrays.spliterator(elements).trySplit());

        Spliterator<String> sp = Arrays.spliterator(elements);
        assertTrue(sp.hasCharacteristics(Spliterator.ORDERED));

        // Basic split tests.
        Spliterator<String> sp1 =  sp.trySplit();
        assertNotNull(sp1);

        ArrayList<String> recorder = new ArrayList<>();
        sp1.forEachRemaining(value -> recorder.add(value));
        sp.forEachRemaining(value -> recorder.add(value));
        Collections.sort(recorder);
        assertEquals(expected, recorder);
    }

    public void test_spliterator_ref_bounds() {
        String[] elements = { "BAD", "EVIL", "a", "b", "c", "d", "e", "f", "g", "h",
                "i", "j", "k", "l", "m", "n", "o", "p", "DO", "NOT", "INCLUDE" };

        ArrayList<String> expected = new ArrayList<>(
                Arrays.asList(Arrays.copyOfRange(elements, 2, 16)));

        SpliteratorTester.runBasicIterationTests(Arrays.spliterator(elements, 2, 16), expected);
        SpliteratorTester.testSpliteratorNPE(Arrays.spliterator(elements, 2, 16));
        assertNotNull(Arrays.spliterator(elements, 2, 16).trySplit());

        Spliterator<String> sp = Arrays.spliterator(elements, 2, 16);
        assertTrue(sp.hasCharacteristics(Spliterator.ORDERED));

        // Basic split tests.
        Spliterator<String> sp1 =  sp.trySplit();
        assertNotNull(sp1);

        ArrayList<String> recorder = new ArrayList<>();
        sp1.forEachRemaining(value -> recorder.add(value));
        sp.forEachRemaining(value -> recorder.add(value));
        Collections.sort(recorder);
        assertEquals(expected, recorder);
    }

    private static class PrimitiveIntArrayList {
        final int[] array;
        int idx;

        PrimitiveIntArrayList(int size) {
            array = new int[size];
        }

        public void add(int element) {
            array[idx++] = element;
        }

        public int[] toSortedArray() {
            Arrays.sort(array);
            return array;
        }
    }

    private static class PrimitiveLongArrayList {
        final long[] array;
        int idx;

        PrimitiveLongArrayList(int size) {
            array = new long[size];
        }

        public void add(long element) {
            array[idx++] = element;
        }

        public long[] toSortedArray() {
            Arrays.sort(array);
            return array;
        }
    }

    private static class PrimitiveDoubleArrayList {
        final double[] array;
        int idx;

        PrimitiveDoubleArrayList(int size) {
            array = new double[size];
        }

        public void add(double element) {
            array[idx++] = element;
        }

        public double[] toSortedArray() {
            Arrays.sort(array);
            return array;
        }
    }

    public void test_spliterator_int() {
        int[] elements = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        Spliterator.OfInt intSp = Arrays.spliterator(elements);

        assertEquals(16, intSp.estimateSize());
        assertEquals(16, intSp.getExactSizeIfKnown());
        assertTrue(intSp.hasCharacteristics(Spliterator.ORDERED));

        assertTrue(intSp.tryAdvance((Integer value) -> assertEquals(1, (int) value)));
        assertTrue(intSp.tryAdvance((int value) -> assertEquals(2, (int) value)));

        PrimitiveIntArrayList recorder = new PrimitiveIntArrayList(16);
        // Record elements observed by previous tests.
        recorder.add(1);
        recorder.add(2);

        Spliterator.OfInt split1 = intSp.trySplit();
        assertNotNull(split1);
        assertTrue(split1.tryAdvance((int value) -> recorder.add(value)));
        assertTrue(split1.tryAdvance((Integer value) -> recorder.add(value)));

        // Assert that splits can themselves resplit.
        Spliterator.OfInt split2 = split1.trySplit();
        assertNotNull(split2);
        split2.forEachRemaining((int value) -> recorder.add(value));
        assertFalse(split2.tryAdvance((int value) -> fail()));
        assertFalse(split2.tryAdvance((Integer value) -> fail()));

        // Iterate over the remaning elements so we can make sure we've looked at
        // everything.
        split1.forEachRemaining((int value) -> recorder.add(value));
        intSp.forEachRemaining((int value) -> recorder.add(value));

        int[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(elements), Arrays.toString(recorded));
    }

    public void test_spliterator_intOffsetBasic() {
        int[] elements = { 123123, 131321312, 1, 2, 3, 4, 32323232, 45454};
        Spliterator.OfInt sp = Arrays.spliterator(elements, 2, 6);

        PrimitiveIntArrayList recorder = new PrimitiveIntArrayList(4);
        sp.tryAdvance((Integer value) -> recorder.add((int) value));
        sp.tryAdvance((int value) -> recorder.add(value));
        sp.forEachRemaining((int value) -> recorder.add(value));

        int[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(new int[] { 1, 2, 3, 4 }), Arrays.toString(recorded));
    }

    public void test_spliterator_longOffsetBasic() {
        long[] elements = { 123123, 131321312, 1, 2, 3, 4, 32323232, 45454};
        Spliterator.OfLong sp = Arrays.spliterator(elements, 2, 6);

        PrimitiveLongArrayList recorder = new PrimitiveLongArrayList(4);
        sp.tryAdvance((Long value) -> recorder.add((long) value));
        sp.tryAdvance((long value) -> recorder.add(value));
        sp.forEachRemaining((long value) -> recorder.add(value));

        long[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(new long[] { 1, 2, 3, 4 }), Arrays.toString(recorded));
    }

    public void test_spliterator_doubleOffsetBasic() {
        double[] elements = { 123123, 131321312, 1, 2, 3, 4, 32323232, 45454};
        Spliterator.OfDouble sp = Arrays.spliterator(elements, 2, 6);

        PrimitiveDoubleArrayList recorder = new PrimitiveDoubleArrayList(4);
        sp.tryAdvance((Double value) -> recorder.add((double) value));
        sp.tryAdvance((double value) -> recorder.add(value));
        sp.forEachRemaining((double value) -> recorder.add(value));

        double[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(new double[] { 1, 2, 3, 4 }), Arrays.toString(recorded));
    }

    public void test_spliterator_long() {
        long[] elements = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        Spliterator.OfLong longSp = Arrays.spliterator(elements);

        assertEquals(16, longSp.estimateSize());
        assertEquals(16, longSp.getExactSizeIfKnown());
        assertTrue(longSp.hasCharacteristics(Spliterator.ORDERED));

        assertTrue(longSp.tryAdvance((Long value) -> assertEquals(1, (long) value)));
        assertTrue(longSp.tryAdvance((long value) -> assertEquals(2, (long) value)));

        PrimitiveLongArrayList recorder = new PrimitiveLongArrayList(16);
        // Record elements observed by previous tests.
        recorder.add(1);
        recorder.add(2);

        Spliterator.OfLong split1 = longSp.trySplit();
        assertNotNull(split1);
        assertTrue(split1.tryAdvance((long value) -> recorder.add(value)));
        assertTrue(split1.tryAdvance((Long value) -> recorder.add(value)));

        // Assert that splits can themselves resplit.
        Spliterator.OfLong split2 = split1.trySplit();
        assertNotNull(split2);
        split2.forEachRemaining((long value) -> recorder.add(value));
        assertFalse(split2.tryAdvance((long value) -> fail()));
        assertFalse(split2.tryAdvance((Long value) -> fail()));

        // Iterate over the remaning elements so we can make sure we've looked at
        // everything.
        split1.forEachRemaining((long value) -> recorder.add(value));
        longSp.forEachRemaining((long value) -> recorder.add(value));

        long[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(elements), Arrays.toString(recorded));
    }


    public void test_spliterator_double() {
        double[] elements = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        Spliterator.OfDouble doubleSp = Arrays.spliterator(elements);

        assertEquals(16, doubleSp.estimateSize());
        assertEquals(16, doubleSp.getExactSizeIfKnown());
        assertTrue(doubleSp.hasCharacteristics(Spliterator.ORDERED));

        assertTrue(doubleSp.tryAdvance((Double value) -> assertEquals(1.0, (double) value)));
        assertTrue(doubleSp.tryAdvance((double value) -> assertEquals(2.0, (double) value)));

        PrimitiveDoubleArrayList recorder = new PrimitiveDoubleArrayList(16);
        // Record elements observed by previous tests.
        recorder.add(1);
        recorder.add(2);

        Spliterator.OfDouble split1 = doubleSp.trySplit();
        assertNotNull(split1);
        assertTrue(split1.tryAdvance((double value) -> recorder.add(value)));
        assertTrue(split1.tryAdvance((Double value) -> recorder.add(value)));

        // Assert that splits can themselves resplit.
        Spliterator.OfDouble split2 = split1.trySplit();
        assertNotNull(split2);
        split2.forEachRemaining((double value) -> recorder.add(value));
        assertFalse(split2.tryAdvance((double value) -> fail()));
        assertFalse(split2.tryAdvance((Double value) -> fail()));

        // Iterate over the remaining elements so we can make sure we've looked at
        // everything.
        split1.forEachRemaining((double value) -> recorder.add(value));
        doubleSp.forEachRemaining((double value) -> recorder.add(value));

        double[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(elements), Arrays.toString(recorded));
    }

    public void test_primitive_spliterators_NPE() {
        final int[] elements = { 1, 2, 3, 4, 5, 6};
        Spliterator.OfInt intSp = Arrays.spliterator(elements);
        try {
            intSp.forEachRemaining((Consumer<Integer>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            intSp.tryAdvance((Consumer<Integer>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            intSp.forEachRemaining((IntConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            intSp.tryAdvance((IntConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }

        final long[] longElements = { 1, 2, 3, 4, 5, 6};
        Spliterator.OfLong longSp = Arrays.spliterator(longElements);
        try {
            longSp.forEachRemaining((Consumer<Long>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            longSp.tryAdvance((Consumer<Long>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            longSp.forEachRemaining((LongConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            longSp.tryAdvance((LongConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }

        final double[] doubleElements = { 1, 2, 3, 4, 5, 6};
        Spliterator.OfDouble doubleSp = Arrays.spliterator(doubleElements);
        try {
            doubleSp.forEachRemaining((Consumer<Double>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            doubleSp.tryAdvance((Consumer<Double>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            doubleSp.forEachRemaining((DoubleConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            doubleSp.tryAdvance((DoubleConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$B(int size) {
        if (size % 256 != 0) {
            fail("test_parallelSort$B size needs to be dividable by 256");
        }
        int mul256Count = size / 256;
        byte[] sortedArray = new byte[size];
        byte curentValue = Byte.MIN_VALUE;
        for (int counter = 0; counter < size; counter++) {
            sortedArray[counter] = curentValue;
            if (counter != 0 && counter % mul256Count == 0) {
                curentValue++;
            }
        }
        byte[] reversedArray = new byte[size];
        for (int counter = 0; counter < size; counter++) {
            reversedArray[counter] = sortedArray[size - counter - 1];
        }

        Arrays.parallelSort(reversedArray);
        assertTrue(Arrays.equals(sortedArray, reversedArray));
    }

    /**
     * java.util.Arrays#parallelSort(byte[])
     */
    public void test_parallelSort$B() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$B(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$B(256 * 64);
        }
    }

    private void test_parallelSort$BII(int size) {
        int startIndex = 100;
        int endIndex = size - 100;
        byte[] reversedArray = new byte[size];
        byte[] originalReversedArray = new byte[size];
        Arrays.fill(reversedArray, 0 , startIndex, (byte)100);
        Arrays.fill(reversedArray, endIndex, size, (byte)100);
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = (byte) (size - counter - startIndex - 1);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Arrays.parallelSort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(byte[], int, int)
     */
    public void test_parallelSort$BII() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$BII(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$BII(256 * 64);
        }
    }

    /**
     * java.util.Arrays#parallelSort(byte[]) & (byte[], int, int) NPE
     */
    public void test_parallelSort$B_NPE() {
        byte[] byte_array_null = null;
        try {
            java.util.Arrays.parallelSort(byte_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
        try {
            java.util.Arrays.parallelSort(byte_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$C(int size) {
        char[] sortedArray = new char[size];
        for (int counter = 0; counter < size; counter++)
            sortedArray[counter] = (char)(Short.MIN_VALUE + counter);
        char[] reversedArray = new char[size];
        for (int counter = 0; counter < size; counter++) {
            reversedArray[counter] = sortedArray[size - counter - 1];
        }
        Arrays.parallelSort(reversedArray);
        assertTrue(Arrays.equals(sortedArray, reversedArray));
    }

    /**
     * java.util.Arrays#parallelSort(char[])
     */
    public void test_parallelSort$C() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$C(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$C(256 * 64);
        }
    }

    private void test_parallelSort$CII(int size) {
        int startIndex = 100;
        int endIndex = size - 100;
        char[] reversedArray = new char[size];
        char[] originalReversedArray = new char[size];

        Arrays.fill(reversedArray, 0 , startIndex, (char)100);
        Arrays.fill(reversedArray, endIndex, size, (char)100);
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = (char)(size - counter - startIndex - 1);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Arrays.parallelSort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(char[], int, int)
     */
    public void test_parallelSort$CII() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$CII(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$CII(256 * 64);
        }
    }

    /**
     * java.util.Arrays#parallelSort(char[]) & (char[], int, int) NPE
     */
    public void test_parallelSort$C_NPE() {
        char[] char_array_null = null;
        try {
            java.util.Arrays.parallelSort(char_array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
        try {
            java.util.Arrays.parallelSort(char_array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$S(int size) {
        short[] sortedArray = new short[size];
        for (int counter = 0; counter < size; counter++)
            sortedArray[counter] = (short)(Short.MIN_VALUE + counter);
        short[] reversedArray = new short[size];
        for (int counter = 0; counter < size; counter++) {
            reversedArray[counter] = sortedArray[size - counter - 1];
        }
        Arrays.parallelSort(reversedArray);
        assertTrue(Arrays.equals(sortedArray, reversedArray));
    }

    /**
     * java.util.Arrays#parallelSort(short[])
     */
    public void test_parallelSort$S() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$S(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$S(256 * 64);
        }
    }

    private void test_parallelSort$SII(int size) {
        int startIndex = 100;
        int endIndex = size - 100;
        short[] reversedArray = new short[size];
        short[] originalReversedArray = new short[size];

        Arrays.fill(reversedArray, 0 , startIndex, (short)100);
        Arrays.fill(reversedArray, endIndex, size, (short)100);
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = (short)(size - counter - startIndex - 1);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Arrays.parallelSort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(short[], int, int)
     */
    public void test_parallelSort$SII() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$SII(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$SII(256 * 64);
        }
    }

    /**
     * java.util.Arrays#parallelSort(short[]) & (short[], int, int) NPE
     */
    public void test_parallelSort$S_NPE() {
        short[] array_null = null;
        try {
            java.util.Arrays.parallelSort(array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
        try {
            java.util.Arrays.parallelSort(array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$I(int size) {
        int[] sortedArray = new int[size];
        for (int counter = 0; counter < size; counter++)
            sortedArray[counter] = (int)(Integer.MIN_VALUE + counter);
        int[] reversedArray = new int[size];
        for (int counter = 0; counter < size; counter++) {
            reversedArray[counter] = sortedArray[size - counter - 1];
        }
        Arrays.parallelSort(reversedArray);
        assertTrue(Arrays.equals(sortedArray, reversedArray));
    }

    /**
     * java.util.Arrays#parallelSort(int[])
     */
    public void test_parallelSort$I() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$I(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$I(256 * 64);
        }
    }

    private void test_parallelSort$III(int size) {
        int startIndex = 100;
        int endIndex = size - 100;
        int[] reversedArray = new int[size];
        int[] originalReversedArray = new int[size];

        Arrays.fill(reversedArray, 0 , startIndex, (int)100);
        Arrays.fill(reversedArray, endIndex, size, (int)100);
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = (int)(size - counter - startIndex - 1);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Arrays.parallelSort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(int[], int, int)
     */
    public void test_parallelSort$III() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$III(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$III(256 * 64);
        }
    }

    /**
     * java.util.Arrays#parallelSort(int[]) & (int[], int, int) NPE
     */
    public void test_parallelSort$I_NPE() {
        int[] array_null = null;
        try {
            java.util.Arrays.parallelSort(array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
        try {
            java.util.Arrays.parallelSort(array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$J(int size) {
        long[] reversedArray = new long[size];
        for (int counter = 0; counter < size; counter++)
            reversedArray[counter] = (long)(size - counter - 1);
        Arrays.parallelSort(reversedArray);

        for (int counter = 0; counter < size; counter++)
            assertTrue("Resulting array not sorted",
                    reversedArray[counter] == (long) counter);
    }

    /**
     * java.util.Arrays#parallelSort(long[])
     */
    public void test_parallelSort$J() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$J(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$J(256 * 64);
        }
    }

    private void test_parallelSort$JII(int size) {
        int startIndex = 100;
        int endIndex = size - 100;
        long[] reversedArray = new long[size];
        long[] originalReversedArray = new long[size];

        Arrays.fill(reversedArray, 0 , startIndex, (long)100);
        Arrays.fill(reversedArray, endIndex, size, (long)100);
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = (long)(size - counter - startIndex - 1);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Arrays.parallelSort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(long[], int, int)
     */
    public void test_parallelSort$JII() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$JII(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$JII(256 * 64);
        }
    }

    /**
     * java.util.Arrays#parallelSort(long[]) & (long[], int, int) NPE
     */
    public void test_parallelSort$J_NPE() {
        long[] array_null = null;
        try {
            java.util.Arrays.parallelSort(array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
        try {
            java.util.Arrays.parallelSort(array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$D(int size) {
        double[] sortedArray = new double[size];
        for (int counter = 0; counter < size; counter++)
            sortedArray[counter] = (double)(counter);
        double[] reversedArray = new double[size];
        for (int counter = 0; counter < size; counter++) {
            reversedArray[counter] = sortedArray[size - counter - 1];
        }
        Arrays.parallelSort(reversedArray);
        assertTrue(Arrays.equals(sortedArray, reversedArray));
    }

    /**
     * java.util.Arrays#parallelSort(double[])
     */
    public void test_parallelSort$D() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$D(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$D(256 * 64);
        }
    }

    private void test_parallelSort$DII(int size) {
        int startIndex = 100;
        int endIndex = size-100;
        double[] reversedArray = new double[size];
        double[] originalReversedArray = new double[size];

        Arrays.fill(reversedArray, 0 , startIndex, (double)100);
        Arrays.fill(reversedArray, endIndex, size, (double)100);
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = (double) (size - counter - startIndex - 1);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Arrays.parallelSort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(double[], int, int)
     */
    public void test_parallelSort$DII() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$DII(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$DII(64*256);
        }
    }

    /**
     * java.util.Arrays#parallelSort(double[]) & (double[], int, int) NPE
     */
    public void test_parallelSort$D_NPE() {
        double[] array_null = null;
        try {
            java.util.Arrays.parallelSort(array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
        try {
            java.util.Arrays.parallelSort(array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$F(int size) {
        float[] sortedArray = new float[size];
        for (int counter = 0; counter < size; counter++)
            sortedArray[counter] = (float)(counter);
        float[] reversedArray = new float[size];
        for (int counter = 0; counter < size; counter++) {
            reversedArray[counter] = sortedArray[size - counter - 1];
        }
        Arrays.parallelSort(reversedArray);
        assertTrue(Arrays.equals(sortedArray, reversedArray));
    }

    /**
     * java.util.Arrays#parallelSort(float[])
     */
    public void test_parallelSort$F() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$F(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$F(256 * 64);
        }
    }

    private void test_parallelSort$FII(int size) {
        int startIndex = 100;
        int endIndex = size-100;
        float[] reversedArray = new float[size];
        float[] originalReversedArray = new float[size];

        Arrays.fill(reversedArray, 0 , startIndex, (float)100);
        Arrays.fill(reversedArray, endIndex, size, (float)100);
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = (float) (size - counter - startIndex - 1);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Arrays.parallelSort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       reversedArray[counter] <= reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(float[], int, int)
     */
    public void test_parallelSort$FII() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$FII(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$FII(64*256);
        }
    }

    /**
     * java.util.Arrays#parallelSort(float[]) & (float[], int, int) NPE
     */
    public void test_parallelSort$F_NPE() {
        float[] array_null = null;
        try {
            java.util.Arrays.parallelSort(array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {

        }
        try {
            java.util.Arrays.parallelSort(array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$Ljava_lang_Comparable(int size) {
        Comparable[] sortedArray = new Comparable[size];
        for (int counter = 0; counter < size; counter++)
            sortedArray[counter] = new Integer(counter);
        Comparable[] reversedArray = new Comparable[size];
        for (int counter = 0; counter < size; counter++) {
            reversedArray[counter] = sortedArray[size - counter - 1];
        }
        Arrays.parallelSort(reversedArray);
        assertTrue(Arrays.equals(sortedArray, reversedArray));

        Arrays.fill(reversedArray, 0, reversedArray.length/2, "String");
        Arrays.fill(reversedArray, reversedArray.length/2, reversedArray.length, new Integer(1));

        try {
            Arrays.sort(reversedArray);
            fail("ClassCastException expected");
        } catch (ClassCastException expected) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(java.lang.Comparable[])
     */
    public void test_parallelSort$Ljava_lang_Comparable() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$Ljava_lang_Comparable(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$Ljava_lang_Comparable(256 * 64);
        }
    }

    private void test_parallelSort$Ljava_lang_ComparableII(int size) {
        int startIndex = 100;
        int endIndex = size-100;
        Comparable[] reversedArray = new Comparable[size];
        Comparable[] originalReversedArray = new Comparable[size];
        Arrays.fill(reversedArray, 0 , startIndex, new Integer(100));
        Arrays.fill(reversedArray, endIndex, size, new Integer(100));
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = new Integer(size - counter - startIndex - 1);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Arrays.parallelSort(reversedArray, startIndex, endIndex);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       (int)(Integer)reversedArray[counter] <= (int)reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(java.lang.Comparable[], int, int)
     */
    public void test_parallelSort$Ljava_lang_ComparableII() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$Ljava_lang_ComparableII(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$Ljava_lang_ComparableII(64*256);
        }
    }


    /**
     * java.util.Arrays#parallelSort(java_lang_Comparable[]) & (java_lang_Comparable[], int, int) NPE
     */
    public void test_parallelSort$Ljava_lang_Comparable_NPE() {
        Comparable[] array_null = null;
        try {
            java.util.Arrays.parallelSort(array_null);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {

        }
        try {
            java.util.Arrays.parallelSort(array_null, (int) -1, (int) 1);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private void test_parallelSort$Ljava_lang_ObjectLjava_util_Comparator(int size) {
        Object[] reversedArray = new Object[size];
        for (int counter = 0; counter < size; counter++)
            reversedArray[counter] = new Integer(counter);
        Comparator comparator = new ReversedIntegerComparator();
        Arrays.parallelSort(reversedArray, comparator);

        for (int counter = 0; counter < size; counter++)
            assertTrue("Resulting array not sorted",
                       (int)(reversedArray[counter]) == (size - counter -1 ));

        Arrays.fill(reversedArray, 0, reversedArray.length/2, "String");
        Arrays.fill(reversedArray, reversedArray.length/2, reversedArray.length, new Integer(1));

        try {
            Arrays.sort(reversedArray, comparator);
            fail("ClassCastException expected");
        } catch (ClassCastException expected) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(java.lang.Object[], java.util.Comparator)
     */
    public void test_parallelSort$Ljava_lang_Objectjava_util_Comparator() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$Ljava_lang_ObjectLjava_util_Comparator(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$Ljava_lang_ObjectLjava_util_Comparator(256 * 64);
        }
    }

    private void test_parallelSort$Ljava_lang_ObjectLjava_util_ComparatorII(int size) {
        int startIndex = 100;
        int endIndex = size-100;
        Integer[] reversedArray = new Integer[size];
        Integer[] originalReversedArray = new Integer[size];
        Arrays.fill(reversedArray, 0 , startIndex, new Integer(100));
        Arrays.fill(reversedArray, endIndex, size, new Integer(100));
        for (int counter = startIndex; counter < endIndex; counter++) {
            reversedArray[counter] = new Integer(counter - startIndex);
        }
        System.arraycopy(reversedArray, 0, originalReversedArray, 0, size);

        Comparator comparator = new ReversedIntegerComparator();
        Arrays.parallelSort(reversedArray, startIndex, endIndex, comparator);
        for (int counter = 0; counter < startIndex; counter++)
            assertTrue("Array modified outside of bounds",
                 reversedArray[counter] == originalReversedArray[counter]);
        for (int counter = startIndex; counter < endIndex - 1; counter++)
            assertTrue("Array not sorted within bounds",
                       (int)(Integer)reversedArray[counter] >= (int)reversedArray[counter + 1]);
        for (int counter = endIndex; counter < arraySize; counter++)
            assertTrue("Array modified outside of bounds",
                       reversedArray[counter] == originalReversedArray[counter]);

        //exception testing
        try {
            Arrays.parallelSort(reversedArray, startIndex + 1, startIndex, comparator);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, -1, startIndex, comparator);
            fail("ArrayIndexOutOfBoundsException expected (1)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }

        try {
            Arrays.parallelSort(reversedArray, startIndex, reversedArray.length + 1, comparator);
            fail("ArrayIndexOutOfBoundsException expected (2)");
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
    }

    /**
     * java.util.Arrays#parallelSort(java.lang.Object[], int, int, java.util.Comparator)
     */
    public void test_parallelSort$Ljava_lang_ObjectLjava_util_ComparatorII() {
        // This will result in single thread sort
        assertTrue(256 <= Arrays.MIN_ARRAY_SORT_GRAN);
        test_parallelSort$Ljava_lang_ObjectLjava_util_ComparatorII(256);
        // This should trigger true parallel sort
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            assertTrue(256 * 64 > Arrays.MIN_ARRAY_SORT_GRAN);
            test_parallelSort$Ljava_lang_ObjectLjava_util_ComparatorII(64*256);
        }
    }

    /**
     * java.util.Arrays#parallelSort(Object[],Comparator) & (Object[], int, int, Comparator) NPE
     */
    public void test_parallelSort$Ljava_lang_ObjectLjava_util_Comparator_NPE() {
        Object[] array_null = null;
        Comparator comparator = new ReversedIntegerComparator();
        try {
            java.util.Arrays.parallelSort(array_null, comparator);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {

        }
        try {
            java.util.Arrays.parallelSort(array_null, (int) -1, (int) 1, comparator);
            fail("Should throw java.lang.NullPointerException");
        } catch (NullPointerException expected) {
        }
    }


    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        objArray = null;
        booleanArray = null;
        byteArray = null;
        charArray = null;
        doubleArray = null;
        floatArray = null;
        intArray = null;
        longArray = null;
        objectArray = null;
        shortArray = null;
    }
}
