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

/*-[
#include "BufferUtils.h"
#include "Portability.h"

#if defined(__arm__)
// ARM has load/store alignment restrictions for longs, ints and shorts.
#if defined(__LP64__)
#define LONG_ALIGNMENT_MASK 0x7
#define INT_ALIGNMENT_MASK 0x3
#define SHORT_ALIGNMENT_MASK 0x1
#else
#define LONG_ALIGNMENT_MASK 0x3
#define INT_ALIGNMENT_MASK 0x3
#define SHORT_ALIGNMENT_MASK 0x1
#endif
#else
// x86 can load anything at any alignment.
#define LONG_ALIGNMENT_MASK 0x0
#define INT_ALIGNMENT_MASK 0x0
#define SHORT_ALIGNMENT_MASK 0x0
#endif
]-*/

/**
 * Unsafe access to memory.
 */
public final class Memory {
    private Memory() { }

    /*-[
      // Use packed structures for access to unaligned data on targets with alignment restrictions.
      // The compiler will generate appropriate code to access these structures without
      // generating alignment exceptions.
      #define GET_UNALIGNED(SCALAR_TYPE, NAME) \
        static inline SCALAR_TYPE get_##NAME##_unaligned(const SCALAR_TYPE *address) { \
          typedef struct __attribute__ ((packed)) { \
            SCALAR_TYPE v; \
          } unaligned; \
          const unaligned *p = (const unaligned *) address; \
          return p->v; \
        }

      GET_UNALIGNED(short, short);
      GET_UNALIGNED(int, int);
      GET_UNALIGNED(long long, long);

      #define PUT_UNALIGNED(SCALAR_TYPE, NAME) \
        static inline void put_##NAME##_unaligned(SCALAR_TYPE *address, SCALAR_TYPE v) { \
          typedef struct __attribute__ ((packed)) { \
            SCALAR_TYPE v; \
          } unaligned; \
          unaligned *p = (unaligned *) address; \
          p->v = v; \
        }

      PUT_UNALIGNED(short, short);
      PUT_UNALIGNED(int, int);

      // Byte-swap 2 short values packed in an int.
      static inline int bswap_2x16(int v) {
        // v is initially ABCD
        #if defined(__mips__) && defined(__mips_isa_rev) && (__mips_isa_rev >= 2)
          __asm__ volatile ("wsbh %0, %0" : "+r" (v));  // v=BADC
        #else
          v = bswap_32(v);                              // v=DCBA
          v = (v << 16) | ((v >> 16) & 0xffff);         // v=BADC
        #endif
        return v;
      }

      static inline void swapShorts(short* dstShorts, const short* srcShorts, size_t count) {
        // Do 32-bit swaps as long as possible...
        int *dst = (int *) dstShorts;
        const int* src = (const int *) srcShorts;

        if (((uintptr_t) dst & INT_ALIGNMENT_MASK) == 0 &&
          ((uintptr_t) src & INT_ALIGNMENT_MASK) == 0) {
          for (size_t i = 0; i < count / 2; ++i) {
            int v = *src++;
            *dst++ = bswap_2x16(v);
          }
          // ...with one last 16-bit swap if necessary.
          if ((count % 2) != 0) {
            short v = *(const short *) src;
            *(short *) dst = bswap_16(v);
          }
        } else {
          for (size_t i = 0; i < count / 2; ++i) {
              int v = get_int_unaligned(src++);
              put_int_unaligned(dst++, bswap_2x16(v));
          }
          if ((count % 2) != 0) {
            short v = get_short_unaligned((const short *) src);
            put_short_unaligned((short *) dst, bswap_16(v));
          }
        }
      }

      static inline void swapInts(int *dstInts, const int *srcInts, size_t count) {
        if (((uintptr_t) dstInts & INT_ALIGNMENT_MASK) == 0 &&
          ((uintptr_t) srcInts & INT_ALIGNMENT_MASK) == 0) {
          for (size_t i = 0; i < count; ++i) {
            int v = *srcInts++;
            *dstInts++ = bswap_32(v);
          }
        } else {
          for (size_t i = 0; i < count; ++i) {
            int v = get_int_unaligned(srcInts++);
            put_int_unaligned(dstInts++, bswap_32(v));
          }
        }
      }

      static inline void swapLongs(long long *dstLongs, const long long *srcLongs, size_t count) {
        int *dst = (int *) dstLongs;
        const int *src = (const int *) srcLongs;
        if (((uintptr_t) dstLongs & INT_ALIGNMENT_MASK) == 0 &&
          ((uintptr_t) srcLongs & INT_ALIGNMENT_MASK) == 0) {
          for (size_t i = 0; i < count; ++i) {
            int v1 = *src++;
            int v2 = *src++;
            *dst++ = bswap_32(v2);
            *dst++ = bswap_32(v1);
          }
        } else {
          for (size_t i = 0; i < count; ++i) {
              int v1 = get_int_unaligned(src++);
              int v2 = get_int_unaligned(src++);
              put_int_unaligned(dst++, bswap_32(v2));
              put_int_unaligned(dst++, bswap_32(v1));
          }
        }
      }

      #define PEEKER(SCALAR_TYPE, NAME, SWAP_TYPE, SWAP_FN) \
        if (swap) { \
          const SWAP_TYPE *src = (const SWAP_TYPE *) address; \
          SWAP_FN((SWAP_TYPE *) IOS ## NAME ## Array_GetRef(dst, dstOffset), src, count); \
        } else { \
          const SCALAR_TYPE *src = (const SCALAR_TYPE *) address; \
          memmove(IOS ## NAME ## Array_GetRef(dst, dstOffset), src, count * sizeof(SCALAR_TYPE)); \
        }

      #define POKER(SCALAR_TYPE, NAME, SWAP_TYPE, SWAP_FN) \
        if (swap) { \
          SWAP_TYPE *dst = (SWAP_TYPE *) address; \
          SWAP_FN(dst, (const SWAP_TYPE *) IOS ## NAME ## Array_GetRef(src, srcOffset), count); \
        } else { \
          SCALAR_TYPE *dst = (SCALAR_TYPE *) address; \
          memmove(dst, IOS ## NAME ## Array_GetRef(src, srcOffset), count * sizeof(SCALAR_TYPE)); \
        }


      extern void unsafeBulkCopy(char *dst, const char *src, int byteCount, int sizeofElement,
          BOOL swap) {
        if (!swap) {
          memcpy(dst, src, byteCount);
          return;
        }
        if (sizeofElement == 2) {
          swapShorts((short *) dst, (short *) src, byteCount / 2);
        } else if (sizeofElement == 4) {
          swapInts((int *) dst, (int *) src, byteCount / 4);
        } else if (sizeofElement == 8) {
          swapLongs((long long *) dst, (long long *) src, byteCount / 8);
        }
      }
    ]-*/

