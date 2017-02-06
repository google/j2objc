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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import tests.support.resource.Support_Resources;

/**
 * java.security.cert test utilities
 *
 */
public class TestUtils {
    // Certificate type used during testing
    private static final String certType = "X.509";
    // Key store type used during testing
    private static final String keyStoreType = "BKS";
    // The file name prefix to load keystore from
    private static final String keyStoreFileName = "test." + keyStoreType
            + ".ks";
    //
    // The file name suffixes to load keystore from
    //  *.ks1 - keystore containing untrusted certificates only
    //  *.ks2 - keystore containing trusted certificates only
    //  *.ks3 - keystore containing both trusted and untrusted certificates
    //
    public static final int UNTRUSTED = 1;
    public static final int TRUSTED = 2;
    public static final int TRUSTED_AND_UNTRUSTED = 3;
    //
    // Common passwords for all test keystores
    //
    private final static char[] storepass =
        new char[] {'s','t','o','r','e','p','w','d'};

    /**
     * Creates <code>TrustAnchor</code> instance
     * constructed using self signed test certificate
     *
     * @return <code>TrustAnchor</code> instance
     */
//    public static TrustAnchor getTrustAnchor() {
//        CertificateFactory cf = null;
//        try {
//            cf = CertificateFactory.getInstance(certType);
//        } catch (CertificateException e) {
//            // requested cert type is not available in the
//            // default provider package or any of the other provider packages
//            // that were searched
//            throw new RuntimeException(e);
//        }
//        BufferedInputStream bis = null;
//        try {
//            bis = new BufferedInputStream(new ByteArrayInputStream(
//                    getEncodedX509Certificate()));
//            X509Certificate c1 = (X509Certificate)cf.generateCertificate(bis);
//
//            return new TrustAnchor(c1, null);
//        } catch (Exception e) {
//            // all failures are fatal
//            throw new RuntimeException(e);
//        } finally {
//            if (bis != null) {
//                try {
//                    bis.close() ;
//                } catch (IOException ign) {}
//            }
//        }
//    }

    /**
     * Creates <code>Set</code> of <code>TrustAnchor</code>s
     * containing single element (self signed test certificate).
     * @return Returns <code>Set</code> of <code>TrustAnchor</code>s
     */
//    public static Set<TrustAnchor> getTrustAnchorSet() {
//        TrustAnchor ta = getTrustAnchor();
//        if (ta == null) {
//            return null;
//        }
//        HashSet<TrustAnchor> set = new HashSet<TrustAnchor>();
//        if (!set.add(ta)) {
//            throw new RuntimeException("Could not create trust anchor set");
//        }
//        return set;
//    }

    /**
     * Creates test <code>KeyStore</code> instance
     *
     * @param initialize
     *  Do not initialize returned <code>KeyStore</code> if false
     *
     * @param testKeyStoreType
     *  this parameter ignored if <code>initialize</code> is false;
     *  The following types supported:<br>
     *  1 - <code>KeyStore</code> with untrusted certificates only<br>
     *  2 - <code>KeyStore</code> with trusted certificates only<br>
     *  3 - <code>KeyStore</code> with both trusted and untrusted certificates
     *
     * @return Returns test <code>KeyStore</code> instance
     */
    public static KeyStore getKeyStore(boolean initialize,
            int testKeyStoreType) {
        BufferedInputStream bis = null;
        try {
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            if (initialize) {
                String fileName = keyStoreFileName + testKeyStoreType;
                ks.load(Support_Resources.getResourceStream(fileName),
                        storepass);
            }
            return ks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (initialize && bis != null) {
                try {
                    bis.close();
                } catch (IOException ign) {}
            }
        }
    }

    /**
     * Creates <code>List</code> of <code>CollectionCertStores</code>
     *
     * @return The list created
     *
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     */
//    public static List<CertStore> getCollectionCertStoresList()
//        throws InvalidAlgorithmParameterException,
//               NoSuchAlgorithmException {
//        CertStore cs = CertStore.getInstance("Collection",
//                new CollectionCertStoreParameters());
//        ArrayList<CertStore> l = new ArrayList<CertStore>();
//        if (!l.add(cs)) {
//            throw new RuntimeException("Could not create cert stores list");
//        }
//        return l;
//    }

    /**
     * Creates stub implementation of the <code>PKIXCertPathChecker</code>
     *
     * @return Stub implementation of the <code>PKIXCertPathChecker</code>
     */
//    public static PKIXCertPathChecker getTestCertPathChecker() {
//        // stub implementation for testing purposes only
//        return new PKIXCertPathChecker() {
//            private boolean forward = false;
//
//
//            @SuppressWarnings({"unused", "unchecked"})
//            public void check(Certificate arg0, Collection arg1)
//                    throws CertPathValidatorException {
//            }
//
//            public Set<String> getSupportedExtensions() {
//                return null;
//            }
//
//            @SuppressWarnings("unused")
//            public void init(boolean arg0) throws CertPathValidatorException {
//                forward = arg0;
//            }
//
//            public boolean isForwardCheckingSupported() {
//                // just to check this checker state
//                return forward;
//            }
//        };
//    }

