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
 * A reference that is cleared when its referent is not strongly reachable and
 * there is memory pressure.
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
 * <h3>Garbage Collection of Soft References</h3>
 * When the garbage collector encounters an object {@code obj} that is
 * softly-reachable, the following happens:
 * <ul>
 *   <li>A set {@code refs} of references is determined. {@code refs} contains
 *       the following elements:
 *       <ul>
 *         <li>All soft references pointing to {@code obj}.</li>
 *         <li>All soft references pointing to objects from which {@code obj} is
 *           strongly reachable.</li>
 *       </ul>
 *   </li>
 *   <li>All references in {@code refs} are atomically cleared.</li>
 *   <li>At the same time or some time in the future, all references in {@code
 *       refs} will be enqueued with their corresponding reference queues, if
 *       any.</li>
 * </ul>
 * The system may delay clearing and enqueueing soft references, yet all {@code
 * SoftReference}s pointing to softly reachable objects will be cleared before
 * the runtime throws an {@link OutOfMemoryError}.
 *
 * <p>Unlike a {@code WeakReference}, a {@code SoftReference} will not be
 * cleared and enqueued until the runtime must reclaim memory to satisfy an
 * allocation.
 */
public class SoftReference<T> extends Reference<T> {

    /**
     * Constructs a new soft reference to the given referent. The newly created
     * reference is not registered with any reference queue.
     *
     * @param r the referent to track
     */
    public SoftReference(T r) {
        super(r, null);
    }

    /**
     * Constructs a new soft reference to the given referent. The newly created
     * reference is registered with the given reference queue.
     *
     * @param r the referent to track
     * @param q the queue to register to the reference object with. A null value
     *          results in a weak reference that is not associated with any
     *          queue.
     */
    public SoftReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }
}
