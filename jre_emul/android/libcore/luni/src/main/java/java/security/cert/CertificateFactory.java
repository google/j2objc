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

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.harmony.security.fortress.Engine;


/**
 * This class implements the functionality of a certificate factory algorithm,
 * relying on parsing a stream of bytes.
 * <p>
 * It defines methods for parsing certificate chains (certificate paths) and
 * <i>Certificate Revocation Lists</i> (CRLs).
 */
public class CertificateFactory {

    // Store CertificateFactory service name
    private static final String SERVICE = "CertificateFactory";

    // Used to access common engine functionality
    private static final Engine ENGINE = new Engine(SERVICE);

    // Store used provider
    private final Provider provider;

    // Store used CertificateFactorySpi implementation
    private final CertificateFactorySpi spiImpl;

    // Store used type
    private final String type;

    /**
     * Creates a new {@code CertificateFactory} instance.
     *
     * @param certFacSpi
     *            the implementation delegate.
     * @param provider
     *            the associated provider.
     * @param type
     *            the certificate type.
     */
    protected CertificateFactory(CertificateFactorySpi certFacSpi,
            Provider provider, String type) {
        this.provider = provider;
        this.type = type;
        this.spiImpl = certFacSpi;
    }

    /**
     * Creates a new {@code CertificateFactory} instance that provides the
     * requested certificate type.
     *
     * @param type
     *            the certificate type.
     * @return the new {@code CertificateFactory} instance.
     * @throws CertificateException
     *             if the specified certificate type is not available at any
     *             installed provider.
     * @throws NullPointerException if {@code type == null}
     */
    public static final CertificateFactory getInstance(String type)
            throws CertificateException {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        try {
            Engine.SpiAndProvider sap = ENGINE.getInstance(type, null);
            return new CertificateFactory((CertificateFactorySpi) sap.spi, sap.provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(e);
        }
    }

    /**
     * Creates a new {@code CertificateFactory} instance from the specified
     * provider that provides the requested certificate type.
     *
     * @param type
     *            the certificate type.
     * @param provider
     *            the name of the provider providing certificates of the
     *            specified type.
     * @return the new {@code CertificateFactory} instance.
     * @throws CertificateException
     *             if the specified certificate type is not available by the
     *             specified provider.
     * @throws NoSuchProviderException
     *             if no provider with the specified name can be found.
     * @throws IllegalArgumentException if {@code provider == null || provider.isEmpty()}
     * @throws NullPointerException
     *             it {@code type} is {@code null}.
     */
    public static final CertificateFactory getInstance(String type,
            String provider) throws CertificateException,
            NoSuchProviderException {
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("provider == null || provider.isEmpty()");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(type, impProvider);
    }

    /**
     * Creates a new {@code CertificateFactory} instance from the specified
     * provider that provides the requested certificate type.
     *
     * @param type
     *            the certificate type.
     * @param provider
     *            the name of the provider providing certificates of the
     *            specified type.
     * @return the new {@code CertificateFactory} instance.
     * @throws CertificateException
     *             if the specified certificate type is not available at the
     *             specified provider.
     * @throws IllegalArgumentException
     *             if the specified provider is {@code null}.
     * @throws NullPointerException if {@code type == null}
     * @throws IllegalArgumentException if {@code provider == null}
     */
    public static final CertificateFactory getInstance(String type,
            Provider provider) throws CertificateException {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null");
        }
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        try {
            Object spi = ENGINE.getInstance(type, provider, null);
            return new CertificateFactory((CertificateFactorySpi) spi, provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(e);
        }
    }

    /**
     * Returns the {@code Provider} of the certificate factory represented by
     * the certificate.
     *
     * @return the provider of this certificate factory.
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Returns the Certificate type.
     *
     * @return type of certificate being used.
     */
    public final String getType() {
        return type;
    }

    /**
     * Generates and initializes a {@code Certificate} from the provided input
     * stream.
     *
     * @param inStream
     *            the stream from where data is read to create the {@code
     *            Certificate}.
     * @return an initialized Certificate.
     * @throws CertificateException
     *             if parsing problems are detected.
     */
    public final Certificate generateCertificate(InputStream inStream)
            throws CertificateException {
        return spiImpl.engineGenerateCertificate(inStream);
    }

    /**
     * Returns an {@code Iterator} over the supported {@code CertPath} encodings
     * (as Strings). The first element is the default encoding scheme to apply.
     *
     * @return an iterator over supported {@link CertPath} encodings (as
     *         Strings).
     */
    public final Iterator<String> getCertPathEncodings() {
        return spiImpl.engineGetCertPathEncodings();
    }

    /**
     * Generates a {@code CertPath} (a certificate chain) from the provided
     * {@code InputStream}. The default encoding scheme is applied.
     *
     * @param inStream
     *            {@code InputStream} with encoded data.
     * @return a {@code CertPath} initialized from the provided data.
     * @throws CertificateException
     *             if parsing problems are detected.
     */
    public final CertPath generateCertPath(InputStream inStream) throws CertificateException {
        Iterator<String> it = getCertPathEncodings();
        if (!it.hasNext()) {
            throw new CertificateException("There are no CertPath encodings");
        }
        return spiImpl.engineGenerateCertPath(inStream, it.next());
    }

    /**
     * Generates a {@code CertPath} (a certificate chain) from the given
     * {@code inputStream}, assuming the given {@code encoding} from
     * {@link #getCertPathEncodings()}.
     *
     * @throws CertificateException
     *             if parsing problems are detected.
     * @throws UnsupportedOperationException
     *             if the provider does not implement this method.
     */
    public final CertPath generateCertPath(InputStream inputStream, String encoding)
            throws CertificateException {
        return spiImpl.engineGenerateCertPath(inputStream, encoding);
    }

    /**
     * Generates a {@code CertPath} from the provided list of certificates. The
     * encoding is the default encoding.
     *
     * @param certificates
     *            the list containing certificates in a format supported by the
     *            {@code CertificateFactory}.
     * @return a {@code CertPath} initialized from the provided data.
     * @throws CertificateException
     *             if parsing problems are detected.
     * @throws UnsupportedOperationException
     *             if the provider does not implement this method.
     */
    public final CertPath generateCertPath(List<? extends Certificate> certificates)
            throws CertificateException {
        return spiImpl.engineGenerateCertPath(certificates);
    }

    /**
     * Generates and initializes a collection of (unrelated) certificates from
     * the provided input stream.
     *
     * @param inStream
     *            the stream from which the data is read to create the
     *            collection.
     * @return an initialized collection of certificates.
     * @throws CertificateException
     *             if parsing problems are detected.
     */
    public final Collection<? extends Certificate> generateCertificates(InputStream inStream)
            throws CertificateException {
        return spiImpl.engineGenerateCertificates(inStream);
    }

    /**
     * Generates and initializes a <i>Certificate Revocation List</i> (CRL) from
     * the provided input stream.
     *
     * @param inStream
     *            the stream from where data is read to create the CRL.
     * @return an initialized CRL.
     * @throws CRLException
     *                if parsing problems are detected.
     */
    public final CRL generateCRL(InputStream inStream) throws CRLException {
        return spiImpl.engineGenerateCRL(inStream);
    }

    /**
     * Generates and initializes a collection of <i>Certificate Revocation
     * List</i> (CRL) from the provided input stream.
     *
     * @param inStream
     *            the stream from which the data is read to create the CRLs.
     * @return an initialized collection of CRLs.
     * @throws CRLException
     *                if parsing problems are detected.
     */
    public final Collection<? extends CRL> generateCRLs(InputStream inStream)
            throws CRLException {
        return spiImpl.engineGenerateCRLs(inStream);
    }
}
