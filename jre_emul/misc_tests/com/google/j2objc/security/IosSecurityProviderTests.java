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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * iOS security provider unit test suite.
 */
public class IosSecurityProviderTests extends TestSuite {

  private static final Class<?>[] smallTestClasses = new Class[] {
    IosRSAKeyPairGeneratorTest.class,
    IosRSAKeyTest.class,
    IosRSASignatureTest.class,
    IosSecureRandomImplTest.class,
    IosSHAMessageDigestTest.class
  };

  public static Test suite() {
    return new TestSuite(smallTestClasses);
  }
}
