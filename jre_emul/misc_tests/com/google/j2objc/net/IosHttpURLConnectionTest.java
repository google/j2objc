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

package com.google.j2objc.net;

import java.io.IOException;
import javax.net.ssl.SSLException;
import junit.framework.TestCase;

/**
 * Unit tests for {@link IosHttpURLConnection}.
 */
public class IosHttpURLConnectionTest extends TestCase {

  /**
   * This test fails when reflection is stripped if SSLException is not annotated with
   * ReflectionSupport (https://github.com/google/j2objc/issues/1032).
   */
  public void testSecureConnectionException() {
    IOException exception = IosHttpURLConnection.secureConnectionException("Test");
    assertTrue(exception instanceof SSLException);
  }
}
