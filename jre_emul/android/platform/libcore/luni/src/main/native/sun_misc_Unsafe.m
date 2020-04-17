/*
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

#include "java/lang/AssertionError.h"
#include "java/lang/IllegalAccessException.h"
#include "java/lang/OutOfMemoryError.h"
#include "java/lang/reflect/Field.h"
#include "sun/misc/Unsafe.h"

#include "jni.h"
#include <unistd.h>

static void unalignedPointer(void *ptr) {
  @throw create_JavaLangAssertionError_initWithId_(
      [NSString stringWithFormat:@"Cannot perform atomic access on unaligned address %p", ptr]);
}

#define PTR(OBJ, OFFSET) (uintptr_t)(((uintptr_t)OBJ) + OFFSET)
#define CHECK_ADDR(TYPE, PTR) \
  if (sizeof(volatile_##TYPE) != sizeof(TYPE) \
      || (PTR & (__alignof__(volatile_##TYPE) - 1)) != 0) { \
    unalignedPointer((void *)PTR); \
  }

#define GET_IMPL(TYPE, MEM_ORDER) \
  uintptr_t ptr = ((uintptr_t)address); \
  CHECK_ADDR(TYPE, ptr) \
  return __c11_atomic_load((volatile_##TYPE *)ptr, __ATOMIC_##MEM_ORDER);

#define GET_IMPL_OFFSET(TYPE, MEM_ORDER) \
  uintptr_t ptr = PTR(obj, offset); \
  CHECK_ADDR(TYPE, ptr) \
  return __c11_atomic_load((volatile_##TYPE *)ptr, __ATOMIC_##MEM_ORDER);

#define PUT_IMPL(TYPE, MEM_ORDER) \
  uintptr_t ptr = ((uintptr_t)address); \
  CHECK_ADDR(TYPE, ptr) \
  __c11_atomic_store((volatile_##TYPE *)ptr, newValue, __ATOMIC_##MEM_ORDER);

#define PUT_IMPL_OFFSET(TYPE, MEM_ORDER) \
  uintptr_t ptr = PTR(obj, offset); \
  CHECK_ADDR(TYPE, ptr) \
  __c11_atomic_store((volatile_##TYPE *)ptr, newValue, __ATOMIC_##MEM_ORDER);

#define GET_OBJECT_IMPL() \
  uintptr_t ptr = PTR(obj, offset); \
  CHECK_ADDR(id, ptr) \
  return *((id *)ptr);

#define GET_OBJECT_VOLATILE_IMPL() \
  uintptr_t ptr = PTR(obj, offset); \
  CHECK_ADDR(id, ptr) \
  return JreLoadVolatileId((volatile_id *)ptr);

#define PUT_OBJECT_IMPL() \
  uintptr_t ptr = PTR(obj, offset); \
  CHECK_ADDR(id, ptr) \
  JreStrongAssign((id *)ptr, newValue);

#define PUT_OBJECT_VOLATILE_IMPL() \
  uintptr_t ptr = PTR(obj, offset); \
  CHECK_ADDR(id, ptr) \
  JreVolatileStrongAssign((volatile_id *)ptr, newValue);


// Native method implementations.

/*
 * Method:    addressSize
 * Signature: ()I
 */
jint Java_sun_misc_Unsafe_addressSize(JNIEnv *env, jobject self) {
  return sizeof(void*);
}


/*
 * Method:    allocateInstance
 * Signature: (Ljava/lang/Class;)Ljava/lang/Object;
 */
jobject Java_sun_misc_Unsafe_allocateInstance(JNIEnv *env, jobject self, jclass c) {
  return (*env)->AllocObject(env, c);
}


/*
 * Method:    allocateMemory
 * Signature: (J)J
 */
jlong Java_sun_misc_Unsafe_allocateMemory(JNIEnv *env, jobject self, jlong bytes) {
  // bytes is nonnegative and fits into size_t
  if (bytes < 0 || bytes != (jlong)(size_t) bytes) {
    @throw create_JavaLangIllegalAccessException_initWithNSString_(@"wrong number of bytes");
    return 0;
  }
  void* mem = malloc((size_t)bytes);
  if (mem == NULL) {
    @throw create_JavaLangOutOfMemoryError_initWithNSString_(@"native alloc");
    return 0;
  }
  return (jlong)mem;
}


/*
 * Method:    compareAndSwapInt
 * Signature: (Ljava/lang/Object;JII)Z
 */
