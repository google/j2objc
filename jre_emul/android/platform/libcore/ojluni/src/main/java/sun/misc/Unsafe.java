/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A collection of methods for performing low-level, unsafe operations.
 * Although the class and all methods are public, use of this class is
 * limited because only trusted code can obtain instances of it.
 *
 * @author John R. Rose
 * @see #getUnsafe
 */
public final class Unsafe {
    /** Traditional dalvik name. */
    private static final Unsafe THE_ONE = new Unsafe();

    private static final Unsafe theUnsafe = THE_ONE;
    public static final int INVALID_FIELD_OFFSET   = -1;

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
            throw new IllegalArgumentException("valid for instance fields only");
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
    private static native long objectFieldOffset0(Field field);

    public static long staticFieldOffset(Field f) {
        if (!Modifier.isStatic(f.getModifiers())) {
            throw new IllegalArgumentException("valid for static fields only");
        }
        return objectFieldOffset0(f);
    }

    public static Object staticFieldBase(Field f) {
      return null;
    }

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

    private static native int getArrayBaseOffsetForComponentType(Class component_class);
    private static native int getArrayIndexScaleForComponentType(Class component_class);

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
            int expectedValue, int newValue);

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
            long expectedValue, long newValue);

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
            Object expectedValue, Object newValue);
    /**
     * Gets a <code>boolean</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native boolean getBooleanVolatile(Object obj, long offset);

    /**
     * Stores a <code>boolean</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putBooleanVolatile(Object obj, long offset, boolean newValue);

    /**
     * Gets a <code>byte</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native byte getByteVolatile(Object obj, long offset);

    /**
     * Stores a <code>byte</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putByteVolatile(Object obj, long offset, byte newValue);

    /**
     * Gets a <code>char</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native char getCharVolatile(Object obj, long offset);

    /**
     * Stores a <code>char</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putCharVolatile(Object obj, long offset, char newValue);

    /**
     * Gets a <code>double</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native double getDoubleVolatile(Object obj, long offset);

    /**
     * Stores a <code>double</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putDoubleVolatile(Object obj, long offset, double newValue);

    /**
     * Gets a <code>float</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native float getFloatVolatile(Object obj, long offset);

    /**
     * Stores a <code>float</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putFloatVolatile(Object obj, long offset, float newValue);

    /**
     * Gets an <code>int</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native int getIntVolatile(Object obj, long offset);

    /**
     * Stores an <code>int</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putIntVolatile(Object obj, long offset, int newValue);

    /**
     * Gets a <code>long</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native long getLongVolatile(Object obj, long offset);

    /**
     * Stores a <code>long</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putLongVolatile(Object obj, long offset, long newValue);

    /**
     * Gets an <code>Object</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native Object getObjectVolatile(Object obj, long offset);

    /**
     * Stores an <code>Object</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putObjectVolatile(Object obj, long offset,
            Object newValue);

    /**
     * Gets a <code>short</code> field from the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native short getShortVolatile(Object obj, long offset);

    /**
     * Stores a <code>short</code> field into the given object,
     * using <code>volatile</code> semantics.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putShortVolatile(Object obj, long offset, short newValue);

    /**
     * Gets an <code>int</code> field from the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native int getInt(Object obj, long offset);

    /**
     * Stores an <code>int</code> field into the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putInt(Object obj, long offset, int newValue);

    /**
     * Lazy set an int field.
     */
    public native void putOrderedInt(Object obj, long offset, int newValue);

    /**
     * Gets a <code>long</code> field from the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native long getLong(Object obj, long offset);

    /**
     * Stores a <code>long</code> field into the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putLong(Object obj, long offset, long newValue);

    /**
     * Lazy set a long field.
     */
    public native void putOrderedLong(Object obj, long offset, long newValue);

    /**
     * Gets an <code>Object</code> field from the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @return the retrieved value
     */
    public native Object getObject(Object obj, long offset);

    /**
     * Stores an <code>Object</code> field into the given object.
     *
     * @param obj non-null; object containing the field
     * @param offset offset to the field within <code>obj</code>
     * @param newValue the value to store
     */
    public native void putObject(Object obj, long offset, Object newValue);

    /**
     * Lazy set an object field.
     */
    public native void putOrderedObject(Object obj, long offset,
            Object newValue);


    public native boolean getBoolean(Object obj, long offset);
    public native void putBoolean(Object obj, long offset, boolean newValue);
    public native byte getByte(Object obj, long offset);
    public native void putByte(Object obj, long offset, byte newValue);
    public native char getChar(Object obj, long offset);
    public native void putChar(Object obj, long offset, char newValue);
    public native short getShort(Object obj, long offset);
    public native void putShort(Object obj, long offset, short newValue);
    public native float getFloat(Object obj, long offset);
    public native void putFloat(Object obj, long offset, float newValue);
    public native double getDouble(Object obj, long offset);
    public native void putDouble(Object obj, long offset, double newValue);

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
            Thread.currentThread().parkUntil$(time);
        } else {
            Thread.currentThread().parkFor$(time);
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
            ((Thread) obj).unpark$();
        } else {
            throw new IllegalArgumentException("valid for Threads only");
        }
    }

    /**
     * Allocates an instance of the given class without running the constructor.
     * The class' <clinit> will be run, if necessary.
     */
    public native Object allocateInstance(Class<?> c);

    public native int addressSize();

    public native int pageSize();

    public native long allocateMemory(long bytes);

    public native void freeMemory(long address);

    public native void setMemory(long address, long bytes, byte value);

    public native byte getByte(long address);

    public native void putByte(long address, byte x);

    public native short getShort(long address);

    public native void putShort(long address, short x);

    public native char getChar(long address);

    public native void putChar(long address, char x);

    public native int getInt(long address);

    public native void putInt(long address, int x);

    public native long getLong(long address);

    public native void putLong(long address, long x);

    public native float getFloat(long address);

    public native void putFloat(long address, float x);

    public native double getDouble(long address);

    public native void putDouble(long address, double x);

