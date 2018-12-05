/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.java.lang.ref;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
/* J2ObjC removed.
import sun.misc.Cleaner;
*/

public final class ReferenceQueueTest extends TestCase {

    public void testRemoveWithInvalidTimeout() throws Exception {
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        try {
            referenceQueue.remove(-1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRemoveWithVeryLargeTimeout() throws Exception {
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        enqueueLater(referenceQueue, 500);
        referenceQueue.remove(Long.MAX_VALUE);
    }

    public void testRemoveWithSpuriousNotify() throws Exception {
        final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

        runLater(new Runnable() {
            @Override public void run() {
                synchronized (referenceQueue) {
                    referenceQueue.notifyAll();
                }
            }
        }, 500);

        long startNanos = System.nanoTime();
        referenceQueue.remove(1000);
        long durationNanos = System.nanoTime() - startNanos;
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        assertTrue(durationMillis > 750 && durationMillis < 1250);
    }

    public void testRemoveWithImmediateResultAndNoTimeout() throws Exception {
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        enqueue(referenceQueue);
        assertNotNull(referenceQueue.remove());
    }

    public void testRemoveWithImmediateResultAndTimeout() throws Exception {
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        enqueue(referenceQueue);
        assertNotNull(referenceQueue.remove(1000));
    }

    public void testRemoveWithDelayedResultAndNoTimeout() throws Exception {
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        enqueueLater(referenceQueue, 500);
        long startNanos = System.nanoTime();
        referenceQueue.remove();
        long durationNanos = System.nanoTime() - startNanos;
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        assertTrue(durationMillis > 250 && durationMillis < 750);
    }

    public void testRemoveWithDelayedResultAndTimeout() throws Exception {
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        enqueueLater(referenceQueue, 500);
        long startNanos = System.nanoTime();
        referenceQueue.remove(1000);
        long durationNanos = System.nanoTime() - startNanos;
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        assertTrue(durationMillis > 250 && durationMillis < 750);
    }

    /* J2ObjC removed.
    public void testCleanersCleaned() {
        Object object = new Object();
        final CountDownLatch cdl = new CountDownLatch(1);

        Cleaner cleaner = Cleaner.create(object, new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
            }
        });

        boolean countedDown = false;
        object = null;
        for (int i = 0; i < 5; ++i) {
            Runtime.getRuntime().gc();
            try {
                countedDown = cdl.await(1000, TimeUnit.MILLISECONDS);
                if (countedDown) {
                    break;
                }
            } catch (InterruptedException ie) {
                fail();
            }
        }

        assertTrue(countedDown);
    }
    */

    private void runLater(Runnable runnable, int delayMillis) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(runnable, delayMillis, TimeUnit.MILLISECONDS);
        executor.shutdown();
    }

    private void enqueueLater(final ReferenceQueue<Object> queue, int delayMillis) {
        runLater(new Runnable() {
            @Override public void run() {
                enqueue(queue);
            }
        }, delayMillis);
    }

    private void enqueue(ReferenceQueue<Object> queue) {
        new WeakReference<Object>(new Object(), queue).enqueue();
    }
}
