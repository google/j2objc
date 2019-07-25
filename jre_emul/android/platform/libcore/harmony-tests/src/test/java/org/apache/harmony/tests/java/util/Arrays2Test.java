/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tests.java.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.RandomAccess;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

public class Arrays2Test extends TestCase {

    /**
     * java.util.Arrays#binarySearch(double[], double)
     */
    public void test_binarySearch$DD() {
        double[] specials = new double[] { Double.NEGATIVE_INFINITY,
                -Double.MAX_VALUE, -2d, -Double.MIN_VALUE, -0d, 0d,
                Double.MIN_VALUE, 2d, Double.MAX_VALUE,
                Double.POSITIVE_INFINITY, Double.NaN };

        for (int i = 0; i < specials.length; i++) {
            int result = Arrays.binarySearch(specials, specials[i]);
            assertTrue("Assert 0: " + specials[i] + " invalid: " + result,
                    result == i);
        }
        assertEquals("Assert 1: Invalid search index for -1d",
                -4, Arrays.binarySearch(specials, -1d));
        assertEquals("Assert 2: Invalid search index for 1d",
                -8, Arrays.binarySearch(specials, 1d));
    }

    /**
     * java.util.Arrays#binarySearch(float[], float)
     */
    public void test_binarySearch$FF() {
        float[] specials = new float[] { Float.NEGATIVE_INFINITY,
                -Float.MAX_VALUE, -2f, -Float.MIN_VALUE, -0f, 0f,
                Float.MIN_VALUE, 2f, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
                Float.NaN };

        for (int i = 0; i < specials.length; i++) {
            int result = Arrays.binarySearch(specials, specials[i]);
            assertTrue("Assert 0: " + specials[i] + " invalid: " + result,
                    result == i);
        }
        assertEquals("Assert 1: Invalid search index for -1f",
                -4, Arrays.binarySearch(specials, -1f));
        assertEquals("Assert 2: Invalid search index for 1f",
                -8, Arrays.binarySearch(specials, 1f));
    }

    /**
     * java.util.Arrays#equals(double[], double[])
     */
    public void test_equals$D$D() {
        double d[] = new double[100];
        double x[] = new double[100];
        Arrays.fill(d, Double.MAX_VALUE);
        Arrays.fill(x, Double.MIN_VALUE);

        assertTrue("Assert 0: Inequal arrays returned true", !Arrays.equals(d, x));

        Arrays.fill(x, Double.MAX_VALUE);
        assertTrue("Assert 1: equal arrays returned false", Arrays.equals(d, x));

        assertTrue("Assert 2: should be false",
                !Arrays.equals(new double[] { 1.0 }, new double[] { 2.0 }));

        assertTrue("Assert 3: NaN not equals",
                Arrays.equals(new double[] { Double.NaN }, new double[] { Double.NaN }));
        assertTrue("Assert 4: 0d equals -0d",
                !Arrays.equals(new double[] { 0d }, new double[] { -0d }));
    }

    /**
     * java.util.Arrays#equals(float[], float[])
     */
    public void test_equals$F$F() {
        float d[] = new float[100];
        float x[] = new float[100];
        Arrays.fill(d, Float.MAX_VALUE);
        Arrays.fill(x, Float.MIN_VALUE);

        assertTrue("Assert 0: Inequal arrays returned true", !Arrays.equals(d, x));

        Arrays.fill(x, Float.MAX_VALUE);
        assertTrue("Assert 1: equal arrays returned false", Arrays.equals(d, x));

        assertTrue("Assert 2: NaN not equals",
                Arrays.equals(new float[] { Float.NaN }, new float[] { Float.NaN }));
        assertTrue("Assert 3: 0f equals -0f",
                !Arrays.equals(new float[] { 0f }, new float[] { -0f }));
    }

    /**
     * java.util.Arrays#sort(double[])
     */
    public void test_sort$D() {
        // Test a basic sort
        double[] reversedArray = new double[100];
        for (int counter = 0; counter < reversedArray.length; counter++) {
            reversedArray[counter] = (reversedArray.length - counter - 1);
        }
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < reversedArray.length; counter++) {
            assertTrue("Assert 0: Resulting array not sorted",
                    reversedArray[counter] == counter);
        }

