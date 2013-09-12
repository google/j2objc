/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang.ref;

/**
 * The {@code ReferenceQueue} is the container on which reference objects are
 * enqueued when the garbage collector detects the reachability type specified
 * for the referent.
 *
 * @since 1.2
 */
public class ReferenceQueue<T> {
    private static final int NANOS_PER_MILLI = 1000000;

    private Reference<? extends T> head;

    /**
     * Constructs a new instance of this class.
     */
    public ReferenceQueue() {
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Does not wait for a reference to become available.
     *
     * @return the next available reference, or {@code null} if no reference is
     *         immediately available
     */
    @SuppressWarnings("unchecked")
    public synchronized Reference<? extends T> poll() {
        if (head == null) {
            return null;
        }

        Reference<? extends T> ret;

        ret = head;

        if (head == head.queueNext) {
            head = null;
        } else {
            head = head.queueNext;
        }

        ret.queueNext = null;

        return ret;
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Waits indefinitely for a reference to become available.
     *
     * @throws InterruptedException if the blocking call was interrupted
     */
    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0L);
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Waits for a reference to become available or the given timeout
     * period to elapse, whichever happens first.
     *
     * @param timeoutMillis maximum time to spend waiting for a reference object
     *     to become available. A value of {@code 0} results in the method
     *     waiting indefinitely.
     * @return the next available reference, or {@code null} if no reference
     *     becomes available within the timeout period
     * @throws IllegalArgumentException if {@code timeoutMillis < 0}.
     * @throws InterruptedException if the blocking call was interrupted
     */
    public synchronized Reference<? extends T> remove(long timeoutMillis)
            throws InterruptedException {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("timeout < 0: " + timeoutMillis);
        }

        if (head != null) {
            return poll();
        }

        // avoid overflow: if total > 292 years, just wait forever
        if (timeoutMillis == 0 || (timeoutMillis > Long.MAX_VALUE / NANOS_PER_MILLI)) {
            do {
                wait(0);
            } while (head == null);
            return poll();
        }

        // guaranteed to not overflow
        long nanosToWait = timeoutMillis * NANOS_PER_MILLI;
        int timeoutNanos = 0;

        // wait until notified or the timeout has elapsed
        long startTime = System.nanoTime();
        while (true) {
            wait(timeoutMillis, timeoutNanos);
            if (head != null) {
                break;
            }
            long nanosElapsed = System.nanoTime() - startTime;
            long nanosRemaining = nanosToWait - nanosElapsed;
            if (nanosRemaining <= 0) {
                break;
            }
            timeoutMillis = nanosRemaining / NANOS_PER_MILLI;
            timeoutNanos = (int) (nanosRemaining - timeoutMillis * NANOS_PER_MILLI);
        }
        return poll();
    }

    /**
     * Enqueue the reference object on the receiver.
     *
     * @param reference
     *            reference object to be enqueued.
     */
    synchronized void enqueue(Reference<? extends T> reference) {
        if (head == null) {
            reference.queueNext = reference;
        } else {
            reference.queueNext = head;
        }
        head = reference;
        notify();
    }

    /** @hide */
    public static Reference<?> unenqueued = null;

    static void add(Reference<?> list) {
        synchronized (ReferenceQueue.class) {
            if (unenqueued == null) {
                unenqueued = list;
            } else {
                Reference<?> next = unenqueued.pendingNext;
                unenqueued.pendingNext = list.pendingNext;
                list.pendingNext = next;
            }
            ReferenceQueue.class.notifyAll();
        }
    }
}
