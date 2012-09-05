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
package org.apache.harmony.luni.tests.java.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import tests.support.Support_UnmodifiableCollectionTest;

public class ArraysTest extends junit.framework.TestCase {

	public static class ReversedIntegerComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return -(((Integer) o1).compareTo((Integer) o2));
		}

		public boolean equals(Object o1, Object o2) {
			return ((Integer) o1).compareTo((Integer) o2) == 0;
		}
	}

    static class MockComparable implements Comparable{
        public int compareTo(Object o) {
            return 0;
        }
    }

	final static int arraySize = 100;

	static Object[] objArray;

	static boolean[] booleanArray;

	static byte[] byteArray;

	static char[] charArray;

	static double[] doubleArray;

	static float[] floatArray;

	static int[] intArray;

	static long[] longArray;

	static Object[] objectArray;

	static short[] shortArray;
	{
		objArray = new Object[arraySize];
		for (int i = 0; i < objArray.length; i++)
			objArray[i] = new Integer(i);
	}

	/**
	 * @tests java.util.Arrays#asList(java.lang.Object[])
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
			Arrays.asList((Object[])null);
			fail("asList with null arg didn't throw NPE");
		} catch (NullPointerException e) {
			// Expected
		}
	}

	/**
	 * @tests java.util.Arrays#binarySearch(byte[], byte)
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
	 * @tests java.util.Arrays#binarySearch(char[], char)
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
	 * @tests java.util.Arrays#binarySearch(double[], double)
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
	 * @tests java.util.Arrays#binarySearch(float[], float)
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
	 * @tests java.util.Arrays#binarySearch(int[], int)
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
	 * @tests java.util.Arrays#binarySearch(long[], long)
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
	 * @tests java.util.Arrays#binarySearch(java.lang.Object[],
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
	 * @tests java.util.Arrays#binarySearch(java.lang.Object[],
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
	 * @tests java.util.Arrays#binarySearch(short[], short)
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

	/**
	 * @tests java.util.Arrays#fill(byte[], byte)
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
	 * @tests java.util.Arrays#fill(byte[], int, int, byte)
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
	 * @tests java.util.Arrays#fill(short[], short)
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
	 * @tests java.util.Arrays#fill(short[], int, int, short)
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
	}

	/**
	 * @tests java.util.Arrays#fill(char[], char)
	 */
	public void test_fill$CC() {
		// Test for method void java.util.Arrays.fill(char [], char)

		char d[] = new char[1000];
		Arrays.fill(d, 'V');
		for (int i = 0; i < d.length; i++)
			assertEquals("Failed to fill char array correctly", 'V', d[i]);
	}

	/**
	 * @tests java.util.Arrays#fill(char[], int, int, char)
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
	}

	/**
	 * @tests java.util.Arrays#fill(int[], int)
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
	 * @tests java.util.Arrays#fill(int[], int, int, int)
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
	}

	/**
	 * @tests java.util.Arrays#fill(long[], long)
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
	 * @tests java.util.Arrays#fill(long[], int, int, long)
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
	}

	/**
	 * @tests java.util.Arrays#fill(float[], float)
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
	 * @tests java.util.Arrays#fill(float[], int, int, float)
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
	}

	/**
	 * @tests java.util.Arrays#fill(double[], double)
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
	 * @tests java.util.Arrays#fill(double[], int, int, double)
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
	}

	/**
	 * @tests java.util.Arrays#fill(boolean[], boolean)
	 */
	public void test_fill$ZZ() {
		// Test for method void java.util.Arrays.fill(boolean [], boolean)

		boolean d[] = new boolean[1000];
		Arrays.fill(d, true);
		for (int i = 0; i < d.length; i++)
			assertTrue("Failed to fill boolean array correctly", d[i]);
	}

	/**
	 * @tests java.util.Arrays#fill(boolean[], int, int, boolean)
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
	}

	/**
	 * @tests java.util.Arrays#fill(java.lang.Object[], java.lang.Object)
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
	 * @tests java.util.Arrays#fill(java.lang.Object[], int, int,
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
	}

	/**
	 * @tests java.util.Arrays#equals(byte[], byte[])
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
	 * @tests java.util.Arrays#equals(short[], short[])
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
	 * @tests java.util.Arrays#equals(char[], char[])
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
	 * @tests java.util.Arrays#equals(int[], int[])
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
	 * @tests java.util.Arrays#equals(long[], long[])
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
	 * @tests java.util.Arrays#equals(float[], float[])
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
	 * @tests java.util.Arrays#equals(double[], double[])
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
	 * @tests java.util.Arrays#equals(boolean[], boolean[])
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
	 * @tests java.util.Arrays#equals(java.lang.Object[], java.lang.Object[])
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
	 * @tests java.util.Arrays#sort(byte[])
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
	 * @tests java.util.Arrays#sort(byte[], int, int)
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

		//exception order testing
		try {
			Arrays.sort(new byte[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ignore) {
		}
	}

	/**
	 * @tests java.util.Arrays#sort(char[])
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
	 * @tests java.util.Arrays#sort(char[], int, int)
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

		//exception order testing
		try {
			Arrays.sort(new char[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ignore) {
		}
	}

	/**
	 * @tests java.util.Arrays#sort(double[])
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
                Double.NaN, 1.0, 3.0, -0.0};
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
	 * @tests java.util.Arrays#sort(double[], int, int)
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

		//exception order testing
		try {
			Arrays.sort(new double[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ignore) {
		}
	}

	/**
	 * @tests java.util.Arrays#sort(float[])
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
	 * @tests java.util.Arrays#sort(float[], int, int)
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

		//exception order testing
		try {
			Arrays.sort(new float[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ignore) {
		}
	}

	/**
	 * @tests java.util.Arrays#sort(int[])
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
	 * @tests java.util.Arrays#sort(int[], int, int)
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

		//exception order testing
		try {
			Arrays.sort(new int[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ignore) {
		}
	}

	/**
	 * @tests java.util.Arrays#sort(long[])
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
	 * @tests java.util.Arrays#sort(long[], int, int)
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

		//exception order testing
		try {
			Arrays.sort(new long[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ignore) {
		}
	}

	/**
	 * @tests java.util.Arrays#sort(java.lang.Object[])
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
	}

	/**
	 * @tests java.util.Arrays#sort(java.lang.Object[], int, int)
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

		//exception order testing
		try {
			Arrays.sort(new Object[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ignore) {
		}
	}

	/**
	 * @tests java.util.Arrays#sort(java.lang.Object[], int, int,
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
	}

	/**
	 * @tests java.util.Arrays#sort(java.lang.Object[], java.util.Comparator)
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
	 * @tests java.util.Arrays#sort(short[])
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
	 * @tests java.util.Arrays#sort(short[], int, int)
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

		//exception order testing
		try {
			Arrays.sort(new short[1], startIndex + 1, startIndex);
            fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException ignore) {
		}
	}

    /**
     * @tests java.util.Arrays#sort(byte[], int, int)
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
     * @tests java.util.Arrays#sort(char[], int, int)
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
     * @tests java.util.Arrays#sort(double[], int, int)
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
     * @tests java.util.Arrays#sort(float[], int, int)
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
     * @tests java.util.Arrays#sort(int[], int, int)
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
     * @tests java.util.Arrays#sort(Object[], int, int)
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
     * @tests java.util.Arrays#sort(long[], int, int)
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
     * @tests java.util.Arrays#sort(short[], int, int)
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

    /**
     * @tests java.util.Arrays#deepEquals(Object[], Object[])
     */
    /* TODO(user): enable when Arrays.deepEquals is implemented.
    public void test_deepEquals$Ljava_lang_ObjectLjava_lang_Object() {
       int [] a1 = {1, 2, 3};
       short [] a2 = {0, 1};
       Object [] a3 = {new Integer(1), a2};
       int [] a4 = {6, 5, 4};

       int [] b1 = {1, 2, 3};
       short [] b2 = {0, 1};
       Object [] b3 = {new Integer(1), b2};

       Object a [] = {a1, a2, a3};
       Object b [] = {b1, b2, b3};

       assertFalse(Arrays.equals(a, b));
       assertTrue(Arrays.deepEquals(a,b));

       a[2] = a4;

       assertFalse(Arrays.deepEquals(a, b));
    }
    */

    /**
     * @tests java.util.Arrays#deepHashCode(Object[])
     */
    /* TODO(user): enable when Arrays.deepHashCode is implemented.
    public void test_deepHashCode$Ljava_lang_Object() {
        int [] a1 = {1, 2, 3};
        short [] a2 = {0, 1};
        Object [] a3 = {new Integer(1), a2};

        int [] b1 = {1, 2, 3};
        short [] b2 = {0, 1};
        Object [] b3 = {new Integer(1), b2};

        Object a [] = {a1, a2, a3};
        Object b [] = {b1, b2, b3};

        int deep_hash_a = Arrays.deepHashCode(a);
        int deep_hash_b = Arrays.deepHashCode(b);

        assertEquals(deep_hash_a, deep_hash_b);
     }
     */

    /**
     * @tests java.util.Arrays#hashCode(boolean[] a)
     */
    public void test_hashCode$LZ() {
        int listHashCode;
        int arrayHashCode;

        boolean [] boolArr = {true, false, false, true, false};
        List listOfBoolean = new LinkedList();
        for (int i = 0; i < boolArr.length; i++) {
            listOfBoolean.add(new Boolean(boolArr[i]));
        }
        listHashCode = listOfBoolean.hashCode();
        arrayHashCode = Arrays.hashCode(boolArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * @tests java.util.Arrays#hashCode(int[] a)
     */
    public void test_hashCode$LI() {
        int listHashCode;
        int arrayHashCode;

        int [] intArr = {10, 5, 134, 7, 19};
        List listOfInteger = new LinkedList();

        for (int i = 0; i < intArr.length; i++) {
            listOfInteger.add(new Integer(intArr[i]));
        }
        listHashCode = listOfInteger.hashCode();
        arrayHashCode = Arrays.hashCode(intArr);
        assertEquals(listHashCode, arrayHashCode);

        int [] intArr2 = {10, 5, 134, 7, 19};
        assertEquals(Arrays.hashCode(intArr2), Arrays.hashCode(intArr));
    }

    /**
     * @tests java.util.Arrays#hashCode(char[] a)
     */
    public void test_hashCode$LC() {
        int listHashCode;
        int arrayHashCode;

        char [] charArr = {'a', 'g', 'x', 'c', 'm'};
        List listOfCharacter = new LinkedList();
        for (int i = 0; i < charArr.length; i++) {
            listOfCharacter.add(new Character(charArr[i]));
        }
        listHashCode = listOfCharacter.hashCode();
        arrayHashCode = Arrays.hashCode(charArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * @tests java.util.Arrays#hashCode(byte[] a)
     */
    public void test_hashCode$LB() {
        int listHashCode;
        int arrayHashCode;

        byte [] byteArr = {5, 9, 7, 6, 17};
        List listOfByte = new LinkedList();
        for (int i = 0; i < byteArr.length; i++) {
            listOfByte.add(new Byte(byteArr[i]));
        }
        listHashCode = listOfByte.hashCode();
        arrayHashCode = Arrays.hashCode(byteArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * @tests java.util.Arrays#hashCode(long[] a)
     */
    public void test_hashCode$LJ() {
        int listHashCode;
        int arrayHashCode;

        long [] longArr = {67890234512l, 97587236923425l, 257421912912l,
                6754268100l, 5};
        List listOfLong = new LinkedList();
        for (int i = 0; i < longArr.length; i++) {
            listOfLong.add(new Long(longArr[i]));
        }
        listHashCode = listOfLong.hashCode();
        arrayHashCode = Arrays.hashCode(longArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * @tests java.util.Arrays#hashCode(float[] a)
     */
    public void test_hashCode$LF() {
        int listHashCode;
        int arrayHashCode;

        float [] floatArr = {0.13497f, 0.268934f, 12e-5f, -3e+2f, 10e-4f};
        List listOfFloat = new LinkedList();
        for (int i = 0; i < floatArr.length; i++) {
            listOfFloat.add(new Float(floatArr[i]));
        }
        listHashCode = listOfFloat.hashCode();
        arrayHashCode = Arrays.hashCode(floatArr);
        assertEquals(listHashCode, arrayHashCode);

        float [] floatArr2 = {0.13497f, 0.268934f, 12e-5f, -3e+2f, 10e-4f};
        assertEquals(Arrays.hashCode(floatArr2), Arrays.hashCode(floatArr));
    }

    /**
     * @tests java.util.Arrays#hashCode(double[] a)
     */
    public void test_hashCode$LD() {
        int listHashCode;
        int arrayHashCode;

        double [] doubleArr = {0.134945657, 0.0038754, 11e-150, -30e-300, 10e-4};
        List listOfDouble = new LinkedList();
        for (int i = 0; i < doubleArr.length; i++) {
            listOfDouble.add(new Double(doubleArr[i]));
        }
        listHashCode = listOfDouble.hashCode();
        arrayHashCode = Arrays.hashCode(doubleArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * @tests java.util.Arrays#hashCode(short[] a)
     */
    public void test_hashCode$LS() {
        int listHashCode;
        int arrayHashCode;

        short [] shortArr = {35, 13, 45, 2, 91};
        List listOfShort = new LinkedList();
        for (int i = 0; i < shortArr.length; i++) {
            listOfShort.add(new Short(shortArr[i]));
        }
        listHashCode = listOfShort.hashCode();
        arrayHashCode = Arrays.hashCode(shortArr);
        assertEquals(listHashCode, arrayHashCode);
    }

    /**
     * @tests java.util.Arrays#hashCode(Object[] a)
     */
    public void test_hashCode$Ljava_lang_Object() {
        int listHashCode;
        int arrayHashCode;

        Object[] objectArr = {new Integer(1), new Float(10e-12f), null};
        List listOfObject= new LinkedList();
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
     * @tests java.util.Arrays#swap(int, int, Object[])
     */
    /* TODO(user): enable when reflection supports method parameters.
    public void test_swap_I_I_$Ljava_lang_Object() throws Exception {
    	Method m = Arrays.class.getDeclaredMethod("swap", int.class, int.class, Object[].class);
    	m.setAccessible(true);
    	Integer[] arr = {new Integer(0), new Integer(1), new Integer(2)};
    	m.invoke(null,0, 1, arr);
    	assertEquals("should be equal to 1",1, arr[0].intValue());
    	assertEquals("should be equal to 0",0, arr[1].intValue());
    }
    */

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
