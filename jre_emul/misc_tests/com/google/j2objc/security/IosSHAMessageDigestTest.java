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

package com.google.j2objc.security;

import java.security.MessageDigest;
import junit.framework.TestCase;


/**
 * Unit tests for {@link IosSHAMessageDigest}.
 *
 * @author Tom Ball
 */
public class IosSHAMessageDigestTest extends TestCase {

  // Source: https://en.wikipedia.org/wiki/SHA-2#Test_vectors
  private static final String SHA256_HASH_EMPTY_STRING
      = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
  private static final String SHA384_HASH_EMPTY_STRING
      = "38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da"
          + "274edebfe76f65fbd51ad2f14898b95b";
  private static final String SHA512_HASH_EMPTY_STRING
      = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce"
          + "47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";

  // Issue #797: cloned digests returned different hash values.
  public void testDigestCloning() throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    byte[] hash1 = hash("foo", (MessageDigest) digest.clone());
    byte[] hash2 = hash("foo", (MessageDigest) digest.clone());
    assertEquals(hexString(hash1), hexString(hash2));
  }

  // Issue #929: verify MessageDigest.digest() methods reset the digest's state.
  private void digestResetTest(MessageDigest md, String emptyVector) throws Exception {
    // Initial state.
    assertEquals(emptyVector, hexString(md.digest()));

    // Update with empty array.
    md.update(new byte[0]);
    assertEquals(emptyVector, hexString(md.digest()));

    // Create a non-empty digest, verify it has a different hash.
    md.update(new byte[64]);
    byte[] tmp = md.digest();
    assertFalse(emptyVector.equals(hexString(tmp)));

    // Verify that digest(array, offset, length) method also resets the digest.
    int len = md.digest(tmp, 0, tmp.length);
    assertEquals(emptyVector, hexString(md.digest()));

    // Verify correct digest length was returned.
    assertEquals(md.getDigestLength(), len);
  }

  public void testSHA256DigestReset() throws Exception {
    digestResetTest(MessageDigest.getInstance("SHA-256"), SHA256_HASH_EMPTY_STRING);
  }

  public void testSHA384DigestReset() throws Exception {
    digestResetTest(MessageDigest.getInstance("SHA-384"), SHA384_HASH_EMPTY_STRING);
  }

  public void testSHA512DigestReset() throws Exception {
    digestResetTest(MessageDigest.getInstance("SHA-512"), SHA512_HASH_EMPTY_STRING);
  }

  private byte[] hash(String input, MessageDigest digest) throws Exception {
    byte[] bytes = input.getBytes("UTF-8");
    digest.update(bytes);
    return digest.digest();
  }

  private String hexString(byte[] hash) {
    StringBuilder sb = new StringBuilder();
    for (byte b : hash) {
      sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
    }
    return sb.toString();
  }

  private static final char[] hexDigits = "0123456789abcdef".toCharArray();
}
