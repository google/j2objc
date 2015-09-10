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

import com.google.j2objc.annotations.AutoreleasePool;

import junit.framework.TestCase;

import org.junit.Test;

import static org.junit.Assert.*;

/*-[
#import "IOSReference.h"
]-*/

/**
 * iOS-specific unit test for {@link SoftReference}. The Android and
 * Apache Harmony tests can't be reused because they assume GC behavior.
 * This class uses autorelease pools to manage when references have
 * soft referents.
 *
 * Note: only iOS provides the low-memory notification that triggers
 * soft references to be queued/released. This test therefore fakes a
 * low-memory condition to verify that soft references are freed.
 *
 * @author Tom Ball
 */
public class SoftReferenceTest extends TestCase {
  private SoftReference<?> softRef;

  @Test
  public void testSoftReference() {
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      for (@AutoreleasePool int j = 0; j < 1; j++) {
        // Create a referent inside this autorelease pool.
        Object referent = new Object();
        softRef = new SoftReference<Object>(referent);
        assertSame("softRef get doesn't return referent", referent, softRef.get());

        // Clear referent ref, verify it's still available in the reference.
        referent = null;
        assertNotNull("softRef was cleared", softRef.get());
      }

      // Verify soft reference wasn't cleared.
      assertNotNull("softRef was cleared", softRef.get());
    }

    // Send low memory notification and verify reference was released.
    fakeLowMemoryNotification();
    assertNull("softRef was not cleared", softRef.get());
  }

  @Test
  public void testQueuedSoftReference() {
    final int fakeHash = 123456789;
    ReferenceQueue<? super Object> queue = new ReferenceQueue<Object>();
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      for (@AutoreleasePool int j = 0; j < 1; j++) {
        for (@AutoreleasePool int k = 0; k < 1; k++) {
          Object referent = new Object() {
            @Override
            public int hashCode() {
              return fakeHash;
            }
          };
          softRef = new SoftReference<Object>(referent, queue);
          assertSame("softRef.get doesn't return referent", referent, softRef.get());

          // Remove reference to o, verify it's still available in the reference.
          referent = null;
          assertNotNull("softRef was cleared", softRef.get());
        }
      }

      // Verify soft reference wasn't queued.
      Reference<?> queuedRef = queue.poll();
      assertNull("softRef was queued", queuedRef);

      // Verify soft reference wasn't cleared.
      assertNotNull("softRef was cleared", softRef.get());

      // Send low memory notification and verify reference was queued.
      fakeLowMemoryNotification();
      queuedRef = queue.poll();
      assertNotNull("softRef wasn't queued", queuedRef);
      assertEquals("queuedRef.get doesn't return referent", fakeHash, queuedRef.get().hashCode());
    }

    // Verify soft reference was cleared.
    assertNull("softRef wasn't cleared", softRef.get());
  }

  private static native void fakeLowMemoryNotification() /*-[
    [IOSReference handleMemoryWarning:nil];
  ]-*/;
}
