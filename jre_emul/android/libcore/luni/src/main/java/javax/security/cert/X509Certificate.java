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

package javax.security.cert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.util.Date;

/**
 * Abstract base class for X.509 certificates.
 * <p>
 * This represents a standard way for accessing the attributes of X.509 v1
 * certificates.
 * <p>
 * Note: This package is provided only for compatibility reasons.
 * It contains a simplified version of the java.security.cert package that was
 * previously used by JSSE (Java SSL package). All applications that do not have
 * to be compatible with older versions of JSSE (that is before Java SDK 1.5)
 * should only use java.security.cert.
 */
public abstract class X509Certificate extends Certificate {

    private static Constructor constructor;
    static {
        try {
            String classname = Security.getProperty("cert.provider.x509v1");
            Class cl = Class.forName(classname);
            constructor = cl.getConstructor(new Class[] {InputStream.class});
        } catch (Throwable e) {
        }
    }

    /**
     * Creates a new {@code X509Certificate}.
     */
    public X509Certificate() {
    }

    /**
     * Creates a new {@code X509Certificate} and initializes it from the
     * specified input stream.
     *
     * @param inStream
     *            input stream containing data to initialize the certificate.
     * @return the certificate initialized from the specified input stream
     * @throws CertificateException
     *             if the certificate cannot be created or initialized.
     */
    public static final X509Certificate getInstance(InputStream inStream)
                                             throws CertificateException {
        if (inStream == null) {
            throw new CertificateException("inStream == null");
        }
        if (constructor != null) {
            try {
                return (X509Certificate)
                    constructor.newInstance(new Object[] {inStream});
            } catch (Throwable e) {
                throw new CertificateException(e.getMessage());
            }
        }

        final java.security.cert.X509Certificate cert;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (java.security.cert.X509Certificate)
                                            cf.generateCertificate(inStream);
        } catch (java.security.cert.CertificateException e) {
            throw new CertificateException(e.getMessage());
        }

