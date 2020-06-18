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
 *
 */
public class Support_ProviderTrust extends Provider {
    private static final long serialVersionUID = 1L;

    // Provider name
    private static final String NAME = "ProviderTrust";

    // Version of the services
    private static final double VERSION = 1.0;

    private static final String INFO = NAME
            + " DSA key, parameter generation and signing; SHA-1 digest; SHA1PRNG SecureRandom";

    /**
     * Constructs a new instance of the dummy provider.
     *
     */
    public Support_ProviderTrust() {
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

                // Algorithm parameter generator
                put("AlgorithmParameterGenerator.DSA",
                        "made.up.provider.name.AlgorithmParameterGeneratorDSA");

                // Algorithm parameters
                put("AlgorithmParameters.DSA",
                        "made.up.provider.name.AlgorithmParametersDSA");

                // Key pair generator
                put("KeyPairGenerator.DSA",
                        "made.up.provider.name.KeyPairGeneratorDSA");

                // Key factory
                put("KeyFactory.DSA", "made.up.provider.name.KeyFactoryDSA");
                put("KeyFactory.RSA", "made.up.provider.name.KeyFactoryRSA");

                // Signature algorithm
                put("Signature.SHA1withDSA",
                        "made.up.provider.name.SignatureDSA");

                // KeyStore
                put("KeyStore.PKCS#12/Netscape",
                        "made.up.provider.name.KeyStore");

                // Certificate
                put("CertificateFactory.X509",
                        "made.up.provider.name.CertificateFactoryX509");

                return null;
            }
        });
    }
}