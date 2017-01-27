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
 * This class represents Certificate Revocation Lists (CRLs) maintained by a
 * certificate authority. They are used to indicate that a given Certificate has
 * expired and consequently has become invalid.
 *
 * @see CertificateFactory
 */
public abstract class CRL {
    // The CRL type
    private final String type;

    /**
     * Creates a new certificate revocation list of the specified type.
     *
     * @param type
     *            the type for the CRL.
     */
    protected CRL(String type) {
        this.type = type;
    }

    /**
     * Returns the type of this CRL.
     *
     * @return the type of this CRL.
     */
    public final String getType() {
        return type;
    }

    /**
     * Returns whether the specified certificate is revoked by this CRL.
     *
     * @param cert
     *            the certificate to check.
     * @return {@code true} if the certificate is revoked by this CRL, otherwise
     *         {@code false}.
     */
    public abstract boolean isRevoked(Certificate cert);

    /**
     * Returns the string representation of this instance.
     *
     * @return the string representation of this instance.
     */
    public abstract String toString();
}
