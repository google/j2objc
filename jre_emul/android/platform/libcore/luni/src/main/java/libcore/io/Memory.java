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

package libcore.io;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Unsafe access to memory.
 */
public final class Memory {
    private Memory() { }

    /**
     * Used to optimize nio heap buffer bulk get operations. 'dst' must be a primitive array.
     * 'dstOffset' is measured in units of 'sizeofElements' bytes.
     */
    public static native void unsafeBulkGet(Object dst, int dstOffset, int byteCount,
            byte[] src, int srcOffset, int sizeofElements, boolean swap);

    /**
     * Used to optimize nio heap buffer bulk put operations. 'src' must be a primitive array.
     * 'srcOffset' is measured in units of 'sizeofElements' bytes.
     */
    public static native void unsafeBulkPut(byte[] dst, int dstOffset, int byteCount,
            Object src, int srcOffset, int sizeofElements, boolean swap);

    public static int peekInt(byte[] src, int offset, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            return (((src[offset++] & 0xff) << 24) |
                    ((src[offset++] & 0xff) << 16) |
                    ((src[offset++] & 0xff) <<  8) |
                    ((src[offset  ] & 0xff) <<  0));
        } else {
            return (((src[offset++] & 0xff) <<  0) |
                    ((src[offset++] & 0xff) <<  8) |
                    ((src[offset++] & 0xff) << 16) |
                    ((src[offset  ] & 0xff) << 24));
        }
    }

    public static long peekLong(byte[] src, int offset, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            int h = ((src[offset++] & 0xff) << 24) |
                    ((src[offset++] & 0xff) << 16) |
                    ((src[offset++] & 0xff) <<  8) |
                    ((src[offset++] & 0xff) <<  0);
            int l = ((src[offset++] & 0xff) << 24) |
                    ((src[offset++] & 0xff) << 16) |
                    ((src[offset++] & 0xff) <<  8) |
                    ((src[offset  ] & 0xff) <<  0);
            return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
        } else {
            int l = ((src[offset++] & 0xff) <<  0) |
                    ((src[offset++] & 0xff) <<  8) |
                    ((src[offset++] & 0xff) << 16) |
                    ((src[offset++] & 0xff) << 24);
            int h = ((src[offset++] & 0xff) <<  0) |
                    ((src[offset++] & 0xff) <<  8) |
                    ((src[offset++] & 0xff) << 16) |
                    ((src[offset  ] & 0xff) << 24);
            return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
        }
    }

    public static short peekShort(byte[] src, int offset, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            return (short) ((src[offset] << 8) | (src[offset + 1] & 0xff));
        } else {
            return (short) ((src[offset + 1] << 8) | (src[offset] & 0xff));
        }
    }

    public static void pokeInt(byte[] dst, int offset, int value, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            dst[offset++] = (byte) ((value >> 24) & 0xff);
            dst[offset++] = (byte) ((value >> 16) & 0xff);
            dst[offset++] = (byte) ((value >>  8) & 0xff);
            dst[offset  ] = (byte) ((value >>  0) & 0xff);
        } else {
            dst[offset++] = (byte) ((value >>  0) & 0xff);
            dst[offset++] = (byte) ((value >>  8) & 0xff);
            dst[offset++] = (byte) ((value >> 16) & 0xff);
            dst[offset  ] = (byte) ((value >> 24) & 0xff);
        }
    }

    public static void pokeLong(byte[] dst, int offset, long value, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            int i = (int) (value >> 32);
            dst[offset++] = (byte) ((i >> 24) & 0xff);
            dst[offset++] = (byte) ((i >> 16) & 0xff);
            dst[offset++] = (byte) ((i >>  8) & 0xff);
            dst[offset++] = (byte) ((i >>  0) & 0xff);
            i = (int) value;
            dst[offset++] = (byte) ((i >> 24) & 0xff);
            dst[offset++] = (byte) ((i >> 16) & 0xff);
            dst[offset++] = (byte) ((i >>  8) & 0xff);
            dst[offset  ] = (byte) ((i >>  0) & 0xff);
        } else {
            int i = (int) value;
            dst[offset++] = (byte) ((i >>  0) & 0xff);
            dst[offset++] = (byte) ((i >>  8) & 0xff);
            dst[offset++] = (byte) ((i >> 16) & 0xff);
            dst[offset++] = (byte) ((i >> 24) & 0xff);
            i = (int) (value >> 32);
            dst[offset++] = (byte) ((i >>  0) & 0xff);
            dst[offset++] = (byte) ((i >>  8) & 0xff);
            dst[offset++] = (byte) ((i >> 16) & 0xff);
            dst[offset  ] = (byte) ((i >> 24) & 0xff);
        }
    }

    public static void pokeShort(byte[] dst, int offset, short value, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            dst[offset++] = (byte) ((value >> 8) & 0xff);
            dst[offset  ] = (byte) ((value >> 0) & 0xff);
        } else {
            dst[offset++] = (byte) ((value >> 0) & 0xff);
            dst[offset  ] = (byte) ((value >> 8) & 0xff);
        }
    }

    /**
     * Copies 'byteCount' bytes from the source to the destination. The objects are either
     * instances of DirectByteBuffer or byte[]. The offsets in the byte[] case must include
     * the Buffer.arrayOffset if the array came from a Buffer.array call. We could make this
     * private and provide the four type-safe variants, but then ByteBuffer.put(ByteBuffer)
     * would need to work out which to call based on whether the source and destination buffers
     * are direct or not.
     *
     * @hide make type-safe before making public?
     */
    public static native void memmove(Object dstObject, int dstOffset, Object srcObject, int srcOffset, long byteCount);

    public static native byte peekByte(long address);

    public static int peekInt(long address, boolean swap) {
        int result = peekIntNative(address);
        if (swap) {
            result = Integer.reverseBytes(result);
        }
        return result;
    }
    private static native int peekIntNative(long address);

    public static long peekLong(long address, boolean swap) {
        long result = peekLongNative(address);
        if (swap) {
            result = Long.reverseBytes(result);
        }
        return result;
    }
    private static native long peekLongNative(long address);

    public static short peekShort(long address, boolean swap) {
        short result = peekShortNative(address);
        if (swap) {
            result = Short.reverseBytes(result);
        }
        return result;
    }
    private static native short peekShortNative(long address);

    public static native void peekByteArray(long address, byte[] dst, int dstOffset, int byteCount);
    public static native void peekCharArray(long address, char[] dst, int dstOffset, int charCount, boolean swap);
    public static native void peekDoubleArray(long address, double[] dst, int dstOffset, int doubleCount, boolean swap);
    public static native void peekFloatArray(long address, float[] dst, int dstOffset, int floatCount, boolean swap);
    public static native void peekIntArray(long address, int[] dst, int dstOffset, int intCount, boolean swap);
    public static native void peekLongArray(long address, long[] dst, int dstOffset, int longCount, boolean swap);
    public static native void peekShortArray(long address, short[] dst, int dstOffset, int shortCount, boolean swap);

    public static native void pokeByte(long address, byte value);

    public static void pokeInt(long address, int value, boolean swap) {
        if (swap) {
            value = Integer.reverseBytes(value);
        }
        pokeIntNative(address, value);
    }
    private static native void pokeIntNative(long address, int value);

    public static void pokeLong(long address, long value, boolean swap) {
        if (swap) {
            value = Long.reverseBytes(value);
        }
        pokeLongNative(address, value);
    }
    private static native void pokeLongNative(long address, long value);

    public static void pokeShort(long address, short value, boolean swap) {
        if (swap) {
            value = Short.reverseBytes(value);
        }
        pokeShortNative(address, value);
    }
    private static native void pokeShortNative(long address, short value);

    public static native void pokeByteArray(long address, byte[] src, int offset, int count);
    public static native void pokeCharArray(long address, char[] src, int offset, int count, boolean swap);
    public static native void pokeDoubleArray(long address, double[] src, int offset, int count, boolean swap);
    public static native void pokeFloatArray(long address, float[] src, int offset, int count, boolean swap);
    public static native void pokeIntArray(long address, int[] src, int offset, int count, boolean swap);
    public static native void pokeLongArray(long address, long[] src, int offset, int count, boolean swap);
    public static native void pokeShortArray(long address, short[] src, int offset, int count, boolean swap);
}
