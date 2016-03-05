/*
 * Copyright (C) 2013 The Android Open Source Project
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

package libcore.java.util;

import java.util.Arrays;
import java.util.Objects;

public class ObjectsTest extends junit.framework.TestCase {
  public static final class Hello {
    public String toString() { return "hello"; }
  }

  public void test_compare() throws Exception {
    assertEquals(0, Objects.compare(null, null, String.CASE_INSENSITIVE_ORDER));
    assertEquals(0, Objects.compare("a", "A", String.CASE_INSENSITIVE_ORDER));
    assertEquals(-1, Objects.compare("a", "b", String.CASE_INSENSITIVE_ORDER));
    assertEquals(1, Objects.compare("b", "a", String.CASE_INSENSITIVE_ORDER));
  }

  public void test_deepEquals() throws Exception {
    int[] xs = new int[3];
    int[] ys = new int[4];
    int[] zs = new int[3];
    String[] o1 = new String[] { "hello" };
    String[] o2 = new String[] { "world" };
    String[] o3 = new String[] { "hello" };
    assertTrue(Objects.deepEquals(null, null));
    assertFalse(Objects.deepEquals(xs, null));
    assertFalse(Objects.deepEquals(null, xs));
    assertTrue(Objects.deepEquals(xs, xs));
    assertTrue(Objects.deepEquals(xs, zs));
    assertFalse(Objects.deepEquals(xs, ys));
    assertTrue(Objects.deepEquals(o1, o1));
    assertTrue(Objects.deepEquals(o1, o3));
    assertFalse(Objects.deepEquals(o1, o2));
    assertTrue(Objects.deepEquals("hello", "hello"));
    assertFalse(Objects.deepEquals("hello", "world"));
  }

  public void test_equals() throws Exception {
    Hello h1 = new Hello();
    Hello h2 = new Hello();
    assertTrue(Objects.equals(null, null));
    assertFalse(Objects.equals(h1, null));
    assertFalse(Objects.equals(null, h1));
    assertFalse(Objects.equals(h1, h2));
    assertTrue(Objects.equals(h1, h1));
  }

  public void test_hash() throws Exception {
    assertEquals(Arrays.hashCode(new Object[0]), Objects.hash());
    assertEquals(31, Objects.hash((Object) null));
    assertEquals(0, Objects.hash((Object[]) null));
    // TODO(kstanger): String.hashCode() is not consistent between Java and ObjC.
    //assertEquals(-1107615551, Objects.hash("hello", "world"));
    //assertEquals(23656287, Objects.hash("hello", "world", null));
  }

  public void test_hashCode() throws Exception {
    Hello h = new Hello();
    assertEquals(h.hashCode(), Objects.hashCode(h));
    assertEquals(0, Objects.hashCode(null));
  }

  public void test_requireNonNull_T() throws Exception {
    Hello h = new Hello();
    assertEquals(h, Objects.requireNonNull(h));
    try {
      Objects.requireNonNull(null);
      fail();
    } catch (NullPointerException expected) {
      assertEquals(null, expected.getMessage());
    }
  }

  public void test_requireNonNull_T_String() throws Exception {
    Hello h = new Hello();
    assertEquals(h, Objects.requireNonNull(h, "test"));
    try {
      Objects.requireNonNull(null, "message");
      fail();
    } catch (NullPointerException expected) {
      assertEquals("message", expected.getMessage());
    }
    try {
      Objects.requireNonNull(null, null);
      fail();
    } catch (NullPointerException expected) {
      assertEquals(null, expected.getMessage());
    }
  }

  public void test_toString_Object() throws Exception {
    assertEquals("hello", Objects.toString(new Hello()));
    assertEquals("null", Objects.toString(null));
  }

  public void test_toString_Object_String() throws Exception {
    assertEquals("hello", Objects.toString(new Hello(), "world"));
    assertEquals("world", Objects.toString(null, "world"));
    assertEquals(null, Objects.toString(null, null));
  }
}