jboolean Java_sun_misc_Unsafe_compareAndSwapInt(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jint expectedValue, jint newValue) {
  uintptr_t ptr = PTR(obj, offset);
  CHECK_ADDR(jint, ptr)
  return __c11_atomic_compare_exchange_strong(
      (volatile_jint *)ptr, &expectedValue, newValue, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
}


/*
 * Method:    compareAndSwapLong
 * Signature: (Ljava/lang/Object;JJJ)Z
 */
jboolean Java_sun_misc_Unsafe_compareAndSwapLong(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jlong expectedValue, jlong newValue) {
  uintptr_t ptr = PTR(obj, offset);
  CHECK_ADDR(jlong, ptr)
  return __c11_atomic_compare_exchange_strong(
      (volatile_jlong *)ptr, &expectedValue, newValue, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
}


/*
 * Method:    compareAndSwapObject
 * Signature: (Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z
 */
jboolean Java_sun_misc_Unsafe_compareAndSwapObject(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jobject expectedValue, jobject newValue) {
  uintptr_t ptr = PTR(obj, offset);
  CHECK_ADDR(id, ptr)
  return JreCompareAndSwapVolatileStrongId((volatile_id *)ptr, expectedValue, newValue);
}


/*
 * Method:    freeMemory
 * Signature: (J)V
 */
void Java_sun_misc_Unsafe_freeMemory(JNIEnv *env, jobject self, jlong address) {
  free((void*)address);
}


/*
 * Method:    getArrayBaseOffsetForComponentType
 * Signature: (Ljava/lang/Class;)I
 */
jint Java_sun_misc_Unsafe_getArrayBaseOffsetForComponentType(
    JNIEnv *env, jclass cls, jclass component_class) {
  Class arrayCls = [component_class objcArrayClass];
  Ivar ivar = class_getInstanceVariable(arrayCls, "buffer_");
  if (!ivar) {
    @throw create_JavaLangAssertionError_initWithId_(@"buffer_ ivar not found.");
  }
  return (jint)ivar_getOffset(ivar);
}


/*
 * Method:    getArrayIndexScaleForComponentType
 * Signature: (Ljava/lang/Class;)I
 */
jint Java_sun_misc_Unsafe_getArrayIndexScaleForComponentType(
    JNIEnv *env, jclass cls, jclass component_class) {
  return (jint)[component_class getSizeof];
}


/*
 * Method:    objectFieldOffset0
 * Signature: (Ljava/lang/reflect/Field;)J
 */
jlong Java_sun_misc_Unsafe_objectFieldOffset0(
    JNIEnv *env, jclass cls, JavaLangReflectField *field) {
  return [field unsafeOffset];
}


/*
 * Method:    pageSize
 * Signature: ()I
 */
jint Java_sun_misc_Unsafe_pageSize(JNIEnv *env, jobject self) {
  return (jint)sysconf(_SC_PAGESIZE);
}


/*
 * Method:    setMemory
 * Signature: (JJB)V
 */
void Java_sun_misc_Unsafe_setMemory(
    JNIEnv *env, jobject self, jlong address, jlong bytes, jbyte value) {
  memset((void*)address, value, (size_t)bytes);
}


/*
 * Method:    copyMemory
 * Signature: (JJJ)V
 */
void Java_sun_misc_Unsafe_copyMemory(
    JNIEnv *env, jobject self, jobject srcBase, jlong srcOffset, jobject destBase, jlong destOffset,
    jlong size) {
  if (size == 0) {
    return;
  }
  // size is nonnegative and fits into size_t
  if (size < 0 || size != (jlong)(size_t) size) {
    create_JavaLangIllegalAccessException_initWithNSString_(@"wrong number of bytes");
  }
  size_t sz = (size_t)size;
  memcpy((void *)PTR(destBase, destOffset), (void *)PTR(srcBase, srcOffset), sz);
}

/*
 * Method:    loadFence
 * Signature: ()V
 */
void Java_sun_misc_Unsafe_loadFence(JNIEnv *env, jobject self) {
  __c11_atomic_thread_fence(__ATOMIC_ACQUIRE);
}

/*
 * Method:    storeFence
 * Signature: ()V
 */
void Java_sun_misc_Unsafe_storeFence(JNIEnv *env, jobject self) {
  __c11_atomic_thread_fence(__ATOMIC_RELEASE);
}

/*
 * Method:    fullFence
 * Signature: ()V
 */
void Java_sun_misc_Unsafe_fullFence(JNIEnv *env, jobject self) {
  __c11_atomic_thread_fence(__ATOMIC_SEQ_CST);
}


// Field accessor methods.


/*
 * Method:    getBoolean
 * Signature: (Ljava/lang/Object;J)Z
 */
jboolean Java_sun_misc_Unsafe_getBoolean
(JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jboolean, RELAXED)
}

/*
 * Method:    getBooleanVolatile
 * Signature: (Ljava/lang/Object;J)Z
 */
jboolean Java_sun_misc_Unsafe_getBooleanVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jboolean, SEQ_CST)
}


