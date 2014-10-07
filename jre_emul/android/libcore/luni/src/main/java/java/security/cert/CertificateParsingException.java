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
 * The exception that is thrown when a {@code Certificate} can not be parsed.
 */
public class CertificateParsingException extends CertificateException {

    private static final long serialVersionUID = -7989222416793322029L;

    /**
     * Creates a new {@code CertificateParsingException} with the specified
     * message.
     *
     * @param msg
     *            the detail message for the exception.
     */
    public CertificateParsingException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@code CertificateParsingException}.
     */
    public CertificateParsingException() {
    }

    /**
     * Creates a new {@code CertificateParsingException} with the specified
     * message and cause.
     *
     * @param message
     *            the detail message for the exception.
     * @param cause
     *            the exception's source.
     */
    public CertificateParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@code CertificateParsingException} with the specified
     * cause.
     *
     * @param cause
     *            the exception's source.
     */
    public CertificateParsingException(Throwable cause) {
        super(cause);
    }
}
