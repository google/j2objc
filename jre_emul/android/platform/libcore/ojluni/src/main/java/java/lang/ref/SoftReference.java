/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.ref;


/**
 * Soft reference objects, which are cleared at the discretion of the garbage
 * collector in response to memory demand.
 *
 * <p> Suppose that the garbage collector determines at a certain point in time
 * that an object is <a href="package-summary.html#reachability">softly
 * reachable</a>.  At that time it may choose to clear atomically all soft
 * references to that object and all soft references to any other
 * softly-reachable objects from which that object is reachable through a chain
 * of strong references.  At the same time or at some later time it will
 * enqueue those newly-cleared soft references that are registered with
 * reference queues.
 *
 * <p> All soft references to softly-reachable objects are guaranteed to have
 * been cleared before the virtual machine throws an
 * <code>OutOfMemoryError</code>.  Otherwise no constraints are placed upon the
 * time at which a soft reference will be cleared or the order in which a set
 * of such references to different objects will be cleared.  Virtual machine
 * implementations are, however, encouraged to bias against clearing
 * recently-created or recently-used soft references.
 *
 * <h3>Avoid Soft References for Caching</h3>
 * In practice, soft references are inefficient for caching. The runtime doesn't
 * have enough information on which references to clear and which to keep. Most
 * fatally, it doesn't know what to do when given the choice between clearing a
 * soft reference and growing the heap.
 *
 * <p>The lack of information on the value to your application of each reference
 * limits the usefulness of soft references. References that are cleared too
 * early cause unnecessary work; those that are cleared too late waste memory.
 *
 * <p>Most applications should use an {@code android.util.LruCache} instead of
 * soft references. LruCache has an effective eviction policy and lets the user
 * tune how much memory is allotted.
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class SoftReference<T> extends Reference<T> {

    /* J2ObjC removed.
     * Timestamp clock, updated by the garbage collector
    static private long clock;
    */

    /* J2ObjC removed.
     * Timestamp updated by each invocation of the get method.  The VM may use
     * this field when selecting soft references to be cleared, but it is not
     * required to do so.
    private long timestamp;
    */

    /**
     * Creates a new soft reference that refers to the given object.  The new
     * reference is not registered with any queue.
     *
     * @param referent object the new soft reference will refer to
     */
    public SoftReference(T referent) {
        super(referent);
        // J2ObjC removed.
        //this.timestamp = clock;
    }

    /**
     * Creates a new soft reference that refers to the given object and is
     * registered with the given queue.
     *
     * @param referent object the new soft reference will refer to
     * @param q the queue with which the reference is to be registered,
     *          or <tt>null</tt> if registration is not required
     *
     */
    public SoftReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
        // J2ObjC removed.
        //this.timestamp = clock;
    }

    /* J2ObjC removed.
     * Returns this reference object's referent.  If this reference object has
     * been cleared, either by the program or by the garbage collector, then
     * this method returns <code>null</code>.
     *
     * @return   The object to which this reference refers, or
     *           <code>null</code> if this reference object has been cleared
    public T get() {
        T o = super.get();
        if (o != null && this.timestamp != clock)
            this.timestamp = clock;
        return o;
    }
    */
}
