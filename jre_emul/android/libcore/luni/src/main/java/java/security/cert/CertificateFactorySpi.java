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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class defines the <i>Service Provider Interface</i> (<b>SPI</b>) for the
 * {@code CertificateFactory} class. This SPI must be implemented for each
 * certificate type a security provider wishes to support.
 */

public abstract class CertificateFactorySpi {

    /**
     * Constructs a new instance of this class.
     */
    public CertificateFactorySpi() {
    }

    /**
     * Generates and initializes a {@code Certificate} from the provided input
     * stream.
     *
     * @param inStream
     *            the stream from which the data is read to create the
     *            certificate.
     * @return an initialized certificate.
     * @throws CertificateException
     *                if parsing problems are detected.
     */
    public abstract Certificate engineGenerateCertificate(InputStream inStream)
            throws CertificateException;

    /**
     * Generates and initializes a collection of certificates from the provided
     * input stream.
     *
     * @param inStream
     *            the stream from where data is read to create the certificates.
     * @return a collection of certificates.
     * @throws CertificateException
     *                if parsing problems are detected.
     */
    public abstract Collection<? extends Certificate>
        engineGenerateCertificates(InputStream inStream) throws CertificateException;

    /**
     * Generates and initializes a <i>Certificate Revocation List</i> (CRL) from
     * the provided input stream.
     *
     * @param inStream
     *            the stream from where data is read to create the CRL.
     * @return an CRL instance.
     * @throws CRLException
     *                if parsing problems are detected.
     */
    public abstract CRL engineGenerateCRL(InputStream inStream)
            throws CRLException;

    /**
     * Generates and initializes a collection of <i>Certificate Revocation
     * List</i> (CRL) from the provided input stream.
     *
     * @param inStream
     *            the stream from which the data is read to create the CRLs.
     * @return a collection of CRLs.
     * @throws CRLException
     *                if parsing problems are detected.
     */
    public abstract Collection<? extends CRL>
        engineGenerateCRLs(InputStream inStream) throws CRLException;

    /**
     * Generates a {@code CertPath} from the provided {@code InputStream}. The
     * default encoding scheme is applied.
     *
     * @param inStream
     *            an input stream with encoded data.
     * @return a {@code CertPath} initialized from the provided data.
     * @throws CertificateException
     *             if parsing problems are detected.
     */
    public CertPath engineGenerateCertPath(InputStream inStream)
            throws CertificateException {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a {@code CertPath} (a certificate chain) from the given
     * {@code inputStream}, assuming the given {@code encoding} from
     * {@link #engineGetCertPathEncodings()}.
     *
     * @throws CertificateException
     *             if parsing problems are detected.
     * @throws UnsupportedOperationException
     *             if the provider does not implement this method.
     */
    public CertPath engineGenerateCertPath(InputStream inStream, String encoding)
            throws CertificateException {
        throw new UnsupportedOperationException();
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
    public CertPath engineGenerateCertPath(List<? extends Certificate>  certificates)
            throws CertificateException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an {@code Iterator} over the supported {@code CertPath} encodings
     * (as Strings). The first element is the default encoding.
     *
     * @return an iterator over supported {@code CertPath} encodings (as
     *         Strings).
     */
    public Iterator<String> engineGetCertPathEncodings() {
        throw new UnsupportedOperationException();
    }
}