/*
 * Method:    putBoolean
 * Signature: (Ljava/lang/Object;JZ)V
 */
void Java_sun_misc_Unsafe_putBoolean(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jboolean newValue) {
  PUT_IMPL_OFFSET(jboolean, RELAXED)
}

/*
 * Method:    putBooleanVolatile
 * Signature: (Ljava/lang/Object;JZ)V
 */
void Java_sun_misc_Unsafe_putBooleanVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jboolean newValue) {
  PUT_IMPL_OFFSET(jboolean, SEQ_CST)
}


/*
 * Method:    getByte
 * Signature: (J)B
 */
jbyte Java_sun_misc_Unsafe_getByte__J(JNIEnv *env, jobject self, jlong address) {
  GET_IMPL(jbyte, RELAXED)
}

/*
 * Method:    getByte
 * Signature: (Ljava/lang/Object;J)B
 */
jbyte Java_sun_misc_Unsafe_getByte__Ljava_lang_Object_2J(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jbyte, RELAXED)
}

/*
 * Method:    getByteVolatile
 * Signature: (Ljava/lang/Object;J)B
 */
jbyte Java_sun_misc_Unsafe_getByteVolatile(JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jbyte, SEQ_CST)
}

/*
 * Method:    putByte
 * Signature: (JB)V
 */
void Java_sun_misc_Unsafe_putByte__JB(JNIEnv *env, jobject self, jlong address, jbyte newValue) {
  PUT_IMPL(jbyte, RELAXED)
}

/*
 * Method:    putByte
 * Signature: (Ljava/lang/Object;JB)V
 */
void Java_sun_misc_Unsafe_putByte__Ljava_lang_Object_2JB(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jbyte newValue) {
  PUT_IMPL_OFFSET(jbyte, RELAXED)
}

/*
 * Method:    putByteVolatile
 * Signature: (Ljava/lang/Object;JB)V
 */
void Java_sun_misc_Unsafe_putByteVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jbyte newValue) {
  PUT_IMPL_OFFSET(jbyte, SEQ_CST)
}


/*
 * Method:    getChar
 * Signature: (J)C
 */
jchar Java_sun_misc_Unsafe_getChar__J(JNIEnv *env, jobject self, jlong address) {
  GET_IMPL(jchar, RELAXED)
}

/*
 * Method:    getChar
 * Signature: (Ljava/lang/Object;J)C
 */
jchar Java_sun_misc_Unsafe_getChar__Ljava_lang_Object_2J(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jchar, RELAXED)
}

/*
 * Method:    getCharVolatile
 * Signature: (Ljava/lang/Object;J)C
 */
jchar Java_sun_misc_Unsafe_getCharVolatile(JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jchar, SEQ_CST)
}

/*
 * Method:    putChar
 * Signature: (JC)V
 */
void Java_sun_misc_Unsafe_putChar__JC(JNIEnv *env, jobject self, jlong address, jchar newValue) {
  PUT_IMPL(jchar, RELAXED)
}

/*
 * Method:    putChar
 * Signature: (Ljava/lang/Object;JC)V
 */
void Java_sun_misc_Unsafe_putChar__Ljava_lang_Object_2JC(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jchar newValue) {
  PUT_IMPL_OFFSET(jchar, RELAXED)
}

/*
 * Method:    putCharVolatile
 * Signature: (Ljava/lang/Object;JC)V
 */
void Java_sun_misc_Unsafe_putCharVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jchar newValue) {
  PUT_IMPL_OFFSET(jchar, SEQ_CST)
}


/*
 * Method:    getDouble
 * Signature: (J)D
 */
jdouble Java_sun_misc_Unsafe_getDouble__J(JNIEnv *env, jobject self, jlong address) {
  GET_IMPL(jdouble, RELAXED)
}

/*
 * Method:    getDouble
 * Signature: (Ljava/lang/Object;J)D
 */
jdouble Java_sun_misc_Unsafe_getDouble__Ljava_lang_Object_2J(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jdouble, RELAXED)
}

/*
 * Method:    getDoubleVolatile
 * Signature: (Ljava/lang/Object;J)D
 */
jdouble Java_sun_misc_Unsafe_getDoubleVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jdouble, SEQ_CST)
}

/*
 * Method:    putDouble
 * Signature: (JD)V
 */