    /**
     * Creates policy tree stub containing two <code>PolicyNode</code>s
     * for testing purposes
     *
     * @return root <code>PolicyNode</code> of the policy tree
     */
//    public static PolicyNode getPolicyTree() {
//        return new PolicyNode() {
//            final PolicyNode parent = this;
//            public int getDepth() {
//                // parent
//                return 0;
//            }
//
//            public boolean isCritical() {
//                return false;
//            }
//
//            public String getValidPolicy() {
//                return null;
//            }
//
//            public PolicyNode getParent() {
//                return null;
//            }
//
//            public Iterator<PolicyNode> getChildren() {
//                PolicyNode child = new PolicyNode() {
//                    public int getDepth() {
//                        // child
//                        return 1;
//                    }
//
//                    public boolean isCritical() {
//                        return false;
//                    }
//
//                    public String getValidPolicy() {
//                        return null;
//                    }
//
//                    public PolicyNode getParent() {
//                        return parent;
//                    }
//
//                    public Iterator<PolicyNode> getChildren() {
//                        return null;
//                    }
//
//                    public Set<String> getExpectedPolicies() {
//                        return null;
//                    }
//
//                    public Set<? extends PolicyQualifierInfo> getPolicyQualifiers() {
//                        return null;
//                    }
//                };
//                HashSet<PolicyNode> s = new HashSet<PolicyNode>();
//                s.add(child);
//                return s.iterator();
//            }
//
//            public Set<String> getExpectedPolicies() {
//                return null;
//            }
//
//            public Set<? extends PolicyQualifierInfo> getPolicyQualifiers() {
//                return null;
//            }
//        };
//    }
    // X.509 encoded certificate
    private static final String ENCODED_X509_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDHTCCAtsCBEFT72swCwYHKoZIzjgEAwUAMHQxCzAJBgNVBAYTAlJVMQwwCgYDVQQIEwNOU08x\n"
            + "FDASBgNVBAcTC05vdm9zaWJpcnNrMQ4wDAYDVQQKEwVJbnRlbDEVMBMGA1UECxMMRFJMIFNlY3Vy\n"
            + "aXR5MRowGAYDVQQDExFWbGFkaW1pciBNb2xvdGtvdjAeFw0wNDA5MjQwOTU2NTlaFw0wNjA1MTcw\n"
            + "OTU2NTlaMHQxCzAJBgNVBAYTAlJVMQwwCgYDVQQIEwNOU08xFDASBgNVBAcTC05vdm9zaWJpcnNr\n"
            + "MQ4wDAYDVQQKEwVJbnRlbDEVMBMGA1UECxMMRFJMIFNlY3VyaXR5MRowGAYDVQQDExFWbGFkaW1p\n"
            + "ciBNb2xvdGtvdjCCAbgwggEsBgcqhkjOOAQBMIIBHwKBgQD9f1OBHXUSKVLfSpwu7OTn9hG3Ujzv\n"
            + "RADDHj+AtlEmaUVdQCJR+1k9jVj6v8X1ujD2y5tVbNeBO4AdNG/yZmC3a5lQpaSfn+gEexAiwk+7\n"
            + "qdf+t8Yb+DtX58aophUPBPuD9tPFHsMCNVQTWhaRMvZ1864rYdcq7/IiAxmd0UgBxwIVAJdgUI8V\n"
            + "IwvMspK5gqLrhAvwWBz1AoGBAPfhoIXWmz3ey7yrXDa4V7l5lK+7+jrqgvlXTAs9B4JnUVlXjrrU\n"
            + "WU/mcQcQgYC0SRZxI+hMKBYTt88JMozIpuE8FnqLVHyNKOCjrh4rs6Z1kW6jfwv6ITVi8ftiegEk\n"
            + "O8yk8b6oUZCJqIPf4VrlnwaSi2ZegHtVJWQBTDv+z0kqA4GFAAKBgQDiNmj9jgWu1ILYqYWcUhNN\n"
            + "8CjjRitf80yWP/s/565wZz3anb2w72jum63mdShDko9eOOOd1hiVuiBnNhSL7D6JfIYBJvNXr1av\n"
            + "Gw583BBv12OBgg0eAW/GRWBn2Ak2JjsoBc5x2c1HAEufakep7T6RoC+n3lqbKPKyHWVdfqQ9KTAL\n"
            + "BgcqhkjOOAQDBQADLwAwLAIUaRS3C9dXcMbrOAhmidFBr7oMvH0CFEC3LUwfLJX5gY8P6uxpkPx3\n"
            + "JDSM\n" + "-----END CERTIFICATE-----\n";

    public static byte[] getEncodedX509Certificate() {
        return ENCODED_X509_CERTIFICATE.getBytes();
    }

