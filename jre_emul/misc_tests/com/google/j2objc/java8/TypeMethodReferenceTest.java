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
package com.google.j2objc.java8;

import junit.framework.TestCase;

interface H {
  Object copy(int[] i);
}

/**
 * Command-line tests for type method references.
 *
 * @author Seth Kirby
 */
// TODO(kirbs): Find and test more examples of type method references. Most of the examples that
// Eclipse is using are coming from special compiler flags to parse ExpressionMethodReferences
// as TypeMethodReferences. Using these specially compiled constructs breaks us currently, but
// I'm not sure if that is because of the special compilation, or an actual issue on our side.
// Thankfully, this should be a small use case anyway.
public class TypeMethodReferenceTest extends TestCase {
  public TypeMethodReferenceTest() { }

  public void testBasicReferences() throws Exception {
    H h = int[]::clone;
    int[] xs = { 42, 3, 13 };
    int[] ys = (int[]) h.copy(xs);
    for (int i = 0; i < xs.length; i++) {
      assertEquals(xs[i], ys[i]);
    }
  }
}
