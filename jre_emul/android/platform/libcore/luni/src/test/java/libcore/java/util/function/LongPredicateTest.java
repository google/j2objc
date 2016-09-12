/*
 * Copyright (C) 2016 The Android Open Source Project
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

package libcore.java.util.function;

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongPredicate;

public class LongPredicateTest extends TestCase {

  public void testAnd() throws Exception {
    AtomicBoolean alwaysTrueInvoked = new AtomicBoolean(false);
    AtomicBoolean alwaysTrue2Invoked = new AtomicBoolean(false);
    AtomicBoolean alwaysFalseInvoked = new AtomicBoolean(false);
    AtomicBoolean alwaysFalse2Invoked = new AtomicBoolean(false);
    AtomicBoolean[] invocationState = {
        alwaysTrueInvoked, alwaysTrue2Invoked, alwaysFalseInvoked, alwaysFalse2Invoked };

    LongPredicate alwaysTrue = x -> { alwaysTrueInvoked.set(true); return true; };
    LongPredicate alwaysTrue2 = x -> { alwaysTrue2Invoked.set(true); return true; };
    LongPredicate alwaysFalse = x -> { alwaysFalseInvoked.set(true); return false; };
    LongPredicate alwaysFalse2 = x -> { alwaysFalse2Invoked.set(true); return false; };

    // true && true
    resetToFalse(invocationState);
    assertTrue(alwaysTrue.and(alwaysTrue2).test(1L));
    assertTrue(alwaysTrueInvoked.get() && alwaysTrue2Invoked.get());

    // true && false
    resetToFalse(invocationState);
    assertFalse(alwaysTrue.and(alwaysFalse).test(1L));
    assertTrue(alwaysTrueInvoked.get() && alwaysFalseInvoked.get());

    // false && false
    resetToFalse(invocationState);
    assertFalse(alwaysFalse.and(alwaysFalse2).test(1L));
    assertTrue(alwaysFalseInvoked.get() && !alwaysFalse2Invoked.get());

    // false && true
    resetToFalse(invocationState);
    assertFalse(alwaysFalse.and(alwaysTrue).test(1L));
    assertTrue(alwaysFalseInvoked.get() && !alwaysTrueInvoked.get());
  }

  public void testAnd_null() throws Exception {
    LongPredicate alwaysTrue = x -> true;
    try {
      alwaysTrue.and(null);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testNegate() throws Exception {
    long arg = 5L;
    LongPredicate alwaysTrue = x -> { assertEquals(x, arg); return true; };
    assertFalse(alwaysTrue.negate().test(arg));

    LongPredicate alwaysFalse = x -> { assertEquals(x, arg); return false; };
    assertTrue(alwaysFalse.negate().test(arg));
  }

  public void testOr() throws Exception {
    AtomicBoolean alwaysTrueInvoked = new AtomicBoolean(false);
    AtomicBoolean alwaysTrue2Invoked = new AtomicBoolean(false);
    AtomicBoolean alwaysFalseInvoked = new AtomicBoolean(false);
    AtomicBoolean alwaysFalse2Invoked = new AtomicBoolean(false);
    AtomicBoolean[] invocationState = {
        alwaysTrueInvoked, alwaysTrue2Invoked, alwaysFalseInvoked, alwaysFalse2Invoked };

    LongPredicate alwaysTrue = x -> { alwaysTrueInvoked.set(true); return true; };
    LongPredicate alwaysTrue2 = x -> { alwaysTrue2Invoked.set(true); return true; };
    LongPredicate alwaysFalse = x -> { alwaysFalseInvoked.set(true); return false; };
    LongPredicate alwaysFalse2 = x -> { alwaysFalse2Invoked.set(true); return false; };

    // true || true
    resetToFalse(invocationState);
    assertTrue(alwaysTrue.or(alwaysTrue2).test(1L));
    assertTrue(alwaysTrueInvoked.get() && !alwaysTrue2Invoked.get());

    // true || false
    resetToFalse(invocationState);
    assertTrue(alwaysTrue.or(alwaysFalse).test(1L));
    assertTrue(alwaysTrueInvoked.get() && !alwaysFalseInvoked.get());

    // false || false
    resetToFalse(invocationState);
    assertFalse(alwaysFalse.or(alwaysFalse2).test(1L));
    assertTrue(alwaysFalseInvoked.get() && alwaysFalse2Invoked.get());

    // false || true
    resetToFalse(invocationState);
    assertTrue(alwaysFalse.or(alwaysTrue).test(1L));
    assertTrue(alwaysFalseInvoked.get() && alwaysTrueInvoked.get());
  }

  public void testOr_null() throws Exception {
    LongPredicate alwaysTrue = x -> true;
    try {
      alwaysTrue.or(null);
      fail();
    } catch (NullPointerException expected) {}
  }

  private static void resetToFalse(AtomicBoolean... toResets) {
    for (AtomicBoolean toReset : toResets) {
      toReset.set(false);
    }
  }
}