    /**
     * Returns X.509 certificate encoding corresponding to version v1.
     *
     * Certificate encoding was created by hands according to X.509 Certificate
     * ASN.1 notation. The certificate encoding has the following encoded
     * field values:<br>
     * - version: 1<br>
     * - serialNumber: 5<br>
     * - issuer: CN=Z<br>
     * - notBefore: 13 Dec 1999 14:15:16<br>
     * - notAfter: 01 Jan 2000 00:00:00<br>
     * - subject: CN=Y<br>
     *
     * @return X.509 certificate encoding corresponding to version v1.
     */
    public static byte[] getX509Certificate_v1() {
        return new byte[] {
        // Certificate: SEQUENCE
            0x30, 0x6B,

            //
            // TBSCertificate: SEQUENCE {
            //
            0x30, 0x5C,

            // version: [0] EXPLICIT Version DEFAULT v1
            (byte) 0xA0, 0x03, 0x02, 0x01, 0x00,

            // serialNumber: CertificateSerialNumber
            0x02, 0x01, 0x05,

            // signature: AlgorithmIdentifier
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY

            //issuer: Name
            0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
            0x13, 0x01, 0x5A, // CN=Z

            //validity: Validity
            0x30, 0x1E, // SEQUENCE
            // notBefore: UTCTime
            0x17, 0x0D, 0x39, 0x39, 0x31, 0x32, 0x31, 0x33, 0x31, 0x34, 0x31,
            0x35, 0x31, 0x36, 0x5A, // 13 Dec 1999 14:15:16
            // notAfter:  UTCTime
            0x17, 0x0D, 0x30, 0x30, 0x30, 0x31, 0x30, 0x31, 0x30, 0x30, 0x30,
            0x30, 0x30, 0x30, 0x5A, // 01 Jan 2000 00:00:00

            //subject: Name
            0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
            0x13, 0x01, 0x59, // CN=Y
            //SubjectPublicKeyInfo  ::=  SEQUENCE  {
            //    algorithm            AlgorithmIdentifier,
            //    subjectPublicKey     BIT STRING  }
            0x30, 0x0D, // SEQUENCE
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY
            0x03, 0x02, 0x00, 0x01, // subjectPublicKey

            // issuerUniqueID - missed
            // subjectUniqueID - missed
            // extensions - missed

            // } end TBSCertificate

            //
            // signatureAlgorithm: AlgorithmIdentifier
            //
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY

            //
            // signature: BIT STRING
            //
            0x03, 0x02, 0x00, 0x01 };
    }

    /**
     * Returns X.509 certificate encoding corresponding to version v3.
     *
     * Certificate encoding was created by hands according to X.509 Certificate
     * ASN.1 notation. The certificate encoding has the following encoded
     * field values:<br>
     * - version: 3<br>
     * - serialNumber: 5<br>
     * - issuer: CN=Z<br>
     * - notBefore: 13 Dec 1999 14:15:16<br>
     * - notAfter: 01 Jan 2000 00:00:00<br>
     * - subject: CN=Y<br>
     * - extensions:
     *       1) AuthorityKeyIdentifier(OID=2.5.29.35): no values in it(empty sequence)
     *
     * @return X.509 certificate encoding corresponding to version v3.
     */
    public static byte[] getX509Certificate_v3() {
        return new byte[] {
        // Certificate: SEQUENCE
            0x30, 0x7D,

            //
            // TBSCertificate: SEQUENCE {
            //
            0x30, 0x6E,

            // version: [0] EXPLICIT Version DEFAULT v1
            (byte) 0xA0, 0x03, 0x02, 0x01, 0x02,

            // serialNumber: CertificateSerialNumber
            0x02, 0x01, 0x05,

            // signature: AlgorithmIdentifier
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY

            //issuer: Name
            0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
            0x13, 0x01, 0x5A, // CN=Z

            //validity: Validity
            0x30, 0x1E, // SEQUENCE
            // notBefore: UTCTime
            0x17, 0x0D, 0x39, 0x39, 0x31, 0x32, 0x31, 0x33, 0x31, 0x34, 0x31,
            0x35, 0x31, 0x36, 0x5A, // 13 Dec 1999 14:15:16
            // notAfter:  UTCTime
            0x17, 0x0D, 0x30, 0x30, 0x30, 0x31, 0x30, 0x31, 0x30, 0x30, 0x30,
            0x30, 0x30, 0x30, 0x5A, // 01 Jan 2000 00:00:00

            //subject: Name
            0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
            0x13, 0x01, 0x59, // CN=Y
            //SubjectPublicKeyInfo  ::=  SEQUENCE  {
            //    algorithm            AlgorithmIdentifier,
            //    subjectPublicKey     BIT STRING  }
            0x30, 0x0D, // SEQUENCE
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY
            0x03, 0x02, 0x00, 0x01, // subjectPublicKey

            // issuerUniqueID - missed
            // subjectUniqueID - missed
            // extensions : [3]  EXPLICIT Extensions OPTIONAL
            (byte) 0xA3, 0x10,
            // Extensions  ::=  SEQUENCE SIZE (1..MAX) OF Extension
            0x30, 0x0E,
            // Extension  ::=  SEQUENCE  {
            // extnID      OBJECT IDENTIFIER,
            // critical    BOOLEAN DEFAULT FALSE,
            // extnValue   OCTET STRING  }

            // 1) AuthorityKeyIdentifier extension (see HARMONY-3384)
            0x30, 0x0C,
            0x06, 0x03, 0x55, 0x1D, 0x23, // OID = 2.5.29.35
            0x01, 0x01, 0x00, // critical = FALSE
            0x04, 0x02, 0x30, 0x00, // extnValue: MUST be empty sequence
            // missed: keyIdentifier
            // missed: authorityCertIssuer
            // missed" authorityCertSerialNumber

            // } end TBSCertificate

            //
            // signatureAlgorithm: AlgorithmIdentifier
            //
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY

            //
            // signature: BIT STRING
            //
            0x03, 0x02, 0x00, 0x01 };
    }

