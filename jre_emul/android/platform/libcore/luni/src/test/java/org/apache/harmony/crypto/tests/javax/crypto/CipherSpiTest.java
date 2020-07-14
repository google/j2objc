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

package org.apache.harmony.crypto.tests.javax.crypto;

import java.nio.DirectByteBuffer;
import java.security.spec.AlgorithmParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.AlgorithmParameters;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.CipherSpi;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;


/**
 * Tests for <code>CipherSpi</code> class constructors and methods.
 *
 */
public class CipherSpiTest extends TestCase {
    class Mock_CipherSpi extends myCipherSpi {

        @Override
        protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
                throws IllegalBlockSizeException, BadPaddingException {
            return super.engineDoFinal(input, inputOffset, inputLen);
        }

        @Override
        protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output,
                int outputOffset) throws ShortBufferException, IllegalBlockSizeException,
                BadPaddingException {
            return super.engineDoFinal(input, inputOffset, inputLen, output, outputOffset);
        }

        @Override
        protected int engineGetBlockSize() {
            return super.engineGetBlockSize();
        }

        @Override
        protected byte[] engineGetIV() {
            return super.engineGetIV();
        }

        @Override
        protected int engineGetOutputSize(int inputLen) {
            return super.engineGetOutputSize(inputLen);
        }

        @Override
        protected AlgorithmParameters engineGetParameters() {
            return super.engineGetParameters();
        }

        @Override
        protected void engineInit(int opmode, Key key, SecureRandom random)
                throws InvalidKeyException {
            super.engineInit(opmode, key, random);
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            super.engineInit(opmode, key, params, random);
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameters params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            super.engineInit(opmode, key, params, random);
        }

