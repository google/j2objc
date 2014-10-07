/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.BitSet;

public class BitSetTest extends junit.framework.TestCase {
    public void test_toString() throws Exception {
        // From the RI javadoc.
        BitSet bs = new BitSet();
        assertEquals("{}", bs.toString());
        bs.set(2);
        assertEquals("{2}", bs.toString());
        bs.set(4);
        bs.set(10);
        assertEquals("{2, 4, 10}", bs.toString());
    }

    private static void assertBitSet(BitSet bs, long[] longs, String s) {
        for (int i = 0; i < 64 * longs.length; ++i) {
            assertEquals(bs.toString(), ((longs[i / 64] & (1L << (i % 64))) != 0), bs.get(i));
        }
        int cardinality = 0;
        for (int i = 0; i < longs.length; ++i) {
            cardinality += Long.bitCount(longs[i]);
        }
        if (cardinality != 0) {
            assertFalse(bs.isEmpty());
        } else {
            assertTrue(bs.isEmpty());
        }
        assertEquals(cardinality, bs.cardinality());
        assertEquals(64 * longs.length, bs.size());
        assertEquals(s, bs.toString());
    }

    private static void assertBitSet(long[] longs, String s) {
        // Test BitSet.valueOf(long[]).
        assertBitSet(BitSet.valueOf(longs), longs, s);
        // Test BitSet.valueOf(LongBuffer).
        assertBitSet(BitSet.valueOf(LongBuffer.wrap(longs)), longs, s);
        // Surround 'longs' with junk set bits but exclude them from the LongBuffer.
        long[] paddedLongs = new long[1 + longs.length + 1];
        paddedLongs[0] = paddedLongs[paddedLongs.length - 1] = -1L;
        System.arraycopy(longs, 0, paddedLongs, 1, longs.length);
        assertBitSet(BitSet.valueOf(LongBuffer.wrap(paddedLongs, 1, longs.length)), longs, s);

        // Check that the long[] is copied.
        if (longs.length > 0) {
            BitSet original = BitSet.valueOf(longs);
            longs[0] = ~longs[0];
            assertFalse(BitSet.valueOf(longs).equals(original));
        }
    }

    public void test_valueOf_long() throws Exception {
        assertBitSet(new long[0], "{}");
        assertBitSet(new long[] { 1L }, "{0}");
        assertBitSet(new long[] { 0x111L }, "{0, 4, 8}");
        assertBitSet(new long[] { 0x101L, 0x4000000000000000L }, "{0, 8, 126}");
    }

    private static void assertBitSet(BitSet bs, byte[] bytes, String s) {
        for (int i = 0; i < 8 * bytes.length; ++i) {
            assertEquals(bs.toString(), ((bytes[i / 8] & (1L << (i % 8))) != 0), bs.get(i));
        }
        int cardinality = 0;
        for (int i = 0; i < bytes.length; ++i) {
            cardinality += Integer.bitCount(((int) bytes[i]) & 0xff);
        }
        if (cardinality != 0) {
            assertFalse(bs.isEmpty());
        } else {
            assertTrue(bs.isEmpty());
        }
        assertEquals(cardinality, bs.cardinality());
        assertEquals(roundUp(8 * bytes.length, 64), bs.size());
        assertEquals(s, bs.toString());
    }

    private static int roundUp(int n, int multiple) {
        return (n == 0) ? 0 : ((n + multiple - 1) / multiple) * multiple;
    }

    private static void assertBitSet(byte[] bytes, String s) {
        // Test BitSet.valueOf(byte[]).
        assertBitSet(BitSet.valueOf(bytes), bytes, s);
        // Test BitSet.valueOf(ByteBuffer).
        assertBitSet(BitSet.valueOf(ByteBuffer.wrap(bytes)), bytes, s);
        // Surround 'bytes' with junk set bits but exclude them from the ByteBuffer.
        byte[] paddedBytes = new byte[1 + bytes.length + 1];
        paddedBytes[0] = paddedBytes[paddedBytes.length - 1] = (byte) -1;
        System.arraycopy(bytes, 0, paddedBytes, 1, bytes.length);
        assertBitSet(BitSet.valueOf(ByteBuffer.wrap(paddedBytes, 1, bytes.length)), bytes, s);

        // Check that the byte[] is copied.
        if (bytes.length > 0) {
            BitSet original = BitSet.valueOf(bytes);
            bytes[0] = (byte) ~bytes[0];
            assertFalse(BitSet.valueOf(bytes).equals(original));
        }
    }