    /**
     * Returns X.509 CRL encoding corresponding to version v1.
     *
     * CRL encoding was created by hands according to X.509 CRL ASN.1
     * notation. The CRL encoding has the following encoded field values:<br>
     * - version: 1<br>
     * - issuer: CN=Z<br>
     * - thisUpdate: 01 Jan 2001 01:02:03<br>
     *
     * @return X.509 CRL encoding corresponding to version v1.
     */
    public static byte[] getX509CRL_v1() {
        return new byte[] {
                //CertificateList: SEQUENCE
                0x30, 0x35,

                // TBSCertList: SEQUENCE
                0x30, 0x27,
                // Version: INTEGER OPTIONAL
                // 0x02, 0x01, 0x01, - missed here cause it is v1
                // signature: AlgorithmIdentifier
                0x30, 0x06, // SEQUENCE
                0x06, 0x01, 0x01, // OID
                0x01, 0x01, 0x11, // ANY
                // issuer: Name
                0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x03, 0x13, 0x01, 0x5A, // CN=Z
                // thisUpdate: ChoiceOfTime
                // GeneralizedTime: 01 Jan 2001 01:02:03
                0x18, 0x0F, 0x32, 0x30, 0x30, 0x31, 0x30, 0x31, 0x30, 0x31,
                0x30, 0x31, 0x30, 0x32, 0x30, 0x33, 0x5A,

                // nextUpdate - missed
                // revokedCertificates - missed
                // crlExtensions - missed

                // signatureAlgorithm: AlgorithmIdentifier
                0x30, 0x06, // SEQUENCE
                0x06, 0x01, 0x01, //OID
                0x01, 0x01, 0x11, //ANY
                // signature: BIT STRING
                0x03, 0x02, 0x00, 0x01 };
    }
    //--------------------------------------------------------------------------

