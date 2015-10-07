/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

import java.util.Arrays;
import java.lang.reflect.Array;
import sun.misc.Unsafe;

/*-[
#include "java/lang/IndexOutOfBoundsException.h"
]-*/

/**
 * An array of object references in which elements may be updated
 * atomically.  See the {@link java.util.concurrent.atomic} package
 * specification for description of the properties of atomic
 * variables.
 * @since 1.5
 * @author Doug Lea
 * @param <E> The base class of elements held in this array
 */
public class AtomicReferenceArray<E> implements java.io.Serializable {
    private static final long serialVersionUID = -6209656149925076980L;

    private static final Unsafe unsafe;
    private static final long arrayFieldOffset;
    private final Object[] array; // must have exact type Object[]

    static {
        try {
            unsafe = Unsafe.getUnsafe();
            arrayFieldOffset = unsafe.objectFieldOffset
                (AtomicReferenceArray.class.getDeclaredField("array"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Creates a new AtomicReferenceArray of the given length, with all
     * elements initially null.
     *
     * @param length the length of the array
     */
    public AtomicReferenceArray(int length) {
        array = new Object[length];
    }

    /**
     * Creates a new AtomicReferenceArray with the same length as, and
     * all elements copied from, the given array.
     *
     * @param array the array to copy elements from
     * @throws NullPointerException if array is null
     */
    public AtomicReferenceArray(E[] array) {
        // Visibility guaranteed by final field guarantees
        this.array = Arrays.copyOf(array, array.length, Object[].class);
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
    static void CheckIdx(JavaUtilConcurrentAtomicAtomicReferenceArray *self, jint i) {
      if (i < 0 || i >= self->array_->size_) {
        @throw [new_JavaLangIndexOutOfBoundsException_initWithNSString_(
            JreStrcat("$I", @"index ", i)) autorelease];
      }
    }

    static inline volatile_id *GetPtrUnchecked(
        JavaUtilConcurrentAtomicAtomicReferenceArray *self, jint i) {
      return (volatile_id *)&self->array_->buffer_[i];
    }

    static inline volatile_id *GetPtrChecked(
        JavaUtilConcurrentAtomicAtomicReferenceArray *self, jint i) {
      CheckIdx(self, i);
      return (volatile_id *)&self->array_->buffer_[i];
    }
    ]-*/

    /**
     * Gets the current value at position {@code i}.
     *
     * @param i the index
     * @return the current value
     */
    public final native E get(int i) /*-[
      return JreLoadVolatileId(GetPtrChecked(self, i));
    ]-*/;

    private final native E getUnchecked(int i) /*-[
      return JreLoadVolatileId(GetPtrUnchecked(self, i));
    ]-*/;

    /**
     * Sets the element at position {@code i} to the given value.
     *
     * @param i the index
     * @param newValue the new value
     */
    public final native void set(int i, E newValue) /*-[
      JreVolatileStrongAssign(GetPtrChecked(self, i), newValue);
    ]-*/;

    /**
     * Eventually sets the element at position {@code i} to the given value.
     *
     * @param i the index
     * @param newValue the new value
     * @since 1.6
     */
    public final native void lazySet(int i, E newValue) /*-[
      JreVolatileStrongAssign(GetPtrChecked(self, i), newValue);
    ]-*/;

    /**
     * Atomically sets the element at position {@code i} to the given
     * value and returns the old value.
     *
     * @param i the index
     * @param newValue the new value
     * @return the previous value
     */
    public final native E getAndSet(int i, E newValue) /*-[
      return JreExchangeVolatileStrongId(GetPtrChecked(self, i), newValue);
    ]-*/;

    /**
     * Atomically sets the element at position {@code i} to the given
     * updated value if the current value {@code ==} the expected value.
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final native boolean compareAndSet(int i, E expect, E update) /*-[
      return JreCompareAndSwapVolatileStrongId(GetPtrChecked(self, i), expect, update);
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
     * @return true if successful
     */
    public final native boolean weakCompareAndSet(int i, E expect, E update) /*-[
      return JreCompareAndSwapVolatileStrongId(GetPtrChecked(self, i), expect, update);
    ]-*/;

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

    /**
     * Reconstitutes the instance from a stream (that is, deserializes it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException,
        java.io.InvalidObjectException {
        // Note: This must be changed if any additional fields are defined
        Object a = s.readFields().get("array", null);
        if (a == null || !a.getClass().isArray())
            throw new java.io.InvalidObjectException("Not array type");
        if (a.getClass() != Object[].class)
            a = Arrays.copyOf((Object[])a, Array.getLength(a), Object[].class);
        unsafe.putObjectVolatile(this, arrayFieldOffset, a);
    }

}
