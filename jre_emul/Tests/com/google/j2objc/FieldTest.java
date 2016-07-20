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

  int field = -1;
  final long finalField = 5;
  final boolean finalUnsetField;
  static double staticField = Math.PI;
  public static final long CONSTANT_FIELD = 25;

  static class BoundedGenericClass<T extends Runnable> {
    T value;
  }

  public FieldTest() {
    finalUnsetField = true;
  }

  public void testBoundedTypeVariable() throws Exception {
    Field f = BoundedGenericClass.class.getDeclaredField("value");
    assertEquals("java.lang.Runnable", f.getType().getName());
  }

  public void testReadFinalField() throws Exception {
    Field f = FieldTest.class.getDeclaredField("finalField");
    assertEquals(5, f.getLong(this));
  }

  public void testReadUnsetFinalField() throws Exception {
    Field f = FieldTest.class.getDeclaredField("finalUnsetField");
    assertTrue(f.getBoolean(this));
  }

  public void testReadWriteField() throws Exception {
    Field f = FieldTest.class.getDeclaredField("field");
    assertEquals(-1, f.getInt(this));
    f.setInt(this, 42);
    assertEquals(42, f.getInt(this));
  }

  public void testReadWriteStaticField() throws Exception {
    Field f = FieldTest.class.getDeclaredField("staticField");
    assertEquals(Math.PI, f.getDouble(null));
    f.setDouble(this, Math.E);
    assertEquals(Math.E, f.getDouble(null));
  }

  public void testReadConstant() throws Exception {
    Field f = FieldTest.class.getDeclaredField("CONSTANT_FIELD");
    assertEquals(25, f.getLong(null));
  }
}
