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

package java.security;

import java.nio.ByteBuffer;
import java.security.spec.AlgorithmParameterSpec;

/**
 * {@code SignatureSpi} is the <i>Service Provider Interface</i> (<b>SPI</b>)
 * definition for {@link Signature}.
 *
 * @see Signature
 */
public abstract class SignatureSpi {

    /**
     * Implementation specific source of randomness.
     */
    protected SecureRandom appRandom;

    /**
     * Initializes this {@code SignatureSpi} instance for signature
     * verification, using the public key of the identity whose signature is
     * going to be verified.
     *
     * @param publicKey
     *            the public key.
     * @throws InvalidKeyException
     *             if {@code publicKey} is not valid.
     */
    protected abstract void engineInitVerify(PublicKey publicKey)
            throws InvalidKeyException;

    /**
     * Initializes this {@code SignatureSpi} instance for signing, using the
     * private key of the identity whose signature is going to be generated.
     *
     * @param privateKey
     *            the private key.
     * @throws InvalidKeyException
     *             if {@code privateKey} is not valid.
     */
    protected abstract void engineInitSign(PrivateKey privateKey)
            throws InvalidKeyException;

    /**
     * Initializes this {@code SignatureSpi} instance for signing, using the
     * private key of the identity whose signature is going to be generated and
     * the specified source of randomness.
     *
     * @param privateKey
     *            the private key.
     * @param random
     *            the {@code SecureRandom} to use.
     * @throws InvalidKeyException
     *             if {@code privateKey} is not valid.
     */
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {
        appRandom = random;
        engineInitSign(privateKey);
    }

    /**
     * Updates the data to be verified or to be signed, using the specified
     * {@code byte}.
     *
     * @param b
     *            the byte to update with.
     * @throws SignatureException
     *             if this {@code SignatureSpi} instance is not initialized
     *             properly.
     */
    protected abstract void engineUpdate(byte b) throws SignatureException;

    /**
     * Updates the data to be verified or to be signed, using the given {@code
     * byte[]}, starting form the specified index for the specified length.
     *
     * @param b
     *            the byte array to update with.
     * @param off
     *            the start index in {@code b} of the data.
     * @param len
     *            the number of bytes to use.
     * @throws SignatureException
     *             if this {@code SignatureSpi} instance is not initialized
     *             properly.
     */
    protected abstract void engineUpdate(byte[] b, int off, int len)
            throws SignatureException;

