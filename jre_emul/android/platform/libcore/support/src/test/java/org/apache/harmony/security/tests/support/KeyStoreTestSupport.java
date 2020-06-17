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

package org.apache.harmony.security.tests.support;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;

/**
 * Support class for KeyStore tests
 *
 */

public class KeyStoreTestSupport {

    public static final String srvKeyStore = "KeyStore";

    public static String[] validValues = { "bks", "BKS", "bKS", "Bks", "bKs",
            "BkS" };

    public static String defaultType = "bks";

    public static boolean JKSSupported = false;

    public static String defaultProviderName = null;

    public static Provider defaultProvider = null;

    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType, srvKeyStore);
        JKSSupported = (defaultProvider != null);
        defaultProviderName = (JKSSupported ? defaultProvider.getName() : null);
    }

    /**
     * Additional class to create SecretKey object
     */
    public static class SKey implements SecretKey {
        private String type;

        private byte[] encoded;

        public SKey(String type, byte[] encoded) {
            this.type = type;
            this.encoded = encoded;
        }

        public String getAlgorithm() {
            return type;
        }

        public byte[] getEncoded() {
            return encoded;
        }

        public String getFormat() {
            return "test";
        }
    }

    /**
     * Additional class to create PrivateKey object
     */
    public static class MyPrivateKey implements PrivateKey {
        private String algorithm;

        private String format;

        private byte[] encoded;

        public MyPrivateKey(String algorithm, String format, byte[] encoded) {
            this.algorithm = algorithm;
            this.format = format;
            this.encoded = encoded;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getFormat() {
            return format;
        }

        public byte[] getEncoded() {
            return encoded;
        }
    }

    /**
     * Additional class to create Certificate and Key objects
     */
    public static class MCertificate extends Certificate {
        private final byte[] encoding;

        private final String type;

        public MCertificate(String type, byte[] encoding) {
            super(type);
            this.encoding = encoding;
            this.type = type;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return encoding.clone();
        }

        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }

        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }

        public String toString() {
            return "[MCertificate, type: " + getType() + "]";
        }

        public PublicKey getPublicKey() {
            return new PublicKey() {
                public String getAlgorithm() {
                    return type;
                }

                public byte[] getEncoded() {
                    return encoding;
                }

                public String getFormat() {
                    return "test";
                }
            };
        }
    }

    /**
     * Additional class to create ProtectionParameter object
     */
    public static class ProtPar implements KeyStore.ProtectionParameter {
    }

    /**
     * Additional class to create KeyStore.Entry object
     */
    public static class AnotherEntry implements KeyStore.Entry {
    }
}