void Java_sun_misc_Unsafe_putDouble__JD(
    JNIEnv *env, jobject self, jlong address, jdouble newValue) {
  PUT_IMPL(jdouble, RELAXED)
}

/*
 * Method:    putDouble
 * Signature: (Ljava/lang/Object;JD)V
 */
void Java_sun_misc_Unsafe_putDouble__Ljava_lang_Object_2JD(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jdouble newValue) {
  PUT_IMPL_OFFSET(jdouble, RELAXED)
}

/*
 * Method:    putDoubleVolatile
 * Signature: (Ljava/lang/Object;JD)V
 */
void Java_sun_misc_Unsafe_putDoubleVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jdouble newValue) {
  PUT_IMPL_OFFSET(jdouble, SEQ_CST)
}


/*
 * Method:    getFloat
 * Signature: (J)F
 */
jfloat Java_sun_misc_Unsafe_getFloat__J(JNIEnv *env, jobject self, jlong address) {
  GET_IMPL(jfloat, RELAXED)
}

/*
 * Method:    getFloat
 * Signature: (Ljava/lang/Object;J)F
 */
jfloat Java_sun_misc_Unsafe_getFloat__Ljava_lang_Object_2J(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jfloat, RELAXED)
}

/*
 * Method:    getFloatVolatile
 * Signature: (Ljava/lang/Object;J)F
 */
jfloat Java_sun_misc_Unsafe_getFloatVolatile(JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jfloat, SEQ_CST)
}

/*
 * Method:    putFloat
 * Signature: (JF)V
 */
void Java_sun_misc_Unsafe_putFloat__JF(JNIEnv *env, jobject self, jlong address, jfloat newValue) {
  PUT_IMPL(jfloat, RELAXED)
}

/*
 * Method:    putFloat
 * Signature: (Ljava/lang/Object;JF)V
 */
void Java_sun_misc_Unsafe_putFloat__Ljava_lang_Object_2JF(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jfloat newValue) {
  PUT_IMPL_OFFSET(jfloat, RELAXED)
}

/*
 * Method:    putFloatVolatile
 * Signature: (Ljava/lang/Object;JF)V
 */
void Java_sun_misc_Unsafe_putFloatVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jfloat newValue) {
  PUT_IMPL_OFFSET(jfloat, SEQ_CST)
}


/*
 * Method:    getInt
 * Signature: (J)I
 */
jint Java_sun_misc_Unsafe_getInt__J(JNIEnv *env, jobject self, jlong address) {
  GET_IMPL(jint, RELAXED)
}

/*
 * Method:    getInt
 * Signature: (Ljava/lang/Object;J)I
 */
jint Java_sun_misc_Unsafe_getInt__Ljava_lang_Object_2J(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jint, RELAXED)
}

/*
 * Method:    getIntVolatile
 * Signature: (Ljava/lang/Object;J)I
 */
jint Java_sun_misc_Unsafe_getIntVolatile(JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jint, SEQ_CST)
}

/*
 * Method:    putInt
 * Signature: (JI)V
 */
void Java_sun_misc_Unsafe_putInt__JI(JNIEnv *env, jobject self, jlong address, jint newValue) {
  PUT_IMPL(jint, RELAXED)
}

/*
 * Method:    putInt
 * Signature: (Ljava/lang/Object;JI)V
 */
void Java_sun_misc_Unsafe_putInt__Ljava_lang_Object_2JI(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jint newValue) {
  PUT_IMPL_OFFSET(jint, RELAXED)
}

/*
 * Method:    putIntVolatile
 * Signature: (Ljava/lang/Object;JI)V
 */
void Java_sun_misc_Unsafe_putIntVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jint newValue) {
  PUT_IMPL_OFFSET(jint, SEQ_CST)
}

/*
 * Method:    putOrderedInt
 * Signature: (Ljava/lang/Object;JI)V
 */
void Java_sun_misc_Unsafe_putOrderedInt(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jint newValue) {
  PUT_IMPL_OFFSET(jint, RELEASE)
}


/*
 * Method:    getLong
 * Signature: (J)J
 */
jlong Java_sun_misc_Unsafe_getLong__J(JNIEnv *env, jobject self, jlong address) {
  GET_IMPL(jlong, RELAXED)
}

/*
 * Method:    getLong
 * Signature: (Ljava/lang/Object;J)J
 */
jlong Java_sun_misc_Unsafe_getLong__Ljava_lang_Object_2J(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jlong, RELAXED)
}

/*
 * Method:    getLongVolatile
 * Signature: (Ljava/lang/Object;J)J
 */
jlong Java_sun_misc_Unsafe_getLongVolatile(JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jlong, SEQ_CST)
}

