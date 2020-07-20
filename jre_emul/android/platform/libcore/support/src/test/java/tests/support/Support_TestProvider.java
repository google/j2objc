/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.support;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

/**
 * This class implements a dummy provider.
 */
public class Support_TestProvider extends Provider {
    private static final long serialVersionUID = 1L;

    // Provider name
    private static final String NAME = "TestProvider";

    // Version of the services provided
    private static final double VERSION = 1.0;

    private static final String INFO = NAME
            + " DSA key, parameter generation and signing; SHA-1 digest; "
            + "SHA1PRNG SecureRandom; PKCS#12/Netscape KeyStore";

    /**
     * Constructs a new instance of the dummy provider.
     */
    public Support_TestProvider() {
        super(NAME, VERSION, INFO);
        registerServices();
    }

    /**
     * Register the services the receiver provides.
     */
    private void registerServices() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                // Digest engine
                put("MessageDigest.SHA",
                        "made.up.provider.name.MessageDigestSHA");
                put("MessageDigest.MD5",
                        "made.up.provider.name.MessageDigestMD5");
                // aliases
                put("Alg.Alias.MessageDigest.SHA1", "SHA");
                put("Alg.Alias.MessageDigest.SHA-1", "SHA");
                put("Alg.Alias.MessageDigest.OID.1.3.14.3.2.26", "SHA");
                put("Alg.Alias.MessageDigest.1.3.14.3.2.26", "SHA");

                // Algorithm parameter generator
                put("AlgorithmParameterGenerator.DSA",
                        "made.up.provider.name.AlgorithmParameterGeneratorDSA");

                // Algorithm parameters
                put("AlgorithmParameters.DSA",
                        "made.up.provider.name.AlgorithmParametersDSA");
                // aliases
                put("Alg.Alias.AlgorithmParameters.1.2.840.10040.4.1", "DSA");
                put("Alg.Alias.AlgorithmParameters.1.3.14.3.2.12", "DSA");

                // Key pair generator
                put("KeyPairGenerator.DSA",
                        "made.up.provider.name.KeyPairGeneratorDSA");
                // aliases
                put("Alg.Alias.KeyPairGenerator.OID.1.2.840.10040.4.1", "DSA");
                put("Alg.Alias.KeyPairGenerator.1.2.840.10040.4.1", "DSA");
                put("Alg.Alias.KeyPairGenerator.1.3.14.3.2.12", "DSA");

                // Key factory
                put("KeyFactory.DSA", "made.up.provider.name.KeyFactoryDSA");
                put("KeyFactory.RSA", "made.up.provider.name.KeyFactoryRSA");
                // aliases
                put("Alg.Alias.KeyFactory.1.2.840.10040.4.1", "DSA");
                put("Alg.Alias.KeyFactory.1.3.14.3.2.12", "DSA");

                // Signature algorithm
                put("Signature.SHA1withDSA",
                        "made.up.provider.name.SignatureDSA");

                // aliases
                put("Alg.Alias.Signature.DSA", "SHA1withDSA");
                put("Alg.Alias.Signature.DSS", "SHA1withDSA");
                put("Alg.Alias.Signature.SHA/DSA", "SHA1withDSA");
                put("Alg.Alias.Signature.SHA1/DSA", "SHA1withDSA");
                put("Alg.Alias.Signature.SHA-1/DSA", "SHA1withDSA");
                put("Alg.Alias.Signature.SHAwithDSA", "SHA1withDSA");
                put("Alg.Alias.Signature.DSAwithSHA1", "SHA1withDSA");
                put("Alg.Alias.Signature.DSAWithSHA1", "SHA1withDSA");
                put("Alg.Alias.Signature.SHA-1withDSA", "SHA1withDSA");
                put("Alg.Alias.Signature.OID.1.2.840.10040.4.3", "SHA1withDSA");
                put("Alg.Alias.Signature.1.2.840.10040.4.3", "SHA1withDSA");
                put("Alg.Alias.Signature.1.3.14.3.2.13", "SHA1withDSA");
                put("Alg.Alias.Signature.1.3.14.3.2.27", "SHA1withDSA");
                put("Alg.Alias.Signature.OID.1.3.14.3.2.13", "SHA1withDSA");
                put("Alg.Alias.Signature.OID.1.3.14.3.2.27", "SHA1withDSA");

                put("KeyStore.PKCS#12/Netscape",
                        "tests.support.Support_DummyPKCS12Keystore");

                // Certificate
                put("CertificateFactory.X509",
                        "made.up.provider.name.CertificateFactoryX509");
                // aliases
                put("Alg.Alias.CertificateFactory.X.509", "X509");

                return null;
            }
        });
    }
}