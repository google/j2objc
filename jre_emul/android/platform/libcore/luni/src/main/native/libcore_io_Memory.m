/*
 * Copyright (C) 2007 The Android Open Source Project
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

#define LOG_TAG "Memory"

#include "BufferUtils.h"
#include "Portability.h"
#include "jni.h"
#include "libcore/io/Memory.h"

#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>

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
PUT_UNALIGNED(long long, long);

// Byte-swap 2 jshort values packed in a jint.
static inline jint bswap_2x16(jint v) {
    // v is initially ABCD
#if defined(__mips__) && defined(__mips_isa_rev) && (__mips_isa_rev >= 2)
    __asm__ volatile ("wsbh %0, %0" : "+r" (v));  // v=BADC
#else
    v = bswap_32(v);                              // v=DCBA
    v = (v << 16) | ((v >> 16) & 0xffff);         // v=BADC
#endif
    return v;
}

static inline void swapShorts(jshort* dstShorts, const jshort* srcShorts, size_t count) {
    // Do 32-bit swaps as long as possible...
    jint* dst = (jint*) dstShorts;
    const jint* src = (const jint*) srcShorts;
    for (size_t i = 0; i < count / 2; ++i) {
        jint v = get_int_unaligned(src++);
        put_int_unaligned(dst++, bswap_2x16(v));
    }
    if ((count % 2) != 0) {
      jshort v = get_short_unaligned((const jshort*) src);
      put_short_unaligned((jshort*) dst, bswap_16(v));
    }
}

static inline void swapInts(jint* dstInts, const jint* srcInts, size_t count) {
    for (size_t i = 0; i < count; ++i) {
        jint v = get_int_unaligned(srcInts++);
        put_int_unaligned(dstInts++, bswap_32(v));
    }
}

static inline void swapLongs(jlong* dstLongs, const jlong* srcLongs, size_t count) {
    jint* dst = (jint*) dstLongs;
    const jint* src = (const jint*) srcLongs;
    for (size_t i = 0; i < count; ++i) {
        jint v1 = get_int_unaligned(src++);
        jint v2 = get_int_unaligned(src++);
        put_int_unaligned(dst++, bswap_32(v2));
        put_int_unaligned(dst++, bswap_32(v1));
    }
}

void Java_libcore_io_Memory_memmove(JNIEnv* env, jclass c, jobject dstObject, jint dstOffset, jobject srcObject, jint srcOffset, jlong length) {
    char *dstBytes = BytesRW(dstObject);
    if (!dstBytes) {
      return;
    }
    const char *srcBytes = BytesRO(srcObject);
    if (!srcBytes) {
      return;
    }
    memmove(dstBytes + dstOffset, srcBytes + srcOffset, (size_t)length);
}

jbyte Java_libcore_io_Memory_peekByte(JNIEnv* env, jclass c, jlong srcAddress) {
    return *(const char *) srcAddress;
}

void Java_libcore_io_Memory_peekByteArray(JNIEnv* env, jclass c, jlong srcAddress, jbyteArray dst, jint dstOffset, jint byteCount) {
    memmove(((IOSByteArray *) dst)->buffer_ + dstOffset, (const char *) srcAddress, byteCount);
}

// Implements the peekXArray methods:
// - For unswapped access, we just use the JNI SetXArrayRegion functions.
// - For swapped access, we use GetXArrayElements and our own copy-and-swap routines.
//   GetXArrayElements is disproportionately cheap on Dalvik because it doesn't copy (as opposed
//   to Hotspot, which always copies). The SWAP_FN copies and swaps in one pass, which is cheaper
//   than copying and then swapping in a second pass. Depending on future VM/GC changes, the
//   swapped case might need to be revisited.
#define PEEKER(SCALAR_TYPE, NAME, SWAP_TYPE, SWAP_FN) \
  if (swap) { \
    const SWAP_TYPE *src = (const SWAP_TYPE *) srcAddress; \
    SWAP_FN((SWAP_TYPE *) IOS ## NAME ## Array_GetRef(dst, dstOffset), src, count); \
  } else { \
    const SCALAR_TYPE *src = (const SCALAR_TYPE *) srcAddress; \
    memmove(IOS ## NAME ## Array_GetRef(dst, dstOffset), src, count * sizeof(SCALAR_TYPE)); \
  }

void Java_libcore_io_Memory_peekCharArray(JNIEnv* env, jclass c, jlong srcAddress, jcharArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jchar, Char, jshort, swapShorts);
}

void Java_libcore_io_Memory_peekDoubleArray(JNIEnv* env, jclass c, jlong srcAddress, jdoubleArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jdouble, Double, jlong, swapLongs);
}

void Java_libcore_io_Memory_peekFloatArray(JNIEnv* env, jclass c, jlong srcAddress, jfloatArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jfloat, Float, jint, swapInts);
}

void Java_libcore_io_Memory_peekIntArray(JNIEnv* env, jclass c, jlong srcAddress, jintArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jint, Int, jint, swapInts);
}

void Java_libcore_io_Memory_peekLongArray(JNIEnv* env, jclass c, jlong srcAddress, jlongArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jlong, Long, jlong, swapLongs);
}

void Java_libcore_io_Memory_peekShortArray(JNIEnv* env, jclass c, jlong srcAddress, jshortArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jshort, Short, jshort, swapShorts);
}

void Java_libcore_io_Memory_pokeByte(JNIEnv* env, jclass c, jlong dstAddress, jbyte value) {
    *(int *) dstAddress = value;
}

void Java_libcore_io_Memory_pokeByteArray(JNIEnv* env, jclass c, jlong dstAddress, jbyteArray src, jint offset, jint length) {
    memmove((char *) dstAddress, src->buffer_ + offset, length);
}

// Implements the pokeXArray methods:
// - For unswapped access, we just use the JNI GetXArrayRegion functions.
// - For swapped access, we use GetXArrayElements and our own copy-and-swap routines.
//   GetXArrayElements is disproportionately cheap on Dalvik because it doesn't copy (as opposed
//   to Hotspot, which always copies). The SWAP_FN copies and swaps in one pass, which is cheaper
//   than copying and then swapping in a second pass. Depending on future VM/GC changes, the
//   swapped case might need to be revisited.
#define POKER(SCALAR_TYPE, NAME, SWAP_TYPE, SWAP_FN) \
  if (swap) { \
    SWAP_TYPE *dst = (SWAP_TYPE *) dstAddress; \
    SWAP_FN(dst, (const SWAP_TYPE *) IOS ## NAME ## Array_GetRef(src, srcOffset), count); \
  } else { \
    SCALAR_TYPE *dst = (SCALAR_TYPE *) dstAddress; \
    memmove(dst, IOS ## NAME ## Array_GetRef(src, srcOffset), count * sizeof(SCALAR_TYPE)); \
  }

void Java_libcore_io_Memory_pokeCharArray(JNIEnv* env, jclass c, jlong dstAddress, jcharArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jchar, Char, jshort, swapShorts);
}

void Java_libcore_io_Memory_pokeDoubleArray(JNIEnv* env, jclass c, jlong dstAddress, jdoubleArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jdouble, Double, jlong, swapLongs);
}

void Java_libcore_io_Memory_pokeFloatArray(JNIEnv* env, jclass c, jlong dstAddress, jfloatArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jfloat, Float, jint, swapInts);
}

void Java_libcore_io_Memory_pokeIntArray(JNIEnv* env, jclass c, jlong dstAddress, jintArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jint, Int, jint, swapInts);
}

void Java_libcore_io_Memory_pokeLongArray(JNIEnv* env, jclass c, jlong dstAddress, jlongArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jlong, Long, jlong, swapLongs);
}

void Java_libcore_io_Memory_pokeShortArray(JNIEnv* env, jclass c, jlong dstAddress, jshortArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jshort, Short, jshort, swapShorts);
}

jshort Java_libcore_io_Memory_peekShortNative(JNIEnv* env, jclass c, jlong srcAddress) {
    return get_short_unaligned((const jshort*) srcAddress);
}

void Java_libcore_io_Memory_pokeShortNative(JNIEnv* env, jclass c, jlong dstAddress, jshort value) {
    put_short_unaligned((jshort*) dstAddress, value);
}

jint Java_libcore_io_Memory_peekIntNative(JNIEnv* env, jclass c, jlong srcAddress) {
    return get_int_unaligned((const jint*) srcAddress);
}

void Java_libcore_io_Memory_pokeIntNative(JNIEnv* env, jclass c, jlong dstAddress, jint value) {
    put_int_unaligned((jint*) dstAddress, value);
}

jlong Java_libcore_io_Memory_peekLongNative(JNIEnv* env, jclass c, jlong srcAddress) {
    return get_long_unaligned((const jlong*) srcAddress );
}

void Java_libcore_io_Memory_pokeLongNative(JNIEnv* env, jclass c, jlong dstAddress, jlong value) {
    put_long_unaligned((jlong*) dstAddress, value);
}

static void unsafeBulkCopy(char *dst, const char *src, int byteCount, int sizeofElement,
    BOOL swap) {
  if (!swap) {
    memcpy(dst, (const signed char *) src, byteCount);
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

void Java_libcore_io_Memory_unsafeBulkGet(JNIEnv* env, jclass c, jobject dstObject, jint dstOffset,
        jint byteCount, jbyteArray srcArray, jint srcOffset, jint sizeofElement, jboolean swap) {
    const char *srcBytes = (const char *)srcArray->buffer_;
    if (srcBytes == NULL) {
        return;
    }
    jbyte* dstBytes = (jbyte*) BytesRW(dstObject);
    if (dstBytes == NULL) {
        return;
    }
    jbyte* dst = dstBytes + dstOffset*sizeofElement;
    const char* src = srcBytes + srcOffset;
    unsafeBulkCopy((char *)dst, src, byteCount, sizeofElement, swap);
}

void Java_libcore_io_Memory_unsafeBulkPut(JNIEnv* env, jclass c, jbyteArray dstArray, jint dstOffset,
        jint byteCount, jobject srcObject, jint srcOffset, jint sizeofElement, jboolean swap) {
    char *dstBytes = (char *)dstArray->buffer_;
    if (dstBytes == NULL) {
        return;
    }
    jbyte* srcBytes = (jbyte *) BytesRO(srcObject);
    if (srcBytes == NULL) {
        return;
    }
    char *dst = dstBytes + dstOffset;
    const jbyte *src = srcBytes + srcOffset*sizeofElement;
    unsafeBulkCopy(dst, (const char *)src, byteCount, sizeofElement, swap);
}

/* J2ObjC: unused.
static JNINativeMethod gMethods[] = {
    NATIVE_METHOD(Memory, memmove, "(Ljava/lang/Object;ILjava/lang/Object;IJ)V"),
    NATIVE_METHOD(Memory, peekByte, "!(J)B"),
    NATIVE_METHOD(Memory, peekByteArray, "(J[BII)V"),
    NATIVE_METHOD(Memory, peekCharArray, "(J[CIIZ)V"),
    NATIVE_METHOD(Memory, peekDoubleArray, "(J[DIIZ)V"),
    NATIVE_METHOD(Memory, peekFloatArray, "(J[FIIZ)V"),
    NATIVE_METHOD(Memory, peekIntNative, "!(J)I"),
    NATIVE_METHOD(Memory, peekIntArray, "(J[IIIZ)V"),
    NATIVE_METHOD(Memory, peekLongNative, "!(J)J"),
    NATIVE_METHOD(Memory, peekLongArray, "(J[JIIZ)V"),
    NATIVE_METHOD(Memory, peekShortNative, "!(J)S"),
    NATIVE_METHOD(Memory, peekShortArray, "(J[SIIZ)V"),
    NATIVE_METHOD(Memory, pokeByte, "!(JB)V"),
    NATIVE_METHOD(Memory, pokeByteArray, "(J[BII)V"),
    NATIVE_METHOD(Memory, pokeCharArray, "(J[CIIZ)V"),
    NATIVE_METHOD(Memory, pokeDoubleArray, "(J[DIIZ)V"),
    NATIVE_METHOD(Memory, pokeFloatArray, "(J[FIIZ)V"),
    NATIVE_METHOD(Memory, pokeIntNative, "!(JI)V"),
    NATIVE_METHOD(Memory, pokeIntArray, "(J[IIIZ)V"),
    NATIVE_METHOD(Memory, pokeLongNative, "!(JJ)V"),
    NATIVE_METHOD(Memory, pokeLongArray, "(J[JIIZ)V"),
    NATIVE_METHOD(Memory, pokeShortNative, "!(JS)V"),
    NATIVE_METHOD(Memory, pokeShortArray, "(J[SIIZ)V"),
    NATIVE_METHOD(Memory, unsafeBulkGet, "(Ljava/lang/Object;II[BIIZ)V"),
    NATIVE_METHOD(Memory, unsafeBulkPut, "([BIILjava/lang/Object;IIZ)V"),
};
void register_libcore_io_Memory(JNIEnv* env) {
    jniRegisterNativeMethods(env, "libcore/io/Memory", gMethods, NELEM(gMethods));
}
*/
