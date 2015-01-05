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
 * {@code AlgorithmParameterGeneratorSpi} is the Service Provider Interface
 * (SPI) definition for {@code AlgorithmParameterGenerator}.
 *
 * @see AlgorithmParameterGenerator
 */
public abstract class AlgorithmParameterGeneratorSpi {

    /**
     * Constructs a new instance of {@code AlgorithmParameterGeneratorSpi} .
     */
    public AlgorithmParameterGeneratorSpi() {
    }

    /**
     * Initializes this {@code AlgorithmParameterGeneratorSpi} with the given
     * size and the given {@code SecureRandom}. The default parameter set
     * will be used.
     *
     * @param size
     *            the size (in number of bits).
     * @param random
     *            the source of randomness.
     */
    protected abstract void engineInit(int size, SecureRandom random);

    /**
     * Initializes this {@code AlgorithmParameterGeneratorSpi} with the given
     * {@code AlgorithmParameterSpec} and the given {@code SecureRandom}.
     *
     * @param genParamSpec
     *            the parameters to use.
     * @param random
     *            the source of randomness.
     * @throws InvalidAlgorithmParameterException
     *             if the specified parameters are not supported.
     */
    protected abstract void engineInit(AlgorithmParameterSpec genParamSpec,
            SecureRandom random) throws InvalidAlgorithmParameterException;

    /**
     * Computes and returns {@code AlgorithmParameters} for this generator's
     * algorithm.
     *
     * @return {@code AlgorithmParameters} for this generator's algorithm.
     */
    protected abstract AlgorithmParameters engineGenerateParameters();
}
