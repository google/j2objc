/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.system;

import java.net.UnknownHostException;
import libcore.io.NetworkOs;
import libcore.io.OsConstants;

/**
 * An unchecked exception thrown when {@code getaddrinfo} or {@code getnameinfo} fails.
 * This exception contains the native {@link #error} value, should sophisticated
 * callers need to adjust their behavior based on the exact failure.
 *
 * @hide
 */
public final class GaiException extends RuntimeException {
  private final String functionName;

  /**
   * The native error value, for comparison with the {@code GAI_} constants in {@link OsConstants}.
   */
  public final int error;

  /**
   * Constructs an instance with the given function name and error value.
   */
  public GaiException(String functionName, int error) {
    this.functionName = functionName;
    this.error = error;
  }

  /**
   * Constructs an instance with the given function name, error value, and cause.
   */
  public GaiException(String functionName, int error, Throwable cause) {
    super(cause);
    this.functionName = functionName;
    this.error = error;
  }

  /**
   * Converts the stashed function name and error value to a human-readable string.
   * We do this here rather than in the constructor so that callers only pay for
   * this if they need it.
   */
  @Override public String getMessage() {
    String gaiName = OsConstants.gaiName(error);
    if (gaiName == null) {
      gaiName = "GAI_ error " + error;
    }
    String description = NetworkOs.gai_strerror(error);
    return functionName + " failed: " + gaiName + " (" + description + ")";
  }

  /**
   * @hide - internal use only.
   */
  public UnknownHostException rethrowAsUnknownHostException(String detailMessage) throws UnknownHostException {
    UnknownHostException newException = new UnknownHostException(detailMessage);
    newException.initCause(this);
    throw newException;
  }

  /**
   * @hide - internal use only.
   */
  public UnknownHostException rethrowAsUnknownHostException() throws UnknownHostException {
    throw rethrowAsUnknownHostException(getMessage());
  }
}