        return new X509Certificate() {

            public byte[] getEncoded() throws CertificateEncodingException {
                try {
                    return cert.getEncoded();
                } catch (java.security.cert.CertificateEncodingException e) {
                    throw new CertificateEncodingException(e.getMessage());
                }
            }

            public void verify(PublicKey key) throws CertificateException,
                                NoSuchAlgorithmException, InvalidKeyException,
                                NoSuchProviderException, SignatureException {
                try {
                    cert.verify(key);
                } catch (java.security.cert.CertificateException e) {
                    throw new CertificateException(e.getMessage());
                }
            }

            public void verify(PublicKey key, String sigProvider)
                            throws CertificateException,
                                NoSuchAlgorithmException, InvalidKeyException,
                                NoSuchProviderException, SignatureException {
                try {
                    cert.verify(key, sigProvider);
                } catch (java.security.cert.CertificateException e) {
                    throw new CertificateException(e.getMessage());
                }
            }

            public String toString() {
                return cert.toString();
            }

            public PublicKey getPublicKey() {
                return cert.getPublicKey();
            }

            public void checkValidity() throws CertificateExpiredException,
                                   CertificateNotYetValidException {
                try {
                    cert.checkValidity();
                } catch (java.security.cert.CertificateNotYetValidException e) {
                    throw new CertificateNotYetValidException(e.getMessage());
                } catch (java.security.cert.CertificateExpiredException e) {
                    throw new CertificateExpiredException(e.getMessage());
                }
            }

            public void checkValidity(Date date)
                            throws CertificateExpiredException,
                                   CertificateNotYetValidException {
                try {
                    cert.checkValidity(date);
                } catch (java.security.cert.CertificateNotYetValidException e) {
                    throw new CertificateNotYetValidException(e.getMessage());
                } catch (java.security.cert.CertificateExpiredException e) {
                    throw new CertificateExpiredException(e.getMessage());
                }
            }

            public int getVersion() {
                return 2;
            }

            public BigInteger getSerialNumber() {
                return cert.getSerialNumber();
            }

            public Principal getIssuerDN() {
                return cert.getIssuerDN();
            }

            public Principal getSubjectDN() {
                return cert.getSubjectDN();
            }

            public Date getNotBefore() {
                return cert.getNotBefore();
            }

            public Date getNotAfter() {
                return cert.getNotAfter();
            }

            public String getSigAlgName() {
                return cert.getSigAlgName();
            }

            public String getSigAlgOID() {
                return cert.getSigAlgOID();
            }

            public byte[] getSigAlgParams() {
                return cert.getSigAlgParams();
            }
        };
    }

    /**
     * Creates a new {@code X509Certificate} and initializes it from the
     * specified byte array.
     *
     * @param certData
     *            byte array containing data to initialize the certificate.
     * @return the certificate initialized from the specified byte array
     * @throws CertificateException
     *             if the certificate cannot be created or initialized.
     */
    public static final X509Certificate getInstance(byte[] certData)
                                             throws CertificateException {
        if (certData == null) {
            throw new CertificateException("certData == null");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(certData);
        return getInstance(bais);
    }

    /**
     * Checks whether the certificate is currently valid.
     * <p>
     * The validity defined in ASN.1:
     *
     * <pre>
     * validity             Validity
     *
     * Validity ::= SEQUENCE {
     *      notBefore       CertificateValidityDate,
     *      notAfter        CertificateValidityDate }
     *
     * CertificateValidityDate ::= CHOICE {
     *      utcTime         UTCTime,
     *      generalTime     GeneralizedTime }
     * </pre>
     *
     * @throws CertificateExpiredException
     *             if the certificate has expired.
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid.
     */
    public abstract void checkValidity()
            throws CertificateExpiredException, CertificateNotYetValidException;


    /**
     * Checks whether the certificate is valid at the specified date.
     *
     * @param date
     *            the date to check the validity against.
     * @throws CertificateExpiredException
     *             if the certificate has expired.
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid.
     * @see #checkValidity()
     */
    public abstract void checkValidity(Date date)
            throws CertificateExpiredException, CertificateNotYetValidException;

    /**
     * Returns the certificates {@code version} (version number).
     * <p>
     * The version defined is ASN.1:
     *
     * <pre>
     * Version ::=  INTEGER  {  v1(0), v2(1), v3(2)  }
     * </pre>
     *
     * @return the version number.
     */
    public abstract int getVersion();

    /**
     * Returns the {@code serialNumber} of the certificate.
     * <p>
     * The ASN.1 definition of {@code serialNumber}:
     *
     * <pre>
     * CertificateSerialNumber  ::=  INTEGER
     * </pre>
     *
     * @return the serial number.
     */
    public abstract BigInteger getSerialNumber();

    /**
     * Returns the {@code issuer} (issuer distinguished name) as an
     * implementation specific {@code Principal} object.
     * <p>
     * The ASN.1 definition of {@code issuer}:
     *
     * <pre>
     *  issuer      Name
     *
     *  Name ::= CHOICE {
     *      RDNSequence }
     *
     *    RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     *
     *    RelativeDistinguishedName ::= SET OF AttributeTypeAndValue
     *
     *    AttributeTypeAndValue ::= SEQUENCE {
     *      type     AttributeType,
     *      value    AttributeValue }
     *
     *    AttributeType ::= OBJECT IDENTIFIER
     *
     *    AttributeValue ::= ANY DEFINED BY AttributeType
     * </pre>
     *
     * @return the {@code issuer} as an implementation specific {@code
     *         Principal}.
     */
    public abstract Principal getIssuerDN();

    /**
     * Returns the {@code subject} (subject distinguished name) as an
     * implementation specific {@code Principal} object.
     * <p>
     * The ASN.1 definition of {@code subject}:
     *
     * <pre>
     * subject      Name
     *
     *  Name ::= CHOICE {
     *      RDNSequence }
     *
     *    RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     *
     *    RelativeDistinguishedName ::= SET OF AttributeTypeAndValue
     *
     *    AttributeTypeAndValue ::= SEQUENCE {
     *      type     AttributeType,
     *      value    AttributeValue }
     *
     *    AttributeType ::= OBJECT IDENTIFIER
     *
     *    AttributeValue ::= ANY DEFINED BY AttributeType
     * </pre>
     *
     * @return the {@code subject} (subject distinguished name).
     */
    public abstract Principal getSubjectDN();

    /**
     * Returns the {@code notBefore} date from the validity period of the
     * certificate.
     *
     * @return the start of the validity period.
     */
    public abstract Date getNotBefore();

    /**
     * Returns the {@code notAfter} date of the validity period of the
     * certificate.
     *
     * @return the end of the validity period.
     */
    public abstract Date getNotAfter();

    /**
     * Returns the name of the algorithm for the certificate signature.
     *
     * @return the signature algorithm name.
     */
    public abstract String getSigAlgName();

    /**
     * Returns the OID of the signature algorithm from the certificate.
     *
     * @return the OID of the signature algorithm.
     */
    public abstract String getSigAlgOID();

    /**
     * Returns the parameters of the signature algorithm in DER-encoded format.
     *
     * @return the parameters of the signature algorithm, or null if none are
     *         used.
     */
    public abstract byte[] getSigAlgParams();
}
