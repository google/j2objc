/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

/*-[
#include <libkern/OSAtomic.h>
]-*/

/**
 * An object reference that may be updated atomically. See the {@link
 * java.util.concurrent.atomic} package specification for description
 * of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 * @param <V> The type of object referred to by this reference
 */
public class AtomicReference<V>  implements java.io.Serializable {
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
        memoryBarrier();
        return value;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final void set(V newValue) {
        memoryBarrier();
        value = newValue;
    }

    /**
     * Eventually sets to the given value.
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final void lazySet(V newValue) {
        memoryBarrier();
        value = newValue;
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(V expect, V update) {
        return compareAndSwapValue(expect, update);
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * <p>May <a href="package-summary.html#Spurious">fail spuriously</a>
     * and does not provide ordering guarantees, so is only rarely an
     * appropriate alternative to {@code compareAndSet}.
     *
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public final boolean weakCompareAndSet(V expect, V update) {
        return compareAndSwapValue(expect, update);
    }

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final V getAndSet(V newValue) {
        while (true) {
            V x = get();
            if (compareAndSet(x, newValue))
                return x;
        }
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value.
     */
    public String toString() {
        return String.valueOf(get());
    }

    private static native void memoryBarrier() /*-[
      OSMemoryBarrier();
    ]-*/;

    private native boolean compareAndSwapValue(V oldValue, V newValue) /*-[
#if __has_feature(objc_arc)
      void * volatile tmp = (__bridge void * volatile) value_;
      return OSAtomicCompareAndSwapPtrBarrier(
          (__bridge void *) oldValue, (__bridge void *) newValue, &tmp);
#else
      id tmp = self->value_;
      if (OSAtomicCompareAndSwapPtrBarrier(oldValue, newValue, (void * volatile *) &self->value_)) {
        [self->value_ retain];
        [tmp autorelease];
        return YES;
      }
      return NO;
#endif
    ]-*/;

}
