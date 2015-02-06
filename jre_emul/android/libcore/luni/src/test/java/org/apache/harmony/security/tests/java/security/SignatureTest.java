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

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.harmony.security.tests.support.MySignature1;

import junit.framework.TestCase;

/**
 * Tests for <code>Signature</code> constructor and methods
 *
 */
public class SignatureTest extends TestCase {

    /*
     * Class under test for Signature(String)
     */
    public void testConstructor() {
        String [] algorithms = { "SHA256WITHRSA", "NONEWITHDSA", "SHA384WITHRSA",
            "MD5ANDSHA1WITHRSA", "SHA512WITHRSA",
            "SHA1WITHRSA", "SHA1WITHDSA", "MD5WITHRSA" };
        for (int i = 0; i < algorithms.length; i ++) {
            MySignature1 s = new MySignature1(algorithms[i]);
            assertEquals(algorithms[i],s.getAlgorithm());
            assertNull(s.getProvider());
            assertEquals(0, s.getState());
        }

        MySignature1 s1 = new MySignature1(null);
        assertNull(s1.getAlgorithm());
        assertNull(s1.getProvider());
        assertEquals(0, s1.getState());

        MySignature1 s2 = new MySignature1("ABCD@#&^%$)(*&");
        assertEquals("ABCD@#&^%$)(*&", s2.getAlgorithm());
        assertNull(s2.getProvider());
        assertEquals(0, s2.getState());
    }