        // These have to sort as per the Double compare ordering
        double[] specials1 = new double[] { Double.NaN, Double.MAX_VALUE, Double.MIN_VALUE, 0d, -0d, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        double[] specials2 = new double[] { 0d, Double.POSITIVE_INFINITY, -0d, Double.NEGATIVE_INFINITY, Double.MIN_VALUE, Double.NaN, Double.MAX_VALUE };
        double[] answer = new double[] { Double.NEGATIVE_INFINITY, -0d, 0d, Double.MIN_VALUE, Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN };

        Arrays.sort(specials1);
        Object[] print1 = new Object[specials1.length];
        for (int i = 0; i < specials1.length; i++) {
            print1[i] = new Double(specials1[i]);
        }
        assertTrue("Assert 1: specials sort incorrectly" + Arrays.asList(print1),
                Arrays.equals(specials1, answer));

        Arrays.sort(specials2);
        Object[] print2 = new Object[specials2.length];
        for (int i = 0; i < specials2.length; i++) {
            print2[i] = new Double(specials2[i]);
        }
        assertTrue("Assert 2: specials sort incorrectly " + Arrays.asList(print2),
                Arrays.equals(specials2, answer));
    }

    /**
     * java.util.Arrays#sort(float[])
     */
    public void test_sort$F() {
        // Test a basic sort
        float[] reversedArray = new float[100];
        for (int counter = 0; counter < reversedArray.length; counter++) {
            reversedArray[counter] = (reversedArray.length - counter - 1);
        }
        Arrays.sort(reversedArray);
        for (int counter = 0; counter < reversedArray.length; counter++) {
            assertTrue("Assert 0: Resulting array not sorted",
                    reversedArray[counter] == counter);
        }

        float[] specials1 = new float[] { Float.NaN, Float.MAX_VALUE, Float.MIN_VALUE, 0f, -0f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY };
        float[] specials2 = new float[] { 0f, Float.POSITIVE_INFINITY, -0f, Float.NEGATIVE_INFINITY, Float.MIN_VALUE, Float.NaN, Float.MAX_VALUE };
        float[] answer = new float[] { Float.NEGATIVE_INFINITY, -0f, 0f, Float.MIN_VALUE, Float.MAX_VALUE, Float.POSITIVE_INFINITY, Float.NaN };

        Arrays.sort(specials1);
        Object[] print1 = new Object[specials1.length];
        for (int i = 0; i < specials1.length; i++) {
            print1[i] = new Float(specials1[i]);
        }
        assertTrue("Assert 1: specials sort incorrectly" + Arrays.asList(print1),
                Arrays.equals(specials1, answer));

        Arrays.sort(specials2);
        Object[] print2 = new Object[specials2.length];
        for (int i = 0; i < specials2.length; i++) {
            print2[i] = new Float(specials2[i]);
        }
        assertTrue("Assert 2: specials sort incorrectly" + Arrays.asList(print2),
                Arrays.equals(specials2, answer));
    }

    /**
     * java.util.Arrays#toString(boolean[])
     */
    public void test_toString$Z() {
        assertEquals("null", Arrays.toString((boolean[]) null));
        assertEquals("[]", Arrays.toString(new boolean[] { }));
        assertEquals("[true]", Arrays.toString(new boolean[] { true }));
        assertEquals("[true, false]", Arrays.toString(new boolean[] { true, false }));
        assertEquals("[true, false, true]", Arrays.toString(new boolean[] { true, false, true }));
    }

    /**
     * java.util.Arrays#toString(byte[])
     */
    public void test_toString$B() {
        assertEquals("null", Arrays.toString((byte[]) null));
        assertEquals("[]", Arrays.toString(new byte[] { }));
        assertEquals("[0]", Arrays.toString(new byte[] { 0 }));
        assertEquals("[-1, 0]", Arrays.toString(new byte[] { -1, 0 }));
        assertEquals("[-1, 0, 1]", Arrays.toString(new byte[] { -1, 0, 1 }));
    }

