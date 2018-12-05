/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.targets.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import junit.framework.TestCase;

public abstract class KeyStoreTest extends TestCase {

    private final String algorithmName;
    private final byte[] keyStoreData;
    private final String keyStorePassword;

    public KeyStoreTest(String algorithmName, byte[] keyStoreData,
            String keyStorePassword) {
        this.algorithmName = algorithmName;
        this.keyStoreData = keyStoreData;
        this.keyStorePassword = keyStorePassword;
    }

    public void testKeyStoreLoad() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(algorithmName);
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        }

        try {
            keyStore.load(new ByteArrayInputStream(keyStoreData),
                    keyStorePassword.toCharArray());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        } catch (CertificateException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            assertTrue("keystore is empty", keyStore.aliases()
                    .hasMoreElements());
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        }
    }

    public void testKeyStoreCreate() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(algorithmName);
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        }

        try {
            keyStore.load(null, "the secret password".toCharArray());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        } catch (CertificateException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        CertificateFactory certificateFactory = null;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            fail(e.getMessage());
        }

        Certificate certificate = null;
        try {
            certificate = certificateFactory
                    .generateCertificate(new ByteArrayInputStream(
                            encodedCertificate.getBytes()));
        } catch (CertificateException e) {
            fail(e.getMessage());
        }

        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(certificate.getPublicKey()
                    .getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }

        KeyPair keyPair = generator.generateKeyPair();

        PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(keyPair
                .getPrivate(), new Certificate[] {certificate});

        try {
            keyStore.setEntry("aPrivateKey", privateKeyEntry,
                    new PasswordProtection("the key password".toCharArray()));
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        }

        try {
            assertTrue(keyStore.containsAlias("aPrivateKey"));
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        }

        try {
            PrivateKeyEntry entry = (PrivateKeyEntry) keyStore.getEntry(
                    "aPrivateKey", new PasswordProtection("the key password"
                            .toCharArray()));
            PrivateKey privateKey = entry.getPrivateKey();
            assertEquals(keyPair.getPrivate(), privateKey);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        } catch (UnrecoverableEntryException e) {
            fail(e.getMessage());
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        }

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            keyStore.store(stream, "the keystore password".toCharArray());
            assertTrue("keystore not written", stream.size() > 0);
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        } catch (CertificateException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private String encodedCertificate = "-----BEGIN CERTIFICATE-----\n"
            + "MIID0jCCAzugAwIBAgIBAjANBgkqhkiG9w0BAQQFADCBmjELMAkGA1UEBhMCVUsx\n"
            + "EjAQBgNVBAgTCUhhbXBzaGlyZTETMBEGA1UEBxMKV2luY2hlc3RlcjETMBEGA1UE\n"
            + "ChMKSUJNIFVLIEx0ZDEMMAoGA1UECxMDSlRDMRYwFAYDVQQDEw1QYXVsIEggQWJi\n"
            + "b3R0MScwJQYJKoZIhvcNAQkBFhhQYXVsX0hfQWJib3R0QHVrLmlibS5jb20wHhcN\n"
            + "MDQwNjIyMjA1MDU1WhcNMDUwNjIyMjA1MDU1WjCBmDELMAkGA1UEBhMCVUsxEjAQ\n"
            + "BgNVBAgTCUhhbXBzaGlyZTETMBEGA1UEBxMKV2luY2hlc3RlcjETMBEGA1UEChMK\n"
            + "SUJNIFVrIEx0ZDEMMAoGA1UECxMDSkVUMRQwEgYDVQQDEwtQYXVsIEFiYm90dDEn\n"
            + "MCUGCSqGSIb3DQEJARYYUGF1bF9IX0FiYm90dEB1ay5pYm0uY29tMIGfMA0GCSqG\n"
            + "SIb3DQEBAQUAA4GNADCBiQKBgQDitZBQ5d18ecNJpcnuKTraHYtqsAugoc95/L5Q\n"
            + "28s3t1QAu2505qQR1MZaAkY7tDNyl1vPnZoym+Y06UswTrZoVYo/gPNeyWPMTsLA\n"
            + "wzQvk5/6yhtE9ciH7B0SqYw6uSiDTbUY/zQ6qed+TsQhjlbn3PUHRjnI2P8A04cg\n"
            + "LgYYGQIDAQABo4IBJjCCASIwCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3Bl\n"
            + "blNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFPplRPs65hUfxUBs\n"
            + "6/Taq7nN8i1UMIHHBgNVHSMEgb8wgbyAFJOMtPAwlXdZLqE7DKU6xpL6FjFtoYGg\n"
            + "pIGdMIGaMQswCQYDVQQGEwJVSzESMBAGA1UECBMJSGFtcHNoaXJlMRMwEQYDVQQH\n"
            + "EwpXaW5jaGVzdGVyMRMwEQYDVQQKEwpJQk0gVUsgTHRkMQwwCgYDVQQLEwNKVEMx\n"
            + "FjAUBgNVBAMTDVBhdWwgSCBBYmJvdHQxJzAlBgkqhkiG9w0BCQEWGFBhdWxfSF9B\n"
            + "YmJvdHRAdWsuaWJtLmNvbYIBADANBgkqhkiG9w0BAQQFAAOBgQAnQ22Jw2HUrz7c\n"
            + "VaOap31mTikuQ/CQxpwPYiSyTJ4s99eEzn+2yAk9tIDIJpqoay/fj+OLgPUQKIAo\n"
            + "XpRVvmHlGE7UqMKebZtSZJQzs6VoeeKFhgHmqg8eVC2AsTc4ZswJmg4wCui5AH3a\n"
            + "oqG7PIM3LxZqXYQlZiPSZ6kCpDOWVg==\n"
            + "-----END CERTIFICATE-----\n";
}
