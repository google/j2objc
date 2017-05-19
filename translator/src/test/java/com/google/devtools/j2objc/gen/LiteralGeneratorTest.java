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

package com.google.devtools.j2objc.gen;

import junit.framework.TestCase;

/**
 * Tests for {@link LiteralGenerator}.
 *
 * @author Tom Ball, Keith Stanger
 */
public class LiteralGeneratorTest extends TestCase {

  public void testBuildStringFromChars() {
    String s = "a\uffffz";
    String result = LiteralGenerator.buildStringFromChars(s);
    assertEquals(
        "[NSString stringWithCharacters:(jchar[]) "
            + "{ (int) 0x61, (int) 0xffff, (int) 0x7a } length:3]",
        result);
  }
}
