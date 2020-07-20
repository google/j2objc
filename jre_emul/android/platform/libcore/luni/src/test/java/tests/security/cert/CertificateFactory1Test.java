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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package tests.security.cert;

import junit.framework.TestCase;

import org.apache.harmony.security.tests.support.SpiEngUtils;
import org.apache.harmony.security.tests.support.cert.MyCertPath;
import org.apache.harmony.security.tests.support.cert.MyCertificate;
import org.apache.harmony.security.tests.support.cert.MyCertificateFactorySpi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateFactorySpi;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Tests for <code>CertificateFactory</code> class methods and constructor
 *
 */
public class CertificateFactory1Test extends TestCase {

    public static final String srvCertificateFactory = "CertificateFactory";

    private static String defaultProviderName = null;

    private static Provider defaultProvider = null;

    private static boolean X509Support = false;

    public static String defaultType = "X.509";

    public static final String[] validValues = {
            "X.509", "x.509" };

    private final static String[] invalidValues = SpiEngUtils.invalidValues;

    private static String NotSupportMsg = "";

    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                srvCertificateFactory);
        X509Support = (defaultProvider != null);
        defaultProviderName = (X509Support ? defaultProvider.getName() : null);
        NotSupportMsg = defaultType.concat(" is not supported");    }

    private static CertificateFactory[] initCertFs() {
        if (!X509Support) {
            fail(NotSupportMsg);
            return null;
        }
        try {
            CertificateFactory[] certFs = new CertificateFactory[3];
            certFs[0] = CertificateFactory.getInstance(defaultType);
            certFs[1] = CertificateFactory.getInstance(defaultType,
                    defaultProviderName);
            certFs[2] = CertificateFactory.getInstance(defaultType,
                    defaultProvider);
            return certFs;
        } catch (Exception e) {
            return null;
        }
    }

    private static MyCertificate createMC() {
        byte[] enc = { (byte) 0, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
        return new MyCertificate("Test_Test", enc);
    }

    /**
     * Test for <code>getInstance(String type)</code> method
     * Assertion: returns CertificateFactory if type is X.509
     */
    public void testCertificateFactory01() throws CertificateException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            CertificateFactory certF = CertificateFactory
                    .getInstance(validValues[i]);
            assertEquals("Incorrect type: ", validValues[i], certF.getType());
        }
    }

    /**
     * Test for <code>getInstance(String type)</code> method
     * Assertion:
     * throws NullPointerException when type is null
     * throws CertificateException when type is not available
     */
    public void testCertificateFactory02() {
        try {
            CertificateFactory.getInstance(null);
            fail("NullPointerException or CertificateException must be thrown when type is null");
        } catch (CertificateException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertificateFactory.getInstance(invalidValues[i]);
                fail("CertificateException must be thrown when type: "
                        .concat(invalidValues[i]));
            } catch (CertificateException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String type, String provider)</code> method
     * Assertion: throws IllegalArgumentException when provider is null or empty
     */
    public void testCertificateFactory03() throws CertificateException,
            NoSuchProviderException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                CertificateFactory.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
            try {
                CertificateFactory.getInstance(validValues[i], "");
                fail("IllegalArgumentException  must be thrown when provider is empty");
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String type, String provider)</code> method
     * Assertion:
     * throws NullPointerException when type is null
     * throws CertificateException when type is not available
     */
    public void testCertificateFactory04() throws NoSuchProviderException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        try {
            CertificateFactory.getInstance(null, defaultProviderName);
            fail("NullPointerException or CertificateException must be thrown when type is null");
        } catch (CertificateException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertificateFactory.getInstance(invalidValues[i],
                        defaultProviderName);
                fail("CertificateException must be thrown (type: ".concat(
                        invalidValues[i]).concat(" provider: ").concat(
                        defaultProviderName).concat(")"));
            } catch (CertificateException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String type, String provider)</code> method
     * Assertion: returns CertificateFactory when type and provider have valid
     * values
     */
    public void testCertificateFactory05() throws CertificateException,
            NoSuchProviderException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory certF;
        for (int i = 0; i < validValues.length; i++) {
            certF = CertificateFactory.getInstance(validValues[i],
                    defaultProviderName);
            assertEquals("Incorrect type", certF.getType(), validValues[i]);
            assertEquals("Incorrect provider name", certF.getProvider()
                    .getName(), defaultProviderName);
        }
    }

    /**
     * Test for <code>getInstance(String type, Provider provider)</code>
     * method
     * Assertion: throws IllegalArgumentException when provider is null
     */
    public void testCertificateFactory06() throws CertificateException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        Provider provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                CertificateFactory.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown  when provider is null");
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String type, Provider provider)</code>
     * method
     * Assertion:
     * throws NullPointerException when type is null
     * throws CertificateException when type is not available
     */
    public void testCertificateFactory07() {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        try {
            CertificateFactory.getInstance(null, defaultProvider);
            fail("NullPointerException or CertificateException must be thrown when type is null");
        } catch (CertificateException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertificateFactory.getInstance(invalidValues[i],
                        defaultProvider);
                fail("CertificateException was not thrown as expected (type:"
                        .concat(invalidValues[i]).concat(" provider: ").concat(
                                defaultProvider.getName()).concat(")"));
            } catch (CertificateException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String type, Provider provider)</code>
     * method
     * Assertion: returns CertificateFactorythrows when type and provider
     * have valid values
     */
    public void testCertificateFactory08() throws CertificateException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory certF;
        for (int i = 0; i < validValues.length; i++) {
            certF = CertificateFactory.getInstance(validValues[i],
                    defaultProvider);
            assertEquals("Incorrect provider", certF.getProvider(),
                    defaultProvider);
            assertEquals("Incorrect type", certF.getType(), validValues[i]);
        }
    }

    /**
     * Test for <code>getCertPathEncodings()</code> method
     * Assertion: returns encodings
     */
    public void testCertificateFactory09() {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        Iterator<String> it1 = certFs[0].getCertPathEncodings();
        Iterator<String> it2 = certFs[1].getCertPathEncodings();
        assertEquals("Incorrect encodings", it1.hasNext(), it2.hasNext());
        while (it1.hasNext()) {
            it2 = certFs[1].getCertPathEncodings();
            String s1 = it1.next();
            boolean yesNo = false;
            while (it2.hasNext()) {
                if (s1.equals(it2.next())) {
                    yesNo = true;
                    break;
                }
            }
            assertTrue("Encoding: ".concat(s1).concat(
                    " does not define for certF2 CertificateFactory"), yesNo);
        }
        it1 = certFs[0].getCertPathEncodings();
        it2 = certFs[2].getCertPathEncodings();
        assertEquals("Incorrect encodings", it1.hasNext(), it2.hasNext());
        while (it1.hasNext()) {
            it2 = certFs[2].getCertPathEncodings();
            String s1 = it1.next();
            boolean yesNo = false;
            while (it2.hasNext()) {
                if (s1.equals(it2.next())) {
                    yesNo = true;
                    break;
                }
            }
            assertTrue("Encoding: ".concat(s1).concat(
                    " does not define for certF3 CertificateFactory"), yesNo);
        }
    }

    /**
     * Test for <code>generateCertificate(InputStream inStream)</code>
     * <code>generateCertificates(InputStream inStream)</code>
     * <code>generateCRL(InputStream inStream)</code>
     * <code>generateCRLs(InputStream inStream)</code>
     * methods
     * Assertion: throw CertificateException and CRLException when
     * inStream is null or empty
     */
    public void testCertificateFactory10() {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        byte [] bb = {};
        InputStream is = new ByteArrayInputStream(bb);
        Collection<?> colCer;
        Collection<?> colCrl;
        for (int i = 0; i < certFs.length; i++) {
            try {
                certFs[i].generateCertificate(null);
                fail("generateCertificate must thrown CertificateException or NullPointerEXception when input stream is null");
            } catch (CertificateException e) {
            } catch (NullPointerException e) {
            }
            is = new ByteArrayInputStream(bb);
            try {
                certFs[i].generateCertificates(null);
                fail("generateCertificates must throw CertificateException or NullPointerException when input stream is null");
            } catch (CertificateException e) {
            } catch (NullPointerException e) {
            }
            is = new ByteArrayInputStream(bb);
            try {
                certFs[i].generateCertificate(is);
            } catch (CertificateException e) {
            }
            is = new ByteArrayInputStream(bb);
            try {
                colCer = certFs[i].generateCertificates(is);
                if (colCer != null) {
                    assertTrue("Not empty certificate collection", colCer.isEmpty());
                }
            } catch (CertificateException e) {
            }
        }
        for (int i = 0; i < certFs.length; i++) {
            try {
                certFs[i].generateCRL(null);
            } catch (CRLException e) {
            } catch (NullPointerException e) {
            }
            try {
                colCrl = certFs[i].generateCRLs(null);
                if (colCrl != null) {
                    assertTrue("Not empty CRL collection was returned from null stream", colCrl.isEmpty());
                }
            } catch (CRLException e) {
            } catch (NullPointerException e) {
            }
            is = new ByteArrayInputStream(bb);
            try {
                 certFs[i].generateCRL(is);
            } catch (CRLException e) {
            }
            is = new ByteArrayInputStream(bb);
            try {
                certFs[i].generateCRLs(is);
                colCrl = certFs[i].generateCRLs(null);
                if (colCrl != null) {
                    assertTrue("Not empty CRL collection was returned from empty stream", colCrl.isEmpty());
                }
            } catch (CRLException e) {
            }
        }
    }

    /*
     * Test for <code> generateCertificate(InputStream inStream) </code><code>
     * generateCertificates(InputStream inStream) </code><code>
     * generateCRL(InputStream inStream) </code><code>
     * generateCRLs(InputStream inStream) </code>
     * methods
     * Assertion: throw CertificateException and CRLException when inStream
     * contains incompatible datas
     */
    public void testCertificateFactory11() throws IOException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        MyCertificate mc = createMC();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(mc);
        oos.flush();
        oos.close();

        Certificate cer;
        Collection<?> colCer;
        CRL crl;
        Collection<?> colCrl;

        byte[] arr = os.toByteArray();
        ByteArrayInputStream is;
        for (int i = 0; i < certFs.length; i++) {
            is = new ByteArrayInputStream(arr);
            try {
                cer = certFs[i].generateCertificate(is);
                assertNull("Not null certificate was created", cer);
            } catch (CertificateException e) {
            }
            is = new ByteArrayInputStream(arr);
            try {
                colCer = certFs[i].generateCertificates(is);
                if (colCer != null) {
                    assertTrue("Not empty certificate Collection was created", colCer.isEmpty());
                }
            } catch (CertificateException e) {
            }
            is = new ByteArrayInputStream(arr);
            try {
                crl = certFs[i].generateCRL(is);
                assertNull("Not null CRL was created", crl);
            } catch (CRLException e) {
            }
            is = new ByteArrayInputStream(arr);
            try {
                colCrl = certFs[i].generateCRLs(is);
                if (colCrl != null) {
                    assertTrue("Not empty CRL Collection was created", colCrl.isEmpty());
                }
            } catch (CRLException e) {
            }
        }
    }

    /**
     * Test for <code>generateCertPath(InputStream inStream)</code>
     * <code>generateCertPath(InputStream inStream, String encoding)</code>
     * methods
     * Assertion: throws CertificateException when inStream is null or
     * when isStream contains invalid datas
     */
    public void testCertificateFactory12() {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        InputStream is1 = null;
        InputStream is2 = new ByteArrayInputStream(new byte[10]);

        for (int i = 0; i < certFs.length; i++) {
            try {
                certFs[i].generateCertPath(is1);
                fail("generateCertificate must thrown CertificateException or NullPointerException when input stream is null");
            } catch (CertificateException e) {
            } catch (NullPointerException e) {
            }
            try {
                certFs[i].generateCertPath(is2);
                fail("generateCertificate must thrown CertificateException when input stream contains invalid datas");
            } catch (CertificateException e) {
            }
            Iterator<String> it = certFs[i].getCertPathEncodings();
            while (it.hasNext()) {
                String enc = it.next();
                try {
                    certFs[i].generateCertPath(is1, enc);
                    fail("generateCertificate must thrown CertificateException or NullPointerException when input stream is null and encodings "
                            .concat(enc));
                } catch (CertificateException e) {
                } catch (NullPointerException e) {
                }
                try {
                    certFs[i].generateCertPath(is2, enc);
                    fail("generateCertificate must thrown CertificateException when input stream contains invalid datas  and encodings "
                            .concat(enc));
                } catch (CertificateException e) {
                }
            }
        }
    }

    /**
     * Test for <code>generateCertPath(InputStream inStream)</code>
     * <code>generateCertPath(InputStream inStream, String encoding)</code>
     * methods
     * Assertion: throw CertificateException when isStream contains invalid datas
     */
    // Test passed on RI
    public void testCertificateFactory13() throws IOException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        byte[] enc = { (byte) 0, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
        MyCertPath mc = new MyCertPath(enc);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(mc);
        oos.flush();
        oos.close();

        byte[] arr = os.toByteArray();
        ByteArrayInputStream is = new ByteArrayInputStream(arr);

        for (int i = 0; i < certFs.length; i++) {
            try {
                certFs[i].generateCertPath(is);
                fail("CertificateException must be thrown because input stream contains incorrect datas");
            } catch (CertificateException e) {
            }
            Iterator<String> it = certFs[i].getCertPathEncodings();
            while (it.hasNext()) {
                try {
                    certFs[i].generateCertPath(is, it.next());
                    fail("CertificateException must be thrown because input stream contains incorrect datas");
                } catch (CertificateException e) {
                }
            }
        }
    }

    /**
     * Test for <code>generateCertPath(List certificates)</code> method
     * Assertion: throw NullPointerException certificates is null
     */
    public void testCertificateFactory14() throws CertificateException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        List<Certificate> list = null;
        for (int i = 0; i < certFs.length; i++) {
            try {
                certFs[i].generateCertPath(list);
                fail("generateCertificate must thrown CertificateException when list is null");
            } catch (NullPointerException e) {
            }
        }
    }

    /**
     * Test for <code>generateCertPath(List certificates)</code> method
     * Assertion: returns empty CertPath if certificates is empty
     */
    public void testCertificateFactory15() throws CertificateException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        List<Certificate> list = new Vector<Certificate>();
        for (int i = 0; i < certFs.length; i++) {
            CertPath cp = certFs[i].generateCertPath(list);
            List<? extends Certificate> list1 = cp.getCertificates();
            assertTrue("List should be empty", list1.isEmpty());
        }
    }

    /**
     * Test for <code>generateCertPath(List certificates)</code> method
     * Assertion: throws CertificateException when certificates contains
     * incorrect Certificate
     */
    public void testCertificateFactory16() {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        MyCertificate ms = createMC();
        List<Certificate> list = new Vector<Certificate>();
        list.add(ms);
        for (int i = 0; i < certFs.length; i++) {
            try {
                certFs[i].generateCertPath(list);
                fail("CertificateException must be thrown");
            } catch (CertificateException e) {
            }
        }
    }

    /**
     * Test for <code>CertificateFactory</code> constructor
     * Assertion: returns CertificateFactory object
     */
    public void testCertificateFactory17() throws CRLException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactorySpi spi = new MyCertificateFactorySpi();
        CertificateFactory cf = new myCertificateFactory(spi, defaultProvider,
                defaultType);
        assertEquals("Incorrect type", cf.getType(), defaultType);
        assertEquals("Incorrect provider", cf.getProvider(), defaultProvider);
        try {
            cf.generateCRLs(null);
            fail("CRLException must be thrown");
        } catch (CRLException e) {
        }

        cf = new myCertificateFactory(null, null, null);
        assertNull("Incorrect type", cf.getType());
        assertNull("Incorrect provider", cf.getProvider());
        try {
            cf.generateCRLs(null);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test for <code>getType()</code> method
     */
    public void testCertificateFactory18() throws CertificateException {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            try {
                CertificateFactory certF = CertificateFactory
                        .getInstance(validValues[i]);
                assertEquals("Incorrect type: ", validValues[i], certF
                        .getType());
                certF = CertificateFactory.getInstance(validValues[i],
                        defaultProviderName);
                assertEquals("Incorrect type", certF.getType(), validValues[i]);

                certF = CertificateFactory.getInstance(validValues[i],
                        defaultProvider);
                assertEquals("Incorrect provider", certF.getProvider(),
                        defaultProvider);
                assertEquals("Incorrect type", certF.getType(), validValues[i]);

            } catch (NoSuchProviderException e) {
                fail("Unexpected NoSuchProviderException " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("cast")
    public void testCertificateFactory19() {
        if (!X509Support) {
            fail(NotSupportMsg);
            return;
        }
        CertificateFactorySpi spi = new MyCertificateFactorySpi();
        myCertificateFactory cf;
        try {
            cf = new myCertificateFactory(spi, defaultProvider,
                                      defaultType);
            assertEquals("Incorrect type", cf.getType(), defaultType);
            assertEquals("Incorrect provider", cf.getProvider(), defaultProvider);
            assertTrue(cf instanceof CertificateFactory);
        } catch (Exception e) {
            fail("Unexpected exception" + e);
        }

        try {
            cf = new myCertificateFactory(null, null, null);
        } catch (Exception e) {
            fail("Unexpected exception" + e);
        }
    }
}
/**
 * Additional class to verify CertificateFactory constructor
 */

class myCertificateFactory extends CertificateFactory {

    public myCertificateFactory(CertificateFactorySpi spi, Provider prov,
            String type) {
        super(spi, prov, type);
    }
}
