/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

import junit.framework.TestCase;

/**
 * Tests for NSObject+JavaObject
 *
 * @author Tim Gao
 */
public class ObjectTest extends TestCase {

  public void testObjectWaitInNestedSynchronizationBlock() {
    final long shortDelay = 1000; // 1s
    final long longDelay = 10 * shortDelay; // 10s
    Object obj = new Object();

    Thread t = new Thread() {
        public final void run() {
          synchronized (obj) {
            synchronized (obj) { 
              try {
                // Wait for 10s
                obj.wait(longDelay);
              } catch (Exception e) {
                ;
              }
            }
          }
        }};

    long startTime = System.currentTimeMillis();
    t.start();
    try {
      // Sleep for 1s
      Thread.sleep(shortDelay);
    } catch (Exception e) {
      // fall-through
    }

    // interrupt() will attempt to synchronize on the thread's blocker, which is "obj" in this case.
    // If obj.wait() fails to release obj's mutex, t.interrupt() will be blocked until wait() times
    // out.
    t.interrupt();
    long endTime = System.currentTimeMillis();
    assertTrue((endTime - startTime) / 1000 < 2);
  }
}