    /**
     * java.util.Arrays#toString(char[])
     */
    public void test_toString$C() {
        assertEquals("null", Arrays.toString((char[]) null));
        assertEquals("[]", Arrays.toString(new char[] { }));
        assertEquals("[a]", Arrays.toString(new char[] { 'a' }));
        assertEquals("[a, b]", Arrays.toString(new char[] { 'a', 'b' }));
        assertEquals("[a, b, c]", Arrays.toString(new char[] { 'a', 'b', 'c' }));
    }

    /**
     * java.util.Arrays#toString(double[])
     */
    public void test_toString$D() {
        assertEquals("null", Arrays.toString((double[]) null));
        assertEquals("[]", Arrays.toString(new double[] { }));
        assertEquals("[0.0]", Arrays.toString(new double[] { 0.0D }));
        assertEquals("[-1.0, 0.0]", Arrays.toString(new double[] { -1.0D, 0.0D }));
        assertEquals("[-1.0, 0.0, 1.0]", Arrays.toString(new double[] { -1.0D, 0.0D, 1.0D }));
    }

    /**
     * java.util.Arrays#toString(float[])
     */
    public void test_toString$F() {
        assertEquals("null", Arrays.toString((float[]) null));
        assertEquals("[]", Arrays.toString(new float[] { }));
        assertEquals("[0.0]", Arrays.toString(new float[] { 0.0F }));
        assertEquals("[-1.0, 0.0]", Arrays.toString(new float[] { -1.0F, 0.0F }));
        assertEquals("[-1.0, 0.0, 1.0]", Arrays.toString(new float[] { -1.0F, 0.0F, 1.0F }));
    }

    /**
     * java.util.Arrays#toString(int[])
     */
    public void test_toString$I() {
        assertEquals("null", Arrays.toString((int[]) null));
        assertEquals("[]", Arrays.toString(new int[] { }));
        assertEquals("[0]", Arrays.toString(new int[] { 0 }));
        assertEquals("[-1, 0]", Arrays.toString(new int[] { -1, 0 }));
        assertEquals("[-1, 0, 1]", Arrays.toString(new int[] { -1, 0, 1 }));
    }

    /**
     * java.util.Arrays#toString(long[])
     */
    public void test_toString$J() {
        assertEquals("null", Arrays.toString((long[]) null));
        assertEquals("[]", Arrays.toString(new long[] { }));
        assertEquals("[0]", Arrays.toString(new long[] { 0 }));
        assertEquals("[-1, 0]", Arrays.toString(new long[] { -1, 0 }));
        assertEquals("[-1, 0, 1]", Arrays.toString(new long[] { -1, 0, 1 }));
    }

    /**
     * java.util.Arrays#toString(short[])
     */
    public void test_toString$S() {
        assertEquals("null", Arrays.toString((short[]) null));
        assertEquals("[]", Arrays.toString(new short[] { }));
        assertEquals("[0]", Arrays.toString(new short[] { 0 }));
        assertEquals("[-1, 0]", Arrays.toString(new short[] { -1, 0 }));
        assertEquals("[-1, 0, 1]", Arrays.toString(new short[] { -1, 0, 1 }));
    }

    /**
     * java.util.Arrays#toString(Object[])
     */
    public void test_toString$Ljava_lang_Object() {
        assertEquals("null", Arrays.toString((Object[]) null));
        assertEquals("[]", Arrays.toString(new Object[] { }));
        assertEquals("[fixture]", Arrays.toString(new Object[] { "fixture" }));
        assertEquals("[fixture, null]", Arrays.toString(new Object[] { "fixture", null }));
        assertEquals("[fixture, null, fixture]", Arrays.toString(new Object[] { "fixture", null, "fixture" }));
    }

