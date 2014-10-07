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

import com.google.j2objc.security.IosSecureRandomImpl;

import junit.framework.TestCase;

import java.util.Arrays;


/**
 * Unit tests for {@link IosSecureRandomImpl}.
 *
 * @author Tom Ball
 */
public class IosSecureRandomImplTest extends TestCase {

  static class TestEngine extends IosSecureRandomImpl {
    @Override
    public byte[] engineGenerateSeed(int numBytes) {
      // TODO Auto-generated method stub
      return super.engineGenerateSeed(numBytes);
    }

    @Override
    public void engineNextBytes(byte[] bytes) {
      super.engineNextBytes(bytes);
    }
  }

  private void testRandomNextBytes(int size) {
    byte[] bytes = new byte[size];
    int originalHash = Arrays.hashCode(bytes);

    // Try several times, since it's possible for a random sequence to have the same hash,
    // or even be the same, as the original sequence. This test will always be a little
    // flaky, but this improves the odds of passing if the service is working correctly.
    for (int i = 0; i < 5; i++) {
      new TestEngine().engineNextBytes(bytes);
      if (Arrays.hashCode(bytes) != originalHash) {
        return;
      }
    }
    fail("Non-random bytes returned");
  }

  public void testEngineNextBytes() {
    testRandomNextBytes(7);
    testRandomNextBytes(666);
    testRandomNextBytes(2014);
    testRandomNextBytes(882014);
  }

  private void testRandomSeed(int size) {
    byte[] lastSeed = new byte[size];

    for (int i = 0; i < 5; i++) {
      byte[] seed = new TestEngine().engineGenerateSeed(size);
      assertEquals(size, seed.length);
      assertFalse(Arrays.equals(lastSeed, seed));
      lastSeed = seed;
    }
  }

  public void testEngineGenerateSeed() {
    testRandomSeed(3);
    testRandomSeed(747);
    testRandomSeed(1337);
    testRandomSeed(25549);
  }
}
