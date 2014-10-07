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

package libcore.io;

import java.net.UnknownHostException;
import libcore.io.OsConstants;

/**
 * An unchecked exception thrown when the {@link Os} {@code getaddrinfo} or {@code getnameinfo}
 * methods fail. This exception contains the native error value, for comparison against the
 * {@code GAI_} constants in {@link OsConstants}, should sophisticated
 * callers need to adjust their behavior based on the exact failure.
 */
public final class GaiException extends RuntimeException {
    private final String functionName;
    public final int error;

    public GaiException(String functionName, int error) {
        this.functionName = functionName;
        this.error = error;
    }

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
        String description = Libcore.os.gai_strerror(error);
        return functionName + " failed: " + gaiName + " (" + description + ")";
    }

    public UnknownHostException rethrowAsUnknownHostException(String detailMessage) throws UnknownHostException {
        UnknownHostException newException = new UnknownHostException(detailMessage);
        newException.initCause(this);
        throw newException;
    }

    public UnknownHostException rethrowAsUnknownHostException() throws UnknownHostException {
        throw rethrowAsUnknownHostException(getMessage());
    }
}
