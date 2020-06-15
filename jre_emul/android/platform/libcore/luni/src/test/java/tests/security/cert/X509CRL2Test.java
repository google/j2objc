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

package tests.security.cert;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class X509CRL2Test extends TestCase {

    private X509Certificate pemCert = null;

    String certificate = "-----BEGIN CERTIFICATE-----\n"
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



    protected void setUp() throws Exception {
        ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
                .getBytes());

        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        pemCert = (X509Certificate) certFact.generateCertificate(certArray);
    }

    /**
     * java.security.cert.X509CRL#getExtensionValue(java.lang.String)
     */
    public void test_getExtensionValueLjava_lang_String() {
        try {
            setUp();
        } catch (Exception e) {
            fail("Exception " + e + " was thrown during configaration");
        }
        if (pemCert != null) {
            Vector<String> extensionOids = new Vector<String>();
            extensionOids.addAll(pemCert.getCriticalExtensionOIDs());
            extensionOids.addAll(pemCert.getNonCriticalExtensionOIDs());
            assertFalse(pemCert.hasUnsupportedCriticalExtension());
            Iterator<String> i = extensionOids.iterator();
            while (i.hasNext()) {
                String oid = i.next();
                byte[] value = pemCert.getExtensionValue(oid);
                if (value != null && value.length > 0) {
                    // check that it is an encoded as a OCTET STRING
                    assertTrue("The extension value for the oid " + oid
                            + " was not encoded as an OCTET STRING",
                            value[0] == 0x04);
                }
            }// end while
        } else {
            fail("Unable to obtain X509Certificate");
        }
    }

    /**
     * java.security.cert.X509CRL#X509CRL()
     */
    @SuppressWarnings("cast")
    public void test_X509CRL() {
        try {
            MyX509CRL crl = new MyX509CRL();
            assertNotNull(crl);
            assertTrue(crl instanceof X509CRL);
        } catch (Exception e) {
            fail("Unexpected exception for constructor");
        }
    }

    class MyX509CRL extends X509CRL implements X509Extension {

        public MyX509CRL() {
            super();
        }

        @Override
        public byte[] getEncoded() {
            return null;
        }

        @Override
        public Principal getIssuerDN() {
            return null;
        }

        @Override
        public Date getNextUpdate() {
            return null;
        }

        @Override
        public X509CRLEntry getRevokedCertificate(BigInteger serialNumber) {
            return null;
        }

        @Override
        public Set<? extends X509CRLEntry> getRevokedCertificates() {
            return null;
        }

        @Override
        public String getSigAlgName() {
            return null;
        }

        @Override
        public String getSigAlgOID() {
            return null;
        }

        @Override
        public byte[] getSigAlgParams() {
            return null;
        }

        @Override
        public byte[] getSignature() {
            return null;
        }

        @Override
        public byte[] getTBSCertList() {
            return null;
        }

        @Override
        public Date getThisUpdate() {
            return null;
        }

        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public void verify(PublicKey key) {
        }

        @Override
        public void verify(PublicKey key, String sigProvider) {
        }

        @Override
        public boolean isRevoked(Certificate cert) {
            return false;
        }

        @Override
        public String toString() {
            return null;
        }

        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }

        public byte[] getExtensionValue(String oid) {
            return null;
        }

        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }

        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }
    }

}
