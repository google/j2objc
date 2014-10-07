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

package javax.security.cert;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;

/**
 * Abstract class to represent identity certificates. It represents a way to
 * verify the binding of a Principal and its public key. Examples are X.509,
 * PGP, and SDSI.
 * <p>
 * Note: This package is provided only for compatibility reasons.
 * It contains a simplified version of the java.security.cert package that was
 * previously used by JSSE (Java SSL package). All applications that do not have
 * to be compatible with older versions of JSSE (that is before Java SDK 1.5)
 * should only use java.security.cert.
 */
public abstract class Certificate {

    /**
     * Creates a new {@code Certificate}.
     */
    public Certificate() {}

    /**
     * Compares the argument to this Certificate. If both have the same bytes
     * they are assumed to be equal.
     *
     * @param obj
     *            the {@code Certificate} to compare with this object
     * @return <code>true</code> if {@code obj} is the same as this
     *         {@code Certificate}, <code>false</code> otherwise
     * @see #hashCode
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Certificate)) {
            return false;
        }
        Certificate object = (Certificate) obj;
        try {
            return Arrays.equals(getEncoded(), object.getEncoded());
        } catch (CertificateEncodingException e) {
            return false;
        }
    }

    /**
     * Returns an integer hash code for the receiver. Any two objects which
     * return <code>true</code> when passed to <code>equals</code> must answer
     * the same value for this method.
     *
     * @return the receiver's hash
     * @see #equals
     */
    public int hashCode() {
        int res = 0;
        try {
            byte[] array = getEncoded();
            for (int i=0; i<array.length; i++) {
                res += array[i];
            }
        } catch (CertificateEncodingException e) {
        }
        return res;
    }

    /**
     * Returns the encoded representation for this certificate.
     *
     * @return the encoded representation for this certificate.
     * @throws CertificateEncodingException
     *             if encoding fails.
     */
    public abstract byte[] getEncoded()
            throws CertificateEncodingException;

    /**
     * Verifies that this certificate was signed with the given public key.
     *
     * @param key
     *            public key for which verification should be performed.
     * @throws CertificateException
     *             if encoding errors are detected
     * @throws NoSuchAlgorithmException
     *             if an unsupported algorithm is detected
     * @throws InvalidKeyException
     *             if an invalid key is detected
     * @throws NoSuchProviderException
     *             if there is no default provider
     * @throws SignatureException
     *             if signature errors are detected
     */
    public abstract void verify(PublicKey key)
            throws CertificateException, NoSuchAlgorithmException,
                   InvalidKeyException, NoSuchProviderException,
                   SignatureException;

    /**
     * Verifies that this certificate was signed with the given public key. Uses
     * the signature algorithm given by the provider.
     *
     * @param key
     *            public key for which verification should be performed.
     * @param sigProvider
     *            the name of the signature provider.
     * @throws CertificateException
     *                if encoding errors are detected
     * @throws NoSuchAlgorithmException
     *                if an unsupported algorithm is detected
     * @throws InvalidKeyException
     *                if an invalid key is detected
     * @throws NoSuchProviderException
     *                if the specified provider does not exists.
     * @throws SignatureException
     *                if signature errors are detected
     */
    public abstract void verify(PublicKey key, String sigProvider)
            throws CertificateException, NoSuchAlgorithmException,
                   InvalidKeyException, NoSuchProviderException,
                   SignatureException;

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     *
     * @return a printable representation for the receiver.
     */
    public abstract String toString();

    /**
     * Returns the public key corresponding to this certificate.
     *
     * @return the public key corresponding to this certificate.
     */
    public abstract PublicKey getPublicKey();
}
