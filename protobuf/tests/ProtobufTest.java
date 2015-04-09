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

import junit.framework.TestCase;

class ProtobufTest extends TestCase {

  // So we don't have to add byte casts within hard-coded arrays.
  protected static byte[] asBytes(int[] ints) {
    byte[] bytes = new byte[ints.length];
    for (int i = 0; i < ints.length; i++) {
      bytes[i] = (byte) ints[i];
    }
    return bytes;
  }

  protected String byteArrayAsString(byte[] bytes) {
    StringBuilder sb = new StringBuilder("{ ");
    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(String.format("0x%02X", bytes[i]));
    }
    sb.append(" }");
    return sb.toString();
  }

  protected void failBytesCheck(byte[] expected, byte[] actual) {
    if (expected.length < 1000) {
      fail("Bytes don't match. Expected " + byteArrayAsString(expected) + " but was "
           + byteArrayAsString(actual));
    } else {
      fail("Bytes don't match.");
    }
  }

  protected void checkBytes(byte[] expected, byte[] actual) {
    if (expected.length != actual.length) {
      failBytesCheck(expected, actual);
    }
    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        failBytesCheck(expected, actual);
      }
    }
  }
}
