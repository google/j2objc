/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jsr166;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for java.util.concurrent package.
 */
public class ConcurrencyTests extends TestSuite {

  private static final Class<?>[] testClasses = new Class[] {
    AbstractExecutorServiceTest.class,
    AbstractQueueTest.class,
    AbstractQueuedLongSynchronizerTest.class,
    AbstractQueuedSynchronizerTest.class,
    ArrayBlockingQueueTest.class,
    ArrayDequeTest.class,
    Atomic8Test.class,
    AtomicBooleanTest.class,
    AtomicIntegerArrayTest.class,
    AtomicIntegerFieldUpdaterTest.class,
    AtomicIntegerTest.class,
    AtomicLongArrayTest.class,
    AtomicLongFieldUpdaterTest.class,
    AtomicLongTest.class,
    AtomicMarkableReferenceTest.class,
    AtomicReferenceArrayTest.class,
    AtomicReferenceFieldUpdaterTest.class,
    AtomicReferenceTest.class,
    AtomicStampedReferenceTest.class,
    CompletableFutureTest.class,
    ConcurrentHashMapTest.class,
    ConcurrentLinkedDequeTest.class,
    ConcurrentLinkedQueueTest.class,
    ConcurrentSkipListMapTest.class,
    ConcurrentSkipListSetTest.class,
    ConcurrentSkipListSubMapTest.class,
    ConcurrentSkipListSubSetTest.class,
    CopyOnWriteArrayListTest.class,
    CopyOnWriteArraySetTest.class,
    CountDownLatchTest.class,
    CountedCompleterTest.class,
    CyclicBarrierTest.class,
    DelayQueueTest.class,
    EntryTest.class,
    ExchangerTest.class,
    ExecutorCompletionServiceTest.class,
    ExecutorsTest.class,
    ForkJoinPool8Test.class,
    ForkJoinPoolTest.class,
    ForkJoinTask8Test.class,
    ForkJoinTaskTest.class,
    FutureTaskTest.class,
    LinkedBlockingDequeTest.class,
    LinkedBlockingQueueTest.class,
    LinkedListTest.class,
    LinkedTransferQueueTest.class,
    LockSupportTest.class,
    PhaserTest.class,
    PriorityBlockingQueueTest.class,
    PriorityQueueTest.class,
    RecursiveActionTest.class,
    RecursiveTaskTest.class,
    ReentrantLockTest.class,
    ReentrantReadWriteLockTest.class,
    ScheduledExecutorSubclassTest.class,
    ScheduledExecutorTest.class,
    SynchronousQueueTest.class,
    SystemTest.class,
    ThreadLocalRandomTest.class,
    ThreadPoolExecutorSubclassTest.class,
    ThreadPoolExecutorTest.class,
    ThreadTest.class,
    TimeUnitTest.class,
    TreeMapTest.class,
    TreeSetTest.class,
    TreeSubMapTest.class,
    TreeSubSetTest.class,
  };

  public static Test suite() {
    return new TestSuite(testClasses);
  }
}
