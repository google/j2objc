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

import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * Command-line tests for java.lang.reflect.Field support.
 *
 * @author Keith Stanger
 */
public class FieldTest extends TestCase {

  static class BoundedGenericClass<T extends Runnable> {
    T value;
  }

  public void testBoundedTypeVariable() throws Exception {
    Field f = BoundedGenericClass.class.getDeclaredField("value");
    assertEquals("java.lang.Runnable", f.getType().getName());
  }
}
