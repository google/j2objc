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

package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import org.apache.harmony.security.fortress.Engine;

/**
 * The factory for {@code TrustManager}s based on {@code KeyStore} or provider
 * specific implementation.
 */
public class TrustManagerFactory {
    // Store TrustManager service name
    private static final String SERVICE = "TrustManagerFactory";

    // Used to access common engine functionality
    private static final Engine ENGINE = new Engine(SERVICE);

    // Store default property name
    private static final String PROPERTY_NAME = "ssl.TrustManagerFactory.algorithm";

    // Default value of TrustManagerFactory type.
    private static final String DEFAULT_PROPERTY = "PKIX";

    /**
     * Returns the default algorithm name for the {@code TrustManagerFactory}. The
     * default algorithm name is specified by the security property
     * {@code 'ssl.TrustManagerFactory.algorithm'}.
     *
     * @return the default algorithm name.
     */
    public static final String getDefaultAlgorithm() {
        String algorithm = Security.getProperty(PROPERTY_NAME);
        return (algorithm != null ? algorithm : DEFAULT_PROPERTY);
    }

    /**
     * Creates a new {@code TrustManagerFactory} instance for the specified
     * trust management algorithm.
     *
     * @param algorithm
     *            the name of the requested trust management algorithm.
     * @return a trust manager factory for the requested algorithm.
     * @throws NoSuchAlgorithmException
     *             if no installed provider can provide the requested algorithm.
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null} (instead of
     *             NoSuchAlgorithmException as in 1.4 release)
     */
    public static final TrustManagerFactory getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Engine.SpiAndProvider sap = ENGINE.getInstance(algorithm, null);
        return new TrustManagerFactory((TrustManagerFactorySpi) sap.spi, sap.provider, algorithm);
    }

    /**
     * Creates a new {@code TrustManagerFactory} instance for the specified
     * trust management algorithm from the specified provider.
     *
     * @param algorithm
     *            the name of the requested trust management algorithm name.
     * @param provider
     *            the name of the provider that provides the requested
     *            algorithm.
     * @return a trust manager factory for the requested algorithm.
     * @throws NoSuchAlgorithmException
     *             if the specified provider cannot provide the requested
     *             algorithm.
     * @throws NoSuchProviderException
     *             if the specified provider does not exist.
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null} (instead of
     *             NoSuchAlgorithmException as in 1.4 release)
     */
    public static final TrustManagerFactory getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException("Provider is null or empty");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, impProvider);
    }

    /**
     * Creates a new {@code TrustManagerFactory} instance for the specified
     * trust management algorithm from the specified provider.
     *
     * @param algorithm
     *            the name of the requested key management algorithm name.
     * @param provider
     *            the provider that provides the requested algorithm.
     * @return a key manager factory for the requested algorithm.
     * @throws NoSuchAlgorithmException
     *             if the specified provider cannot provide the requested
     *             algorithm.
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null} (instead of
     *             NoSuchAlgorithmException as in 1.4 release)
     */
    public static final TrustManagerFactory getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Object spi = ENGINE.getInstance(algorithm, provider, null);
        return new TrustManagerFactory((TrustManagerFactorySpi) spi, provider, algorithm);
    }

    // Store used provider
    private final Provider provider;

    // Store used TrustManagerFactorySpi implementation
    private final TrustManagerFactorySpi spiImpl;

    // Store used algorithm
    private final String algorithm;

    /**
     * Creates a new {@code TrustManagerFactory} instance.
     *
     * @param factorySpi
     *            the implementation delegate.
     * @param provider
     *            the provider
     * @param algorithm
     *            the algorithm name.
     */
    protected TrustManagerFactory(TrustManagerFactorySpi factorySpi, Provider provider,
            String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = factorySpi;
    }

    /**
     * Returns the name of this {@code TrustManagerFactory} algorithm
     * implementation.
     *
     * @return the name of this {@code TrustManagerFactory} algorithm
     *         implementation.
     */
    public final String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the provider for this {@code TrustManagerFactory} instance.
     *
     * @return the provider for this {@code TrustManagerFactory} instance.
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Initializes this factory instance with the specified keystore as source
     * of certificate authorities and trust material.
     *
     * @param ks
     *            the keystore or {@code null}.
     * @throws KeyStoreException
     *             if the initialization fails.
     */
    public final void init(KeyStore ks) throws KeyStoreException {
        spiImpl.engineInit(ks);
    }

    /**
     * Initializes this factory instance with the specified provider-specific
     * parameters for a source of trust material.
     *
     * @param spec
     *            the provider-specific parameters.
     * @throws InvalidAlgorithmParameterException
     *             if the initialization fails.
     */
    public final void init(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(spec);
    }

    /**
     * Returns the list of {@code TrustManager}s with one entry for each type
     * of trust material.
     *
     * @return the list of {@code TrustManager}s
     */
    public final TrustManager[] getTrustManagers() {
        return spiImpl.engineGetTrustManagers();
    }

}
