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

package com.google.j2objc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@code synchronized} monitor happens-before semantics across {@code objc-sync}. */
@RunWith(JUnit4.class)
public class SynchronizedTest {

  // J2ObjC omits weakCompareAndSetPlain; weakCompareAndSet lowers to __ATOMIC_RELAXED.
  @SuppressWarnings("deprecation")
  private static void advancePhaseRelaxed(AtomicInteger phase, int from, int to) {
    while (!phase.weakCompareAndSet(from, to)) {
      Thread.onSpinWait();
    }
  }

  // J2ObjC omits weakCompareAndSetPlain; weakCompareAndSet lowers to __ATOMIC_RELAXED.
  @SuppressWarnings("deprecation")
  private static void awaitPhaseRelaxed(AtomicInteger phase, int expected) {
    while (!phase.weakCompareAndSet(expected, expected)) {
      Thread.onSpinWait();
    }
  }

  /**
   * White-box regression test for a monitor-recycling happens-before bug in J2ObjC's APSL {@code
   * objc-sync} striped {@code SyncData} monitor pool ({@code apple_apsl/objc-sync.m}).
   *
   * <p>While the assertion itself checks standard Java Memory Model monitor semantics (unlocking
   * {@code lockA} must happen-before any subsequent lock of {@code lockA}, so the test remains
   * valid on any JVM or replacement runtime), the object setup white-boxes {@code objc-sync.m}'s
   * 16-bucket address hash ({@code (ptr >> 5) & 15}), per-bucket spinlock ({@code lockp}), and
   * {@code SyncData} node recycling ({@code threadCount == 0}). Once all three threads pass {@code
   * startLatch}, all {@code phase} handoffs use {@code weakCompareAndSet} ({@code
   * __ATOMIC_RELAXED}) so {@code phase} adds no side-channel happens-before edges.
   *
   * <pre>{@code
   * Setup in bucket B = HASH(lockA) == HASH(lockB) (after startLatch barrier):
   *   Unused SyncData nodes in bucket B: D1 (mutex M1, count=0) and D2 (mutex M2, count=0)
   *
   * Thread t1 (lockA)        Bucket B / D1.threadCount     Thread t2 (lockB) / Thread t3 (lockA)
   * -----------------        -------------------------     -------------------------------------
   * enter sync(lockA):
   *   lock(lockp)   -------> [claim D1: obj=lockA, cnt=1]
   *   unlock(lockp) <-------
   *   lock(M1)
   *   shared[2] = 42
   * exit sync(lockA):
   *   D1.cnt: 1 -> 0 ======> (acq_rel release #1)
   *   unlock(M1)                  |
   * phase 0 -> 1 (relaxed)        | synchronizes-with
   *                               v
   *                          (acquire #1: D1.cnt == 0) <-- t2: await phase == 1 (relaxed)
   *                                                        t2: enter sync(lockB):
   *                          [recycle D1: obj=lockB,   <--   lock(lockp)
   *                           cnt=1]
   *                          (lockp release #2) <=========   unlock(lockp)  [before lock(M1)!]
   *                               |                          lock(M1)       [t2 holds D1/M1]
   *                               | synchronizes-with      t2: phase 1 -> 2 (relaxed)
   *                               v
   *                          (lockp acquire #2) <========= t3: await phase == 2 (relaxed)
   *                                                        t3: enter sync(lockA):
   *                          [D1 busy on lockB;        <--   lock(lockp)
   *                           claim D2: obj=lockA, cnt=1]
   *                                                          unlock(lockp)
   *                                                          lock(M2)       [M2 != M1]
   *                                                          shared[2] += 1 [reads 42 -> 43]
   *                                                        t3: exit sync(lockA)
   *                                                        t3: phase 2 -> 3 (relaxed)
   * await phase == 3 (relaxed)                             t2: await phase == 3, exit sync(lockB)
   * }</pre>
   *
   * <p>In a runtime with dedicated per-object monitors, two {@code synchronized (lockA)} blocks
   * always acquire the same underlying lock. In {@code objc-sync}'s pooled {@code SyncData} table,
   * however, {@code t2} (synchronizing on a hash-colliding object {@code lockB}) recycles {@code
   * t1}'s old mutex ({@code M1}) and keeps holding it, forcing {@code t3} onto a second mutex
   * ({@code M2}) for {@code lockA}. Because {@code t1} and {@code t3} never touch the same mutex,
   * and {@code t2} releases the bucket spinlock ({@code lockp}) before locking {@code M1}, the
   * mutexes alone never connect {@code t1} to {@code t3}. Instead, {@code t1}'s release decrement
   * of {@code D1->threadCount} to {@code 0} must synchronize-with {@code t2}'s acquire load during
   * recycling under {@code lockp}, which then synchronizes-with {@code t3}'s acquisition of {@code
   * lockp}.
   */
  @Test
  public void testSynchronizedHappensBeforeAcrossBucketCollision() throws Exception {
    for (int trial = 0; trial < 10; trial++) {
      runBucketCollisionTrial();
    }
  }

