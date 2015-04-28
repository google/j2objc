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

package javax.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

/**
 * The <i>Service-Provider Interface</i> (<b>SPI</b>) definition for the {@code
 * Mac} class.
 *
 * @see Mac
 */
public abstract class MacSpi {

    /**
     * Creates a new {@code MacSpi} instance.
     */
    public MacSpi() {
    }

    /**
     * Returns the length of this MAC (in bytes).
     *
     * @return the length of this MAC (in bytes).
     */
    protected abstract int engineGetMacLength();

    /**
     * Initializes this {@code MacSpi} instance with the specified key and
     * algorithm parameters.
     *
     * @param key
     *            the key to initialize this algorithm.
     * @param params
     *            the parameters for this algorithm.
     * @throws InvalidKeyException
     *             if the specified key cannot be used to initialize this
     *             algorithm, or it is {@code null}.
     * @throws InvalidAlgorithmParameterException
     *             if the specified parameters cannot be used to initialize this
     *             algorithm.
     */
    protected abstract void engineInit(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * Updates this {@code MacSpi} instance with the specified byte.
     *
     * @param input
     *            the byte.
     */
    protected abstract void engineUpdate(byte input);

    /**
     * Updates this {@code MacSpi} instance with the data from the specified
     * buffer {@code input} from the specified {@code offset} and length {@code
     * len}.
     *
     * @param input
     *            the buffer.
     * @param offset
     *            the offset in the buffer.
     * @param len
     *            the length of the data in the buffer.
     */
    protected abstract void engineUpdate(byte[] input, int offset, int len);

    /**
     * Updates this {@code MacSpi} instance with the data from the specified
     * buffer, starting at {@link ByteBuffer#position()}, including the next
     * {@link ByteBuffer#remaining()} bytes.
     *
     * @param input
     *            the buffer.
     */
    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        byte[] bInput;
        if (input.hasArray()) {
            bInput = input.array();
            int offset = input.arrayOffset();
            int position = input.position();
            int limit = input.limit();
            engineUpdate(bInput, offset + position, limit - position);
            input.position(limit);
        } else {
            bInput = new byte[input.limit() - input.position()];
            input.get(bInput);
            engineUpdate(bInput, 0, bInput.length);
        }
    }

    /**
     * Computes the digest of this MAC based on the data previously specified in
     * {@link #engineUpdate} calls.
     * <p>
     * This {@code MacSpi} instance is reverted to its initial state and
     * can be used to start the next MAC computation with the same parameters or
     * initialized with different parameters.
     *
     * @return the generated digest.
     */
    protected abstract byte[] engineDoFinal();

    /**
     * Resets this {@code MacSpi} instance to its initial state.
     * <p>
     * This {@code MacSpi} instance is reverted to its initial state and can be
     * used to start the next MAC computation with the same parameters or
     * initialized with different parameters.
     */
    protected abstract void engineReset();

    /**
     * Clones this {@code MacSpi} instance.
     *
     * @return the cloned instance.
     * @throws CloneNotSupportedException
     *             if cloning is not supported.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
