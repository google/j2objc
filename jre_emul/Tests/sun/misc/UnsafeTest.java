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

package sun.misc;

import junit.framework.TestCase;

/**
 * Tests for sun.misc.Unsafe.
 */
public class UnsafeTest extends TestCase {

  abstract static class AbstractTestClass {}
  static enum TestEnum { A, B, C; }
  static interface TestInterface {}

  public void testAllocInstance() {
    Unsafe unsafe = Unsafe.getUnsafe();

    Object o = unsafe.allocateInstance(UnsafeTest.class);
    assertTrue(o instanceof UnsafeTest);

    try {
      o = unsafe.allocateInstance(AbstractTestClass.class);
      fail("abstract class instantiated");
    } catch (Exception e) {
      // Can't directly catch InstantiationException since it is not
      // declared by Unsafe.allocateInstance().
      assertTrue(e instanceof InstantiationException);
    }

    try {
      o = unsafe.allocateInstance(TestEnum.class);
      fail("enum instantiated");
    } catch (Exception e) {
      // Can't directly catch InstantiationException since it is not
      // declared by Unsafe.allocateInstance().
      assertTrue(e instanceof InstantiationException);
    }

    try {
      o = unsafe.allocateInstance(TestInterface.class);
      fail("interface class instantiated");
    } catch (Exception e) {
      // Can't directly catch InstantiationException since it is not
      // declared by Unsafe.allocateInstance().
      assertTrue(e instanceof InstantiationException);
    }

    try {
      int[] array = new int[0];
      o = unsafe.allocateInstance(array.getClass());
      fail("array class instantiated");
    } catch (Exception e) {
      // Can't directly catch InstantiationException since it is not
      // declared by Unsafe.allocateInstance().
      assertTrue(e instanceof InstantiationException);
    }
  }
}
