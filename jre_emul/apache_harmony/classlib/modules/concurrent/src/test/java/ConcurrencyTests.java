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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for java.util.concurrent package.
 */
public class ConcurrencyTests extends TestSuite {

  private static final Class<?>[] testClasses = new Class[] {
    AbstractExecutorServiceTest.class,
    AbstractQueuedSynchronizerTest.class,
    AbstractQueueTest.class,
    ArrayBlockingQueueTest.class,
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
    ConcurrentHashMapTest.class,
    CopyOnWriteArrayListTest.class,
    CopyOnWriteArraySetTest.class,
    CountDownLatchTest.class,
    CyclicBarrierTest.class,
    DelayQueueTest.class,
    ExchangerTest.class,
    ExecutorCompletionServiceTest.class,
    ExecutorsTest.class,
    FutureTaskTest.class,
    LinkedBlockingQueueTest.class,
    LinkedListTest.class,
    LockSupportTest.class,
    PriorityBlockingQueueTest.class,
    PriorityQueueTest.class,
    ReentrantLockTest.class,
    ReentrantReadWriteLockTest.class,
    ScheduledExecutorTest.class,
//    SemaphoreTest.class,
    SynchronousQueueTest.class,
//    SystemTest.class,
//    ThreadLocalTest.class,
    ThreadPoolExecutorTest.class,
    TimeUnitTest.class,
  };

  public static Test suite() {
    return new TestSuite(testClasses);
  }
}
