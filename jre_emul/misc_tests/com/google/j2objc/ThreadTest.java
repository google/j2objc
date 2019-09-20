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

import static java.lang.Thread.State;

import junit.framework.TestCase;

/**
 * Tests for java.lang.Thread.
 *
 * @author Tim Gao
 */
public class ThreadTest extends TestCase {

  public void testThreadStateWaiting() {
    final long shortDelay = 100; // 0.1s
    Object obj = new Object();

    Thread t = new Thread() {
        public final void run() {
          synchronized (obj) {
            try {
              obj.wait();
            } catch (Exception e) {
              assertTrue(Thread.currentThread().getState() == State.RUNNABLE);
            }
          }
        }};

    t.start();
    try {
      Thread.sleep(shortDelay);
    } catch (Exception e) {
      // fall-through
    }
    try {
      t.interrupt();
      t.join();
    } catch (Exception e) {
      // fall-through
    }
  }

  public void testThreadStateTimedWaiting() {
    final long shortDelay = 100; // 0.1s
    final long longDelay = 5000; // 5s
    Object obj = new Object();

    Thread t = new Thread() {
        public final void run() {
          synchronized (obj) {
            try {
              // Wait for 5s
              obj.wait(longDelay);
            } catch (Exception e) {
              assertTrue(Thread.currentThread().getState() == State.RUNNABLE);
            }
          }
        }};

    t.start();
    try {
      // Sleep for 0.1s
      Thread.sleep(shortDelay);
    } catch (Exception e) {
      // fall-through
    }
    try {
      t.interrupt();
      t.join();
    } catch (Exception e) {
      // fall-through
    }
  }

  public void testThreadStateBlocked() {
    Object obj = new Object();

    Thread s = new Thread() {
      public void run() {
        Object lock = new Object();
        synchronized (obj) {
          synchronized (lock) {
            try {
              lock.wait();
            } catch (Exception e) {
              assertTrue(Thread.currentThread().getState() == State.RUNNABLE);
            }
          }
        }
      }};
    Thread t = new Thread() {
      public void run() {
        s.start();

        try {
          Thread.currentThread().sleep(100);
        } catch (Exception e) {
          // fall-through
        }

        // At this point, thread s has acquired the mutex of obj. So thread t will be blocked until
        // thread s is interrupted.
        synchronized (obj) {
          assertTrue(Thread.currentThread().getState() == State.RUNNABLE);
        }
      }};
  
    t.start();
    try {
      Thread.currentThread().sleep(200);
    } catch (Exception e) {
      // fall-through
    }
    try {
      s.interrupt();
      t.interrupt();
      s.join();
      t.join();
    } catch (Exception e) {
      // fall-through
    }
  }
}
