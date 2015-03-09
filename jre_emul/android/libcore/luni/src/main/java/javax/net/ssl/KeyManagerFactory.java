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
import java.security.UnrecoverableKeyException;
import org.apache.harmony.security.fortress.Engine;

/**
 * The public API for {@code KeyManagerFactory} implementations.
 */
public class KeyManagerFactory {
    // Store KeyManagerFactory service name
    private static final String SERVICE = "KeyManagerFactory";

    // Used to access common engine functionality
    private static final Engine ENGINE = new Engine(SERVICE);

    // Store default property name
    private static final String PROPERTY_NAME = "ssl.KeyManagerFactory.algorithm";

    // Default value of KeyManagerFactory type.
    private static final String DEFAULT_PROPERTY = "PKIX";

    /**
     * Returns the default key manager factory algorithm name.
     * <p>
     * The default algorithm name is specified by the security property:
     * {@code 'ssl.KeyManagerFactory.algorithm'}.
     *
     * @return the default algorithm name.
     */
    public static final String getDefaultAlgorithm() {
        String algorithm = Security.getProperty(PROPERTY_NAME);
        return (algorithm != null ? algorithm : DEFAULT_PROPERTY);
    }

    /**
     * Creates a new {@code KeyManagerFactory} instance for the specified key
     * management algorithm.
     *
     * @param algorithm
     *            the name of the requested key management algorithm.
     * @return a key manager factory for the requested algorithm.
     * @throws NoSuchAlgorithmException
     *             if no installed provider can provide the requested algorithm.
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null} (instead of
     *             NoSuchAlgorithmException as in 1.4 release)
     */
    public static final KeyManagerFactory getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Engine.SpiAndProvider sap = ENGINE.getInstance(algorithm, null);
        return new KeyManagerFactory((KeyManagerFactorySpi) sap.spi, sap.provider, algorithm);
    }

    /**
     * Creates a new {@code KeyManagerFactory} instance for the specified key
     * management algorithm from the specified provider.
     *
     * @param algorithm
     *            the name of the requested key management algorithm name.
     * @param provider
     *            the name of the provider that provides the requested
     *            algorithm.
     * @return a key manager factory for the requested algorithm.
     * @throws NoSuchAlgorithmException
     *             if the specified provider cannot provide the requested
     *             algorithm.
     * @throws NoSuchProviderException
     *             if the specified provider does not exist.
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null} (instead of
     *             NoSuchAlgorithmException as in 1.4 release)
     */
    public static final KeyManagerFactory getInstance(String algorithm, String provider)
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
     * Creates a new {@code KeyManagerFactory} instance for the specified key
     * management algorithm from the specified provider.
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
    public static final KeyManagerFactory getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Object spi = ENGINE.getInstance(algorithm, provider, null);
        return new KeyManagerFactory((KeyManagerFactorySpi) spi, provider, algorithm);
    }

    // Store used provider
    private final Provider provider;

    // Store used KeyManagerFactorySpi implementation
    private final KeyManagerFactorySpi spiImpl;

    // Store used algorithm
    private final String algorithm;

    /**
     * Creates a new {@code KeyManagerFactory}.
     *
     * @param factorySpi
     *            the implementation delegate.
     * @param provider
     *            the provider.
     * @param algorithm
     *            the key management algorithm name.
     */
    protected KeyManagerFactory(KeyManagerFactorySpi factorySpi, Provider provider,
                                String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = factorySpi;
    }

    /**
     * Returns the name of the key management algorithm.
     *
     * @return the name of the key management algorithm.
     */
    public final String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the provider for this {@code KeyManagerFactory} instance.
     *
     * @return the provider for this {@code KeyManagerFactory} instance.
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Initializes this instance with the specified key store and password.
     *
     * @param ks
     *            the key store or {@code null} to use the default key store.
     * @param password
     *            the password for the specified key store or {@code null} if no
     *            key store is provided.
     * @throws KeyStoreException
     *             if initializing this key manager factory fails.
     * @throws NoSuchAlgorithmException
     *             if a required algorithm is not available.
     * @throws UnrecoverableKeyException
     *             if a key cannot be recovered.
     */
    public final void init(KeyStore ks, char[] password) throws KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException {
        spiImpl.engineInit(ks, password);
    }

    /**
     * Initializes this instance with the specified factory parameters.
     *
     * @param spec
     *            the factory parameters.
     * @throws InvalidAlgorithmParameterException
     *             if an error occurs.
     */
    public final void init(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(spec);
    }

    /**
     * Returns a list of key managers, one instance for each type of key in the
     * key store.
     *
     * @return a list of key managers.
     */
    public final KeyManager[] getKeyManagers() {
        return spiImpl.engineGetKeyManagers();
    }
}
