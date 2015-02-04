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

package sun.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/*-[
#include "java/lang/AssertionError.h"
#include "java/lang/reflect/Array.h"
#include "volatile.h"
#include <libkern/OSAtomic.h>
]-*/

/**
 * The package name notwithstanding, this class is the quasi-standard
 * way for Java code to gain access to and use functionality which,
 * when unsupervised, would allow one to break the pointer/type safety
 * of Java.
 */
public final class Unsafe {
    /** Traditional dalvik name. */
    private static final Unsafe THE_ONE = new Unsafe();
    /** Traditional RI name. */
    private static final Unsafe theUnsafe = THE_ONE;

    /**
     * This class is only privately instantiable.
     */
    private Unsafe() {}

    /**
     * Gets the unique instance of this class. This is only allowed in
     * very limited situations.
     */
    public static Unsafe getUnsafe() {
        return THE_ONE;
    }

    /**
     * Gets the raw byte offset from the start of an object's memory to
     * the memory used to store the indicated instance field.
     *
     * @param field non-null; the field in question, which must be an
     * instance field
     * @return the offset to the field
     */
    public long objectFieldOffset(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException(
                    "valid for instance fields only");
        }

        return objectFieldOffset0(field);
    }

    /**
     * Helper for {@link #objectFieldOffset}, which does all the work,
     * assuming the parameter is deemed valid.
     *
     * @param field non-null; the instance field
     * @return the offset to the field
     */
    private static native long objectFieldOffset0(Field field) /*-[
      return (long long) [field unsafeOffset];
    ]-*/;

    /**
     * Gets the offset from the start of an array object's memory to
     * the memory used to store its initial (zeroeth) element.
     *
     * @param clazz non-null; class in question; must be an array class
     * @return the offset to the initial element
     */
    public int arrayBaseOffset(Class clazz) {
        Class<?> component = clazz.getComponentType();
        if (component == null) {
            throw new IllegalArgumentException("Valid for array classes only: " + clazz);
        }
        return getArrayBaseOffsetForComponentType(component);
    }

    private static native int getArrayBaseOffsetForComponentType(Class component_class) /*-[
      Class arrayCls = [component_class objcArrayClass];
      Ivar ivar = class_getInstanceVariable(arrayCls, "buffer_");
      if (!ivar) {
        @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:
            @"buffer_ ivar not found."]);
      }
      return (jint)ivar_getOffset(ivar);
    ]-*/;

    /**
     * Gets the size of each element of the given array class.
     *
     * @param clazz non-null; class in question; must be an array class
     * @return &gt; 0; the size of each element of the array
     */
    public int arrayIndexScale(Class clazz) {
      Class<?> component = clazz.getComponentType();
      if (component == null) {
          throw new IllegalArgumentException("Valid for array classes only: " + clazz);
      }
      return getArrayIndexScaleForComponentType(component);
    }

    private static native int getArrayIndexScaleForComponentType(Class component_class) /*-[
      return (jint)[component_class getSizeof];
    ]-*/;

    /**
     * Performs a compare-and-set operation on an <code>int</code>
     * field within the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param expectedValue expected value of the field
     * @param newValue new value to store in the field if the contents are
     * as expected
     * @return <code>true</code> if the new value was in fact stored, and
     * <code>false</code> if not
     */
    public native boolean compareAndSwapInt(Object obj, long offset,
            int expectedValue, int newValue) /*-[
      volatile int *address = (volatile int *) (((u_int8_t *) obj) + offset);
      return OSAtomicCompareAndSwapIntBarrier(expectedValue, newValue, address);
    ]-*/;

    /**
     * Performs a compare-and-set operation on a <code>long</code>
     * field within the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param expectedValue expected value of the field
     * @param newValue new value to store in the field if the contents are
     * as expected
     * @return <code>true</code> if the new value was in fact stored, and
     * <code>false</code> if not
     */
    public native boolean compareAndSwapLong(Object obj, long offset,
            long expectedValue, long newValue) /*-[
      volatile long long *address = (volatile long long *) (((u_int8_t *) obj) + offset);
      return OSAtomicCompareAndSwap64Barrier(expectedValue, newValue, address);
    ]-*/;

    /**
     * Performs a compare-and-set operation on an <code>Object</code>
     * field (that is, a reference field) within the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param expectedValue expected value of the field
     * @param newValue new value to store in the field if the contents are
     * as expected
     * @return <code>true</code> if the new value was in fact stored, and
     * <code>false</code> if not
     */
    public native boolean compareAndSwapObject(Object obj, long offset,
            Object expectedValue, Object newValue) /*-[
      id volatile *address = (id volatile *) (((u_int8_t *) obj) + offset);
      id tmp = *address;
      if (OSAtomicCompareAndSwapPtrBarrier(expectedValue, newValue, (void * volatile *) address)) {
        [*address retain];
        [tmp autorelease];
        return YES;
      }
      return NO;
    ]-*/;

    /**
     * Gets an <code>int</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native int getIntVolatile(Object obj, long offset) /*-[
      volatile_int *address = (volatile_int *) (((u_int8_t *) obj) + offset);
      return volatile_load(address);
    ]-*/;

    /**
     * Stores an <code>int</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putIntVolatile(Object obj, long offset, int newValue) /*-[
      volatile_int *address = (volatile_int *) (((u_int8_t *) obj) + offset);
      volatile_store(address, newValue);
    ]-*/;


    /**
     * Gets a <code>long</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native long getLongVolatile(Object obj, long offset) /*-[
      volatile_long *address = (volatile_long *) (((u_int8_t *) obj) + offset);
      return volatile_load(address);
    ]-*/;

    /**
     * Stores a <code>long</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putLongVolatile(Object obj, long offset, long newValue) /*-[
      volatile_long *address = (volatile_long *) (((u_int8_t *) obj) + offset);
      volatile_store(address, newValue);
    ]-*/;

    /**
     * Gets an <code>Object</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native Object getObjectVolatile(Object obj, long offset) /*-[
      id volatile *address = (id volatile *) (((u_int8_t *) obj) + offset);
      return *address;
    ]-*/;

    /**
     * Stores an <code>Object</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putObjectVolatile(Object obj, long offset,
            Object newValue) /*-[
      id *address = (id *) (((u_int8_t *) obj) + offset);
      id oldValue = nil;
      [newValue retain];
      do {
        oldValue = *address;
      } while (!OSAtomicCompareAndSwapPtrBarrier(oldValue, newValue, (void * volatile *)address));
      [oldValue autorelease];
    ]-*/;

    /**
     * Gets an <code>int</code> field from the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native int getInt(Object obj, long offset) /*-[
      int *address = (int *) (((u_int8_t *) obj) + offset);
      return *address;
    ]-*/;

    /**
     * Stores an <code>int</code> field into the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putInt(Object obj, long offset, int newValue) /*-[
      int *address = (int *) (((u_int8_t *) obj) + offset);
      *address = newValue;
    ]-*/;

    /**
     * Lazy set an int field.
     */
    public native void putOrderedInt(Object obj, long offset, int newValue) /*-[
      int *address = (int *) (((u_int8_t *) obj) + offset);
      OSMemoryBarrier();
      *address = newValue;
    ]-*/;

    /**
     * Gets a <code>long</code> field from the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native long getLong(Object obj, long offset) /*-[
      long long *address = (long long *) (((u_int8_t *) obj) + offset);
      return *address;
    ]-*/;

    /**
     * Stores a <code>long</code> field into the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putLong(Object obj, long offset, long newValue) /*-[
      long long *address = (long long *) (((u_int8_t *) obj) + offset);
      *address = newValue;
    ]-*/;

    /**
     * Lazy set a long field.
     */
    public native void putOrderedLong(Object obj, long offset, long newValue) /*-[
      long long *address = (long long *) (((u_int8_t *) obj) + offset);
      OSMemoryBarrier();
      *address = newValue;
    ]-*/;

    /**
     * Gets an <code>Object</code> field from the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native Object getObject(Object obj, long offset) /*-[
      id *address = (id *) (((u_int8_t *) obj) + offset);
      return *address;
    ]-*/;

    /**
     * Stores an <code>Object</code> field into the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putObject(Object obj, long offset, Object newValue) /*-[
      id *address = (id *) (((u_int8_t *) obj) + offset);
      id oldValue = nil;
      [newValue retain];
      do {
        oldValue = *address;
      } while (!OSAtomicCompareAndSwapPtr(oldValue, newValue, (void * volatile *)address));
      [oldValue autorelease];
    ]-*/;

    /**
     * Lazy set an object field.
     */
    public native void putOrderedObject(Object obj, long offset, Object newValue) /*-[
      id *address = (id *) (((u_int8_t *) obj) + offset);
      id oldValue = nil;
      [newValue retain];
      do {
        oldValue = *address;
      } while (!OSAtomicCompareAndSwapPtrBarrier(oldValue, newValue, (void * volatile *)address));
      [oldValue autorelease];
    ]-*/;

    /**
     * Parks the calling thread for the specified amount of time,
     * unless the "permit" for the thread is already available (due to
     * a previous call to {@link #unpark}. This method may also return
     * spuriously (that is, without the thread being told to unpark
     * and without the indicated amount of time elapsing).
     *
     * <p>See {@link java.util.concurrent.locks.LockSupport} for more
     * in-depth information of the behavior of this method.</p>
     *
     * @param absolute whether the given time value is absolute
     * milliseconds-since-the-epoch (<code>true</code>) or relative
     * nanoseconds-from-now (<code>false</code>)
     * @param time the (absolute millis or relative nanos) time value
     */
    public void park(boolean absolute, long time) {
        if (absolute) {
            Thread.currentThread().parkUntil(time);
        } else {
            Thread.currentThread().parkFor(time);
        }
    }

    /**
     * Unparks the given object, which must be a {@link Thread}.
     *
     * <p>See {@link java.util.concurrent.locks.LockSupport} for more
     * in-depth information of the behavior of this method.</p>
     *
     * @param obj non-null; the object to unpark
     */
    public void unpark(Object obj) {
        if (obj instanceof Thread) {
            ((Thread) obj).unpark();
        } else {
            throw new IllegalArgumentException("valid for Threads only");
        }
    }

    /**
     * Allocates an instance of the given class without running the constructor.
     * The class' <clinit> will be run, if necessary.
     */
    public native Object allocateInstance(Class<?> c) /*-[
      return [c.objcClass alloc];
    ]-*/;

    // iOS-specific methods, necessary because an array object's element
    // buffer isn't inlined as the JVM's is. That makes calculating an
    // offset from the array address impossible.

    /**
     * Gets an indexed element from the given array.
     *
     * @param array non-null
     * @param index element index in <code>array</code>
     * @return the retrieved value
     */
    public native Object getArrayObject(Object array, int index) /*-[
      return ((IOSObjectArray *) array)->buffer_[index];
    ]-*/;

    /**
     * Gets an indexed element from the given array,
     * using <code>volatile</code> semantics.
     *
     * @param array non-null
     * @param index element index in <code>array</code>
     * @return the retrieved value
     */
    public native Object getArrayObjectVolatile(Object array, int index) /*-[
      id volatile result = ((IOSObjectArray *) array)->buffer_[index];
      return result;
    ]-*/;

    /**
     * Stores an <code>Object</code> into the given array.
     *
     * @param array non-null
     * @param index element index in <code>array</code>
     * @param newValue the value to store
     */
    public native void putArrayObject(Object array, int index, Object newValue) /*-[
      id *address = &((IOSObjectArray *) array)->buffer_[index];
      id oldValue = nil;
      [newValue retain];
      do {
        oldValue = *address;
      } while (!OSAtomicCompareAndSwapPtr(oldValue, newValue, (void * volatile *)address));
      [oldValue autorelease];
    ]-*/;

    /**
     * Stores an <code>Object</code> into the given array.
     *
     * @param array non-null
     * @param index element index in <code>array</code>
     * @param newValue the value to store
     */
    public native void putArrayOrderedObject(Object array, int index, Object newValue) /*-[
      id *address = &((IOSObjectArray *) array)->buffer_[index];
      id oldValue = nil;
      [newValue retain];
      do {
        oldValue = *address;
      } while (!OSAtomicCompareAndSwapPtrBarrier(oldValue, newValue, (void * volatile *)address));
      [oldValue autorelease];
    ]-*/;

    /**
     * Stores an <code>Object</code> into the given array.
     *
     * @param array non-null
     * @param index element index in <code>array</code>
     * @param newValue the value to store
     */
    public native void putArrayObjectVolatile(Object array, int index, Object newValue) /*-[
      id *address = &((IOSObjectArray *) array)->buffer_[index];
      id oldValue = nil;
      [newValue retain];
      do {
        oldValue = *address;
      } while (!OSAtomicCompareAndSwapPtrBarrier(oldValue, newValue, (void * volatile *)address));
      [oldValue autorelease];
    ]-*/;

    /**
     * Performs a compare-and-set operation on an indexed element
     * within the given object.
     *
     * @param array non-null
     * @param index element index in <code>array</code>
     * @param expectedValue expected value of the field
     * @param newValue new value to store in the field if the contents are
     * as expected
     * @return <code>true</code> if the new value was in fact stored, and
     * <code>false</code> if not
     */
    public native boolean compareAndSwapArrayObject(Object array, int index,
            Object expectedValue, Object newValue) /*-[
      id volatile *buffer = ((IOSObjectArray *) array)->buffer_;
      id tmp = buffer[index];
      if (OSAtomicCompareAndSwapPtrBarrier(expectedValue, newValue,
          (void * volatile *) (buffer + index))) {
        [buffer[index] retain];
        [tmp autorelease];
        return YES;
      }
      return NO;
    ]-*/;
}