    public void test_valueOf_byte() throws Exception {
        // Nothing...
        assertBitSet(new byte[0], "{}");
        // Less than a long...
        assertBitSet(new byte[] { 0x01 }, "{0}");
        assertBitSet(new byte[] { 0x01, 0x11 }, "{0, 8, 12}");
        assertBitSet(new byte[] { 0x01, 0x01, 0x00, 0x00, 0x01 }, "{0, 8, 32}");
        // Exactly one long....
        assertBitSet(new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80 }, "{0, 63}");
        // One long and a byte left over...
        assertBitSet(new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 }, "{0, 64}");
        // Two longs...
        byte[] bytes = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80,
                                    0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80 };
        assertBitSet(bytes, "{0, 63, 64, 127}");
    }

    public void test_toLongArray() throws Exception {
        assertEquals("[]", Arrays.toString(BitSet.valueOf(new long[0]).toLongArray()));
        assertEquals("[1]", Arrays.toString(BitSet.valueOf(new long[] { 1 }).toLongArray()));
        assertEquals("[1, 2]", Arrays.toString(BitSet.valueOf(new long[] { 1, 2 }).toLongArray()));

        // Check that we're not returning trailing empty space.
        assertEquals("[]", Arrays.toString(new BitSet(128).toLongArray()));
        BitSet bs = new BitSet();
        bs.set(0);
        bs.set(64, 66);
        bs.clear(64, 66);
        assertEquals("[1]", Arrays.toString(bs.toLongArray()));
    }

    public void test_toByteArray() throws Exception {
        assertEquals("[]", Arrays.toString(BitSet.valueOf(new long[0]).toByteArray()));
        assertEquals("[1]", Arrays.toString(BitSet.valueOf(new long[] { 1 }).toByteArray()));
        assertEquals("[-17, -51, -85, -112, 120, 86, 52, 18]",
                Arrays.toString(BitSet.valueOf(new long[] { 0x1234567890abcdefL }).toByteArray()));
        assertEquals("[1, 0, 0, 0, 0, 0, 0, 0, 2]",
                Arrays.toString(BitSet.valueOf(new long[] { 1, 2 }).toByteArray()));
    }

    public void test_previousSetBit() {
        assertEquals(-1, new BitSet().previousSetBit(666));

        BitSet bs;

        bs = new BitSet();
        bs.set(32);
        assertEquals(32, bs.previousSetBit(999));
        assertEquals(32, bs.previousSetBit(33));
        assertEquals(32, bs.previousSetBit(32));
        assertEquals(-1, bs.previousSetBit(31));

        bs = new BitSet();
        bs.set(0);
        bs.set(1);
        bs.set(32);
        bs.set(192);
        bs.set(666);

        assertEquals(666, bs.previousSetBit(999));
        assertEquals(666, bs.previousSetBit(667));
        assertEquals(666, bs.previousSetBit(666));
        assertEquals(192, bs.previousSetBit(665));
        assertEquals(32, bs.previousSetBit(191));
        assertEquals(1, bs.previousSetBit(31));
        assertEquals(0, bs.previousSetBit(0));
        assertEquals(-1, bs.previousSetBit(-1));
    }

    private static BitSet big() {
        BitSet result = new BitSet();
        result.set(1000);
        return result;
    }

    private static BitSet small() {
        BitSet result = new BitSet();
        result.set(10);
        return result;
    }

    public void test_differentSizes() {
        BitSet result = big();
        result.and(small());
        assertEquals("{}", result.toString());
        result = small();
        result.and(big());
        assertEquals("{}", result.toString());

        result = big();
        result.andNot(small());
        assertEquals("{1000}", result.toString());
        result = small();
        result.andNot(big());
        assertEquals("{10}", result.toString());

        assertFalse(big().intersects(small()));
        assertFalse(small().intersects(big()));

        result = big();
        result.or(small());
        assertEquals("{10, 1000}", result.toString());
        result = small();
        result.or(big());
        assertEquals("{10, 1000}", result.toString());

        result = big();
        result.xor(small());
        assertEquals("{10, 1000}", result.toString());
        result = small();
        result.xor(big());
        assertEquals("{10, 1000}", result.toString());
    }
}