// J2ObjC: implement if requested by app developers.
//    public native void copyMemoryToPrimitiveArray(long srcAddr,
//            Object dst, long dstOffset, long bytes);
//
//    public native void copyMemoryFromPrimitiveArray(Object src, long srcOffset,
//            long dstAddr, long bytes);

    public native void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset,
                                  long bytes);

    public void copyMemory(long srcAddr, long dstAddr, long bytes) {
      copyMemory(null, srcAddr, null, dstAddr, bytes);
    }


    // The following contain CAS-based Java implementations used on
    // platforms not supporting native instructions

    /**
     * Atomically adds the given value to the current value of a field
     * or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param delta the value to add
     * @return the previous value
     * @since 1.8
     */
    // @HotSpotIntrinsicCandidate
    public final int getAndAddInt(Object o, long offset, int delta) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, v + delta));
        return v;
    }

    /**
     * Atomically adds the given value to the current value of a field
     * or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param delta the value to add
     * @return the previous value
     * @since 1.8
     */
    // @HotSpotIntrinsicCandidate
    public final long getAndAddLong(Object o, long offset, long delta) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!compareAndSwapLong(o, offset, v, v + delta));
        return v;
    }

    /**
     * Atomically exchanges the given value with the current value of
     * a field or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param newValue new value
     * @return the previous value
     * @since 1.8
     */
    // @HotSpotIntrinsicCandidate
    public final int getAndSetInt(Object o, long offset, int newValue) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, newValue));
        return v;
    }

    /**
     * Atomically exchanges the given value with the current value of
     * a field or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param newValue new value
     * @return the previous value
     * @since 1.8
     */
    // @HotSpotIntrinsicCandidate
    public final long getAndSetLong(Object o, long offset, long newValue) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!compareAndSwapLong(o, offset, v, newValue));
        return v;
    }

    /**
     * Atomically exchanges the given reference value with the current
     * reference value of a field or array element within the given
     * object {@code o} at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param newValue new value
     * @return the previous value
     * @since 1.8
     */
    // @HotSpotIntrinsicCandidate
    public final Object getAndSetObject(Object o, long offset, Object newValue) {
        Object v;
        do {
            v = getObjectVolatile(o, offset);
        } while (!compareAndSwapObject(o, offset, v, newValue));
        return v;
    }


    /**
     * Ensures that loads before the fence will not be reordered with loads and
     * stores after the fence; a "LoadLoad plus LoadStore barrier".
     *
     * Corresponds to C11 atomic_thread_fence(memory_order_acquire)
     * (an "acquire fence").
     *
     * A pure LoadLoad fence is not provided, since the addition of LoadStore
     * is almost always desired, and most current hardware instructions that
     * provide a LoadLoad barrier also provide a LoadStore barrier for free.
     * @since 1.8
     */
    // @HotSpotIntrinsicCandidate
    public native void loadFence();

    /**
     * Ensures that loads and stores before the fence will not be reordered with
     * stores after the fence; a "StoreStore plus LoadStore barrier".
     *
     * Corresponds to C11 atomic_thread_fence(memory_order_release)
     * (a "release fence").
     *
     * A pure StoreStore fence is not provided, since the addition of LoadStore
     * is almost always desired, and most current hardware instructions that
     * provide a StoreStore barrier also provide a LoadStore barrier for free.
     * @since 1.8
     */
    // @HotSpotIntrinsicCandidate
    public native void storeFence();

    /**
     * Ensures that loads and stores before the fence will not be reordered
     * with loads and stores after the fence.  Implies the effects of both
     * loadFence() and storeFence(), and in addition, the effect of a StoreLoad
     * barrier.
     *
     * Corresponds to C11 atomic_thread_fence(memory_order_seq_cst).
     * @since 1.8
     */
    // @HotSpotIntrinsicCandidate
    public native void fullFence();
}
