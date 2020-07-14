/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License.
 */

package libcore.java.util;

import java.util.Arrays;
import java.util.Random;

public class RandomTest extends junit.framework.TestCase {
    public void test_subclassing() throws Exception {
        // http://b/2502231
        // Ensure that Random's constructors call setSeed by emulating the active ingredient
        // from the bug: the subclass' setSeed had a side-effect necessary for the correct
        // functioning of next.
        class MyRandom extends Random {
            public String state;
            public MyRandom() { super(); }
            public MyRandom(long l) { super(l); }
            @Override protected synchronized int next(int bits) { return state.length(); }
            @Override public synchronized void setSeed(long seed) { state = Long.toString(seed); }
        }
        // Test the 0-argument constructor...
        MyRandom r1 = new MyRandom();
        r1.nextInt();
        assertNotNull(r1.state);
        // Test the 1-argument constructor...
        MyRandom r2 = new MyRandom(123L);
        r2.nextInt();
        assertNotNull(r2.state);
    }

    public void test_ints$() {
        final int limit = 128; // We can't test for every element in an infinite stream.

        Random rand = new Random(0);
        int[] rands = new int[limit];
        for(int i = 0; i < limit; ++i) {
            rands[i] = rand.nextInt();
        }

        int[] streamRands = new Random(0).ints().limit(limit).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
    }

    public void test_ints$L() {
        final int size = 32;

        Random rand = new Random(0);
        int[] rands = new int[size];
        for(int i = 0; i < size; ++i) {
            rands[i] = rand.nextInt();
        }

        int[] streamRands = new Random(0).ints(size).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
        assertEquals(size, new Random(0).ints(size).count());

        try {
            new Random(0).ints(-1);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_ints$II() {
        final int limit = 128; // We can't test for every element in an infinite stream.
        final int origin = 128, bound = 256;

        Random rand = new Random(0);
        int[] rands = new int[limit];
        for(int i = 0; i < limit; ++i) {
            rands[i] = rand.nextInt(bound - origin) + origin;
        }

        int[] streamRands = new Random(0).ints(origin, bound).limit(limit).toArray();
        assertTrue(Arrays.equals(rands, streamRands));

        try {
            new Random(0).ints(100, 0);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_ints$LII() {
        final int size = 32;
        final int origin = 128, bound = 256;

        Random rand = new Random(0);
        int[] rands = new int[size];
        for(int i = 0; i < size; ++i) {
            rands[i] = rand.nextInt(bound - origin) + origin;
        }

        int[] streamRands = new Random(0).ints(size, origin, bound).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
        assertEquals(size, new Random(0).ints(size, origin, bound).count());

        try {
            new Random(0).ints(-1, 10, 20);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            new Random(0).ints(10, 100, 0);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_longs$() {
        final int limit = 128; // We can't test for every element in an infinite stream.

        Random rand = new Random(0);
        long[] rands = new long[limit];
        for(int i = 0; i < limit; ++i) {
            rands[i] = rand.nextLong();
        }

        long[] streamRands = new Random(0).longs().limit(limit).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
    }

    public void test_longs$L() {
        final int size = 32;

        Random rand = new Random(0);
        long[] rands = new long[size];
        for(int i = 0; i < size; ++i) {
            rands[i] = rand.nextLong();
        }

        long[] streamRands = new Random(0).longs(size).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
        assertEquals(size, new Random(0).longs(size).count());

        try {
            new Random(0).longs(-1);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_longs$II() {
        final int limit = 128; // We can't test for every element in an infinite stream.
        final int origin = 128, bound = 256;

        Random rand = new Random(0);
        long[] rands = new long[limit];
        for(int i = 0; i < limit; ++i) {
            rands[i] = (rand.nextLong() & 127) + origin;
        }

        long[] streamRands = new Random(0).longs(origin, bound).limit(limit).toArray();
        assertTrue(Arrays.equals(rands, streamRands));

        try {
            new Random(0).longs(100, 0);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_longs$LII() {
        final int size = 32;
        final int origin = 128, bound = 256;

        Random rand = new Random(0);
        long[] rands = new long[size];
        for(int i = 0; i < size; ++i) {
            rands[i] = (rand.nextLong() & 127) + origin;
        }

        long[] streamRands = new Random(0).longs(size, origin, bound).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
        assertEquals(size, new Random(0).longs(size, origin, bound).count());

        try {
            new Random(0).longs(-1, 10, 20);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            new Random(0).longs(10, 100, 0);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_doubles$() {
        final int limit = 128; // We can't test for every element in an infinite stream.

        Random rand = new Random(0);
        double[] rands = new double[limit];
        for(int i = 0; i < limit; ++i) {
            rands[i] = rand.nextDouble();
        }

        double[] streamRands = new Random(0).doubles().limit(limit).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
    }

    public void test_doubles$L() {
        final int size = 32;

        Random rand = new Random(0);
        double[] rands = new double[size];
        for(int i = 0; i < size; ++i) {
            rands[i] = rand.nextDouble();
        }

        double[] streamRands = new Random(0).doubles(size).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
        assertEquals(size, new Random(0).doubles(size).count());

        try {
            new Random(0).ints(-1);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_doubles$II() {
        final int limit = 128; // We can't test for every element in an infinite stream.
        final int origin = 128, bound = 256;

        Random rand = new Random(0);
        double[] rands = new double[limit];
        for(int i = 0; i < limit; ++i) {
            double r = rand.nextDouble() * (bound - origin) + origin;
            if (r >= bound) {
                r = Math.nextDown(r);
            }
            rands[i] = r;
        }

        double[] streamRands = new Random(0).doubles(origin, bound).limit(limit).toArray();
        assertTrue(Arrays.equals(rands, streamRands));

        try {
            new Random(0).doubles(100, 0);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_doubles$LII() {
        final int size = 32;
        final int origin = 128, bound = 256;

        Random rand = new Random(0);
        double[] rands = new double[size];
        for(int i = 0; i < size; ++i) {
            double r = rand.nextDouble() * (bound - origin) + origin;
            if (r >= bound) {
                r = Math.nextDown(r);
            }
            rands[i] = r;
        }

        double[] streamRands = new Random(0).doubles(size, origin, bound).toArray();
        assertTrue(Arrays.equals(rands, streamRands));
        assertEquals(size, new Random(0).doubles(size, origin, bound).count());

        try {
            new Random(0).doubles(-1, 10, 20);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            new Random(0).doubles(10, 100, 0);
            fail();
        } catch (IllegalArgumentException expected) {}
    }
}
