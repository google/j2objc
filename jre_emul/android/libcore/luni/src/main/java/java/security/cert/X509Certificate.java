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

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.security.auth.x500.X500Principal;

/**
 * Abstract base class for X.509 certificates.
 * <p>
 * This represents a standard way for accessing the attributes of X.509
 * certificates.
 * <p>
 * The basic X.509 v3 format described in ASN.1:
 *
 * <pre>
 * Certificate  ::=  SEQUENCE  {
 *     tbsCertificate       TBSCertificate,
 *     signatureAlgorithm   AlgorithmIdentifier,
 *     signature            BIT STRING  }
 *
 * TBSCertificate  ::=  SEQUENCE  {
 *      version         [0]  EXPLICIT Version DEFAULT v1,
 *      serialNumber         CertificateSerialNumber,
 *      signature            AlgorithmIdentifier,
 *      issuer               Name,
 *      validity             Validity,
 *      subject              Name,
 *      subjectPublicKeyInfo SubjectPublicKeyInfo,
 *      issuerUniqueID  [1]  IMPLICIT UniqueIdentifier OPTIONAL,
 *                           -- If present, version must be v2 or v3
 *      subjectUniqueID [2]  IMPLICIT UniqueIdentifier OPTIONAL,
 *                           -- If present, version must be v2 or v3
 *      extensions      [3]  EXPLICIT Extensions OPTIONAL
 *                           -- If present, version must be v3
 *      }
 * </pre>
 * <p>
 * For more information consult RFC 2459
 * "Internet X.509 Public Key Infrastructure Certificate and CRL Profile" at <a
 * href
 * ="http://www.ietf.org/rfc/rfc2459.txt">http://www.ietf.org/rfc/rfc2459.txt
 * </a> .
 */