/*
 * Method:    putLong
 * Signature: (JJ)V
 */
void Java_sun_misc_Unsafe_putLong__JJ(JNIEnv *env, jobject self, jlong address, jlong newValue) {
  PUT_IMPL(jlong, RELAXED)
}

/*
 * Method:    putLong
 * Signature: (Ljava/lang/Object;JJ)V
 */
void Java_sun_misc_Unsafe_putLong__Ljava_lang_Object_2JJ(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jlong newValue) {
  PUT_IMPL_OFFSET(jlong, RELAXED)
}

/*
 * Method:    putOrderedLong
 * Signature: (Ljava/lang/Object;JJ)V
 */
void Java_sun_misc_Unsafe_putOrderedLong(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jlong newValue) {
  PUT_IMPL_OFFSET(jlong, RELEASE)
}

/*
 * Method:    putLongVolatile
 * Signature: (Ljava/lang/Object;JJ)V
 */
void Java_sun_misc_Unsafe_putLongVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jlong newValue) {
  PUT_IMPL_OFFSET(jlong, SEQ_CST)
}


/*
 * Method:    getObject
 * Signature: (Ljava/lang/Object;J)Ljava/lang/Object;
 */
jobject Java_sun_misc_Unsafe_getObject(JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_OBJECT_IMPL()
}

/*
 * Method:    getObjectVolatile
 * Signature: (Ljava/lang/Object;J)Ljava/lang/Object;
 */
jobject Java_sun_misc_Unsafe_getObjectVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_OBJECT_VOLATILE_IMPL()
}

/*
 * Method:    putObject
 * Signature: (Ljava/lang/Object;JLjava/lang/Object;)V
 */
void Java_sun_misc_Unsafe_putObject(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jobject newValue) {
  PUT_OBJECT_IMPL()
}

/*
 * Method:    putOrderedObject
 * Signature: (Ljava/lang/Object;JLjava/lang/Object;)V
 */
void Java_sun_misc_Unsafe_putOrderedObject(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jobject newValue) {
  PUT_OBJECT_VOLATILE_IMPL()
}


/*
 * Method:    putObjectVolatile
 * Signature: (Ljava/lang/Object;JLjava/lang/Object;)V
 */
void Java_sun_misc_Unsafe_putObjectVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jobject newValue) {
  PUT_OBJECT_VOLATILE_IMPL()
}


/*
 * Method:    getShort
 * Signature: (J)S
 */
jshort Java_sun_misc_Unsafe_getShort__J(JNIEnv *env, jobject self, jlong address) {
  GET_IMPL(jshort, RELAXED)
}

/*
 * Method:    getShort
 * Signature: (Ljava/lang/Object;J)S
 */
jshort Java_sun_misc_Unsafe_getShort__Ljava_lang_Object_2J(
    JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jshort, RELAXED)
}

/*
 * Method:    getShortVolatile
 * Signature: (Ljava/lang/Object;J)S
 */
jshort Java_sun_misc_Unsafe_getShortVolatile(JNIEnv *env, jobject self, jobject obj, jlong offset) {
  GET_IMPL_OFFSET(jshort, SEQ_CST)
}


/*
 * Method:    putShort
 * Signature: (JS)V
 */
void Java_sun_misc_Unsafe_putShort__JS(JNIEnv *env, jobject self, jlong address, jshort newValue) {
  PUT_IMPL(jshort, RELAXED)
}

/*
 * Method:    putShort
 * Signature: (Ljava/lang/Object;JS)V
 */
void Java_sun_misc_Unsafe_putShort__Ljava_lang_Object_2JS(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jshort newValue) {
  PUT_IMPL_OFFSET(jshort, RELAXED)
}


/*
 * Method:    putShortVolatile
 * Signature: (Ljava/lang/Object;JS)V
 */
void Java_sun_misc_Unsafe_putShortVolatile(
    JNIEnv *env, jobject self, jobject obj, jlong offset, jshort newValue) {
  PUT_IMPL_OFFSET(jshort, SEQ_CST)
}


/*
 * Method:    copyMemoryToPrimitiveArray
 * Signature: (JLjava/lang/Object;JJ)V
 */
//void Java_sun_misc_Unsafe_copyMemoryToPrimitiveArray(
//    JNIEnv *env, jobject self, jlong, jobject, jlong, jlong);

/*
 * Method:    copyMemoryFromPrimitiveArray
 * Signature: (Ljava/lang/Object;JJJ)V
 */
//void Java_sun_misc_Unsafe_copyMemoryFromPrimitiveArray(
//    JNIEnv *env, jobject self, jobject, jlong, jlong, jlong);
