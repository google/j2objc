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

import junit.framework.TestCase;

/**
 * Unit tests for {@link NSErrorException}.
 *
 * @author Tom Ball
 */
public class NSErrorExceptionTest extends TestCase {

  private static native Object createNSError(String domain, long code, String description,
      String failingURL, Object underlyingError) /*-[
    NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
    if (description_) {
      [userInfo setObject:description_ forKey:NSLocalizedDescriptionKey];
    }
    if (failingURL) {
      [userInfo setObject:failingURL forKey:NSURLErrorFailingURLStringErrorKey];
    }
    if (underlyingError) {
      [userInfo setObject:underlyingError forKey:NSUnderlyingErrorKey];
    }
    return [NSError errorWithDomain:domain code:code userInfo:userInfo];
  ]-*/;

  public void testBasicError() {
    // NSURLErrorCancelled = -999.
    String description = "cancelled";
    Object nsError = createNSError("NSURLErrorDomain", -999, description, null, null);
    NSErrorException exception = new NSErrorException(nsError);
    assertEquals("incorrect domain", "NSURLErrorDomain", exception.getDomain());
    assertEquals("incorrect code", -999L, exception.getErrorCode());
    assertEquals("incorrect description", description, exception.getMessage());
    assertEquals("incorrect code name", "NSURLErrorCancelled", exception.getErrorName());
    assertNull("unexpected URL", exception.getFailingURLString());
    assertNull("unexpected cause", exception.getCause());
  }

  public void testNextedError() {
    // NSURLErrorSecureConnectionFailed = -1200, NSURLErrorServerCertificateUntrusted = -1202.
    String untrustedDescription = "certificate untrusted";
    String url = "https://0.0.0.0";
    Object untrustedError =
        createNSError("NSURLErrorDomain", -1202, untrustedDescription, url, null);
    String connectFailedDescription = "secure connection failed";
    Object nsError =
        createNSError("NSURLErrorDomain", -1200, connectFailedDescription, url, untrustedError);
    NSErrorException exception = new NSErrorException(nsError);
    assertEquals("incorrect domain", "NSURLErrorDomain", exception.getDomain());
    assertEquals("incorrect code", -1200L, exception.getErrorCode());
    assertEquals("incorrect description", connectFailedDescription, exception.getMessage());
    assertEquals("incorrect code name", "NSURLErrorSecureConnectionFailed",
        exception.getErrorName());
    assertEquals("incorrect URL", url, exception.getFailingURLString());
    NSErrorException cause = (NSErrorException) exception.getCause();
    assertNotNull("no cause", cause);

    assertEquals("incorrect cause domain", "NSURLErrorDomain", cause.getDomain());
    assertEquals("incorrect cause code", -1202L, cause.getErrorCode());
    assertEquals("incorrect cause description", untrustedDescription, cause.getMessage());
    assertEquals("incorrect cause code name", "NSURLErrorServerCertificateUntrusted",
        cause.getErrorName());
    assertEquals("incorrect cause URL", url, cause.getFailingURLString());
    assertNull("unexpected nested cause", cause.getCause());
  }
}