public abstract class X509Certificate
        extends Certificate implements X509Extension {

    private static final long serialVersionUID = -2491127588187038216L;

    /**
     * Creates a new {@code X509Certificate}.
     */
    protected X509Certificate() {
        super("X.509");
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
     * <b>replaced by:</b> {@link #getIssuerX500Principal()}.
     *
     * @return the {@code issuer} as an implementation specific {@code
     *         Principal}.
     */
    public abstract Principal getIssuerDN() ;

    /**
     * Returns the {@code issuer} (issuer distinguished name) as an {@code
     * X500Principal}.
     *
     * @return the {@code issuer} (issuer distinguished name).
     */
    public X500Principal getIssuerX500Principal() {

        try {
            // TODO if there is no X.509 certificate provider installed
            // should we try to access Harmony X509CertImpl via classForName?
            CertificateFactory factory = CertificateFactory
                    .getInstance("X.509");

            X509Certificate cert = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(getEncoded()));

            return cert.getIssuerX500Principal();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get X500Principal issuer", e);
        }
    }

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
     * <p>
     * <b>replaced by:</b> {@link #getSubjectX500Principal()}.
     *
     * @return the {@code subject} (subject distinguished name).
     */
    public abstract Principal getSubjectDN();

    /**
     * Returns the {@code subject} (subject distinguished name) as an {@code
     * X500Principal}.
     *
     * @return the {@code subject} (subject distinguished name)
     */
    public X500Principal getSubjectX500Principal() {

        try {
            // TODO if there is no X.509 certificate provider installed
            // should we try to access Harmony X509CertImpl via classForName?
            CertificateFactory factory = CertificateFactory
                    .getInstance("X.509");

            X509Certificate cert = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(getEncoded()));

            return cert.getSubjectX500Principal();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get X500Principal subject", e);
        }
    }

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
     * Returns the {@code tbsCertificate} information from this certificate in
     * DER-encoded format.
     *
     * @return the DER-encoded certificate information.
     * @throws CertificateEncodingException
     *             if an error occurs in encoding
     */
    public abstract byte[] getTBSCertificate()
                                    throws CertificateEncodingException;

    /**
     * Returns the raw signature bits from the certificate.
     *
     * @return the raw signature bits from the certificate.
     */
    public abstract byte[] getSignature();

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
     * @return the parameters of the signature algorithm, or {@code null} if
     *         none are used.
     */
    public abstract byte[] getSigAlgParams();

    /**
     * Returns the {@code issuerUniqueID} from the certificate.
     *
     * @return the {@code issuerUniqueID} or {@code null} if there's none in the
     *         certificate.
     */
    public abstract boolean[] getIssuerUniqueID();

    /**
     * Returns the {@code subjectUniqueID} from the certificate.
     *
     * @return the {@code subjectUniqueID} or null if there's none in the
     *         certificate.
     */
    public abstract boolean[] getSubjectUniqueID();

    /**
     * Returns the {@code KeyUsage} extension as a {@code boolean} array.
     * <p>
     * The ASN.1 definition of {@code KeyUsage}:
     *
     * <pre>
     * KeyUsage ::= BIT STRING {
     *      digitalSignature        (0),
     *      nonRepudiation          (1),
     *      keyEncipherment         (2),
     *      dataEncipherment        (3),
     *      keyAgreement            (4),
     *      keyCertSign             (5),
     *      cRLSign                 (6),
     *      encipherOnly            (7),
     *      decipherOnly            (8) }
     *
     * </pre>
     *
     * @return the {@code KeyUsage} extension or {@code null} if there's none in
     *         the certificate.
     */
    public abstract boolean[] getKeyUsage();

    /**
     * Returns a read-only list of OID strings representing the {@code
     * ExtKeyUsageSyntax} field of the extended key usage extension.
     *
     * @return the extended key usage extension, or {@code null} if there's none
     *         in the certificate.
     * @throws CertificateParsingException
     *             if the extension decoding fails.
     */
    public List<String> getExtendedKeyUsage()
                        throws CertificateParsingException {
        return null;
    }

    /**
     * Returns the path length of the certificate constraints from the {@code
     * BasicContraints} extension.
     *
     * If the certificate has no basic constraints or is not a
     * certificate authority, {@code -1} is returned. If the
     * certificate is a certificate authority without a path length,
     * {@code Integer.MAX_VALUE} is returned. Otherwise, the
     * certificate authority's path length is returned.
     */
    public abstract int getBasicConstraints();

    /**
     * Returns a read-only list of the subject alternative names from the
     * {@code SubjectAltName} extension.
     * <p>
     * The ASN.1 definition of {@code SubjectAltName}:
     *
     * <pre>
     * SubjectAltName ::= GeneralNames
     *
     * GeneralNames ::= SEQUENCE SIZE (1..MAX) OF GeneralName
     *
     * GeneralName ::= CHOICE {
     *      otherName                       [0]     AnotherName,
     *      rfc822Name                      [1]     IA5String,
     *      dNSName                         [2]     IA5String,
     *      x400Address                     [3]     ORAddress,
     *      directoryName                   [4]     Name,
     *      ediPartyName                    [5]     EDIPartyName,
     *      uniformResourceIdentifier       [6]     IA5String,
     *      iPAddress                       [7]     OCTET STRING,
     *      registeredID                    [8]     OBJECT IDENTIFIER }
     *
     * </pre>
     *
     * @return the subject alternative names or {@code null} if there are none
     *         in the certificate.
     * @throws CertificateParsingException
     *             if decoding of the extension fails.
     */
    public Collection<List<?>> getSubjectAlternativeNames()
                                    throws CertificateParsingException {
        return null;
    }

    /**
     * Returns a read-only list of the issuer alternative names from the {@code
     * IssuerAltName} extension.
     * <p>
     * The ASN.1 definition of {@code IssuerAltName}:
     *
     * <pre>
     * IssuerAltName ::= GeneralNames
     *
     * GeneralNames ::= SEQUENCE SIZE (1..MAX) OF GeneralName
     *
     * GeneralName ::= CHOICE {
     *      otherName                       [0]     AnotherName,
     *      rfc822Name                      [1]     IA5String,
     *      dNSName                         [2]     IA5String,
     *      x400Address                     [3]     ORAddress,
     *      directoryName                   [4]     Name,
     *      ediPartyName                    [5]     EDIPartyName,
     *      uniformResourceIdentifier       [6]     IA5String,
     *      iPAddress                       [7]     OCTET STRING,
     *      registeredID                    [8]     OBJECT IDENTIFIER }
     *
     * </pre>
     *
     * @return the issuer alternative names of {@code null} if there are none in
     *         the certificate.
     * @throws CertificateParsingException
     *             if decoding of the extension fails.
     */
    public Collection<List<?>> getIssuerAlternativeNames()
                                    throws CertificateParsingException {
        return null;
    }
}