    // Second example
    /**
     * Certificate:
     * <pre>
     * $ openssl req -x509 -nodes -days 365 -subj '/C=AN/ST=Android/O=Android/OU=Android/CN=Android/emailAddress=android' -newkey rsa:1024 -keyout root.pem -out root.pem -text -days 36500
     * Generating a 1024 bit RSA private key
     * ..........................................++++++
     * .................++++++
     * writing new private key to 'root.pem'
     * -----BEGIN RSA PRIVATE KEY-----
     * MIICXwIBAAKBgQDKS+qP2kgqYBtwY4QoJ5p0yyEl35sBr2ZKtAWn6SL4vXgvaIrj
     * K7vG93CvG239bXfacniGMEBitedBlcqjdPREEY0DQn3jLXyAOd3tnlKcutNH3RjA
     * fPlnDWNGKLnDdSd9QZEc0G1MsMg/HrERPm1hMfZQG85zdtbYmi2CJ/jS5wIDAQAB
     * AoGBAIZhvdSHjS7RHwkeonjGLh1tnnx5OI/7AzmWsrci8L9JpZ/gk3pq39dBIhLA
     * ZuVVpatwJU4GmY65BYEUz0Kb+3JY0PXagypwQKuWs9wb9C0aRnDVy9DNXkbJ+D+L
     * DNvyZAG5BNknZapxsFSenR5UO4BY08wIsdBtWD/B7YcMTuvxAkEA9zKP18pJCmku
     * TUDTJkonF/fGvI4PvsBm6YFyINb130yGzKJKCcEn5j2Fm+wF+lGY7nmtUIgQekRm
     * WkwbjG/v3wJBANGACjKFVIFvuXH6EoyWx90uYw9C8+m2jOtrRaAMfRyUanCvF2Li
     * ZYOLThPcxv/QvvQAa7RKJjxsK69Ajm+b3fkCQQCR7xWgTVmlfcbJ8LU265v8uFhp
     * RGzjLe8Td0oLPRxWQXVrJXwUGiYV9MgF7ubwim+AifDZlBo2NF9Ae6Hf3M19AkEA
     * nJEGDe+a0gj/HHD5f9wHjgLmwTcWNmnZMu8+X3g14DACxCf2YE4183MebLWoevI0
     * YwIVe+2WWb21gAnM6RghcQJBALq0RZcYkZoQA8qr9TPuuMzi+fF3Y+4m/pDDcCd5
     * zXbsroEZPdWPfAXKT95juW9yKdVzeOZHO1uwRWmQ9ZlPMhY=
     * -----END RSA PRIVATE KEY-----
     * -----
     * Certificate:
     *     Data:
     *         Version: 3 (0x2)
     *         Serial Number:
     *             8a:12:37:ed:2d:ad:02:6e
     *         Signature Algorithm: sha1WithRSAEncryption
     *         Issuer: C=AN, ST=Android, O=Android, OU=Android, CN=Android/emailAddress=android
     *         Validity
     *             Not Before: Oct  4 02:20:28 2010 GMT
     *             Not After : Sep 10 02:20:28 2110 GMT
     *         Subject: C=AN, ST=Android, O=Android, OU=Android, CN=Android/emailAddress=android
     *         Subject Public Key Info:
     *             Public Key Algorithm: rsaEncryption
     *             RSA Public Key: (1024 bit)
     *                 Modulus (1024 bit):
     *                     00:ca:4b:ea:8f:da:48:2a:60:1b:70:63:84:28:27:
     *                     9a:74:cb:21:25:df:9b:01:af:66:4a:b4:05:a7:e9:
     *                     22:f8:bd:78:2f:68:8a:e3:2b:bb:c6:f7:70:af:1b:
     *                     6d:fd:6d:77:da:72:78:86:30:40:62:b5:e7:41:95:
     *                     ca:a3:74:f4:44:11:8d:03:42:7d:e3:2d:7c:80:39:
     *                     dd:ed:9e:52:9c:ba:d3:47:dd:18:c0:7c:f9:67:0d:
     *                     63:46:28:b9:c3:75:27:7d:41:91:1c:d0:6d:4c:b0:
     *                     c8:3f:1e:b1:11:3e:6d:61:31:f6:50:1b:ce:73:76:
     *                     d6:d8:9a:2d:82:27:f8:d2:e7
     *                 Exponent: 65537 (0x10001)
     *         X509v3 extensions:
     *             X509v3 Subject Key Identifier:
     *                 14:7D:36:ED:63:44:BF:4F:DB:7D:28:96:78:6A:E7:EC:CE:2C:40:BF
     *             X509v3 Authority Key Identifier:
     *                 keyid:14:7D:36:ED:63:44:BF:4F:DB:7D:28:96:78:6A:E7:EC:CE:2C:40:BF
     *                 DirName:/C=AN/ST=Android/O=Android/OU=Android/CN=Android/emailAddress=android
     *                 serial:8A:12:37:ED:2D:AD:02:6E
     *
     *             X509v3 Basic Constraints:
     *                 CA:TRUE
     *     Signature Algorithm: sha1WithRSAEncryption
     *         7c:f2:84:c0:ee:40:a5:b9:94:85:19:ab:36:02:1d:17:4b:98:
     *         f9:b9:c8:c5:1a:b0:c1:4f:0f:1d:1c:e8:c4:cf:c7:87:52:19:
     *         9e:64:55:35:bb:34:e1:38:2f:27:08:c5:ca:e7:97:02:90:fd:
     *         27:cd:8e:5a:08:40:f5:34:ff:70:65:c4:d6:1f:70:4f:d6:2c:
     *         cb:28:d8:ed:91:b7:eb:35:06:cd:0e:02:a8:51:cd:b7:3e:f9:
     *         85:16:97:31:7b:42:4c:cb:6f:de:4b:dd:ae:5e:9d:ef:84:83:
     *         89:f9:0f:a6:5f:e4:93:cc:30:b5:e9:1d:f4:08:f4:e6:e9:58:
     *         4b:ba
     * -----BEGIN CERTIFICATE-----
     * MIIDLTCCApagAwIBAgIJAIoSN+0trQJuMA0GCSqGSIb3DQEBBQUAMG0xCzAJBgNV
     * BAYTAkFOMRAwDgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYD
     * VQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMRYwFAYJKoZIhvcNAQkBFgdh
     * bmRyb2lkMCAXDTEwMTAwNDAyMjAyOFoYDzIxMTAwOTEwMDIyMDI4WjBtMQswCQYD
     * VQQGEwJBTjEQMA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4G
     * A1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEWMBQGCSqGSIb3DQEJARYH
     * YW5kcm9pZDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAykvqj9pIKmAbcGOE
     * KCeadMshJd+bAa9mSrQFp+ki+L14L2iK4yu7xvdwrxtt/W132nJ4hjBAYrXnQZXK
     * o3T0RBGNA0J94y18gDnd7Z5SnLrTR90YwHz5Zw1jRii5w3UnfUGRHNBtTLDIPx6x
     * ET5tYTH2UBvOc3bW2Jotgif40ucCAwEAAaOB0jCBzzAdBgNVHQ4EFgQUFH027WNE
     * v0/bfSiWeGrn7M4sQL8wgZ8GA1UdIwSBlzCBlIAUFH027WNEv0/bfSiWeGrn7M4s
     * QL+hcaRvMG0xCzAJBgNVBAYTAkFOMRAwDgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQK
     * EwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMRYw
     * FAYJKoZIhvcNAQkBFgdhbmRyb2lkggkAihI37S2tAm4wDAYDVR0TBAUwAwEB/zAN
     * BgkqhkiG9w0BAQUFAAOBgQB88oTA7kCluZSFGas2Ah0XS5j5ucjFGrDBTw8dHOjE
     * z8eHUhmeZFU1uzThOC8nCMXK55cCkP0nzY5aCED1NP9wZcTWH3BP1izLKNjtkbfr
     * NQbNDgKoUc23PvmFFpcxe0JMy2/eS92uXp3vhIOJ+Q+mX+STzDC16R30CPTm6VhL
     * ug==
     * -----END CERTIFICATE-----
     * $
     * </pre>
     */
    public static final String rootCert = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIIDLTCCApagAwIBAgIJAIoSN+0trQJuMA0GCSqGSIb3DQEBBQUAMG0xCzAJBgNV\n"
            + "BAYTAkFOMRAwDgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYD\n"
            + "VQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMRYwFAYJKoZIhvcNAQkBFgdh\n"
            + "bmRyb2lkMCAXDTEwMTAwNDAyMjAyOFoYDzIxMTAwOTEwMDIyMDI4WjBtMQswCQYD\n"
            + "VQQGEwJBTjEQMA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4G\n"
            + "A1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEWMBQGCSqGSIb3DQEJARYH\n"
            + "YW5kcm9pZDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAykvqj9pIKmAbcGOE\n"
            + "KCeadMshJd+bAa9mSrQFp+ki+L14L2iK4yu7xvdwrxtt/W132nJ4hjBAYrXnQZXK\n"
            + "o3T0RBGNA0J94y18gDnd7Z5SnLrTR90YwHz5Zw1jRii5w3UnfUGRHNBtTLDIPx6x\n"
            + "ET5tYTH2UBvOc3bW2Jotgif40ucCAwEAAaOB0jCBzzAdBgNVHQ4EFgQUFH027WNE\n"
            + "v0/bfSiWeGrn7M4sQL8wgZ8GA1UdIwSBlzCBlIAUFH027WNEv0/bfSiWeGrn7M4s\n"
            + "QL+hcaRvMG0xCzAJBgNVBAYTAkFOMRAwDgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQK\n"
            + "EwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMRYw\n"
            + "FAYJKoZIhvcNAQkBFgdhbmRyb2lkggkAihI37S2tAm4wDAYDVR0TBAUwAwEB/zAN\n"
            + "BgkqhkiG9w0BAQUFAAOBgQB88oTA7kCluZSFGas2Ah0XS5j5ucjFGrDBTw8dHOjE\n"
            + "z8eHUhmeZFU1uzThOC8nCMXK55cCkP0nzY5aCED1NP9wZcTWH3BP1izLKNjtkbfr\n"
            + "NQbNDgKoUc23PvmFFpcxe0JMy2/eS92uXp3vhIOJ+Q+mX+STzDC16R30CPTm6VhL\n"
            + "ug==\n"
            + "-----END CERTIFICATE-----\n";

