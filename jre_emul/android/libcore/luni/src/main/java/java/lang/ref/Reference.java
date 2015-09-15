/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang.ref;

import com.google.j2objc.annotations.Weak;

/*-[
#import "IOSReference.h"
]-*/

/**
 * Provides an abstract class which describes behavior common to all reference
 * objects. It is not possible to create immediate subclasses of
 * {@code Reference} in addition to the ones provided by this package. It is
 * also not desirable to do so, since references require very close cooperation
 * with the system's garbage collector. The existing, specialized reference
 * classes should be used instead.
 *
 * <p>Three different type of references exist, each being weaker than the preceding one:
 * {@link java.lang.ref.SoftReference}, {@link java.lang.ref.WeakReference}, and
 * {@link java.lang.ref.PhantomReference}. "Weakness" here means that less restrictions are
 * being imposed on the garbage collector as to when it is allowed to
 * actually garbage-collect the referenced object.
 *
 * <p>In order to use reference objects properly it is important to understand
 * the different types of reachability that trigger their clearing and
 * enqueueing. The following table lists these, from strongest to weakest.
 * For each row, an object is said to have the reachability on the left side
 * if (and only if) it fulfills all of the requirements on the right side. In
 * all rows, consider the <em>root set</em> to be a set of references that
 * are "resistant" to garbage collection (that is, running threads, method
 * parameters, local variables, static fields and the like).
 *
 * <p><table>
 * <tr>
 * <td>Strongly reachable</td>
 * <td> <ul>
 * <li>There exists at least one path from the root set to the object that does not traverse any
 * instance of a {@code java.lang.ref.Reference} subclass.
 * </li>
 * </ul> </td>
 * </tr>
 *
 * <tr>
 * <td>Softly reachable</td>
 * <td> <ul>
 * <li>The object is not strongly reachable.</li>
 * <li>There exists at least one path from the root set to the object that does traverse
 * a {@code java.lang.ref.SoftReference} instance, but no {@code java.lang.ref.WeakReference}
 * or {@code java.lang.ref.PhantomReference} instances.</li>
 * </ul> </td>
 * </tr>
 *
 * <tr>
 * <td>Weakly reachable</td>
 * <td> <ul>
 * <li>The object is neither strongly nor softly reachable.</li>
 * <li>There exists at least one path from the root set to the object that does traverse a
 * {@code java.lang.ref.WeakReference} instance, but no {@code java.lang.ref.PhantomReference}
 * instances.</li>
 * </ul> </td>
 * </tr>
 *
 * <tr>
 * <td>Phantom-reachable</td>
 * <td> <ul>
 * <li>The object is neither strongly, softly, nor weakly reachable.</li>
 * <li>The object is referenced by a {@code java.lang.ref.PhantomReference} instance.</li>
 * <li>The object has already been finalized.</li>
 * </ul> </td>
 * </tr>
 * </table>
 */
public abstract class Reference<T> {

    /**
     * The object to which this reference refers.
     * VM requirement: this field <em>must</em> be called "referent"
     * and be an object.
     */
    @Weak
    volatile T referent;

    /**
     * If non-null, the queue on which this reference will be enqueued
     * when the referent is appropriately reachable.
     * VM requirement: this field <em>must</em> be called "queue"
     * and be a java.lang.ref.ReferenceQueue.
     */
    @Weak
    volatile ReferenceQueue<? super T> queue;

    /**
     * Used internally by java.lang.ref.ReferenceQueue.
     * VM requirement: this field <em>must</em> be called "queueNext"
     * and be a java.lang.ref.Reference.
     */
    @SuppressWarnings("unchecked")
    volatile Reference queueNext;

    /**
     * Used internally by the VM.  This field forms a circular and
     * singly linked list of reference objects discovered by the
     * garbage collector and awaiting processing by the reference
     * queue thread.
     *
     * @hide
     */
    public volatile Reference<?> pendingNext;

    /**
     * Constructs a new instance of this class.
     */
    Reference() {
    }

    Reference(T r, ReferenceQueue<? super T> q) {
        referent = r;
        queue = q;
        initReferent();
    }

    /**
     * Makes the referent {@code null}. This does not force the reference
     * object to be enqueued.
     */
    public native void clear() /*-[
      [IOSReference clearReferent:self];
    ]-*/;

    /**
     * Adds an object to its reference queue.
     *
     * @return {@code true} if this call has caused the {@code Reference} to
     * become enqueued, or {@code false} otherwise
     *
     * @hide
     */
    public final synchronized boolean enqueueInternal() {
        if (queue != null && queueNext == null) {
            queue.enqueue(this);
            queue = null;
            return true;
        }
        return false;
    }

    /**
     * Forces the reference object to be enqueued if it has been associated with
     * a queue.
     *
     * @return {@code true} if this call has caused the {@code Reference} to
     * become enqueued, or {@code false} otherwise
     */
    public boolean enqueue() {
        return enqueueInternal();
    }

    /**
     * Returns the referent of the reference object.
     *
     * @return the referent to which reference refers, or {@code null} if the
     *         object has been cleared.
     */
    public native T get() /*-[
      return [IOSReference getReferent:self];
    ]-*/;

    /**
     * Checks whether the reference object has been enqueued.
     *
     * @return {@code true} if the {@code Reference} has been enqueued, {@code
     *         false} otherwise
     */
    public boolean isEnqueued() {
        return queueNext != null;
    }

    @Override
    protected void finalize() {
      clear();
    }

    private native void initReferent() /*-[
      [IOSReference initReferent:self];
    ]-*/;
}
