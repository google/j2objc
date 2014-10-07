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
 * Implements a phantom reference, which is the weakest of the three types of
 * references. Once the garbage collector decides that an object {@code obj} is
 * phantom-reachable, it is being enqueued
 * on the corresponding queue, but its referent is not cleared. That is, the
 * reference queue of the phantom reference must explicitly be processed by some
 * application code. As a consequence, a phantom reference that is not
 * registered with any reference queue does not make any sense.
 * <p>
 * Phantom references are useful for implementing cleanup operations that are
 * necessary before an object gets garbage-collected. They are sometimes more
 * flexible than the {@link Object#finalize()} method.
 */
public class PhantomReference<T> extends Reference<T> {

    /**
     * Constructs a new phantom reference and registers it with the given
     * reference queue. The reference queue may be {@code null}, but this case
     * does not make any sense, since the reference will never be enqueued, and
     * the {@link #get()} method always returns {@code null}.
     *
     * @param r the referent to track
     * @param q the queue to register the phantom reference object with
     */
    public PhantomReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }

    /**
     * Returns {@code null}.  The referent of a phantom reference is not
     * accessible.
     *
     * @return {@code null} (always)
     */
    @Override
    public T get() {
        return null;
    }
}
