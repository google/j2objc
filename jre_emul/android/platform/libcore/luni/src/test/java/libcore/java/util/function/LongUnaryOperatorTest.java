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

import java.util.function.LongUnaryOperator;

public class LongUnaryOperatorTest extends TestCase {

  public void testIdentity() throws Exception {
    assertEquals(1L, LongUnaryOperator.identity().applyAsLong(1L));
    assertEquals(Long.MAX_VALUE, LongUnaryOperator.identity().applyAsLong(Long.MAX_VALUE));
  }

  public void testCompose() throws Exception {
    LongUnaryOperator plusOne = x -> x + 1L;
    LongUnaryOperator twice = x -> 2L *x;
    assertEquals(11L, plusOne.compose(twice).applyAsLong(5L));
  }

  public void testCompose_null() throws Exception {
    LongUnaryOperator plusOne = x -> x + 1;
    try {
      plusOne.compose(null);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testAndThen() throws Exception {
    LongUnaryOperator plusOne = x -> x + 1L;
    LongUnaryOperator twice = x -> 2L *x;
    assertEquals(12, plusOne.andThen(twice).applyAsLong(5L));
  }

  public void testAndThen_null() throws Exception {
    LongUnaryOperator plusOne = x -> x + 1L;
    try {
      plusOne.andThen(null);
      fail();
    } catch (NullPointerException expected) {}
  }
}
