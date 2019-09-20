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
import java.util.Iterator;
import junit.framework.TestCase;

/**
 * Tests for com.google.protobuf.ByteString.
 */
public class ByteStringTest extends TestCase {

  public void testByteStringIsEqualAndHashCode() throws Exception {
    ByteString s1 = ByteString.copyFrom("foo".getBytes("UTF-8"));
    ByteString s2 = ByteString.copyFrom("foo".getBytes("UTF-8"));
    ByteString s3 = ByteString.copyFrom("bar".getBytes("UTF-8"));
    assertTrue(s1.equals(s2));
    assertTrue(s2.equals(s1));
    assertEquals(s1.hashCode(), s2.hashCode());
    assertFalse(s1.equals(s3));
    assertFalse(s3.equals(s2));
  }

  public void testToString() throws Exception {
    ByteString s1 = ByteString.copyFrom("foo".getBytes("UTF-8"));
    ByteString s2 = ByteString.copyFrom("你好".getBytes("UTF-8"));
    assertEquals("foo", s1.toString("UTF-8"));
    assertEquals("你好", s2.toString("UTF-8"));
    assertEquals("foo", s1.toStringUtf8());
    assertEquals("你好", s2.toStringUtf8());
  }

  public void testIterator() throws Exception {
    ByteString s1 = ByteString.copyFrom("foo".getBytes("UTF-8"));
    ByteString s2 = ByteString.copyFrom("你好".getBytes("UTF-8"));
    Iterator<Byte> i1 = s1.iterator();
    Iterator<Byte> i2 = s2.iterator();
    for (int i = 0; i < s1.size(); i++) {
      assertEquals((Byte) s1.byteAt(i), i1.next());
    }
    for (int i = 0; i < s2.size(); i++) {
      assertEquals((Byte) s2.byteAt(i), i2.next());
    }
  }

  public void testFastEnumeration() throws Exception {
    byte[] buf1 = "foo".getBytes("UTF-8");
    byte[] buf2 = "你好".getBytes("UTF-8");
    ByteString s1 = ByteString.copyFrom(buf1);
    ByteString s2 = ByteString.copyFrom(buf2);
    int i = 0;
    for (Byte b : s1) {
      assertEquals((Byte) buf1[i++], b);
    }
    i = 0;
    for (Byte b : s2) {
      assertEquals((Byte) buf2[i++], b);
    }
  }
}
