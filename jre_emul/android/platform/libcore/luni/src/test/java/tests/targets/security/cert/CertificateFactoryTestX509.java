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
package tests.targets.security.cert;

import tests.security.CertificateFactoryTest;

public class CertificateFactoryTestX509 extends CertificateFactoryTest {

    public CertificateFactoryTestX509() {
        super("X509", encodedCertificate.getBytes());
    }

    public static final String encodedCertificate =
        "-----BEGIN CERTIFICATE-----\n"
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
