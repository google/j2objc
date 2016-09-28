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

  // Issue #797: cloned digests returned different hash values.
  public void testDigestCloning() throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    String hash1 = hash("foo", (MessageDigest) digest.clone());
    String hash2 = hash("foo", (MessageDigest) digest.clone());
    assertEquals(hash1, hash2);
  }

  private String hash(String input, MessageDigest digest) throws Exception {
    byte[] bytes = input.getBytes("UTF-8");
    digest.update(bytes);
    byte[] hash = digest.digest();
    StringBuilder sb = new StringBuilder(2 * bytes.length);
    for (byte b : hash) {
      sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
    }
    return sb.toString();
  }

  private static final char[] hexDigits = "0123456789abcdef".toCharArray();
}
