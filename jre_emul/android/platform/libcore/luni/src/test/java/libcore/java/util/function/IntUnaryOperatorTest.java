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

import java.util.function.IntUnaryOperator;

public class IntUnaryOperatorTest extends TestCase {

  public void testIdentity() throws Exception {
    assertEquals(1, IntUnaryOperator.identity().applyAsInt(1));
    assertEquals(Integer.MAX_VALUE, IntUnaryOperator.identity().applyAsInt(Integer.MAX_VALUE));
  }

  public void testCompose() throws Exception {
    IntUnaryOperator plusOne = x -> x + 1;
    IntUnaryOperator twice = x -> 2 *x;
    assertEquals(11, plusOne.compose(twice).applyAsInt(5));
  }

  public void testCompose_null() throws Exception {
    IntUnaryOperator plusOne = x -> x + 1;
    try {
      plusOne.compose(null);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testAndThen() throws Exception {
    IntUnaryOperator plusOne = x -> x + 1;
    IntUnaryOperator twice = x -> 2 *x;
    assertEquals(12, plusOne.andThen(twice).applyAsInt(5));
  }

  public void testAndThen_null() throws Exception {
    IntUnaryOperator plusOne = x -> x + 1;
    try {
      plusOne.andThen(null);
      fail();
    } catch (NullPointerException expected) {}
  }
}
