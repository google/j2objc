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

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import org.apache.harmony.security.fortress.Engine;


/**
 * {@code AlgorithmParameters} is an engine class which provides algorithm
 * parameters.
 */
public class AlgorithmParameters {
    /**
     * The service name.
     */
    private static final String SEVICE = "AlgorithmParameters";

    /**
     * Used to access common engine functionality.
     */
    private static final Engine ENGINE = new Engine(SEVICE);

    /**
     * The security provider.
     */
    private final Provider provider;

    /**
     * The SPI implementation.
     */
    private final AlgorithmParametersSpi spiImpl;

    /**
     * The security algorithm.
     */
    private final String algorithm;

    /**
     * The initialization state.
     */
    private boolean initialized;

    /**
     * Constructs a new instance of {@code AlgorithmParameters} with the given
     * arguments.
     *
     * @param algPramSpi
     *            the concrete implementation.
     * @param provider
     *            the security provider.
     * @param algorithm
     *            the name of the algorithm.
     */
    protected AlgorithmParameters(AlgorithmParametersSpi algPramSpi,
            Provider provider, String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = algPramSpi;
    }

    /**
     * Returns a new instance of {@code AlgorithmParameters} for the specified
     * algorithm.
     *
     * @param algorithm
     *            the name of the algorithm to use.
     * @return a new instance of {@code AlgorithmParameters} for the specified
     *         algorithm.
     * @throws NoSuchAlgorithmException
     *             if the specified algorithm is not available.
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}.
     */
    public static AlgorithmParameters getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Engine.SpiAndProvider sap = ENGINE.getInstance(algorithm, null);
        return new AlgorithmParameters((AlgorithmParametersSpi) sap.spi, sap.provider, algorithm);
    }

    /**
     * Returns a new instance of {@code AlgorithmParameters} from the specified
     * provider for the specified algorithm.
     *
     * @param algorithm
     *            the name of the algorithm to use.
     * @param provider
     *            name of the provider of the {@code AlgorithmParameters}.
     * @return a new instance of {@code AlgorithmParameters} for the specified
     *         algorithm.
     * @throws NoSuchAlgorithmException
     *             if the specified algorithm is not available.
     * @throws NoSuchProviderException
     *             if the specified provider is not available.
     * @throws IllegalArgumentException if {@code provider == null || provider.isEmpty()}
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}.
     */
    public static AlgorithmParameters getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("provider == null || provider.isEmpty()");
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, p);
    }

    /**
     * Returns a new instance of {@code AlgorithmParameters} from the specified
     * provider for the specified algorithm. The {@code provider} supplied does
     * not have to be registered.
     *
     * @param algorithm
     *            the name of the algorithm to use.
     * @param provider
     *            the provider of the {@code AlgorithmParameters}.
     * @return a new instance of {@code AlgorithmParameters} for the specified
     *         algorithm.
     * @throws NoSuchAlgorithmException
     *             if the specified algorithm is not available.
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}.
     * @throws IllegalArgumentException if {@code provider == null}
     */
    public static AlgorithmParameters getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Object spi = ENGINE.getInstance(algorithm, provider, null);
        return new AlgorithmParameters((AlgorithmParametersSpi) spi, provider, algorithm);
    }

    /**
     * Returns the provider associated with this {@code AlgorithmParameters}.
     *
     * @return the provider associated with this {@code AlgorithmParameters}.
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Returns the name of the algorithm.
     *
     * @return the name of the algorithm.
     */
    public final String getAlgorithm() {
        return algorithm;
    }

    /**
     * Initializes this {@code AlgorithmParameters} with the specified {@code
     * AlgorithmParameterSpec}.
     *
     * @param paramSpec
     *            the parameter specification.
     * @throws InvalidParameterSpecException
     *             if this {@code AlgorithmParameters} has already been
     *             initialized or the given {@code paramSpec} is not appropriate
     *             for initializing this {@code AlgorithmParameters}.
     */
    public final void init(AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException {
        if (initialized) {
            throw new InvalidParameterSpecException("Parameter has already been initialized");
        }
        spiImpl.engineInit(paramSpec);
        initialized = true;
    }

    /**
     * Initializes this {@code AlgorithmParameters} with the specified {@code
     * byte[]} using the default decoding format for parameters. The default
     * encoding format is ASN.1.
     *
     * @param params
     *            the encoded parameters.
     * @throws IOException
     *             if this {@code AlgorithmParameters} has already been
     *             initialized, or the parameter could not be encoded.
     */
    public final void init(byte[] params) throws IOException {
        if (initialized) {
            throw new IOException("Parameter has already been initialized");
        }
        spiImpl.engineInit(params);
        initialized = true;
    }

    /**
     * Initializes this {@code AlgorithmParameters} with the specified {@code
     * byte[]} using the specified decoding format.
     *
     * @param params
     *            the encoded parameters.
     * @param format
     *            the name of the decoding format.
     * @throws IOException
     *             if this {@code AlgorithmParameters} has already been
     *             initialized, or the parameter could not be encoded.
     */
    public final void init(byte[] params, String format) throws IOException {
        if (initialized) {
            throw new IOException("Parameter has already been initialized");
        }
        spiImpl.engineInit(params, format);
        initialized = true;
    }

    /**
     * Returns the {@code AlgorithmParameterSpec} for this {@code
     * AlgorithmParameters}.
     *
     * @param paramSpec
     *            the type of the parameter specification in which this
     *            parameters should be converted.
     * @return the {@code AlgorithmParameterSpec} for this {@code
     *         AlgorithmParameters}.
     * @throws InvalidParameterSpecException
     *             if this {@code AlgorithmParameters} has already been
     *             initialized, or if this parameters could not be converted to
     *             the specified class.
     */
    public final <T extends AlgorithmParameterSpec> T getParameterSpec(Class<T> paramSpec)
            throws InvalidParameterSpecException {
        if (!initialized) {
            throw new InvalidParameterSpecException("Parameter has not been initialized");
        }
        return spiImpl.engineGetParameterSpec(paramSpec);
    }

    /**
     * Returns this {@code AlgorithmParameters} in their default encoding
     * format. The default encoding format is ASN.1.
     *
     * @return the encoded parameters.
     * @throws IOException
     *             if this {@code AlgorithmParameters} has already been
     *             initialized, or if this parameters could not be encoded.
     */
    public final byte[] getEncoded() throws IOException {
        if (!initialized) {
            throw new IOException("Parameter has not been initialized");
        }
        return spiImpl.engineGetEncoded();
    }

    /**
     * Returns this {@code AlgorithmParameters} in the specified encoding
     * format.
     *
     * @param format
     *            the name of the encoding format.
     * @return the encoded parameters.
     * @throws IOException
     *             if this {@code AlgorithmParameters} has already been
     *             initialized, or if this parameters could not be encoded.
     */
    public final byte[] getEncoded(String format) throws IOException {
        if (!initialized) {
            throw new IOException("Parameter has not been initialized");
        }
        return spiImpl.engineGetEncoded(format);
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * {@code AlgorithmParameters}.
     *
     * @return a printable representation for this {@code AlgorithmParameters}.
     */
    @Override
    public final String toString() {
        if (!initialized) {
            return null;
        }
        return spiImpl.engineToString();
    }
}
