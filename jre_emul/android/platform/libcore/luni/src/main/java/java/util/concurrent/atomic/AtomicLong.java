/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * A {@code long} value that may be updated atomically.  See the
 * {@link java.util.concurrent.atomic} package specification for
 * description of the properties of atomic variables. An
 * {@code AtomicLong} is used in applications such as atomically
 * incremented sequence numbers, and cannot be used as a replacement
 * for a {@link java.lang.Long}. However, this class does extend
 * {@code Number} to allow uniform access by tools and utilities that
 * deal with numerically-based classes.
 *
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLong extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 1927816293512124184L;

    /* J2ObjC removed.
    private static final sun.misc.Unsafe U = sun.misc.Unsafe.getUnsafe();
    private static final long VALUE;
    */

    /**
     * Records whether the underlying JVM supports lockless
     * compareAndSwap for longs. While the Unsafe.compareAndSwapLong
     * method works in either case, some constructions should be
     * handled at Java level to avoid locking user-visible locks.
     */
    static final boolean VM_SUPPORTS_LONG_CAS = VMSupportsCS8();

    /**
     * Returns whether underlying JVM supports lockless CompareAndSet
     * for longs. Called only once and cached in VM_SUPPORTS_LONG_CAS.
     */
    private static native boolean VMSupportsCS8() /*-[
      return sizeof(jlong) == sizeof(volatile_jlong);
    ]-*/;

    /* J2ObjC removed.
    static {
        try {
            VALUE = U.objectFieldOffset
                (AtomicLong.class.getDeclaredField("value"));
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }
    */

    private volatile long value;

    /**
     * Creates a new AtomicLong with the given initial value.
     *
     * @param initialValue the initial value
     */
    public AtomicLong(long initialValue) {
        value = initialValue;
    }

    /**
     * Creates a new AtomicLong with initial value {@code 0}.
     */
    public AtomicLong() {
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public final long get() {
        return value;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final native void set(long newValue) /*-[
      __c11_atomic_store(&self->value_, newValue, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Eventually sets to the given value.
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final native void lazySet(long newValue) /*-[
      __c11_atomic_store(&self->value_, newValue, __ATOMIC_RELEASE);
    ]-*/;

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final native long getAndSet(long newValue) /*-[
      return __c11_atomic_exchange(&self->value_, newValue, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final native boolean compareAndSet(long expect, long update) /*-[
      return __c11_atomic_compare_exchange_strong(
          &self->value_, &expect, update, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * <p><a href="package-summary.html#weakCompareAndSet">May fail
     * spuriously and does not provide ordering guarantees</a>, so is
     * only rarely an appropriate alternative to {@code compareAndSet}.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     */
    public final native boolean weakCompareAndSet(long expect, long update) /*-[
      return __c11_atomic_compare_exchange_weak(
          &self->value_, &expect, update, __ATOMIC_RELAXED, __ATOMIC_RELAXED);
    ]-*/;

    /**
     * Atomically increments by one the current value.
     *
     * @return the previous value
     */
    public final long getAndIncrement() {
      return getAndAdd(1);
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @return the previous value
     */
    public final long getAndDecrement() {
      return getAndAdd(-1);
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public final native long getAndAdd(long delta) /*-[
      return __c11_atomic_fetch_add(&self->value_, delta, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Atomically increments by one the current value.
     *
     * @return the updated value
     */
    public final long incrementAndGet() {
      return addAndGet(1);
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @return the updated value
     */
    public final long decrementAndGet() {
      return addAndGet(-1);
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public final native long addAndGet(long delta) /*-[
      return __c11_atomic_fetch_add(&self->value_, delta, __ATOMIC_SEQ_CST) + delta;
    ]-*/;

    /**
     * Atomically updates the current value with the results of
     * applying the given function, returning the previous value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.
     *
     * @param updateFunction a side-effect-free function
     * @return the previous value
     * @since 1.8
     */
    public final long getAndUpdate(LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    /**
     * Atomically updates the current value with the results of
     * applying the given function, returning the updated value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.
     *
     * @param updateFunction a side-effect-free function
     * @return the updated value
     * @since 1.8
     */
    public final long updateAndGet(LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    /**
     * Atomically updates the current value with the results of
     * applying the given function to the current and given values,
     * returning the previous value. The function should be
     * side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function
     * is applied with the current value as its first argument,
     * and the given update as the second argument.
     *
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the previous value
     * @since 1.8
     */
    public final long getAndAccumulate(long x,
                                       LongBinaryOperator accumulatorFunction) {
        long prev, next;
        do {
            prev = get();
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    /**
     * Atomically updates the current value with the results of
     * applying the given function to the current and given values,
     * returning the updated value. The function should be
     * side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function
     * is applied with the current value as its first argument,
     * and the given update as the second argument.
     *
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the updated value
     * @since 1.8
     */
    public final long accumulateAndGet(long x,
                                       LongBinaryOperator accumulatorFunction) {
        long prev, next;
        do {
            prev = get();
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value
     */
    public String toString() {
        return Long.toString(get());
    }

    /**
     * Returns the value of this {@code AtomicLong} as an {@code int}
     * after a narrowing primitive conversion.
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public int intValue() {
        return (int)get();
    }

    /**
     * Returns the value of this {@code AtomicLong} as a {@code long}.
     * Equivalent to {@link #get()}.
     */
    public long longValue() {
        return get();
    }

    /**
     * Returns the value of this {@code AtomicLong} as a {@code float}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * Returns the value of this {@code AtomicLong} as a {@code double}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)get();
    }

    /*
     * These ObjC methods are needed to support subclassing of NSNumber.
     * objCType is used by descriptionWithLocale:.
     * getValue: is used by copyWithZone:.
     */
    /*-[
    - (const char *)objCType {
      return "q";
    }

    - (void)getValue:(void *)buffer {
      *((long long int *) buffer) = value_;
    }
    ]-*/

}