    /**
     * Certificate:
     * <pre>
     * $ openssl req -nodes -days 365 -subj '/C=AN/ST=Android/L=Android/O=Android/OU=Android/CN=Android Certificate/emailAddress=android' -newkey rsa:1024 -keyout certreq.pem -out certreq.pem -text -days 36500
     * Generating a 1024 bit RSA private key
     * .......++++++
     * ......................++++++
     * writing new private key to 'certreq.pem'
     * -----
     * $ openssl x509 -req -in certreq.pem -CA root.pem -CAcreateserial -out cert.pem -days 36500
     * Signature ok
     * subject=/C=AN/ST=Android/L=Android/O=Android/OU=Android/CN=Android Certificate/emailAddress=android
     * Getting Private key
     * $ rm root.srl
     * $ openssl rsa -in certreq.pem
     * writing RSA key
     * -----BEGIN RSA PRIVATE KEY-----
     * MIICXQIBAAKBgQDGvQZRB7fsuLvnZ0Sx43sTCkvwv/SEYrzRumyV16OC+lvKGC2X
     * lYW9qv7of88hqSVq5823MB+uEP1xZLWaiKkYyEn72RwgV/HqB8KEgGYXEbMKKzUv
     * j0D1X8kZ/EDGqsZjFKlk/7sZYcg3UqCcGUiEEszTadhyJ6FcowHM1EhrcQIDAQAB
     * AoGAS4CQn8Qw6ewc5wLipDpqDYfB5grnGExys7MBgcPUyPPYX2TkHUye7LnD8gxs
     * YrtiDcVW8BuGTZkC0EuUesskgiwGLimNiU3vU3LwH7OvtfUTMdvhv9nd2GFlfiQo
     * PfwhITZ85GwhDkhiBBXjToDcNc0ntXVgACNAKU1ZlJyoyukCQQDwsGmD0GwKFtJH
     * cGXI+IK0aB+pXjujZJU/Ikg+eTPMSWDsKD6ReZu9uJJc8W36Xiki/No1/NZvj0gB
     * MwgIkwh7AkEA02FzaGcWLFSHaRfV1wpx1F3Iuu3X2wWqTzBlhGG9ZDQyy7gWZqHJ
     * jElCdajiMnbh0mk62hobYy4FcLuvkkJWAwJBAK7FKpkQaqMY1zAQqZg4+4/MW9E8
     * H8oRa14gopzanYYlcj+JKYWw7CnjMERU+yrl3LEPMdQp9/uh6wMT7y1qtqkCQCNG
     * mxTsRzYEsUhnkuc9Nfvj3tDbSm+hxWdLw1VRXmLvlx6KTSq5i0IfI7kxAva7Ajq0
     * Fv845iMqFfxXRhiZe3MCQQCxD0vLzEBegLQPgiavGXfBnRPrRrXgkuAJg7Fq/1Vt
     * 3InSGat3Tv8GW+pCWWVgmV8iQ4wWReg+Bd03SCSP5uAY
     * -----END RSA PRIVATE KEY-----
     * $ openssl x509 -in cert.pem -text
     * Certificate:
     *     Data:
     *         Version: 1 (0x0)
     *         Serial Number:
     *             89:34:5f:d5:01:2e:a2:2b
     *         Signature Algorithm: sha1WithRSAEncryption
     *         Issuer: C=AN, ST=Android, O=Android, OU=Android, CN=Android/emailAddress=android
     *         Validity
     *             Not Before: Oct  4 04:41:54 2010 GMT
     *             Not After : Sep 10 04:41:54 2110 GMT
     *         Subject: C=AN, ST=Android, L=Android, O=Android, OU=Android, CN=Android Certificate/emailAddress=android
     *         Subject Public Key Info:
     *             Public Key Algorithm: rsaEncryption
     *             RSA Public Key: (1024 bit)
     *                 Modulus (1024 bit):
     *                     00:c6:bd:06:51:07:b7:ec:b8:bb:e7:67:44:b1:e3:
     *                     7b:13:0a:4b:f0:bf:f4:84:62:bc:d1:ba:6c:95:d7:
     *                     a3:82:fa:5b:ca:18:2d:97:95:85:bd:aa:fe:e8:7f:
     *                     cf:21:a9:25:6a:e7:cd:b7:30:1f:ae:10:fd:71:64:
     *                     b5:9a:88:a9:18:c8:49:fb:d9:1c:20:57:f1:ea:07:
     *                     c2:84:80:66:17:11:b3:0a:2b:35:2f:8f:40:f5:5f:
     *                     c9:19:fc:40:c6:aa:c6:63:14:a9:64:ff:bb:19:61:
     *                     c8:37:52:a0:9c:19:48:84:12:cc:d3:69:d8:72:27:
     *                     a1:5c:a3:01:cc:d4:48:6b:71
     *                 Exponent: 65537 (0x10001)
     *     Signature Algorithm: sha1WithRSAEncryption
     *         80:06:54:ba:4c:a2:0d:2e:6b:d5:b0:b1:89:b2:fa:c2:fd:d6:
     *         02:ab:74:af:fb:1c:bc:47:43:58:89:57:80:ad:59:79:e9:2e:
     *         d9:60:a7:a6:0f:9c:10:9f:e1:80:a1:66:19:59:7e:11:28:17:
     *         17:0a:1d:e9:8d:78:e8:c2:61:36:03:fc:42:b1:54:bd:28:39:
     *         3c:48:fd:3c:79:e7:ca:1a:16:c3:8a:77:42:07:96:14:8c:d2:
     *         51:ca:8e:db:b8:82:31:84:5e:3f:68:b1:a5:f0:96:ae:a9:ca:
     *         86:f3:01:76:63:98:65:dd:41:81:11:d7:71:c8:ae:17:c7:20:
     *         e7:22
     * -----BEGIN CERTIFICATE-----
     * MIICcjCCAdsCCQCJNF/VAS6iKzANBgkqhkiG9w0BAQUFADBtMQswCQYDVQQGEwJB
     * TjEQMA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMH
     * QW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEWMBQGCSqGSIb3DQEJARYHYW5kcm9p
     * ZDAgFw0xMDEwMDQwNDQxNTRaGA8yMTEwMDkxMDA0NDE1NFowgYsxCzAJBgNVBAYT
     * AkFOMRAwDgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQHEwdBbmRyb2lkMRAwDgYDVQQK
     * EwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRwwGgYDVQQDExNBbmRyb2lkIENl
     * cnRpZmljYXRlMRYwFAYJKoZIhvcNAQkBFgdhbmRyb2lkMIGfMA0GCSqGSIb3DQEB
     * AQUAA4GNADCBiQKBgQDGvQZRB7fsuLvnZ0Sx43sTCkvwv/SEYrzRumyV16OC+lvK
     * GC2XlYW9qv7of88hqSVq5823MB+uEP1xZLWaiKkYyEn72RwgV/HqB8KEgGYXEbMK
     * KzUvj0D1X8kZ/EDGqsZjFKlk/7sZYcg3UqCcGUiEEszTadhyJ6FcowHM1EhrcQID
     * AQABMA0GCSqGSIb3DQEBBQUAA4GBAIAGVLpMog0ua9WwsYmy+sL91gKrdK/7HLxH
     * Q1iJV4CtWXnpLtlgp6YPnBCf4YChZhlZfhEoFxcKHemNeOjCYTYD/EKxVL0oOTxI
     * /Tx558oaFsOKd0IHlhSM0lHKjtu4gjGEXj9osaXwlq6pyobzAXZjmGXdQYER13HI
     * rhfHIOci
     * -----END CERTIFICATE-----
     * $
     * </pre>
     */
    public static final String  endCert = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIICcjCCAdsCCQCJNF/VAS6iKzANBgkqhkiG9w0BAQUFADBtMQswCQYDVQQGEwJB\n"
            + "TjEQMA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMH\n"
            + "QW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEWMBQGCSqGSIb3DQEJARYHYW5kcm9p\n"
            + "ZDAgFw0xMDEwMDQwNDQxNTRaGA8yMTEwMDkxMDA0NDE1NFowgYsxCzAJBgNVBAYT\n"
            + "AkFOMRAwDgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQHEwdBbmRyb2lkMRAwDgYDVQQK\n"
            + "EwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRwwGgYDVQQDExNBbmRyb2lkIENl\n"
            + "cnRpZmljYXRlMRYwFAYJKoZIhvcNAQkBFgdhbmRyb2lkMIGfMA0GCSqGSIb3DQEB\n"
            + "AQUAA4GNADCBiQKBgQDGvQZRB7fsuLvnZ0Sx43sTCkvwv/SEYrzRumyV16OC+lvK\n"
            + "GC2XlYW9qv7of88hqSVq5823MB+uEP1xZLWaiKkYyEn72RwgV/HqB8KEgGYXEbMK\n"
            + "KzUvj0D1X8kZ/EDGqsZjFKlk/7sZYcg3UqCcGUiEEszTadhyJ6FcowHM1EhrcQID\n"
            + "AQABMA0GCSqGSIb3DQEBBQUAA4GBAIAGVLpMog0ua9WwsYmy+sL91gKrdK/7HLxH\n"
            + "Q1iJV4CtWXnpLtlgp6YPnBCf4YChZhlZfhEoFxcKHemNeOjCYTYD/EKxVL0oOTxI\n"
            + "/Tx558oaFsOKd0IHlhSM0lHKjtu4gjGEXj9osaXwlq6pyobzAXZjmGXdQYER13HI\n"
            + "rhfHIOci\n"
            + "-----END CERTIFICATE-----\n";

