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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package tests.security.cert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.tests.support.cert.TestUtils;

import tests.security.cert.X509CRL2Test.MyX509CRL;
/**
 */
public class X509CRLTest extends TestCase {

    private X509CRL tbt_crl;

    String certificate = "-----BEGIN CERTIFICATE-----\n"
        + "MIICZTCCAdICBQL3AAC2MA0GCSqGSIb3DQEBAgUAMF8xCzAJBgNVBAYTAlVTMSAw\n"
        + "HgYDVQQKExdSU0EgRGF0YSBTZWN1cml0eSwgSW5jLjEuMCwGA1UECxMlU2VjdXJl\n"
        + "IFNlcnZlciBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw05NzAyMjAwMDAwMDBa\n"
        + "Fw05ODAyMjAyMzU5NTlaMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv\n"
        + "cm5pYTESMBAGA1UEBxMJUGFsbyBBbHRvMR8wHQYDVQQKExZTdW4gTWljcm9zeXN0\n"
        + "ZW1zLCBJbmMuMSEwHwYDVQQLExhUZXN0IGFuZCBFdmFsdWF0aW9uIE9ubHkxGjAY\n"
        + "BgNVBAMTEWFyZ29uLmVuZy5zdW4uY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB\n"
        + "iQKBgQCofmdY+PiUWN01FOzEewf+GaG+lFf132UpzATmYJkA4AEA/juW7jSi+LJk\n"
        + "wJKi5GO4RyZoyimAL/5yIWDV6l1KlvxyKslr0REhMBaD/3Z3EsLTTEf5gVrQS6sT\n"
        + "WMoSZAyzB39kFfsB6oUXNtV8+UKKxSxKbxvhQn267PeCz5VX2QIDAQABMA0GCSqG\n"
        + "SIb3DQEBAgUAA34AXl3at6luiV/7I9MN5CXYoPJYI8Bcdc1hBagJvTMcmlqL2uOZ\n"
        + "H9T5hNMEL9Tk6aI7yZPXcw/xI2K6pOR/FrMp0UwJmdxX7ljV6ZtUZf7pY492UqwC\n"
        + "1777XQ9UEZyrKJvF5ntleeO0ayBqLGVKCWzWZX9YsXCpv47FNLZbupE=\n"
        + "-----END CERTIFICATE-----\n";

    ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
        .getBytes());

    /**
     * The stub class used for testing of non abstract methods.
     */
    private class TBTCRL extends X509CRL {
        public String toString() {
            return null;
        }

        public boolean isRevoked(Certificate cert) {
            return true;
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

        public byte[] getEncoded() {
            return null;
        }

        public void verify(PublicKey key)
                 throws CRLException, NoSuchAlgorithmException,
                        InvalidKeyException, NoSuchProviderException,
                        SignatureException
        {
        }

        public void verify(PublicKey key, String sigProvider)
                 throws CRLException, NoSuchAlgorithmException,
                        InvalidKeyException, NoSuchProviderException,
                        SignatureException
        {
        }

        public int getVersion() {
            return 2;
        }

        public Principal getIssuerDN() {
            return null;
        }

        public Date getThisUpdate() {
            return null;
        }

        public Date getNextUpdate() {
            return null;
        }

        public X509CRLEntry getRevokedCertificate(BigInteger serialNumber) {
            return null;
        }

        public Set<X509CRLEntry> getRevokedCertificates() {
            return null;
        }

        public byte[] getTBSCertList() {
            return null;
        }

        public byte[] getSignature() {
            return null;
        }

        public String getSigAlgName() {
            return null;
        }

        public String getSigAlgOID() {
            return null;
        }

        public byte[] getSigAlgParams() {
            return null;
        }
    }


    public X509CRLTest() {

    }

    public void setUp() {
        tbt_crl = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {1, 2, 3};
            }
        };
    }

    /**
     * getType() method testing. Tests that getType() method returns
     * the value "X.509"
     */
    public void testGetType() {
        assertEquals("The type of X509CRL should be X.509",
                                            tbt_crl.getType(), "X.509");
    }

    /**
     * equals(Object other) method testing. Tests the correctness of equal
     * operation: it should be reflexive, symmetric, transitive, consistent
     * and should be false on null object.
     */
    public void testEquals() {
        TBTCRL tbt_crl_1 = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {1, 2, 3};
            }
        };

        TBTCRL tbt_crl_2 = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {1, 2, 3};
            }
        };

        TBTCRL tbt_crl_3 = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {3, 2, 1};
            }
        };

        // checking for reflexive law:
        assertTrue("The equivalence relation should be reflexive.",
                                                    tbt_crl.equals(tbt_crl));

        assertEquals("The CRLs with equal encoded form should be equal",
                                                    tbt_crl, tbt_crl_1);
        // checking for symmetric law:
        assertTrue("The equivalence relation should be symmetric.",
                                                    tbt_crl_1.equals(tbt_crl));

        assertEquals("The CRLs with equal encoded form should be equal",
                                                    tbt_crl_1, tbt_crl_2);
        // checking for transitive law:
        assertTrue("The equivalence relation should be transitive.",
                                                    tbt_crl.equals(tbt_crl_2));

        assertFalse("Should not be equal to null object.",
                                                    tbt_crl.equals(null));

        assertFalse("The CRLs with differing encoded form should not be equal",
                                                    tbt_crl.equals(tbt_crl_3));
        assertFalse("The CRL should not be equals to the object which is not "
                    + "an instance of X509CRL", tbt_crl.equals(new Object()));
    }

    /**
     * hashCode() method testing. Tests that for equal objects hash codes
     * are equal.
     */
    public void testHashCode() {
        TBTCRL tbt_crl_1 = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {1, 2, 3};
            }
        };
        assertTrue("Equal objects should have the same hash codes.",
                                    tbt_crl.hashCode() == tbt_crl_1.hashCode());
    }

    /**
     * java.security.cert.X509CRL#getIssuerX500Principal()
     */
    public void testGetIssuerX500Principal() {
        // return valid encoding
        TBTCRL crl = new TBTCRL() {
            public byte[] getEncoded() {
                return TestUtils.getX509CRL_v1();
            }
        };

        assertEquals(new X500Principal("CN=Z"), crl.getIssuerX500Principal());
    }

    /**
     * getRevokedCertificate(X509Certificate certificate) method testing.
     * Check if the default implementation throws NullPointerException
     * on null input data.
     */
    // AndroidOnly("Test filed on RI: getRevokedCertificate throws " +
    //        "RuntimeException.")
    public void testGetRevokedCertificate() {
        TBTCRL crl = new TBTCRL() {
          @Override
          public byte[] getEncoded() {
            return TestUtils.getX509CRL_v1();
          }
        };
        try {
            crl.getRevokedCertificate((X509Certificate) null);
            fail("NullPointerException should be thrown "
                        + "in the case of null input data.");
        } catch (NullPointerException e) {
        }


        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(certArray);
            crl.getRevokedCertificate(cert);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    public void testAbstractMethods() {
        TBTCRL crl = new TBTCRL() {
            public byte[] getEncoded() {
                return TestUtils.getX509CRL_v1();
            }
        };

        try {
            crl.getEncoded();
            crl.getIssuerDN();
            crl.getNextUpdate();
            crl.getRevokedCertificate(BigInteger.ONE);
            crl.getRevokedCertificates();
            crl.getSigAlgName();
            crl.getSigAlgOID();
            crl.getSigAlgParams();
            crl.getSignature();
            crl.getTBSCertList();
            crl.getThisUpdate();
            crl.getVersion();

            crl.verify(null);
            crl.verify(null, "test");
        } catch (Exception e) {
            fail("Unexpected exception for constructor");
        }
    }

    public void testX509CRL() {
        try {
            TBTCRL crl = new TBTCRL();
            assertTrue(crl instanceof X509CRL);
        } catch (Exception e) {
            fail("Unexpected exception for constructor");
        }
    }

    public static Test suite() {
        return new TestSuite(X509CRLTest.class);
    }
}

