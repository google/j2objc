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


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;

import org.apache.harmony.security.tests.support.cert.MyCertStoreParameters;
import org.apache.harmony.security.tests.support.cert.MyCertStoreSpi;

/**
 * Tests for <code>CertStoreSpi</code> class constructors and methods.
 *
 */
public class CertStoreSpiTest extends TestCase {


    /**
     * Test for <code>CertStoreSpi</code> constructor Assertion: constructs
     * CertStoreSpi
     */
    public void testCertStoreSpi01() throws InvalidAlgorithmParameterException,
            CertStoreException {
        CertStoreSpi certStoreSpi = null;
        CertSelector certSelector = new tmpCertSelector();//new
                                                          // X509CertSelector();
        CRLSelector crlSelector = new tmpCRLSelector();//new X509CRLSelector();
        try {
            certStoreSpi = new MyCertStoreSpi(null);
            fail("InvalidAlgorithmParameterException must be thrown");
        } catch (InvalidAlgorithmParameterException e) {
        }
        certStoreSpi = new MyCertStoreSpi(new MyCertStoreParameters());
        assertNull("Not null collection", certStoreSpi
                .engineGetCertificates(certSelector));
        assertNull("Not null collection", certStoreSpi
                .engineGetCRLs(crlSelector));
    }

    public static Test suite() {
        return new TestSuite(CertStoreSpiTest.class);
    }

    /**
     * Additional classes for verification CertStoreSpi class
     */
    public static class tmpCRLSelector implements CRLSelector {
        public Object clone() {
            return null;
        }
        public boolean match (CRL crl) {
            return false;
        }
    }
    public static class tmpCertSelector implements CertSelector {
        public Object clone() {
            return null;
        }
        public boolean match (Certificate crl) {
            return true;
        }
    }

}
