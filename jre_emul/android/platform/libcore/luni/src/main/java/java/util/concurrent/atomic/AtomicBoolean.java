/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

/**
 * A {@code boolean} value that may be updated atomically. See the
 * {@link java.util.concurrent.atomic} package specification for
 * description of the properties of atomic variables. An
 * {@code AtomicBoolean} is used in applications such as atomically
 * updated flags, and cannot be used as a replacement for a
 * {@link java.lang.Boolean}.
 *
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicBoolean implements java.io.Serializable {
    private static final long serialVersionUID = 4654671469794556979L;

    /* J2ObjC removed.
    private static final sun.misc.Unsafe U = sun.misc.Unsafe.getUnsafe();
    private static final long VALUE;

    static {
        try {
            VALUE = U.objectFieldOffset
                (AtomicBoolean.class.getDeclaredField("value"));
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }
    */

    private volatile int value;

    /**
     * Creates a new {@code AtomicBoolean} with the given initial value.
     *
     * @param initialValue the initial value
     */
    public AtomicBoolean(boolean initialValue) {
        value = initialValue ? 1 : 0;
    }

    /**
     * Creates a new {@code AtomicBoolean} with initial value {@code false}.
     */
    public AtomicBoolean() {
    }

    /**
     * Returns the current value.
     *
     * @return the current value
     */
    public final boolean get() {
        return value != 0;
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final native boolean compareAndSet(boolean expect, boolean update) /*-[
      jint e = expect ? 1 : 0;
      return __c11_atomic_compare_exchange_strong(
          &self->value_, &e, update ? 1 : 0, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
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
    public native boolean weakCompareAndSet(boolean expect, boolean update) /*-[
      jint e = expect ? 1 : 0;
      return __c11_atomic_compare_exchange_weak(
          &self->value_, &e, update ? 1 : 0, __ATOMIC_RELAXED, __ATOMIC_RELAXED);
    ]-*/;

    /**
     * Unconditionally sets to the given value.
     *
     * @param newValue the new value
     */
    public final void set(boolean newValue) {
        value = newValue ? 1 : 0;
    }

    /**
     * Eventually sets to the given value.
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final native void lazySet(boolean newValue) /*-[
      __c11_atomic_store(&self->value_, newValue ? 1 : 0, __ATOMIC_RELEASE);
    ]-*/;

    /**
     * Atomically sets to the given value and returns the previous value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final native boolean getAndSet(boolean newValue) /*-[
      return __c11_atomic_exchange(&self->value_, newValue ? 1 : 0, __ATOMIC_SEQ_CST);
    ]-*/;

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value
     */
    public String toString() {
        return Boolean.toString(get());
    }

}