    /*
     * Class under test for Object clone()
     */
    public void testClone() {
        MySignature1 s = new MySignature1("ABC");
        try {
            s.clone();
            fail("No expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
        }

        MySignature sc = new MySignature();
        try {
            sc.clone();
        } catch (CloneNotSupportedException e) {
            fail("unexpected exception: " + e);
        }

    }

    public void testGetProvider() {
        MySignature1 s = new MySignature1("ABC");

        assertEquals("state", MySignature1.UNINITIALIZED, s.getState());
        assertNull("provider", s.getProvider());
    }

    public void testGetAlgorithm() {
        MySignature1 s = new MySignature1("ABC");

        assertEquals("state", MySignature1.UNINITIALIZED, s.getState());
        assertEquals("algorithm", "ABC", s.getAlgorithm());
    }

    /*
     * Class under test for void initVerify(PublicKey)
     */
    public void testInitVerifyPublicKey() throws InvalidKeyException {
        MySignature1 s = new MySignature1("ABC");

        s.initVerify(new MyPublicKey());
        assertEquals("state", MySignature1.VERIFY, s.getState());
        assertTrue("initVerify() failed", s.runEngineInitVerify);

        try {
            Signature sig = getTestSignature();
            sig.initVerify((PublicKey)null);
        } catch (InvalidKeyException e) {
            // ok
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected : " + e);
        }
    }

    /*
     * Class under test for void initVerify(Certificate)
     */
    public void testInitVerifyCertificate() throws InvalidKeyException {
        MySignature1 s = new MySignature1("ABC");

        s.initVerify(new MyCertificate());
        assertEquals("state", MySignature1.VERIFY, s.getState());
        assertTrue("initVerify() failed", s.runEngineInitVerify);

        try {
            Signature sig = getTestSignature();
            sig.initVerify(new MyCertificate());
            fail("expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            // ok
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected : " + e);
        }
    }

    /*
     * Class under test for void initSign(PrivateKey)
     */
    public void testInitSignPrivateKey() throws InvalidKeyException {
        MySignature1 s = new MySignature1("ABC");

        s.initSign(new MyPrivateKey());
        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("initSign() failed", s.runEngineInitSign);

        try {
            Signature signature = getTestSignature();
            signature.initSign(null);
            fail("expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            // ok
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        }
    }

    private Signature getTestSignature() throws NoSuchAlgorithmException {
        Provider provider = new MyProvider("TestProvider", 1.0, "Test Provider", "Signature.ABC", MySignature.class.getName());
        Security.insertProviderAt(provider, 1);

        try {
            return Signature.getInstance("ABC");
        }
        finally {
           Security.removeProvider("TestProvider");
        }

    }

    /*
     * Class under test for void initSign(PrivateKey, SecureRandom)
     */
    public void testInitSignPrivateKeySecureRandom() throws InvalidKeyException {
        MySignature1 s = new MySignature1("ABC");

        s.initSign(new MyPrivateKey(), new SecureRandom());
        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("initSign() failed", s.runEngineInitSign);

        try {
            Signature sig = getTestSignature();
            sig.initSign(null, null);
            fail("expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            // ok
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected : " + e);
        }
    }

    /*
     * Class under test for byte[] sign()
     */
    public void testSign() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        try {
            s.sign();
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initVerify(new MyPublicKey());

        try {
            s.sign();
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initSign(new MyPrivateKey());
        s.sign();
        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("sign() failed", s.runEngineSign);
    }

    /*
     * Class under test for sign(byte[], offset, len)
     */
    public void testSignbyteintint() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] outbuf = new byte [10];
        try {
            s.sign(outbuf, 0, outbuf.length);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initVerify(new MyPublicKey());

        try {
            s.sign(outbuf, 0, outbuf.length);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initSign(new MyPrivateKey());
        assertEquals(s.getBufferLength(), s.sign(outbuf, 0, outbuf.length));
        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("sign() failed", s.runEngineSign);

        try {
            s.initSign(new MyPrivateKey());
            s.sign(outbuf, outbuf.length, 0);
            fail("expected SignatureException");
        } catch (SignatureException e) {
            // ok
        }

        try {
            s.initSign(new MyPrivateKey());
            s.sign(outbuf, outbuf.length, 3);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }

    }


    /*
     * Class under test for boolean verify(byte[])
     */
    public void testVerifybyteArray() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};
        try {
            s.verify(b);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initSign(new MyPrivateKey());
        try {
            s.verify(b);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initVerify(new MyPublicKey());
        s.verify(b);
        assertEquals("state", MySignature1.VERIFY, s.getState());
        assertTrue("verify() failed", s.runEngineVerify);
    }

    /*
     * Class under test for boolean verify(byte[], int, int)
     */
    public void testVerifybyteArrayintint() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};
        try {
            s.verify(b, 0, 3);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initSign(new MyPrivateKey());

        try {
            s.verify(b, 0, 3);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initVerify(new MyPublicKey());

        try {
            s.verify(b, 0, 5);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        s.verify(b, 0, 3);
        assertEquals("state", MySignature1.VERIFY, s.getState());
        assertTrue("verify() failed", s.runEngineVerify);
    }

    /*
     * Class under test for void update(byte)
     */
    public void testUpdatebyte() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        try {
            s.update((byte)1);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initVerify(new MyPublicKey());
        s.update((byte) 1);
        s.initSign(new MyPrivateKey());
        s.update((byte) 1);

        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("update() failed", s.runEngineUpdate1);

        try {
            Signature sig = getTestSignature();
            sig.update((byte) 42);
            fail("expected SignatureException");
        } catch (SignatureException e) {
            // ok
        }
    }

    /*
     * Class under test for void update(byte[])
     */
    public void testUpdatebyteArray() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};
        try {
            s.update(b);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initVerify(new MyPublicKey());
        s.update(b);
        s.initSign(new MyPrivateKey());
        s.update(b);

        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("update() failed", s.runEngineUpdate2);

        try {
            Signature sig = getTestSignature();
            sig.update(b);
            fail("expected SignatureException");
        } catch (SignatureException e) {
            // ok
        }

        try {
            Signature sig = getTestSignature();
            sig.update((byte[])null);
            fail("expected NullPointerException");
        } catch (SignatureException e) {
            // ok
        } catch (NullPointerException e) {
            // ok
        }
    }

    /*
     * Class under test for void update(byte[], int, int)
     */
    public void testUpdatebyteArrayintint() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};
        try {
            s.update(b, 0, 3);
            fail("No expected SignatureException");
        } catch (SignatureException e) {
        }

        s.initVerify(new MyPublicKey());
        s.update(b, 0, 3);
        s.initSign(new MyPrivateKey());
        s.update(b, 0, 3);

        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("update() failed", s.runEngineUpdate2);

        try {
            s.update(b, 3, 0);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            s.update(b, 0, b.length + 1);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            s.update(b, -1, b.length);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }

    }

    /*
     * Class under test for void update(byte[], int, int)
     */
    public void testUpdatebyteArrayintint2() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};

        s.initVerify(new MyPublicKey());
        s.update(b, 0, 3);
        s.initSign(new MyPrivateKey());
        s.update(b, 0, 3);

        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("update() failed", s.runEngineUpdate2);

        try {
            s.update(null, 0, 3);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
            // ok
        } catch (IllegalArgumentException se) {
            // ok
        }
    }


    /*
     * Class under test for void setParameter(String, Object)
     */
    @SuppressWarnings("deprecation")
    public void testSetParameterStringObject() {
        MySignature1 s = new MySignature1("ABC");
        s.setParameter("aaa", new Object());

        try {
            Signature sig = getTestSignature();
            sig.setParameter("TestParam", new Integer(42));
            fail("expected InvalidParameterException");
        } catch (InvalidParameterException e) {
            // expected
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        }
    }

    /*
     * Class under test for void setParameter(AlgorithmParameterSpec)
     */
    public void testSetParameterAlgorithmParameterSpec() throws InvalidAlgorithmParameterException {
        MySignature1 s = new MySignature1("ABC");
        try {
            s.setParameter((java.security.spec.AlgorithmParameterSpec)null);
            fail("No expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e){
        }

        try {
            Signature sig = getTestSignature();
            sig.setParameter(new AlgorithmParameterSpec() {});
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        }
    }

    @SuppressWarnings("deprecation")
    public void testGetParameter() {
        MySignature1 s = new MySignature1("ABC");
        s.getParameter("aaa");

        try {
            MySignature se = new MySignature();
            se.getParameter("test");
        } catch (InvalidParameterException e) {
            // ok
        }

    }

    private class MyKey implements Key {
        public String getFormat() {
            return "123";
        }
        public byte[] getEncoded() {
            return null;
        }
        public String getAlgorithm() {
            return "aaa";
        }
    }

    private class MyPublicKey extends MyKey implements PublicKey {}

    private class MyPrivateKey extends MyKey implements PrivateKey {}

    private class MyCertificate extends java.security.cert.Certificate {
        public  MyCertificate() {
            super("MyCertificateType");
        }

        public PublicKey getPublicKey() {
            return new MyPublicKey();
        }

        public byte[] getEncoded() {
            return null;
        }
        public void verify(PublicKey key) {}

        public void verify(PublicKey key, String sigProvider) {}

        public String toString() {
            return "MyCertificate";
        }
    }

    @SuppressWarnings("unused")
    protected static class MySignature extends Signature implements Cloneable {

        public MySignature() {
            super("TestSignature");
        }

        @Override
        protected Object engineGetParameter(String param)
                throws InvalidParameterException {
            throw new InvalidParameterException();
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey)
                throws InvalidKeyException {
            throw new InvalidKeyException();
        }

        @Override
        protected void engineInitVerify(PublicKey publicKey)
                throws InvalidKeyException {
            throw new InvalidKeyException();
        }

        @Override
        protected void engineSetParameter(String param, Object value)
                throws InvalidParameterException {
            throw new InvalidParameterException();
        }

        @Override
        protected byte[] engineSign() throws SignatureException {
            return null;
        }

        @Override
        protected void engineUpdate(byte b) throws SignatureException {
            throw new SignatureException();
        }

        @Override
        protected void engineUpdate(byte[] b, int off, int len)
                throws SignatureException {

        }

        @Override
        protected boolean engineVerify(byte[] sigBytes)
                throws SignatureException {
            return false;
        }

        @Override
        protected void engineSetParameter(AlgorithmParameterSpec params)
                throws InvalidAlgorithmParameterException {
            if (params == null) {
                throw new InvalidAlgorithmParameterException();
            }
        }
    }

    private class MyProvider extends Provider {

        protected MyProvider(String name, double version, String info, String signame, String className) {
            super(name, version, info);
            put(signame, className);
        }

    }
}
