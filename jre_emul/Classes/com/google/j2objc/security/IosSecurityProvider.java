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

package com.google.j2objc.security;

import com.google.j2objc.security.cert.IosCertificateFactory;

import java.security.Provider;

/**
 * Security provider that maps to iOS security algorithms. Provider keys
 * are from org.conscrypt.OpenSSLProvider, Android's primary provider.
 *
 * @author Tom Ball
 */
public class IosSecurityProvider extends Provider {

  public static final String PROVIDER_NAME = "J2ObjCSecurity";

  private static final String PREFIX = "com.google.j2objc.security.";

  public IosSecurityProvider() {
    super(PROVIDER_NAME, 1.0, "J2ObjC's iOS Security Framework-backed provider");

    // Secure random implementation.
    put("SecureRandom.SHA1PRNG", PREFIX + "IosSecureRandomImpl");

    // X509 certificate provider.
    put("CertificateFactory.X.509", "com.google.j2objc.security.cert.IosCertificateFactory");

    /* === Message Digests === */
    put("MessageDigest.SHA-1", PREFIX + "IosSHAMessageDigest$SHA1");
    put("Alg.Alias.MessageDigest.SHA1", "SHA-1");
    put("Alg.Alias.MessageDigest.SHA", "SHA-1");
    put("Alg.Alias.MessageDigest.1.3.14.3.2.26", "SHA-1");

    put("MessageDigest.SHA-256", PREFIX + "IosSHAMessageDigest$SHA256");
    put("Alg.Alias.MessageDigest.SHA256", "SHA-256");
    put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.1", "SHA-256");

    put("MessageDigest.SHA-384", PREFIX + "IosSHAMessageDigest$SHA384");
    put("Alg.Alias.MessageDigest.SHA384", "SHA-384");
    put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.2", "SHA-384");

    put("MessageDigest.SHA-512", PREFIX + "IosSHAMessageDigest$SHA512");
    put("Alg.Alias.MessageDigest.SHA512", "SHA-512");
    put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.3", "SHA-512");

    // iso(1) member-body(2) US(840) rsadsi(113549) digestAlgorithm(2) md5(5)
    put("MessageDigest.MD5", "com.google.j2objc.security.IosMD5MessageDigest");
    put("Alg.Alias.MessageDigest.1.2.840.113549.2.5", "MD5");

    /* == KeyPairGenerators == */
    put("KeyPairGenerator.RSA", PREFIX + "IosRSAKeyPairGenerator");
    put("Alg.Alias.KeyPairGenerator.1.2.840.113549.1.1.1", "RSA");

    /* == KeyFactory == */
    put("KeyFactory.RSA", PREFIX + "IosRSAKeyFactory");
    put("Alg.Alias.KeyFactory.1.2.840.113549.1.1.1", "RSA");

  }

  // Reference all dynamically loaded classes, so they are linked into apps.
  private static final Class<?>[] unused = {
    IosMD5MessageDigest.class,
    IosSecureRandomImpl.class,
    IosSHAMessageDigest.class,
    IosCertificateFactory.class
  };
}
