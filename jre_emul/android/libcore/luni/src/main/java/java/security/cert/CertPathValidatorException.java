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
 * The exception that is thrown when a certification path (or certificate chain)
 * cannot be validated.
 * <p>
 * A {@code CertPathValidatorException} may optionally include the certification
 * path instance that failed the validation and the index of the failed
 * certificate.
 */
public class CertPathValidatorException extends GeneralSecurityException {

    private static final long serialVersionUID = -3083180014971893139L;

    /**
     * the certification path.
     */
    private CertPath certPath;

    /**
     * the index of the certificate.
     */
    private int index = -1;

    /**
     * Creates a new {@code CertPathValidatorException} with the specified
     * message , cause, certification path and certificate index in the
     * certification path.
     *
     * @param msg
     *            the detail message for this exception.
     * @param cause
     *            the cause.
     * @param certPath
     *            the certification path that failed the validation.
     * @param index
     *            the index of the failed certificate.
     * @throws IllegalArgumentException
     *             if {@code certPath} is {@code null} and index is not {@code
     *             -1}.
     * @throws IndexOutOfBoundsException
     *             if {@code certPath} is not {@code null} and index is not
     *             referencing an certificate in the certification path.
     */
    public CertPathValidatorException(String msg, Throwable cause,
            CertPath certPath, int index) {
        super(msg, cause);
        // check certPath and index parameters
        if ((certPath == null) && (index != -1)) {
            throw new IllegalArgumentException("Index should be -1 when CertPath is null");
        }
        if ((certPath != null) && ((index < -1) || (index >= certPath.getCertificates().size()))) {
            throw new IndexOutOfBoundsException();
        }
        this.certPath = certPath;
        this.index = index;
    }

    /**
     * Creates a new {@code CertPathValidatorException} with the specified
     * message and cause.
     *
     * @param msg
     *            the detail message for this exception.
     * @param cause
     *            the cause why the path could not be validated.
     */
    public CertPathValidatorException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates a new {@code CertPathValidatorException} with the specified
     * cause.
     *
     * @param cause
     *            the cause why the path could not be validated.
     */
    public CertPathValidatorException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new {@code CertPathValidatorException} with the specified
     * message.
     *
     * @param msg
     *            the detail message for this exception.
     */
    public CertPathValidatorException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@code CertPathValidatorException}.
     */
    public CertPathValidatorException() {
    }

    /**
     * Returns the certification path that failed validation.
     *
     * @return the certification path that failed validation, or {@code null} if
     *         none was specified.
     */
    public CertPath getCertPath() {
        return certPath;
    }

    /**
     * Returns the index of the failed certificate in the certification path.
     *
     * @return the index of the failed certificate in the certification path, or
     *         {@code -1} if none was specified.
     */
    public int getIndex() {
        return index;
    }
}
