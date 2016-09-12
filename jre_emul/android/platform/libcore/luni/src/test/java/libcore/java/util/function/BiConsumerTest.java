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

import java.util.function.BiConsumer;

public class BiConsumerTest extends TestCase {

  public void testAndThen() throws Exception {
    BiConsumer<String, StringBuilder> one = (s, t) -> t.append("one").append(s);
    BiConsumer<String, StringBuilder> two = (s, t) -> t.append("two").append(s);

    StringBuilder sb = new StringBuilder();
    one.andThen(two).accept("z", sb);
    assertEquals("oneztwoz", sb.toString());

    sb.setLength(0);
    two.andThen(one).accept("z", sb);
    assertEquals("twozonez", sb.toString());
  }

  public void testAndThen_null() throws Exception {
    BiConsumer<String, StringBuilder> one = (s, t) -> t.append("one").append(s);
    try {
      one.andThen(null);
      fail();
    } catch (NullPointerException expected) {}
  }
}
