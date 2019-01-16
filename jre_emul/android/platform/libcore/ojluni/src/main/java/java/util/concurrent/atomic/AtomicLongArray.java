/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

/*-[
#include "java/lang/IndexOutOfBoundsException.h"
]-*/

import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * A {@code long} array in which elements may be updated atomically.
 * See the {@link java.util.concurrent.atomic} package specification
 * for description of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLongArray implements java.io.Serializable {
    private static final long serialVersionUID = -2308431214976778248L;

    private final long[] array;
    /* J2ObjC removed.
    private static final sun.misc.Unsafe U = sun.misc.Unsafe.getUnsafe();
    private static final int ABASE;
    private static final int ASHIFT;

    static {
        ABASE = U.arrayBaseOffset(long[].class);
        int scale = U.arrayIndexScale(long[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("array index scale not a power of two");
        ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);

        return byteOffset(i);
    }

    private static long byteOffset(int i) {
        return ((long) i << ASHIFT) + ABASE;
    }
    */

    /**
     * Creates a new AtomicLongArray of the given length, with all
     * elements initially zero.
     *
     * @param length the length of the array
     */
    public AtomicLongArray(int length) {
        array = new long[length];
    }

    /**
     * Creates a new AtomicLongArray with the same length as, and
     * all elements copied from, the given array.
     *
     * @param array the array to copy elements from
     * @throws NullPointerException if array is null
     */
    public AtomicLongArray(long[] array) {
        // Visibility guaranteed by final field guarantees
        this.array = array.clone();
    }

    /**
     * Returns the length of the array.
     *
     * @return the length of the array
     */
    public final int length() {
        return array.length;
    }

    /*-[
    static void CheckIdx(JavaUtilConcurrentAtomicAtomicLongArray *self, jint i) {
      if (i < 0 || i >= self->array_->size_) {
        @throw create_JavaLangIndexOutOfBoundsException_initWithNSString_(
            JreStrcat("$I", @"index ", i));
      }
    }

    static inline volatile_jlong *GetPtrUnchecked(
        JavaUtilConcurrentAtomicAtomicLongArray *self, jint i) {
      return (volatile_jlong *)&self->array_->buffer_[i];
    }

    static inline volatile_jlong *GetPtrChecked(
        JavaUtilConcurrentAtomicAtomicLongArray *self, jint i) {
      CheckIdx(self, i);
      return (volatile_jlong *)&self->array_->buffer_[i];
    }
    ]-*/

    /**
     * Gets the current value at position {@code i}.
     *
     * @param i the index
     * @return the current value
     */
    public final native long get(int i) /*-[
      return __c11_atomic_load(GetPtrChecked(self, i), __ATOMIC_SEQ_CST);
    ]-*/;

    private final native long getUnchecked(int i) /*-[
      return __c11_atomic_load(GetPtrUnchecked(self, i), __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Sets the element at position {@code i} to the given value.
     *
     * @param i the index
     * @param newValue the new value
     */
    public final native void set(int i, long newValue) /*-[
      __c11_atomic_store(GetPtrChecked(self, i), newValue, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Eventually sets the element at position {@code i} to the given value.
     *
     * @param i the index
     * @param newValue the new value
     * @since 1.6
     */
    public final native void lazySet(int i, long newValue) /*-[
      __c11_atomic_store(GetPtrChecked(self, i), newValue, __ATOMIC_RELEASE);
    ]-*/;

    /**
     * Atomically sets the element at position {@code i} to the given value
     * and returns the old value.
     *
     * @param i the index
     * @param newValue the new value
     * @return the previous value
     */
    public final native long getAndSet(int i, long newValue) /*-[
      return __c11_atomic_exchange(GetPtrChecked(self, i), newValue, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Atomically sets the element at position {@code i} to the given
     * updated value if the current value {@code ==} the expected value.
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final native boolean compareAndSet(int i, long expect, long update) /*-[
      return __c11_atomic_compare_exchange_strong(
          GetPtrChecked(self, i), &expect, update, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Atomically sets the element at position {@code i} to the given
     * updated value if the current value {@code ==} the expected value.
     *
     * <p><a href="package-summary.html#weakCompareAndSet">May fail
     * spuriously and does not provide ordering guarantees</a>, so is
     * only rarely an appropriate alternative to {@code compareAndSet}.
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     */
    public final native boolean weakCompareAndSet(int i, long expect, long update) /*-[
      return __c11_atomic_compare_exchange_weak(
          GetPtrChecked(self, i), &expect, update, __ATOMIC_RELAXED, __ATOMIC_RELAXED);
    ]-*/;

    /**
     * Atomically increments by one the element at index {@code i}.
     *
     * @param i the index
     * @return the previous value
     */
    public final long getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    /**
     * Atomically decrements by one the element at index {@code i}.
     *
     * @param i the index
     * @return the previous value
     */
    public final long getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    /**
     * Atomically adds the given value to the element at index {@code i}.
     *
     * @param i the index
     * @param delta the value to add
     * @return the previous value
     */
    public final native long getAndAdd(int i, long delta) /*-[
      return __c11_atomic_fetch_add(GetPtrChecked(self, i), delta, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Atomically increments by one the element at index {@code i}.
     *
     * @param i the index
     * @return the updated value
     */
    public final long incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    /**
     * Atomically decrements by one the element at index {@code i}.
     *
     * @param i the index
     * @return the updated value
     */
    public final long decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }

    /**
     * Atomically adds the given value to the element at index {@code i}.
     *
     * @param i the index
     * @param delta the value to add
     * @return the updated value
     */
    public long addAndGet(int i, long delta) {
        return getAndAdd(i, delta) + delta;
    }

    /**
     * Atomically updates the element at index {@code i} with the results
     * of applying the given function, returning the previous value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.
     *
     * @param i the index
     * @param updateFunction a side-effect-free function
     * @return the previous value
     * @since 1.8
     */
    public final long getAndUpdate(int i, LongUnaryOperator updateFunction) {
        // long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = get(i);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(i, prev, next));
        return prev;
    }

    /**
     * Atomically updates the element at index {@code i} with the results
     * of applying the given function, returning the updated value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.
     *
     * @param i the index
     * @param updateFunction a side-effect-free function
     * @return the updated value
     * @since 1.8
     */
    public final long updateAndGet(int i, LongUnaryOperator updateFunction) {
        // long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = get(i);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(i, prev, next));
        return next;
    }

    /**
     * Atomically updates the element at index {@code i} with the
     * results of applying the given function to the current and
     * given values, returning the previous value. The function should
     * be side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function is
     * applied with the current value at index {@code i} as its first
     * argument, and the given update as the second argument.
     *
     * @param i the index
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the previous value
     * @since 1.8
     */
    public final long getAndAccumulate(int i, long x,
                                      LongBinaryOperator accumulatorFunction) {
        // long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = get(i);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(i, prev, next));
        return prev;
    }

    /**
     * Atomically updates the element at index {@code i} with the
     * results of applying the given function to the current and
     * given values, returning the updated value. The function should
     * be side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function is
     * applied with the current value at index {@code i} as its first
     * argument, and the given update as the second argument.
     *
     * @param i the index
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the updated value
     * @since 1.8
     */
    public final long accumulateAndGet(int i, long x,
                                      LongBinaryOperator accumulatorFunction) {
        // long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = get(i);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(i, prev, next));
        return next;
    }

    /**
     * Returns the String representation of the current values of array.
     * @return the String representation of the current values of array
     */
    public String toString() {
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(getUnchecked(i));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }

}
