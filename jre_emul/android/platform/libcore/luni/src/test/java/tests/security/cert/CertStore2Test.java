package tests.security.cert;

import junit.framework.TestCase;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;

public class CertStore2Test extends TestCase {

    private static final String CERT_STORE_PROVIDER_NAME = "TestCertStoreProvider";
    private static final String CERT_STORE_NAME = "TestCertStore";

    Provider provider;

    protected void setUp() throws Exception {
        super.setUp();
        provider = new MyCertStoreProvider();
        Security.addProvider(provider);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(CERT_STORE_PROVIDER_NAME);
    }

    public void testGetInstanceStringCertStoreParameters() {
        try {
            CertStoreParameters parameters = new MyCertStoreParameters();
            CertStore certStore = CertStore.getInstance(CERT_STORE_NAME,
                    parameters);
            assertNotNull(certStore);
            assertNotNull(certStore.getCertStoreParameters());
            assertNotSame(parameters, certStore.getCertStoreParameters());
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        }

        try {
            CertStore certStore = CertStore.getInstance(CERT_STORE_NAME, null);
            assertNotNull(certStore);
            assertNull(certStore.getCertStoreParameters());
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }

        try {
            CertStore.getInstance("UnknownCertStore", null);
            fail("expected NoSuchAlgorithmException");
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            // ok
        }

        try {
            CertStore.getInstance(CERT_STORE_NAME,
                    new MyOtherCertStoreParameters());
            fail("expected InvalidAlgorithmParameterException");
        } catch (InvalidAlgorithmParameterException e) {
            // ok
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }
    }

    public void testGetInstanceStringCertStoreParametersString() {
        try {
            CertStoreParameters parameters = new MyCertStoreParameters();
            CertStore certStore = CertStore.getInstance(CERT_STORE_NAME,
                    parameters, CERT_STORE_PROVIDER_NAME);
            assertNotNull(certStore);
            assertNotNull(certStore.getCertStoreParameters());
            assertNotSame(parameters, certStore.getCertStoreParameters());
            assertEquals(CERT_STORE_PROVIDER_NAME, certStore.getProvider()
                    .getName());
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchProviderException e) {
            fail("unexpected exception: " + e);
        }

        try {
            CertStore certStore = CertStore.getInstance(CERT_STORE_NAME, null,
                    CERT_STORE_PROVIDER_NAME);
            assertNotNull(certStore);
            assertNull(certStore.getCertStoreParameters());
            assertEquals(CERT_STORE_PROVIDER_NAME, certStore.getProvider()
                    .getName());
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchProviderException e) {
            fail("unexpected exception: " + e);
        }

        try {
            CertStore.getInstance("UnknownCertStore",
                    new MyCertStoreParameters(), CERT_STORE_PROVIDER_NAME);
            fail("expected NoSuchAlgorithmException");
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            // ok
        } catch (NoSuchProviderException e) {
            fail("unexpected exception: " + e);
        }

        try {
            CertStore.getInstance(CERT_STORE_NAME, null,
                    "UnknownCertStoreProvider");
            fail("expected NoSuchProviderException");
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchProviderException e) {
            // ok
        }

        try {
            CertStore.getInstance(CERT_STORE_NAME,
                    new MyOtherCertStoreParameters(), CERT_STORE_PROVIDER_NAME);
        } catch (InvalidAlgorithmParameterException e) {
            // ok
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchProviderException e) {
            fail("unexpected exception: " + e);
        }


    }

