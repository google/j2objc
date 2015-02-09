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

import java.security.spec.AlgorithmParameterSpec;

/**
 * {@code KeyPairGeneratorSpi} is the Service Provider Interface (SPI)
 * definition for {@link KeyPairGenerator}.
 *
 * @see KeyPairGenerator
 */
public abstract class KeyPairGeneratorSpi {
    /**
     * Constructs a new instance of {@code KeyPairGeneratorSpi}.
     */
    public KeyPairGeneratorSpi() {
    }

    /**
     * Computes and returns a new unique {@code KeyPair} each time this method
     * is called.
     *
     * @return a new unique {@code KeyPair} each time this method is called.
     */
    public abstract KeyPair generateKeyPair();

    /**
     * Initializes this {@code KeyPairGeneratorSpi} with the given key size and
     * the given {@code SecureRandom}. The default parameter set will be used.
     *
     * @param keysize
     *            the key size (number of bits).
     * @param random
     *            the source of randomness.
     */
    public abstract void initialize(int keysize, SecureRandom random);

    /**
     * Initializes this {@code KeyPairGeneratorSpi} with the given {@code
     * AlgorithmParameterSpec} and the given {@code SecureRandom}.
     *
     * @param params
     *            the parameters to use.
     * @param random
     *            the source of randomness.
     * @throws InvalidAlgorithmParameterException
     *             if the specified parameters are not supported.
     */
    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException();
    }
}
