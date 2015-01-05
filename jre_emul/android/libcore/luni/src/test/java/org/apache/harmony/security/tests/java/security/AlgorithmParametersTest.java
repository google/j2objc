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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.AlgorithmParametersSpi;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import junit.framework.TestCase;

import org.apache.harmony.security.tests.support.MyAlgorithmParameterGeneratorSpi;

/**
 * Tests for <code>AlgorithmParameters</code> class constructors and
 * methods.
 *
 */
public class AlgorithmParametersTest extends TestCase {

    /**
     * Provider
     */
    Provider p;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        p = new MyProvider();
        Security.insertProviderAt(p, 1);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(p.getName());
    }

    /**
     * java.security.AlgorithmParameters#getAlgorithm()
     */
    public void test_getAlgorithm() throws Exception {

        // test: null value
        AlgorithmParameters ap = new DummyAlgorithmParameters(null, p, null);
        assertNull(ap.getAlgorithm());

        // test: not null value
        ap = new DummyAlgorithmParameters(null, p, "AAA");
        assertEquals("AAA", ap.getAlgorithm());
    }

    /**
     * java.security.AlgorithmParameters#getEncoded()
     */
    public void test_getEncoded() throws Exception {

        final byte[] enc = new byte[] { 0x02, 0x01, 0x03 };

        MyAlgorithmParameters paramSpi = new MyAlgorithmParameters() {
            protected byte[] engineGetEncoded() throws IOException {
                return enc;
            }
        };

        AlgorithmParameters params = new DummyAlgorithmParameters(paramSpi, p,
                "algorithm");

        //
        // test: IOException if not initialized
        //
        try {
            params.getEncoded();
            fail("should not get encoded from un-initialized instance");
        } catch (IOException e) {
            // expected
        }

        //
        // test: corresponding spi method is invoked
        //
        params.init(new MyAlgorithmParameterSpec());
        assertSame(enc, params.getEncoded());
    }

    /**
     * java.security.AlgorithmParameters#getEncoded(String)
     */
    public void test_getEncodedLjava_lang_String() throws Exception {

        final byte[] enc = new byte[] { 0x02, 0x01, 0x03 };

        final String strFormatParam = "format";

        MyAlgorithmParameters paramSpi = new MyAlgorithmParameters() {
            protected byte[] engineGetEncoded(String format) throws IOException {
                assertEquals(strFormatParam, format);
                return enc;
            }
        };

        AlgorithmParameters params = new DummyAlgorithmParameters(paramSpi, p,
                "algorithm");

        //
        // test: IOException if not initialized
        //
        try {
            params.getEncoded(strFormatParam);
            fail("should not get encoded from un-initialized instance");
        } catch (IOException e) {
            // expected
        }

        //
        // test: corresponding spi method is invoked
        //
        params.init(new MyAlgorithmParameterSpec());
        assertSame(enc, params.getEncoded(strFormatParam));

        //
        // test: if format param is null
        // Regression test for HARMONY-2680
        //
        paramSpi = new MyAlgorithmParameters() {
            protected byte[] engineGetEncoded(String format) throws IOException {
                assertNull(format); // null is passed to spi-provider
                return enc;
            }
        };

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(new MyAlgorithmParameterSpec());
        assertSame(enc, params.getEncoded(null));
    }

    /**
     * java.security.AlgorithmParameters#getInstance(String)
     */
    public void test_getInstanceLjava_lang_String() {
        String[] str = {"", "qwertyu", "!@#$%^&*()"};
        try {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("ABC");
            checkUnititialized(ap);
            ap.init(new MyAlgorithmParameterSpec());
            checkAP(ap, p);
        } catch (Exception e) {
            fail("Unexpected exception");
        }

        for(int i = 0; i < str.length; i++) {
            try {
                AlgorithmParameters ap = AlgorithmParameters.getInstance(str[i]);
                fail("NoSuchAlgorithmException was not thrown for parameter " + str[i]);
            } catch (NoSuchAlgorithmException nsae) {
                //expected
            }
        }
    }

    /**
     * java.security.AlgorithmParameters#getInstance(String, String)
     */
    public void test_getInstanceLjava_lang_StringLjava_lang_String() {
        String[] alg = {"", "qwertyu", "!@#$%^&*()"};
        String[] prv = {"", null};
        String[] prv1 = {"1234567890", "qwertyu", "!@#$%^&*()"};
        try {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("ABC", "MyProvider");
            checkUnititialized(ap);
            ap.init(new byte[6]);
            checkAP(ap, p);
        } catch (Exception e) {
            fail("Unexpected exception");
        }

        for (int i = 0; i < alg.length; i++) {
            try {
                AlgorithmParameters ap = AlgorithmParameters.getInstance(alg[i], "MyProvider");
                fail("NoSuchAlgorithmException was not thrown for parameter " + alg[i]);
            } catch (NoSuchAlgorithmException nsae) {
                //expected
            } catch (Exception e) {
                fail("Incorrect exception " + e + " was thrown for " + alg[i]);
            }
        }

        for (int i = 0; i < prv.length; i++) {
            try {
                AlgorithmParameters ap = AlgorithmParameters.getInstance("ABC", prv[i]);
                fail("IllegalArgumentException was not thrown for parameter " + prv[i]);
            } catch (IllegalArgumentException iae) {
                //expected
            } catch (Exception e) {
                fail("Incorrect exception " + e + " was thrown for " + prv[i]);
            }
        }

        for (int i = 0; i < prv1.length; i++) {
            try {
                AlgorithmParameters ap = AlgorithmParameters.getInstance("ABC", prv1[i]);
                fail("NoSuchProviderException was not thrown for parameter " + prv1[i]);
            } catch (NoSuchProviderException nspe) {
                //expected
            } catch (Exception e) {
                fail("Incorrect exception " + e + " was thrown for " + prv1[i]);
            }
        }
    }

    /**
     * java.security.AlgorithmParameters#getParameterSpec(Class)
     */
    public void test_getParameterSpecLjava_lang_Class() throws Exception {

        final MyAlgorithmParameterSpec myParamSpec = new MyAlgorithmParameterSpec();

        MyAlgorithmParameters paramSpi = new MyAlgorithmParameters() {
            protected AlgorithmParameterSpec engineGetParameterSpec(
                    Class paramSpec) {
                return myParamSpec;
            }
        };

        AlgorithmParameters params = new DummyAlgorithmParameters(paramSpi, p,
                "algorithm");

        //
        // test: InvalidParameterSpecException if not initialized
        //
        try {
            params.getParameterSpec(null);
            fail("No expected InvalidParameterSpecException");
        } catch (InvalidParameterSpecException e) {
            // expected
        }
        try {
            params.getParameterSpec(MyAlgorithmParameterSpec.class);
            fail("No expected InvalidParameterSpecException");
        } catch (InvalidParameterSpecException e) {
            // expected
        }

        //
        // test: corresponding spi method is invoked
        //
        params.init(new MyAlgorithmParameterSpec());
        assertSame(myParamSpec, params
                .getParameterSpec(MyAlgorithmParameterSpec.class));

        //
        // test: if paramSpec is null
        // Regression test for HARMONY-2733
        //
        paramSpi = new MyAlgorithmParameters() {

            protected AlgorithmParameterSpec engineGetParameterSpec(
                    Class paramSpec) {
                assertNull(paramSpec); // null is passed to spi-provider
                return null;
            }
        };

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(new MyAlgorithmParameterSpec());
        assertNull(params.getParameterSpec(null));
    }

    /**
     * java.security.AlgorithmParameters#getInstance(String, Provider)
     */
    public void test_getInstanceLjava_lang_StringLjava_security_Provider() {
        String[] alg = {"", "qwertyu", "!@#$%^&*()"};
        Provider pp = null;

        try {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("ABC", p);
            checkUnititialized(ap);
            ap.init(new byte[6], "aaa");
            checkAP(ap, p);
        } catch (Exception e){
            fail("Unexpected exception");
        }

        for (int i = 0; i < alg.length; i++) {
            try {
                AlgorithmParameters ap = AlgorithmParameters.getInstance(alg[i], p);
                fail("NoSuchAlgorithmException was not thrown for parameter " + alg[i]);
            } catch (NoSuchAlgorithmException nsae) {
                //expected
            } catch (Exception e) {
                fail("Incorrect exception " + e + " was thrown for " + alg[i]);
            }
        }

        try {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("ABC", pp);
            fail("IllegalArgumentException was not thrown for NULL provider");
        } catch (IllegalArgumentException iae) {
            //expected
        } catch (Exception e){
            fail("Incorrect exception " + e + " was thrown");
        }
    }

    /**
     * java.security.AlgorithmParameters#getProvider()
     */
    public void test_getProvider() throws Exception {
        // test: null value
        AlgorithmParameters ap = new DummyAlgorithmParameters(null, null, "AAA");
        assertNull(ap.getProvider());

        // test: not null value
        ap = new DummyAlgorithmParameters(null, p, "AAA");
        assertSame(p, ap.getProvider());
    }

    /**
     * java.security.AlgorithmParameters#init(java.security.spec.AlgorithmParameterSpec)
     */
    public void test_initLjava_security_spec_AlgorithmParameterSpec()
            throws Exception {

        //
        // test: corresponding spi method is invoked
        //
        final MyAlgorithmParameterSpec spec = new MyAlgorithmParameterSpec();

        MyAlgorithmParameters paramSpi = new MyAlgorithmParameters() {
            protected void engineInit(AlgorithmParameterSpec paramSpec)
                    throws InvalidParameterSpecException {
                assertSame(spec, paramSpec);
                runEngineInit_AlgParamSpec = true;
            }
        };

        AlgorithmParameters params = new DummyAlgorithmParameters(paramSpi, p,
                "algorithm");

        params.init(spec);
        assertTrue(paramSpi.runEngineInit_AlgParamSpec);

        //
        // test: InvalidParameterSpecException if already initialized
        //
        try {
            params.init(spec);
            fail("No expected InvalidParameterSpecException");
        } catch (InvalidParameterSpecException e) {
            // expected
        }

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(new byte[0]);
        try {
            params.init(spec);
            fail("No expected InvalidParameterSpecException");
        } catch (InvalidParameterSpecException e) {
            // expected
        }

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(new byte[0], "format");
        try {
            params.init(spec);
            fail("No expected InvalidParameterSpecException");
        } catch (InvalidParameterSpecException e) {
            // expected
        }

        //
        // test: if paramSpec is null
        //
        paramSpi = new MyAlgorithmParameters() {

            protected void engineInit(AlgorithmParameterSpec paramSpec)
                    throws InvalidParameterSpecException {
                assertNull(paramSpec);// null is passed to spi-provider
                runEngineInit_AlgParamSpec = true;
            }
        };

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init((AlgorithmParameterSpec) null);
        assertTrue(paramSpi.runEngineInit_AlgParamSpec);
    }

    /**
     * java.security.AlgorithmParameters#init(byte[])
     */
    public void test_init$B() throws Exception {

        //
        // test: corresponding spi method is invoked
        //
        final byte[] enc = new byte[] { 0x02, 0x01, 0x03 };

        MyAlgorithmParameters paramSpi = new MyAlgorithmParameters() {
            protected void engineInit(byte[] params) throws IOException {
                runEngineInitB$ = true;
                assertSame(enc, params);
            }
        };

        AlgorithmParameters params = new DummyAlgorithmParameters(paramSpi, p,
                "algorithm");

        params.init(enc);
        assertTrue(paramSpi.runEngineInitB$);

        //
        // test: IOException if already initialized
        //
        try {
            params.init(enc);
            fail("No expected IOException");
        } catch (IOException e) {
            // expected
        }

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(new MyAlgorithmParameterSpec());
        try {
            params.init(enc);
            fail("No expected IOException");
        } catch (IOException e) {
            // expected
        }

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(enc, "format");
        try {
            params.init(enc);
            fail("No expected IOException");
        } catch (IOException e) {
            // expected
        }

        //
        // test: if params is null
        //
        paramSpi = new MyAlgorithmParameters() {

            protected void engineInit(byte[] params) throws IOException {
                runEngineInitB$ = true;
                assertNull(params); // null is passed to spi-provider
            }
        };

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init((byte[]) null);
        assertTrue(paramSpi.runEngineInitB$);
    }

    /**
     * java.security.AlgorithmParameters#init(byte[],String)
     */
    public void test_init$BLjava_lang_String() throws Exception {

        //
        // test: corresponding spi method is invoked
        //
        final byte[] enc = new byte[] { 0x02, 0x01, 0x03 };
        final String strFormatParam = "format";

        MyAlgorithmParameters paramSpi = new MyAlgorithmParameters() {
            protected void engineInit(byte[] params, String format)
                    throws IOException {

                runEngineInitB$String = true;
                assertSame(enc, params);
                assertSame(strFormatParam, format);
            }
        };

        AlgorithmParameters params = new DummyAlgorithmParameters(paramSpi, p,
                "algorithm");

        params.init(enc, strFormatParam);
        assertTrue(paramSpi.runEngineInitB$String);

        //
        // test: IOException if already initialized
        //
        try {
            params.init(enc, strFormatParam);
            fail("No expected IOException");
        } catch (IOException e) {
            // expected
        }

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(new MyAlgorithmParameterSpec());
        try {
            params.init(enc, strFormatParam);
            fail("No expected IOException");
        } catch (IOException e) {
            // expected
        }

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(enc);
        try {
            params.init(enc, strFormatParam);
            fail("No expected IOException");
        } catch (IOException e) {
            // expected
        }

        //
        // test: if params and format are null
        // Regression test for HARMONY-2724
        //
        paramSpi = new MyAlgorithmParameters() {

            protected void engineInit(byte[] params, String format)
                    throws IOException {

                runEngineInitB$String = true;

                // null is passed to spi-provider
                assertNull(params);
                assertNull(format);
            }
        };

        params = new DummyAlgorithmParameters(paramSpi, p, "algorithm");
        params.init(null, null);
        assertTrue(paramSpi.runEngineInitB$String);
    }

    /**
     * java.security.AlgorithmParameters#toString()
     */
    public void test_toString() throws Exception {

        final String str = "AlgorithmParameters";

        MyAlgorithmParameters paramSpi = new MyAlgorithmParameters() {
            protected String engineToString() {
                return str;
            }
        };

        AlgorithmParameters params = new DummyAlgorithmParameters(paramSpi, p,
                "algorithm");

        assertNull("unititialized", params.toString());

        params.init(new byte[0]);

        assertSame(str, params.toString());
    }

    /**
     * Tests DSA AlgorithmParameters provider
     */
    public void testDSAProvider() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("DSA");

        assertEquals("Algorithm", "DSA", params.getAlgorithm());

        // init(AlgorithmParameterSpec)
        BigInteger p = BigInteger.ONE;
        BigInteger q = BigInteger.TEN;
        BigInteger g = BigInteger.ZERO;
        params.init(new DSAParameterSpec(p, q, g));

        // getEncoded() and getEncoded(String) (TODO verify returned encoding)
        byte[] enc = params.getEncoded();
        assertNotNull(enc);
        assertNotNull(params.getEncoded("ASN.1"));
        // TODO assertNotNull(params.getEncoded(null)); // HARMONY-2680

        // getParameterSpec(Class)
        DSAParameterSpec spec = params.getParameterSpec(DSAParameterSpec.class);
        assertEquals("p is wrong ", p, spec.getP());
        assertEquals("q is wrong ", q, spec.getQ());
        assertEquals("g is wrong ", g, spec.getG());

        // init(byte[])
        params = AlgorithmParameters.getInstance("DSA");
        params.init(enc);
        assertTrue("param encoded is different", Arrays.equals(enc, params
                .getEncoded()));

        // init(byte[], String)
        params = AlgorithmParameters.getInstance("DSA");
        params.init(enc, "ASN.1");
        assertTrue("param encoded is different", Arrays.equals(enc, params
                .getEncoded()));

        params = AlgorithmParameters.getInstance("DSA");
        try {
            params.init(enc, "DOUGLASMAWSON");
            fail("unsupported format should have raised IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * Tests OAEP AlgorithmParameters provider
     */
    public void testOAEPProvider() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("OAEP");

        assertEquals("Algorithm", "OAEP", params.getAlgorithm());
    }

    /**
     * Test for <code>AlgorithmParameters</code> constructor
     * Assertion: returns AlgorithmParameters object
     */
    public void testAlgorithmParametersConst() throws Exception {
        AlgorithmParametersSpi spi = new MyAlgorithmParameters();
        AlgorithmParameters ap = new myAlgP(spi, p, "ABC");

        checkUnititialized(ap);
        ap.init(new byte[6], "aaa");
        checkAP(ap, p);

        //NULL parameters
        try {
            ap = new myAlgP(null, null, null);
        } catch (Exception e){
            fail("Exception should be not thrown");
        }
    }

    private void checkUnititialized(AlgorithmParameters ap) {
        assertNull("Uninitialized: toString() failed", ap.toString());
    }

    private void checkAP(AlgorithmParameters ap, Provider p) throws Exception {

        assertSame("getProvider() failed", p, ap.getProvider());
        assertEquals("getAlgorithm() failed", "ABC", ap.getAlgorithm());

        assertEquals("AlgorithmParameters", ap.toString());
        assertTrue("toString() failed", MyAlgorithmParameters.runEngineToString);
    }

    @SuppressWarnings("serial")
    private class MyProvider extends Provider {
        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("AlgorithmParameters.ABC", MyAlgorithmParameters.class
                    .getName());
        }

        MyProvider(String name, double version, String info) {
            super(name, version, info);
        }
    }

    private class MyAlgorithmParameterSpec implements java.security.spec.AlgorithmParameterSpec{
    }

    private class DummyAlgorithmParameters extends AlgorithmParameters {
        public DummyAlgorithmParameters(AlgorithmParametersSpi paramSpi,
                Provider provider, String algorithm) {
            super(paramSpi, provider, algorithm);
        }
    }

    public static class MyAlgorithmParameters extends AlgorithmParametersSpi {

        public boolean runEngineInit_AlgParamSpec = false;

        public boolean runEngineInitB$ = false;

        public boolean runEngineInitB$String = false;

        public static boolean runEngineToString = false;

        protected void engineInit(AlgorithmParameterSpec paramSpec)
                throws InvalidParameterSpecException {
        }

        protected void engineInit(byte[] params) throws IOException {
        }

        protected void engineInit(byte[] params, String format)
                throws IOException {
        }

        protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec)
                throws InvalidParameterSpecException {
            return null;
        }

        protected byte[] engineGetEncoded() throws IOException {
            return null;
        }

        protected byte[] engineGetEncoded(String format) throws IOException {
            return null;
        }

        protected String engineToString() {
            runEngineToString = true;
            return "AlgorithmParameters";
        }
    }

    /**
     * Additional class to verify AlgorithmParameters constructor
     */
    class myAlgP extends AlgorithmParameters {
        public myAlgP(AlgorithmParametersSpi spi, Provider prov, String alg) {
            super(spi, prov, alg);
        }
    }
}