    /**
     * Updates the data to be verified or to be signed, using the specified
     * {@code ByteBuffer}.
     *
     * @param input
     *            the {@code ByteBuffer} to update with.
     * @throws RuntimeException
     *             since {@code SignatureException} is not specified for this
     *             method it throws a {@code RuntimeException} if underlying
     *             {@link #engineUpdate(byte[], int, int)} throws {@code
     *             SignatureException}.
     */
    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        byte[] tmp;
        if (input.hasArray()) {
            tmp = input.array();
            int offset = input.arrayOffset();
            int position = input.position();
            int limit = input.limit();
            try {
                engineUpdate(tmp, offset + position, limit - position);
            } catch (SignatureException e) {
                throw new RuntimeException(e); //Wrap SignatureException
            }
            input.position(limit);
        } else {
            tmp = new byte[input.limit() - input.position()];
            input.get(tmp);
            try {
                engineUpdate(tmp, 0, tmp.length);
            } catch (SignatureException e) {
                throw new RuntimeException(e); //Wrap SignatureException
            }
        }
    }

    /**
     * Generates and returns the signature of all updated data.
     * <p>
     * This {@code SignatureSpi} instance is reset to the state of its last
     * initialization for signing and thus can be used for another signature
     * from the same identity.
     *
     * @return the signature of all updated data.
     * @throws SignatureException
     *             if this {@code SignatureSpi} instance is not initialized
     *             properly.
     */
    protected abstract byte[] engineSign() throws SignatureException;

    /**
     * Generates and stores the signature of all updated data in the provided
     * {@code byte[]} at the specified position with the specified length.
     * <p>
     * This {@code SignatureSpi} instance is reset to the state of its last
     * initialization for signing and thus can be used for another signature
     * from the same identity.
     *
     * @param outbuf
     *            the buffer to store the signature.
     * @param offset
     *            the index of the first byte in {@code outbuf} to store.
     * @param len
     *            the number of bytes allocated for the signature.
     * @return the number of bytes stored in {@code outbuf}.
     * @throws SignatureException
     *             if this {@code SignatureSpi} instance is not initialized
     *             properly.
     * @throws IllegalArgumentException
     *             if {@code offset} or {@code len} are not valid in respect to
     *             {@code outbuf}.
     */
    protected int engineSign(byte[] outbuf, int offset, int len) throws SignatureException {
        byte[] tmp = engineSign();
        if (tmp == null) {
            return 0;
        }
        if (len < tmp.length) {
            throw new SignatureException("The value of len parameter is less than the actual signature length");
        }
        if (offset < 0) {
            throw new SignatureException("offset < 0");
        }
        if (offset + len > outbuf.length) {
            throw new SignatureException("offset + len > outbuf.length");
        }
        System.arraycopy(tmp, 0, outbuf, offset, tmp.length);
        return tmp.length;
    }

    /**
     * Indicates whether the given {@code sigBytes} can be verified using the
     * public key or a certificate of the signer.
     * <p>
     * This {@code SignatureSpi} instance is reset to the state of its last
     * initialization for verifying and thus can be used to verify another
     * signature of the same signer.
     *
     * @param sigBytes
     *            the signature to verify.
     * @return {@code true} if the signature was verified, {@code false}
     *         otherwise.
     * @throws SignatureException
     *             if this {@code SignatureSpi} instance is not initialized
     *             properly.
     */
    protected abstract boolean engineVerify(byte[] sigBytes)
            throws SignatureException;

    /**
     * Indicates whether the given {@code sigBytes} starting at index {@code
     * offset} with {@code length} bytes can be verified using the public key or
     * a certificate of the signer.
     * <p>
     * This {@code SignatureSpi} instance is reset to the state of its last
     * initialization for verifying and thus can be used to verify another
     * signature of the same signer.
     *
     * @param sigBytes
     *            the {@code byte[]} containing the signature to verify.
     * @param offset
     *            the start index in {@code sigBytes} of the signature
     * @param length
     *            the number of bytes allocated for the signature.
     * @return {@code true} if the signature was verified, {@code false}
     *         otherwise.
     * @throws SignatureException
     *             if this {@code SignatureSpi} instance is not initialized
     *             properly.
     * @throws IllegalArgumentException
     *             if {@code offset} or {@code length} are not valid in respect
     *             to {@code sigBytes}.
     */
    protected boolean engineVerify(byte[] sigBytes, int offset, int length)
            throws SignatureException {
        byte[] tmp = new byte[length];
        System.arraycopy(sigBytes, offset, tmp, 0, length);
        return engineVerify(tmp);
    }

    /**
     * Sets the specified parameter to the given value.
     *
     * @param param
     *            the name of the parameter.
     * @param value
     *            the parameter value.
     * @throws InvalidParameterException
     *             if the parameter is invalid, already set or is not allowed to
     *             be changed.
     * @deprecated Use {@link #engineSetParameter(AlgorithmParameterSpec)}
     */
    @Deprecated
    protected abstract void engineSetParameter(String param, Object value)
            throws InvalidParameterException;

    /**
     * Sets the specified {@code AlgorithmParameterSpec}.
     *
     * @param params
     *            the parameter to set.
     * @throws InvalidAlgorithmParameterException
     *             if the parameter is invalid, already set or is not allowed to
     *             be changed.
     */
    protected void engineSetParameter(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@code AlgorithmParameters} of this {@link SignatureSpi}
     * instance.
     *
     * @return the {@code AlgorithmParameters} of this {@link SignatureSpi}
     *         instance, maybe {@code null}.
     */
    protected AlgorithmParameters engineGetParameters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the value of the parameter with the specified name.
     *
     * @param param
     *            the name of the requested parameter value.
     * @return the value of the parameter with the specified name, maybe {@code
     *         null}.
     * @throws InvalidParameterException
     *             if {@code param} is not a valid parameter for this {@code
     *             SignatureSpi} or an other error occurs.
     * @deprecated There is no generally accepted parameter naming convention.
     */
    @Deprecated
    protected abstract Object engineGetParameter(String param)
            throws InvalidParameterException;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