  private static void runBucketCollisionTrial() throws Exception {
    // Find 4 distinct objects that map to the same objc-sync bucket (((ptr >> 5) & 15)).
    Object[] bucketMatches = new Object[4];
    List<List<Object>> buckets = new ArrayList<>(16);
    for (int i = 0; i < 16; i++) {
      buckets.add(new ArrayList<>());
    }
    while (true) {
      Object candidate = new Object();
      int bucket = (System.identityHashCode(candidate) >>> 5) & 15;
      List<Object> bucketList = buckets.get(bucket);
      bucketList.add(candidate);
      if (bucketList.size() == 4) {
        for (int i = 0; i < 4; i++) {
          bucketMatches[i] = bucketList.get(i);
        }
        break;
      }
    }

    final Object lockA = bucketMatches[0];
    final Object lockB = bucketMatches[1];
    final Object warm1 = bucketMatches[2];
    final Object warm2 = bucketMatches[3];

    // Pre-populate two reusable SyncData nodes in this bucket before starting threads.
    synchronized (warm1) {
      synchronized (warm2) {
      }
    }

    // Index 2 sits in its own 8-byte TSan shadow granule (separate from IOSIntArray's size_
    // header).
    final int[] sharedProtectedByA = new int[4];
    final CountDownLatch startLatch = new CountDownLatch(3);
    final AtomicInteger phase = new AtomicInteger(0);

    Thread t1 =
        new Thread(() -> {
          try {
            // Pre-initialize per-thread SyncCache and wait until all Thread.start() calls
            // finish so t2.start()/t3.start() monitors on main cannot bridge t1 -> main -> t3.
            synchronized (new Object()) {
            }
            startLatch.countDown();
            startLatch.await();
          } catch (InterruptedException e) {
            throw new AssertionError(e);
          }
          synchronized (lockA) {
            sharedProtectedByA[2] = 42;
          }
          advancePhaseRelaxed(phase, 0, 1);
          // Stay alive until t3 finishes so Thread termination locks cannot add side-channel
          // sync.
          awaitPhaseRelaxed(phase, 3);
        });

    Thread t2 =
        new Thread(() -> {
          try {
            synchronized (new Object()) {
            }
            startLatch.countDown();
            startLatch.await();
          } catch (InterruptedException e) {
            throw new AssertionError(e);
          }
          awaitPhaseRelaxed(phase, 1);
          synchronized (lockB) { // Recycles lockA's SyncData node D1 and holds it
            advancePhaseRelaxed(phase, 1, 2);
            awaitPhaseRelaxed(phase, 3);
          }
        });

    Thread t3 =
        new Thread(() -> {
          try {
            synchronized (new Object()) {
            }
            startLatch.countDown();
            startLatch.await();
          } catch (InterruptedException e) {
            throw new AssertionError(e);
          }
          awaitPhaseRelaxed(phase, 2);
          synchronized (lockA) { // Forced onto second SyncData node D2 (different mutex)
            sharedProtectedByA[2] += 1;
          }
          advancePhaseRelaxed(phase, 2, 3);
        });

    t1.start();
    t2.start();
    t3.start();
    t1.join();
    t2.join();
    t3.join();

    assertEquals(43, sharedProtectedByA[2]);
  }
}
