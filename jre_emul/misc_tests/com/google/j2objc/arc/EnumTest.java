/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.arc;

import com.google.j2objc.NativeUtil;

import junit.framework.TestCase;

/**
 * Tests enum functionality under ARC compilation.
 *
 * @author Keith Stanger
 */
public class EnumTest extends TestCase {

  // The existence of this enum type serves as a regression test for enum
  // compilation with ARC.
  enum Color {
    RED, GREEN, BLUE
  }

  // All enum values should always have a retain count of 1.
  public void testEnumRetainCount() {
    assertEquals(1, NativeUtil.getRetainCount(Color.RED));
    assertEquals(1, NativeUtil.getRetainCount(Color.GREEN));
    assertEquals(1, NativeUtil.getRetainCount(Color.BLUE));
  }
}
