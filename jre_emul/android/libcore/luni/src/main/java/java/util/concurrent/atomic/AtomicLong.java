/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

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
    public final void set(long newValue) {
        value = newValue;
    }

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
     * @return true if successful. False return indicates that
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
     * @return true if successful
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
     * Returns the String representation of the current value.
     * @return the String representation of the current value
     */
    public String toString() {
        return Long.toString(get());
    }

    /**
     * Returns the value of this {@code AtomicLong} as an {@code int}
     * after a narrowing primitive conversion.
     */
    public int intValue() {
        return (int)get();
    }

    /**
     * Returns the value of this {@code AtomicLong} as a {@code long}.
     */
    public long longValue() {
        return get();
    }

    /**
     * Returns the value of this {@code AtomicLong} as a {@code float}
     * after a widening primitive conversion.
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * Returns the value of this {@code AtomicLong} as a {@code double}
     * after a widening primitive conversion.
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
