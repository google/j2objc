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
import java.util.function.BiPredicate;

public class BiPredicateTest extends TestCase {

  public void testAnd() throws Exception {
    Object arg1 = "one";
    Object arg2 = "two";
    AtomicBoolean alwaysTrueInvoked = new AtomicBoolean(false);
    AtomicBoolean alwaysTrue2Invoked = new AtomicBoolean(false);
    AtomicBoolean alwaysFalseInvoked = new AtomicBoolean(false);
    AtomicBoolean alwaysFalse2Invoked = new AtomicBoolean(false);
    AtomicBoolean[] invocationState = {
        alwaysTrueInvoked, alwaysTrue2Invoked, alwaysFalseInvoked, alwaysFalse2Invoked };

    BiPredicate<Object, Object> alwaysTrue =
        (x, y) -> { alwaysTrueInvoked.set(true); assertSame(arg1, x); assertSame(arg2, y); return true; };
    BiPredicate<Object, Object> alwaysTrue2 =
        (x, y) -> { alwaysTrue2Invoked.set(true); assertSame(arg1, x); assertSame(arg2, y); return true; };
    BiPredicate<Object, Object> alwaysFalse =
        (x, y) -> { alwaysFalseInvoked.set(true); assertSame(arg1, x); assertSame(arg2, y); return false; };
    BiPredicate<Object, Object> alwaysFalse2 =
        (x, y) -> { alwaysFalse2Invoked.set(true); assertSame(arg1, x); assertSame(arg2, y); return false; };

    // true && true
    resetToFalse(invocationState);
    assertTrue(alwaysTrue.and(alwaysTrue2).test(arg1, arg2));
    assertTrue(alwaysTrueInvoked.get() && alwaysTrue2Invoked.get());

    // true && false
    resetToFalse(invocationState);
    assertFalse(alwaysTrue.and(alwaysFalse).test(arg1, arg2));
    assertTrue(alwaysTrueInvoked.get() && alwaysFalseInvoked.get());

    // false && false
    resetToFalse(invocationState);
    assertFalse(alwaysFalse.and(alwaysFalse2).test(arg1, arg2));
    assertTrue(alwaysFalseInvoked.get() && !alwaysFalse2Invoked.get());

    // false && true
    resetToFalse(invocationState);
    assertFalse(alwaysFalse.and(alwaysTrue).test(arg1, arg2));
    assertTrue(alwaysFalseInvoked.get() && !alwaysTrueInvoked.get());
  }

  public void testAnd_null() throws Exception {
    BiPredicate<Object, Object> alwaysTrue = (x, y) -> true;
    try {
      alwaysTrue.and(null);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testNegate() throws Exception {
    Object arg1 = "one";
    Object arg2 = "two";
    BiPredicate<Object, Object> alwaysTrue =
        (x, y) -> { assertSame(arg1, x); assertSame(arg2, y); return true; };
    assertFalse(alwaysTrue.negate().test(arg1, arg2));

    BiPredicate<Object, Object> alwaysFalse =
        (x, y) -> { assertSame(arg1, x); assertSame(arg2, y); return false; };
    assertTrue(alwaysFalse.negate().test(arg1, arg2));
  }

  public void testOr() throws Exception {
    Object arg1 = "one";
    Object arg2 = "two";
    AtomicBoolean alwaysTrueInvoked = new AtomicBoolean(false);
    AtomicBoolean alwaysTrue2Invoked = new AtomicBoolean(false);
    AtomicBoolean alwaysFalseInvoked = new AtomicBoolean(false);
    AtomicBoolean alwaysFalse2Invoked = new AtomicBoolean(false);
    AtomicBoolean[] invocationState = {
        alwaysTrueInvoked, alwaysTrue2Invoked, alwaysFalseInvoked, alwaysFalse2Invoked };

    BiPredicate<Object, Object> alwaysTrue =
        (x, y) -> { alwaysTrueInvoked.set(true); assertSame(arg1, x); assertSame(arg2, y); return true; };
    BiPredicate<Object, Object> alwaysTrue2 =
        (x, y) -> { alwaysTrue2Invoked.set(true); assertSame(arg1, x); assertSame(arg2, y); return true; };
    BiPredicate<Object, Object> alwaysFalse =
        (x, y) -> { alwaysFalseInvoked.set(true); assertSame(arg1, x); assertSame(arg2, y); return false; };
    BiPredicate<Object, Object> alwaysFalse2 =
        (x, y) -> { alwaysFalse2Invoked.set(true); assertSame(arg1, x); assertSame(arg2, y); return false; };

    // true || true
    resetToFalse(invocationState);
    assertTrue(alwaysTrue.or(alwaysTrue2).test(arg1, arg2));
    assertTrue(alwaysTrueInvoked.get() && !alwaysTrue2Invoked.get());

    // true || false
    resetToFalse(invocationState);
    assertTrue(alwaysTrue.or(alwaysFalse).test(arg1, arg2));
    assertTrue(alwaysTrueInvoked.get() && !alwaysFalseInvoked.get());

    // false || false
    resetToFalse(invocationState);
    assertFalse(alwaysFalse.or(alwaysFalse2).test(arg1, arg2));
    assertTrue(alwaysFalseInvoked.get() && alwaysFalse2Invoked.get());

    // false || true
    resetToFalse(invocationState);
    assertTrue(alwaysFalse.or(alwaysTrue).test(arg1, arg2));
    assertTrue(alwaysFalseInvoked.get() && alwaysTrueInvoked.get());
  }

  public void testOr_null() throws Exception {
    BiPredicate<Object, Object> alwaysTrue = (x, y) -> true;
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
