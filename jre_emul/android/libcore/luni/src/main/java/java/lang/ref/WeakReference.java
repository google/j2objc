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

/**
 * Implements a weak reference, which is the middle of the three types of
 * references. Once the garbage collector decides that an object {@code obj} is
 * is weakly-reachable, the following
 * happens:
 *
 * <ul>
 *   <li>
 *     A set {@code ref} of references is determined. {@code ref} contains the
 *     following elements:
 *     <ul>
 *       <li>
 *         All weak references pointing to {@code obj}.
 *       </li>
 *       <li>
 *         All weak references pointing to objects from which {@code obj} is
 *         either strongly or softly reachable.
 *       </li>
 *     </ul>
 *   </li>
 *   <li>
 *     All references in {@code ref} are atomically cleared.
 *   </li>
 *   <li>
 *     All objects formerly being referenced by {@code ref} become eligible for
 *     finalization.
 *   </li>
 *   <li>
 *     At some future point, all references in {@code ref} will be enqueued
 *     with their corresponding reference queues, if any.
 *   </li>
 * </ul>
 *
 * Weak references are useful for mappings that should have their entries
 * removed automatically once they are not referenced any more (from outside).
 * The difference between a {@code SoftReference} and a {@code WeakReference} is
 * the point of time at which the decision is made to clear and enqueue the
 * reference:
 *
 * <ul>
 *   <li>
 *     A {@code SoftReference} should be cleared and enqueued <em>as late as
 *     possible</em>, that is, in case the VM is in danger of running out of
 *     memory.
 *   </li>
 *   <li>
 *     A {@code WeakReference} may be cleared and enqueued as soon as is
 *     known to be weakly-referenced.
 *   </li>
 * </ul>
 */
public class WeakReference<T> extends Reference<T> {

    /**
     * Constructs a new weak reference to the given referent. The newly created
     * reference is not registered with any reference queue.
     *
     * @param r the referent to track
     */
    public WeakReference(T r) {
        super(r, null);
    }

    /**
     * Constructs a new weak reference to the given referent. The newly created
     * reference is registered with the given reference queue.
     *
     * @param r the referent to track
     * @param q the queue to register to the reference object with. A null value
     *          results in a weak reference that is not associated with any
     *          queue.
     */
    public WeakReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }
}
