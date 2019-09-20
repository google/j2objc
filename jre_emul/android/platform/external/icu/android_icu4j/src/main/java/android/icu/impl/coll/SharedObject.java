/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* SharedObject.java, ported from sharedobject.h/.cpp
*
* C++ version created on: 2013dec19
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.util.concurrent.atomic.AtomicInteger;

import android.icu.util.ICUCloneNotSupportedException;

/**
 * Base class for shared, reference-counted, auto-deleted objects.
 * Java subclasses are mutable and must implement clone().
 *
 * <p>In C++, the SharedObject base class is used for both memory and ownership management.
 * In Java, memory management (deletion after last reference is gone)
 * is up to the garbage collector,
 * but the reference counter is still used to see whether the referent is the sole owner.
 *
 * <p>Usage:
 * <pre>
 * class S extends SharedObject {
 *     public clone() { ... }
 * }
 *
 * // Either use the nest class Reference (which costs an extra allocation),
 * // or duplicate its code in the class that uses S
 * // (which duplicates code and is more error-prone).
 * class U {
 *     // For read-only access, use s.readOnly().
 *     // For writable access, use S ownedS = s.copyOnWrite();
 *     private SharedObject.Reference&lt;S&gt; s;
 *     // Returns a writable version of s.
 *     // If there is exactly one owner, then s itself is returned.
 *     // If there are multiple owners, then s is replaced with a clone,
 *     // and that is returned.
 *     private S getOwnedS() {
 *         return s.copyOnWrite();
 *     }
 *     public U clone() {
 *         ...
 *         c.s = s.clone();
 *         ...
 *     }
 * }
 *
 * class V {
 *     // For read-only access, use s directly.
 *     // For writable access, use S ownedS = getOwnedS();
 *     private S s;
 *     // Returns a writable version of s.
 *     // If there is exactly one owner, then s itself is returned.
 *     // If there are multiple owners, then s is replaced with a clone,
 *     // and that is returned.
 *     private S getOwnedS() {
 *         if(s.getRefCount() > 1) {
 *             S ownedS = s.clone();
 *             s.removeRef();
 *             s = ownedS;
 *             ownedS.addRef();
 *         }
 *         return s;
 *     }
 *     public U clone() {
 *         ...
 *         s.addRef();
 *         ...
 *     }
 *     protected void finalize() {
 *         ...
 *         if(s != null) {
 *             s.removeRef();
 *             s = null;
 *         }
 *         ...
 *     }
 * }
 * </pre>
 *
 * Either use only Java memory management, or use addRef()/removeRef().
 * Sharing requires reference-counting.
 *
 * TODO: Consider making this more widely available inside ICU,
 * or else adopting a different model.
 * @hide Only a subset of ICU is exposed in Android
 */
public class SharedObject implements Cloneable {
    /**
     * Similar to a smart pointer, basically a port of the static methods of C++ SharedObject.
     */
    public static final class Reference<T extends SharedObject> implements Cloneable {
        private T ref;

        public Reference(T r) {
            ref = r;
            if(r != null) {
                r.addRef();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Reference<T> clone() {
            Reference<T> c;
            try {
                c = (Reference<T>)super.clone();
            } catch (CloneNotSupportedException e) {
                // Should never happen.
                throw new ICUCloneNotSupportedException(e);
            }
            if(ref != null) {
                ref.addRef();
            }
            return c;
        }

        public T readOnly() { return ref; }

        /**
         * Returns a writable version of the reference.
         * If there is exactly one owner, then the reference itself is returned.
         * If there are multiple owners, then the reference is replaced with a clone,
         * and that is returned.
         */
        public T copyOnWrite() {
            T r = ref;
            if(r.getRefCount() <= 1) { return r; }
            @SuppressWarnings("unchecked")
            T r2 = (T)r.clone();
            r.removeRef();
            ref = r2;
            r2.addRef();
            return r2;
        }

        public void clear() {
            if(ref != null) {
                ref.removeRef();
                ref = null;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            clear();
        }
    }

    /** Initializes refCount to 0. */
    public SharedObject() {}

    /** Initializes refCount to 0. */
    @Override
    public SharedObject clone() {
        SharedObject c;
        try {
            c = (SharedObject)super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen.
            throw new ICUCloneNotSupportedException(e);
        }
        c.refCount = new AtomicInteger();
        return c;
    }

    /**
     * Increments the number of references to this object. Thread-safe.
     */
    public final void addRef() { refCount.incrementAndGet(); }
    /**
     * Decrements the number of references to this object,
     * and auto-deletes "this" if the number becomes 0. Thread-safe.
     */
    public final void removeRef() {
        // Deletion in Java is up to the garbage collector.
        refCount.decrementAndGet();
    }

    /**
     * Returns the reference counter. Uses a memory barrier.
     */
    public final int getRefCount() { return refCount.get(); }

    public final void deleteIfZeroRefCount() {
        // Deletion in Java is up to the garbage collector.
    }

    private AtomicInteger refCount = new AtomicInteger();
}
