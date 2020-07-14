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

package tests.security.cert;

import junit.framework.TestCase;

import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.util.Collection;
import java.util.Vector;

import org.apache.harmony.security.tests.support.cert.MyCertificate;

/**
 * Tests for <code>CollectionCertStoreParameters</code>.
 */
public class CollectionCertStoreParametersTest extends TestCase {

    //
    // Tests
    //

    /**
     * Test #1 for <code>CollectionCertStoreParameters()</code> constructor<br>
     */
    public final void testCollectionCertStoreParameters01() {
        CertStoreParameters cp = new CollectionCertStoreParameters();
        assertTrue("isCollectionCertStoreParameters",
                cp instanceof CollectionCertStoreParameters);
    }

    /**
     * Test #2 for <code>CollectionCertStoreParameters</code> constructor<br>
     */
    @SuppressWarnings("unchecked")
    public final void testCollectionCertStoreParameters02() {
        CollectionCertStoreParameters cp = new CollectionCertStoreParameters();
        Collection c = cp.getCollection();
        assertTrue("isEmpty", c.isEmpty());

        // check that empty collection is immutable
        try {
            // try to modify it
            c.add(new Object());
            fail("empty collection must be immutable");
        } catch (Exception e) {
        }
    }

    /**
     * Test #1 for <code>CollectionCertStoreParameters(Collection)</code>
     * constructor<br>
     */
    public final void testCollectionCertStoreParametersCollection01() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        certificates.add(new MyCertificate("TEST", new byte[] {}));
        new CollectionCertStoreParameters(certificates);
    }

    /**
     * Test #2 for <code>CollectionCertStoreParameters(Collection)</code>
     * constructor<br>
     */
    public final void testCollectionCertStoreParametersCollection02() {
        // just check that we able to create CollectionCertStoreParameters
        // object passing Collection containing Object which is not
        // a Certificate or CRL
        Vector<String> certificates = new Vector<String>();
        certificates.add(new String("Not a Certificate"));
        new CollectionCertStoreParameters(certificates);
    }

    /**
     * Test #3 for <code>CollectionCertStoreParameters(Collection)</code>
     * constructor<br>
     */
    public final void testCollectionCertStoreParametersCollection03() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        // create using empty collection
        CollectionCertStoreParameters cp =
            new CollectionCertStoreParameters(certificates);
        // check that the reference is used
        assertTrue("isRefUsed_1", certificates == cp.getCollection());
        // check that collection still empty
        assertTrue("isEmpty", cp.getCollection().isEmpty());
        // modify our collection
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)1}));
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)2}));
        // check that internal state has been changed accordingly
        assertTrue("isRefUsed_2", certificates.equals(cp.getCollection()));
    }

    /**
     * Test #4 for <code>CollectionCertStoreParameters(Collection)</code>
     * constructor<br>
     */
    public final void testCollectionCertStoreParametersCollection04() {
        try {
            new CollectionCertStoreParameters(null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test #1 for <code>clone()</code> method<br>
     */
    public final void testClone01() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)4}));
        CollectionCertStoreParameters cp1 =
            new CollectionCertStoreParameters(certificates);
        CollectionCertStoreParameters cp2 =
            (CollectionCertStoreParameters)cp1.clone();
        // check that that we have new object
        assertTrue(cp1 != cp2);
    }

    /**
     * Test #2 for <code>clone()</code> method<br>
     */
    public final void testClone02() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)4}));
        CollectionCertStoreParameters cp1 =
            new CollectionCertStoreParameters(certificates);
        CollectionCertStoreParameters cp2 =
            (CollectionCertStoreParameters)cp1.clone();
        // check that both objects hold the same reference
        assertTrue(cp1.getCollection() == cp2.getCollection());
    }

    /**
     * Test #3 for <code>clone()</code> method<br>
     */
    public final void testClone03() {
        CollectionCertStoreParameters cp1 =
            new CollectionCertStoreParameters();
        CollectionCertStoreParameters cp2 =
            (CollectionCertStoreParameters)cp1.clone();
        CollectionCertStoreParameters cp3 =
            (CollectionCertStoreParameters)cp2.clone();
        // check that all objects hold the same reference
        assertTrue(cp1.getCollection() == cp2.getCollection() &&
                   cp3.getCollection() == cp2.getCollection());
    }

    /**
     * Test #1 for <code>toString()</code> method<br>
     */
    public final void testToString01() {
        CollectionCertStoreParameters cp =
            new CollectionCertStoreParameters();
        String s = cp.toString();
        assertNotNull(s);
    }

    /**
     * Test #2 for <code>toString()</code> method<br>
     */
    public final void testToString02() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)4}));
        CollectionCertStoreParameters cp =
            new CollectionCertStoreParameters(certificates);

        assertNotNull(cp.toString());
    }

    /**
     * Test #1 for <code>getCollection()</code> method<br>
     */
    public final void testGetCollection01() {
        CollectionCertStoreParameters cp = new CollectionCertStoreParameters();
        assertNotNull(cp.getCollection());
    }

    /**
     * Test #2 for <code>getCollection()</code> method<br>
     */
    public final void testGetCollection02() {
        Vector certificates = new Vector();
        CollectionCertStoreParameters cp =
            new CollectionCertStoreParameters(certificates);
        assertNotNull(cp.getCollection());
    }

}
