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

package java.security.cert;

import java.security.GeneralSecurityException;

/**
 * The base class for all {@code Certificate} related exceptions.
 */
public class CertificateException extends GeneralSecurityException {

    private static final long serialVersionUID = 3192535253797119798L;

    /**
     * Creates a new {@code CertificateException} with the specified message.
     *
     * @param msg
     *            the detail message for the exception.
     */
    public CertificateException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@code CertificateException}.
     */
    public CertificateException() {
    }

    /**
     * Creates a new {@code CertificateException} with the specified message and
     * cause.
     *
     * @param message
     *            the detail message for the exception.
     * @param cause
     *            the cause.
     */
    public CertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@code CertificateException} with the specified cause.
     *
     * @param cause
     *            the cause
     */
    public CertificateException(Throwable cause) {
        super(cause);
    }
}
