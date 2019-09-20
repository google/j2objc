package org.apache.harmony.security.tests.java.security;

import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashSet;
import java.util.Set;

public class SignatureSpiTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SuppressWarnings("cast")
    public void testSignatureSpi() {
        try {
            MySignatureSpi1 ss1 = new MySignatureSpi1();
            assertNotNull(ss1);
            assertTrue(ss1 instanceof SignatureSpi);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }

    public void testClone() {
        MySignatureSpi1 ss1 = new MySignatureSpi1();
        try {
            MySignatureSpi1 ssc1 = (MySignatureSpi1) ss1.clone();
            assertTrue(ss1 != ssc1);
        } catch (CloneNotSupportedException e) {
            fail("Unexpected CloneNotSupportedException " + e.getMessage());
        }

        MySignatureSpi2 ss2 = new MySignatureSpi2();
        try {
            ss2.clone();
            fail("CloneNotSupportedException expected ");
        } catch (CloneNotSupportedException e) {
            // expected
        }
    }

    public void testAbstractMethods() {
        MySignatureSpi1 ss1 = new MySignatureSpi1();
        byte[] b = {0, 1, 2, 3, 4, 5};
        try {
            ss1.engineGetParameter("test");
            ss1.engineInitSign(null);
            ss1.engineInitVerify(null);
            ss1.engineSetParameter("test", null);
            ss1.engineSign();
            ss1.engineUpdate(b[1]);
            ss1.engineUpdate(b, 0, b.length);
            ss1.engineVerify(b);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }

    private boolean engineGetParametersCalled = false;
    private boolean engineGetParametersExceptionOcurred = false;

    public void testEngineGetParameters() {
        // or rather test that no UnsupportedOperationException is thrown?

        @SuppressWarnings("unused")
        Signature s = new Signature("dummy") {
            protected AlgorithmParameters engineGetParameters() {
                engineGetParametersCalled = true;
                try {
                    super.engineGetParameters();
                } catch (UnsupportedOperationException e) {
                    engineGetParametersExceptionOcurred = true;
                }
                return null;
            }

            @Override
            protected Object engineGetParameter(String param)
                    throws InvalidParameterException {
                return null;
            }

            @Override
            protected void engineInitSign(PrivateKey privateKey)
                    throws InvalidKeyException {

            }

            @Override
            protected void engineInitVerify(PublicKey publicKey)
                    throws InvalidKeyException {

            }

            @Override
            protected void engineSetParameter(String param, Object value)
                    throws InvalidParameterException {

            }

            @Override
            protected byte[] engineSign() throws SignatureException {
                return null;
            }

            @Override
            protected void engineUpdate(byte b) throws SignatureException {

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
        };
        // must call engineGetParameters
        s.getParameters();
        assertTrue(engineGetParametersCalled);
        assertTrue(engineGetParametersExceptionOcurred);
    }

    class MySignatureSpi1 extends SignatureSpi implements Cloneable {

        public Object engineGetParameter(String param) {
            return null;
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public void engineInitSign(PrivateKey privateKey) {
        }

        public void engineInitVerify(PublicKey publicKey) {
        }

        public void engineSetParameter(String param, Object value) {
        }

        public byte[] engineSign() {
            return null;
        }

        public void engineUpdate(byte b) {
        }

        public void engineUpdate(byte[] b, int off, int len) {
        }

        public boolean engineVerify(byte[] sigBytes) {
            return false;
        }

    }

    class MySignatureSpi2 extends SignatureSpi {
        public Object engineGetParameter(String param) {
            return null;
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public void engineInitSign(PrivateKey privateKey) {
        }

        public void engineInitVerify(PublicKey publicKey) {
        }

        public void engineSetParameter(String param, Object value) {
        }

        public byte[] engineSign() {
            return null;
        }

        public void engineUpdate(byte b) {
        }

        public void engineUpdate(byte[] b, int off, int len) {
        }

        public boolean engineVerify(byte[] sigBytes) {
            return false;
        }
    }

    @SuppressWarnings("unused")
    class MySignature extends Signature {

        Set<String> calledMethods = new HashSet<String>();
        protected MySignature(String algorithm) {
            super(algorithm);
        }

        @Override
        protected Object engineGetParameter(String param)
                throws InvalidParameterException {
            methodCalled("engineGetParameter_String");
            return null;
        }


        @Override
        protected void engineInitSign(PrivateKey privateKey)
                throws InvalidKeyException {
            methodCalled("engineInitSign_PrivateKey");
        }

        @Override
        protected void engineInitVerify(PublicKey publicKey)
                throws InvalidKeyException {
            methodCalled("engineInitVerify_PublicKey");
        }

        @Override
        protected void engineSetParameter(String param, Object value)
                throws InvalidParameterException {
            methodCalled("engineSetParameter_String_Object");
        }

        @Override
        protected byte[] engineSign() throws SignatureException {
            methodCalled("engineSign");
            return null;
        }

        @Override
        protected void engineUpdate(byte b) throws SignatureException {
            methodCalled("engineUpdate_[B");
        }

        @Override
        protected void engineUpdate(byte[] b, int off, int len)
                throws SignatureException {
            methodCalled("engineUpdate_[BII");
        }

        @Override
        protected boolean engineVerify(byte[] sigBytes)
                throws SignatureException {
            methodCalled("engineVerify_[B");
            return false;
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey, SecureRandom random)
                throws InvalidKeyException {
            methodCalled("engineInitSign_PrivateKey_SecureRandom");
        }

        @Override
        protected void engineSetParameter(AlgorithmParameterSpec params)
                throws InvalidAlgorithmParameterException {
            methodCalled("engineSetParameter_AlgorithmParameterSpec");
        }

        @Override
        protected int engineSign(byte[] outbuf, int offset, int len)
                throws SignatureException {
            methodCalled("engineSign_[BII");
            return 0;
        }

        @Override
        protected void engineUpdate(ByteBuffer input) {
            methodCalled("engineUpdate_ByteBuffer");
        }

        @Override
        protected boolean engineVerify(byte[] sigBytes, int offset, int length)
                throws SignatureException {
            methodCalled("engineVerify_[BII");
            return false;
        }

        boolean wasMethodCalled(String methodName) {
            return calledMethods.contains(methodName);
        }

        void methodCalled(String methodName) {
            calledMethods.add(methodName);
        }
    }

    public void testEngineInitSign_PrivateKey_SecureRandom() {
        MySignature signature = new MySignature("dummy");

        try {
            signature.initSign(null, null);
            assertTrue("SPI method not called", signature
                    .wasMethodCalled("engineInitSign_PrivateKey_SecureRandom"));
        } catch (InvalidKeyException e) {
            fail("unexpected exception: " + e);
        }
    }

    public void testEngineSetParameter()
    {
        MySignature signature = new MySignature("dummy");

        try {
            signature.setParameter(null);
            assertTrue(
                    "SPI method not called",
                    signature
                            .wasMethodCalled("engineSetParameter_AlgorithmParameterSpec"));
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected exception: " + e);
        }
    }

    public void testEngineSign_BII() {
        MySignature signature = new MySignature("dummy");
        try {
            signature.initSign(new PrivateKey() {

                public String getFormat() {
                    return null;
                }

                public byte[] getEncoded() {
                    return null;
                }

                public String getAlgorithm() {
                    return null;
                }
            });
        } catch (InvalidKeyException e) {
            fail("unexpected exception: " + e);
        }
        byte[] buf = new byte[10];
        try {
            signature.sign(buf, 2, 1);
            assertTrue("SPI method not called", signature
                    .wasMethodCalled("engineSign_[BII"));
        } catch (SignatureException e) {
            fail("unexpected exception: " + e);
        }
    }

    public void testEngineUpdate_ByteBuffer() {
        MySignature signature = new MySignature("dummy");
        try {
            signature.initSign(new PrivateKey() {

                public String getFormat() {
                    return null;
                }

                public byte[] getEncoded() {
                    return null;
                }

                public String getAlgorithm() {
                    return null;
                }
            });
        } catch (InvalidKeyException e) {
            fail("unexpected exception: " + e);
        }

        try {
            signature.update(ByteBuffer.wrap("Hello".getBytes()));
            assertTrue("SPI method not called", signature
                    .wasMethodCalled("engineUpdate_ByteBuffer"));
        } catch (SignatureException e) {
            fail("unexpected exception");
        }
    }

    public void testEngineVerify_BII() {
        MySignature signature = new MySignature("dummy");

        try {
            signature.initVerify(new PublicKey() {

                public String getFormat() {
                    return null;
                }

                public byte[] getEncoded() {
                    return null;
                }

                public String getAlgorithm() {
                    return null;
                }
            });
        } catch (InvalidKeyException e) {
            fail("unexpected exception");
        }

        byte[] buf = new byte[10];

        try {
            signature.verify(buf, 2, 5);
            signature.wasMethodCalled("engineVerify_[BII");
        } catch (SignatureException e) {
            fail("unexpected exception");
        }
    }

}
