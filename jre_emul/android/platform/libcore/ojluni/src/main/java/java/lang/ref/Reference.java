/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

import com.google.j2objc.annotations.Weak;

/*-[
#import "IOSReference.h"
]-*/

/**
 * Abstract base class for reference objects.  This class defines the
 * operations common to all reference objects.  Because reference objects are
 * implemented in close cooperation with the garbage collector, this class may
 * not be subclassed directly.
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public abstract class Reference<T> {

    @Weak
    volatile T referent;         /* Treated specially by GC */
    @Weak
    final ReferenceQueue<? super T> queue;

    /*
     * This field forms a singly-linked list of reference objects that have
     * been enqueued. The queueNext field is non-null if and only if this
     * reference has been enqueued. After this reference has been enqueued and
     * before it has been removed from its queue, the queueNext field points
     * to the next reference on the queue. The last reference on a queue
     * points to itself. Once this reference has been removed from the
     * reference queue, the queueNext field points to the
     * ReferenceQueue.sQueueNextUnenqueued sentinel reference object for the
     * rest of this reference's lifetime.
     * <p>
     * Access to the queueNext field is guarded by synchronization on a lock
     * internal to 'queue'.
     */
    Reference queueNext;

    /**
     * The pendingNext field is initially set by the GC. After the GC forms a
     * complete circularly linked list, the list is handed off to the
     * ReferenceQueueDaemon using the ReferenceQueue.class lock. The
     * ReferenceQueueDaemon can then read the pendingNext fields without
     * additional synchronization.
     */
    Reference<?> pendingNext;

    /* -- Referent accessor and setters -- */

    /**
     * Returns this reference object's referent.  If this reference object has
     * been cleared, either by the program or by the garbage collector, then
     * this method returns <code>null</code>.
     *
     * @return   The object to which this reference refers, or
     *           <code>null</code> if this reference object has been cleared
     */
    public native T get() /*-[
      return [IOSReference getReferent:self];
    ]-*/;

    /**
     * Clears this reference object.  Invoking this method will not cause this
     * object to be enqueued.
     *
     * <p> This method is invoked only by Java code; when the garbage collector
     * clears references it does so directly, without invoking this method.
     */
    public native void clear() /*-[
      [IOSReference clearReferent:self];
    ]-*/;

    /* -- Queue operations -- */

    /**
     * Tells whether or not this reference object has been enqueued, either by
     * the program or by the garbage collector.  If this reference object was
     * not registered with a queue when it was created, then this method will
     * always return <code>false</code>.
     *
     * @return   <code>true</code> if and only if this reference object has
     *           been enqueued
     */
    public boolean isEnqueued() {
        // Contrary to what the documentation says, this method returns false
        // after this reference object has been removed from its queue
        // (b/26647823). ReferenceQueue.isEnqueued preserves this historically
        // incorrect behavior.
        return queue != null && queue.isEnqueued(this);
    }

    /**
     * Adds this reference object to the queue with which it is registered,
     * if any.
     *
     * <p> This method is invoked only by Java code; when the garbage collector
     * enqueues references it does so directly, without invoking this method.
     *
     * @return   <code>true</code> if this reference object was successfully
     *           enqueued; <code>false</code> if it was already enqueued or if
     *           it was not registered with a queue when it was created
     */
    public boolean enqueue() {
       return queue != null && queue.enqueue(this);
    }


    /* -- Constructors -- */

    Reference(T referent) {
        this(referent, null);
    }

    Reference(T referent, ReferenceQueue<? super T> queue) {
        this.referent = referent;
        this.queue = queue;
        initReferent();
    }

    @Override
    protected void finalize() {
      clear();
    }

    private native void initReferent() /*-[
      [IOSReference initReferent:self];
    ]-*/;
}
