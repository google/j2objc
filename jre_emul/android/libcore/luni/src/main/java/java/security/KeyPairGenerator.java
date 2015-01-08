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
import org.apache.harmony.security.fortress.Engine;


/**
 * {@code KeyPairGenerator} is an engine class which is capable of generating a
 * private key and its related public key utilizing the algorithm it was
 * initialized with.
 *
 * @see KeyPairGeneratorSpi
 */
public abstract class KeyPairGenerator extends KeyPairGeneratorSpi {

    // Store KeyPairGenerator SERVICE name
    private static final String SERVICE = "KeyPairGenerator";

    // Used to access common engine functionality
    private static final Engine ENGINE = new Engine(SERVICE);

    // Store SecureRandom
    private static final SecureRandom RANDOM = new SecureRandom();

    // Store used provider
    private Provider provider;

    // Store used algorithm
    private String algorithm;

    /**
     * Constructs a new instance of {@code KeyPairGenerator} with the name of
     * the algorithm to use.
     *
     * @param algorithm
     *            the name of algorithm to use
     */
    protected KeyPairGenerator(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns the name of the algorithm of this {@code KeyPairGenerator}.
     *
     * @return the name of the algorithm of this {@code KeyPairGenerator}
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns a new instance of {@code KeyPairGenerator} that utilizes the
     * specified algorithm.
     *
     * @param algorithm
     *            the name of the algorithm to use
     * @return a new instance of {@code KeyPairGenerator} that utilizes the
     *         specified algorithm
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}
     */
    public static KeyPairGenerator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Engine.SpiAndProvider sap = ENGINE.getInstance(algorithm, null);
        Object spi = sap.spi;
        Provider provider = sap.provider;
        if (spi instanceof KeyPairGenerator) {
            KeyPairGenerator result = (KeyPairGenerator) spi;
            result.algorithm = algorithm;
            result.provider = provider;
            return result;
        }
        return new KeyPairGeneratorImpl((KeyPairGeneratorSpi) spi, provider, algorithm);
    }

    /**
     * Returns a new instance of {@code KeyPairGenerator} that utilizes the
     * specified algorithm from the specified provider.
     *
     * @param algorithm
     *            the name of the algorithm to use
     * @param provider
     *            the name of the provider
     * @return a new instance of {@code KeyPairGenerator} that utilizes the
     *         specified algorithm from the specified provider
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws NoSuchProviderException if the specified provider is not available
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}
     * @throws IllegalArgumentException if {@code provider == null || provider.isEmpty()}
     */
    public static KeyPairGenerator getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, impProvider);
    }

    /**
     * Returns a new instance of {@code KeyPairGenerator} that utilizes the
     * specified algorithm from the specified provider. The {@code provider}
     * supplied does not have to be registered.
     *
     * @param algorithm
     *            the name of the algorithm to use
     * @param provider
     *            the provider
     * @return a new instance of {@code KeyPairGenerator} that utilizes the
     *         specified algorithm from the specified provider
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}
     * @throws IllegalArgumentException if {@code provider == null}
     */
    public static KeyPairGenerator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Object spi = ENGINE.getInstance(algorithm, provider, null);
        if (spi instanceof KeyPairGenerator) {
            KeyPairGenerator result = (KeyPairGenerator) spi;
            result.algorithm = algorithm;
            result.provider = provider;
            return result;
        }
        return new KeyPairGeneratorImpl((KeyPairGeneratorSpi) spi, provider, algorithm);
    }

    /**
     * Returns the provider associated with this {@code KeyPairGenerator}.
     *
     * @return the provider associated with this {@code KeyPairGenerator}
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Initializes this {@code KeyPairGenerator} with the given key size. The
     * default parameter set and a default {@code SecureRandom} instance will be
     * used.
     *
     * @param keysize
     *            the size of the key (number of bits)
     */
    public void initialize(int keysize) {
        initialize(keysize, RANDOM);
    }

    /**
     * Initializes this {@code KeyPairGenerator} with the given {@code
     * AlgorithmParameterSpec}. A default {@code SecureRandom} instance will be
     * used.
     *
     * @param param
     *            the parameters to use
     * @throws InvalidAlgorithmParameterException
     *             if the specified parameters are not supported
     */
    public void initialize(AlgorithmParameterSpec param)
            throws InvalidAlgorithmParameterException {
        initialize(param, RANDOM);
    }

    /**
     * Computes and returns a new unique {@code KeyPair} each time this method
     * is called.
     * <p>
     * This does exactly the same as {@link #generateKeyPair()}.
     *
     * @return a new unique {@code KeyPair} each time this method is called
     */
    public final KeyPair genKeyPair() {
        return generateKeyPair();
    }

    /**
     * Computes and returns a new unique {@code KeyPair} each time this method
     * is called.
     * <p>
     * This does exactly the same as {@link #genKeyPair()}.
     *
     * @return a new unique {@code KeyPair} each time this method is called
     */
    @Override
    public KeyPair generateKeyPair() {
        return null;
    }

    /**
     * Initializes this {@code KeyPairGenerator} with the given key size and the
     * given {@code SecureRandom}. The default parameter set will be used.
     *
     * @param keysize
     *            the key size
     * @param random
     *            the source of randomness
     */
    @Override
    public void initialize(int keysize, SecureRandom random) {
    }

    /**
     * Initializes this {@code KeyPairGenerator} with the given {@code
     * AlgorithmParameterSpec} and the given {@code SecureRandom}.
     *
     * @param param
     *            the parameters to use
     * @param random
     *            the source of randomness
     * @throws InvalidAlgorithmParameterException
     *             if the specified parameters are not supported
     */
    @Override
    public void initialize(AlgorithmParameterSpec param, SecureRandom random)
            throws InvalidAlgorithmParameterException {
    }

    /**
     *
     * Internal class: KeyPairGenerator implementation
     *
     */
    private static class KeyPairGeneratorImpl extends KeyPairGenerator {
        // Save KeyPairGeneratorSpi
        private KeyPairGeneratorSpi spiImpl;

        private KeyPairGeneratorImpl(KeyPairGeneratorSpi keyPairGeneratorSpi,
                Provider provider, String algorithm) {
            super(algorithm);
            super.provider = provider;
            spiImpl = keyPairGeneratorSpi;
        }

        // implementation of initialize(int keysize, SecureRandom random)
        // using corresponding spi initialize() method
        @Override
        public void initialize(int keysize, SecureRandom random) {
            spiImpl.initialize(keysize, random);
        }

        // implementation of generateKeyPair()
        // using corresponding spi generateKeyPair() method
        @Override
        public KeyPair generateKeyPair() {
            return spiImpl.generateKeyPair();
        }

        // implementation of initialize(int keysize, SecureRandom random)
        // using corresponding spi initialize() method
        @Override
        public void initialize(AlgorithmParameterSpec param, SecureRandom random)
                throws InvalidAlgorithmParameterException {
            spiImpl.initialize(param, random);
        }

    }

}
