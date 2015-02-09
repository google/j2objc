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

/**
 * iOS-specific unit tests for {@link WeakReference}. The Android and
 * Apache Harmony tests can't be reused because they assume GC behavior.
 * This class uses autorelease pools to manage when references have
 * weak referents.
 *
 * @author Tom Ball
 */
public class WeakReferenceTest extends TestCase {
  private WeakReference<?> weakRef;

  @Test
  public void testWeakReference() {
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      // Create a referent inside this autorelease pool.
      Object referent = new Object();
      weakRef = new WeakReference<Object>(referent);
      assertSame("weakRef get doesn't return referent", referent, weakRef.get());

      // Clear referent ref, verify it's still available in the reference.
      referent = null;
      assertNotNull("weakRef cleared too soon", weakRef.get());
    }

    // Verify weak reference was cleared.
    assertNull("weakRef wasn't cleared", weakRef.get());
  }

  @Test
  public void testQueuedWeakReference() {
    final int fakeHash = 123456789;
    ReferenceQueue<? super Object> queue = new ReferenceQueue<Object>();
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      for (@AutoreleasePool int j = 0; j < 1; j++) {
        Object referent = new Object() {
          @Override
          public int hashCode() {
            return fakeHash;
          }
        };
        weakRef = new WeakReference<Object>(referent, queue);
        assertSame("weakRef.get doesn't return referent", referent, weakRef.get());

        // Remove reference to o, verify it's still available in the reference.
        referent = null;
        assertNotNull("weakRef cleared too soon", weakRef.get());
      }

      // Verify weak reference was queued.
      Reference<?> queuedRef = queue.poll();
      assertNotNull("weakRef wasn't queued", queuedRef);
      assertEquals("queuedRef.get doesn't return referent", fakeHash, weakRef.get().hashCode());
    }

    // Verify weak reference was cleared.
     assertNull("weakRef wasn't cleared", weakRef.get());
  }

  @Test
  public void testGetClassMethod() {
    Object obj = new Object();
    new WeakReference(obj);
    assertSame(Object.class, obj.getClass());
  }
}
