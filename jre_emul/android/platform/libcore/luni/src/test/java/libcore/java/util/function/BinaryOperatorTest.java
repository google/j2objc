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

import java.util.Comparator;
import java.util.function.BinaryOperator;

public class BinaryOperatorTest extends TestCase {

  public void testMinBy() throws Exception {
    Comparator<String> stringComparator = String::compareTo;
    assertEquals("a", BinaryOperator.minBy(stringComparator).apply("a", "b"));
  }

  public void testMinBy_null() throws Exception {
    try {
      BinaryOperator.minBy(null);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testMaxBy() throws Exception {
    Comparator<String> stringComparator = String::compareTo;
    assertEquals("b", BinaryOperator.maxBy(stringComparator).apply("a", "b"));
  }

  public void testMaxBy_null() throws Exception {
    try {
      BinaryOperator.maxBy(null);
      fail();
    } catch (NullPointerException expected) {}
  }
}
