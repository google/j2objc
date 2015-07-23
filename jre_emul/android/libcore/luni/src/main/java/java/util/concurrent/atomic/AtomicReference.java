/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

/**
 * An object reference that may be updated atomically. See the {@link
 * java.util.concurrent.atomic} package specification for description
 * of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 * @param <V> The type of object referred to by this reference
 */
public class AtomicReference<V> implements java.io.Serializable {
    private static final long serialVersionUID = -1848883965231344442L;

    private volatile V value;

    /**
     * Creates a new AtomicReference with the given initial value.
     *
     * @param initialValue the initial value
     */
    public AtomicReference(V initialValue) {
        value = initialValue;
    }

    /**
     * Creates a new AtomicReference with null initial value.
     */
    public AtomicReference() {
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public final V get() {
        return value;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final void set(V newValue) {
        value = newValue;
    }

    /**
     * Eventually sets to the given value.
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final native void lazySet(V newValue) /*-[
      id oldValue = __c11_atomic_exchange(&self->value_, newValue, __ATOMIC_RELEASE);
      [newValue retain];
      [oldValue autorelease];
    ]-*/;

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final native boolean compareAndSet(V expect, V update) /*-[
      if (__c11_atomic_compare_exchange_strong(
          &self->value_, (void **)&expect, update, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST)) {
        [update retain];
        [expect autorelease];
        return YES;
      }
      return NO;
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
    public final native boolean weakCompareAndSet(V expect, V update) /*-[
      if (__c11_atomic_compare_exchange_weak(
          &self->value_, (void **)&expect, update, __ATOMIC_RELAXED, __ATOMIC_RELAXED)) {
        [update retain];
        [expect autorelease];
        return YES;
      }
      return NO;
    ]-*/;

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final native V getAndSet(V newValue) /*-[
      id oldValue = __c11_atomic_exchange(&self->value_, newValue, __ATOMIC_SEQ_CST);
      [newValue retain];
      [oldValue autorelease];
      return oldValue;
    ]-*/;

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value
     */
    public String toString() {
        return String.valueOf(get());
    }

}