    /**
     * java.util.Arrays#deepToString(Object[])
     */
    public void test_deepToString$java_lang_Object() {
        assertEquals("null", Arrays.deepToString((Object[]) null));
        assertEquals("[]", Arrays.deepToString(new Object[] { }));
        assertEquals("[fixture]", Arrays.deepToString(new Object[] { "fixture" }));
        assertEquals("[fixture, null]", Arrays.deepToString(new Object[] { "fixture", null }));
        assertEquals("[fixture, null, fixture]", Arrays.deepToString(new Object[] { "fixture", null, "fixture" }));

        Object[] fixture = new Object[1];
        fixture[0] = fixture;
        assertEquals("[[...]]", Arrays.deepToString(fixture));

        fixture = new Object[2];
        fixture[0] = "fixture";
        fixture[1] = fixture;
        assertEquals("[fixture, [...]]", Arrays.deepToString(fixture));

        fixture = new Object[10];
        fixture[0] = new boolean[] { true, false };
        fixture[1] = new byte[] { 0, 1 };
        fixture[2] = new char[] { 'a', 'b' };
        fixture[3] = new double[] { 0.0D, 1.0D };
        fixture[4] = new float[] { 0.0F, 1.0F };
        fixture[5] = new int[] { 0, 1 };
        fixture[6] = new long[] { 0L, 1L };
        fixture[7] = new short[] { 0, 1 };
        fixture[8] = fixture[0];
        fixture[9] = new Object[9];
        ((Object[]) fixture[9])[0] = fixture;
        ((Object[]) fixture[9])[1] = fixture[1];
        ((Object[]) fixture[9])[2] = fixture[2];
        ((Object[]) fixture[9])[3] = fixture[3];
        ((Object[]) fixture[9])[4] = fixture[4];
        ((Object[]) fixture[9])[5] = fixture[5];
        ((Object[]) fixture[9])[6] = fixture[6];
        ((Object[]) fixture[9])[7] = fixture[7];
        Object[] innerFixture = new Object[4];
        innerFixture[0] = "innerFixture0";
        innerFixture[1] = innerFixture;
        innerFixture[2] = fixture;
        innerFixture[3] = "innerFixture3";
        ((Object[]) fixture[9])[8] = innerFixture;

        String expected = "[[true, false], [0, 1], [a, b], [0.0, 1.0], [0.0, 1.0], [0, 1], [0, 1], [0, 1], [true, false], [[...], [0, 1], [a, b], [0.0, 1.0], [0.0, 1.0], [0, 1], [0, 1], [0, 1], [innerFixture0, [...], [...], innerFixture3]]]";

        assertEquals(expected, Arrays.deepToString(fixture));
    }

