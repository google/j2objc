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
import org.apache.harmony.security.tests.support.cert.TestUtils;
import tests.support.resource.Support_Resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Provider;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Tests for <code>CertificateFactory</code> class methods
 */
public class CertificateFactory3Test extends TestCase {

    private static String defaultProviderName = null;

    private static Provider defaultProvider = null;

    private static String defaultType = CertificateFactory1Test.defaultType;

    public static String fileCertPathPki = "java/security/cert/CertPath.PkiPath";

    private static boolean X509Support = false;

    private static String NotSupportMsg = "";

    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                CertificateFactory1Test.srvCertificateFactory);
        X509Support = defaultProvider != null;
        defaultProviderName = X509Support ? defaultProvider.getName() : null;

        NotSupportMsg = defaultType.concat(" is not supported");
    }

    private static CertificateFactory[] initCertFs() throws Exception {
        if (!X509Support) {
            fail(NotSupportMsg);
        }

        CertificateFactory[] certFs = new CertificateFactory[3];
        certFs[0] = CertificateFactory.getInstance(defaultType);
        certFs[1] = CertificateFactory.getInstance(defaultType,
                defaultProviderName);
        certFs[2] = CertificateFactory
                .getInstance(defaultType, defaultProvider);
        return certFs;
    }

    /**
     * Test for <code>generateCertificate(InputStream inStream)</code> method
     * Assertion: returns Certificate
     */
    public void testGenerateCertificate() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        Certificate[] certs = new Certificate[3];
        for (int i = 0; i < certFs.length; i++) {
            certs[i] = certFs[i].generateCertificate(new ByteArrayInputStream(
                    TestUtils.getEncodedX509Certificate()));
        }
        assertEquals(certs[0], certs[1]);
        assertEquals(certs[0], certs[2]);
    }

    /**
     * Test for <code>generateCertificates(InputStream inStream)</code> method
     * Assertion: returns Collection which consists of 1 Certificate
     */
    public void testGenerateCertificates() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        Certificate cert = certFs[0]
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getEncodedX509Certificate()));
        for (int i = 0; i < certFs.length; i++) {
            Collection<? extends Certificate> col = null;
            col = certFs[i].generateCertificates(new ByteArrayInputStream(
                    TestUtils.getEncodedX509Certificate()));
            Iterator<? extends Certificate> it = col.iterator();
            assertEquals("Incorrect Collection size", col.size(), 1);
            assertEquals("Incorrect Certificate in Collection", cert, it.next());
        }
    }

    /**
     * Test for <code>generateCertPath(List certificates)</code> method
     * Assertion: returns CertPath with 1 Certificate
     */
    public void testGenerateCertPath01() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        // create list of certificates with one certificate
        Certificate cert = certFs[0]
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getEncodedX509Certificate()));
        List<Certificate> list = new Vector<Certificate>();
        list.add(cert);
        for (int i = 0; i < certFs.length; i++) {
            CertPath certPath = null;
            certPath = certFs[i].generateCertPath(list);
            assertEquals(cert.getType(), certPath.getType());
            List<? extends Certificate> list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
            Iterator<? extends Certificate> it = list1.iterator();
            assertEquals("Incorrect Certificate in CertPath", cert, it.next());
        }
    }

    /**
     * Test for
     * <code>generateCertPath(InputStream inStream, String encoding)</code>
     * method Assertion: returns CertPath with 1 Certificate
     */
    public void testGenerateCertPath02() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        for (int i = 0; i < certFs.length; i++) {
            CertPath certPath = null;
            InputStream fis = Support_Resources
                    .getResourceStream(fileCertPathPki);
            certPath = certFs[i].generateCertPath(fis, "PkiPath");
            fis.close();
            assertEquals(defaultType, certPath.getType());

            List<? extends Certificate> list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
        }
    }

    /**
     * Test for <code>generateCertPath(InputStream inStream)</code> method
     * Assertion: returns CertPath with 1 Certificate
     */
    public void testGenerateCertPath03() throws Exception {
        String certPathEncoding = "PkiPath";
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        for (int i = 0; i < certFs.length; i++) {
            Iterator<String> it = certFs[0].getCertPathEncodings();

            assertTrue("no CertPath encodings", it.hasNext());

            assertEquals("Incorrect default encoding", certPathEncoding, it
                    .next());

            CertPath certPath = null;
            InputStream fis = Support_Resources
                    .getResourceStream(fileCertPathPki);
            certPath = certFs[i].generateCertPath(fis);
            fis.close();
            assertEquals(defaultType, certPath.getType());

            List<? extends Certificate> list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
        }
    }
}
