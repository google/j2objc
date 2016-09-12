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

import java.util.function.Consumer;

public class ConsumerTest extends TestCase {

  public void testAndThen() throws Exception {
    Consumer<StringBuilder> sweet = s -> s.append("sweet");
    Consumer<StringBuilder> dude = s -> s.append("dude");

    StringBuilder sb = new StringBuilder();
    sweet.andThen(dude).accept(sb);
    assertEquals("sweetdude", sb.toString());

    sb.setLength(0);
    dude.andThen(sweet).accept(sb);
    assertEquals("dudesweet", sb.toString());
  }

  public void testAndThen_null() throws Exception {
    Consumer<StringBuilder> one = s -> s.append("one");
    try {
      one.andThen(null);
      fail();
    } catch (NullPointerException expected) {}
  }
}
