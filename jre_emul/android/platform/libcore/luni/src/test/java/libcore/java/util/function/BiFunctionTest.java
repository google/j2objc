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

import java.util.function.BiFunction;
import java.util.function.Function;

public class BiFunctionTest extends TestCase {

  public void testAndThen() throws Exception {
    BiFunction<Integer, Integer, Integer> add = (x, y) -> x + y;
    Function<Integer, String> toString = i -> Integer.toString(i);
    assertEquals("4", add.andThen(toString).apply(2, 2));
  }

  public void testAndThen_nullFunction() throws Exception {
    BiFunction<Integer, Integer, Integer> add = (x, y) -> x + y;
    try {
      add.andThen(null);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testAndThen_nullResult() throws Exception {
    BiFunction<Integer, Integer, Integer> toNull = (x, y) -> null;
    Function<Integer, String> assertNull = i -> { assertNull(i); return "ok"; };
    assertEquals("ok", toNull.andThen(assertNull).apply(2, 2));
  }
}
