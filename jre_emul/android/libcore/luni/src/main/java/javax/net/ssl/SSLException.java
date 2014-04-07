/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.net.ssl;

import java.io.IOException;

/**
 * The base class for all SSL related exceptions.
 */
public class SSLException extends IOException {
    private static final long serialVersionUID = 4511006460650708967L;

    /**
     * Creates a new {@code SSLException} with the specified reason.
     *
     * @param reason
     *            the reason for the exception.
     */
    public SSLException(String reason) {
        super(reason);
    }

    /**
     * Creates a new {@code SSLException} with the specified message and cause.
     *
     * @param message
     *            the detail message for the exception.
     * @param cause
     *            the cause.
     */
    public SSLException(String message, Throwable cause) {
        super(message);
        super.initCause(cause);
    }

    /**
     * Creates a new {@code SSLException} with the specified cause.
     *
     * @param cause
     *            the cause
     */
    public SSLException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        super.initCause(cause);
    }
}
