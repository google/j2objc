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

package j2objc.java.lang.ref;

import static org.junit.Assert.*;

import com.google.j2objc.annotations.AutoreleasePool;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

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
    final int[] finalizeCount = { 0 };
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      // Create a referent inside this autorelease pool.
      Object referent = new Object() {
        public void finalize() {
          finalizeCount[0]++;
        }
      };
      weakRef = new WeakReference<Object>(referent);
      assertSame("weakRef get doesn't return referent", referent, weakRef.get());

      // Clear referent ref, verify it's still available in the reference.
      referent = null;
      assertNotNull("weakRef cleared too soon", weakRef.get());
      assertEquals("referent dealloc'ed too soon", 0, finalizeCount[0]);
    }

    // Verify weak reference was cleared.
    assertNull("weakRef wasn't cleared", weakRef.get());
    assertEquals("referent wasn't dealloc'ed", 1, finalizeCount[0]);
  }

  @Test
  public void testQueuedWeakReference() {
    final boolean[] dealloced = { false };
    ReferenceQueue<? super Object> queue = new ReferenceQueue<Object>();
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      Object referent = new Object() {
        public void finalize() {
          dealloced[0] = true;
        }
      };
      weakRef = new WeakReference<Object>(referent, queue);
      assertSame("weakRef.get doesn't return referent", referent, weakRef.get());

      // Remove reference to o, verify it's still available in the reference.
      referent = null;
      assertNotNull("weakRef cleared too soon", weakRef.get());
      assertFalse("referent dealloc'ed too soon", dealloced[0]);
    }

    // Verify weak reference was queued.
    Reference<?> queuedRef = queue.poll();
    assertNotNull("weakRef wasn't queued", queuedRef);

    // Verify weak reference was cleared.
    assertNull("weakRef wasn't cleared", weakRef.get());
    assertTrue("referent wasn't dealloc'ed", dealloced[0]);
  }

  @Test
  public void testGetClassMethod() {
    Object obj = new Object();
    new WeakReference<>(obj);
    assertSame(Object.class, obj.getClass());
  }

  @Test
  public void testWeakReferenceMap() {
    // weak_ref_maps in IOSReference should not call referent's hashCode().
    final int[] hashCodeCount = { 0 };
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      Object referent = new Object() {
        @Override
        public int hashCode() {
          hashCodeCount[0]++;
          return super.hashCode();
        }
      };

      weakRef = new WeakReference<Object>(referent);
      referent = null;
    }
    // Verify that referent's hashCode() was not called
    assertEquals("referent's hashCode() was called", 0, hashCodeCount[0]);
  }

  @Ignore("b/433938552")
  public void ignore_testConstantStringWeakReference() {
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      weakRef = new WeakReference<>("constant-string");
    }

    assertNotNull("weakRef with constant string was cleared", weakRef.get());
  }

  @Ignore("b/433938552")
  public void ignore_testNonConstantStringWeakReference() {
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      weakRef = new WeakReference<>(new String("non-constant-string"));
    }

    assertNull("weakRef with non-constant string wasn't cleared", weakRef.get());
  }
}
