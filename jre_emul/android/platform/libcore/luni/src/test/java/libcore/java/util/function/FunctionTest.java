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

import java.util.function.Function;

public class FunctionTest extends TestCase {

  public void testIdentity() throws Exception {
    Object o = new Object();
    assertSame(o, Function.identity().apply(o));
    assertNull(Function.identity().apply(null));
  }

  public void testCompose() throws Exception {
    Function<Double, Double> plusOne = x -> x + 1.0d;
    Function<Double, Double> twice = x -> 2 *x;
    assertEquals(11.0d, plusOne.compose(twice).apply(5.0d));
  }

  public void testCompose_null() throws Exception {
    Function<Double, Double> plusOne = x -> x + 1.0d;
    try {
      plusOne.compose(null);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testAndThen() throws Exception {
    Function<Double, Double> plusOne = x -> x + 1.0d;
    Function<Double, Double> twice = x -> 2 *x;
    assertEquals(12.0d, plusOne.andThen(twice).apply(5.0d));
  }

  public void testAndThen_null() throws Exception {
    Function<Double, Double> plusOne = x -> x + 1.0d;
    try {
      plusOne.andThen(null);
      fail();
    } catch (NullPointerException expected) {}
  }
}
