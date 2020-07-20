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

/**
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package org.apache.harmony.security.tests.support.cert;

import java.io.ObjectStreamException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Extension;
import java.util.Set;

/**
 * Stub class for <code>java.security.cert.Certificate</code> tests
 */
public class MyCertificate extends Certificate implements X509Extension {

    private static final long serialVersionUID = -1835303280727190066L;
    // MyCertificate encoding
    private final byte[] encoding;

    public CertificateRep rep;

    /**
     * Constructs new object of class <code>MyCertificate</code>
     *
     * @param type
     * @param encoding
     */
    public MyCertificate(String type, byte[] encoding) {
        super(type);
        // don't copy to allow null parameter in test
        this.encoding = encoding;
    }

    /**
     * Returns <code>MyCertificate</code> encoding
     */
    public byte[] getEncoded() throws CertificateEncodingException {
        // do copy to force NPE in test
        return encoding.clone();
    }

    /**
     * Does nothing
     */
    public void verify(PublicKey key) throws CertificateException,
            NoSuchAlgorithmException, InvalidKeyException,
            NoSuchProviderException, SignatureException {
    }

    /**
     * Does nothing
     */
    public void verify(PublicKey key, String sigProvider)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException {
    }

    /**
     * Returns formatted <code>String</code>
     * describing <code>MyCertificate</code> object
     */
    public String toString() {
        return "[My test Certificate, type: " + getType() + "]";
    }

    public Object writeReplace() throws ObjectStreamException {
        return super.writeReplace();
    }

    /**
     * Returns public key (stub) from <code>MyCertificate</code> object
     */
    public PublicKey getPublicKey() {
        return new PublicKey() {
           private static final long serialVersionUID = 788077928335589816L;
            public String getAlgorithm() {
                return "TEST";
            }
            public byte[] getEncoded() {
                return new byte[] {(byte)1, (byte)2, (byte)3};
            }
            public String getFormat() {
                return "TEST_FORMAT";
            }
        };
    }

    public Certificate.CertificateRep getCertificateRep()
            throws ObjectStreamException {
        Object obj = super.writeReplace();
        return (MyCertificateRep) obj;
    }

    public class MyCertificateRep extends Certificate.CertificateRep {

        private static final long serialVersionUID = -3474284043994635553L;

        private String type;
        private byte[] data;

        public MyCertificateRep(String type, byte[] data) {
            super(type, data);
            this.data = data;
            this.type = type;
        }

        public Object readResolve() throws ObjectStreamException {
            return super.readResolve();
        }

        public String getType() {
            return type;
        }

        public byte[] getData() {
            return data;
        }
    }
    public Set<String> getNonCriticalExtensionOIDs() {
        return null;
    }

    public Set<String> getCriticalExtensionOIDs() {
        return null;
    }

    public byte[] getExtensionValue(String oid) {
        return null;
    }

    public boolean hasUnsupportedCriticalExtension() {
        return false;
    }
}