    /**
     * a self signed certificate
     */
    public static X509Certificate rootCertificateSS;

    public static X509Certificate endCertificate;

    public static MyCRL crl;

//    public static X509CertSelector theCertSelector;
//
//    public static CertPathBuilder builder;
//    private static CertStore store;

//    public static void initCertPathSSCertChain() throws CertificateException,
//            InvalidAlgorithmParameterException, NoSuchAlgorithmException,
//            IOException {
//        // create certificates and CRLs
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        ByteArrayInputStream bi = new ByteArrayInputStream(rootCert.getBytes());
//        rootCertificateSS = (X509Certificate) cf.generateCertificate(bi);
//        bi = new ByteArrayInputStream(endCert.getBytes());
//        endCertificate = (X509Certificate) cf.generateCertificate(bi);
//        BigInteger revokedSerialNumber = BigInteger.valueOf(1);
//        crl = new MyCRL("X.509");
////        X509CRL rootCRL = X509CRL;
////        X509CRL interCRL = X509CRLExample.createCRL(interCert, interPair
////                .getPrivate(), revokedSerialNumber);
//
//        // create CertStore to support path building
//        List<Object> list = new ArrayList<Object>();
//
//        list.add(rootCertificateSS);
//        list.add(endCertificate);
//
//        CollectionCertStoreParameters params = new CollectionCertStoreParameters(
//                list);
//        store = CertStore.getInstance("Collection", params);
//
//        theCertSelector = new X509CertSelector();
//        theCertSelector.setCertificate(endCertificate);
//        theCertSelector.setIssuer(endCertificate.getIssuerX500Principal()
//                .getEncoded());
//
//        // build the path
//        builder = CertPathBuilder.getInstance("PKIX");
//
//    }
//
//    public static CertPathBuilder getCertPathBuilder() {
//        if (builder == null) {
//            throw new RuntimeException(
//            "Call initCertPathSSCertChain prior to initCertPathSSCertChain");
//        }
//        return builder;
//    }
//
//    public static CertPath buildCertPathSSCertChain() throws Exception {
//        return builder.build(getCertPathParameters()).getCertPath();
//    }
//
//    public static CertPathParameters getCertPathParameters()
//            throws InvalidAlgorithmParameterException {
//        if ((rootCertificateSS == null) || (theCertSelector == null)
//                || (builder == null)) {
//            throw new RuntimeException(
//                    "Call initCertPathSSCertChain prior to buildCertPath");
//        }
//        PKIXBuilderParameters buildParams = new PKIXBuilderParameters(
//                Collections.singleton(new TrustAnchor(rootCertificateSS, null)),
//                theCertSelector);
//
//        buildParams.addCertStore(store);
//        buildParams.setRevocationEnabled(false);
//
//        return buildParams;
//
//    }
}
