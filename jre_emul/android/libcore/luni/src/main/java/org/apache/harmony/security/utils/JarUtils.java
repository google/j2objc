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

/**
* @author Boris Kuznetsov
* @version $Revision$
*/
package org.apache.harmony.security.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.pkcs7.ContentInfo;
import org.apache.harmony.security.pkcs7.SignedData;
import org.apache.harmony.security.pkcs7.SignerInfo;
import org.apache.harmony.security.x501.AttributeTypeAndValue;

public class JarUtils {

    // as defined in PKCS #9: Selected Attribute Types:
    // http://www.ietf.org/rfc/rfc2985.txt
    private static final int[] MESSAGE_DIGEST_OID =
        new int[] {1, 2, 840, 113549, 1, 9, 4};

    /**
     * This method handle all the work with  PKCS7, ASN1 encoding, signature verifying,
     * and certification path building.
     * See also PKCS #7: Cryptographic Message Syntax Standard:
     * http://www.ietf.org/rfc/rfc2315.txt
     * @param signature - the input stream of signature file to be verified
     * @param signatureBlock - the input stream of corresponding signature block file
     * @return array of certificates used to verify the signature file
     * @throws IOException - if some errors occurs during reading from the stream
     * @throws GeneralSecurityException - if signature verification process fails
     */
    public static Certificate[] verifySignature(InputStream signature, InputStream
            signatureBlock) throws IOException, GeneralSecurityException {

        BerInputStream bis = new BerInputStream(signatureBlock);
        ContentInfo info = (ContentInfo)ContentInfo.ASN1.decode(bis);
        SignedData signedData = info.getSignedData();
        if (signedData == null) {
            throw new IOException("No SignedData found");
        }
        Collection<org.apache.harmony.security.x509.Certificate> encCerts
                = signedData.getCertificates();
        if (encCerts.isEmpty()) {
            return null;
        }
        X509Certificate[] certs = new X509Certificate[encCerts.size()];
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        int i = 0;
        for (org.apache.harmony.security.x509.Certificate encCert : encCerts) {
            final byte[] encoded = encCert.getEncoded();
            final InputStream is = new ByteArrayInputStream(encoded);
            certs[i++] = new VerbatimX509Certificate((X509Certificate) cf.generateCertificate(is),
                    encoded);
        }

        List<SignerInfo> sigInfos = signedData.getSignerInfos();
        SignerInfo sigInfo;
        if (!sigInfos.isEmpty()) {
            sigInfo = sigInfos.get(0);
        } else {
            return null;
        }

        // Issuer
        X500Principal issuer = sigInfo.getIssuer();

        // Certificate serial number
        BigInteger snum = sigInfo.getSerialNumber();

        // Locate the certificate
        int issuerSertIndex = 0;
        for (i = 0; i < certs.length; i++) {
            if (issuer.equals(certs[i].getIssuerDN()) &&
                    snum.equals(certs[i].getSerialNumber())) {
                issuerSertIndex = i;
                break;
            }
        }
        if (i == certs.length) { // No issuer certificate found
            return null;
        }

        if (certs[issuerSertIndex].hasUnsupportedCriticalExtension()) {
            throw new SecurityException("Can not recognize a critical extension");
        }

        // Get Signature instance
        final String daOid = sigInfo.getDigestAlgorithm();
        final String daName = sigInfo.getDigestAlgorithmName();
        final String deaOid = sigInfo.getDigestEncryptionAlgorithm();
        final String deaName = sigInfo.getDigestEncryptionAlgorithmName();

        String alg = null;
        Signature sig = null;

        if (daOid != null && deaOid != null) {
            alg = daOid + "with" + deaOid;
            try {
                sig = Signature.getInstance(alg);
            } catch (NoSuchAlgorithmException e) {
            }

            // Try to convert to names instead of OID.
            if (sig == null && daName != null && deaName != null) {
                alg = daName + "with" + deaName;
                try {
                    sig = Signature.getInstance(alg);
                } catch (NoSuchAlgorithmException e) {
                }
            }
        }

        if (sig == null && deaOid != null) {
            alg = deaOid;
            try {
                sig = Signature.getInstance(alg);
            } catch (NoSuchAlgorithmException e) {
            }

            if (sig == null) {
                alg = deaName;
                try {
                    sig = Signature.getInstance(alg);
                } catch (NoSuchAlgorithmException e) {
                }
            }
        }

        // We couldn't find a valid Signature type.
        if (sig == null) {
            return null;
        }

        sig.initVerify(certs[issuerSertIndex]);

        // If the authenticatedAttributes field of SignerInfo contains more than zero attributes,
        // compute the message digest on the ASN.1 DER encoding of the Attributes value.
        // Otherwise, compute the message digest on the data.
        List<AttributeTypeAndValue> atr = sigInfo.getAuthenticatedAttributes();

        byte[] sfBytes = new byte[signature.available()];
        signature.read(sfBytes);

        if (atr == null) {
            sig.update(sfBytes);
        } else {
            sig.update(sigInfo.getEncodedAuthenticatedAttributes());

            // If the authenticatedAttributes field contains the message-digest attribute,
            // verify that it equals the computed digest of the signature file
            byte[] existingDigest = null;
            for (AttributeTypeAndValue a : atr) {
                if (Arrays.equals(a.getType().getOid(), MESSAGE_DIGEST_OID)) {
                    if (existingDigest != null) {
                        throw new SecurityException("Too many MessageDigest attributes");
                    }
                    Collection<?> entries = a.getValue().getValues(ASN1OctetString.getInstance());
                    if (entries.size() != 1) {
                        throw new SecurityException("Too many values for MessageDigest attribute");
                    }
                    existingDigest = (byte[]) entries.iterator().next();
                }
            }

            // RFC 2315 section 9.1: if authenticatedAttributes is present, it
            // must have a message-digest attribute.
            if (existingDigest == null) {
                throw new SecurityException("Missing MessageDigest in Authenticated Attributes");
            }

            MessageDigest md = null;
            if (daOid != null) {
                md = MessageDigest.getInstance(daOid);
            }
            if (md == null && daName != null) {
                md = MessageDigest.getInstance(daName);
            }
            if (md == null) {
                return null;
            }

            byte[] computedDigest = md.digest(sfBytes);
            if (!Arrays.equals(existingDigest, computedDigest)) {
                throw new SecurityException("Incorrect MD");
            }
        }

        if (!sig.verify(sigInfo.getEncryptedDigest())) {
            throw new SecurityException("Incorrect signature");
        }

        return createChain(certs[issuerSertIndex], certs);
    }

