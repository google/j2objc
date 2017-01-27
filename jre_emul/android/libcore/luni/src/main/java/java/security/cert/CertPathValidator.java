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

package java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import org.apache.harmony.security.fortress.Engine;

/**
 * This class provides the functionality for validating certification paths
 * (certificate chains) establishing a trust chain from a certificate to a trust
 * anchor.
 */
public class CertPathValidator {
    // Store CertPathValidator implementation service name
    private static final String SERVICE = "CertPathValidator";

    // Used to access common engine functionality
    private static final Engine ENGINE = new Engine(SERVICE);

    // Store default property name
    private static final String PROPERTY_NAME = "certpathvalidator.type";

    // Default value of CertPathBuilder type. It returns if certpathbuild.type
    // property is not defined in java.security file
    private static final String DEFAULT_PROPERTY = "PKIX";

    // Store used provider
    private final Provider provider;

    // Store used spi implementation
    private final CertPathValidatorSpi spiImpl;

    // Store used algorithm value
    private final String algorithm;

    /**
     * Creates a new {@code CertPathValidator} instance.
     *
     * @param validatorSpi
     *            the implementation delegate.
     * @param provider
     *            the security provider.
     * @param algorithm
     *            the name of the algorithm.
     */
    protected CertPathValidator(CertPathValidatorSpi validatorSpi,
            Provider provider, String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = validatorSpi;
    }

    /**
     * Returns the certification path algorithm name.
     *
     * @return the certification path algorithm name.
     */
    public final String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the security provider.
     *
     * @return the provider.
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Returns a new certification path validator for the specified algorithm.
     *
     * @param algorithm
     *            the algorithm name.
     * @return a certification path validator for the requested algorithm.
     * @throws NoSuchAlgorithmException
     *             if no installed provider provides the specified algorithm.
     * @throws NullPointerException
     *             if algorithm is {@code null}.
     */
    public static CertPathValidator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Engine.SpiAndProvider sap = ENGINE.getInstance(algorithm, null);
        return new CertPathValidator((CertPathValidatorSpi) sap.spi, sap.provider, algorithm);
    }

    /**
     * Returns a new certification path validator for the specified algorithm
     * from the specified provider.
     *
     * @param algorithm
     *            the algorithm name.
     * @param provider
     *            the security provider name.
     * @return a certification path validator for the requested algorithm.
     * @throws NoSuchAlgorithmException
     *             if the specified security provider cannot provide the
     *             requested algorithm.
     * @throws NoSuchProviderException
     *             if no provider with the specified name can be found.
     * @throws NullPointerException
     *             if algorithm is {@code null}.
     * @throws IllegalArgumentException if {@code provider == null || provider.isEmpty()}
     */
    public static CertPathValidator getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
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
     * Returns a new certification path validator for the specified algorithm
     * from the specified provider. The {@code provider} supplied does not
     * have to be registered.
     *
     * @param algorithm
     *            the algorithm name.
     * @param provider
     *            the security provider name.
     * @return a certification path validator for the requested algorithm.
     * @throws NoSuchAlgorithmException
     *             if the specified provider cannot provide the requested
     *             algorithm.
     * @throws IllegalArgumentException if {@code provider == null}
     * @throws NullPointerException
     *             if algorithm is {@code null}.
     */
    public static CertPathValidator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Object spi = ENGINE.getInstance(algorithm, provider, null);
        return new CertPathValidator((CertPathValidatorSpi) spi, provider, algorithm);
    }

    /**
     * Validates the {@code CertPath} with the algorithm of this {@code
     * CertPathValidator} using the specified algorithm parameters.
     *
     * @param certPath
     *            the certification path to be validated.
     * @param params
     *            the certification path validator algorithm parameters.
     * @return the validation result.
     * @throws CertPathValidatorException
     *             if the validation fails, or the algorithm of the specified
     *             certification path cannot be validated using the algorithm of
     *             this instance.
     * @throws InvalidAlgorithmParameterException
     *             if the specified algorithm parameters cannot be used with
     *             this algorithm.
     * @see CertPathValidatorResult
     */
    public final CertPathValidatorResult validate(CertPath certPath,
            CertPathParameters params) throws CertPathValidatorException,
            InvalidAlgorithmParameterException {
        return spiImpl.engineValidate(certPath, params);
    }

    /**
     * Returns the default {@code CertPathValidator} type from the <i>Security
     * Properties</i>.
     *
     * @return the default {@code CertPathValidator} type from the <i>Security
     *         Properties</i>, or the string {@code "PKIX"} if it cannot be
     *         determined.
     */
    public static final String getDefaultType() {
        String defaultType = Security.getProperty(PROPERTY_NAME);
        return (defaultType != null ? defaultType : DEFAULT_PROPERTY);
    }
}
