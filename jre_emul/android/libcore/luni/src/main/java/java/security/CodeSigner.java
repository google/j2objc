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

package java.security;

import java.io.Serializable;
import java.security.cert.CertPath;

/**
 * {@code CodeSigner} represents a signer of code. Instances are immutable.
 */
public final class CodeSigner implements Serializable {

    private static final long serialVersionUID = 6819288105193937581L;

    private CertPath signerCertPath;

    private Timestamp timestamp;

    // Cached hash code value
    private transient int hash;

    /**
     * Constructs a new instance of {@code CodeSigner}.
     *
     * @param signerCertPath
     *            the certificate path associated with this code signer.
     * @param timestamp
     *            the time stamp associated with this code signer, maybe {@code
     *            null}.
     * @throws NullPointerException
     *             if {@code signerCertPath} is {@code null}.
     */
    public CodeSigner(CertPath signerCertPath, Timestamp timestamp) {
        if (signerCertPath == null) {
            throw new NullPointerException("signerCertPath == null");
        }
        this.signerCertPath = signerCertPath;
        this.timestamp = timestamp;
    }

    /**
     * Compares the specified object with this {@code CodeSigner} for equality.
     * Returns {@code true} if the specified object is also an instance of
     * {@code CodeSigner}, the two {@code CodeSigner} encapsulate the same
     * certificate path and the same time stamp, if present in both.
     *
     * @param obj
     *            object to be compared for equality with this {@code
     *            CodeSigner}.
     * @return {@code true} if the specified object is equal to this {@code
     *         CodeSigner}, otherwise {@code false}.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof CodeSigner) {
            CodeSigner that = (CodeSigner) obj;
            if (!signerCertPath.equals(that.signerCertPath)) {
                return false;
            }
            return timestamp == null ? that.timestamp == null : timestamp
                    .equals(that.timestamp);
        }
        return false;
    }

    /**
     * Returns the certificate path associated with this {@code CodeSigner}.
     *
     * @return the certificate path associated with this {@code CodeSigner}.
     */
    public CertPath getSignerCertPath() {
        return signerCertPath;
    }

    /**
     * Returns the time stamp associated with this {@code CodeSigner}.
     *
     * @return the time stamp associated with this {@code CodeSigner}, maybe
     *         {@code null}.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the hash code value for this {@code CodeSigner}. Returns the same
     * hash code for {@code CodeSigner}s that are equal to each other as
     * required by the general contract of {@link Object#hashCode}.
     *
     * @return the hash code value for this {@code CodeSigner}.
     * @see Object#equals(Object)
     * @see CodeSigner#equals(Object)
     */
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = signerCertPath.hashCode()
                    ^ (timestamp == null ? 0 : timestamp.hashCode());
        }
        return hash;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * this {@code CodeSigner} including its first certificate and its time
     * stamp, if present.
     *
     * @return a printable representation for this {@code CodeSigner}.
     */
    @Override
    public String toString() {
        // There is no any special reason for '256' here, it's taken abruptly
        StringBuilder buf = new StringBuilder(256);
        // The javadoc says nothing, and the others implementations behavior seems as
        // dumping only the first certificate. Well, let's do the same.
        buf.append("CodeSigner [").append(signerCertPath.getCertificates().get(0));
        if( timestamp != null ) {
            buf.append("; ").append(timestamp);
        }
        buf.append("]");
        return buf.toString();
    }
}
