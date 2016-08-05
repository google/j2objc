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

package com.google.j2objc;

import com.google.j2objc.annotations.AutoreleasePool;

import junit.framework.TestCase;

/**
 * Various tests that ref counting is behaving correctly.
 *
 * @author Keith Stanger
 */
public class MemoryTest extends TestCase {

  private static Object staticObjectField;

  private static class ConstructionException extends RuntimeException {}

  private static class ConstructorThrowsType {

    static int dealloced = 0;

    ConstructorThrowsType() {
      throw new ConstructionException();
    }

    protected void finalize() {
      dealloced++;
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ConstructorThrowsType.dealloced = 0;
  }

  public void testReleasingConstructorThrows() {
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      try {
        new ConstructorThrowsType();
        fail("Constructor was expected to throw.");
      } catch (ConstructionException t) {
        // Expected
      }
    }
    assertEquals(1, ConstructorThrowsType.dealloced);
  }

  public void testRetainingConstructorThrows() {
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      try {
        // Assigning directly to a field will translate to the retaining constructor.
        staticObjectField = new ConstructorThrowsType();
        fail("Constructor was expected to throw.");
      } catch (ConstructionException t) {
        // Expected
      }
    }
    assertEquals(1, ConstructorThrowsType.dealloced);
  }
}
