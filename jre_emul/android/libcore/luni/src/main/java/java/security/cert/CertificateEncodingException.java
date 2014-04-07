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

/**
 * The exception that is thrown when an error occurs while a {@code Certificate}
 * is being encoded.
 */
public class CertificateEncodingException extends CertificateException {

    private static final long serialVersionUID = 6219492851589449162L;

    /**
     * Creates a new {@code CertificateEncodingException} with the specified
     * message.
     *
     * @param msg
     *            The detail message for the exception.
     */
    public CertificateEncodingException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@code CertificateEncodingException}.
     */
    public CertificateEncodingException() {
    }

    /**
     * Creates a new {@code CertificateEncodingException} with the specified
     * message and cause.
     *
     * @param message
     *            the detail message for the exception.
     * @param cause
     *            the cause.
     */
    public CertificateEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@code CertificateEncodingException} with the specified
     * cause.
     *
     * @param cause
     *            the cause.
     */
    public CertificateEncodingException(Throwable cause) {
        super(cause);
    }
}
