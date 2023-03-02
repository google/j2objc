/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

/* J2ObjC removed
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
*/

import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * A {@code long} value that may be updated atomically.  See the
 * {@link VarHandle} specification for descriptions of the properties
 * of atomic accesses. An {@code AtomicLong} is used in applications
 * such as atomically incremented sequence numbers, and cannot be used
 * as a replacement for a {@link java.lang.Long}. However, this class
 * does extend {@code Number} to allow uniform access by tools and
 * utilities that deal with numerically-based classes.
 *
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLong extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 1927816293512124184L;

    /**
     * Records whether the underlying JVM supports lockless
     * compareAndSet for longs. While the intrinsic compareAndSetLong
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

    /*
     * This class intended to be implemented using VarHandles, but there
     * are unresolved cyclic startup dependencies.
     */
    // BEGIN Android-changed: Using VarHandle instead of Unsafe
    // private static final jdk.internal.misc.Unsafe U = jdk.internal.misc.Unsafe.getUnsafe();
    // private static final long VALUE = U.objectFieldOffset(AtomicLong.class, "value");
    /* J2ObjC removed.
    private static final VarHandle VALUE;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            VALUE = l.findVarHandle(AtomicLong.class, "value", long.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    */
    // END Android-changed: Using VarHandle instead of Unsafe

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
     * Returns the current value,
     * with memory effects as specified by {@link VarHandle#getVolatile}.
     *
     * @return the current value
     */
    public final long get() {
        return value;
    }

    /**
     * Sets the value to {@code newValue},
     * with memory effects as specified by {@link VarHandle#setVolatile}.
     *
     * @param newValue the new value
     */
    public final native void set(long newValue) /*-[
      __c11_atomic_store(&self->value_, newValue, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Sets the value to {@code newValue},
     * with memory effects as specified by {@link VarHandle#setRelease}.
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final native void lazySet(long newValue) /*-[
      __c11_atomic_store(&self->value_, newValue, __ATOMIC_RELEASE);
    ]-*/;

    /**
     * Atomically sets the value to {@code newValue} and returns the old value,
     * with memory effects as specified by {@link VarHandle#getAndSet}.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final native long getAndSet(long newValue) /*-[
      return __c11_atomic_exchange(&self->value_, newValue, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Atomically sets the value to {@code newValue}
     * if the current value {@code == expectedValue},
     * with memory effects as specified by {@link VarHandle#compareAndSet}.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final native boolean compareAndSet(long expect, long update) /*-[
      return __c11_atomic_compare_exchange_strong(
          &self->value_, &expect, update, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Possibly atomically sets the value to {@code newValue}
     * if the current value {@code == expectedValue},
     * with memory effects as specified by {@link VarHandle#weakCompareAndSetPlain}.
     *
     * @deprecated This method has plain memory effects but the method
     * name implies volatile memory effects (see methods such as
     * {@link #compareAndExchange} and {@link #compareAndSet}).  To avoid
     * confusion over plain or volatile memory effects it is recommended that
     * the method {@link #weakCompareAndSetPlain} be used instead.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @see #weakCompareAndSetPlain
     */
    @Deprecated() // J2ObjC modified: removed since="9" argument
    public final native boolean weakCompareAndSet(long expect, long update) /*-[
      return __c11_atomic_compare_exchange_weak(
          &self->value_, &expect, update, __ATOMIC_RELAXED, __ATOMIC_RELAXED);
    ]-*/;

    /**
     * Possibly atomically sets the value to {@code newValue}
     * if the current value {@code == expectedValue},
     * with memory effects as specified by {@link VarHandle#weakCompareAndSetPlain}.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @since 9
     */
    /* J2ObjC removed
    public final boolean weakCompareAndSetPlain(long expectedValue, long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.weakCompareAndSetLongPlain(this, VALUE, expectedValue, newValue);
        return VALUE.weakCompareAndSetPlain(this, expectedValue, newValue);
    }
    */

    /**
     * Atomically increments the current value,
     * with memory effects as specified by {@link VarHandle#getAndAdd}.
     *
     * <p>Equivalent to {@code getAndAdd(1)}.
     *
     * @return the previous value
     */
    public final long getAndIncrement() {
      return getAndAdd(1);
    }

    /**
     * Atomically decrements the current value,
     * with memory effects as specified by {@link VarHandle#getAndAdd}.
     *
     * <p>Equivalent to {@code getAndAdd(-1)}.
     *
     * @return the previous value
     */
    public final long getAndDecrement() {
      return getAndAdd(-1);
    }

    /**
     * Atomically adds the given value to the current value,
     * with memory effects as specified by {@link VarHandle#getAndAdd}.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public final native long getAndAdd(long delta) /*-[
      return __c11_atomic_fetch_add(&self->value_, delta, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Atomically increments the current value,
     * with memory effects as specified by {@link VarHandle#getAndAdd}.
     *
     * <p>Equivalent to {@code addAndGet(1)}.
     *
     * @return the updated value
     */
    public final long incrementAndGet() {
      return addAndGet(1);
    }

    /**
     * Atomically decrements the current value,
     * with memory effects as specified by {@link VarHandle#getAndAdd}.
     *
     * <p>Equivalent to {@code addAndGet(-1)}.
     *
     * @return the updated value
     */
    public final long decrementAndGet() {
      return addAndGet(-1);
    }

    /**
     * Atomically adds the given value to the current value,
     * with memory effects as specified by {@link VarHandle#getAndAdd}.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public final native long addAndGet(long delta) /*-[
      return __c11_atomic_fetch_add(&self->value_, delta, __ATOMIC_SEQ_CST) + delta;
    ]-*/;

    /**
     * Atomically updates (with memory effects as specified by {@link
     * VarHandle#compareAndSet}) the current value with the results of
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
     * Atomically updates (with memory effects as specified by {@link
     * VarHandle#compareAndSet}) the current value with the results of
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
     * Atomically updates (with memory effects as specified by {@link
     * VarHandle#compareAndSet}) the current value with the results of
     * applying the given function to the current and given values,
     * returning the previous value. The function should be
     * side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function is
     * applied with the current value as its first argument, and the
     * given update as the second argument.
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
     * Atomically updates (with memory effects as specified by {@link
     * VarHandle#compareAndSet}) the current value with the results of
     * applying the given function to the current and given values,
     * returning the updated value. The function should be
     * side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function is
     * applied with the current value as its first argument, and the
     * given update as the second argument.
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
     * Returns the current value of this {@code AtomicLong} as an {@code int}
     * after a narrowing primitive conversion,
     * with memory effects as specified by {@link VarHandle#getVolatile}.
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public int intValue() {
        return (int)get();
    }

    /**
     * Returns the current value of this {@code AtomicLong} as a {@code long},
     * with memory effects as specified by {@link VarHandle#getVolatile}.
     * Equivalent to {@link #get()}.
     */
    public long longValue() {
        return get();
    }

    /**
     * Returns the current value of this {@code AtomicLong} as a {@code float}
     * after a widening primitive conversion,
     * with memory effects as specified by {@link VarHandle#getVolatile}.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * Returns the current value of this {@code AtomicLong} as a {@code double}
     * after a widening primitive conversion,
     * with memory effects as specified by {@link VarHandle#getVolatile}.
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
    
    /* J2ObjC removed
    // jdk9

    /**
     * Returns the current value, with memory semantics of reading as if the
     * variable was declared non-{@code volatile}.
     *
     * @return the value
     * @since 9
     * /
    public final long getPlain() {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.getLong(this, VALUE);
        return (long)VALUE.get(this);
    }

    /**
     * Sets the value to {@code newValue}, with memory semantics
     * of setting as if the variable was declared non-{@code volatile}
     * and non-{@code final}.
     *
     * @param newValue the new value
     * @since 9
     * /
    public final void setPlain(long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // U.putLong(this, VALUE, newValue);
        VALUE.set(this, newValue);
    }

    /**
     * Returns the current value,
     * with memory effects as specified by {@link VarHandle#getOpaque}.
     *
     * @return the value
     * @since 9
     * /
    public final long getOpaque() {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.getLongOpaque(this, VALUE);
        return (long)VALUE.getOpaque(this);
    }

    /**
     * Sets the value to {@code newValue},
     * with memory effects as specified by {@link VarHandle#setOpaque}.
     *
     * @param newValue the new value
     * @since 9
     * /
    public final void setOpaque(long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // U.putLongOpaque(this, VALUE, newValue);
        VALUE.setOpaque(this, newValue);
    }

    /**
     * Returns the current value,
     * with memory effects as specified by {@link VarHandle#getAcquire}.
     *
     * @return the value
     * @since 9
     * /
    public final long getAcquire() {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.getLongAcquire(this, VALUE);
        return (long)VALUE.getAcquire(this);
    }

    /**
     * Sets the value to {@code newValue},
     * with memory effects as specified by {@link VarHandle#setRelease}.
     *
     * @param newValue the new value
     * @since 9
     * /
    public final void setRelease(long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // U.putLongRelease(this, VALUE, newValue);
        VALUE.setRelease(this, newValue);
    }

    /**
     * Atomically sets the value to {@code newValue} if the current value,
     * referred to as the <em>witness value</em>, {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#compareAndExchange}.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return the witness value, which will be the same as the
     * expected value if successful
     * @since 9
     * /
    public final long compareAndExchange(long expectedValue, long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.compareAndExchangeLong(this, VALUE, expectedValue, newValue);
        return (long)VALUE.compareAndExchange(this, expectedValue, newValue);
    }

    /**
     * Atomically sets the value to {@code newValue} if the current value,
     * referred to as the <em>witness value</em>, {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#compareAndExchangeAcquire}.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return the witness value, which will be the same as the
     * expected value if successful
     * @since 9
     * /
    public final long compareAndExchangeAcquire(long expectedValue, long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.compareAndExchangeLongAcquire(this, VALUE, expectedValue, newValue);
        return (long)VALUE.compareAndExchangeAcquire(this, expectedValue, newValue);
    }

    /**
     * Atomically sets the value to {@code newValue} if the current value,
     * referred to as the <em>witness value</em>, {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#compareAndExchangeRelease}.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return the witness value, which will be the same as the
     * expected value if successful
     * @since 9
     * /
    public final long compareAndExchangeRelease(long expectedValue, long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.compareAndExchangeLongRelease(this, VALUE, expectedValue, newValue);
        return (long)VALUE.compareAndExchangeRelease(this, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the value to {@code newValue}
     * if the current value {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#weakCompareAndSet}.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @since 9
     * /
    public final boolean weakCompareAndSetVolatile(long expectedValue, long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.weakCompareAndSetLong(this, VALUE, expectedValue, newValue);
        return VALUE.weakCompareAndSet(this, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the value to {@code newValue}
     * if the current value {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#weakCompareAndSetAcquire}.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @since 9
     * /
    public final boolean weakCompareAndSetAcquire(long expectedValue, long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.weakCompareAndSetLongAcquire(this, VALUE, expectedValue, newValue);
        return VALUE.weakCompareAndSetAcquire(this, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the value to {@code newValue}
     * if the current value {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#weakCompareAndSetRelease}.
     *
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @since 9
     * /
    public final boolean weakCompareAndSetRelease(long expectedValue, long newValue) {
        // Android-changed: Using VarHandle instead of Unsafe
        // return U.weakCompareAndSetLongRelease(this, VALUE, expectedValue, newValue);
        return VALUE.weakCompareAndSetRelease(this, expectedValue, newValue);
    }
    */

}