    public static int peekInt(byte[] src, int offset, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            return (((src[offset  ] & 0xff) << 24) |
                    ((src[offset+1] & 0xff) << 16) |
                    ((src[offset+2] & 0xff) <<  8) |
                    ((src[offset+3] & 0xff) <<  0));
        } else {
            return (((src[offset  ] & 0xff) <<  0) |
                    ((src[offset+1] & 0xff) <<  8) |
                    ((src[offset+2] & 0xff) << 16) |
                    ((src[offset+3] & 0xff) << 24));
        }
    }

    public static long peekLong(byte[] src, int offset, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            int h = ((src[offset  ] & 0xff) << 24) |
                    ((src[offset+1] & 0xff) << 16) |
                    ((src[offset+2] & 0xff) <<  8) |
                    ((src[offset+3] & 0xff) <<  0);
            int l = ((src[offset+4] & 0xff) << 24) |
                    ((src[offset+5] & 0xff) << 16) |
                    ((src[offset+6] & 0xff) <<  8) |
                    ((src[offset+7] & 0xff) <<  0);
            return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
        } else {
            int l = ((src[offset  ] & 0xff) <<  0) |
                    ((src[offset+1] & 0xff) <<  8) |
                    ((src[offset+2] & 0xff) << 16) |
                    ((src[offset+3] & 0xff) << 24);
            int h = ((src[offset+4] & 0xff) <<  0) |
                    ((src[offset+5] & 0xff) <<  8) |
                    ((src[offset+6] & 0xff) << 16) |
                    ((src[offset+7] & 0xff) << 24);
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
            dst[offset  ] = (byte) ((value >> 24) & 0xff);
            dst[offset+1] = (byte) ((value >> 16) & 0xff);
            dst[offset+2] = (byte) ((value >>  8) & 0xff);
            dst[offset+3] = (byte) ((value >>  0) & 0xff);
        } else {
            dst[offset  ] = (byte) ((value >>  0) & 0xff);
            dst[offset+1] = (byte) ((value >>  8) & 0xff);
            dst[offset+2] = (byte) ((value >> 16) & 0xff);
            dst[offset+3] = (byte) ((value >> 24) & 0xff);
        }
    }

    public static void pokeLong(byte[] dst, int offset, long value, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            int i = (int) (value >> 32);
            dst[offset  ] = (byte) ((i >> 24) & 0xff);
            dst[offset+1] = (byte) ((i >> 16) & 0xff);
            dst[offset+2] = (byte) ((i >>  8) & 0xff);
            dst[offset+3] = (byte) ((i >>  0) & 0xff);
            i = (int) value;
            dst[offset+4] = (byte) ((i >> 24) & 0xff);
            dst[offset+5] = (byte) ((i >> 16) & 0xff);
            dst[offset+6] = (byte) ((i >>  8) & 0xff);
            dst[offset+7] = (byte) ((i >>  0) & 0xff);
        } else {
            int i = (int) value;
            dst[offset  ] = (byte) ((i >>  0) & 0xff);
            dst[offset+1] = (byte) ((i >>  8) & 0xff);
            dst[offset+2] = (byte) ((i >> 16) & 0xff);
            dst[offset+3] = (byte) ((i >> 24) & 0xff);
            i = (int) (value >> 32);
            dst[offset+4] = (byte) ((i >>  0) & 0xff);
            dst[offset+5] = (byte) ((i >>  8) & 0xff);
            dst[offset+6] = (byte) ((i >> 16) & 0xff);
            dst[offset+7] = (byte) ((i >> 24) & 0xff);
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
    public static native void memmove(Object dstObject, int dstOffset, Object srcObject,
        int srcOffset, long byteCount) /*-[
      char *dstBytes = BytesRW(dstObject);
      if (!dstBytes) {
        return;
      }
      const char *srcBytes = BytesRO(srcObject);
      if (!srcBytes) {
        return;
      }
      memmove(dstBytes + dstOffset, srcBytes + srcOffset, (size_t)byteCount);
    ]-*/;

    public static native byte peekByte(long address) /*-[
      return *(const char *) address;
    ]-*/;

    public static int peekInt(long address, boolean swap) {
        int result = peekIntNative(address);
        if (swap) {
            result = Integer.reverseBytes(result);
        }
        return result;
    }

    private static native int peekIntNative(long address) /*-[
      return *(int *) address;
    ]-*/;

    public static long peekLong(long address, boolean swap) {
        long result = peekLongNative(address);
        if (swap) {
            result = Long.reverseBytes(result);
        }
        return result;
    }

    private static native long peekLongNative(long address) /*-[
      long long result;
      const long long* src = (const long long *) address;
      if ((address & LONG_ALIGNMENT_MASK) == 0) {
          result = *src;
      } else {
          result = get_long_unaligned(src);
      }
      return result;
    ]-*/;

    public static short peekShort(long address, boolean swap) {
        short result = peekShortNative(address);
        if (swap) {
            result = Short.reverseBytes(result);
        }
        return result;
    }

    private static native short peekShortNative(long address) /*-[
      return *(short *) address;
    ]-*/;

    public static native void peekByteArray(long address, byte[] dst, int dstOffset,
        int count) /*-[
      memmove(((IOSByteArray *) dst)->buffer_ + dstOffset, (const char *) address, count);
    ]-*/;

    public static native void peekCharArray(long address, char[] dst, int dstOffset, int count,
        boolean swap) /*-[
      PEEKER(unichar, Char, short, swapShorts);
    ]-*/;

    public static native void peekDoubleArray(long address, double[] dst, int dstOffset,
        int count, boolean swap) /*-[
      PEEKER(double, Double, long long, swapLongs);
    ]-*/;

    public static native void peekFloatArray(long address, float[] dst, int dstOffset,
        int count, boolean swap) /*-[
      PEEKER(float, Float, int, swapInts);
    ]-*/;

    public static native void peekIntArray(long address, int[] dst, int dstOffset,
        int count, boolean swap) /*-[
      PEEKER(int, Int, int, swapInts);
    ]-*/;

    public static native void peekLongArray(long address, long[] dst, int dstOffset,
        int count, boolean swap) /*-[
      PEEKER(long long, Long, long long, swapLongs);
    ]-*/;

    public static native void peekShortArray(long address, short[] dst, int dstOffset,
        int count, boolean swap) /*-[
      PEEKER(short, Short, short, swapShorts);
    ]-*/;


    public static native void pokeByte(long address, byte value) /*-[
      *(char *) address = value;
    ]-*/;

    public static void pokeInt(long address, int value, boolean swap) {
        if (swap) {
            value = Integer.reverseBytes(value);
        }
        pokeIntNative(address, value);
    }

    private static native void pokeIntNative(long address, int value) /*-[
      *(int *) address = value;
    ]-*/;

    public static void pokeLong(long address, long value, boolean swap) {
        if (swap) {
            value = Long.reverseBytes(value);
        }
        pokeLongNative(address, value);
    }

    private static native void pokeLongNative(long address, long value) /*-[
      *(long long *) address = value;
    ]-*/;

    public static void pokeShort(long address, short value, boolean swap) {
        if (swap) {
            value = Short.reverseBytes(value);
        }
        pokeShortNative(address, value);
    }

    private static native void pokeShortNative(long address, short value) /*-[
      *(short *) address = value;
    ]-*/;


    public static native void pokeByteArray(long address, byte[] src, int srcOffset, int count) /*-[
      memmove((char *) address, src->buffer_ + srcOffset, count);
    ]-*/;

    public static native void pokeCharArray(long address, char[] src, int srcOffset, int count,
        boolean swap) /*-[
      POKER(unichar, Char, short, swapShorts);
    ]-*/;

    public static native void pokeDoubleArray(long address, double[] src, int srcOffset, int count,
        boolean swap) /*-[
      POKER(double, Double, long long, swapLongs);
    ]-*/;

    public static native void pokeFloatArray(long address, float[] src, int srcOffset, int count,
        boolean swap) /*-[
      POKER(float, Float, int, swapInts);
    ]-*/;

    public static native void pokeIntArray(long address, int[] src, int srcOffset, int count,
        boolean swap) /*-[
      POKER(int, Int, int, swapInts);
    ]-*/;

    public static native void pokeLongArray(long address, long[] src, int srcOffset, int count,
        boolean swap) /*-[
      POKER(long long, Long, long long, swapLongs);
    ]-*/;

    public static native void pokeShortArray(long address, short[] src, int srcOffset, int count,
        boolean swap) /*-[
      POKER(short, Short, short, swapShorts);
    ]-*/;
}
