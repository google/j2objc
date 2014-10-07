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
import java.util.Iterator;
import java.util.List;

/**
 * An immutable certificate path that can be validated. All certificates in the
 * path are of the same type (i.e., X509).
 * <p>
 * A {@code CertPath} can be represented as a byte array in at least one
 * supported encoding scheme (i.e. PkiPath or PKCS7) when serialized.
 * <p>
 * When a {@code List} of the certificates is obtained it must be immutable.
 * <p>
 * A {@code CertPath} must be thread-safe without requiring coordinated access.
 *
 * @see Certificate
 */
public abstract class CertPath implements Serializable {

    private static final long serialVersionUID = 6068470306649138683L;
    // Standard name of the type of certificates in this path
    private final String type;

    /**
     * Creates a new {@code CertPath} instance for the specified certificate
     * type.
     *
     * @param type
     *            the certificate type.
     */
    protected CertPath(String type) {
        this.type = type;
    }

    /**
     * Returns the type of {@code Certificate} in this instance.
     *
     * @return the certificate type.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns {@code true} if {@code Certificate}s in the list are the same
     * type and the lists are equal (and by implication the certificates
     * contained within are the same).
     *
     * @param other
     *            {@code CertPath} to be compared for equality.
     * @return {@code true} if the object are equal, {@code false} otherwise.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof CertPath) {
            CertPath o = (CertPath)other;
            if (getType().equals(o.getType())) {
                if (getCertificates().equals(o.getCertificates())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Overrides {@code Object.hashCode()}. The function is defined as follows:
     * <pre>
     * {@code hashCode = 31 * path.getType().hashCode() +
     * path.getCertificates().hashCode();}
     * </pre>
     *
     * @return the hash code for this instance.
     */
    public int hashCode() {
        int hash = getType().hashCode();
        hash = hash*31 + getCertificates().hashCode();
        return hash;
    }

    /**
     * Returns a {@code String} representation of this {@code CertPath}
     * instance. It is the result of calling {@code toString} on all {@code
     * Certificate}s in the {@code List}.
     *
     * @return a string representation of this instance.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(getType());
        sb.append(" Cert Path, len=");
        sb.append(getCertificates().size());
        sb.append(": [\n");
        int n=1;
        for (Iterator<? extends Certificate> i=getCertificates().iterator(); i.hasNext(); n++) {
            sb.append("---------------certificate ");
            sb.append(n);
            sb.append("---------------\n");
            sb.append(((Certificate)i.next()).toString());
        }
        sb.append("\n]");
        return sb.toString();
    }

    /**
     * Returns an immutable List of the {@code Certificate}s contained
     * in the {@code CertPath}.
     *
     * @return a list of {@code Certificate}s in the {@code CertPath}.
     */
    public abstract List<? extends Certificate> getCertificates();

    /**
     * Returns an encoding of the {@code CertPath} using the default encoding.
     *
     * @return default encoding of the {@code CertPath}.
     * @throws CertificateEncodingException
     *             if the encoding fails.
     */
    public abstract byte[] getEncoded()
        throws CertificateEncodingException;

    /**
     * Returns an encoding of this {@code CertPath} using the given
     * {@code encoding} from {@link #getEncodings()}.
     *
     * @throws CertificateEncodingException
     *             if the encoding fails.
     */
    public abstract byte[] getEncoded(String encoding) throws CertificateEncodingException;

    /**
     * Returns an {@code Iterator} over the supported encodings for a
     * representation of the certificate path.
     *
     * @return {@code Iterator} over supported encodings (as {@code String}s).
     */
    public abstract Iterator<String> getEncodings();

    /**
     * Returns an alternate object to be serialized.
     *
     * @return an alternate object to be serialized.
     * @throws ObjectStreamException
     *             if the creation of the alternate object fails.
     */
    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertPathRep(getType(), getEncoded());
        } catch (CertificateEncodingException e) {
            throw new NotSerializableException("Could not create serialization object: " + e);
        }
    }

    /**
     * The alternate {@code Serializable} class to be used for serialization and
     * deserialization on {@code CertPath} objects.
     */
    protected static class CertPathRep implements Serializable {

        private static final long serialVersionUID = 3015633072427920915L;
        // Standard name of the type of certificates in this path
        private final String type;
        // cert path data
        private final byte[] data;

        // Force default serialization to use writeUnshared/readUnshared
        // for cert path data
        private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("type", String.class),
            new ObjectStreamField("data", byte[].class, true),
        };

        /**
         * Creates a new {@code CertPathRep} instance with the specified type
         * and encoded data.
         *
         * @param type
         *            the certificate type.
         * @param data
         *            the encoded data.
         */
        protected CertPathRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        /**
         * Deserializes a {@code CertPath} from a serialized {@code CertPathRep}
         * object.
         *
         * @return the deserialized {@code CertPath}.
         * @throws ObjectStreamException
         *             if deserialization fails.
         */
        protected Object readResolve() throws ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(type);
                return cf.generateCertPath(new ByteArrayInputStream(data));
            } catch (Throwable t) {
                throw new NotSerializableException("Could not resolve cert path: " + t);
            }
        }
    }
}