    private static X509Certificate[] createChain(X509Certificate signer,
            X509Certificate[] candidates) {
        Principal issuer = signer.getIssuerDN();

        // Signer is self-signed
        if (signer.getSubjectDN().equals(issuer)) {
            return new X509Certificate[] { signer };
        }

        ArrayList<X509Certificate> chain = new ArrayList<X509Certificate>(candidates.length + 1);
        chain.add(0, signer);

        X509Certificate issuerCert;
        int count = 1;
        while (true) {
            issuerCert = findCert(issuer, candidates);
            if (issuerCert == null) {
                break;
            }
            chain.add(issuerCert);
            count++;
            /* Prevent growing infinitely if there is a loop */
            if (count > candidates.length) {
                break;
            }
            issuer = issuerCert.getIssuerDN();
            if (issuerCert.getSubjectDN().equals(issuer)) {
                break;
            }
        }
        return chain.toArray(new X509Certificate[count]);
    }

    private static X509Certificate findCert(Principal issuer, X509Certificate[] candidates) {
        for (int i = 0; i < candidates.length; i++) {
            if (issuer.equals(candidates[i].getSubjectDN())) {
                return candidates[i];
            }
        }
        return null;
    }

    /**
     * For legacy reasons we need to return exactly the original encoded
     * certificate bytes, instead of letting the underlying implementation have
     * a shot at re-encoding the data.
     */
    private static class VerbatimX509Certificate extends WrappedX509Certificate {
        private byte[] encodedVerbatim;

        public VerbatimX509Certificate(X509Certificate wrapped, byte[] encodedVerbatim) {
            super(wrapped);
            this.encodedVerbatim = encodedVerbatim;
        }

        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            return encodedVerbatim;
        }
    }
}
