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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public class SpliteratorsTest extends TestCase {

    public void testEmptyIntSpliterator() {
        Spliterator.OfInt empty = Spliterators.emptyIntSpliterator();
        assertNull(empty.trySplit());
        assertEquals(0, empty.estimateSize());
        assertEquals(0, empty.getExactSizeIfKnown());

        IntConsumer alwaysFails = (int value) -> fail();
        Consumer<Integer> alwaysFailsBoxed = (Integer value) -> fail();
        empty.tryAdvance(alwaysFails);
        empty.tryAdvance(alwaysFailsBoxed);

        empty.forEachRemaining(alwaysFails);
        empty.forEachRemaining(alwaysFailsBoxed);

        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    }

    public void testEmptyRefSpliterator() {
        Spliterator<Object> empty = Spliterators.emptySpliterator();
        assertNull(empty.trySplit());
        assertEquals(0, empty.estimateSize());
        assertEquals(0, empty.getExactSizeIfKnown());

        Consumer<Object> alwaysFails = (Object value) -> fail();
        empty.tryAdvance(alwaysFails);
        empty.forEachRemaining(alwaysFails);

        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    }

    public void testEmptyLongSpliterator() {
        Spliterator.OfLong empty = Spliterators.emptyLongSpliterator();
        assertNull(empty.trySplit());
        assertEquals(0, empty.estimateSize());
        assertEquals(0, empty.getExactSizeIfKnown());

        LongConsumer alwaysFails = (long value) -> fail();
        Consumer<Long> alwaysFailsBoxed = (Long value) -> fail();
        empty.tryAdvance(alwaysFails);
        empty.tryAdvance(alwaysFailsBoxed);

        empty.forEachRemaining(alwaysFails);
        empty.forEachRemaining(alwaysFailsBoxed);

        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    }

    public void testEmptyDoubleSpliterator() {
        Spliterator.OfDouble empty = Spliterators.emptyDoubleSpliterator();
        assertNull(empty.trySplit());
        assertEquals(0, empty.estimateSize());
        assertEquals(0, empty.getExactSizeIfKnown());

        DoubleConsumer alwaysFails = (double value) -> fail();
        Consumer<Double> alwaysFailsBoxed = (Double value) -> fail();
        empty.tryAdvance(alwaysFails);
        empty.tryAdvance(alwaysFailsBoxed);

        empty.forEachRemaining(alwaysFails);
        empty.forEachRemaining(alwaysFailsBoxed);

        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    }

    public void testSpliteratorObjectArray() {
        String[] array = { "a", "b", "c", "d", "e", "f", "g", "h" };
        ArrayList<String> expectedValues = new ArrayList<>(Arrays.asList(array));

        Spliterator<String> sp = Spliterators.spliterator(array, 0);
        assertEquals(8, sp.estimateSize());
        assertEquals(8, sp.getExactSizeIfKnown());

        sp = Spliterators.spliterator(array, 0);
        SpliteratorTester.runBasicIterationTests(sp, expectedValues);

        sp = Spliterators.spliterator(array, 0);
        SpliteratorTester.testSpliteratorNPE(sp);

        sp = Spliterators.spliterator(array, 0);
        SpliteratorTester.runBasicSplitTests(sp, expectedValues, String::compareTo);

        sp = Spliterators.spliterator(array, 0);
        SpliteratorTester.runSizedTests(sp, 8);

        sp = Spliterators.spliterator(array, 0);
        SpliteratorTester.runSubSizedTests(sp, 8);

        // Assert the spliterator inherits any characteristics we ask it to.
        sp = Spliterators.spliterator(array, Spliterator.ORDERED);
        assertTrue(sp.hasCharacteristics(Spliterator.ORDERED));
    }

    public void testSpliteratorObjectArrayRange() {
        String[] array = { "FOO", "BAR", "a", "b", "c", "d", "e", "f", "g", "h", "BAZ", "BAH" };
        ArrayList<String> expectedValues = new ArrayList<>(
                Arrays.asList(Arrays.copyOfRange(array, 2, 10)));

        Spliterator<String> sp = Spliterators.spliterator(array, 2, 10, 0);
        assertEquals(8, sp.estimateSize());
        assertEquals(8, sp.getExactSizeIfKnown());

        sp = Spliterators.spliterator(array, 2, 10, 0);
        SpliteratorTester.runBasicIterationTests(sp, expectedValues);

        sp = Spliterators.spliterator(array, 2, 10, 0);
        SpliteratorTester.testSpliteratorNPE(sp);

        sp = Spliterators.spliterator(array, 2, 10, 0);
        SpliteratorTester.runBasicSplitTests(sp, expectedValues, String::compareTo);

        sp = Spliterators.spliterator(array, 2, 10, 0);
        SpliteratorTester.runSizedTests(sp, 8);

        sp = Spliterators.spliterator(array, 2, 10, 0);
        SpliteratorTester.runSubSizedTests(sp, 8);

        // Assert the spliterator inherits any characteristics we ask it to.
        sp = Spliterators.spliterator(array, 2, 10, Spliterator.ORDERED);
        assertTrue(sp.hasCharacteristics(Spliterator.ORDERED));
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

        Spliterator.OfInt intSp = Spliterators.spliterator(elements, 0);

        assertEquals(16, intSp.estimateSize());
        assertEquals(16, intSp.getExactSizeIfKnown());

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
        Spliterator.OfInt sp = Spliterators.spliterator(elements, 2, 6, 0);

        PrimitiveIntArrayList recorder = new PrimitiveIntArrayList(4);
        sp.tryAdvance((Integer value) -> recorder.add((int) value));
        sp.tryAdvance((int value) -> recorder.add(value));
        sp.forEachRemaining((int value) -> recorder.add(value));

        int[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(new int[] { 1, 2, 3, 4 }), Arrays.toString(recorded));
    }

    public void test_spliterator_longOffsetBasic() {
        long[] elements = { 123123, 131321312, 1, 2, 3, 4, 32323232, 45454};
        Spliterator.OfLong sp = Spliterators.spliterator(elements, 2, 6, 0);

        PrimitiveLongArrayList recorder = new PrimitiveLongArrayList(4);
        sp.tryAdvance((Long value) -> recorder.add((long) value));
        sp.tryAdvance((long value) -> recorder.add(value));
        sp.forEachRemaining((long value) -> recorder.add(value));

        long[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(new long[] { 1, 2, 3, 4 }), Arrays.toString(recorded));
    }

    public void test_spliterator_doubleOffsetBasic() {
        double[] elements = { 123123, 131321312, 1, 2, 3, 4, 32323232, 45454};
        Spliterator.OfDouble sp = Spliterators.spliterator(elements, 2, 6, 0);

        PrimitiveDoubleArrayList recorder = new PrimitiveDoubleArrayList(4);
        sp.tryAdvance((Double value) -> recorder.add((double) value));
        sp.tryAdvance((double value) -> recorder.add(value));
        sp.forEachRemaining((double value) -> recorder.add(value));

        double[] recorded = recorder.toSortedArray();
        assertEquals(Arrays.toString(new double[] { 1, 2, 3, 4 }), Arrays.toString(recorded));
    }

    public void test_spliterator_long() {
        long[] elements = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        Spliterator.OfLong longSp = Spliterators.spliterator(elements, 0);

        assertEquals(16, longSp.estimateSize());
        assertEquals(16, longSp.getExactSizeIfKnown());

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

        Spliterator.OfDouble doubleSp = Spliterators.spliterator(elements, 0);

        assertEquals(16, doubleSp.estimateSize());
        assertEquals(16, doubleSp.getExactSizeIfKnown());

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
        Spliterator.OfInt intSp = Spliterators.spliterator(elements, 0);
        try {
            intSp.forEachRemaining((Consumer<Integer>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            assertTrue(intSp.tryAdvance((Consumer<Integer>) null));
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
        Spliterator.OfLong longSp = Spliterators.spliterator(longElements, 0);
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
        Spliterator.OfDouble doubleSp = Spliterators.spliterator(doubleElements, 0);
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

    public void testSpliterator_Collection() {
        String[] array = { "a", "b", "c", "d", "e", "f", "g", "h" };
        ArrayList<String> expectedValues = new ArrayList<>(Arrays.asList(array));

        Collection<String> collection = Arrays.asList(array);

        Spliterator<String> sp = Spliterators.spliterator(collection, 0);
        assertEquals(8, sp.estimateSize());
        assertEquals(8, sp.getExactSizeIfKnown());

        sp = Spliterators.spliterator(collection, 0);
        SpliteratorTester.runBasicIterationTests(sp, expectedValues);

        sp = Spliterators.spliterator(collection, 0);
        SpliteratorTester.testSpliteratorNPE(sp);

        sp = Spliterators.spliterator(collection, 0);
        SpliteratorTester.runBasicSplitTests(sp, expectedValues, String::compareTo);

        sp = Spliterators.spliterator(collection, 0);
        SpliteratorTester.runSizedTests(sp, 8);

        sp = Spliterators.spliterator(collection, 0);
        SpliteratorTester.runSubSizedTests(sp, 8);

        // Assert the spliterator inherits any characteristics we ask it to.
        sp = Spliterators.spliterator(collection, Spliterator.ORDERED);
        assertTrue(sp.hasCharacteristics(Spliterator.ORDERED));
    }

    public void testSpliterator_sizedIterator() {
        String[] array = { "a", "b", "c", "d", "e", "f", "g", "h" };
        List<String> asList = Arrays.asList(array);
        ArrayList<String> expectedValues = new ArrayList<>(asList);

        Spliterator<String> sp = Spliterators.spliterator(asList.iterator(), 8 /* size */, 0);
        assertEquals(8, sp.estimateSize());
        assertEquals(8, sp.getExactSizeIfKnown());

        sp = Spliterators.spliterator(asList.iterator(), 8 /* size */, 0);
        SpliteratorTester.runBasicIterationTests(sp, expectedValues);

        sp = Spliterators.spliterator(asList.iterator(), 8 /* size */, 0);
        SpliteratorTester.testSpliteratorNPE(sp);

        sp = Spliterators.spliterator(asList.iterator(), 8 /* size */, 0);
        SpliteratorTester.runBasicSplitTests(sp, expectedValues, String::compareTo);

        sp = Spliterators.spliterator(asList.iterator(), 8 /* size */, 0);
        SpliteratorTester.runSizedTests(sp, 8);

        sp = Spliterators.spliterator(asList.iterator(), 8 /* size */, 0);
        SpliteratorTester.runSubSizedTests(sp, 8);

        // Assert the spliterator inherits any characteristics we ask it to.
        sp = Spliterators.spliterator(array, Spliterator.ORDERED);
        assertTrue(sp.hasCharacteristics(Spliterator.ORDERED));
    }

    public void testSpliterator_unsizedIterator() {
        String[] array = { "a", "b", "c", "d", "e", "f", "g", "h" };
        List<String> asList = Arrays.asList(array);
        ArrayList<String> expectedValues = new ArrayList<>(asList);

        Spliterator<String> sp = Spliterators.spliteratorUnknownSize(asList.iterator(), 0);
        SpliteratorTester.runBasicIterationTests(sp, expectedValues);

        sp = Spliterators.spliteratorUnknownSize(asList.iterator(), 0);
        SpliteratorTester.testSpliteratorNPE(sp);

        sp = Spliterators.spliteratorUnknownSize(asList.iterator(), 0);
        SpliteratorTester.runBasicSplitTests(sp, expectedValues, String::compareTo);

        // Assert the spliterator inherits any characteristics we ask it to.
        sp = Spliterators.spliterator(array, Spliterator.ORDERED);
        assertTrue(sp.hasCharacteristics(Spliterator.ORDERED));
    }

    private static class CannedIntPrimitiveIterator implements PrimitiveIterator.OfInt {
        private final int[] data;
        private int idx;

        public CannedIntPrimitiveIterator(int[] data) {
            this.data = data;
            this.idx = 0;
        }

        @Override
        public int nextInt() {
            return data[idx++];
        }

        @Override
        public boolean hasNext() {
            return idx < data.length;
        }
    }

    private static class CannedLongPrimitiveIterator implements PrimitiveIterator.OfLong {
        private final long[] data;
        private int idx;

        public CannedLongPrimitiveIterator(long[] data) {
            this.data = data;
            this.idx = 0;
        }

        @Override
        public long nextLong() {
            return data[idx++];
        }

        @Override
        public boolean hasNext() {
            return idx < data.length;
        }
    }

    private static class CannedDoublePrimitiveIterator implements PrimitiveIterator.OfDouble {
        private final double[] data;
        private int idx;

        public CannedDoublePrimitiveIterator(double[] data) {
            this.data = data;
            this.idx = 0;
        }

        @Override
        public double nextDouble() {
            return data[idx++];
        }

        @Override
        public boolean hasNext() {
            return idx < data.length;
        }


    }

    public void test_spliterator_intPrimitiveIterator() {
        int[] data = new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        PrimitiveIterator.OfInt elements = new CannedIntPrimitiveIterator(data);
        Spliterator.OfInt intSp = Spliterators.spliterator(elements, 16 /* size */, 0);

        assertEquals(16, intSp.estimateSize());
        assertEquals(16, intSp.getExactSizeIfKnown());

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
        assertEquals(Arrays.toString(data), Arrays.toString(recorded));
    }

    public void test_spliterator_intPrimitiveIterator_unsized() {
        int[] data = new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        PrimitiveIterator.OfInt elements = new CannedIntPrimitiveIterator(data);
        Spliterator.OfInt intSp = Spliterators.spliteratorUnknownSize(elements, 0);

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
        assertEquals(Arrays.toString(data), Arrays.toString(recorded));
    }

    public void test_spliterator_longPrimitiveIterator() {
        long[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        PrimitiveIterator.OfLong elements = new CannedLongPrimitiveIterator(data);
        Spliterator.OfLong longSp = Spliterators.spliterator(elements, 16 /* size */, 0);

        assertEquals(16, longSp.estimateSize());
        assertEquals(16, longSp.getExactSizeIfKnown());

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
        assertEquals(Arrays.toString(data), Arrays.toString(recorded));
    }

    public void test_spliterator_longPrimitiveIterator_unsized() {
        long[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        PrimitiveIterator.OfLong elements = new CannedLongPrimitiveIterator(data);
        Spliterator.OfLong longSp = Spliterators.spliteratorUnknownSize(elements, 0);

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
        assertEquals(Arrays.toString(data), Arrays.toString(recorded));
    }

    public void test_spliterator_doublePrimitiveIterator() {
        double[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        PrimitiveIterator.OfDouble elements = new CannedDoublePrimitiveIterator(data);
        Spliterator.OfDouble doubleSp = Spliterators.spliterator(elements, 16 /* size */, 0);

        assertEquals(16, doubleSp.estimateSize());
        assertEquals(16, doubleSp.getExactSizeIfKnown());

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
        assertEquals(Arrays.toString(data), Arrays.toString(recorded));
    }

    public void test_spliterator_doublePrimitiveIterator_unsized() {
        double[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        PrimitiveIterator.OfDouble elements = new CannedDoublePrimitiveIterator(data);
        Spliterator.OfDouble doubleSp = Spliterators.spliteratorUnknownSize(elements, 0);

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
        assertEquals(Arrays.toString(data), Arrays.toString(recorded));
    }

    // Conversions from spliterators to iterators

    public void test_iterator_int() {
        int[] data = new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        Spliterator.OfInt intSp = Spliterators.spliterator(data, 0);
        PrimitiveIterator.OfInt it = Spliterators.iterator(intSp);

        assertTrue(it.hasNext());
        assertEquals(1, (int) it.next());
        assertEquals(2, it.nextInt());

        PrimitiveIntArrayList recorder = new PrimitiveIntArrayList(16);
        recorder.add(1);
        recorder.add(2);
        it.forEachRemaining((Integer l) -> recorder.add(l));
        assertEquals(Arrays.toString(data), Arrays.toString(recorder.toSortedArray()));

        assertFalse(it.hasNext());
    }

    public void test_iterator_long() {
        long[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        Spliterator.OfLong longSp = Spliterators.spliterator(data, 0);
        PrimitiveIterator.OfLong it = Spliterators.iterator(longSp);

        assertTrue(it.hasNext());
        assertEquals(1, (long) it.next());
        assertEquals(2, it.nextLong());

        PrimitiveLongArrayList recorder = new PrimitiveLongArrayList(16);
        recorder.add(1);
        recorder.add(2);
        it.forEachRemaining((Long l) -> recorder.add(l));
        assertEquals(Arrays.toString(data), Arrays.toString(recorder.toSortedArray()));

        assertFalse(it.hasNext());
    }

    public void test_iterator_double() {
        double[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        Spliterator.OfDouble doubleSp = Spliterators.spliterator(data, 0);

        PrimitiveIterator.OfDouble it = Spliterators.iterator(doubleSp);

        assertTrue(it.hasNext());
        assertEquals(1.0, it.next());
        assertEquals(2.0, it.nextDouble());

        PrimitiveDoubleArrayList recorder = new PrimitiveDoubleArrayList(16);
        recorder.add(1.0);
        recorder.add(2.0);
        it.forEachRemaining((Double l) -> recorder.add(l));
        assertEquals(Arrays.toString(data), Arrays.toString(recorder.toSortedArray()));

        assertFalse(it.hasNext());
    }

    public void test_iterator_ref() {
        String[] array = { "a", "b", "c", "d", "e", "f", "g", "h" };
        Spliterator<String> stringSp = Spliterators.spliterator(array, 0);
        Iterator<String> it = Spliterators.iterator(stringSp);

        assertTrue(it.hasNext());
        assertEquals("a", it.next());

        ArrayList<String> recorder = new ArrayList<>();
        recorder.add("a");
        it.forEachRemaining((val) -> recorder.add(val));
        assertEquals(Arrays.toString(array), Arrays.toString(recorder.toArray()));

        assertFalse(it.hasNext());
    }
}
