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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

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

  // fromHex() and equal() tests from Protobuf's ByteStringTest.
  // https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/test/java/com/google/protobuf/ByteStringTest.java
  public void testFromHex_hexString() {
    ByteString byteString;
    byteString = ByteString.fromHex("0a0b0c");
    assertWithMessage("fromHex must contain the expected bytes")
        .that(isArray(byteString.toByteArray(), new byte[] {0x0a, 0x0b, 0x0c}))
        .isTrue();

    byteString = ByteString.fromHex("0A0B0C");
    assertWithMessage("fromHex must contain the expected bytes")
        .that(isArray(byteString.toByteArray(), new byte[] {0x0a, 0x0b, 0x0c}))
        .isTrue();

    byteString = ByteString.fromHex("0a0b0c0d0e0f");
    assertWithMessage("fromHex must contain the expected bytes")
        .that(isArray(byteString.toByteArray(), new byte[] {0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f}))
        .isTrue();
  }

  @SuppressWarnings("AlwaysThrows") // Verifying that indeed these calls do throw.
  public void testFromHex_invalidHexString() {
    try {
      ByteString.fromHex("a0b0c");
      assertWithMessage("Should throw").fail();
    } catch (NumberFormatException expected) {
      assertThat(expected).hasMessageThat().contains("even");
    }

    try {
      ByteString.fromHex("0x0y0z");
      assertWithMessage("Should throw").fail();
    } catch (NumberFormatException expected) {
      assertThat(expected).hasMessageThat().contains("[0-9a-fA-F]");
    }

    try {
      ByteString.fromHex("0૫");
      assertWithMessage("Should throw").fail();
    } catch (NumberFormatException expected) {
      assertThat(expected).hasMessageThat().contains("[0-9a-fA-F]");
    }
  }

  // Compare the entire left array with a subset of the right array.
  private static boolean isArrayRange(byte[] left, byte[] right, int rightOffset, int length) {
    boolean stillEqual = (left.length == length);
    for (int i = 0; (stillEqual && i < length); ++i) {
      stillEqual = (left[i] == right[rightOffset + i]);
    }
    return stillEqual;
  }

  // Returns true only if the given two arrays have identical contents.
  private static boolean isArray(byte[] left, byte[] right) {
    return left.length == right.length && isArrayRange(left, right, 0, left.length);
  }

  public void testEmpty_isEmpty() {
    ByteString byteString = ByteString.empty();
    assertThat(byteString.isEmpty()).isTrue();
    assertWithMessage("ByteString.empty() must return empty byte array")
        .that(isArray(byteString.toByteArray(), new byte[] {}))
        .isTrue();
  }

  public void testEmpty_referenceEquality() {
    assertThat(ByteString.empty()).isSameInstanceAs(ByteString.EMPTY);
    assertThat(ByteString.empty()).isSameInstanceAs(ByteString.empty());
  }
}