    public void testGetInstanceStringCertStoreParametersProvider() {
        try {
            CertStoreParameters parameters = new MyCertStoreParameters();
            CertStore certStore = CertStore.getInstance(CERT_STORE_NAME,
                    parameters, provider);
            assertNotNull(certStore);
            assertNotNull(certStore.getCertStoreParameters());
            assertNotSame(parameters, certStore.getCertStoreParameters());
            assertSame(provider, certStore.getProvider());
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        }

        try {
            CertStore certStore = CertStore.getInstance(CERT_STORE_NAME, null,
                    provider);
            assertNotNull(certStore);
            assertNull(certStore.getCertStoreParameters());
            assertSame(provider, certStore.getProvider());
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        }

        try {
            CertStore.getInstance("UnknownCertStore", null, provider);
            fail("expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
            // ok
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        }

        try {
            CertStore.getInstance(CERT_STORE_NAME,
                    new MyOtherCertStoreParameters(), provider);
            fail("expected InvalidAlgorithmParameterException");
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (InvalidAlgorithmParameterException e) {
            // ok
        }

    }

    public void testGetCertificates() {
        CertStore certStore = null;
        try {
            certStore = CertStore.getInstance(CERT_STORE_NAME, null);
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }

        assertNotNull(certStore);

        try {
            Collection<? extends Certificate> certificates = certStore.getCertificates(null);
            assertNull(certificates);
        } catch (CertStoreException e) {
            fail("unexpected exception: " + e);
        }

        try {
            Collection<? extends Certificate> certificates = certStore.getCertificates(new MyCertSelector());
            assertNotNull(certificates);
            assertTrue(certificates.isEmpty());
        } catch (CertStoreException e) {
            fail("unexpected exception: " + e);
        }

        try {
            certStore.getCertificates(new MyOtherCertSelector());
            fail("expected CertStoreException");
        } catch (CertStoreException e) {
            // ok
        }
    }

    public void testGetCRLs() {
        CertStore certStore = null;
        try {
            certStore = CertStore.getInstance(CERT_STORE_NAME, new MyCertStoreParameters());
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }

        assertNotNull(certStore);

        try {
            Collection<? extends CRL> ls = certStore.getCRLs(null);
            assertNull(ls);
        } catch (CertStoreException e) {
            fail("unexpected exception: " + e);
        }

        try {
            Collection<? extends CRL> ls = certStore.getCRLs(new MyCRLSelector());
            assertNotNull(ls);
            assertTrue(ls.isEmpty());
        } catch (CertStoreException e) {
            fail("unexpected exception: " + e);
        }

        try {
            certStore.getCRLs(new MyOtherCRLSelector());
            fail("expected CertStoreException");
        } catch (CertStoreException e) {
            // ok
        }
    }

    static class MyCertStoreProvider extends Provider {

        protected MyCertStoreProvider() {
            super(CERT_STORE_PROVIDER_NAME, 1.0, "Test CertStore Provider 1.0");
            put("CertStore." + CERT_STORE_NAME, MyCertStoreSpi.class.getName());
        }
    }

    static class MyCertStoreParameters implements CertStoreParameters {
        public Object clone() {
            return new MyCertStoreParameters();
        }
    }

    static class MyOtherCertStoreParameters implements CertStoreParameters {
        public Object clone() {
            return new MyCertStoreParameters();
        }
    }

    static class MyCRLSelector implements CRLSelector {

        public boolean match(CRL crl) {
            return false;
        }

        public Object clone() {
            return new MyCRLSelector();
        }
    }

    static class MyOtherCRLSelector implements CRLSelector {
        public boolean match(CRL crl) {
            return false;
        }

        public Object clone() {
            return new MyOtherCRLSelector();
        }

    }

    static class MyCertSelector implements CertSelector {

        public boolean match(Certificate cert) {
            return false;
        }

        public Object clone() {
            return new MyCertSelector();
        }

    }

    static class MyOtherCertSelector implements CertSelector {
        public boolean match(Certificate crl) {
            return false;
        }

        public Object clone() {
            return new MyOtherCRLSelector();
        }

    }

    public static class MyCertStoreSpi extends CertStoreSpi {

        public MyCertStoreSpi() throws InvalidAlgorithmParameterException {
            super(null);
        }

        public MyCertStoreSpi(CertStoreParameters params)
                throws InvalidAlgorithmParameterException {
            super(params);
            if (params != null && !(params instanceof MyCertStoreParameters)) {
                throw new InvalidAlgorithmParameterException(
                        "invalid parameters");
            }
        }

        @Override
        public Collection<? extends CRL> engineGetCRLs(CRLSelector selector)
                throws CertStoreException {
            if (selector != null) {
                if (!(selector instanceof MyCRLSelector)) {
                    throw new CertStoreException();
                }
                return new ArrayList<CRL>();
            }
            return null;
        }

        @Override
        public Collection<? extends Certificate> engineGetCertificates(
                CertSelector selector) throws CertStoreException {
            if (selector != null) {
                if (!(selector instanceof MyCertSelector)) {
                    throw new CertStoreException();
                }
                return new ArrayList<Certificate>();
            }
            return null;
        }

    }

}
