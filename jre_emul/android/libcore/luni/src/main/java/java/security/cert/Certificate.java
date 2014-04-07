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

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
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
 */
public abstract class Certificate implements Serializable {

    private static final long serialVersionUID = -3585440601605666277L;

    // The standard name of the certificate type
    private final String type;

    /**
     * Creates a new {@code Certificate} with the specified type.
     *
     * @param type
     *        the certificate type.
     */
    protected Certificate(String type) {
        this.type = type;
    }

    /**
     * Returns the certificate type.
     *
     * @return the certificate type.
     */
    public final String getType() {
        return type;
    }

    /**
     * Compares the argument to the certificate, and returns {@code true} if they
     * represent the <em>same</em> object using a class specific comparison. The
     * implementation in Object returns {@code true} only if the argument is the
     * exact same object as the callee (==).
     *
     * @param other
     *            the object to compare with this object.
     * @return {@code true} if the object is the same as this object, {@code
     *         false} if it is different from this object.
     * @see #hashCode
     */
    public boolean equals(Object other) {
        // obj equal to itself
        if (this == other) {
            return true;
        }
        if (other instanceof Certificate) {
            try {
                // check that encoded forms match
                return Arrays.equals(this.getEncoded(),
                        ((Certificate)other).getEncoded());
            } catch (CertificateEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Returns an integer hash code for the certificate. Any two objects which
     * return {@code true} when passed to {@code equals} must return the same
     * value for this method.
     *
     * @return the certificate's hash
     * @see #equals
     */
    public int hashCode() {
        try {
            byte[] encoded = getEncoded();
            int hash = 0;
            for (int i=0; i<encoded.length; i++) {
                hash += i*encoded[i];
            }
            return hash;
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the encoded representation for this certificate.
     *
     * @return the encoded representation for this certificate.
     * @throws CertificateEncodingException
     *             if the encoding fails.
     */
    public abstract byte[] getEncoded() throws CertificateEncodingException;

    /**
     * Verifies that this certificate was signed with the given public key.
     *
     * @param key
     *            PublicKey public key for which verification should be
     *            performed.
     * @throws CertificateException
     *             if encoding errors are detected.
     * @throws NoSuchAlgorithmException
     *             if an unsupported algorithm is detected.
     * @throws InvalidKeyException
     *             if an invalid key is detected.
     * @throws NoSuchProviderException
     *             if there is no default provider.
     * @throws SignatureException
     *             if signature errors are detected.
     */
    public abstract void verify(PublicKey key)
        throws CertificateException,
               NoSuchAlgorithmException,
               InvalidKeyException,
               NoSuchProviderException,
               SignatureException;

    /**
     * Verifies that this certificate was signed with the given public key. It
     * Uses the signature algorithm given by the provider.
     *
     * @param key
     *            PublicKey public key for which verification should be
     *            performed.
     * @param sigProvider
     *            String the name of the signature provider.
     * @throws CertificateException
     *                if encoding errors are detected.
     * @throws NoSuchAlgorithmException
     *                if an unsupported algorithm is detected.
     * @throws InvalidKeyException
     *                if an invalid key is detected.
     * @throws NoSuchProviderException
     *                if the specified provider does not exists.
     * @throws SignatureException
     *                if signature errors are detected.
     */
    public abstract void verify(PublicKey key, String sigProvider)
        throws CertificateException,
               NoSuchAlgorithmException,
               InvalidKeyException,
               NoSuchProviderException,
               SignatureException;

    /**
     * Returns a string containing a concise, human-readable description of the
     * certificate.
     *
     * @return a printable representation for the certificate.
     */
    public abstract String toString();

    /**
     * Returns the public key corresponding to this certificate.
     *
     * @return the public key corresponding to this certificate.
     */
    public abstract PublicKey getPublicKey();

    /**
     * Returns an alternate object to be serialized.
     *
     * @return the object to serialize.
     * @throws ObjectStreamException
     *             if the creation of the alternate object fails.
     */
    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertificateRep(getType(), getEncoded());
        } catch (CertificateEncodingException e) {
            throw new NotSerializableException("Could not create serialization object: " + e);
        }
    }

    /**
     * The alternate {@code Serializable} class to be used for serialization and
     * deserialization of {@code Certificate} objects.
     */
    protected static class CertificateRep implements Serializable {

        private static final long serialVersionUID = -8563758940495660020L;
        // The standard name of the certificate type
        private final String type;
        // The certificate data
        private final byte[] data;

        // Force default serialization to use writeUnshared/readUnshared
        // for the certificate data
        private static final ObjectStreamField[] serialPersistentFields = {
             new ObjectStreamField("type", String.class),
             new ObjectStreamField("data", byte[].class, true)
        };

        /**
         * Creates a new {@code CertificateRep} instance with the specified
         * certificate type and encoded data.
         *
         * @param type
         *            the certificate type.
         * @param data
         *            the encoded data.
         */
        protected CertificateRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        /**
         * Deserializes a {@code Certificate} from a serialized {@code
         * CertificateRep} object.
         *
         * @return the deserialized {@code Certificate}.
         * @throws ObjectStreamException
         *             if deserialization fails.
         */
        protected Object readResolve() throws ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(type);
                return cf.generateCertificate(new ByteArrayInputStream(data));
            } catch (Throwable t) {
                throw new NotSerializableException("Could not resolve certificate: " + t);
            }
        }
    }
}
