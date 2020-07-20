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
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public class PrimitiveIteratorTest extends TestCase {
    public static class CannedIntIterator implements PrimitiveIterator.OfInt {
        private final int[] ints;
        private int idx;

        public CannedIntIterator(int[] ints) {
            this.ints = ints;
            this.idx = 0;
        }

        @Override
        public int nextInt() {
            return ints[idx++];
        }

        @Override
        public boolean hasNext() {
            return idx < ints.length;
        }
    }

    public static class CannedLongIterator implements PrimitiveIterator.OfLong {
        private final long[] longs;
        private int idx;

        public CannedLongIterator(long[] longs) {
            this.longs = longs;
            this.idx = 0;
        }

        @Override
        public long nextLong() {
            return longs[idx++];
        }

        @Override
        public boolean hasNext() {
            return idx < longs.length;
        }
    }

    public static class CannedDoubleIterator implements PrimitiveIterator.OfDouble {
        private final double[] doubles;
        private int idx;

        public CannedDoubleIterator(double[] doubles) {
            this.doubles = doubles;
            this.idx = 0;
        }

        @Override
        public double nextDouble() {
            return doubles[idx++];
        }

        @Override
        public boolean hasNext() {
            return idx < doubles.length;
        }
    }

    public void testIntIterator_forEachRemaining_Consumer() {
        final int[] data = new int[] { 1, 2, 4, 5 };

        final ArrayList<Integer> recorder = new ArrayList<>();
        CannedIntIterator cit = new CannedIntIterator(data);
        cit.forEachRemaining((int i) -> recorder.add(i));

        assertEquals(Arrays.asList(1, 2, 4, 5), recorder);

        cit = new CannedIntIterator(data);
        try {
            cit.forEachRemaining((IntConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testIntIterator_forEachRemaining_boxedConsumer() {
        final int[] data = new int[] { 1, 2, 4, 5 };

        final ArrayList<Integer> recorder = new ArrayList<>();
        CannedIntIterator cit = new CannedIntIterator(data);
        cit.forEachRemaining((Integer i) -> recorder.add(i));

        assertEquals(Arrays.asList(1, 2, 4, 5), recorder);

        // Test that the boxed and unboxed iterators produce the same
        // set of events.
        final ArrayList<Integer> recorder2 = new ArrayList<>();
        cit = new CannedIntIterator(data);
        cit.forEachRemaining((int i) -> recorder2.add(i));
        assertEquals(recorder, recorder2);

        cit = new CannedIntIterator(data);
        try {
            cit.forEachRemaining((Consumer<Integer>) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testIntIterator_forEachRemaining_boxedNext() {
        final int[] data = new int[] { 1 };
        CannedIntIterator cit = new CannedIntIterator(data);
        assertEquals(1, (int) cit.next());
    }

    public void testLongIterator_forEachRemaining_Consumer() {
        final long[] data = new long[] { 1, 2, 4, 5 };

        final ArrayList<Long> recorder = new ArrayList<>();
        CannedLongIterator cit = new CannedLongIterator(data);
        cit.forEachRemaining((long i) -> recorder.add(i));

        assertEquals(Arrays.asList(1L, 2L, 4L, 5L), recorder);

        cit = new CannedLongIterator(data);
        try {
            cit.forEachRemaining((LongConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testLongIterator_forEachRemaining_boxedConsumer() {
        final long[] data = new long[] { 1, 2, 4, 5 };

        final ArrayList<Long> recorder = new ArrayList<>();
        CannedLongIterator cit = new CannedLongIterator(data);
        cit.forEachRemaining((Long i) -> recorder.add(i));

        assertEquals(Arrays.asList(1L, 2L, 4L, 5L), recorder);

        // Test that the boxed and unboxed iterators produce the same
        // set of events.
        final ArrayList<Long> recorder2 = new ArrayList<>();
        cit = new CannedLongIterator(data);
        cit.forEachRemaining((long i) -> recorder2.add(i));
        assertEquals(recorder, recorder2);

        cit = new CannedLongIterator(data);
        try {
            cit.forEachRemaining((Consumer<Long>) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testLongIterator_forEachRemaining_boxedNext() {
        final long[] data = new long[] { 1L };
        CannedLongIterator clt = new CannedLongIterator(data);
        assertEquals(1, (long) clt.next());
    }

    public void testDoubleIterator_forEachRemaining_Consumer() {
        final double[] data = new double[] { 1, 2, 4, 5 };

        final ArrayList<Double> recorder = new ArrayList<>();
        CannedDoubleIterator cit = new CannedDoubleIterator(data);
        cit.forEachRemaining((double i) -> recorder.add(i));

        assertEquals(Arrays.asList(1.0d, 2.0d, 4.0d, 5.0d), recorder);

        cit = new CannedDoubleIterator(data);
        try {
            cit.forEachRemaining((DoubleConsumer) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testDoubleIterator_forEachRemaining_boxedConsumer() {
        final double[] data = new double[] { 1, 2, 4, 5 };

        final ArrayList<Double> recorder = new ArrayList<>();
        CannedDoubleIterator cit = new CannedDoubleIterator(data);
        cit.forEachRemaining((Double i) -> recorder.add(i));

        assertEquals(Arrays.asList(1.0d, 2.0d, 4.0d, 5.0d), recorder);

        // Test that the boxed and unboxed iterators produce the same
        // set of events.
        final ArrayList<Double> recorder2 = new ArrayList<>();
        cit = new CannedDoubleIterator(data);
        cit.forEachRemaining((double i) -> recorder2.add(i));
        assertEquals(recorder, recorder2);

        cit = new CannedDoubleIterator(data);
        try {
            cit.forEachRemaining((Consumer<Double>) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testDoubleIterator_forEachRemaining_boxedNext() {
        final double[] data = new double[] { 1.0 };
        CannedDoubleIterator clt = new CannedDoubleIterator(data);
        assertEquals(1.0, (double) clt.next());
    }
}
