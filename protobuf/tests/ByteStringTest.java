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

import com.google.protobuf.ByteString;

import junit.framework.TestCase;

/**
 * Tests for com.google.protobuf.ByteString.
 */
public class ByteStringTest extends TestCase {

  public void testByteStringIsEqualAndHashCode() throws Exception {
    ByteString s1 = ByteString.copyFrom("foo".getBytes());
    ByteString s2 = ByteString.copyFrom("foo".getBytes());
    ByteString s3 = ByteString.copyFrom("bar".getBytes());
    assertTrue(s1.equals(s2));
    assertTrue(s2.equals(s1));
    assertEquals(s1.hashCode(), s2.hashCode());
    assertFalse(s1.equals(s3));
    assertFalse(s3.equals(s2));
  }

  public void testToString() throws Exception {
    ByteString s1 = ByteString.copyFrom("foo".getBytes());
    ByteString s2 = ByteString.copyFrom("你好".getBytes("UTF-8"));
    assertEquals("foo", s1.toString("UTF-8"));
    assertEquals("你好", s2.toString("UTF-8"));
    assertEquals("foo", s1.toStringUtf8());
    assertEquals("你好", s2.toStringUtf8());
  }
}