    public void test_asListTvararg() throws Exception {
        List<String> stringsList = Arrays.asList("0", "1");
        assertEquals(2, stringsList.size());
        assertEquals("0", stringsList.get(0));
        assertEquals("1", stringsList.get(1));
        assertTrue(stringsList instanceof RandomAccess);
        assertTrue(stringsList instanceof Serializable);

        assertEquals(stringsList, SerializationTest
                .copySerializable((Serializable) stringsList));

        //test from javadoc
        List<String> stooges = Arrays.asList("Larry", "Moe", "Curly");
        assertEquals(3, stooges.size());
        assertEquals("Larry", stooges.get(0));
        assertEquals("Moe", stooges.get(1));
        assertEquals("Curly", stooges.get(2));

        stringsList = Arrays.asList((String) null);
        assertEquals(1, stringsList.size());
        assertEquals((String) null, stringsList.get(0));

        try {
            Arrays.asList((Object[]) null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    public void test_binarySearch$TTLjava_util_ComparatorsuperT() {
        String[] strings = new String[] { "a", "B", "c", "D" };
        Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);
        assertEquals(0, Arrays.binarySearch(strings, "a",
                String.CASE_INSENSITIVE_ORDER));
        assertEquals(0, Arrays.binarySearch(strings, "A",
                String.CASE_INSENSITIVE_ORDER));
        assertEquals(1, Arrays.binarySearch(strings, "b",
                String.CASE_INSENSITIVE_ORDER));
        assertEquals(1, Arrays.binarySearch(strings, "B",
                String.CASE_INSENSITIVE_ORDER));
        assertEquals(2, Arrays.binarySearch(strings, "c",
                String.CASE_INSENSITIVE_ORDER));
        assertEquals(2, Arrays.binarySearch(strings, "C",
                String.CASE_INSENSITIVE_ORDER));
        assertEquals(3, Arrays.binarySearch(strings, "d",
                String.CASE_INSENSITIVE_ORDER));
        assertEquals(3, Arrays.binarySearch(strings, "D",
                String.CASE_INSENSITIVE_ORDER));


        assertTrue(Arrays.binarySearch(strings, "e",
                String.CASE_INSENSITIVE_ORDER) < 0);
        assertTrue(Arrays.binarySearch(strings, "" + ('A' - 1),
                String.CASE_INSENSITIVE_ORDER) < 0);

        //test with null comparator, which switches back to Comparable
        Arrays.sort(strings, null);
        //B, D, a, c
        assertEquals(2, Arrays.binarySearch(strings, "a", (Comparator<String>) null));
        assertEquals(-1, Arrays.binarySearch(strings, "A", (Comparator<String>) null));
        assertEquals(-4, Arrays.binarySearch(strings, "b", (Comparator<String>) null));
        assertEquals(0, Arrays.binarySearch(strings, "B", (Comparator<String>) null));
        assertEquals(3, Arrays.binarySearch(strings, "c", (Comparator<String>) null));
        assertEquals(-2, Arrays.binarySearch(strings, "C", (Comparator<String>) null));
        assertEquals(-5, Arrays.binarySearch(strings, "d", (Comparator<String>) null));
        assertEquals(1, Arrays.binarySearch(strings, "D", (Comparator<String>) null));

        assertTrue(Arrays.binarySearch(strings, "e", null) < 0);
        assertTrue(Arrays.binarySearch(strings, "" + ('A' - 1), null) < 0);

        try {
            Arrays.binarySearch((String[]) null, "A", String.CASE_INSENSITIVE_ORDER);
            fail("No NPE");
        } catch (NullPointerException e) {
        }

        try {
            Arrays.binarySearch(strings, (String) null, String.CASE_INSENSITIVE_ORDER);
            fail("No NPE");
        } catch (NullPointerException e) {
        }

        try {
            Arrays.binarySearch(strings, (String) null, (Comparator<String>) null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }

    }

    public void test_sort$TLjava_lang_ComparatorsuperT() {
        String[] strings = new String[] { "a", "B", "c", "D" };
        Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);
        assertEquals("a", strings[0]);
        assertEquals("B", strings[1]);
        assertEquals("c", strings[2]);
        assertEquals("D", strings[3]);

        //test with null comparator, which switches back to Comparable
        Arrays.sort(strings, null);
        //B, D, a, c
        assertEquals("B", strings[0]);
        assertEquals("D", strings[1]);
        assertEquals("a", strings[2]);
        assertEquals("c", strings[3]);

        try {
            Arrays.sort((String[]) null, String.CASE_INSENSITIVE_ORDER);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    public void test_sort$TIILjava_lang_ComparatorsuperT() {
        String[] strings = new String[] { "a", "B", "c", "D" };
        Arrays.sort(strings, 0, strings.length, String.CASE_INSENSITIVE_ORDER);
        assertEquals("a", strings[0]);
        assertEquals("B", strings[1]);
        assertEquals("c", strings[2]);
        assertEquals("D", strings[3]);

        //test with null comparator, which switches back to Comparable
        Arrays.sort(strings, 0, strings.length, null);
        //B, D, a, c
        assertEquals("B", strings[0]);
        assertEquals("D", strings[1]);
        assertEquals("a", strings[2]);
        assertEquals("c", strings[3]);

        try {
            Arrays.sort((String[]) null, String.CASE_INSENSITIVE_ORDER);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    public void test_forEach() throws Exception {
        List<Integer> list = Arrays.asList(0, 1, 2);
        ArrayList<Integer> output = new ArrayList<>();
        list.forEach(k -> output.add(k));
        assertEquals(list, output);
    }

    public void test_forEach_NPE() throws Exception {
        List<Integer> list = Arrays.asList(0, 1, 2);
        try {
            list.forEach(null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_replaceAll() throws Exception {
        List<Integer> list = Arrays.asList(0, 1, 2);
        list.replaceAll(k -> k + 1);
        assertEquals((Integer)1, list.get(0));
        assertEquals((Integer)2, list.get(1));
        assertEquals((Integer)3, list.get(2));
        assertEquals(3, list.size());
    }

    public void test_replaceAll_NPE() throws Exception {
        List<Integer> list = Arrays.asList(0, 1, 2);
        try {
            list.replaceAll(null);
            fail();
        } catch(NullPointerException expected) {}
    }
}
