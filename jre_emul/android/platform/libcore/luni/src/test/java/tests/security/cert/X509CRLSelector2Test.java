package tests.security.cert;

import junit.framework.TestCase;

import org.apache.harmony.security.tests.support.cert.TestUtils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import sun.security.util.DerOutputStream;
import sun.security.x509.CRLNumberExtension;

public class X509CRLSelector2Test extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * constructor testing.
     *
     */
    public void testX509CRLSelector() {
        X509CRLSelector selector = new X509CRLSelector();
        assertNull(selector.getDateAndTime());
        assertNull(selector.getCertificateChecking());
        assertNull(selector.getIssuerNames());
        assertNull(selector.getIssuers());
        assertNull(selector.getMaxCRL());
        assertNull(selector.getMinCRL());
    }

    /**
     * addIssuer(X500Principal issuer) method testing. Tests if CRLs with
     * specified issuers match the selector, and if not specified issuer does
     * not match the selector.
     */
    public void testAddIssuerLjavax_security_auth_x500_X500Principal02() {
        X509CRLSelector selector = new X509CRLSelector();
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        CRL crl1 = new TestCRL(iss1);
        CRL crl2 = new TestCRL(iss2);

        selector.addIssuer(iss1);
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl1));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl2));
        selector.addIssuer(iss2);
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl2));
    }

    /**
     * addIssuerName(String name) method testing. Tests if CRLs with specified
     * issuers match the selector, and if not specified issuer does not match
     * the selector.
     */
    public void testAddIssuerNameLjava_lang_String03() {
        X509CRLSelector selector = new X509CRLSelector();
        String iss1 = "O=First Org.";
        String iss2 = "O=Second Org.";
        TestCRL crl1 = new TestCRL(new X500Principal(iss1));
        TestCRL crl2 = new TestCRL(new X500Principal(iss2));

        try {
            selector.addIssuerName(iss1);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl1));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl2));
        try {
            selector.addIssuerName(iss2);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl2));
    }

    /**
     * setIssuerNames(Collection <?> names) method testing. Tests if CRLs with
     * any issuers match the selector in the case of null issuerNames criteria,
     * if specified issuers match the selector, if not specified issuer does not
     * match the selector, and if the internal collection of issuer names is
     * copied during initialization.
     */
    @SuppressWarnings("unchecked")
    public void testSetIssuerNamesLjava_util_Collection02() {
        X509CRLSelector selector = new X509CRLSelector();
        String iss1 = "O=First Org.";
        byte[] iss2 = new byte[]
        // manually obtained DER encoding of "O=Second Org." issuer name;
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };

        String iss3 = "O=Third Org.";
        TestCRL crl1 = new TestCRL(new X500Principal(iss1));
        TestCRL crl2 = new TestCRL(new X500Principal(iss2));
        TestCRL crl3 = new TestCRL(new X500Principal(iss3));

        try {
            selector.setIssuerNames(null);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any CRL issuers should match in the case of null issuers.",
                selector.match(crl1) && selector.match(crl2));

        ArrayList issuers = new ArrayList(2);
        issuers.add(iss1);
        issuers.add(iss2);
        try {
            selector.setIssuerNames(issuers);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl1)
                && selector.match(crl2));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl3));
        issuers.add(iss3);
        assertFalse("The internal issuer collection is not protected "
                + "against the modifications.", selector.match(crl3));
    }

    /**
     * setIssuers(Collection <X500Principal> issuers) method testing. Tests if
     * CRLs with any issuers match the selector in the case of null issuerNames
     * criteria, if specified issuers match the selector, and if not specified
     * issuer does not match the selector.
     */
    public void testSetIssuersLjava_util_Collection() {
        X509CRLSelector selector = new X509CRLSelector();
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        X500Principal iss3 = new X500Principal("O=Third Org.");
        TestCRL crl1 = new TestCRL(iss1);
        TestCRL crl2 = new TestCRL(iss2);
        TestCRL crl3 = new TestCRL(iss3);

        selector.setIssuers(null);
        assertTrue("Any CRL issuers should match in the case of null issuers.",
                selector.match(crl1) && selector.match(crl2));

        ArrayList<X500Principal> issuers = new ArrayList<X500Principal>(2);
        issuers.add(iss1);
        issuers.add(iss2);
        selector.setIssuers(issuers);
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl1)
                && selector.match(crl2));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl3));
        issuers.add(iss3);
        assertFalse("The internal issuer collection is not protected "
                + "against the modifications.", selector.match(crl3));
    }

    /**
     * addIssuerName(byte[] name) method testing. Tests if CRLs with specified
     * issuers match the selector, and if not specified issuer does not match
     * the selector.
     */
    public void testAddIssuerName$B() {
        X509CRLSelector selector = new X509CRLSelector();
        byte[] iss1 = new byte[]
        // manually obtained DER encoding of "O=First Org." issuer name;
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] iss2 = new byte[]
        // manually obtained DER encoding of "O=Second Org." issuer name;
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        TestCRL crl1 = new TestCRL(new X500Principal(iss1));
        TestCRL crl2 = new TestCRL(new X500Principal(iss2));

        try {
            selector.addIssuerName(iss1);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl1));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl2));
        try {
            selector.addIssuerName(iss2);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl2));
    }

    /**
     * setMinCRLNumber(BigInteger minCRL) method testing. Tests if CRLs with any
     * crl number value match the selector in the case of null crlNumber
     * criteria, if specified minCRL value matches the selector, and if CRL with
     * inappropriate crlNumber value does not match the selector.
     */
    public void testSetMinCRLNumberLjava_math_BigInteger() {
        X509CRLSelector selector = new X509CRLSelector();
        BigInteger minCRL = new BigInteger("10000");
        CRL crl = new TestCRL(minCRL);

        selector.setMinCRLNumber(null);
        assertTrue("Any CRL should match in the case of null minCRLNumber.",
                selector.match(crl));
        selector.setMinCRLNumber(minCRL);
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl));
        selector.setMinCRLNumber(new BigInteger("10001"));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl));
    }

    /**
     * setMaxCRLNumber(BigInteger maxCRL) method testing. Tests if CRLs with any
     * crl number value match the selector in the case of null crlNumber
     * criteria, if specified maxCRL value matches the selector, and if CRL with
     * inappropriate crlNumber value does not match the selector.
     */
    public void testSetMaxCRLNumberLjava_math_BigInteger() {
        X509CRLSelector selector = new X509CRLSelector();
        BigInteger maxCRL = new BigInteger("10000");
        TestCRL crl = new TestCRL(maxCRL);

        selector.setMaxCRLNumber(null);
        assertTrue("Any CRL should match in the case of null minCRLNumber.",
                selector.match(crl));
        selector.setMaxCRLNumber(maxCRL);
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl));
        selector.setMaxCRLNumber(new BigInteger("9999"));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl));
    }

    /**
     * setDateAndTime(Date dateAndTime) method testing. Tests if CRLs with any
     * update dates match the selector in the case of null dateAndTime criteria,
     * if correct dates match and incorrect do not match the selector.
     */
    public void testSetDateAndTimeLjava_util_Date() {
        X509CRLSelector selector = new X509CRLSelector();
        TestCRL crl = new TestCRL(new Date(200), new Date(300));
        selector.setDateAndTime(null);
        assertTrue("Any CRL should match in the case of null dateAndTime.",
                selector.match(crl));
        selector.setDateAndTime(new Date(200));
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl));
        selector.setDateAndTime(new Date(250));
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl));
        selector.setDateAndTime(new Date(300));
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl));
        selector.setDateAndTime(new Date(150));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl));
        selector.setDateAndTime(new Date(350));
        assertFalse("The CRL should not match the selection criteria.",
                selector.match(crl));
    }

    /**
     * setCertificateChecking(X509Certificate) method testing.
     */
    public void testSetCertificateCheckingLjava_X509Certificate()
            throws CertificateException {
        X509CRLSelector selector = new X509CRLSelector();

        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate cert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));

        TestCRL crl = new TestCRL();
        selector.setCertificateChecking(cert);
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl));
        assertEquals(cert, selector.getCertificateChecking());

        selector.setCertificateChecking(null);
        assertTrue("The CRL should match the selection criteria.", selector
                .match(crl));
        assertNull(selector.getCertificateChecking());
    }

    /**
     * getIssuers() method testing. Tests if the method return null in the case
     * of not specified issuers, if the returned collection corresponds to the
     * specified issuers and this collection is unmodifiable.
     */
    public void testGetIssuers() {
        X509CRLSelector selector = new X509CRLSelector();
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        X500Principal iss3 = new X500Principal("O=Third Org.");
        assertNull("The collection should be null.", selector.getIssuers());
        selector.addIssuer(iss1);
        selector.addIssuer(iss2);
        Collection<X500Principal> result = selector.getIssuers();
        try {
            result.add(iss3);
            fail("The returned collection should be unmodifiable.");
        } catch (UnsupportedOperationException e) {
        }
        assertTrue("The collection should contain the specified DN.", result
                .contains(iss2));
    }

    /**
     * getIssuerNames() method testing. Tests if the method return null in the
     * case of not specified issuers, if the returned collection corresponds to
     * the specified issuers.
     */
    public void testGetIssuerNames() {
        X509CRLSelector selector = new X509CRLSelector();
        byte[] iss1 = new byte[]
        // manually obtained DER encoding of "O=First Org." issuer name;
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] iss2 = new byte[]
        // manually obtained DER encoding of "O=Second Org." issuer name;
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        assertNull("The collection should be null.", selector.getIssuerNames());
        try {
            selector.addIssuerName(iss1);
            selector.addIssuerName(iss2);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
        Collection<Object> result = selector.getIssuerNames();
        assertEquals("The collection should contain all of the specified DNs.",
                2, result.size());
    }

    /**
     * getMinCRL() method testing. Tests if the method return null in the case
     * of not specified minCRL criteria, and if the returned value corresponds
     * to the specified one.
     */
    public void testGetMinCRL() {
        X509CRLSelector selector = new X509CRLSelector();
        assertNull("Initially the minCRL should be null.", selector.getMinCRL());
        BigInteger minCRL = new BigInteger("10000");
        selector.setMinCRLNumber(minCRL);
        assertTrue("The result should be equal to specified.", minCRL
                .equals(selector.getMinCRL()));
    }

    /**
     * getMaxCRL() method testing. Tests if the method return null in the case
     * of not specified maxCRL criteria, and if the returned value corresponds
     * to the specified one.
     */
    public void testGetMaxCRL() {
        X509CRLSelector selector = new X509CRLSelector();
        assertNull("Initially the maxCRL should be null.", selector.getMaxCRL());
        BigInteger maxCRL = new BigInteger("10000");
        selector.setMaxCRLNumber(maxCRL);
        assertTrue("The result should be equal to specified.", maxCRL
                .equals(selector.getMaxCRL()));
    }

    /**
     * getDateAndTime() method testing. Tests if the method return null in the
     * case of not specified dateAndTime criteria, and if the returned value
     * corresponds to the specified one.
     */
    public void testGetDateAndTime() {
        X509CRLSelector selector = new X509CRLSelector();
        assertNull("Initially the dateAndTime criteria should be null.",
                selector.getDateAndTime());
        Date date = new Date(200);
        selector.setDateAndTime(date);
        assertTrue("The result should be equal to specified.", date
                .equals(selector.getDateAndTime()));
    }

    /**
     * getCertificateChecking() method testing.
     */
    public void testGetCertificateCheckingLjava_X509Certificate()
            throws CertificateException {
        X509CRLSelector selector = new X509CRLSelector();

        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate cert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));

        selector.setCertificateChecking(cert);
        assertEquals(cert, selector.getCertificateChecking());

        selector.setCertificateChecking(null);
        assertNull(selector.getCertificateChecking());
    }

    /**
     * match(CRL crl) method testing. Tests if the null object matches to the
     * selector or not.
     */
    public void testMatchLjava_security_cert_X509CRL() {
        X509CRLSelector selector = new X509CRLSelector();
        assertFalse("The null object should not match", selector
                .match((X509CRL) null));
    }

    /**
     * clone() method testing. Tests if the selector is cloned correctly: the
     * crl which matche to the initial selector should match to the clone and
     * the change of clone should not cause the change of initial selector.
     */
    public void testClone() {
        X509CRLSelector selector = new X509CRLSelector();
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        X500Principal iss3 = new X500Principal("O=Third Org.");
        BigInteger minCRL = new BigInteger("10000");
        BigInteger maxCRL = new BigInteger("10000");
        Date date = new Date(200);

        selector.addIssuer(iss1);
        selector.addIssuer(iss2);
        selector.setMinCRLNumber(minCRL);
        selector.setMaxCRLNumber(maxCRL);
        selector.setDateAndTime(date);

        X509CRLSelector clone = (X509CRLSelector) selector.clone();
        TestCRL crl = new TestCRL(iss1);
        crl.setCrlNumber(minCRL);
        crl.setUpdateDates(new Date(200), new Date(200));
        assertTrue("The specified CRL should match the clone selector.",
                selector.match(crl));

        clone.addIssuer(iss3);
        assertFalse("The changes of the clone selector should not cause "
                + "the changes of initial object", selector.getIssuerNames()
                .size() == 3);
    }
    public void testToString() {
        X509CRLSelector selector = new X509CRLSelector();
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        BigInteger minCRL = new BigInteger("10000");
        BigInteger maxCRL = new BigInteger("10000");
        Date date = new Date(200);

        selector.addIssuer(iss1);
        selector.addIssuer(iss2);
        selector.setMinCRLNumber(minCRL);
        selector.setMaxCRLNumber(maxCRL);
        selector.setDateAndTime(date);

        assertNotNull("The result should not be null.", selector.toString());
    }

    /**
     * The abstract class stub implementation.
     */
    private class TestCRL extends X509CRL {

        private X500Principal principal = null;

        private BigInteger crlNumber = null;

        private Date thisUpdate = null;

        private Date nextUpdate = null;

        public TestCRL() {
        }

        public TestCRL(X500Principal principal) {
            this.principal = principal;
        }

        public TestCRL(Date thisUpdate, Date nextUpdate) {
            setUpdateDates(thisUpdate, nextUpdate);
        }

        public TestCRL(BigInteger crlNumber) {
            setCrlNumber(crlNumber);
        }

        public void setUpdateDates(Date thisUpdate, Date nextUpdate) {
            this.thisUpdate = thisUpdate;
            this.nextUpdate = nextUpdate;
        }

        public void setCrlNumber(BigInteger crlNumber) {
            this.crlNumber = crlNumber;
        }

        public X500Principal getIssuerX500Principal() {
            return principal;
        }

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
            if ("2.5.29.20".equals(oid) && (crlNumber != null)) {
                DerOutputStream out = new DerOutputStream();
                try {
                    out.putOctetString((new CRLNumberExtension(crlNumber)).getExtensionValue());
                } catch (IOException e) {
                    throw new IllegalStateException("Unexpected IOException" , e);
                }
                return out.toByteArray();
            }
            return null;
        }

        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }

        public byte[] getEncoded() {
            return null;
        }

        @SuppressWarnings("unused")
        public void verify(PublicKey key) throws CRLException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }

        @SuppressWarnings("unused")
        public void verify(PublicKey key, String sigProvider)
                throws CRLException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }

        public int getVersion() {
            return 2;
        }

        public Principal getIssuerDN() {
            return null;
        }

        public Date getThisUpdate() {
            return thisUpdate;
        }

        public Date getNextUpdate() {
            return nextUpdate;
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
}