        @Override
        protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
            super.engineSetMode(mode);
        }

        @Override
        protected void engineSetPadding(String padding) throws NoSuchPaddingException {
            super.engineSetPadding(padding);
        }

        @Override
        protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
            return super.engineUpdate(input, inputOffset, inputLen);
        }

        @Override
        protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output,
                int outputOffset) throws ShortBufferException {
            return super.engineUpdate(input, inputOffset, inputLen, output, outputOffset);
        }

        @Override
        protected void engineUpdateAAD(byte[] src, int offset, int len) {
            super.engineUpdateAAD(src, offset, len);
        }

        @Override
        protected void engineUpdateAAD(ByteBuffer buf) {
            super.engineUpdateAAD(buf);
        }

        @Override
        protected int engineGetKeySize(Key key) throws InvalidKeyException {
            return super.engineGetKeySize(key);
        }

        @Override
        protected byte[] engineWrap(Key key) throws InvalidKeyException, IllegalBlockSizeException {
            return super.engineWrap(key);
        }

        @Override
        protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
                throws InvalidKeyException, NoSuchAlgorithmException {
            return super.engineUnwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
        }
    }

    /**
     * Test for <code>CipherSpi</code> constructor
     * Assertion: constructs CipherSpi
     */
    public void testCipherSpiTests01() throws IllegalBlockSizeException,
            BadPaddingException, ShortBufferException {

        Mock_CipherSpi cSpi = new Mock_CipherSpi();
        assertEquals("BlockSize is not 0", cSpi.engineGetBlockSize(), 0);
        assertEquals("OutputSize is not 0", cSpi.engineGetOutputSize(1), 0);
        byte[] bb = cSpi.engineGetIV();
        assertEquals("Length of result byte array is not 0", bb.length, 0);
        assertNull("Not null result", cSpi.engineGetParameters());
        byte[] bb1 = new byte[10];
        byte[] bb2 = new byte[10];
        bb = cSpi.engineUpdate(bb1, 1, 2);
        assertEquals("Incorrect result of engineUpdate(byte, int, int)",
                bb.length, 2);
        bb = cSpi.engineDoFinal(bb1, 1, 2);
        assertEquals("Incorrect result of engineDoFinal(byte, int, int)", 2,
                bb.length);
        assertEquals(
                "Incorrect result of engineUpdate(byte, int, int, byte, int)",
                cSpi.engineUpdate(bb1, 1, 2, bb2, 7), 2);
        assertEquals(
                "Incorrect result of engineDoFinal(byte, int, int, byte, int)",
                2, cSpi.engineDoFinal(bb1, 1, 2, bb2, 0));
    }

    /**
     * Test for <code>engineGetKeySize(Key)</code> method
     * Assertion: It throws UnsupportedOperationException if it is not overridden
     */
    public void testCipherSpi02() throws Exception {
        Mock_CipherSpi cSpi = new Mock_CipherSpi();
        try {
            cSpi.engineGetKeySize(null);
            fail("UnsupportedOperationException must be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * Test for <code>engineWrap(Key)</code> method
     * Assertion: It throws UnsupportedOperationException if it is not overridden
     */
    public void testCipherSpi03() throws Exception {
        Mock_CipherSpi cSpi = new Mock_CipherSpi();
        try {
            cSpi.engineWrap(null);
            fail("UnsupportedOperationException must be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * Test for <code>engineUnwrap(byte[], String, int)</code> method
     * Assertion: It throws UnsupportedOperationException if it is not overridden
     */
    public void testCipherSpi04() throws Exception {
        Mock_CipherSpi cSpi = new Mock_CipherSpi();
        try {
            cSpi.engineUnwrap(new byte[0], "", 0);
            fail("UnsupportedOperationException must be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * Test for <code>engineUpdate(ByteBuffer, ByteBuffer)</code> method
     * Assertions:
     * throws NullPointerException if one of these buffers is null;
     * throws ShortBufferException is there is no space in output to hold result
     */
    public void testCipherSpi05() throws ShortBufferException {
        Mock_CipherSpi cSpi = new Mock_CipherSpi();
        byte[] bb = { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4,
                (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10 };
        int pos = 5;
        int len = bb.length;
        ByteBuffer bbNull = null;
        ByteBuffer bb1 = ByteBuffer.allocate(len);
        bb1.put(bb);
        bb1.position(0);
        try {
            cSpi.engineUpdate(bbNull, bb1);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        try {
            cSpi.engineUpdate(bb1, bbNull);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        ByteBuffer bb2 = ByteBuffer.allocate(bb.length);
        bb1.position(len);
        assertEquals("Incorrect number of stored bytes", 0, cSpi.engineUpdate(
                bb1, bb2));

        bb1.position(0);
        bb2.position(len - 2);
        try {
            cSpi.engineUpdate(bb1, bb2);
            fail("ShortBufferException bust be thrown. Output buffer remaining: "
                    .concat(Integer.toString(bb2.remaining())));
        } catch (ShortBufferException e) {
        }
        bb1.position(10);
        bb2.position(0);
        assertTrue("Incorrect number of stored bytes", cSpi.engineUpdate(bb1,
                bb2) > 0);
        bb1.position(bb.length);
        cSpi.engineUpdate(bb1, bb2);

        bb1.position(pos);
        bb2.position(0);
        int res = cSpi.engineUpdate(bb1, bb2);
        assertTrue("Incorrect result", res > 0);
    }

    /**
     * Test for <code>engineDoFinal(ByteBuffer, ByteBuffer)</code> method
     * Assertions:
     * throws NullPointerException if one of these buffers is null;
     * throws ShortBufferException is there is no space in output to hold result
     */
    public void testCipherSpi06() throws BadPaddingException,
            ShortBufferException, IllegalBlockSizeException {
        Mock_CipherSpi cSpi = new Mock_CipherSpi();
        int len = 10;
        byte[] bbuf = new byte[len];
        for (int i = 0; i < bbuf.length; i++) {
            bbuf[i] = (byte) i;
        }
        ByteBuffer bb1 = ByteBuffer.wrap(bbuf);
        ByteBuffer bbNull = null;
        try {
            cSpi.engineDoFinal(bbNull, bb1);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        try {
            cSpi.engineDoFinal(bb1, bbNull);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        ByteBuffer bb2 = ByteBuffer.allocate(len);
        bb1.position(bb1.limit());
        assertEquals("Incorrect result", 0, cSpi.engineDoFinal(bb1, bb2));

        bb1.position(0);
        bb2.position(len - 2);
        try {
            cSpi.engineDoFinal(bb1, bb2);
            fail("ShortBufferException must be thrown. Output buffer remaining: "
                    .concat(Integer.toString(bb2.remaining())));
        } catch (ShortBufferException e) {
        }
        int pos = 5;
        bb1.position(pos);
        bb2.position(0);
        assertTrue("Incorrect result", cSpi.engineDoFinal(bb1, bb2) > 0);
    }

    /**
     * Test for <code>engineUpdateAAD(ByteBuffer)</code> method
     * Assertion: It throws UnsupportedOperationException if it is not overridden
     */
    public void testCipherSpi07() {
        Mock_CipherSpi cSpi = new Mock_CipherSpi();
        try {
            cSpi.engineUpdateAAD(ByteBuffer.wrap(new byte[] { 0x00, 0x01}));
            fail("UnsupportedOperationException must be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * Test for <code>engineUpdateAAD(byte[], int, int)</code> method
     * Assertion: It throws UnsupportedOperationException if it is not overridden
     */
    public void testCipherSpi08() {
        Mock_CipherSpi cSpi = new Mock_CipherSpi();
        try {
            cSpi.engineUpdateAAD(new byte[] { 0x00, 0x01}, 0, 2);
            fail("UnsupportedOperationException must be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testCrypt_doNotCallPositionInNonArrayBackedInputBuffer() throws Exception {
        ByteBuffer nonArrayBackedInputBuffer = new MockNonArrayBackedByteBuffer(10, false);
        ByteBuffer nonArrayBackedOutputBuffer = new MockNonArrayBackedByteBuffer(10, false);
        Mock_CipherSpi cipherSpi = new Mock_CipherSpi() {
            public int engineGetOutputSize(int inputLength) {
                return inputLength;
            }
        };
        cipherSpi.engineUpdate(nonArrayBackedInputBuffer, nonArrayBackedOutputBuffer);
        assertEquals(0, nonArrayBackedInputBuffer.position());
    }

    public void testCrypt_doNotCallPutForZeroLengthOutput() throws Exception {
        ByteBuffer nonArrayBackedInputBuffer = new MockNonArrayBackedByteBuffer(10, false);
        ByteBuffer nonArrayBackedOutputBuffer = new MockNonArrayBackedByteBuffer(10, false) {
            @Override
            public ByteBuffer put(byte[] dst, int offset, int length) {
                if (length == 0) {
                    throw new IllegalStateException("put shouldn't be called with length 0");
                }
                return this;
            }
        };

        Mock_CipherSpi cipherSpi = new Mock_CipherSpi() {
            public int engineUpdate(
                    byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
                return 0;
            }
        };

        // The put method is not called in the output buffer and so the test passes.
        cipherSpi.engineUpdate(nonArrayBackedInputBuffer, nonArrayBackedOutputBuffer);
    }

    // In case a call to engineGetOutputSize returns 0 for the whole input size, but a positive
    // value for the chunk size to be written, check that the positive output size is used in the
    // second attempt to read from the the buffer.
    public void testCrypt_outputSizeUpdatedAfterShortBufferException()
            throws Exception {

        // 4096 is the value hardcoded for a maximum array allocation in CipherSpi#getTempArraySize
        final int maxInternalArrayAllocation = 4096;
        // The length of the input is greater than the max chunk allowed, so the size of the chunk
        // and the size of the input will differ.
        final int testInputLength = maxInternalArrayAllocation + 1;
        // Length to be returned the second time engineGetOutputSize is called (that is, when it's
        // called with maxInternalArrayAllocation). First length returned (that is, when it's
        // called with testInputLength) is 0.
        final int testSecondOutputLength = 1000;

        final AtomicInteger firstGetLength = new AtomicInteger(0);
        final AtomicInteger secondGetLength = new AtomicInteger(0);

        ByteBuffer inputBuffer = new MockNonArrayBackedByteBuffer(testInputLength, false) {
            private boolean getWasCalled = false;

            @Override
            public ByteBuffer get(byte[] dst, int offset, int length) {
                if (!getWasCalled) {
                    getWasCalled = true;
                    firstGetLength.set(length);
                } else {
                    if (secondGetLength.get() == 0) {
                        secondGetLength.set(length);
                    }
                }
                return this;
            }
        };

        ByteBuffer outputBuffer = new MockNonArrayBackedByteBuffer(10, false);

        Mock_CipherSpi cipherSpi = new Mock_CipherSpi() {
            @Override
            public int engineGetOutputSize(int inputLength) {
                if (inputLength == testInputLength) {
                    return 0;
                } else if (inputLength == maxInternalArrayAllocation) {
                    return testSecondOutputLength;
                } else {
                    throw new IllegalStateException("Unexpected value " + inputLength);
                }
            }

            @Override
            public int engineUpdate(
                    byte[] inArray, int inOfs, int inLen, byte[] outArray, int outputOffset)
                    throws ShortBufferException {
                if (inLen == maxInternalArrayAllocation) {
                    throw new ShortBufferException("to be caught in order to retry with a new"
                            + "output size");
                }
                return 0;
            }
        };

        cipherSpi.engineUpdate(inputBuffer, outputBuffer);

        assertEquals(
                "first call to get must use the input length, as the output length "
                        + "from engineGetOutputSize is 0",
                maxInternalArrayAllocation,
                firstGetLength.get());

        assertEquals(
                "second call to get must use the new output length",
                testSecondOutputLength,
                secondGetLength.get());
    }

    // The tests using ByteBuffer depend on final methods (like hasArray) that cannot be mocked in
    // Mockito, so the mock is done manually. ByteBuffer has abstract methods that are
    // package-private, so extending DirectByteBuffer. It happens to be not backed by an array, so
    // we use it when we need a byte buffer not array-backed.
    private class MockNonArrayBackedByteBuffer extends DirectByteBuffer {
        public MockNonArrayBackedByteBuffer(int capacity, boolean isReadOnly) {
            super(capacity, 0 /* addr */, null /* fd */, null /* unmapper */, isReadOnly);
        }

        @Override
        public ByteBuffer get(byte[] dst, int offset, int length) {
            return this;
        }

        @Override
        public ByteBuffer put(byte[] dst, int offset, int length) {
            return this;
        }
    }
}
/**
 *
 * Additional class for CipherGeneratorSpi constructor verification
 */

class myCipherSpi extends CipherSpi {
    private byte[] initV;

    private static byte[] resV = { (byte) 7, (byte) 6, (byte) 5, (byte) 4,
            (byte) 3, (byte) 2, (byte) 1, (byte) 0 };

    public myCipherSpi() {
        this.initV = new byte[0];
    }

    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    }

    protected void engineSetPadding(String padding)
            throws NoSuchPaddingException {
    }

    protected int engineGetBlockSize() {
        return 0;
    }

    protected int engineGetOutputSize(int inputLen) {
        return 0;
    }

    protected byte[] engineGetIV() {
        return new byte[0];
    }

    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
    }

    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameters params,
            SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        if (initV.length < inputLen) {
            initV = new byte[inputLen];
        }
        for (int i = 0; i < inputLen; i++) {
            initV[i] = input[inputOffset + i];
        }
        return initV;
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen,
            byte[] output, int outputOffset) throws ShortBufferException {
        byte []res = engineUpdate(input, inputOffset, inputLen);
        int t = res.length;
        if ((output.length - outputOffset) < t) {
            throw new ShortBufferException("Update");
        }
        for (int i = 0; i < t; i++) {
            output[i + outputOffset] = initV[i];
        }
        return t;
    }

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {
        if (resV.length > inputLen) {
            byte[] bb = new byte[inputLen];
            for (int i = 0; i < inputLen; i++) {
                bb[i] = resV[i];
            }
            return bb;
        }
        return resV;
    }

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen,
            byte[] output, int outputOffset) throws ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] res = engineDoFinal(input, inputOffset, inputLen);

        int t = res.length;
        if ((output.length - outputOffset) < t) {
            throw new ShortBufferException("DoFinal");
        }
        for (int i = 0; i < t; i++) {
            output[i + outputOffset] = res[i];
        }
        return t;
    }


    protected int engineUpdate(ByteBuffer input, ByteBuffer output)
    throws ShortBufferException {
        return super.engineUpdate(input, output);
    }
    protected int engineDoFinal(ByteBuffer input, ByteBuffer output)
    throws ShortBufferException, IllegalBlockSizeException,
    BadPaddingException {
        return super.engineDoFinal(input, output);
    }
}
